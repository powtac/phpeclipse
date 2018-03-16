/*
 * Copyright (c) 2003-2004 Christopher Lenz and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Christopher Lenz - initial API and implementation
 * 
 * $Id: XMLTextHover.java,v 1.2 2006-10-21 23:14:13 pombredanne Exp $
 */
package net.sourceforge.phpeclipse.xml.ui.internal.text;

import java.util.Iterator;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;

/**
 * Implements simple annotation hover to show the associated messages.
 */
public class XMLTextHover implements ITextHover {
	/**
	 * This hovers annotation model.
	 */
	private IAnnotationModel model;

	/**
	 * Creates a new annotation hover.
	 * 
	 * @param model
	 *            this hover's annotation model
	 */
	public XMLTextHover(IAnnotationModel model) {
		this.model = model;
	}

	/*
	 * @see ITextHover#getHoverInfo(ITextViewer, IRegion)
	 */
	public String getHoverInfo(ITextViewer textViewer, IRegion region) {
		Iterator e = new XMLAnnotationIterator(model, true);
		while (e.hasNext()) {
			Annotation a = (Annotation) e.next();

			Position p = model.getPosition(a);
			if (p.overlapsWith(region.getOffset(), region.getLength())) {
				String text = a.getText();
				if ((text != null) && (text.trim().length() > 0)) {
					return text;
				}
			}
		}

		return null;
	}

	/*
	 * @see ITextHover#getHoverRegion(ITextViewer, int)
	 */
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		return XMLWordFinder.findWord(textViewer.getDocument(), offset);
	}
}
