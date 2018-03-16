/**********************************************************************
 Copyright (c) 2002  Widespace, OU  and others.
 All rights reserved.   This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://solareclipse.sourceforge.net/legal/cpl-v10.html

 Contributors:
 Igor Malinin - initial contribution

 $Id: PHPPartitionScanner.java,v 1.35 2007-03-17 14:07:31 axelcl Exp $
 **********************************************************************/
package net.sourceforge.phpeclipse.phpeditor.php;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.phpdt.internal.compiler.parser.Scanner;
import net.sourceforge.phpeclipse.ui.text.rules.AbstractPartitioner;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * 
 * 
 * @author Igor Malinin
 */
public class PHPPartitionScanner implements IPartitionTokenScanner {
	public static final String PHP_SCRIPTING_AREA = "__php_scripting_area ";

	public static final int STATE_DEFAULT = 0;

	// public static final int STATE_TAG = 1;
	// public static final int STATE_SCRIPT = 2;

	private IDocument document;

	// private int begin;

	private int end;

	private int offset;

	private int length;

	private int position;

	// private int state;

	private Map tokens = new HashMap();

	public PHPPartitionScanner() {
	}

	/*
	 * @see org.eclipse.jface.text.rules.ITokenScanner#nextToken()
	 */
	public IToken nextToken() {
		offset += length;

		/*
		 * switch (state) { case STATE_TAG: return nextTagToken(); }
		 */

		switch (read()) {
		case ICharacterScanner.EOF:
			// state = STATE_DEFAULT;
			return getToken(null);

		case '<':
			switch (read()) {
			case ICharacterScanner.EOF:
				// state = STATE_DEFAULT;
				return getToken(null);

			case '?': // <?
				// int ch = read();
				//
				// switch (ch) {
				// case ICharacterScanner.EOF:
				// state = STATE_DEFAULT;
				// return getToken(PHP_SCRIPTING_AREA);
				// }
				return scanUntilPHPEndToken(PHP_SCRIPTING_AREA);
			}

			unread();
		}

		loop: while (true) {
			switch (read()) {
			case ICharacterScanner.EOF:
				// state = STATE_DEFAULT;
				return getToken(null);

			case '<':
				switch (read()) {
				case ICharacterScanner.EOF:
					// state = STATE_DEFAULT;
					return getToken(null);

				case '?':
					unread();
					break;

				case '<':
					unread();

				default:
					continue loop;
				}

				unread();

				// state = STATE_DEFAULT;
				return getToken(null);
			}
		}
	}

	private IToken scanUntilPHPEndToken(String token) {
		int ch = read();
		while (true) {
			switch (ch) {
			case ICharacterScanner.EOF:
				// state = STATE_DEFAULT;
				return getToken(token);
			case '"': // double quoted string
				// read until end of double quoted string
				if (!readUntilEscapedDQ()) {
					// state = STATE_DEFAULT;
					return getToken(token);
				}
				break;
			case '<': // heredoc string
				ch = read();
				switch (ch) {
				case ICharacterScanner.EOF:
					break;
				case '<':
					ch = read();
					switch (ch) {
					case ICharacterScanner.EOF:
						break;
					case '<':
						// read until end of heredoc string
						if (!readUntilEscapedHEREDOC()) {
							// state = STATE_DEFAULT;
							return getToken(token);
						}
					}
				}
				break;
			case '\'': // single quoted string
				// read until end of single quoted string
				if (!readUntilEscapedSQ()) {
					// state = STATE_DEFAULT;
					return getToken(token);
				}
				break;
			case '/': // comment start?
				ch = read();
				switch (ch) {
				case ICharacterScanner.EOF:
					break;
				case '/':
					// read until end of line
					if (!readSingleLine()) {
						// state = STATE_DEFAULT;
						return getToken(token);
					}
					break;
				case '*':
					// read until end of comment
					if (!readMultiLineComment()) {
						// state = STATE_DEFAULT;
						return getToken(token);
					}
					break;
				default:
					continue;
				}
				break;
			case '#': // line comment
				// read until end of line
				if (!readSingleLine()) {
					// state = STATE_DEFAULT;
					return getToken(token);
				}
				break;
			case '?':
				ch = read();
				switch (ch) {
				case ICharacterScanner.EOF:
				case '>':
					// state = STATE_DEFAULT;
					return getToken(token);

				case '?':
					continue;
				default:
					continue;
				}
			}

			ch = read();
		}
	}

	private IToken getToken(String type) {
		length = position - offset;

		if (length == 0) {
			return Token.EOF;
		}

		// if (length<0) {
		// try {
		// System.out.println("Length<0:"+document.get(offset,5)+""+length);
		// } catch (BadLocationException e) {
		// e.printStackTrace();
		// }
		// }

		if (type == null) {
			return Token.UNDEFINED;
		}

		IToken token = (IToken) tokens.get(type);
		if (token == null) {
			token = new Token(type);
			tokens.put(type, token);
		}

		return token;
	}

