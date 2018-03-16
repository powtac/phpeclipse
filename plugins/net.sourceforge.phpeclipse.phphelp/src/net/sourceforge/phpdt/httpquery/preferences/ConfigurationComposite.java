/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 �*
 * Contributors:
 *    IBM - Initial API and implementation
 **********************************************************************/
package net.sourceforge.phpdt.httpquery.preferences;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.phpdt.httpquery.config.IConfiguration;
import net.sourceforge.phpdt.httpquery.config.IConfigurationWorkingCopy;
import net.sourceforge.phpdt.phphelp.PHPHelpPlugin;

import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * 
 */
public class ConfigurationComposite extends Composite {
	protected Table table;

	protected TableViewer tableViewer;

	protected Button edit;

	protected Button remove;

	// protected Button start;
	// protected Button stop;

	protected List selection2;

	public ConfigurationComposite(Composite parent, int style) {
		super(parent, style);

		createWidgets();
	}

	protected void createWidgets() {
		GridLayout layout = new GridLayout();
		layout.horizontalSpacing = 6;
		layout.verticalSpacing = 6;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 2;
		setLayout(layout);

		GridData data = new GridData(GridData.FILL_BOTH);
		setLayoutData(data);

		Label label = new Label(this, SWT.WRAP);
		label.setText(PHPHelpPlugin.getResource("%configurationsList"));
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_CENTER));

		label = new Label(this, SWT.NONE);

		table = new Table(this, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL
				| SWT.MULTI | SWT.FULL_SELECTION);
		data = new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_FILL);
		data.widthHint = 300;
		data.heightHint = 300;
		// WorkbenchHelp.setHelp(table, ContextIds.PREF_MONITORS);

		table.setLayoutData(data);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableLayout tableLayout = new TableLayout();

		TableColumn statusColumn = new TableColumn(table, SWT.NONE);
		statusColumn.setText(PHPHelpPlugin.getResource("%columnName"));
		ColumnWeightData colData = new ColumnWeightData(5, 30, true);
		tableLayout.addColumnData(colData);

		TableColumn typeColumn = new TableColumn(table, SWT.NONE);
		typeColumn.setText(PHPHelpPlugin.getResource("%columnType"));
		colData = new ColumnWeightData(5, 30, true);
		tableLayout.addColumnData(colData);

		// TableColumn urlColumn = new TableColumn(table, SWT.NONE);
		// urlColumn.setText(PHPHelpPlugin.getResource("%columnUser"));
		// colData = new ColumnWeightData(5, 30, true);
		// tableLayout.addColumnData(colData);

		TableColumn localColumn = new TableColumn(table, SWT.NONE);
		localColumn.setText(PHPHelpPlugin.getResource("%columnURL"));
		colData = new ColumnWeightData(5, 150, true);
		tableLayout.addColumnData(colData);

		table.setLayout(tableLayout);

		tableViewer = new TableViewer(table);
		tableViewer.setContentProvider(new ConfigurationContentProvider());
		tableViewer.setLabelProvider(new ConfigurationTableLabelProvider());
		tableViewer.setInput("root");
		tableViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						setSelection(event.getSelection());
					}
				});

		Composite buttonComp = new Composite(this, SWT.NONE);
		layout = new GridLayout();
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 8;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 1;
		buttonComp.setLayout(layout);
		data = new GridData(GridData.HORIZONTAL_ALIGN_END
				| GridData.VERTICAL_ALIGN_FILL);
		buttonComp.setLayoutData(data);

		Button add = SWTUtil.createButton(buttonComp, PHPHelpPlugin
				.getResource("%add"));
		add.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ConfigurationDialog dialog = new ConfigurationDialog(getShell());
				if (dialog.open() == Window.CANCEL)
					return;
				tableViewer.refresh();

				List list = PHPHelpPlugin.getConfigurations();
				Object configuration = list.get(list.size() - 1);
				tableViewer
						.setSelection(new StructuredSelection(configuration));
			}
		});

		edit = SWTUtil.createButton(buttonComp, PHPHelpPlugin
				.getResource("%edit"));
		edit.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IConfiguration monitor = (IConfiguration) getSelection().get(0);
				IConfigurationWorkingCopy wc = monitor.getWorkingCopy();

				ConfigurationDialog dialog = new ConfigurationDialog(
						getShell(), wc);
				if (dialog.open() != Window.CANCEL) {
					try {
						tableViewer.refresh(wc.save());
					} catch (Exception ex) {
					}
				}
			}
		});
		edit.setEnabled(false);

		remove = SWTUtil.createButton(buttonComp, PHPHelpPlugin
				.getResource("%remove"));
		remove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Iterator iterator = getSelection().iterator();
				while (iterator.hasNext()) {
					IConfiguration monitor = (IConfiguration) iterator.next();
					try {
						monitor.delete();
					} catch (Exception ex) {
					}
					tableViewer.remove(monitor);

					List list = PHPHelpPlugin.getConfigurations();
					Object monitor2 = list.get(list.size() - 1);
					tableViewer.setSelection(new StructuredSelection(monitor2));
				}
			}
		});
		remove.setEnabled(false);

	}

	protected List getSelection() {
		return selection2;
	}

	protected void setSelection(ISelection sel2) {
		IStructuredSelection sel = (IStructuredSelection) sel2;
		Iterator iterator = sel.iterator();
		selection2 = new ArrayList();

		while (iterator.hasNext()) {
			Object obj = iterator.next();
			if (obj instanceof IConfiguration)
				selection2.add(obj);
		}

		if (!selection2.isEmpty()) {
			remove.setEnabled(true);

			edit.setEnabled(true);
		} else {
			edit.setEnabled(false);
			remove.setEnabled(false);
		}
	}
}