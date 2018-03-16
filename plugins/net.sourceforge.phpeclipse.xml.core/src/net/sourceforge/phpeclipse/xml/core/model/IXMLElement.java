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
 * $Id: IXMLElement.java,v 1.1 2004-09-02 18:26:55 jsurfer Exp $
 */

package net.sourceforge.phpeclipse.xml.core.model;

import net.sourceforge.phpeclipse.core.model.ISourceReference;

/**
 * Basic model element
 */
public interface IXMLElement extends ISourceReference {

	IXMLElement[] getChildren();

	String getLocalName();

	String getNamespaceURI();

	String getPrefix();

	IXMLElement getParent();

}
