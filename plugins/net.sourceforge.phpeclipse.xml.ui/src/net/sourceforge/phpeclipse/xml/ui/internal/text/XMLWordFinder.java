/*
 * Copyright (c) 2004 Widespace, OU and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Igor Malinin - initial implementation
 * 
 * $Id: XMLWordFinder.java,v 1.2 2006-10-21 23:14:13 pombredanne Exp $
 */
package net.sourceforge.phpeclipse.xml.ui.internal.text;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

/**
 * 
 * 
 * @author Igor Malinin
 */
public class XMLWordFinder {
	public static IRegion findWord(IDocument document, int offset) {
		int length = document.getLength();

		try {
			int pos = offset;

			while (pos >= 0) {
				if (!Character.isUnicodeIdentifierPart(document.getChar(pos))) {
					break;
				}
				--pos;
			}

			int start = pos;

			pos = offset;

			while (pos < length) {
				if (!Character.isUnicodeIdentifierPart(document.getChar(pos))) {
					break;
				}
				++pos;
			}

			int end = pos;

			if (start == offset) {
				return new Region(start, end - start);
			}

			return new Region(start + 1, end - start - 1);
		} catch (BadLocationException x) {
			return null;
		}
	}
}
