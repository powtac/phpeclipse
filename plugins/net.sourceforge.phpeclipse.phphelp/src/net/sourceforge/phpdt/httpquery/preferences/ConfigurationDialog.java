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

import net.sourceforge.phpdt.httpquery.config.IConfigurationWorkingCopy;
import net.sourceforge.phpdt.phphelp.PHPHelpPlugin;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 
 */
public class ConfigurationDialog extends Dialog {
	protected IConfigurationWorkingCopy fConfiguration;

	protected boolean isEdit;

	private Button okButton;

	private Text fName;

	private Text fUrl;

	// private Text fPassword;

	interface StringModifyListener {
		public void valueChanged(String s);
	}

	interface BooleanModifyListener {
		public void valueChanged(boolean b);
	}

	interface TypeModifyListener {
		public void valueChanged(String fType);
	}

	/**
	 * @param parentShell
	 */
	public ConfigurationDialog(Shell parentShell,
			IConfigurationWorkingCopy configuration) {
		super(parentShell);
		this.fConfiguration = configuration;
		isEdit = true;
	}

	public ConfigurationDialog(Shell parentShell) {
		super(parentShell);
		fConfiguration = PHPHelpPlugin.createConfiguration();
		isEdit = false;
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (isEdit)
			shell.setText(PHPHelpPlugin.getResource("%editConfig"));
		else
			shell.setText(PHPHelpPlugin.getResource("%newConfig"));
	}

	protected Label createLabel(Composite comp, String txt) {
		Label label = new Label(comp, SWT.NONE);
		label.setText(txt);
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING
				| GridData.VERTICAL_ALIGN_BEGINNING));
		return label;
	}

	protected Text createPassword(Composite comp, String txt,
			final StringModifyListener listener) {
		final Text text = new Text(comp, SWT.BORDER | SWT.PASSWORD);
		if (txt != null)
			text.setText(txt);
		GridData data = new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING);
		data.widthHint = 150;
		text.setLayoutData(data);
		if (listener != null)
			text.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					listener.valueChanged(text.getText());
				}
			});
		return text;
	}

	protected Text createText(Composite comp, String txt,
			final StringModifyListener listener) {
		final Text text = new Text(comp, SWT.BORDER);
		if (txt != null)
			text.setText(txt);
		GridData data = new GridData(GridData.FILL_HORIZONTAL
				| GridData.VERTICAL_ALIGN_BEGINNING);
		data.widthHint = 150;
		text.setLayoutData(data);
		if (listener != null)
			text.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					listener.valueChanged(text.getText());
				}
			});
		return text;
	}

	protected Combo createTypeCombo(Composite comp, final ArrayList types,
			String sel, final TypeModifyListener listener) {
		final Combo combo = new Combo(comp, SWT.DROP_DOWN | SWT.READ_ONLY);
		int size = types.size();
		String[] items = new String[size];
		int index = -1;
		for (int i = 0; i < size; i++) {
			items[i] = (String) types.get(i);
			if (items[i].equals(sel))
				index = i;
		}
		combo.setItems(items);
		if (index >= 0)
			combo.select(index);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_BEGINNING);
		data.widthHint = 150;
		combo.setLayoutData(data);
		if (listener != null)
			combo.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					listener.valueChanged((String) types.get(combo
							.getSelectionIndex()));
				}

				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
			});
		return combo;
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		((GridLayout) composite.getLayout()).numColumns = 2;

		// WorkbenchHelp.setHelp(composite, ContextIds.PREF_DIALOG);

		createLabel(composite, PHPHelpPlugin.getResource("%name"));
		fName = createText(composite, fConfiguration.getName() + "",
				new StringModifyListener() {
					public void valueChanged(String name) {
						fConfiguration.setName(name);
						validateFields();
					}
				});

		Group group = new Group(composite, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		group.setLayout(layout);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;

		group.setLayoutData(data);
		group.setText(PHPHelpPlugin.getResource("%configGroup"));

		// createLabel(group, PHPHelpPlugin.getResource("%user"));
		// fUserName = createText(group, fConfiguration.getUser() + "", new
		// StringModifyListener() {
		// public void valueChanged(String s) {
		// fConfiguration.setUser(s);
		// validateFields();
		// }
		// });

		// Composite warningComposite = new Composite(group, SWT.NONE);
		// layout = new GridLayout();
		// layout.numColumns = 2;
		// layout.marginHeight = 0;
		// layout.marginHeight = 0;
		// warningComposite.setLayout(layout);
		// data = new GridData(GridData.FILL_HORIZONTAL);
		// data.horizontalSpan = 3;
		// warningComposite.setLayoutData(data);
		// Label warningLabel = new Label(warningComposite, SWT.NONE);
		// warningLabel.setImage(getImage(DLG_IMG_MESSAGE_WARNING));
		// warningLabel.setLayoutData(new
		// GridData(GridData.VERTICAL_ALIGN_BEGINNING
		// | GridData.HORIZONTAL_ALIGN_BEGINNING));
		// Label warningText = new Label(warningComposite, SWT.WRAP);
		// warningText.setText(PHPHelpPlugin.getResource("%scrambledPassword"));
		// //$NON-NLS-1$
		// data = new GridData(GridData.FILL_HORIZONTAL);
		// data.widthHint = 300;
		// warningText.setLayoutData(data);

		// createLabel(group, PHPHelpPlugin.getResource("%password"));
		// fPassword = createPassword(group, fConfiguration.getPassword() + "",
		// new StringModifyListener() {
		// public void valueChanged(String s) {
		// fConfiguration.setPassword(s);
		// validateFields();
		// }
		// });

		createLabel(group, PHPHelpPlugin.getResource("%url"));
		fUrl = createText(group, fConfiguration.getURL(),
				new StringModifyListener() {
					public void valueChanged(String s) {
						fConfiguration.setURL(s);
						validateFields();
					}
				});

		createLabel(group, PHPHelpPlugin.getResource("%parseType"));
		createTypeCombo(group, PHPHelpPlugin.getTypes(), fConfiguration
				.getType(), new TypeModifyListener() {
			public void valueChanged(String fType) {
				fConfiguration.setType(fType);
			}
		});

		return composite;
	}

	protected void okPressed() {
		fConfiguration.save();
		super.okPressed();
	}

	protected Control createButtonBar(Composite parent) {
		Control buttonControl = super.createButtonBar(parent);
		validateFields();
		return buttonControl;
	}

	private void setOKButtonEnabled(boolean curIsEnabled) {
		if (okButton == null)
			okButton = getButton(IDialogConstants.OK_ID);

		if (okButton != null)
			okButton.setEnabled(curIsEnabled);
	}

	protected void validateFields() {
		boolean result = true;

		setOKButtonEnabled(result);
	}

}