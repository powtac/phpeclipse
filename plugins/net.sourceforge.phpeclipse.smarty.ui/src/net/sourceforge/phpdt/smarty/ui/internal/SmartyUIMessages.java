/*
 * Copyright (c) 2004 Christopher Lenz and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Christopher Lenz - initial API and implementation
 * 
 * $Id: SmartyUIMessages.java,v 1.2 2006-10-21 23:19:32 pombredanne Exp $
 */

package net.sourceforge.phpdt.smarty.ui.internal;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility class that provides easy access to externalized strings.
 */
public final class SmartyUIMessages {

	// Constants ---------------------------------------------------------------

	/**
	 * Qualified name of the resource bundle containing the localized messages.
	 */
	private static final String RESOURCE_BUNDLE = "net.sourceforge.phpdt.smarty.ui.internal.SmartyUIMessages"; //$NON-NLS-1$

	// Class Variables ---------------------------------------------------------

	/**
	 * The resource bundle.
	 */
	private static ResourceBundle resourceBundle = ResourceBundle
			.getBundle(RESOURCE_BUNDLE);

	// Constructors ------------------------------------------------------------

	/**
	 * Hidden constructor.
	 */
	private SmartyUIMessages() {
		// Hidden
	}

	// Public Methods ----------------------------------------------------------

	/**
	 * Returns the resource bundle.
	 * 
	 * @return the resource bundle
	 */
	public static ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	/**
	 * Returns the message identified by the specified key.
	 * 
	 * @param key
	 *            the message key
	 * @return the localized message, or the key enclosed by exclamation marks
	 *         if no message was found for the key
	 */
	public static String getString(String key) {
		try {
			return resourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!"; //$NON-NLS-2$ //$NON-NLS-1$
		}
	}

	/**
	 * Returns the message identified by the specified key, replacing a single
	 * parameter with the provided value.
	 * 
	 * @param key
	 *            the message key
	 * @param arg
	 *            the parameter value
	 * @return the formatted string, or the key enclosed by exclamation marks if
	 *         no message was found for the key
	 */
	public static String getString(String key, String arg) {
		return getString(key, new String[] { arg });
	}

	/**
	 * Returns the message identified by the specified key, replacing all
	 * parameters with the provided values.
	 * 
	 * @param key
	 *            the message key
	 * @param args
	 *            the parameter values
	 * @return the formatted string, or the key enclosed by exclamation marks if
	 *         no message was found for the key
	 */
	public static String getString(String key, String[] args) {
		return MessageFormat.format(getString(key), args);
	}

}
