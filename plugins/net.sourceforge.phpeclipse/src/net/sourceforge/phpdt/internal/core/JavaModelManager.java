/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.phpdt.internal.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.zip.ZipFile;

import net.sourceforge.phpdt.core.ElementChangedEvent;
import net.sourceforge.phpdt.core.IClasspathEntry;
import net.sourceforge.phpdt.core.ICompilationUnit;
import net.sourceforge.phpdt.core.IElementChangedListener;
import net.sourceforge.phpdt.core.IJavaElement;
import net.sourceforge.phpdt.core.IJavaElementDelta;
import net.sourceforge.phpdt.core.IJavaModel;
import net.sourceforge.phpdt.core.IJavaProject;
import net.sourceforge.phpdt.core.IPackageFragment;
import net.sourceforge.phpdt.core.IPackageFragmentRoot;
import net.sourceforge.phpdt.core.IParent;
import net.sourceforge.phpdt.core.IProblemRequestor;
import net.sourceforge.phpdt.core.IWorkingCopy;
import net.sourceforge.phpdt.core.JavaCore;
import net.sourceforge.phpdt.core.JavaModelException;
import net.sourceforge.phpdt.core.WorkingCopyOwner;
import net.sourceforge.phpdt.core.compiler.IProblem;
import net.sourceforge.phpdt.internal.core.builder.PHPBuilder;
import net.sourceforge.phpdt.internal.core.util.Util;
import net.sourceforge.phpdt.internal.ui.util.PHPFileUtil;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;

/**
 * The <code>JavaModelManager</code> manages instances of
 * <code>IJavaModel</code>. <code>IElementChangedListener</code>s register
 * with the <code>JavaModelManager</code>, and receive
 * <code>ElementChangedEvent</code>s for all <code>IJavaModel</code>s.
 * <p>
 * The single instance of <code>JavaModelManager</code> is available from the
 * static method <code>JavaModelManager.getJavaModelManager()</code>.
 */
public class JavaModelManager implements ISaveParticipant {
	/**
	 * Unique handle onto the JavaModel
	 */
	final JavaModel javaModel = new JavaModel();

	// public IndexManager indexManager = new IndexManager();
	/**
	 * Classpath variables pool
	 */
	public static HashMap Variables = new HashMap(5);

	public static HashMap PreviousSessionVariables = new HashMap(5);

	public static HashSet OptionNames = new HashSet(20);

	public final static String CP_VARIABLE_PREFERENCES_PREFIX = PHPeclipsePlugin.PLUGIN_ID
			+ ".classpathVariable."; //$NON-NLS-1$

	public final static String CP_CONTAINER_PREFERENCES_PREFIX = PHPeclipsePlugin.PLUGIN_ID
			+ ".classpathContainer."; //$NON-NLS-1$

	public final static String CP_ENTRY_IGNORE = "##<cp entry ignore>##"; //$NON-NLS-1$

	/**
	 * Classpath containers pool
	 */
	public static HashMap containers = new HashMap(5);

	public static HashMap PreviousSessionContainers = new HashMap(5);

	/**
	 * Name of the extension point for contributing classpath variable
	 * initializers
	 */
	// public static final String CPVARIABLE_INITIALIZER_EXTPOINT_ID =
	// "classpathVariableInitializer" ; //$NON-NLS-1$
	/**
	 * Name of the extension point for contributing classpath container
	 * initializers
	 */
	// public static final String CPCONTAINER_INITIALIZER_EXTPOINT_ID =
	// "classpathContainerInitializer" ; //$NON-NLS-1$
	/**
	 * Name of the extension point for contributing a source code formatter
	 */
	public static final String FORMATTER_EXTPOINT_ID = "codeFormatter"; // $/**

	/**
	 * Value of the content-type for Java source files
	 */
	public static final String JAVA_SOURCE_CONTENT_TYPE = PHPeclipsePlugin.PLUGIN_ID
			+ ".phpSource"; //$NON-NLS-1$NON-NLS-1$

	/**
	 * Special value used for recognizing ongoing initialization and breaking
	 * initialization cycles
	 */
	public final static IPath VariableInitializationInProgress = new Path(
			"Variable Initialization In Progress"); //$NON-NLS-1$
	// public final static IClasspathContainer ContainerInitializationInProgress
	// = new IClasspathContainer() {
	// public IClasspathEntry[] getClasspathEntries() { return null; }
	// public String getDescription() { return "Container Initialization In
	// Progress"; } //$NON-NLS-1$
	// public int getKind() { return 0; }
	// public IPath getPath() { return null; }
	// public String toString() { return getDescription(); }
	// };

	private static final String INDEX_MANAGER_DEBUG = PHPeclipsePlugin.PLUGIN_ID
			+ "/debug/indexmanager"; //$NON-NLS-1$

	private static final String COMPILER_DEBUG = PHPeclipsePlugin.PLUGIN_ID
			+ "/debug/compiler"; //$NON-NLS-1$

	private static final String JAVAMODEL_DEBUG = PHPeclipsePlugin.PLUGIN_ID
			+ "/debug/javamodel"; //$NON-NLS-1$

	private static final String CP_RESOLVE_DEBUG = PHPeclipsePlugin.PLUGIN_ID
			+ "/debug/cpresolution"; //$NON-NLS-1$

	private static final String ZIP_ACCESS_DEBUG = PHPeclipsePlugin.PLUGIN_ID
			+ "/debug/zipaccess"; //$NON-NLS-1$

	private static final String DELTA_DEBUG = PHPeclipsePlugin.PLUGIN_ID
			+ "/debug/javadelta"; //$NON-NLS-1$

	private static final String HIERARCHY_DEBUG = PHPeclipsePlugin.PLUGIN_ID
			+ "/debug/hierarchy"; //$NON-NLS-1$

	private static final String POST_ACTION_DEBUG = PHPeclipsePlugin.PLUGIN_ID
			+ "/debug/postaction"; //$NON-NLS-1$

	private static final String BUILDER_DEBUG = PHPeclipsePlugin.PLUGIN_ID
			+ "/debug/builder"; //$NON-NLS-1$

	private static final String COMPLETION_DEBUG = PHPeclipsePlugin.PLUGIN_ID
			+ "/debug/completion"; //$NON-NLS-1$

	private static final String SELECTION_DEBUG = PHPeclipsePlugin.PLUGIN_ID
			+ "/debug/selection"; //$NON-NLS-1$

	private static final String SHARED_WC_DEBUG = PHPeclipsePlugin.PLUGIN_ID
			+ "/debug/sharedworkingcopy"; //$NON-NLS-1$

	private static final String SEARCH_DEBUG = PHPeclipsePlugin.PLUGIN_ID
			+ "/debug/search"; //$NON-NLS-1$

	public final static IWorkingCopy[] NoWorkingCopy = new IWorkingCopy[0];

	/**
	 * Table from WorkingCopyOwner to a table of ICompilationUnit (working copy
	 * handle) to PerWorkingCopyInfo. NOTE: this object itself is used as a lock
	 * to synchronize creation/removal of per working copy infos
	 */
	protected Map perWorkingCopyInfos = new HashMap(5);

