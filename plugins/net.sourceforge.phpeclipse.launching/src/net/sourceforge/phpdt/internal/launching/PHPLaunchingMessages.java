package net.sourceforge.phpdt.internal.launching;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class PHPLaunchingMessages {

	private static final String BUNDLE_NAME = PHPLaunchingMessages.class
			.getName();

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	private PHPLaunchingMessages() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
