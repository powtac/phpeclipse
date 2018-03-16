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
package net.sourceforge.phpeclipse.webbrowser.internal;

import java.util.Iterator;

import net.sourceforge.phpeclipse.webbrowser.IExternalWebBrowser;
import net.sourceforge.phpeclipse.webbrowser.IExternalWebBrowserWorkingCopy;
import net.sourceforge.phpeclipse.webbrowser.IInternalWebBrowser;
import net.sourceforge.phpeclipse.webbrowser.IInternalWebBrowserWorkingCopy;
import net.sourceforge.phpeclipse.webbrowser.IWebBrowser;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
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
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

/**
 * 
 */
public class BrowserTableComposite extends Composite {
	protected Table table;

	protected CheckboxTableViewer tableViewer;

	protected Button edit;

	protected Button remove;

	protected Button search;

	protected IWebBrowser selection;

	protected Label location;

	protected Label parameters;

	public BrowserTableComposite(Composite parent, int style) {
		super(parent, style);
		createWidgets();
	}

	protected void createWidgets() {
		GridLayout layout = new GridLayout();
		layout.horizontalSpacing = 5;
		layout.verticalSpacing = 5;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 2;
		setLayout(layout);

		GridData data = new GridData(GridData.FILL_BOTH);
		setLayoutData(data);

		Label label = new Label(this, SWT.NONE);
		label.setText(WebBrowserUIPlugin.getResource("%browserList"));
		data = new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_CENTER);
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		table = new Table(this, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL
				| SWT.H_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
		data = new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_FILL);
		data.widthHint = 300;
		table.setLayoutData(data);
		table.setHeaderVisible(false);
		table.setLinesVisible(false);

		TableLayout tableLayout = new TableLayout();
		new TableColumn(table, SWT.NONE);

		tableLayout.addColumnData(new ColumnWeightData(100));

		table.setLayout(tableLayout);

		tableViewer = new CheckboxTableViewer(table);

		tableViewer.setContentProvider(new BrowserContentProvider());
		tableViewer.setLabelProvider(new BrowserTableLabelProvider());

		tableViewer.setInput("root");

