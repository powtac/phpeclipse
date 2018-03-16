package net.sourceforge.phpeclipse.webbrowser.views;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.part.ShowInContext;

/**
 * Adds an URL String to the ShowInContext for the BrowserUtil
 * 
 * 
 */
public class ShowInContextBrowser extends ShowInContext {
	String fLocalhostUrl;

	public ShowInContextBrowser(Object input, ISelection selection,
			String localhostUrl) {
		super(input, selection);
		fLocalhostUrl = localhostUrl;
	}

	public String getLocalhostUrl() {
		return fLocalhostUrl;
	}
}
