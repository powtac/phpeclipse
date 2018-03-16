/**
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * Created on 05.03.2003
 *
 * @author Stefan Langer (musk)
 * @version $Revision: 1.3 $
 */
package net.sourceforge.phpeclipse.phpeditor.php;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.phpdt.internal.ui.text.IPHPPartitions;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * 
 */
public class HTMLPartitionScanner implements IPartitionTokenScanner {
	private static final boolean DEBUG = false;

	private boolean fInString = false;

	private boolean fInDoubString = false;

	private IDocument fDocument = null;

	private int fOffset = -1;

	private String fContentType = IPHPPartitions.HTML;

	private String fPrevContentType = IPHPPartitions.HTML;

	private boolean partitionBorder = false;

	private int fTokenOffset;

	private int fEnd = -1;

	private int fLength;

	private int fCurrentLength;

	private int fFileType;

	private Map tokens = new HashMap();

	public HTMLPartitionScanner() {
		this(IPHPPartitions.PHP_FILE);
	}

	public HTMLPartitionScanner(int fileType) {
		this.tokens.put(IPHPPartitions.HTML, new Token(IPHPPartitions.HTML));
		this.tokens.put(IPHPPartitions.HTML_MULTILINE_COMMENT, new Token(
				IPHPPartitions.HTML_MULTILINE_COMMENT));

		this.tokens
				.put(IPHPPartitions.SMARTY, new Token(IPHPPartitions.SMARTY));
		this.tokens.put(IPHPPartitions.SMARTY_MULTILINE_COMMENT, new Token(
				IPHPPartitions.SMARTY_MULTILINE_COMMENT));

		this.tokens.put(IDocument.DEFAULT_CONTENT_TYPE, new Token(
				IDocument.DEFAULT_CONTENT_TYPE));
		fFileType = fileType;
	}

