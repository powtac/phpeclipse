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
package net.sourceforge.phpeclipse.xdebug.php.launching;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.phpeclipse.externaltools.ExternalToolsPlugin;
import net.sourceforge.phpeclipse.xdebug.core.IProxyEventListener;
import net.sourceforge.phpeclipse.xdebug.core.IXDebugPreferenceConstants;
import net.sourceforge.phpeclipse.xdebug.core.XDebugCorePlugin;
import net.sourceforge.phpeclipse.xdebug.core.XDebugProxy;
import net.sourceforge.phpeclipse.xdebug.php.model.XDebugTarget;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
//import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;


public class PHPLaunchConfigurationDelegate extends LaunchConfigurationDelegate {
	
	/**
	 * @see ILaunchConfigurationDelegate#launch(ILaunchConfiguration, String, ILaunch, IProgressMonitor)
	 */
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		List commandList = new ArrayList();
		
		String phpInterpreter = configuration.getAttribute(IXDebugConstants.ATTR_PHP_INTERPRETER, (String)null);
		boolean useDefaultInterpreter= configuration.getAttribute(IXDebugConstants.ATTR_PHP_DEFAULT_INTERPRETER, true);

		if (useDefaultInterpreter)	 {	
			phpInterpreter=XDebugCorePlugin.getDefault().getPreferenceStore().getString(IXDebugPreferenceConstants.PHP_INTERPRETER_PREFERENCE);
			if (phpInterpreter=="") {
				phpInterpreter=ExternalToolsPlugin.getDefault().getPreferenceStore().getString(ExternalToolsPlugin.PHP_RUN_PREF);
			}
		}
		File exe = new File(phpInterpreter);
		// Just to get sure that the interpreter exists
		if (!exe.exists()) {
			abort(MessageFormat.format("Specified PHP executable {0} does not exist. Check value of PHP-Interpreter.", new String[]{phpInterpreter}), null);
		}
		commandList.add(phpInterpreter);
		
		// Project name
		String projectName = configuration.getAttribute(IXDebugConstants.ATTR_PHP_PROJECT, (String)null);
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
//		 Just to get sure that the project exists
		if (project == null) {
			abort("Project does not exist.", null);
		}
		String fileName = configuration.getAttribute(IXDebugConstants.ATTR_PHP_FILE, (String)null);

		IFile file = project.getFile(fileName);
		// Just to get sure that the script exists
		if (!file.exists()) {
			abort(MessageFormat.format("PHP-Script {0} does not exist.", new String[] {file.getFullPath().toString()}), null);
		}
		
		commandList.add(file.getLocation().toOSString());

		// Get the Debugport  from the preferences
		int debugPort=XDebugCorePlugin.getDefault().getPreferenceStore().getInt(IXDebugPreferenceConstants.DEBUGPORT_PREFERENCE);
		
		// check for default port
		if (debugPort==0)
			debugPort=IXDebugPreferenceConstants.DEFAULT_DEBUGPORT;

		String[] envp=DebugPlugin.getDefault().getLaunchManager().getEnvironment(configuration);
		// appends the environment to the native environment
		if (envp==null) {
			Map stringVars = DebugPlugin.getDefault().getLaunchManager().getNativeEnvironment();
			int idx=0;
			envp= new String[stringVars.size()];
			for (Iterator i = stringVars.keySet().iterator(); i.hasNext();) {
				String key = (String) i.next();
				String value = (String) stringVars.get(key);
				envp[idx++]=key+"="+value;
			}
		}
		String idekey=fileName+"-"+(int)(Math.random()*100000);
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			String[] env = new String[envp.length+1];
			for(int i=0;i<envp.length;i++)
					env[i+1]=envp[i];
			env[0]="XDEBUG_CONFIG=idekey="+idekey+" remote_enable=1 remote_port="+debugPort;
			envp=env;
		}
		System.out.println("ideKey= "+idekey);
		
		String[] commandLine = (String[]) commandList.toArray(new String[commandList.size()]);

		XDebugProxy proxy = XDebugCorePlugin.getDefault().getXDebugProxy();
		proxy.start();
		
		XDebugTarget target = null;
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			target = new XDebugTarget(launch, null, idekey);
			proxy.addProxyEventListener((IProxyEventListener) target, idekey);
		}

		Process process = DebugPlugin.exec(commandLine, null,envp);
		IProcess p = DebugPlugin.newProcess(launch, process, phpInterpreter);
	
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			target.addProcess(p);
			launch.addDebugTarget(target);
		}

	}
	
	/**
	 * Throws an exception with a new status containing the given
	 * message and optional exception.
	 * 
	 * @param message error message
	 * @param e underlying exception
	 * @throws CoreException
	 */
	private void abort(String message, Throwable e) throws CoreException {
		// TODO: the plug-in code should be the example plug-in, not Perl debug model id
		throw new CoreException(new Status(IStatus.ERROR, IXDebugConstants.ID_PHP_DEBUG_MODEL, 0, message, e));
	}

}
