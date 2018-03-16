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
 * $Id: SmartyTagScanner.java,v 1.2 2006-10-21 23:19:32 pombredanne Exp $
 */

package net.sourceforge.phpdt.smarty.ui.internal.text;

import java.util.Map;

import net.sourceforge.phpeclipse.xml.ui.internal.text.NameDetector;
import net.sourceforge.phpeclipse.xml.ui.internal.text.XMLTagRule;
import net.sourceforge.phpeclipse.xml.ui.text.IXMLSyntaxConstants;

import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

/**
 * @author Igor Malinin
 */
public class SmartyTagScanner extends BufferedRuleBasedScanner {

	/**
	 * Creates a color token scanner.
	 */
	public SmartyTagScanner(Map tokens) {
		setDefaultReturnToken((Token) tokens
				.get(IXMLSyntaxConstants.XML_DEFAULT));

		IToken tag = (Token) tokens.get(IXMLSyntaxConstants.XML_TAG);
		IToken smartyTag = (Token) tokens.get(IXMLSyntaxConstants.XML_SMARTY);
		IToken attribute = (Token) tokens.get(IXMLSyntaxConstants.XML_ATT_NAME);

		IRule[] rules = { new XMLTagRule(tag), new SmartyTagRule(smartyTag),
				new WordRule(new NameDetector(), attribute), };

		setRules(rules);
	}
}
