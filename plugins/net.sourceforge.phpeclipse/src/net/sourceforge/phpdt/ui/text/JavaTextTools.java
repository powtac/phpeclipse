package net.sourceforge.phpdt.ui.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import net.sourceforge.phpdt.internal.ui.text.FastJavaPartitionScanner;
import net.sourceforge.phpdt.internal.ui.text.IPHPPartitions;
import net.sourceforge.phpdt.internal.ui.text.JavaColorManager;
import net.sourceforge.phpdt.internal.ui.text.phpdoc.PHPDocCodeScanner;
import net.sourceforge.phpeclipse.IPreferenceConstants;
import net.sourceforge.phpeclipse.phpeditor.php.HTMLPartitionScanner;
import net.sourceforge.phpeclipse.phpeditor.php.PHPCodeScanner;
import net.sourceforge.phpeclipse.phpeditor.php.PHPDocumentPartitioner;
import net.sourceforge.phpeclipse.phpeditor.php.PHPPartitionScanner;
import net.sourceforge.phpeclipse.phpeditor.php.SmartyCodeScanner;
import net.sourceforge.phpeclipse.phpeditor.php.SmartyDocCodeScanner;
import net.sourceforge.phpeclipse.xml.ui.XMLPlugin;
import net.sourceforge.phpeclipse.xml.ui.text.XMLTextTools;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.DefaultPartitioner;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

//
// import org.phpeclipse.phpdt.internal.ui.text.FastJavaPartitionScanner;
// import org.phpeclipse.phpdt.internal.ui.text.JavaColorManager;
// import org.phpeclipse.phpdt.internal.ui.text.JavaPartitionScanner;
// import org.phpeclipse.phpdt.internal.ui.text.SingleTokenJavaScanner;
// import org.phpeclipse.phpdt.internal.ui.text.php.JavaCodeScanner;
// import org.phpeclipse.phpdt.internal.ui.text.phpdoc.JavaDocScanner;

