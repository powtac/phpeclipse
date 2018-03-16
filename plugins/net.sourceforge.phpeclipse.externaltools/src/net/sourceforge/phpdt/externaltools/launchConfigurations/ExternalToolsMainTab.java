package net.sourceforge.phpdt.externaltools.launchConfigurations;

/***********************************************************************************************************************************
 * Copyright (c) 2000, 2002 IBM Corp. All rights reserved. This file is made available under the terms of the Common Public License
 * v1.0 which accompanies this distribution, and is available at http://www.eclipse.org/legal/cpl-v10.html
 **********************************************************************************************************************************/

import java.io.File;

import net.sourceforge.phpdt.externaltools.group.IGroupDialogPage;
import net.sourceforge.phpdt.externaltools.internal.dialog.ExternalToolVariableForm;
import net.sourceforge.phpdt.externaltools.internal.model.ExternalToolsImages;
import net.sourceforge.phpdt.externaltools.internal.registry.ExternalToolVariable;
import net.sourceforge.phpdt.externaltools.model.IExternalToolConstants;
import net.sourceforge.phpdt.externaltools.model.ToolUtil;
import net.sourceforge.phpdt.externaltools.variable.ExpandVariableContext;
import net.sourceforge.phpeclipse.externaltools.ExternalToolsPlugin;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.ResourceSelectionDialog;
import org.eclipse.ui.dialogs.SelectionDialog;

public class ExternalToolsMainTab extends AbstractLaunchConfigurationTab {

	protected Combo locationField;

	protected Text workDirectoryField;

	protected Button fileLocationButton;

	protected Button workspaceLocationButton;

	protected Button fileWorkingDirectoryButton;

	protected Button workspaceWorkingDirectoryButton;

	protected Button runBackgroundButton;

	protected Text argumentField;

	protected Button variableButton;

	protected SelectionAdapter selectionAdapter;

