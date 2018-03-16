package net.sourceforge.phpeclipse.webbrowser.internal;

import net.sourceforge.phpeclipse.webbrowser.IExternalWebBrowser;
import net.sourceforge.phpeclipse.webbrowser.IExternalWebBrowserWorkingCopy;

/**
 * 
 */
public class ExternalWebBrowserWorkingCopy extends ExternalWebBrowser implements
		IExternalWebBrowserWorkingCopy {
	protected ExternalWebBrowser browser;

	// creation
	public ExternalWebBrowserWorkingCopy() {
	}

	// working copy
	public ExternalWebBrowserWorkingCopy(ExternalWebBrowser browser) {
		this.browser = browser;
		setInternal(browser);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.phpeclipse.webbrowser.IExternalWebBrowserWorkingCopy#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.phpeclipse.webbrowser.IExternalWebBrowserWorkingCopy#setLocation(java.lang.String)
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.phpeclipse.webbrowser.IExternalWebBrowserWorkingCopy#setParameters(java.lang.String)
	 */
	public void setParameters(String params) {
		this.parameters = params;
	}

	public boolean isWorkingCopy() {
		return true;
	}

	public IExternalWebBrowserWorkingCopy getWorkingCopy() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.phpeclipse.webbrowser.IExternalWebBrowserWorkingCopy#save()
	 */
	public IExternalWebBrowser save() {
		if (browser != null) {
			browser.setInternal(this);
			BrowserManager.getInstance().browserChanged(browser);
		} else {
			browser = new ExternalWebBrowser();
			browser.setInternal(this);
			BrowserManager.getInstance().addBrowser(browser);
		}
		return browser;
	}
}