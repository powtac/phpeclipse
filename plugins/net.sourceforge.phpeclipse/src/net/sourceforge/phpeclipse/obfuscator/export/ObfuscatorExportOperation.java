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
package net.sourceforge.phpeclipse.obfuscator.export;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.phpdt.internal.compiler.parser.Scanner;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;
import net.sourceforge.phpeclipse.obfuscator.ObfuscatorIgnores;
import net.sourceforge.phpeclipse.obfuscator.ObfuscatorPass1Exporter;
import net.sourceforge.phpeclipse.obfuscator.ObfuscatorPass2Exporter;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IOverwriteQuery;

/**
 * Operation for exporting the contents of a resource to the local file system.
 */
/* package */
class ObfuscatorExportOperation implements IRunnableWithProgress {
	private IPath fPath;

	private IProgressMonitor fMonitor;

	private ObfuscatorPass1Exporter fExporter1 = null;

	private ObfuscatorPass2Exporter fExporter2 = null;

	private HashMap fCurrentIdentifierMap = null;

	private HashMap fProjectMap = null;

	private String fCurrentProjectName = "";

	private List fResourcesToExport;

	private IOverwriteQuery fOverwriteCallback;

	private IResource fResource;

	private List errorTable = new ArrayList(1);

	// The constants for the overwrite 3 state
	private static final int OVERWRITE_NOT_SET = 0;

	private static final int OVERWRITE_NONE = 1;

	private static final int OVERWRITE_ALL = 2;

	private int overwriteState = OVERWRITE_NOT_SET;

	// private boolean createLeadupStructure = true;
	private boolean createContainerDirectories = true;

	/**
	 * Create an instance of this class. Use this constructor if you wish to
	 * export specific resources without a common parent resource
	 */
	// public ObfuscatorExportOperation(List resources, String destinationPath,
	// IOverwriteQuery overwriteImplementor) {
	// super();
	//
	// exporter1 = new ObfuscatorPass1Exporter(new Scanner(false, false),
	// identifierMap);
	// exporter2 = new ObfuscatorPass2Exporter(new Scanner(true, true),
	// identifierMap);
	// identifierMap = null;
	//		
	// // Eliminate redundancies in list of resources being exported
	// Iterator elementsEnum = resources.iterator();
	// while (elementsEnum.hasNext()) {
	// IResource currentResource = (IResource) elementsEnum.next();
	// if (isDescendent(resources, currentResource))
	// elementsEnum.remove(); //Remove currentResource
	// }
	//
	// resourcesToExport = resources;
	// path = new Path(destinationPath);
	// overwriteCallback = overwriteImplementor;
	// }
	/**
	 * Create an instance of this class. Use this constructor if you wish to
	 * recursively export a single resource
	 */
	public ObfuscatorExportOperation(IResource res, String destinationPath,
			IOverwriteQuery overwriteImplementor) {
		super();

		fResource = res;
		fPath = new Path(destinationPath);
		fOverwriteCallback = overwriteImplementor;
	}

	/**
	 * Create an instance of this class. Use this constructor if you wish to
	 * export specific resources with a common parent resource (affects
	 * container directory creation)
	 */
	public ObfuscatorExportOperation(IResource res, List resources,
			String destinationPath, IOverwriteQuery overwriteImplementor) {
		this(res, destinationPath, overwriteImplementor);
		fResourcesToExport = resources;
	}

