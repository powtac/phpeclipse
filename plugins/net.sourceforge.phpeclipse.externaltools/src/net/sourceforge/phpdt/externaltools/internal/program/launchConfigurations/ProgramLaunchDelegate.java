package net.sourceforge.phpdt.externaltools.internal.program.launchConfigurations;

/**********************************************************************
 Copyright (c) 2002 IBM Corp. and others. All rights reserved.
 This file is made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html
 �
 Contributors:
 **********************************************************************/

import java.io.File;

import net.sourceforge.phpdt.externaltools.launchConfigurations.ExternalToolsUtil;
import net.sourceforge.phpdt.externaltools.variable.ExpandVariableContext;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.IProcess;

/**
 * Launch delegate for a program.
 */
public class ProgramLaunchDelegate implements ILaunchConfigurationDelegate {

	/**
	 * Constructor for ProgramLaunchDelegate.
	 */
	public ProgramLaunchDelegate() {
		super();
	}

	/**
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration,
	 *      java.lang.String, org.eclipse.debug.core.ILaunch,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {

		if (monitor.isCanceled()) {
			return;
		}

		// get variable context
		ExpandVariableContext resourceContext = ExternalToolsUtil
				.getVariableContext();

		if (monitor.isCanceled()) {
			return;
		}

		// resolve location
		IPath location = ExternalToolsUtil.getLocation(configuration,
				resourceContext);

		if (monitor.isCanceled()) {
			return;
		}

		// resolve working directory
		IPath workingDirectory = ExternalToolsUtil.getWorkingDirectory(
				configuration, resourceContext);

		if (monitor.isCanceled()) {
			return;
		}

		// resolve arguments
		String[] arguments = ExternalToolsUtil.getArguments(configuration,
				resourceContext);

		if (monitor.isCanceled()) {
			return;
		}

		int cmdLineLength = 1;
		if (arguments != null) {
			cmdLineLength += arguments.length;
		}
		String[] cmdLine = new String[cmdLineLength];
		cmdLine[0] = location.toOSString();
		if (arguments != null) {
			System.arraycopy(arguments, 0, cmdLine, 1, arguments.length);
		}

		File workingDir = null;
		if (workingDirectory != null) {
			workingDir = workingDirectory.toFile();
		}

		if (monitor.isCanceled()) {
			return;
		}

		Process p = DebugPlugin.exec(cmdLine, workingDir);
		IProcess process = null;
		if (p != null) {
			process = DebugPlugin.newProcess(launch, p, location.toOSString());
		}
		process.setAttribute(IProcess.ATTR_CMDLINE, renderCommandLine(cmdLine));

		if (ExternalToolsUtil.isBackground(configuration)) {
			// refresh resources after process finishes
			if (ExternalToolsUtil.getRefreshScope(configuration) != null) {
				BackgroundResourceRefresher refresher = new BackgroundResourceRefresher(
						configuration, process, resourceContext);
				refresher.startBackgroundRefresh();
			}
		} else {
			// wait for process to exit
			while (!process.isTerminated()) {
				try {
					if (monitor.isCanceled()) {
						process.terminate();
						break;
					}
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
			}

			// refresh resources
			ExternalToolsUtil.refreshResources(configuration, resourceContext,
					monitor);
		}

	}

	protected static String renderCommandLine(String[] commandLine) {
		if (commandLine.length < 1)
			return ""; //$NON-NLS-1$
		StringBuffer buf = new StringBuffer(commandLine[0]);
		for (int i = 1; i < commandLine.length; i++) {
			buf.append(' ');
			buf.append(commandLine[i]);
		}
		return buf.toString();
	}

}
