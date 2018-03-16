/**
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 �*
 * Contributors:
 *    IBM - Initial API and implementation
 */
package net.sourceforge.phpeclipse.webbrowser;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Action to open the Web broswer.
 */
public class OpenBrowserWorkbenchAction implements
		IWorkbenchWindowActionDelegate {
	/**
	 * OpenBrowserWorkbenchAction constructor comment.
	 */
	public OpenBrowserWorkbenchAction() {
		super();
	}

	/**
	 * Disposes this action delegate. The implementor should unhook any
	 * references to itself so that garbage collection can occur.
	 */
	public void dispose() {
	}

	/**
	 * Initializes this action delegate with the workbench window it will work
	 * in.
	 * 
	 * @param window
	 *            the window that provides the context for this delegate
	 */
	public void init(IWorkbenchWindow window) {
	}

	/**
	 * Performs this action.
	 * <p>
	 * This method is called when the delegating action has been triggered.
	 * Implement this method to do the actual work.
	 * </p>
	 * 
	 * @param action
	 *            the action proxy that handles the presentation portion of the
	 *            action
	 */
	public void run(IAction action) {
		WebBrowser.openURL(new WebBrowserEditorInput(null,
				WebBrowserEditorInput.SHOW_ALL
						| WebBrowserEditorInput.FORCE_NEW_PAGE));
	}

	/**
	 * Notifies this action delegate that the selection in the workbench has
	 * changed.
	 * <p>
	 * Implementers can use this opportunity to change the availability of the
	 * action or to modify other presentation properties.
	 * </p>
	 * 
	 * @param action
	 *            the action proxy that handles presentation portion of the
	 *            action
	 * @param selection
	 *            the current selection in the workbench
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
}