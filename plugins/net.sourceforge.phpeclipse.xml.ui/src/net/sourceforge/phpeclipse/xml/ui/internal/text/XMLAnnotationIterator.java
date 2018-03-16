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
 * $Id: XMLAnnotationIterator.java,v 1.2 2006-10-21 23:14:13 pombredanne Exp $
 */
package net.sourceforge.phpeclipse.xml.ui.internal.text;

import java.util.Iterator;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;

/**
 * @author Igor Malinin
 */
public class XMLAnnotationIterator implements Iterator {
	private boolean skipIrrelevants;

	private Iterator iterator;

	private Annotation next;

	public XMLAnnotationIterator(IAnnotationModel model, boolean skipIrrelevants) {
		this.skipIrrelevants = skipIrrelevants;

		iterator = model.getAnnotationIterator();
		skip();
	}

	private void skip() {
		while (iterator.hasNext()) {
			Annotation next = (Annotation) iterator.next();
			if (next instanceof XMLAnnotation) {
				if (skipIrrelevants) {
					if (!next.isMarkedDeleted()) {
						this.next = next;
						return;
					}
				} else {
					this.next = next;
					return;
				}
			}
		}

		this.next = null;
	}

	/*
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		return (next != null);
	}

	/*
	 * @see java.util.Iterator#next()
	 */
	public Object next() {
		try {
			return next;
		} finally {
			skip();
		}
	}

	/*
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
