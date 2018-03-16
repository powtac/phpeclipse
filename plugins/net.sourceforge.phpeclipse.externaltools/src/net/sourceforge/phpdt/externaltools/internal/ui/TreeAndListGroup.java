package net.sourceforge.phpdt.externaltools.internal.ui;

/**********************************************************************
 Copyright (c) 2002 IBM Corp. and others. All rights reserved.
 This file is made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html
 **********************************************************************/

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;

/**
 * This class was derived from
 * org.eclipse.ui.internal.misc.CheckboxTreeAndListGroup
 * 
 */
public class TreeAndListGroup implements ISelectionChangedListener {
	private Object root;

	private Object currentTreeSelection;

	private Collection listeners = new HashSet();

	private ITreeContentProvider treeContentProvider;

	private IStructuredContentProvider listContentProvider;

	private ILabelProvider treeLabelProvider;

	private ILabelProvider listLabelProvider;

	// widgets
	private TreeViewer treeViewer;

	private TableViewer listViewer;

	private boolean allowMultiselection = false;

	/**
	 * Create an instance of this class. Use this constructor if you wish to
	 * specify the width and/or height of the combined widget (to only hardcode
	 * one of the sizing dimensions, specify the other dimension's value as -1)
	 * 
	 * @param parent
	 *            org.eclipse.swt.widgets.Composite
	 * @param style
	 *            int
	 * @param rootObject
	 *            java.lang.Object
	 * @param childPropertyName
	 *            java.lang.String
	 * @param parentPropertyName
	 *            java.lang.String
	 * @param listPropertyName
	 *            java.lang.String
	 * @param width
	 *            int
	 * @param height
	 *            int
	 */
	public TreeAndListGroup(Composite parent, Object rootObject,
			ITreeContentProvider treeContentProvider,
			ILabelProvider treeLabelProvider,
			IStructuredContentProvider listContentProvider,
			ILabelProvider listLabelProvider, int style, int width, int height) {

		root = rootObject;
		this.treeContentProvider = treeContentProvider;
		this.listContentProvider = listContentProvider;
		this.treeLabelProvider = treeLabelProvider;
		this.listLabelProvider = listLabelProvider;
		createContents(parent, width, height, style);
	}

	/**
	 * This method must be called just before this window becomes visible.
	 */
	public void aboutToOpen() {
		currentTreeSelection = null;

		// select the first element in the list
		Object[] elements = treeContentProvider.getElements(root);
		Object primary = elements.length > 0 ? elements[0] : null;
		if (primary != null) {
			treeViewer.setSelection(new StructuredSelection(primary));
		}
		treeViewer.getControl().setFocus();
	}

	/**
	 * Add the passed listener to collection of clients that listen for changes
	 * to list viewer selection state
	 * 
	 * @param listener
	 *            ISelectionChangedListener
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	/**
	 * Notify all checked state listeners that the passed element has had its
	 * checked state changed to the passed state
	 */
	protected void notifySelectionListeners(SelectionChangedEvent event) {
		Iterator listenersEnum = listeners.iterator();
		while (listenersEnum.hasNext()) {
			((ISelectionChangedListener) listenersEnum.next())
					.selectionChanged(event);
		}
	}

	/**
	 * Lay out and initialize self's visual components.
	 * 
	 * @param parent
	 *            org.eclipse.swt.widgets.Composite
	 * @param width
	 *            int
	 * @param height
	 *            int
	 */
	protected void createContents(Composite parent, int width, int height,
			int style) {
		// group pane
		Composite composite = new Composite(parent, style);
		composite.setFont(parent.getFont());
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		createTreeViewer(composite, width / 2, height);
		createListViewer(composite, width / 2, height);

		initialize();
	}

