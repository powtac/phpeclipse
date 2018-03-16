/***********************************************************************************************************************************
 * Copyright (c) 2000, 2002 IBM Corp. and others. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: www.phpeclipse.de
 **********************************************************************************************************************************/
package net.sourceforge.phpeclipse.actions;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.sourceforge.phpdt.internal.ui.util.PHPFileUtil;
import net.sourceforge.phpdt.internal.ui.viewsupport.ListContentProvider;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;
import net.sourceforge.phpeclipse.builder.IdentifierIndexManager;
import net.sourceforge.phpeclipse.builder.PHPIdentifierLocation;
import net.sourceforge.phpeclipse.phpeditor.PHPEditor;
import net.sourceforge.phpeclipse.phpeditor.php.PHPWordExtractor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.dialogs.ListSelectionDialog;

public class OpenDeclarationEditorAction {

	private PHPEditor fEditor;

	private IProject fProject;

	private boolean isIncludeString;

	public OpenDeclarationEditorAction(PHPEditor editor) {
		fEditor = editor;
		fProject = null;
		isIncludeString = false;
	}

	/**
	 * @param selection
	 */
	protected void openSelectedElement(ITextSelection selection) {
		IDocument doc = fEditor.getDocumentProvider().getDocument(
				fEditor.getEditorInput());
		int pos = selection.getOffset();
		openSelectedPosition(doc, pos);
	}

	protected void openSelectedPosition(IDocument doc, int position) {
		IFile f = ((IFileEditorInput) fEditor.getEditorInput()).getFile();
		fProject = f.getProject();
		// System.out.println(selection.getText());
		String identifierOrInclude = getIdentifierOrInclude(doc, position);
		// System.out.println(word);
		if (identifierOrInclude != null && !identifierOrInclude.equals("")) {
			if (isIncludeString) {
				openIncludeFile(identifierOrInclude);
			} else {
				openIdentifierDeclaration(f, identifierOrInclude);
			}
		}
	}

