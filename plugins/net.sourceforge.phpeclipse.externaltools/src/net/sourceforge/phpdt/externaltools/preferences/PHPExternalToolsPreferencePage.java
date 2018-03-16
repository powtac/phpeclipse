package net.sourceforge.phpdt.externaltools.preferences;

import net.sourceforge.phpeclipse.externaltools.ExternalToolsPlugin;

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

public class PHPExternalToolsPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	StringFieldEditor externalParserSFE;

	FileFieldEditor phpRunFFE;

	public PHPExternalToolsPreferencePage() {
		super();
		setPreferenceStore(ExternalToolsPlugin.getDefault()
				.getPreferenceStore());
	}

	public void init(IWorkbench workbench) {
	}

	protected void performDefaults() {
		externalParserSFE.loadDefault();
		phpRunFFE.loadDefault();
		super.performDefaults();
	}

	public boolean performOk() {
		externalParserSFE.store();
		phpRunFFE.store();
		return super.performOk();
	}

	protected Control createContents(Composite parent) {
		initializeDialogUnits(parent);
		Composite composite = new Composite(parent, SWT.LEFT);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		phpRunFFE = new FileFieldEditor(ExternalToolsPlugin.PHP_RUN_PREF,
				PHPPreferencesMessages
						.getString("PHPBasePreferencePage.console.php"),
				composite);
		phpRunFFE.setPage(this);
		phpRunFFE.setPreferenceStore(getPreferenceStore());
		phpRunFFE.load();

		externalParserSFE = new StringFieldEditor(
				ExternalToolsPlugin.EXTERNAL_PARSER_PREF,
				PHPPreferencesMessages
						.getString("PHPBasePreferencePage.parsers.extcommand"),
				composite);
		externalParserSFE.setPage(this);
		externalParserSFE.setPreferenceStore(getPreferenceStore());
		externalParserSFE.load();
		new Label(composite, SWT.NONE);

		composite.setLayout(new GridLayout(3, false));
		return composite;
	}
}