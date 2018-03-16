/**********************************************************************
 Copyright (c) 2000, 2002 IBM Corp. and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html

 **********************************************************************/
package net.sourceforge.phpeclipse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.Set;

import net.sourceforge.phpdt.core.IBuffer;
import net.sourceforge.phpdt.core.IBufferFactory;
import net.sourceforge.phpdt.core.ICompilationUnit;
import net.sourceforge.phpdt.core.IJavaElement;
import net.sourceforge.phpdt.core.JavaCore;
import net.sourceforge.phpdt.core.WorkingCopyOwner;
import net.sourceforge.phpdt.internal.core.BatchOperation;
import net.sourceforge.phpdt.internal.core.JavaModelManager;
import net.sourceforge.phpdt.internal.core.util.Util;
import net.sourceforge.phpdt.internal.corext.template.php.CodeTemplateContextType;
import net.sourceforge.phpdt.internal.corext.template.php.HTMLContextType;
import net.sourceforge.phpdt.internal.corext.template.php.JavaContextType;
import net.sourceforge.phpdt.internal.corext.template.php.JavaDocContextType;
import net.sourceforge.phpdt.internal.ui.IJavaStatusConstants;
import net.sourceforge.phpdt.internal.ui.JavaElementAdapterFactory;
import net.sourceforge.phpdt.internal.ui.ResourceAdapterFactory;
import net.sourceforge.phpdt.internal.ui.preferences.MembersOrderPreferenceCache;
import net.sourceforge.phpdt.internal.ui.preferences.MockupPreferenceStore;
import net.sourceforge.phpdt.internal.ui.text.PreferencesAdapter;
import net.sourceforge.phpdt.internal.ui.text.folding.JavaFoldingStructureProviderRegistry;
import net.sourceforge.phpdt.internal.ui.text.java.hover.JavaEditorTextHoverDescriptor;
import net.sourceforge.phpdt.internal.ui.viewsupport.ImageDescriptorRegistry;
import net.sourceforge.phpdt.internal.ui.viewsupport.ProblemMarkerManager;
import net.sourceforge.phpdt.ui.IContextMenuConstants;
import net.sourceforge.phpdt.ui.IWorkingCopyManager;
import net.sourceforge.phpdt.ui.PreferenceConstants;
import net.sourceforge.phpdt.ui.text.JavaTextTools;
import net.sourceforge.phpeclipse.builder.ExternalEditorInput;
import net.sourceforge.phpeclipse.builder.ExternalStorageDocumentProvider;
import net.sourceforge.phpeclipse.builder.FileStorage;
import net.sourceforge.phpeclipse.builder.IdentifierIndexManager;
import net.sourceforge.phpeclipse.phpeditor.CustomBufferFactory;
import net.sourceforge.phpeclipse.phpeditor.DocumentAdapter;
import net.sourceforge.phpeclipse.phpeditor.ICompilationUnitDocumentProvider;
import net.sourceforge.phpeclipse.phpeditor.PHPDocumentProvider;
import net.sourceforge.phpeclipse.phpeditor.PHPSyntaxRdr;
import net.sourceforge.phpeclipse.phpeditor.WorkingCopyManager;
import net.sourceforge.phpeclipse.phpeditor.util.PHPColorProvider;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.ConfigurationElementSorter;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class PHPeclipsePlugin extends AbstractUIPlugin implements
		IPreferenceConstants {
	/**
	 * The id of the PHP plugin (value <code>"net.sourceforge.phpeclipse"</code>).
	 */
	public static final String PLUGIN_ID = "net.sourceforge.phpeclipse"; //$NON-NLS-1$

	public static final String EDITOR_ID = PHPeclipsePlugin.PLUGIN_ID
			+ ".PHPUnitEditor";

	public static final String ID_PERSPECTIVE = "net.sourceforge.phpeclipse.PHPPerspective"; //$NON-NLS-1$

	IWorkspace w;

	/**
	 * id of builder - matches plugin.xml (concatenate pluginid.builderid)
	 */
	public static final String BUILDER_PARSER_ID = PLUGIN_ID + ".parserbuilder";

	// public static final String BUILDER_INDEX_ID = PLUGIN_ID +
	// ".indexbuilder";
	/** General debug flag */

	public static final boolean DEBUG = false;

	/**
	 * The maximum number of allowed proposals by category
	 */
	public final static int MAX_PROPOSALS = 200;

	/**
	 * The key to store customized templates.
	 * 
	 * @since 3.0
	 */
	private static final String TEMPLATES_KEY = "net.sourceforge.phpdt.ui.text.custom_templates"; //$NON-NLS-1$

	/**
	 * The key to store customized code templates.
	 * 
	 * @since 3.0
	 */
	private static final String CODE_TEMPLATES_KEY = "net.sourceforge.phpdt.ui.text.custom_code_templates"; //$NON-NLS-1$

	public static final String PHP_CODING_ACTION_SET_ID = PLUGIN_ID
			+ ".ui.CodingActionSet"; //$NON-NLS-1$

	public final static String PHP_NATURE_ID = PLUGIN_ID + ".phpnature";

	public static final String PHPPARSER_ORIGINAL = "net.sourceforge.phpdt.internal.compiler.parser.Parser";

	public static final String PHPPARSER_NEW = "test.PHPParser";

	/** Change this if you want to switch PHP Parser. */
	public static final String PHPPARSER = PHPPARSER_ORIGINAL;

	// The shared instance.
	private static PHPeclipsePlugin plugin;

	/**
	 * The template context type registry for the java editor.
	 * 
	 * @since 3.0
	 */
	private ContextTypeRegistry fContextTypeRegistry;

	/**
	 * The code template context type registry for the java editor.
	 * 
	 * @since 3.0
	 */
	private ContextTypeRegistry fCodeTemplateContextTypeRegistry;

	/**
	 * The template store for the java editor.
	 * 
	 * @since 3.0
	 */
	private TemplateStore fTemplateStore;

	/**
	 * The coded template store for the java editor.
	 * 
	 * @since 3.0
	 */
	private TemplateStore fCodeTemplateStore;

	/** Windows 9x */
	private static final int WINDOWS_9x = 6;

	/** Windows NT */
	private static final int WINDOWS_NT = 5;

	private ImageDescriptorRegistry fImageDescriptorRegistry;

	private HashMap fIndexManagerMap = new HashMap();

	private IWorkingCopyManager fWorkingCopyManager;

	private IBufferFactory fBufferFactory;

	private ICompilationUnitDocumentProvider fCompilationUnitDocumentProvider;

	private JavaTextTools fJavaTextTools;

	private ProblemMarkerManager fProblemMarkerManager;

	private MembersOrderPreferenceCache fMembersOrderPreferenceCache;

	private IFile fLastEditorFile = null;

	private JavaEditorTextHoverDescriptor[] fJavaEditorTextHoverDescriptors;

	private JavaElementAdapterFactory fJavaElementAdapterFactory;

	// private MarkerAdapterFactory fMarkerAdapterFactory;
	// private EditorInputAdapterFactory fEditorInputAdapterFactory;
	private ResourceAdapterFactory fResourceAdapterFactory;

	// private LogicalPackageAdapterFactory fLogicalPackageAdapterFactory;
	private IPropertyChangeListener fFontPropertyChangeListener;

	/**
	 * Property change listener on this plugin's preference store.
	 * 
	 * @since 3.0
	 */
	// private IPropertyChangeListener fPropertyChangeListener;
	/**
	 * The combined preference store.
	 * 
	 * @since 3.0
	 */
	private IPreferenceStore fCombinedPreferenceStore;

	/**
	 * The extension point registry for the
	 * <code>net.sourceforge.phpdt.ui.javaFoldingStructureProvider</code>
	 * extension point.
	 * 
	 * @since 3.0
	 */
	private JavaFoldingStructureProviderRegistry fFoldingStructureProviderRegistry;

	/**
	 * Mockup preference store for firing events and registering listeners on
	 * project setting changes. FIXME: Temporary solution.
	 * 
	 * @since 3.0
	 */
	private MockupPreferenceStore fMockupPreferenceStore;

	/**
	 * The constructor.
	 */
	public PHPeclipsePlugin() {
		super();
		plugin = this;
		// externalTools = new ExternalToolsPlugin();

		// try {
		// resourceBundle =
		// ResourceBundle.getBundle("net.sourceforge.PHPeclipsePluginResources");
		// } catch (MissingResourceException x) {
		// resourceBundle = null;
		// }
	}

	// /**
	// * Returns all Java editor text hovers contributed to the workbench.
	// *
	// * @return an array of JavaEditorTextHoverDescriptor
	// * @since 2.1
	// */
	// public JavaEditorTextHoverDescriptor[]
	// getJavaEditorTextHoverDescriptors()
	// {
	// if (fJavaEditorTextHoverDescriptors == null)
	// fJavaEditorTextHoverDescriptors = JavaEditorTextHoverDescriptor
	// .getContributedHovers();
	// return fJavaEditorTextHoverDescriptors;
	// }
	/**
	 * Returns all Java editor text hovers contributed to the workbench.
	 * 
	 * @return an array of JavaEditorTextHoverDescriptor
	 * @since 2.1
	 */
	public JavaEditorTextHoverDescriptor[] getJavaEditorTextHoverDescriptors() {
		if (fJavaEditorTextHoverDescriptors == null) {
			fJavaEditorTextHoverDescriptors = JavaEditorTextHoverDescriptor
					.getContributedHovers();
			ConfigurationElementSorter sorter = new ConfigurationElementSorter() {
				/*
				 * @see org.eclipse.ui.texteditor.ConfigurationElementSorter#getConfigurationElement(java.lang.Object)
				 */
				public IConfigurationElement getConfigurationElement(
						Object object) {
					return ((JavaEditorTextHoverDescriptor) object)
							.getConfigurationElement();
				}
			};
			sorter.sort(fJavaEditorTextHoverDescriptors);

			// The Problem hover has to be the first and the Annotation hover
			// has to
			// be the last one in the JDT UI's hover list
			int length = fJavaEditorTextHoverDescriptors.length;
			int first = -1;
			int last = length - 1;
			int problemHoverIndex = -1;
			int annotationHoverIndex = -1;
			for (int i = 0; i < length; i++) {
				if (!fJavaEditorTextHoverDescriptors[i].getId().startsWith(
						PLUGIN_ID)) {
					if (problemHoverIndex == -1 || annotationHoverIndex == -1)
						continue;
					else {
						last = i - 1;
						break;
					}
				}
				if (first == -1)
					first = i;

				if (fJavaEditorTextHoverDescriptors[i].getId().equals(
						"net.sourceforge.phpdt.ui.AnnotationHover")) { //$NON-NLS-1$
					annotationHoverIndex = i;
					continue;
				}
				if (fJavaEditorTextHoverDescriptors[i].getId().equals(
						"net.sourceforge.phpdt.ui.ProblemHover")) { //$NON-NLS-1$
					problemHoverIndex = i;
					continue;
				}
			}

			JavaEditorTextHoverDescriptor hoverDescriptor = null;

			if (first > -1 && problemHoverIndex > -1
					&& problemHoverIndex != first) {
				// move problem hover to beginning
				hoverDescriptor = fJavaEditorTextHoverDescriptors[first];
				fJavaEditorTextHoverDescriptors[first] = fJavaEditorTextHoverDescriptors[problemHoverIndex];
				fJavaEditorTextHoverDescriptors[problemHoverIndex] = hoverDescriptor;

				// update annotation hover index if needed
				if (annotationHoverIndex == first)
					annotationHoverIndex = problemHoverIndex;
			}

			if (annotationHoverIndex > -1 && annotationHoverIndex != last) {
				// move annotation hover to end
				hoverDescriptor = fJavaEditorTextHoverDescriptors[last];
				fJavaEditorTextHoverDescriptors[last] = fJavaEditorTextHoverDescriptors[annotationHoverIndex];
				fJavaEditorTextHoverDescriptors[annotationHoverIndex] = hoverDescriptor;
			}

			// Move Best Match hover to front
			for (int i = 0; i < fJavaEditorTextHoverDescriptors.length - 1; i++) {
				if (PreferenceConstants.ID_BESTMATCH_HOVER
						.equals(fJavaEditorTextHoverDescriptors[i].getId())) {
					hoverDescriptor = fJavaEditorTextHoverDescriptors[i];
					for (int j = i; j > 0; j--)
						fJavaEditorTextHoverDescriptors[j] = fJavaEditorTextHoverDescriptors[j - 1];
					fJavaEditorTextHoverDescriptors[0] = hoverDescriptor;
					break;
				}

			}
		}

		return fJavaEditorTextHoverDescriptors;
	}

	/**
	 * Resets the Java editor text hovers contributed to the workbench.
	 * <p>
	 * This will force a rebuild of the descriptors the next time a client asks
	 * for them.
	 * </p>
	 * 
	 * @return an array of JavaEditorTextHoverDescriptor
	 * @since 2.1
	 */
	public void resetJavaEditorTextHoverDescriptors() {
		fJavaEditorTextHoverDescriptors = null;
	}

	/**
	 * Creates the PHP plugin standard groups in a context menu.
	 */
	public static void createStandardGroups(IMenuManager menu) {
		if (!menu.isEmpty())
			return;
		menu.add(new Separator(IContextMenuConstants.GROUP_NEW));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_GOTO));
		menu.add(new Separator(IContextMenuConstants.GROUP_OPEN));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_SHOW));
		menu.add(new Separator(IContextMenuConstants.GROUP_REORGANIZE));
		menu.add(new Separator(IContextMenuConstants.GROUP_GENERATE));
		menu.add(new Separator(IContextMenuConstants.GROUP_SEARCH));
		menu.add(new Separator(IContextMenuConstants.GROUP_BUILD));
		menu.add(new Separator(IContextMenuConstants.GROUP_ADDITIONS));
		menu.add(new Separator(IContextMenuConstants.GROUP_VIEWER_SETUP));
		menu.add(new Separator(IContextMenuConstants.GROUP_PROPERTIES));
	}

	public static IWorkbenchPage getActivePage() {
		return getDefault().internalGetActivePage();
	}

	public static Shell getActiveWorkbenchShell() {
		return getActiveWorkbenchWindow().getShell();
	}

	/**
	 * Returns an array of all editors that have an unsaved content. If the
	 * identical content is presented in more than one editor, only one of those
	 * editor parts is part of the result.
	 * 
	 * @return an array of all dirty editor parts.
	 */
	public static IEditorPart[] getDirtyEditors() {
		Set inputs = new HashSet();
		List result = new ArrayList(0);
		IWorkbench workbench = getDefault().getWorkbench();
		IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			IWorkbenchPage[] pages = windows[i].getPages();
			for (int x = 0; x < pages.length; x++) {
				IEditorPart[] editors = pages[x].getDirtyEditors();
				for (int z = 0; z < editors.length; z++) {
					IEditorPart ep = editors[z];
					IEditorInput input = ep.getEditorInput();
					if (!inputs.contains(input)) {
						inputs.add(input);
						result.add(ep);
					}
				}
			}
		}
		return (IEditorPart[]) result.toArray(new IEditorPart[result.size()]);
	}

	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	/**
	 * Returns the shared instance.
	 */
	public static PHPeclipsePlugin getDefault() {
		return plugin;
	}

	public static ImageDescriptorRegistry getImageDescriptorRegistry() {
		return getDefault().internalGetImageDescriptorRegistry();
	}

	static IPath getInstallLocation() {
		return new Path(getDefault().getBundle().getEntry("/").getFile());
	}

	// public static int getJVM() {
	// return jvm;
	// }

	public static String getPluginId() {
		return getDefault().getBundle().getSymbolicName();
	}

	/**
	 * Returns the standard display to be used. The method first checks, if the
	 * thread calling this method has an associated display. If so, this display
	 * is returned. Otherwise the method returns the default display.
	 */
	public static Display getStandardDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}

	// public static ExternalToolsPlugin getExternalTools() {
	// return externalTools;
	// }
	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	public static boolean isDebug() {
		return getDefault().isDebugging();
	}

	// public static void logErrorMessage(String message) {
	// log(new Status(IStatus.ERROR, getPluginId(),
	// JavaStatusConstants.INTERNAL_ERROR, message, null));
	// }
	//
	// public static void logErrorStatus(String message, IStatus status) {
	// if (status == null) {
	// logErrorMessage(message);
	// return;
	// }
	// MultiStatus multi= new MultiStatus(getPluginId(),
	// JavaStatusConstants.INTERNAL_ERROR, message, null);
	// multi.add(status);
	// log(multi);
	// }
	//
	// public static void log(Throwable e) {
	// log(new Status(IStatus.ERROR, getPluginId(),
	// JavaStatusConstants.INTERNAL_ERROR,
	// JavaUIMessages.getString("JavaPlugin.internal_error"), e)); //$NON-NLS-1$
	// }
	public static void log(int severity, String message) {
		Status status = new Status(severity, PLUGIN_ID, IStatus.OK, message,
				null);
		log(status);
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void log(Throwable t) {
		log(error(t));
	}

	public static void log(String message, Throwable t) {
		log(error(message, t));
	}

	public static void logErrorMessage(String message) {
		log(new Status(IStatus.ERROR, getPluginId(),
				IJavaStatusConstants.INTERNAL_ERROR, message, null));
	}

	public static IStatus error(Throwable t) {
		return error("PHPeclipsePlugin.internalErrorOccurred", t); //$NON-NLS-1$
	}

	public static IStatus error(String message, Throwable t) {
		return new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, message, t);
	}

	// private static void setJVM() {
	// String osName = System.getProperty("os.name");
	// if (osName.startsWith("Mac OS")) {
	// String mrjVersion = System.getProperty("mrj.version");
	// String majorMRJVersion = mrjVersion.substring(0, 3);
	// jvm = OTHER;
	// try {
	// double version = Double.valueOf(majorMRJVersion).doubleValue();
	// if (version == 2) {
	// jvm = MRJ_2_0;
	// } else if (version >= 2.1 && version < 3) {
	// jvm = MRJ_2_1;
	// } else if (version == 3.0) {
	// jvm = MRJ_3_0;
	// } else if (version >= 3.1) {
	// jvm = MRJ_3_1;
	// }
	// } catch (NumberFormatException nfe) {
	// }
	// } else if (osName.startsWith("Windows")) {
	// if (osName.indexOf("9") != -1) {
	// jvm = WINDOWS_9x;
	// } else {
	// jvm = WINDOWS_NT;
	// }
	// }
	// }

	// TODO: refactor this into a better method name !
	public synchronized ICompilationUnitDocumentProvider getCompilationUnitDocumentProvider() {
		if (fCompilationUnitDocumentProvider == null)
			fCompilationUnitDocumentProvider = new PHPDocumentProvider();
		return fCompilationUnitDocumentProvider;
	}

	/**
	 * Get the identifier index manager for the given project
	 * 
	 * @param iProject
	 *            the current project
	 * @return
	 */
	public IdentifierIndexManager getIndexManager(IProject iProject) {
		IPath path = iProject.getWorkingLocation(PHPeclipsePlugin.PLUGIN_ID);
		path = path.append("project.index");
		String indexFilename = path.toString();
		// try {
		// IdentDB db = IdentDB.getInstance();
		// } catch (ClassNotFoundException e) {
		// e.printStackTrace();
		// } catch (SQLException e) {
		// e.printStackTrace();
		// }
		IdentifierIndexManager indexManager = (IdentifierIndexManager) fIndexManagerMap
				.get(indexFilename);
		if (indexManager == null) {
			indexManager = new IdentifierIndexManager(indexFilename);
			fIndexManagerMap.put(indexFilename, indexManager);
		}
		return indexManager;
	}

	public synchronized IWorkingCopyManager getWorkingCopyManager() {
		if (fWorkingCopyManager == null) {
			ICompilationUnitDocumentProvider provider = getCompilationUnitDocumentProvider();
			fWorkingCopyManager = new WorkingCopyManager(provider);
		}
		return fWorkingCopyManager;
	}

	public synchronized MembersOrderPreferenceCache getMemberOrderPreferenceCache() {
		if (fMembersOrderPreferenceCache == null)
			fMembersOrderPreferenceCache = new MembersOrderPreferenceCache();
		return fMembersOrderPreferenceCache;
	}

	/**
	 * Returns the mockup preference store for firing events and registering
	 * listeners on project setting changes. Temporary solution.
	 */
	public MockupPreferenceStore getMockupPreferenceStore() {
		if (fMockupPreferenceStore == null)
			fMockupPreferenceStore = new MockupPreferenceStore();

		return fMockupPreferenceStore;
	}

	public synchronized ProblemMarkerManager getProblemMarkerManager() {
		if (fProblemMarkerManager == null)
			fProblemMarkerManager = new ProblemMarkerManager();
		return fProblemMarkerManager;
	}

	// public synchronized JavaTextTools getJavaTextTools() {
	// if (fJavaTextTools == null)
	// fJavaTextTools = new JavaTextTools(getPreferenceStore());
	// return fJavaTextTools;
	// }
	public synchronized JavaTextTools getJavaTextTools() {
		if (fJavaTextTools == null)
			fJavaTextTools = new JavaTextTools(getPreferenceStore(), JavaCore
					.getPlugin().getPluginPreferences());
		return fJavaTextTools;
	}

	public IFile getLastEditorFile() {
		return fLastEditorFile;
	}

	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not
	 * found.
	 */
	// public static String getResourceString(String key) {
	// ResourceBundle bundle =
	// PHPeclipsePlugin.getDefault().getResourceBundle();
	// try {
	// return bundle.getString(key);
	// } catch (MissingResourceException e) {
	// return key;
	// }
	// }
	/**
	 * Returns the plugin's resource bundle,
	 */
	// public ResourceBundle getResourceBundle() {
	// return resourceBundle;
	// }
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		String operatingSystem = Platform.getOS();
		// maxosx, linux, solaris, win32,...
		try {
			InputStream is = getDefault()
					.openStream(
							new Path("prefs/default_" + operatingSystem
									+ ".properties"));
			PropertyResourceBundle resourceBundle = new PropertyResourceBundle(
					is);
			Enumeration e = resourceBundle.getKeys();
			String key;
			while (e.hasMoreElements()) {
				key = (String) e.nextElement();
				store.setDefault(key, resourceBundle.getString(key));
			}
		} catch (Exception e) {
			// no default properties found
			if (operatingSystem.equals(Platform.OS_WIN32)) {
				// store.setDefault(PHP_RUN_PREF, "c:\\apache\\php\\php.exe");
				// store.setDefault(EXTERNAL_PARSER_PREF, "c:\\apache\\php\\php
				// -l -f {0}");
				// store.setDefault(MYSQL_RUN_PREF,
				// "c:\\apache\\mysql\\bin\\mysqld-nt.exe");
				// store.setDefault(APACHE_RUN_PREF, "c:\\apache\\apache.exe");
				// store.setDefault(XAMPP_START_PREF,
				// "c:\\xampp\\xampp_start.exe");
				// store.setDefault(XAMPP_STOP_PREF,
				// "c:\\xampp\\xampp_stop.exe");
				// store.setDefault(
				// ETC_HOSTS_PATH_PREF,
				// "c:\\windows\\system32\\drivers\\etc\\hosts");
			} else {
				// store.setDefault(PHP_RUN_PREF, "/apache/php/php");
				// store.setDefault(EXTERNAL_PARSER_PREF, "/apache/php/php -l -f
				// {0}");
				// store.setDefault(MYSQL_RUN_PREF, "/apache/mysql/bin/mysqld");
				// store.setDefault(APACHE_RUN_PREF, "/apache/apache");
				// store.setDefault(XAMPP_START_PREF, "xamp/xampp_start");
				// store.setDefault(XAMPP_STOP_PREF, "xampp/xampp_stop");
			}
			// store.setDefault(MYSQL_PREF, "--standalone");
			// store.setDefault(APACHE_START_PREF, "-c \"DocumentRoot
			// \"{0}\"\"");
			// store.setDefault(APACHE_STOP_PREF, "-k shutdown");
			// store.setDefault(APACHE_RESTART_PREF, "-k restart");
			// store.setDefault(MYSQL_START_BACKGROUND, "true");
			// store.setDefault(APACHE_START_BACKGROUND, "true");
			// store.setDefault(APACHE_STOP_BACKGROUND, "true");
			// store.setDefault(APACHE_RESTART_BACKGROUND, "true");
		}

		// php syntax highlighting
		store.setDefault(PHP_USERDEF_XMLFILE, "");
		PreferenceConverter.setDefault(store, PHP_TAG, PHPColorProvider.TAG);
		PreferenceConverter.setDefault(store, PHP_KEYWORD,
				PHPColorProvider.KEYWORD);
		PreferenceConverter.setDefault(store, PHP_VARIABLE,
				PHPColorProvider.VARIABLE);
		PreferenceConverter.setDefault(store, PHP_VARIABLE_DOLLAR,
				PHPColorProvider.VARIABLE);
		PreferenceConverter.setDefault(store, PHP_FUNCTIONNAME,
				PHPColorProvider.FUNCTION_NAME);
		PreferenceConverter.setDefault(store, PHP_CONSTANT,
				PHPColorProvider.CONSTANT);
		PreferenceConverter.setDefault(store, PHP_TYPE, PHPColorProvider.TYPE);
		PreferenceConverter.setDefault(store, PHP_DEFAULT,
				PHPColorProvider.DEFAULT);
		PreferenceConverter.setDefault(store, PHPDOC_KEYWORD,
				PHPColorProvider.PHPDOC_KEYWORD);
		PreferenceConverter.setDefault(store, PHPDOC_TAG,
				PHPColorProvider.PHPDOC_TAG);
		PreferenceConverter.setDefault(store, PHPDOC_LINK,
				PHPColorProvider.PHPDOC_LINK);
		PreferenceConverter.setDefault(store, PHPDOC_DEFAULT,
				PHPColorProvider.PHPDOC_DEFAULT);

		PreferenceConverter.setDefault(store, EDITOR_PHP_KEYWORD_RETURN_COLOR,
				new RGB(127, 0, 85));
		store.setDefault(EDITOR_PHP_KEYWORD_RETURN_BOLD, true);
		store.setDefault(EDITOR_PHP_KEYWORD_RETURN_ITALIC, false);

		PreferenceConverter.setDefault(store, EDITOR_PHP_OPERATOR_COLOR,
				new RGB(0, 0, 0));
		store.setDefault(EDITOR_PHP_OPERATOR_BOLD, false);
		store.setDefault(EDITOR_PHP_OPERATOR_ITALIC, false);

		PreferenceConverter.setDefault(store, EDITOR_PHP_BRACE_OPERATOR_COLOR,
				new RGB(0, 0, 0));
		store.setDefault(EDITOR_PHP_BRACE_OPERATOR_BOLD, false);
		store.setDefault(EDITOR_PHP_BRACE_OPERATOR_ITALIC, false);

		// this will initialize the static fields in the syntaxrdr class
		new PHPSyntaxRdr();
		JavaCore.initializeDefaultPluginPreferences();
		PreferenceConstants.initializeDefaultValues(store);
		// externalTools.initializeDefaultPreferences(store);
		// MarkerAnnotationPreferences.initializeDefaultValues(store);
	}

	private IWorkbenchPage internalGetActivePage() {
		IWorkbenchWindow window = getWorkbench().getActiveWorkbenchWindow();
		if (window != null)
			return window.getActivePage();
		return null;
	}

	private ImageDescriptorRegistry internalGetImageDescriptorRegistry() {
		if (fImageDescriptorRegistry == null)
			fImageDescriptorRegistry = new ImageDescriptorRegistry();
		return fImageDescriptorRegistry;
	}

	/**
	 * Open a file in the Workbench that may or may not exist in the workspace.
	 * Must be run on the UI thread.
	 * 
	 * @param filename
	 * @throws CoreException
	 */
	public ITextEditor openFileInTextEditor(String filename)
			throws CoreException {
		// reject directories
		if (new File(filename).isDirectory())
			return null;
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getWorkbenchWindows()[0];
		IWorkbenchPage page = window.getActivePage();
		IPath path = new Path(filename);
		// If the file exists in the workspace, open it
		IFile file = getWorkspace().getRoot().getFile(path);
		IEditorPart editor;
		ITextEditor textEditor;
		if (file != null && file.exists()) {
			editor = IDE.openEditor(page, file, true);
			textEditor = (ITextEditor) editor.getAdapter(ITextEditor.class);
		} else {
			// Otherwise open the stream directly
			if (page == null)
				return null;
			FileStorage storage = new FileStorage(path);
			IEditorRegistry registry = getWorkbench().getEditorRegistry();
			IEditorDescriptor desc = registry.getDefaultEditor(filename);
			if (desc == null) {
				desc = registry
						.findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
				// desc = registry.getDefaultEditor();
			}
			IEditorInput input = new ExternalEditorInput(storage);
			editor = page.openEditor(input, desc.getId());
			textEditor = (ITextEditor) editor.getAdapter(ITextEditor.class);
			// If the storage provider is not ours, we can't guarantee
			// read/write.
			if (textEditor != null) {
				IDocumentProvider documentProvider = textEditor
						.getDocumentProvider();
				if (!(documentProvider instanceof ExternalStorageDocumentProvider)) {
					storage.setReadOnly();
				}
			}
		}
		return textEditor;
	}

	/**
	 * Open a file in the Workbench that may or may not exist in the workspace.
	 * Must be run on the UI thread.
	 * 
	 * @param filename
	 * @param line
	 * @throws CoreException
	 */
	public void openFileAndGotoLine(String filename, int line)
			throws CoreException {
		ITextEditor textEditor = openFileInTextEditor(filename);
		if (textEditor != null) {
			// If a line number was given, go to it
			if (line > 0) {
				try {
					line--; // document is 0 based
					IDocument document = textEditor.getDocumentProvider()
							.getDocument(textEditor.getEditorInput());
					textEditor.selectAndReveal(document.getLineOffset(line),
							document.getLineLength(line));
				} catch (BadLocationException e) {
					// invalid text position -> do nothing
				}
			}
		}
	}

	/**
	 * Open a file in the Workbench that may or may not exist in the workspace.
	 * Must be run on the UI thread.
	 * 
	 * @param filename
	 * @param offset
	 * @throws CoreException
	 */
	public void openFileAndGotoOffset(String filename, int offset, int length)
			throws CoreException {
		ITextEditor textEditor = openFileInTextEditor(filename);
		if (textEditor != null) {
			// If a line number was given, go to it
			if (offset >= 0) {
				IDocument document = textEditor.getDocumentProvider()
						.getDocument(textEditor.getEditorInput());
				textEditor.selectAndReveal(offset, length);
			}
		}
	}

	public void openFileAndFindString(String filename, String findString)
			throws CoreException {
		ITextEditor textEditor = openFileInTextEditor(filename);
		if (textEditor != null) {
			// If a string was given, go to it
			if (findString != null) {
				try {
					IDocument document = textEditor.getDocumentProvider()
							.getDocument(textEditor.getEditorInput());
					int offset = document.search(0, findString, true, false,
							true);
					textEditor.selectAndReveal(offset, findString.length());
				} catch (BadLocationException e) {
					// invalid text position -> do nothing
				}
			}
		}
	}

	public void setLastEditorFile(IFile textEditor) {
		this.fLastEditorFile = textEditor;
	}

	/*
	 * @see org.eclipse.core.runtime.Plugin#stop
	 */
	public void stop(BundleContext context) throws Exception {
		try {
			// JavaCore.stop(this, context);
			plugin.savePluginPreferences();
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			workspace.removeResourceChangeListener(JavaModelManager
					.getJavaModelManager().deltaState);
			workspace.removeSaveParticipant(plugin);

			JavaModelManager.getJavaModelManager().shutdown();

			// save the information from the php index files if necessary
			Collection collection = fIndexManagerMap.values();
			Iterator iterator = collection.iterator();
			IdentifierIndexManager indexManager = null;
			while (iterator.hasNext()) {
				indexManager = (IdentifierIndexManager) iterator.next();
				indexManager.writeFile();
			}
			if (fImageDescriptorRegistry != null)
				fImageDescriptorRegistry.dispose();

			// AllTypesCache.terminate();

			if (fImageDescriptorRegistry != null)
				fImageDescriptorRegistry.dispose();

			unregisterAdapters();

			// if (fASTProvider != null) {
			// fASTProvider.dispose();
			// fASTProvider= null;
			// }

			if (fWorkingCopyManager != null) {
				fWorkingCopyManager.shutdown();
				fWorkingCopyManager = null;
			}

			if (fCompilationUnitDocumentProvider != null) {
				fCompilationUnitDocumentProvider.shutdown();
				fCompilationUnitDocumentProvider = null;
			}

			if (fJavaTextTools != null) {
				fJavaTextTools.dispose();
				fJavaTextTools = null;
			}
			// JavaDocLocations.shutdownJavadocLocations();

			uninstallPreferenceStoreBackwardsCompatibility();

			// RefactoringCore.getUndoManager().shutdown();
		} finally {
			super.stop(context);
		}
	}

	/**
	 * Installs backwards compatibility for the preference store.
	 */
	private void installPreferenceStoreBackwardsCompatibility() {

		/*
		 * Installs backwards compatibility: propagate the Java editor font from
		 * a pre-2.1 plug-in to the Platform UI's preference store to preserve
		 * the Java editor font from a pre-2.1 workspace. This is done only
		 * once.
		 */
		String fontPropagatedKey = "fontPropagated"; //$NON-NLS-1$
		if (getPreferenceStore().contains(JFaceResources.TEXT_FONT)
				&& !getPreferenceStore().isDefault(JFaceResources.TEXT_FONT)) {
			if (!getPreferenceStore().getBoolean(fontPropagatedKey))
				PreferenceConverter
						.setValue(PlatformUI.getWorkbench()
								.getPreferenceStore(),
								PreferenceConstants.EDITOR_TEXT_FONT,
								PreferenceConverter.getFontDataArray(
										getPreferenceStore(),
										JFaceResources.TEXT_FONT));
		}
		getPreferenceStore().setValue(fontPropagatedKey, true);

		/*
		 * Backwards compatibility: set the Java editor font in this plug-in's
		 * preference store to let older versions access it. Since 2.1 the Java
		 * editor font is managed by the workbench font preference page.
		 */
		PreferenceConverter.putValue(getPreferenceStore(),
				JFaceResources.TEXT_FONT, JFaceResources.getFontRegistry()
						.getFontData(PreferenceConstants.EDITOR_TEXT_FONT));

		fFontPropertyChangeListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (PreferenceConstants.EDITOR_TEXT_FONT.equals(event
						.getProperty()))
					PreferenceConverter.putValue(getPreferenceStore(),
							JFaceResources.TEXT_FONT,
							JFaceResources.getFontRegistry().getFontData(
									PreferenceConstants.EDITOR_TEXT_FONT));
			}
		};
		JFaceResources.getFontRegistry().addListener(
				fFontPropertyChangeListener);
	}

	/**
	 * Uninstalls backwards compatibility for the preference store.
	 */
	private void uninstallPreferenceStoreBackwardsCompatibility() {
		JFaceResources.getFontRegistry().removeListener(
				fFontPropertyChangeListener);
		// getPreferenceStore().removePropertyChangeListener(fPropertyChangeListener);
	}

	/*
	 * (non - Javadoc) Method declared in Plugin
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);

		// JavaCore.start(this, context);
		final JavaModelManager modelManager = JavaModelManager
				.getJavaModelManager();
		try {
			modelManager.configurePluginDebugOptions();

			// request state folder creation (workaround 19885)
			getStateLocation();
			// retrieve variable values
			PHPeclipsePlugin.getDefault().getPluginPreferences()
					.addPropertyChangeListener(
							new JavaModelManager.PluginPreferencesListener());
			// manager.loadVariablesAndContainers();

			final IWorkspace workspace = ResourcesPlugin.getWorkspace();
			workspace.addResourceChangeListener(modelManager.deltaState,
					IResourceChangeEvent.PRE_BUILD
							| IResourceChangeEvent.POST_BUILD
							| IResourceChangeEvent.POST_CHANGE
							| IResourceChangeEvent.PRE_DELETE
							| IResourceChangeEvent.PRE_CLOSE);

			ISavedState savedState = workspace.addSaveParticipant(
					PHPeclipsePlugin.this, modelManager);

			// process deltas since last activated in indexer thread so that
			// indexes are up-to-date.
			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=38658
// This causes timeout at EclipseLazyStarter
//			Job processSavedState = new Job(Util.bind("savedState.jobName")) { //$NON-NLS-1$
//				protected IStatus run(IProgressMonitor monitor) {
//					try {
//						// add save participant and process delta atomically
//						// see
//						// https://bugs.eclipse.org/bugs/show_bug.cgi?id=59937
//						workspace.run(new IWorkspaceRunnable() {
//							public void run(IProgressMonitor progress)
//									throws CoreException {
//								ISavedState savedState = workspace
//										.addSaveParticipant(
//												PHPeclipsePlugin.this,
//												modelManager);
//								if (savedState != null) {
//									// the event type coming from the saved
//									// state is always POST_AUTO_BUILD
//									// force it to be POST_CHANGE so that the
//									// delta processor can handle it
//									modelManager.deltaState.getDeltaProcessor().overridenEventType = IResourceChangeEvent.POST_CHANGE;
//									savedState
//											.processResourceChangeEvents(modelManager.deltaState);
//								}
//							}
//						}, monitor);
//					} catch (CoreException e) {
//						return e.getStatus();
//					}
//					return Status.OK_STATUS;
//				}
//			};
// Replace Job + IWorkspace.run() to WorkspaceJob
			WorkspaceJob processSavedState = new WorkspaceJob(
					Util.bind("savedState.jobName")) { //$NON-NLS-1$
				public IStatus runInWorkspace(IProgressMonitor monitor)
						throws CoreException {
					ISavedState savedState = workspace.addSaveParticipant(
							PHPeclipsePlugin.this, modelManager);
					if (savedState != null) {
						modelManager.deltaState.getDeltaProcessor().overridenEventType
								= IResourceChangeEvent.POST_CHANGE;
						savedState.processResourceChangeEvents(modelManager.deltaState);
					}
					return Status.OK_STATUS;
				}
			};
			processSavedState.setSystem(true);
			processSavedState.setPriority(Job.SHORT); // process asap
			processSavedState.schedule();
		} catch (RuntimeException e) {
			modelManager.shutdown();
			throw e;
		}

		registerAdapters();

		// if (USE_WORKING_COPY_OWNERS) {
		WorkingCopyOwner.setPrimaryBufferProvider(new WorkingCopyOwner() {
			public IBuffer createBuffer(ICompilationUnit workingCopy) {
				ICompilationUnit original = workingCopy.getPrimary();
				IResource resource = original.getResource();
				if (resource instanceof IFile)
					return new DocumentAdapter(workingCopy, (IFile) resource);
				return DocumentAdapter.NULL;
			}
		});
		// }

		installPreferenceStoreBackwardsCompatibility();

	}

	private void registerAdapters() {
		fJavaElementAdapterFactory = new JavaElementAdapterFactory();
		fResourceAdapterFactory = new ResourceAdapterFactory();

		IAdapterManager manager = Platform.getAdapterManager();
		manager
				.registerAdapters(fJavaElementAdapterFactory,
						IJavaElement.class);
		manager.registerAdapters(fResourceAdapterFactory, IResource.class);
	}

	private void unregisterAdapters() {
		IAdapterManager manager = Platform.getAdapterManager();
		manager.unregisterAdapters(fJavaElementAdapterFactory);
		manager.unregisterAdapters(fResourceAdapterFactory);
	}

	/**
	 * Returns a combined preference store, this store is read-only.
	 * 
	 * @return the combined preference store
	 * 
	 * @since 3.0
	 */
	public IPreferenceStore getCombinedPreferenceStore() {
		if (fCombinedPreferenceStore == null) {
			IPreferenceStore generalTextStore = EditorsUI.getPreferenceStore();
			fCombinedPreferenceStore = new ChainedPreferenceStore(
					new IPreferenceStore[] {
							getPreferenceStore(),
							new PreferencesAdapter(PHPeclipsePlugin
									.getDefault().getPluginPreferences()),
							generalTextStore });
		}
		return fCombinedPreferenceStore;
	}

	public synchronized IBufferFactory getBufferFactory() {
		if (fBufferFactory == null)
			fBufferFactory = new CustomBufferFactory();
		return fBufferFactory;
	}

	/**
	 * Returns the registry of the extensions to the
	 * <code>net.sourceforge.phpdt.ui.javaFoldingStructureProvider</code>
	 * extension point.
	 * 
	 * @return the registry of contributed
	 *         <code>IJavaFoldingStructureProvider</code>
	 * @since 3.0
	 */
	public synchronized JavaFoldingStructureProviderRegistry getFoldingStructureProviderRegistry() {
		if (fFoldingStructureProviderRegistry == null)
			fFoldingStructureProviderRegistry = new JavaFoldingStructureProviderRegistry();
		return fFoldingStructureProviderRegistry;
	}

	/**
	 * Runs the given action as an atomic Java model operation.
	 * <p>
	 * After running a method that modifies java elements, registered listeners
	 * receive after-the-fact notification of what just transpired, in the form
	 * of a element changed event. This method allows clients to call a number
	 * of methods that modify java elements and only have element changed event
	 * notifications reported at the end of the entire batch.
	 * </p>
	 * <p>
	 * If this method is called outside the dynamic scope of another such call,
	 * this method runs the action and then reports a single element changed
	 * event describing the net effect of all changes done to java elements by
	 * the action.
	 * </p>
	 * <p>
	 * If this method is called in the dynamic scope of another such call, this
	 * method simply runs the action.
	 * </p>
	 * 
	 * @param action
	 *            the action to perform
	 * @param monitor
	 *            a progress monitor, or <code>null</code> if progress
	 *            reporting and cancellation are not desired
	 * @exception CoreException
	 *                if the operation failed.
	 * @since 2.1
	 */
	public static void run(IWorkspaceRunnable action, IProgressMonitor monitor)
			throws CoreException {
		run(action, ResourcesPlugin.getWorkspace().getRoot(), monitor);
	}

	/**
	 * Runs the given action as an atomic Java model operation.
	 * <p>
	 * After running a method that modifies java elements, registered listeners
	 * receive after-the-fact notification of what just transpired, in the form
	 * of a element changed event. This method allows clients to call a number
	 * of methods that modify java elements and only have element changed event
	 * notifications reported at the end of the entire batch.
	 * </p>
	 * <p>
	 * If this method is called outside the dynamic scope of another such call,
	 * this method runs the action and then reports a single element changed
	 * event describing the net effect of all changes done to java elements by
	 * the action.
	 * </p>
	 * <p>
	 * If this method is called in the dynamic scope of another such call, this
	 * method simply runs the action.
	 * </p>
	 * <p>
	 * The supplied scheduling rule is used to determine whether this operation
	 * can be run simultaneously with workspace changes in other threads. See
	 * <code>IWorkspace.run(...)</code> for more details.
	 * </p>
	 * 
	 * @param action
	 *            the action to perform
	 * @param rule
	 *            the scheduling rule to use when running this operation, or
	 *            <code>null</code> if there are no scheduling restrictions
	 *            for this operation.
	 * @param monitor
	 *            a progress monitor, or <code>null</code> if progress
	 *            reporting and cancellation are not desired
	 * @exception CoreException
	 *                if the operation failed.
	 * @since 3.0
	 */
	public static void run(IWorkspaceRunnable action, ISchedulingRule rule,
			IProgressMonitor monitor) throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		if (workspace.isTreeLocked()) {
			new BatchOperation(action).run(monitor);
		} else {
			// use IWorkspace.run(...) to ensure that a build will be done in
			// autobuild mode
			workspace.run(new BatchOperation(action), rule,
					IWorkspace.AVOID_UPDATE, monitor);
		}
	}

	/**
	 * Returns the template context type registry for the java plugin.
	 * 
	 * @return the template context type registry for the java plugin
	 * @since 3.0
	 */
	public ContextTypeRegistry getTemplateContextRegistry() {
		if (fContextTypeRegistry == null) {
			fContextTypeRegistry = new ContributionContextTypeRegistry();

			fContextTypeRegistry.addContextType(new JavaContextType());
			fContextTypeRegistry.addContextType(new JavaDocContextType());
			fContextTypeRegistry.addContextType(new HTMLContextType());
		}

		return fContextTypeRegistry;
	}

	/**
	 * Returns the template store for the java editor templates.
	 * 
	 * @return the template store for the java editor templates
	 * @since 3.0
	 */
	public TemplateStore getTemplateStore() {
		if (fTemplateStore == null) {
			fTemplateStore = new ContributionTemplateStore(
					getTemplateContextRegistry(), getPreferenceStore(),
					TEMPLATES_KEY);
			try {
				fTemplateStore.load();
			} catch (IOException e) {
				log(e);
			}
		}

		return fTemplateStore;
	}

	/**
	 * Returns the template context type registry for the code generation
	 * templates.
	 * 
	 * @return the template context type registry for the code generation
	 *         templates
	 * @since 3.0
	 */
	public ContextTypeRegistry getCodeTemplateContextRegistry() {
		if (fCodeTemplateContextTypeRegistry == null) {
			fCodeTemplateContextTypeRegistry = new ContributionContextTypeRegistry();

			CodeTemplateContextType
					.registerContextTypes(fCodeTemplateContextTypeRegistry);
		}

		return fCodeTemplateContextTypeRegistry;
	}

	/**
	 * Returns the template store for the code generation templates.
	 * 
	 * @return the template store for the code generation templates
	 * @since 3.0
	 */
	public TemplateStore getCodeTemplateStore() {
		if (fCodeTemplateStore == null) {
			fCodeTemplateStore = new ContributionTemplateStore(
					getCodeTemplateContextRegistry(), getPreferenceStore(),
					CODE_TEMPLATES_KEY);
			try {
				fCodeTemplateStore.load();
			} catch (IOException e) {
				log(e);
			}
		}

		return fCodeTemplateStore;
	}
}