	/**
	 * Returns whether the given full path (for a package) conflicts with the
	 * output location of the given project.
	 */
	public static boolean conflictsWithOutputLocation(IPath folderPath,
			JavaProject project) {
		try {
			IPath outputLocation = project.getOutputLocation();
			if (outputLocation == null) {
				// in doubt, there is a conflict
				return true;
			}
			if (outputLocation.isPrefixOf(folderPath)) {
				// only allow nesting in project's output if there is a
				// corresponding source folder
				// or if the project's output is not used (in other words, if
				// all source folders have their custom output)
				IClasspathEntry[] classpath = project
						.getResolvedClasspath(true);
				boolean isOutputUsed = false;
				for (int i = 0, length = classpath.length; i < length; i++) {
					IClasspathEntry entry = classpath[i];
					if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
						if (entry.getPath().equals(outputLocation)) {
							return false;
						}
						if (entry.getOutputLocation() == null) {
							isOutputUsed = true;
						}
					}
				}
				return isOutputUsed;
			}
			return false;
		} catch (JavaModelException e) {
			// in doubt, there is a conflict
			return true;
		}
	}

	// public static IClasspathContainer containerGet(IJavaProject project,
	// IPath containerPath) {
	// Map projectContainers = (Map)Containers.get(project);
	// if (projectContainers == null){
	// return null;
	// }
	// IClasspathContainer container =
	// (IClasspathContainer)projectContainers.get(containerPath);
	// return container;
	// }

	// public static void containerPut(IJavaProject project, IPath
	// containerPath, IClasspathContainer container){
	//
	// Map projectContainers = (Map)Containers.get(project);
	// if (projectContainers == null){
	// projectContainers = new HashMap(1);
	// Containers.put(project, projectContainers);
	// }
	//
	// if (container == null) {
	// projectContainers.remove(containerPath);
	// Map previousContainers = (Map)PreviousSessionContainers.get(project);
	// if (previousContainers != null){
	// previousContainers.remove(containerPath);
	// }
	// } else {
	// projectContainers.put(containerPath, container);
	// }
	//
	// // do not write out intermediate initialization value
	// if (container == JavaModelManager.ContainerInitializationInProgress) {
	// return;
	// }
	// Preferences preferences =
	// PHPeclipsePlugin.getPlugin().getPluginPreferences();
	// String containerKey =
	// CP_CONTAINER_PREFERENCES_PREFIX+project.getElementName()
	// +"|"+containerPath;//$NON-NLS-1$
	// String containerString = CP_ENTRY_IGNORE;
	// try {
	// if (container != null) {
	// containerString =
	// ((JavaProject)project).encodeClasspath(container.getClasspathEntries(),
	// null, false);
	// }
	// } catch(JavaModelException e){
	// }
	// preferences.setDefault(containerKey, CP_ENTRY_IGNORE); // use this
	// default to get rid of removed ones
	// preferences.setValue(containerKey, containerString);
	// PHPeclipsePlugin.getPlugin().savePluginPreferences();
	// }

	/**
	 * Returns the Java element corresponding to the given resource, or
	 * <code>null</code> if unable to associate the given resource with a Java
	 * element.
	 * <p>
	 * The resource must be one of:
	 * <ul>
	 * <li>a project - the element returned is the corresponding
	 * <code>IJavaProject</code></li>
	 * <li>a <code>.java</code> file - the element returned is the
	 * corresponding <code>ICompilationUnit</code></li>
	 * <li>a <code>.class</code> file - the element returned is the
	 * corresponding <code>IClassFile</code></li>
	 * <li>a <code>.jar</code> file - the element returned is the
	 * corresponding <code>IPackageFragmentRoot</code></li>
	 * <li>a folder - the element returned is the corresponding
	 * <code>IPackageFragmentRoot</code> or <code>IPackageFragment</code></li>
	 * <li>the workspace root resource - the element returned is the
	 * <code>IJavaModel</code></li>
	 * </ul>
	 * <p>
	 * Creating a Java element has the side effect of creating and opening all
	 * of the element's parents if they are not yet open.
	 */
	public static IJavaElement create(IResource resource, IJavaProject project) {
		if (resource == null) {
			return null;
		}
		int type = resource.getType();
		switch (type) {
		case IResource.PROJECT:
			return JavaCore.create((IProject) resource);
		case IResource.FILE:
			return create((IFile) resource, project);
		case IResource.FOLDER:
			return create((IFolder) resource, project);
		case IResource.ROOT:
			return JavaCore.create((IWorkspaceRoot) resource);
		default:
			return null;
		}
	}

	/**
	 * Returns the Java element corresponding to the given file, its project
	 * being the given project. Returns <code>null</code> if unable to
	 * associate the given file with a Java element.
	 * 
	 * <p>
	 * The file must be one of:
	 * <ul>
	 * <li>a <code>.java</code> file - the element returned is the
	 * corresponding <code>ICompilationUnit</code></li>
	 * <li>a <code>.class</code> file - the element returned is the
	 * corresponding <code>IClassFile</code></li>
	 * <li>a <code>.jar</code> file - the element returned is the
	 * corresponding <code>IPackageFragmentRoot</code></li>
	 * </ul>
	 * <p>
	 * Creating a Java element has the side effect of creating and opening all
	 * of the element's parents if they are not yet open.
	 */
	public static IJavaElement create(IFile file, IJavaProject project) {
		if (file == null) {
			return null;
		}
		if (project == null) {
			project = JavaCore.create(file.getProject());
		}

		if (file.getFileExtension() != null) {
			String name = file.getName();
			if (PHPFileUtil.isValidPHPUnitName(name))
				// if (PHPFileUtil.isPHPFile(file))
				return createCompilationUnitFrom(file, project);
			// if (ProjectPrefUtil.isValidClassFileName(name))
			// return createClassFileFrom(file, project);
			// if (ProjectPrefUtil.isArchiveFileName(name))
			// return createJarPackageFragmentRootFrom(file, project);
		}
		return null;
	}

	/**
	 * Returns the package fragment or package fragment root corresponding to
	 * the given folder, its parent or great parent being the given project. or
	 * <code>null</code> if unable to associate the given folder with a Java
	 * element.
	 * <p>
	 * Note that a package fragment root is returned rather than a default
	 * package.
	 * <p>
	 * Creating a Java element has the side effect of creating and opening all
	 * of the element's parents if they are not yet open.
	 */
	public static IJavaElement create(IFolder folder, IJavaProject project) {
		if (folder == null) {
			return null;
		}
		if (project == null) {
			project = JavaCore.create(folder.getProject());
		}
		IJavaElement element = determineIfOnClasspath(folder, project);
		if (conflictsWithOutputLocation(folder.getFullPath(),
				(JavaProject) project)
				|| (folder.getName().indexOf('.') >= 0 && !(element instanceof IPackageFragmentRoot))) {
			return null; // only package fragment roots are allowed with dot
							// names
		} else {
			return element;
		}
	}

	/**
	 * Creates and returns a class file element for the given
	 * <code>.class</code> file, its project being the given project. Returns
	 * <code>null</code> if unable to recognize the class file.
	 */
	// public static IClassFile createClassFileFrom(IFile file, IJavaProject
	// project ) {
	// if (file == null) {
	// return null;
	// }
	// if (project == null) {
	// project = PHPCore.create(file.getProject());
	// }
	// IPackageFragment pkg = (IPackageFragment) determineIfOnClasspath(file,
	// project);
	// if (pkg == null) {
	// // fix for 1FVS7WE
	// // not on classpath - make the root its folder, and a default package
	// IPackageFragmentRoot root =
	// project.getPackageFragmentRoot(file.getParent());
	// pkg = root.getPackageFragment(IPackageFragment.DEFAULT_PACKAGE_NAME);
	// }
	// return pkg.getClassFile(file.getName());
	// }
	/**
	 * Creates and returns a compilation unit element for the given
	 * <code>.java</code> file, its project being the given project. Returns
	 * <code>null</code> if unable to recognize the compilation unit.
	 */
	public static ICompilationUnit createCompilationUnitFrom(IFile file,
			IJavaProject project) {

		if (file == null)
			return null;

		if (project == null) {
			project = JavaCore.create(file.getProject());
		}
		IPackageFragment pkg = (IPackageFragment) determineIfOnClasspath(file,
				project);
		if (pkg == null) {
			// not on classpath - make the root its folder, and a default
			// package
			IPackageFragmentRoot root = project.getPackageFragmentRoot(file
					.getParent());
			pkg = root
					.getPackageFragment(IPackageFragment.DEFAULT_PACKAGE_NAME);

			if (VERBOSE) {
				System.out
						.println("WARNING : creating unit element outside classpath (" + Thread.currentThread() + "): " + file.getFullPath()); //$NON-NLS-1$//$NON-NLS-2$
			}
		}
		return pkg.getCompilationUnit(file.getName());
	}

	/**
	 * Creates and returns a handle for the given JAR file, its project being
	 * the given project. The Java model associated with the JAR's project may
	 * be created as a side effect. Returns <code>null</code> if unable to
	 * create a JAR package fragment root. (for example, if the JAR file
	 * represents a non-Java resource)
	 */
	// public static IPackageFragmentRoot createJarPackageFragmentRootFrom(IFile
	// file, IJavaProject project) {
	// if (file == null) {
	// return null;
	// }
	// if (project == null) {
	// project = PHPCore.create(file.getProject());
	// }
	//
	// // Create a jar package fragment root only if on the classpath
	// IPath resourcePath = file.getFullPath();
	// try {
	// IClasspathEntry[] entries =
	// ((JavaProject)project).getResolvedClasspath(true);
	// for (int i = 0, length = entries.length; i < length; i++) {
	// IClasspathEntry entry = entries[i];
	// IPath rootPath = entry.getPath();
	// if (rootPath.equals(resourcePath)) {
	// return project.getPackageFragmentRoot(file);
	// }
	// }
	// } catch (JavaModelException e) {
	// }
	// return null;
	// }
	/**
	 * Returns the package fragment root represented by the resource, or the
	 * package fragment the given resource is located in, or <code>null</code>
	 * if the given resource is not on the classpath of the given project.
	 */
	public static IJavaElement determineIfOnClasspath(IResource resource,
			IJavaProject project) {

		IPath resourcePath = resource.getFullPath();
		try {
			IClasspathEntry[] entries = net.sourceforge.phpdt.internal.compiler.util.Util
					.isJavaFileName(resourcePath.lastSegment()) ? project
					.getRawClasspath() // JAVA file can only live inside SRC
										// folder (on the raw path)
					: ((JavaProject) project).getResolvedClasspath(true);

			for (int i = 0; i < entries.length; i++) {
				IClasspathEntry entry = entries[i];
				if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT)
					continue;
				IPath rootPath = entry.getPath();
				if (rootPath.equals(resourcePath)) {
					return project.getPackageFragmentRoot(resource);
				} else if (rootPath.isPrefixOf(resourcePath)
						&& !Util.isExcluded(resource, null,
								((ClasspathEntry) entry)
										.fullExclusionPatternChars())) {
					// given we have a resource child of the root, it cannot be
					// a JAR pkg root
					IPackageFragmentRoot root = ((JavaProject) project)
							.getFolderPackageFragmentRoot(rootPath);
					if (root == null)
						return null;
					IPath pkgPath = resourcePath.removeFirstSegments(rootPath
							.segmentCount());
					if (resource.getType() == IResource.FILE) {
						// if the resource is a file, then remove the last
						// segment which
						// is the file name in the package
						pkgPath = pkgPath.removeLastSegments(1);

						// don't check validity of package name (see
						// http://bugs.eclipse.org/bugs/show_bug.cgi?id=26706)
						// String pkgName = pkgPath.toString().replace('/',
						// '.');
						String pkgName = pkgPath.toString();
						return root.getPackageFragment(pkgName);
					} else {
						String pkgName = Util.packageName(pkgPath);
						if (pkgName == null) {// ||
												// JavaConventions.validatePackageName(pkgName).getSeverity()
												// == IStatus.ERROR) {
							return null;
						}
						return root.getPackageFragment(pkgName);
					}
				}
			}
		} catch (JavaModelException npe) {
			return null;
		}
		return null;
	}

	/**
	 * The singleton manager
	 */
	private final static JavaModelManager Manager = new JavaModelManager();

	/**
	 * Infos cache.
	 */
	protected JavaModelCache cache = new JavaModelCache();

	/*
	 * Temporary cache of newly opened elements
	 */
	private ThreadLocal temporaryCache = new ThreadLocal();

	/**
	 * Set of elements which are out of sync with their buffers.
	 */
	protected Map elementsOutOfSynchWithBuffers = new HashMap(11);

	/**
	 * Holds the state used for delta processing.
	 */
	public DeltaProcessingState deltaState = new DeltaProcessingState();

	/**
	 * Turns delta firing on/off. By default it is on.
	 */
	private boolean isFiring = true;

	/**
	 * Queue of deltas created explicily by the Java Model that have yet to be
	 * fired.
	 */
	ArrayList javaModelDeltas = new ArrayList();

	/**
	 * Queue of reconcile deltas on working copies that have yet to be fired.
	 * This is a table form IWorkingCopy to IJavaElementDelta
	 */
	HashMap reconcileDeltas = new HashMap();

	/**
	 * Collection of listeners for Java element deltas
	 */
	private IElementChangedListener[] elementChangedListeners = new IElementChangedListener[5];

	private int[] elementChangedListenerMasks = new int[5];

	private int elementChangedListenerCount = 0;

	public int currentChangeEventType = ElementChangedEvent.PRE_AUTO_BUILD;

	public static final int DEFAULT_CHANGE_EVENT = 0; // must not collide with
														// ElementChangedEvent
														// event masks

	/**
	 * Used to convert <code>IResourceDelta</code>s into
	 * <code>IJavaElementDelta</code>s.
	 */
	// public final DeltaProcessor deltaProcessor = new DeltaProcessor(this);
	/**
	 * Used to update the JavaModel for <code>IJavaElementDelta</code>s.
	 */
	private final ModelUpdater modelUpdater = new ModelUpdater();

	/**
	 * Workaround for bug 15168 circular errors not reported This is a cache of
	 * the projects before any project addition/deletion has started.
	 */
	public IJavaProject[] javaProjectsCache;

	/**
	 * Table from IProject to PerProjectInfo. NOTE: this object itself is used
	 * as a lock to synchronize creation/removal of per project infos
	 */
	protected Map perProjectInfo = new HashMap(5);

	/**
	 * A map from ICompilationUnit to IWorkingCopy of the shared working copies.
	 */
	public Map sharedWorkingCopies = new HashMap();

	/**
	 * A weak set of the known scopes.
	 */
	protected WeakHashMap searchScopes = new WeakHashMap();

	// public static class PerProjectInfo {
	// public IProject project;
	// public Object savedState;
	// public boolean triedRead;
	// public IClasspathEntry[] classpath;
	// public IClasspathEntry[] lastResolvedClasspath;
	// public Map resolvedPathToRawEntries; // reverse map from resolved path to
	// raw entries
	// public IPath outputLocation;
	// public Preferences preferences;
	// public PerProjectInfo(IProject project) {
	//
	// this.triedRead = false;
	// this.savedState = null;
	// this.project = project;
	// }
	// }

	public static class PerProjectInfo {

		public IProject project;

		public Object savedState;

		public boolean triedRead;

		public IClasspathEntry[] rawClasspath;

		public IClasspathEntry[] resolvedClasspath;

		public Map resolvedPathToRawEntries; // reverse map from resolved
												// path to raw entries

		public IPath outputLocation;

		public Preferences preferences;

		public PerProjectInfo(IProject project) {

			this.triedRead = false;
			this.savedState = null;
			this.project = project;
		}

		// updating raw classpath need to flush obsoleted cached information
		// about resolved entries
		public synchronized void updateClasspathInformation(
				IClasspathEntry[] newRawClasspath) {

			this.rawClasspath = newRawClasspath;
			this.resolvedClasspath = null;
			this.resolvedPathToRawEntries = null;
		}

		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append("Info for "); //$NON-NLS-1$
			buffer.append(this.project.getFullPath());
			buffer.append("\nRaw classpath:\n"); //$NON-NLS-1$
			if (this.rawClasspath == null) {
				buffer.append("  <null>\n"); //$NON-NLS-1$
			} else {
				for (int i = 0, length = this.rawClasspath.length; i < length; i++) {
					buffer.append("  "); //$NON-NLS-1$
					buffer.append(this.rawClasspath[i]);
					buffer.append('\n');
				}
			}
			buffer.append("Resolved classpath:\n"); //$NON-NLS-1$
			IClasspathEntry[] resolvedCP = this.resolvedClasspath;
			if (resolvedCP == null) {
				buffer.append("  <null>\n"); //$NON-NLS-1$
			} else {
				for (int i = 0, length = resolvedCP.length; i < length; i++) {
					buffer.append("  "); //$NON-NLS-1$
					buffer.append(resolvedCP[i]);
					buffer.append('\n');
				}
			}
			buffer.append("Output location:\n  "); //$NON-NLS-1$
			if (this.outputLocation == null) {
				buffer.append("<null>"); //$NON-NLS-1$
			} else {
				buffer.append(this.outputLocation);
			}
			return buffer.toString();
		}
	}

	public static class PerWorkingCopyInfo implements IProblemRequestor {
		int useCount = 0;

		IProblemRequestor problemRequestor;

		ICompilationUnit workingCopy;

		public PerWorkingCopyInfo(ICompilationUnit workingCopy,
				IProblemRequestor problemRequestor) {
			this.workingCopy = workingCopy;
			this.problemRequestor = problemRequestor;
		}

		public void acceptProblem(IProblem problem) {
			if (this.problemRequestor == null)
				return;
			this.problemRequestor.acceptProblem(problem);
		}

		public void beginReporting() {
			if (this.problemRequestor == null)
				return;
			this.problemRequestor.beginReporting();
		}

		public void endReporting() {
			if (this.problemRequestor == null)
				return;
			this.problemRequestor.endReporting();
		}

		public ICompilationUnit getWorkingCopy() {
			return this.workingCopy;
		}

		public boolean isActive() {
			return this.problemRequestor != null
					&& this.problemRequestor.isActive();
		}

		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append("Info for "); //$NON-NLS-1$
			buffer.append(((JavaElement) workingCopy).toStringWithAncestors());
			buffer.append("\nUse count = "); //$NON-NLS-1$
			buffer.append(this.useCount);
			buffer.append("\nProblem requestor:\n  "); //$NON-NLS-1$
			buffer.append(this.problemRequestor);
			return buffer.toString();
		}
	}

	public static boolean VERBOSE = false;

	public static boolean CP_RESOLVE_VERBOSE = false;

	public static boolean ZIP_ACCESS_VERBOSE = false;

	/**
	 * A cache of opened zip files per thread. (map from Thread to map of IPath
	 * to java.io.ZipFile) NOTE: this object itself is used as a lock to
	 * synchronize creation/removal of entries
	 */
	private HashMap zipFiles = new HashMap();

	/**
	 * Update the classpath variable cache
	 */
	public static class PluginPreferencesListener implements
			Preferences.IPropertyChangeListener {
		/**
		 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		public void propertyChange(Preferences.PropertyChangeEvent event) {
			// TODO : jsurfer temp-del
			// String propertyName = event.getProperty();
			// if (propertyName.startsWith(CP_VARIABLE_PREFERENCES_PREFIX)) {
			// String varName =
			// propertyName.substring(CP_VARIABLE_PREFERENCES_PREFIX.length());
			// String newValue = (String)event.getNewValue();
			// if (newValue != null && !(newValue =
			// newValue.trim()).equals(CP_ENTRY_IGNORE)) {
			// Variables.put(varName, new Path(newValue));
			// } else {
			// Variables.remove(varName);
			// }
			// }
			// if (propertyName.startsWith(CP_CONTAINER_PREFERENCES_PREFIX)) {
			// recreatePersistedContainer(propertyName,
			// (String)event.getNewValue(), false);
			// }
		}
	}

	/**
	 * Line separator to use throughout the JavaModel for any source edit
	 * operation
	 */
	public static String LINE_SEPARATOR = System.getProperty("line.separator"); //$NON-NLS-1$

	/**
	 * Constructs a new JavaModelManager
	 */
	private JavaModelManager() {
	}

	/**
	 * @deprecated - discard once debug has converted to not using it
	 */
	public void addElementChangedListener(IElementChangedListener listener) {
		this.addElementChangedListener(listener,
				ElementChangedEvent.POST_CHANGE
						| ElementChangedEvent.POST_RECONCILE);
	}

	/**
	 * addElementChangedListener method comment. Need to clone defensively the
	 * listener information, in case some listener is reacting to some
	 * notification iteration by adding/changing/removing any of the other (for
	 * example, if it deregisters itself).
	 */
	public void addElementChangedListener(IElementChangedListener listener,
			int eventMask) {
		for (int i = 0; i < this.elementChangedListenerCount; i++) {
			if (this.elementChangedListeners[i].equals(listener)) {

				// only clone the masks, since we could be in the middle of
				// notifications and one listener decide to change
				// any event mask of another listeners (yet not notified).
				int cloneLength = this.elementChangedListenerMasks.length;
				System
						.arraycopy(
								this.elementChangedListenerMasks,
								0,
								this.elementChangedListenerMasks = new int[cloneLength],
								0, cloneLength);
				this.elementChangedListenerMasks[i] = eventMask; // could be
																	// different
				return;
			}
		}
		// may need to grow, no need to clone, since iterators will have cached
		// original arrays and max boundary and we only add to the end.
		int length;
		if ((length = this.elementChangedListeners.length) == this.elementChangedListenerCount) {
			System
					.arraycopy(
							this.elementChangedListeners,
							0,
							this.elementChangedListeners = new IElementChangedListener[length * 2],
							0, length);
			System.arraycopy(this.elementChangedListenerMasks, 0,
					this.elementChangedListenerMasks = new int[length * 2], 0,
					length);
		}
		this.elementChangedListeners[this.elementChangedListenerCount] = listener;
		this.elementChangedListenerMasks[this.elementChangedListenerCount] = eventMask;
		this.elementChangedListenerCount++;
	}

	/**
	 * Starts caching ZipFiles. Ignores if there are already clients.
	 */
	public void cacheZipFiles() {
		synchronized (this.zipFiles) {
			Thread currentThread = Thread.currentThread();
			if (this.zipFiles.get(currentThread) != null)
				return;
			this.zipFiles.put(currentThread, new HashMap());
		}
	}

	public void closeZipFile(ZipFile zipFile) {
		if (zipFile == null)
			return;
		synchronized (this.zipFiles) {
			if (this.zipFiles.get(Thread.currentThread()) != null) {
				return; // zip file will be closed by call to flushZipFiles
			}
			try {
				if (JavaModelManager.ZIP_ACCESS_VERBOSE) {
					System.out
							.println("(" + Thread.currentThread() + ") [JavaModelManager.closeZipFile(ZipFile)] Closing ZipFile on " + zipFile.getName()); //$NON-NLS-1$	//$NON-NLS-2$
				}
				zipFile.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Configure the plugin with respect to option settings defined in
	 * ".options" file
	 */
	public void configurePluginDebugOptions() {
		if (JavaCore.getPlugin().isDebugging()) {
			// TODO jsurfer temp-del

			String option = Platform.getDebugOption(BUILDER_DEBUG);
			// if(option != null) JavaBuilder.DEBUG =
			// option.equalsIgnoreCase("true") ; //$NON-NLS-1$
			//
			// option = Platform.getDebugOption(COMPILER_DEBUG);
			// if(option != null) Compiler.DEBUG =
			// option.equalsIgnoreCase("true") ; //$NON-NLS-1$
			//
			// option = Platform.getDebugOption(COMPLETION_DEBUG);
			// if(option != null) CompletionEngine.DEBUG =
			// option.equalsIgnoreCase("true") ; //$NON-NLS-1$
			//
			option = Platform.getDebugOption(CP_RESOLVE_DEBUG);
			if (option != null)
				JavaModelManager.CP_RESOLVE_VERBOSE = option
						.equalsIgnoreCase("true"); //$NON-NLS-1$

			option = Platform.getDebugOption(DELTA_DEBUG);
			if (option != null)
				DeltaProcessor.VERBOSE = option.equalsIgnoreCase("true"); //$NON-NLS-1$

			// option = Platform.getDebugOption(HIERARCHY_DEBUG);
			// if(option != null) TypeHierarchy.DEBUG =
			// option.equalsIgnoreCase("true") ; //$NON-NLS-1$
			//
			// option = Platform.getDebugOption(INDEX_MANAGER_DEBUG);
			// if(option != null) IndexManager.VERBOSE =
			// option.equalsIgnoreCase("true") ; //$NON-NLS-1$

			option = Platform.getDebugOption(JAVAMODEL_DEBUG);
			if (option != null)
				JavaModelManager.VERBOSE = option.equalsIgnoreCase("true"); //$NON-NLS-1$

			option = Platform.getDebugOption(POST_ACTION_DEBUG);
			if (option != null)
				JavaModelOperation.POST_ACTION_VERBOSE = option
						.equalsIgnoreCase("true"); //$NON-NLS-1$

			// option = Platform.getDebugOption(SEARCH_DEBUG);
			// if(option != null) SearchEngine.VERBOSE =
			// option.equalsIgnoreCase("true") ; //$NON-NLS-1$
			//
			// option = Platform.getDebugOption(SELECTION_DEBUG);
			// if(option != null) SelectionEngine.DEBUG =
			// option.equalsIgnoreCase("true") ; //$NON-NLS-1$

			option = Platform.getDebugOption(ZIP_ACCESS_DEBUG);
			if (option != null)
				JavaModelManager.ZIP_ACCESS_VERBOSE = option
						.equalsIgnoreCase("true"); //$NON-NLS-1$
		}
	}

	/*
	 * Discards the per working copy info for the given working copy (making it
	 * a compilation unit) if its use count was 1. Otherwise, just decrement the
	 * use count. If the working copy is primary, computes the delta between its
	 * state and the original compilation unit and register it. Close the
	 * working copy, its buffer and remove it from the shared working copy
	 * table. Ignore if no per-working copy info existed. NOTE: it must be
	 * synchronized as it may interact with the element info cache (if useCount
	 * is decremented to 0), see bug 50667. Returns the new use count (or -1 if
	 * it didn't exist).
	 */
	public synchronized int discardPerWorkingCopyInfo(
			CompilationUnit workingCopy) throws JavaModelException {
		synchronized (perWorkingCopyInfos) {
			WorkingCopyOwner owner = workingCopy.owner;
			Map workingCopyToInfos = (Map) this.perWorkingCopyInfos.get(owner);
			if (workingCopyToInfos == null)
				return -1;

			PerWorkingCopyInfo info = (PerWorkingCopyInfo) workingCopyToInfos
					.get(workingCopy);
			if (info == null)
				return -1;

			if (--info.useCount == 0) {
				// create the delta builder (this remembers the current content
				// of the working copy)
				JavaElementDeltaBuilder deltaBuilder = null;
				if (workingCopy.isPrimary()) {
					deltaBuilder = new JavaElementDeltaBuilder(workingCopy);
				}

				// remove per working copy info
				workingCopyToInfos.remove(workingCopy);
				if (workingCopyToInfos.isEmpty()) {
					this.perWorkingCopyInfos.remove(owner);
				}

				// remove infos + close buffer (since no longer working copy)
				removeInfoAndChildren(workingCopy);
				workingCopy.closeBuffer();

				// compute the delta if needed and register it if there are
				// changes
				if (deltaBuilder != null) {
					deltaBuilder.buildDeltas();
					if ((deltaBuilder.delta != null)
							&& (deltaBuilder.delta.getAffectedChildren().length > 0)) {
						getDeltaProcessor().registerJavaModelDelta(
								deltaBuilder.delta);
					}
				}

			}
			return info.useCount;
		}
	}

	/**
	 * @see ISaveParticipant
	 */
	public void doneSaving(ISaveContext context) {
	}

	/**
	 * Fire Java Model delta, flushing them after the fact after post_change
	 * notification. If the firing mode has been turned off, this has no effect.
	 */
	public void fire(IJavaElementDelta customDelta, int eventType) {

		if (!this.isFiring)
			return;

		if (DeltaProcessor.VERBOSE
				&& (eventType == DEFAULT_CHANGE_EVENT || eventType == ElementChangedEvent.PRE_AUTO_BUILD)) {
			System.out
					.println("-----------------------------------------------------------------------------------------------------------------------");//$NON-NLS-1$
		}

		IJavaElementDelta deltaToNotify;
		if (customDelta == null) {
			deltaToNotify = this.mergeDeltas(this.javaModelDeltas);
		} else {
			deltaToNotify = customDelta;
		}

		// Refresh internal scopes
		if (deltaToNotify != null) {
			// TODO temp-del
			// Iterator scopes = this.scopes.keySet().iterator();
			// while (scopes.hasNext()) {
			// AbstractSearchScope scope = (AbstractSearchScope)scopes.next();
			// scope.processDelta(deltaToNotify);
			// }
		}

		// Notification

		// Important: if any listener reacts to notification by updating the
		// listeners list or mask, these lists will
		// be duplicated, so it is necessary to remember original lists in a
		// variable (since field values may change under us)
		IElementChangedListener[] listeners = this.elementChangedListeners;
		int[] listenerMask = this.elementChangedListenerMasks;
		int listenerCount = this.elementChangedListenerCount;

		switch (eventType) {
		case DEFAULT_CHANGE_EVENT:
			firePreAutoBuildDelta(deltaToNotify, listeners, listenerMask,
					listenerCount);
			firePostChangeDelta(deltaToNotify, listeners, listenerMask,
					listenerCount);
			fireReconcileDelta(listeners, listenerMask, listenerCount);
			break;
		case ElementChangedEvent.PRE_AUTO_BUILD:
			firePreAutoBuildDelta(deltaToNotify, listeners, listenerMask,
					listenerCount);
			break;
		case ElementChangedEvent.POST_CHANGE:
			firePostChangeDelta(deltaToNotify, listeners, listenerMask,
					listenerCount);
			fireReconcileDelta(listeners, listenerMask, listenerCount);
			break;
		}

	}

	private void firePreAutoBuildDelta(IJavaElementDelta deltaToNotify,
			IElementChangedListener[] listeners, int[] listenerMask,
			int listenerCount) {

		if (DeltaProcessor.VERBOSE) {
			System.out
					.println("FIRING PRE_AUTO_BUILD Delta [" + Thread.currentThread() + "]:"); //$NON-NLS-1$//$NON-NLS-2$
			System.out
					.println(deltaToNotify == null ? "<NONE>" : deltaToNotify.toString()); //$NON-NLS-1$
		}
		if (deltaToNotify != null) {
			notifyListeners(deltaToNotify, ElementChangedEvent.PRE_AUTO_BUILD,
					listeners, listenerMask, listenerCount);
		}
	}

	private void firePostChangeDelta(IJavaElementDelta deltaToNotify,
			IElementChangedListener[] listeners, int[] listenerMask,
			int listenerCount) {

		// post change deltas
		if (DeltaProcessor.VERBOSE) {
			System.out
					.println("FIRING POST_CHANGE Delta [" + Thread.currentThread() + "]:"); //$NON-NLS-1$//$NON-NLS-2$
			System.out
					.println(deltaToNotify == null ? "<NONE>" : deltaToNotify.toString()); //$NON-NLS-1$
		}
		if (deltaToNotify != null) {
			// flush now so as to keep listener reactions to post their own
			// deltas for subsequent iteration
			this.flush();

			notifyListeners(deltaToNotify, ElementChangedEvent.POST_CHANGE,
					listeners, listenerMask, listenerCount);
		}
	}

	private void fireReconcileDelta(IElementChangedListener[] listeners,
			int[] listenerMask, int listenerCount) {

		IJavaElementDelta deltaToNotify = mergeDeltas(this.reconcileDeltas
				.values());
		if (DeltaProcessor.VERBOSE) {
			System.out
					.println("FIRING POST_RECONCILE Delta [" + Thread.currentThread() + "]:"); //$NON-NLS-1$//$NON-NLS-2$
			System.out
					.println(deltaToNotify == null ? "<NONE>" : deltaToNotify.toString()); //$NON-NLS-1$
		}
		if (deltaToNotify != null) {
			// flush now so as to keep listener reactions to post their own
			// deltas for subsequent iteration
			this.reconcileDeltas = new HashMap();

			notifyListeners(deltaToNotify, ElementChangedEvent.POST_RECONCILE,
					listeners, listenerMask, listenerCount);
		}
	}

	public void notifyListeners(IJavaElementDelta deltaToNotify, int eventType,
			IElementChangedListener[] listeners, int[] listenerMask,
			int listenerCount) {
		final ElementChangedEvent extraEvent = new ElementChangedEvent(
				deltaToNotify, eventType);
		for (int i = 0; i < listenerCount; i++) {
			if ((listenerMask[i] & eventType) != 0) {
				final IElementChangedListener listener = listeners[i];
				long start = -1;
				if (DeltaProcessor.VERBOSE) {
					System.out
							.print("Listener #" + (i + 1) + "=" + listener.toString());//$NON-NLS-1$//$NON-NLS-2$
					start = System.currentTimeMillis();
				}
				// wrap callbacks with Safe runnable for subsequent listeners to
				// be called when some are causing grief
				Platform.run(new ISafeRunnable() {
					public void handleException(Throwable exception) {
						Util
								.log(exception,
										"Exception occurred in listener of Java element change notification"); //$NON-NLS-1$
					}

					public void run() throws Exception {
						listener.elementChanged(extraEvent);
					}
				});
				if (DeltaProcessor.VERBOSE) {
					System.out
							.println(" -> " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
	}

	/**
	 * Flushes all deltas without firing them.
	 */
	protected void flush() {
		this.javaModelDeltas = new ArrayList();
	}

	/**
	 * Flushes ZipFiles cache if there are no more clients.
	 */
	public void flushZipFiles() {
		synchronized (this.zipFiles) {
			Thread currentThread = Thread.currentThread();
			HashMap map = (HashMap) this.zipFiles.remove(currentThread);
			if (map == null)
				return;
			Iterator iterator = map.values().iterator();
			while (iterator.hasNext()) {
				try {
					ZipFile zipFile = (ZipFile) iterator.next();
					if (JavaModelManager.ZIP_ACCESS_VERBOSE) {
						System.out
								.println("(" + currentThread + ") [JavaModelManager.flushZipFiles()] Closing ZipFile on " + zipFile.getName()); //$NON-NLS-1$//$NON-NLS-2$
					}
					zipFile.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public DeltaProcessor getDeltaProcessor() {
		return this.deltaState.getDeltaProcessor();
	}

	/**
	 * Returns the set of elements which are out of synch with their buffers.
	 */
	protected Map getElementsOutOfSynchWithBuffers() {
		return this.elementsOutOfSynchWithBuffers;
	}

	// public IndexManager getIndexManager() {
	// return this.indexManager;
	// }
	/**
	 * Returns the <code>IJavaElement</code> represented by the
	 * <code>String</code> memento.
	 */
	public IJavaElement getHandleFromMemento(String memento)
			throws JavaModelException {
		if (memento == null) {
			return null;
		}
		JavaModel model = (JavaModel) getJavaModel();
		if (memento.equals("")) { // workspace memento //$NON-NLS-1$
			return model;
		}
		int modelEnd = memento.indexOf(JavaElement.JEM_JAVAPROJECT);
		if (modelEnd == -1) {
			return null;
		}
		boolean returnProject = false;
		int projectEnd = memento.indexOf(JavaElement.JEM_PACKAGEFRAGMENTROOT,
				modelEnd);
		if (projectEnd == -1) {
			projectEnd = memento.length();
			returnProject = true;
		}
		String projectName = memento.substring(modelEnd + 1, projectEnd);
		JavaProject proj = (JavaProject) model.getJavaProject(projectName);
		if (returnProject) {
			return proj;
		}
		int rootEnd = memento.indexOf(JavaElement.JEM_PACKAGEFRAGMENT,
				projectEnd + 1);
		// TODO temp-del
		// if (rootEnd == -1) {
		// return model.getHandleFromMementoForRoot(memento, proj, projectEnd,
		// memento.length());
		// }
		// IPackageFragmentRoot root =
		// model.getHandleFromMementoForRoot(memento, proj, projectEnd,
		// rootEnd);
		// if (root == null)
		// return null;
		//
		// int end= memento.indexOf(JavaElement.JEM_COMPILATIONUNIT, rootEnd);
		// if (end == -1) {
		// end= memento.indexOf(JavaElement.JEM_CLASSFILE, rootEnd);
		// if (end == -1) {
		// if (rootEnd + 1 == memento.length()) {
		// return
		// root.getPackageFragment(IPackageFragment.DEFAULT_PACKAGE_NAME);
		// } else {
		// return root.getPackageFragment(memento.substring(rootEnd + 1));
		// }
		// }
		// //deal with class file and binary members
		// return model.getHandleFromMementoForBinaryMembers(memento, root,
		// rootEnd, end);
		// }
		//
		// //deal with compilation units and source members
		// return model.getHandleFromMementoForSourceMembers(memento, root,
		// rootEnd, end);
		return null;
	}

	// public IndexManager getIndexManager() {
	// return this.deltaProcessor.indexManager;
	// }

	/**
	 * Returns the info for the element.
	 */
	public Object getInfo(IJavaElement element) {
		return this.cache.getInfo(element);
	}

	/**
	 * Returns the handle to the active Java Model.
	 */
	public final JavaModel getJavaModel() {
		return javaModel;
	}

	/**
	 * Returns the singleton JavaModelManager
	 */
	public final static JavaModelManager getJavaModelManager() {
		return Manager;
	}

	/**
	 * Returns the last built state for the given project, or null if there is
	 * none. Deserializes the state if necessary.
	 * 
	 * For use by image builder and evaluation support only
	 */
	public Object getLastBuiltState(IProject project, IProgressMonitor monitor) {
		if (!JavaProject.hasJavaNature(project))
			return null; // should never be requested on non-Java projects
		PerProjectInfo info = getPerProjectInfo(project, true/*
																 * create if
																 * missing
																 */);
		if (!info.triedRead) {
			info.triedRead = true;
			try {
				if (monitor != null)
					monitor.subTask(Util.bind(
							"build.readStateProgress", project.getName())); //$NON-NLS-1$
				info.savedState = readState(project);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return info.savedState;
	}

	/*
	 * Returns the per-project info for the given project. If specified, create
	 * the info if the info doesn't exist.
	 */
	public PerProjectInfo getPerProjectInfo(IProject project, boolean create) {
		synchronized (perProjectInfo) { // use the perProjectInfo collection as
										// its own lock
			PerProjectInfo info = (PerProjectInfo) perProjectInfo.get(project);
			if (info == null && create) {
				info = new PerProjectInfo(project);
				perProjectInfo.put(project, info);
			}
			return info;
		}
	}

	/*
	 * Returns the per-project info for the given project. If the info doesn't
	 * exist, check for the project existence and create the info. @throws
	 * JavaModelException if the project doesn't exist.
	 */
	public PerProjectInfo getPerProjectInfoCheckExistence(IProject project)
			throws JavaModelException {
		JavaModelManager.PerProjectInfo info = getPerProjectInfo(project, false /*
																				 * don't
																				 * create
																				 * info
																				 */);
		if (info == null) {
			if (!JavaProject.hasJavaNature(project)) {
				throw ((JavaProject) JavaCore.create(project))
						.newNotPresentException();
			}
			info = getPerProjectInfo(project, true /* create info */);
		}
		return info;
	}

	/*
	 * Returns the per-working copy info for the given working copy at the given
	 * path. If it doesn't exist and if create, add a new per-working copy info
	 * with the given problem requestor. If recordUsage, increment the
	 * per-working copy info's use count. Returns null if it doesn't exist and
	 * not create.
	 */
	public PerWorkingCopyInfo getPerWorkingCopyInfo(
			CompilationUnit workingCopy, boolean create, boolean recordUsage,
			IProblemRequestor problemRequestor) {
		synchronized (perWorkingCopyInfos) { // use the perWorkingCopyInfo
												// collection as its own lock
			WorkingCopyOwner owner = workingCopy.owner;
			Map workingCopyToInfos = (Map) this.perWorkingCopyInfos.get(owner);
			if (workingCopyToInfos == null && create) {
				workingCopyToInfos = new HashMap();
				this.perWorkingCopyInfos.put(owner, workingCopyToInfos);
			}

			PerWorkingCopyInfo info = workingCopyToInfos == null ? null
					: (PerWorkingCopyInfo) workingCopyToInfos.get(workingCopy);
			if (info == null && create) {
				info = new PerWorkingCopyInfo(workingCopy, problemRequestor);
				workingCopyToInfos.put(workingCopy, info);
			}
			if (info != null && recordUsage)
				info.useCount++;
			return info;
		}
	}

	/**
	 * Returns the name of the variables for which an CP variable initializer is
	 * registered through an extension point
	 */
	public static String[] getRegisteredVariableNames() {

		Plugin jdtCorePlugin = JavaCore.getPlugin();
		if (jdtCorePlugin == null)
			return null;

		ArrayList variableList = new ArrayList(5);
		// IExtensionPoint extension =
		// jdtCorePlugin.getDescriptor().getExtensionPoint(JavaModelManager.CPVARIABLE_INITIALIZER_EXTPOINT_ID);
		// if (extension != null) {
		// IExtension[] extensions = extension.getExtensions();
		// for(int i = 0; i < extensions.length; i++){
		// IConfigurationElement [] configElements =
		// extensions[i].getConfigurationElements();
		// for(int j = 0; j < configElements.length; j++){
		// String varAttribute = configElements[j].getAttribute("variable");
		// //$NON-NLS-1$
		// if (varAttribute != null) variableList.add(varAttribute);
		// }
		// }
		// }
		String[] variableNames = new String[variableList.size()];
		variableList.toArray(variableNames);
		return variableNames;
	}

	/**
	 * Returns the name of the container IDs for which an CP container
	 * initializer is registered through an extension point
	 */
	// public static String[] getRegisteredContainerIDs(){
	//
	// Plugin jdtCorePlugin = PHPCore.getPlugin();
	// if (jdtCorePlugin == null) return null;
	//
	// ArrayList containerIDList = new ArrayList(5);
	// IExtensionPoint extension =
	// jdtCorePlugin.getDescriptor().getExtensionPoint(JavaModelManager.CPCONTAINER_INITIALIZER_EXTPOINT_ID);
	// if (extension != null) {
	// IExtension[] extensions = extension.getExtensions();
	// for(int i = 0; i < extensions.length; i++){
	// IConfigurationElement [] configElements =
	// extensions[i].getConfigurationElements();
	// for(int j = 0; j < configElements.length; j++){
	// String idAttribute = configElements[j].getAttribute("id"); //$NON-NLS-1$
	// if (idAttribute != null) containerIDList.add(idAttribute);
	// }
	// }
	// }
	// String[] containerIDs = new String[containerIDList.size()];
	// containerIDList.toArray(containerIDs);
	// return containerIDs;
	// }
	/**
	 * Returns the File to use for saving and restoring the last built state for
	 * the given project.
	 */
	private File getSerializationFile(IProject project) {
		if (!project.exists())
			return null;
		IPath workingLocation = project.getWorkingLocation(JavaCore.PLUGIN_ID);
		return workingLocation.append("state.dat").toFile(); //$NON-NLS-1$
	}

	/*
	 * Returns the temporary cache for newly opened elements for the current
	 * thread. Creates it if not already created.
	 */
	public HashMap getTemporaryCache() {
		HashMap result = (HashMap) this.temporaryCache.get();
		if (result == null) {
			result = new HashMap();
			this.temporaryCache.set(result);
		}
		return result;
	}

	/**
	 * Returns the open ZipFile at the given location. If the ZipFile does not
	 * yet exist, it is created, opened, and added to the cache of open
	 * ZipFiles. The location must be a absolute path.
	 * 
	 * @exception CoreException
	 *                If unable to create/open the ZipFile
	 */
	public ZipFile getZipFile(IPath path) throws CoreException {

		synchronized (this.zipFiles) { // TODO: use PeThreadObject which does
										// synchronization
			Thread currentThread = Thread.currentThread();
			HashMap map = null;
			ZipFile zipFile;
			if ((map = (HashMap) this.zipFiles.get(currentThread)) != null
					&& (zipFile = (ZipFile) map.get(path)) != null) {

				return zipFile;
			}
			String fileSystemPath = null;
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IResource file = root.findMember(path);
			if (path.isAbsolute() && file != null) {
				if (file == null) { // external file
					fileSystemPath = path.toOSString();
				} else { // internal resource (not an IFile or not existing)
					IPath location;
					if (file.getType() != IResource.FILE
							|| (location = file.getFullPath()) == null) {
						throw new CoreException(
								new Status(
										IStatus.ERROR,
										JavaCore.PLUGIN_ID,
										-1,
										Util
												.bind(
														"file.notFound", path.toString()), null)); //$NON-NLS-1$
					}
					fileSystemPath = location.toOSString();
				}
			} else if (!path.isAbsolute()) {
				file = root.getFile(path);
				if (file == null || file.getType() != IResource.FILE) {
					throw new CoreException(new Status(IStatus.ERROR,
							JavaCore.PLUGIN_ID, -1, Util.bind(
									"file.notFound", path.toString()), null)); //$NON-NLS-1$
				}
				IPath location = file.getFullPath();
				if (location == null) {
					throw new CoreException(new Status(IStatus.ERROR,
							JavaCore.PLUGIN_ID, -1, Util.bind(
									"file.notFound", path.toString()), null)); //$NON-NLS-1$
				}
				fileSystemPath = location.toOSString();
			} else {
				fileSystemPath = path.toOSString();
			}

			try {
				if (ZIP_ACCESS_VERBOSE) {
					System.out
							.println("(" + currentThread + ") [JavaModelManager.getZipFile(IPath)] Creating ZipFile on " + fileSystemPath); //$NON-NLS-1$ //$NON-NLS-2$
				}
				zipFile = new ZipFile(fileSystemPath);
				if (map != null) {
					map.put(path, zipFile);
				}
				return zipFile;
			} catch (IOException e) {
				throw new CoreException(new Status(Status.ERROR,
						JavaCore.PLUGIN_ID, -1,
						Util.bind("status.IOException"), e)); //$NON-NLS-1$
			}
		}
	}

	/*
	 * Returns whether there is a temporary cache for the current thread.
	 */
	public boolean hasTemporaryCache() {
		return this.temporaryCache.get() != null;
	}

	// public void loadVariablesAndContainers() throws CoreException {
	//
	// // backward compatibility, consider persistent property
	// QualifiedName qName = new QualifiedName(PHPCore.PLUGIN_ID, "variables");
	// //$NON-NLS-1$
	// String xmlString =
	// ResourcesPlugin.getWorkspace().getRoot().getPersistentProperty(qName);
	//
	// try {
	// if (xmlString != null){
	// StringReader reader = new StringReader(xmlString);
	// Element cpElement;
	// try {
	// DocumentBuilder parser =
	// DocumentBuilderFactory.newInstance().newDocumentBuilder();
	// cpElement = parser.parse(new InputSource(reader)).getDocumentElement();
	// } catch(SAXException e) {
	// return;
	// } catch(ParserConfigurationException e){
	// return;
	// } finally {
	// reader.close();
	// }
	// if (cpElement == null) return;
	// if (!cpElement.getNodeName().equalsIgnoreCase("variables")) {
	// //$NON-NLS-1$
	// return;
	// }
	//
	// NodeList list= cpElement.getChildNodes();
	// int length= list.getLength();
	// for (int i= 0; i < length; ++i) {
	// Node node= list.item(i);
	// short type= node.getNodeType();
	// if (type == Node.ELEMENT_NODE) {
	// Element element= (Element) node;
	// if (element.getNodeName().equalsIgnoreCase("variable")) { //$NON-NLS-1$
	// variablePut(
	// element.getAttribute("name"), //$NON-NLS-1$
	// new Path(element.getAttribute("path"))); //$NON-NLS-1$
	// }
	// }
	// }
	// }
	// } catch(IOException e){
	// } finally {
	// if (xmlString != null){
	// ResourcesPlugin.getWorkspace().getRoot().setPersistentProperty(qName,
	// null); // flush old one
	// }
	//
	// }
	//
	// // load variables and containers from preferences into cache
	// Preferences preferences =
	// PHPeclipsePlugin.getDefault().getPluginPreferences();

	// // only get variable from preferences not set to their default
	// String[] propertyNames = preferences.propertyNames();
	// int variablePrefixLength = CP_VARIABLE_PREFERENCES_PREFIX.length();
	// for (int i = 0; i < propertyNames.length; i++){
	// String propertyName = propertyNames[i];
	// if (propertyName.startsWith(CP_VARIABLE_PREFERENCES_PREFIX)){
	// String varName = propertyName.substring(variablePrefixLength);
	// IPath varPath = new Path(preferences.getString(propertyName).trim());
	//
	// Variables.put(varName, varPath);
	// PreviousSessionVariables.put(varName, varPath);
	// }
	// if (propertyName.startsWith(CP_CONTAINER_PREFERENCES_PREFIX)){
	// recreatePersistedContainer(propertyName,
	// preferences.getString(propertyName), true/*add to container values*/);
	// }
	// }
	// // override persisted values for variables which have a registered
	// initializer
	// String[] registeredVariables = getRegisteredVariableNames();
	// for (int i = 0; i < registeredVariables.length; i++) {
	// String varName = registeredVariables[i];
	// Variables.put(varName, null); // reset variable, but leave its entry in
	// the Map, so it will be part of variable names.
	// }
	// // override persisted values for containers which have a registered
	// initializer
	// String[] registeredContainerIDs = getRegisteredContainerIDs();
	// for (int i = 0; i < registeredContainerIDs.length; i++) {
	// String containerID = registeredContainerIDs[i];
	// Iterator projectIterator = Containers.keySet().iterator();
	// while (projectIterator.hasNext()){
	// IJavaProject project = (IJavaProject)projectIterator.next();
	// Map projectContainers = (Map)Containers.get(project);
	// if (projectContainers != null){
	// Iterator containerIterator = projectContainers.keySet().iterator();
	// while (containerIterator.hasNext()){
	// IPath containerPath = (IPath)containerIterator.next();
	// if (containerPath.segment(0).equals(containerID)) { // registered
	// container
	// projectContainers.put(containerPath, null); // reset container value, but
	// leave entry in Map
	// }
	// }
	// }
	// }
	// }
	// }

	/**
	 * Merged all awaiting deltas.
	 */
	public IJavaElementDelta mergeDeltas(Collection deltas) {
		if (deltas.size() == 0)
			return null;
		if (deltas.size() == 1)
			return (IJavaElementDelta) deltas.iterator().next();

		if (DeltaProcessor.VERBOSE) {
			System.out
					.println("MERGING " + deltas.size() + " DELTAS [" + Thread.currentThread() + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		Iterator iterator = deltas.iterator();
		IJavaElement javaModel = this.getJavaModel();
		JavaElementDelta rootDelta = new JavaElementDelta(javaModel);
		boolean insertedTree = false;
		while (iterator.hasNext()) {
			JavaElementDelta delta = (JavaElementDelta) iterator.next();
			if (DeltaProcessor.VERBOSE) {
				System.out.println(delta.toString());
			}
			IJavaElement element = delta.getElement();
			if (javaModel.equals(element)) {
				IJavaElementDelta[] children = delta.getAffectedChildren();
				for (int j = 0; j < children.length; j++) {
					JavaElementDelta projectDelta = (JavaElementDelta) children[j];
					rootDelta.insertDeltaTree(projectDelta.getElement(),
							projectDelta);
					insertedTree = true;
				}
				IResourceDelta[] resourceDeltas = delta.getResourceDeltas();
				if (resourceDeltas != null) {
					for (int i = 0, length = resourceDeltas.length; i < length; i++) {
						rootDelta.addResourceDelta(resourceDeltas[i]);
						insertedTree = true;
					}
				}
			} else {
				rootDelta.insertDeltaTree(element, delta);
				insertedTree = true;
			}
		}
		if (insertedTree) {
			return rootDelta;
		} else {
			return null;
		}
	}

	/**
	 * Returns the info for this element without disturbing the cache ordering.
	 */
	// TODO: should be synchronized, could answer unitialized info or if cache
	// is in middle of rehash, could even answer distinct element info
	protected Object peekAtInfo(IJavaElement element) {
		return this.cache.peekAtInfo(element);
	}

	/**
	 * @see ISaveParticipant
	 */
	public void prepareToSave(ISaveContext context) throws CoreException {
	}

	protected void putInfo(IJavaElement element, Object info) {
		this.cache.putInfo(element, info);
	}

	/*
	 * Puts the infos in the given map (keys are IJavaElements and values are
	 * JavaElementInfos) in the Java model cache in an atomic way. First checks
	 * that the info for the opened element (or one of its ancestors) has not
	 * been added to the cache. If it is the case, another thread has opened the
	 * element (or one of its ancestors). So returns without updating the cache.
	 */
	protected synchronized void putInfos(IJavaElement openedElement,
			Map newElements) {
		// remove children
		Object existingInfo = this.cache.peekAtInfo(openedElement);
		if (openedElement instanceof IParent
				&& existingInfo instanceof JavaElementInfo) {
			IJavaElement[] children = ((JavaElementInfo) existingInfo)
					.getChildren();
			for (int i = 0, size = children.length; i < size; ++i) {
				JavaElement child = (JavaElement) children[i];
				try {
					child.close();
				} catch (JavaModelException e) {
					// ignore
				}
			}
		}

		Iterator iterator = newElements.keySet().iterator();
		while (iterator.hasNext()) {
			IJavaElement element = (IJavaElement) iterator.next();
			Object info = newElements.get(element);
			this.cache.putInfo(element, info);
		}
	}

	/**
	 * Reads the build state for the relevant project.
	 */
	protected Object readState(IProject project) throws CoreException {
		File file = getSerializationFile(project);
		if (file != null && file.exists()) {
			try {
				DataInputStream in = new DataInputStream(
						new BufferedInputStream(new FileInputStream(file)));
				try {
					String pluginID = in.readUTF();
					if (!pluginID.equals(JavaCore.PLUGIN_ID))
						throw new IOException(Util
								.bind("build.wrongFileFormat")); //$NON-NLS-1$
					String kind = in.readUTF();
					if (!kind.equals("STATE")) //$NON-NLS-1$
						throw new IOException(Util
								.bind("build.wrongFileFormat")); //$NON-NLS-1$
					if (in.readBoolean())
						return PHPBuilder.readState(project, in);
					if (PHPBuilder.DEBUG)
						System.out
								.println("Saved state thinks last build failed for " + project.getName()); //$NON-NLS-1$
				} finally {
					in.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new CoreException(
						new Status(
								IStatus.ERROR,
								JavaCore.PLUGIN_ID,
								Platform.PLUGIN_ERROR,
								"Error reading last build state for project " + project.getName(), e)); //$NON-NLS-1$
			}
		}
		return null;
	}

	// public static void recreatePersistedContainer(String propertyName, String
	// containerString, boolean addToContainerValues) {
	// int containerPrefixLength = CP_CONTAINER_PREFERENCES_PREFIX.length();
	// int index = propertyName.indexOf('|', containerPrefixLength);
	// if (containerString != null) containerString = containerString.trim();
	// if (index > 0) {
	// final String projectName = propertyName.substring(containerPrefixLength,
	// index).trim();
	// JavaProject project =
	// (JavaProject)getJavaModelManager().getJavaModel().getJavaProject(projectName);
	// final IPath containerPath = new
	// Path(propertyName.substring(index+1).trim());
	//
	// if (containerString == null || containerString.equals(CP_ENTRY_IGNORE)) {
	// containerPut(project, containerPath, null);
	// } else {
	// final IClasspathEntry[] containerEntries =
	// project.decodeClasspath(containerString, false, false);
	// if (containerEntries != null && containerEntries !=
	// JavaProject.INVALID_CLASSPATH) {
	// IClasspathContainer container = new IClasspathContainer() {
	// public IClasspathEntry[] getClasspathEntries() {
	// return containerEntries;
	// }
	// public String getDescription() {
	// return "Persisted container ["+containerPath+" for project ["+
	// projectName+"]"; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
	// }
	// public int getKind() {
	// return 0;
	// }
	// public IPath getPath() {
	// return containerPath;
	// }
	// public String toString() {
	// return getDescription();
	// }
	//
	// };
	// if (addToContainerValues) {
	// containerPut(project, containerPath, container);
	// }
	// Map projectContainers = (Map)PreviousSessionContainers.get(project);
	// if (projectContainers == null){
	// projectContainers = new HashMap(1);
	// PreviousSessionContainers.put(project, projectContainers);
	// }
	// projectContainers.put(containerPath, container);
	// }
	// }
	// }
	// }

	/**
	 * Registers the given delta with this manager.
	 */
	protected void registerJavaModelDelta(IJavaElementDelta delta) {
		this.javaModelDeltas.add(delta);
	}

	/**
	 * Remembers the given scope in a weak set (so no need to remove it: it will
	 * be removed by the garbage collector)
	 */
	// public void rememberScope(AbstractSearchScope scope) {
	// // NB: The value has to be null so as to not create a strong reference on
	// the scope
	// this.scopes.put(scope, null);
	// }
	/**
	 * removeElementChangedListener method comment.
	 */
	public void removeElementChangedListener(IElementChangedListener listener) {

		for (int i = 0; i < this.elementChangedListenerCount; i++) {

			if (this.elementChangedListeners[i].equals(listener)) {

				// need to clone defensively since we might be in the middle of
				// listener notifications (#fire)
				int length = this.elementChangedListeners.length;
				IElementChangedListener[] newListeners = new IElementChangedListener[length];
				System.arraycopy(this.elementChangedListeners, 0, newListeners,
						0, i);
				int[] newMasks = new int[length];
				System.arraycopy(this.elementChangedListenerMasks, 0, newMasks,
						0, i);

				// copy trailing listeners
				int trailingLength = this.elementChangedListenerCount - i - 1;
				if (trailingLength > 0) {
					System.arraycopy(this.elementChangedListeners, i + 1,
							newListeners, i, trailingLength);
					System.arraycopy(this.elementChangedListenerMasks, i + 1,
							newMasks, i, trailingLength);
				}

				// update manager listener state (#fire need to iterate over
				// original listeners through a local variable to hold onto
				// the original ones)
				this.elementChangedListeners = newListeners;
				this.elementChangedListenerMasks = newMasks;
				this.elementChangedListenerCount--;
				return;
			}
		}
	}

	/**
	 * Remembers the given scope in a weak set (so no need to remove it: it will
	 * be removed by the garbage collector)
	 */
	// public void rememberScope(AbstractSearchScope scope) {
	// // NB: The value has to be null so as to not create a strong reference on
	// the scope
	// this.searchScopes.put(scope, null);
	// }
	/*
	 * Removes all cached info for the given element (including all children)
	 * from the cache. Returns the info for the given element, or null if it was
	 * closed.
	 */
	public synchronized Object removeInfoAndChildren(JavaElement element)
			throws JavaModelException {
		Object info = this.cache.peekAtInfo(element);
		if (info != null) {
			boolean wasVerbose = false;
			try {
				if (VERBOSE) {
					System.out
							.println("CLOSING Element (" + Thread.currentThread() + "): " + element.toStringWithAncestors()); //$NON-NLS-1$//$NON-NLS-2$
					wasVerbose = true;
					VERBOSE = false;
				}
				element.closing(info);
				if (element instanceof IParent
						&& info instanceof JavaElementInfo) {
					IJavaElement[] children = ((JavaElementInfo) info)
							.getChildren();
					for (int i = 0, size = children.length; i < size; ++i) {
						JavaElement child = (JavaElement) children[i];
						child.close();
					}
				}
				this.cache.removeInfo(element);
				if (wasVerbose) {
					System.out
							.println("-> Package cache size = " + this.cache.pkgSize()); //$NON-NLS-1$
					System.out
							.println("-> Openable cache filling ratio = " + NumberFormat.getInstance().format(this.cache.openableFillingRatio()) + "%"); //$NON-NLS-1$//$NON-NLS-2$
				}
			} finally {
				JavaModelManager.VERBOSE = wasVerbose;
			}
			return info;
		}
		return null;
	}

	public void removePerProjectInfo(JavaProject javaProject) {
		synchronized (perProjectInfo) { // use the perProjectInfo collection as
										// its own lock
			IProject project = javaProject.getProject();
			PerProjectInfo info = (PerProjectInfo) perProjectInfo.get(project);
			if (info != null) {
				perProjectInfo.remove(project);
			}
		}
	}

	/*
	 * Resets the temporary cache for newly created elements to null.
	 */
	public void resetTemporaryCache() {
		this.temporaryCache.set(null);
	}

	/**
	 * @see ISaveParticipant
	 */
	public void rollback(ISaveContext context) {
	}

	private void saveState(PerProjectInfo info, ISaveContext context)
			throws CoreException {

		// passed this point, save actions are non trivial
		if (context.getKind() == ISaveContext.SNAPSHOT)
			return;

		// save built state
		if (info.triedRead)
			saveBuiltState(info);
	}

	/**
	 * Saves the built state for the project.
	 */
	private void saveBuiltState(PerProjectInfo info) throws CoreException {
		if (PHPBuilder.DEBUG)
			System.out.println(Util.bind(
					"build.saveStateProgress", info.project.getName())); //$NON-NLS-1$
		File file = getSerializationFile(info.project);
		if (file == null)
			return;
		long t = System.currentTimeMillis();
		try {
			DataOutputStream out = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(file)));
			try {
				out.writeUTF(JavaCore.PLUGIN_ID);
				out.writeUTF("STATE"); //$NON-NLS-1$
				if (info.savedState == null) {
					out.writeBoolean(false);
				} else {
					out.writeBoolean(true);
					PHPBuilder.writeState(info.savedState, out);
				}
			} finally {
				out.close();
			}
		} catch (RuntimeException e) {
			try {
				file.delete();
			} catch (SecurityException se) {
			}
			throw new CoreException(
					new Status(
							IStatus.ERROR,
							JavaCore.PLUGIN_ID,
							Platform.PLUGIN_ERROR,
							Util
									.bind(
											"build.cannotSaveState", info.project.getName()), e)); //$NON-NLS-1$
		} catch (IOException e) {
			try {
				file.delete();
			} catch (SecurityException se) {
			}
			throw new CoreException(
					new Status(
							IStatus.ERROR,
							JavaCore.PLUGIN_ID,
							Platform.PLUGIN_ERROR,
							Util
									.bind(
											"build.cannotSaveState", info.project.getName()), e)); //$NON-NLS-1$
		}
		if (PHPBuilder.DEBUG) {
			t = System.currentTimeMillis() - t;
			System.out.println(Util.bind(
					"build.saveStateComplete", String.valueOf(t))); //$NON-NLS-1$
		}
	}

	private synchronized Map containerClone(IJavaProject project) {
		Map originalProjectContainers = (Map) JavaModelManager.containers.get(project);
		if (originalProjectContainers == null)
			return null;
		Map projectContainers = new HashMap(originalProjectContainers.size());
		projectContainers.putAll(originalProjectContainers);
		return projectContainers;
	}

	/**
	 * @see ISaveParticipant
	 */
	public void saving(ISaveContext context) throws CoreException {

		// save container values on snapshot/full save
		Preferences preferences = JavaCore.getPlugin().getPluginPreferences();
		IJavaProject[] projects = getJavaModel().getJavaProjects();
		for (int i = 0, length = projects.length; i < length; i++) {
			IJavaProject project = projects[i];
			// clone while iterating (see
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=59638)
			Map projectContainers = containerClone(project);
			if (projectContainers == null)
				continue;
			for (Iterator keys = projectContainers.keySet().iterator(); keys
					.hasNext();) {
				IPath containerPath = (IPath) keys.next();
				// IClasspathContainer container = (IClasspathContainer)
				// projectContainers.get(containerPath);
				String containerKey = CP_CONTAINER_PREFERENCES_PREFIX
						+ project.getElementName() + "|" + containerPath;//$NON-NLS-1$
				String containerString = CP_ENTRY_IGNORE;
				// try {
				// if (container != null) {
				// containerString =
				// ((JavaProject)project).encodeClasspath(container.getClasspathEntries(),
				// null, false);
				// }
				// } catch(JavaModelException e){
				// // could not encode entry: leave it as CP_ENTRY_IGNORE
				// }
				preferences.setDefault(containerKey, CP_ENTRY_IGNORE); // use
																		// this
																		// default
																		// to
																		// get
																		// rid
																		// of
																		// removed
																		// ones
				preferences.setValue(containerKey, containerString);
			}
		}
		JavaCore.getPlugin().savePluginPreferences();

		// if (context.getKind() == ISaveContext.FULL_SAVE) {
		// // will need delta since this save (see
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=38658)
		// context.needDelta();
		//
		// // clean up indexes on workspace full save
		// // (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=52347)
		// IndexManager manager = this.indexManager;
		// if (manager != null) {
		// manager.cleanUpIndexes();
		// }
		// }

		IProject savedProject = context.getProject();
		if (savedProject != null) {
			if (!JavaProject.hasJavaNature(savedProject))
				return; // ignore
			PerProjectInfo info = getPerProjectInfo(savedProject, true /*
																		 * create
																		 * info
																		 */);
			saveState(info, context);
			return;
		}

		ArrayList vStats = null; // lazy initialized
		for (Iterator iter = perProjectInfo.values().iterator(); iter.hasNext();) {
			try {
				PerProjectInfo info = (PerProjectInfo) iter.next();
				saveState(info, context);
			} catch (CoreException e) {
				if (vStats == null)
					vStats = new ArrayList();
				vStats.add(e.getStatus());
			}
		}
		if (vStats != null) {
			IStatus[] stats = new IStatus[vStats.size()];
			vStats.toArray(stats);
			throw new CoreException(new MultiStatus(JavaCore.PLUGIN_ID,
					IStatus.ERROR, stats,
					Util.bind("build.cannotSaveStates"), null)); //$NON-NLS-1$
		}
	}

	/**
	 * @see ISaveParticipant
	 */
	// public void saving(ISaveContext context) throws CoreException {
	//
	// IProject savedProject = context.getProject();
	// if (savedProject != null) {
	// if (!JavaProject.hasJavaNature(savedProject)) return; // ignore
	// PerProjectInfo info = getPerProjectInfo(savedProject, true /* create info
	// */);
	// saveState(info, context);
	// return;
	// }
	//
	// ArrayList vStats= null; // lazy initialized
	// for (Iterator iter = perProjectInfo.values().iterator(); iter.hasNext();)
	// {
	// try {
	// PerProjectInfo info = (PerProjectInfo) iter.next();
	// saveState(info, context);
	// } catch (CoreException e) {
	// if (vStats == null)
	// vStats= new ArrayList();
	// vStats.add(e.getStatus());
	// }
	// }
	// if (vStats != null) {
	// IStatus[] stats= new IStatus[vStats.size()];
	// vStats.toArray(stats);
	// throw new CoreException(new MultiStatus(JavaCore.PLUGIN_ID,
	// IStatus.ERROR, stats, Util.bind("build.cannotSaveStates"), null));
	// //$NON-NLS-1$
	// }
	// }
	/**
	 * Record the order in which to build the java projects (batch build). This
	 * order is based on the projects classpath settings.
	 */
	protected void setBuildOrder(String[] javaBuildOrder)
			throws JavaModelException {

		// optional behaviour
		// possible value of index 0 is Compute
		if (!JavaCore.COMPUTE.equals(JavaCore
				.getOption(JavaCore.CORE_JAVA_BUILD_ORDER)))
			return; // cannot be customized at project level

		if (javaBuildOrder == null || javaBuildOrder.length <= 1)
			return;

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceDescription description = workspace.getDescription();
		String[] wksBuildOrder = description.getBuildOrder();

		String[] newOrder;
		if (wksBuildOrder == null) {
			newOrder = javaBuildOrder;
		} else {
			// remove projects which are already mentionned in java builder
			// order
			int javaCount = javaBuildOrder.length;
			HashMap newSet = new HashMap(javaCount); // create a set for fast
														// check
			for (int i = 0; i < javaCount; i++) {
				newSet.put(javaBuildOrder[i], javaBuildOrder[i]);
			}
			int removed = 0;
			int oldCount = wksBuildOrder.length;
			for (int i = 0; i < oldCount; i++) {
				if (newSet.containsKey(wksBuildOrder[i])) {
					wksBuildOrder[i] = null;
					removed++;
				}
			}
			// add Java ones first
			newOrder = new String[oldCount - removed + javaCount];
			System.arraycopy(javaBuildOrder, 0, newOrder, 0, javaCount); // java
																			// projects
																			// are
																			// built
																			// first

			// copy previous items in their respective order
			int index = javaCount;
			for (int i = 0; i < oldCount; i++) {
				if (wksBuildOrder[i] != null) {
					newOrder[index++] = wksBuildOrder[i];
				}
			}
		}
		// commit the new build order out
		description.setBuildOrder(newOrder);
		try {
			workspace.setDescription(description);
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}
	}

	/**
	 * Sets the last built state for the given project, or null to reset it.
	 */
	public void setLastBuiltState(IProject project, Object state) {
		if (!JavaProject.hasJavaNature(project))
			return; // should never be requested on non-Java projects
		PerProjectInfo info = getPerProjectInfo(project, true /*
																 * create if
																 * missing
																 */);
		info.triedRead = true; // no point trying to re-read once using setter
		info.savedState = state;
		if (state == null) { // delete state file to ensure a full build
								// happens if the workspace crashes
			try {
				File file = getSerializationFile(project);
				if (file != null && file.exists())
					file.delete();
			} catch (SecurityException se) {
			}
		}
	}

	public void shutdown() {
		// TODO temp-del
		// if (this.deltaProcessor.indexManager != null){ // no more indexing
		// this.deltaProcessor.indexManager.shutdown();
		// }
		try {
			IJavaModel model = this.getJavaModel();
			if (model != null) {

				model.close();
			}
		} catch (JavaModelException e) {
		}
	}

	/**
	 * Turns the firing mode to on. That is, deltas that are/have been
	 * registered will be fired.
	 */
	public void startDeltas() {
		this.isFiring = true;
	}

	/**
	 * Turns the firing mode to off. That is, deltas that are/have been
	 * registered will not be fired until deltas are started again.
	 */
	public void stopDeltas() {
		this.isFiring = false;
	}

	/**
	 * Update Java Model given some delta
	 */
	public void updateJavaModel(IJavaElementDelta customDelta) {

		if (customDelta == null) {
			for (int i = 0, length = this.javaModelDeltas.size(); i < length; i++) {
				IJavaElementDelta delta = (IJavaElementDelta) this.javaModelDeltas
						.get(i);
				this.modelUpdater.processJavaDelta(delta);
			}
		} else {
			this.modelUpdater.processJavaDelta(customDelta);
		}
	}

	public static IPath variableGet(String variableName) {
		return (IPath) Variables.get(variableName);
	}

	public static String[] variableNames() {
		int length = Variables.size();
		String[] result = new String[length];
		Iterator vars = Variables.keySet().iterator();
		int index = 0;
		while (vars.hasNext()) {
			result[index++] = (String) vars.next();
		}
		return result;
	}

	public static void variablePut(String variableName, IPath variablePath) {

		// update cache - do not only rely on listener refresh
		if (variablePath == null) {
			Variables.remove(variableName);
			PreviousSessionVariables.remove(variableName);
		} else {
			Variables.put(variableName, variablePath);
		}

		// do not write out intermediate initialization value
		if (variablePath == JavaModelManager.VariableInitializationInProgress) {
			return;
		}
		Preferences preferences = JavaCore.getPlugin().getPluginPreferences();
		String variableKey = CP_VARIABLE_PREFERENCES_PREFIX + variableName;
		String variableString = variablePath == null ? CP_ENTRY_IGNORE
				: variablePath.toString();
		preferences.setDefault(variableKey, CP_ENTRY_IGNORE); // use this
																// default to
																// get rid of
																// removed ones
		preferences.setValue(variableKey, variableString);
		JavaCore.getPlugin().savePluginPreferences();
	}

	/*
	 * Returns all the working copies which have the given owner. Adds the
	 * working copies of the primary owner if specified. Returns null if it has
	 * none.
	 */
	public ICompilationUnit[] getWorkingCopies(WorkingCopyOwner owner,
			boolean addPrimary) {
		synchronized (perWorkingCopyInfos) {
			ICompilationUnit[] primaryWCs = addPrimary
					&& owner != DefaultWorkingCopyOwner.PRIMARY ? getWorkingCopies(
					DefaultWorkingCopyOwner.PRIMARY, false)
					: null;
			Map workingCopyToInfos = (Map) perWorkingCopyInfos.get(owner);
			if (workingCopyToInfos == null)
				return primaryWCs;
			int primaryLength = primaryWCs == null ? 0 : primaryWCs.length;
			int size = workingCopyToInfos.size(); // note size is > 0
													// otherwise
													// pathToPerWorkingCopyInfos
													// would be null
			ICompilationUnit[] result = new ICompilationUnit[primaryLength
					+ size];
			if (primaryWCs != null) {
				System.arraycopy(primaryWCs, 0, result, 0, primaryLength);
			}
			Iterator iterator = workingCopyToInfos.values().iterator();
			int index = primaryLength;
			while (iterator.hasNext()) {
				result[index++] = ((JavaModelManager.PerWorkingCopyInfo) iterator
						.next()).getWorkingCopy();
			}
			return result;
		}
	}

	/*
	 * A HashSet that contains the IJavaProject whose classpath is being
	 * resolved.
	 */
	private ThreadLocal classpathsBeingResolved = new ThreadLocal();

	private HashSet getClasspathBeingResolved() {
		HashSet result = (HashSet) this.classpathsBeingResolved.get();
		if (result == null) {
			result = new HashSet();
			this.classpathsBeingResolved.set(result);
		}
		return result;
	}

	public boolean isClasspathBeingResolved(IJavaProject project) {
		return getClasspathBeingResolved().contains(project);
	}

	public void setClasspathBeingResolved(IJavaProject project,
			boolean classpathIsResolved) {
		if (classpathIsResolved) {
			getClasspathBeingResolved().add(project);
		} else {
			getClasspathBeingResolved().remove(project);
		}
	}
}