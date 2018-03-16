/**
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. � This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 �*
 * Contributors:
 *    IBM - Initial API and implementation
 */
package net.sourceforge.phpeclipse.webbrowser;

import net.sourceforge.phpeclipse.webbrowser.internal.WebBrowser;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;

/**
 * Web browser widget. Extends the eclipse Browser widget by adding an optional
 * toolbar and statusbar.
 */
public class ExtendedBrowser extends WebBrowser {
	public ExtendedBrowser(Composite parent, final boolean showToolbar,
			final boolean showStatusbar) {
		super(parent, showToolbar, showStatusbar);
	}

	/**
	 * Return the underlying browser control.
	 * 
	 * @return org.eclipse.swt.browser.Browser
	 */
	public Browser getBrowser() {
		return browser;
	}

	public void home() {
		super.home();
	}

	public void setURL(String url) {
		super.setURL(url);
	}
}