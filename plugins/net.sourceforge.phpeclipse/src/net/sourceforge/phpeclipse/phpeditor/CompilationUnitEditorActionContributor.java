/***********************************************************************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **********************************************************************************************************************************/
package net.sourceforge.phpeclipse.phpeditor;

import net.sourceforge.phpdt.ui.actions.PHPdtActionConstants;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;
import net.sourceforge.phpeclipse.ui.editor.ShowExternalPreviewAction;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

public class CompilationUnitEditorActionContributor extends
		BasicEditorActionContributor {
	public CompilationUnitEditorActionContributor() {
		super();
	}

	/*
	 * @see IEditorActionBarContributor#setActiveEditor(IEditorPart)
	 */
	public void setActiveEditor(IEditorPart part) {
		super.setActiveEditor(part);

		ITextEditor textEditor = null;
		if (part instanceof ITextEditor)
			textEditor = (ITextEditor) part;

		// Source menu.
		IActionBars bars = getActionBars();
		bars.setGlobalActionHandler(PHPdtActionConstants.COMMENT, getAction(
				textEditor, "Comment")); //$NON-NLS-1$
		bars.setGlobalActionHandler(PHPdtActionConstants.UNCOMMENT, getAction(
				textEditor, "Uncomment")); //$NON-NLS-1$
		bars.setGlobalActionHandler(PHPdtActionConstants.TOGGLE_COMMENT,
				getAction(textEditor, "ToggleComment")); //$NON-NLS-1$
		bars.setGlobalActionHandler(PHPdtActionConstants.FORMAT, getAction(
				textEditor, "Format")); //$NON-NLS-1$
		bars.setGlobalActionHandler(PHPdtActionConstants.ADD_BLOCK_COMMENT,
				getAction(textEditor, "AddBlockComment")); //$NON-NLS-1$
		bars.setGlobalActionHandler(PHPdtActionConstants.REMOVE_BLOCK_COMMENT,
				getAction(textEditor, "RemoveBlockComment")); //$NON-NLS-1$
		// bars.setGlobalActionHandler(PHPdtActionConstants.INDENT, getAction(
		// textEditor, "Indent")); //$NON-NLS-1$ //$NON-NLS-2$

		if (textEditor != null) {
			IFile file = null;
			IEditorInput editorInput = textEditor.getEditorInput();

			if (editorInput instanceof IFileEditorInput) {
				file = ((IFileEditorInput) editorInput).getFile();
			}

			PHPeclipsePlugin.getDefault().setLastEditorFile(file);

			ShowExternalPreviewAction fShowExternalPreviewAction = ShowExternalPreviewAction
					.getInstance();
			fShowExternalPreviewAction.setEditor(textEditor);
			fShowExternalPreviewAction.update();
			// if (fShowExternalPreviewAction != null) {
			// fShowExternalPreviewAction
			// .doRun(ShowExternalPreviewAction.PHP_TYPE);
			// }
			fShowExternalPreviewAction
					.refresh(ShowExternalPreviewAction.PHP_TYPE);
		}
	}
}