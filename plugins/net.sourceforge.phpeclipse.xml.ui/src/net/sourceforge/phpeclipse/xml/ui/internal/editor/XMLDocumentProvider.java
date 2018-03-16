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
 * $Id: XMLDocumentProvider.java,v 1.3 2006-10-21 23:14:14 pombredanne Exp $
 */

package net.sourceforge.phpeclipse.xml.ui.internal.editor;

import java.net.MalformedURLException;

import net.sourceforge.phpeclipse.xml.core.internal.model.XMLDocument;
import net.sourceforge.phpeclipse.xml.core.model.IXMLDocument;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;

/**
 * Document provider for XML files.
 * 
 * TODO Merge the encoding detection support from I18NDocumentProvider and
 * AbstractDocumentProvider into this class
 * 
 * TODO This class currently doubles as a model manager which will need to be
 * moved into core at some point, and would make this class pretty much useless
 */
public class XMLDocumentProvider extends TextFileDocumentProvider {

	// Inner Classes -----------------------------------------------------------

	private class XMLFileInfo extends FileInfo {
		IXMLDocument xmlDocument;
	}

	// TestFileDocumentProvider Implementation ---------------------------------

	/*
	 * @see TextFileDocumentProvider#createEmptyFileInfo()
	 */
	protected FileInfo createEmptyFileInfo() {
		return new XMLFileInfo();
	}

	/*
	 * @see TextFileDocumentProvider#createFileInfo(Object)
	 */
	protected FileInfo createFileInfo(Object element) throws CoreException {
		FileInfo fileInfo = super.createFileInfo(element);
		if (!(fileInfo instanceof XMLFileInfo)) {
			return null;
		}

		IDocument document = fileInfo.fTextFileBuffer.getDocument();

		String systemId = null;
		try {
			systemId = getSystemFile(fileInfo).toURL().toString();
		} catch (MalformedURLException e) {
		}

		IXMLDocument xmlDocument = createModel(document, systemId);
		if (xmlDocument instanceof IDocumentListener) {
			document.addDocumentListener((IDocumentListener) xmlDocument);
		}

		XMLFileInfo xmlFileInfo = (XMLFileInfo) fileInfo;
		xmlFileInfo.xmlDocument = xmlDocument;

		return xmlFileInfo;
	}

	/*
	 * @see TextFileDocumentProvider#disposeFileInfo(Object,
	 *      TextFileDocumentProvider.FileInfo)
	 */
	protected void disposeFileInfo(Object element, FileInfo info) {
		if (info instanceof XMLFileInfo) {
			IDocument document = getDocument(element);
			if (document != null) {
				IXMLDocument xmlDocument = ((XMLFileInfo) info).xmlDocument;
				if (xmlDocument instanceof IDocumentListener) {
					document
							.removeDocumentListener((IDocumentListener) xmlDocument);
				}
			}
		}

		super.disposeFileInfo(element, info);
	}

	// Public Methods ----------------------------------------------------------

	/**
	 * Creates the XML document model object corresponding to the specified
	 * document.
	 * 
	 * @param document
	 *            the document to parse
	 * @param systemId
	 *            the system ID of the document
	 * @return the document model object
	 */
	public IXMLDocument createModel(IDocument document, String systemId) {
		return new XMLDocument(document, systemId);
	}

	/**
	 * Returns the XML document model associated with the specified element.
	 * 
	 * @param element
	 *            the element
	 * @return the document model associated with the element
	 */
	public IXMLDocument getModel(Object element) {
		FileInfo info = getFileInfo(element);
		if (info instanceof XMLFileInfo) {
			XMLFileInfo xmlFileInfo = (XMLFileInfo) info;
			return xmlFileInfo.xmlDocument;
		}

		return null;
	}
}
