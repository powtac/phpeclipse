package net.sourceforge.phpdt.phphelp;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author jsurfer
 * 
 * 
 */
public class PHPHelpPreferenceMessages {

	private static final String BUNDLE_NAME = "net.sourceforge.phpdt.phphelp.PHPHelpPreferenceMessages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	/**
	 * 
	 */
	private PHPHelpPreferenceMessages() {

		// TODO Auto-generated constructor stub
	}

	/**
	 * @param key
	 * @return
	 */
	public static String getString(String key) {
		// TODO Auto-generated method stub
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
