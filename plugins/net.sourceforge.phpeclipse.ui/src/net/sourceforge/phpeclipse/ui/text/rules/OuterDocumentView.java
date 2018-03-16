/*
 * Copyright (c) 2002-2004 Widespace, OU and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://solareclipse.sourceforge.net/legal/cpl-v10.html
 * 
 * Contributors:
 *     Igor Malinin - initial contribution
 * 
 * $Id: OuterDocumentView.java,v 1.5 2006-10-21 23:13:53 pombredanne Exp $
 */

package net.sourceforge.phpeclipse.ui.text.rules;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextStore;

/**
 * Outer view to parent document.
 * 
 * @author Igor Malinin
 */
public class OuterDocumentView extends AbstractDocument implements
		IDocumentView {

	/**
	 * Implements ITextStore based on IDocument.
	 */
	class TextStore implements ITextStore {

		/*
		 * @see ITextStore#set
		 */
		public void set(String txt) {
			try {
				parent.replace(0, parent.getLength(), txt);
			} catch (BadLocationException x) {
				// cannot happen
			}
		}

		/*
		 * @see ITextStore#replace
		 */
		public void replace(int offset, int length, String txt) {
			try {
				int start = getParentOffset(offset);
				int end = getParentOffset(offset + length - 1) + 1;

				parent.replace(start, end - start, txt);
			} catch (BadLocationException x) {
				// ignored as surrounding document should have handled this
			}
		}

		/*
		 * @see ITextStore#getLength
		 */
		public int getLength() {
			int length = parent.getLength();

			Iterator i = ranges.iterator();
			while (i.hasNext()) {
				length -= ((FlatNode) i.next()).length;
			}

			return length;
		}

		/*
		 * @see ITextStore#get
		 */
		public String get(int offset, int length) {
			StringBuffer buf = new StringBuffer(length);

			try {
				FlatNode range = null;

				Iterator i = ranges.iterator();
				while (i.hasNext()) {
					range = (FlatNode) i.next();

					if (offset < range.offset) {
						break;
					}

					offset += range.length;

					range = null;
				}

				int remainder = length - buf.length();
				while (remainder > 0) {
					if (range == null || offset + remainder < range.offset) {
						buf.append(parent.get(offset, remainder));
						break;
					}

					buf.append(parent.get(offset, range.offset - offset));
					offset = range.offset + range.length;
					range = i.hasNext() ? (FlatNode) i.next() : null;

					remainder = length - buf.length();
				}
			} catch (BadLocationException x) {
				return null;
			}

			return buf.toString();
		}

		/*
		 * @see ITextStore#get
		 */
		public char get(int offset) {
			try {
				return parent.getChar(getParentOffset(offset));
			} catch (BadLocationException x) {
			}

			return (char) 0;
		}
	}

	/** The parent document */
	IDocument parent;

	/** The section inside the parent document */
	List ranges;

	/**
	 * Constructs outer view to parent document.
	 * 
	 * @param parent
	 *            parent document
	 */
	public OuterDocumentView(IDocument parent, List ranges) {
		this.parent = parent;
		this.ranges = ranges;

		setTextStore(new TextStore());
		setLineTracker(new DefaultLineTracker());
		int length = getLength();
		if (length < 0) {
			length = 0;
		}
		getTracker().set(getStore().get(0, length));

		completeInitialization();
	}

	// public void addRange(Position range) {
	// DocumentEvent event = new DocumentEvent(this,
	// getLocalOffset(range.offset), range.length, "");
	// fireDocumentAboutToBeChanged(event);
	// ranges.add(-getIndex(range) - 1, range);
	// fireDocumentChanged(event);
	// }
	//
	// public void removeRange(Position range) {
	// String text;
	// try {
	// text = parent.get(range.offset, range.length);
	// } catch (BadLocationException e) {
	// return;
	// }
	// DocumentEvent event = new DocumentEvent(this,
	// getLocalOffset(range.offset), 0, text);
	// fireDocumentAboutToBeChanged(event);
	// deleteRange(range);
	// fireDocumentChanged(event);
	// }
	//
	// public void deleteRange(Position range) {
	// ranges.remove(getIndex(range));
	// }
	//
	// private int getIndex(Position range) {
	// return Collections.binarySearch(ranges, range, new Comparator() {
	// public int compare(Object o1, Object o2) {
	// int offset1 = ((Position) o1).offset;
	// int offset2 = ((Position) o2).offset;
	//
	// if (offset1 < offset2) return -1;
	// if (offset1 > offset2) return 1;
	// return 0;
	// }
	// });
	// }

	/*
	 * @see org.eclipse.jface.text.AbstractDocument#fireDocumentAboutToBeChanged(DocumentEvent)
	 */
	protected void fireDocumentAboutToBeChanged(DocumentEvent event) {
		super.fireDocumentAboutToBeChanged(event);
	}

	/*
	 * @see org.eclipse.jface.text.AbstractDocument#fireDocumentChanged(DocumentEvent)
	 */
	protected void fireDocumentChanged(DocumentEvent event) {
		try {
			// TODO: move to a better place
			getTracker().replace(event.getOffset(), event.getLength(),
					event.getText());
		} catch (IndexOutOfBoundsException x) {

		} catch (BadLocationException x) {
		}

		super.fireDocumentChanged(event);
	}

	/*
	 * @see net.sourceforge.phpeclipse.text.rules.IDocumentView#getParentDocument()
	 */
	public IDocument getParentDocument() {
		return parent;
	}

	/*
	 * @see net.sourceforge.phpeclipse.text.rules.IDocumentView#getParentOffset(int)
	 */
	public int getParentOffset(int localOffset) {
		int offset = localOffset;

		Iterator i = ranges.iterator();
		while (i.hasNext()) {
			FlatNode range = (FlatNode) i.next();

			if (offset < range.offset) {
				break;
			}

			offset += range.length;
		}

		return offset;
	}

	/*
	 * @see net.sf.wdte.text.rules.IDocumentView#getLocalOffset(int)
	 */
	public int getLocalOffset(int parentOffset) {
		// Assert.isTrue(parentOffset>=0);
		int localOffset = parentOffset;

		Iterator i = ranges.iterator();
		while (i.hasNext()) {
			FlatNode range = (FlatNode) i.next();

			if (parentOffset <= range.offset) {
				break;
			}

			if (parentOffset <= range.offset + range.length) {
				localOffset -= parentOffset - range.offset;
				break;
			}

			localOffset -= range.length;
		}
		// TODO jsurfer change start - check this
		if (localOffset < 0) {
			return 0;
		} else if (localOffset > getLength()) {
			return getLength();
		}
		// jsurfer change end
		return localOffset;
	}
}
