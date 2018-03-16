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
package net.sourceforge.phpdt.phphelp.actions;

import java.io.IOException;
import java.text.MessageFormat;

import net.sourceforge.phpdt.phphelp.PHPHelpPlugin;
import net.sourceforge.phpeclipse.phpeditor.PHPEditor;
import net.sourceforge.phpeclipse.phpeditor.php.PHPWordExtractor;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionDelegate;

public class PHPEclipseShowContextHelp extends ActionDelegate implements
		IEditorActionDelegate {

	private IWorkbenchWindow window;

	private PHPEditor editor;

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (!selection.isEmpty()) {
			if (selection instanceof TextSelection) {
				action.setEnabled(true);
			} else if (window.getActivePage() != null
					&& window.getActivePage().getActivePart() != null) {
				//
			}
		}
	}

	public void run(IAction action) {
		if (editor == null) {
			IEditorPart targetEditor = window.getActivePage().getActiveEditor();
			if (targetEditor != null && (targetEditor instanceof PHPEditor)) {
				editor = (PHPEditor) targetEditor;
			}
		}
		if (editor != null) {
			ITextSelection selection = (ITextSelection) editor
					.getSelectionProvider().getSelection();
			IDocument doc = editor.getDocumentProvider().getDocument(
					editor.getEditorInput());
			if (null == window) {
				window = editor.getSite().getWorkbenchWindow();
			}
			int pos = selection.getOffset();
			String word = getFunctionName(doc, pos);
			openContextHelp(word);
		}
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor != null && (targetEditor instanceof PHPEditor)) {
			editor = (PHPEditor) targetEditor;
		}
	}

	public void openContextHelp(String word) {
		IPreferenceStore store = PHPHelpPlugin.getDefault()
				.getPreferenceStore();
		if (store.getBoolean(PHPHelpPlugin.PHP_CHM_ENABLED)) {
			String[] arguments = { store.getString(PHPHelpPlugin.PHP_CHM_FILE),
					word };
			MessageFormat form = new MessageFormat(store
					.getString(PHPHelpPlugin.PHP_CHM_COMMAND));
			try {
				Runtime runtime = Runtime.getRuntime();
				String command = form.format(arguments);

				runtime.exec(command);
			} catch (IOException e) {
			}
		} else {
			PHPFunctionHelpResource helpResource = new PHPFunctionHelpResource(
					word);
			window.getWorkbench().getHelpSystem().displayHelpResource(
					helpResource.getHref());
		}
	}

	private String getFunctionName(IDocument doc, int pos) {
		Point word = PHPWordExtractor.findWord(doc, pos);
		if (word != null) {
			try {
				return doc.get(word.x, word.y).replace('_', '-');
			} catch (BadLocationException e) {
			}
		}
		return "";
	}
}
