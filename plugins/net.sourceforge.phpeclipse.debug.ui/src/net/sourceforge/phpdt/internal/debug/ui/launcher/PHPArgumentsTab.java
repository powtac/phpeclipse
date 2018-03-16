package net.sourceforge.phpdt.internal.debug.ui.launcher;

import net.sourceforge.phpdt.debug.ui.PHPDebugUiConstants;
import net.sourceforge.phpdt.debug.ui.PHPDebugUiImages;
import net.sourceforge.phpdt.internal.debug.ui.PHPDebugUiMessages;
import net.sourceforge.phpdt.internal.debug.ui.PHPDebugUiPlugin;
import net.sourceforge.phpdt.internal.launching.PHPLaunchConfigurationAttribute;
import net.sourceforge.phpdt.internal.ui.util.DirectorySelector;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class PHPArgumentsTab extends AbstractLaunchConfigurationTab {
	protected Text interpreterArgsText, programArgsText;

	protected DirectorySelector workingDirectorySelector;

	protected Button useDefaultWorkingDirectoryButton;

	private class ArgumentsTabListener extends SelectionAdapter implements
			ModifyListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
		 */
		public void modifyText(ModifyEvent e) {
			updateLaunchConfigurationDialog();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetSelected(SelectionEvent e) {
			updateLaunchConfigurationDialog();
		}

	}

	private ArgumentsTabListener fListener = new ArgumentsTabListener();

	public PHPArgumentsTab() {
		super();
	}

	public void createControl(Composite parent) {
		Composite composite = createPageRoot(parent);

//		new Label(composite, SWT.NONE).setText(PHPDebugUiMessages
//				.getString("LaunchConfigurationTab.PHPArguments.working_dir"));
//		workingDirectorySelector = new DirectorySelector(composite);
		Group grpWorkingDir = new Group(composite, SWT.NONE);
		grpWorkingDir.setText(PHPDebugUiMessages
				.getString("LaunchConfigurationTab.PHPArguments.working_dir"));
		grpWorkingDir.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpWorkingDir.setLayout(new GridLayout());
		workingDirectorySelector = new DirectorySelector(grpWorkingDir);
		workingDirectorySelector
				.setBrowseDialogMessage(PHPDebugUiMessages
						.getString("LaunchConfigurationTab.PHPArguments.working_dir_browser_message"));
		workingDirectorySelector.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));
		workingDirectorySelector.addModifyListener(fListener);

		Composite defaultWorkingDirectoryComposite = new Composite(
				grpWorkingDir, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		defaultWorkingDirectoryComposite.setLayout(layout);
		useDefaultWorkingDirectoryButton = new Button(
				defaultWorkingDirectoryComposite, SWT.CHECK);
		useDefaultWorkingDirectoryButton
				.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						setUseDefaultWorkingDirectory(((Button) e.getSource())
								.getSelection());
					}
				});
		useDefaultWorkingDirectoryButton.addSelectionListener(fListener);
		
		new Label(defaultWorkingDirectoryComposite, SWT.NONE)
				.setText(PHPDebugUiMessages
						.getString("LaunchConfigurationTab.PHPArguments.working_dir_use_default_message"));
		defaultWorkingDirectoryComposite.pack();
		workingDirectorySelector.addModifyListener(fListener);

		
//		new Label(composite, SWT.NONE)
//				.setText(PHPDebugUiMessages
//						.getString("LaunchConfigurationTab.PHPArguments.interpreter_args_box_title"));
		Group grpArgs = new Group(composite, SWT.NONE);
		grpArgs.setText(PHPDebugUiMessages
				.getString("LaunchConfigurationTab.PHPArguments.interpreter_args_box_title"));
		grpArgs.setLayoutData(new GridData(GridData.FILL_BOTH));
		grpArgs.setLayout(new GridLayout());
		interpreterArgsText = new Text(grpArgs, SWT.MULTI | SWT.V_SCROLL
				| SWT.BORDER | SWT.WRAP);
		interpreterArgsText.setLayoutData(new GridData(GridData.FILL_BOTH));
		interpreterArgsText.addModifyListener(fListener);

		
