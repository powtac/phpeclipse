/*
 * Copyright (c) 2002-2004 Widespace, OU and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Igor Malinin - initial contribution
 *
 * $Id: DeclScanner.java,v 1.3 2006-10-21 23:14:13 pombredanne Exp $
 */

package net.sourceforge.phpeclipse.xml.ui.internal.text;

import java.util.Map;

import net.sourceforge.phpeclipse.xml.ui.text.IXMLSyntaxConstants;

import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

/**
 * @author Igor Malinin
 */
public class DeclScanner extends BufferedRuleBasedScanner {

	/**
	 * Creates a color scanner for XML text or attribute value.
	 */
	public DeclScanner(Map tokens) {
		IToken decl = (Token) tokens.get(IXMLSyntaxConstants.XML_DECL);

		setDefaultReturnToken(decl);

		IToken markup = (Token) tokens.get(IXMLSyntaxConstants.XML_ATT_NAME);

		WordRule rule = new WordRule(new NmtokenDetector(), markup);
		rule.addWord("ATTLIST", decl);
		rule.addWord("CDATA", decl);
		rule.addWord("DOCTYPE", decl);
		rule.addWord("ELEMENT", decl);
		rule.addWord("EMPTY", decl);
		rule.addWord("ENTITY", decl);
		rule.addWord("FIXED", decl);
		rule.addWord("ID", decl);
		rule.addWord("IDREF", decl);
		rule.addWord("IDREFS", decl);
		rule.addWord("IMPLIED", decl);
		rule.addWord("PCDATA", decl);
		rule.addWord("PUBLIC", decl);
		rule.addWord("REQUIRED", decl);
		rule.addWord("SYSTEM", decl);

		IToken string = (Token) tokens.get(IXMLSyntaxConstants.XML_ATT_VALUE);
		IToken entity = (Token) tokens.get(IXMLSyntaxConstants.XML_ENTITY);

		IRule[] rules = { rule, new MultiLineRule("\"", "\"", string),
				new MultiLineRule("'", "'", string),
				new EntityRule('%', entity), };

		setRules(rules);
	}
}
