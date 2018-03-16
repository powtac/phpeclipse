/**
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. � This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 �*
 * Contributors:
 *    IBM - Initial API and implementation
 */
package net.sourceforge.phpeclipse.webbrowser.internal;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.ActionFactory;

/**
 * ActionBarContributor for the Web browser. Just adds cut, copy, paste actions.
 */
public class WebBrowserEditorActionBarContributor implements
		IEditorActionBarContributor {
	protected IActionBars actionBars;

	/**
	 * WebBrowserEditorActionBarContributor constructor comment.
	 */
	public WebBrowserEditorActionBarContributor() {
		super();
	}

	/**
	 * Initializes this contributor, which is expected to add contributions as
	 * required to the given action bars and global action handlers.
	 * 
	 * @param bars
	 *            the action bars
	 */
	public void init(IActionBars bars, IWorkbenchPage page) {
		this.actionBars = bars;
	}

	/**
	 * Sets the active editor for the contributor. Implementors should
	 * disconnect from the old editor, connect to the new editor, and update the
	 * actions to reflect the new editor.
	 * 
	 * @param targetEditor
	 *            the new editor target
	 */
	public void setActiveEditor(IEditorPart targetEditor) {
		if (targetEditor instanceof WebBrowserEditor) {
			WebBrowserEditor editor = (WebBrowserEditor) targetEditor;

			actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(),
					editor.getCopyAction());
			actionBars.setGlobalActionHandler(ActionFactory.CUT.getId(), editor
					.getCutAction());
			actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(),
					editor.getPasteAction());

			editor.updateActions();
		}
	}

	/**
	 * Disposes this contributor.
	 */
	public void dispose() {
	}
}