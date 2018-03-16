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
package net.sourceforge.phpdt.internal.ui.viewsupport;

import net.sourceforge.phpdt.core.Flags;
import net.sourceforge.phpdt.core.IField;
import net.sourceforge.phpdt.core.IJavaElement;
import net.sourceforge.phpdt.core.IMember;
import net.sourceforge.phpdt.core.IType;
import net.sourceforge.phpdt.core.JavaModelException;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Filter for the methods viewer. Changing a filter property does not trigger a
 * refiltering of the viewer
 */
public class MemberFilter extends ViewerFilter {

	public static final int FILTER_NONPUBLIC = 1;

	public static final int FILTER_STATIC = 2;

	public static final int FILTER_FIELDS = 4;

	private int fFilterProperties;

	/**
	 * Modifies filter and add a property to filter for
	 */
	public final void addFilter(int filter) {
		fFilterProperties |= filter;
	}

	/**
	 * Modifies filter and remove a property to filter for
	 */
	public final void removeFilter(int filter) {
		fFilterProperties &= (-1 ^ filter);
	}

	/**
	 * Tests if a property is filtered
	 */
	public final boolean hasFilter(int filter) {
		return (fFilterProperties & filter) != 0;
	}

	/*
	 * @see ViewerFilter@isFilterProperty
	 */
	public boolean isFilterProperty(Object element, Object property) {
		return false;
	}

	/*
	 * @see ViewerFilter@select
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		try {
			if (hasFilter(FILTER_FIELDS) && element instanceof IField) {
				return false;
			}
			if (element instanceof IMember) {
				IMember member = (IMember) element;
				if (member.getElementName().startsWith("<")) { // filter out
																// <clinit>
																// //$NON-NLS-1$
					return false;
				}
				int flags = member.getFlags();
				if (hasFilter(FILTER_STATIC)
						&& (Flags.isStatic(flags) || isFieldInInterface(member))
						&& member.getElementType() != IJavaElement.TYPE) {
					return false;
				}
				if (hasFilter(FILTER_NONPUBLIC) && !Flags.isPublic(flags)
						&& !isMemberInInterface(member)
						&& !isTopLevelType(member)) {
					return false;
				}
			}
		} catch (JavaModelException e) {
			// ignore
		}
		return true;
	}

	private boolean isMemberInInterface(IMember member)
			throws JavaModelException {
		IType parent = member.getDeclaringType();
		return parent != null && parent.isInterface();
	}

	private boolean isFieldInInterface(IMember member)
			throws JavaModelException {
		return (member.getElementType() == IJavaElement.FIELD)
				&& member.getDeclaringType().isInterface();
	}

	private boolean isTopLevelType(IMember member) throws JavaModelException {
		IType parent = member.getDeclaringType();
		return parent == null;
	}
}
