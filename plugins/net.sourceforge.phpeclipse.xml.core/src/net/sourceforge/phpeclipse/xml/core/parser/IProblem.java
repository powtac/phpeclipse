/*
 * Copyright (c) 2004 Christopher Lenz and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Christopher Lenz - initial API
 * 
 * $Id: IProblem.java,v 1.2 2006-10-21 23:13:43 pombredanne Exp $
 */

package net.sourceforge.phpeclipse.xml.core.parser;

/**
 * 
 */
public interface IProblem {

	/**
	 * Returns a localized, human-readable message string which describes the
	 * problem.
	 * 
	 * @return the problem message
	 */
	String getMessage();

	/**
	 * Returns the start position of the problem (inclusive).
	 * 
	 * @return the start position of the problem, or -1 if unknown
	 */
	int getSourceStart();

	/**
	 * Returns the end position of the problem (inclusive).
	 * 
	 * @return the end position of the problem), or -1 if unknown
	 */
	int getSourceEnd();

	/**
	 * Returns the line number of the source where the problem begins.
	 * 
	 * @return the line number of the source where the problem begins
	 */
	int getSourceLineNumber();

	/**
	 * Returns whether the problem is an error.
	 * 
	 * @return <code>true</code> if the problem is an error,
	 *         <code>false</code> otherwise
	 */
	boolean isError();

	/**
	 * Returns whether the problem is a warning.
	 * 
	 * @return <code>true</code> if the problem is a warning,
	 *         <code>false</code> otherwise
	 */
	boolean isWarning();

}
