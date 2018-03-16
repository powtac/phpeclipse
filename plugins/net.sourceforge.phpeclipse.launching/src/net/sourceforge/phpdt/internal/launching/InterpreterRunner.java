package net.sourceforge.phpdt.internal.launching;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import net.sourceforge.phpdt.internal.core.JavaProject;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;

public class InterpreterRunner {

	public InterpreterRunner() {
	}

	public IProcess run(InterpreterRunnerConfiguration configuration,
			ILaunch launch) {
		String commandLine = renderCommandLine(configuration);
		File workingDirectory = configuration.getAbsoluteWorkingDirectory();

		setEnvironmentVariables(configuration);
		String[] env = configuration.getEnvironment();
		Process nativePHPProcess = null;
		try {
			nativePHPProcess = configuration.getInterpreter().exec(commandLine,
					workingDirectory, env);
		} catch (IOException e) {
			throw new RuntimeException("Unable to execute interpreter: "
					+ commandLine + workingDirectory);
		}

		IProcess process = DebugPlugin.newProcess(launch, nativePHPProcess,
				renderLabel(configuration));
		process
				.setAttribute(PHPLaunchingPlugin.PLUGIN_ID
						+ ".launcher.cmdline", commandLine);
		process.setAttribute(IProcess.ATTR_PROCESS_TYPE,
				PHPLaunchConfigurationAttribute.PHP_LAUNCH_PROCESS_TYPE);

		return process;
	}

	protected String renderLabel(InterpreterRunnerConfiguration configuration) {
		StringBuffer buffer = new StringBuffer();

		PHPInterpreter interpreter = configuration.getInterpreter();
		buffer.append("PHP ");
		buffer.append(interpreter.getCommand());
		buffer.append(" : ");
		buffer.append(configuration.getFileName());

		return buffer.toString();
	}

	protected String renderCommandLine(
			InterpreterRunnerConfiguration configuration) {
		PHPInterpreter interpreter = configuration.getInterpreter();

		StringBuffer buffer = new StringBuffer();
		buffer.append(this.getDebugCommandLineArgument());
		// buffer.append(renderLoadPath(configuration));
		buffer.append(" " + configuration.getInterpreterArguments());
		// buffer.append(interpreter.endOfOptionsDelimeter);
		buffer.append(" "
				+ osDependentPath(configuration.getAbsoluteFileName()));
		buffer.append(" " + configuration.getProgramArguments());

		return buffer.toString();
	}

	protected void setEnvironmentVariables(
			InterpreterRunnerConfiguration configuration) {
		IPath FilePath = new Path(configuration.getAbsoluteFileName());
		String OSFilePath = FilePath.toOSString();
		configuration.addEnvironmentValue("REDIRECT_URL", OSFilePath, true);
		configuration.addEnvironmentValue("REQUEST_URI", OSFilePath, true);
		configuration.addEnvironmentValue("PATH_INFO", OSFilePath, true);
		configuration.addEnvironmentValue("PATH_TRANSLATED", OSFilePath, true);
		configuration.addEnvironmentValue("SCRIPT_FILENAME", configuration
				.getInterpreter().getCommand(), true);
		configuration
				.addEnvironmentValue("SERVER_PROTOCOL", "HTTP / 1.1", true);

		configuration.addEnvironmentValue("REDIRECT_QUERY_STRING", "", true);
		configuration.addEnvironmentValue("REDIRECT_STATUS", "200", true);
		configuration.addEnvironmentValue("SERVER_SOFTWARE", "DBG / 2.1", true);
		configuration.addEnvironmentValue("SERVER_NAME", "localhost", true);
		configuration.addEnvironmentValue("SERVER_ADDR", "127.0.0.1", true);
		configuration.addEnvironmentValue("SERVER_PORT", "80", true);
		configuration.addEnvironmentValue("REMOTE_ADDR", "127.0.0.1", true);

		configuration.addEnvironmentValue("GATEWAY_INTERFACE", "CGI / 1.1",
				true);
		configuration.addEnvironmentValue("REQUEST_METHOD", "GET", true);

		Map stringVars = DebugPlugin.getDefault().getLaunchManager()
				.getNativeEnvironment();
		if (stringVars.containsKey("SYSTEMROOT"))
			configuration.addEnvironmentValue("SYSTEMROOT", (String) stringVars
					.get("SYSTEMROOT"), true);

	}

	protected String renderLoadPath(InterpreterRunnerConfiguration configuration) {
		StringBuffer loadPath = new StringBuffer();

		JavaProject project = configuration.getProject();
		addToLoadPath(loadPath, project.getProject());

		Iterator referencedProjects = project.getReferencedProjects()
				.iterator();
		while (referencedProjects.hasNext())
			addToLoadPath(loadPath, (IProject) referencedProjects.next());

		return loadPath.toString();
	}

	protected void addToLoadPath(StringBuffer loadPath, IProject project) {
		loadPath.append(" -I "
				+ osDependentPath(project.getFullPath().toOSString()));
	}

	protected String osDependentPath(String aPath) {
		if (Platform.getOS().equals(Platform.OS_WIN32))
			aPath = "\"" + aPath + "\"";

		return aPath;
	}

	protected String getDebugCommandLineArgument() {
		return "";
	}
}
