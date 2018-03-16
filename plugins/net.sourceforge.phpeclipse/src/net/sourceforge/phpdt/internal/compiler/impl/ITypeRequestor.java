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
package net.sourceforge.phpdt.internal.compiler.impl;

import net.sourceforge.phpdt.internal.compiler.env.IBinaryType;
import net.sourceforge.phpdt.internal.compiler.env.ICompilationUnit;
import net.sourceforge.phpdt.internal.compiler.env.ISourceType;
import net.sourceforge.phpdt.internal.compiler.lookup.PackageBinding;

public interface ITypeRequestor {

	/**
	 * Accept the resolved binary form for the requested type.
	 */
	void accept(IBinaryType binaryType, PackageBinding packageBinding);

	/**
	 * Accept the requested type's compilation unit.
	 */
	void accept(ICompilationUnit unit);

	/**
	 * Accept the unresolved source forms for the requested type. Note that the
	 * multiple source forms can be answered, in case the target compilation
	 * unit contains multiple types. The first one is then guaranteed to be the
	 * one corresponding to the requested type.
	 */
	void accept(ISourceType[] sourceType, PackageBinding packageBinding);
}
