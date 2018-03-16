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

package net.sourceforge.phpdt.internal.ui.preferences;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.phpdt.core.IJavaProject;
import net.sourceforge.phpdt.internal.ui.wizards.IStatusChangeListener;
import net.sourceforge.phpdt.ui.PreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

/**
 * Options configuration block for editor related settings.
 * 
 * @since 3.0
 */
public class EditorConfigurationBlock extends OptionsConfigurationBlock {

	/** Preference keys for the preferences in this block */
	private static final String PREF_EDITOR_SAVE_ON_BLUR = PreferenceConstants.EDITOR_SAVE_ON_BLUR;

	private static final String PREF_EDITOR_P_RTRIM_ON_SAVE = PreferenceConstants.EDITOR_P_RTRIM_ON_SAVE;
	
	/**
	 * Creates a new editor configuration block.
	 * 
	 * @param context
	 *            The status change listener
	 * @param project
	 *            The Java project
	 */
	public EditorConfigurationBlock(final IStatusChangeListener context,
			final IJavaProject project) {
		super(context, project, getAllKeys());
	}

	/*
	 * @see net.sourceforge.phpdt.internal.ui.preferences.OptionsConfigurationBlock#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(final Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);

		final String[] trueFalse = new String[] { IPreferenceStore.TRUE,
				IPreferenceStore.FALSE };

		Group user = new Group(composite, SWT.NONE);
		user.setText(PreferencesMessages
				.getString("EditorPreferencePage.file.title")); //$NON-NLS-1$
		user.setLayout(new GridLayout());
		user.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		String label = PreferencesMessages
				.getString("EditorPreferencePage.save_on_blur"); //$NON-NLS-1$
		addCheckBox(user, label, PREF_EDITOR_SAVE_ON_BLUR, trueFalse, 0);

		label = PreferencesMessages
				.getString("EditorPreferencePage.p_rtrim_on_save"); //$NON-NLS-1$
		addCheckBox(user, label, PREF_EDITOR_P_RTRIM_ON_SAVE, trueFalse, 0);

		return composite;
	}

	private static String[] getAllKeys() {
		return new String[] { PREF_EDITOR_SAVE_ON_BLUR,
				PREF_EDITOR_P_RTRIM_ON_SAVE };
	}

	/*
	 * @see net.sourceforge.phpdt.internal.ui.preferences.OptionsConfigurationBlock#getDefaultOptions()
	 */
	protected Map getDefaultOptions() {

		final String[] keys = fAllKeys;
		final Map options = new HashMap();
		final IPreferenceStore store = PreferenceConstants.getPreferenceStore();

		for (int index = 0; index < keys.length; index++)
			options.put(keys[index], store.getDefaultString(keys[index]));

		return options;
	}

	/*
	 * @see net.sourceforge.phpdt.internal.ui.preferences.OptionsConfigurationBlock#getFullBuildDialogStrings(boolean)
	 */
	protected final String[] getFullBuildDialogStrings(final boolean workspace) {
		return null;
	}

	/*
	 * @see net.sourceforge.phpdt.internal.ui.preferences.OptionsConfigurationBlock#getOptions(boolean)
	 */
	protected Map getOptions(final boolean inherit) {

		final String[] keys = fAllKeys;
		final Map options = new HashMap();
		final IPreferenceStore store = PreferenceConstants.getPreferenceStore();

		for (int index = 0; index < keys.length; index++)
			options.put(keys[index], store.getString(keys[index]));

		return options;
	}

	/*
	 * @see net.sourceforge.phpdt.internal.ui.preferences.OptionsConfigurationBlock#setOptions(java.util.Map)
	 */
	protected void setOptions(final Map options) {

		final String[] keys = fAllKeys;
		final IPreferenceStore store = PreferenceConstants.getPreferenceStore();

		for (int index = 0; index < keys.length; index++)
			store.setValue(keys[index], (String) fWorkingValues
					.get(keys[index]));
	}

	/*
	 * @see net.sourceforge.phpdt.internal.ui.preferences.OptionsConfigurationBlock#validateSettings(java.lang.String,java.lang.String)
	 */
	protected void validateSettings(final String key, final String value) {
	}
}
