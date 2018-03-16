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

package net.sourceforge.phpeclipse.phpeditor;

import net.sourceforge.phpdt.core.ICompilationUnit;
import net.sourceforge.phpdt.core.IJavaElement;
import net.sourceforge.phpdt.core.IJavaProject;
import net.sourceforge.phpdt.core.IMember;
import net.sourceforge.phpdt.core.IWorkingCopy;
import net.sourceforge.phpdt.core.JavaCore;
import net.sourceforge.phpdt.core.JavaModelException;
import net.sourceforge.phpdt.internal.corext.util.JavaModelUtil;
import net.sourceforge.phpdt.ui.JavaUI;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * A number of routines for working with JavaElements in editors
 * 
 * Use 'isOpenInEditor' to test if an element is already open in a editor Use
 * 'openInEditor' to force opening an element in a editor With 'getWorkingCopy'
 * you get the working copy (element in the editor) of an element
 */
public class EditorUtility {

	public static boolean isEditorInput(Object element, IEditorPart editor) {
		if (editor != null) {
			try {
				return editor.getEditorInput().equals(getEditorInput(element));
			} catch (JavaModelException x) {
				PHPeclipsePlugin.log(x.getStatus());
			}
		}
		return false;
	}

	/**
	 * Tests if a cu is currently shown in an editor
	 * 
	 * @return the IEditorPart if shown, null if element is not open in an
	 *         editor
	 */
	public static IEditorPart isOpenInEditor(Object inputElement) {
		IEditorInput input = null;

		try {
			input = getEditorInput(inputElement);
		} catch (JavaModelException x) {
			PHPeclipsePlugin.log(x.getStatus());
		}

		if (input != null) {
			IWorkbenchPage p = PHPeclipsePlugin.getActivePage();
			if (p != null) {
				return p.findEditor(input);
			}
		}

		return null;
	}

	/**
	 * Opens a Java editor for an element such as <code>IJavaElement</code>,
	 * <code>IFile</code>, or <code>IStorage</code>. The editor is
	 * activated by default.
	 * 
	 * @return the IEditorPart or null if wrong element type or opening failed
	 */
	public static IEditorPart openInEditor(Object inputElement)
			throws JavaModelException, PartInitException {
		return openInEditor(inputElement, true);
	}

	/**
	 * Opens a Java editor for an element (IJavaElement, IFile, IStorage...)
	 * 
	 * @return the IEditorPart or null if wrong element type or opening failed
	 */
	public static IEditorPart openInEditor(Object inputElement, boolean activate)
			throws JavaModelException, PartInitException {

		if (inputElement instanceof IFile)
			return openInEditor((IFile) inputElement, activate);

		IEditorInput input = getEditorInput(inputElement);
		if (input instanceof IFileEditorInput) {
			IFileEditorInput fileInput = (IFileEditorInput) input;
			return openInEditor(fileInput.getFile(), activate);
		}

		if (input != null)
			return openInEditor(input, getEditorID(input, inputElement),
					activate);

		return null;
	}

	/**
	 * Selects a Java Element in an editor
	 */
	public static void revealInEditor(IEditorPart part, IJavaElement element) {
		if (element != null && part instanceof PHPEditor) {
			((PHPEditor) part).setSelection(element);
		}
	}

	private static IEditorPart openInEditor(IFile file, boolean activate)
			throws PartInitException {
		if (file != null) {
			IWorkbenchPage p = PHPeclipsePlugin.getActivePage();
			if (p != null) {
				IEditorPart editorPart = IDE.openEditor(p, file, activate);
				initializeHighlightRange(editorPart);
				return editorPart;
			}
		}
		return null;
	}

	private static IEditorPart openInEditor(IEditorInput input,
			String editorID, boolean activate) throws PartInitException {
		if (input != null) {
			IWorkbenchPage p = PHPeclipsePlugin.getActivePage();
			if (p != null) {
				IEditorPart editorPart = p
						.openEditor(input, editorID, activate);
				initializeHighlightRange(editorPart);
				return editorPart;
			}
		}
		return null;
	}

