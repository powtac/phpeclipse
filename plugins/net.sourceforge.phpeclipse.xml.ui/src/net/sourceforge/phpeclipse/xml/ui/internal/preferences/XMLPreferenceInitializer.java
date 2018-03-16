/*
 * Copyright (c) 2002-2004 Roberto Gonzalez Rocha and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Roberto Gonzalez Rocha - Initial version
 *     Igor Malinin - refactoring, minor changes
 *
 * $Id: XMLPreferenceInitializer.java,v 1.3 2006-10-21 23:14:13 pombredanne Exp $
 */
package net.sourceforge.phpeclipse.xml.ui.internal.preferences;

import net.sourceforge.phpeclipse.ui.preferences.ITextStylePreferences;
import net.sourceforge.phpeclipse.xml.ui.XMLPlugin;
import net.sourceforge.phpeclipse.xml.ui.text.IXMLSyntaxConstants;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * @author Igor Malinin
 */
public class XMLPreferenceInitializer extends AbstractPreferenceInitializer {
	/*
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		final IPreferenceStore store = XMLPlugin.getDefault()
				.getPreferenceStore();
		final Display display = Display.getDefault();

		// TODO: ChainedPreferenceStore does not work for preferences preview

		display.syncExec(new Runnable() {
			public void run() {
				PreferenceConverter.setDefault(store,
						AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND, display
								.getSystemColor(SWT.COLOR_LIST_FOREGROUND)
								.getRGB());
			}
		});

		store.setDefault(
				AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT,
				true);

		display.syncExec(new Runnable() {
			public void run() {
				PreferenceConverter.setDefault(store,
						AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND, display
								.getSystemColor(SWT.COLOR_LIST_BACKGROUND)
								.getRGB());
			}
		});

		store.setDefault(
				AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT,
				true);

		// end of common preferences

		setDefault(store, IXMLSyntaxConstants.XML_DEFAULT, "0,0,0",
				ITextStylePreferences.STYLE_NORMAL);

		setDefault(store, IXMLSyntaxConstants.XML_TAG, "127,0,127",
				ITextStylePreferences.STYLE_NORMAL);

		setDefault(store, IXMLSyntaxConstants.XML_ATT_NAME, "0,127,0",
				ITextStylePreferences.STYLE_NORMAL);

		setDefault(store, IXMLSyntaxConstants.XML_ATT_VALUE, "0,0,255",
				ITextStylePreferences.STYLE_NORMAL);

		setDefault(store, IXMLSyntaxConstants.XML_ENTITY, "127,127,0",
				ITextStylePreferences.STYLE_NORMAL);

		setDefault(store, IXMLSyntaxConstants.XML_CDATA, "127,127,0",
				ITextStylePreferences.STYLE_NORMAL);

		setDefault(store, IXMLSyntaxConstants.XML_PI, "127,127,127",
				ITextStylePreferences.STYLE_BOLD);

		setDefault(store, IXMLSyntaxConstants.XML_COMMENT, "127,0,0",
				ITextStylePreferences.STYLE_NORMAL);

		setDefault(store, IXMLSyntaxConstants.XML_DECL, "127,0,127",
				ITextStylePreferences.STYLE_BOLD);

		setDefault(store, IXMLSyntaxConstants.XML_SMARTY, "255,0,127",
				ITextStylePreferences.STYLE_BOLD);

		setDefault(store, IXMLSyntaxConstants.DTD_CONDITIONAL, "127,127,0",
				ITextStylePreferences.STYLE_BOLD);
	}

	private static void setDefault(IPreferenceStore store, String constant,
			String color, String style) {
		store.setDefault(constant + ITextStylePreferences.SUFFIX_FOREGROUND,
				color);
		store.setDefault(constant + ITextStylePreferences.SUFFIX_STYLE, style);
	}
}
