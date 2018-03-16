/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.phpdt.internal.ui.actions;

import net.sourceforge.phpdt.core.IJavaElement;
import net.sourceforge.phpdt.core.IJavaProject;
import net.sourceforge.phpdt.core.IPackageFragment;
import net.sourceforge.phpdt.core.IPackageFragmentRoot;
import net.sourceforge.phpdt.internal.corext.refactoring.util.ResourceUtil;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;
import net.sourceforge.phpeclipse.phpeditor.PHPEditor;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

/*
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=19104
 */
public class ActionUtil {

	private ActionUtil() {
	}

	// bug 31998 we will have to disable renaming of linked packages (and cus)
	public static boolean mustDisableJavaModelAction(Shell shell, Object element) {
		if (!(element instanceof IPackageFragment)
				&& !(element instanceof IPackageFragmentRoot))
			return false;

		IResource resource = ResourceUtil.getResource(element);
		if ((resource == null) || (!(resource instanceof IFolder))
				|| (!resource.isLinked()))
			return false;

		MessageDialog
				.openInformation(
						shell,
						ActionMessages.getString("ActionUtil.not_possible"), ActionMessages.getString("ActionUtil.no_linked")); //$NON-NLS-1$ //$NON-NLS-2$
		return true;
	}

	public static boolean isProcessable(Shell shell, PHPEditor editor) {
		if (editor == null)
			return true;
		IJavaElement input = SelectionConverter.getInput(editor);
		// if a Java editor doesn't have an input of type Java element
		// then it is for sure not on the build path
		if (input == null) {
			MessageDialog.openInformation(shell, ActionMessages
					.getString("ActionUtil.notOnBuildPath.title"), //$NON-NLS-1$
					ActionMessages
							.getString("ActionUtil.notOnBuildPath.message")); //$NON-NLS-1$
			return false;
		}
		return isProcessable(shell, input);
	}

	public static boolean isProcessable(Shell shell, Object element) {
		if (!(element instanceof IJavaElement))
			return true;

		if (isOnBuildPath((IJavaElement) element))
			return true;
		MessageDialog.openInformation(shell, ActionMessages
				.getString("ActionUtil.notOnBuildPath.title"), //$NON-NLS-1$
				ActionMessages.getString("ActionUtil.notOnBuildPath.message")); //$NON-NLS-1$
		return false;
	}

	public static boolean isOnBuildPath(IJavaElement element) {
		// fix for bug http://dev.eclipse.org/bugs/show_bug.cgi?id=20051
		if (element.getElementType() == IJavaElement.JAVA_PROJECT)
			return true;
		IJavaProject project = element.getJavaProject();
		try {
			// if (!project.isOnClasspath(element))
			// return false;
			IProject resourceProject = project.getProject();
			if (resourceProject == null)
				return false;
			IProjectNature nature = resourceProject
					.getNature(PHPeclipsePlugin.PHP_NATURE_ID);
			// We have a Java project
			if (nature != null)
				return true;
		} catch (CoreException e) {
		}
		return false;
	}
}
