/**********************************************************************
 Copyright (c) 2000, 2002 IBM Corp. and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html

 Contributors:
 IBM Corporation - Initial implementation
 www.phpeclipse.de
 **********************************************************************/
package net.sourceforge.phpeclipse.phpeditor;

import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

/**
 * A toolbar action which toggles the presentation model of the connected text
 * editor. The editor shows either the highlight range only or always the whole
 * document.
 */
public class PresentationAction extends TextEditorAction {

	/**
	 * Constructs and updates the action.
	 */
	public PresentationAction() {
		super(PHPEditorMessages.getResourceBundle(),
				"TogglePresentation.", null); //$NON-NLS-1$
		update();
	}

	/*
	 * (non-Javadoc) Method declared on IAction
	 */
	public void run() {

		ITextEditor editor = getTextEditor();

		editor.resetHighlightRange();
		boolean show = editor.showsHighlightRangeOnly();
		setChecked(!show);
		editor.showHighlightRangeOnly(!show);
	}

	/*
	 * (non-Javadoc) Method declared on TextEditorAction
	 */
	public void update() {
		setChecked(getTextEditor() != null
				&& getTextEditor().showsHighlightRangeOnly());
		setEnabled(true);
	}
}
