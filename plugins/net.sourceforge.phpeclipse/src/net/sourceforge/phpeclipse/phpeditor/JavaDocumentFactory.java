/**********************************************************************
 Copyright (c) 2000, 2003 IBM Corp. and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html

 Contributors:
 IBM Corporation - Initial implementation
 **********************************************************************/
package net.sourceforge.phpeclipse.phpeditor;

import org.eclipse.core.filebuffers.IDocumentFactory;
import org.eclipse.jface.text.IDocument;

/**
 * The document factory for JDT UI,
 */
public class JavaDocumentFactory implements IDocumentFactory {

	public JavaDocumentFactory() {
	}

	/*
	 * @see org.eclipse.core.filebuffers.IDocumentFactory#createDocument()
	 */
	public IDocument createDocument() {
		return new PartiallySynchronizedDocument();
	}
}
