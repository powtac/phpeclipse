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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
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
 * Helper class for exporting resources to the file system.
 */
public class ObfuscatorPass2Exporter implements ITerminalSymbols {
	private Scanner fScanner;

	private int fToken;

	private int fCounter;

	protected HashMap fIdentifierMap;

	public ObfuscatorPass2Exporter(Scanner scanner, HashMap identifierMap) {
		fScanner = scanner;
		fIdentifierMap = identifierMap;
		fCounter = 0;
	}

	/**
	 * gets the next token from input
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

	private boolean obfuscate(StringBuffer buf) {
		char[] ident;
		String identifier;
		PHPIdentifier value;

		int startPosition = 0;
		int lastPosition = 0;

		IPreferenceStore store = PHPeclipsePlugin.getDefault()
				.getPreferenceStore();
		try {
			while (fToken != TokenNameEOF && fToken != TokenNameERROR) {
				if (fToken == TokenNameVariable) {
					identifier = new String(fScanner
							.getCurrentIdentifierSource());
					lastPosition = fScanner.startPosition;
					int len = lastPosition - startPosition;
					buf.append(fScanner.source, startPosition, len);
					value = (PHPIdentifier) fIdentifierMap.get(identifier);
					if (value != null) {
						String obfuscatedIdentifier = value.getIdentifier();
						if (obfuscatedIdentifier == null) {
							buf.append("$v" + Integer.toString(fCounter));
							value.setIdentifier("$v"
									+ Integer.toString(fCounter++));
						} else {
							buf.append(obfuscatedIdentifier);
						}
						// System.out.println(hexString.toString());
					} else {
						buf.append(identifier);
					}
					startPosition = fScanner.currentPosition;
					getNextToken();
				} else if (fToken == TokenNameIdentifier) {
					identifier = new String(fScanner
							.getCurrentIdentifierSource());
					lastPosition = fScanner.startPosition;
					int len = lastPosition - startPosition;
					buf.append(fScanner.source, startPosition, len);
					value = (PHPIdentifier) fIdentifierMap.get(identifier);
					if (value != null) {
						String obfuscatedIdentifier = value.getIdentifier();
						if (obfuscatedIdentifier == null) {
							buf.append("_" + Integer.toString(fCounter));
							value.setIdentifier("_"
									+ Integer.toString(fCounter++));
						} else {
							buf.append(obfuscatedIdentifier);
						}
						// System.out.println(hexString.toString());
					} else {
						buf.append(identifier);
					}
					startPosition = fScanner.currentPosition;
					getNextToken();

				} else if (fToken == TokenNameCOMMENT_LINE
						|| fToken == TokenNameCOMMENT_BLOCK
						|| fToken == TokenNameCOMMENT_PHPDOC) {
					lastPosition = fScanner.startPosition;
					buf.append(fScanner.source, startPosition, lastPosition
							- startPosition);
					startPosition = fScanner.currentPosition;
					getNextToken();
				} else if (fToken == TokenNameStringDoubleQuote) {
					char currentCharacter;
					int i = fScanner.startPosition;
					ArrayList varList = new ArrayList();

					lastPosition = fScanner.startPosition;
					int len = lastPosition - startPosition;
					buf.append(fScanner.source, startPosition, len);

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
					StringBuffer stringLiteral = new StringBuffer();
					stringLiteral.append(fScanner.source,
							fScanner.startPosition, fScanner.currentPosition
									- fScanner.startPosition);
					String stringIdent;
					String replacement;
					int index;

					for (int j = 0; j < varList.size(); j++) {
						stringIdent = (String) varList.get(j);
						len = stringIdent.length();
						value = (PHPIdentifier) fIdentifierMap.get(stringIdent);
						if (value != null) {
							String obfuscatedIdentifier = value.getIdentifier();
							if (obfuscatedIdentifier == null) {
								replacement = "$v" + Integer.toString(fCounter);
								value.setIdentifier("$v"
										+ Integer.toString(fCounter++));
							} else {
								replacement = obfuscatedIdentifier;
							}
							// System.out.println(hexString.toString());
						} else {
							replacement = stringIdent;
						}
						index = stringLiteral.indexOf(stringIdent);
						if (index >= 0) {
							if (index > 0
									&& stringLiteral.charAt(index - 1) != '\\') {
								stringLiteral.replace(index, index
										+ stringIdent.length(), replacement);
							} else if (index == 0) {
								stringLiteral.replace(index, index
										+ stringIdent.length(), replacement);
							}
						}
					}
					buf.append(stringLiteral);
					startPosition = fScanner.currentPosition;
					getNextToken();
				}
				if (fToken == TokenNameMINUS_GREATER) { // i.e. $this->var_name
					getNextToken();
					if (fToken == TokenNameIdentifier) {
						// assuming this is a dereferenced variable
						identifier = new String(fScanner
								.getCurrentIdentifierSource());
						lastPosition = fScanner.startPosition;
						int len = lastPosition - startPosition;
						buf.append(fScanner.source, startPosition, len);
						value = (PHPIdentifier) fIdentifierMap.get("$"
								+ identifier);
						if (value != null && value.isVariable()) {
							String obfuscatedIdentifier = value.getIdentifier();
							if (obfuscatedIdentifier == null) {
								// note: don't place a $ before the identifier
								buf.append("v" + Integer.toString(fCounter));
								value.setIdentifier("$v"
										+ Integer.toString(fCounter++));
							} else {
								if (obfuscatedIdentifier.charAt(0) == '$') {
									buf.append(obfuscatedIdentifier
											.substring(1));
								} else {
									buf.append(obfuscatedIdentifier);
								}
							}
						} else {
							buf.append(identifier);
						}
						startPosition = fScanner.currentPosition;
						getNextToken();
					}

				} else {
					getNextToken();
				}
			}
			if (startPosition < fScanner.source.length) {
				buf.append(fScanner.source, startPosition,
						fScanner.source.length - startPosition);
			}
			return true;
		} catch (SyntaxError sytaxErr) {
			// do nothing
		}

		return false;
	}

	/**
	 * Creates the specified file system directory at
	 * <code>destinationPath</code>. This creates a new file system
	 * directory.
	 */
	public void createFolder(IPath destinationPath) {
		new File(destinationPath.toOSString()).mkdir();
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
	 * Writes the passed file resource to the specified destination on the local
	 * file system
	 */
	protected void writeFile(IFile file, IPath destinationPath)
			throws IOException, CoreException {
		if (PHPFileUtil.isPHPFile(file)) {
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

			fScanner.setSource(charArray);
			fScanner.setPHPMode(false);
			fToken = TokenNameEOF;
			getNextToken();

			StringBuffer buf = new StringBuffer();
			if (!obfuscate(buf)) {
				copyFile(file, destinationPath);
			} else {
				// charArray = buf.toString().toCharArray();
				// File targetFile = new File(destinationPath.toOSString());
				BufferedWriter bw = new BufferedWriter(new FileWriter(
						destinationPath.toOSString()));
				bw.write(buf.toString());
				bw.close();
			}

		} else {
			copyFile(file, destinationPath);
		}
	}

	private void copyFile(IFile file, IPath destinationPath)
			throws FileNotFoundException, CoreException, IOException {
		FileOutputStream output = null;
		InputStream contentStream = null;

		try {
			output = new FileOutputStream(destinationPath.toOSString());
			contentStream = file.getContents(false);
			int chunkSize = contentStream.available();
			byte[] readBuffer = new byte[chunkSize];
			int n = contentStream.read(readBuffer);

			while (n > 0) {
				output.write(readBuffer);
				n = contentStream.read(readBuffer);
			}
		} finally {
			if (output != null)
				output.close();
			if (contentStream != null)
				contentStream.close();
		}
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
