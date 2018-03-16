/**********************************************************************
 Copyright (c) 2000, 2002 IBM Corp. and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html

 Contributors:
 IBM Corporation - Initial implementation
 www.phpeclipse.de
 **********************************************************************/
package net.sourceforge.phpeclipse.phpeditor.php;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.phpdt.internal.ui.text.AbstractJavaScanner;
import net.sourceforge.phpdt.ui.text.IColorManager;
import net.sourceforge.phpeclipse.IPreferenceConstants;
import net.sourceforge.phpeclipse.phpeditor.PHPSyntaxRdr;
import net.sourceforge.phpeclipse.phpeditor.util.PHPWhitespaceDetector;
import net.sourceforge.phpeclipse.phpeditor.util.PHPWordDetector;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

/**
 * PHP Code Scanner
 */
public class PHPCodeScanner extends AbstractJavaScanner {

	/**
	 * Rule to detect java operators.
	 * 
	 * @since 3.0
	 */
	protected class OperatorRule implements IRule {

		/** Java operators */
		private final char[] PHP_OPERATORS = { ';', '(', ')', '.', '=', '/',
				'\\', '+', '-', '*', '[', ']', '<', '>', ':', '?', '!', ',',
				'|', '&', '^', '%', '~', '@' };

		/** Token to return for this rule */
		private final IToken fToken;

		/** Token to return for braces */
		private final IToken fTokenBraces;

		/** Token to return for heredocs */
		private final IToken fTokenHeredoc;

		/**
		 * Creates a new operator rule.
		 * 
		 * @param token
		 *            Token to use for this rule
		 * @param tokenHeredoc
		 *            TODO
		 */
		public OperatorRule(IToken token, IToken tokenBraces,
				IToken tokenHeredoc) {
			fToken = token;
			fTokenBraces = tokenBraces;
			fTokenHeredoc = tokenHeredoc;
		}

		/**
		 * Is this character an operator character?
		 * 
		 * @param character
		 *            Character to determine whether it is an operator character
		 * @return <code>true</code> iff the character is an operator,
		 *         <code>false</code> otherwise.
		 */
		public boolean isOperator(char character) {
			for (int index = 0; index < PHP_OPERATORS.length; index++) {
				if (PHP_OPERATORS[index] == character)
					return true;
			}
			return false;
		}

		/*
		 * @see org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner)
		 */
		public IToken evaluate(ICharacterScanner scanner) {

			int character = scanner.read();
			if (character == '{' || character == '}') {
				return fTokenBraces;
			}
			if (isOperator((char) character)) {
				int lastCharacter = character;
				character = scanner.read();
				// the readHEREDOC(scanner) call doesn't work, if we have our
				// own partitions for single quoted
				// or double quoted strings:
				//
				// if (lastCharacter == '<' && character == '<') {
				// int heredocCharacter = scanner.read();
				// if (heredocCharacter == '<') {
				// // start of heredoc comment;
				// if (readHEREDOC(scanner)) {
				// return fTokenHeredoc;
				// }
				// } else {
				// scanner.unread();
				// }
				// }
				if (!isOperator((char) character)) {
					scanner.unread();
					return fToken;
				}
				if (checkPHPTag(scanner, lastCharacter, character)) {
					return Token.UNDEFINED;
				}
				do {
					lastCharacter = character;
					character = scanner.read();
					if (checkPHPTag(scanner, lastCharacter, character)) {
						return fToken;
					}
					if (character == ICharacterScanner.EOF) {
						return fToken;
					}
				} while (isOperator((char) character));
				scanner.unread();
				return fToken;
			} else {
				scanner.unread();
				return Token.UNDEFINED;
			}
		}

		// private boolean readHEREDOC(ICharacterScanner scanner) {
		// // search until heredoc ends
		// int ch;
		// StringBuffer buf = new StringBuffer();
		// char[] heredocIdent;
		//
		// ch = scanner.read();
		// if (!Scanner.isPHPIdentifierStart((char)ch)) {
		// scanner.unread();
		// scanner.unread();
		// return false;
		// }
		// while (Scanner.isPHPIdentifierPart((char)ch)) {
		// buf.append((char)ch);
		// ch = scanner.read();
		// }
		// if (ch==ICharacterScanner.EOF) {
		// return true;
		// }
		// heredocIdent = buf.toString().toCharArray();
		// while (true) {
		// ch = scanner.read();
		// if (ch==ICharacterScanner.EOF) {
		// return true;
		// }
		// if (ch == '\n') { // heredoc could end after a newline
		// int pos = 0;
		// while (true) {
		// if (pos == heredocIdent.length) {
		// return true;
		// }
		// ch = scanner.read(); // ignore escaped character
		// if (ch != heredocIdent[pos]) {
		// break;
		// }
		// if (ch==ICharacterScanner.EOF) {
		// return true;
		// }
		// pos++;
		// }
		// }
		// }
		// }

