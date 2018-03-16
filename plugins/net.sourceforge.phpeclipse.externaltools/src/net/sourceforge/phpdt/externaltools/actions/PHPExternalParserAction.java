/**********************************************************************
 Copyright (c) 2000, 2002 IBM Corp. and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html

 Contributors:
 IBM Corporation - Initial implementation
 www.phpeclipse.de
 **********************************************************************/
package net.sourceforge.phpdt.externaltools.actions;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class PHPExternalParserAction implements IObjectActionDelegate {

	private IWorkbenchPart workbenchPart;

	/**
	 * Constructor for Action1.
	 */
	public PHPExternalParserAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		workbenchPart = targetPart;
	}

	// public static void open(final URL url, final Shell shell, final String
	// dialogTitle) {
	// IHelp help= WorkbenchHelp.getHelpSupport();
	// if (help != null) {
	// WorkbenchHelp.getHelpSupport().displayHelpResource(url.toExternalForm());
	// } else {
	// showMessage(shell, dialogTitle,
	// ActionMessages.getString("OpenBrowserUtil.help_not_available"), false);
	// //$NON-NLS-1$
	// }
	// }

	public void run(IAction action) {
		ISelectionProvider selectionProvider = null;
		selectionProvider = workbenchPart.getSite().getSelectionProvider();

		StructuredSelection selection = null;
		selection = (StructuredSelection) selectionProvider.getSelection();

		// Shell shell = null;
		Iterator iterator = null;
		iterator = selection.iterator();
		while (iterator.hasNext()) {
			// obj => selected object in the view
			Object obj = iterator.next();

			// is it a resource
			if (obj instanceof IResource) {
				IResource resource = (IResource) obj;

				// check if it's a file resource
				switch (resource.getType()) {

				case IResource.FILE:
					// single file:
					ExternalPHPParser parser = new ExternalPHPParser(
							(IFile) resource);
					parser.phpExternalParse();
				}
			}
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

}
