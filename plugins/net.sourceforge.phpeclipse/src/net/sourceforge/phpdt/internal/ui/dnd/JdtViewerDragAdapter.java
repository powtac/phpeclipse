/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.phpdt.internal.ui.dnd;

import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DragSourceEvent;

public class JdtViewerDragAdapter extends DelegatingDragAdapter {

	private StructuredViewer fViewer;

	public JdtViewerDragAdapter(StructuredViewer viewer,
			TransferDragSourceListener[] listeners) {
		super(listeners);
		Assert.isNotNull(viewer);
		fViewer = viewer;
	}

	public void dragStart(DragSourceEvent event) {
		IStructuredSelection selection = (IStructuredSelection) fViewer
				.getSelection();
		if (selection.isEmpty()) {
			event.doit = false;
			return;
		}
		super.dragStart(event);
	}
}
