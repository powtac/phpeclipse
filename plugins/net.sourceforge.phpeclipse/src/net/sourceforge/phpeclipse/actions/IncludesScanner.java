package net.sourceforge.phpeclipse.actions;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sourceforge.phpdt.core.compiler.ITerminalSymbols;
import net.sourceforge.phpdt.core.compiler.InvalidInputException;
import net.sourceforge.phpdt.internal.compiler.parser.Scanner;
import net.sourceforge.phpdt.internal.compiler.parser.SyntaxError;
import net.sourceforge.phpdt.internal.compiler.util.Util;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IFileEditorInput;

public class IncludesScanner implements ITerminalSymbols {
	// private final PHPOpenAllIncludesEditorAction fOpenAllIncludesAction;
	private IProject fProject;

	private IFileEditorInput fEditorInput;

	private HashSet fSet;

	public IncludesScanner(IProject project, IFileEditorInput editorInput) {
		fProject = project;
		// fOpenAllIncludesAction = action;
		fEditorInput = editorInput;
		fSet = new HashSet();
	}

	/**
	 * Add the information for a given IFile resource
	 * 
	 */
	public void addFile(IFile fileToParse) {

		try {
			if (fileToParse.exists()) {
				addInputStream(new BufferedInputStream(fileToParse
						.getContents()), fileToParse.getProjectRelativePath()
						.toString());
			}
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
	}

	private void addInputStream(InputStream stream, String filePath)
			throws CoreException {
		try {
			if (fSet.add(filePath)) { // new entry in set
				parseIdentifiers(Util.getInputStreamAsCharArray(stream, -1,
						null));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stream != null) {
					stream.close();
				}
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Get the next token from input
	 */
	private int getNextToken(Scanner scanner) {
		int token;
		try {
			token = scanner.getNextToken();
			if (Scanner.DEBUG) {
				int currentEndPosition = scanner.getCurrentTokenEndPosition();
				int currentStartPosition = scanner
						.getCurrentTokenStartPosition();
				System.out.print(currentStartPosition + ","
						+ currentEndPosition + ": ");
				System.out.println(scanner.toStringAction(token));
			}
			return token;
		} catch (InvalidInputException e) {
		}
		return TokenNameERROR;
	}

	private void parseIdentifiers(char[] charArray) {
		IFile file;
		Scanner scanner = new Scanner(false, false, false, false, true, null,
				null, true /* taskCaseSensitive */);
		scanner.setSource(charArray);
		scanner.setPHPMode(false);
		int token = getNextToken(scanner);
		try {
			while (token != TokenNameEOF) { // && fToken != TokenNameERROR) {
				if (token == TokenNameinclude || token == TokenNameinclude_once
						|| token == TokenNamerequire
						|| token == TokenNamerequire_once) {
					while (token != TokenNameEOF && token != TokenNameERROR
							&& token != TokenNameSEMICOLON
							&& token != TokenNameRPAREN
							&& token != TokenNameLBRACE
							&& token != TokenNameRBRACE) {
						token = getNextToken(scanner);
						if (token == TokenNameStringDoubleQuote
								|| token == TokenNameStringSingleQuote) {
							char[] includeName = scanner
									.getCurrentStringLiteralSource();
							try {
							    System.out.println(includeName);
								file = getIncludeFile(new String(includeName));
								addFile(file);
							} catch (Exception e) {
								// ignore
							}
							break;
						}
					}
				}
				token = getNextToken(scanner);
			}
		} catch (SyntaxError e) {
			// e.printStackTrace();
		}
	}

	private IContainer getWorkingLocation(IFileEditorInput editorInput) {
		if (editorInput == null || editorInput.getFile() == null) {
			return null;
		}
		return editorInput.getFile().getParent();
	}

	public IFile getIncludeFile(String relativeFilename) {
		IContainer container = getWorkingLocation(fEditorInput);
		IFile file = null;
		if (relativeFilename.startsWith("../")) {
			Path path = new Path(relativeFilename);
			file = container.getFile(path);
			return file;
		}
		int index = relativeFilename.lastIndexOf('/');

		if (index >= 0) {
			Path path = new Path(relativeFilename);
			file = fProject.getFile(path);
			if (file.exists()) {
				return file;
			}
		}
		Path path = new Path(relativeFilename);
		file = container.getFile(path);

		return file;
	}

	/**
	 * Returns a list of includes
	 * 
	 * @return the determined list of includes
	 */
	public List getList() {
		ArrayList list = new ArrayList();
		list.addAll(fSet);
		return list;
	}

	/**
	 * @return Returns the set.
	 */
	public Set getSet() {
		return fSet;
	}
}