	private IToken getToken(String type) {
		fLength = fCurrentLength;
		if (DEBUG) {

			try {
				if (fLength <= 0) {
					int line = fDocument.getLineOfOffset(fOffset);
					System.err.println("Error at "
							+ line
							+ " offset:"
							+ String.valueOf(fOffset
									- fDocument.getLineOffset(line)));
				}
			} catch (BadLocationException e) { // should never happen
				// TODO Write stacktrace to log
				e.printStackTrace();
			}
		}
		Assert.isTrue(fLength > 0, "Partition length <= 0!");
		fCurrentLength = 0;
		// String can never cross partition borders so reset string detection
		fInString = false;
		fInDoubString = false;
		IToken token = (IToken) this.tokens.get(type);
		Assert.isNotNull(token, "Token for type \"" + type + "\" not found!");
		if (DEBUG) {
			System.out.println("Partition: fTokenOffset=" + fTokenOffset
					+ " fContentType=" + type + " fLength=" + fLength);
		}
		return token;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.rules.IPartitionTokenScanner#setPartialRange(org.eclipse.jface.text.IDocument,
	 *      int, int, java.lang.String, int)
	 */
	public void setPartialRange(IDocument document, int offset, int length,
			String contentType, int partitionOffset) {
		if (DEBUG) {
			System.out.println("*****");
			System.out.println("PartialRange: contentType=" + contentType
					+ " partitionOffset=" + partitionOffset);
		}

		try {
			if (partitionOffset > -1) {
				partitionBorder = false;
				// because of strings we have to parse the whole partition
				this.setRange(document, partitionOffset, offset
						- partitionOffset + length);
				// sometimes we get a wrong partition so we retrieve the
				// partition
				// directly from the document
				fContentType = fDocument.getContentType(partitionOffset);
			} else
				this.setRange(document, offset, length);

		} catch (BadLocationException e) {
			// should never happen
			// TODO print stack trace to log
			// fall back just scan the whole document again
			this.setRange(document, 0, fDocument.getLength());
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.rules.ITokenScanner#getTokenLength()
	 */
	public int getTokenLength() {
		return fLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.rules.ITokenScanner#getTokenOffset()
	 */
	public int getTokenOffset() {
		return fTokenOffset;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.rules.ITokenScanner#nextToken()
	 */
	public IToken nextToken() {
		int c;

		// check if we are not allready at the end of the
		// file
		if ((c = read()) == ICharacterScanner.EOF) {
			partitionBorder = false;
			return Token.EOF;
		} else
			unread();

		if (partitionBorder) {
			fTokenOffset = fOffset;
			partitionBorder = false;
		}

		while ((c = read()) != ICharacterScanner.EOF) {
			switch (c) {
			case '<':
				if (checkPattern(new char[] { '!', '-', '-' })) { // return
																	// previouse
																	// partition
					if (fContentType != IPHPPartitions.HTML_MULTILINE_COMMENT
							&& fCurrentLength > 4) {
						unread(4);
						IToken token = getToken(fContentType);
						fContentType = IPHPPartitions.HTML_MULTILINE_COMMENT;
						return token;
					} else
						fContentType = IPHPPartitions.HTML_MULTILINE_COMMENT;

					fTokenOffset = fOffset - 4;
					fCurrentLength = 4;
				}
				break;
			case '-':
				if (fContentType == IPHPPartitions.HTML_MULTILINE_COMMENT
						&& checkPattern(new char[] { '-', '>' })) {
					fContentType = IPHPPartitions.HTML;
					partitionBorder = true;
					return getToken(IPHPPartitions.HTML_MULTILINE_COMMENT);
				}
				break;
			case '{': // SMARTY code starts here ?
				if (fFileType == IPHPPartitions.SMARTY_FILE) {
					if ((c = read()) == '*') {
						if (DEBUG) {
							System.out.println("SMARTYDOC_TOKEN start "
									+ fTokenOffset + " fContentType="
									+ fContentType + " fLength=" + fLength
									+ " fOffset=" + fOffset
									+ " fCurrentLength=" + fCurrentLength);
						}
						if (fContentType != IPHPPartitions.SMARTY_MULTILINE_COMMENT
								&& fCurrentLength > 2) {
							// SMARTY doc code starts here
							unread(2);
							IToken token = getToken(fContentType);
							fContentType = IPHPPartitions.SMARTY_MULTILINE_COMMENT;
							return token;
							// } else if (fContentType ==
							// IPHPPartitionScannerConstants.HTML && fOffset ==
							// 2) {
							// fContentType =
							// IPHPPartitionScannerConstants.SMARTY_MULTILINE_COMMENT;
						} else { // if (fContentType ==
									// IPHPPartitionScannerConstants.SMARTY_MULTILINE_COMMENT)
									// {
							fContentType = IPHPPartitions.SMARTY_MULTILINE_COMMENT;
							fTokenOffset = fOffset - 2;
							fCurrentLength = 2;
						}
						break;
					}
					if (DEBUG) {
						System.out.println("SMARTY_TOKEN start " + fTokenOffset
								+ " fContentType=" + fContentType + " fLength="
								+ fLength + " fOffset=" + fOffset);
					}
					if (c != ICharacterScanner.EOF) {
						unread();
					}
					if (fContentType != IPHPPartitions.SMARTY
							&& fCurrentLength > 1) {
						unread(1);
						IToken token = getToken(fContentType);
						fContentType = IPHPPartitions.SMARTY;
						return token;
						// } else if (fContentType ==
						// IPHPPartitionScannerConstants.HTML && fOffset==1) {
						// fContentType = IPHPPartitionScannerConstants.SMARTY;
					} else {
						fContentType = IPHPPartitions.SMARTY;
						fTokenOffset = fOffset - 1;
						fCurrentLength = 1;
					}
				}
				break;
			case '}': // SMARTY code ends here ?
				if (fFileType == IPHPPartitions.SMARTY_FILE
						&& fContentType == IPHPPartitions.SMARTY) {
					if (DEBUG) {
						System.out.println("SMARTY_TOKEN end " + fTokenOffset
								+ " fContentType=" + fContentType + " fLength="
								+ fLength + " fOffset=" + fOffset);
					}
					fContentType = IPHPPartitions.HTML;
					partitionBorder = true;
					return getToken(IPHPPartitions.SMARTY);
				}
				break;
			// case '/' :
			// if (!isInString(IPHPPartitions.PHP_PARTITIONING) && (c = read())
			// == '*') { // MULTINE COMMENT JAVASCRIPT, CSS, PHP
			// if (fContentType == IPHPPartitions.PHP_PARTITIONING &&
			// fCurrentLength > 2) {
			// unread(2);
			// IToken token = getToken(fContentType);
			// fContentType = IPHPPartitions.PHP_PHPDOC_COMMENT;
			// return token;
			// } else if (fContentType == IPHPPartitions.PHP_PHPDOC_COMMENT) {
			// fTokenOffset = fOffset - 2;
			// fCurrentLength = 2;
			// }
			//
			// } else if (!isInString(IPHPPartitions.PHP_PARTITIONING) && c !=
			// ICharacterScanner.EOF)
			// unread();
			// break;
			case '*':
				if (fFileType == IPHPPartitions.SMARTY_FILE
						&& (c = read()) == '}') {
					if (DEBUG) {
						System.out.println("SMARTYDOC_TOKEN end "
								+ fTokenOffset + " fContentType="
								+ fContentType + " fLength=" + fLength
								+ " fOffset=" + fOffset);
					}
					if (fContentType == IPHPPartitions.SMARTY_MULTILINE_COMMENT) {
						fContentType = IPHPPartitions.HTML;
						partitionBorder = true;
						return getToken(IPHPPartitions.SMARTY_MULTILINE_COMMENT);
					}
				}
				break;
			case '\'':
				if (!fInDoubString)
					fInString = !fInString;
				break;
			case '"':
				// toggle String mode
				if (!fInString)
					fInDoubString = !fInDoubString;
				break;
			}
		} // end of file reached but we have to return the
		// last partition.
		return getToken(fContentType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.rules.ITokenScanner#setRange(org.eclipse.jface.text.IDocument,
	 *      int, int)
	 */
	public void setRange(IDocument document, int offset, int length) {
		if (DEBUG) {
			System.out.println("SET RANGE: offset=" + offset + " length="
					+ length);
		}

		fDocument = document;
		fOffset = offset;
		fTokenOffset = offset;
		fCurrentLength = 0;
		fLength = 0;
		fEnd = fOffset + length;
		fInString = false;
		fInDoubString = false;
		fContentType = IPHPPartitions.HTML;
		// String[] prev = getPartitionStack(offset);
	}

	private int read() {
		try {
			if (fOffset < fEnd) {
				fCurrentLength++;
				return fDocument.getChar(fOffset++);
			}
			return ICharacterScanner.EOF;
		} catch (BadLocationException e) {
			// should never happen
			// TODO write stacktrace to log
			fOffset = fEnd;
			return ICharacterScanner.EOF;
		}
	}

	private void unread() {
		--fOffset;
		--fCurrentLength;
	}

	private void unread(int num) {
		fOffset -= num;
		fCurrentLength -= num;
	}

	private boolean checkPattern(char[] pattern) {
		return checkPattern(pattern, false);
	}

	/**
	 * Check if next character sequence read from document is equals to the
	 * provided pattern. Pattern is read from left to right until the first
	 * character read doesn't match. If this happens all read characters are
	 * unread.
	 * 
	 * @param pattern
	 *            The pattern to check.
	 * @return <code>true</code> if pattern is equals else returns
	 *         <code>false</code>.
	 */
	private boolean checkPattern(char[] pattern, boolean ignoreCase) {
		int prevOffset = fOffset;
		int prevLength = fCurrentLength;
		for (int i = 0; i < pattern.length; i++) {
			int c = read();

			if (c == ICharacterScanner.EOF
					|| !letterEquals(c, pattern[i], ignoreCase)) {
				fOffset = prevOffset;
				fCurrentLength = prevLength;
				return false;
			}
		}

		return true;
	}

	private boolean letterEquals(int test, char letter, boolean ignoreCase) {
		if (test == letter)
			return true;
		else if (ignoreCase && Character.isLowerCase(letter)
				&& test == Character.toUpperCase(letter))
			return true;
		else if (ignoreCase && Character.isUpperCase(letter)
				&& test == Character.toLowerCase(letter))
			return true;

		return false;
	}

	/**
	 * Checks wether the offset is in a <code>String</code> and the specified
	 * contenttype is the current content type. Strings are delimited, mutual
	 * exclusive, by a " or by a '.
	 * 
	 * @param contentType
	 *            The contenttype to check.
	 * @return <code>true</code> if the current offset is in a string else
	 *         returns false.
	 */
	private boolean isInString(String contentType) {
		if (fContentType == contentType)
			return (fInString || fInDoubString);
		else
			return false;
	}

	/**
	 * Returns the previouse partition stack for the given offset.
	 * 
	 * @param offset
	 *            The offset to return the previouse partitionstack for.
	 * 
	 * @return The stack as a string array.
	 */
	private String[] getPartitionStack(int offset) {
		ArrayList types = new ArrayList();
		int tmpOffset = 0;
		try {
			ITypedRegion region = fDocument.getPartition(offset);
			tmpOffset = region.getOffset();
			while (tmpOffset - 1 > 0) {
				region = fDocument.getPartition(tmpOffset - 1);
				tmpOffset = region.getOffset();
				types.add(0, region.getType());
			}
		} catch (BadLocationException e) {
			if (DEBUG) {
				e.printStackTrace();
			}
		}

		String[] retVal = new String[types.size()];

		retVal = (String[]) types.toArray(retVal);
		return retVal;
	}

}
