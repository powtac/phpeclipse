/*
 * $Id: UnknownElementWizardPage.java,v 1.2 2006-10-21 23:18:43 pombredanne Exp $
 * Copyright Narushima Hironori. All rights reserved.
 */
package net.sourceforge.phpeclipse.wizards.html;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * 
 * 
 */
public class UnknownElementWizardPage extends EditElementWizardPage {

	// key of TableCell for attribute editor.
	final static String NAME = "ColumnProperty-name",
			VALUE = "ColumnProperty-value";

	Button emptyElementCheck, addButton, removeButton, upButton, downButton;

	TableViewer unknownElementAttrs;

	ArrayList attrs = new ArrayList(), listeners = new ArrayList();

	SelectionListener elemTypeChangeListener = new SelectionListener() {
		public void widgetSelected(SelectionEvent e) {
			refreshPreview();
		}

		public void widgetDefaultSelected(SelectionEvent e) {
		}
	};

	public UnknownElementWizardPage() {
		super("UnknownElementEditPage");
		setTitle("Unknown");
		setDescription("Editor for any HTML element.");
	}

	static IInputValidator attrValidator = new IInputValidator() {
		public String isValid(String newText) {
			if (newText.length() == 0) {
				return "Need to specify name";
			}
			if (newText.indexOf(' ') != -1 || newText.indexOf('\n') != -1
					|| newText.indexOf('\t') != -1) {
				return "Not contain blank";
			}
			return null;
		}
	};

	protected void createChildControl(Composite parent) {
		// empty eleemnt
		parent.setLayout(new GridLayout(2, false));

		// // attribute editor
		Label labe = new Label(parent, SWT.NONE);
		labe.setText("Element &Attribute:");
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		labe.setLayoutData(gd);
		new Label(parent, SWT.NONE);

		// attribute display table setting
		unknownElementAttrs = new TableViewer(parent, SWT.BORDER | SWT.SINGLE
				| SWT.FULL_SELECTION);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 1;
		gd.verticalSpan = 4;
		unknownElementAttrs.getControl().setLayoutData(gd);

		final Table table = unknownElementAttrs.getTable();
		new TableColumn(table, SWT.LEFT).setText("Name");
		new TableColumn(table, SWT.LEFT).setText("Value");

		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		// modifier setting
		unknownElementAttrs.setColumnProperties(new String[] { NAME, VALUE });
		unknownElementAttrs.setContentProvider(new ArrayContentProvider());

		unknownElementAttrs.setCellEditors(new CellEditor[] {
				new TextCellEditor(table), new TextCellEditor(table) });
		unknownElementAttrs.setCellModifier(new ICellModifier() {
			public boolean canModify(Object element, String property) {
				return true;
			}

			public Object getValue(Object element, String property) {
				return ((String[]) element)[property.equals(NAME) ? 0 : 1];
			}

			public void modify(Object element, String property, Object value) {
				if (element instanceof Item) {
					((String[]) ((Item) element).getData())[property
							.equals(NAME) ? 0 : 1] = HTMLUtilities
							.unescape((String) value);
					refreshPreview();
				}
			}
		});

		unknownElementAttrs.setLabelProvider(new ITableLabelProvider() {
			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}

			public String getColumnText(Object element, int columnIndex) {
				return ((String[]) element)[columnIndex];
			}

			public void addListener(ILabelProviderListener listener) {
			}

			public void removeListener(ILabelProviderListener listener) {
			}

			public void dispose() {
			}

			public boolean isLabelProperty(Object element, String property) {
				return property.equals(NAME) || property.equals(VALUE);
			}
		});

		resetAttributes();
		unknownElementAttrs.setInput(attrs);

		TableColumn[] columns = table.getColumns();
		for (int i = 0; i < columns.length; i++) {
			columns[i].pack();
		}

