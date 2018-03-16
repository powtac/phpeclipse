package net.sourceforge.phpeclipse.phpmanual;


import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class PHPManualUiMessages {

	private static final String BUNDLE_NAME = PHPManualUiMessages.class
			.getName();

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	private PHPManualUiMessages() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	public static String getFormattedString(String key, Object arg) {
		return MessageFormat.format(getString(key), new Object[] { arg });
	}

	public static String getFormattedString(String key, Object[] args) {
		return MessageFormat.format(getString(key), args);
	}
}