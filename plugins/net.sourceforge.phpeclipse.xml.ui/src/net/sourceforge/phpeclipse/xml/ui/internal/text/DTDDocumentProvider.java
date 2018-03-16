/*
 * Copyright (c) 2002-2004 Widespace, OU and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Igor Malinin - initial contribution
 *
 * $Id: DTDDocumentProvider.java,v 1.3 2006-10-21 23:14:13 pombredanne Exp $
 */

package net.sourceforge.phpeclipse.xml.ui.internal.text;

import net.sourceforge.phpeclipse.xml.ui.XMLPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;

/**
 * DTD document provider.
 * 
 * @author Igor Malinin
 */
public class DTDDocumentProvider extends AbstractDocumentProvider {
	/*
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#createDocument(Object)
	 */
	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document = super.createDocument(element);
		if (document != null) {
			IDocumentPartitioner partitioner = XMLPlugin.getDefault()
					.getDTDTextTools().createDTDPartitioner();

			if (partitioner != null) {
				partitioner.connect(document);
				document.setDocumentPartitioner(partitioner);
			}
		}

		return document;
	}
}
