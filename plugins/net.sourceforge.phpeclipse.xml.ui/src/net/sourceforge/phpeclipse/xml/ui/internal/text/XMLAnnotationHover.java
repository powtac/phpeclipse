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
 * $Id: XMLAnnotationHover.java,v 1.2 2006-10-21 23:14:13 pombredanne Exp $
 */
package net.sourceforge.phpeclipse.xml.ui.internal.text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;

/**
 * Implements simple annotation hover to show the associated messages.
 */
public class XMLAnnotationHover implements IAnnotationHover {
	/*
	 * @see org.eclipse.jface.text.source.IAnnotationHover#getHoverInfo(org.eclipse.jface.text.source.ISourceViewer,
	 *      int)
	 */
	public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
		List annotations = getAnnotationsForLine(sourceViewer, lineNumber);
		if (annotations != null) {
			List messages = new ArrayList();

			Iterator e = annotations.iterator();
			while (e.hasNext()) {
				Annotation annotation = (Annotation) e.next();

				String message = annotation.getText();
				if (message != null) {
					message = message.trim();
					if (message.length() > 0) {
						messages.add(message);
					}
				}
			}

			if (messages.size() == 1) {
				return (String) messages.get(0);
			}

			if (messages.size() > 1) {
				return formatMessages(messages);
			}
		}

		return null;
	}

	/**
	 * Formats multiple annotation messages for display.
	 */
	private String formatMessages(List messages) {
		StringBuffer buffer = new StringBuffer();

		Iterator e = messages.iterator();
		while (e.hasNext()) {
			buffer.append("- "); //$NON-NLS-1$
			buffer.append(e.next());
			buffer.append('\n');
		}

		return buffer.toString();
	}

	/**
	 * Returns annotations for the ruler's line of activity.
	 */
	private List getAnnotationsForLine(ISourceViewer viewer, int line) {
		IDocument document = viewer.getDocument();

		IAnnotationModel model = viewer.getAnnotationModel();
		if (model == null) {
			return null;
		}

		List retVal = new ArrayList();

		Iterator e = new XMLAnnotationIterator(model, true);
		while (e.hasNext()) {
			Annotation a = (Annotation) e.next();

			Position position = model.getPosition(a);
			if (position != null) {
				try {
					int annotationLine = document.getLineOfOffset(position
							.getOffset());
					if (annotationLine == line) {
						retVal.add(a);
					}
				} catch (BadLocationException e1) {
					// ignore
				}
			}
		}

		return retVal;
	}
}
