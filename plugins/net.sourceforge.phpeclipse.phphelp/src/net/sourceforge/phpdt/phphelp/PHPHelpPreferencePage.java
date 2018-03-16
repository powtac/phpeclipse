package net.sourceforge.phpdt.phphelp;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PHPHelpPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	FileFieldEditor phpCHMHelpFile;

	BooleanFieldEditor phpCHMHelpEnabled;

	StringFieldEditor phpCHMHelpCommand;

	public PHPHelpPreferencePage() {
		super();
		setPreferenceStore(PHPHelpPlugin.getDefault().getPreferenceStore());
		setDescription(PHPHelpPreferenceMessages
				.getString("PHPHelpPreferencePage.PHPHelpSettings")); //$NON-NLS-1$
	}

	public void init(IWorkbench workbench) {
	}

	protected void performDefaults() {
		phpCHMHelpFile.loadDefault();
		phpCHMHelpEnabled.loadDefault();
		phpCHMHelpCommand.loadDefault();
		super.performDefaults();
	}

	public boolean performOk() {
		phpCHMHelpFile.store();
		phpCHMHelpEnabled.store();
		phpCHMHelpCommand.store();
		return super.performOk();
	}

	protected Control createContents(Composite parent) {
		initializeDialogUnits(parent);
		Composite composite = new Composite(parent, SWT.LEFT);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Composite helpSettingsComposite = new Composite(composite, SWT.NONE);
		// helpSettingsComposite.setLayout(new GridLayout());
		// helpSettingsComposite.setLayoutData(new
		// GridData(GridData.FILL_HORIZONTAL));
		// Group helpSettingsGroup = new Group(helpSettingsComposite, SWT.NONE);
		// helpSettingsGroup.setText("Windows CHM settings");
		// helpSettingsGroup.setLayoutData(new
		// GridData(GridData.FILL_HORIZONTAL));
		// helpSettingsGroup.setLayout(new GridLayout());

		phpCHMHelpEnabled = new BooleanFieldEditor(
				PHPHelpPlugin.PHP_CHM_ENABLED, PHPHelpPreferenceMessages
						.getString("PHPHelpPreferencePage.PHPHelp.format"), //$NON-NLS-1$
				composite);
		phpCHMHelpEnabled.setPage(this);
		phpCHMHelpEnabled.setPreferenceStore(getPreferenceStore());
		phpCHMHelpEnabled.load();

		new Label(composite, SWT.NONE);
		phpCHMHelpFile = new FileFieldEditor(PHPHelpPlugin.PHP_CHM_FILE,
				PHPHelpPreferenceMessages
						.getString("PHPHelpPreferencePage.PHPHelp.chm.file"), //$NON-NLS-1$
				composite);
		phpCHMHelpFile.setPage(this);
		phpCHMHelpFile.setPreferenceStore(getPreferenceStore());
		phpCHMHelpFile.load();

		new Label(composite, SWT.NONE);
		phpCHMHelpCommand = new StringFieldEditor(
				PHPHelpPlugin.PHP_CHM_COMMAND, PHPHelpPreferenceMessages
						.getString("PHPHelpPreferencePage.PHPHelp.command"), //$NON-NLS-1$
				composite);
		phpCHMHelpCommand.setPage(this);
		phpCHMHelpCommand.setPreferenceStore(getPreferenceStore());
		phpCHMHelpCommand.load();

		return composite;
	}
}
