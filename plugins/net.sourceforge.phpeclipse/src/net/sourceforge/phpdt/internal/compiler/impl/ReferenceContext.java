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

import net.sourceforge.phpdt.internal.compiler.CompilationResult;

/*
 * Implementors are valid compilation contexts from which we can escape in case
 * of error: For example: method, type or compilation unit.
 */
public interface ReferenceContext {
	void abort(int abortLevel);

	CompilationResult compilationResult();

	void tagAsHavingErrors();

	boolean hasErrors();
}
