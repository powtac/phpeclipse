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
 * $Id: XMLDocument.java,v 1.2 2006-10-21 23:13:43 pombredanne Exp $
 */

package net.sourceforge.phpeclipse.xml.core.internal.model;

import net.sourceforge.phpeclipse.core.model.SourceReference;
import net.sourceforge.phpeclipse.xml.core.internal.parser.XMLParser;
import net.sourceforge.phpeclipse.xml.core.model.IXMLDocument;
import net.sourceforge.phpeclipse.xml.core.model.IXMLElement;
import net.sourceforge.phpeclipse.xml.core.parser.IProblemCollector;
import net.sourceforge.phpeclipse.xml.core.parser.IXMLParser;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;

/**
 * 
 */
public class XMLDocument extends SourceReference implements IXMLDocument,
		IDocumentListener {
	// Instance Variables ------------------------------------------------------

	private IXMLElement root;

	private String systemId;

	private Object dirtyLock = new Object();

	private boolean dirty = true;

	// Constructors ------------------------------------------------------------

	public XMLDocument(IDocument document, String systemId) {
		super(document, 0, document.getLength());
		this.systemId = systemId;
	}

	// IXMLDocument Implementation ---------------------------------------------

	/*
	 * @see IXMLDocument#getRoot()
	 */
	public IXMLElement getRoot() {
		return root;
	}

	/*
	 * @see net.sourceforge.phpeclipse.xml.core.model.IXMLDocument#getSystemId()
	 */
	public String getSystemId() {
		return systemId;
	}

	/*
	 * @see IStyleSheet#reconcile(IProblemCollector)
	 */
	public void reconcile(IProblemCollector problemCollector, IFile file) {
		synchronized (dirtyLock) {
			if (!dirty) {
				return;
			}
			dirty = false;
		}

		synchronized (this) {
			boolean doParse = false;
			root = null;
			if (file != null) {
				String filename = file.getFullPath().toString();
				int len = filename.length();
				if (len >= 4) {
					if ((filename.charAt(len - 1) != 'l' && filename
							.charAt(len - 1) != 'L')
							|| (filename.charAt(len - 2) != 'p' && filename
									.charAt(len - 2) != 'P')
							|| (filename.charAt(len - 3) != 't' && filename
									.charAt(len - 3) != 'T')
							|| (filename.charAt(len - 4) != '.')) {
						if ((filename.charAt(len - 1) != 'm' && filename
								.charAt(len - 1) != 'M')
								|| (filename.charAt(len - 2) != 't' && filename
										.charAt(len - 2) != 'T')
								|| (filename.charAt(len - 3) != 'h' && filename
										.charAt(len - 3) != 'H')
								|| (filename.charAt(len - 4) != '.')) {
							if (len >= 5) {
								if ((filename.charAt(len - 1) != 'l' && filename
										.charAt(len - 1) != 'L')
										|| (filename.charAt(len - 2) != 'm' && filename
												.charAt(len - 2) != 'M')
										|| (filename.charAt(len - 3) != 't' && filename
												.charAt(len - 3) != 'T')
										|| (filename.charAt(len - 4) != 'h' && filename
												.charAt(len - 4) != 'H')
										|| (filename.charAt(len - 5) != '.')) {
									doParse = true;
								}
							}
						}
					}
				} else {
					doParse = true;
				}
			}
			if (doParse) {
				IXMLParser parser = new XMLParser();
				parser.setProblemCollector(problemCollector);
				parser.setSource(getDocument());
				parser.setSystemId(systemId);
				IXMLDocument model = parser.parse();
				if (model != null) {
					root = model.getRoot();
				}
			}
		}
	}

	// IDocumentListener Implementation ----------------------------------------

	/*
	 * @see IDocumentListener#documentAboutToBeChanged(DocumentEvent)
	 */
	public void documentAboutToBeChanged(DocumentEvent event) {
		// do nothing
	}

	/*
	 * @see IDocumentListener#documentChanged(DocumentEvent)
	 */
	public void documentChanged(DocumentEvent event) {
		synchronized (dirtyLock) {
			dirty = true;
		}
	}

	// Public Methods ----------------------------------------------------------

	/**
	 * Sets the root element.
	 * 
	 * @param root
	 *            the root element to set
	 */
	public void setRoot(IXMLElement root) {
		this.root = root;
	}

}