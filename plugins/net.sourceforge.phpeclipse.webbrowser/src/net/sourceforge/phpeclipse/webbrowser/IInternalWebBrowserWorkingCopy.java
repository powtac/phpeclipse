package net.sourceforge.phpeclipse.webbrowser;

/**
 * 
 */
public interface IInternalWebBrowserWorkingCopy extends IInternalWebBrowser {
	public void setUseNewPage(boolean b);

	public void setClearHistoryOnExit(boolean b);

	public IInternalWebBrowser save();
}