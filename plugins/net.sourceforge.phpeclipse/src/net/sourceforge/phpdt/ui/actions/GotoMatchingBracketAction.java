package net.sourceforge.phpdt.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import net.sourceforge.phpdt.internal.corext.Assert;
import net.sourceforge.phpeclipse.phpeditor.PHPEditor;
import net.sourceforge.phpeclipse.phpeditor.PHPEditorMessages;

import org.eclipse.jface.action.Action;

public class GotoMatchingBracketAction extends Action {

	public final static String GOTO_MATCHING_BRACKET = "GotoMatchingBracket"; //$NON-NLS-1$

	private final PHPEditor fEditor;

	public GotoMatchingBracketAction(PHPEditor editor) {
		super(PHPEditorMessages.getString("GotoMatchingBracket.label"));
		Assert.isNotNull(editor);
		fEditor = editor;
		setEnabled(null != fEditor);
	}

	public void run() {
		fEditor.gotoMatchingBracket();
	}

}