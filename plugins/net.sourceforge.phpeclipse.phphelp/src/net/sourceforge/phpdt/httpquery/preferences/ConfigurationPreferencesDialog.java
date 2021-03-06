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

import net.sourceforge.phpdt.httpquery.config.IConfigurationWorkingCopy;
import net.sourceforge.phpdt.phphelp.PHPHelpPlugin;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class ConfigurationPreferencesDialog extends Dialog {
	protected IConfigurationWorkingCopy monitor;

	protected boolean isEdit;

	/**
	 * @param parentShell
	 */
	public ConfigurationPreferencesDialog(Shell parentShell,
			IConfigurationWorkingCopy monitor) {
		super(parentShell);
		this.monitor = monitor;
		isEdit = true;
	}

	public ConfigurationPreferencesDialog(Shell composite) {
		super(composite);
		isEdit = false;
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(PHPHelpPlugin.getResource("%preferenceTitle"));
	}

	protected Control createDialogArea(Composite parent) {
		Composite compositeParent = (Composite) super.createDialogArea(parent);

		Composite composite = new Composite(compositeParent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = convertHorizontalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertVerticalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		// WorkbenchHelp.setHelp(composite, ContextIds.PREF);

		ConfigurationComposite monitorComp = new ConfigurationComposite(
				composite, SWT.NONE);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		monitorComp.setLayoutData(data);

		Dialog.applyDialogFont(composite);

		return composite;
	}

	protected void okPressed() {
		PHPHelpPlugin.getDefault().savePluginPreferences();
		super.okPressed();
	}
}