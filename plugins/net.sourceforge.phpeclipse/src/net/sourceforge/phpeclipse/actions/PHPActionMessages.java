/*
 * (c) Copyright Improve S.A., 2002.
 * All Rights Reserved.
 */
package net.sourceforge.phpeclipse.actions;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class PHPActionMessages {

	private static final String RESOURCE_BUNDLE = "net.sourceforge.phpeclipse.actions.PHPActionMessages";//$NON-NLS-1$

	private static ResourceBundle fgResourceBundle = ResourceBundle
			.getBundle(RESOURCE_BUNDLE);

	private PHPActionMessages() {
	}

	public static String getString(String key) {
		try {
			return fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
		}
	}

	public static ResourceBundle getResourceBundle() {
		return fgResourceBundle;
	}
}
