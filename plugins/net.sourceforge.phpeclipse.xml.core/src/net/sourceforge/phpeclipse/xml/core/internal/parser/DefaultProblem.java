/*
 * Copyright (c) 2003-2004 Christopher Lenz and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Christopher Lenz - initial API and implementation
 * 
 * $Id: DefaultProblem.java,v 1.2 2006-10-21 23:13:43 pombredanne Exp $
 */

package net.sourceforge.phpeclipse.xml.core.internal.parser;

import net.sourceforge.phpeclipse.xml.core.parser.IProblem;

public class DefaultProblem implements IProblem {

	// Instance Variables ------------------------------------------------------

	private String message;

	private int sourceStart;

	private int sourceEnd;

	private int sourceLineNumber;

	private boolean error;

	// Constructors ------------------------------------------------------------

	public DefaultProblem(String message, int sourceStart, int sourceEnd,
			int sourceLineNumber, boolean error) {
		this.message = message;
		this.sourceStart = sourceStart;
		this.sourceEnd = sourceEnd;
		this.sourceLineNumber = sourceLineNumber;
		this.error = error;
	}

	// IProblem Implementation -------------------------------------------------

	/*
	 * @see IProblem#getMessage()
	 */
	public String getMessage() {
		return message;
	}

	/*
	 * @see IProblem#getSourceStart()
	 */
	public int getSourceStart() {
		return sourceStart;
	}

	/*
	 * @see IProblem#getSourceEnd()
	 */
	public int getSourceEnd() {
		return sourceEnd;
	}

	/*
	 * @see IProblem#getSourceLineNumber()
	 */
	public int getSourceLineNumber() {
		return sourceLineNumber;
	}

	/*
	 * @see IProblem#isError()
	 */
	public boolean isError() {
		return error;
	}

	/*
	 * @see IProblem#isWarning()
	 */
	public boolean isWarning() {
		return !error;
	}

}
