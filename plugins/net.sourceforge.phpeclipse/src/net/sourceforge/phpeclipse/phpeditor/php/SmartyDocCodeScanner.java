/**********************************************************************
 Copyright (c) 2000, 2002 IBM Corp. and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html

 Contributors:
 IBM Corporation - Initial implementation
 **********************************************************************/
package net.sourceforge.phpeclipse.phpeditor.php;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.phpdt.internal.ui.text.AbstractJavaScanner;
import net.sourceforge.phpdt.ui.text.IColorManager;
import net.sourceforge.phpeclipse.IPreferenceConstants;
import net.sourceforge.phpeclipse.phpeditor.util.PHPWhitespaceDetector;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

/**
 * A rule based SmartyDoc scanner.
 */
public final class SmartyDocCodeScanner extends AbstractJavaScanner {

	/**
	 * A key word detector.
	 */
	static class JavaDocKeywordDetector implements IWordDetector {

		/**
		 * @see IWordDetector#isWordStart
		 */
		public boolean isWordStart(char c) {
			return (c == '@');
		}

		/**
		 * @see IWordDetector#isWordPart
		 */
		public boolean isWordPart(char c) {
			return Character.isLetter(c);
		}
	};

	/**
	 * Detector for HTML comment delimiters.
	 */
	static class HTMLCommentDetector implements IWordDetector {

		/**
		 * @see IWordDetector#isWordStart
		 */
		public boolean isWordStart(char c) {
			return (c == '<' || c == '-');
		}

		/**
		 * @see IWordDetector#isWordPart
		 */
		public boolean isWordPart(char c) {
			return (c == '-' || c == '!' || c == '>');
		}
	};

	class TagRule extends SingleLineRule {

		/*
		 * @see SingleLineRule
		 */
		public TagRule(IToken token) {
			super("<", ">", token, (char) 0); //$NON-NLS-2$ //$NON-NLS-1$
		}

		/*
		 * @see SingleLineRule
		 */
		public TagRule(IToken token, char escapeCharacter) {
			super("<", ">", token, escapeCharacter); //$NON-NLS-2$ //$NON-NLS-1$
		}

		private IToken checkForWhitespace(ICharacterScanner scanner) {

			try {

				char c = getDocument().getChar(getTokenOffset() + 1);
				if (!Character.isWhitespace(c))
					return fToken;

			} catch (BadLocationException x) {
			}

			return Token.UNDEFINED;
		}

		/*
		 * @see PatternRule#evaluate(ICharacterScanner)
		 */
		public IToken evaluate(ICharacterScanner scanner) {
			IToken result = super.evaluate(scanner);
			if (result == fToken)
				return checkForWhitespace(scanner);
			return result;
		}
	};

	private static String[] fgKeywords = {
		"@author", "@deprecated", "@exception", "@link", "@param", "@return", "@see", "@since", "@throws", "@value", "@version", "@license", "@abstract", "@access", "@category",
		"@copyright", "@example", "@final", "@filesource", "@global", "@ignore", "@internal", "@link", "@method", "@name", "@package", "@param", "@property", "@static",
		"@staticvar", "@subpackage", "@todo", "@tutorial", "@uses", "@var","@id", "inheritdoc", "@property-read", "@property-write", "@source" }; //$NON-NLS-12$ //$NON-NLS-11$ //$NON-NLS-10$ //$NON-NLS-7$ //$NON-NLS-9$ //$NON-NLS-8$ //$NON-NLS-6$ //$NON-NLS-5$ //$NON-NLS-4$ //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$

	private static String[] fgTokenProperties = {
			IPreferenceConstants.PHPDOC_KEYWORD,
			IPreferenceConstants.PHPDOC_TAG, IPreferenceConstants.PHPDOC_LINK,
			IPreferenceConstants.PHPDOC_DEFAULT };

	public SmartyDocCodeScanner(IColorManager manager, IPreferenceStore store) {
		super(manager, store);
		initialize();
	}

	public IDocument getDocument() {
		return fDocument;
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

		List list = new ArrayList();

		// Add rule for tags.
		Token token = getToken(IPreferenceConstants.PHPDOC_TAG);
		list.add(new TagRule(token));

		// Add rule for HTML comments
		WordRule wordRule = new WordRule(new HTMLCommentDetector(), token);
		wordRule.addWord("<!--", token); //$NON-NLS-1$
		wordRule.addWord("--!>", token); //$NON-NLS-1$
		list.add(wordRule);

		// Add rule for links.
		token = getToken(IPreferenceConstants.PHPDOC_LINK);
		list.add(new SingleLineRule("{@link", "}", token)); //$NON-NLS-2$ //$NON-NLS-1$

		// Add generic whitespace rule.
		list.add(new WhitespaceRule(new PHPWhitespaceDetector()));

		// Add word rule for keywords.
		token = getToken(IPreferenceConstants.PHPDOC_DEFAULT);
		wordRule = new WordRule(new JavaDocKeywordDetector(), token);

		token = getToken(IPreferenceConstants.PHPDOC_KEYWORD);
		for (int i = 0; i < fgKeywords.length; i++)
			wordRule.addWord(fgKeywords[i], token);
		list.add(wordRule);

		setDefaultReturnToken(getToken(IPreferenceConstants.PHPDOC_DEFAULT));
		return list;
	}
}
