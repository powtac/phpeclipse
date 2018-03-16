/*******************************************************************************
 * Copyright (c) 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     IBM Corporation - added #createScanner allowing to make comment check stricter
 ******************************************************************************/
package net.sourceforge.phpdt.core;

import java.util.Map;

import net.sourceforge.phpdt.internal.compiler.parser.Scanner;
import net.sourceforge.phpdt.internal.formatter.CodeFormatter;

import org.eclipse.core.runtime.Plugin;

/**
 * Factory for creating various compiler tools, such as scanners, parsers and
 * compilers.
 * <p>
 * This class provides static methods only; it is not intended to be
 * instantiated or subclassed by clients.
 * </p>
 * 
 * @since 2.0
 */
public class ToolFactory {

	/**
	 * Create an instance of a code formatter. A code formatter implementation
	 * can be contributed via the extension point
	 * "org.phpeclipse.phpdt.core.codeFormatter". If unable to find a registered
	 * extension, the factory will default to using the default code formatter.
	 * 
	 * @return an instance of a code formatter
	 * @see ICodeFormatter
	 * @see ToolFactory#createDefaultCodeFormatter(Map)
	 */
	public static ICodeFormatter createCodeFormatter() {

		Plugin jdtCorePlugin = JavaCore.getPlugin();
		if (jdtCorePlugin == null)
			return null;

		// IExtensionPoint extension =
		// jdtCorePlugin.getDescriptor().getExtensionPoint(JavaModelManager.FORMATTER_EXTPOINT_ID);
		// if (extension != null) {
		// IExtension[] extensions = extension.getExtensions();
		// for(int i = 0; i < extensions.length; i++){
		// IConfigurationElement [] configElements =
		// extensions[i].getConfigurationElements();
		// for(int j = 0; j < configElements.length; j++){
		// try {
		// Object execExt =
		// configElements[j].createExecutableExtension("class"); //$NON-NLS-1$
		// if (execExt instanceof ICodeFormatter){
		// // use first contribution found
		// return (ICodeFormatter)execExt;
		// }
		// } catch(CoreException e){
		// }
		// }
		// }
		// }
		// no proper contribution found, use default formatter
		return createDefaultCodeFormatter(null);
	}

	/**
	 * Create an instance of the built-in code formatter. A code formatter
	 * implementation can be contributed via the extension point
	 * "org.phpeclipse.phpdt.core.codeFormatter". If unable to find a registered
	 * extension, the factory will default to using the default code formatter.
	 * 
	 * @param options -
	 *            the options map to use for formatting with the default code
	 *            formatter. Recognized options are documented on
	 *            <code>JavaCore#getDefaultOptions()</code>. If set to
	 *            <code>null</code>, then use the current settings from
	 *            <code>JavaCore#getOptions</code>.
	 * @return an instance of the built-in code formatter
	 * @see ICodeFormatter
	 * @see ToolFactory#createCodeFormatter()
	 * @see JavaCore#getOptions()
	 */
	public static ICodeFormatter createDefaultCodeFormatter(Map options) {

		if (options == null)
			options = JavaCore.getOptions();
		return new CodeFormatter(options);
	}

