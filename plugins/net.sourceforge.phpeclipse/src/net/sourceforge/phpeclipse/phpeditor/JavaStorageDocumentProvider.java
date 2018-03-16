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

import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.StorageDocumentProvider;

/**
 * @since 3.0
 */
public class JavaStorageDocumentProvider extends StorageDocumentProvider {

	public JavaStorageDocumentProvider() {
		super();
	}

	/*
	 * @see org.eclipse.ui.editors.text.StorageDocumentProvider#setupDocument(java.lang.Object,
	 *      org.eclipse.jface.text.IDocument)
	 */
	protected void setupDocument(Object element, IDocument document) {

		if (document != null) {
			JavaTextTools tools = PHPeclipsePlugin.getDefault()
					.getJavaTextTools();
			tools.setupJavaDocumentPartitioner(document,
					IPHPPartitions.PHP_PARTITIONING);

			// tools.setupJavaDocumentPartitioner(document,
			// IDocument.DEFAULT_CONTENT_TYPE, element); //IPHPPartitions.HTML,
			// element);
		}
	}
}
