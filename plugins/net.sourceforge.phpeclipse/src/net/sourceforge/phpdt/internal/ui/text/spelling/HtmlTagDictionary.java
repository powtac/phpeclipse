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

import net.sourceforge.phpdt.internal.ui.text.phpdoc.IHtmlTagConstants;
import net.sourceforge.phpdt.internal.ui.text.spelling.engine.AbstractSpellDictionary;

/**
 * Dictionary for html tags.
 * 
 * @since 3.0
 */
public class HtmlTagDictionary extends AbstractSpellDictionary implements
		IHtmlTagConstants {

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

		if (word.charAt(0) == HTML_TAG_PREFIX)
			return super.isCorrect(word);

		return false;
	}

	/*
	 * @see net.sourceforge.phpdt.ui.text.spelling.engine.AbstractSpellDictionary#load(java.net.URL)
	 */
	protected boolean load(final URL url) {

		unload();

		for (int index = 0; index < HTML_GENERAL_TAGS.length; index++) {

			hashWord(HTML_TAG_PREFIX + HTML_GENERAL_TAGS[index]
					+ HTML_TAG_POSTFIX);
			hashWord(HTML_CLOSE_PREFIX + HTML_GENERAL_TAGS[index]
					+ HTML_TAG_POSTFIX);
		}
		return true;
	}
}
