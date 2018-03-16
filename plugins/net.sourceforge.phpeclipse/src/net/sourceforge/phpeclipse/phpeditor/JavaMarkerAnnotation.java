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
import net.sourceforge.phpdt.core.IJavaElement;
import net.sourceforge.phpdt.core.IJavaModelMarker;
import net.sourceforge.phpdt.core.JavaCore;
import net.sourceforge.phpdt.internal.corext.util.JavaModelUtil;

import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.MarkerAnnotation;

public class JavaMarkerAnnotation extends MarkerAnnotation implements
		IJavaAnnotation {

	public static final String JAVA_MARKER_TYPE_PREFIX = "net.sourceforge.phpdt"; //$NON-NLS-1$

	public static final String ERROR_ANNOTATION_TYPE = "net.sourceforge.phpdt.ui.error"; //$NON-NLS-1$

	public static final String WARNING_ANNOTATION_TYPE = "net.sourceforge.phpdt.ui.warning"; //$NON-NLS-1$

	public static final String INFO_ANNOTATION_TYPE = "net.sourceforge.phpdt.ui.info"; //$NON-NLS-1$

	public static final String TASK_ANNOTATION_TYPE = "org.eclipse.ui.workbench.texteditor.task"; //$NON-NLS-1$

	private IJavaAnnotation fOverlay;

	public JavaMarkerAnnotation(IMarker marker) {
		super(marker);
	}

	/*
	 * @see net.sourceforge.phpdt.internal.ui.javaeditor.IJavaAnnotation#getImage(org.eclipse.swt.widgets.Display)
	 */
	public Image getImage(Display display) {
		return super.getImage(display);
	}

	/*
	 * @see IJavaAnnotation#getArguments()
	 */
	public String[] getArguments() {
		IMarker marker = getMarker();
		if (marker != null && marker.exists() && isProblem())
			return JavaModelUtil.getProblemArgumentsFromMarker(marker
					.getAttribute(IJavaModelMarker.ARGUMENTS, "")); //$NON-NLS-1$
		return null;
	}

	/*
	 * @see IJavaAnnotation#getId()
	 */
	public int getId() {
		IMarker marker = getMarker();
		if (marker == null || !marker.exists())
			return -1;

		if (isProblem())
			return marker.getAttribute(IJavaModelMarker.ID, -1);

		// if (TASK_ANNOTATION_TYPE.equals(getAnnotationType())) {
		// try {
		// if (marker.isSubtypeOf(IJavaModelMarker.TASK_MARKER)) {
		// return IProblem.Task;
		// }
		// } catch (CoreException e) {
		// JavaPlugin.log(e); // should no happen, we test for marker.exists
		// }
		// }

		return -1;
	}

	/*
	 * @see IJavaAnnotation#isProblem()
	 */
	public boolean isProblem() {
		String type = getType();
		return WARNING_ANNOTATION_TYPE.equals(type)
				|| ERROR_ANNOTATION_TYPE.equals(type);
	}

	/**
	 * Overlays this annotation with the given javaAnnotation.
	 * 
	 * @param javaAnnotation
	 *            annotation that is overlaid by this annotation
	 */
	public void setOverlay(IJavaAnnotation javaAnnotation) {
		if (fOverlay != null)
			fOverlay.removeOverlaid(this);

		fOverlay = javaAnnotation;
		if (!isMarkedDeleted())
			markDeleted(fOverlay != null);

		if (fOverlay != null)
			fOverlay.addOverlaid(this);
	}

	/*
	 * @see IJavaAnnotation#hasOverlay()
	 */
	public boolean hasOverlay() {
		return fOverlay != null;
	}

	/*
	 * @see net.sourceforge.phpdt.internal.ui.javaeditor.IJavaAnnotation#getOverlay()
	 */
	public IJavaAnnotation getOverlay() {
		return fOverlay;
	}

	/*
	 * @see IJavaAnnotation#addOverlaid(IJavaAnnotation)
	 */
	public void addOverlaid(IJavaAnnotation annotation) {
		// not supported
	}

	/*
	 * @see IJavaAnnotation#removeOverlaid(IJavaAnnotation)
	 */
	public void removeOverlaid(IJavaAnnotation annotation) {
		// not supported
	}

	/*
	 * @see IJavaAnnotation#getOverlaidIterator()
	 */
	public Iterator getOverlaidIterator() {
		// not supported
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.phpdt.internal.ui.javaeditor.IJavaAnnotation#getCompilationUnit()
	 */
	public ICompilationUnit getCompilationUnit() {
		IJavaElement element = JavaCore.create(getMarker().getResource());
		if (element instanceof ICompilationUnit) {
			ICompilationUnit cu = (ICompilationUnit) element;
			ICompilationUnit workingCopy = EditorUtility.getWorkingCopy(cu);
			if (workingCopy != null) {
				return workingCopy;
			}
			return cu;
		}
		return null;
	}
}
