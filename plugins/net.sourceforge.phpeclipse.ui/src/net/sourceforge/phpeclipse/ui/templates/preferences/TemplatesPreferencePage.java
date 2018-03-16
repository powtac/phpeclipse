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
package net.sourceforge.phpeclipse.ui.templates.preferences;

import net.sourceforge.phpeclipse.ui.WebUI;

import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;

/**
 * @see org.eclipse.jface.preference.PreferencePage
 */
public class TemplatesPreferencePage extends TemplatePreferencePage implements
		IWorkbenchPreferencePage {

	public TemplatesPreferencePage() {
		setPreferenceStore(WebUI.getDefault().getPreferenceStore());
		setTemplateStore(WebUI.getDefault().getTemplateStore());
		setContextTypeRegistry(WebUI.getDefault().getContextTypeRegistry());
	}

	protected boolean isShowFormatterSetting() {
		return false;
	}

	public boolean performOk() {
		boolean ok = super.performOk();

		WebUI.getDefault().savePluginPreferences();

		return ok;
	}
}
