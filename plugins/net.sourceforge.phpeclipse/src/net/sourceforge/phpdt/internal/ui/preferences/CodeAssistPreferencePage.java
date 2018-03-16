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

package net.sourceforge.phpdt.internal.ui.preferences;

import net.sourceforge.phpdt.internal.ui.IJavaHelpContextIds;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

/**
 * Code Assist preference page.
 * <p>
 * Note: Must be public since it is referenced from plugin.xml
 * </p>
 * 
 * @since 3.0
 */
public class CodeAssistPreferencePage extends
		AbstractConfigurationBlockPreferencePage {

	/*
	 * @see net.sourceforge.phpdt.internal.ui.preferences.AbstractConfigureationBlockPreferencePage#getHelpId()
	 */
	protected String getHelpId() {
		return IJavaHelpContextIds.JAVA_EDITOR_PREFERENCE_PAGE;
	}

	/*
	 * @see net.sourceforge.phpdt.internal.ui.preferences.AbstractConfigurationBlockPreferencePage#setDescription()
	 */
	protected void setDescription() {
		// This page has no description
	}

	/*
	 * @see net.sourceforge.phpdt.internal.ui.preferences.AbstractConfigurationBlockPreferencePage#setPreferenceStore()
	 */
	protected void setPreferenceStore() {
		setPreferenceStore(PHPeclipsePlugin.getDefault().getPreferenceStore());
	}

	/*
	 * @see net.sourceforge.phpdt.internal.ui.preferences.AbstractConfigureationBlockPreferencePage#createConfigurationBlock(net.sourceforge.phpdt.internal.ui.preferences.OverlayPreferenceStore)
	 */
	protected IPreferenceConfigurationBlock createConfigurationBlock(
			OverlayPreferenceStore overlayPreferenceStore) {
		return new CodeAssistConfigurationBlock(this, overlayPreferenceStore);
	}
}
