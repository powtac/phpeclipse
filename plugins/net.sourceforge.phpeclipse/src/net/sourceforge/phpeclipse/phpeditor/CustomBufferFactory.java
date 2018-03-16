/**********************************************************************
 Copyright (c) 2000, 2003 IBM Corp. and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html

 Contributors:
 IBM Corporation - Initial implementation
 **********************************************************************/

package net.sourceforge.phpeclipse.phpeditor;

import net.sourceforge.phpdt.core.IBuffer;
import net.sourceforge.phpdt.core.IBufferFactory;
import net.sourceforge.phpdt.core.ICompilationUnit;
import net.sourceforge.phpdt.core.IOpenable;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

/**
 * Creates <code>IBuffer</code>s based on documents.
 * 
 * @deprecated since 3.0 no longer used
 */
public class CustomBufferFactory implements IBufferFactory {

	/*
	 * @see net.sourceforge.phpdt.core.IBufferFactory#createBuffer(net.sourceforge.phpdt.core.IOpenable)
	 */
	public IBuffer createBuffer(IOpenable owner) {
		if (owner instanceof ICompilationUnit) {
			ICompilationUnit unit = (ICompilationUnit) owner;
			ICompilationUnit original = unit.getPrimary();
			IResource resource = original.getResource();
			if (resource instanceof IFile) {
				return new DocumentAdapter(unit, (IFile) resource);
			}

		}
		return DocumentAdapter.NULL;
	}
}