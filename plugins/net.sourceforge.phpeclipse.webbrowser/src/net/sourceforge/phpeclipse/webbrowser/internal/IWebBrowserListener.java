/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 �*
 * Contributors:
 *    IBM - Initial API and implementation
 **********************************************************************/
package net.sourceforge.phpeclipse.webbrowser.internal;

import net.sourceforge.phpeclipse.webbrowser.IWebBrowser;

/**
 * 
 */
public interface IWebBrowserListener {
	public void browserAdded(IWebBrowser browser);

	public void browserChanged(IWebBrowser browser);

	public void browserRemoved(IWebBrowser browser);
}