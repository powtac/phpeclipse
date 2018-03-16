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

import java.util.Iterator;

import net.sourceforge.phpdt.core.ICompilationUnit;
import net.sourceforge.phpdt.core.IJavaElement;
import net.sourceforge.phpdt.core.IType;
import net.sourceforge.phpdt.core.JavaModelException;
import net.sourceforge.phpdt.internal.ui.util.ExceptionHandler;
import net.sourceforge.phpdt.ui.IWorkingCopyManager;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;
import net.sourceforge.phpeclipse.phpeditor.PHPEditor;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPart;

public class SelectionConverter {

	private static final IJavaElement[] EMPTY_RESULT = new IJavaElement[0];

	private SelectionConverter() {
		// no instance
	}

	/**
	 * Converts the selection provided by the given part into a structured
	 * selection. The following conversion rules are used:
	 * <ul>
	 * <li><code>part instanceof PHPEditor</code>: returns a structured
	 * selection using code resolve to convert the editor's text selection.</li>
	 * <li><code>part instanceof IWorkbenchPart</code>: returns the part's
	 * selection if it is a structured selection.</li>
	 * <li><code>default</code>: returns an empty structured selection.</li>
	 * </ul>
	 */
	public static IStructuredSelection getStructuredSelection(
			IWorkbenchPart part) throws JavaModelException {
		if (part instanceof PHPEditor)
			return new StructuredSelection(codeResolve((PHPEditor) part));
		ISelectionProvider provider = part.getSite().getSelectionProvider();
		if (provider != null) {
			ISelection selection = provider.getSelection();
			if (selection instanceof IStructuredSelection)
				return (IStructuredSelection) selection;
		}
		return StructuredSelection.EMPTY;
	}

	/**
	 * Converts the given structured selection into an array of Java elements.
	 * An empty array is returned if one of the elements stored in the
	 * structured selection is not of tupe <code>IJavaElement</code>
	 */
	public static IJavaElement[] getElements(IStructuredSelection selection) {
		if (!selection.isEmpty()) {
			IJavaElement[] result = new IJavaElement[selection.size()];
			int i = 0;
			for (Iterator iter = selection.iterator(); iter.hasNext(); i++) {
				Object element = (Object) iter.next();
				if (!(element instanceof IJavaElement))
					return EMPTY_RESULT;
				result[i] = (IJavaElement) element;
			}
			return result;
		}
		return EMPTY_RESULT;
	}

	public static boolean canOperateOn(PHPEditor editor) {
		if (editor == null)
			return false;
		return getInput(editor) != null;

	}

	/**
	 * Converts the text selection provided by the given editor into an array of
	 * Java elements. If the selection doesn't cover a Java element and the
	 * selection's length is greater than 0 the methods returns the editor's
	 * input element.
	 */
	public static IJavaElement[] codeResolveOrInput(PHPEditor editor)
			throws JavaModelException {
		IJavaElement input = getInput(editor);
		ITextSelection selection = (ITextSelection) editor
				.getSelectionProvider().getSelection();
		IJavaElement[] result = codeResolve(input, selection);
		if (result.length == 0) {
			result = new IJavaElement[] { input };
		}
		return result;
	}

