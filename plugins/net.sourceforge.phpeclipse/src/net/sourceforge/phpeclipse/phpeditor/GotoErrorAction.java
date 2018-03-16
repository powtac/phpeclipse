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

package net.sourceforge.phpeclipse.phpeditor;

import net.sourceforge.phpdt.internal.ui.IJavaHelpContextIds;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

public class GotoErrorAction extends TextEditorAction {

	private boolean fForward;

	public GotoErrorAction(String prefix, boolean forward) {
		super(PHPEditorMessages.getResourceBundle(), prefix, null);
		fForward = forward;
		if (forward)
			PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
					IJavaHelpContextIds.GOTO_NEXT_ERROR_ACTION);
		else
			PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
					IJavaHelpContextIds.GOTO_PREVIOUS_ERROR_ACTION);
	}

	public void run() {
		PHPEditor e = (PHPEditor) getTextEditor();
		e.gotoError(fForward);
	}

	public void setEditor(ITextEditor editor) {
		if (editor instanceof PHPEditor)
			super.setEditor(editor);
		update();
	}

	public void update() {
		setEnabled(getTextEditor() instanceof PHPEditor);
	}
}