/**
 * Tools required to configure a Java text viewer. The color manager and all
 * scanner exist only one time, i.e. the same instances are returned to all
 * clients. Thus, clients share those tools.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class JavaTextTools implements IPHPPartitions {
	// private static final String[] TOKENS = {
	// JSPScriptScanner.JSP_DEFAULT,
	// JSPScriptScanner.JSP_BRACKET };
	private final static String[] LEGAL_CONTENT_TYPES = new String[] {
			PHP_PHPDOC_COMMENT, PHP_MULTILINE_COMMENT, PHP_SINGLELINE_COMMENT,
			PHP_STRING_DQ, PHP_STRING_SQ, PHP_STRING_HEREDOC };

	// private static XMLPartitionScanner HTML_PARTITION_SCANNER = null;

	// private static FastJavaPartitionScanner PHP_PARTITION_SCANNER = null;

	private static HTMLPartitionScanner SMARTY_PARTITION_SCANNER = null;

	// private static XMLPartitionScanner XML_PARTITION_SCANNER = null;

	// private final static String[] TYPES= new String[] {
	// PHPPartitionScanner.PHP, PHPPartitionScanner.JAVA_DOC,
	// PHPPartitionScanner.JAVA_MULTILINE_COMMENT };
	// private final static String[] TYPES = new String[] {
	// IPHPPartitions.PHP_PARTITIONING,
	// IPHPPartitions.PHP_PHPDOC_COMMENT,
	// // IPHPPartitions.HTML,
	// // IPHPPartitions.HTML_MULTILINE_COMMENT,
	// IPHPPartitions.JAVASCRIPT,
	// IPHPPartitions.CSS,
	// IPHPPartitions.SMARTY,
	// IPHPPartitions.SMARTY_MULTILINE_COMMENT };

	/**
	 * This tools' preference listener.
	 */
	private class PreferenceListener implements IPropertyChangeListener,
			Preferences.IPropertyChangeListener {
		public void propertyChange(PropertyChangeEvent event) {
			adaptToPreferenceChange(event);
		}

		public void propertyChange(Preferences.PropertyChangeEvent event) {
			adaptToPreferenceChange(new PropertyChangeEvent(event.getSource(),
					event.getProperty(), event.getOldValue(), event
							.getNewValue()));
		}
	};

	// /** The color manager */
	private JavaColorManager colorManager;

	/** The PHP source code scanner */
	private PHPCodeScanner fCodeScanner;

	/** The PHP multiline comment scanner */
	private SingleTokenPHPScanner fMultilineCommentScanner;

	/** The Java singleline comment scanner */
	private SingleTokenPHPScanner fSinglelineCommentScanner;

	/** The PHP double quoted string scanner */
	// private SingleTokenPHPScanner fStringDQScanner;
	/** The PHP single quoted string scanner */
	// private SingleTokenPHPScanner fStringSQScanner;
	/** The PHPDoc scanner */
	private PHPDocCodeScanner fPHPDocScanner;

	/** The HTML scanner */
	// private HTMLCodeScanner fHTMLScanner;
	/** The Smarty scanner */
	private SmartyCodeScanner fSmartyScanner;

	/** The SmartyDoc scanner */
	private SmartyDocCodeScanner fSmartyDocScanner;

	/** The Java partitions scanner. */
	private FastJavaPartitionScanner fPartitionScanner;

	/** The preference store */
	private IPreferenceStore fPreferenceStore;

	/** The XML Language text tools */
	private XMLTextTools xmlTextTools;

	/**
	 * The core preference store.
	 * 
	 * @since 2.1
	 */
	private Preferences fCorePreferenceStore;

	/** The preference change listener */
	private PreferenceListener fPreferenceListener = new PreferenceListener();

	/** The JSP partitions scanner */
	private PHPPartitionScanner jspPartitionScanner = null;

	/** The JSP script subpartitions scanner */
	// private JSPScriptScanner jspScriptScanner;
	/** The PHP plain text scanner */
	// private RuleBasedScanner jspTextScanner;
	/** The PHP brackets scanner */
	// private RuleBasedScanner jspBracketScanner;
	/**
	 * Creates a new Java text tools collection.
	 * 
	 * @param store
	 *            the preference store to initialize the text tools. The text
	 *            tool instance installs a listener on the passed preference
	 *            store to adapt itself to changes in the preference store. In
	 *            general <code>PreferenceConstants.
	 *			getPreferenceStore()</code>
	 *            should be used to initialize the text tools.
	 * @param coreStore
	 *            optional preference store to initialize the text tools. The
	 *            text tool instance installs a listener on the passed
	 *            preference store to adapt itself to changes in the preference
	 *            store.
	 * @see net.sourceforge.phpdt.ui.PreferenceConstants#getPreferenceStore()
	 * @since 2.1
	 */
	public JavaTextTools(IPreferenceStore store, Preferences coreStore) {
		this(store, coreStore, true);
	}

	/**
	 * Creates a new Java text tools collection.
	 * 
	 * @param store
	 *            the preference store to initialize the text tools. The text
	 *            tool instance installs a listener on the passed preference
	 *            store to adapt itself to changes in the preference store. In
	 *            general <code>PreferenceConstants.
	 *			getPreferenceStore()</code>
	 *            shoould be used to initialize the text tools.
	 * @param coreStore
	 *            optional preference store to initialize the text tools. The
	 *            text tool instance installs a listener on the passed
	 *            preference store to adapt itself to changes in the preference
	 *            store.
	 * @param autoDisposeOnDisplayDispose
	 *            if <code>true</code> the color manager automatically
	 *            disposes all managed colors when the current display gets
	 *            disposed and all calls to
	 *            {@link org.eclipse.jface.text.source.ISharedTextColors#dispose()}are
	 *            ignored.
	 * @see net.sourceforge.phpdt.ui.PreferenceConstants#getPreferenceStore()
	 * @since 2.1
	 */
	public JavaTextTools(IPreferenceStore store, Preferences coreStore,
			boolean autoDisposeOnDisplayDispose) {
		// super(store, TOKENS, );
		// REVISIT: preference store
		xmlTextTools = new XMLTextTools(XMLPlugin.getDefault()
				.getPreferenceStore());

		colorManager = new JavaColorManager(autoDisposeOnDisplayDispose);
		fPreferenceStore = store;
		fPreferenceStore.addPropertyChangeListener(fPreferenceListener);

		fCorePreferenceStore = coreStore;
		if (fCorePreferenceStore != null)
			fCorePreferenceStore.addPropertyChangeListener(fPreferenceListener);

		fCodeScanner = new PHPCodeScanner((JavaColorManager) colorManager,
				store);
		fMultilineCommentScanner = new SingleTokenPHPScanner(
				(JavaColorManager) colorManager, store,
				IPreferenceConstants.PHP_MULTILINE_COMMENT);
		fSinglelineCommentScanner = new SingleTokenPHPScanner(
				(JavaColorManager) colorManager, store,
				IPreferenceConstants.PHP_SINGLELINE_COMMENT);
		// fStringDQScanner = new SingleTokenPHPScanner((JavaColorManager)
		// colorManager, store, IPreferenceConstants.PHP_STRING);
		// fStringSQScanner = new SingleTokenPHPScanner((JavaColorManager)
		// colorManager, store, IPreferenceConstants.PHP_STRING);

		fPHPDocScanner = new PHPDocCodeScanner((JavaColorManager) colorManager,
				store);
		// fHTMLScanner = new HTMLCodeScanner((JavaColorManager)fColorManager,
		// store);
		fSmartyScanner = new SmartyCodeScanner((JavaColorManager) colorManager,
				store);
		fSmartyDocScanner = new SmartyDocCodeScanner(
				(JavaColorManager) colorManager, store);

		fPartitionScanner = new FastJavaPartitionScanner();

		// jspScriptScanner = new JSPScriptScanner();
		// fPartitionScanner = new FastJavaPartitionScanner();
		// fPartitionScanner = new PHPPartitionScanner();

		// jspBracketScanner = new RuleBasedScanner();
		// jspBracketScanner.setDefaultReturnToken(new
		// Token(JSPScriptScanner.JSP_BRACKET));
		// jspTextScanner = new RuleBasedScanner();
		// jspTextScanner.setDefaultReturnToken(new
		// Token(JSPScriptScanner.JSP_DEFAULT));
	}

	/**
	 * 
	 */
	public XMLTextTools getXMLTextTools() {
		return xmlTextTools;
	}

	/**
	 * Disposes all the individual tools of this tools collection.
	 */
	public void dispose() {

		fCodeScanner = null;
		fMultilineCommentScanner = null;
		fSinglelineCommentScanner = null;
		// fStringDQScanner = null;
		// fStringSQScanner = null;
		fPHPDocScanner = null;
		// fPartitionScanner = null;

		if (colorManager != null) {
			colorManager.dispose();
			colorManager = null;
		}

		if (fPreferenceStore != null) {
			fPreferenceStore.removePropertyChangeListener(fPreferenceListener);
			fPreferenceStore = null;

			if (fCorePreferenceStore != null) {
				fCorePreferenceStore
						.removePropertyChangeListener(fPreferenceListener);
				fCorePreferenceStore = null;
			}

			fPreferenceListener = null;
		}
	}

	/**
	 * Returns the color manager which is used to manage any Java-specific
	 * colors needed for such things like syntax highlighting.
	 * 
	 * @return the color manager to be used for Java text viewers
	 */
	public JavaColorManager getColorManager() {
		return (JavaColorManager) colorManager;
	}

	/**
	 * Returns a scanner which is configured to scan Java source code.
	 * 
	 * @return a Java source code scanner
	 */
	public RuleBasedScanner getCodeScanner() {
		return fCodeScanner;
	}

	/**
	 * Returns a scanner which is configured to scan Java multiline comments.
	 * 
	 * @return a Java multiline comment scanner
	 * 
	 * @since 2.0
	 */
	public RuleBasedScanner getMultilineCommentScanner() {
		return fMultilineCommentScanner;
	}

	/**
	 * Returns a scanner which is configured to scan HTML code.
	 * 
	 * @return a HTML scanner
	 * 
	 * @since 2.0
	 */
	// public RuleBasedScanner getHTMLScanner() {
	// return fHTMLScanner;
	// }
	/**
	 * Returns a scanner which is configured to scan Smarty code.
	 * 
	 * @return a Smarty scanner
	 * 
	 * @since 2.0
	 */
	public RuleBasedScanner getSmartyScanner() {
		return fSmartyScanner;
	}

	/**
	 * Returns a scanner which is configured to scan Smarty code.
	 * 
	 * @return a Smarty scanner
	 * 
	 * @since 2.0
	 */
	public RuleBasedScanner getSmartyDocScanner() {
		return fSmartyDocScanner;
	}

	/**
	 * Returns a scanner which is configured to scan Java singleline comments.
	 * 
	 * @return a Java singleline comment scanner
	 * 
	 * @since 2.0
	 */
	public RuleBasedScanner getSinglelineCommentScanner() {
		return fSinglelineCommentScanner;
	}

	/**
	 * Returns a scanner which is configured to scan Java strings.
	 * 
	 * @return a Java string scanner
	 * 
	 * @since 2.0
	 */
	// public RuleBasedScanner getStringScanner() {
	// return fStringDQScanner;
	// }
	/**
	 * Returns a scanner which is configured to scan JavaDoc compliant comments.
	 * Notes that the start sequence "/**" and the corresponding end sequence
	 * are part of the JavaDoc comment.
	 * 
	 * @return a JavaDoc scanner
	 */
	public RuleBasedScanner getJavaDocScanner() {
		return fPHPDocScanner;
	}

	/**
	 * Returns a scanner which is configured to scan Java-specific partitions,
	 * which are multi-line comments, JavaDoc comments, and regular Java source
	 * code.
	 * 
	 * @return a Java partition scanner
	 */
	// public IPartitionTokenScanner getPartitionScanner() {
	// return fPartitionScanner;
	// }
	/**
	 * Factory method for creating a PHP-specific document partitioner using
	 * this object's partitions scanner. This method is a convenience method.
	 * 
	 * @return a newly created Java document partitioner
	 */
	public IDocumentPartitioner createDocumentPartitioner() {
		return createDocumentPartitioner(".php");
	}

	/**
	 * Factory method for creating a PHP-specific document partitioner using
	 * this object's partitions scanner. This method is a convenience method.
	 * 
	 * @return a newly created Java document partitioner
	 */
	public IDocumentPartitioner createDocumentPartitioner(String extension) {

		// String[] types =
		// new String[] {
		// FastJavaPartitionScanner.JAVA_DOC,
		// FastJavaPartitionScanner.JAVA_MULTI_LINE_COMMENT,
		// FastJavaPartitionScanner.JAVA_SINGLE_LINE_COMMENT,
		// FastJavaPartitionScanner.JAVA_STRING };
		//
		// return new DefaultPartitioner(getPartitionScanner(), types);
		IDocumentPartitioner partitioner = null;
		// System.out.println(extension);
		if (extension.equalsIgnoreCase(".html")
				|| extension.equalsIgnoreCase(".htm")) {
			// html
			partitioner = createHTMLPartitioner();
			partitioner = createJSPPartitioner();
		} else if (extension.equalsIgnoreCase(".xml")) {
			// xml
			partitioner = createXMLPartitioner();
			// } else if (extension.equalsIgnoreCase(".js")) {
			// // javascript
			// partitioner = createJavaScriptPartitioner();
			// } else if (extension.equalsIgnoreCase(".css")) {
			// // cascading style sheets
			// partitioner = createCSSPartitioner();
		} else if (extension.equalsIgnoreCase(".tpl")) {
			// smarty ?
			partitioner = createSmartyPartitioner();
			// } else if (extension.equalsIgnoreCase(".inc")) {
			// // php include files ?
			// partitioner = createIncludePartitioner();
		}

		if (partitioner == null) {
			partitioner = createJSPPartitioner();
		}

		return partitioner;
	}

	/**
	 * Sets up the Java document partitioner for the given document for the
	 * given partitioning.
	 * 
	 * @param document
	 *            the document to be set up
	 * @param partitioning
	 *            the document partitioning
	 * @param element
	 *            TODO
	 * 
	 * @since 3.0
	 */
	// public void setupJavaDocumentPartitioner(IDocument document, String
	// partitioning, Object element) {
	// IDocumentPartitioner partitioner = createDocumentPartitioner(".php");
	//
	// // if (document instanceof IDocumentExtension3) {
	// // IDocumentExtension3 extension3= (IDocumentExtension3) document;
	// // extension3.setDocumentPartitioner(partitioning, partitioner);
	// // } else {
	// document.setDocumentPartitioner(partitioner);
	// // }
	// partitioner.connect(document);
	// }
	public void setupHTMLDocumentPartitioner(IDocument document,
			String partitioning, Object element) {
		IDocumentPartitioner partitioner = createDocumentPartitioner(".html");

		// if (document instanceof IDocumentExtension3) {
		// IDocumentExtension3 extension3= (IDocumentExtension3) document;
		// extension3.setDocumentPartitioner(partitioning, partitioner);
		// } else {
		document.setDocumentPartitioner(partitioner);
		// }
		partitioner.connect(document);
	}

	public void setupSmartyDocumentPartitioner(IDocument document,
			String partitioning, Object element) {
		IDocumentPartitioner partitioner = createDocumentPartitioner(".tpl");

		// if (document instanceof IDocumentExtension3) {
		// IDocumentExtension3 extension3= (IDocumentExtension3) document;
		// extension3.setDocumentPartitioner(partitioning, partitioner);
		// } else {
		document.setDocumentPartitioner(partitioner);
		// }
		partitioner.connect(document);
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

	/**
	 * Determines whether the preference change encoded by the given event
	 * changes the behavior of one its contained components.
	 * 
	 * @param event
	 *            the event to be investigated
	 * @return <code>true</code> if event causes a behavioral change
	 * @since 2.0
	 * @deprecated As of 3.0, replaced by
	 *             {@link net.sourceforge.phpdt.ui.text.JavaSourceViewerConfiguration#affectsTextPresentation(PropertyChangeEvent)}
	 */
	// public boolean affectsBehavior(PropertyChangeEvent event) {
	// return fCodeScanner.affectsBehavior(event)
	// || fMultilineCommentScanner.affectsBehavior(event)
	// || fSinglelineCommentScanner.affectsBehavior(event)
	// || fStringDQScanner.affectsBehavior(event)
	// || fPHPDocScanner.affectsBehavior(event);
	// }
	/**
	 * Adapts the behavior of the contained components to the change encoded in
	 * the given event.
	 * 
	 * @param event
	 *            the event to which to adapt
	 * @since 2.0
	 */
	protected void adaptToPreferenceChange(PropertyChangeEvent event) {
		if (fCodeScanner.affectsBehavior(event))
			fCodeScanner.adaptToPreferenceChange(event);
		if (fMultilineCommentScanner.affectsBehavior(event))
			fMultilineCommentScanner.adaptToPreferenceChange(event);
		if (fSinglelineCommentScanner.affectsBehavior(event))
			fSinglelineCommentScanner.adaptToPreferenceChange(event);
		// if (fStringDQScanner.affectsBehavior(event))
		// fStringDQScanner.adaptToPreferenceChange(event);
		if (fPHPDocScanner.affectsBehavior(event))
			fPHPDocScanner.adaptToPreferenceChange(event);
		// if (fHTMLScanner.affectsBehavior(event))
		// fHTMLScanner.adaptToPreferenceChange(event);
		if (fSmartyScanner.affectsBehavior(event))
			fSmartyScanner.adaptToPreferenceChange(event);
		if (fSmartyDocScanner.affectsBehavior(event))
			fSmartyDocScanner.adaptToPreferenceChange(event);
		// if (XMLPlugin.getDefault().getXMLTextTools().affectsBehavior(event))
		// {
		// XMLPlugin.getDefault().getXMLTextTools().adaptToPreferenceChange(event);
		// }
	}

	/**
	 * Return a partitioner for .html files.
	 */
	public IDocumentPartitioner createHTMLPartitioner() {
		// return new DefaultPartitioner(getHTMLPartitionScanner(), TYPES);
		return xmlTextTools.createXMLPartitioner();
	}

	// private static IDocumentPartitioner createIncludePartitioner() {
	// // return new DefaultPartitioner(getPHPPartitionScanner(), TYPES);
	// return new DefaultPartitioner(getPHPPartitionScanner(),
	// FastJavaPartitionScanner.PHP_PARTITION_TYPES);
	//
	// }

	// private static IDocumentPartitioner createJavaScriptPartitioner() {
	// return new DefaultPartitioner(getHTMLPartitionScanner(), TYPES);
	// }

	/**
	 * Return a partitioner for .php files.
	 */
	public IDocumentPartitioner createPHPPartitioner() {
		// return new DefaultPartitioner(getPHPPartitionScanner(), TYPES);
		return new DefaultPartitioner(getPHPPartitionScanner(),
				LEGAL_CONTENT_TYPES);
	}

	private IDocumentPartitioner createJSPPartitioner() {
		return new PHPDocumentPartitioner(getJSPPartitionScanner());
		// return new JSPDocumentPartitioner(getJSPPartitionScanner(),
		// jspScriptScanner);
	}

	/**
	 * 
	 */
	// public IPartitionTokenScanner getJSPScriptScanner() {
	// return jspScriptScanner;
	// }
	private IDocumentPartitioner createSmartyPartitioner() {
		return new DefaultPartitioner(getSmartyPartitionScanner(),
				XMLTextTools.TYPES);
	}

	private IDocumentPartitioner createXMLPartitioner() {
		// return new DefaultPartitioner(getXMLPartitionScanner(),
		// XMLTextTools.TYPES);
		return xmlTextTools.createXMLPartitioner();
	}

	// private IDocumentPartitioner createCSSPartitioner() {
	// return new DefaultPartitioner(getHTMLPartitionScanner(),
	// XMLTextTools.TYPES);
	// }

	/**
	 * Return a scanner for creating html partitions.
	 */
	// private static XMLPartitionScanner getHTMLPartitionScanner() {
	// // if (HTML_PARTITION_SCANNER == null)
	// // HTML_PARTITION_SCANNER = new
	// HTMLPartitionScanner(IPHPPartitions.HTML_FILE);
	// // return HTML_PARTITION_SCANNER;^
	// if (HTML_PARTITION_SCANNER == null)
	// HTML_PARTITION_SCANNER = new XMLPartitionScanner(false);
	// return HTML_PARTITION_SCANNER;
	// }
	/**
	 * Return a scanner for creating php partitions.
	 */
	private FastJavaPartitionScanner getPHPPartitionScanner() {
		// if (PHP_PARTITION_SCANNER == null)
		// PHP_PARTITION_SCANNER = new FastJavaPartitionScanner(); //new
		// PHPPartitionScanner(IPHPPartitions.PHP_FILE);
		// return PHP_PARTITION_SCANNER;
		return fPartitionScanner;
	}

	/**
	 * Returns a scanner which is configured to scan plain text in JSP.
	 * 
	 * @return a JSP text scanner
	 */
	// public RuleBasedScanner getJSPTextScanner() {
	// return jspTextScanner;
	// }
	/**
	 * Returns a scanner which is configured to scan plain text in JSP.
	 * 
	 * @return a JSP text scanner
	 */
	// public RuleBasedScanner getJSPBracketScanner() {
	// return jspBracketScanner;
	// }
	/**
	 * Return a scanner for creating smarty partitions.
	 */
	private static HTMLPartitionScanner getSmartyPartitionScanner() {
		if (SMARTY_PARTITION_SCANNER == null)
			SMARTY_PARTITION_SCANNER = new HTMLPartitionScanner(
					IPHPPartitions.SMARTY_FILE);
		return SMARTY_PARTITION_SCANNER;
	}

	/**
	 * Return a scanner for creating xml partitions.
	 */
	// private static XMLPartitionScanner getXMLPartitionScanner() {
	// // if (XML_PARTITION_SCANNER == null)
	// // XML_PARTITION_SCANNER = new
	// HTMLPartitionScanner(IPHPPartitions.XML_FILE);
	// // return XML_PARTITION_SCANNER;
	// if (XML_PARTITION_SCANNER == null)
	// XML_PARTITION_SCANNER = new XMLPartitionScanner(false);
	// return XML_PARTITION_SCANNER;
	// }
	private PHPPartitionScanner getJSPPartitionScanner() {
		if (jspPartitionScanner == null)
			jspPartitionScanner = new PHPPartitionScanner();
		return jspPartitionScanner;
	}

	/**
	 * Sets up the Java document partitioner for the given document for the
	 * default partitioning.
	 * 
	 * @param document
	 *            the document to be set up
	 * @since 3.0
	 */
	public void setupJavaDocumentPartitioner(IDocument document) {
		setupJavaDocumentPartitioner(document,
				IDocumentExtension3.DEFAULT_PARTITIONING);
	}

	/**
	 * Sets up the Java document partitioner for the given document for the
	 * given partitioning.
	 * 
	 * @param document
	 *            the document to be set up
	 * @param partitioning
	 *            the document partitioning
	 * @since 3.0
	 */
	public void setupJavaDocumentPartitioner(IDocument document,
			String partitioning) {
		IDocumentPartitioner partitioner = createDocumentPartitioner();
		if (document instanceof IDocumentExtension3) {
			IDocumentExtension3 extension3 = (IDocumentExtension3) document;
			extension3.setDocumentPartitioner(partitioning, partitioner);
		} else {
			document.setDocumentPartitioner(partitioner);
		}
		partitioner.connect(document);
	}

	/**
	 * Returns this text tool's preference store.
	 * 
	 * @return the preference store
	 * @since 3.0
	 */
	protected IPreferenceStore getPreferenceStore() {
		return fPreferenceStore;
	}

	/**
	 * Returns this text tool's core preference store.
	 * 
	 * @return the core preference store
	 * @since 3.0
	 */
	protected Preferences getCorePreferenceStore() {
		return fCorePreferenceStore;
	}
}