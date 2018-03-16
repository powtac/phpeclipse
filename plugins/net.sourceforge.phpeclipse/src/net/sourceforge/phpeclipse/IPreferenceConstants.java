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
package net.sourceforge.phpeclipse;

/**
 * Predefined prference constants
 */
public interface IPreferenceConstants {
	/**
	 * Preference key suffix for bold text style preference keys.
	 * 
	 * @since 2.1
	 */
	public static final String EDITOR_BOLD_SUFFIX = "_bold"; //$NON-NLS-1$

	/**
	 * Preference key suffix for italic text style preference keys.
	 * 
	 * @since 3.0
	 */
	public static final String EDITOR_ITALIC_SUFFIX = "_italic"; //$NON-NLS-1$

	// public static final String LOCALHOST_PREF = "_localhost";
	// public static final String DOCUMENTROOT_PREF = "_documentroot";
	// public static final String XAMPP_START_PREF = "_xampp_start_pref";
	//
	// public static final String XAMPP_STOP_PREF = "_xampp_stop_pref";
	//
	// public static final String MYSQL_RUN_PREF = "_mysql_run_pref";
	//
	// public static final String MYSQL_START_BACKGROUND =
	// "_mysql_start_background";
	//
	// public static final String MYSQL_PREF = "__mysql_start";
	//
	// public static final String APACHE_RUN_PREF = "_apache_run_pref";
	//
	// public static final String APACHE_START_BACKGROUND =
	// "_apache_start_background";
	//
	// public static final String APACHE_START_PREF = "__apache_start";
	//
	// public static final String APACHE_STOP_BACKGROUND =
	// "_apache_stop_background";
	//
	// public static final String APACHE_STOP_PREF = "__apache_stop";
	//
	// public static final String APACHE_RESTART_BACKGROUND =
	// "_apache_restart_background";
	//
	// public static final String APACHE_RESTART_PREF = "__apache_restart";

	// public static final String HTTPD_CONF_PATH_PREF = "__httpd_conf_path";
	//
	// public static final String ETC_HOSTS_PATH_PREF = "__etc_hosts_path";

	// public static final String SHOW_OUTPUT_IN_CONSOLE =
	// "_show_output_in_console";

	// public static final String PHP_RUN_PREF = "_php_run_pref";
	//
	// public static final String EXTERNAL_PARSER_PREF = "_external_parser";

	// public static final String PHP_EXTENSION_PREFS =
	// "_php_parser_extensions";

	// public static final String PHP_PARSER_DEFAULT = "_php_parser_default";

	// public static final String PHP_INTERNAL_PARSER = "_php_internal_parser";
	// public static final String PHP_EXTERNAL_PARSER = "_php_external_parser";
	// public static final String PHP_PARSE_ON_SAVE = "_php_parse_on_save";
	public static final String PHP_MULTILINE_COMMENT = "_php_multilineComment";

	public static final String PHP_MULTILINE_COMMENT_BOLD = "_php_multilineComment_bold";

	public static final String PHP_MULTILINE_COMMENT_ITALIC = "_php_multilineComment_italic";

	public static final String PHP_MULTILINE_COMMENT_UNDERLINE = "_php_multilineComment_underline";

	/**
	 * The color key for operators and brackets in PHP code (value
	 * <code>"__php_operator"</code>).
	 * 
	 * @since 3.0
	 */
	public static final String PHP_OPERATOR = "__php_operator"; //$NON-NLS-1$

	/**
	 * The color key for {} in PHP code (value
	 * <code>"__php_brace_operator"</code>).
	 * 
	 * @since 3.0
	 */
	public static final String PHP_BRACE_OPERATOR = "__php_brace_operator"; //$NON-NLS-1$

	/**
	 * A named preference that holds the color used to render operators and
	 * brackets.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 3.0
	 */
	public final static String EDITOR_PHP_OPERATOR_COLOR = PHP_OPERATOR;

	/**
	 * A named preference that controls whether operators and brackets are
	 * rendered in bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public final static String EDITOR_PHP_OPERATOR_BOLD = PHP_OPERATOR
			+ EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether operators and brackets are
	 * rendered in italic.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public final static String EDITOR_PHP_OPERATOR_ITALIC = PHP_OPERATOR
			+ EDITOR_ITALIC_SUFFIX;

	/**
	 * A named preference that holds the color used to render operators and
	 * brackets.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 3.0
	 */
	public final static String EDITOR_PHP_BRACE_OPERATOR_COLOR = PHP_BRACE_OPERATOR;

	/**
	 * A named preference that controls whether operators and brackets are
	 * rendered in bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public final static String EDITOR_PHP_BRACE_OPERATOR_BOLD = PHP_BRACE_OPERATOR
			+ EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether operators and brackets are
	 * rendered in italic.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public final static String EDITOR_PHP_BRACE_OPERATOR_ITALIC = PHP_BRACE_OPERATOR
			+ EDITOR_ITALIC_SUFFIX;

	/**
	 * The color key for keyword 'return' in PHP code (value
	 * <code>"__php_keyword_return"</code>).
	 * 
	 * @since 3.0
	 */
	public static final String PHP_KEYWORD_RETURN = "__php_keyword_return"; //$NON-NLS-1$

