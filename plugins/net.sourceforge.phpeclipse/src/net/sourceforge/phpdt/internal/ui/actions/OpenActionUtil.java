/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.phpdt.internal.ui.actions;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.phpdt.core.IJavaElement;
import net.sourceforge.phpdt.core.ISourceReference;
import net.sourceforge.phpdt.core.JavaModelException;
import net.sourceforge.phpdt.ui.JavaElementLabelProvider;
import net.sourceforge.phpeclipse.phpeditor.EditorUtility;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

public class OpenActionUtil {

	private OpenActionUtil() {
		// no instance.
	}

	/**
	 * Opens the editor on the given element and subsequently selects it.
	 */
	public static void open(Object element) throws JavaModelException,
			PartInitException {
		open(element, true);
	}

	/**
	 * Opens the editor on the given element and subsequently selects it.
	 */
	public static void open(Object element, boolean activate)
			throws JavaModelException, PartInitException {
		IEditorPart part = EditorUtility.openInEditor(element, activate);
		if (element instanceof IJavaElement)
			EditorUtility.revealInEditor(part, (IJavaElement) element);
	}

	/**
	 * Filters out source references from the given code resolve results. A
	 * utility method that can be called by subclassers.
	 */
	public static List filterResolveResults(IJavaElement[] codeResolveResults) {
		int nResults = codeResolveResults.length;
		List refs = new ArrayList(nResults);
		for (int i = 0; i < nResults; i++) {
			if (codeResolveResults[i] instanceof ISourceReference)
				refs.add(codeResolveResults[i]);
		}
		return refs;
	}

	/**
	 * Shows a dialog for resolving an ambigous java element. Utility method
	 * that can be called by subclassers.
	 */
	public static IJavaElement selectJavaElement(IJavaElement[] elements,
			Shell shell, String title, String message) {

		int nResults = elements.length;

		if (nResults == 0)
			return null;

		if (nResults == 1)
			return elements[0];

		int flags = JavaElementLabelProvider.SHOW_DEFAULT
				| JavaElementLabelProvider.SHOW_QUALIFIED
				| JavaElementLabelProvider.SHOW_ROOT;

		ElementListSelectionDialog dialog = new ElementListSelectionDialog(
				shell, new JavaElementLabelProvider(flags));
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.setElements(elements);

		if (dialog.open() == ElementListSelectionDialog.OK) {
			Object[] selection = dialog.getResult();
			if (selection != null && selection.length > 0) {
				nResults = selection.length;
				for (int i = 0; i < nResults; i++) {
					Object current = selection[i];
					if (current instanceof IJavaElement)
						return (IJavaElement) current;
				}
			}
		}
		return null;
	}
}
