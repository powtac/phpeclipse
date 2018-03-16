// Copyright (c) 2005 by Leif Frenzel. All rights reserved.
// See http://leiffrenzel.de
// modified for phpeclipse.de project by axelcl
package net.sourceforge.phpdt.ltk.core;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import net.sourceforge.phpdt.core.ISourceRange;
import net.sourceforge.phpdt.core.compiler.ITerminalSymbols;
import net.sourceforge.phpdt.core.compiler.InvalidInputException;
import net.sourceforge.phpdt.internal.compiler.parser.Scanner;
import net.sourceforge.phpdt.internal.compiler.parser.SyntaxError;
import net.sourceforge.phpdt.internal.core.SourceMethod;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.IConditionChecker;
import org.eclipse.ltk.core.refactoring.participants.ValidateEditChecker;

/**
 * <p>
 * delegate object that contains the logic used by the processor.
 * </p>
 * 
 */
public class RenameLocalVariableDelegate extends RenameIdentifierDelegate {

	public RenameLocalVariableDelegate(final RenameIdentifierInfo info) {
		super(info);
	}

	RefactoringStatus checkInitialConditions() {
		RefactoringStatus result = new RefactoringStatus();
		IFile sourceFile = info.getSourceFile();
		if (sourceFile == null || !sourceFile.exists()) {
			result.addFatalError(CoreTexts.renamePropertyDelegate_noSourceFile);
		} else if (info.getSourceFile().isReadOnly()) {
			result.addFatalError(CoreTexts.renamePropertyDelegate_roFile);
		} else if (isEmpty(info.getOldName())) {
			// || !isPropertyKey( info.getSourceFile(), info.getOldName() ) ) {
			result.addFatalError(CoreTexts.renamePropertyDelegate_noPHPKey);
		}
		return result;
	}

	RefactoringStatus checkFinalConditions(final IProgressMonitor pm,
			final CheckConditionsContext ctxt) {
		RefactoringStatus result = new RefactoringStatus();
		pm.beginTask(CoreTexts.renamePropertyDelegate_checking, 100);
		// do something long-running here: traverse the entire project (or even
		// workspace) to look for all *.p files with the same bundle
		// base name
		IFile file = info.getSourceFile();
		IProject project = file.getProject();
		try {
			SourceMethod method = info.getMethod();
			ISourceRange range = method.getSourceRange();
			if (project.isNatureEnabled(PHPeclipsePlugin.PHP_NATURE_ID)) {
				determineMethodOffsets(file, range.getOffset(), range
						.getLength(), result);
			}
		} catch (CoreException e) {
			String msg = "Project: " + project.getFullPath().toOSString()
					+ " CoreException " + e.getMessage();
			result.addError(msg);
		} catch (Exception e) {
			String msg = "Project: " + project.getFullPath().toOSString()
					+ " Exception " + e.getMessage();
			result.addError(msg);
		}

		pm.worked(50);

		if (ctxt != null) {
			IFile[] files = new IFile[phpFiles.size()];
			phpFiles.keySet().toArray(files);
			IConditionChecker checker = ctxt
					.getChecker(ValidateEditChecker.class);
			ValidateEditChecker editChecker = (ValidateEditChecker) checker;
			editChecker.addFiles(files);
		}
		pm.done();
		return result;
	}

	protected void createChange(final IProgressMonitor pm,
			final CompositeChange rootChange) {
		try {
			pm.beginTask(CoreTexts.renamePropertyDelegate_collectingChanges,
					100);
			// all files in the same bundle
			rootChange.addAll(createChangesForContainer(pm));
		} finally {
			pm.done();
		}
	}

