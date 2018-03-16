/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.phpdt.internal.ui.text;

import net.sourceforge.phpeclipse.ui.text.rules.AbstractPartitioner;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * This scanner recognizes the JavaDoc comments, Java multi line comments, Java
 * single line comments, Java strings.
 */
public class FastJavaPartitionScanner implements IPartitionTokenScanner,
		IPHPPartitions {

	// states
	private static final int PHP = 0;

	private static final int SINGLE_LINE_COMMENT = 1;

	private static final int MULTI_LINE_COMMENT = 2;

	private static final int PHPDOC = 3;

	private static final int STRING_DQ = 4;

	private static final int STRING_SQ = 5;

	private static final int STRING_HEREDOC = 6;

	// beginning of prefixes and postfixes
	private static final int NONE = 0;

	private static final int BACKSLASH = 1; // postfix for STRING_DQ and
											// CHARACTER

	private static final int SLASH = 2; // prefix for SINGLE_LINE or MULTI_LINE
										// or

	// JAVADOC

	private static final int SLASH_STAR = 3; // prefix for MULTI_LINE_COMMENT
												// or

	// JAVADOC

	private static final int SLASH_STAR_STAR = 4; // prefix for
													// MULTI_LINE_COMMENT

	// or JAVADOC

	private static final int STAR = 5; // postfix for MULTI_LINE_COMMENT or

	// JAVADOC

	private static final int CARRIAGE_RETURN = 6; // postfix for STRING_DQ,

	// CHARACTER and
	// SINGLE_LINE_COMMENT

	// private static final int HEREDOC = 7;

	/** The scanner. */
	private final BufferedDocumentScanner fScanner = new BufferedDocumentScanner(
			1000); // faster

	// implementation

	/** The offset of the last returned token. */
	private int fTokenOffset;

	/** The length of the last returned token. */
	private int fTokenLength;

	/** The state of the scanner. */
	private int fState;

	/** The last significant characters read. */
	private int fLast;

	/** The amount of characters already read on first call to nextToken(). */
	private int fPrefixLength;

	// emulate JavaPartitionScanner
	private boolean fEmulate = false;

	private int fJavaOffset;

	private int fJavaLength;

	private final IToken[] fTokens = new IToken[] { new Token(null),
			new Token(PHP_SINGLELINE_COMMENT),
			new Token(PHP_MULTILINE_COMMENT), new Token(PHP_PHPDOC_COMMENT),
			new Token(PHP_STRING_DQ), new Token(PHP_STRING_SQ),
			new Token(PHP_STRING_HEREDOC) };

	public FastJavaPartitionScanner(boolean emulate) {
		fEmulate = emulate;
	}

	public FastJavaPartitionScanner() {
		this(false);
	}

	/*
	 * @see org.eclipse.jface.text.rules.ITokenScanner#nextToken()
	 */
	public IToken nextToken() {

		// emulate JavaPartitionScanner
		if (fEmulate) {
			if (fJavaOffset != -1
					&& fTokenOffset + fTokenLength != fJavaOffset + fJavaLength) {
				fTokenOffset += fTokenLength;
				return fTokens[PHP];
			} else {
				fJavaOffset = -1;
				fJavaLength = 0;
			}
		}

		fTokenOffset += fTokenLength;
		fTokenLength = fPrefixLength;

		while (true) {
			final int ch = fScanner.read();

			// characters
			switch (ch) {
			case ICharacterScanner.EOF:
				if (fTokenLength > 0) {
					fLast = NONE; // ignore last
					return preFix(fState, PHP, NONE, 0);

				} else {
					fLast = NONE;
					fPrefixLength = 0;
					return Token.EOF;
				}

			case '\r':
				// emulate JavaPartitionScanner
				if (!fEmulate && fLast != CARRIAGE_RETURN) {
					fLast = CARRIAGE_RETURN;
					fTokenLength++;
					continue;

				} else {

					switch (fState) {
					case SINGLE_LINE_COMMENT:
						// case CHARACTER:
						// case STRING_DQ:
						// case STRING_SQ:
						if (fTokenLength > 0) {
							IToken token = fTokens[fState];

							// emulate JavaPartitionScanner
							if (fEmulate) {
								fTokenLength++;
								fLast = NONE;
								fPrefixLength = 0;
							} else {
								fLast = CARRIAGE_RETURN;
								fPrefixLength = 1;
							}

							fState = PHP;
							return token;

						} else {
							consume();
							continue;
						}

					default:
						consume();
						continue;
					}
				}

			case '\n':
				switch (fState) {
				case SINGLE_LINE_COMMENT:
					// case CHARACTER:
					// case STRING_DQ:
					// case STRING_SQ:
					// assert(fTokenLength > 0);
					return postFix(fState);

				default:
					consume();
					continue;
				}

			case '?':
				if (fState == SINGLE_LINE_COMMENT) {
					int nextch = fScanner.read();
					if (nextch == '>') {
						// <h1>This is an <?php # echo 'simple' ?> example.</h1>
						fTokenLength--;
						fScanner.unread();
						fScanner.unread();
						return postFix(fState);
					} else {
						// bug #1404228: Crash on <?php // comment ?>
						if (nextch != ICharacterScanner.EOF) {
							fScanner.unread();
						}
					}
				}

			default:
				if (!fEmulate && fLast == CARRIAGE_RETURN) {
					switch (fState) {
					case SINGLE_LINE_COMMENT:
						// case CHARACTER:
						// case STRING_DQ:
						// case STRING_SQ:
						int last;
						int newState;
						switch (ch) {
						case '/':
							last = SLASH;
							newState = PHP;
							break;

						case '*':
							last = STAR;
							newState = PHP;
							break;

						case '\'':
							last = NONE;
							newState = STRING_SQ;
							break;

						case '"':
							last = NONE;
							newState = STRING_DQ;
							break;

						case '\r':
							last = CARRIAGE_RETURN;
							newState = PHP;
							break;

						case '\\':
							last = BACKSLASH;
							newState = PHP;
							break;

						default:
							last = NONE;
							newState = PHP;
							break;
						}

						fLast = NONE; // ignore fLast
						return preFix(fState, newState, last, 1);

					default:
						break;
					}
				}
			}

			// states
			switch (fState) {
			case PHP:
				switch (ch) {
				case '#':
					if (fTokenLength > 0) {
						return preFix(PHP, SINGLE_LINE_COMMENT, NONE, 1);
					} else {
						preFix(PHP, SINGLE_LINE_COMMENT, NONE, 1);
						fTokenOffset += fTokenLength;
						fTokenLength = fPrefixLength;
						break;
					}
				case '/':
					if (fLast == SLASH) {
						if (fTokenLength - getLastLength(fLast) > 0) {
							return preFix(PHP, SINGLE_LINE_COMMENT, NONE, 2);
						} else {
							preFix(PHP, SINGLE_LINE_COMMENT, NONE, 2);
							fTokenOffset += fTokenLength;
							fTokenLength = fPrefixLength;
							break;
						}

					} else {
						fTokenLength++;
						fLast = SLASH;
						break;
					}

				case '*':
					if (fLast == SLASH) {
						if (fTokenLength - getLastLength(fLast) > 0)
							return preFix(PHP, MULTI_LINE_COMMENT, SLASH_STAR,
									2);
						else {
							preFix(PHP, MULTI_LINE_COMMENT, SLASH_STAR, 2);
							fTokenOffset += fTokenLength;
							fTokenLength = fPrefixLength;
							break;
						}

					} else {
						consume();
						break;
					}

				case '\'':
					fLast = NONE; // ignore fLast
					if (fTokenLength > 0)
						return preFix(PHP, STRING_SQ, NONE, 1);
					else {
						preFix(PHP, STRING_SQ, NONE, 1);
						fTokenOffset += fTokenLength;
						fTokenLength = fPrefixLength;
						break;
					}

				case '"':
					fLast = NONE; // ignore fLast
					if (fTokenLength > 0)
						return preFix(PHP, STRING_DQ, NONE, 1);
					else {
						preFix(PHP, STRING_DQ, NONE, 1);
						fTokenOffset += fTokenLength;
						fTokenLength = fPrefixLength;
						break;
					}

				default:
					consume();
					break;
				}
				break;

			case SINGLE_LINE_COMMENT:
				consume();
				break;

			case PHPDOC:
				switch (ch) {
				case '/':
					switch (fLast) {
					case SLASH_STAR_STAR:
						return postFix(MULTI_LINE_COMMENT);

					case STAR:
						return postFix(PHPDOC);

					default:
						consume();
						break;
					}
					break;

				case '*':
					fTokenLength++;
					fLast = STAR;
					break;

				default:
					consume();
					break;
				}
				break;

			case MULTI_LINE_COMMENT:
				switch (ch) {
				case '*':
					if (fLast == SLASH_STAR) {
						fLast = SLASH_STAR_STAR;
						fTokenLength++;
						fState = PHPDOC;
					} else {
						fTokenLength++;
						fLast = STAR;
					}
					break;

				case '/':
					if (fLast == STAR) {
						return postFix(MULTI_LINE_COMMENT);
					} else {
						consume();
						break;
					}

				default:
					consume();
					break;
				}
				break;

			case STRING_DQ:
				switch (ch) {
				case '\\':
					fLast = (fLast == BACKSLASH) ? NONE : BACKSLASH;
					fTokenLength++;
					break;

				case '\"':
					if (fLast != BACKSLASH) {
						return postFix(STRING_DQ);

					} else {
						consume();
						break;
					}

				default:
					consume();
					break;
				}
				break;
			case STRING_SQ:
				switch (ch) {
				case '\\':
					fLast = (fLast == BACKSLASH) ? NONE : BACKSLASH;
					fTokenLength++;
					break;

				case '\'':
					if (fLast != BACKSLASH) {
						return postFix(STRING_SQ);

					} else {
						consume();
						break;
					}

				default:
					consume();
					break;
				}
				break;
			// case CHARACTER:
			// switch (ch) {
			// case '\\':
			// fLast= (fLast == BACKSLASH) ? NONE : BACKSLASH;
			// fTokenLength++;
			// break;
			//
			// case '\'':
			// if (fLast != BACKSLASH) {
			// return postFix(CHARACTER);
			//
			// } else {
			// consume();
			// break;
			// }
			//
			// default:
			// consume();
			// break;
			// }
			// break;
			}
		}
	}

	private static final int getLastLength(int last) {
		switch (last) {
		default:
			return -1;

		case NONE:
			return 0;

		case CARRIAGE_RETURN:
		case BACKSLASH:
		case SLASH:
		case STAR:
			return 1;

		case SLASH_STAR:
			return 2;

		case SLASH_STAR_STAR:
			return 3;
		}
	}

	private final void consume() {
		fTokenLength++;
		fLast = NONE;
	}

	private final IToken postFix(int state) {
		fTokenLength++;
		fLast = NONE;
		fState = PHP;
		fPrefixLength = 0;
		return fTokens[state];
	}

	private final IToken preFix(int state, int newState, int last,
			int prefixLength) {
		// emulate JavaPartitionScanner
		if (fEmulate && state == PHP
				&& (fTokenLength - getLastLength(fLast) > 0)) {
			fTokenLength -= getLastLength(fLast);
			fJavaOffset = fTokenOffset;
			fJavaLength = fTokenLength;
			fTokenLength = 1;
			fState = newState;
			fPrefixLength = prefixLength;
			fLast = last;
			return fTokens[state];

		} else {
			fTokenLength -= getLastLength(fLast);
			fLast = last;
			fPrefixLength = prefixLength;
			IToken token = fTokens[state];
			fState = newState;
			return token;
		}
	}

	private static int getState(String contentType) {

		if (contentType == null)
			return PHP;

		else if (contentType.equals(PHP_SINGLELINE_COMMENT))
			return SINGLE_LINE_COMMENT;

		else if (contentType.equals(PHP_MULTILINE_COMMENT))
			return MULTI_LINE_COMMENT;

		else if (contentType.equals(PHP_PHPDOC_COMMENT))
			return PHPDOC;

		else if (contentType.equals(PHP_STRING_DQ))
			return STRING_DQ;

		else if (contentType.equals(PHP_STRING_SQ))
			return STRING_SQ;

		else if (contentType.equals(PHP_STRING_HEREDOC))
			return STRING_HEREDOC;

		// else if (contentType.equals(JAVA_CHARACTER))
		// return CHARACTER;

		else
			return PHP;
	}

	/*
	 * @see IPartitionTokenScanner#setPartialRange(IDocument, int, int, String,
	 *      int)
	 */
	public void setPartialRange(IDocument document, int offset, int length,
			String contentType, int partitionOffset) {
		fScanner.setRange(document, offset, length);
		setRange(document, offset, length);
		fTokenOffset = partitionOffset;
		fTokenLength = 0;
		fPrefixLength = offset - partitionOffset;
		fLast = NONE;

		if (offset == partitionOffset) {
			// restart at beginning of partition
			fState = PHP;
		} else {
			fState = getState(contentType);
		}

		// emulate JavaPartitionScanner
		if (fEmulate) {
			fJavaOffset = -1;
			fJavaLength = 0;
		}
	}

	/*
	 * @see ITokenScanner#setRange(IDocument, int, int)
	 */
	public void setRange(IDocument document, int offset, int length) {
		fScanner.setRange(document, offset, length);
		fTokenOffset = offset;
		fTokenLength = 0;
		fPrefixLength = 0;
		fLast = NONE;
		fState = PHP;

		// emulate JavaPartitionScanner
		if (fEmulate) {
			fJavaOffset = -1;
			fJavaLength = 0;
		}
	}

	/*
	 * @see ITokenScanner#getTokenLength()
	 */
	public int getTokenLength() {
		return fTokenLength;
	}

	/*
	 * @see ITokenScanner#getTokenOffset()
	 */
	public int getTokenOffset() {
		if (AbstractPartitioner.DEBUG) {
			Assert.isTrue(fTokenOffset >= 0, Integer.toString(fTokenOffset));
		}
		return fTokenOffset;
	}

}