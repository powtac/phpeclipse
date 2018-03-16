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

import net.sourceforge.phpeclipse.webbrowser.IInternalWebBrowserWorkingCopy;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * 
 */
public class InternalBrowserDialog extends Dialog {
	protected IInternalWebBrowserWorkingCopy browser;

	protected boolean isEdit;

	protected Button newPageCheckbox;

	protected Button clearURLHistoryCheckbox;

	/**
	 * @param parentShell
	 */
	public InternalBrowserDialog(Shell parentShell,
			IInternalWebBrowserWorkingCopy browser) {
		super(parentShell);
		this.browser = browser;
		isEdit = true;
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);

		if (isEdit)
			shell.setText(WebBrowserUIPlugin
					.getResource("%editInternalBrowser"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		((GridLayout) composite.getLayout()).numColumns = 1;

		Composite comp = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 10;
		layout.marginWidth = 10;
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite,
				ContextIds.PREF_BROWSER_INTERNAL);

		newPageCheckbox = SWTUtil.createCheckbox(comp, WebBrowserUIPlugin
				.getResource("%prefBrowserNewPage"), false);
		clearURLHistoryCheckbox = SWTUtil.createCheckbox(comp,
				WebBrowserUIPlugin.getResource("%clearURLHistory"), true);

		newPageCheckbox.setSelection(browser.getUseNewPage());
		clearURLHistoryCheckbox.setSelection(browser.getClearHistoryOnExit());

		return composite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		browser.setUseNewPage(newPageCheckbox.getSelection());
		browser.setClearHistoryOnExit(clearURLHistoryCheckbox.getSelection());
		browser.save();

		super.okPressed();
	}
}