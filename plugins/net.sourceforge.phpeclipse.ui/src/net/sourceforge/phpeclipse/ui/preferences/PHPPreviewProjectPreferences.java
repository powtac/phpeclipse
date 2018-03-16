package net.sourceforge.phpeclipse.ui.preferences;

import net.sourceforge.phpeclipse.ui.IPreferenceConstants;
import net.sourceforge.phpeclipse.ui.WebUI;
import net.sourceforge.phpeclipse.ui.overlaypages.FieldEditorOverlayPage;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * 
 * This page will be added to the project's property page dialog when the
 * "Properties..." popup menu item is selected
 */
public class PHPPreviewProjectPreferences extends FieldEditorOverlayPage
		implements IWorkbenchPreferencePage {
	public final static String PREF_ID = "net.sourceforge.phpeclipse.preferences.PHPPreviewProjectPreferences";

	public PHPPreviewProjectPreferences() {
		super(GRID, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bdaum.overlayPages.FieldEditorOverlayPage#getPageId()
	 */
	protected String getPageId() {
		return PREF_ID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {
		Composite composite = getFieldEditorParent();
		addField(new BooleanFieldEditor(
				IPreferenceConstants.PHP_AUTO_PREVIEW_DEFAULT,
				PHPPreferencesMessages
						.getString("PHPPreviewProjectPreferences.auto_preview"),
				composite));
		addField(new BooleanFieldEditor(
				IPreferenceConstants.PHP_BRING_TO_TOP_PREVIEW_DEFAULT,
				PHPPreferencesMessages
						.getString("PHPPreviewProjectPreferences.bring_to_top_preview"),
				composite));
		addField(new BooleanFieldEditor(
				IPreferenceConstants.PHP_STICKY_BROWSER_URL_DEFAULT,
				PHPPreferencesMessages
						.getString("PHPPreviewProjectPreferences.sticky_browser_url"),
				composite));
		// addField(new BooleanFieldEditor(
		// IPreferenceConstants.PHP_SHOW_HTML_FILES_LOCAL,
		// PHPPreferencesMessages
		// .getString("PHPPreviewProjectPreferences.show_html_files_local"),
		// composite));
		// addField(new BooleanFieldEditor(
		// IPreferenceConstants.PHP_SHOW_XML_FILES_LOCAL, PHPPreferencesMessages
		// .getString("PHPPreviewProjectPreferences.show_xml_files_local"),
		// composite));
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
		setDescription("Default entries for Previewer.");
	}
}