		// uncheck any other elements that might be checked and leave only the
		// element checked to
		// remain checked since one can only chose one brower at a time to be
		// current.
		tableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent e) {
				checkNewDefaultBrowser(e.getElement());
				IWebBrowser browser = (IWebBrowser) e.getElement();
				BrowserManager.getInstance().setCurrentWebBrowser(browser);

				// if no other browsers are checked, don't allow the single one
				// currently
				// checked to become unchecked, and lose a current browser. That
				// is, don't
				// permit unchecking if no other item is checked which is
				// supposed to be the case.
				Object[] obj = tableViewer.getCheckedElements();
				if (obj.length == 0)
					tableViewer.setChecked(e.getElement(), true);
			}
		});

		// set a default, checked browser based on the current browser. If there
		// is not a
		// current browser, but the first item exists, use that instead.
		// This will work currently until workbench shutdown, because current
		// browser is not yet persisted.
		IWebBrowser browser = BrowserManager.getInstance()
				.getCurrentWebBrowser();
		if (browser != null)
			tableViewer.setChecked(browser, true);
		else {
			Object obj = tableViewer.getElementAt(0);
			if (obj != null)
				tableViewer.setChecked(obj, true);
		}

		tableViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						Object obj = getSelection(event.getSelection());

						if (obj instanceof IInternalWebBrowser) {
							selection = (IInternalWebBrowser) obj;
							remove.setEnabled(false);
							edit.setEnabled(true);
						} else if (obj instanceof IExternalWebBrowser) {
							selection = (IExternalWebBrowser) obj;
							remove.setEnabled(true);
							edit.setEnabled(true);
						} else
							selection = null;

						if (selection == null) {
							edit.setEnabled(false);
							remove.setEnabled(false);
						}
					}
				});

		Composite buttonComp = new Composite(this, SWT.NONE);
		layout = new GridLayout();
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 5;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 1;
		buttonComp.setLayout(layout);
		data = new GridData(GridData.HORIZONTAL_ALIGN_END
				| GridData.VERTICAL_ALIGN_FILL);
		buttonComp.setLayoutData(data);

		Button add = SWTUtil.createButton(buttonComp, WebBrowserUIPlugin
				.getResource("%add"));
		add.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ExternalBrowserDialog dialog = new ExternalBrowserDialog(
						getShell());
				if (dialog.open() == Window.CANCEL)
					return;
				tableViewer.refresh();
			}
		});

		edit = SWTUtil.createButton(buttonComp, WebBrowserUIPlugin
				.getResource("%edit"));
		edit.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IWebBrowser browser2 = getSelectedWebBrowser();

				if (browser2 instanceof IInternalWebBrowser) {
					IInternalWebBrowserWorkingCopy wc = ((IInternalWebBrowser) browser2)
							.getWorkingCopy();
					InternalBrowserDialog dialog = new InternalBrowserDialog(
							getShell(), wc);
					if (dialog.open() != Window.CANCEL) {
						try {
							tableViewer.refresh(wc.save());
						} catch (Exception ex) {
						}
					}
				} else if (browser2 instanceof IExternalWebBrowser) {
					IExternalWebBrowserWorkingCopy wc = ((IExternalWebBrowser) browser2)
							.getWorkingCopy();
					ExternalBrowserDialog dialog = new ExternalBrowserDialog(
							getShell(), wc);
					if (dialog.open() != Window.CANCEL) {
						try {
							tableViewer.refresh(wc.save());
						} catch (Exception ex) {
						}
					}
				}
			}
		});
		edit.setEnabled(false);

		remove = SWTUtil.createButton(buttonComp, WebBrowserUIPlugin
				.getResource("%remove"));
		remove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IWebBrowser browser2 = getSelectedWebBrowser();
				try {
					if (browser2 instanceof IInternalWebBrowser) {
						remove.setEnabled(false);
						return; // nothing else possible to do
					} else if (browser2 instanceof IExternalWebBrowser) {
						remove.setEnabled(true);
						((IExternalWebBrowser) browser2).delete();

						tableViewer.remove(browser2);

						// need here to ensure that if the item deleted was
						// checked, ie, was
						// the current browser, that the new current browser
						// will be the first in the
						// list, typically, the internal browser, which cannot
						// be deleted, and be current.
						if (((IExternalWebBrowser) browser2) == BrowserManager
								.getInstance().getCurrentWebBrowser()) {
							Object obj = tableViewer.getElementAt(0);
							if (obj != null) {
								BrowserManager.getInstance()
										.setCurrentWebBrowser(
												(InternalWebBrowser) obj);
								tableViewer.setChecked(obj, true);
							}
						}
					}
				} catch (Exception ex) {
				}
			}
		});
		remove.setEnabled(false);

		search = SWTUtil.createButton(buttonComp, WebBrowserUIPlugin
				.getResource("%search"));
		search.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				java.util.List browsersToCreate = BrowserSearcher
						.search(getShell());

				if (browsersToCreate == null) // cancelled
					return;

				if (browsersToCreate.isEmpty()) { // no browsers found
					WebBrowserUtil.openMessage(WebBrowserUIPlugin
							.getResource("%searchingNoneFound"));
					return;
				}

				Iterator iterator = browsersToCreate.iterator();
				while (iterator.hasNext()) {
					IExternalWebBrowserWorkingCopy browser2 = (IExternalWebBrowserWorkingCopy) iterator
							.next();
					browser2.save();
				}
				tableViewer.refresh();
			}
		});
		PlatformUI.getWorkbench().getHelpSystem().setHelp(search,
				ContextIds.PREF_BROWSER_EXTERNAL_SEARCH);

		tableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent e) {
				checkNewDefaultBrowser(e.getElement());
				IWebBrowser browser2 = (IWebBrowser) e.getElement();
				BrowserManager.getInstance().setCurrentWebBrowser(browser2);
			}
		});
		search.setEnabled(true);
	}

	public IWebBrowser getSelectedWebBrowser() {
		return selection;
	}

	protected Object getSelection(ISelection sel2) {
		IStructuredSelection sel = (IStructuredSelection) sel2;
		return sel.getFirstElement();
	}

	// Uncheck all the items except the current one that was just checked
	protected void checkNewDefaultBrowser(Object browser) {
		TableItem[] children = tableViewer.getTable().getItems();
		for (int i = 0; i < children.length; i++) {
			TableItem item = children[i];

			if (!(item.getData().equals(browser)))
				item.setChecked(false);
		}
	}
}