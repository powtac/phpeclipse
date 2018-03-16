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
package net.sourceforge.phpeclipse.phpeditor;

import java.util.Iterator;

import net.sourceforge.phpdt.core.ICompilationUnit;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * Interface of annotations representing markers and problems.
 * 
 * @see org.eclipse.core.resources.IMarker
 * @see net.sourceforge.phpdt.core.compiler.IProblem
 */
public interface IJavaAnnotation {

	/**
	 * @see org.eclipse.jface.text.source.Annotation#getType()
	 */
	String getType();

	/**
	 * @see org.eclipse.jface.text.source.Annotation#isPersistent()
	 */
	boolean isPersistent();

	/**
	 * @see org.eclipse.jface.text.source.Annotation#isMarkedDeleted()
	 */
	boolean isMarkedDeleted();

	/**
	 * @see org.eclipse.jface.text.source.Annotation#getText()
	 */
	String getText();

	/**
	 * Returns whether this annotation is overlaid.
	 * 
	 * @return <code>true</code> if overlaid
	 */
	boolean hasOverlay();

	/**
	 * Returns the overlay of this annotation.
	 * 
	 * @return the annotation's overlay
	 * @since 3.0
	 */
	IJavaAnnotation getOverlay();

	/**
	 * Returns an iterator for iterating over the annotation which are overlaid
	 * by this annotation.
	 * 
	 * @return an iterator over the overlaid annotaions
	 */
	Iterator getOverlaidIterator();

	/**
	 * Adds the given annotation to the list of annotations which are overlaid
	 * by this annotations.
	 * 
	 * @param annotation
	 *            the problem annoation
	 */
	void addOverlaid(IJavaAnnotation annotation);

	/**
	 * Removes the given annotation from the list of annotations which are
	 * overlaid by this annotation.
	 * 
	 * @param annotation
	 *            the problem annoation
	 */
	void removeOverlaid(IJavaAnnotation annotation);

	/**
	 * Tells whether this annotation is a problem annotation.
	 * 
	 * @return <code>true</code> if it is a problem annotation
	 */
	boolean isProblem();

	/**
	 * Returns the compilation unit corresponding to the document on which the
	 * annotation is set or <code>null</code> if no corresponding
	 * co0mpilationunit exists.
	 */
	ICompilationUnit getCompilationUnit();

	String[] getArguments();

	int getId();

	Image getImage(Display display);
}
