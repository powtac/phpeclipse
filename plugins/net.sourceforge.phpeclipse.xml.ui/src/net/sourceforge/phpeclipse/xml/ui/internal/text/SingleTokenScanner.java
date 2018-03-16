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
 * $Id: SingleTokenScanner.java,v 1.1 2004-09-02 18:28:03 jsurfer Exp $
 */

package net.sourceforge.phpeclipse.xml.ui.internal.text;

import java.util.Map;

import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;

/**
 * @author Igor Malinin
 */
public class SingleTokenScanner extends RuleBasedScanner {

	/**
	 * Creates a single token scanner.
	 */
	public SingleTokenScanner(Map tokens, String property) {
		setDefaultReturnToken((Token) tokens.get(property));
	}
}
