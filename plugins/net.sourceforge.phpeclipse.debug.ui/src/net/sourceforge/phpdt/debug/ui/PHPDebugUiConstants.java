package net.sourceforge.phpdt.debug.ui;

import net.sourceforge.phpdt.internal.debug.ui.PHPDebugUiPlugin;

public interface PHPDebugUiConstants {
	public static final String DEFAULT_WORKING_DIRECTORY = PHPDebugUiPlugin
			.getWorkspace().getRoot().getLocation().toString();

	public static final String PREFERENCE_KEYWORDS = PHPDebugUiPlugin.PLUGIN_ID
			+ ".preference_keywords";
}
