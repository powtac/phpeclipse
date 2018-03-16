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
 * $Id: XMLConfiguration.java,v 1.2 2006-10-21 23:14:13 pombredanne Exp $
 */

package net.sourceforge.phpeclipse.xml.ui.internal.text;

import net.sourceforge.phpeclipse.ui.templates.template.BasicCompletionProcessor;
import net.sourceforge.phpeclipse.ui.text.TextDoubleClickStrategy;
import net.sourceforge.phpeclipse.xml.ui.text.XMLTextTools;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * XML editor configuration.
 * 
 * @author Igor Malinin
 */
public class XMLConfiguration extends TextSourceViewerConfiguration {
	protected XMLTextTools xmlTextTools;

	private ITextDoubleClickStrategy dcsDefault;

	private ITextDoubleClickStrategy dcsSimple;

	private ITextDoubleClickStrategy dcsTag;

	private ITextDoubleClickStrategy dcsAttValue;

	/** The associated editor. */
	private ITextEditor editor;

	public XMLConfiguration(XMLTextTools tools) {
		this(tools, null);
	}

	public XMLConfiguration(XMLTextTools tools, ITextEditor editor) {
		xmlTextTools = tools;
		this.editor = editor;
		dcsDefault = new TextDoubleClickStrategy();
		dcsSimple = new SimpleDoubleClickStrategy();
		dcsTag = new TagDoubleClickStrategy();
		dcsAttValue = new AttValueDoubleClickStrategy();
	}