	private void determineMethodOffsets(final IFile file, int offset,
			int length, final RefactoringStatus status) {
		ArrayList matches = new ArrayList();
		try {
			String content = readFileContent(file, status);

			//
			// Find a PHPdoc directly before the method
			//
			Scanner firstScanner = new Scanner(true, false);
			firstScanner.setSource(content.toCharArray());
			int fToken = ITerminalSymbols.TokenNameEOF;
			int start = 0;
			int phpdocStart = -1;
			try {
				fToken = firstScanner.getNextToken();
				while (fToken != ITerminalSymbols.TokenNameEOF
						&& start < offset) {
					if (fToken == ITerminalSymbols.TokenNameCOMMENT_PHPDOC) {
						phpdocStart = firstScanner
								.getCurrentTokenStartPosition();
					} else {
						phpdocStart = -1;
					}
					fToken = firstScanner.getNextToken();
					start = firstScanner.getCurrentTokenStartPosition();
				}

			} catch (InvalidInputException e) {
				String msg = "File: " + file.getFullPath().toOSString()
						+ " InvalidInputException " + e.getMessage();
				status.addError(msg);
			} catch (SyntaxError e) {
				String msg = "File: " + file.getFullPath().toOSString()
						+ " SyntaxError " + e.getMessage();
				status.addError(msg);
			}

			//
			// Find matches for the word in the PHPdoc+method declaration
			//
			if (phpdocStart >= 0 && phpdocStart < offset) {
				length += offset - phpdocStart;
				offset = phpdocStart;
			}
			String methodString = content.substring(offset, offset + length);
			Scanner secondScanner = new Scanner(true, false);
			secondScanner.setSource(methodString.toCharArray());
			secondScanner.setPHPMode(true);
			String wordStr = info.getOldName();
			boolean renameDQString = info.isRenameDQString();
			boolean renamePHPdoc = info.isRenamePHPdoc();
			boolean renameOtherComments = info.isRenameOtherComments();
			char[] word = wordStr.toCharArray();

			fToken = ITerminalSymbols.TokenNameEOF;
			// double quoted string
			String tokenString;
			// double quoted string offset
			int tokenOffset;
			int index;
			try {
				fToken = secondScanner.getNextToken();
				while (fToken != ITerminalSymbols.TokenNameEOF) {
					if (fToken == ITerminalSymbols.TokenNameVariable) {
						if (secondScanner.equalsCurrentTokenSource(word)) {
							// the current variable token is equal to the given
							// word
							matches.add(new Integer(secondScanner
									.getCurrentTokenStartPosition()
									+ offset));
						}
					} else if (fToken == ITerminalSymbols.TokenNameStringDoubleQuote
							&& renameDQString) {
						// determine the word in double quoted strings:
						tokenString = new String(secondScanner
								.getCurrentTokenSource());
						tokenOffset = secondScanner
								.getCurrentTokenStartPosition();
						index = -1;
						while ((index = tokenString.indexOf(wordStr, index + 1)) >= 0) {
							matches.add(new Integer(offset + tokenOffset
									+ index));
						}
					} else if (fToken == ITerminalSymbols.TokenNameCOMMENT_PHPDOC
							&& renamePHPdoc) {
						tokenString = new String(secondScanner
								.getCurrentTokenSource());
						tokenOffset = secondScanner
								.getCurrentTokenStartPosition();
						index = -1;
						while ((index = tokenString.indexOf(wordStr, index + 1)) >= 0) {
							matches.add(new Integer(offset + tokenOffset
									+ index));
						}
					} else if ((fToken == ITerminalSymbols.TokenNameCOMMENT_BLOCK || fToken == ITerminalSymbols.TokenNameCOMMENT_LINE)
							&& renameOtherComments) {
						tokenString = new String(secondScanner
								.getCurrentTokenSource());
						tokenOffset = secondScanner
								.getCurrentTokenStartPosition();
						index = -1;
						while ((index = tokenString.indexOf(wordStr, index + 1)) >= 0) {
							matches.add(new Integer(offset + tokenOffset
									+ index));
						}
					}
					fToken = secondScanner.getNextToken();
				}

			} catch (InvalidInputException e) {
				String msg = "File: " + file.getFullPath().toOSString()
						+ " InvalidInputException " + e.getMessage();
				status.addError(msg);
			} catch (SyntaxError e) {
				String msg = "File: " + file.getFullPath().toOSString()
						+ " SyntaxError " + e.getMessage();
				status.addError(msg);
			}

		} catch (Exception e) {
			String msg = "File: " + file.getFullPath().toOSString()
					+ " Exception " + e.getMessage();
			status.addError(msg);
		}
		if (matches.size() > 0) {
			phpFiles.put(file, matches);
		}
	}

	private String readFileContent(final IFile file,
			final RefactoringStatus refStatus) {
		String result = null;
		try {
			InputStream is = file.getContents();
			byte[] buf = new byte[1024];
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int len = is.read(buf);
			while (len > 0) {
				bos.write(buf, 0, len);
				len = is.read(buf);
			}
			is.close();
			result = new String(bos.toByteArray());
		} catch (Exception ex) {
			String msg = ex.toString();
			refStatus.addFatalError(msg);
			String pluginId = PHPeclipsePlugin.getPluginId();
			IStatus status = new Status(IStatus.ERROR, pluginId, 0, msg, ex);
			PHPeclipsePlugin.getDefault().getLog().log(status);
		}
		return result;
	}

}