		// buttonss
		upButton = createButton(parent, "&Up");
		upButton.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				int index = getSelectionIndex();
				if (index > 0) {
					attrs.add(index - 1, attrs.remove(index));
					refreshPreview();
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		downButton = createButton(parent, "&Down");
		downButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				int index = getSelectionIndex();
				if (index < attrs.size() - 1) {
					attrs.add(index + 1, attrs.remove(index));
					refreshPreview();
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		addButton = createButton(parent, "&Add");
		addButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				int insertIndex = getSelectionIndex();
				String[] newData = inputValue();
				if (newData != null) {
					attrs.add(newData);
					refreshPreview();
				}
			}

			String[] inputValue() {
				SomeItemInputDialog dialog = new SomeItemInputDialog(
						getShell(), "Input new attribute", new String[] {
								"Attribute name", "Attribute value" },
						new IInputValidator[] { attrValidator, null });

				if (dialog.open() == Window.OK) {
					return dialog.getValues();
				}
				return null;
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		removeButton = createButton(parent, "&Remove");
		removeButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				int index = getSelectionIndex();
				if (index != -1) {
					attrs.remove(index);
					refreshPreview();
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		emptyElementCheck = new Button(parent, SWT.CHECK);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		emptyElementCheck.setLayoutData(gd);
		emptyElementCheck.setText("&Empty Element");
		emptyElementCheck.addSelectionListener(elemTypeChangeListener);
		emptyElementCheck.setSelection(isEmptyAsText());

		new Label(parent, SWT.NONE);
	}

	static Button createButton(Composite parent, String text) {
		Button button = new Button(parent, SWT.PUSH);
		GridData gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING
				| GridData.HORIZONTAL_ALIGN_END);
		gd.widthHint = 60;
		button.setLayoutData(gd);
		button.setText(text);
		return button;
	}

	public String getPreviewText() {
		String elemName = getElementName();
		if (elemName == null) {
			return null;
		}

		// sets values

		boolean empty = false;
		if (emptyElementCheck == null) {
			// ui uninitialized
			empty = isEmptyAsText();
		} else {
			// ui initialized
			empty = emptyElementCheck.getSelection();
		}

		String content = getSelectionText();
		if (!empty && getEditType() == MODIFY) {
			content = chooseContent(content);
		}

		String previewText = "<" + elemName + attrsCode();
		if (empty) {
			previewText += " />";
		} else {
			previewText += ">" + content + "</" + elemName + ">";
		}
		return previewText;
	}

	boolean isEmptyAsText() {
		String selText = getSelectionText();
		if (getEditType() == MODIFY) {
			int len = selText.length();
			return selText.substring(len - 2, len).equals("/>");
		}
		return false;
	}

	void resetAttributes() {
		attrs.clear();

		Element elem = getParsedSelectionText();
		if (elem != null) {
			NamedNodeMap as = elem.getAttributes();
			for (int i = 0; i < as.getLength(); i++) {
				Node n = as.item(i);
				attrs.add(new String[] { n.getNodeName(), n.getNodeValue() });
			}
		}
	}

	String attrsCode() {
		StringBuffer buff = new StringBuffer();
		Object[] as = attrs.toArray();
		for (int i = 0; i < as.length; i++) {
			String[] a = (String[]) as[i];
			buff.append(" " + a[0] + "=\"" + HTMLUtilities.escape(a[1]) + "\"");
		}
		return buff.toString();
	}

	int getSelectionIndex() {
		Object sel = unknownElementAttrs.getSelection();
		if (sel instanceof IStructuredSelection) {
			Object item = ((IStructuredSelection) sel).getFirstElement();
			return attrs.indexOf(item);
		} else {
			return -1;
		}
	}

	public void refreshPreview() {
		unknownElementAttrs.refresh();
		super.refreshPreview();
	}

	public void setElementName(String elemName) {
		super.setElementName(elemName);
		setTitle("\"" + elemName + "\" Element");
	}

}