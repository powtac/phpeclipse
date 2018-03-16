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

package net.sourceforge.phpdt.internal.ui.text.spelling;

import java.net.URL;

import net.sourceforge.phpdt.internal.ui.text.phpdoc.IJavaDocTagConstants;
import net.sourceforge.phpdt.internal.ui.text.spelling.engine.AbstractSpellDictionary;

/**
 * Dictionary for Javadoc tags.
 * 
 * @since 3.0
 */
public class JavaDocTagDictionary extends AbstractSpellDictionary implements
		IJavaDocTagConstants {

	/*
	 * @see net.sourceforge.phpdt.internal.ui.text.spelling.engine.AbstractSpellDictionary#getName()
	 */
	protected final URL getURL() {
		return null;
	}

	/*
	 * @see net.sourceforge.phpdt.internal.ui.text.spelling.engine.ISpellDictionary#isCorrect(java.lang.String)
	 */
	public boolean isCorrect(final String word) {

		if (word.charAt(0) == JAVADOC_TAG_PREFIX)
			return super.isCorrect(word);

		return false;
	}

	/*
	 * @see net.sourceforge.phpdt.ui.text.spelling.engine.AbstractSpellDictionary#load(java.net.URL)
	 */
	protected boolean load(final URL url) {

		unload();

		for (int index = 0; index < JAVADOC_LINK_TAGS.length; index++)
			hashWord(JAVADOC_LINK_TAGS[index]);

		for (int index = 0; index < JAVADOC_ROOT_TAGS.length; index++)
			hashWord(JAVADOC_ROOT_TAGS[index]);

		for (int index = 0; index < JAVADOC_PARAM_TAGS.length; index++)
			hashWord(JAVADOC_PARAM_TAGS[index]);

		return true;
	}
}
