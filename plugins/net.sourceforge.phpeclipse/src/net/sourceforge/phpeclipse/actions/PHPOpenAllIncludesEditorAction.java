/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corp. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: www.phpeclipse.de
 ******************************************************************************/
package net.sourceforge.phpeclipse.actions;

import java.io.File;
import java.util.List;

import net.sourceforge.phpdt.internal.ui.viewsupport.ListContentProvider;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;
import net.sourceforge.phpeclipse.phpeditor.PHPEditor;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.dialogs.ListSelectionDialog;

public class PHPOpenAllIncludesEditorAction extends ActionDelegate implements
		IEditorActionDelegate {

	private IWorkbenchWindow fWindow;

	private PHPEditor fEditor;

	private IProject fProject;

	private IncludesScanner fIncludesScanner;

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
		this.fWindow = window;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (!selection.isEmpty()) {
			if (selection instanceof TextSelection) {
				action.setEnabled(true);
			} else if (fWindow.getActivePage() != null
					&& fWindow.getActivePage().getActivePart() != null) {
				//
			}
		}
	}

	private IWorkbenchPage getActivePage() {
		IWorkbenchWindow workbenchWindow = fEditor.getEditorSite()
				.getWorkbenchWindow();
		IWorkbenchPage page = workbenchWindow.getActivePage();
		return page;
	}

	public IContainer getWorkingLocation(IFileEditorInput editorInput) {
		if (editorInput == null || editorInput.getFile() == null) {
			return null;
		}
		return editorInput.getFile().getParent();
	}

	private IFile getIncludeFile(IProject project,
			IFileEditorInput editorInput, String relativeFilename) {
		IContainer container = getWorkingLocation(editorInput);
		String fullPath = project.getFullPath().toString();
		IFile file = null;
		if (relativeFilename.startsWith("../")) {
			Path path = new Path(relativeFilename);
			file = container.getFile(path);
			return file;
		}
		int index = relativeFilename.lastIndexOf('/');

		if (index >= 0) {
			Path path = new Path(relativeFilename);
			file = project.getFile(path);
			if (file.exists()) {
				return file;
			}
		}

		Path path = new Path(relativeFilename);
		file = container.getFile(path);

		return file;
	}

	public void run(IAction action) {
		if (fEditor == null) {
			IEditorPart targetEditor = fWindow.getActivePage()
					.getActiveEditor();
			if (targetEditor != null && (targetEditor instanceof PHPEditor)) {
				fEditor = (PHPEditor) targetEditor;
			}
		}
		if (fEditor != null) {
			// determine the current Project from a (file-based) Editor
			IFile f = ((IFileEditorInput) fEditor.getEditorInput()).getFile();
			fProject = f.getProject();
			// System.out.println(fProject.toString());

			ITextSelection selection = (ITextSelection) fEditor
					.getSelectionProvider().getSelection();
			IDocument doc = fEditor.getDocumentProvider().getDocument(
					fEditor.getEditorInput());
			fIncludesScanner = new IncludesScanner(fProject,
					(IFileEditorInput) fEditor.getEditorInput());
			int pos = selection.getOffset();
			// System.out.println(selection.getText());
			String filename = getPHPIncludeText(doc, pos);

			if (filename != null && !filename.equals("")) {
				try {
					IFile file = fIncludesScanner.getIncludeFile(filename);
					fIncludesScanner.addFile(file);
				} catch (Exception e) {
					// ignore
				}

				try {

					List list = fIncludesScanner.getList();
					if (list != null && list.size() > 0) {
						// String workspaceLocation =
						// PHPeclipsePlugin.getWorkspace().getRoot().getLocation().toString();
						String workspaceLocation = fProject.getFullPath()
								.toString()
								+ File.separatorChar;

						ListSelectionDialog listSelectionDialog = new ListSelectionDialog(
								PHPeclipsePlugin.getDefault().getWorkbench()
										.getActiveWorkbenchWindow().getShell(),
								list, new ListContentProvider(),
								new LabelProvider(),
								"Select the includes to open.");
						listSelectionDialog.setTitle("Multiple includes found");
						if (listSelectionDialog.open() == Window.OK) {
							Object[] locations = listSelectionDialog
									.getResult();
							if (locations != null) {
								try {
									for (int i = 0; i < locations.length; i++) {
										// PHPIdentifierLocation location =
										// (PHPIdentifierLocation)
										// locations[i];
										String openFilename = workspaceLocation
												+ ((String) locations[i]);
										PHPeclipsePlugin.getDefault()
												.openFileInTextEditor(
														openFilename);
									}
								} catch (CoreException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}

					}
				} catch (Exception e) {
				}

			}
		}
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor != null && (targetEditor instanceof PHPEditor)) {
			fEditor = (PHPEditor) targetEditor;
		}
	}

	private String getPHPIncludeText(IDocument doc, int pos) {
		Point word = null;
		int start = -1;
		int end = -1;

		try {

			int position = pos;
			char character;

			while (position >= 0) {
				character = doc.getChar(position);
				if ((character == '\"') || (character == '\'')
						|| (character == '\r') || (character == '\n'))
					break;
				--position;
			}

			start = position;

			position = pos;
			int length = doc.getLength();

			while (position < length) {
				character = doc.getChar(position);
				if ((character == '\"') || (character == '\'')
						|| (character == '\r') || (character == '\n'))
					break;
				++position;
			}

			start++;
			end = position;

			if (end > start)
				word = new Point(start, end - start);

		} catch (BadLocationException x) {
		}

		if (word != null) {
			try {
				return doc.get(word.x, word.y);
			} catch (BadLocationException e) {
			}
		}
		return "";
	}
}