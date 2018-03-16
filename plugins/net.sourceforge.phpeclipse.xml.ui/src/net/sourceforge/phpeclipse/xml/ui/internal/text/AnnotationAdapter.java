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
 * $Id: AnnotationAdapter.java,v 1.2 2006-10-21 23:14:13 pombredanne Exp $
 */

package net.sourceforge.phpeclipse.xml.ui.internal.text;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.IReconcileResult;
import org.eclipse.jface.text.source.Annotation;

/**
 * Adapts a temporary or persistent annotation to a reconcile result.
 */
public abstract class AnnotationAdapter implements IReconcileResult {

	/**
	 * Creates and returns the annotation adapted by this adapter.
	 * 
	 * @return an annotation (can be temporary or persistent)
	 */
	public abstract Annotation createAnnotation();

	/**
	 * The position of the annotation adapted by this adapter.
	 * 
	 * @return the position
	 */
	public abstract Position getPosition();

}
