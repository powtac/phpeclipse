/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.phpdt.internal.ui.text.java;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationPresenter;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;

public class JavaParameterListValidator implements
		IContextInformationValidator, IContextInformationPresenter {

	private int fPosition;

	private ITextViewer fViewer;

	private IContextInformation fInformation;

	private int fCurrentParameter;

	public JavaParameterListValidator() {
	}

	/**
	 * @see IContextInformationValidator#install(IContextInformation,
	 *      ITextViewer, int)
	 * @see IContextInformationPresenter#install(IContextInformation,
	 *      ITextViewer, int)
	 */
	public void install(IContextInformation info, ITextViewer viewer,
			int documentPosition) {
		fPosition = documentPosition;
		fViewer = viewer;
		fInformation = info;

		fCurrentParameter = -1;
	}

	private int getCommentEnd(IDocument d, int pos, int end)
			throws BadLocationException {
		while (pos < end) {
			char curr = d.getChar(pos);
			pos++;
			if (curr == '*') {
				if (pos < end && d.getChar(pos) == '/') {
					return pos + 1;
				}
			}
		}
		return end;
	}

	private int getStringEnd(IDocument d, int pos, int end, char ch)
			throws BadLocationException {
		while (pos < end) {
			char curr = d.getChar(pos);
			pos++;
			if (curr == '\\') {
				// ignore escaped characters
				pos++;
			} else if (curr == ch) {
				return pos;
			}
		}
		return end;
	}

	private int getCharCount(IDocument document, int start, int end,
			String increments, String decrements, boolean considerNesting)
			throws BadLocationException {

		Assert.isTrue((increments.length() != 0 || decrements.length() != 0)
				&& !increments.equals(decrements));

		int nestingLevel = 0;
		int charCount = 0;
		while (start < end) {
			char curr = document.getChar(start++);
			switch (curr) {
			case '/':
				if (start < end) {
					char next = document.getChar(start);
					if (next == '*') {
						// a comment starts, advance to the comment end
						start = getCommentEnd(document, start + 1, end);
					} else if (next == '/') {
						// '//'-comment: nothing to do anymore on this line
						start = end;
					}
				}
				break;
			case '*':
				if (start < end) {
					char next = document.getChar(start);
					if (next == '/') {
						// we have been in a comment: forget what we read before
						charCount = 0;
						++start;
					}
				}
				break;
			case '"':
			case '\'':
				start = getStringEnd(document, start, end, curr);
				break;
			default:

				if (considerNesting) {

					if ('(' == curr)
						++nestingLevel;
					else if (')' == curr)
						--nestingLevel;

					if (nestingLevel != 0)
						break;
				}

				if (increments.indexOf(curr) >= 0) {
					++charCount;
				}

				if (decrements.indexOf(curr) >= 0) {
					--charCount;
				}
			}
		}

		return charCount;
	}

	/**
	 * @see IContextInformationValidator#isContextInformationValid(int)
	 */
	public boolean isContextInformationValid(int position) {

		try {
			if (position < fPosition)
				return false;

			IDocument document = fViewer.getDocument();
			IRegion line = document.getLineInformationOfOffset(fPosition);

			if (position < line.getOffset() || position >= document.getLength())
				return false;

			return getCharCount(document, fPosition, position,
					"(<", ")>", false) >= 0; //$NON-NLS-1$//$NON-NLS-2$

		} catch (BadLocationException x) {
			return false;
		}
	}

	/**
	 * @see IContextInformationPresenter#updatePresentation(int,
	 *      TextPresentation)
	 */
	public boolean updatePresentation(int position,
			TextPresentation presentation) {

		int currentParameter = -1;

		try {
			currentParameter = getCharCount(fViewer.getDocument(), fPosition,
					position, ",", "", true); //$NON-NLS-1$//$NON-NLS-2$
		} catch (BadLocationException x) {
			return false;
		}

		if (fCurrentParameter != -1) {
			if (currentParameter == fCurrentParameter)
				return false;
		}

		presentation.clear();
		fCurrentParameter = currentParameter;

		String s = fInformation.getInformationDisplayString();
		int start = 0;
		int occurrences = 0;
		while (occurrences < fCurrentParameter) {
			int found = s.indexOf(',', start);
			if (found == -1)
				break;
			start = found + 1;
			++occurrences;
		}

		if (occurrences < fCurrentParameter) {
			presentation.addStyleRange(new StyleRange(0, s.length(), null,
					null, SWT.NORMAL));
			return true;
		}

		if (start == -1)
			start = 0;

		int end = s.indexOf(',', start);
		if (end == -1)
			end = s.length();

		if (start > 0)
			presentation.addStyleRange(new StyleRange(0, start, null, null,
					SWT.NORMAL));

		if (end > start)
			presentation.addStyleRange(new StyleRange(start, end - start, null,
					null, SWT.BOLD));

		if (end < s.length())
			presentation.addStyleRange(new StyleRange(end, s.length() - end,
					null, null, SWT.NORMAL));

		return true;
	}
}