	private static void initializeHighlightRange(IEditorPart editorPart) {
		if (editorPart instanceof ITextEditor) {
			TogglePresentationAction toggleAction = new TogglePresentationAction();
			// Initialize editor
			toggleAction.setEditor((ITextEditor) editorPart);
			// Reset action
			toggleAction.setEditor(null);
		}
	}

	/**
	 * @deprecated Made it public again for java debugger UI.
	 */
	public static String getEditorID(IEditorInput input, Object inputObject) {
		IEditorRegistry registry = PlatformUI.getWorkbench()
				.getEditorRegistry();
		IEditorDescriptor descriptor = registry.getDefaultEditor(input
				.getName());
		if (descriptor != null)
			return descriptor.getId();
		return null;
	}

	private static IEditorInput getEditorInput(IJavaElement element)
			throws JavaModelException {
		while (element != null) {
			if (element instanceof IWorkingCopy
					&& ((IWorkingCopy) element).isWorkingCopy())
				element = ((IWorkingCopy) element).getOriginalElement();

			if (element instanceof ICompilationUnit) {
				ICompilationUnit unit = (ICompilationUnit) element;
				IResource resource = unit.getResource();
				if (resource instanceof IFile)
					return new FileEditorInput((IFile) resource);
			}

			// if (element instanceof IClassFile)
			// return new InternalClassFileEditorInput((IClassFile) element);
			//			
			element = element.getParent();
		}

		return null;
	}

	public static IEditorInput getEditorInput(Object input)
			throws JavaModelException {

		if (input instanceof IJavaElement)
			return getEditorInput((IJavaElement) input);

		if (input instanceof IFile)
			return new FileEditorInput((IFile) input);

		// if (input instanceof IStorage)
		// return new JarEntryEditorInput((IStorage)input);

		return null;
	}

	/**
	 * If the current active editor edits a java element return it, else return
	 * null
	 */
	public static IJavaElement getActiveEditorJavaInput() {
		IWorkbenchPage page = PHPeclipsePlugin.getActivePage();
		if (page != null) {
			IEditorPart part = page.getActiveEditor();
			if (part != null) {
				IEditorInput editorInput = part.getEditorInput();
				if (editorInput != null) {
					return (IJavaElement) editorInput
							.getAdapter(IJavaElement.class);
				}
			}
		}
		return null;
	}

	/**
	 * Gets the working copy of an compilation unit opened in an editor
	 * 
	 * @param part
	 *            the editor part
	 * @param cu
	 *            the original compilation unit (or another working copy)
	 * @return the working copy of the compilation unit, or null if not found
	 */
	public static ICompilationUnit getWorkingCopy(ICompilationUnit cu) {
		if (cu == null)
			return null;
		if (cu.isWorkingCopy())
			return cu;

		return (ICompilationUnit) cu.findSharedWorkingCopy(JavaUI
				.getBufferFactory());
	}

	/**
	 * Gets the working copy of an member opened in an editor
	 * 
	 * @param member
	 *            the original member or a member in a working copy
	 * @return the corresponding member in the shared working copy or
	 *         <code>null</code> if not found
	 */
	public static IMember getWorkingCopy(IMember member)
			throws JavaModelException {
		ICompilationUnit cu = member.getCompilationUnit();
		if (cu != null) {
			ICompilationUnit workingCopy = getWorkingCopy(cu);
			if (workingCopy != null) {
				return JavaModelUtil.findMemberInCompilationUnit(workingCopy,
						member);
			}
		}
		return null;
	}

	/**
	 * Returns the compilation unit for the given java element.
	 * 
	 * @param element
	 *            the java element whose compilation unit is searched for
	 * @return the compilation unit of the given java element
	 */
	private static ICompilationUnit getCompilationUnit(IJavaElement element) {

		if (element == null)
			return null;

		if (element instanceof IMember)
			return ((IMember) element).getCompilationUnit();

		int type = element.getElementType();
		if (IJavaElement.COMPILATION_UNIT == type)
			return (ICompilationUnit) element;
		if (IJavaElement.CLASS_FILE == type)
			return null;

		return getCompilationUnit(element.getParent());
	}

