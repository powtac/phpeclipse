/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.phpdt.internal.ui.text.java.hover;

import net.sourceforge.phpdt.ui.text.java.hover.IJavaEditorTextHover;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorPart;

public class JavaTypeHover implements IJavaEditorTextHover {

	private IJavaEditorTextHover fAnnotationHover;

	// private IJavaEditorTextHover fJavadocHover;

	public JavaTypeHover() {
		fAnnotationHover = new AnnotationHover();
		// fJavadocHover= new JavadocHover();
	}

	/**
	 * @see IJavaEditorTextHover#setEditor(IEditorPart)
	 */
	public void setEditor(IEditorPart editor) {
		fAnnotationHover.setEditor(editor);
		// fJavadocHover.setEditor(editor);
	}

	/*
	 * @see ITextHover#getHoverRegion(ITextViewer, int)
	 */
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		// return fJavadocHover.getHoverRegion(textViewer, offset);
		return null;
	}

	/*
	 * @see ITextHover#getHoverInfo(ITextViewer, IRegion)
	 */
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		String hoverInfo = fAnnotationHover.getHoverInfo(textViewer,
				hoverRegion);
		if (hoverInfo != null)
			return hoverInfo;

		// return fJavadocHover.getHoverInfo(textViewer, hoverRegion);
		return null;
	}
}
