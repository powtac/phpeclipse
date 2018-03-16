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
package net.sourceforge.phpdt.internal.corext.refactoring.util;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.phpdt.core.ICompilationUnit;
import net.sourceforge.phpdt.core.IJavaElement;
import net.sourceforge.phpdt.core.IMember;
import net.sourceforge.phpdt.core.IOpenable;
import net.sourceforge.phpdt.internal.corext.Assert;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

public class ResourceUtil {

	private ResourceUtil() {
	}

	public static IFile[] getFiles(ICompilationUnit[] cus) {
		List files = new ArrayList(cus.length);
		for (int i = 0; i < cus.length; i++) {
			IResource resource = ResourceUtil.getResource(cus[i]);
			if (resource != null && resource.getType() == IResource.FILE)
				files.add(resource);
		}
		return (IFile[]) files.toArray(new IFile[files.size()]);
	}

	public static IFile getFile(ICompilationUnit cu) {
		IResource resource = ResourceUtil.getResource(cu);
		if (resource != null && resource.getType() == IResource.FILE)
			return (IFile) resource;
		else
			return null;
	}

	// ----- other ------------------------------

	/**
	 * Finds an <code>IResource</code> for a given
	 * <code>ICompilationUnit</code>. If the parameter is a working copy then
	 * the <code>IResource</code> for the original element is returned.
	 */
	public static IResource getResource(ICompilationUnit cu) {
		return cu.getResource();
	}

	/**
	 * Returns the <code>IResource</code> that the given <code>IMember</code>
	 * is defined in.
	 * 
	 * @see #getResource
	 */
	public static IResource getResource(IMember member) {
		Assert.isTrue(!member.isBinary());
		return getResource(member.getCompilationUnit());
	}

	public static IResource getResource(Object o) {
		if (o instanceof IResource)
			return (IResource) o;
		if (o instanceof IJavaElement)
			return getResource((IJavaElement) o);
		return null;
	}

	private static IResource getResource(IJavaElement element) {
		if (element.getElementType() == IJavaElement.COMPILATION_UNIT)
			return getResource((ICompilationUnit) element);
		else if (element instanceof IOpenable)
			return element.getResource();
		else
			return null;
	}
}
