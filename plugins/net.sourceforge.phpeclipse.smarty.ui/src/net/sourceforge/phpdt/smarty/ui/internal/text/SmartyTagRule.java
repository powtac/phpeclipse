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
 * $Id: SmartyTagRule.java,v 1.3 2006-10-21 23:19:32 pombredanne Exp $
 */

package net.sourceforge.phpdt.smarty.ui.internal.text;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * Rule detecting smarty tags
 */
public class SmartyTagRule implements IRule {

	private IToken token;

	public SmartyTagRule(IToken token) {
		this.token = token;
	}

	public IToken evaluate(ICharacterScanner scanner) {
		int ch = scanner.read();
		if (ch == '}') {
			return token;
		}
		if (ch == '/') {
			ch = scanner.read();
			if (ch == '}') {
				return token;
			}

			scanner.unread();
			scanner.unread();
			return Token.UNDEFINED;
		}
		if (ch == '{') {
			ch = scanner.read();
			if (ch == '/') {
				ch = scanner.read();
			}
			loop: while (true) {
				switch (ch) {
				case ICharacterScanner.EOF:
				case 0x09:
				case 0x0A:
				case 0x0D:
				case 0x20:
					scanner.unread();
					break loop;
				case '}':
					break loop;
				}

				ch = scanner.read();
			}
			return token;
		}
		scanner.unread();
		return Token.UNDEFINED;
	}
}