/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 �*
 * Contributors:
 *    IBM - Initial API and implementation
 **********************************************************************/
package net.sourceforge.phpeclipse.webbrowser.internal;

import net.sourceforge.phpeclipse.webbrowser.IExternalWebBrowser;
import net.sourceforge.phpeclipse.webbrowser.IInternalWebBrowser;
import net.sourceforge.phpeclipse.webbrowser.IWebBrowser;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Web browser table label provider.
 */
public class BrowserTableLabelProvider implements ITableLabelProvider {
	/**
	 * BrowserTableLabelProvider constructor comment.
	 */
	public BrowserTableLabelProvider() {
		super();
	}

	/**
	 * 
	 */
	public void addListener(ILabelProviderListener listener) {
	}

	/**
	 * 
	 */
	public void dispose() {
	}

	/**
	 * 
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex == 0) {
			if (element instanceof IInternalWebBrowser)
				return ImageResource
						.getImage(ImageResource.IMG_INTERNAL_BROWSER);
			else
				return ImageResource
						.getImage(ImageResource.IMG_EXTERNAL_BROWSER);
		}
		return null;
	}

	/**
	 * Returns the label text for the given column of the given element.
	 * 
	 * @param element
	 *            the object representing the entire row, or <code>null</code>
	 *            indicating that no input object is set in the viewer
	 * @param columnIndex
	 *            the zero-based index of the column in which the label appears
	 */
	public String getColumnText(Object element, int columnIndex) {
		IWebBrowser browser = (IWebBrowser) element;
		if (browser instanceof IExternalWebBrowser) {
			if (columnIndex == 0)
				return notNull(((IExternalWebBrowser) browser).getName());
			else if (columnIndex == 1)
				return notNull(((IExternalWebBrowser) browser).getLocation());
			else if (columnIndex == 2)
				return notNull(((IExternalWebBrowser) browser).getParameters());
		} else if (browser instanceof IInternalWebBrowser) {
			if (columnIndex == 0)
				return notNull(((IInternalWebBrowser) browser).getName());
		}
		return "";
	}

	protected String notNull(String s) {
		if (s != null)
			return s;
		else
			return "";
	}

	/**
	 * 
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/**
	 * 
	 */
	public void removeListener(ILabelProviderListener listener) {
	}
}