	private int read() {
		if (position >= end) {
			return ICharacterScanner.EOF;
		}

		try {
			return document.getChar(position++);
		} catch (BadLocationException e) {
			--position;
			return ICharacterScanner.EOF;
		}
	}

	private boolean readUntilEscapedDQ() {
		// search last double quoted character
		try {
			char ch;
			while (true) {
				if (position >= end) {
					return false;
				}
				ch = document.getChar(position++);
				if (ch == '\\') {
					if (position >= end) {
						return false;
					}
					ch = document.getChar(position++); // ignore escaped
					// character
				} else if (ch == '"') {
					return true;
				}
			}
		} catch (BadLocationException e) {
			--position;
		}
		return false;
	}

	private boolean readUntilEscapedSQ() {
		// search last single quoted character
		try {
			char ch;
			while (true) {
				if (position >= end) {
					return false;
				}
				ch = document.getChar(position++);
				if (ch == '\\') {
					if (position >= end) {
						return false;
					}
					ch = document.getChar(position++); // ignore escaped
					// character
				} else if (ch == '\'') {
					return true;
				}
			}
		} catch (BadLocationException e) {
			--position;
		}
		return false;
	}

	private boolean readUntilEscapedHEREDOC() {
		// search until heredoc ends
		try {
			char ch;
			StringBuffer buf = new StringBuffer();
			char[] heredocIdent;
			if (position >= end) {
				return false;
			}
			ch = document.getChar(position++);
			// #1493165 start
			while (ch == ' ') {
				if (position >= end) {
					return false;
				}
				ch = document.getChar(position++);
			}
			// #1493165 end
			if (!Scanner.isPHPIdentifierStart(ch)) {
				return false;
			}
			while (Scanner.isPHPIdentifierPart(ch)) {
				buf.append(ch);
				if (position >= end) {
					return false;
				}
				ch = document.getChar(position++);
			}
			heredocIdent = buf.toString().toCharArray();
			while (true) {
				if (position >= end) {
					return false;
				}
				ch = document.getChar(position++);
				if (ch == '\n') { // heredoc could end after a newline
					int pos = 0;
					while (true) {
						if (position >= end) {
							return false;
						}
						if (pos == heredocIdent.length) {
							return true;
						}
						ch = document.getChar(position++); // ignore escaped
						// character
						if (ch != heredocIdent[pos]) {
							break;
						}
						pos++;
					}
				}
			}
		} catch (BadLocationException e) {
			--position;
		}
		return false;
	}

	private boolean readSingleLine() {
		try {
			do {
				if (position >= end) {
					return false;
				}
			} while (document.getChar(position++) != '\n');
			return true;
		} catch (BadLocationException e) {
			--position;
		}
		return false;
	}

	private boolean readMultiLineComment() {
		try {
			char ch;
			while (true) {
				if (position >= end) {
					return false;
				}
				ch = document.getChar(position++);
				if (ch == '*') {
					if (position >= end) {
						return false;
					}
					if (document.getChar(position) == '/') {
						position++;
						return true;
					}
				}
			}
		} catch (BadLocationException e) {
			--position;
		}
		return false;
	}

	private void unread() {
		--position;
	}

	/*
	 * @see org.eclipse.jface.text.rules.ITokenScanner#getTokenOffset()
	 */
	public int getTokenOffset() {
		if (AbstractPartitioner.DEBUG) {
			Assert.isTrue(offset >= 0, Integer.toString(offset));
		}
		return offset;
	}

	/*
	 * @see org.eclipse.jface.text.rules.ITokenScanner#getTokenLength()
	 */
	public int getTokenLength() {
		return length;
	}

	/*
	 * @see org.eclipse.jface.text.rules.ITokenScanner#setRange(IDocument, int,
	 *      int)
	 */
	public void setRange(IDocument document, int offset, int length) {
		this.document = document;
		// this.begin = offset;
		this.end = offset + length;

		this.offset = offset;
		this.position = offset;
		this.length = 0;
	}

	/*
	 * @see org.eclipse.jface.text.rules.IPartitionTokenScanner
	 */
	public void setPartialRange(IDocument document, int offset, int length,
			String contentType, int partitionOffset) {
		// state = STATE_DEFAULT;
		if (partitionOffset > -1) {
			int delta = offset - partitionOffset;
			if (delta > 0) {
				setRange(document, partitionOffset, length + delta);
				return;
			}
		}
		setRange(document, partitionOffset, length);
	}

	// private boolean isContinuationPartition(IDocument document, int offset) {
	// try {
	// String type = document.getContentType(offset - 1);
	//
	// if (type != IDocument.DEFAULT_CONTENT_TYPE) {
	// return true;
	// }
	// } catch (BadLocationException e) {}
	//
	// return false;
	// }
}