/*
 * Copyright (c) 2004 Christopher Lenz and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Christopher Lenz - initial implementation
 * 
 * $Id: XMLDocumentSetupParticipant.java,v 1.3 2006-10-21 23:14:14 pombredanne Exp $
 */

package net.sourceforge.phpeclipse.xml.ui.internal.editor;

import net.sourceforge.phpeclipse.xml.ui.XMLPlugin;
import net.sourceforge.phpeclipse.xml.ui.text.XMLTextTools;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;

/**
 * Document setup participant that sets up the CSS specific partitioning.
 */
public class XMLDocumentSetupParticipant implements IDocumentSetupParticipant {

	/*
	 * @see IDocumentSetupParticipant#setup(IDocument)
	 */
	public void setup(IDocument document) {
		if (document != null) {
			XMLTextTools tools = XMLPlugin.getDefault().getXMLTextTools();
			IDocumentPartitioner partitioner = tools.createXMLPartitioner();
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
	}

}
