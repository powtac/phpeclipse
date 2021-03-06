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

import net.sourceforge.phpdt.core.IJavaElement;
import net.sourceforge.phpdt.core.IJavaProject;
import net.sourceforge.phpdt.internal.ui.IJavaHelpContextIds;
import net.sourceforge.phpdt.internal.ui.dialogs.StatusInfo;
import net.sourceforge.phpdt.internal.ui.dialogs.StatusUtil;
import net.sourceforge.phpdt.internal.ui.wizards.IStatusChangeListener;
import net.sourceforge.phpdt.internal.ui.wizards.dialogfields.DialogField;
import net.sourceforge.phpdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import net.sourceforge.phpdt.internal.ui.wizards.dialogfields.LayoutUtil;
import net.sourceforge.phpdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Property page used to configure project specific task tags settings
 */
public class TodoTaskPropertyPage extends PropertyPage {

	private TodoTaskConfigurationBlock fConfigurationBlock;

	private Control fConfigurationBlockControl;

	private ControlEnableState fBlockEnableState;

	private SelectionButtonDialogField fUseWorkspaceSettings;

	private SelectionButtonDialogField fChangeWorkspaceSettings;

	private SelectionButtonDialogField fUseProjectSettings;

	private IStatus fBlockStatus;

	public TodoTaskPropertyPage() {
		fBlockStatus = new StatusInfo();
		fBlockEnableState = null;

		IDialogFieldListener listener = new IDialogFieldListener() {
			public void dialogFieldChanged(DialogField field) {
				doDialogFieldChanged(field);
			}
		};

		fUseWorkspaceSettings = new SelectionButtonDialogField(SWT.RADIO);
		fUseWorkspaceSettings.setDialogFieldListener(listener);
		fUseWorkspaceSettings.setLabelText(PreferencesMessages
				.getString("TodoTaskPropertyPage.useworkspacesettings.label")); //$NON-NLS-1$

		fChangeWorkspaceSettings = new SelectionButtonDialogField(SWT.PUSH);
		fChangeWorkspaceSettings.setLabelText(PreferencesMessages
				.getString("TodoTaskPropertyPage.useworkspacesettings.change")); //$NON-NLS-1$
		fChangeWorkspaceSettings.setDialogFieldListener(listener);

		fUseWorkspaceSettings.attachDialogField(fChangeWorkspaceSettings);

		fUseProjectSettings = new SelectionButtonDialogField(SWT.RADIO);
		fUseProjectSettings.setDialogFieldListener(listener);
		fUseProjectSettings.setLabelText(PreferencesMessages
				.getString("TodoTaskPropertyPage.useprojectsettings.label")); //$NON-NLS-1$
	}

	/*
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
				IJavaHelpContextIds.TODOTASK_PROPERTY_PAGE);
	}

	/*
	 * @see org.eclipse.jface.preference.IPreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		IStatusChangeListener listener = new IStatusChangeListener() {
			public void statusChanged(IStatus status) {
				fBlockStatus = status;
				doStatusChanged();
			}
		};
		fConfigurationBlock = new TodoTaskConfigurationBlock(listener,
				getProject());

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 1;
		composite.setLayout(layout);

		fUseWorkspaceSettings.doFillIntoGrid(composite, 1);
		LayoutUtil.setHorizontalGrabbing(fUseWorkspaceSettings
				.getSelectionButton(null));

		fChangeWorkspaceSettings.doFillIntoGrid(composite, 1);
		GridData data = (GridData) fChangeWorkspaceSettings.getSelectionButton(
				null).getLayoutData();
		data.horizontalIndent = convertWidthInCharsToPixels(3);
		data.horizontalAlignment = GridData.BEGINNING;

		fUseProjectSettings.doFillIntoGrid(composite, 1);

		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_FILL);
		data.horizontalSpan = 1;
		data.horizontalIndent = convertWidthInCharsToPixels(2);

		fConfigurationBlockControl = fConfigurationBlock
				.createContents(composite);
		fConfigurationBlockControl.setLayoutData(data);

		boolean useProjectSettings = fConfigurationBlock
				.hasProjectSpecificOptions();

		fUseProjectSettings.setSelection(useProjectSettings);
		fUseWorkspaceSettings.setSelection(!useProjectSettings);

		updateEnableState();
		Dialog.applyDialogFont(composite);
		return composite;
	}

	private boolean useProjectSettings() {
		return fUseProjectSettings.isSelected();
	}

	private void doDialogFieldChanged(DialogField field) {
		if (field == fChangeWorkspaceSettings) {
			TodoTaskPreferencePage page = new TodoTaskPreferencePage();
			showPreferencePage(TodoTaskPreferencePage.ID, page);
		} else {
			updateEnableState();
			doStatusChanged();
		}
	}

	/**
	 * Method statusChanged.
	 */
	private void doStatusChanged() {
		updateStatus(useProjectSettings() ? fBlockStatus : new StatusInfo());
	}

	/**
	 * Method getProject.
	 */
	private IJavaProject getProject() {
		return (IJavaProject) getElement().getAdapter(IJavaElement.class);
	}

	private void updateEnableState() {
		if (useProjectSettings()) {
			if (fBlockEnableState != null) {
				fBlockEnableState.restore();
				fBlockEnableState = null;
			}
		} else {
			if (fBlockEnableState == null) {
				fBlockEnableState = ControlEnableState
						.disable(fConfigurationBlockControl);
			}
		}
	}

	/*
	 * @see org.eclipse.jface.preference.IPreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		if (useProjectSettings()) {
			fUseProjectSettings.setSelection(false);
			fUseWorkspaceSettings.setSelection(true);
			fConfigurationBlock.performDefaults();
		}
		super.performDefaults();
	}

	/*
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		return fConfigurationBlock.performOk(useProjectSettings());
	}

	private void updateStatus(IStatus status) {
		setValid(!status.matches(IStatus.ERROR));
		StatusUtil.applyToStatusLine(this, status);
	}

	private boolean showPreferencePage(String id, IPreferencePage page) {
		final IPreferenceNode targetNode = new PreferenceNode(id, page);

		PreferenceManager manager = new PreferenceManager();
		manager.addToRoot(targetNode);
		final PreferenceDialog dialog = new PreferenceDialog(getShell(),
				manager);
		final boolean[] result = new boolean[] { false };
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				dialog.create();
				dialog.setMessage(targetNode.getLabelText());
				result[0] = (dialog.open() == Window.OK);
			}
		});
		return result[0];
	}

}
