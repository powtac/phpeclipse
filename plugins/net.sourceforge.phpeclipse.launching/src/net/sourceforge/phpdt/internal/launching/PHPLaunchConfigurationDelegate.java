/**********************************************************************
 Copyright (c) 2000, 2002 IBM Corp. and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html
 
 Contributors:
 IBM Corporation - Initial implementation
 Vicente Fernando - www.alfersoft.com.ar
 **********************************************************************/
package net.sourceforge.phpdt.internal.launching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;

public class PHPLaunchConfigurationDelegate extends LaunchConfigurationDelegate {

	protected static final InterpreterRunner interpreterRunner;

	protected static final DebuggerRunner debuggerRunner;

	static {
		interpreterRunner = new InterpreterRunner();
		debuggerRunner = new DebuggerRunner();
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate2#getLaunch(org.eclipse.debug.core.ILaunchConfiguration,
	 *      java.lang.String)
	 */
	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode)
			throws CoreException {
		PHPSourceLocator locator = new PHPSourceLocator();
		locator.initializeDefaults(configuration);
		return new Launch(configuration, mode, locator);
	}

	/**
	 * @see ILaunchConfigurationDelegate#launch(ILaunchConfiguration, String,
	 *      ILaunch, IProgressMonitor)
	 */
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		//if (PHPRuntime.getDefault().getSelectedInterpreter() == null) {
		if (configuration.getAttribute(
				PHPLaunchConfigurationAttribute.SELECTED_INTERPRETER, "")
				.equals("")) {
			if (!configuration.getAttribute(
					PHPLaunchConfigurationAttribute.REMOTE_DEBUG, false)
					&& mode.equals("debug") || mode.equals("run")) {
				String pid = PHPLaunchingPlugin.PLUGIN_ID;
				String msg = "You must define an interpreter before running PHP.";
				IStatus s = new Status(IStatus.ERROR, pid, IStatus.OK, msg,
						null);
				throw new CoreException(s);
			}
		}

		InterpreterRunnerConfiguration conf = new InterpreterRunnerConfiguration(
				configuration);
		ILaunchManager m = DebugPlugin.getDefault().getLaunchManager();
		conf.setEnvironment(m.getEnvironment(configuration));
		if (mode.equals("debug")) {
			debuggerRunner.run(conf, launch);
		} else {
			interpreterRunner.run(conf, launch);
		}
	}
}
