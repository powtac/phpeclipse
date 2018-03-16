/*
 * $Id: TableElementContentProvider.java,v 1.3 2006-10-21 23:18:43 pombredanne Exp $
 * Copyright Narushima Hironori. All rights reserved.
 */
package net.sourceforge.phpeclipse.wizards.html;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * 
 */
public class TableElementContentProvider implements IStructuredContentProvider {

	public TableElementContentProvider() {
		super();
	}

	public Object[] getElements(Object inputElement) {
		TableElementModel model = (TableElementModel) inputElement;
		return model.getRows();
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

	}

}
