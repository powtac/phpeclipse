/**********************************************************************
 Copyright (c) 2000, 2002 IBM Corp. and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html

 Contributors:
 IBM Corporation - Initial implementation
 www.phpeclipse.de
 **********************************************************************/
package net.sourceforge.phpdt.externaltools.actions;

import net.sourceforge.phpeclipse.externaltools.ExternalToolsPlugin;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;

public class PHPStopApacheAction extends PHPStartApacheAction {
	public void run(IAction action) {
		final IPreferenceStore store = ExternalToolsPlugin.getDefault()
				.getPreferenceStore();
		// execute(store.getString(PHPeclipsePlugin.APACHE_STOP_PREF), "Stop
		// Apache: ");
		execute("apache_stop", store
				.getString(ExternalToolsPlugin.APACHE_RUN_PREF), store
				.getString(ExternalToolsPlugin.APACHE_STOP_PREF), store
				.getBoolean(ExternalToolsPlugin.APACHE_STOP_BACKGROUND));
	}
}
