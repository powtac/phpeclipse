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
 * $Id: IProblemReporter.java,v 1.2 2006-10-21 23:13:43 pombredanne Exp $
 */

package net.sourceforge.phpeclipse.xml.core.parser;

/**
 * Basic interface for classes that report errors to a problem collector.
 */
public interface IProblemReporter {

	/**
	 * Sets the problem collector that should be used by the reporter.
	 * 
	 * @param problemCollector
	 *            the problem collector to use
	 */
	void setProblemCollector(IProblemCollector problemCollector);

}
