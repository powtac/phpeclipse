/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.phpdt.internal.ui.preferences;

import net.sourceforge.phpdt.internal.ui.IJavaHelpContextIds;
import net.sourceforge.phpdt.internal.ui.dialogs.StatusUtil;
import net.sourceforge.phpdt.internal.ui.wizards.IStatusChangeListener;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/*
 * The page to configure the compiler options.
 */
public class TodoTaskPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage, IStatusChangeListener {

	public static final String ID = "net.sourceforge.phpdt.ui.preferences.TodoTaskPreferencePage"; //$NON-NLS-1$

	private TodoTaskConfigurationBlock fConfigurationBlock;

	public TodoTaskPreferencePage() {
		setPreferenceStore(PHPeclipsePlugin.getDefault().getPreferenceStore());
		// setDescription(PreferencesMessages.getString("TodoTaskPreferencePage.description"));
		// //$NON-NLS-1$

		// only used when page is shown programatically
		setTitle(PreferencesMessages.getString("TodoTaskPreferencePage.title")); //$NON-NLS-1$

		fConfigurationBlock = new TodoTaskConfigurationBlock(this, null);
	}

	/*
	 * @see IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	/*
	 * @see PreferencePage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		// added for 1GEUGE6: ITPJUI:WIN2000 - Help is the same on all
		// preference pages
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
				IJavaHelpContextIds.TODOTASK_PREFERENCE_PAGE);
	}

	/*
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Control result = fConfigurationBlock.createContents(parent);
		Dialog.applyDialogFont(result);
		return result;
	}

	/*
	 * @see IPreferencePage#performOk()
	 */
	public boolean performOk() {
		if (!fConfigurationBlock.performOk(true)) {
			return false;
		}
		return super.performOk();
	}

	/*
	 * @see PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		fConfigurationBlock.performDefaults();
		super.performDefaults();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.phpdt.internal.ui.wizards.IStatusChangeListener#statusChanged(org.eclipse.core.runtime.IStatus)
	 */
	public void statusChanged(IStatus status) {
		setValid(!status.matches(IStatus.ERROR));
		StatusUtil.applyToStatusLine(this, status);
	}

}