		/**
		 * Check if lastCharacter/character are a PHP start or end token ( &lt;?
		 * ... ?&gt; )
		 * 
		 * @param scanner
		 * @param lastCharacter
		 * @param character
		 * @return
		 */
		private boolean checkPHPTag(ICharacterScanner scanner,
				int lastCharacter, int character) {
			if (lastCharacter == '<' && character == '?') {
				scanner.unread();
				scanner.unread();
				return true;
			} else if (lastCharacter == '?' && character == '>') {
				scanner.unread();
				scanner.unread();
				return true;
			}
			return false;
		}
	}

	protected class AccentStringRule implements IRule {

		/** Token to return for this rule */
		private final IToken fToken;

		public AccentStringRule(IToken token) {
			fToken = token;

		}

		/*
		 * @see org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner)
		 */
		public IToken evaluate(ICharacterScanner scanner) {

			int character = scanner.read();

			if (character == '`') {

				while (character != ICharacterScanner.EOF) {
					character = scanner.read();
					if (character == '\\') {
						character = scanner.read();
					} else if (character == '`') {
						return fToken;
					}
				}
				scanner.unread();
				return Token.UNDEFINED;
			} else {
				scanner.unread();
				return Token.UNDEFINED;
			}
		}

	}

	private class PHPWordRule extends WordRule {
		private StringBuffer fBuffer = new StringBuffer();

		protected Map fWordsIgnoreCase = new HashMap();

		public PHPWordRule(IWordDetector detector) {
			super(detector, Token.UNDEFINED);
		}

		public PHPWordRule(IWordDetector detector, IToken defaultToken) {
			super(detector, defaultToken);
		}

		/**
		 * Adds a word and the token to be returned if it is detected.
		 * 
		 * @param word
		 *            the word this rule will search for, may not be
		 *            <code>null</code>
		 * @param token
		 *            the token to be returned if the word has been found, may
		 *            not be <code>null</code>
		 */
		public void addWordIgnoreCase(String word, IToken token) {
			Assert.isNotNull(word);
			Assert.isNotNull(token);

			fWordsIgnoreCase.put(word, token);
		}

		public IToken evaluate(ICharacterScanner scanner) {
			int c = scanner.read();
			boolean isVariable = false;
			boolean isUnderscore = false;
			String word;
			if (c == '<') {
				c = scanner.read();
				if (c != '?') {
					scanner.unread();
					scanner.unread();
					return Token.UNDEFINED;
				} else {
					c = scanner.read();
					if (c == '=') { // <?=
						return getToken(IPreferenceConstants.PHP_TAG);
					}
					if (c != 'p' && c != 'P') {
						scanner.unread();
						return getToken(IPreferenceConstants.PHP_TAG);
					} else {
						c = scanner.read();
						if (c != 'h' && c != 'H') {
							scanner.unread();
							scanner.unread();
							return getToken(IPreferenceConstants.PHP_TAG);
						} else {
							c = scanner.read();
							if (c != 'p' && c != 'P') {
								scanner.unread();
								scanner.unread();
								scanner.unread();
								return getToken(IPreferenceConstants.PHP_TAG);
							} else {
								return getToken(IPreferenceConstants.PHP_TAG);
							}
						}
					}
				}
			}
			if (c == '?') {
				c = scanner.read();
				if (c == '>') {
					return getToken(IPreferenceConstants.PHP_TAG);
				}
				scanner.unread();
				scanner.unread();
				return Token.UNDEFINED;
			}
			if (fDetector.isWordStart((char) c)) {
				if (c == '$') {
					isVariable = true;
				}
				if (fColumn == UNDEFINED
						|| (fColumn == scanner.getColumn() - 1)) {

					fBuffer.setLength(0);
					fBuffer.append((char) c);
					c = scanner.read();
					if (c == '_') {
						isUnderscore = true;
					}
					while (c != ICharacterScanner.EOF
							&& fDetector.isWordPart((char) c)) {
						fBuffer.append((char) c);
						c = scanner.read();
					}
					scanner.unread();

					if (isVariable) {
						if (isUnderscore) {
							return getToken(IPreferenceConstants.PHP_VARIABLE_DOLLAR);
						}
						return getToken(IPreferenceConstants.PHP_VARIABLE);
					}
					word = fBuffer.toString();
					IToken token = (IToken) fWords.get(word);
					if (token != null)
						return token;

					token = (IToken) fWordsIgnoreCase.get(word.toLowerCase());
					if (token != null)
						return token;

					if (fDefaultToken.isUndefined())
						unreadBuffer(scanner);

					return fDefaultToken;
				}
			}

			scanner.unread();
			return Token.UNDEFINED;
		}
	}

