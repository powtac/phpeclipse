/***********************************************************************************************************************************
 * Copyright (c) 2000, 2002 IBM Corp. and others. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - Initial implementation Vicente Fernando - www.alfersoft.com.ar Christian Perkonig - remote Debug
 **********************************************************************************************************************************/
package net.sourceforge.phpdt.internal.launching;

import java.util.Iterator;

import net.sourceforge.phpdt.internal.core.JavaProject;
import net.sourceforge.phpdt.internal.debug.core.PHPDBGProxy;
import net.sourceforge.phpdt.internal.debug.core.model.PHPDebugTarget;
import net.sourceforge.phpeclipse.ui.editor.BrowserUtil;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.swt.widgets.Display;

// import net.sourceforge.phpeclipse.resourcesview.PHPProject;

public class DebuggerRunner extends InterpreterRunner {

	public IProcess run(InterpreterRunnerConfiguration configuration,
			ILaunch launch) {
		String[] env;
		String name, value;
		PHPDBGProxy newPHPDBGProxy = new PHPDBGProxy(configuration
				.useRemoteDebugger(), configuration.getRemoteSourcePath(),
				configuration.usePathTranslation(), configuration.getPathMap());
		int pos;

		IProcess process = null;
		PHPDebugTarget debugTarget = new PHPDebugTarget(launch, process);
		newPHPDBGProxy.setDebugTarget(debugTarget);
		newPHPDBGProxy.start();
		if (configuration.useRemoteDebugger()) {
			// listener for remote debuger is started
			if (configuration.useDBGSessionInBrowser()) {
				activateDBGSESSIDPreview(configuration, newPHPDBGProxy
						.getPort());
			}
		} else {
			setEnvironmentVariables(configuration, newPHPDBGProxy.getPort());
			// env=configuration.getEnvironment();
			process = super.run(configuration, launch);
			debugTarget.setProcess(process);
		}
		launch.addDebugTarget(debugTarget);

		return process;
	}

	/**
	 * Open the browser in the UI thread with the current debugger URL
	 * 
	 * @param configuration
	 * @param port
	 */
	protected static void activateDBGSESSIDPreview(
			final InterpreterRunnerConfiguration configuration, final int port) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				String fileName = configuration.getFileName();
				JavaProject jproject = configuration.getProject();
				IProject project = jproject.getProject();
				IFile file = project.getFile(fileName);
				if (configuration.useDBGSessionInExternalBrowser()) {
					BrowserUtil.showBrowserAsExternal(file,
							"?DBGSESSID=1@clienthost:" + port);
				} else {
					BrowserUtil.showPreview(file, true,
							"?DBGSESSID=1@clienthost:" + port);
				}
			}
		});
	}

	protected void setEnvironmentVariables(
			InterpreterRunnerConfiguration configuration, int listenPort) {
		String DBGSessID;
		String env[] = new String[18];
		long id = Math.round(Math.random() * 100000);

		DBGSessID = "DBGSESSID=" + id + "@clienthost:" + listenPort;
		configuration.addEnvironmentValue("HTTP_COOKIE", DBGSessID, false);
		/*
		 * configuration.addEnvironmentValue("REDIRECT_URL",OSFilePath,true);
		 * configuration.addEnvironmentValue("REQUEST_URI",OSFilePath,true);
		 * configuration.addEnvironmentValue("PATH_INFO",OSFilePath,true);
		 * configuration.addEnvironmentValue("PATH_TRANSLATED",OSFilePath,true);
		 * configuration.addEnvironmentValue("SCRIPT_FILENAME",interpreter,true);
		 * configuration.addEnvironmentValue("SERVER_PROTOCOL","HTTP /
		 * 1.1",true);
		 */
		/*
		 * env[0]= "HTTP_COOKIE=" + DBGSessID; env[1]= "REDIRECT_QUERY_STRING=";
		 * env[2]= "REDIRECT_STATUS=200"; env[3]= "REDIRECT_URL=" + OSFilePath;
		 * env[4]= "SERVER_SOFTWARE=DBG / 2.1"; env[5]= "SERVER_NAME=localhost";
		 * env[6]= "SERVER_ADDR=127.0.0.1"; env[7]= "SERVER_PORT=80"; env[8]=
		 * "REMOTE_ADDR=127.0.0.1"; env[9]= "SCRIPT_FILENAME=" + interpreter;
		 * env[10]= "GATEWAY_INTERFACE=CGI / 1.1"; env[11]=
		 * "SERVER_PROTOCOL=HTTP / 1.1"; env[12]= "REQUEST_METHOD=GET"; env[13]=
		 * "QUERY_STRING=test=1"; env[14]= "REQUEST_URI=" + OSFilePath; env[15]=
		 * "PATH_INFO=" + OSFilePath; env[16]= "PATH_TRANSLATED=" + OSFilePath;
		 * env[17]= "SystemRoot=" + Environment.getenv("SystemRoot");
		 */
		// return env;
	}

	protected String getDebugCommandLineArgument() {
		return "";
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
}