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

import net.sourceforge.phpdt.core.IBuffer;
import net.sourceforge.phpdt.internal.corext.util.Strings;

/**
 * Reads a phpdoc comment from a phpdoc comment. Skips star-character on begin
 * of line
 */
public class PHPDocBufferCommentReader extends SingleCharReader {

	private IBuffer fBuffer;

	private int fCurrPos;

	private int fStartPos;

	private int fEndPos;

	private boolean fWasNewLine;

	public PHPDocBufferCommentReader(IBuffer buf, int start, int end) {
		fBuffer = buf;
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
					ch = fBuffer.getChar(fCurrPos++);
				} while (fCurrPos < fEndPos && Character.isWhitespace(ch));
				if (ch == '*') {
					if (fCurrPos < fEndPos) {
						do {
							ch = fBuffer.getChar(fCurrPos++);
						} while (ch == '*');
					} else {
						return -1;
					}
				}
			} else {
				ch = fBuffer.getChar(fCurrPos++);
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
		fBuffer = null;
	}

	/**
	 * @see java.io.Reader#reset()
	 */
	public void reset() {
		fCurrPos = fStartPos;
		fWasNewLine = true;
	}

}
