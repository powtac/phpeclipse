/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.phpeclipse.webbrowser.views;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import net.sourceforge.phpeclipse.webbrowser.internal.WebBrowser;
import net.sourceforge.phpeclipse.webbrowser.internal.WebBrowserUtil;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.CloseWindowListener;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;

/**
 * <code>BrowserView</code> is a simple demonstration of the SWT Browser
 * widget. It consists of a workbench view and tab folder where each tab in the
 * folder allows the user to interact with a control.
 * 
 * @see ViewPart
 */
public class BrowserView extends ViewPart implements IShowInTarget {
	public final static String ID_BROWSER = "net.sourceforge.phpeclipse.webbrowser.views";

	WebBrowser fInstance = null;

	String fUrl = null;

	/**
	 * Create the example
	 * 
	 * @see ViewPart#createPartControl
	 */
	public void createPartControl(Composite frame) {
		try {
			if (WebBrowserUtil.isInternalBrowserOperational()) {
				fInstance = new WebBrowser(frame, true, true);
				// #1365431 (toshihiro) start
				fInstance.getBrowser().addCloseWindowListener(
						new CloseWindowListener() {
							public void close(WindowEvent event) {
								getViewSite().getPage().hideView(
										BrowserView.this);
							}
						});
				// #1365431 (toshihiro) end
			}
		} catch (Exception e) {
			fInstance = null;
		}
	}

	/**
	 * Called when we must grab focus.
	 * 
	 * @see org.eclipse.ui.part.ViewPart#setFocus
	 */
	public void setFocus() {
		if (fInstance != null) {
			fInstance.setFocus();
		}
	}

	/**
	 * Called when the View is to be disposed
	 */
	public void dispose() {
		if (fInstance != null) {
			fInstance.dispose();
			fInstance = null;
		}
		super.dispose();
	}

	public String getUrl() {
		if (fInstance != null) {
			return fInstance.getURL();
		} else {
			return null;
		}
	}

	public void setUrl(final String url) {
		if (fInstance != null) {
				fUrl = url;
				fInstance.setURL(url);
			// try {
			// ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
			// public void run(IProgressMonitor monitor) throws CoreException {
			// instance.setURL(url);
			// }
			// }, null);
			// } catch (CoreException e) {
			// // TO DO Auto-generated catch block
			// e.printStackTrace();
			// }
		}
	}

	public void refresh() {
		if (fInstance != null) {
			fInstance.refresh();
			// try {
			// ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
			// public void run(IProgressMonitor monitor) throws CoreException {
			// instance.refresh();
			// }
			// }, null);
			// } catch (CoreException e) {
			// // TO DO Auto-generated catch block
			// e.printStackTrace();
			// }
		}
	}

	
	
	public void refresh(String url) {
		if (fInstance != null && url != null) {
			if (fUrl == null) {
				setUrl(url);
			} else {
				Browser browser = fInstance.getBrowser();
				if (browser != null) {
					String browserUrl = browser.getUrl();
					try {
						browserUrl = URLDecoder.decode(browserUrl, J5CharsetEmulator.defaultCharset().name());
					} catch (UnsupportedEncodingException e) {
						// e.printStackTrace();
					}
					if (!url.equals(browserUrl)) {
						setUrl(url);
					}
				}
			}
		}
	}

	public void addProgressListener(ProgressListener listener) {
		if (fInstance != null) {
			fInstance.addProgressListener(listener);
		}
	}

	public void addStatusTextListener(StatusTextListener listener) {
		if (fInstance != null) {
			fInstance.addStatusTextListener(listener);
		}
	}

	public void addTitleListener(TitleListener listener) {
		if (fInstance != null) {
			fInstance.addTitleListener(listener);
		}
	}

	public boolean show(ShowInContext context) {
		if (context instanceof ShowInContextBrowser) {
			ShowInContextBrowser contextBrowser = (ShowInContextBrowser) context;
			String localhostURL = contextBrowser.getLocalhostUrl();
			if (localhostURL != null) {
				setUrl(localhostURL);
				return true;
			}
		}
		// *WARNING*
		// This causes unexpected behaviour such as downloading (save file).
		// It depends on mime-types setting and native browser, it isn't under control of eclipse.
		// (IE shows script as plain text since .php is unknown type by default.
		//  Mozilla downloads script file since .php is defined in mimeTypes.rdf as such.)
		//
		//if (context.getInput() instanceof IFile) {
		//	IFile file = (IFile) context.getInput();
		//	String localhostURL;
		//	localhostURL = "file:///" + file.getLocation().toString();
		//	setUrl(localhostURL);
		//	return true;
		//}
		return false;
	}
}