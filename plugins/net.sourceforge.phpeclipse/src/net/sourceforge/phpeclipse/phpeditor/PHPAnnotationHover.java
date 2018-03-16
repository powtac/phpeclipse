package net.sourceforge.phpeclipse.phpeditor;

/**********************************************************************
 Copyright (c) 2000, 2002 IBM Corp. and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html

 Contributors:
 IBM Corporation - Initial implementation
 www.phpeclipse.de
 **********************************************************************/

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * The PHPAnnotationHover provides the hover support for PHP editors.
 */

public class PHPAnnotationHover implements IAnnotationHover {

	/*
	 * (non-Javadoc) Method declared on IAnnotationHover
	 */
	// public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
	// IDocument document= sourceViewer.getDocument();
	//
	// try {
	// IRegion info= document.getLineInformation(lineNumber);
	// return document.get(info.getOffset(), info.getLength());
	// } catch (BadLocationException x) {
	// }
	//
	// return null;
	// }
	//	
	static final int MAX_INFO_LENGTH = 80;

	/**
	 * @see org.eclipse.jface.text.source.IAnnotationHover#getHoverInfo(org.eclipse.jface.text.source.ISourceViewer,
	 *      int)
	 */

	public String getHoverInfo(ISourceViewer viewer, int line) {
		String info = null;
		List markers = getMarkersForLine(viewer, line);
		if (markers != null) {
			info = "";
			for (int i = 0; i < markers.size(); i++) {
				IMarker marker = (IMarker) markers.get(i);
				String message = marker.getAttribute(IMarker.MESSAGE,
						(String) null);
				if (message != null && message.trim().length() > 0) {

					if (message.length() > MAX_INFO_LENGTH) {
						message = splitMessage(message);
					}
					info += message;

					if (i != markers.size() - 1) {
						info += "\n";
					}
				}
			}
		}
		return info;
	}

	private String splitMessage(String message) {
		String result = "";

		if (message.length() <= MAX_INFO_LENGTH) {
			return message;
		}

		String tmpStr = new String(message);

		while (tmpStr.length() > MAX_INFO_LENGTH) {

			int spacepos = tmpStr.indexOf(" ", MAX_INFO_LENGTH);

			if (spacepos != -1) {
				result += tmpStr.substring(0, spacepos) + "\n";
				tmpStr = tmpStr.substring(spacepos);
			} else {
				result += tmpStr.substring(0, MAX_INFO_LENGTH) + "\n";
				tmpStr = tmpStr.substring(MAX_INFO_LENGTH);
			}

		}

		result += tmpStr;

		return result;
	}

	/**
	 * Returns all markers which includes the ruler's line of activity.
	 */
	protected List getMarkersForLine(ISourceViewer aViewer, int aLine) {
		List markers = new ArrayList();
		IAnnotationModel model = aViewer.getAnnotationModel();
		if (model != null) {
			Iterator e = model.getAnnotationIterator();
			while (e.hasNext()) {
				Object o = e.next();
				if (o instanceof MarkerAnnotation) {
					MarkerAnnotation a = (MarkerAnnotation) o;
					if (compareRulerLine(model.getPosition(a), aViewer
							.getDocument(), aLine) != 0) {
						markers.add(a.getMarker());
					}
				}
			}
		}
		return markers;
	}

	/**
	 * Returns one marker which includes the ruler's line of activity.
	 */
	protected IMarker getMarkerForLine(ISourceViewer aViewer, int aLine) {
		IMarker marker = null;
		IAnnotationModel model = aViewer.getAnnotationModel();
		if (model != null) {
			Iterator e = model.getAnnotationIterator();
			while (e.hasNext()) {
				Object o = e.next();
				if (o instanceof MarkerAnnotation) {
					MarkerAnnotation a = (MarkerAnnotation) o;
					if (compareRulerLine(model.getPosition(a), aViewer
							.getDocument(), aLine) != 0) {
						marker = a.getMarker();
					}
				}
			}
		}
		return marker;
	}

	/**
	 * Returns distance of given line to specified position (1 = same line, 2 =
	 * included in given position, 0 = not related).
	 */
	protected int compareRulerLine(Position aPosition, IDocument aDocument,
			int aLine) {
		int distance = 0;
		if (aPosition.getOffset() > -1 && aPosition.getLength() > -1) {
			try {
				int markerLine = aDocument.getLineOfOffset(aPosition
						.getOffset());
				if (aLine == markerLine) {
					distance = 1;
				} else if (markerLine <= aLine
						&& aLine <= aDocument.getLineOfOffset(aPosition
								.getOffset()
								+ aPosition.getLength())) {
					distance = 2;
				}
			} catch (BadLocationException e) {
			}
		}
		return distance;
	}
}
