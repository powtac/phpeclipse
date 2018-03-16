/*
 * $Id: InsertHTMLElementAction.java,v 1.3 2006-10-21 23:18:43 pombredanne Exp $
 * Copyright Narushima Hironori. All rights reserved.
 */
package net.sourceforge.phpeclipse.wizards.html;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * InsertTagAction
 */
public class InsertHTMLElementAction implements IEditorActionDelegate {

	ITextEditor targetEditor = null;

	public InsertHTMLElementAction() {
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor instanceof ITextEditor) {
			this.targetEditor = (ITextEditor) targetEditor;
		}
	}

	public void run(IAction action) {

		WizardDialog wizDialog = new WizardDialog(targetEditor.getSite()
				.getShell(), new EditElementWizard(targetEditor, null)) {

			protected int getShellStyle() {
				return super.getShellStyle() | SWT.RESIZE;
			}
		};

		wizDialog.open();
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}