	/*
	 * @see SourceViewerConfiguration#getAnnotationHover(ISourceViewer)
	 */
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return new XMLAnnotationHover();
	}

	/*
	 * @see SourceViewerConfiguration#getTextHover(ISourceViewer, String)
	 */
	public ITextHover getTextHover(ISourceViewer sourceViewer,
			String contentType) {
		if (editor != null) {
			IDocumentProvider provider = editor.getDocumentProvider();
			IEditorInput input = editor.getEditorInput();
			IAnnotationModel model = provider.getAnnotationModel(input);
			return new XMLTextHover(model);
		}

		return super.getTextHover(sourceViewer, contentType);
	}

	/*
	 * @see SourceViewerConfiguration#getDoubleClickStrategy(ISourceViewer,
	 *      String)
	 */
	public ITextDoubleClickStrategy getDoubleClickStrategy(
			ISourceViewer sourceViewer, String contentType) {
		if (XMLPartitionScanner.XML_COMMENT.equals(contentType)) {
			return dcsSimple;
		}

		if (XMLPartitionScanner.XML_PI.equals(contentType)) {
			return dcsSimple;
		}

		if (XMLPartitionScanner.XML_TAG.equals(contentType)) {
			return dcsTag;
		}

		if (XMLPartitionScanner.XML_ATTRIBUTE.equals(contentType)) {
			return dcsAttValue;
		}

		if (XMLPartitionScanner.XML_CDATA.equals(contentType)) {
			return dcsSimple;
		}

		if (contentType.startsWith(XMLPartitionScanner.DTD_INTERNAL)) {
			return dcsSimple;
		}

		return dcsDefault;
	}

	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getConfiguredContentTypes(ISourceViewer)
	 */
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] { IDocument.DEFAULT_CONTENT_TYPE,
				XMLPartitionScanner.XML_PI, XMLPartitionScanner.XML_COMMENT,
				XMLPartitionScanner.XML_DECL, XMLPartitionScanner.XML_TAG,
				XMLPartitionScanner.XML_ATTRIBUTE,
				XMLPartitionScanner.XML_CDATA,
				XMLPartitionScanner.DTD_INTERNAL,
				XMLPartitionScanner.DTD_INTERNAL_PI,
				XMLPartitionScanner.DTD_INTERNAL_COMMENT,
				XMLPartitionScanner.DTD_INTERNAL_DECL, };
	}

	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getPresentationReconciler(ISourceViewer)
	 */
	public IPresentationReconciler getPresentationReconciler(
			ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr;

		dr = new DefaultDamagerRepairer(xmlTextTools.getXMLTextScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		dr = new DefaultDamagerRepairer(xmlTextTools.getDTDTextScanner());
		reconciler.setDamager(dr, XMLPartitionScanner.DTD_INTERNAL);
		reconciler.setRepairer(dr, XMLPartitionScanner.DTD_INTERNAL);

		dr = new DefaultDamagerRepairer(xmlTextTools.getXMLPIScanner());

		reconciler.setDamager(dr, XMLPartitionScanner.XML_PI);
		reconciler.setRepairer(dr, XMLPartitionScanner.XML_PI);
		reconciler.setDamager(dr, XMLPartitionScanner.DTD_INTERNAL_PI);
		reconciler.setRepairer(dr, XMLPartitionScanner.DTD_INTERNAL_PI);

		dr = new DefaultDamagerRepairer(xmlTextTools.getXMLCommentScanner());

		reconciler.setDamager(dr, XMLPartitionScanner.XML_COMMENT);
		reconciler.setRepairer(dr, XMLPartitionScanner.XML_COMMENT);
		reconciler.setDamager(dr, XMLPartitionScanner.DTD_INTERNAL_COMMENT);
		reconciler.setRepairer(dr, XMLPartitionScanner.DTD_INTERNAL_COMMENT);

		dr = new DefaultDamagerRepairer(xmlTextTools.getXMLDeclScanner());

		reconciler.setDamager(dr, XMLPartitionScanner.XML_DECL);
		reconciler.setRepairer(dr, XMLPartitionScanner.XML_DECL);
		reconciler.setDamager(dr, XMLPartitionScanner.DTD_INTERNAL_DECL);
		reconciler.setRepairer(dr, XMLPartitionScanner.DTD_INTERNAL_DECL);

		dr = new DefaultDamagerRepairer(xmlTextTools.getXMLTagScanner());

		reconciler.setDamager(dr, XMLPartitionScanner.XML_TAG);
		reconciler.setRepairer(dr, XMLPartitionScanner.XML_TAG);

		dr = new DefaultDamagerRepairer(xmlTextTools.getXMLAttributeScanner());

		reconciler.setDamager(dr, XMLPartitionScanner.XML_ATTRIBUTE);
		reconciler.setRepairer(dr, XMLPartitionScanner.XML_ATTRIBUTE);

		dr = new DefaultDamagerRepairer(xmlTextTools.getXMLCDATAScanner());

		reconciler.setDamager(dr, XMLPartitionScanner.XML_CDATA);
		reconciler.setRepairer(dr, XMLPartitionScanner.XML_CDATA);

		return reconciler;
	}

	/*
	 * @see SourceViewerConfiguration#getReconciler(ISourceViewer)
	 */
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		if ((editor != null) && editor.isEditable()) {
			MonoReconciler reconciler = new MonoReconciler(
					new XMLReconcilingStrategy(editor), false);
			reconciler.setProgressMonitor(new NullProgressMonitor());
			reconciler.setDelay(500);
			return reconciler;
		}

		return null;
	}

	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant assistant = new ContentAssistant();
		assistant
				.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

		IContentAssistProcessor processor = new BasicCompletionProcessor();
		assistant.setContentAssistProcessor(processor,
				IDocument.DEFAULT_CONTENT_TYPE);
		assistant.setContentAssistProcessor(processor,
				XMLPartitionScanner.XML_TAG);
		assistant.setContentAssistProcessor(processor,
				XMLPartitionScanner.XML_PI);
		assistant.setContentAssistProcessor(processor,
				XMLPartitionScanner.XML_COMMENT);
		assistant.setContentAssistProcessor(processor,
				XMLPartitionScanner.XML_DECL);
		assistant.setContentAssistProcessor(processor,
				XMLPartitionScanner.XML_TAG);
		assistant.setContentAssistProcessor(processor,
				XMLPartitionScanner.XML_ATTRIBUTE);
		assistant.setContentAssistProcessor(processor,
				XMLPartitionScanner.XML_CDATA);
		assistant.setContentAssistProcessor(processor,
				XMLPartitionScanner.DTD_INTERNAL);
		assistant.setContentAssistProcessor(processor,
				XMLPartitionScanner.DTD_INTERNAL_PI);
		assistant.setContentAssistProcessor(processor,
				XMLPartitionScanner.DTD_INTERNAL_COMMENT);
		assistant.setContentAssistProcessor(processor,
				XMLPartitionScanner.DTD_INTERNAL_DECL);
		assistant
				.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		assistant
				.setInformationControlCreator(getInformationControlCreator(sourceViewer));

		return assistant;
	}

}