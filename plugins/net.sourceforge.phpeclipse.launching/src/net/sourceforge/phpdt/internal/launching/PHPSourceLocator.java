package net.sourceforge.phpdt.internal.launching;

import java.util.Iterator;
import java.util.Map;

import net.sourceforge.phpdt.internal.debug.core.model.PHPStackFrame;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;
import net.sourceforge.phpeclipse.builder.ExternalEditorInput;
import net.sourceforge.phpeclipse.builder.FileStorage;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.ui.ISourcePresentation;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

public class PHPSourceLocator implements IPersistableSourceLocator, ISourcePresentation {
	private String 		absoluteWorkingDirectory;
	private Map 		pathMap = null;
	private boolean 	remoteDebug;
	private IPath 		remoteSourcePath;
	private String 		projectName;

  public PHPSourceLocator() {

  }

  public String getAbsoluteWorkingDirectory() {
    return absoluteWorkingDirectory;
  }

  /**
   * @see org.eclipse.debug.core.model.IPersistableSourceLocator#getMemento()
   */
  public String getMemento() throws CoreException {
    return null;
  }

  /**
   * @see org.eclipse.debug.core.model.IPersistableSourceLocator#initializeFromMemento(String)
   */
  public void initializeFromMemento(String memento) throws CoreException {
  }

  /**
   * @see org.eclipse.debug.core.model.IPersistableSourceLocator#initializeDefaults(ILaunchConfiguration)
   */
  public void initializeDefaults (ILaunchConfiguration configuration) throws CoreException {
    this.absoluteWorkingDirectory = configuration.getAttribute (PHPLaunchConfigurationAttribute.WORKING_DIRECTORY, "");
	this.remoteDebug              = configuration.getAttribute (PHPLaunchConfigurationAttribute.REMOTE_DEBUG,false);
	this.pathMap                  = configuration.getAttribute (PHPLaunchConfigurationAttribute.FILE_MAP, (Map)null);
	this.projectName              = configuration.getAttribute (PHPLaunchConfigurationAttribute.PROJECT_NAME, "");

	this.remoteSourcePath = new Path (configuration.getAttribute (PHPLaunchConfigurationAttribute.REMOTE_PATH, ""));
  }

  /**
   * @see org.eclipse.debug.core.model.ISourceLocator#getSourceElement(IStackFrame)
   *
   * Return the client side source filename for the server side source file.
   * E.g. when cross debugging, the server side filename could be /var/www/index.php
   * on the client side it is either a Eclipse_PHP_projectname\index.php (when it is a linked file)
   *
   *
   * @param stackFrame    The stackframe for which we want the client side source file name
   * @return              The filename as it appears on the client side
   */
  public Object getSourceElement (IStackFrame stackFrame) {
	IPath    projectPath;
	IPath    remotePath;
	IPath    path;
	IPath    localPath;
	Iterator iterator;
	String   fileName;
	String   file;
	String   local;

	fileName = ((PHPStackFrame) stackFrame).getFileName ();			// Get the filename as it is submitted by DBG
	file     = "";

    if (remoteDebug) {                                              // Is it a remote debugging session
		path = new Path (fileName);                                 // Create a IPath object for the server side filename

		if (!remoteSourcePath.isEmpty()) {
			if (remoteSourcePath.isPrefixOf (path)) {                   // Is the server side filename with the remote source path
				path        = path.removeFirstSegments (remoteSourcePath.matchingFirstSegments (path)); // Remove the remote source path
				file        = path.toString ();                         // The filename without the remote source path
				projectPath = (PHPeclipsePlugin.getWorkspace().getRoot().getProject(projectName).getLocation()); // Get the absolute project path

				return (projectPath.append (path)).toOSString ();       // Return the filename as absolute client side path
			}
		}
		else {
			if (pathMap == null) {                                      // Do we have path mapping (e.g. for cross platform debugging)
				return fileName;                                        // No, then return the filename as it given by DBG (the full server side path)
			}

			iterator = pathMap.keySet().iterator();

			while (iterator.hasNext ()) {
				local      = (String) iterator.next ();                 // Get the local/client side path of the mapping
				remotePath = new Path ((String) pathMap.get (local));   // Get the remote/server side path of the mapping

				if (remotePath.isPrefixOf (path)) {                     // Starts the remote/server side file path with the remote/server side mapping path
					path      = path.removeFirstSegments (remotePath.matchingFirstSegments (path)); // Remove the absolute path from filename
					localPath = new Path (local);                       // Create new IPath object for the local/client side path
					path      = localPath.append (path);                // Prepend the project relative path to filename

					projectPath = (PHPeclipsePlugin.getWorkspace().getRoot().getProject(projectName).getLocation()); // Get the absolute project path

					return (projectPath.append (path)).toOSString ();       // Return the filename as absolute client side path
				}
			}
		}

		if (pathMap == null) {                                      // Do we have path mapping (e.g. for cross platform debugging)
			return fileName;                                        // No, then return the filename as it given by DBG (the full server side path)
		}

		iterator = pathMap.keySet().iterator();

		while (iterator.hasNext ()) {
			local      = (String) iterator.next ();                 // Get the local/client side path of the mapping
			remotePath = new Path ((String) pathMap.get (local));   // Get the remote/server side path of the mapping

			if (remotePath.isPrefixOf (path)) {                     // Starts the remote/server side file path with the remote/server side mapping path
				path      = path.removeFirstSegments (remotePath.matchingFirstSegments (path)); // Remove the absolute path from filename
				localPath = new Path (local);                       // Create new IPath object for the local/client side path

				return localPath.append (path).toOSString ();       // Append the remote filename to the client side path (So we return the absolute path
																	// to the source file as the client side sees it.
			}
		}

	 	return fileName;

    } else {

    	IWorkspaceRoot root = PHPLaunchingPlugin.getWorkspace().getRoot();
    	Path filePath = new Path(fileName);

    	if (root.getFileForLocation(filePath) == null) {
			IProject proj = root.getProject(projectName);
			IFile[] files = root.findFilesForLocation(filePath);
			for (int i = 0; i < files.length; i++) {
				if (files[i].getProject().equals(proj)) {
					fileName = proj.getFullPath().append(files[i].getProjectRelativePath()).toOSString();
					break;
				}
			}
		}

		return fileName;
    }
  }

