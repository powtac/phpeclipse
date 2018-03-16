package net.sourceforge.phpdt.internal.launching;

public interface PHPLaunchConfigurationAttribute {
	static final String PHP_LAUNCH_CONFIGURATION_TYPE = "net.sourceforge.phpdt.launching.LaunchConfigurationTypePHPApplication";

	static final String PHP_LAUNCH_PROCESS_TYPE = "net.sourceforge.phpdt.launching.processType";

	static final String CUSTOM_LOAD_PATH = PHPLaunchingPlugin.PLUGIN_ID
			+ ".CUSTOM_LOAD_PATH";

	static final String FILE_NAME = PHPLaunchingPlugin.PLUGIN_ID + ".FILE_NAME";

	static final String INTERPRETER_ARGUMENTS = PHPLaunchingPlugin.PLUGIN_ID
			+ ".INTERPRETER_ARGUMENTS";

	static final String MODULE_NAME = PHPLaunchingPlugin.PLUGIN_ID
			+ ".MODULE_NAME";

	static final String PROGRAM_ARGUMENTS = PHPLaunchingPlugin.PLUGIN_ID
			+ ".PROGRAM_ARGUMENTS";

	static final String PROJECT_NAME = PHPLaunchingPlugin.PLUGIN_ID
			+ ".PROJECT_NAME";

	static final String SELECTED_INTERPRETER = PHPLaunchingPlugin.PLUGIN_ID
			+ ".SELECTED_INTERPRETER";

	static final String WORKING_DIRECTORY = PHPLaunchingPlugin.PLUGIN_ID
			+ ".WORKING_DIRECTORY";

	// static final String USE_DEFAULT_LOAD_PATH = PHPLaunchingPlugin.PLUGIN_ID
	// + ".USE_DEFAULT_LOAD_PATH";
	static final String USE_DEFAULT_WORKING_DIRECTORY = PHPLaunchingPlugin.PLUGIN_ID
			+ ".USE_DEFAULT_WORKING_DIRECTORY";

	static final String REMOTE_DEBUG = PHPLaunchingPlugin.PLUGIN_ID
			+ ".REMOTE_DEBUG";

	static final String REMOTE_DEBUG_TRANSLATE = PHPLaunchingPlugin.PLUGIN_ID
			+ ".REMOTE_DEBUG_TRANSLATE";

	static final String REMOTE_PATH = PHPLaunchingPlugin.PLUGIN_ID
			+ ".REMOTE_PATH";

	static final String OPEN_DBGSESSION_IN_BROWSER = PHPLaunchingPlugin.PLUGIN_ID
			+ ".OPEN_DBGSESSION_IN_BROWSER";

	static final String OPEN_DBGSESSION_IN_EXTERNAL_BROWSER = PHPLaunchingPlugin.PLUGIN_ID
			+ ".OPEN_DBGSESSION_IN_EXTERNAL_BROWSER";

	static final String FILE_MAP = PHPLaunchingPlugin.PLUGIN_ID + ".FILE_MAP";
}