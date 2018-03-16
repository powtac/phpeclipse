package net.sourceforge.phpeclipse.webbrowser.internal;

import java.net.URL;

import net.sourceforge.phpeclipse.webbrowser.IExternalWebBrowser;
import net.sourceforge.phpeclipse.webbrowser.IExternalWebBrowserWorkingCopy;

import org.eclipse.swt.program.Program;
import org.eclipse.ui.IMemento;

/**
 * 
 */
public class ExternalWebBrowser implements IExternalWebBrowser {
	private static final String MEMENTO_NAME = "name";

	private static final String MEMENTO_LOCATION = "location";

	private static final String MEMENTO_PARAMETERS = "parameters";

	protected String name;

	protected String location;

	protected String parameters;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.phpeclipse.webbrowser.IWebBrowser#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.phpeclipse.webbrowser.IExternalWebBrowser#getLocation()
	 */
	public String getLocation() {
		return location;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.phpeclipse.webbrowser.IExternalWebBrowser#getParameters()
	 */
	public String getParameters() {
		return parameters;
	}

	public void delete() {
		BrowserManager.getInstance().removeWebBrowser(this);
	}

	public boolean isWorkingCopy() {
		return false;
	}

	public IExternalWebBrowserWorkingCopy getWorkingCopy() {
		return new ExternalWebBrowserWorkingCopy(this);
	}

	protected void setInternal(IExternalWebBrowser browser) {
		name = browser.getName();
		location = browser.getLocation();
		parameters = browser.getParameters();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.phpeclipse.webbrowser.IWebBrowser#openURL(java.net.URL)
	 */
	public void openURL(URL url) {
		String urlText = WebBrowserPreference.getHomePageURL();

		if (url != null)
			urlText = url.toExternalForm();
		else if (urlText.startsWith("file:") & urlText.length() > 6) {
			if (urlText.charAt(5) != '/' && urlText.charAt(5) != '\\')
				urlText = urlText.substring(0, 5) + "/" + urlText.substring(5);
		}

		// change spaces to "%20"
		if (!WebBrowserUtil.isWindows()) {
			int index = urlText.indexOf(" ");
			while (index >= 0) {
				urlText = urlText.substring(0, index) + "%20"
						+ urlText.substring(index + 1);
				index = urlText.indexOf(" ");
			}
		}

		Trace.trace(Trace.FINEST, "Launching external Web browser: " + location
				+ " - " + parameters + " - " + urlText);
		if (location == null || location.length() == 0) {
			try {
				String extension = null;
				if (url != null)
					extension = url.getFile();
				else
					extension = "html";
				int index = extension.indexOf(".");
				if (index >= 0)
					extension = extension.substring(index + 1);
				Program program = Program.findProgram(extension);
				program.execute(urlText);
			} catch (Exception e) {
				Trace.trace(Trace.SEVERE,
						"Error launching default external browser", e);
				WebBrowserUtil.openError(WebBrowserUIPlugin.getResource(
						"%errorCouldNotLaunchWebBrowser", urlText));
			}
			return;
		}

		String params = parameters;
		if (params == null)
			params = "";

		int urlIndex = params.indexOf(WebBrowserPreference.URL_PARAMETER);
		if (urlIndex >= 0)
			params = params.substring(0, urlIndex)
					+ " "
					+ urlText
					+ " "
					+ params.substring(urlIndex
							+ WebBrowserPreference.URL_PARAMETER.length());
		else {
			if (!params.endsWith(" "))
				params += " ";
			params += urlText;
		}

		try {
			Trace.trace(Trace.FINEST, "Launching " + location + " " + params);
			Runtime.getRuntime().exec(location + " " + params);
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Could not launch external browser", e);
			WebBrowserUtil.openError(WebBrowserUIPlugin.getResource(
					"%errorCouldNotLaunchWebBrowser", urlText));
		}
	}

	protected void save(IMemento memento) {
		memento.putString(MEMENTO_NAME, name);
		memento.putString(MEMENTO_LOCATION, location);
		memento.putString(MEMENTO_PARAMETERS, parameters);
	}

	protected void load(IMemento memento) {
		name = memento.getString(MEMENTO_NAME);
		location = memento.getString(MEMENTO_LOCATION);
		parameters = memento.getString(MEMENTO_PARAMETERS);
	}

	public String toString() {
		return "External Web browser: " + getName() + " / " + getLocation()
				+ " / " + getParameters();
	}
}