	/**
	 * Add a new entry to the error table with the passed information
	 */
	protected void addError(String message, Throwable e) {
		errorTable.add(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0,
				message, e));
	}

	/**
	 * Answer the total number of file resources that exist at or below self in
	 * the resources hierarchy.
	 * 
	 * @return int
	 * @param resource
	 *            org.eclipse.core.resources.IResource
	 */
	protected int countChildrenOf(IResource resource) throws CoreException {
		if (resource.getType() == IResource.FILE)
			return 1;

		int count = 0;
		if (resource.isAccessible()) {
			IResource[] children = ((IContainer) resource).members();
			for (int i = 0; i < children.length; i++)
				count += countChildrenOf(children[i]);
		}

		return count;
	}

	/**
	 * Answer a boolean indicating the number of file resources that were
	 * specified for export
	 * 
	 * @return int
	 */
	protected int countSelectedResources() throws CoreException {
		int result = 0;
		Iterator resources = fResourcesToExport.iterator();

		while (resources.hasNext())
			result += countChildrenOf((IResource) resources.next());

		return result;
	}

	/**
	 * Create the directories required for exporting the passed resource, based
	 * upon its container hierarchy
	 * 
	 * @param resource
	 *            org.eclipse.core.resources.IResource
	 */
	protected void createLeadupDirectoriesFor(IResource resource) {
		IPath resourcePath = resource.getFullPath().removeLastSegments(1);

		for (int i = 0; i < resourcePath.segmentCount(); i++) {
			fPath = fPath.append(resourcePath.segment(i));
			fExporter2.createFolder(fPath);
		}
	}

	/**
	 * Recursively export the previously-specified resource
	 */
	protected void exportAllResources1() throws InterruptedException {
		if (fResource.getType() == IResource.FILE) {
			exportFile1((IFile) fResource, fPath);
		} else {
			try {
				setExporters(fResource);
				exportChildren1(((IContainer) fResource).members(), fPath);
			} catch (CoreException e) {
				// not safe to show a dialog
				// should never happen because the file system export wizard
				// ensures that the
				// single resource chosen for export is both existent and
				// accessible
				errorTable.add(e);
			}
		}
	}

	/**
	 * Recursively export the previously-specified resource
	 */
	protected void exportAllResources2() throws InterruptedException {
		if (fResource.getType() == IResource.FILE) {
			exportFile2((IFile) fResource, fPath);
		} else {
			try {
				setExporters(fResource);
				exportChildren2(((IContainer) fResource).members(), fPath);
			} catch (CoreException e) {
				// not safe to show a dialog
				// should never happen because the file system export wizard
				// ensures that the
				// single resource chosen for export is both existent and
				// accessible
				errorTable.add(e);
			}
		}
	}

	/**
	 * Export all of the resources contained in the passed collection
	 * 
	 * @param children
	 *            java.util.Enumeration
	 * @param currentPath
	 *            IPath
	 */
	protected void exportChildren1(IResource[] children, IPath currentPath)
			throws InterruptedException {
		for (int i = 0; i < children.length; i++) {
			IResource child = children[i];
			if (!child.isAccessible())
				continue;

			if (child.getType() == IResource.FILE)
				exportFile1((IFile) child, currentPath);
			else {
				IPath destination = currentPath.append(child.getName());
				fExporter1.createFolder(destination);
				try {
					exportChildren1(((IContainer) child).members(), destination);
				} catch (CoreException e) {
					// not safe to show a dialog
					// should never happen because:
					// i. this method is called recursively iterating over the
					// result of #members,
					// which only answers existing children
					// ii. there is an #isAccessible check done before #members
					// is invoked
					errorTable.add(e.getStatus());
				}
			}
		}
	}

	/**
	 * Export all of the resources contained in the passed collection
	 * 
	 * @param children
	 *            java.util.Enumeration
	 * @param currentPath
	 *            IPath
	 */
	protected void exportChildren2(IResource[] children, IPath currentPath)
			throws InterruptedException {
		for (int i = 0; i < children.length; i++) {
			IResource child = children[i];
			if (!child.isAccessible())
				continue;

			if (child.getType() == IResource.FILE)
				exportFile2((IFile) child, currentPath);
			else {
				IPath destination = currentPath.append(child.getName());
				fExporter2.createFolder(destination);
				try {
					exportChildren2(((IContainer) child).members(), destination);
				} catch (CoreException e) {
					// not safe to show a dialog
					// should never happen because:
					// i. this method is called recursively iterating over the
					// result of #members,
					// which only answers existing children
					// ii. there is an #isAccessible check done before #members
					// is invoked
					errorTable.add(e.getStatus());
				}
			}
		}
	}

	protected void exportFile1(IFile file, IPath location)
			throws InterruptedException {
		IPath fullPath = location.append(file.getName());
		fMonitor.subTask(file.getFullPath().toString());
		String properPathString = fullPath.toOSString();
		File targetFile = new File(properPathString);

		// if (targetFile.exists()) {
		// if (!targetFile.canWrite()) {
		// errorTable.add(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0,
		// ObfuscatorExportMessages.format("ObfuscatorTransfer.cannotOverwrite",
		// //$NON-NLS-1$
		// new Object[] { targetFile.getAbsolutePath()}), null));
		// monitor.worked(1);
		// return;
		// }
		//
		// if (overwriteState == OVERWRITE_NONE)
		// return;
		//
		// if (overwriteState != OVERWRITE_ALL) {
		// String overwriteAnswer =
		// overwriteCallback.queryOverwrite(properPathString);
		//
		// if (overwriteAnswer.equals(IOverwriteQuery.CANCEL))
		// throw new InterruptedException();
		//
		// if (overwriteAnswer.equals(IOverwriteQuery.NO)) {
		// monitor.worked(1);
		// return;
		// }
		//
		// if (overwriteAnswer.equals(IOverwriteQuery.NO_ALL)) {
		// monitor.worked(1);
		// overwriteState = OVERWRITE_NONE;
		// return;
		// }
		//
		// if (overwriteAnswer.equals(IOverwriteQuery.ALL))
		// overwriteState = OVERWRITE_ALL;
		// }
		// }

		try {
			setExporters(file);
			fExporter1.write(file, fullPath);
		} catch (IOException e) {
			errorTable.add(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0,
					ObfuscatorExportMessages.format(
							"ObfuscatorTransfer.errorExporting", //$NON-NLS-1$
							new Object[] { fullPath, e.getMessage() }), e));
		} catch (CoreException e) {
			errorTable.add(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0,
					ObfuscatorExportMessages.format(
							"ObfuscatorTransfer.errorExporting", //$NON-NLS-1$
							new Object[] { fullPath, e.getMessage() }), e));
		}

		fMonitor.worked(1);
		ModalContext.checkCanceled(fMonitor);
	}

	/**
	 * Export the passed file to the specified location
	 * 
	 * @param file
	 *            org.eclipse.core.resources.IFile
	 * @param location
	 *            org.eclipse.core.runtime.IPath
	 */
	protected void exportFile2(IFile file, IPath location)
			throws InterruptedException {
		IPath fullPath = location.append(file.getName());
		fMonitor.subTask(file.getFullPath().toString());
		String properPathString = fullPath.toOSString();
		File targetFile = new File(properPathString);

		if (targetFile.exists()) {
			if (!targetFile.canWrite()) {
				errorTable.add(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID,
						0, ObfuscatorExportMessages.format(
								"ObfuscatorTransfer.cannotOverwrite", //$NON-NLS-1$
								new Object[] { targetFile.getAbsolutePath() }),
						null));
				fMonitor.worked(1);
				return;
			}

			if (overwriteState == OVERWRITE_NONE)
				return;

			if (overwriteState != OVERWRITE_ALL) {
				String overwriteAnswer = fOverwriteCallback
						.queryOverwrite(properPathString);

				if (overwriteAnswer.equals(IOverwriteQuery.CANCEL))
					throw new InterruptedException();

				if (overwriteAnswer.equals(IOverwriteQuery.NO)) {
					fMonitor.worked(1);
					return;
				}

				if (overwriteAnswer.equals(IOverwriteQuery.NO_ALL)) {
					fMonitor.worked(1);
					overwriteState = OVERWRITE_NONE;
					return;
				}

				if (overwriteAnswer.equals(IOverwriteQuery.ALL))
					overwriteState = OVERWRITE_ALL;
			}
		}

		try {
			setExporters(file);
			fExporter2.write(file, fullPath);
		} catch (IOException e) {
			errorTable.add(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0,
					ObfuscatorExportMessages.format(
							"ObfuscatorTransfer.errorExporting", //$NON-NLS-1$
							new Object[] { fullPath, e.getMessage() }), e));
		} catch (CoreException e) {
			errorTable.add(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0,
					ObfuscatorExportMessages.format(
							"ObfuscatorTransfer.errorExporting", //$NON-NLS-1$
							new Object[] { fullPath, e.getMessage() }), e));
		}

		fMonitor.worked(1);
		ModalContext.checkCanceled(fMonitor);
	}

	protected void exportSpecifiedResources1() throws InterruptedException {
		Iterator resources = fResourcesToExport.iterator();
		IPath initPath = (IPath) fPath.clone();

		while (resources.hasNext()) {
			IResource currentResource = (IResource) resources.next();
			if (!currentResource.isAccessible())
				continue;
			setExporters(currentResource);
			fPath = initPath;

			if (fResource == null) {
				// No root resource specified and creation of containment
				// directories
				// is required. Create containers from depth 2 onwards (ie.-
				// project's
				// child inclusive) for each resource being exported.
				// if (createLeadupStructure)
				// createLeadupDirectoriesFor(currentResource);

			} else {
				// Root resource specified. Must create containment directories
				// from this point onwards for each resource being exported
				IPath containersToCreate = currentResource.getFullPath()
						.removeFirstSegments(
								fResource.getFullPath().segmentCount())
						.removeLastSegments(1);

				for (int i = 0; i < containersToCreate.segmentCount(); i++) {
					fPath = fPath.append(containersToCreate.segment(i));
					fExporter1.createFolder(fPath);
				}
			}

			if (currentResource.getType() == IResource.FILE)
				exportFile1((IFile) currentResource, fPath);
			else {
				if (createContainerDirectories) {
					fPath = fPath.append(currentResource.getName());
					fExporter1.createFolder(fPath);
				}

				try {
					exportChildren1(((IContainer) currentResource).members(),
							fPath);
				} catch (CoreException e) {
					// should never happen because #isAccessible is called
					// before #members is invoked,
					// which implicitly does an existence check
					errorTable.add(e.getStatus());
				}
			}
		}
	}

	/**
	 * Export the resources contained in the previously-defined
	 * resourcesToExport collection
	 */
	protected void exportSpecifiedResources2() throws InterruptedException {
		Iterator resources = fResourcesToExport.iterator();
		IPath initPath = (IPath) fPath.clone();

		while (resources.hasNext()) {
			IResource currentResource = (IResource) resources.next();
			if (!currentResource.isAccessible())
				continue;
			setExporters(currentResource);

			fPath = initPath;

			if (fResource == null) {
				// No root resource specified and creation of containment
				// directories
				// is required. Create containers from depth 2 onwards (ie.-
				// project's
				// child inclusive) for each resource being exported.
				// if (createLeadupStructure)
				// createLeadupDirectoriesFor(currentResource);

			} else {
				// Root resource specified. Must create containment directories
				// from this point onwards for each resource being exported
				IPath containersToCreate = currentResource.getFullPath()
						.removeFirstSegments(
								fResource.getFullPath().segmentCount())
						.removeLastSegments(1);

				for (int i = 0; i < containersToCreate.segmentCount(); i++) {
					fPath = fPath.append(containersToCreate.segment(i));
					fExporter2.createFolder(fPath);
				}
			}

			if (currentResource.getType() == IResource.FILE)
				exportFile2((IFile) currentResource, fPath);
			else {
				if (createContainerDirectories) {
					fPath = fPath.append(currentResource.getName());
					fExporter2.createFolder(fPath);
				}

				try {
					exportChildren2(((IContainer) currentResource).members(),
							fPath);
				} catch (CoreException e) {
					// should never happen because #isAccessible is called
					// before #members is invoked,
					// which implicitly does an existence check
					errorTable.add(e.getStatus());
				}
			}
		}
	}

	/**
	 * Returns the status of the export operation. If there were any errors, the
	 * result is a status object containing individual status objects for each
	 * error. If there were no errors, the result is a status object with error
	 * code <code>OK</code>.
	 * 
	 * @return the status
	 */
	public IStatus getStatus() {
		IStatus[] errors = new IStatus[errorTable.size()];
		errorTable.toArray(errors);
		return new MultiStatus(
				PlatformUI.PLUGIN_ID,
				IStatus.OK,
				errors,
				ObfuscatorExportMessages
						.getString("ObfuscatorExportOperation.problemsExporting"), //$NON-NLS-1$
				null);
	}

	/**
	 * Answer a boolean indicating whether the passed child is a descendent of
	 * one or more members of the passed resources collection
	 * 
	 * @return boolean
	 * @param resources
	 *            java.util.List
	 * @param child
	 *            org.eclipse.core.resources.IResource
	 */
	protected boolean isDescendent(List resources, IResource child) {
		if (child.getType() == IResource.PROJECT)
			return false;

		IResource parent = child.getParent();
		if (resources.contains(parent))
			return true;

		return isDescendent(resources, parent);
	}

	private void setExporters(IResource resource) {
		if (fCurrentIdentifierMap == null) {
			if (fProjectMap == null) {
				fProjectMap = new HashMap();
			}
			createExporters(resource);
		} else {
			IProject project = resource.getProject();
			if (!fCurrentProjectName.equals(project.getName())) {
				HashMap temp = (HashMap) fProjectMap.get(project.getName());
				if (temp != null) {
					fCurrentProjectName = project.getName();
					fCurrentIdentifierMap = temp;
					fExporter1 = new ObfuscatorPass1Exporter(new Scanner(false,
							false), fCurrentIdentifierMap);
					fExporter2 = new ObfuscatorPass2Exporter(new Scanner(true,
							true), fCurrentIdentifierMap);
					return;
				}
				createExporters(resource);
			}
		}
	}

	private void createExporters(IResource resource) {
		IProject project = resource.getProject();
		IPreferenceStore store = PHPeclipsePlugin.getDefault()
				.getPreferenceStore();
		ObfuscatorIgnores ignore = new ObfuscatorIgnores(project);
		fCurrentIdentifierMap = ignore.getIdentifierMap();
		fCurrentProjectName = project.getName();
		fProjectMap.put(fCurrentProjectName, fCurrentIdentifierMap);
		fExporter1 = new ObfuscatorPass1Exporter(new Scanner(false, false),
				fCurrentIdentifierMap);
		fExporter2 = new ObfuscatorPass2Exporter(new Scanner(true, true),
				fCurrentIdentifierMap);
	}

	/**
	 * Export the resources that were previously specified for export (or if a
	 * single resource was specified then export it recursively)
	 */
	public void run(IProgressMonitor monitor) throws InterruptedException {
		this.fMonitor = monitor;
		final IPath tempPath = (IPath) fPath.clone();
		if (fResource != null) {
			setExporters(fResource);
			// if (createLeadupStructure)
			// createLeadupDirectoriesFor(resource);

			if (createContainerDirectories
					&& fResource.getType() != IResource.FILE) {
				// ensure it's a container
				fPath = fPath.append(fResource.getName());
				fExporter2.createFolder(fPath);
			}
		}

		try {
			// reset variables for this run:
			fCurrentIdentifierMap = null;
			fProjectMap = null;
			fCurrentProjectName = "";

			// count number of files
			int totalWork = IProgressMonitor.UNKNOWN;
			try {
				if (fResourcesToExport == null) {
					totalWork = countChildrenOf(fResource);
				} else {
					totalWork = countSelectedResources();
				}
			} catch (CoreException e) {
				// Should not happen
				errorTable.add(e.getStatus());
			}
			monitor
					.beginTask(
							ObfuscatorExportMessages
									.getString("ObfuscatorTransfer.exportingTitle1"), totalWork); //$NON-NLS-1$
			if (fResourcesToExport == null) {
				exportAllResources1();
			} else {
				exportSpecifiedResources1();
			}

			// try {
			// if (resourcesToExport == null)
			// totalWork = countChildrenOf(resource);
			// else
			// totalWork = countSelectedResources();
			// } catch (CoreException e) {
			// // Should not happen
			// errorTable.add(e.getStatus());
			// }

			// reset path:
			fPath = tempPath;
			monitor
					.beginTask(
							ObfuscatorExportMessages
									.getString("ObfuscatorTransfer.exportingTitle2"), totalWork); //$NON-NLS-1$
			if (fResourcesToExport == null) {
				exportAllResources2();
			} else {
				exportSpecifiedResources2();
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * Set this boolean indicating whether a directory should be created for
	 * Folder resources that are explicitly passed for export
	 * 
	 * @param value
	 *            boolean
	 */
	// public void setCreateContainerDirectories(boolean value) {
	// createContainerDirectories = value;
	// }
	/**
	 * Set this boolean indicating whether each exported resource's complete
	 * path should include containment hierarchies as dictated by its parents
	 * 
	 * @param value
	 *            boolean
	 */
	// public void setCreateLeadupStructure(boolean value) {
	// createLeadupStructure = value;
	// }
	/**
	 * Set this boolean indicating whether exported resources should
	 * automatically overwrite existing files when a conflict occurs. If not
	 * query the user.
	 * 
	 * @param value
	 *            boolean
	 */
	public void setOverwriteFiles(boolean value) {
		if (value)
			overwriteState = OVERWRITE_ALL;
	}
}