	/**
	 * Create this group's list viewer.
	 */
	protected void createListViewer(Composite parent, int width, int height) {
		int style;
		if (allowMultiselection) {
			style = SWT.MULTI;
		} else {
			style = SWT.SINGLE;
		}
		listViewer = new TableViewer(parent, SWT.BORDER | style);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = width;
		data.heightHint = height;
		listViewer.getTable().setLayoutData(data);
		listViewer.getTable().setFont(parent.getFont());
		listViewer.setContentProvider(listContentProvider);
		listViewer.setLabelProvider(listLabelProvider);
		listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				notifySelectionListeners(event);
			}
		});
	}

	/**
	 * Create this group's tree viewer.
	 */
	protected void createTreeViewer(Composite parent, int width, int height) {
		Tree tree = new Tree(parent, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = width;
		data.heightHint = height;
		tree.setLayoutData(data);
		tree.setFont(parent.getFont());

		treeViewer = new TreeViewer(tree);
		treeViewer.setContentProvider(treeContentProvider);
		treeViewer.setLabelProvider(treeLabelProvider);
		treeViewer.addSelectionChangedListener(this);
	}

	public Table getListTable() {
		return listViewer.getTable();
	}

	public IStructuredSelection getListTableSelection() {
		ISelection selection = this.listViewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			return (IStructuredSelection) selection;
		} else {
			return StructuredSelection.EMPTY;
		}
	}

	protected void initialListItem(Object element) {
		Object parent = treeContentProvider.getParent(element);
		selectAndRevealFolder(parent);
	}

	public void selectAndRevealFolder(Object treeElement) {
		treeViewer.reveal(treeElement);
		IStructuredSelection selection = new StructuredSelection(treeElement);
		treeViewer.setSelection(selection);
	}

	public void selectAndRevealFile(Object treeElement) {
		listViewer.reveal(treeElement);
		IStructuredSelection selection = new StructuredSelection(treeElement);
		listViewer.setSelection(selection);
	}

	/**
	 * Initialize this group's viewers after they have been laid out.
	 */
	protected void initialize() {
		treeViewer.setInput(root);
	}

	/**
	 * Handle the selection of an item in the tree viewer
	 * 
	 * @param selection
	 *            ISelection
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		IStructuredSelection selection = (IStructuredSelection) event
				.getSelection();
		Object selectedElement = selection.getFirstElement();
		if (selectedElement == null) {
			currentTreeSelection = null;
			listViewer.setInput(currentTreeSelection);
			return;
		}

		// ie.- if not an item deselection
		if (selectedElement != currentTreeSelection) {
			listViewer.setInput(selectedElement);
		}

		currentTreeSelection = selectedElement;
	}

	/**
	 * Set the list viewer's providers to those passed
	 * 
	 * @param contentProvider
	 *            ITreeContentProvider
	 * @param labelProvider
	 *            ILabelProvider
	 */
	public void setListProviders(IStructuredContentProvider contentProvider,
			ILabelProvider labelProvider) {
		listViewer.setContentProvider(contentProvider);
		listViewer.setLabelProvider(labelProvider);
	}

	/**
	 * Set the sorter that is to be applied to self's list viewer
	 */
	public void setListSorter(ViewerSorter sorter) {
		listViewer.setSorter(sorter);
	}

	/**
	 * Set the root of the widget to be new Root. Regenerate all of the tables
	 * and lists from this value.
	 * 
	 * @param newRoot
	 */
	public void setRoot(Object newRoot) {
		this.root = newRoot;
		initialize();
	}

	/**
	 * Set the tree viewer's providers to those passed
	 * 
	 * @param contentProvider
	 *            ITreeContentProvider
	 * @param labelProvider
	 *            ILabelProvider
	 */
	public void setTreeProviders(ITreeContentProvider contentProvider,
			ILabelProvider labelProvider) {
		treeViewer.setContentProvider(contentProvider);
		treeViewer.setLabelProvider(labelProvider);
	}

	/**
	 * Set the sorter that is to be applied to self's tree viewer
	 */
	public void setTreeSorter(ViewerSorter sorter) {
		treeViewer.setSorter(sorter);
	}

	/**
	 * Set the focus on to the list widget.
	 */
	public void setFocus() {

		this.treeViewer.getTree().setFocus();
	}

	public void setAllowMultiselection(boolean allowMultiselection) {
		this.allowMultiselection = allowMultiselection;

	}
}
