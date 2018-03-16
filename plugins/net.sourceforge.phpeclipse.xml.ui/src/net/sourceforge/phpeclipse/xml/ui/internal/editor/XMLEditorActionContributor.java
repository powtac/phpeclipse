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
package net.sourceforge.phpeclipse.xml.ui.internal.editor;

import net.sourceforge.phpeclipse.ui.editor.ShowExternalPreviewAction;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Common base class for action contributors for Java editors.
 */
public class XMLEditorActionContributor extends
		BasicTextEditorActionContributor {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorActionBarContributor#setActiveEditor(org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor(IEditorPart part) {
		super.setActiveEditor(part);
		ITextEditor textEditor = null;
		if (part instanceof ITextEditor)
			textEditor = (ITextEditor) part;

		if (textEditor != null) {
			IFile file = null;
			IEditorInput editorInput = textEditor.getEditorInput();

			if (editorInput instanceof IFileEditorInput) {
				file = ((IFileEditorInput) editorInput).getFile();
			}

			ShowExternalPreviewAction fShowExternalPreviewAction = ShowExternalPreviewAction
					.getInstance();
			fShowExternalPreviewAction.setEditor(textEditor);
			fShowExternalPreviewAction.update();
			if (fShowExternalPreviewAction != null)
				fShowExternalPreviewAction
						.doRun(ShowExternalPreviewAction.PHP_TYPE);
		}
	}
}
