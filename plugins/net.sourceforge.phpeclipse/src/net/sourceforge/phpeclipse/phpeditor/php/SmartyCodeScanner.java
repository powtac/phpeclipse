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
import net.sourceforge.phpeclipse.phpeditor.util.PHPWhitespaceDetector;
import net.sourceforge.phpeclipse.phpeditor.util.PHPWordDetector;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

/**
 * PHP Code Scanner
 */
public class SmartyCodeScanner extends AbstractJavaScanner {
	public static String[] SMARTY_KEYWORDS = { "capture", "config_load",
			"else", "elseif", "foreach", "foreachelse", "if", "include",
			"insert", "ldelim", "literal", "php", "rdelim", "section",
			"sectionelse", "strip" };

	public static String[] SMARTY_FUNCTION_NAMES = { "assign", "counter",
			"cycle", "debug", "eval", "fetch", "html_checkboxes", "html_image",
			"html_options", "html_radios", "html_select_date",
			"html_select_time", "html_table", "math", "popup", "popup_init",
			"textformat" };

	private class SmartyWordRule extends WordRule {
		private StringBuffer fBuffer = new StringBuffer();

		public SmartyWordRule(IWordDetector detector) {
			super(detector, Token.UNDEFINED);
		}

		public SmartyWordRule(IWordDetector detector, IToken defaultToken) {
			super(detector, defaultToken);
		}

		public IToken evaluate(ICharacterScanner scanner) {
			int c = scanner.read();
			boolean isVariable = false;
			if (c == '{') {
				c = scanner.read();
				if (c != '/') {
					scanner.unread();
				}
				return getToken(IPreferenceConstants.PHP_TAG);
			}
			if (c == '}') {
				return getToken(IPreferenceConstants.PHP_TAG);
			}
			if (fDetector.isWordStart((char) c)) {
				if (c == '$') {
					isVariable = true;
				}
				if (fColumn == UNDEFINED
						|| (fColumn == scanner.getColumn() - 1)) {

					fBuffer.setLength(0);
					do {
						fBuffer.append((char) c);
						c = scanner.read();
					} while (c != ICharacterScanner.EOF
							&& fDetector.isWordPart((char) c));
					scanner.unread();

					if (isVariable) {
						return getToken(IPreferenceConstants.PHP_VARIABLE);
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

	private static String[] fgTokenProperties = {
			IPreferenceConstants.PHP_MULTILINE_COMMENT,
			IPreferenceConstants.PHP_SINGLELINE_COMMENT,
			IPreferenceConstants.PHP_TAG, IPreferenceConstants.PHP_KEYWORD,
			IPreferenceConstants.PHP_FUNCTIONNAME,
			IPreferenceConstants.PHP_VARIABLE,
			IPreferenceConstants.PHP_VARIABLE_DOLLAR,
			IPreferenceConstants.PHP_STRING_DQ,
			IPreferenceConstants.PHP_STRING_SQ, IPreferenceConstants.PHP_TYPE,
			IPreferenceConstants.PHP_CONSTANT, IPreferenceConstants.PHP_DEFAULT };

	/**
	 * Creates a PHP code scanner
	 */
	// public PHPCodeScanner(JavaColorManager provider, IPreferenceStore store)
	// {
	public SmartyCodeScanner(IColorManager manager, IPreferenceStore store) {
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
		// Add rule for strings and character constants.
		Token token = getToken(IPreferenceConstants.PHP_STRING_DQ);
		rules.add(new MultiLineRule("\"", "\"", token, '\\')); //$NON-NLS-2$ //$NON-NLS-1$

		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new PHPWhitespaceDetector()));
		// Add word rule for keywords, types, and constants.
		token = getToken(IPreferenceConstants.PHP_DEFAULT);

		SmartyWordRule wordRule = new SmartyWordRule(new PHPWordDetector(),
				token);

		Token keyword = getToken(IPreferenceConstants.PHP_KEYWORD);
		Token functionName = getToken(IPreferenceConstants.PHP_FUNCTIONNAME);
		// Token type = getToken(IPreferenceConstants.PHP_TYPE);
		// Token constant = getToken(IPreferenceConstants.PHP_CONSTANT);

		for (int i = 0; i < SMARTY_KEYWORDS.length; i++) {
			wordRule.addWord(SMARTY_KEYWORDS[i], keyword);
		}

		for (int i = 0; i < SMARTY_FUNCTION_NAMES.length; i++) {
			wordRule.addWord(SMARTY_FUNCTION_NAMES[i], functionName);
		}

		// ArrayList buffer = PHPSyntaxRdr.getSyntaxData();
		// PHPElement elbuffer = null;
		// for (int i = 0; i < buffer.size(); i++) {
		//
		// elbuffer = (PHPElement) buffer.get(i);
		// if (elbuffer instanceof PHPKeyword)
		// wordRule.addWord(((PHPKeyword) elbuffer).getName(), keyword);
		// // if (elbuffer instanceof PHPFunction)
		// // wordRule.addWord(((PHPFunction) elbuffer).getName(),
		// functionName);
		// if (elbuffer instanceof PHPType)
		// wordRule.addWord(elbuffer.getName(), type);
		// if (elbuffer instanceof PHPConstant)
		// wordRule.addWord(elbuffer.getName(), constant);
		// }
		rules.add(wordRule);
		setDefaultReturnToken(getToken(IPreferenceConstants.PHP_DEFAULT));
		return rules;
	}
}
