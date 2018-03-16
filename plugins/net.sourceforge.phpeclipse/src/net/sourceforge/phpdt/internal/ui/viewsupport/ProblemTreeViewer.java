/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package net.sourceforge.phpdt.internal.ui.viewsupport;

import java.util.ArrayList;

import net.sourceforge.phpdt.core.IMember;
import net.sourceforge.phpdt.ui.IWorkingCopyProvider;
import net.sourceforge.phpdt.ui.ProblemsLabelDecorator.ProblemsLabelChangedEvent;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;

/**
 * Extends a TreeViewer to allow more performance when showing error ticks. A
 * <code>ProblemItemMapper</code> is contained that maps all items in the tree
 * to underlying resource
 */
public class ProblemTreeViewer extends TreeViewer {

	protected ResourceToItemsMapper fResourceToItemsMapper;

	/*
	 * @see TreeViewer#TreeViewer(Composite)
	 */
	public ProblemTreeViewer(Composite parent) {
		super(parent);
		initMapper();
	}

	/*
	 * @see TreeViewer#TreeViewer(Composite, int)
	 */
	public ProblemTreeViewer(Composite parent, int style) {
		super(parent, style);
		initMapper();
	}

	/*
	 * @see TreeViewer#TreeViewer(Tree)
	 */
	public ProblemTreeViewer(Tree tree) {
		super(tree);
		initMapper();
	}

	private void initMapper() {
		fResourceToItemsMapper = new ResourceToItemsMapper(this);
	}

	/*
	 * @see StructuredViewer#mapElement(Object, Widget)
	 */
	protected void mapElement(Object element, Widget item) {
		super.mapElement(element, item);
		if (item instanceof Item) {
			fResourceToItemsMapper.addToMap(element, (Item) item);
		}
	}

	/*
	 * @see StructuredViewer#unmapElement(Object, Widget)
	 */
	protected void unmapElement(Object element, Widget item) {
		if (item instanceof Item) {
			fResourceToItemsMapper.removeFromMap(element, (Item) item);
		}
		super.unmapElement(element, item);
	}

	/*
	 * @see StructuredViewer#unmapAllElements()
	 */
	protected void unmapAllElements() {
		fResourceToItemsMapper.clearMap();
		super.unmapAllElements();
	}

	/*
	 * @see ContentViewer#handleLabelProviderChanged(LabelProviderChangedEvent)
	 */
	protected void handleLabelProviderChanged(LabelProviderChangedEvent event) {
		if (event instanceof ProblemsLabelChangedEvent) {
			ProblemsLabelChangedEvent e = (ProblemsLabelChangedEvent) event;
			if (!e.isMarkerChange() && canIgnoreChangesFromAnnotionModel()) {
				return;
			}
		}

		Object[] changed = event.getElements();
		if (changed != null && !fResourceToItemsMapper.isEmpty()) {
			ArrayList others = new ArrayList();
			for (int i = 0; i < changed.length; i++) {
				Object curr = changed[i];
				if (curr instanceof IResource) {
					fResourceToItemsMapper.resourceChanged((IResource) curr);
				} else {
					others.add(curr);
				}
			}
			if (others.isEmpty()) {
				return;
			}
			event = new LabelProviderChangedEvent((IBaseLabelProvider) event
					.getSource(), others.toArray());
		}
		super.handleLabelProviderChanged(event);
	}

	/**
	 * Answers whether this viewer can ignore label provider changes resulting
	 * from marker changes in annotation models
	 */
	private boolean canIgnoreChangesFromAnnotionModel() {
		Object contentProvider = getContentProvider();
		return contentProvider instanceof IWorkingCopyProvider
				&& !((IWorkingCopyProvider) contentProvider)
						.providesWorkingCopies();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#isExpandable(java.lang.Object)
	 */
	public boolean isExpandable(Object element) {
		// workaround for 65762
		if (hasFilters() && element instanceof IMember) {
			return getFilteredChildren(element).length > 0;
		}
		return super.isExpandable(element);
	}
}
