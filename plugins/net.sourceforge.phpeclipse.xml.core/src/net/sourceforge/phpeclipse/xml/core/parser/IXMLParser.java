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
 * $Id: IXMLParser.java,v 1.1 2004-09-02 18:26:55 jsurfer Exp $
 */

package net.sourceforge.phpeclipse.xml.core.parser;

import net.sourceforge.phpeclipse.xml.core.model.IXMLDocument;

import org.eclipse.jface.text.IDocument;

/**
 * Interface for classes that implement parsing of XML files.
 */
public interface IXMLParser extends IProblemReporter {

	IXMLDocument parse();

	void setSource(IDocument source);

	void setSystemId(String systemId);
}
