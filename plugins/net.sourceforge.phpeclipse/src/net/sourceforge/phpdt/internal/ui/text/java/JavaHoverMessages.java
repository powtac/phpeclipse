/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.phpdt.internal.ui.text.java;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

class JavaHoverMessages {

	private static final String RESOURCE_BUNDLE = JavaHoverMessages.class
			.getName();

	private static ResourceBundle fgResourceBundle = ResourceBundle
			.getBundle(RESOURCE_BUNDLE);

	private JavaHoverMessages() {
	}

	public static String getString(String key) {
		try {
			return fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
		}
	}

	/**
	 * Gets a string from the resource bundle and formats it with the argument
	 * 
	 * @param key
	 *            the string used to get the bundle value, must not be null
	 * @since 3.0
	 */
	public static String getFormattedString(String key, Object arg) {
		String format = null;
		try {
			format = fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
		}
		if (arg == null)
			arg = ""; //$NON-NLS-1$
		return MessageFormat.format(format, new Object[] { arg });
	}

	/**
	 * Gets a string from the resource bundle and formats it with the arguments
	 * 
	 * @param key
	 *            the string used to get the bundle value, must not be null
	 * @since 3.0
	 */
	public static String getFormattedString(String key, Object arg1, Object arg2) {
		String format = null;
		try {
			format = fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
		}
		if (arg1 == null)
			arg1 = ""; //$NON-NLS-1$
		if (arg2 == null)
			arg2 = ""; //$NON-NLS-1$
		return MessageFormat.format(format, new Object[] { arg1, arg2 });
	}

	/**
	 * Gets a string from the resource bundle and formats it with the argument
	 * 
	 * @param key
	 *            the string used to get the bundle value, must not be null
	 * @since 3.0
	 */
	public static String getFormattedString(String key, boolean arg) {
		String format = null;
		try {
			format = fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
		}
		return MessageFormat.format(format, new Object[] { new Boolean(arg) });
	}
}
