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

import net.sourceforge.phpdt.core.ICompilationUnit;
import net.sourceforge.phpdt.core.IJavaElement;
import net.sourceforge.phpdt.core.IPackageFragmentRoot;
import net.sourceforge.phpdt.internal.corext.util.JavaModelUtil;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.IContributorResourceAdapter;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.views.properties.FilePropertySource;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.ResourcePropertySource;

/**
 * Implements basic UI support for Java elements. Implements handle to
 * persistent support for Java elements.
 */
public class JavaElementAdapterFactory implements IAdapterFactory,
		IContributorResourceAdapter {

	private static Class[] PROPERTIES = new Class[] {
			IPropertySource.class,
			IResource.class,
			// IWorkbenchAdapter.class,
			// IResourceLocator.class,
			IPersistableElement.class, IProject.class,
			IContributorResourceAdapter.class,
	// ITaskListResourceAdapter.class,
	// IContainmentAdapter.class
	};

	// private Object fSearchPageScoreComputer;
	// private static IResourceLocator fgResourceLocator= new ResourceLocator();
	// private static JavaWorkbenchAdapter fgJavaWorkbenchAdapter= new
	// JavaWorkbenchAdapter();
	// private static ITaskListResourceAdapter fgTaskListAdapter= new
	// JavaTaskListAdapter();
	// private static JavaElementContainmentAdapter
	// fgJavaElementContainmentAdapter= new JavaElementContainmentAdapter();

	public Class[] getAdapterList() {
		// updateLazyLoadedAdapters();
		return PROPERTIES;
	}

	public Object getAdapter(Object element, Class key) {
		// updateLazyLoadedAdapters();
		IJavaElement java = (IJavaElement) element;

		if (IPropertySource.class.equals(key)) {
			return getProperties(java);
		}
		if (IResource.class.equals(key)) {
			return getResource(java);
		}
		if (IProject.class.equals(key)) {
			return getProject(java);
			// } if (fSearchPageScoreComputer != null &&
			// ISearchPageScoreComputer.class.equals(key)) {
			// return fSearchPageScoreComputer;
			// } if (IWorkbenchAdapter.class.equals(key)) {
			// return fgJavaWorkbenchAdapter;
			// } if (IResourceLocator.class.equals(key)) {
			// return fgResourceLocator;
			// } if (IPersistableElement.class.equals(key)) {
			// return new PersistableJavaElementFactory(java);
		}
		if (IContributorResourceAdapter.class.equals(key)) {
			return this;
			// } if (ITaskListResourceAdapter.class.equals(key)) {
			// return fgTaskListAdapter;
			// } if (IContainmentAdapter.class.equals(key)) {
			// return fgJavaElementContainmentAdapter;
		}
		return null;
	}

	private IResource getResource(IJavaElement element) {
		// can't use IJavaElement.getResource directly as we are interrested in
		// the
		// corresponding resource
		switch (element.getElementType()) {
		case IJavaElement.TYPE:
			// top level types behave like the CU
			IJavaElement parent = element.getParent();
			if (parent instanceof ICompilationUnit) {
				return JavaModelUtil.toOriginal((ICompilationUnit) parent)
						.getResource();
			}
			return null;
		case IJavaElement.COMPILATION_UNIT:
			return JavaModelUtil.toOriginal((ICompilationUnit) element)
					.getResource();
		case IJavaElement.CLASS_FILE:
		case IJavaElement.PACKAGE_FRAGMENT:
			// test if in a archive
			IPackageFragmentRoot root = (IPackageFragmentRoot) element
					.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
			if (!root.isArchive()) {
				return element.getResource();
			}
			return null;
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
		case IJavaElement.JAVA_PROJECT:
		case IJavaElement.JAVA_MODEL:
			return element.getResource();
		default:
			return null;
		}
	}

	/*
	 * @see org.eclipse.ui.IContributorResourceAdapter#getAdaptedResource(org.eclipse.core.runtime.IAdaptable)
	 */
	public IResource getAdaptedResource(IAdaptable adaptable) {
		return getResource((IJavaElement) adaptable);
	}

	private IResource getProject(IJavaElement element) {
		return element.getJavaProject().getProject();
	}

	private IPropertySource getProperties(IJavaElement element) {
		IResource resource = getResource(element);
		if (resource == null)
			return new JavaElementProperties(element);
		if (resource.getType() == IResource.FILE)
			return new FilePropertySource((IFile) resource);
		return new ResourcePropertySource(resource);
	}

	// private void updateLazyLoadedAdapters() {
	// if (fSearchPageScoreComputer == null &&
	// SearchUtil.isSearchPlugInActivated())
	// createSearchPageScoreComputer();
	// }

	// private void createSearchPageScoreComputer() {
	// fSearchPageScoreComputer= new JavaSearchPageScoreComputer();
	// PROPERTIES= new Class[] {
	// IPropertySource.class,
	// IResource.class,
	// ISearchPageScoreComputer.class,
	// IWorkbenchAdapter.class,
	// IResourceLocator.class,
	// IPersistableElement.class,
	// IProject.class,
	// IContributorResourceAdapter.class,
	// ITaskListResourceAdapter.class,
	// IContainmentAdapter.class
	// };
	// }
}
