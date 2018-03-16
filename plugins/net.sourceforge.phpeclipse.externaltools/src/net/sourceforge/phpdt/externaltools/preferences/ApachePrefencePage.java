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

public class ApachePrefencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	StringFieldEditor apacheStartSFE;

	StringFieldEditor apacheStopSFE;

	StringFieldEditor apacheRestartSFE;

	FileFieldEditor apacheRunFFE;

	FileFieldEditor httpdConfFFE;

	FileFieldEditor etcHostsFFE;

	BooleanFieldEditor apacheStartBFE;

	BooleanFieldEditor apacheStopBFE;

	BooleanFieldEditor apacheRestartBFE;

	public ApachePrefencePage() {
		super();
		setPreferenceStore(ExternalToolsPlugin.getDefault()
				.getPreferenceStore());
	}

	protected Control createContents(Composite parent) {
		initializeDialogUnits(parent);
		Composite composite = new Composite(parent, SWT.LEFT);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		composite.setLayout(new GridLayout());

		apacheStartSFE = new StringFieldEditor(
				ExternalToolsPlugin.APACHE_START_PREF, PHPPreferencesMessages
						.getString("PHPBasePreferencePage.apacheGroup.start"),
				composite);
		apacheStartSFE.setPage(this);
		apacheStartSFE.setPreferenceStore(getPreferenceStore());
		apacheStartSFE.load();
		new Label(composite, SWT.NONE);

		new Label(composite, SWT.NONE);
		apacheStartBFE = new BooleanFieldEditor(
				ExternalToolsPlugin.APACHE_START_BACKGROUND,
				PHPPreferencesMessages
						.getString("PHPBasePreferencePage.apacheGroup.start_background"),
				composite);
		apacheStartBFE.setPage(this);
		apacheStartBFE.setPreferenceStore(getPreferenceStore());
		apacheStartBFE.load();
		new Label(composite, SWT.NONE);

		apacheStopSFE = new StringFieldEditor(
				ExternalToolsPlugin.APACHE_STOP_PREF, PHPPreferencesMessages
						.getString("PHPBasePreferencePage.apacheGroup.stop"),
				composite);
		apacheStopSFE.setPage(this);
		apacheStopSFE.setPreferenceStore(getPreferenceStore());
		apacheStopSFE.load();
		new Label(composite, SWT.NONE);

		new Label(composite, SWT.NONE);
		apacheStopBFE = new BooleanFieldEditor(
				ExternalToolsPlugin.APACHE_STOP_BACKGROUND,
				PHPPreferencesMessages
						.getString("PHPBasePreferencePage.apacheGroup.stop_background"),
				composite);
		apacheStopBFE.setPage(this);
		apacheStopBFE.setPreferenceStore(getPreferenceStore());
		apacheStopBFE.load();
		new Label(composite, SWT.NONE);

		apacheRestartSFE = new StringFieldEditor(
				ExternalToolsPlugin.APACHE_RESTART_PREF,
				PHPPreferencesMessages
						.getString("PHPBasePreferencePage.apacheGroup.restart"),
				composite);
		apacheRestartSFE.setPage(this);
		apacheRestartSFE.setPreferenceStore(getPreferenceStore());
		apacheRestartSFE.load();
		new Label(composite, SWT.NONE);

		new Label(composite, SWT.NONE);
		apacheRestartBFE = new BooleanFieldEditor(
				ExternalToolsPlugin.APACHE_RESTART_BACKGROUND,
				PHPPreferencesMessages
						.getString("PHPBasePreferencePage.apacheGroup.restart_background"),
				composite);
		apacheRestartBFE.setPage(this);
		apacheRestartBFE.setPreferenceStore(getPreferenceStore());
		apacheRestartBFE.load();
		new Label(composite, SWT.NONE);

		apacheRunFFE = new FileFieldEditor(ExternalToolsPlugin.APACHE_RUN_PREF,
				PHPPreferencesMessages
						.getString("PHPBasePreferencePage.apacheGroup.run"),
				composite);
		apacheRunFFE.setPage(this);
		apacheRunFFE.setPreferenceStore(getPreferenceStore());
		apacheRunFFE.load();

		httpdConfFFE = new FileFieldEditor(
				ExternalToolsPlugin.HTTPD_CONF_PATH_PREF,
				"Path to httpd.conf:", composite);
		httpdConfFFE.setPage(this);
		httpdConfFFE.setPreferenceStore(getPreferenceStore());
		httpdConfFFE.load();

		etcHostsFFE = new FileFieldEditor(
				ExternalToolsPlugin.ETC_HOSTS_PATH_PREF, "Path to etc/hosts:",
				composite);
		etcHostsFFE.setPage(this);
		etcHostsFFE.setPreferenceStore(getPreferenceStore());
		etcHostsFFE.load();

		return composite;
	}

	public void init(IWorkbench workbench) {
	}

	protected void performDefaults() {
		apacheStartSFE.loadDefault();
		apacheStopSFE.loadDefault();
		apacheRestartSFE.loadDefault();
		apacheRunFFE.loadDefault();
		httpdConfFFE.loadDefault();
		etcHostsFFE.loadDefault();
		apacheStartBFE.loadDefault();
		apacheStopBFE.loadDefault();
		apacheRestartBFE.loadDefault();
		super.performDefaults();
	}

	public boolean performOk() {
		apacheStartSFE.store();
		apacheStopSFE.store();
		apacheRestartSFE.store();
		apacheRunFFE.store();
		httpdConfFFE.store();
		etcHostsFFE.store();
		apacheStartBFE.store();
		apacheStopBFE.store();
		apacheRestartBFE.store();
		return super.performOk();
	}

}