//		new Label(composite, SWT.NONE)
//				.setText(PHPDebugUiMessages
//						.getString("LaunchConfigurationTab.PHPArguments.program_args_box_title"));
		Group grpProgArgs = new Group(composite, SWT.NONE);
		grpProgArgs.setText(PHPDebugUiMessages
				.getString("LaunchConfigurationTab.PHPArguments.program_args_box_title"));
		grpProgArgs.setLayoutData(new GridData(GridData.FILL_BOTH));
		grpProgArgs.setLayout(new GridLayout());
		programArgsText = new Text(grpProgArgs, SWT.MULTI | SWT.V_SCROLL
				| SWT.BORDER | SWT.WRAP);
		programArgsText.setLayoutData(new GridData(GridData.FILL_BOTH));
		programArgsText.addModifyListener(fListener);
	}

	protected void setUseDefaultWorkingDirectory(boolean useDefault) {
		if (!useDefaultWorkingDirectoryButton.getSelection() == useDefault)
			useDefaultWorkingDirectoryButton.setSelection(useDefault);
		if (useDefault)
			workingDirectorySelector
					.setSelectionText(PHPDebugUiConstants.DEFAULT_WORKING_DIRECTORY);
		workingDirectorySelector.setEnabled(!useDefault);
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(
				PHPLaunchConfigurationAttribute.WORKING_DIRECTORY,
				PHPDebugUiConstants.DEFAULT_WORKING_DIRECTORY);
		// set hidden attribute
		configuration.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID,
				"net.sourceforge.phpdt.debug.ui.PHPSourceLocator");
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		String workingDirectory = "", interpreterArgs = "", programArgs = "";
		boolean useDefaultWorkDir = true;
		try {
			workingDirectory = configuration.getAttribute(
					PHPLaunchConfigurationAttribute.WORKING_DIRECTORY, "");
			interpreterArgs = configuration.getAttribute(
					PHPLaunchConfigurationAttribute.INTERPRETER_ARGUMENTS, "");
			programArgs = configuration.getAttribute(
					PHPLaunchConfigurationAttribute.PROGRAM_ARGUMENTS, "");
			useDefaultWorkDir = configuration
					.getAttribute(
							PHPLaunchConfigurationAttribute.USE_DEFAULT_WORKING_DIRECTORY,
							true);
		} catch (CoreException e) {
			log(e);
		}

		workingDirectorySelector.setSelectionText(workingDirectory);
		interpreterArgsText.setText(interpreterArgs);
		programArgsText.setText(programArgs);
		setUseDefaultWorkingDirectory(useDefaultWorkDir);
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(
				PHPLaunchConfigurationAttribute.WORKING_DIRECTORY,
				workingDirectorySelector.getValidatedSelectionText());
		configuration.setAttribute(
				PHPLaunchConfigurationAttribute.INTERPRETER_ARGUMENTS,
				interpreterArgsText.getText());
		configuration.setAttribute(
				PHPLaunchConfigurationAttribute.PROGRAM_ARGUMENTS,
				programArgsText.getText());
		configuration.setAttribute(
				PHPLaunchConfigurationAttribute.USE_DEFAULT_WORKING_DIRECTORY,
				useDefaultWorkingDirectoryButton.getSelection());
	}

	protected Composite createPageRoot(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout compositeLayout = new GridLayout();
//		compositeLayout.marginWidth = 0;
		compositeLayout.numColumns = 1;
		composite.setLayout(compositeLayout);

		setControl(composite);
		return composite;
	}

	public String getName() {
		return PHPDebugUiMessages
				.getString("LaunchConfigurationTab.PHPArguments.name");
	}

	public boolean isValid(ILaunchConfiguration launchConfig) {
		try {
			String workingDirectory = launchConfig.getAttribute(
					PHPLaunchConfigurationAttribute.WORKING_DIRECTORY, "");
			if (workingDirectory.length() == 0) {
				setErrorMessage(PHPDebugUiMessages
						.getString("LaunchConfigurationTab.PHPArguments.working_dir_error_message"));
				return false;
			}
		} catch (CoreException e) {
			log(e);
		}

		setErrorMessage(null);
		return true;
	}

	protected void log(Throwable t) {
		PHPDebugUiPlugin.log(t);
	}

	public Image getImage() {
		return PHPDebugUiImages.get(PHPDebugUiImages.IMG_EVIEW_ARGUMENTS_TAB);
	}

}