/*******************************************************************************
 * Copyright (c) 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package net.sourceforge.phpdt.internal.ui.actions;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Class that gives access to the action messages resource bundle.
 */
public class ActionMessages {

	private static final String BUNDLE_NAME = "net.sourceforge.phpdt.internal.ui.actions.ActionMessages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	private ActionMessages() {
		// no instance
	}

	/**
	 * Returns the resource string associated with the given key in the resource
	 * bundle. If there isn't any value under the given key, the key is
	 * returned.
	 * 
	 * @param key
	 *            the resource key
	 * @return the string
	 */
	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	/**
	 * Returns the resource bundle managed by the receiver.
	 * 
	 * @return the resource bundle
	 * @since 3.0
	 */
	public static ResourceBundle getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

	/**
	 * Returns the formatted resource string associated with the given key in
	 * the resource bundle. <code>MessageFormat</code> is used to format the
	 * message. If there isn't any value under the given key, the key is
	 * returned.
	 * 
	 * @param key
	 *            the resource key
	 * @param arg
	 *            the message argument
	 * @return the string
	 */
	public static String getFormattedString(String key, Object arg) {
		return getFormattedString(key, new Object[] { arg });
	}

	/**
	 * Returns the formatted resource string associated with the given key in
	 * the resource bundle. <code>MessageFormat</code> is used to format the
	 * message. If there isn't any value under the given key, the key is
	 * returned.
	 * 
	 * @param key
	 *            the resource key
	 * @param args
	 *            the message arguments
	 * @return the string
	 */
	public static String getFormattedString(String key, Object[] args) {
		return MessageFormat.format(getString(key), args);
	}
}