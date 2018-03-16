/**********************************************************************
 Copyright (c) 2000, 2002 IBM Corp. and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html

 Contributors:
 IBM Corporation - Initial implementation
 www.phpeclipse.de
 **********************************************************************/
package net.sourceforge.phpdt.ui.text;

import java.util.Vector;

import net.sourceforge.phpdt.core.JavaCore;
import net.sourceforge.phpdt.internal.ui.text.AbstractJavaScanner;
import net.sourceforge.phpdt.internal.ui.text.ContentAssistPreference;
import net.sourceforge.phpdt.internal.ui.text.HTMLTextPresenter;
import net.sourceforge.phpdt.internal.ui.text.IPHPPartitions;
import net.sourceforge.phpdt.internal.ui.text.JavaAnnotationHover;
import net.sourceforge.phpdt.internal.ui.text.JavaCompositeReconcilingStrategy;
import net.sourceforge.phpdt.internal.ui.text.JavaElementProvider;
import net.sourceforge.phpdt.internal.ui.text.JavaOutlineInformationControl;
import net.sourceforge.phpdt.internal.ui.text.JavaPresentationReconciler;
import net.sourceforge.phpdt.internal.ui.text.JavaReconciler;
import net.sourceforge.phpdt.internal.ui.text.java.JavaFormattingStrategy;
import net.sourceforge.phpdt.internal.ui.text.java.JavaStringAutoIndentStrategyDQ;
import net.sourceforge.phpdt.internal.ui.text.java.JavaStringAutoIndentStrategySQ;
import net.sourceforge.phpdt.internal.ui.text.java.hover.JavaEditorTextHoverDescriptor;
import net.sourceforge.phpdt.internal.ui.text.java.hover.JavaEditorTextHoverProxy;
import net.sourceforge.phpdt.internal.ui.text.java.hover.JavaInformationProvider;
import net.sourceforge.phpdt.internal.ui.text.phpdoc.JavaDocAutoIndentStrategy;
import net.sourceforge.phpdt.internal.ui.text.phpdoc.PHPDocCodeScanner;
import net.sourceforge.phpdt.internal.ui.text.phpdoc.PHPDocCompletionProcessor;
import net.sourceforge.phpdt.ui.PreferenceConstants;
import net.sourceforge.phpeclipse.IPreferenceConstants;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;
import net.sourceforge.phpeclipse.phpeditor.php.HTMLCompletionProcessor;
import net.sourceforge.phpeclipse.phpeditor.php.PHPAutoIndentStrategy;
import net.sourceforge.phpeclipse.phpeditor.php.PHPCodeScanner;
import net.sourceforge.phpeclipse.phpeditor.php.PHPCompletionProcessor;
import net.sourceforge.phpeclipse.phpeditor.php.PHPDocumentPartitioner;
import net.sourceforge.phpeclipse.phpeditor.php.PHPDoubleClickSelector;
import net.sourceforge.phpeclipse.phpeditor.php.PHPPartitionScanner;
import net.sourceforge.phpeclipse.xml.ui.XMLPlugin;
import net.sourceforge.phpeclipse.xml.ui.internal.text.XMLConfiguration;
import net.sourceforge.phpeclipse.xml.ui.internal.text.XMLPartitionScanner;
import net.sourceforge.phpeclipse.xml.ui.text.XMLTextTools;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.ContentFormatter;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.formatter.IFormattingStrategy;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.DefaultPartitioner;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Configuration for an <code>SourceViewer</code> which shows PHP code.
 */
public class PHPSourceViewerConfiguration extends SourceViewerConfiguration {
	/**
	 * Preference key used to look up display tab width.
	 * 
	 * @since 2.0
	 */
	public final static String PREFERENCE_TAB_WIDTH = PreferenceConstants.EDITOR_TAB_WIDTH;

	/**
	 * Preference key for inserting spaces rather than tabs.
	 * 
	 * @since 2.0
	 */
	public final static String SPACES_FOR_TABS = PreferenceConstants.EDITOR_SPACES_FOR_TABS;

	// public static final String HTML_DEFAULT =
	// IPHPPartitionScannerConstants.HTML;
	// IDocument.DEFAULT_CONTENT_TYPE;
	// private JavaTextTools fJavaTextTools;

	private ITextEditor fTextEditor;

	/**
	 * The document partitioning.
	 * 
	 * @since 3.0
	 */
	private String fDocumentPartitioning;

	private ContentFormatter fFormatter;

	/**
	 * Single token scanner.
	 */
	static class SingleTokenScanner extends BufferedRuleBasedScanner {
		public SingleTokenScanner(TextAttribute attribute) {
			setDefaultReturnToken(new Token(attribute));
		}
	};

	/**
	 * The document partitioning.
	 * 
	 * @since 3.0
	 */
	// private String fDocumentPartitioning;
	/**
	 * The Java source code scanner
	 * 
	 * @since 3.0
	 */
	private AbstractJavaScanner fCodeScanner;

	/**
	 * The Java multi-line comment scanner
	 * 
	 * @since 3.0
	 */
	private AbstractJavaScanner fMultilineCommentScanner;

	/**
	 * The Java single-line comment scanner
	 * 
	 * @since 3.0
	 */
	private AbstractJavaScanner fSinglelineCommentScanner;

	/**
	 * The PHP double quoted string scanner
	 */
	private AbstractJavaScanner fStringDQScanner;

	/**
	 * The PHP single quoted string scanner
	 */
	private AbstractJavaScanner fStringSQScanner;

	/**
	 * The Javadoc scanner
	 * 
	 * @since 3.0
	 */
	private AbstractJavaScanner fJavaDocScanner;

	/**
	 * The preference store, can be read-only
	 * 
	 * @since 3.0
	 */
	private IPreferenceStore fPreferenceStore;

