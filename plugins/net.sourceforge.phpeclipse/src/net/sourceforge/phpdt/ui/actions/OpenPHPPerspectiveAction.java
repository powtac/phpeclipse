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
package net.sourceforge.phpdt.ui.actions;

import net.sourceforge.phpdt.internal.ui.actions.ActionMessages;
import net.sourceforge.phpdt.internal.ui.util.ExceptionHandler;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;

/**
 * Action to programmatically open a Java perspective.
 * 
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @since 2.0
 */
public class OpenPHPPerspectiveAction extends Action {

	/**
	 * Create a new <code>OpenPHPPerspectiveAction</code>.
	 */
	public OpenPHPPerspectiveAction() {
		// WorkbenchHelp.setHelp(this,
		// IJavaHelpContextIds.OPEN_JAVA_PERSPECTIVE_ACTION);
	}

	public void run() {
		IWorkbench workbench = PHPeclipsePlugin.getDefault().getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		IAdaptable input;
		if (page != null)
			input = page.getInput();
		else
			input = ResourcesPlugin.getWorkspace().getRoot();
		try {
			workbench.showPerspective(PHPeclipsePlugin.ID_PERSPECTIVE, window,
					input);
		} catch (WorkbenchException e) {
			ExceptionHandler
					.handle(
							e,
							window.getShell(),
							ActionMessages
									.getString("OpenPHPPerspectiveAction.dialog.title"), //$NON-NLS-1$
							ActionMessages
									.getString("OpenPHPPerspectiveAction.error.open_failed")); //$NON-NLS-1$
		}
	}
}