	/**
	 * A named preference that holds the color used to render the 'return'
	 * keyword.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 3.0
	 */
	public final static String EDITOR_PHP_KEYWORD_RETURN_COLOR = PHP_KEYWORD_RETURN;

	/**
	 * A named preference that controls whether 'return' keyword is rendered in
	 * bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public final static String EDITOR_PHP_KEYWORD_RETURN_BOLD = PHP_KEYWORD_RETURN
			+ EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether 'return' keyword is rendered in
	 * italic.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public final static String EDITOR_PHP_KEYWORD_RETURN_ITALIC = PHP_KEYWORD_RETURN
			+ EDITOR_ITALIC_SUFFIX;

	public static final String PHP_SINGLELINE_COMMENT = "_php_singlelineComment";

	public static final String PHP_SINGLELINE_COMMENT_BOLD = "_php_singlelineComment_bold";//$NON-NLS-1$

	public static final String PHP_SINGLELINE_COMMENT_ITALIC = "_php_singlelineComment_italic";//$NON-NLS-1$

	public static final String PHP_SINGLELINE_COMMENT_UNDERLINE = "_php_singlelineComment_underline";//$NON-NLS-1$

	public static final String PHP_TAG = "_php_tag";//$NON-NLS-1$

	public static final String PHP_TAG_BOLD = "_php_tag_bold";//$NON-NLS-1$

	public static final String PHP_TAG_ITALIC = "_php_tag_italic";//$NON-NLS-1$

	public static final String PHP_TAG_UNDERLINE = "_php_tag_underline";//$NON-NLS-1$

	public static final String PHP_KEYWORD = "_php_keyword";//$NON-NLS-1$

	public static final String PHP_KEYWORD_BOLD = "_php_keyword_bold";//$NON-NLS-1$

	public static final String PHP_KEYWORD_ITALIC = "_php_keyword_italic";//$NON-NLS-1$

	public static final String PHP_KEYWORD_UNDERLINE = "_php_keyword_underline";//$NON-NLS-1$

	public static final String PHP_VARIABLE = "_php_variable";//$NON-NLS-1$

	public static final String PHP_VARIABLE_BOLD = "_php_variable_bold";//$NON-NLS-1$

	public static final String PHP_VARIABLE_ITALIC = "_php_variable_italic";//$NON-NLS-1$

	public static final String PHP_VARIABLE_UNDERLINE = "_php_variable_underline";//$NON-NLS-1$

	public static final String PHP_VARIABLE_DOLLAR = "_php_variable_dollar";//$NON-NLS-1$

	public static final String PHP_VARIABLE_DOLLAR_BOLD = "_php_variable_dollar_bold";//$NON-NLS-1$

	public static final String PHP_VARIABLE_DOLLAR_ITALIC = "_php_variable_dollar_italic";//$NON-NLS-1$

	public static final String PHP_VARIABLE_DOLLAR_UNDERLINE = "_php_variable_dollar_underline";//$NON-NLS-1$

	public static final String PHP_TYPE = "_php_type";//$NON-NLS-1$

	public static final String PHP_TYPE_BOLD = "_php_type_bold";//$NON-NLS-1$

	public static final String PHP_TYPE_ITALIC = "_php_type_italic";//$NON-NLS-1$

	public static final String PHP_TYPE_UNDERLINE = "_php_type_underline";//$NON-NLS-1$

	public static final String PHP_CONSTANT = "_php_constant";//$NON-NLS-1$

	public static final String PHP_CONSTANT_BOLD = "_php_constant_bold";//$NON-NLS-1$

	public static final String PHP_CONSTANT_ITALIC = "_php_constant_italic";//$NON-NLS-1$

	public static final String PHP_CONSTANT_UNDERLINE = "_php_constant_underline";//$NON-NLS-1$

	public static final String PHP_FUNCTIONNAME = "_php_functionname";//$NON-NLS-1$

	public static final String PHP_FUNCTIONNAME_BOLD = "_php_functionname_bold";//$NON-NLS-1$

	public static final String PHP_FUNCTIONNAME_ITALIC = "_php_functionname_italic";//$NON-NLS-1$

	public static final String PHP_FUNCTIONNAME_UNDERLINE = "_php_functionname_underline";//$NON-NLS-1$

	public static final String PHP_STRING_DQ = "_php_string";//$NON-NLS-1$

	public static final String PHP_STRING_BOLD_DQ = "_php_string_bold";

	public static final String PHP_STRING_ITALIC_DQ = "_php_string_italic";

	public static final String PHP_STRING_UNDERLINE_DQ = "_php_string_underline";

	public static final String PHP_STRING_SQ = "_php_string_sq";//$NON-NLS-1$

	public static final String PHP_STRING_BOLD_SQ = "_php_string_sq_bold";

	public static final String PHP_STRING_ITALIC_SQ = "_php_string_sq_italic";

	public static final String PHP_STRING_UNDERLINE_SQ = "_php_string_sq_underline";

	public static final String PHP_DEFAULT = "_php_default";

	public static final String PHP_DEFAULT_BOLD = "_php_default_bold";

	public static final String PHP_DEFAULT_ITALIC = "_php_default_italic";

	public static final String PHP_DEFAULT_UNDERLINE = "_php_default_underline";

	public static final String TASK_TAG = "_php_comment_task_tag"; //$NON-NLS-1$

	public static final String TASK_TAG_BOLD = "_php_comment_task_tag_bold"; //$NON-NLS-1$

	/**
	 * The color key for PHPDoc keywords (<code>@foo</code>) in PHPDoc comments.
	 */
	public static final String PHPDOC_KEYWORD = "_php_doc_keyword"; //$NON-NLS-1$

