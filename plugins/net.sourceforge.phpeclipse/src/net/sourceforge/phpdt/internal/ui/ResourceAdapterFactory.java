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
package net.sourceforge.phpdt.internal.ui;

import net.sourceforge.phpdt.core.IJavaElement;
import net.sourceforge.phpdt.core.JavaCore;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;

public class ResourceAdapterFactory implements IAdapterFactory {

	private static Class[] PROPERTIES = new Class[] { IJavaElement.class };

	public Class[] getAdapterList() {
		return PROPERTIES;
	}

	public Object getAdapter(Object element, Class key) {
		if (IJavaElement.class.equals(key)) {
			return JavaCore.create((IResource) element);
		}
		return null;
	}
}
