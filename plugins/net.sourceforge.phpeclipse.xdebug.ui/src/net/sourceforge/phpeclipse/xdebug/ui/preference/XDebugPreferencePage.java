package net.sourceforge.phpeclipse.xdebug.ui.preference;

import net.sourceforge.phpeclipse.externaltools.ExternalToolsPlugin;
import net.sourceforge.phpeclipse.xdebug.core.IXDebugPreferenceConstants;
import net.sourceforge.phpeclipse.xdebug.core.XDebugCorePlugin;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


public class XDebugPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	/* Preference page for the default XDebug-Settings */
	
	private IntegerFieldEditor debugPort;

	public XDebugPreferencePage() {
		super(FieldEditorPreferencePage.GRID);

		// Set the preference store for the preference page.
		IPreferenceStore store =
			XDebugCorePlugin.getDefault().getPreferenceStore();
		store.setDefault(IXDebugPreferenceConstants.DEBUGPORT_PREFERENCE,IXDebugPreferenceConstants.DEFAULT_DEBUGPORT);
		// get the default form the externalToolsPlugin 
		String interpreter=ExternalToolsPlugin.getDefault().getPreferenceStore().getString(ExternalToolsPlugin.PHP_RUN_PREF);
		store.setDefault(IXDebugPreferenceConstants.PHP_INTERPRETER_PREFERENCE,interpreter);
		setPreferenceStore(store);
	}

	public void init(IWorkbench workbench) {
		setDescription("Default Entries for XDebug:");
	}

	protected void createFieldEditors() {
		debugPort = new IntegerFieldEditor(IXDebugPreferenceConstants.DEBUGPORT_PREFERENCE, "&Debugport:", getFieldEditorParent(),5);
		debugPort.setValidRange(1025,65535);
		
		debugPort.setErrorMessage("Debugport must be between 1024 and 65536");
		addField(debugPort);
		
		FileFieldEditor phpInterpreter = new FileFieldEditor(IXDebugPreferenceConstants.PHP_INTERPRETER_PREFERENCE, "PHP &Interpreter:",true,getFieldEditorParent());
	    phpInterpreter.setErrorMessage("File not found");
		addField(phpInterpreter);
		
	}
	
	public void performApply() {
		super.performApply();
		//XDebugCorePlugin.getDefault().setProxyPort(debugPort.getIntValue());
	}




}