	public static final String PHPDOC_KEYWORD_BOLD = "_php_doc_keyword_bold";

	public static final String PHPDOC_KEYWORD_ITALIC = "_php_doc_keyword_italic";

	public static final String PHPDOC_KEYWORD_UNDERLINE = "_php_doc_keyword_underline";

	/**
	 * The color key for HTML tags (<code>&lt;foo&gt;</code>) in PHPDoc
	 * comments.
	 */
	public static final String PHPDOC_TAG = "_php_doc_tag"; //$NON-NLS-1$

	public static final String PHPDOC_TAG_BOLD = "_php_doc_tag_bold";

	public static final String PHPDOC_TAG_ITALIC = "_php_doc_tag_italic";

	public static final String PHPDOC_TAG_UNDERLINE = "_php_doc_tag_underline";

	/**
	 * The color key for PHPDoc links (<code>{foo}</code>) in PHPDoc
	 * comments.
	 */
	public static final String PHPDOC_LINK = "_php_doc_link"; //$NON-NLS-1$

	public static final String PHPDOC_LINK_BOLD = "_php_doc_link_bold";

	public static final String PHPDOC_LINK_ITALIC = "_php_doc_link_italic";

	public static final String PHPDOC_LINK_UNDERLINE = "_php_doc_link_underline";

	/**
	 * The color key for everthing in PHPDoc comments for which no other color
	 * is specified.
	 */
	public static final String PHPDOC_DEFAULT = "_php_doc_default"; //$NON-NLS-1$

	public static final String PHPDOC_DEFAULT_BOLD = "_php_doc_default_bold";

	public static final String PHPDOC_DEFAULT_ITALIC = "_php_doc_default_italic";

	public static final String PHPDOC_DEFAULT_UNDERLINE = "_php_doc_default_underline";

	// public static final String LINKED_POSITION_COLOR =
	// "_linkedPositionColor";
	// public static final String PHP_EDITOR_BACKGROUND =
	// "_php_editor_background";
	public static final String PHP_USERDEF_XMLFILE = "_userdef_xmlfile";

	/** Preference key for showing the line number ruler */
	// public final static String LINE_NUMBER_RULER = "_lineNumberRuler";
	// //$NON-NLS-1$
	/** Preference key for the foreground color of the line numbers */
	// public final static String LINE_NUMBER_COLOR = "_lineNumberColor";
	// //$NON-NLS-1$
	// public final static String PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT =
	// "_defaultBackgroundColor"; //$NON-NLS-1$
	// public final static String PREFERENCE_COLOR_BACKGROUND =
	// "backgroundColor"; //$NON-NLS-1$
	/** Preference key for content assist proposal color */
	public final static String PROPOSALS_FOREGROUND = "content_assist_proposals_foreground"; //$NON-NLS-1$

	/** Preference key for content assist proposal color */
	public final static String PROPOSALS_BACKGROUND = "content_assist_proposals_background"; //$NON-NLS-1$

	public static final String EDITOR_EVALUTE_TEMPORARY_PROBLEMS = null;

	public static final String EDITOR_CORRECTION_INDICATION = null;

	// public static final String PHP_OBFUSCATOR_DEFAULT =
	// "_php_obfuscator_default";
	// public static final String PHP_BOOKMARK_DEFAULT =
	// "_php_bookmark_default";
	// public static final String PHP_LOCALHOST_PREF = "_php_localhost";
	// public static final String PHP_DOCUMENTROOT_PREF = "_php_documentroot";
	//
	// public static final String PHP_AUTO_PREVIEW_DEFAULT = "_auto_preview";
	// public static final String PHP_BRING_TO_TOP_PREVIEW_DEFAULT =
	// "_bring_to_top_preview";
	// public static final String PHP_SHOW_HTML_FILES_LOCAL =
	// "_show_html_files_local";
}