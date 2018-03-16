/***********************************************************************************************************************************
 * Copyright (c) 2000, 2002 IBM Corp. and others. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - Initial implementation www.phpeclipse.de
 **********************************************************************************************************************************/
package net.sourceforge.phpdt.externaltools.actions;

import java.io.File;

import net.sourceforge.phpdt.externaltools.launchConfigurations.ExternalToolsUtil;
import net.sourceforge.phpeclipse.externaltools.ExternalToolsPlugin;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class PHPStartXAMPPAction implements IWorkbenchWindowActionDelegate {
	protected IWorkbenchWindow activeWindow = null;

	public void run(IAction action) {
		final IPreferenceStore store = ExternalToolsPlugin.getDefault()
				.getPreferenceStore();
		String executable = store
				.getString(ExternalToolsPlugin.XAMPP_START_PREF);
		String workingDirectory = null;
		if (executable != null && executable.length() > 0) {
			int index = executable.lastIndexOf(File.separatorChar);
			if (index > 0) {
				workingDirectory = executable.substring(0, index);
			}
		}
		execute("xampp_start", executable, workingDirectory, true);
	}

	/**
	 * Executes an external progam and saves the LaunchConfiguration under
	 * external tools
	 * 
	 * @param command
	 *            external tools command name
	 * @param executable
	 *            executable path i.e.c:\apache\apache.exe
	 * @param background
	 *            run this configuration in background mode
	 */
	public static void execute(String command, String executable,
			String workingDirectory, boolean background) {
		// PHPConsole console = new PHPConsole();
		// String consoleMessage;
		// if (background) {
		// consoleMessage = "run in background mode-" + command + ": " +
		// executable;
		// } else {
		// consoleMessage = "run in foreground mode-" + command + ": " +
		// executable;
		// }
		// console.println(consoleMessage);

		ExternalToolsUtil.execute(command, executable, workingDirectory, null,
				background);
	}

	public void selectionChanged(IAction action, ISelection selection) {

	}

	public void init(IWorkbenchWindow window) {
		this.activeWindow = window;
	}

	public void dispose() {

	}

}