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
 * $Id: IProblemCollector.java,v 1.2 2006-10-21 23:13:43 pombredanne Exp $
 */

package net.sourceforge.phpeclipse.xml.core.parser;

/**
 * 
 */
public interface IProblemCollector {

	/**
	 * Notification of an error or warning.
	 * 
	 * @param problem
	 *            the discovered problem
	 */
	void addProblem(IProblem problem);

}
