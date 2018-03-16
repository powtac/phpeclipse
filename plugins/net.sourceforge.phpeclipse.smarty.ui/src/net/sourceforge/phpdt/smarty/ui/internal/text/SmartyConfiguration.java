/*
 * Created on 25.08.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.sourceforge.phpdt.smarty.ui.internal.text;

import java.util.Map;

import net.sourceforge.phpeclipse.xml.ui.internal.text.XMLConfiguration;
import net.sourceforge.phpeclipse.xml.ui.internal.text.XMLPartitionScanner;
import net.sourceforge.phpeclipse.xml.ui.text.XMLTextTools;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.ITextEditor;

public class SmartyConfiguration extends XMLConfiguration {
	private SmartyTagScanner smartyTagScanner;

	public SmartyConfiguration(XMLTextTools tools) {
		this(tools, null);
	}

	public SmartyConfiguration(XMLTextTools tools, ITextEditor editor) {
		super(tools, editor);
		Map tokens = tools.getTokens();
		smartyTagScanner = new SmartyTagScanner(tokens);
	}

	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant assistant = new ContentAssistant();
		assistant
				.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

		IContentAssistProcessor processor = new SmartyCompletionProcessor();
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

		assistant
				.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		assistant
				.setInformationControlCreator(getInformationControlCreator(sourceViewer));

		return assistant;
	}

	/**
	 * Returns a scanner which is configured to scan XML text.
	 * 
	 * @return an XML text scanner
	 */
	public SmartyTagScanner getSmartyScanner() {
		return smartyTagScanner;
	}

	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getPresentationReconciler(ISourceViewer)
	 */
	public IPresentationReconciler getPresentationReconciler(
			ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr;

		dr = new DefaultDamagerRepairer(getSmartyScanner());
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
}