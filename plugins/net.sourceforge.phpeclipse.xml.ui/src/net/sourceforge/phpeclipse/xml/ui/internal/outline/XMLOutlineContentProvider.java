/*
 * Copyright (c) 2004 Christopher Lenz and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Christopher Lenz - initial API and implementation
 * 
 * $Id: XMLOutlineContentProvider.java,v 1.3 2006-10-21 23:14:14 pombredanne Exp $
 */

package net.sourceforge.phpeclipse.xml.ui.internal.outline;

import net.sourceforge.phpeclipse.xml.core.model.IXMLDocument;
import net.sourceforge.phpeclipse.xml.core.model.IXMLElement;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Content provider for the XML outline page.
 */
public class XMLOutlineContentProvider implements ITreeContentProvider {

	// Instance Variables ------------------------------------------------------

	/**
	 * The parsed XML document.
	 */
	private IXMLDocument document;

	// ITreeContentProvider Implementation -------------------------------------

	/*
	 * ITreeContentProvider#getChildren(Object)
	 */
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IXMLElement) {
			return ((IXMLElement) parentElement).getChildren();
		}
		return new Object[0];
	}

	/*
	 * @see ITreeContentProvider#getParent(Object)
	 */
	public Object getParent(Object element) {
		if (element instanceof IXMLElement) {
			return ((IXMLElement) element).getParent();
		}
		return null;
	}

	/*
	 * @see ITreeContentProvider#hasChildren(Object)
	 */
	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	/*
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object inputElement) {
		if ((document != null) && (document.getRoot() != null)) {
			return new Object[] { document.getRoot() };
		}
		return new Object[0];
	}

	/*
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		document = null;
	}

	/*
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer,
	 *      Object, Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (oldInput != newInput) {
			if (oldInput instanceof IXMLDocument) {
				document = null;
			}
			if (newInput instanceof IXMLDocument) {
				document = (IXMLDocument) newInput;
			}
		}
	}

}