	/**
	 * The color manager
	 * 
	 * @since 3.0
	 */
	private IColorManager fColorManager;

	private XMLTextTools fXMLTextTools;

	private XMLConfiguration xmlConfiguration;

	/**
	 * Creates a new Java source viewer configuration for viewers in the given
	 * editor using the given preference store, the color manager and the
	 * specified document partitioning.
	 * <p>
	 * Creates a Java source viewer configuration in the new setup without text
	 * tools. Clients are allowed to call
	 * {@link JavaSourceViewerConfiguration#handlePropertyChangeEvent(PropertyChangeEvent)}and
	 * disallowed to call
	 * {@link JavaSourceViewerConfiguration#getPreferenceStore()}on the
	 * resulting Java source viewer configuration.
	 * </p>
	 * 
	 * @param colorManager
	 *            the color manager
	 * @param preferenceStore
	 *            the preference store, can be read-only
	 * @param editor
	 *            the editor in which the configured viewer(s) will reside
	 * @param partitioning
	 *            the document partitioning for this configuration
	 * @since 3.0
	 */
	public PHPSourceViewerConfiguration(IColorManager colorManager,
			IPreferenceStore preferenceStore, ITextEditor editor,
			String partitioning) {
		fColorManager = colorManager;
		fPreferenceStore = preferenceStore;
		fTextEditor = editor;
		fDocumentPartitioning = partitioning;
		// fJavaTextTools = PHPeclipsePlugin.getDefault().getJavaTextTools();
		fXMLTextTools = XMLPlugin.getDefault().getXMLTextTools();
		xmlConfiguration = new XMLConfiguration(fXMLTextTools);
		fColorManager = colorManager;
		fPreferenceStore = preferenceStore;
		fTextEditor = editor;
		fDocumentPartitioning = partitioning;

		initializeScanners();
	}

	/**
	 * Creates a new Java source viewer configuration for viewers in the given
	 * editor using the given Java tools.
	 * 
	 * @param tools
	 *            the Java text tools to be used
	 * @param editor
	 *            the editor in which the configured viewer(s) will reside
	 * @see JavaTextTools
	 * @deprecated As of 3.0, replaced by
	 *             {@link JavaSourceViewerConfiguration#JavaSourceViewerConfiguration(IColorManager, IPreferenceStore, ITextEditor, String)}
	 */
	// public PHPSourceViewerConfiguration(JavaTextTools tools, PHPEditor
	// editor, String partitioning) {
	// fJavaTextTools = tools;
	// fColorManager = tools.getColorManager();
	// fPreferenceStore = createPreferenceStore();
	// fDocumentPartitioning = partitioning;
	// fCodeScanner = (AbstractJavaScanner) fJavaTextTools.getCodeScanner();
	// fMultilineCommentScanner = (AbstractJavaScanner)
	// fJavaTextTools.getMultilineCommentScanner();
	// fSinglelineCommentScanner = (AbstractJavaScanner)
	// fJavaTextTools.getSinglelineCommentScanner();
	// fStringDQScanner = (AbstractJavaScanner)
	// fJavaTextTools.getStringScanner();
	// fJavaDocScanner = (AbstractJavaScanner)
	// fJavaTextTools.getJavaDocScanner();
	// fTextEditor = editor;
	// fXMLTextTools = XMLPlugin.getDefault().getXMLTextTools();
	// xmlConfiguration = new XMLConfiguration(fXMLTextTools);
	// }
	/**
	 * Returns the color manager for this configuration.
	 * 
	 * @return the color manager
	 */
	protected IColorManager getColorManager() {
		return fColorManager;
	}

	/**
	 * Initializes the scanners.
	 * 
	 * @since 3.0
	 */
	private void initializeScanners() {
		// Assert.isTrue(isNewSetup());
		fCodeScanner = new PHPCodeScanner(getColorManager(), fPreferenceStore);
		fMultilineCommentScanner = new SingleTokenPHPScanner(getColorManager(),
				fPreferenceStore, IPreferenceConstants.PHP_MULTILINE_COMMENT);
		fSinglelineCommentScanner = new SingleTokenPHPScanner(
				getColorManager(), fPreferenceStore,
				IPreferenceConstants.PHP_SINGLELINE_COMMENT);
		// fStringDQScanner = new SingleTokenPHPScanner(getColorManager(),
		// fPreferenceStore, IPreferenceConstants.PHP_STRING_DQ);
		fStringDQScanner = new PHPStringDQCodeScanner(getColorManager(),
				fPreferenceStore);
		fStringSQScanner = new SingleTokenPHPScanner(getColorManager(),
				fPreferenceStore, IPreferenceConstants.PHP_STRING_SQ);
		fJavaDocScanner = new PHPDocCodeScanner(getColorManager(),
				fPreferenceStore);
	}

	/**
	 * Determines whether the preference change encoded by the given event
	 * changes the behavior of one of its contained components.
	 * 
	 * @param event
	 *            the event to be investigated
	 * @return <code>true</code> if event causes a behavioral change
	 * @since 3.0
	 */
	public boolean affectsTextPresentation(PropertyChangeEvent event) {
		return fCodeScanner.affectsBehavior(event)
				|| fMultilineCommentScanner.affectsBehavior(event)
				|| fSinglelineCommentScanner.affectsBehavior(event)
				|| fStringDQScanner.affectsBehavior(event)
				|| fStringSQScanner.affectsBehavior(event)
				|| fJavaDocScanner.affectsBehavior(event);
	}

