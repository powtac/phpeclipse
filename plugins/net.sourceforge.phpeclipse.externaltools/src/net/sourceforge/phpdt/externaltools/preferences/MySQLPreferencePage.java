package net.sourceforge.phpdt.externaltools.preferences;

import net.sourceforge.phpeclipse.externaltools.ExternalToolsPlugin;

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

public class MySQLPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	StringFieldEditor mySQLCommandSFE;

	FileFieldEditor mysqlRunFFE;

	BooleanFieldEditor mysqlStartBFE;

	public MySQLPreferencePage() {
		super();
		setPreferenceStore(ExternalToolsPlugin.getDefault()
				.getPreferenceStore());
	}

	protected Control createContents(Composite parent) {
		initializeDialogUnits(parent);
		Composite composite = new Composite(parent, SWT.LEFT);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		composite.setLayout(new GridLayout());

		mysqlStartBFE = new BooleanFieldEditor(
				ExternalToolsPlugin.MYSQL_START_BACKGROUND,
				PHPPreferencesMessages
						.getString("PHPBasePreferencePage.mySQLGroup.start_background"),
				composite);
		mysqlStartBFE.setPage(this);
		mysqlStartBFE.setPreferenceStore(getPreferenceStore());
		mysqlStartBFE.load();
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);

		mySQLCommandSFE = new StringFieldEditor(ExternalToolsPlugin.MYSQL_PREF,
				PHPPreferencesMessages
						.getString("PHPBasePreferencePage.mySQLGroup.command"),
				composite);
		mySQLCommandSFE.setPage(this);
		mySQLCommandSFE.setPreferenceStore(getPreferenceStore());
		mySQLCommandSFE.load();
		new Label(composite, SWT.NONE);

		mysqlRunFFE = new FileFieldEditor(ExternalToolsPlugin.MYSQL_RUN_PREF,
				PHPPreferencesMessages
						.getString("PHPBasePreferencePage.mySQLGroup.run"),
				composite);
		mysqlRunFFE.setPage(this);
		mysqlRunFFE.setPreferenceStore(getPreferenceStore());
		mysqlRunFFE.load();

		return composite;
	}

	public void init(IWorkbench workbench) {
	}

	protected void performDefaults() {
		mySQLCommandSFE.loadDefault();
		mysqlRunFFE.loadDefault();
		mysqlStartBFE.loadDefault();
		super.performDefaults();
	}

	public boolean performOk() {
		mySQLCommandSFE.store();
		mysqlRunFFE.store();
		mysqlStartBFE.store();
		return super.performOk();
	}

}
