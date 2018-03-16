/***********************************************************************************************************************************
 * Copyright (c) 2000, 2002 IBM Corp. and others. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: www.phpeclipse.de
 **********************************************************************************************************************************/
package net.sourceforge.phpeclipse.actions;

import net.sourceforge.phpeclipse.phpeditor.PHPEditor;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionDelegate;

public class PHPOpenDeclarationEditorAction extends ActionDelegate implements
		IEditorActionDelegate {
	private IWorkbenchWindow fWindow;

	private PHPEditor fEditor;

	public void init(IWorkbenchWindow window) {
		this.fWindow = window;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (!selection.isEmpty()) {
			if (selection instanceof TextSelection) {
				action.setEnabled(true);
			} else if (fWindow.getActivePage() != null
					&& fWindow.getActivePage().getActivePart() != null) {
				//
			}
		}
	}

	private boolean checkEnabled(IStructuredSelection selection) {
		if (selection.isEmpty())
			return false;
		return true;
	}

	public void run(IAction action) {
		if (fEditor == null) {
			IEditorPart targetEditor = fWindow.getActivePage()
					.getActiveEditor();
			if (targetEditor != null && (targetEditor instanceof PHPEditor)) {
				fEditor = (PHPEditor) targetEditor;
			}
		}
		if (fEditor != null) {
			ITextSelection selection = (ITextSelection) fEditor
					.getSelectionProvider().getSelection();
			OpenDeclarationEditorAction openAction = new OpenDeclarationEditorAction(
					fEditor);
			openAction.openSelectedElement(selection);
		}
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor != null && (targetEditor instanceof PHPEditor)) {
			fEditor = (PHPEditor) targetEditor;
		}
	}

}