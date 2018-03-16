/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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

/**
 * Preference page for spell checking preferences.
 * 
 * @since 3.0
 */
public class EditorPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage, IStatusChangeListener {

	/** The spelling configuration block */
	private final EditorConfigurationBlock fBlock = new EditorConfigurationBlock(
			this, null);

	/**
	 * Creates a new spelling preference page.
	 */
	public EditorPreferencePage() {

		setPreferenceStore(PHPeclipsePlugin.getDefault().getPreferenceStore());
		setDescription(PreferencesMessages
				.getString("EditorPreferencePage.description")); //$NON-NLS-1$
		setTitle(PreferencesMessages.getString("EditorPreferencePage.title")); //$NON-NLS-1$
	}

	/*
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(final Composite parent) {

		final Control control = fBlock.createContents(parent);
		Dialog.applyDialogFont(control);

		return control;
	}

	/*
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(final Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
				IJavaHelpContextIds.JAVA_EDITOR_PREFERENCE_PAGE);
	}

	/*
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(final IWorkbench workbench) {
		// Do nothing
	}

	/*
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		fBlock.performDefaults();

		super.performDefaults();
	}

	/*
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {

		if (!fBlock.performOk(true))
			return false;

		return super.performOk();
	}

	/*
	 * @see net.sourceforge.phpdt.internal.ui.wizards.IStatusChangeListener#statusChanged(org.eclipse.core.runtime.IStatus)
	 */
	public void statusChanged(final IStatus status) {
		setValid(!status.matches(IStatus.ERROR));

		StatusUtil.applyToStatusLine(this, status);
	}
}
