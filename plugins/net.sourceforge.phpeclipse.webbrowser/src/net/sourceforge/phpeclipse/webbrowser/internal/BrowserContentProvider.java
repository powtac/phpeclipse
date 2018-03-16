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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.phpeclipse.webbrowser.IWebBrowser;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Monitor content provider.
 */
public class BrowserContentProvider implements IStructuredContentProvider {
	/**
	 * BrowserContentProvider constructor comment.
	 */
	public BrowserContentProvider() {
		super();
	}

	/**
	 * Disposes of this content provider. This is called by the viewer when it
	 * is disposed.
	 */
	public void dispose() {
	}

	/**
	 * Returns the elements to display in the viewer when its input is set to
	 * the given element. These elements can be presented as rows in a table,
	 * items in a list, etc. The result is not modified by the viewer.
	 * 
	 * @param inputElement
	 *            the input element
	 * @return the array of elements to display in the viewer
	 */
	public Object[] getElements(Object inputElement) {
		List list = new ArrayList();
		Iterator iterator = BrowserManager.getInstance().getWebBrowsers()
				.iterator();
		while (iterator.hasNext()) {
			IWebBrowser browser = (IWebBrowser) iterator.next();
			list.add(browser);
		}
		return list.toArray();
	}

	/**
	 * Notifies this content provider that the given viewer's input has been
	 * switched to a different element.
	 * <p>
	 * A typical use for this method is registering the content provider as a
	 * listener to changes on the new input (using model-specific means), and
	 * deregistering the viewer from the old input. In response to these change
	 * notifications, the content provider propagates the changes to the viewer.
	 * </p>
	 * 
	 * @param viewer
	 *            the viewer
	 * @param oldInput
	 *            the old input element, or <code>null</code> if the viewer
	 *            did not previously have an input
	 * @param newInput
	 *            the new input element, or <code>null</code> if the viewer
	 *            does not have an input
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}