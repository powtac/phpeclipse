/***********************************************************************************************************************************
 * Copyright (c) 2000, 2002 IBM Corp. and others. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors: IBM Corporation - Initial implementation www.phpeclipse.de
 **********************************************************************************************************************************/
package net.sourceforge.phpdt.externaltools.actions;

import java.text.MessageFormat;

import net.sourceforge.phpdt.externaltools.launchConfigurations.ExternalToolsUtil;
import net.sourceforge.phpeclipse.externaltools.ExternalToolsPlugin;
import net.sourceforge.phpeclipse.ui.WebUI;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class PHPStartApacheAction implements IWorkbenchWindowActionDelegate {
	protected IWorkbenchWindow activeWindow = null;

	public void run(IAction action) {
		final IPreferenceStore webUIStore = WebUI.getDefault()
				.getPreferenceStore();

		String documentRoot = webUIStore.getString(WebUI.PHP_DOCUMENTROOT_PREF);
		final IPreferenceStore store = ExternalToolsPlugin.getDefault()
				.getPreferenceStore();

		// replace backslash with slash in the DocumentRoot under Windows
		documentRoot = documentRoot.replace('\\', '/');
		String[] arguments = { documentRoot };
		MessageFormat form = new MessageFormat(store
				.getString(ExternalToolsPlugin.APACHE_START_PREF));
		execute("apache_start", store
				.getString(ExternalToolsPlugin.APACHE_RUN_PREF), form
				.format(arguments), store
				.getBoolean(ExternalToolsPlugin.APACHE_START_BACKGROUND));
	}

	/**
	 * Executes an external progam and saves the LaunchConfiguration under
	 * external tools
	 * 
	 * @param command
	 *            external tools command name
	 * @param executable
	 *            executable path i.e.c:\apache\apache.exe
	 * @param arguments
	 *            arguments for this configuration
	 * @param background
	 *            run this configuration in background mode
	 */
	public static void execute(String command, String executable,
			String arguments, boolean background) {
		// PHPConsole console = new PHPConsole();
		// String consoleMessage;
		// if (background) {
		// consoleMessage = "run in background mode-" + command + ": " +
		// executable + " " + arguments;
		// } else {
		// consoleMessage = "run in foreground mode-" + command + ": " +
		// executable + " " + arguments;
		// }
		// console.println(consoleMessage);

		ExternalToolsUtil.execute(command, executable, arguments, background);
	}

	public void selectionChanged(IAction action, ISelection selection) {

	}

	public void init(IWorkbenchWindow window) {
		this.activeWindow = window;
	}

	public void dispose() {

	}

}