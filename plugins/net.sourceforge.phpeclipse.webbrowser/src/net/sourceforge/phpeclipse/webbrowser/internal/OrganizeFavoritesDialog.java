package net.sourceforge.phpeclipse.webbrowser.internal;

/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. � This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 �*
 * Contributors:
 *    IBM - Initial API and implementation
 **********************************************************************/
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Dialog to manage the favorites list.
 */
public class OrganizeFavoritesDialog extends Dialog {
	protected List favorites = WebBrowserPreference
			.getInternalWebBrowserFavorites();

	public class FavoriteContentProvider implements IStructuredContentProvider {
		public FavoriteContentProvider() {
			super();
		}

		public void dispose() {
		}

		public Object[] getElements(Object inputElement) {
			return favorites.toArray();
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	public class FavoriteLabelProvider implements ITableLabelProvider {
		public FavoriteLabelProvider() {
			super();
		}

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 0)
				return ImageResource.getImage(ImageResource.IMG_FAVORITE);
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			Favorite favorite = (Favorite) element;
			if (columnIndex == 0)
				return favorite.getName();
			else
				return favorite.getURL();
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}
	}

	/**
	 * ManageFavoritesDialog constructor comment.
	 * 
	 * @param parentShell
	 * org.eclipse.swt.widgets.Shell @
	 */
	public OrganizeFavoritesDialog(Shell parentShell) {
		super(parentShell);

		setBlockOnOpen(true);
	}

	/**
	 * 
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(WebBrowserUIPlugin
				.getResource("%dialogOrganizeFavoritesTitle"));
	}

	/**
	 * 
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setFont(parent.getFont());
		// WorkbenchHelp.setHelp(composite, ContextIds.TERMINATE_SERVER_DIALOG);

		Label label = new Label(composite, SWT.NONE);
		label.setText(WebBrowserUIPlugin
				.getResource("%dialogOrganizeFavoritesMessage"));
		GridData data = new GridData();
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		final Table table = new Table(composite, SWT.BORDER
				| SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		data.widthHint = 300;
		data.heightHint = 150;
		table.setLayoutData(data);
		table.setLinesVisible(true);

		TableLayout tableLayout = new TableLayout();
		table.setLayout(tableLayout);
		table.setHeaderVisible(true);

		tableLayout.addColumnData(new ColumnWeightData(5, 50, true));
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setText(WebBrowserUIPlugin
				.getResource("%dialogOrganizeFavoritesName"));

		tableLayout.addColumnData(new ColumnWeightData(6, 60, true));
		col = new TableColumn(table, SWT.NONE);
		col.setText(WebBrowserUIPlugin
				.getResource("%dialogOrganizeFavoritesURL"));
		table.setLayout(tableLayout);

		final TableViewer tableViewer = new TableViewer(table);
		tableViewer.setContentProvider(new FavoriteContentProvider());
		tableViewer.setLabelProvider(new FavoriteLabelProvider());
		tableViewer.setInput("root");
		tableViewer.setColumnProperties(new String[] { "name", "url" });

		tableViewer.setCellEditors(new CellEditor[] {
				new TextCellEditor(table), new TextCellEditor(table) });

		ICellModifier cellModifier = new ICellModifier() {
			public Object getValue(Object element, String property) {
				Favorite f = (Favorite) element;
				if ("name".equals(property))
					return f.getName();
				else
					return f.getURL();
			}

			public boolean canModify(Object element, String property) {
				return true;
			}

			public void modify(Object element, String property, Object value) {
				if (element instanceof Item)
					element = ((Item) element).getData();

				try {
					Favorite f = (Favorite) element;
					String s = (String) value;
					if ("name".equals(property))
						f.setName(s);
					else
						f.setURL(s);
					tableViewer.refresh(f);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};
		tableViewer.setCellModifier(cellModifier);

		final Button remove = SWTUtil.createButton(composite,
				WebBrowserUIPlugin.getResource("%remove"));
		remove.setEnabled(false);

		tableViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						remove.setEnabled(!event.getSelection().isEmpty());
					}
				});

		remove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int index = table.getSelectionIndex();
				if (index < 0 || index >= favorites.size())
					return;

				tableViewer.remove(favorites.get(index));
				favorites.remove(index);
			}
		});

		Dialog.applyDialogFont(composite);

		return composite;
	}

	protected void okPressed() {
		WebBrowserPreference.setInternalWebBrowserFavorites(favorites);
		super.okPressed();
	}
}