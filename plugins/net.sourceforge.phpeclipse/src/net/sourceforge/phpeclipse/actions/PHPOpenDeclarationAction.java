/***********************************************************************************************************************************
 * Copyright (c) 2000, 2002 IBM Corp. and others. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: www.phpeclipse.de
 **********************************************************************************************************************************/
package net.sourceforge.phpeclipse.actions;

import net.sourceforge.phpdt.internal.ui.actions.ActionUtil;
import net.sourceforge.phpdt.internal.ui.actions.SelectionConverter;
import net.sourceforge.phpdt.ui.actions.SelectionDispatchAction;
import net.sourceforge.phpeclipse.phpeditor.PHPEditor;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkbenchWindow;

public class PHPOpenDeclarationAction extends SelectionDispatchAction {
	private IWorkbenchWindow fWindow;

	private PHPEditor fEditor;

	public void dispose() {
	}

	public PHPOpenDeclarationAction(IWorkbenchSite site) {
		super(site);
		// setText(ActionMessages.getString("OpenAction.label")); //$NON-NLS-1$
		// setToolTipText(ActionMessages.getString("OpenAction.tooltip"));
		// //$NON-NLS-1$
		// setDescription(ActionMessages.getString("OpenAction.description"));
		// //$NON-NLS-1$
		// WorkbenchHelp.setHelp(this, IJavaHelpContextIds.OPEN_ACTION);
	}

	/**
	 * Note: This constructor is for internal use only. Clients should not call
	 * this constructor.
	 */
	public PHPOpenDeclarationAction(PHPEditor editor) {
		this(editor.getEditorSite());
		fEditor = editor;
		// setText(ActionMessages.getString("OpenAction.declaration.label"));
		// //$NON-NLS-1$
		setEnabled(SelectionConverter.canOperateOn(fEditor));
	}

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

	/*
	 * (non-Javadoc) Method declared on SelectionDispatchAction.
	 */
	public void run(ITextSelection selection) {
		if (!ActionUtil.isProcessable(getShell(), fEditor))
			return;
		OpenDeclarationEditorAction openAction = new OpenDeclarationEditorAction(
				fEditor);
		openAction.openSelectedElement(selection);
	}

	/*
	 * (non-Javadoc) Method declared on SelectionDispatchAction.
	 */
	public void run(IStructuredSelection selection) {
		if (!checkEnabled(selection))
			return;
		run(selection.toArray());
	}

	/**
	 * Note: this method is for internal use only. Clients should not call this
	 * method.
	 */
	public void run(Object[] elements) {
		if (elements != null && elements.length > 0) {
			ITextSelection selection = (ITextSelection) fEditor
					.getSelectionProvider().getSelection();
			IDocument doc = fEditor.getDocumentProvider().getDocument(
					fEditor.getEditorInput());
			int pos = selection.getOffset();

			OpenDeclarationEditorAction openAction = new OpenDeclarationEditorAction(
					fEditor);
			openAction.openSelectedPosition(doc, pos);
		}
	}

}