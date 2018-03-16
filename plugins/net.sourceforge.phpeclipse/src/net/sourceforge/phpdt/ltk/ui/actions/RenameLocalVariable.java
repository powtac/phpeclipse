// Copyright (c) 2005 by Leif Frenzel. All rights reserved.
// See http://leiffrenzel.de
// modified for phpeclipse.de project by axelcl
package net.sourceforge.phpdt.ltk.ui.actions;

import net.sourceforge.phpdt.core.ICompilationUnit;
import net.sourceforge.phpdt.core.IJavaElement;
import net.sourceforge.phpdt.core.JavaModelException;
import net.sourceforge.phpdt.internal.core.SourceMethod;
import net.sourceforge.phpdt.internal.ui.util.PHPFileUtil;
import net.sourceforge.phpdt.ltk.core.RenameIdentifierInfo;
import net.sourceforge.phpdt.ltk.core.RenameIdentifierRefactoring;
import net.sourceforge.phpdt.ltk.core.RenameLocalVariableDelegate;
import net.sourceforge.phpdt.ltk.core.RenamePHPProcessor;
import net.sourceforge.phpdt.ltk.ui.UITexts;
import net.sourceforge.phpdt.ltk.ui.wizards.RenameLocalVariableWizard;
import net.sourceforge.phpdt.ui.IWorkingCopyManager;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;
import net.sourceforge.phpeclipse.phpeditor.PHPEditor;
import net.sourceforge.phpeclipse.phpeditor.php.PHPWordExtractor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

public class RenameLocalVariable implements IEditorActionDelegate {

	private ISelection selection;

	private IEditorPart targetEditor;

	private boolean onPHPFile;

	private RenameIdentifierInfo info = new RenameIdentifierInfo();

	public void setActiveEditor(final IAction action,
			final IEditorPart targetEditor) {
		this.targetEditor = targetEditor;
		onPHPFile = false;
		IFile file = getFile();

		if (file != null && PHPFileUtil.isPHPFile(file)) {
			onPHPFile = true;
		}
	}

	public void run(final IAction action) {
		if (!onPHPFile) {
			refuse();
		} else {
			if (selection != null && selection instanceof ITextSelection) {
				String word = null;
				Point point = null;
				if (targetEditor != null && (targetEditor instanceof PHPEditor)) {
					PHPEditor editor = (PHPEditor) targetEditor;
					if (editor != null) {
						ITextSelection textSelection = (ITextSelection) editor
								.getSelectionProvider().getSelection();
						IDocument doc = editor.getDocumentProvider()
								.getDocument(editor.getEditorInput());
						int pos = textSelection.getOffset();
						point = PHPWordExtractor.findWord(doc, pos);
						if (point != null) {
							try {
								word = doc.get(point.x, point.y);
								IWorkingCopyManager manager = PHPeclipsePlugin
										.getDefault().getWorkingCopyManager();
								ICompilationUnit unit = manager
										.getWorkingCopy(editor.getEditorInput());
								SourceMethod method = (SourceMethod) findEnclosingElement(
										point.x, unit, IJavaElement.METHOD);
								if (word == null || word.charAt(0) != '$'
										|| method == null
										|| !(method instanceof SourceMethod)) {
									refuseLocalVariable();
								} else {
									applySelection((ITextSelection) selection,
											word, point, method);
									if (saveAll()) {
										openWizard();
									}
								}
							} catch (BadLocationException e) {
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Returns the enclosing element of a particular element type,
	 * <code>null</code> if no enclosing element of that type exists.
	 */
	public IJavaElement findEnclosingElement(int start, ICompilationUnit cu,
			int elementType) {
		if (cu == null)
			return null;

		try {
			IJavaElement element = cu.getElementAt(start);
			if (element == null) {
				element = cu;
			}

			return element.getAncestor(elementType);

		} catch (JavaModelException e) {
			return null;
		}
	}

	public void selectionChanged(final IAction action,
			final ISelection selection) {
		this.selection = selection;
	}

	// helping methods
	// ////////////////

	private void applySelection(final ITextSelection textSelection,
			String word, Point point, SourceMethod method) {
		if (word != null) {
			info.setOldName(word);
			info.setNewName(word);
			info.setOffset(point.x);
		} else {
			info.setOldName(textSelection.getText());
			info.setNewName(textSelection.getText());
			info.setOffset(textSelection.getOffset());
		}
		info.setMethod(method);
		info.setSourceFile(getFile());
	}

	private void refuseLocalVariable() {
		String title = UITexts.renameLocalVariable_refuseDlg_title;
		String message = UITexts.renameLocalVariable_refuseDlg_message;
		MessageDialog.openInformation(getShell(), title, message);
	}

	private void refuse() {
		String title = UITexts.renameProperty_refuseDlg_title;
		String message = UITexts.renameProperty_refuseDlg_message;
		MessageDialog.openInformation(getShell(), title, message);
	}

	private static boolean saveAll() {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		return IDE.saveAllEditors(new IResource[] { workspaceRoot }, false);
	}

	private void openWizard() {
		RenameLocalVariableDelegate delegate = new RenameLocalVariableDelegate(
				info);
		RefactoringProcessor processor = new RenamePHPProcessor(info, delegate);
		RenameIdentifierRefactoring ref = new RenameIdentifierRefactoring(
				processor);
		RenameLocalVariableWizard wizard = new RenameLocalVariableWizard(ref,
				info);
		RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(
				wizard);
		try {
			String titleForFailedChecks = ""; //$NON-NLS-1$
			op.run(getShell(), titleForFailedChecks);
		} catch (final InterruptedException irex) {
			// operation was cancelled
		}
	}

	private Shell getShell() {
		Shell result = null;
		if (targetEditor != null) {
			result = targetEditor.getSite().getShell();
		} else {
			result = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getShell();
		}
		return result;
	}

	private final IFile getFile() {
		IFile result = null;
		if (targetEditor instanceof ITextEditor) {
			ITextEditor editor = (ITextEditor) targetEditor;
			IEditorInput input = editor.getEditorInput();
			if (input instanceof IFileEditorInput) {
				result = ((IFileEditorInput) input).getFile();
			}
		}
		return result;
	}
}
