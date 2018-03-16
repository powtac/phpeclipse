/*
 * Created on 09.08.2003
 *
 */
package net.sourceforge.phpdt.internal.ui.util;

import java.io.File;
import java.util.List;

import net.sourceforge.phpeclipse.PHPeclipsePlugin;
import net.sourceforge.phpeclipse.ui.overlaypages.ProjectPrefUtil;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class PHPFileUtil {
	// private static String[] PHP_EXTENSIONS = null;

	public final static String[] SMARTY_EXTENSIONS = { "tpl" };

	public static boolean isPHPFile(IFile file) {
                return isPHPFileName(file.getFullPath().toString());
        }

	// public final static String getFileExtension(String name) {
	// int index = name.lastIndexOf('.');
	// if (index == -1)
	// return null;
	// if (index == (name.length() - 1))
	// return null; //$NON-NLS-1$
	// return name.substring(index + 1);
	// }

	/**
	 * Returns true iff str.toLowerCase().endsWith(".php") implementation is not
	 * creating extra strings.
	 */
	public final static boolean isPHPFileName(String name) {

		// avoid handling a file without base name, e.g. ".php", which is a
		// valid
		// Eclipse resource name
		File file = new File(name);
		if (file.getName().startsWith(".")) {
			return false;
		}
		IWorkbench workbench = PlatformUI.getWorkbench();
		IEditorRegistry registry = workbench.getEditorRegistry();
		IEditorDescriptor[] descriptors = registry.getEditors(name);

		for (int i = 0; i < descriptors.length; i++) {
			if (descriptors[i].getId().equals(PHPeclipsePlugin.EDITOR_ID)) {
				return true;
			}
		}
		// String extension = getFileExtension(name);
		// if (extension == null) {
		// return false;
		// }
		// extension = extension.toLowerCase();
		// PHP_EXTENSIONS = getExtensions();
		// if (PHP_EXTENSIONS == null) {
		// return false;
		// }
		// for (int i = 0; i < PHP_EXTENSIONS.length; i++) {
		// if (extension.equals(PHP_EXTENSIONS[i])) {
		// return true;
		// }
		// }
		return false;
	}

	/**
	 * Returns true iff the file extension is a valid PHP Unit name
	 * implementation is not creating extra strings.
	 */
	public final static boolean isValidPHPUnitName(String filename) {
		return PHPFileUtil.isPHPFileName(filename);
	}

	/**
	 * @return Returns the PHP extensions.
	 */
	// public static String[] getExtensions() {
	// if (PHP_EXTENSIONS == null) {
	// ArrayList list = new ArrayList();
	// final IPreferenceStore store =
	// PHPeclipsePlugin.getDefault().getPreferenceStore();
	// String extensions =
	// store.getString(PHPeclipsePlugin.PHP_EXTENSION_PREFS);
	// extensions = extensions.trim();
	// if (extensions.length() != 0) {
	// StringTokenizer tokenizer = new StringTokenizer(extensions, " ,;:/-|");
	// String token;
	// while (tokenizer.hasMoreTokens()) {
	// token = tokenizer.nextToken();
	// if (token != null && token.length() >= 1) {
	// list.add(token);
	// }
	// }
	// if (list.size() != 0) {
	// PHP_EXTENSIONS = new String[list.size()];
	// for (int i = 0; i < list.size(); i++) {
	// PHP_EXTENSIONS[i] = (String) list.get(i);
	// }
	// }
	// }
	// }
	// return PHP_EXTENSIONS;
	// }
	/**
	 * @param php_extensions
	 *            The PHP extensions to set.
	 */
	// public static void setExtensions(String[] php_extensions) {
	// PHP_EXTENSIONS = php_extensions;
	// }
	/**
	 * Creata the file for the given absolute file path
	 * 
	 * @param absoluteFilePath
	 * @param project
	 * @return the file for the given absolute file path or <code>null</code>
	 *         if no existing file can be found
	 */
	public static IFile createFile(IPath absoluteFilePath, IProject project) {
		if (absoluteFilePath == null || project == null) {
			return null;
		}

		String projectPath = project.getFullPath().toString();
		String filePath = absoluteFilePath.toString().substring(
				projectPath.length() + 1);
		return project.getFile(filePath);

	}

	/**
	 * Determine the path of an include name string
	 * 
	 * @param includeNameString
	 * @param resource
	 * @param project
	 * @return the path for the given include filename or <code>null</code> if
	 *         no existing file can be found
	 */
	public static IPath determineFilePath(String includeNameString,
			IResource resource, IProject project) {
		IPath documentRootPath = ProjectPrefUtil.getDocumentRoot(project);
		IPath resourcePath = resource.getProjectRelativePath();

		IPath path = null;
		
		// script location based
		path = project.getFullPath().append(resourcePath.removeLastSegments(1))
				.append(includeNameString);
		//path = 
		if (fileExists(path, false)) {
			return path;
		}
		// project root based
		path = project.getFullPath().append(includeNameString);
		if (fileExists(path, false)) {
			return path;
		}
		
		// DocumentRoot (absolute path) based
		path = documentRootPath.append(includeNameString);
		if (fileExists(path, true)) {
			return path;
		}

		// IncludePaths settings (absolute path) based
		List includePaths = ProjectPrefUtil.getIncludePaths(project);
		if (includePaths.size() > 0) {
			for (int i = 0; i < includePaths.size(); i++) {
				path = new Path(includePaths.get(i).toString())
						.append(includeNameString);
				if (fileExists(path, true)) {
					return path;
				}
			}
		}
		return null;
	}

	private static boolean fileExists(IPath path, boolean absolute) {
		File file = path.toFile();
		if (file.exists()) {
			return true;
		}
		if (!absolute) {
			IFile ifile = FileBuffers.getWorkspaceFileAtLocation(path);
			if (ifile != null) {
			    IResource resource = ifile;
                if (resource.exists()) {
                    return true;
                }
			}
		}
		return false;
	}
}