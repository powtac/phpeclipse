package net.sourceforge.phpeclipse.webbrowser;

/**
 * 
 */
public interface IExternalWebBrowser extends IWebBrowser {
	public String getLocation();

	public String getParameters();

	public void delete();

	public boolean isWorkingCopy();

	public IExternalWebBrowserWorkingCopy getWorkingCopy();
}