	public static IJavaElement[] codeResolveOrInputHandled(PHPEditor editor,
			Shell shell, String title) {
		try {
			return codeResolveOrInput(editor);
		} catch (JavaModelException e) {
			ExceptionHandler.handle(e, shell, title, ActionMessages
					.getString("SelectionConverter.codeResolve_failed")); //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * Converts the text selection provided by the given editor a Java element
	 * by asking the user if code reolve returned more than one result. If the
	 * selection doesn't cover a Java element and the selection's length is
	 * greater than 0 the methods returns the editor's input element.
	 */
	public static IJavaElement codeResolveOrInput(PHPEditor editor,
			Shell shell, String title, String message)
			throws JavaModelException {
		IJavaElement[] elements = codeResolveOrInput(editor);
		if (elements == null || elements.length == 0)
			return null;
		IJavaElement candidate = elements[0];
		if (elements.length > 1) {
			candidate = OpenActionUtil.selectJavaElement(elements, shell,
					title, message);
		}
		return candidate;
	}

	public static IJavaElement codeResolveOrInputHandled(PHPEditor editor,
			Shell shell, String title, String message) {
		try {
			return codeResolveOrInput(editor, shell, title, message);
		} catch (JavaModelException e) {
			ExceptionHandler.handle(e, shell, title, ActionMessages
					.getString("SelectionConverter.codeResolveOrInput_failed")); //$NON-NLS-1$
		}
		return null;
	}

	public static IJavaElement[] codeResolve(PHPEditor editor)
			throws JavaModelException {
		return codeResolve(getInput(editor), (ITextSelection) editor
				.getSelectionProvider().getSelection());
	}

	/**
	 * Converts the text selection provided by the given editor a Java element
	 * by asking the user if code reolve returned more than one result. If the
	 * selection doesn't cover a Java element <code>null</code> is returned.
	 */
	public static IJavaElement codeResolve(PHPEditor editor, Shell shell,
			String title, String message) throws JavaModelException {
		IJavaElement[] elements = codeResolve(editor);
		if (elements == null || elements.length == 0)
			return null;
		IJavaElement candidate = elements[0];
		if (elements.length > 1) {
			candidate = OpenActionUtil.selectJavaElement(elements, shell,
					title, message);
		}
		return candidate;
	}

	public static IJavaElement[] codeResolveHandled(PHPEditor editor,
			Shell shell, String title) {
		try {
			return codeResolve(editor);
		} catch (JavaModelException e) {
			ExceptionHandler.handle(e, shell, title, ActionMessages
					.getString("SelectionConverter.codeResolve_failed")); //$NON-NLS-1$
		}
		return null;
	}

	public static IJavaElement getElementAtOffset(PHPEditor editor)
			throws JavaModelException {
		return getElementAtOffset(getInput(editor), (ITextSelection) editor
				.getSelectionProvider().getSelection());
	}

	public static IType getTypeAtOffset(PHPEditor editor)
			throws JavaModelException {
		IJavaElement element = SelectionConverter.getElementAtOffset(editor);
		IType type = (IType) element.getAncestor(IJavaElement.TYPE);
		if (type == null) {
			ICompilationUnit unit = SelectionConverter
					.getInputAsCompilationUnit(editor);
			if (unit != null)
				type = unit.findPrimaryType();
		}
		return type;
	}

	public static IJavaElement getInput(PHPEditor editor) {
		if (editor == null)
			return null;
		IEditorInput input = editor.getEditorInput();
		// if (input instanceof IClassFileEditorInput)
		// return ((IClassFileEditorInput)input).getClassFile();
		IWorkingCopyManager manager = PHPeclipsePlugin.getDefault()
				.getWorkingCopyManager();
		return manager.getWorkingCopy(input);
	}

	public static ICompilationUnit getInputAsCompilationUnit(PHPEditor editor) {
		Object editorInput = SelectionConverter.getInput(editor);
		if (editorInput instanceof ICompilationUnit)
			return (ICompilationUnit) editorInput;
		else
			return null;
	}

	private static IJavaElement[] codeResolve(IJavaElement input,
			ITextSelection selection) throws JavaModelException {
		// if (input instanceof ICodeAssist) {
		// IJavaElement[] elements=
		// ((ICodeAssist)input).codeSelect(selection.getOffset(),
		// selection.getLength());
		// if (elements != null && elements.length > 0)
		// return elements;
		// }
		return EMPTY_RESULT;
	}

	private static IJavaElement getElementAtOffset(IJavaElement input,
			ITextSelection selection) throws JavaModelException {
		if (input instanceof ICompilationUnit) {
			ICompilationUnit cunit = (ICompilationUnit) input;
			if (cunit.isWorkingCopy()) {
				synchronized (cunit) {
					cunit.reconcile();
				}
			}
			IJavaElement ref = cunit.getElementAt(selection.getOffset());
			if (ref == null)
				return input;
			else
				return ref;
		}
		// else if (input instanceof IClassFile) {
		// IJavaElement ref=
		// ((IClassFile)input).getElementAt(selection.getOffset());
		// if (ref == null)
		// return input;
		// else
		// return ref;
		// }
		return null;
	}
}
