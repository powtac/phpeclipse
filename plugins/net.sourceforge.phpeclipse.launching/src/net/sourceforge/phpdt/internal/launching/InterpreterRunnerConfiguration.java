package net.sourceforge.phpdt.internal.launching;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.phpdt.internal.core.JavaProject;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.launchConfigurations.EnvironmentVariable;

public class InterpreterRunnerConfiguration {
	protected ILaunchConfiguration configuration;

	private HashMap fEnvironment;

	public InterpreterRunnerConfiguration(ILaunchConfiguration aConfiguration) {
		configuration = aConfiguration;
		fEnvironment = new HashMap();
	}

	public String getAbsoluteFileName() {
		IPath path = new Path(getFileName());
		IProject project = getProject().getProject();

		//return project.getLocation().toOSString() + "/" + getFileName();
		IResource file = project.findMember(path);
		return file.getProjectRelativePath().toOSString();
	}

	public String getFileName() {
		String fileName = "";

		try {
			fileName = configuration.getAttribute(
					PHPLaunchConfigurationAttribute.FILE_NAME,
					"No file specified in configuration");
		} catch (CoreException e) {
		}

		return fileName.replace('\\', '/');
	}

	public JavaProject getProject() {
		String projectName = "";

		try {
			projectName = configuration.getAttribute(
					PHPLaunchConfigurationAttribute.PROJECT_NAME, "");
		} catch (CoreException e) {
			PHPLaunchingPlugin.log(e);
		}

		IProject project = PHPLaunchingPlugin.getWorkspace().getRoot()
				.getProject(projectName);

		JavaProject phpProject = new JavaProject();
		phpProject.setProject(project);
		return phpProject;
	}

	public File getAbsoluteWorkingDirectory() {
		String file = null;
		try {
			file = configuration.getAttribute(
					PHPLaunchConfigurationAttribute.WORKING_DIRECTORY, "");
		} catch (CoreException e) {
			PHPLaunchingPlugin.log(e);
		}
		return new File(file);
	}

	public String getInterpreterArguments() {
		try {
			return configuration.getAttribute(
					PHPLaunchConfigurationAttribute.INTERPRETER_ARGUMENTS, "");
		} catch (CoreException e) {
		}

		return "";
	}

	public String getProgramArguments() {
		try {
			return configuration.getAttribute(
					PHPLaunchConfigurationAttribute.PROGRAM_ARGUMENTS, "");
		} catch (CoreException e) {
		}

		return "";
	}

	public PHPInterpreter getInterpreter() {
		String selectedInterpreter = null;
		try {
			selectedInterpreter = configuration.getAttribute(
					PHPLaunchConfigurationAttribute.SELECTED_INTERPRETER, "");
		} catch (CoreException e) {
		}

		return PHPRuntime.getDefault().getInterpreter(selectedInterpreter);
	}

	public boolean useRemoteDebugger() {
		try {
			return configuration.getAttribute(
					PHPLaunchConfigurationAttribute.REMOTE_DEBUG, false);
		} catch (CoreException e) {
			PHPLaunchingPlugin.log(e);
		}
		return false;
	}

	public boolean usePathTranslation() {
		try {
			return configuration.getAttribute(
					PHPLaunchConfigurationAttribute.REMOTE_DEBUG_TRANSLATE,
					false);
		} catch (CoreException e) {
			PHPLaunchingPlugin.log(e);
		}
		return false;
	}

	public Map getPathMap() {
		try {
			return configuration.getAttribute(
					PHPLaunchConfigurationAttribute.FILE_MAP, (Map) null);
		} catch (CoreException e) {
			PHPLaunchingPlugin.log(e);
		}
		return (Map) null;
	}

	public boolean useDBGSessionInBrowser() {
		try {
			return configuration.getAttribute(
					PHPLaunchConfigurationAttribute.OPEN_DBGSESSION_IN_BROWSER,
					true);
		} catch (CoreException e) {
			PHPLaunchingPlugin.log(e);
		}
		return false;
	}

	public void setEnvironment(String[] envp) {
		if (envp == null)
			return;
		for (int i = 0; i < envp.length; i++) {
			addEnvironmentValue(envp[i], true);
		}
	}

	public void addEnvironmentValue(String env, boolean replace) {
		String value = env.substring(env.indexOf('=') + 1);
		String key = env.substring(0, env.indexOf('='));
		addEnvironmentValue(key, value, replace);
	}

	public void addEnvironmentValue(String key, String value, boolean replace) {
		if (!replace && fEnvironment.containsKey(key)) {
			EnvironmentVariable ev = (EnvironmentVariable) fEnvironment
					.get(key);
			ev.setValue(ev.getValue() + ";" + value);
			fEnvironment.put(key, ev);
		} else
			this.fEnvironment.put(key, new EnvironmentVariable(key, value));
	}

	public String[] getEnvironment() {

		Iterator iter = fEnvironment.entrySet().iterator();
		List strings = new ArrayList(fEnvironment.size());
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			StringBuffer buffer = new StringBuffer((String) entry.getKey());
			buffer.append('=').append(
					((EnvironmentVariable) entry.getValue()).getValue());
			strings.add(buffer.toString());
		}
		return (String[]) strings.toArray(new String[strings.size()]);

	}

	public String getRemoteSourcePath() {

		IProject project = getProject().getProject();
		if (!useRemoteDebugger())
			return project.getFullPath().toOSString();
		else {
			try {
				return configuration.getAttribute(
						PHPLaunchConfigurationAttribute.REMOTE_PATH, "");
			} catch (CoreException e) {
				PHPLaunchingPlugin.log(e);
			}
		}

		return "";
	}

	public boolean useDBGSessionInExternalBrowser() {
		try {
			return configuration
					.getAttribute(
							PHPLaunchConfigurationAttribute.OPEN_DBGSESSION_IN_EXTERNAL_BROWSER,
							false);
		} catch (CoreException e) {
			PHPLaunchingPlugin.log(e);
		}
		return false;
	}

}