	/**
	 * @param filename
	 */
	private void openIncludeFile(String filename) {
		if (filename != null && !filename.equals("")) {
			try {
				IFile currentFile = ((IFileEditorInput) fEditor
						.getEditorInput()).getFile();
				IPath path = PHPFileUtil.determineFilePath(filename,
						currentFile, fProject);
				if (path != null) {
					//IFile file = PHPFileUtil.createFile(path, fProject);
					//if (file != null && file.exists()) {
					//	PHPeclipsePlugin.getDefault().openFileInTextEditor(
					//			file.getLocation().toString());
					//	return;
					//}
					PHPeclipsePlugin.getDefault().openFileInTextEditor(
							path.toString());
					return;
				}
			} catch (Exception e) {
				// ignore
			}

			try {

				IdentifierIndexManager indexManager = PHPeclipsePlugin
						.getDefault().getIndexManager(fProject);
				// filename = StringUtil.replaceRegExChars(filename);
				List list = indexManager.getFileList(filename);
				if (list != null && list.size() > 0) {
					// String workspaceLocation =
					// PHPeclipsePlugin.getWorkspace().getRoot().getLocation().toString();
					String workspaceLocation = fProject.getFullPath()
							.toString()
							+ java.io.File.separatorChar;

					ListSelectionDialog listSelectionDialog = new ListSelectionDialog(
							PHPeclipsePlugin.getDefault().getWorkbench()
									.getActiveWorkbenchWindow().getShell(),
							list, new ListContentProvider(),
							new LabelProvider(), "Select the includes to open.");
					listSelectionDialog.setTitle("Multiple includes found");
					if (listSelectionDialog.open() == Window.OK) {
						Object[] locations = listSelectionDialog.getResult();
						if (locations != null) {
							try {
								for (int i = 0; i < locations.length; i++) {
									// PHPIdentifierLocation location =
									// (PHPIdentifierLocation)
									// locations[i];
									String openFilename = workspaceLocation
											+ ((String) locations[i]);
									PHPeclipsePlugin.getDefault()
											.openFileInTextEditor(openFilename);
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
		return;
	}

	/**
	 * @param f
	 * @param identiifer
	 */
	private void openIdentifierDeclaration(IFile f, String identiifer) {
		if (identiifer != null && !identiifer.equals("")) {
			IdentifierIndexManager indexManager = PHPeclipsePlugin.getDefault()
					.getIndexManager(fProject);
			List locationsList = indexManager.getLocations(identiifer);
			if (locationsList != null && locationsList.size() > 0) {

				// String workspaceLocation =
				// PHPeclipsePlugin.getWorkspace().getRoot()
				// .getLocation().toString();

				String workspaceLocation = fProject.getFullPath().toString()
						+ java.io.File.separatorChar;
				// TODO show all entries of the list in a dialog box
				// at the moment always the first entry will be opened
				if (locationsList.size() > 1) {
					// determine all includes:
					IncludesScanner includesScanner = new IncludesScanner(
							fProject, (IFileEditorInput) fEditor
									.getEditorInput());
					includesScanner.addFile(f);
					Set exactIncludeSet = includesScanner.getSet();

					PHPIdentifierLocation includeName;
					for (int i = 0; i < locationsList.size(); i++) {
						includeName = (PHPIdentifierLocation) locationsList
								.get(i);
						if (exactIncludeSet.contains(includeName.getFilename())) {
							includeName
									.setMatch(PHPIdentifierLocation.EXACT_MATCH);
						} else {
							includeName
									.setMatch(PHPIdentifierLocation.UNDEFINED_MATCH);
						}
					}
					Collections.sort(locationsList);

					ListSelectionDialog listSelectionDialog = new ListSelectionDialog(
							PHPeclipsePlugin.getDefault().getWorkbench()
									.getActiveWorkbenchWindow().getShell(),
							locationsList, new ListContentProvider(),
							new LabelProvider(),
							"Select the resources to open.");
					listSelectionDialog.setTitle("Multiple declarations found");
					if (listSelectionDialog.open() == Window.OK) {
						Object[] locations = listSelectionDialog.getResult();
						if (locations != null) {
							try {
								for (int i = 0; i < locations.length; i++) {
									PHPIdentifierLocation location = (PHPIdentifierLocation) locations[i];
									String filename = workspaceLocation
											+ location.getFilename();
									// System.out.println(filename);
									if (location.getOffset() >= 0) {
										PHPeclipsePlugin.getDefault()
												.openFileAndGotoOffset(
														filename,
														location.getOffset(),
														identiifer.length());
									} else {
										PHPeclipsePlugin.getDefault()
												.openFileAndFindString(
														filename, identiifer);
									}
								}
							} catch (CoreException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				} else {
					try {
						PHPIdentifierLocation location = (PHPIdentifierLocation) locationsList
								.get(0);
						String filename = workspaceLocation
								+ location.getFilename();
						// System.out.println(filename);
						if (location.getOffset() >= 0) {
							PHPeclipsePlugin.getDefault()
									.openFileAndGotoOffset(filename,
											location.getOffset(),
											identiifer.length());
						} else {
							PHPeclipsePlugin
									.getDefault()
									.openFileAndFindString(filename, identiifer);
						}
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	private String getIdentifierOrInclude(IDocument doc, int pos) {
		// private String getPHPIncludeText(IDocument doc, int pos) {
		Point word = null;
		int start = -1;
		int end = -1;
		isIncludeString = false;
		try {
			// try to find an include string
			int position = pos;
			char character = ' ';

			while (position >= 0) {
				character = doc.getChar(position);
				if ((character == '\"') || (character == '\'')
						|| (character == '\r') || (character == '\n'))
					break;
				--position;
			}
			if ((character == '\"') || (character == '\'')) {
				start = position;

				position = pos;
				int length = doc.getLength();
				character = ' ';
				while (position < length) {
					character = doc.getChar(position);
					if ((character == '\"') || (character == '\'')
							|| (character == '\r') || (character == '\n'))
						break;
					++position;
				}
				if ((character == '\"') || (character == '\'')) {
					start++;
					end = position;

					if (end > start) {
						word = new Point(start, end - start); // include name
																// found
						isIncludeString = true;
					}
				}
			}

			// try to find an identifier
			if (word == null) {
				word = PHPWordExtractor.findWord(doc, pos); // identifier found
				isIncludeString = false;
			}
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