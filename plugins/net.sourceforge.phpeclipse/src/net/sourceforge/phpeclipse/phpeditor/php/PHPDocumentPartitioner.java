/**********************************************************************
 Copyright (c) 2002  Widespace, OU  and others.
 All rights reserved.   This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://solareclipse.sourceforge.net/legal/cpl-v10.html

 Contributors:
 Igor Malinin - initial contribution

 $Id: PHPDocumentPartitioner.java,v 1.6 2006-10-21 23:18:33 pombredanne Exp $
 **********************************************************************/
package net.sourceforge.phpeclipse.phpeditor.php;

import net.sourceforge.phpeclipse.PHPeclipsePlugin;
import net.sourceforge.phpeclipse.ui.text.rules.FlatNode;
import net.sourceforge.phpeclipse.ui.text.rules.MultiViewPartitioner;
import net.sourceforge.phpeclipse.ui.text.rules.ViewNode;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;

/**
 * 
 * 
 * @author Igor Malinin
 */
public class PHPDocumentPartitioner extends MultiViewPartitioner {
	public static final String PHP_TEMPLATE_DATA = "__php_template_data";

	public static final String PHP_SCRIPT_CODE = "__php_script_code";

	public static final String[] LEGAL_TYPES = { PHP_TEMPLATE_DATA,
			PHP_SCRIPT_CODE };

	public PHPDocumentPartitioner(IPartitionTokenScanner scanner) {
		super(scanner);
	}

	protected FlatNode createNode(String type, int offset, int length) {
		if (type.equals(PHPPartitionScanner.PHP_SCRIPTING_AREA)) {
			if (DEBUG) {
				Assert.isTrue(offset >= 0);
			}
			ViewNode node = new ViewNode(type);
			node.offset = offset;
			node.length = length;
			return node;
		}

		return super.createNode(type, offset, length);
	}

	/*
	 * @see net.sf.solareclipse.text.rules.DocumentViewPartitioner#createPartitioner(String)
	 */
	protected IDocumentPartitioner createPartitioner(String contentType) {
		if (contentType == null) {
			// return JavaTextTools.createHTMLPartitioner();
			return PHPeclipsePlugin.getDefault().getJavaTextTools()
					.getXMLTextTools().createPHPXMLPartitioner();
		}

		if (contentType.equals(PHPPartitionScanner.PHP_SCRIPTING_AREA)) {
			return PHPeclipsePlugin.getDefault().getJavaTextTools()
					.createPHPPartitioner();
		}
		return null;
	}

	/*
	 * @see net.sf.solareclipse.text.rules.DocumentViewPartitioner#getContentType(String,
	 *      String)
	 */
	protected String getContentType(String parent, String view) {
		if (parent == null) {
			if (view == IDocument.DEFAULT_CONTENT_TYPE) {
				return PHP_TEMPLATE_DATA;
			}
		} else {
			if (view == IDocument.DEFAULT_CONTENT_TYPE) {
				return PHP_SCRIPT_CODE;
			}
		}

		return super.getContentType(parent, view);
	}

	public String[] getLegalContentTypes() {
		return LEGAL_TYPES;
	}
}