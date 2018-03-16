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
 * $Id: TextScanner.java,v 1.3 2006-10-21 23:14:13 pombredanne Exp $
 */

package net.sourceforge.phpeclipse.xml.ui.internal.text;

import java.util.Map;

import net.sourceforge.phpeclipse.xml.ui.text.IXMLSyntaxConstants;

import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * @author Igor Malinin
 */
public class TextScanner extends BufferedRuleBasedScanner {

	/**
	 * Creates a color scanner for XML text or attribute value.
	 */
	public TextScanner(Map tokens, char startEntity, String defaultProperty) {
		setDefaultReturnToken((Token) tokens.get(defaultProperty));

		IToken entity = (Token) tokens.get(IXMLSyntaxConstants.XML_ENTITY);

		IRule[] rules = { new EntityRule(startEntity, entity) };

		setRules(rules);
	}
}
