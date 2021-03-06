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
package net.sourceforge.phpdt.internal.ui.text.template.preferences;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

class TemplatePreferencesMessages {

	private static final String RESOURCE_BUNDLE = TemplatePreferencesMessages.class
			.getName();

	private static ResourceBundle fgResourceBundle = ResourceBundle
			.getBundle(RESOURCE_BUNDLE);

	private TemplatePreferencesMessages() {
	}

	public static String getString(String key) {
		try {
			return fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	/**
	 * Gets a string from the resource bundle and formats it with the argument
	 * 
	 * @param key
	 *            the string used to get the bundle value, must not be null
	 */
	public static String getFormattedString(String key, Object arg) {
		return MessageFormat.format(getString(key), new Object[] { arg });
	}

	/**
	 * Gets a string from the resource bundle and formats it with arguments
	 */
	public static String getFormattedString(String key, Object[] args) {
		return MessageFormat.format(getString(key), args);
	}
}
