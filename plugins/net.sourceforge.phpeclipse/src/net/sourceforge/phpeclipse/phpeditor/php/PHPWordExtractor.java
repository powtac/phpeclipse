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

import net.sourceforge.phpdt.internal.compiler.parser.Scanner;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Point;

/**
 * Detects PHP words in documents.
 */
public class PHPWordExtractor {

	/**
	 * Find the location of the word at offset in document.
	 * 
	 * @returns Point - x is the start position, y is the end position. Return
	 *          null if it is not found.
	 * @param document
	 *            the document being searched.
	 * @param offset -
	 *            the position to start searching from.
	 */
	public static Point findWord(IDocument document, int offset) {

		int start = -1;
		int end = -1;

		try {

			int position = offset;
			char character;

			while (position >= 0) {
				character = document.getChar(position);
				if (!Scanner.isPHPIdentifierPart(character)
						&& (character != '$'))
					break;
				--position;
			}

			start = position;

			position = offset;
			int length = document.getLength();

			while (position < length) {
				character = document.getChar(position);
				if (!Scanner.isPHPIdentifierPart(character)
						&& (character != '$'))
					break;
				++position;
			}

			start++;
			end = position;

			if (end > start)
				return new Point(start, end - start);

		} catch (BadLocationException x) {
		}

		return null;
	}
}
