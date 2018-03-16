/*
 * $Id: TableElementWizardPage.java,v 1.2 2006-10-21 23:18:43 pombredanne Exp $
 * Copyright Narushima Hironori. All rights reserved.
 */
package net.sourceforge.phpeclipse.wizards.html;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * TableElementWizardPage.
 */
public class TableElementWizardPage extends EditElementWizardPage {

	final public static int COLUMNS_MAX = 32, ROWS_MAX = 256;

	final static String[] expandStyleLabels = { "Flat", "Table", "Enumerate", };

	TableElementModel model;

	TableViewer viewer;

	CellEditor[] editors;

	Combo expandStyleCombo = null;

	Text colsText, rowsText;

	Button addButton, removeButton, upButton, downButton;

	SelectionListener buttonListener = new SelectionListener() {
		public void widgetSelected(SelectionEvent ev) {
			Element e = getCurrentSelection();
			if (ev.widget == addButton) {
				model.insertNewRowBefore(e);
			} else if (ev.widget == removeButton) {
				model.removeRow(e);
			} else if (ev.widget == upButton) {
				model.move(e, -1);
			} else if (ev.widget == downButton) {
				model.move(e, 1);
			}
			refreshAll();
		}

		public void widgetDefaultSelected(SelectionEvent e) {
		}
	};

	ModifyListener cellCountChangeListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			try {
				if (e.widget == colsText) {
					int cols = Integer.parseInt(colsText.getText());
					if (cols < 1)
						cols = 1;
					if (cols > COLUMNS_MAX)
						cols = COLUMNS_MAX;
					model.setColumnCount(cols);
				} else if (e.widget == rowsText) {
					int rows = Integer.parseInt(rowsText.getText());
					if (rows < 1)
						rows = 1;
					if (rows > ROWS_MAX)
						rows = ROWS_MAX;
					model.setRowCount(rows);
				}
				refreshAll();
			} catch (NumberFormatException x) {
			}
		}
	};

	public TableElementWizardPage() {
		super("TableElementWizardPage");
		setTitle("Table");
		setDescription("Edit table element and cells modifier.");
	}

	public String getPreviewText() {
		if (model == null) {
			initModel();
		}
		return (model != null) ? model.expandCodes() : null;
	}

	void initModel() {
		String content = ((EditElementWizard) getWizard()).getSelection()
				.getText().trim();
		try {
			model = new TableElementModel(content, getEditType() == NEW);
		} catch (ParserConfigurationException e) {
			PHPeclipsePlugin.log(e);
		} catch (SAXException e) {
			PHPeclipsePlugin.log(e);
		} catch (IOException e) {
			PHPeclipsePlugin.log(e);
		}
	}

	protected void createChildControl(Composite parent) {
		parent.setLayout(new GridLayout(2, false));

		// table settings
		viewer = new TableViewer(parent, SWT.SINGLE | SWT.FULL_SELECTION
				| SWT.BORDER);
		refreshTableHeaderColumns();

		viewer.setContentProvider(new TableElementContentProvider());
		viewer.setLabelProvider(new TableElementLabelProvider());
		viewer.setCellModifier(new TableElementCellModifier(
				new IPropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent event) {
						refreshAll();
					}
				}));

		viewer.setInput(model);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				refreshButtonState();
				refreshPreview();
			}
		});

		Table table = viewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.verticalSpan = 2;
		table.setLayoutData(gd);

		// text input area setting
		Composite textInputArea = new Composite(parent, SWT.NONE);
		textInputArea.setLayout(new GridLayout(1, false));
		textInputArea.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_BEGINNING));
		rowsText = createNumInputText(textInputArea, "&Rows:");
		colsText = createNumInputText(textInputArea, "&Columns:");

		// button area.
		Composite buttonArea = new Composite(parent, SWT.NONE);
		buttonArea.setLayout(new GridLayout(1, false));
		buttonArea.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_END));
		addButton = createButton(buttonArea, "&Add");
		removeButton = createButton(buttonArea, "&Remove");
		upButton = createButton(buttonArea, "&Up");
		downButton = createButton(buttonArea, "&Down");

		// init state
		TableColumn[] cols = table.getColumns();
		for (int i = 0; i < cols.length; i++) {
			cols[i].pack();
		}
		refreshTableLengthText();
		refreshButtonState();
	}

	Button createButton(Composite parent, String text) {
		Button button = new Button(parent, SWT.NONE);
		button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		button.setText(text);
		button.addSelectionListener(buttonListener);
		return button;
	}

	Text createNumInputText(Composite parent, String label) {
		Label labe = new Label(parent, SWT.NONE);
		labe.setText(label);

		Text text = new Text(parent, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		text.setTextLimit(2);
		text.addVerifyListener(new NumVerifyListener());
		text.addModifyListener(cellCountChangeListener);
		return text;
	}

	void refreshAll() {
		refreshTableHeaderColumns();
		refreshTableLengthText();
		refreshButtonState();
		refreshPreview();
		viewer.refresh();
	}

	void refreshTableHeaderColumns() {
		if (model == null) {
			initModel();
		}

		Table table = viewer.getTable();
		TableColumn[] cols = table.getColumns();
		CellEditor[] editors = viewer.getCellEditors();

		String[] props = model.getColumnProperties();
		viewer.setColumnProperties(props);
		// modify cell length
		if (props.length > cols.length) {
			CellEditor[] newEditors = new CellEditor[props.length];
			if (editors != null) {
				System.arraycopy(editors, 0, newEditors, 0, editors.length);
			}
			for (int i = cols.length; i < props.length; i++) {
				TableColumn col = new TableColumn(table, SWT.LEFT);
				col.setText(TableElementModel.toColumnName(i));
				newEditors[i] = new TextCellEditor(table);
			}
			viewer.setCellEditors(newEditors);
		} else if (props.length < cols.length) {
			for (int i = props.length; i < cols.length; i++) {
				cols[i].dispose();
				editors[i].dispose();
			}
			CellEditor[] newEditors = new CellEditor[props.length];
			System.arraycopy(editors, 0, newEditors, 0, props.length);
			viewer.setCellEditors(newEditors);
		}

		// adjust table fields.
		viewer.refresh();
		cols = table.getColumns();
		for (int i = 0; i < cols.length; i++) {
			cols[i].pack();
		}
	}

	void refreshTableLengthText() {
		String cols = String.valueOf(model.getColumnCount());
		if (!cols.equals(colsText.getText())) {
			colsText.setText(cols);
		}
		String rows = String.valueOf(model.getRowCount());
		if (!rows.equals(rowsText.getText())) {
			rowsText.setText(rows);
		}
	}

	void refreshButtonState() {
		Element e = getCurrentSelection();
		boolean enable = (e != null);

		removeButton.setEnabled(enable);
		int currentIndex = -1;
		Element[] rows = model.getRows();
		for (int i = 0; i < rows.length; i++) {
			if (rows[i].equals(e)) {
				currentIndex = i;
			}
		}
		upButton.setEnabled(enable && currentIndex > 0);
		downButton.setEnabled(enable && currentIndex < rows.length - 1);
	}

	Element getCurrentSelection() {
		IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
		return (sel != null) ? (Element) sel.getFirstElement() : null;
	}

}