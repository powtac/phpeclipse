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
 * $Id: AttValueDoubleClickStrategy.java,v 1.3 2006-10-21 23:14:13 pombredanne Exp $
 */

package net.sourceforge.phpeclipse.xml.ui.internal.text;

import net.sourceforge.phpeclipse.ui.text.TextDoubleClickStrategy;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;

/**
 * 
 * 
 * @author Igor Malinin
 */
public class AttValueDoubleClickStrategy extends TextDoubleClickStrategy {
	/*
	 * @see org.eclipse.jface.text.ITextDoubleClickStrategy#doubleClicked(ITextViewer)
	 */
	public void doubleClicked(ITextViewer viewer) {
		int offset = viewer.getSelectedRange().x;
		if (offset < 0) {
			return;
		}

		try {
			IDocument document = viewer.getDocument();

			ITypedRegion region = document.getPartition(offset);

			int start = region.getOffset();
			int length = region.getLength();
			int end = start + length - 1;

			if (offset == start) {
				if (document.getChar(start) == document.getChar(end)) {
					viewer.setSelectedRange(start + 1, length - 2);
				} else {
					viewer.setSelectedRange(start + 1, length - 1);
				}

				return;
			}

			if (offset == end) {
				if (document.getChar(start) == document.getChar(end)) {
					viewer.setSelectedRange(start + 1, length - 2);
					return;
				}
			}

			super.doubleClicked(viewer);
		} catch (BadLocationException e) {
		}
	}
}
