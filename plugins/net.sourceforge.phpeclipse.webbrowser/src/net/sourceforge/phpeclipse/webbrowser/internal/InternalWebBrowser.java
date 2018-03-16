package net.sourceforge.phpeclipse.webbrowser.internal;

import java.net.URL;

import net.sourceforge.phpeclipse.webbrowser.IInternalWebBrowser;
import net.sourceforge.phpeclipse.webbrowser.IInternalWebBrowserWorkingCopy;
import net.sourceforge.phpeclipse.webbrowser.WebBrowserEditorInput;

import org.eclipse.ui.IMemento;

/**
 * 
 */
public class InternalWebBrowser implements IInternalWebBrowser {
	private static final String MEMENTO_NEW_PAGE = "new_page";

	private static final String MEMENTO_CLEAR_HISTORY_ON_EXIT = "clear_history";

	protected boolean useNewPage;

	protected boolean clearHistory;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.phpeclipse.webbrowser.IWebBrowser#getName()
	 */
	public String getName() {
		return WebBrowserUIPlugin.getResource("%internalWebBrowserName");
	}

	public boolean getUseNewPage() {
		return useNewPage;
	}

	public boolean getClearHistoryOnExit() {
		return clearHistory;
	}

	public boolean isWorkingCopy() {
		return false;
	}

	public IInternalWebBrowserWorkingCopy getWorkingCopy() {
		return new InternalWebBrowserWorkingCopy(this);
	}

	protected void setInternal(IInternalWebBrowser browser) {
		useNewPage = browser.getUseNewPage();
		clearHistory = browser.getClearHistoryOnExit();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.phpeclipse.webbrowser.IWebBrowser#openURL(java.net.URL)
	 */
	public void openURL(URL url) {
		WebBrowserEditor.open(new WebBrowserEditorInput(url));
	}

	protected void save(IMemento memento) {
		memento.putString(MEMENTO_NEW_PAGE, useNewPage ? "true" : "false");
		memento.putString(MEMENTO_CLEAR_HISTORY_ON_EXIT, clearHistory ? "true"
				: "false");
	}

	protected void load(IMemento memento) {
		String s = memento.getString(MEMENTO_NEW_PAGE);
		if ("true".equals(s))
			useNewPage = true;
		else
			useNewPage = false;

		s = memento.getString(MEMENTO_CLEAR_HISTORY_ON_EXIT);
		if ("true".equals(s))
			clearHistory = true;
		else
			clearHistory = false;
	}

	public String toString() {
		return "Internal Web browser";
	}
}