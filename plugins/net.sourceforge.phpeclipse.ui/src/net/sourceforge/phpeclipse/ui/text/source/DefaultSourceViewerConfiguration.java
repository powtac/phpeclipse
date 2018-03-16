/*
 * Copyright (c) 2002-2004 Widespace, OU and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://solareclipse.sourceforge.net/legal/cpl-v10.html
 * 
 * Contributors:
 *     Igor Malinin - initial contribution
 * 
 * $Id: DefaultSourceViewerConfiguration.java,v 1.2 2006-10-21 23:13:54 pombredanne Exp $
 */

package net.sourceforge.phpeclipse.ui.text.source;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

/**
 * @author Igor Malinin
 */
public class DefaultSourceViewerConfiguration extends SourceViewerConfiguration {

	/** Preference key used to look up display tab width. */
	public static final String PREFERENCE_TAB_WIDTH = "net.sourceforge.phpeclipse.ui.editor.tabWidth"; //$NON-NLS-1$

	/** Preference key for inserting spaces rather than tabs. */
	public static final String PREFERENCE_SPACES_FOR_TABS = "net.sourceforge.phpeclipse.ui.editor.spacesForTabs"; //$NON-NLS-1$

	private IPreferenceStore store;

	public DefaultSourceViewerConfiguration(IPreferenceStore store) {
		this.store = store;
	}

	/*
	 * @see SourceViewerConfiguration#getIndentPrefixes(ISourceViewer, String)
	 */
	public String[] getIndentPrefixes(ISourceViewer sourceViewer,
			String contentType) {
		// prefix[0] is either '\t' or ' ' x tabWidth, depending on useSpaces

		int tabWidth = store.getInt(PREFERENCE_TAB_WIDTH);
		boolean useSpaces = store.getBoolean(PREFERENCE_SPACES_FOR_TABS);

		String[] prefixes = new String[tabWidth + 1];

		for (int i = 0; i <= tabWidth; i++) {
			StringBuffer prefix = new StringBuffer(tabWidth - 1);

			if (useSpaces) {
				for (int j = 0; j + i < tabWidth; j++) {
					prefix.append(' ');
				}

				if (i != 0) {
					prefix.append('\t');
				}
			} else {
				for (int j = 0; j < i; j++) {
					prefix.append(' ');
				}

				if (i != tabWidth) {
					prefix.append('\t');
				}
			}

			prefixes[i] = prefix.toString();
		}

		prefixes[tabWidth] = ""; //$NON-NLS-1$

		return prefixes;
	}
}
