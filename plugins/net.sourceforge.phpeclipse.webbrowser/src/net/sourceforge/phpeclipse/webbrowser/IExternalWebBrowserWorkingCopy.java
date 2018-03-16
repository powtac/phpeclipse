package net.sourceforge.phpeclipse.webbrowser;

/**
 * 
 */
public interface IExternalWebBrowserWorkingCopy extends IExternalWebBrowser {
	public void setName(String name);

	public void setLocation(String location);

	public void setParameters(String params);

	public IExternalWebBrowser save();
}