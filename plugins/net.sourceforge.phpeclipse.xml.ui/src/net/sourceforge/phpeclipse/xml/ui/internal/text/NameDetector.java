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
 * $Id: NameDetector.java,v 1.2 2006-10-21 23:14:13 pombredanne Exp $
 */

package net.sourceforge.phpeclipse.xml.ui.internal.text;

import org.eclipse.jface.text.rules.IWordDetector;

/**
 * XML Name detector.
 * 
 * @author Igor Malinin
 */
public class NameDetector implements IWordDetector {

	/**
	 * @see IWordDetector#isWordPart(char)
	 */
	public boolean isWordPart(char ch) {
		if (Character.isUnicodeIdentifierPart(ch)) {
			return true;
		}
		switch (ch) {
		case '.':
		case '-':
		case '_':
		case ':':
			return true;
		}
		return false;
	}

	/**
	 * @see IWordDetector#isWordStart(char)
	 */
	public boolean isWordStart(char ch) {
		if (Character.isUnicodeIdentifierStart(ch)) {
			return true;
		}
		switch (ch) {
		case '_':
		case ':':
			return true;
		}
		return false;
	}

}