	protected ModifyListener modifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			updateLaunchConfigurationDialog();
		}
	};

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(gridData);
		mainComposite.setFont(parent.getFont());
		createLocationComponent(mainComposite);
		createWorkDirectoryComponent(mainComposite);
		createArgumentComponent(mainComposite);
		createVerticalSpacer(mainComposite, 2);
		createRunBackgroundComponent(mainComposite);
	}

	/**
	 * Creates the controls needed to edit the location attribute of an external
	 * tool
	 * 
	 * @param parent
	 *            the composite to create the controls in
	 */
	protected void createLocationComponent(Composite parent) {
		Font font = parent.getFont();

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 1;
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayout(layout);
		composite.setLayoutData(gridData);

		Label label = new Label(composite, SWT.NONE);
		label.setText(ExternalToolsLaunchConfigurationMessages
				.getString("ExternalToolsMainTab.&Location___2")); //$NON-NLS-1$
		label.setFont(font);

		final IPreferenceStore store = ExternalToolsPlugin.getDefault()
				.getPreferenceStore();
		locationField = new Combo(composite, SWT.DROP_DOWN | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		locationField.setLayoutData(data);
		locationField.setFont(font);
		locationField.add(store.getString(ExternalToolsPlugin.PHP_RUN_PREF), 0);
		locationField.add(store.getString(ExternalToolsPlugin.APACHE_RUN_PREF),
				1);
		locationField.add(store.getString(ExternalToolsPlugin.MYSQL_RUN_PREF),
				2);
		locationField.add(
				store.getString(ExternalToolsPlugin.XAMPP_START_PREF), 3);
		locationField.add(store.getString(ExternalToolsPlugin.XAMPP_STOP_PREF),
				4);
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 1;
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		buttonComposite.setLayout(layout);
		buttonComposite.setLayoutData(gridData);
		buttonComposite.setFont(font);

		createVerticalSpacer(buttonComposite, 1);

		workspaceLocationButton = createPushButton(
				buttonComposite,
				ExternalToolsLaunchConfigurationMessages
						.getString("ExternalToolsMainTab.&Browse_Workspace..._3"), null); //$NON-NLS-1$
		workspaceLocationButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleWorkspaceLocationButtonSelected();
			}
		});
		fileLocationButton = createPushButton(
				buttonComposite,
				ExternalToolsLaunchConfigurationMessages
						.getString("ExternalToolsMainTab.Brows&e_File_System..._4"), null); //$NON-NLS-1$
		fileLocationButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleLocationButtonSelected();
			}
		});
	}

	/**
	 * Creates the controls needed to edit the working directory attribute of an
	 * external tool
	 * 
	 * @param parent
	 *            the composite to create the controls in
	 */
	protected void createWorkDirectoryComponent(Composite parent) {
		Font font = parent.getFont();

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 1;
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayout(layout);
		composite.setLayoutData(gridData);

		Label label = new Label(composite, SWT.NONE);
		label.setText(ExternalToolsLaunchConfigurationMessages
				.getString("ExternalToolsMainTab.Working_&Directory__5")); //$NON-NLS-1$
		label.setFont(font);

		workDirectoryField = new Text(composite, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		workDirectoryField.setLayoutData(data);
		workDirectoryField.setFont(font);

		Composite buttonComposite = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 1;
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		buttonComposite.setLayout(layout);
		buttonComposite.setLayoutData(gridData);
		buttonComposite.setFont(font);

		createVerticalSpacer(buttonComposite, 1);
		workspaceWorkingDirectoryButton = createPushButton(
				buttonComposite,
				ExternalToolsLaunchConfigurationMessages
						.getString("ExternalToolsMainTab.Browse_Wor&kspace..._6"), null); //$NON-NLS-1$
		workspaceWorkingDirectoryButton
				.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent evt) {
						handleWorkspaceWorkingDirectoryButtonSelected();
					}
				});
		fileWorkingDirectoryButton = createPushButton(
				buttonComposite,
				ExternalToolsLaunchConfigurationMessages
						.getString("ExternalToolsMainTab.Browse_F&ile_System..._7"), null); //$NON-NLS-1$
		fileWorkingDirectoryButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleFileWorkingDirectoryButtonSelected();
			}
		});
	}

	/**
	 * Creates the controls needed to edit the argument and prompt for argument
	 * attributes of an external tool
	 * 
	 * @param parent
	 *            the composite to create the controls in
	 */
	protected void createArgumentComponent(Composite parent) {
		Font font = parent.getFont();

		Label label = new Label(parent, SWT.NONE);
		label.setText(ExternalToolsLaunchConfigurationMessages
				.getString("ExternalToolsOptionTab.&Arguments___1")); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		label.setFont(font);

		argumentField = new Text(parent, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		argumentField.setLayoutData(data);
		argumentField.setFont(font);
		argumentField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});

		variableButton = createPushButton(
				parent,
				ExternalToolsLaunchConfigurationMessages
						.getString("ExternalToolsOptionTab.Varia&bles..._2"), null); //$NON-NLS-1$
		variableButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				VariableSelectionDialog dialog = new VariableSelectionDialog(
						getShell());
				if (dialog.open() == SelectionDialog.OK) {
					argumentField
							.insert(dialog.getForm().getSelectedVariable());
				}
			}
		});

		Label instruction = new Label(parent, SWT.NONE);
		instruction
				.setText(ExternalToolsLaunchConfigurationMessages
						.getString("ExternalToolsOptionTab.Note__Enclose_an_argument_containing_spaces_using_double-quotes_(__)._Not_applicable_for_variables._3")); //$NON-NLS-1$
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		instruction.setLayoutData(data);
		instruction.setFont(font);
	}

	/**
	 * Creates the controls needed to edit the run in background attribute of an
	 * external tool
	 * 
	 * @param parent
	 *            the composite to create the controls in
	 */
	protected void createRunBackgroundComponent(Composite parent) {
		runBackgroundButton = new Button(parent, SWT.CHECK);
		runBackgroundButton.setText(ExternalToolsLaunchConfigurationMessages
				.getString("ExternalToolsOptionTab.Run_tool_in_bac&kground_4")); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		runBackgroundButton.setLayoutData(data);
		runBackgroundButton.setFont(parent.getFont());
		runBackgroundButton.addSelectionListener(getSelectionAdapter());
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(
				IExternalToolConstants.ATTR_RUN_IN_BACKGROUND, false);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		updateLocation(configuration);
		updateWorkingDirectory(configuration);
		updateArgument(configuration);
		updateRunBackground(configuration);
	}

	protected void updateWorkingDirectory(ILaunchConfiguration configuration) {
		String workingDir = ""; //$NON-NLS-1$
		try {
			workingDir = configuration.getAttribute(
					IExternalToolConstants.ATTR_WORKING_DIRECTORY, ""); //$NON-NLS-1$
		} catch (CoreException ce) {
			ExternalToolsPlugin
					.getDefault()
					.log(
							ExternalToolsLaunchConfigurationMessages
									.getString("ExternalToolsMainTab.Error_reading_configuration_10"), ce); //$NON-NLS-1$
		}
		workDirectoryField.setText(workingDir);
		workDirectoryField.addModifyListener(modifyListener);

	}

	protected void updateLocation(ILaunchConfiguration configuration) {
		String location = ""; //$NON-NLS-1$
		try {
			location = configuration.getAttribute(
					IExternalToolConstants.ATTR_LOCATION, ""); //$NON-NLS-1$
		} catch (CoreException ce) {
			ExternalToolsPlugin
					.getDefault()
					.log(
							ExternalToolsLaunchConfigurationMessages
									.getString("ExternalToolsMainTab.Error_reading_configuration_10"), ce); //$NON-NLS-1$
		}
		locationField.setText(location);
		locationField.addModifyListener(modifyListener);
	}

	protected void updateArgument(ILaunchConfiguration configuration) {
		String arguments = ""; //$NON-NLS-1$
		try {
			arguments = configuration.getAttribute(
					IExternalToolConstants.ATTR_TOOL_ARGUMENTS, ""); //$NON-NLS-1$
		} catch (CoreException ce) {
			ExternalToolsPlugin
					.getDefault()
					.log(
							ExternalToolsLaunchConfigurationMessages
									.getString("ExternalToolsOptionTab.Error_reading_configuration_7"), ce); //$NON-NLS-1$
		}
		argumentField.setText(arguments);
	}

	protected void updateRunBackground(ILaunchConfiguration configuration) {
		boolean runInBackgroud = true;
		try {
			runInBackgroud = configuration.getAttribute(
					IExternalToolConstants.ATTR_RUN_IN_BACKGROUND, false);
		} catch (CoreException ce) {
			ExternalToolsPlugin
					.getDefault()
					.log(
							ExternalToolsLaunchConfigurationMessages
									.getString("ExternalToolsOptionTab.Error_reading_configuration_7"), ce); //$NON-NLS-1$
		}
		runBackgroundButton.setSelection(runInBackgroud);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		String location = locationField.getText().trim();
		if (location.length() == 0) {
			configuration.setAttribute(IExternalToolConstants.ATTR_LOCATION,
					(String) null);
		} else {
			configuration.setAttribute(IExternalToolConstants.ATTR_LOCATION,
					location);
		}

		String workingDirectory = workDirectoryField.getText().trim();
		if (workingDirectory.length() == 0) {
			configuration.setAttribute(
					IExternalToolConstants.ATTR_WORKING_DIRECTORY,
					(String) null);
		} else {
			configuration.setAttribute(
					IExternalToolConstants.ATTR_WORKING_DIRECTORY,
					workingDirectory);
		}

		setAttribute(IExternalToolConstants.ATTR_RUN_IN_BACKGROUND,
				configuration, runBackgroundButton.getSelection(), false);

		String arguments = argumentField.getText().trim();
		if (arguments.length() == 0) {
			configuration.setAttribute(
					IExternalToolConstants.ATTR_TOOL_ARGUMENTS, (String) null);
		} else {
			configuration.setAttribute(
					IExternalToolConstants.ATTR_TOOL_ARGUMENTS, arguments);
		}
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return ExternalToolsLaunchConfigurationMessages
				.getString("ExternalToolsMainTab.&Main_17"); //$NON-NLS-1$
	}

	/**
	 * @see ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		setMessage(null);
		return validateLocation() && validateWorkDirectory();
	}

	/**
	 * Validates the content of the location field.
	 */
	protected boolean validateLocation() {
		String value = locationField.getText().trim();
		if (value.length() < 1) {
			setErrorMessage(ExternalToolsLaunchConfigurationMessages
					.getString("ExternalToolsMainTab.External_tool_location_cannot_be_empty_18")); //$NON-NLS-1$
			setMessage(null);
			return false;
		}

		// Translate field contents to the actual file location so we
		// can check to ensure the file actually exists.
		MultiStatus multiStatus = new MultiStatus(
				IExternalToolConstants.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
		value = ToolUtil.expandFileLocation(value,
				ExpandVariableContext.EMPTY_CONTEXT, multiStatus);
		if (!multiStatus.isOK()) {
			IStatus[] children = multiStatus.getChildren();
			if (children.length > 0) {
				setErrorMessage(children[0].getMessage());
				setMessage(null);
			}
			return false;
		}

		File file = new File(value);
		if (!file.exists()) { // The file does not exist.
			setErrorMessage(ExternalToolsLaunchConfigurationMessages
					.getString("ExternalToolsMainTab.External_tool_location_does_not_exist_19")); //$NON-NLS-1$
			return false;
		}
		if (!file.isFile()) {
			setErrorMessage(ExternalToolsLaunchConfigurationMessages
					.getString("ExternalToolsMainTab.External_tool_location_specified_is_not_a_file_20")); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	/**
	 * Validates the content of the working directory field.
	 */
	protected boolean validateWorkDirectory() {

		String value = workDirectoryField.getText().trim();
		if (value.length() > 0) {
			// Translate field contents to the actual directory location so we
			// can check to ensure the directory actually exists.
			MultiStatus multiStatus = new MultiStatus(
					IExternalToolConstants.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
			value = ToolUtil.expandDirectoryLocation(value,
					ExpandVariableContext.EMPTY_CONTEXT, multiStatus);
			if (!multiStatus.isOK()) {
				IStatus[] children = multiStatus.getChildren();
				if (children.length > 0) {
					setErrorMessage(children[0].getMessage());
				}
				return false;
			}

			File file = new File(value);
			if (!file.exists()) { // The directory does not exist.
				setErrorMessage(ExternalToolsLaunchConfigurationMessages
						.getString("ExternalToolsMainTab.External_tool_working_directory_does_not_exist_or_is_invalid_21")); //$NON-NLS-1$
				return false;
			}
		}
		return true;
	}

	protected void handleLocationButtonSelected() {
		FileDialog fileDialog = new FileDialog(getShell(), SWT.NONE);
		fileDialog.setFileName(locationField.getText());
		String text = fileDialog.open();
		if (text != null) {
			locationField.setText(text);
		}
	}

	/**
	 * Prompts the user for a workspace location within the workspace and sets
	 * the location as a String containing the workspace_loc variable or
	 * <code>null</code> if no location was obtained from the user.
	 */
	protected void handleWorkspaceLocationButtonSelected() {
		ResourceSelectionDialog dialog;
		dialog = new ResourceSelectionDialog(getShell(), ResourcesPlugin
				.getWorkspace().getRoot(),
				ExternalToolsLaunchConfigurationMessages
						.getString("ExternalToolsMainTab.Select_a_resource_22")); //$NON-NLS-1$
		dialog.open();
		Object[] results = dialog.getResult();
		if (results == null || results.length < 1) {
			return;
		}
		IResource resource = (IResource) results[0];
		StringBuffer buf = new StringBuffer();
		ToolUtil.buildVariableTag(IExternalToolConstants.VAR_WORKSPACE_LOC,
				resource.getFullPath().toString(), buf);
		String text = buf.toString();
		if (text != null) {
			locationField.setText(text);
		}
	}

	/**
	 * Prompts the user for a working directory location within the workspace
	 * and sets the working directory as a String containing the workspace_loc
	 * variable or <code>null</code> if no location was obtained from the
	 * user.
	 */
	protected void handleWorkspaceWorkingDirectoryButtonSelected() {
		ContainerSelectionDialog containerDialog;
		containerDialog = new ContainerSelectionDialog(
				getShell(),
				ResourcesPlugin.getWorkspace().getRoot(),
				false,
				ExternalToolsLaunchConfigurationMessages
						.getString("ExternalToolsMainTab.&Select_a_directory__23")); //$NON-NLS-1$
		containerDialog.open();
		Object[] resource = containerDialog.getResult();
		String text = null;
		if (resource != null && resource.length > 0) {
			text = ToolUtil.buildVariableTag(
					IExternalToolConstants.VAR_RESOURCE_LOC,
					((IPath) resource[0]).toString());
		}
		if (text != null) {
			workDirectoryField.setText(text);
		}
	}

	protected void handleFileWorkingDirectoryButtonSelected() {
		DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.SAVE);
		dialog.setMessage(ExternalToolsLaunchConfigurationMessages
				.getString("ExternalToolsMainTab.&Select_a_directory__23")); //$NON-NLS-1$
		dialog.setFilterPath(workDirectoryField.getText());
		String text = dialog.open();
		if (text != null) {
			workDirectoryField.setText(text);
		}
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return ExternalToolsImages
				.getImage(IExternalToolConstants.IMG_TAB_MAIN);
	}

	/**
	 * Method getSelectionAdapter.
	 * 
	 * @return SelectionListener
	 */
	protected SelectionListener getSelectionAdapter() {
		if (selectionAdapter == null) {
			selectionAdapter = new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					updateLaunchConfigurationDialog();
				}
			};
		}
		return selectionAdapter;
	}

	private class VariableSelectionDialog extends SelectionDialog {
		private ExternalToolVariableForm form;

		private VariableSelectionDialog(Shell parent) {
			super(parent);
			setTitle(ExternalToolsLaunchConfigurationMessages
					.getString("ExternalToolsOptionTab.Select_variable_10")); //$NON-NLS-1$
		}

		protected Control createDialogArea(Composite parent) {
			// Create the dialog area
			Composite composite = (Composite) super.createDialogArea(parent);
			ExternalToolVariable[] variables = ExternalToolsPlugin.getDefault()
					.getArgumentVariableRegistry().getArgumentVariables();
			form = new ExternalToolVariableForm(
					ExternalToolsLaunchConfigurationMessages
							.getString("ExternalToolsOptionTab.&Choose_a_variable__11"), variables); //$NON-NLS-1$
			form.createContents(composite, new IGroupDialogPage() {
				public GridData setButtonGridData(Button button) {
					GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
					data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
					int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
					data.widthHint = Math.max(widthHint, button.computeSize(
							SWT.DEFAULT, SWT.DEFAULT, true).x);
					button.setLayoutData(data);
					return data;
				}

				public void setMessage(String newMessage, int newType) {
					VariableSelectionDialog.this.setMessage(newMessage);
				}

				public void updateValidState() {
				}

				public int convertHeightHint(int chars) {
					return convertHeightInCharsToPixels(chars);
				}

				public String getMessage() {
					if (!form.isValid()) {
						return ExternalToolsLaunchConfigurationMessages
								.getString("ExternalToolsOptionTab.Invalid_selection_12"); //$NON-NLS-1$
					}
					return null;
				}

				public int getMessageType() {
					if (!form.isValid()) {
						return IMessageProvider.ERROR;
					}
					return 0;
				}
			});
			return composite;
		}

		private ExternalToolVariableForm getForm() {
			return form;
		}
	}

}