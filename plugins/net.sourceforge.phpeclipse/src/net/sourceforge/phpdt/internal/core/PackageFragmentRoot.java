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

import java.util.ArrayList;
import java.util.Map;

import net.sourceforge.phpdt.core.IJavaElement;
import net.sourceforge.phpdt.core.IJavaModelStatusConstants;
import net.sourceforge.phpdt.core.IPackageFragment;
import net.sourceforge.phpdt.core.IPackageFragmentRoot;
import net.sourceforge.phpdt.core.JavaCore;
import net.sourceforge.phpdt.core.JavaModelException;
import net.sourceforge.phpdt.core.WorkingCopyOwner;
import net.sourceforge.phpdt.core.compiler.CharOperation;
import net.sourceforge.phpdt.internal.core.util.MementoTokenizer;
import net.sourceforge.phpdt.internal.core.util.Util;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;

/**
 * @see IPackageFragmentRoot
 */
public class PackageFragmentRoot extends Openable implements
		IPackageFragmentRoot {

	/**
	 * The delimiter between the source path and root path in the attachment
	 * server property.
	 */
	protected final static char ATTACHMENT_PROPERTY_DELIMITER = '*';

	/*
	 * No source attachment property
	 */
	protected final static String NO_SOURCE_ATTACHMENT = ""; //$NON-NLS-1$

	/*
	 * No source mapper singleton
	 */
	// protected final static SourceMapper NO_SOURCE_MAPPER = new
	// SourceMapper();
	/**
	 * The resource associated with this root. (an IResource or a java.io.File
	 * (for external jar only))
	 */
	protected Object resource;

	/**
	 * Constructs a package fragment root which is the root of the java package
	 * directory hierarchy.
	 */
	protected PackageFragmentRoot(IResource resource, JavaProject project,
			String name) {
		super(project, name);
		this.resource = resource;
	}

	/**
	 * @see Openable
	 */
	protected boolean buildStructure(OpenableElementInfo info,
			IProgressMonitor pm, Map newElements, IResource underlyingResource)
			throws JavaModelException {

		// check whether this pkg fragment root can be opened
		if (!resourceExists()) { // || !isOnClasspath()) {
			throw newNotPresentException();
		}

		((PackageFragmentRootInfo) info)
				.setRootKind(determineKind(underlyingResource));
		return computeChildren(info, newElements);
	}

	/**
	 * Returns the root's kind - K_SOURCE or K_BINARY, defaults to K_SOURCE if
	 * it is not on the classpath.
	 * 
	 * @exception NotPresentException
	 *                if the project and root do not exist.
	 */
	protected int determineKind(IResource underlyingResource)
			throws JavaModelException {
		// IClasspathEntry[] entries=
		// ((JavaProject)getJavaProject()).getExpandedClasspath(true);
		// for (int i= 0; i < entries.length; i++) {
		// IClasspathEntry entry= entries[i];
		// if (entry.getPath().equals(underlyingResource.getFullPath())) {
		// return entry.getContentKind();
		// }
		// }
		return IPackageFragmentRoot.K_SOURCE;
	}

	/**
	 * Compute the package fragment children of this package fragment root.
	 * 
	 * @exception JavaModelException
	 *                The resource associated with this package fragment root
	 *                does not exist
	 */
	protected boolean computeChildren(OpenableElementInfo info, Map newElements)
			throws JavaModelException {
		// Note the children are not opened (so not added to newElements) for a
		// regular package fragment root
		// Howver they are opened for a Jar package fragment root (see
		// JarPackageFragmentRoot#computeChildren)
		try {
			// the underlying resource may be a folder or a project (in the case
			// that the project folder
			// is actually the package fragment root)
			IResource underlyingResource = getResource();
			if (underlyingResource.getType() == IResource.FOLDER
					|| underlyingResource.getType() == IResource.PROJECT) {
				ArrayList vChildren = new ArrayList(5);
				IContainer rootFolder = (IContainer) underlyingResource;
				// char[][] inclusionPatterns = fullInclusionPatternChars();
				char[][] exclusionPatterns = fullExclusionPatternChars();
				computeFolderChildren(rootFolder, !Util.isExcluded(rootFolder,
						exclusionPatterns), "", vChildren, exclusionPatterns); //$NON-NLS-1$

				IJavaElement[] children = new IJavaElement[vChildren.size()];
				vChildren.toArray(children);
				info.setChildren(children);
			}
		} catch (JavaModelException e) {
			// problem resolving children; structure remains unknown
			info.setChildren(new IJavaElement[] {});
			throw e;
		}
		return true;
	}

	/**
	 * Starting at this folder, create package fragments and add the fragments
	 * that are not exclused to the collection of children.
	 * 
	 * @exception JavaModelException
	 *                The resource associated with this package fragment does
	 *                not exist
	 */
	protected void computeFolderChildren(IContainer folder, boolean isIncluded,
			String prefix, ArrayList vChildren, char[][] exclusionPatterns)
			throws JavaModelException {
		// , char[][] inclusionPatterns, char[][] exclusionPatterns) throws
		// JavaModelException {

		if (isIncluded) {
			IPackageFragment pkg = getPackageFragment(prefix);
			vChildren.add(pkg);
		}
		try {
			JavaProject javaProject = (JavaProject) getJavaProject();
			IResource[] members = folder.members();
			boolean hasIncluded = isIncluded;
			for (int i = 0, max = members.length; i < max; i++) {
				IResource member = members[i];
				String memberName = member.getName();

				switch (member.getType()) {

				case IResource.FOLDER:
					if (Util.isValidFolderNameForPackage(memberName)) {
						boolean isMemberIncluded = !Util.isExcluded(member,
								exclusionPatterns);
						// keep looking inside as long as included already, or
						// may have child included due to inclusion patterns
						// if (isMemberIncluded || inclusionPatterns != null) {
						// // eliminate binary output only if nested inside
						// direct subfolders
						// if (javaProject.contains(member)) {
						// String newPrefix;
						// if (prefix.length() == 0) {
						// newPrefix = memberName;
						// } else {
						// newPrefix = prefix + "." + memberName; //$NON-NLS-1$
						// }
						// computeFolderChildren((IFolder) member,
						// isMemberIncluded, newPrefix, vChildren,
						// inclusionPatterns,
						// exclusionPatterns);
						// }
						// }
					}
					break;
				case IResource.FILE:
					// inclusion filter may only include files, in which case we
					// still want to include the immediate parent package
					// (lazily)
					if (!hasIncluded
							&& Util.isValidCompilationUnitName(memberName)
							&& !Util.isExcluded(member, exclusionPatterns)) {
						hasIncluded = true;
						IPackageFragment pkg = getPackageFragment(prefix);
						vChildren.add(pkg);
					}
					break;
				}
			}
		} catch (IllegalArgumentException e) {
			throw new JavaModelException(e,
					IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST); // could
																		// be
																		// thrown
																		// by
																		// ElementTree
																		// when
																		// path
			// is not found
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}
	}

	/**
	 * @see IPackageFragmentRoot
	 */
	// public void attachSource(IPath sourcePath, IPath rootPath,
	// IProgressMonitor monitor) throws JavaModelException {
	// try {
	// verifyAttachSource(sourcePath);
	// if (monitor != null) {
	// monitor.beginTask(ProjectPrefUtil.bind("element.attachingSource"), 2);
	// //$NON-NLS-1$
	// }
	// SourceMapper oldMapper= getSourceMapper();
	// IWorkspace workspace = ResourcesPlugin.getWorkspace();
	// boolean rootNeedsToBeClosed= false;
	//
	// if (sourcePath == null) {
	// //source being detached
	// rootNeedsToBeClosed= true;
	// setSourceMapper(null);
	// /* Disable deltas (see 1GDTUSD)
	// // fire a delta to notify the UI about the source detachement.
	// JavaModelManager manager = (JavaModelManager)
	// JavaModelManager.getJavaModelManager();
	// JavaModel model = (JavaModel) getJavaModel();
	// JavaElementDelta attachedSourceDelta = new JavaElementDelta(model);
	// attachedSourceDelta .sourceDetached(this); // this would be a
	// PackageFragmentRoot
	// manager.registerResourceDelta(attachedSourceDelta );
	// manager.fire(); // maybe you want to fire the change later. Let us know
	// about it.
	// */
	// } else {
	// /*
	// // fire a delta to notify the UI about the source attachement.
	// JavaModelManager manager = (JavaModelManager)
	// JavaModelManager.getJavaModelManager();
	// JavaModel model = (JavaModel) getJavaModel();
	// JavaElementDelta attachedSourceDelta = new JavaElementDelta(model);
	// attachedSourceDelta .sourceAttached(this); // this would be a
	// PackageFragmentRoot
	// manager.registerResourceDelta(attachedSourceDelta );
	// manager.fire(); // maybe you want to fire the change later. Let us know
	// about it.
	// */
	//
	// //check if different from the current attachment
	// IPath storedSourcePath= getSourceAttachmentPath();
	// IPath storedRootPath= getSourceAttachmentRootPath();
	// if (monitor != null) {
	// monitor.worked(1);
	// }
	// if (storedSourcePath != null) {
	// if (!(storedSourcePath.equals(sourcePath) && (rootPath != null &&
	// rootPath.equals(storedRootPath)) || storedRootPath == null))
	// {
	// rootNeedsToBeClosed= true;
	// }
	// }
	// // check if source path is valid
	// Object target = JavaModel.getTarget(workspace.getRoot(), sourcePath,
	// false);
	// if (target == null) {
	// if (monitor != null) {
	// monitor.done();
	// }
	// throw new JavaModelException(new
	// JavaModelStatus(IJavaModelStatusConstants.INVALID_PATH, sourcePath));
	// }
	// SourceMapper mapper = createSourceMapper(sourcePath, rootPath);
	// if (rootPath == null && mapper.rootPath != null) {
	// // as a side effect of calling the SourceMapper constructor, the root
	// path was computed
	// rootPath = new Path(mapper.rootPath);
	// }
	// setSourceMapper(mapper);
	// }
	// if (sourcePath == null) {
	// setSourceAttachmentProperty(null); //remove the property
	// } else {
	// //set the property to the path of the mapped source
	// setSourceAttachmentProperty(
	// sourcePath.toString()
	// + (rootPath == null ? "" : (ATTACHMENT_PROPERTY_DELIMITER +
	// rootPath.toString()))); //$NON-NLS-1$
	// }
	// if (rootNeedsToBeClosed) {
	// if (oldMapper != null) {
	// oldMapper.close();
	// }
	// BufferManager manager= BufferManager.getDefaultBufferManager();
	// Enumeration openBuffers= manager.getOpenBuffers();
	// while (openBuffers.hasMoreElements()) {
	// IBuffer buffer= (IBuffer) openBuffers.nextElement();
	// IOpenable possibleMember= buffer.getOwner();
	// if (isAncestorOf((IJavaElement) possibleMember)) {
	// buffer.close();
	// }
	// }
	// if (monitor != null) {
	// monitor.worked(1);
	// }
	// }
	// } catch (JavaModelException e) {
	// setSourceAttachmentProperty(null); // loose info - will be recomputed
	// throw e;
	// } finally {
	// if (monitor != null) {
	// monitor.done();
	// }
	// }
	// }
	// SourceMapper createSourceMapper(IPath sourcePath, IPath rootPath) {
	// SourceMapper mapper = new SourceMapper(
	// sourcePath,
	// rootPath == null ? null : rootPath.toOSString(),
	// this.isExternal() ? JavaCore.getOptions() :
	// this.getJavaProject().getOptions(true)); // only project options if
	// associated with
	// resource
	// return mapper;
	// }
	/*
	 * @see net.sourceforge.phpdt.core.IPackageFragmentRoot#delete
	 */
	// public void delete(
	// int updateResourceFlags,
	// int updateModelFlags,
	// IProgressMonitor monitor)
	// throws JavaModelException {
	//
	// DeletePackageFragmentRootOperation op = new
	// DeletePackageFragmentRootOperation(this, updateResourceFlags,
	// updateModelFlags);
	// runOperation(op, monitor);
	// }
	/**
	 * This root is being closed. If this root has an associated source
	 * attachment, close it too.
	 * 
	 * @see JavaElement
	 */
	// protected void closing(Object info) throws JavaModelException { TODO
	// remove after 2.1
	// ((PackageFragmentRootInfo) info).sourceMapper = null;
	// super.closing(info);
	// }
	/**
	 * Compute the package fragment children of this package fragment root.
	 * 
	 * @exception JavaModelException
	 *                The resource associated with this package fragment root
	 *                does not exist
	 */
	// protected boolean computeChildren(OpenableElementInfo info) throws
	// JavaModelException {
	// try {
	// // the underlying resource may be a folder or a project (in the case that
	// the project folder
	// // is actually the package fragment root)
	// IResource resource = getResource();
	// if (resource.getType() == IResource.FOLDER || resource.getType() ==
	// IResource.PROJECT) {
	// ArrayList vChildren = new ArrayList(5);
	// char[][] exclusionPatterns = fullExclusionPatternChars();
	// computeFolderChildren((IContainer) resource, "", vChildren,
	// exclusionPatterns); //$NON-NLS-1$
	// IJavaElement[] children = new IJavaElement[vChildren.size()];
	// vChildren.toArray(children);
	// info.setChildren(children);
	// }
	// } catch (JavaModelException e) {
	// //problem resolving children; structure remains unknown
	// info.setChildren(new IJavaElement[]{});
	// throw e;
	// }
	// return true;
	// }
	/**
	 * Starting at this folder, create package fragments and add the fragments
	 * that are not exclused to the collection of children.
	 * 
	 * @exception JavaModelException
	 *                The resource associated with this package fragment does
	 *                not exist
	 */
	// protected void computeFolderChildren(IContainer folder, String prefix,
	// ArrayList vChildren, char[][] exclusionPatterns) throws
	// JavaModelException {
	// IPackageFragment pkg = getPackageFragment(prefix);
	// vChildren.add(pkg);
	// try {
	// JavaProject javaProject = (JavaProject)getJavaProject();
	// IResource[] members = folder.members();
	// for (int i = 0, max = members.length; i < max; i++) {
	// IResource member = members[i];
	// String memberName = member.getName();
	// if (member.getType() == IResource.FOLDER
	// && ProjectPrefUtil.isValidFolderNameForPackage(memberName)
	// && !ProjectPrefUtil.isExcluded(member, exclusionPatterns)) {
	//					
	// // eliminate binary output only if nested inside direct subfolders
	// if (javaProject.contains(member)) {
	// String newPrefix;
	// if (prefix.length() == 0) {
	// newPrefix = memberName;
	// } else {
	// newPrefix = prefix + "." + memberName; //$NON-NLS-1$
	// }
	// computeFolderChildren((IFolder) member, newPrefix, vChildren,
	// exclusionPatterns);
	// }
	// }
	// }
	// } catch(IllegalArgumentException e){
	// throw new JavaModelException(e,
	// IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST); // could be thrown by
	// ElementTree when path
	// is not found
	// } catch (CoreException e) {
	// throw new JavaModelException(e);
	// }
	// }
	/*
	 * Computes and returns the source attachment root path for the given source
	 * attachment path. Returns <code> null </code> if none could be found.
	 * 
	 * @param sourceAttachmentPath the given absolute path to the source archive
	 * or folder @return the computed source attachment root path or <code> null
	 * </cde> if none could be found @throws JavaModelException
	 */
	// public IPath computeSourceAttachmentRootPath(IPath sourceAttachmentPath)
	// throws JavaModelException {
	// IPath sourcePath = this.getSourceAttachmentPath();
	// if (sourcePath == null) return null;
	// SourceMapper mapper =
	// new SourceMapper(
	// sourcePath,
	// null, // detect root path
	// this.isExternal() ? JavaCore.getOptions() :
	// this.getJavaProject().getOptions(true) // only project options if
	// associated with
	// resource
	// );
	// if (mapper.rootPath == null) return null;
	// return new Path(mapper.rootPath);
	// }
	/*
	 * @see net.sourceforge.phpdt.core.IPackageFragmentRoot#copy
	 */
	// public void copy(
	// IPath destination,
	// int updateResourceFlags,
	// int updateModelFlags,
	// IClasspathEntry sibling,
	// IProgressMonitor monitor)
	// throws JavaModelException {
	//		
	// CopyPackageFragmentRootOperation op =
	// new CopyPackageFragmentRootOperation(this, destination,
	// updateResourceFlags, updateModelFlags, sibling);
	// runOperation(op, monitor);
	// }
	/**
	 * Returns a new element info for this element.
	 */
	protected Object createElementInfo() {
		return new PackageFragmentRootInfo();
	}

	/**
	 * @see IPackageFragmentRoot
	 */
	// public IPackageFragment createPackageFragment(String name, boolean force,
	// IProgressMonitor monitor) throws JavaModelException {
	// CreatePackageFragmentOperation op = new
	// CreatePackageFragmentOperation(this, name, force);
	// runOperation(op, monitor);
	// return getPackageFragment(name);
	// }
	/**
	 * Returns the root's kind - K_SOURCE or K_BINARY, defaults to K_SOURCE if
	 * it is not on the classpath.
	 * 
	 * @exception NotPresentException
	 *                if the project and root do not exist.
	 */
	// protected int determineKind(IResource underlyingResource) throws
	// JavaModelException {
	// IClasspathEntry[] entries=
	// ((JavaProject)getJavaProject()).getExpandedClasspath(true);
	// for (int i= 0; i < entries.length; i++) {
	// IClasspathEntry entry= entries[i];
	// if (entry.getPath().equals(underlyingResource.getFullPath())) {
	// return entry.getContentKind();
	// }
	// }
	// return IPackageFragmentRoot.K_SOURCE;
	// }
	/**
	 * Compares two objects for equality; for <code>PackageFragmentRoot</code>s,
	 * equality is having the same <code>JavaModel</code>, same resources,
	 * and occurrence count.
	 * 
	 */
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof PackageFragmentRoot))
			return false;
		PackageFragmentRoot other = (PackageFragmentRoot) o;
		return getJavaModel().equals(other.getJavaModel())
				&& this.resource.equals(other.resource)
				&& occurrenceCount == other.occurrenceCount;
	}

	/**
	 * @see IJavaElement
	 */
	// public boolean exists() {
	// return super.exists()
	// && isOnClasspath();
	// }
	// public IClasspathEntry findSourceAttachmentRecommendation() {
	// try {
	// IPath rootPath = this.getPath();
	// IClasspathEntry entry;
	// IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
	//		
	// // try on enclosing project first
	// JavaProject parentProject = (JavaProject) getJavaProject();
	// try {
	// entry = parentProject.getClasspathEntryFor(rootPath);
	// if (entry != null){
	// Object target = JavaModel.getTarget(workspaceRoot,
	// entry.getSourceAttachmentPath(), true);
	// if (target instanceof IFile){
	// IFile file = (IFile) target;
	// if (ProjectPrefUtil.isArchiveFileName(file.getName())){
	// return entry;
	// }
	// } else if (target instanceof IFolder) {
	// return entry;
	// }
	// if (target instanceof java.io.File){
	// java.io.File file = (java.io.File) target;
	// if (file.isFile()) {
	// if (ProjectPrefUtil.isArchiveFileName(file.getName())){
	// return entry;
	// }
	// } else {
	// // external directory
	// return entry;
	// }
	// }
	// }
	// } catch(JavaModelException e){
	// }
	//		
	// // iterate over all projects
	// IJavaModel model = getJavaModel();
	// IJavaProject[] jProjects = model.getJavaProjects();
	// for (int i = 0, max = jProjects.length; i < max; i++){
	// JavaProject jProject = (JavaProject) jProjects[i];
	// if (jProject == parentProject) continue; // already done
	// try {
	// entry = jProject.getClasspathEntryFor(rootPath);
	// if (entry != null){
	// Object target = JavaModel.getTarget(workspaceRoot,
	// entry.getSourceAttachmentPath(), true);
	// if (target instanceof IFile){
	// IFile file = (IFile) target;
	// if (ProjectPrefUtil.isArchiveFileName(file.getName())){
	// return entry;
	// }
	// } else if (target instanceof IFolder) {
	// return entry;
	// }
	// if (target instanceof java.io.File){
	// java.io.File file = (java.io.File) target;
	// if (file.isFile()) {
	// if (ProjectPrefUtil.isArchiveFileName(file.getName())){
	// return entry;
	// }
	// } else {
	// // external directory
	// return entry;
	// }
	// }
	// }
	// } catch(JavaModelException e){
	// }
	// }
	// } catch(JavaModelException e){
	// }
	//
	// return null;
	// }
	/*
	 * Returns the exclusion patterns from the classpath entry associated with
	 * this root.
	 */
	char[][] fullExclusionPatternChars() {
		return null;
		// try {

		// if (this.isOpen() && this.getKind() != IPackageFragmentRoot.K_SOURCE)
		// return null;
		// ClasspathEntry entry = (ClasspathEntry)getRawClasspathEntry();
		// if (entry == null) {
		// return null;
		// } else {
		// return entry.fullExclusionPatternChars();
		// }
		// } catch (JavaModelException e) {
		// return null;
		// }
	}

	/**
	 * @see Openable
	 */
	protected boolean generateInfos(OpenableElementInfo info,
			IProgressMonitor pm, Map newElements, IResource underlyingResource)
			throws JavaModelException {

		// ((PackageFragmentRootInfo)
		// info).setRootKind(determineKind(underlyingResource));
		// return computeChildren(info);
		return false;
	}

	/**
	 * @see JavaElement#getHandleMemento()
	 */
	protected char getHandleMementoDelimiter() {
		return JavaElement.JEM_PACKAGEFRAGMENTROOT;
	}

	/**
	 * @see IJavaElement
	 */
	public int getElementType() {
		return PACKAGE_FRAGMENT_ROOT;
	}

	/*
	 * @see JavaElement
	 */
	public IJavaElement getHandleFromMemento(String token,
			MementoTokenizer memento, WorkingCopyOwner owner) {
		switch (token.charAt(0)) {
		case JEM_COUNT:
			return getHandleUpdatingCountFromMemento(memento, owner);
		case JEM_PACKAGEFRAGMENT:
			String pkgName;
			if (memento.hasMoreTokens()) {
				pkgName = memento.nextToken();
				char firstChar = pkgName.charAt(0);
				// if (firstChar == JEM_CLASSFILE || firstChar ==
				// JEM_COMPILATIONUNIT || firstChar == JEM_COUNT) {
				if (firstChar == JEM_COMPILATIONUNIT || firstChar == JEM_COUNT) {
					token = pkgName;
					pkgName = IPackageFragment.DEFAULT_PACKAGE_NAME;
				} else {
					token = null;
				}
			} else {
				pkgName = IPackageFragment.DEFAULT_PACKAGE_NAME;
				token = null;
			}
			JavaElement pkg = (JavaElement) getPackageFragment(pkgName);
			if (token == null) {
				return pkg.getHandleFromMemento(memento, owner);
			} else {
				return pkg.getHandleFromMemento(token, memento, owner);
			}
		}
		return null;
	}

	/**
	 * @see JavaElement#getHandleMemento()
	 */
	public String getHandleMemento() {
		IPath path;
		IResource underlyingResource = getResource();
		if (underlyingResource != null) {
			// internal jar or regular root
			if (getResource().getProject()
					.equals(getJavaProject().getProject())) {
				path = underlyingResource.getProjectRelativePath();
			} else {
				path = underlyingResource.getFullPath();
			}
		} else {
			// external jar
			path = getPath();
		}
		StringBuffer buff = new StringBuffer(((JavaElement) getParent())
				.getHandleMemento());
		buff.append(getHandleMementoDelimiter());
		escapeMementoName(buff, path.toString());
		if (this.occurrenceCount > 1) {
			buff.append(JEM_COUNT);
			buff.append(this.occurrenceCount);
		}
		return buff.toString();
	}

	/**
	 * @see IPackageFragmentRoot
	 */
	public int getKind() throws JavaModelException {
		return ((PackageFragmentRootInfo) getElementInfo()).getRootKind();
	}

	/**
	 * Returns an array of non-java resources contained in the receiver.
	 */
	// public Object[] getNonJavaResources() throws JavaModelException {
	// return ((PackageFragmentRootInfo)
	// getElementInfo()).getNonJavaResources(getJavaProject(), getResource(),
	// this);
	// }
	/**
	 * @see IPackageFragmentRoot
	 */
	public IPackageFragment getPackageFragment(String packageName) {
		if (packageName.indexOf(' ') != -1) { // tolerate package names with
												// spaces (e.g. 'x . y')
			// (http://bugs.eclipse.org/bugs/show_bug.cgi?id=21957)
			char[][] compoundName = Util.toCompoundChars(packageName);
			StringBuffer buffer = new StringBuffer(packageName.length());
			for (int i = 0, length = compoundName.length; i < length; i++) {
				buffer.append(CharOperation.trim(compoundName[i]));
				if (i != length - 1) {
					buffer.append('.');
				}
			}
			packageName = buffer.toString();
		}
		return new PackageFragment(this, packageName);
	}

	/**
	 * Returns the package name for the given folder (which is a decendent of
	 * this root).
	 */
	protected String getPackageName(IFolder folder) throws JavaModelException {
		IPath myPath = getPath();
		IPath pkgPath = folder.getFullPath();
		int mySegmentCount = myPath.segmentCount();
		int pkgSegmentCount = pkgPath.segmentCount();
		StringBuffer name = new StringBuffer(
				IPackageFragment.DEFAULT_PACKAGE_NAME);
		for (int i = mySegmentCount; i < pkgSegmentCount; i++) {
			if (i > mySegmentCount) {
				name.append('.');
			}
			name.append(pkgPath.segment(i));
		}
		return name.toString();
	}

	/**
	 * @see IJavaElement
	 */
	public IPath getPath() {
		return getResource().getFullPath();
	}

	/*
	 * @see IPackageFragmentRoot
	 */
	// public IClasspathEntry getRawClasspathEntry() throws JavaModelException {
	//
	// IClasspathEntry rawEntry = null;
	// IJavaProject project = this.getJavaProject();
	// project.getResolvedClasspath(true); // force the reverse rawEntry cache
	// to be populated
	// JavaModelManager.PerProjectInfo perProjectInfo =
	// JavaModelManager.getJavaModelManager().getPerProjectInfoCheckExistence(project.getProject());
	// if (perProjectInfo != null && perProjectInfo.resolvedPathToRawEntries !=
	// null) {
	// rawEntry = (IClasspathEntry)
	// perProjectInfo.resolvedPathToRawEntries.get(this.getPath());
	// }
	// return rawEntry;
	// }
	/*
	 * @see IJavaElement
	 */
	public IResource getResource() {
		return (IResource) this.resource;
	}

	/**
	 * @see IPackageFragmentRoot
	 */
	// public IPath getSourceAttachmentPath() throws JavaModelException {
	// if (getKind() != K_BINARY) return null;
	//	
	// String serverPathString= getSourceAttachmentProperty();
	// if (serverPathString == null) {
	// return null;
	// }
	// int index= serverPathString.lastIndexOf(ATTACHMENT_PROPERTY_DELIMITER);
	// if (index < 0) {
	// // no root path specified
	// return new Path(serverPathString);
	// } else {
	// String serverSourcePathString= serverPathString.substring(0, index);
	// return new Path(serverSourcePathString);
	// }
	// }
	/**
	 * Returns the server property for this package fragment root's source
	 * attachement.
	 */
	// protected String getSourceAttachmentProperty() throws JavaModelException
	// {
	// String propertyString = null;
	// QualifiedName qName= getSourceAttachmentPropertyName();
	// try {
	// propertyString =
	// ResourcesPlugin.getWorkspace().getRoot().getPersistentProperty(qName);
	//		
	// // if no existing source attachment information, then lookup a
	// recommendation from classpath entries
	// if (propertyString == null) {
	// IClasspathEntry recommendation = findSourceAttachmentRecommendation();
	// if (recommendation != null) {
	// IPath rootPath = recommendation.getSourceAttachmentRootPath();
	// propertyString =
	// recommendation.getSourceAttachmentPath().toString()
	// + ((rootPath == null)
	// ? "" : //$NON-NLS-1$
	// (ATTACHMENT_PROPERTY_DELIMITER + rootPath.toString()));
	// setSourceAttachmentProperty(propertyString);
	// } else {
	// // mark as being already looked up
	// setSourceAttachmentProperty(NO_SOURCE_ATTACHMENT);
	// }
	// } else if (NO_SOURCE_ATTACHMENT.equals(propertyString)) {
	// // already looked up and no source attachment found
	// return null;
	// }
	// return propertyString;
	// } catch (CoreException ce) {
	// throw new JavaModelException(ce);
	// }
	// }
	/**
	 * Returns the qualified name for the source attachment property of this
	 * root.
	 */
	protected QualifiedName getSourceAttachmentPropertyName()
			throws JavaModelException {
		return new QualifiedName(JavaCore.PLUGIN_ID,
				"sourceattachment: " + this.getPath().toOSString()); //$NON-NLS-1$
	}

	public void setSourceAttachmentProperty(String property) {
		try {
			ResourcesPlugin.getWorkspace().getRoot().setPersistentProperty(
					this.getSourceAttachmentPropertyName(), property);
		} catch (CoreException ce) {
		}
	}

	/**
	 * For use by <code>AttachSourceOperation</code> only. Sets the source
	 * mapper associated with this root.
	 */
	// public void setSourceMapper(SourceMapper mapper) throws
	// JavaModelException {
	// ((PackageFragmentRootInfo) getElementInfo()).setSourceMapper(mapper);
	// }
	/**
	 * @see IPackageFragmentRoot
	 */
	// public IPath getSourceAttachmentRootPath() throws JavaModelException {
	// if (getKind() != K_BINARY) return null;
	//	
	// String serverPathString= getSourceAttachmentProperty();
	// if (serverPathString == null) {
	// return null;
	// }
	// int index = serverPathString.lastIndexOf(ATTACHMENT_PROPERTY_DELIMITER);
	// if (index == -1) return null;
	// String serverRootPathString=
	// IPackageFragmentRoot.DEFAULT_PACKAGEROOT_PATH;
	// if (index != serverPathString.length() - 1) {
	// serverRootPathString= serverPathString.substring(index + 1);
	// }
	// return new Path(serverRootPathString);
	// }
	/**
	 * @see JavaElement
	 */
	// public SourceMapper getSourceMapper() {
	// SourceMapper mapper;
	// try {
	// PackageFragmentRootInfo rootInfo = (PackageFragmentRootInfo)
	// getElementInfo();
	// mapper = rootInfo.getSourceMapper();
	// if (mapper == null) {
	// // first call to this method
	// IPath sourcePath= getSourceAttachmentPath();
	// if (sourcePath != null) {
	// IPath rootPath= getSourceAttachmentRootPath();
	// mapper = this.createSourceMapper(sourcePath, rootPath);
	// if (rootPath == null && mapper.rootPath != null) {
	// // as a side effect of calling the SourceMapper constructor, the root
	// path was computed
	// rootPath = new Path(mapper.rootPath);
	//					
	// //set the property to the path of the mapped source
	// this.setSourceAttachmentProperty(
	// sourcePath.toString()
	// + ATTACHMENT_PROPERTY_DELIMITER
	// + rootPath.toString());
	// }
	// rootInfo.setSourceMapper(mapper);
	// } else {
	// // remember that no source is attached
	// rootInfo.setSourceMapper(NO_SOURCE_MAPPER);
	// mapper = null;
	// }
	// } else if (mapper == NO_SOURCE_MAPPER) {
	// // a previous call to this method found out that no source was attached
	// mapper = null;
	// }
	// } catch (JavaModelException e) {
	// // no source can be attached
	// mapper = null;
	// }
	// return mapper;
	// }
	/**
	 * @see IJavaElement
	 */
	public IResource getUnderlyingResource() throws JavaModelException {
		if (!exists())
			throw newNotPresentException();
		return getResource();
	}

	public int hashCode() {
		return this.resource.hashCode();
	}

	/**
	 * @see IPackageFragmentRoot
	 */
	public boolean isArchive() {
		return false;
	}

	/**
	 * @see IPackageFragmentRoot
	 */
	public boolean isExternal() {
		return false;
	}

	/*
	 * Returns whether this package fragment root is on the classpath of its
	 * project.
	 */
	// protected boolean isOnClasspath() {
	// if (this.getElementType() == IJavaElement.JAVA_PROJECT){
	// return true;
	// }
	//	
	// IPath path = this.getPath();
	// try {
	// // check package fragment root on classpath of its project
	// IJavaProject project = this.getJavaProject();
	// IClasspathEntry[] classpath = project.getResolvedClasspath(true);
	// for (int i = 0, length = classpath.length; i < length; i++) {
	// IClasspathEntry entry = classpath[i];
	// if (entry.getPath().equals(path)) {
	// return true;
	// }
	// }
	// } catch(JavaModelException e){
	// // could not read classpath, then assume it is outside
	// }
	// return false;
	// }
	/*
	 * @see net.sourceforge.phpdt.core.IPackageFragmentRoot#move
	 */
	// public void move(
	// IPath destination,
	// int updateResourceFlags,
	// int updateModelFlags,
	// IClasspathEntry sibling,
	// IProgressMonitor monitor)
	// throws JavaModelException {
	//
	// MovePackageFragmentRootOperation op =
	// new MovePackageFragmentRootOperation(this, destination,
	// updateResourceFlags, updateModelFlags, sibling);
	// runOperation(op, monitor);
	// }
	//
	//
	// protected void openWhenClosed(IProgressMonitor pm) throws
	// JavaModelException {
	// if (!this.resourceExists()
	// || !this.isOnClasspath()) {
	// throw newNotPresentException();
	// }
	// super.openWhenClosed(pm);
	// }
	/**
	 * Recomputes the children of this element, based on the current state of
	 * the workbench.
	 */
	// public void refreshChildren() {
	// try {
	// OpenableElementInfo info= (OpenableElementInfo)getElementInfo();
	// computeChildren(info);
	// } catch (JavaModelException e) {
	// // do nothing.
	// }
	// }
	// /*
	// * @see JavaElement#rootedAt(IJavaProject)
	// */
	// public IJavaElement rootedAt(IJavaProject project) {
	// return
	// new PackageFragmentRoot(
	// getResource(),
	// project,
	// name);
	// }
	/**
	 * @private Debugging purposes
	 */
	protected void toStringInfo(int tab, StringBuffer buffer, Object info) {
		buffer.append(this.tabString(tab));
		if (getElementName().length() == 0) {
			buffer.append("[project root]"); //$NON-NLS-1$
		} else {
			IPath path = getPath();
			if (getJavaProject().getElementName().equals(path.segment(0))) {
				buffer.append(path.removeFirstSegments(1).makeRelative());
			} else {
				buffer.append(path);
			}
		}
		if (info == null) {
			buffer.append(" (not open)"); //$NON-NLS-1$
		}
	}

	/**
	 * Possible failures:
	 * <ul>
	 * <li>ELEMENT_NOT_PRESENT - the root supplied to the operation does not
	 * exist
	 * <li>INVALID_ELEMENT_TYPES - the root is not of kind K_BINARY
	 * <li>RELATIVE_PATH - the path supplied to this operation must be an
	 * absolute path
	 * </ul>
	 */
	// protected void verifyAttachSource(IPath sourcePath) throws
	// JavaModelException {
	// if (!exists()) {
	// throw newNotPresentException();
	// } else if (this.getKind() != K_BINARY) {
	// throw new JavaModelException(new
	// JavaModelStatus(IJavaModelStatusConstants.INVALID_ELEMENT_TYPES, this));
	// } else if (sourcePath != null && !sourcePath.isAbsolute()) {
	// throw new JavaModelException(new
	// JavaModelStatus(IJavaModelStatusConstants.RELATIVE_PATH, sourcePath));
	// }
	// }
}