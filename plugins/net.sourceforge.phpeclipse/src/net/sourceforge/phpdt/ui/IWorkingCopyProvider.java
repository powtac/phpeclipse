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
package net.sourceforge.phpdt.ui;

/**
 * Interface used for Java element content providers to indicate that the
 * content provider can return working copy elements for members below
 * compilation units.
 * 
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @see net.sourceforge.phpdt.ui.StandardJavaElementContentProvider
 * @see net.sourceforge.phpdt.core.IWorkingCopy
 * 
 * @since 2.0
 */
public interface IWorkingCopyProvider {

	/**
	 * Returns <code>true</code> if the content provider returns working copy
	 * elements; otherwise <code>false</code> is returned.
	 * 
	 * @return whether working copy elements are provided.
	 */
	public boolean providesWorkingCopies();
}
