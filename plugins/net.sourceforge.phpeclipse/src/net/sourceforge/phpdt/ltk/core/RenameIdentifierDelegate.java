// Copyright (c) 2005 by Leif Frenzel. All rights reserved.
// See http://leiffrenzel.de
// modified for phpeclipse.de project by axelcl
package net.sourceforge.phpdt.ltk.core;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.phpdt.core.compiler.ITerminalSymbols;
import net.sourceforge.phpdt.core.compiler.InvalidInputException;
import net.sourceforge.phpdt.internal.compiler.parser.Scanner;
import net.sourceforge.phpdt.internal.compiler.parser.SyntaxError;
import net.sourceforge.phpdt.internal.ui.util.PHPFileUtil;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.IConditionChecker;
import org.eclipse.ltk.core.refactoring.participants.ValidateEditChecker;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

/**
 * <p>
 * delegate object that contains the logic used by the processor.
 * </p>
 * 
 */
public class RenameIdentifierDelegate {

	// private static final String EXT_PROPERTIES = "properties"; //$NON-NLS-1$

	protected final RenameIdentifierInfo info;

	// PHP file with the identifier to rename -> offset of the key
	protected final Map phpFiles;

	public RenameIdentifierDelegate(final RenameIdentifierInfo info) {
		this.info = info;
		phpFiles = new HashMap();
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
		// workspace) to look for all *.properties files with the same bundle
		// base name
		IContainer rootContainer;
		IProject project;
		if (info.isAllProjects()) {
			rootContainer = ResourcesPlugin.getWorkspace().getRoot();
			IResource[] members;
			try {
				members = rootContainer.members();
				for (int i = 0; i < members.length; i++) {
					if (members[i] instanceof IProject) {
						project = (IProject) members[i];
						try {
							if (project
									.isNatureEnabled(PHPeclipsePlugin.PHP_NATURE_ID)) {
								search(project, result);
							}
						} catch (CoreException e) {
							String msg = "Project: "
									+ project.getFullPath().toOSString()
									+ " CoreException " + e.getMessage();
							result.addError(msg);
						} catch (Exception e) {
							String msg = "Project: "
									+ project.getFullPath().toOSString()
									+ " Exception " + e.getMessage();
							result.addError(msg);
						}
					}
				}
			} catch (CoreException e) {
				String msg = "Workspace: "
						+ rootContainer.getFullPath().toOSString()
						+ " CoreException " + e.getMessage();
				result.addError(msg);
			}
		} else {
			project = info.getSourceFile().getProject();
			try {
				if (project.isNatureEnabled(PHPeclipsePlugin.PHP_NATURE_ID)) {
					search(project, result);
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
			if (info.isUpdateProject()) {
				rootChange.addAll(createChangesForContainer(pm));
			}
		} finally {
			pm.done();
		}
	}

	// helping methods
	// ////////////////

	// private Change createRenameChange() {
	// // create a change object for the file that contains the property the
	// // user has selected to rename
	// IFile file = info.getSourceFile();
	// TextFileChange result = new TextFileChange(file.getName(), file);
	// // a file change contains a tree of edits, first add the root of them
	// MultiTextEdit fileChangeRootEdit = new MultiTextEdit();
	// result.setEdit(fileChangeRootEdit);
	//
	// // edit object for the text replacement in the file, this is the only
	// child
	// ReplaceEdit edit = new ReplaceEdit(info.getOffset(),
	// info.getOldName().length(), info.getNewName());
	// fileChangeRootEdit.addChild(edit);
	// return result;
	// }

	protected Change[] createChangesForContainer(final IProgressMonitor pm) {
		List result = new ArrayList();
		Iterator it = phpFiles.keySet().iterator();
		int numberOfFiles = phpFiles.size();
		double percent = 100 / numberOfFiles;
		int work = 0;
		int indx = 0;
		while (it.hasNext()) {
			IFile file = (IFile) it.next();
			List list = getKeyOffsets(file);
			if (list != null && list.size() > 0) {
				TextFileChange tfc = new TextFileChange(file.getName(), file);
				MultiTextEdit fileChangeRootEdit = new MultiTextEdit();
				tfc.setEdit(fileChangeRootEdit);

				// edit object for the text replacement in the file, there could
				// be
				// multiple childs
				ReplaceEdit edit;
				for (int i = 0; i < list.size(); i++) {
					edit = new ReplaceEdit(((Integer) list.get(i)).intValue(),
							info.getOldName().length(), info.getNewName());
					fileChangeRootEdit.addChild(edit);
				}
				result.add(tfc);
			}
			work = new Double((++indx) * percent).intValue();
			pm.worked(work);
		}
		return (Change[]) result.toArray(new Change[result.size()]);
	}

	protected boolean isEmpty(final String candidate) {
		return candidate == null || candidate.trim().length() == 0;
	}

	// private boolean isPropertyKey( final IFile file, final String candidate )
	// {
	// boolean result = false;
	// try {
	// Properties props = new Properties();
	// props.load( file.getContents() );
	// result = props.containsKey( candidate );
	// } catch( Exception ex ) {
	// // ignore this, we just assume this is not a favourable situation
	// ex.printStackTrace();
	// }
	// return result;
	// }

	// // whether the file is a PHP file with the same base name as the
	// // one we refactor and contains the key that interests us
	// private boolean isToRefactor(final IFile file) {
	// return PHPFileUtil.isPHPFile(file);
	// // && !file.equals( info.getSourceFile() )
	// // && isPropertyKey( file, info.getOldName() );
	// }

	// private String getBundleBaseName() {
	// String result = info.getSourceFile().getName();
	// int underscoreIndex = result.indexOf( '_' );
	// if( underscoreIndex != -1 ) {
	// result = result.substring( 0, underscoreIndex );
	// } else {
	// int index = result.indexOf( EXT_PROPERTIES ) - 1;
	// result = result.substring( 0, index );
	// }
	// return result;
	// }

	private void search(final IContainer rootContainer,
			final RefactoringStatus status) {
		try {
			IResource[] members = rootContainer.members();
			for (int i = 0; i < members.length; i++) {
				if (members[i] instanceof IContainer) {
					search((IContainer) members[i], status);
				} else {
					IFile file = (IFile) members[i];
					handleFile(file, status);
				}
			}
		} catch (final CoreException cex) {
			status.addFatalError(cex.getMessage());
		}
	}

	private void handleFile(final IFile file, final RefactoringStatus status) {
		if (PHPFileUtil.isPHPFile(file)) {
			determineKeyOffsets(file, status);
			// if (keyOffsets.size() > 0) {
			// Integer offset = new Integer(keyOffsets);
			// phpFiles.put(file, offset);
			// }
		}
	}

	protected List getKeyOffsets(final IFile file) {
		return (List) phpFiles.get(file);
	}

	// finds the offsets of the identifier to rename
	// usually, this would be the job of a proper parser;
	// using a primitive brute-force approach here
	private void determineKeyOffsets(final IFile file,
			final RefactoringStatus status) {
		ArrayList matches = new ArrayList();
		try {
			String content = readFileContent(file, status);
			Scanner scanner = new Scanner(true, false);
			scanner.setSource(content.toCharArray());
			scanner.setPHPMode(false);
			char[] word = info.getOldName().toCharArray();

			int fToken = ITerminalSymbols.TokenNameEOF;
			try {
				fToken = scanner.getNextToken();
				while (fToken != ITerminalSymbols.TokenNameEOF) {
					if (fToken == ITerminalSymbols.TokenNameVariable
							|| fToken == ITerminalSymbols.TokenNameIdentifier) {
						if (scanner.equalsCurrentTokenSource(word)) {
							matches.add(new Integer(scanner
									.getCurrentTokenStartPosition()));
						}
					}
					fToken = scanner.getNextToken();
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

		// int result = -1;
		// int candidateIndex = content.indexOf(info.getOldName());
		// result = candidateIndex;
		// while( result == -1 && candidateIndex != -1 ) {
		// if( isKeyOccurrence( content, candidateIndex ) ) {
		// result = candidateIndex;
		// }
		// }
		// if( result == -1 ) {
		// // still nothing found, we add a warning to the status
		// // (we have checked the file contains the property, so that we can't
		// // determine it's offset is probably because of the rough way
		// employed
		// // here to find it)
		// String msg = CoreTexts.renamePropertyDelegate_propNotFound
		// + file.getLocation().toOSString();
		// status.addWarning( msg );
		// }
		// return result;
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

	// we check only that there is a separator before the next line break (this
	// is not sufficient, the whole thing may be in a comment etc. ...)
	// private boolean isKeyOccurrence( final String content,
	// final int candidateIndex ) {
	// int index = candidateIndex + info.getOldName().length();
	// // skip whitespace
	// while( content.charAt( index ) == ' ' || content.charAt( index ) == '\t'
	// )
	// {
	// index++;
	// }
	// return content.charAt( index ) == '=' || content.charAt( index ) == ':';
	// }
}
