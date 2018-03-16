/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.phpdt.internal.corext.phpdoc;

import net.sourceforge.phpdt.internal.corext.util.Strings;

/**
 * Reads a phpdoc comment from a phpdoc comment. Skips star-character on begin
 * of line
 */
public class PHPDocCharArrayCommentReader extends SingleCharReader {

	private char[] fCharArray;

	private int fCurrPos;

	private int fStartPos;

	private int fEndPos;

	private boolean fWasNewLine;

	public PHPDocCharArrayCommentReader(char[] buf) {
		this(buf, 0, buf.length);
	}

	public PHPDocCharArrayCommentReader(char[] buf, int start, int end) {
		fCharArray = buf;
		fStartPos = start + 3;
		fEndPos = end - 2;

		reset();
	}

	/**
	 * @see java.io.Reader#read()
	 */
	public int read() {
		if (fCurrPos < fEndPos) {
			char ch;
			if (fWasNewLine) {
				do {
					ch = fCharArray[fCurrPos++];
				} while (fCurrPos < fEndPos && Character.isWhitespace(ch));
				if (ch == '*') {
					if (fCurrPos < fEndPos) {
						do {
							ch = fCharArray[fCurrPos++];
						} while (ch == '*');
					} else {
						return -1;
					}
				}
			} else {
				ch = fCharArray[fCurrPos++];
			}
			fWasNewLine = Strings.isLineDelimiterChar(ch);

			return ch;
		}
		return -1;
	}

	/**
	 * @see java.io.Reader#close()
	 */
	public void close() {
		fCharArray = null;
	}

	/**
	 * @see java.io.Reader#reset()
	 */
	public void reset() {
		fCurrPos = fStartPos;
		fWasNewLine = true;
	}

}
