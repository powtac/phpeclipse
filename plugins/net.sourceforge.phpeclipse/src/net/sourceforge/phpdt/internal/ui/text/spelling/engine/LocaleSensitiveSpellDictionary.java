/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package net.sourceforge.phpdt.internal.ui.text.spelling.engine;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import net.sourceforge.phpdt.internal.ui.PHPUIMessages;

/**
 * Platform wide read-only locale sensitive dictionary for spell-checking.
 * 
 * @since 3.0
 */
public class LocaleSensitiveSpellDictionary extends AbstractSpellDictionary {

	/** The locale of this dictionary */
	private final Locale fLocale;

	/** The location of the dictionaries */
	private final URL fLocation;

	/**
	 * Creates a new locale sensitive spell dictionary.
	 * 
	 * @param locale
	 *            The locale for this dictionary
	 * @param location
	 *            The location of the locale sensitive dictionaries
	 */
	public LocaleSensitiveSpellDictionary(final Locale locale,
			final URL location) {
		fLocation = location;
		fLocale = locale;
	}

	/**
	 * Returns the locale of this dictionary.
	 * 
	 * @return The locale of this dictionary
	 */
	public final Locale getLocale() {
		return fLocale;
	}

	/*
	 * @see net.sourceforge.phpdt.internal.ui.text.spelling.engine.AbstractSpellDictionary#getURL()
	 */
	protected final URL getURL() throws MalformedURLException {
		return new URL(
				fLocation,
				fLocale.toString().toLowerCase()
						+ "." + PHPUIMessages.getString("Spelling.dictionary.file.extension")); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
