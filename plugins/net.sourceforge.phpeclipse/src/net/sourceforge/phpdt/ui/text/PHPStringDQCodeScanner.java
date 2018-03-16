/**********************************************************************
 Copyright (c) 2000, 2002 IBM Corp. and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html

 Contributors:
 IBM Corporation - Initial implementation
 **********************************************************************/
package net.sourceforge.phpdt.ui.text;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.phpdt.internal.ui.text.AbstractJavaScanner;
import net.sourceforge.phpeclipse.IPreferenceConstants;
import net.sourceforge.phpeclipse.phpeditor.util.PHPVariableDetector;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

/**
 * A rule based PHPDoc scanner.
 */
public final class PHPStringDQCodeScanner extends AbstractJavaScanner {

	private static String[] fgTokenProperties = {
			IPreferenceConstants.PHP_STRING_DQ,
			IPreferenceConstants.PHP_VARIABLE,
			IPreferenceConstants.PHP_VARIABLE_DOLLAR };

	private class PHPWordRule extends WordRule {
		private StringBuffer fBuffer = new StringBuffer();

		public PHPWordRule(IWordDetector detector) {
			super(detector, Token.UNDEFINED);
		}

		public PHPWordRule(IWordDetector detector, IToken defaultToken) {
			super(detector, defaultToken);
		}

		public IToken evaluate(ICharacterScanner scanner) {
			int c = scanner.read();
			boolean isUnderscore = false;
			if (fDetector.isWordStart((char) c)) {
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
						// hack for coloring object elements with variable color
						// instead of string color
						if (c == '-') {
							int c2 = scanner.read();
							if (c2 == '>') {
								fBuffer.append(c);
								fBuffer.append(c2);
								c = scanner.read();
							} else {
								scanner.unread();
							}
						}
						// hack end
					}
					scanner.unread();
					if (isUnderscore) {
						return getToken(IPreferenceConstants.PHP_VARIABLE_DOLLAR);
					}
					return getToken(IPreferenceConstants.PHP_VARIABLE);
				}
			}

			scanner.unread();
			return Token.UNDEFINED;
		}
	}

	public PHPStringDQCodeScanner(IColorManager manager, IPreferenceStore store) {
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
		Token token = getToken(IPreferenceConstants.PHP_STRING_DQ);
		PHPWordRule wordRule = new PHPWordRule(new PHPVariableDetector(), token);

		list.add(wordRule);

		setDefaultReturnToken(getToken(IPreferenceConstants.PHP_STRING_DQ));
		return list;
	}
}
