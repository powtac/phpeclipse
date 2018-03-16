package net.sourceforge.phpdt.internal.launching;

import java.io.File;
import java.io.IOException;

public class PHPInterpreter {

	protected File installLocation;

	public PHPInterpreter(File interpreter) {
		installLocation = interpreter;
	}

	public File getInstallLocation() {
		return installLocation;
	}

	public void setInstallLocation(File interpreter) {
		installLocation = interpreter;
	}

	public String getCommand() {
		return installLocation.toString();
	}

	// private boolean executePHPProcess(String arguments, File
	// workingDirectory, String[] env) {
	// Process process = null;
	// try {
	// StringBuffer buf = new StringBuffer();
	// buf.append(getCommand() + " " + arguments);
	// process = Runtime.getRuntime().exec(buf.toString(), env,
	// workingDirectory);
	// if (process != null) {
	// // construct a formatted command line for the process properties
	//
	// // for (int i= 0; i < args.length; i++) {
	// // buf.append(args[i]);
	// // buf.append(' ');
	// // }
	//
	// ILaunchConfigurationWorkingCopy wc = null;
	// try {
	// ILaunchConfigurationType lcType =
	// DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(
	// PHPLaunchConfigurationAttribute.PHP_LAUNCH_CONFIGURATION_TYPE);
	// String name = "PHP Launcher"; //$NON-NLS-1$
	// wc = lcType.newInstance(null, name);
	// wc.setAttribute(IDebugUIConstants.ATTR_FAVORITE_GROUPS, true);
	//
	// ILaunch newLaunch = new Launch(wc, ILaunchManager.RUN_MODE, null);
	// IProcess iprocess = DebugPlugin.newProcess(newLaunch, process, "PHP
	// Process"); //$NON-NLS-1$
	// iprocess.setAttribute(IProcess.ATTR_CMDLINE, buf.toString());
	// iprocess.setAttribute(IProcess.ATTR_PROCESS_TYPE,
	// PHPLaunchConfigurationAttribute.PHP_LAUNCH_PROCESS_TYPE);
	//
	// DebugPlugin.getDefault().getLaunchManager().addLaunch(newLaunch);
	//
	// } catch (CoreException e) {
	// }
	//
	// return true;
	//
	// }
	// } catch (IOException e) {
	// return false;
	// }
	// return false;
	//
	// }

	public Process exec(String arguments, File workingDirectory, String[] env)
			throws IOException {
		return Runtime.getRuntime().exec(getCommand() + " " + arguments, env,
				workingDirectory);
		// executePHPProcess(arguments, workingDirectory, env);
	}

	public boolean equals(Object other) {
		if (other instanceof PHPInterpreter) {
			PHPInterpreter otherInterpreter = (PHPInterpreter) other;
			return installLocation
					.equals(otherInterpreter.getInstallLocation());
		}
		return false;
	}
}