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

public class PHPStartMySQLAction extends PHPStartApacheAction {
	public void run(IAction action) {
		final IPreferenceStore store = ExternalToolsPlugin.getDefault()
				.getPreferenceStore();
		// execute(store.getString(PHPeclipsePlugin.MYSQL_PREF), "Start MySQL:
		// ");
		execute("mysql_start", store
				.getString(ExternalToolsPlugin.MYSQL_RUN_PREF), store
				.getString(ExternalToolsPlugin.MYSQL_PREF), store
				.getBoolean(ExternalToolsPlugin.MYSQL_START_BACKGROUND));
	}
}