	// private PHPColorProvider fColorProvider;

	private static String[] fgTokenProperties = {
			IPreferenceConstants.PHP_MULTILINE_COMMENT,
			IPreferenceConstants.PHP_SINGLELINE_COMMENT,
			IPreferenceConstants.PHP_TAG, IPreferenceConstants.PHP_KEYWORD,
			IPreferenceConstants.PHP_FUNCTIONNAME,
			IPreferenceConstants.PHP_VARIABLE,
			IPreferenceConstants.PHP_VARIABLE_DOLLAR,
			IPreferenceConstants.PHP_STRING_DQ,
			IPreferenceConstants.PHP_STRING_SQ, IPreferenceConstants.PHP_TYPE,
			IPreferenceConstants.PHP_CONSTANT,
			IPreferenceConstants.PHP_DEFAULT,
			IPreferenceConstants.PHP_OPERATOR,
			IPreferenceConstants.PHP_BRACE_OPERATOR,
			IPreferenceConstants.PHP_KEYWORD_RETURN };

	/**
	 * Creates a PHP code scanner
	 */
	// public PHPCodeScanner(JavaColorManager provider, IPreferenceStore store)
	// {
	public PHPCodeScanner(IColorManager manager, IPreferenceStore store) {
		super(manager, store);
		initialize();
	}

	/*
	 * @see AbstractJavaScanner#getTokenProperties()
	 */
	protected String[] getTokenProperties() {
		return fgTokenProperties;
	}

	/*
	 * @see AbstractJavaScanner#createRules()
	 */
	protected List createRules() {
		List rules = new ArrayList();
		Token token = getToken(IPreferenceConstants.PHP_SINGLELINE_COMMENT);
		// Add rule for single line comments.
		// rules.add(new EndOfLineRule("//", token)); //$NON-NLS-1$
		// rules.add(new EndOfLineRule("#", token)); //$NON-NLS-1$
		// Add rule for strings and character constants.
		// token = getToken(IPreferenceConstants.PHP_STRING_SQ);
		// rules.add(new SingleQuoteStringRule(token));
		// token = getToken(IPreferenceConstants.PHP_STRING_DQ);
		// rules.add(new DoubleQuoteStringRule(token));
		rules.add(new AccentStringRule(token));

		token = getToken(IPreferenceConstants.PHP_MULTILINE_COMMENT);
		rules.add(new MultiLineRule("/*", "*/", token)); //$NON-NLS-2$ //$NON-NLS-1$
		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new PHPWhitespaceDetector()));
		// Add word rule for keywords, types, and constants.
		token = getToken(IPreferenceConstants.PHP_DEFAULT);
		PHPWordRule wordRule = new PHPWordRule(new PHPWordDetector(), token);

		Token keyword = getToken(IPreferenceConstants.PHP_KEYWORD);
		Token functionName = getToken(IPreferenceConstants.PHP_FUNCTIONNAME);
		Token type = getToken(IPreferenceConstants.PHP_TYPE);
		Token constant = getToken(IPreferenceConstants.PHP_CONSTANT);

		ArrayList buffer = PHPSyntaxRdr.getSyntaxData();
		// String strbuffer = null; unused
		PHPElement elbuffer = null;
		String name;
		for (int i = 0; i < buffer.size(); i++) {
			// while ((buffer != null)
			// && (!buffer.isEmpty()
			// && ((elbuffer = (PHPElement) buffer.remove(0)) != null))) {
			elbuffer = (PHPElement) buffer.get(i);
			if (elbuffer instanceof PHPKeyword) {
				name = ((PHPKeyword) elbuffer).getName();
				if (!name.equals("return")) {
					wordRule.addWord(name, keyword);
				}
			} else if (elbuffer instanceof PHPFunction) {
				wordRule.addWordIgnoreCase(((PHPFunction) elbuffer).getName(),
						functionName);
			} else if (elbuffer instanceof PHPType) {
				wordRule.addWord(elbuffer.getName(), type);
			} else if (elbuffer instanceof PHPConstant) {
				wordRule.addWord(elbuffer.getName(), constant);
			}
		}

		// Add word rule for keyword 'return'.
		token = getToken(IPreferenceConstants.PHP_KEYWORD_RETURN);
		wordRule.addWord("return", token);

		// Add rule for operators and brackets (at the end !)
		rules.add(new OperatorRule(getToken(IPreferenceConstants.PHP_OPERATOR),
				getToken(IPreferenceConstants.PHP_BRACE_OPERATOR),
				getToken(IPreferenceConstants.PHP_STRING_DQ)));

		rules.add(wordRule);

		setDefaultReturnToken(getToken(IPreferenceConstants.PHP_DEFAULT));
		return rules;
	}
}