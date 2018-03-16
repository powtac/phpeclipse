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
 * $Id: XMLOutlineLabelProvider.java,v 1.3 2006-10-21 23:14:14 pombredanne Exp $
 */

package net.sourceforge.phpeclipse.xml.ui.internal.outline;

import net.sourceforge.phpeclipse.xml.core.model.IXMLElement;
import net.sourceforge.phpeclipse.xml.ui.XMLPlugin;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Label provider for the XML outline page.
 */
public class XMLOutlineLabelProvider extends LabelProvider {

	// LabelProvider Implementation --------------------------------------------

	/**
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		if (element instanceof IXMLElement) {
			return XMLPlugin.getDefault().getImageRegistry().get(
					XMLPlugin.ICON_ELEMENT);
		}
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		if (element instanceof IXMLElement) {
			IXMLElement xmlElement = (IXMLElement) element;
			StringBuffer buf = new StringBuffer();
			if (xmlElement.getPrefix() != null) {
				buf.append(xmlElement.getPrefix());
				buf.append(':');
			}
			buf.append(xmlElement.getLocalName());
			return buf.toString();
		}
		return null;
	}

}
