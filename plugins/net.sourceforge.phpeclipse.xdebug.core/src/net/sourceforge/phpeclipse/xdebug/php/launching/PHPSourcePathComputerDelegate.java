package net.sourceforge.phpeclipse.xdebug.php.launching;


import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputerDelegate;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.WorkspaceSourceContainer;

public class PHPSourcePathComputerDelegate implements ISourcePathComputerDelegate {

	public ISourceContainer[] computeSourceContainers(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
//		String path = configuration.getAttribute(IXDebugConstants.ATTR_PHP_PROGRAM, (String)null);
//		ISourceContainer sourceContainer = null;
//		if (path != null) {
//			IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(path));
//			if (resource != null) {
//				IContainer container = resource.getParent();
//				if (container.getType() == IResource.PROJECT) {
//					sourceContainer = new ProjectSourceContainer((IProject)container, false);
//				} else if (container.getType() == IResource.FOLDER) {
//					sourceContainer = new FolderSourceContainer(container, false);
//				}
//			}
//		}
//		if (sourceContainer == null) {
//			sourceContainer = new WorkspaceSourceContainer();
//		}
//		return new ISourceContainer[]{sourceContainer};
		String project = configuration.getAttribute(IXDebugConstants.ATTR_PHP_PROJECT, (String)null);
		ISourceContainer sourceContainer = null;
		if (project != null) {
			IProject resource = ResourcesPlugin.getWorkspace().getRoot().getProject(project);
			if (resource != null) {
				sourceContainer = new ProjectSourceContainer(resource, false);
			}
		}
		if (sourceContainer == null) {
			sourceContainer = new WorkspaceSourceContainer();
		}
		return new ISourceContainer[]{sourceContainer};
	
	}

}