	/**
	 * Create a scanner, indicating the level of detail requested for
	 * tokenizing. The scanner can then be used to tokenize some source in a
	 * Java aware way. Here is a typical scanning loop:
	 * 
	 * <code>
	 * <pre>
	 * IScanner scanner = ToolFactory.createScanner(false, false, false, false);
	 * scanner.setSource(&quot;int i = 0;&quot;.toCharArray());
	 * while (true) {
	 * 	int token = scanner.getNextToken();
	 * 	if (token == ITerminalSymbols.TokenNameEOF)
	 * 		break;
	 * 	System.out.println(token + &quot; : &quot;
	 * 			+ new String(scanner.getCurrentTokenSource()));
	 * }
	 * </pre>
	 * </code>
	 * 
	 * <p>
	 * The returned scanner will tolerate unterminated line comments (missing
	 * line separator). It can be made stricter by using API with extra boolean
	 * parameter (<code>strictCommentMode</code>).
	 * <p>
	 * 
	 * @param tokenizeComments
	 *            if set to <code>false</code>, comments will be silently
	 *            consumed
	 * @param tokenizeWhiteSpace
	 *            if set to <code>false</code>, white spaces will be silently
	 *            consumed,
	 * @param assertKeyword
	 *            if set to <code>false</code>, occurrences of 'assert' will
	 *            be reported as identifiers (<code>ITerminalSymbols#TokenNameIdentifier</code>),
	 *            whereas if set to <code>true</code>, it would report assert
	 *            keywords (<code>ITerminalSymbols#TokenNameassert</code>).
	 *            Java 1.4 has introduced a new 'assert' keyword.
	 * @param recordLineSeparator
	 *            if set to <code>true</code>, the scanner will record
	 *            positions of encountered line separator ends. In case of
	 *            multi-character line separators, the last character position
	 *            is considered. These positions can then be extracted using
	 *            <code>IScanner#getLineEnds</code>. Only non-unicode escape
	 *            sequences are considered as valid line separators.
	 * @return a scanner
	 * @see ToolFactory#createScanner(boolean,boolean,boolean,boolean, boolean)
	 * @see org.phpeclipse.phpdt.core.compiler.IScanner
	 */
	// public static IScanner createScanner(boolean tokenizeComments, boolean
	// tokenizeWhiteSpace, boolean recordLineSeparator){
	// return createScanner(tokenizeComments, tokenizeWhiteSpace,
	// recordLineSeparator);
	// }
	/**
	 * Create a scanner, indicating the level of detail requested for
	 * tokenizing. The scanner can then be used to tokenize some source in a
	 * Java aware way. Here is a typical scanning loop:
	 * 
	 * <code>
	 * <pre>
	 * IScanner scanner = ToolFactory.createScanner(false, false, false, false);
	 * scanner.setSource(&quot;int i = 0;&quot;.toCharArray());
	 * while (true) {
	 * 	int token = scanner.getNextToken();
	 * 	if (token == ITerminalSymbols.TokenNameEOF)
	 * 		break;
	 * 	System.out.println(token + &quot; : &quot;
	 * 			+ new String(scanner.getCurrentTokenSource()));
	 * }
	 * </pre>
	 * </code>
	 * 
	 * @param tokenizeComments
	 *            if set to <code>false</code>, comments will be silently
	 *            consumed
	 * @param tokenizeWhiteSpace
	 *            if set to <code>false</code>, white spaces will be silently
	 *            consumed,
	 * @param assertMode
	 *            if set to <code>false</code>, occurrences of 'assert' will
	 *            be reported as identifiers (<code>ITerminalSymbols#TokenNameIdentifier</code>),
	 *            whereas if set to <code>true</code>, it would report assert
	 *            keywords (<code>ITerminalSymbols#TokenNameassert</code>).
	 *            Java 1.4 has introduced a new 'assert' keyword.
	 * @param recordLineSeparator
	 *            if set to <code>true</code>, the scanner will record
	 *            positions of encountered line separator ends. In case of
	 *            multi-character line separators, the last character position
	 *            is considered. These positions can then be extracted using
	 *            <code>IScanner#getLineEnds</code>. Only non-unicode escape
	 *            sequences are considered as valid line separators.
	 * @param strictCommentMode
	 *            if set to <code>true</code>, line comments with no trailing
	 *            line separator will be treated as invalid tokens.
	 * @return a scanner
	 * 
	 * @see org.phpeclipse.phpdt.core.compiler.IScanner
	 * @since 2.1
	 */
	public static Scanner createScanner(boolean tokenizeComments,
			boolean tokenizeWhiteSpace, boolean recordLineSeparator) {

		Scanner scanner = new Scanner(tokenizeComments, tokenizeWhiteSpace,
				false/* nls */);
		scanner.recordLineSeparator = recordLineSeparator;
		return scanner;
	}

	public static Scanner createScanner(boolean tokenizeComments,
			boolean tokenizeWhiteSpace, boolean recordLineSeparator,
			boolean phpMode) {

		Scanner scanner = new Scanner(tokenizeComments, tokenizeWhiteSpace,
				false/* nls */);
		scanner.recordLineSeparator = recordLineSeparator;
		scanner.setPHPMode(phpMode);
		return scanner;
	}
}
