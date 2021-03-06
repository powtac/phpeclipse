/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.phpdt.internal.ui.filters;

import net.sourceforge.phpdt.core.IType;
import net.sourceforge.phpdt.core.JavaModelException;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Filters classes
 */
public class ClassFilter extends ViewerFilter {

	/*
	 * @see ViewerFilter
	 */
	public boolean select(Viewer viewer, Object parent, Object element) {
		if (element instanceof IType) {
			try {
				return ((IType) element).isInterface();
			} catch (JavaModelException ex) {
				return true;
			}
		}
		return true;
	}
}
