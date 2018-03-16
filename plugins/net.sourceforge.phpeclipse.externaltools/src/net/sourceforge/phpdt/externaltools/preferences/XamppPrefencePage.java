package net.sourceforge.phpdt.externaltools.preferences;

import net.sourceforge.phpeclipse.externaltools.ExternalToolsPlugin;

import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class XamppPrefencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	FileFieldEditor xamppStartSFE;

	FileFieldEditor xamppStopSFE;

	public XamppPrefencePage() {
		super();
		setPreferenceStore(ExternalToolsPlugin.getDefault()
				.getPreferenceStore());
	}

	protected Control createContents(Composite parent) {
		initializeDialogUnits(parent);
		Composite composite = new Composite(parent, SWT.LEFT);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		composite.setLayout(new GridLayout());

		xamppStartSFE = new FileFieldEditor(
				ExternalToolsPlugin.XAMPP_START_PREF,
				PHPPreferencesMessages
						.getString("PHPBasePreferencePage.apacheGroup.xampp_start"),
				composite);
		xamppStartSFE.setPage(this);
		xamppStartSFE.setPreferenceStore(getPreferenceStore());
		xamppStartSFE.load();

		xamppStopSFE = new FileFieldEditor(
				ExternalToolsPlugin.XAMPP_STOP_PREF,
				PHPPreferencesMessages
						.getString("PHPBasePreferencePage.apacheGroup.xampp_stop"),
				composite);
		xamppStopSFE.setPage(this);
		xamppStopSFE.setPreferenceStore(getPreferenceStore());
		xamppStopSFE.load();
		return composite;
	}

	public void init(IWorkbench workbench) {
	}

	protected void performDefaults() {
		xamppStartSFE.loadDefault();
		xamppStopSFE.loadDefault();
		super.performDefaults();
	}

	public boolean performOk() {
		xamppStartSFE.store();
		xamppStopSFE.store();
		return super.performOk();
	}

}
