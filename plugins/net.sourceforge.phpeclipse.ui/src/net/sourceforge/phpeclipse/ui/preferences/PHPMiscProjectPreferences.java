package net.sourceforge.phpeclipse.ui.preferences;

import net.sourceforge.phpeclipse.ui.IPreferenceConstants;
import net.sourceforge.phpeclipse.ui.WebUI;
import net.sourceforge.phpeclipse.ui.overlaypages.FieldEditorOverlayPage;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PathEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * 
 * This page will be added to the project's property page dialog when the
 * "Properties..." popup menu item is selected
 */
public class PHPMiscProjectPreferences extends FieldEditorOverlayPage implements
		IWorkbenchPreferencePage, IMiscProjectPreferences {

	public final static String PREF_ID = "net.sourceforge.phpeclipse.preferences.PHPMiscProjectPreferences";

	public PHPMiscProjectPreferences() {
		super(GRID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bdaum.overlayPages.FieldEditorOverlayPage#getPageId()
	 */
	protected String getPageId() {
		return PREF_ID;
	}

	protected void createFieldEditors() {
		Composite composite = getFieldEditorParent();

		addField(new StringFieldEditor(IPreferenceConstants.PHP_LOCALHOST_PREF,
				PHPPreferencesMessages
						.getString("PHPMiscProjectPreferences.localhost"),
				composite));
		// addField(new
		// StringFieldEditor(IPreferenceConstants.PHP_BOOKMARK_DEFAULT,
		// PHPPreferencesMessages.getString("PHPMiscProjectPreferences.bookmark"),
		// composite));
		addField(new StringFieldEditor(
				IPreferenceConstants.PHP_DOCUMENTROOT_PREF,
				PHPPreferencesMessages
						.getString("PHPMiscProjectPreferences.documentroot"),
				composite));

		PathEditor pe = new PathEditor(IPreferenceConstants.PHP_INCLUDE_PATHS,
				PHPPreferencesMessages
						.getString("PHPMiscProjectPreferences.include_paths"),
				"Choose Path...", composite);
		addField(pe);

		// if (!isPropertyPage)) {
		//
		// }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#doGetPreferenceStore()
	 */
	protected IPreferenceStore doGetPreferenceStore() {
		return WebUI.getDefault().getPreferenceStore();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		setDescription("Default entries for PHP projects.");
	}
}