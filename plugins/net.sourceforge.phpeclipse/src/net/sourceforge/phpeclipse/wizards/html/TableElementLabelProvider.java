/*
 * $Id: TableElementLabelProvider.java,v 1.3 2006-10-21 23:18:43 pombredanne Exp $
 * Copyright Narushima Hironori. All rights reserved.
 */
package net.sourceforge.phpeclipse.wizards.html;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.w3c.dom.Element;

/**
 * 
 */
public class TableElementLabelProvider implements ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		Element[] cells = TableElementModel
				.chooseCellElements((Element) element);
		if (columnIndex < cells.length) {
			Element elem = cells[columnIndex];
			return elem.toString();
		} else {
			throw new IllegalArgumentException("Invalid element:" + element);
		}
	}

	public boolean isLabelProperty(Object element, String property) {
		return TableElementModel.toNumeric(property) != -1;
	}

	public void addListener(ILabelProviderListener listener) {
	}

	public void removeListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}

}
