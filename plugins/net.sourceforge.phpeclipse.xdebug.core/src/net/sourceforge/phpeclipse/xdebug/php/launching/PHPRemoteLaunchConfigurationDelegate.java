package net.sourceforge.phpeclipse.xdebug.php.launching;

import java.util.List;

import net.sourceforge.phpeclipse.xdebug.core.IProxyEventListener;
import net.sourceforge.phpeclipse.xdebug.core.XDebugCorePlugin;
import net.sourceforge.phpeclipse.xdebug.core.XDebugProxy;
import net.sourceforge.phpeclipse.xdebug.php.model.XDebugTarget;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
//import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;

public class PHPRemoteLaunchConfigurationDelegate extends LaunchConfigurationDelegate {
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		String projectName = configuration.getAttribute(IXDebugConstants.ATTR_PHP_PROJECT, (String)null);
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
//		 Just to get sure that the project exists
		if (project == null) {
			abort("Project does not exist.", null);
		}

		 List l = configuration.getAttribute(IXDebugConstants.ATTR_PHP_PATHMAP, (List) null);
		if(l == null) {
			abort("The project isn't properly mapped to remote.", null);			
		}

		XDebugProxy proxy = XDebugCorePlugin.getDefault().getXDebugProxy();
		proxy.start();
		String ideID = configuration.getAttribute(IXDebugConstants.ATTR_PHP_IDE_ID, "testID");

		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			IDebugTarget target = new XDebugTarget(launch, null, ideID);
			proxy.addProxyEventListener((IProxyEventListener) target, ideID);
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
		throw new CoreException(new Status(IStatus.ERROR, IXDebugConstants.ID_PHP_DEBUG_MODEL, 0, message, e));
	}
}