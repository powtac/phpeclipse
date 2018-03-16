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

import java.net.URL;
import java.util.Iterator;

import net.sourceforge.phpeclipse.webbrowser.WebBrowser;
import net.sourceforge.phpeclipse.webbrowser.WebBrowserEditorInput;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;

/**
 * Action to open the Web broswer on a resource.
 */
public class OpenWithBrowserActionDelegate implements IActionDelegate {
	private IResource resource;

	/**
	 * OpenBrowserAction constructor comment.
	 */
	public OpenWithBrowserActionDelegate() {
		super();
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
		URL url = null;
		try {
			url = new URL("file://" + resource.getFullPath());
			WebBrowser.openURL(new WebBrowserEditorInput(url,
					WebBrowserEditorInput.SHOW_ALL
							| WebBrowserEditorInput.FORCE_NEW_PAGE));
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Error opening browser on file", e);
		}
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
	public void selectionChanged(IAction action, ISelection sel) {
		if (sel.isEmpty() || !(sel instanceof IStructuredSelection)) {
			action.setEnabled(false);
			return;
		}

		IStructuredSelection select = (IStructuredSelection) sel;
		Iterator iterator = select.iterator();
		Object selection = iterator.next();
		if (iterator.hasNext()) { // more than one selection (should never
									// happen)
			action.setEnabled(false);
			return;
		}

		if (!(selection instanceof IResource)) {
			action.setEnabled(false);
			return;
		}

		resource = (IResource) selection;
		action.setEnabled(true);
	}
}