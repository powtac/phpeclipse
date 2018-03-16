/*
 * Copyright (c) 2004 Christopher Lenz and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Christopher Lenz - initial API
 * 
 * $Id: IXMLDocument.java,v 1.1 2004-09-02 18:26:55 jsurfer Exp $
 */

package net.sourceforge.phpeclipse.xml.core.model;

import net.sourceforge.phpeclipse.core.model.ISourceReference;
import net.sourceforge.phpeclipse.xml.core.parser.IProblemCollector;

import org.eclipse.core.resources.IFile;

/**
 * 
 */
public interface IXMLDocument extends ISourceReference {

	/**
	 * Returns the root element of the document.
	 * 
	 * @return the document element
	 */
	IXMLElement getRoot();

	/**
	 * Returns the system ID of the document.
	 * 
	 * @return the system ID
	 */
	String getSystemId();

	void reconcile(IProblemCollector collector, IFile file);

}
