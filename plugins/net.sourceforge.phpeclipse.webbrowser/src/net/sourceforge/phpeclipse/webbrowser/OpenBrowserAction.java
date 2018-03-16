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

import org.eclipse.jface.action.Action;

/**
 * Action to open the Web browser.
 */
public class OpenBrowserAction extends Action {
	/**
	 * OpenBrowserAction constructor comment.
	 */
	public OpenBrowserAction() {
		super();
	}

	/**
	 * Implementation of method defined on <code>IAction</code>.
	 */
	public void run() {
		WebBrowser.openURL(new WebBrowserEditorInput(null,
				WebBrowserEditorInput.SHOW_ALL
						| WebBrowserEditorInput.FORCE_NEW_PAGE));
	}
}