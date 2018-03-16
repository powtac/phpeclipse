// Copyright (c) 2005 by Leif Frenzel. All rights reserved.
// See http://leiffrenzel.de
// modified for phpeclipse.de project by axelcl
package net.sourceforge.phpdt.ltk.ui.actions;

import net.sourceforge.phpdt.internal.ui.util.PHPFileUtil;
import net.sourceforge.phpdt.ltk.core.RenameIdentifierDelegate;
import net.sourceforge.phpdt.ltk.core.RenameIdentifierInfo;
import net.sourceforge.phpdt.ltk.core.RenameIdentifierRefactoring;
import net.sourceforge.phpdt.ltk.core.RenamePHPProcessor;
import net.sourceforge.phpdt.ltk.ui.UITexts;
import net.sourceforge.phpdt.ltk.ui.wizards.RenameIdentifierWizard;
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

/**
 * <p>
 * action that is triggered from the editor context menu.
 * </p>
 * 
 * <p>
 * This action is declared in the <code>plugin.xml</code>.
 * </p>
 * 
 */
public class RenamePHPIdentifier implements IEditorActionDelegate {

	private ISelection selection;

	private IEditorPart targetEditor;

	private boolean onPHPFile;

	private RenameIdentifierInfo info = new RenameIdentifierInfo();

	// interface methods of IEditorActionDelegate
	// ///////////////////////////////////////////

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
							} catch (BadLocationException e) {
							}
						}
					}
				}
				applySelection((ITextSelection) selection, word, point);
				if (saveAll()) {
					openWizard();
				}
			}
		}
	}

	public void selectionChanged(final IAction action,
			final ISelection selection) {
		this.selection = selection;
	}

	// helping methods
	// ////////////////

	private void applySelection(final ITextSelection textSelection,
			String word, Point point) {
		if (word != null) {
			info.setOldName(word);
			info.setNewName(word);
			info.setOffset(point.x);
		} else {
			info.setOldName(textSelection.getText());
			info.setNewName(textSelection.getText());
			info.setOffset(textSelection.getOffset());
		}
		info.setSourceFile(getFile());
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
		RenameIdentifierDelegate delegate = new RenameIdentifierDelegate(info);
		RefactoringProcessor processor = new RenamePHPProcessor(info, delegate);
		RenameIdentifierRefactoring ref = new RenameIdentifierRefactoring(
				processor);
		RenameIdentifierWizard wizard = new RenameIdentifierWizard(ref, info);
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
