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
 * $Id: XMLElement.java,v 1.2 2006-10-21 23:13:43 pombredanne Exp $
 */

package net.sourceforge.phpeclipse.xml.core.internal.model;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.phpeclipse.core.model.SourceReference;
import net.sourceforge.phpeclipse.xml.core.model.IXMLElement;

import org.eclipse.jface.text.IDocument;

/**
 * 
 */
public class XMLElement extends SourceReference implements IXMLElement {

	// Instance Variables ------------------------------------------------------

	private List children;

	private String localName;

	private String namespaceURI;

	private String prefix;

	private IXMLElement parent;

	// Constructors ------------------------------------------------------------

	public XMLElement(IDocument document) {
		super(document);
	}

	public XMLElement(IDocument document, int offset) {
		super(document, offset);
	}

	public XMLElement(IDocument document, int offset, int length) {
		super(document, offset, length);
	}

	// IXMLElement Implementation
	// -------------------------------------------------

	/*
	 * @see IXMLElement#getChildren()
	 */
	public IXMLElement[] getChildren() {
		if (children != null) {
			return (IXMLElement[]) children.toArray(new IXMLElement[children
					.size()]);
		}
		return new IXMLElement[0];
	}

	/*
	 * @see IXMLElement#getLocalName()
	 */
	public String getLocalName() {
		return localName;
	}

	/*
	 * @see IXMLElement#getNamespaceURI()
	 */
	public String getNamespaceURI() {
		return namespaceURI;
	}

	/*
	 * @see IXMLElement#getPrefix()
	 */
	public String getPrefix() {
		return prefix;
	}

	/*
	 * @see IXMLElement#getParent()
	 */
	public IXMLElement getParent() {
		return parent;
	}

	// Public Methods ----------------------------------------------------------

	public void addChild(IXMLElement child) {
		if (children == null) {
			children = new ArrayList();
		}
		children.add(child);
	}

	public void setLocalName(String localName) {
		this.localName = localName;
	}

	public void setNamespaceURI(String namespaceURI) {
		this.namespaceURI = namespaceURI;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setParent(IXMLElement parent) {
		this.parent = parent;
	}

}
