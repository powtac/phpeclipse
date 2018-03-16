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
import java.util.List;

import net.sourceforge.phpdt.internal.ui.text.AbstractJavaScanner;
import net.sourceforge.phpdt.ui.text.IColorManager;
import net.sourceforge.phpeclipse.IPreferenceConstants;
import net.sourceforge.phpeclipse.phpeditor.util.HTMLWordDetector;
import net.sourceforge.phpeclipse.phpeditor.util.PHPColorProvider;
import net.sourceforge.phpeclipse.phpeditor.util.PHPWhitespaceDetector;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

/**
 * A HTML code scanner.
 */
public class HTMLCodeScanner extends AbstractJavaScanner {

	// private static String[] fgKeywords = {
	// };
	//
	// private static String[] fgTypes = {
	// };

	// private IToken keyword;
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

	/*
	 * @see AbstractJavaScanner#getTokenProperties()
	 */
	protected String[] getTokenProperties() {
		return fgTokenProperties;
	}

	private class HTMLWordRule extends WordRule {
		private StringBuffer fBuffer = new StringBuffer();

		public HTMLWordRule(IWordDetector detector) {
			super(detector, Token.UNDEFINED);
		}

		public HTMLWordRule(IWordDetector detector, IToken defaultToken) {
			super(detector, defaultToken);
		}

		public IToken evaluate(ICharacterScanner scanner) {
			int c = scanner.read();
			boolean tagBegin = false;
			if (fDetector.isWordStart((char) c)) {
				if (c == '<') {
					tagBegin = true;
				}
				if (fColumn == UNDEFINED
						|| (fColumn == scanner.getColumn() - 1)) {

					fBuffer.setLength(0);
					do {
						fBuffer.append((char) c);
						c = scanner.read();
						if (c == '>') {
							fBuffer.append((char) c);
							c = scanner.read();
							break;
						}
						if (c == '/' && (fBuffer.length() > 2)) {
							break;
						}
					} while (c != ICharacterScanner.EOF
							&& fDetector.isWordPart((char) c));
					scanner.unread();

					if (tagBegin) {
						return getToken(IPreferenceConstants.PHP_KEYWORD);
					}
					IToken token = (IToken) fWords.get(fBuffer.toString());
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

	// private static String[] fgConstants = { "__LINE__", "__FILE__", "true",
	// "false" };
	private TextAttribute fComment;

	private TextAttribute fKeyword;

	private TextAttribute fType;

	private TextAttribute fString;

	private PHPColorProvider fColorProvider;

	/**
	 * Creates a Java code scanner
	 */
	public HTMLCodeScanner(IColorManager manager, IPreferenceStore store) {
		super(manager, store);
		initialize();
	}

	/*
	 * @see AbstractJavaScanner#createRules()
	 */
	protected List createRules() {
		List rules = new ArrayList();

		// keyword = new Token(new
		// TextAttribute(provider.getColor(PHPColorProvider.KEYWORD)));
		// IToken type = new Token(new
		// TextAttribute(provider.getColor(PHPColorProvider.FUNCTION_NAME)));
		// IToken string = new Token(new
		// TextAttribute(provider.getColor(PHPColorProvider.STRING_DQ)));
		// IToken comment = new Token(new
		// TextAttribute(provider.getColor(PHPColorProvider.SINGLE_LINE_COMMENT)));
		// IToken multi_comment = new Token(new
		// TextAttribute(provider.getColor(PHPColorProvider.MULTI_LINE_COMMENT)));
		// IToken other = new Token(new
		// TextAttribute(provider.getColor(PHPColorProvider.DEFAULT)));

		// variable = new Token(new
		// TextAttribute(provider.getColor(PHPColorProvider.VARIABLE)));

		// Add rule for single line comments.
		// rules.add(new EndOfLineRule("//", comment)); //$NON-NLS-1$
		// rules.add(new EndOfLineRule("#", comment));

		// Add rule for strings and character constants.
		rules.add(new SingleLineRule(
				"\"", "\"", getToken(IPreferenceConstants.PHP_STRING_DQ))); //$NON-NLS-2$ //$NON-NLS-1$
		// rules.add(new SingleLineRule("'", "'", string, '\\')); //$NON-NLS-2$
		// //$NON-NLS-1$

		// rules.add(new SingleLineRule("//", "//", php_comment));
		// rules.add(new MultiLineRule("/*", "*/", multi_comment));

		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new PHPWhitespaceDetector()));

		// Add word rule for keywords, types, and constants.
		HTMLWordRule wordRule = new HTMLWordRule(new HTMLWordDetector(),
				getToken(IPreferenceConstants.PHP_DEFAULT));
		// for (int i = 0; i < fgKeywords.length; i++)
		// wordRule.addWord(fgKeywords[i], keyword);
		// for (int i = 0; i < fgTypes.length; i++)
		// wordRule.addWord(fgTypes[i], type);
		rules.add(wordRule);

		setDefaultReturnToken(getToken(IPreferenceConstants.PHP_DEFAULT));
		return rules;
	}
}
