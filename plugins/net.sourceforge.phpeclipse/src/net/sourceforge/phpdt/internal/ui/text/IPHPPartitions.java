/*
 * Created on 05.03.2003
 *
 */
package net.sourceforge.phpdt.internal.ui.text;

/**
 * @author Stefan Langer (musk)
 * 
 */
public interface IPHPPartitions {
	public final static String PHP_PARTITIONING = "___php_partitioning"; //$NON-NLS-1$

	public final static String PHP_PHPDOC_COMMENT = "__php_phpdoc_comment"; //$NON-NLS-1$

	public final static String PHP_SINGLELINE_COMMENT = "__php_singleline_comment"; //$NON-NLS-1$

	public final static String PHP_MULTILINE_COMMENT = "__php_multiline_comment"; //$NON-NLS-1$

	public final static String PHP_STRING_DQ = "__php_string"; //$NON-NLS-1$

	public final static String PHP_STRING_SQ = "__php_string_sq"; //$NON-NLS-1$

	public final static String PHP_STRING_HEREDOC = "__php_string_heredoc"; //$NON-NLS-1$

	public final static String JAVASCRIPT = "__javascript"; //$NON-NLS-1$

	public final static String JS_MULTILINE_COMMENT = "__js_multiline_comment"; //$NON-NLS-1$

	public final static String CSS = "__css"; //$NON-NLS-1$

	public final static String CSS_MULTILINE_COMMENT = "__css_multiline_comment"; //$NON-NLS-1$

	public final static String HTML = "__html"; //$NON-NLS-1$

	public final static String HTML_MULTILINE_COMMENT = "__html_multiline_comment"; //$NON-NLS-1$

	public final static String SMARTY = "__smarty"; //$NON-NLS-1$

	public final static String SMARTY_MULTILINE_COMMENT = "__smarty_multiline_comment"; //$NON-NLS-1$

	public final static int PHP_FILE = 1;

	public final static int HTML_FILE = 2;

	public final static int XML_FILE = 3;

	public final static int SMARTY_FILE = 4;
}
