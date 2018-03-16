package net.sourceforge.phpdt.externaltools.internal.model;

/**********************************************************************
 Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
 This file is made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html
 **********************************************************************/

import java.util.Map;

import net.sourceforge.phpdt.externaltools.variable.ExpandVariableContext;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.externaltools.internal.model.ExternalToolBuilder;

/**
 * Maintains the context used to expand variables. The context is based on the
 * selected resource, unless a build is in progress - in which case the context
 * is based on the project being built..
 */
public class VariableContextManager implements IWindowListener,
		ISelectionListener {

	// singleton
	private static VariableContextManager fgDefault;

	private IResource fSelectedResource = null;

	private boolean fBuilding = false;

	private IProject fProject = null;

	private int fKind;

	private VariableContextManager() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench != null) { // may be running headless
			workbench.addWindowListener(this);
			IWorkbenchWindow activeWindow = workbench
					.getActiveWorkbenchWindow();
			if (activeWindow != null) {
				windowActivated(activeWindow);
			}
		}
	}

	/**
	 * Returns the singleton resource selection manager
	 * 
	 * @return VariableContextManager
	 */
	public static VariableContextManager getDefault() {
		if (fgDefault == null) {
			fgDefault = new VariableContextManager();
		}
		return fgDefault;
	}

	/**
	 * @see org.eclipse.ui.IWindowListener#windowActivated(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowActivated(IWorkbenchWindow window) {
		fSelectedResource = null;
		ISelectionService service = window.getSelectionService();
		service.addSelectionListener(this);
		IWorkbenchPage page = window.getActivePage();
		if (page != null) {
			IWorkbenchPart part = page.getActivePart();
			if (part != null) {
				ISelection selection = service.getSelection();
				if (selection != null) {
					selectionChanged(part, selection);
				}
			}
		}
	}

	/**
	 * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowClosed(IWorkbenchWindow window) {
		window.getSelectionService().removeSelectionListener(this);
	}

	/**
	 * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowDeactivated(IWorkbenchWindow window) {
		window.getSelectionService().removeSelectionListener(this);
	}

	/**
	 * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void windowOpened(IWorkbenchWindow window) {
	}

	/**
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		IResource selectedResource = null;
		if (selection instanceof IStructuredSelection) {
			Object result = ((IStructuredSelection) selection)
					.getFirstElement();
			if (result instanceof IResource) {
				selectedResource = (IResource) result;
			} else if (result instanceof IAdaptable) {
				selectedResource = (IResource) ((IAdaptable) result)
						.getAdapter(IResource.class);
			}
		}

		if (selectedResource == null) {
			// If the active part is an editor, get the file resource used as
			// input.
			if (part instanceof IEditorPart) {
				IEditorPart editorPart = (IEditorPart) part;
				IEditorInput input = editorPart.getEditorInput();
				selectedResource = (IResource) input
						.getAdapter(IResource.class);
			}
		}

		fSelectedResource = selectedResource;
	}

	/**
	 * Returns the active variable context. The context is that of the selected
	 * resource, or a project being built.
	 * 
	 * @return variable context
	 */
	public ExpandVariableContext getVariableContext() {
		if (fBuilding) {
			return new ExpandVariableContext(fProject, fKind);
		} else {
			return new ExpandVariableContext(fSelectedResource);
		}
	}

	/**
	 * Notification that the given project is being built.
	 * 
	 * @param project
	 * @param kind
	 * @see ExternalToolBuilder#build(int, Map, IProgressMonitor)
	 */
	public void buildStarted(IProject project, int kind) {
		fBuilding = true;
		fProject = project;
		fKind = kind;
	}

	/**
	 * Notification the building the current project has completed.
	 * 
	 * @see ExternalToolBuilder#build(int, Map, IProgressMonitor)
	 */
	public void buildEnded() {
		fBuilding = false;
		fProject = null;
	}
}
