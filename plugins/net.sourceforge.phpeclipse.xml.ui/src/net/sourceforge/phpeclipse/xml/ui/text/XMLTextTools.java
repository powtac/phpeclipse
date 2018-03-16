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
 * $Id: XMLTextTools.java,v 1.4 2006-10-21 23:14:13 pombredanne Exp $
 */

package net.sourceforge.phpeclipse.xml.ui.text;

import java.util.Map;

import net.sourceforge.phpeclipse.ui.text.AbstractTextTools;
import net.sourceforge.phpeclipse.xml.ui.internal.text.DeclScanner;
import net.sourceforge.phpeclipse.xml.ui.internal.text.PHPXMLPartitionScanner;
import net.sourceforge.phpeclipse.xml.ui.internal.text.SingleTokenScanner;
import net.sourceforge.phpeclipse.xml.ui.internal.text.TextScanner;
import net.sourceforge.phpeclipse.xml.ui.internal.text.XMLCDATAScanner;
import net.sourceforge.phpeclipse.xml.ui.internal.text.XMLPartitionScanner;
import net.sourceforge.phpeclipse.xml.ui.internal.text.XMLTagScanner;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.DefaultPartitioner;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.RuleBasedScanner;

/**
 * 
 * 
 * @author Igor Malinin
 */
public class XMLTextTools extends AbstractTextTools {
	/** Text Attributes for XML editors */
	public static final String[] TOKENS = { IXMLSyntaxConstants.XML_DEFAULT,
			IXMLSyntaxConstants.XML_TAG, IXMLSyntaxConstants.XML_ATT_NAME,
			IXMLSyntaxConstants.XML_ATT_VALUE, IXMLSyntaxConstants.XML_ENTITY,
			IXMLSyntaxConstants.XML_PI, IXMLSyntaxConstants.XML_CDATA,
			IXMLSyntaxConstants.XML_COMMENT, IXMLSyntaxConstants.XML_SMARTY,
			IXMLSyntaxConstants.XML_DECL, };

	/** Content types for XML editors */
	public static final String[] TYPES = { XMLPartitionScanner.XML_PI,
			XMLPartitionScanner.XML_COMMENT, XMLPartitionScanner.XML_DECL,
			XMLPartitionScanner.XML_TAG, XMLPartitionScanner.XML_ATTRIBUTE,
			XMLPartitionScanner.XML_CDATA, XMLPartitionScanner.DTD_INTERNAL,
			XMLPartitionScanner.DTD_INTERNAL_PI,
			XMLPartitionScanner.DTD_INTERNAL_COMMENT,
			XMLPartitionScanner.DTD_INTERNAL_DECL, };

	/** The XML partitions scanner */
	private XMLPartitionScanner xmlPartitionScanner;

	private PHPXMLPartitionScanner phpXMLPartitionScanner;

	/** The XML text scanner */
	private TextScanner xmlTextScanner;

	/** The DTD text scanner */
	private TextScanner dtdTextScanner;

	/** The XML tags scanner */
	private XMLTagScanner xmlTagScanner;

	/** The XML attributes scanner */
	private TextScanner xmlAttributeScanner;

	/** The XML CDATA sections scanner */
	private XMLCDATAScanner xmlCDATAScanner;

	/** The XML processing instructions scanner */
	private SingleTokenScanner xmlPIScanner;

	/** The XML comments scanner */
	private SingleTokenScanner xmlCommentScanner;

	/** The XML declarations scanner */
	private DeclScanner xmlDeclScanner;

	public XMLTextTools(IPreferenceStore store) {
		this(store, TOKENS);
	}

	/**
	 * Creates a new XML text tools collection.
	 */
	public XMLTextTools(IPreferenceStore store, String[] strTokens) {
		super(store, strTokens);

		xmlPartitionScanner = new XMLPartitionScanner(false);
		phpXMLPartitionScanner = new PHPXMLPartitionScanner(false);
		Map tokens = getTokens();

		xmlTextScanner = new TextScanner(tokens, '&',
				IXMLSyntaxConstants.XML_DEFAULT);

		dtdTextScanner = new TextScanner(tokens, '%',
				IXMLSyntaxConstants.XML_DEFAULT);

		xmlPIScanner = new SingleTokenScanner(tokens,
				IXMLSyntaxConstants.XML_PI);

		xmlCommentScanner = new SingleTokenScanner(tokens,
				IXMLSyntaxConstants.XML_COMMENT);

		xmlDeclScanner = new DeclScanner(tokens);

		xmlTagScanner = new XMLTagScanner(tokens);

		xmlAttributeScanner = new TextScanner(tokens, '&',
				IXMLSyntaxConstants.XML_ATT_VALUE);

		xmlCDATAScanner = new XMLCDATAScanner(tokens);
	}

	/**
	 * 
	 */
	public IDocumentPartitioner createXMLPartitioner() {
		return new DefaultPartitioner(xmlPartitionScanner, TYPES);
	}

	public IDocumentPartitioner createPHPXMLPartitioner() {
		return new DefaultPartitioner(phpXMLPartitionScanner, TYPES);
	}

	/**
	 * 
	 */
	// public IPartitionTokenScanner getXMLPartitionScanner() {
	// return xmlPartitionScanner;
	// }
	/**
	 * Returns a scanner which is configured to scan XML text.
	 * 
	 * @return an XML text scanner
	 */
	public RuleBasedScanner getXMLTextScanner() {
		return xmlTextScanner;
	}

	/**
	 * Returns a scanner which is configured to scan DTD text.
	 * 
	 * @return an DTD text scanner
	 */
	public RuleBasedScanner getDTDTextScanner() {
		return dtdTextScanner;
	}

	/**
	 * Returns a scanner which is configured to scan XML tags.
	 * 
	 * @return an XML tag scanner
	 */
	public RuleBasedScanner getXMLTagScanner() {
		return xmlTagScanner;
	}

	/**
	 * Returns a scanner which is configured to scan XML tag attributes.
	 * 
	 * @return an XML tag attribute scanner
	 */
	public RuleBasedScanner getXMLAttributeScanner() {
		return xmlAttributeScanner;
	}

	/**
	 * Returns a scanner which is configured to scan XML CDATA sections.
	 * 
	 * @return an XML CDATA section scanner
	 */
	public ITokenScanner getXMLCDATAScanner() {
		return xmlCDATAScanner;
	}

	/**
	 * Returns a scanner which is configured to scan XML processing
	 * instructions.
	 * 
	 * @return an XML processing instruction scanner
	 */
	public RuleBasedScanner getXMLPIScanner() {
		return xmlPIScanner;
	}

	/**
	 * Returns a scanner which is configured to scan XML comments.
	 * 
	 * @return an XML comment scanner
	 */
	public RuleBasedScanner getXMLCommentScanner() {
		return xmlCommentScanner;
	}

	/**
	 * Returns a scanner which is configured to scan XML declarations.
	 * 
	 * @return an XML declaration scanner
	 */
	public RuleBasedScanner getXMLDeclScanner() {
		return xmlDeclScanner;
	}
}
