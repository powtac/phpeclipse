package net.sourceforge.phpdt.externaltools.internal.ui;

/**********************************************************************
 Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
 This file is made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html
 **********************************************************************/

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * Content provider that maintains a generic list of objects which are shown in
 * a table viewer.
 */
public class ExternalToolsContentProvider implements IStructuredContentProvider {
	protected List elements = new ArrayList();

	protected TableViewer viewer;

	public void add(Object o) {
		if (elements.contains(o)) {
			return;
		}
		elements.add(o);
		viewer.add(o);
	}

	public void dispose() {
	}

	public Object[] getElements(Object inputElement) {
		return (Object[]) elements.toArray(new Object[elements.size()]);
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (TableViewer) viewer;
		elements.clear();
		if (newInput != null) {
			if (newInput instanceof List) {
				elements.addAll((List) newInput);
			} else {
				elements.addAll(Arrays.asList((Object[]) newInput));
			}
		}
	}

	public void remove(Object o) {
		elements.remove(o);
		viewer.remove(o);
	}

	public void remove(IStructuredSelection selection) {
		Object[] array = selection.toArray();
		elements.removeAll(Arrays.asList(array));
		viewer.remove(array);
	}
}