	/**
	 * Returns the working copy of the given java element.
	 * 
	 * @param javaElement
	 *            the javaElement for which the working copyshould be found
	 * @param reconcile
	 *            indicates whether the working copy must be reconcile prior to
	 *            searching it
	 * @return the working copy of the given element or <code>null</code> if
	 *         none
	 */
	public static IJavaElement getWorkingCopy(IJavaElement element,
			boolean reconcile) throws JavaModelException {
		ICompilationUnit unit = getCompilationUnit(element);
		if (unit == null)
			return null;

		if (unit.isWorkingCopy())
			return element;

		ICompilationUnit workingCopy = getWorkingCopy(unit);
		if (workingCopy != null) {
			if (reconcile) {
				synchronized (workingCopy) {
					workingCopy.reconcile();
					return JavaModelUtil.findInCompilationUnit(workingCopy,
							element);
				}
			} else {
				return JavaModelUtil
						.findInCompilationUnit(workingCopy, element);
			}
		}

		return null;
	}

	/**
	 * Maps the localized modifier name to a code in the same manner as
	 * #findModifier.
	 * 
	 * @return the SWT modifier bit, or <code>0</code> if no match was found
	 * @see findModifier
	 * @since 2.1.1
	 */
	public static int findLocalizedModifier(String token) {
		if (token == null)
			return 0;

		if (token.equalsIgnoreCase(Action.findModifierString(SWT.CTRL)))
			return SWT.CTRL;
		if (token.equalsIgnoreCase(Action.findModifierString(SWT.SHIFT)))
			return SWT.SHIFT;
		if (token.equalsIgnoreCase(Action.findModifierString(SWT.ALT)))
			return SWT.ALT;
		if (token.equalsIgnoreCase(Action.findModifierString(SWT.COMMAND)))
			return SWT.COMMAND;

		return 0;
	}

	/**
	 * Returns the modifier string for the given SWT modifier modifier bits.
	 * 
	 * @param stateMask
	 *            the SWT modifier bits
	 * @return the modifier string
	 * @since 2.1.1
	 */
	public static String getModifierString(int stateMask) {
		String modifierString = ""; //$NON-NLS-1$
		if ((stateMask & SWT.CTRL) == SWT.CTRL)
			modifierString = appendModifierString(modifierString, SWT.CTRL);
		if ((stateMask & SWT.ALT) == SWT.ALT)
			modifierString = appendModifierString(modifierString, SWT.ALT);
		if ((stateMask & SWT.SHIFT) == SWT.SHIFT)
			modifierString = appendModifierString(modifierString, SWT.SHIFT);
		if ((stateMask & SWT.COMMAND) == SWT.COMMAND)
			modifierString = appendModifierString(modifierString, SWT.COMMAND);

		return modifierString;
	}

	/**
	 * Appends to modifier string of the given SWT modifier bit to the given
	 * modifierString.
	 * 
	 * @param modifierString
	 *            the modifier string
	 * @param modifier
	 *            an int with SWT modifier bit
	 * @return the concatenated modifier string
	 * @since 2.1.1
	 */
	private static String appendModifierString(String modifierString,
			int modifier) {
		if (modifierString == null)
			modifierString = ""; //$NON-NLS-1$
		String newModifierString = Action.findModifierString(modifier);
		if (modifierString.length() == 0)
			return newModifierString;
		return PHPEditorMessages
				.getFormattedString(
						"EditorUtility.concatModifierStrings", new String[] { modifierString, newModifierString }); //$NON-NLS-1$
	}

	/**
	 * Returns the Java project for a given editor input or <code>null</code>
	 * if no corresponding Java project exists.
	 * 
	 * @param input
	 *            the editor input
	 * @return the corresponding Java project
	 * 
	 * @since 3.0
	 */
	public static IJavaProject getJavaProject(IEditorInput input) {
		IJavaProject jProject = null;
		if (input instanceof IFileEditorInput) {
			IProject project = ((IFileEditorInput) input).getFile()
					.getProject();
			if (project != null) {
				jProject = JavaCore.create(project);
				if (!jProject.exists())
					jProject = null;
			}
		}
		// else if (input instanceof IClassFileEditorInput) {
		// jProject=
		// ((IClassFileEditorInput)input).getClassFile().getJavaProject();
		// }
		return jProject;
	}
}