  /**
   * @see org.eclipse.debug.ui.ISourcePresentation#getEditorId(IEditorInput, Object)
   */
  public String getEditorId(IEditorInput input, Object element) {
    return PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor((String) element).getId();
  }

  /**
   * @see org.eclipse.debug.ui.ISourcePresentation#getEditorInput(Object)
   *
   * @param element The absolute local/client side file path
   */
  public IEditorInput getEditorInput (Object element) {
	String           filename;
	IWorkbench       workbench;
	IWorkbenchWindow window;
    IWorkbenchPage   page;
	IPath            path;
	IFile            eclipseFile;

    filename  = (String) element;
    workbench = PlatformUI.getWorkbench ();
    window    = workbench.getWorkbenchWindows ()[0];
    page      = window.getActivePage ();
    path      = new Path (filename);                                // Create an IPath object of the absolute local/client side file name

    // If the file exists in the workspace, open it
    eclipseFile = PHPeclipsePlugin.getWorkspace().getRoot().getFileForLocation (path);

    //    IFile eclipseFile = PHPeclipsePlugin.getWorkspace().getRoot().getFileForLocation(new Path(filename));
//    if (eclipseFile == null) {
//      filename = this.getAbsoluteWorkingDirectory() + "/" + filename;
//      eclipseFile = PHPeclipsePlugin.getWorkspace().getRoot().getFileForLocation(new Path(filename));
//      if (eclipseFile == null) {
//        PHPeclipsePlugin.log(IStatus.INFO, "Could not find file \"" + element + "\".");
//        return null;
//      }
//    } else

    if (eclipseFile == null || !eclipseFile.exists ()) {
      //		Otherwise open the stream directly
	  //
      if (page == null) {
        PHPeclipsePlugin.log(IStatus.INFO, "Could not find file \"" + element + "\".");
        return null;
      }

      FileStorage storage = new FileStorage (path);
      storage.setReadOnly ();

      //      IEditorRegistry registry = workbench.getEditorRegistry();
      //      IEditorDescriptor desc = registry.getDefaultEditor(filename);
      //      if (desc == null) {
      //        desc = registry.getDefaultEditor();
      //      }
      return new ExternalEditorInput(storage);
    }

    return new FileEditorInput (eclipseFile);
  }
}
