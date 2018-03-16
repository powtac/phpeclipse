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
package net.sourceforge.phpeclipse.obfuscator;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import net.sourceforge.phpdt.core.compiler.ITerminalSymbols;
import net.sourceforge.phpdt.core.compiler.InvalidInputException;
import net.sourceforge.phpdt.internal.compiler.parser.Scanner;
import net.sourceforge.phpdt.internal.compiler.parser.SyntaxError;
import net.sourceforge.phpdt.internal.compiler.util.Util;
import net.sourceforge.phpdt.internal.ui.util.PHPFileUtil;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Analyzing php files in a first pass over all resources !
 */
public class ObfuscatorPass1Exporter implements ITerminalSymbols {

	protected Scanner fScanner;

	protected int fToken;

	protected HashMap fIdentifierMap;

	public ObfuscatorPass1Exporter(Scanner scanner, HashMap identifierMap) {
		fScanner = scanner;
		fIdentifierMap = identifierMap;
	}

	/**
	 * Get the next token from input
	 */
	private void getNextToken() {

		try {
			fToken = fScanner.getNextToken();
			if (Scanner.DEBUG) {
				int currentEndPosition = fScanner.getCurrentTokenEndPosition();
				int currentStartPosition = fScanner
						.getCurrentTokenStartPosition();

				System.out.print(currentStartPosition + ","
						+ currentEndPosition + ": ");
				System.out.println(fScanner.toStringAction(fToken));
			}
			return;
		} catch (InvalidInputException e) {

		}
		fToken = TokenNameERROR;
	}

	private void parseIdentifiers(boolean goBack) {
		char[] ident;
		String identifier;
		PHPIdentifier value;
		int counter = 0;

		IPreferenceStore store = PHPeclipsePlugin.getDefault()
				.getPreferenceStore();
		try {
			while (fToken != TokenNameEOF && fToken != TokenNameERROR) {
				if (fToken == TokenNameVariable) {
					identifier = new String(fScanner
							.getCurrentIdentifierSource());
					value = (PHPIdentifier) fIdentifierMap.get(identifier);
					if (value == null) {
						fIdentifierMap.put(identifier, new PHPIdentifier(null,
								PHPIdentifier.VARIABLE));
					}
					getNextToken();
					// } else if (fToken == TokenNamefunction) {
					// getNextToken();
					// if (fToken == TokenNameAND) {
					// getNextToken();
					// }
					// if (fToken == TokenNameIdentifier) {
					// ident = fScanner.getCurrentIdentifierSource();
					// outlineInfo.addVariable(new String(ident));
					// temp = new PHPFunctionDeclaration(current, new
					// String(ident), fScanner.getCurrentTokenStartPosition());
					// current.add(temp);
					// getNextToken();
					// parseDeclarations(outlineInfo, temp, true);
					// }
					// } else if (fToken == TokenNameclass) {
					// getNextToken();
					// if (fToken == TokenNameIdentifier) {
					// ident = fScanner.getCurrentIdentifierSource();
					// outlineInfo.addVariable(new String(ident));
					// temp = new PHPClassDeclaration(current, new
					// String(ident), fScanner.getCurrentTokenStartPosition());
					// current.add(temp);
					// getNextToken();
					//
					// //skip fTokens for classname, extends and others until we
					// have the opening '{'
					// while (fToken != TokenNameLBRACE && fToken !=
					// TokenNameEOF && fToken != TokenNameERROR) {
					// getNextToken();
					// }
					// parseDeclarations(outlineInfo, temp, true);
					// // stack.pop();
					// }
				} else if (fToken == TokenNameStringDoubleQuote) {
					char currentCharacter;
					int i = fScanner.startPosition;
					ArrayList varList = new ArrayList();

					while (i < fScanner.currentPosition) {
						currentCharacter = fScanner.source[i++];
						if (currentCharacter == '$'
								&& fScanner.source[i - 2] != '\\') {
							StringBuffer varName = new StringBuffer();
							varName.append("$");
							while (i < fScanner.currentPosition) {
								currentCharacter = fScanner.source[i++];
								if (!Scanner
										.isPHPIdentifierPart(currentCharacter)) {
									break; // while loop
								}
								varName.append(currentCharacter);
							}
							varList.add(varName.toString());
						}
					}

					for (i = 0; i < varList.size(); i++) {
						identifier = (String) varList.get(i);
						value = (PHPIdentifier) fIdentifierMap.get(identifier);
						if (value == null) {
							fIdentifierMap.put(identifier, new PHPIdentifier(
									null, PHPIdentifier.VARIABLE));
						}
					}

					getNextToken();
				} else if (fToken == TokenNameLBRACE) {
					getNextToken();
					counter++;
				} else if (fToken == TokenNameRBRACE) {
					getNextToken();
					--counter;
					if (counter == 0 && goBack) {
						return;
					}
				} else {
					getNextToken();
				}
			}
		} catch (SyntaxError sytaxErr) {
			// do nothing
		}
	}

	/**
	 * Do nothing in first pass
	 */
	public void createFolder(IPath destinationPath) {
		// do nothing here
		// new File(destinationPath.toOSString()).mkdir();
	}

	/**
	 * Writes the passed resource to the specified location recursively
	 */
	public void write(IResource resource, IPath destinationPath)
			throws CoreException, IOException {
		if (resource.getType() == IResource.FILE)
			writeFile((IFile) resource, destinationPath);
		else
			writeChildren((IContainer) resource, destinationPath);
	}

	/**
	 * Exports the passed container's children
	 */
	protected void writeChildren(IContainer folder, IPath destinationPath)
			throws CoreException, IOException {
		if (folder.isAccessible()) {
			IResource[] children = folder.members();
			for (int i = 0; i < children.length; i++) {
				IResource child = children[i];
				writeResource(child, destinationPath.append(child.getName()));
			}
		}
	}

	/**
	 * Analyzes the passed file resource for the PHP obfuscator
	 */
	protected void writeFile(IFile file, IPath destinationPath)
			throws IOException, CoreException {
		if (!PHPFileUtil.isPHPFile(file)) {
			return;
		}
		InputStream stream = null;
		char[] charArray = null;
		try {
			stream = new BufferedInputStream(file.getContents());
			charArray = Util.getInputStreamAsCharArray(stream, -1, null);
		} catch (IOException e) {
			return;
		} finally {
			try {
				if (stream != null) {
					stream.close();
				}
			} catch (IOException e) {
			}
		}

		if (charArray == null) {
			// TODO show error message
			return;
		}
		/* fScanner initialization */
		fScanner.setSource(charArray);
		fScanner.setPHPMode(false);
		fToken = TokenNameEOF;
		getNextToken();
		parseIdentifiers(false);
	}

	/**
	 * Writes the passed resource to the specified location recursively
	 */
	protected void writeResource(IResource resource, IPath destinationPath)
			throws CoreException, IOException {
		if (resource.getType() == IResource.FILE)
			writeFile((IFile) resource, destinationPath);
		else {
			createFolder(destinationPath);
			writeChildren((IContainer) resource, destinationPath);
		}
	}
}
