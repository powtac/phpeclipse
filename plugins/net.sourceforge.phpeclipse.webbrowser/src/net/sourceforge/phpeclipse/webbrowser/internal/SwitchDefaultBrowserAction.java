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
package net.sourceforge.phpeclipse.webbrowser.internal;

import net.sourceforge.phpeclipse.webbrowser.IInternalWebBrowser;
import net.sourceforge.phpeclipse.webbrowser.IWebBrowser;

import org.eclipse.jface.action.Action;

/**
 * Action to open the Web browser.
 */
public class SwitchDefaultBrowserAction extends Action {
	protected IWebBrowser webbrowser;

	/**
	 * SwitchDefaultBrowserAction constructor comment.
	 */
	public SwitchDefaultBrowserAction(IWebBrowser webbrowser, boolean current) {
		super();

		this.webbrowser = webbrowser;
		setText(webbrowser.getName());
		if (webbrowser instanceof IInternalWebBrowser)
			setImageDescriptor(ImageResource
					.getImageDescriptor(ImageResource.IMG_INTERNAL_BROWSER));
		else
			setImageDescriptor(ImageResource
					.getImageDescriptor(ImageResource.IMG_EXTERNAL_BROWSER));

		if (current)
			setChecked(true);
	}

	/**
	 * Implementation of method defined on <code>IAction</code>.
	 */
	public void run() {
		BrowserManager.getInstance().setCurrentWebBrowser(webbrowser);
	}
}