	/**
	 * Adapts the behavior of the contained components to the change encoded in
	 * the given event.
	 * <p>
	 * Clients are not allowed to call this method if the old setup with text
	 * tools is in use.
	 * </p>
	 * 
	 * @param event
	 *            the event to which to adapt
	 * @see JavaSourceViewerConfiguration#JavaSourceViewerConfiguration(IColorManager,
	 *      IPreferenceStore, ITextEditor, String)
	 * @since 3.0
	 */
	public void handlePropertyChangeEvent(PropertyChangeEvent event) {
		// Assert.isTrue(isNewSetup());
		if (fCodeScanner.affectsBehavior(event))
			fCodeScanner.adaptToPreferenceChange(event);
		if (fMultilineCommentScanner.affectsBehavior(event))
			fMultilineCommentScanner.adaptToPreferenceChange(event);
		if (fSinglelineCommentScanner.affectsBehavior(event))
			fSinglelineCommentScanner.adaptToPreferenceChange(event);
		if (fStringDQScanner.affectsBehavior(event))
			fStringDQScanner.adaptToPreferenceChange(event);
		if (fStringSQScanner.affectsBehavior(event))
			fStringSQScanner.adaptToPreferenceChange(event);
		if (fJavaDocScanner.affectsBehavior(event))
			fJavaDocScanner.adaptToPreferenceChange(event);
	}

