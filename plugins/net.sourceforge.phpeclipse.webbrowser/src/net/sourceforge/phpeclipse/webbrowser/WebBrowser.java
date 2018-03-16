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
import java.util.List;

import net.sourceforge.phpeclipse.webbrowser.internal.BrowserManager;
import net.sourceforge.phpeclipse.webbrowser.internal.ExternalWebBrowserWorkingCopy;
import net.sourceforge.phpeclipse.webbrowser.internal.Trace;
import net.sourceforge.phpeclipse.webbrowser.internal.WebBrowserEditor;
import net.sourceforge.phpeclipse.webbrowser.internal.WebBrowserUIPlugin;
import net.sourceforge.phpeclipse.webbrowser.internal.WebBrowserUtil;

import org.eclipse.swt.widgets.Display;

/**
 * The main interface to the internal Web browser. If allows you to query the
 * file types supported by the Web browser and open a URL.
 */
public class WebBrowser {
	/**
	 * WebBrowser constructor comment.
	 */
	private WebBrowser() {
		super();
	}

	/**
	 * Returns true if the internal Web browser is supported on this platform
	 * and the user has chosen to use it.
	 * 
	 * @return boolean
	 */
	public static boolean isUsingInternalBrowser() {
		return (getCurrentWebBrowser() instanceof IInternalWebBrowser);
	}

	/**
	 * Display the given URL in a Web browser. If the user has chosen not to use
	 * the internal browser, an external browser will be used. If not, a browser
	 * in the current page will be reused if forceNewPage is not true and the
	 * user preference is not set. Finally, showToolbar will decide when the
	 * toolbar should be shown in the internal browser.
	 * 
	 * @param input
	 */
	public static void openURL(final IWebBrowserEditorInput input) {
		Trace.trace(Trace.FINEST, "openURL() " + input);
		if (input == null)
			return;

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (!isUsingInternalBrowser()) {
					IWebBrowser browser = getCurrentWebBrowser();
					browser.openURL(input.getURL());
				} else
					WebBrowserEditor.open(input);
			}
		});
	}

	/**
	 * Return a list of all the installed Web browsers.
	 * 
	 * @return
	 */
	public static List getWebBrowsers() {
		return BrowserManager.getInstance().getWebBrowsers();
	}

	/**
	 * Return the current default web browser.
	 * 
	 * @return
	 */
	public static IWebBrowser getCurrentWebBrowser() {
		return BrowserManager.getInstance().getCurrentWebBrowser();
	}

	/**
	 * Set the current default web browser.
	 * 
	 * @return
	 */
	public static void getCurrentWebBrowser(IWebBrowser browser) {
		BrowserManager.getInstance().setCurrentWebBrowser(browser);
	}

	/**
	 * Create a new external Web browser.
	 * 
	 * @return
	 */
	public static IExternalWebBrowserWorkingCopy createExternalWebBrowser() {
		return new ExternalWebBrowserWorkingCopy();
	}

	/**
	 * Display the given URL in a Web browser.
	 * 
	 * @param url
	 *            java.net.URL
	 */
	public static void openURL(URL url) {
		IWebBrowser browser = getCurrentWebBrowser();
		if (browser != null)
			browser.openURL(url);
		else {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					WebBrowserUtil.openError(WebBrowserUIPlugin
							.getResource("%errorNoBrowser"));
				}
			});
		}
	}
}