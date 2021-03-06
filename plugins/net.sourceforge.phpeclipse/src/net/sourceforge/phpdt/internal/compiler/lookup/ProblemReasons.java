/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package net.sourceforge.phpdt.internal.compiler.lookup;

public interface ProblemReasons {
	final int NoError = 0;

	final int NotFound = 1;

	final int NotVisible = 2;

	final int Ambiguous = 3;

	final int InternalNameProvided = 4; // used if an internal name is used in
										// source

	final int InheritedNameHidesEnclosingName = 5;

	final int NonStaticReferenceInConstructorInvocation = 6;

	final int NonStaticReferenceInStaticContext = 7;

	final int ReceiverTypeNotVisible = 8;
}
