/**
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 �*
 * Contributors:
 *    IBM - Initial API and implementation
 */
package net.sourceforge.phpeclipse.webbrowser;

import java.net.URL;

import net.sourceforge.phpeclipse.webbrowser.internal.ImageResource;
import net.sourceforge.phpeclipse.webbrowser.internal.Trace;
import net.sourceforge.phpeclipse.webbrowser.internal.WebBrowserPreference;
import net.sourceforge.phpeclipse.webbrowser.internal.WebBrowserUIPlugin;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

/**
 * The editor input for the integrated web browser.
 */
public class WebBrowserEditorInput implements IWebBrowserEditorInput,
		IPersistableElement, IElementFactory {
	// --- constants to pass into constructor ---

	// if used, the toolbar will be available
	public static final int SHOW_TOOLBAR = 1 << 1;

	// if used, the status bar will be available
	public static final int SHOW_STATUSBAR = 1 << 2;

	// if used, this input will always force a new page
	// and will never reuse an open Web browser
	public static final int FORCE_NEW_PAGE = 1 << 3;

	// if used, the original URL will be saved and
	// the page can reopen to the same URL after
	// shutting down
	public static final int SAVE_URL = 1 << 5;

	// if used, the browser will be transient and will not appear
	// in the most recently used file list, nor will it reopen after
	// restarting Eclipse
	public static final int TRANSIENT = 1 << 6;

	public static final int SHOW_ALL = SHOW_TOOLBAR | SHOW_STATUSBAR;

	private static final String ELEMENT_FACTORY_ID = "net.sourceforge.phpeclipse.webbrowser.elementFactory";

	private static final String MEMENTO_URL = "url";

	private static final String MEMENTO_STYLE = "style";

	private static final String MEMENTO_ID = "id";

	private URL url;

	private int style;

	private String id = null;

	/**
	 * WebBrowser editor input for the homepage.
	 */
	public WebBrowserEditorInput() {
		this(null);
	}

	/**
	 * WebBrowserEditorInput constructor comment.
	 */
	public WebBrowserEditorInput(URL url) {
		this(url, SHOW_ALL | SAVE_URL);
	}

	/**
	 * WebBrowserEditorInput constructor comment.
	 */
	public WebBrowserEditorInput(URL url, int style) {
		super();
		this.url = url;
		this.style = style;
	}

	/**
	 * WebBrowserEditorInput constructor comment.
	 */
	public WebBrowserEditorInput(URL url, int style, String browserId) {
		super();
		this.url = url;
		this.style = style;
		this.id = browserId;
	}

	/**
	 * WebBrowserEditorInput constructor comment.
	 */
	public WebBrowserEditorInput(URL url, boolean b) {
		this(url);
	}

	/**
	 * Returns true if this page can reuse the browser that the given input is
	 * being displayed in, or false if it should open up in a new page.
	 * 
	 * @param input
	 *            net.sourceforge.phpeclipse.webbrowser.IWebBrowserEditorInput
	 * @return boolean
	 */
	public boolean canReplaceInput(IWebBrowserEditorInput input) {
		Trace.trace(Trace.FINEST, "canReplaceInput " + this + " " + input);
		if ((style & FORCE_NEW_PAGE) != 0)
			return false;
		else if (input.isToolbarVisible() != isToolbarVisible())
			return false;
		else if (input.isStatusbarVisible() != isStatusbarVisible())
			return false;
		else if (id != null) {
			if (!(input instanceof WebBrowserEditorInput))
				return false;
			String bid = ((WebBrowserEditorInput) input).getBrowserId();
			return id.equals(bid);
		} else
			return false;
	}

	/**
	 * Creates an <code>IElement</code> from the state captured within an
	 * <code>IMemento</code>.
	 * 
	 * @param memento
	 *            a memento containing the state for an element
	 * @return an element, or <code>null</code> if the element could not be
	 *         created
	 */
	public IAdaptable createElement(IMemento memento) {
		URL url2 = null;
		try {
			url2 = new URL(WebBrowserPreference.getHomePageURL());
		} catch (Exception e) {
			// could not determine the URL
		}

		int newStyle = SHOW_TOOLBAR | SHOW_STATUSBAR;
		try {
			newStyle = memento.getInteger(MEMENTO_STYLE).intValue();

			if ((newStyle & SAVE_URL) != 0)
				url = new URL(memento.getString(MEMENTO_URL));
		} catch (Exception e) {
			// could not determine the style
		}

		String id2 = null;
		try {
			id2 = memento.getString(MEMENTO_ID);
			if (id2 != null && id2.length() < 1)
				id2 = null;
		} catch (Exception e) {
		}

		return new WebBrowserEditorInput(url2, newStyle, id2);
	}

	/**
	 * Indicates whether some other object is "equal to" this one. In this case
	 * it means that the underlying IFolders are equal.
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof WebBrowserEditorInput))
			return false;
		WebBrowserEditorInput other = (WebBrowserEditorInput) obj;

		if (url != null && !url.equals(obj))
			return false;

		return canReplaceInput(other);
	}

	/**
	 * Returns whether the editor input exists.
	 * <p>
	 * This method is primarily used to determine if an editor input should
	 * appear in the "File Most Recently Used" menu. An editor input will appear
	 * in the list until the return value of <code>exists</code> becomes
	 * <code>false</code> or it drops off the bottom of the list.
	 * 
	 * @return <code>true</code> if the editor input exists;
	 *         <code>false</code> otherwise
	 */
	public boolean exists() {
		if ((style & TRANSIENT) != 0)
			return false;
		else
			return true;
	}

	/**
	 * Returns an object which is an instance of the given class associated with
	 * this object. Returns <code>null</code> if no such object can be found.
	 * 
	 * @param adapter
	 *            the adapter class to look up
	 * @return a object castable to the given class, or <code>null</code> if
	 *         this object does not have an adapter for the given class
	 */
	public Object getAdapter(Class adapter) {
		return null;
	}

	/**
	 * Returns the ID of an element factory which can be used to recreate this
	 * object. An element factory extension with this ID must exist within the
	 * workbench registry.
	 * 
	 * @return the element factory ID
	 */
	public String getFactoryId() {
		return ELEMENT_FACTORY_ID;
	}

	public ImageDescriptor getImageDescriptor() {
		return ImageResource
				.getImageDescriptor(ImageResource.IMG_INTERNAL_BROWSER);
	}

	/**
	 * Returns the name of this editor input for display purposes.
	 * <p>
	 * For instance, if the fully qualified input name is
	 * <code>"a\b\MyFile.gif"</code>, the return value would be just
	 * <code>"MyFile.gif"</code>.
	 * 
	 * @return the file name string
	 */
	public String getName() {
		return WebBrowserUIPlugin.getResource("%viewWebBrowserTitle");
	}

	/*
	 * Returns an object that can be used to save the state of this editor
	 * input.
	 * 
	 * @return the persistable element, or <code>null</code> if this editor
	 * input cannot be persisted
	 */
	public IPersistableElement getPersistable() {
		if ((style & TRANSIENT) != 0)
			return null;
		else
			return this;
	}

	public String getToolTipText() {
		if (url != null)
			return url.toExternalForm();
		else
			return WebBrowserUIPlugin.getResource("%viewWebBrowserTitle");
	}

	/**
	 * Returns the url.
	 * 
	 * @return java.net.URL
	 */
	public URL getURL() {
		return url;
	}

	/**
	 * Returns the browser id. Browsers with a set id will always & only be
	 * replaced by browsers with the same id.
	 * 
	 * @return String
	 */
	public String getBrowserId() {
		return id;
	}

	/**
	 * Returns true if the status bar should be shown.
	 * 
	 * @return boolean
	 */
	public boolean isStatusbarVisible() {
		return (style & SHOW_STATUSBAR) != 0;
	}

	/**
	 * Returns true if the toolbar should be shown.
	 * 
	 * @return boolean
	 */
	public boolean isToolbarVisible() {
		return (style & SHOW_TOOLBAR) != 0;
	}

	/**
	 * Saves the state of an element within a memento.
	 * 
	 * @param memento
	 *            the storage area for element state
	 */
	public void saveState(IMemento memento) {
		if ((style & SAVE_URL) != 0 && url != null)
			memento.putString(MEMENTO_URL, url.toExternalForm());

		memento.putInteger(MEMENTO_STYLE, style);

		if (id != null)
			memento.putString(MEMENTO_ID, id);
	}

	/**
	 * Converts this object to a string.
	 * 
	 * @return java.lang.String
	 */
	public String toString() {
		return "WebBrowserEditorInput[" + url + " " + style + " " + id + "]";
	}
}