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

import net.sourceforge.phpdt.internal.ui.text.IPHPPartitions;
import net.sourceforge.phpdt.ui.text.JavaTextTools;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.IDocument;

/**
 * The document setup participant for PHPDT.
 */
public class HTMLDocumentSetupParticipant implements IDocumentSetupParticipant {

	public HTMLDocumentSetupParticipant() {
	}

	/*
	 * @see org.eclipse.core.filebuffers.IDocumentSetupParticipant#setup(org.eclipse.jface.text.IDocument)
	 */
	public void setup(IDocument document) {
		JavaTextTools tools = PHPeclipsePlugin.getDefault().getJavaTextTools();
		tools.setupHTMLDocumentPartitioner(document,
				IPHPPartitions.PHP_PARTITIONING, null); // IPHPPartitions.PHP_PARTITIONING,
														// null);
	}
}
