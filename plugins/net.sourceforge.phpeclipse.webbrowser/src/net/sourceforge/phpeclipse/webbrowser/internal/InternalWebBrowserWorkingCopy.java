package net.sourceforge.phpeclipse.webbrowser.internal;

import net.sourceforge.phpeclipse.webbrowser.IInternalWebBrowser;
import net.sourceforge.phpeclipse.webbrowser.IInternalWebBrowserWorkingCopy;

/**
 * 
 */
public class InternalWebBrowserWorkingCopy extends InternalWebBrowser implements
		IInternalWebBrowserWorkingCopy {
	protected InternalWebBrowser browser;

	// working copy
	public InternalWebBrowserWorkingCopy(InternalWebBrowser browser) {
		this.browser = browser;
		setInternal(browser);
	}

	public void setUseNewPage(boolean b) {
		useNewPage = b;
	}

	public void setClearHistoryOnExit(boolean b) {
		clearHistory = b;
	}

	public boolean isWorkingCopy() {
		return true;
	}

	public IInternalWebBrowserWorkingCopy getWorkingCopy() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.phpeclipse.webbrowser.IExternalWebBrowserWorkingCopy#save()
	 */
	public IInternalWebBrowser save() {
		if (browser != null) {
			browser.setInternal(this);
			BrowserManager.getInstance().browserChanged(browser);
		}
		return browser;
	}
}