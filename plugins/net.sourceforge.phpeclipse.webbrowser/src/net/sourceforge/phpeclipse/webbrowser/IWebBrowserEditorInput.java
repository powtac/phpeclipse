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

import java.net.URL;

import org.eclipse.ui.IEditorInput;

/**
 * The editor input for the Web browser editor. If the integrated Web browser
 * exists for this platform, (and the user has chosen to use it) this
 * information will be used to populate the Web browser. If not, this
 * information will be used to launch an external Web browser.
 */
public interface IWebBrowserEditorInput extends IEditorInput {
	/**
	 * Returns true if this page can reuse the browser that the given input is
	 * being displayed in, or false if it should open up in a new page.
	 * 
	 * @param input
	 *            net.sourceforge.phpeclipse.webbrowser.IWebBrowserEditorInput
	 * @return boolean
	 */
	public boolean canReplaceInput(IWebBrowserEditorInput input);

	/**
	 * Returns the url that should be displayed in the browser.
	 * 
	 * @return java.net.URL
	 */
	public URL getURL();

	/**
	 * Returns true if the Web statusbar should be shown.
	 * 
	 * @return boolean
	 */
	public boolean isStatusbarVisible();

	/**
	 * Returns true if the Web toolbar should be shown.
	 * 
	 * @return boolean
	 */
	public boolean isToolbarVisible();
}