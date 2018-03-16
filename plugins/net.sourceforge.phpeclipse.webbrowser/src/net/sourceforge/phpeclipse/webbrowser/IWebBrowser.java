package net.sourceforge.phpeclipse.webbrowser;

import java.net.URL;

/**
 * 
 */
public interface IWebBrowser {
	public String getName();

	public void openURL(URL url);
}