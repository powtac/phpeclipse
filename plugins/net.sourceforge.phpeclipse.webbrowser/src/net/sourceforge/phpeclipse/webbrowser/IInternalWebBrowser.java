package net.sourceforge.phpeclipse.webbrowser;

/**
 * 
 */
public interface IInternalWebBrowser extends IWebBrowser {
	public boolean getUseNewPage();

	public boolean getClearHistoryOnExit();

	public boolean isWorkingCopy();

	public IInternalWebBrowserWorkingCopy getWorkingCopy();
}