	/*
	 * @see SourceViewerConfiguration#getContentFormatter(ISourceViewer)
	 */
	public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
		// if (fFormatter == null) {
		// fFormatter = new ContentFormatter();
		// fFormattingStrategy = new HTMLFormattingStrategy(this,
		// sourceViewer);
		// fFormatter.setFormattingStrategy(fFormattingStrategy, HTML_DEFAULT);
		// fFormatter.enablePartitionAwareFormatting(false);
		// fFormatter.setPartitionManagingPositionCategories(getConfiguredContentTypes(null));
		// }
		// return fFormatter;
		if (fFormatter == null) {
			// ContentFormatter
			fFormatter = new ContentFormatter();
			IFormattingStrategy strategy = new JavaFormattingStrategy(
					sourceViewer);
			fFormatter.setFormattingStrategy(strategy,
					IDocument.DEFAULT_CONTENT_TYPE);
			fFormatter.enablePartitionAwareFormatting(false);
			fFormatter
					.setPartitionManagingPositionCategories(getPartitionManagingPositionCategories());
		}
		return fFormatter;
	}

	/**
	 * Returns the names of the document position categories used by the
	 * document partitioners created by this object to manage their partition
	 * information. If the partitioners don't use document position categories,
	 * the returned result is <code>null</code>.
	 * 
	 * @return the partition managing position categories or <code>null</code>
	 *         if there is none
	 */
	public String[] getPartitionManagingPositionCategories() {
		return new String[] { DefaultPartitioner.CONTENT_TYPES_CATEGORY };
	}

	// /**
	// * Returns the names of the document position categories used by the
	// document
	// * partitioners created by this object to manage their partition
	// information.
	// * If the partitioners don't use document position categories, the
	// returned
	// * result is <code>null</code>.
	// *
	// * @return the partition managing position categories or
	// <code>null</code>
	// * if there is none
	// */
	// private String[] getPartitionManagingPositionCategories() {
	// return new String[] { DefaultPartitioner.CONTENT_TYPES_CATEGORY };
	// }
	public ITextEditor getEditor() {
		return fTextEditor;
	}

	/**
	 * Returns the preference store used by this configuration to initialize the
	 * individual bits and pieces.
	 * 
	 * @return the preference store used to initialize this configuration
	 * 
	 * @since 2.0
	 */
	protected IPreferenceStore getPreferenceStore() {
		return PHPeclipsePlugin.getDefault().getPreferenceStore();
	}

	// /* (non-Javadoc)
	// * Method declared on SourceViewerConfiguration
	// */
	// public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
	// return new PHPAnnotationHover();
	// }
	/*
	 * @see SourceViewerConfiguration#getAnnotationHover(ISourceViewer)
	 */
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return new JavaAnnotationHover(JavaAnnotationHover.VERTICAL_RULER_HOVER);
	}

	/*
	 * @see SourceViewerConfiguration#getOverviewRulerAnnotationHover(ISourceViewer)
	 * @since 3.0
	 */
	public IAnnotationHover getOverviewRulerAnnotationHover(
			ISourceViewer sourceViewer) {
		return new JavaAnnotationHover(JavaAnnotationHover.OVERVIEW_RULER_HOVER);
	}

	public IAutoEditStrategy[] getAutoEditStrategies(
			ISourceViewer sourceViewer, String contentType) {
		IAutoEditStrategy strategy = new DefaultIndentLineAutoEditStrategy();
		if (IPHPPartitions.PHP_PHPDOC_COMMENT.equals(contentType)
				|| IPHPPartitions.PHP_MULTILINE_COMMENT.equals(contentType))
			strategy = new JavaDocAutoIndentStrategy(
					getConfiguredDocumentPartitioning(sourceViewer));
		else if (IPHPPartitions.PHP_STRING_DQ.equals(contentType))
			strategy = new JavaStringAutoIndentStrategyDQ(
					getConfiguredDocumentPartitioning(sourceViewer));
		else if (IPHPPartitions.PHP_STRING_SQ.equals(contentType))
			strategy = new JavaStringAutoIndentStrategySQ(
					getConfiguredDocumentPartitioning(sourceViewer));
		else
			strategy = (PHPDocumentPartitioner.PHP_TEMPLATE_DATA
					.equals(contentType)
					|| PHPDocumentPartitioner.PHP_SCRIPT_CODE
							.equals(contentType)
					|| IDocument.DEFAULT_CONTENT_TYPE.equals(contentType)
					|| IPHPPartitions.PHP_PARTITIONING.equals(contentType)
					|| PHPPartitionScanner.PHP_SCRIPTING_AREA
							.equals(contentType) ? new PHPAutoIndentStrategy()
					: new DefaultIndentLineAutoEditStrategy());
		IAutoEditStrategy[] result = new IAutoEditStrategy[1];
		result[0] = strategy;
		return result;
	}

	/**
	 * Returns the PHP source code scanner for this configuration.
	 * 
	 * @return the PHP source code scanner
	 */
	protected RuleBasedScanner getCodeScanner() {
		return fCodeScanner; // fJavaTextTools.getCodeScanner();
	}

	/**
	 * Returns the Java multi-line comment scanner for this configuration.
	 * 
	 * @return the Java multi-line comment scanner
	 * @since 2.0
	 */
	protected RuleBasedScanner getMultilineCommentScanner() {
		return fMultilineCommentScanner;
	}

	/**
	 * Returns the Java single-line comment scanner for this configuration.
	 * 
	 * @return the Java single-line comment scanner
	 * @since 2.0
	 */
	protected RuleBasedScanner getSinglelineCommentScanner() {
		return fSinglelineCommentScanner;
	}

	/**
	 * Returns the PHP double quoted string scanner for this configuration.
	 * 
	 * @return the PHP double quoted string scanner
	 */
	protected RuleBasedScanner getStringDQScanner() {
		return fStringDQScanner;
	}

	/**
	 * Returns the PHP single quoted string scanner for this configuration.
	 * 
	 * @return the PHP single quoted string scanner
	 */
	protected RuleBasedScanner getStringSQScanner() {
		return fStringSQScanner;
	}

	/**
	 * Returns the HTML source code scanner for this configuration.
	 * 
	 * @return the HTML source code scanner
	 */
	// protected RuleBasedScanner getHTMLScanner() {
	// return fJavaTextTools.getHTMLScanner();
	// }
	/**
	 * Returns the Smarty source code scanner for this configuration.
	 * 
	 * @return the Smarty source code scanner
	 */
	// protected RuleBasedScanner getSmartyScanner() {
	// return fJavaTextTools.getSmartyScanner();
	// }
	/*
	 * @see SourceViewerConfiguration#getReconciler(ISourceViewer)
	 */
	/*
	 * @see SourceViewerConfiguration#getReconciler(ISourceViewer)
	 */
	public IReconciler getReconciler(ISourceViewer sourceViewer) {

		final ITextEditor editor = getEditor();
		if (editor != null && editor.isEditable()) {

			JavaCompositeReconcilingStrategy strategy = new JavaCompositeReconcilingStrategy(
					editor, getConfiguredDocumentPartitioning(sourceViewer));
			JavaReconciler reconciler = new JavaReconciler(editor, strategy,
					false);
			reconciler.setIsIncrementalReconciler(false);
			reconciler.setProgressMonitor(new NullProgressMonitor());
			reconciler.setDelay(500);

			return reconciler;
		}
		return null;
	}

	/*
	 * @see SourceViewerConfiguration#getConfiguredTextHoverStateMasks(ISourceViewer,
	 *      String)
	 * @since 2.1
	 */
	public int[] getConfiguredTextHoverStateMasks(ISourceViewer sourceViewer,
			String contentType) {
		JavaEditorTextHoverDescriptor[] hoverDescs = PHPeclipsePlugin
				.getDefault().getJavaEditorTextHoverDescriptors();
		int stateMasks[] = new int[hoverDescs.length];
		int stateMasksLength = 0;
		for (int i = 0; i < hoverDescs.length; i++) {
			if (hoverDescs[i].isEnabled()) {
				int j = 0;
				int stateMask = hoverDescs[i].getStateMask();
				while (j < stateMasksLength) {
					if (stateMasks[j] == stateMask)
						break;
					j++;
				}
				if (j == stateMasksLength)
					stateMasks[stateMasksLength++] = stateMask;
			}
		}
		if (stateMasksLength == hoverDescs.length)
			return stateMasks;
		int[] shortenedStateMasks = new int[stateMasksLength];
		System.arraycopy(stateMasks, 0, shortenedStateMasks, 0,
				stateMasksLength);
		return shortenedStateMasks;
	}

	/*
	 * @see SourceViewerConfiguration#getTextHover(ISourceViewer, String, int)
	 * @since 2.1
	 */
	public ITextHover getTextHover(ISourceViewer sourceViewer,
			String contentType, int stateMask) {
		JavaEditorTextHoverDescriptor[] hoverDescs = PHPeclipsePlugin
				.getDefault().getJavaEditorTextHoverDescriptors();
		int i = 0;
		while (i < hoverDescs.length) {
			if (hoverDescs[i].isEnabled()
					&& hoverDescs[i].getStateMask() == stateMask)
				return new JavaEditorTextHoverProxy(hoverDescs[i], getEditor());
			i++;
		}
		return null;
		// if (fEditor != null) {
		// IEditorInput editorInput = fEditor.getEditorInput();
		// if (editorInput instanceof IFileEditorInput) {
		// try {
		// IFile f = ((IFileEditorInput) editorInput).getFile();
		// return new PHPTextHover(f.getProject());
		// } catch (NullPointerException e) {
		// // this exception occurs, if getTextHover is called by
		// // preference pages !
		// }
		// }
		// }
		// return new PHPTextHover(null);
	}

	/*
	 * @see SourceViewerConfiguration#getTextHover(ISourceViewer, String)
	 */
	public ITextHover getTextHover(ISourceViewer sourceViewer,
			String contentType) {
		return getTextHover(sourceViewer, contentType,
				ITextViewerExtension2.DEFAULT_HOVER_STATE_MASK);
	}

	/**
	 * Returns the SmartyDoc source code scanner for this configuration.
	 * 
	 * @return the SmartyDoc source code scanner
	 */
	// protected RuleBasedScanner getSmartyDocScanner() {
	// return fJavaTextTools.getSmartyDocScanner();
	// }
	/**
	 * Returns the PHPDoc source code scanner for this configuration.
	 * 
	 * @return the PHPDoc source code scanner
	 */
	protected RuleBasedScanner getPHPDocScanner() {
		return fJavaDocScanner; // fJavaTextTools.getJavaDocScanner();
	}

	/*
	 * (non-Javadoc) Method declared on SourceViewerConfiguration
	 */
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] { IDocument.DEFAULT_CONTENT_TYPE,
				PHPPartitionScanner.PHP_SCRIPTING_AREA,

				IPHPPartitions.HTML, IPHPPartitions.HTML_MULTILINE_COMMENT,
				IPHPPartitions.PHP_PARTITIONING,
				IPHPPartitions.PHP_SINGLELINE_COMMENT,
				IPHPPartitions.PHP_MULTILINE_COMMENT,
				IPHPPartitions.PHP_PHPDOC_COMMENT,
				IPHPPartitions.PHP_STRING_DQ, IPHPPartitions.PHP_STRING_SQ,
				IPHPPartitions.PHP_STRING_HEREDOC, IPHPPartitions.CSS,
				IPHPPartitions.CSS_MULTILINE_COMMENT,
				IPHPPartitions.JAVASCRIPT, IPHPPartitions.JS_MULTILINE_COMMENT,
				IPHPPartitions.SMARTY, IPHPPartitions.SMARTY_MULTILINE_COMMENT,

				XMLPartitionScanner.XML_PI, XMLPartitionScanner.XML_COMMENT,
				XMLPartitionScanner.XML_DECL, XMLPartitionScanner.XML_TAG,
				XMLPartitionScanner.XML_ATTRIBUTE,
				XMLPartitionScanner.XML_CDATA,

				XMLPartitionScanner.DTD_INTERNAL,
				XMLPartitionScanner.DTD_INTERNAL_PI,
				XMLPartitionScanner.DTD_INTERNAL_COMMENT,
				XMLPartitionScanner.DTD_INTERNAL_DECL,

				PHPDocumentPartitioner.PHP_TEMPLATE_DATA,
				PHPDocumentPartitioner.PHP_SCRIPT_CODE };
	}

	public String[] getConfiguredHTMLContentTypes() {
		return new String[] { XMLPartitionScanner.XML_PI,
				XMLPartitionScanner.XML_COMMENT, XMLPartitionScanner.XML_DECL,
				XMLPartitionScanner.XML_TAG, XMLPartitionScanner.XML_ATTRIBUTE,
				XMLPartitionScanner.XML_CDATA,

				XMLPartitionScanner.DTD_INTERNAL,
				XMLPartitionScanner.DTD_INTERNAL_PI,
				XMLPartitionScanner.DTD_INTERNAL_COMMENT,
				XMLPartitionScanner.DTD_INTERNAL_DECL, };
	}

	public String[] getConfiguredPHPContentTypes() {
		return new String[] { IDocument.DEFAULT_CONTENT_TYPE,
				IPHPPartitions.PHP_PARTITIONING,
				IPHPPartitions.PHP_SINGLELINE_COMMENT,
				IPHPPartitions.PHP_MULTILINE_COMMENT,
				IPHPPartitions.PHP_PHPDOC_COMMENT,
				IPHPPartitions.PHP_STRING_DQ, IPHPPartitions.PHP_STRING_SQ,
				IPHPPartitions.PHP_STRING_HEREDOC, IPHPPartitions.CSS,
				IPHPPartitions.CSS_MULTILINE_COMMENT,
				IPHPPartitions.JAVASCRIPT, IPHPPartitions.JS_MULTILINE_COMMENT,
				IPHPPartitions.SMARTY, IPHPPartitions.SMARTY_MULTILINE_COMMENT, };
	}

	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getConfiguredDocumentPartitioning(org.eclipse.jface.text.source.ISourceViewer)
	 * @since 3.0
	 */
	public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
		if (fDocumentPartitioning != null)
			return fDocumentPartitioning;
		return super.getConfiguredDocumentPartitioning(sourceViewer);
	}

	/*
	 * (non-Javadoc) Method declared on SourceViewerConfiguration
	 */
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant assistant = new ContentAssistant();
		IContentAssistProcessor processor = new HTMLCompletionProcessor(
				getEditor());
		assistant
				.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		assistant.setContentAssistProcessor(processor, IPHPPartitions.HTML);
		assistant.setContentAssistProcessor(processor,
				IPHPPartitions.HTML_MULTILINE_COMMENT);

		assistant.setContentAssistProcessor(processor, IPHPPartitions.CSS);
		assistant.setContentAssistProcessor(processor,
				IPHPPartitions.CSS_MULTILINE_COMMENT);
		assistant.setContentAssistProcessor(processor,
				IPHPPartitions.JAVASCRIPT);
		assistant.setContentAssistProcessor(processor,
				IPHPPartitions.JS_MULTILINE_COMMENT);
		// TODO define special smarty partition content assist
		assistant.setContentAssistProcessor(processor, IPHPPartitions.SMARTY);
		assistant.setContentAssistProcessor(processor,
				IPHPPartitions.SMARTY_MULTILINE_COMMENT);

		assistant.setContentAssistProcessor(processor,
				PHPDocumentPartitioner.PHP_TEMPLATE_DATA);
		String[] htmlTypes = getConfiguredHTMLContentTypes();
		for (int i = 0; i < htmlTypes.length; i++) {
			assistant.setContentAssistProcessor(processor, htmlTypes[i]);
		}
		processor = new PHPCompletionProcessor(getEditor());

		assistant.setContentAssistProcessor(processor,
				PHPDocumentPartitioner.PHP_SCRIPT_CODE);
		assistant.setContentAssistProcessor(processor,
				IPHPPartitions.PHP_PARTITIONING);
		assistant.setContentAssistProcessor(processor,
				IPHPPartitions.PHP_STRING_DQ);
		assistant.setContentAssistProcessor(processor,
				IPHPPartitions.PHP_STRING_SQ);
		assistant.setContentAssistProcessor(processor,
				IPHPPartitions.PHP_STRING_HEREDOC);

		assistant.setContentAssistProcessor(new PHPDocCompletionProcessor(
				getEditor()), IPHPPartitions.PHP_PHPDOC_COMMENT);
		// assistant.enableAutoActivation(true);
		// assistant.setAutoActivationDelay(500);
		// assistant.setProposalPopupOrientation(ContentAssistant.PROPOSAL_OVERLAY);
		// ContentAssistPreference.configure(assistant, getPreferenceStore());
		// assistant.setContextInformationPopupOrientation(
		// ContentAssistant.CONTEXT_INFO_ABOVE);
		// assistant.setContextInformationPopupBackground(
		// PHPEditorEnvironment.getPHPColorProvider().getColor(
		// new RGB(150, 150, 0)));
		ContentAssistPreference.configure(assistant, getPreferenceStore());
		assistant
				.setContextInformationPopupOrientation(ContentAssistant.CONTEXT_INFO_ABOVE);
		assistant
				.setInformationControlCreator(getInformationControlCreator(sourceViewer));
		return assistant;
	}

	/*
	 * (non-Javadoc) Method declared on SourceViewerConfiguration
	 */
	// public String getDefaultPrefix(ISourceViewer sourceViewer, String
	// contentType) {
	// return (PHPPartitionScanner.PHP.equals(contentType) ? "//" : null);
	// //$NON-NLS-1$
	// // return (IDocument.DEFAULT_CONTENT_TYPE.equals(contentType) ? "//" :
	// null); //$NON-NLS-1$
	// }
	/*
	 * @see SourceViewerConfiguration#getDefaultPrefix(ISourceViewer, String)
	 * @since 2.0
	 */
	public String[] getDefaultPrefixes(ISourceViewer sourceViewer,
			String contentType) {
		return new String[] { "//", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * (non-Javadoc) Method declared on SourceViewerConfiguration
	 */
	public ITextDoubleClickStrategy getDoubleClickStrategy(
			ISourceViewer sourceViewer, String contentType) {
		return new PHPDoubleClickSelector();
	}

	/*
	 * @see SourceViewerConfiguration#getIndentPrefixes(ISourceViewer, String)
	 */
	public String[] getIndentPrefixes(ISourceViewer sourceViewer,
			String contentType) {
		Vector vector = new Vector();
		// prefix[0] is either '\t' or ' ' x tabWidth, depending on useSpaces
		final IPreferenceStore preferences = PHPeclipsePlugin.getDefault()
				.getPreferenceStore();
		int tabWidth = preferences.getInt(JavaCore.FORMATTER_TAB_SIZE);
		boolean useSpaces = getPreferenceStore().getBoolean(SPACES_FOR_TABS);
		for (int i = 0; i <= tabWidth; i++) {
			StringBuffer prefix = new StringBuffer();
			if (useSpaces) {
				for (int j = 0; j + i < tabWidth; j++)
					prefix.append(' ');
				if (i != 0)
					prefix.append('\t');
			} else {
				for (int j = 0; j < i; j++)
					prefix.append(' ');
				if (i != tabWidth)
					prefix.append('\t');
			}
			vector.add(prefix.toString());
		}
		vector.add(""); //$NON-NLS-1$
		return (String[]) vector.toArray(new String[vector.size()]);
	}

	/**
	 * @return <code>true</code> iff the new setup without text tools is in
	 *         use.
	 * 
	 * @since 3.0
	 */
	// private boolean isNewSetup() {
	// return fJavaTextTools == null;
	// }
	/**
	 * Creates and returns a preference store which combines the preference
	 * stores from the text tools and which is read-only.
	 * 
	 * @return the read-only preference store
	 * @since 3.0
	 */
	// private IPreferenceStore createPreferenceStore() {
	// Assert.isTrue(!isNewSetup());
	// IPreferenceStore generalTextStore = EditorsUI.getPreferenceStore();
	// if (fJavaTextTools.getCorePreferenceStore() == null)
	// return new ChainedPreferenceStore(new IPreferenceStore[] {
	// fJavaTextTools.getPreferenceStore(), generalTextStore });
	//
	// return new ChainedPreferenceStore(new IPreferenceStore[] {
	// fJavaTextTools.getPreferenceStore(),
	// new PreferencesAdapter(fJavaTextTools.getCorePreferenceStore()),
	// generalTextStore });
	// }
	/*
	 * (non-Javadoc) Method declared on SourceViewerConfiguration
	 */
	public IPresentationReconciler getPresentationReconciler(
			ISourceViewer sourceViewer) {
		// PHPColorProvider provider =
		// PHPEditorEnvironment.getPHPColorProvider();
		// JavaColorManager provider =
		// PHPEditorEnvironment.getPHPColorProvider();
		PresentationReconciler phpReconciler = new JavaPresentationReconciler();
		phpReconciler
				.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

		// DefaultDamagerRepairer dr = new
		// DefaultDamagerRepairer(getHTMLScanner());
		// reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		// reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
		// dr = new DefaultDamagerRepairer(getHTMLScanner());
		// reconciler.setDamager(dr, IPHPPartitions.HTML);
		// reconciler.setRepairer(dr, IPHPPartitions.HTML);
		// dr = new DefaultDamagerRepairer(getHTMLScanner());
		// reconciler.setDamager(dr, IPHPPartitions.CSS);
		// reconciler.setRepairer(dr, IPHPPartitions.CSS);
		// dr = new DefaultDamagerRepairer(getHTMLScanner());
		// reconciler.setDamager(dr, IPHPPartitions.CSS_MULTILINE_COMMENT);
		// reconciler.setRepairer(dr, IPHPPartitions.CSS_MULTILINE_COMMENT);
		// dr = new DefaultDamagerRepairer(getHTMLScanner());
		// reconciler.setDamager(dr, IPHPPartitions.JAVASCRIPT);
		// reconciler.setRepairer(dr, IPHPPartitions.JAVASCRIPT);
		// dr = new DefaultDamagerRepairer(getHTMLScanner());
		// reconciler.setDamager(dr, IPHPPartitions.JS_MULTILINE_COMMENT);
		// reconciler.setRepairer(dr, IPHPPartitions.JS_MULTILINE_COMMENT);
		// DefaultDamagerRepairer phpDR = new
		// DefaultDamagerRepairer(getSmartyScanner());
		// phpReconciler.setDamager(phpDR, IPHPPartitions.SMARTY);
		// phpReconciler.setRepairer(phpDR, IPHPPartitions.SMARTY);
		// phpDR = new DefaultDamagerRepairer(getSmartyDocScanner());
		// phpReconciler.setDamager(phpDR,
		// IPHPPartitions.SMARTY_MULTILINE_COMMENT);
		// phpReconciler.setRepairer(phpDR,
		// IPHPPartitions.SMARTY_MULTILINE_COMMENT);
		// dr = new DefaultDamagerRepairer(new SingleTokenScanner(new
		// TextAttribute(fJavaTextTools.getColorManager().getColor(
		// PHPColorProvider.MULTI_LINE_COMMENT))));
		// reconciler.setDamager(dr, IPHPPartitions.HTML_MULTILINE_COMMENT);
		// reconciler.setRepairer(dr, IPHPPartitions.HTML_MULTILINE_COMMENT);

		DefaultDamagerRepairer phpDR = new DefaultDamagerRepairer(
				getCodeScanner());
		phpReconciler.setDamager(phpDR, IDocument.DEFAULT_CONTENT_TYPE);
		phpReconciler.setRepairer(phpDR, IDocument.DEFAULT_CONTENT_TYPE);

		phpDR = new DefaultDamagerRepairer(getCodeScanner());
		phpReconciler.setDamager(phpDR, IPHPPartitions.PHP_PARTITIONING);
		phpReconciler.setRepairer(phpDR, IPHPPartitions.PHP_PARTITIONING);

		phpDR = new DefaultDamagerRepairer(getPHPDocScanner());
		phpReconciler.setDamager(phpDR, IPHPPartitions.PHP_PHPDOC_COMMENT);
		phpReconciler.setRepairer(phpDR, IPHPPartitions.PHP_PHPDOC_COMMENT);

		phpDR = new DefaultDamagerRepairer(getStringDQScanner());
		phpReconciler.setDamager(phpDR, IPHPPartitions.PHP_STRING_DQ);
		phpReconciler.setRepairer(phpDR, IPHPPartitions.PHP_STRING_DQ);
		phpDR = new DefaultDamagerRepairer(getStringSQScanner());
		phpReconciler.setDamager(phpDR, IPHPPartitions.PHP_STRING_SQ);
		phpReconciler.setRepairer(phpDR, IPHPPartitions.PHP_STRING_SQ);
		phpDR = new DefaultDamagerRepairer(getStringDQScanner());
		phpReconciler.setDamager(phpDR, IPHPPartitions.PHP_STRING_HEREDOC);
		phpReconciler.setRepairer(phpDR, IPHPPartitions.PHP_STRING_HEREDOC);
		phpDR = new DefaultDamagerRepairer(getSinglelineCommentScanner());
		phpReconciler.setDamager(phpDR, IPHPPartitions.PHP_SINGLELINE_COMMENT);
		phpReconciler.setRepairer(phpDR, IPHPPartitions.PHP_SINGLELINE_COMMENT);
		phpDR = new DefaultDamagerRepairer(getMultilineCommentScanner());
		phpReconciler.setDamager(phpDR, IPHPPartitions.PHP_MULTILINE_COMMENT);
		phpReconciler.setRepairer(phpDR, IPHPPartitions.PHP_MULTILINE_COMMENT);

		PresentationReconciler reconciler = new PresentationReconciler();
		reconciler
				.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		//
		// JavaTextTools jspTextTools =
		// PHPeclipsePlugin.getDefault().getJavaTextTools();
		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(
				getPHPDocScanner());// jspTextTools.getJSPTextScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		// dr = new DefaultDamagerRepairer(new SingleTokenScanner(new
		// TextAttribute(fJavaTextTools.getColorManager().getColor(
		// PHPColorProvider.PHPDOC_TAG))));//jspTextTools.getJSPBracketScanner());
		// reconciler.setDamager(dr, JSPScriptScanner.JSP_BRACKET);
		// reconciler.setRepairer(dr, JSPScriptScanner.JSP_BRACKET);

		// xml partitions
		configureEmbeddedPresentationReconciler(reconciler, xmlConfiguration
				.getPresentationReconciler(sourceViewer), xmlConfiguration
				.getConfiguredContentTypes(sourceViewer),
				PHPDocumentPartitioner.PHP_TEMPLATE_DATA);

		// java partitions
		configureEmbeddedPresentationReconciler(reconciler, phpReconciler,
				getConfiguredPHPContentTypes(),
				PHPDocumentPartitioner.PHP_SCRIPT_CODE);

		return reconciler;
	}

	private void configureEmbeddedPresentationReconciler(
			PresentationReconciler reconciler,
			IPresentationReconciler embedded, String[] types, String defaultType) {
		for (int i = 0; i < types.length; i++) {
			String type = types[i];

			IPresentationDamager damager = embedded.getDamager(type);
			IPresentationRepairer repairer = embedded.getRepairer(type);

			if (type == IDocument.DEFAULT_CONTENT_TYPE) {
				type = defaultType;
			}

			reconciler.setDamager(damager, type);
			reconciler.setRepairer(repairer, type);
		}
	}

	/*
	 * (non-Javadoc) Method declared on SourceViewerConfiguration
	 */
	public int getTabWidth(ISourceViewer sourceViewer) {
		return getPreferenceStore().getInt(PREFERENCE_TAB_WIDTH);
	}

	/*
	 * (non-Javadoc) Method declared on SourceViewerConfiguration
	 */
	// public ITextHover getTextHover(ISourceViewer sourceViewer, String
	// contentType) {
	// if (fEditor != null) {
	// IEditorInput editorInput = fEditor.getEditorInput();
	// if (editorInput instanceof IFileEditorInput) {
	// try {
	// IFile f = ((IFileEditorInput) editorInput).getFile();
	// return new PHPTextHover(f.getProject());
	// } catch (NullPointerException e) {
	// // this exception occurs, if getTextHover is called by preference pages
	// !
	// }
	// }
	// }
	// return new PHPTextHover(null);
	// }
	/*
	 * @see SourceViewerConfiguration#getInformationControlCreator(ISourceViewer)
	 * @since 2.0
	 */
	public IInformationControlCreator getInformationControlCreator(
			ISourceViewer sourceViewer) {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent, SWT.NONE,
						new HTMLTextPresenter(true));
				// return new HoverBrowserControl(parent);
			}
		};
	}

	/*
	 * @see SourceViewerConfiguration#getInformationPresenter(ISourceViewer)
	 * @since 2.0
	 */
	public IInformationPresenter getInformationPresenter(
			ISourceViewer sourceViewer) {
		InformationPresenter presenter = new InformationPresenter(
				getInformationPresenterControlCreator(sourceViewer));
		presenter
				.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		IInformationProvider provider = new JavaInformationProvider(getEditor());
		presenter.setInformationProvider(provider,
				IDocument.DEFAULT_CONTENT_TYPE);
		presenter.setInformationProvider(provider,
				IPHPPartitions.PHP_PHPDOC_COMMENT);
		// presenter.setInformationProvider(provider,
		// IPHPPartitions.JAVA_CHARACTER);
		presenter.setSizeConstraints(60, 10, true, true);
		return presenter;
	}

	/*
	 * @see SourceViewerConfiguration#getInformationPresenter(ISourceViewer)
	 * @since 2.0
	 */
	// public IInformationPresenter getInformationPresenter(ISourceViewer
	// sourceViewer) {
	// InformationPresenter presenter= new
	// InformationPresenter(getInformationPresenterControlCreator(sourceViewer));
	// IInformationProvider provider= new JavaInformationProvider(getEditor());
	// presenter.setInformationProvider(provider,
	// IDocument.DEFAULT_CONTENT_TYPE);
	// presenter.setInformationProvider(provider, IJavaPartitions.JAVA_DOC);
	// presenter.setSizeConstraints(60, 10, true, true);
	// return presenter;
	// }
	/**
	 * Returns the information presenter control creator. The creator is a
	 * factory creating the presenter controls for the given source viewer. This
	 * implementation always returns a creator for
	 * <code>DefaultInformationControl</code> instances.
	 * 
	 * @param sourceViewer
	 *            the source viewer to be configured by this configuration
	 * @return an information control creator
	 * @since 2.1
	 */
	private IInformationControlCreator getInformationPresenterControlCreator(
			ISourceViewer sourceViewer) {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				int shellStyle = SWT.RESIZE;
				int style = SWT.V_SCROLL | SWT.H_SCROLL;
				return new DefaultInformationControl(parent, shellStyle, style,
						new HTMLTextPresenter(false));
				// return new HoverBrowserControl(parent);
			}
		};
	}

	/**
	 * Returns the outline presenter control creator. The creator is a factory
	 * creating outline presenter controls for the given source viewer. This
	 * implementation always returns a creator for
	 * <code>JavaOutlineInformationControl</code> instances.
	 * 
	 * @param sourceViewer
	 *            the source viewer to be configured by this configuration
	 * @return an information control creator
	 * @since 2.1
	 */
	private IInformationControlCreator getOutlinePresenterControlCreator(
			ISourceViewer sourceViewer) {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				int shellStyle = SWT.RESIZE;
				int treeStyle = SWT.V_SCROLL | SWT.H_SCROLL;
				return new JavaOutlineInformationControl(parent, shellStyle,
						treeStyle);
			}
		};
	}

	/**
	 * Returns the outline presenter which will determine and shown information
	 * requested for the current cursor position.
	 * 
	 * @param sourceViewer
	 *            the source viewer to be configured by this configuration
	 * @param doCodeResolve
	 *            a boolean which specifies whether code resolve should be used
	 *            to compute the Java element
	 * @return an information presenter
	 * @since 2.1
	 */
	public IInformationPresenter getOutlinePresenter(
			ISourceViewer sourceViewer, boolean doCodeResolve) {
		InformationPresenter presenter = new InformationPresenter(
				getOutlinePresenterControlCreator(sourceViewer));
		presenter.setAnchor(InformationPresenter.ANCHOR_GLOBAL);
		IInformationProvider provider = new JavaElementProvider(getEditor(),
				doCodeResolve);
		presenter.setInformationProvider(provider,
				IDocument.DEFAULT_CONTENT_TYPE);
		presenter.setInformationProvider(provider,
				PHPDocumentPartitioner.PHP_SCRIPT_CODE);
		presenter.setInformationProvider(provider,
				IPHPPartitions.PHP_PARTITIONING);
		presenter.setInformationProvider(provider,
				IPHPPartitions.PHP_PHPDOC_COMMENT);
		presenter.setInformationProvider(provider,
				IPHPPartitions.SMARTY_MULTILINE_COMMENT);
		presenter.setInformationProvider(provider, IPHPPartitions.HTML);
		presenter.setInformationProvider(provider,
				IPHPPartitions.HTML_MULTILINE_COMMENT);
		presenter.setSizeConstraints(40, 20, true, false);
		return presenter;
	}
}