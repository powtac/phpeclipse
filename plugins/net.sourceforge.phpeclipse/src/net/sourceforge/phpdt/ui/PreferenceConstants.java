/*******************************************************************************
 * Copyright (c) 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package net.sourceforge.phpdt.ui;

import net.sourceforge.phpdt.core.IClasspathEntry;
import net.sourceforge.phpdt.internal.ui.text.spelling.SpellCheckEngine;
import net.sourceforge.phpdt.internal.ui.text.spelling.engine.ISpellCheckPreferenceKeys;
import net.sourceforge.phpeclipse.IPreferenceConstants;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;
import net.sourceforge.phpeclipse.phpeditor.util.PHPColorProvider;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AbstractTextEditor;

//
// import org.phpeclipse.phpdt.internal.ui.JavaPlugin;
// import
// org.phpeclipse.phpdt.internal.ui.preferences.NewJavaProjectPreferencePage;

/**
 * Preference constants used in the JDT-UI preference store. Clients should only
 * read the JDT-UI preference store using these values. Clients are not allowed
 * to modify the preference store programmatically.
 * 
 * @since 2.0
 */
public class PreferenceConstants {

	private PreferenceConstants() {
	}

	/**
	 * A named preference that controls return type rendering of methods in the
	 * UI.
	 * <p>
	 * Value is of type <code>Boolean</code>: if <code>true</code> return
	 * types are rendered
	 * </p>
	 */
	public static final String APPEARANCE_METHOD_RETURNTYPE = "net.sourceforge.phpdt.ui.methodreturntype"; //$NON-NLS-1$

	/**
	 * A named preference that controls if override indicators are rendered in
	 * the UI.
	 * <p>
	 * Value is of type <code>Boolean</code>: if <code>true</code> override
	 * indicators are rendered
	 * </p>
	 */
	public static final String APPEARANCE_OVERRIDE_INDICATOR = "net.sourceforge.phpdt.ui.overrideindicator"; //$NON-NLS-1$

	/**
	 * A named preference that defines the pattern used for package name
	 * compression.
	 * <p>
	 * Value is of type <code>String</code>. For example foe the given
	 * package name 'net.sourceforge.phpdt' pattern '.' will compress it to
	 * '..jdt', '1~' to 'o~.e~.jdt'.
	 * </p>
	 */
	public static final String APPEARANCE_PKG_NAME_PATTERN_FOR_PKG_VIEW = "PackagesView.pkgNamePatternForPackagesView"; //$NON-NLS-1$

	/**
	 * A named preference that controls if package name compression is turned on
	 * or off.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @see #APPEARANCE_PKG_NAME_PATTERN_FOR_PKG_VIEW
	 */
	public static final String APPEARANCE_COMPRESS_PACKAGE_NAMES = "net.sourceforge.phpdt.ui.compresspackagenames"; //$NON-NLS-1$

	/**
	 * A named preference that controls if empty inner packages are folded in
	 * the hierarchical mode of the package explorer.
	 * <p>
	 * Value is of type <code>Boolean</code>: if <code>true</code> empty
	 * inner packages are folded.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public static final String APPEARANCE_FOLD_PACKAGES_IN_PACKAGE_EXPLORER = "net.sourceforge.phpdt.ui.flatPackagesInPackageExplorer"; //$NON-NLS-1$

	/**
	 * A named preference that defines how member elements are ordered by the
	 * Java views using the <code>JavaElementSorter</code>.
	 * <p>
	 * Value is of type <code>String</code>: A comma separated list of the
	 * following entries. Each entry must be in the list, no duplication. List
	 * order defines the sort order.
	 * <ul>
	 * <li><b>T </b>: Types</li>
	 * <li><b>C </b>: Constructors</li>
	 * <li><b>I </b>: Initializers</li>
	 * <li><b>M </b>: Methods</li>
	 * <li><b>F </b>: Fields</li>
	 * <li><b>SI </b>: Static Initializers</li>
	 * <li><b>SM </b>: Static Methods</li>
	 * <li><b>SF </b>: Static Fields</li>
	 * </ul>
	 * </p>
	 * 
	 * @since 2.1
	 */
	public static final String APPEARANCE_MEMBER_SORT_ORDER = "outlinesortoption"; //$NON-NLS-1$

	/**
	 * A named preference that defines how member elements are ordered by
	 * visibility in the Java views using the <code>JavaElementSorter</code>.
	 * <p>
	 * Value is of type <code>String</code>: A comma separated list of the
	 * following entries. Each entry must be in the list, no duplication. List
	 * order defines the sort order.
	 * <ul>
	 * <li><b>B </b>: Public</li>
	 * <li><b>V </b>: Private</li>
	 * <li><b>R </b>: Protected</li>
	 * <li><b>D </b>: Default</li>
	 * </ul>
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String APPEARANCE_VISIBILITY_SORT_ORDER = "net.sourceforge.phpdt.ui.visibility.order"; //$NON-NLS-1$

	/**
	 * A named preferences that controls if Java elements are also sorted by
	 * visibility.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String APPEARANCE_ENABLE_VISIBILITY_SORT_ORDER = "net.sourceforge.phpdt.ui.enable.visibility.order"; //$NON-NLS-1$

	/**
	 * A named preference that controls if prefix removal during setter/getter
	 * generation is turned on or off.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public static final String CODEGEN_USE_GETTERSETTER_PREFIX = "net.sourceforge.phpdt.ui.gettersetter.prefix.enable"; //$NON-NLS-1$

	/**
	 * A named preference that holds a list of prefixes to be removed from a
	 * local variable to compute setter and gettter names.
	 * <p>
	 * Value is of type <code>String</code>: comma separated list of prefixed
	 * </p>
	 * 
	 * @see #CODEGEN_USE_GETTERSETTER_PREFIX
	 */
	public static final String CODEGEN_GETTERSETTER_PREFIX = "net.sourceforge.phpdt.ui.gettersetter.prefix.list"; //$NON-NLS-1$

	/**
	 * A named preference that controls if suffix removal during setter/getter
	 * generation is turned on or off.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public static final String CODEGEN_USE_GETTERSETTER_SUFFIX = "net.sourceforge.phpdt.ui.gettersetter.suffix.enable"; //$NON-NLS-1$

	/**
	 * A named preference that holds a list of suffixes to be removed from a
	 * local variable to compute setter and getter names.
	 * <p>
	 * Value is of type <code>String</code>: comma separated list of suffixes
	 * </p>
	 * 
	 * @see #CODEGEN_USE_GETTERSETTER_SUFFIX
	 */
	public static final String CODEGEN_GETTERSETTER_SUFFIX = "net.sourceforge.phpdt.ui.gettersetter.suffix.list"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the keyword "this" will be added
	 * automatically to field accesses in generated methods.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String CODEGEN_KEYWORD_THIS = "org.eclipse.jdt.ui.keywordthis"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether to use the prefix "is" or the
	 * prefix "get" for automatically created getters which return a boolean
	 * field.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String CODEGEN_IS_FOR_GETTERS = "org.eclipse.jdt.ui.gettersetter.use.is"; //$NON-NLS-1$

	/**
	 * A named preference that defines the preferred variable names for
	 * exceptions in catch clauses.
	 * <p>
	 * Value is of type <code>String</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String CODEGEN_EXCEPTION_VAR_NAME = "org.eclipse.jdt.ui.exception.name"; //$NON-NLS-1$

	/**
	 * A named preference that controls if comment stubs will be added
	 * automatically to newly created types and methods.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public static final String CODEGEN_ADD_COMMENTS = "net.sourceforge.phpdt.ui.phpdoc"; //$NON-NLS-1$

	/**
	 * A name preference that controls if a JavaDoc stub gets added to newly
	 * created types and methods.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @deprecated Use CODEGEN_ADD_COMMENTS instead (Name is more precise).
	 */
	// public static final String CODEGEN__JAVADOC_STUBS = CODEGEN_ADD_COMMENTS;
	// //$NON-NLS-1$
	/**
	 * A named preference that controls if a non-phpdoc comment gets added to
	 * methods generated via the "Override Methods" operation.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public static final String CODEGEN__NON_JAVADOC_COMMENTS = "net.sourceforge.phpdt.ui.seecomments"; //$NON-NLS-1$

	/**
	 * A named preference that controls if a file comment gets added to newly
	 * created files.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public static final String CODEGEN__FILE_COMMENTS = "net.sourceforge.phpdt.ui.filecomments"; //$NON-NLS-1$

	/**
	 * A named preference that holds a list of comma separated package names.
	 * The list specifies the import order used by the "Organize Imports"
	 * opeation.
	 * <p>
	 * Value is of type <code>String</code>: semicolon separated list of
	 * package names
	 * </p>
	 */
	// public static final String ORGIMPORTS_IMPORTORDER =
	// "net.sourceforge.phpdt.ui.importorder"; //$NON-NLS-1$
	/**
	 * A named preference that specifies the number of imports added before a
	 * star-import declaration is used.
	 * <p>
	 * Value is of type <code>Int</code>: positive value specifing the number
	 * of non star-import is used
	 * </p>
	 */
	public static final String ORGIMPORTS_ONDEMANDTHRESHOLD = "net.sourceforge.phpdt.ui.ondemandthreshold"; //$NON-NLS-1$

	/**
	 * A named preferences that controls if types that start with a lower case
	 * letters get added by the "Organize Import" operation.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public static final String ORGIMPORTS_IGNORELOWERCASE = "net.sourceforge.phpdt.ui.ignorelowercasenames"; //$NON-NLS-1$

	/**
	 * A named preference that speficies whether children of a compilation unit
	 * are shown in the package explorer.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public static final String SHOW_CU_CHILDREN = "net.sourceforge.phpdt.ui.packages.cuchildren"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the package explorer's selection
	 * is linked to the active editor.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public static final String LINK_PACKAGES_TO_EDITOR = "net.sourceforge.phpdt.ui.packages.linktoeditor"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the hierarchy view's selection
	 * is linked to the active editor.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public static final String LINK_TYPEHIERARCHY_TO_EDITOR = "net.sourceforge.phpdt.ui.packages.linktypehierarchytoeditor"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the browsing view's selection is
	 * linked to the active editor.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public static final String LINK_BROWSING_VIEW_TO_EDITOR = "net.sourceforge.phpdt.ui.browsing.linktoeditor"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether new projects are generated using
	 * source and output folder.
	 * <p>
	 * Value is of type <code>Boolean</code>. if <code>true</code> new
	 * projects are created with a source and output folder. If
	 * <code>false</code> source and output folder equals to the project.
	 * </p>
	 */
	public static final String SRCBIN_FOLDERS_IN_NEWPROJ = "net.sourceforge.phpdt.ui.wizards.srcBinFoldersInNewProjects"; //$NON-NLS-1$

	/**
	 * A named preference that specifies the source folder name used when
	 * creating a new Java project. Value is inactive if
	 * <code>SRCBIN_FOLDERS_IN_NEWPROJ</code> is set to <code>false</code>.
	 * <p>
	 * Value is of type <code>String</code>.
	 * </p>
	 * 
	 * @see #SRCBIN_FOLDERS_IN_NEWPROJ
	 */
	public static final String SRCBIN_SRCNAME = "net.sourceforge.phpdt.ui.wizards.srcBinFoldersSrcName"; //$NON-NLS-1$

	/**
	 * A named preference that specifies the output folder name used when
	 * creating a new Java project. Value is inactive if
	 * <code>SRCBIN_FOLDERS_IN_NEWPROJ</code> is set to <code>false</code>.
	 * <p>
	 * Value is of type <code>String</code>.
	 * </p>
	 * 
	 * @see #SRCBIN_FOLDERS_IN_NEWPROJ
	 */
	public static final String SRCBIN_BINNAME = "net.sourceforge.phpdt.ui.wizards.srcBinFoldersBinName"; //$NON-NLS-1$

	/**
	 * A named preference that holds a list of possible JRE libraries used by
	 * the New Java Project wizard. An library consists of a description and an
	 * arbitrary number of <code>IClasspathEntry</code>s, that will represent
	 * the JRE on the new project's classpath.
	 * <p>
	 * Value is of type <code>String</code>: a semicolon separated list of
	 * encoded JRE libraries. <code>NEWPROJECT_JRELIBRARY_INDEX</code> defines
	 * the currently used library. Clients should use the method
	 * <code>encodeJRELibrary</code> to encode a JRE library into a string and
	 * the methods <code>decodeJRELibraryDescription(String)</code> and <code>
	 * decodeJRELibraryClasspathEntries(String)</code>
	 * to decode the description and the array of classpath entries from an
	 * encoded string.
	 * </p>
	 * 
	 * @see #NEWPROJECT_JRELIBRARY_INDEX
	 * @see #encodeJRELibrary(String, IClasspathEntry[])
	 * @see #decodeJRELibraryDescription(String)
	 * @see #decodeJRELibraryClasspathEntries(String)
	 */
	public static final String NEWPROJECT_JRELIBRARY_LIST = "net.sourceforge.phpdt.ui.wizards.jre.list"; //$NON-NLS-1$

	/**
	 * A named preferences that specifies the current active JRE library.
	 * <p>
	 * Value is of type <code>Int</code>: an index into the list of possible
	 * JRE libraries.
	 * </p>
	 * 
	 * @see #NEWPROJECT_JRELIBRARY_LIST
	 */
	public static final String NEWPROJECT_JRELIBRARY_INDEX = "net.sourceforge.phpdt.ui.wizards.jre.index"; //$NON-NLS-1$

	/**
	 * A named preference that controls if a new type hierarchy gets opened in a
	 * new type hierarchy perspective or inside the type hierarchy view part.
	 * <p>
	 * Value is of type <code>String</code>: possible values are <code>
	 * OPEN_TYPE_HIERARCHY_IN_PERSPECTIVE</code>
	 * or <code>
	 * OPEN_TYPE_HIERARCHY_IN_VIEW_PART</code>.
	 * </p>
	 * 
	 * @see #OPEN_TYPE_HIERARCHY_IN_PERSPECTIVE
	 * @see #OPEN_TYPE_HIERARCHY_IN_VIEW_PART
	 */
	public static final String OPEN_TYPE_HIERARCHY = "net.sourceforge.phpdt.ui.openTypeHierarchy"; //$NON-NLS-1$

	/**
	 * A string value used by the named preference
	 * <code>OPEN_TYPE_HIERARCHY</code>.
	 * 
	 * @see #OPEN_TYPE_HIERARCHY
	 */
	public static final String OPEN_TYPE_HIERARCHY_IN_PERSPECTIVE = "perspective"; //$NON-NLS-1$

	/**
	 * A string value used by the named preference
	 * <code>OPEN_TYPE_HIERARCHY</code>.
	 * 
	 * @see #OPEN_TYPE_HIERARCHY
	 */
	public static final String OPEN_TYPE_HIERARCHY_IN_VIEW_PART = "viewPart"; //$NON-NLS-1$

	/**
	 * A named preference that controls the behaviour when double clicking on a
	 * container in the packages view.
	 * <p>
	 * Value is of type <code>String</code>: possible values are <code>
	 * DOUBLE_CLICK_GOES_INTO</code>
	 * or <code>
	 * DOUBLE_CLICK_EXPANDS</code>.
	 * </p>
	 * 
	 * @see #DOUBLE_CLICK_EXPANDS
	 * @see #DOUBLE_CLICK_GOES_INTO
	 */
	public static final String DOUBLE_CLICK = "packageview.doubleclick"; //$NON-NLS-1$

	/**
	 * A string value used by the named preference <code>DOUBLE_CLICK</code>.
	 * 
	 * @see #DOUBLE_CLICK
	 */
	public static final String DOUBLE_CLICK_GOES_INTO = "packageview.gointo"; //$NON-NLS-1$

	/**
	 * A string value used by the named preference <code>DOUBLE_CLICK</code>.
	 * 
	 * @see #DOUBLE_CLICK
	 */
	public static final String DOUBLE_CLICK_EXPANDS = "packageview.doubleclick.expands"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether Java views update their
	 * presentation while editing or when saving the content of an editor.
	 * <p>
	 * Value is of type <code>String</code>: possible values are <code>
	 * UPDATE_ON_SAVE</code>
	 * or <code>
	 * UPDATE_WHILE_EDITING</code>.
	 * </p>
	 * 
	 * @see #UPDATE_ON_SAVE
	 * @see #UPDATE_WHILE_EDITING
	 */
	public static final String UPDATE_JAVA_VIEWS = "JavaUI.update"; //$NON-NLS-1$

	/**
	 * A string value used by the named preference
	 * <code>UPDATE_JAVA_VIEWS</code>
	 * 
	 * @see #UPDATE_JAVA_VIEWS
	 */
	public static final String UPDATE_ON_SAVE = "JavaUI.update.onSave"; //$NON-NLS-1$

	/**
	 * A string value used by the named preference
	 * <code>UPDATE_JAVA_VIEWS</code>
	 * 
	 * @see #UPDATE_JAVA_VIEWS
	 */
	public static final String UPDATE_WHILE_EDITING = "JavaUI.update.whileEditing"; //$NON-NLS-1$

	/**
	 * A named preference that holds the path of the Javadoc command used by the
	 * Javadoc creation wizard.
	 * <p>
	 * Value is of type <code>String</code>.
	 * </p>
	 */
	public static final String JAVADOC_COMMAND = "command"; //$NON-NLS-1$

	/**
	 * A named preference that defines whether hint to make hover sticky should
	 * be shown.
	 * 
	 * @see JavaUI
	 * @since 3.0
	 */
	public static final String EDITOR_SHOW_TEXT_HOVER_AFFORDANCE = "PreferenceConstants.EDITOR_SHOW_TEXT_HOVER_AFFORDANCE"; //$NON-NLS-1$

	/**
	 * A named preference that defines the key for the hover modifiers.
	 * 
	 * @see JavaUI
	 * @since 2.1
	 */
	public static final String EDITOR_TEXT_HOVER_MODIFIERS = "hoverModifiers"; //$NON-NLS-1$

	/**
	 * The id of the best match hover contributed for extension point
	 * <code>javaEditorTextHovers</code>.
	 * 
	 * @since 2.1
	 */
	public static String ID_BESTMATCH_HOVER = "net.sourceforge.phpdt.ui.BestMatchHover"; //$NON-NLS-1$

	/**
	 * The id of the source code hover contributed for extension point
	 * <code>javaEditorTextHovers</code>.
	 * 
	 * @since 2.1
	 */
	public static String ID_SOURCE_HOVER = "net.sourceforge.phpdt.ui.JavaSourceHover"; //$NON-NLS-1$

	/**
	 * The id of the problem hover contributed for extension point
	 * <code>javaEditorTextHovers</code>.
	 * 
	 * @since 2.1
	 */
	public static String ID_PROBLEM_HOVER = "net.sourceforge.phpdt.ui.ProblemHover"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether bracket matching highlighting is
	 * turned on or off.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_MATCHING_BRACKETS = "matchingBrackets"; //$NON-NLS-1$

	/**
	 * A named preference that holds the color used to highlight matching
	 * brackets.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_MATCHING_BRACKETS_COLOR = "matchingBracketsColor"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the current line highlighting is
	 * turned on or off.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_CURRENT_LINE = "currentLine"; //$NON-NLS-1$

	/**
	 * A named preference that holds the color used to highlight the current
	 * line.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_CURRENT_LINE_COLOR = "currentLineColor"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the print margin is turned on or
	 * off.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_PRINT_MARGIN = "printMargin"; //$NON-NLS-1$

	/**
	 * A named preference that holds the color used to render the print margin.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_PRINT_MARGIN_COLOR = "printMarginColor"; //$NON-NLS-1$

	/**
	 * Print margin column. Int value.
	 */
	public final static String EDITOR_PRINT_MARGIN_COLUMN = "printMarginColumn"; //$NON-NLS-1$

	/**
	 * A named preference that holds the color used for the find/replace scope.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_FIND_SCOPE_COLOR = AbstractTextEditor.PREFERENCE_COLOR_FIND_SCOPE;

	/**
	 * A named preference that specifies if the editor uses spaces for tabs.
	 * <p>
	 * Value is of type <code>Boolean</code>. If <code>true</code> spaces
	 * instead of tabs are used in the editor. If <code>false</code> the
	 * editor inserts a tab character when pressing the tab key.
	 * </p>
	 */
	public final static String EDITOR_SPACES_FOR_TABS = "spacesForTabs"; //$NON-NLS-1$

	/**
	 * A named preference that holds the number of spaces used per tab in the
	 * editor.
	 * <p>
	 * Value is of type <code>Int</code>: positive int value specifying the
	 * number of spaces per tab.
	 * </p>
	 */
	public final static String EDITOR_TAB_WIDTH = AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH; // "net.sourceforge.phpdt.ui.editor.tab.width";

	// //$NON-NLS-1$

	/**
	 * A named preference that controls whether the outline view selection
	 * should stay in sync with with the element at the current cursor position.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public final static String EDITOR_SYNC_OUTLINE_ON_CURSOR_MOVE = "JavaEditor.SyncOutlineOnCursorMove"; //$NON-NLS-1$

	/**
	 * A named preference that controls if correction indicators are shown in
	 * the UI.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_CORRECTION_INDICATION = "JavaEditor.ShowTemporaryProblem"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the editor shows problem
	 * indicators in text (squiggly lines).
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	// public final static String EDITOR_PROBLEM_INDICATION =
	// "problemIndication"; //$NON-NLS-1$
	/**
	 * A named preference that holds the color used to render problem
	 * indicators.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see #EDITOR_PROBLEM_INDICATION
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	// public final static String EDITOR_PROBLEM_INDICATION_COLOR =
	// "problemIndicationColor"; //$NON-NLS-1$
	/**
	 * PreferenceConstants.EDITOR_PROBLEM_INDICATION_COLOR; A named preference
	 * that controls whether the editor shows warning indicators in text
	 * (squiggly lines).
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	// public final static String EDITOR_WARNING_INDICATION =
	// "warningIndication"; //$NON-NLS-1$
	/**
	 * A named preference that holds the color used to render warning
	 * indicators.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see #EDITOR_WARNING_INDICATION
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	// public final static String EDITOR_WARNING_INDICATION_COLOR =
	// "warningIndicationColor"; //$NON-NLS-1$
	/**
	 * A named preference that controls whether the editor shows task indicators
	 * in text (squiggly lines).
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_TASK_INDICATION = "taskIndication"; //$NON-NLS-1$

	/**
	 * A named preference that holds the color used to render task indicators.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see #EDITOR_TASK_INDICATION
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_TASK_INDICATION_COLOR = "taskIndicationColor"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the editor shows bookmark
	 * indicators in text (squiggly lines).
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public final static String EDITOR_BOOKMARK_INDICATION = "bookmarkIndication"; //$NON-NLS-1$

	/**
	 * A named preference that holds the color used to render bookmark
	 * indicators.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see #EDITOR_BOOKMARK_INDICATION
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 2.1
	 */
	public final static String EDITOR_BOOKMARK_INDICATION_COLOR = "bookmarkIndicationColor"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the editor shows search
	 * indicators in text (squiggly lines).
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public final static String EDITOR_SEARCH_RESULT_INDICATION = "searchResultIndication"; //$NON-NLS-1$

	/**
	 * A named preference that holds the color used to render search indicators.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see #EDITOR_SEARCH_RESULT_INDICATION
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 2.1
	 */
	public final static String EDITOR_SEARCH_RESULT_INDICATION_COLOR = "searchResultIndicationColor"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the editor shows unknown
	 * indicators in text (squiggly lines).
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public final static String EDITOR_UNKNOWN_INDICATION = "othersIndication"; //$NON-NLS-1$

	/**
	 * A named preference that holds the color used to render unknown
	 * indicators.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see #EDITOR_UNKNOWN_INDICATION
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 2.1
	 */
	public final static String EDITOR_UNKNOWN_INDICATION_COLOR = "othersIndicationColor"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the overview ruler shows error
	 * indicators.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public final static String EDITOR_ERROR_INDICATION_IN_OVERVIEW_RULER = "errorIndicationInOverviewRuler"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the overview ruler shows warning
	 * indicators.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public final static String EDITOR_WARNING_INDICATION_IN_OVERVIEW_RULER = "warningIndicationInOverviewRuler"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the overview ruler shows task
	 * indicators.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public final static String EDITOR_TASK_INDICATION_IN_OVERVIEW_RULER = "taskIndicationInOverviewRuler"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the overview ruler shows
	 * bookmark indicators.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public final static String EDITOR_BOOKMARK_INDICATION_IN_OVERVIEW_RULER = "bookmarkIndicationInOverviewRuler"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the overview ruler shows search
	 * result indicators.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public final static String EDITOR_SEARCH_RESULT_INDICATION_IN_OVERVIEW_RULER = "searchResultIndicationInOverviewRuler"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the overview ruler shows unknown
	 * indicators.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public final static String EDITOR_UNKNOWN_INDICATION_IN_OVERVIEW_RULER = "othersIndicationInOverviewRuler"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the 'close strings' feature is
	 * enabled in PHP mode
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_CLOSE_STRINGS_DQ_PHP = "closeStringsPHPDQ"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the 'close strings' feature is
	 * enabled in PHP mode
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_CLOSE_STRINGS_SQ_PHP = "closeStringsPHPSQ"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the 'close brackets' feature is
	 * enabled in PHP mode
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public final static String EDITOR_CLOSE_BRACKETS_PHP = "closeBracketsPHP"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the 'wrap words' feature is
	 * enabled.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public final static String EDITOR_WRAP_WORDS = "wrapWords"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the 'wrap strings' feature is
	 * enabled.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public final static String EDITOR_WRAP_STRINGS_DQ = "wrapStringsDQ"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the 'escape strings' feature is
	 * enabled.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public final static String EDITOR_ESCAPE_STRINGS_DQ = "escapeStringsDQ"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the 'wrap strings' feature is
	 * enabled.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public final static String EDITOR_WRAP_STRINGS_SQ = "wrapStringsSQ"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the 'escape strings' feature is
	 * enabled.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public final static String EDITOR_ESCAPE_STRINGS_SQ = "escapeStringsSQ"; //$NON-NLS-1$

	/**
	 * A named preference that controls if content assist inserts the common
	 * prefix of all proposals before presenting choices.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public final static String CODEASSIST_PREFIX_COMPLETION = "content_assist_prefix_completion"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the 'close braces' feature is
	 * enabled.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public final static String EDITOR_CLOSE_BRACES = "closeBraces"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the 'close php docs' feature is
	 * enabled.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public final static String EDITOR_CLOSE_JAVADOCS = "closeJavaDocs"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the 'add JavaDoc tags' feature
	 * is enabled.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public final static String EDITOR_ADD_JAVADOC_TAGS = "addJavaDocTags"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the 'format Javadoc tags'
	 * feature is enabled.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public final static String EDITOR_FORMAT_JAVADOCS = "formatJavaDocs"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the 'smart paste' feature is
	 * enabled.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public final static String EDITOR_SMART_PASTE = "smartPaste"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the 'close strings' feature is
	 * enabled in HTML mode
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public final static String EDITOR_CLOSE_STRINGS_HTML = "closeStringsHTML"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the 'close brackets' feature is
	 * enabled in HTML mode
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public final static String EDITOR_CLOSE_BRACKETS_HTML = "closeBracketsHTML"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the 'smart home-end' feature is
	 * enabled.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public final static String EDITOR_SMART_HOME_END = AbstractTextEditor.PREFERENCE_NAVIGATION_SMART_HOME_END;

	/**
	 * A named preference that controls whether the 'sub-word navigation'
	 * feature is enabled.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public final static String EDITOR_SUB_WORD_NAVIGATION = "subWordNavigation"; //$NON-NLS-1$

	/**
	 * A named preference that controls if temporary problems are evaluated and
	 * shown in the UI.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_EVALUTE_TEMPORARY_PROBLEMS = "handleTemporaryProblems"; //$NON-NLS-1$

	/**
	 * A named preference that controls if the overview ruler is shown in the
	 * UI.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_OVERVIEW_RULER = "overviewRuler"; //$NON-NLS-1$

	/**
	 * A named preference that holds the color used to render linked positions
	 * inside code templates.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_LINKED_POSITION_COLOR = "linkedPositionColor"; //$NON-NLS-1$

	/**
	 * A named preference that holds the color used as the text foreground.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_FOREGROUND_COLOR = AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND;

	/**
	 * A named preference that describes if the system default foreground color
	 * is used as the text foreground.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_FOREGROUND_DEFAULT_COLOR = AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT;

	/**
	 * A named preference that holds the color used as the text background.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_BACKGROUND_COLOR = AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND;

	/**
	 * A named preference that describes if the system default background color
	 * is used as the text foreground.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_BACKGROUND_DEFAULT_COLOR = AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT;

	/**
	 * Preference key suffix for bold text style preference keys.
	 */
	public static final String EDITOR_BOLD_SUFFIX = "_bold"; //$NON-NLS-1$

	/**
	 * Preference key suffix for bold text style preference keys.
	 */
	public static final String EDITOR_ITALIC_SUFFIX = "_italic"; //$NON-NLS-1$

	/**
	 * A named preference that holds the color used to render multi line
	 * comments.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_MULTI_LINE_COMMENT_COLOR = IPreferenceConstants.PHP_MULTILINE_COMMENT;

	/**
	 * The symbolic font name for the Java editor text font (value
	 * <code>"net.sourceforge.phpdt.ui.editors.textfont"</code>).
	 * 
	 * @since 2.1
	 */
	public final static String EDITOR_TEXT_FONT = "net.sourceforge.phpdt.ui.editors.textfont"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether multi line comments are rendered
	 * in bold.
	 * <p>
	 * Value is of type <code>Boolean</code>. If <code>true</code> multi
	 * line comments are rendered in bold. If <code>false</code> the are
	 * rendered using no font style attribute.
	 * </p>
	 */
	public final static String EDITOR_MULTI_LINE_COMMENT_BOLD = IPreferenceConstants.PHP_MULTILINE_COMMENT
			+ EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that holds the color used to render single line
	 * comments.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_SINGLE_LINE_COMMENT_COLOR = IPreferenceConstants.PHP_SINGLELINE_COMMENT;

	/**
	 * A named preference that controls whether sinle line comments are rendered
	 * in bold.
	 * <p>
	 * Value is of type <code>Boolean</code>. If <code>true</code> single
	 * line comments are rendered in bold. If <code>false</code> the are
	 * rendered using no font style attribute.
	 * </p>
	 */
	public final static String EDITOR_SINGLE_LINE_COMMENT_BOLD = IPreferenceConstants.PHP_SINGLELINE_COMMENT
			+ EDITOR_BOLD_SUFFIX;

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
	public final static String EDITOR_PHP_OPERATOR_COLOR = IPreferenceConstants.PHP_OPERATOR;

	/**
	 * A named preference that controls whether operators and brackets are
	 * rendered in bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public final static String EDITOR_PHP_OPERATOR_BOLD = IPreferenceConstants.PHP_OPERATOR
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
	public final static String EDITOR_PHP_OPERATOR_ITALIC = IPreferenceConstants.PHP_OPERATOR
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
	public final static String EDITOR_PHP_BRACE_OPERATOR_COLOR = IPreferenceConstants.PHP_BRACE_OPERATOR;

	/**
	 * A named preference that controls whether operators and brackets are
	 * rendered in bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public final static String EDITOR_PHP_BRACE_OPERATOR_BOLD = IPreferenceConstants.PHP_BRACE_OPERATOR
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
	public final static String EDITOR_PHP_BRACE_OPERATOR_ITALIC = IPreferenceConstants.PHP_BRACE_OPERATOR
			+ EDITOR_ITALIC_SUFFIX;

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
	public final static String EDITOR_PHP_KEYWORD_RETURN_COLOR = IPreferenceConstants.PHP_KEYWORD_RETURN;

	/**
	 * A named preference that controls whether 'return' keyword is rendered in
	 * bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public final static String EDITOR_PHP_KEYWORD_RETURN_BOLD = IPreferenceConstants.PHP_KEYWORD_RETURN
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
	public final static String EDITOR_PHP_KEYWORD_RETURN_ITALIC = IPreferenceConstants.PHP_KEYWORD_RETURN
			+ EDITOR_ITALIC_SUFFIX;

	/**
	 * A named preference that holds the color used to render php start and stop
	 * tags.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_PHP_TAG_COLOR = IPreferenceConstants.PHP_TAG;

	/**
	 * A named preference that controls whether php start and stop tags are
	 * rendered in bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_PHP_TAG_BOLD = IPreferenceConstants.PHP_TAG
			+ EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that holds the color used to render php keywords.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_JAVA_KEYWORD_COLOR = IPreferenceConstants.PHP_KEYWORD;

	/**
	 * A named preference that controls whether keywords are rendered in bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_JAVA_KEYWORD_BOLD = IPreferenceConstants.PHP_KEYWORD
			+ EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that holds the color used to render predefined php
	 * function names.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_PHP_FUNCTIONNAME_COLOR = IPreferenceConstants.PHP_FUNCTIONNAME;

	/**
	 * A named preference that controls whether function names are rendered in
	 * bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_PHP_FUNCTIONNAME_BOLD = IPreferenceConstants.PHP_FUNCTIONNAME
			+ EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that holds the color used to render php variables with
	 * prefix '$_'.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_PHP_VARIABLE_DOLLAR_COLOR = IPreferenceConstants.PHP_VARIABLE_DOLLAR;

	/**
	 * A named preference that controls whether variables with prefix '$_' are
	 * rendered in bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_PHP_VARIABLE_DOLLAR_BOLD = IPreferenceConstants.PHP_VARIABLE_DOLLAR
			+ EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that holds the color used to render php variables.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_PHP_VARIABLE_COLOR = IPreferenceConstants.PHP_VARIABLE;

	/**
	 * A named preference that controls whether variables are rendered in bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_PHP_VARIABLE_BOLD = IPreferenceConstants.PHP_VARIABLE
			+ EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that holds the color used to render php constants.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_PHP_CONSTANT_COLOR = IPreferenceConstants.PHP_CONSTANT;

	/**
	 * A named preference that controls whether constants are rendered in bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_PHP_CONSTANT_BOLD = IPreferenceConstants.PHP_CONSTANT
			+ EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that holds the color used to render php types.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_PHP_TYPE_COLOR = IPreferenceConstants.PHP_TYPE;

	/**
	 * A named preference that controls whether types are rendered in bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_PHP_TYPE_BOLD = IPreferenceConstants.PHP_TYPE
			+ EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that holds the color used to render string constants.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_STRING_COLOR_DQ = IPreferenceConstants.PHP_STRING_DQ;

	/**
	 * A named preference that controls whether string constants are rendered in
	 * bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_STRING_BOLD_DQ = IPreferenceConstants.PHP_STRING_DQ
			+ EDITOR_BOLD_SUFFIX;

	public final static String EDITOR_STRING_COLOR_SQ = IPreferenceConstants.PHP_STRING_SQ;

	/**
	 * A named preference that controls whether string constants are rendered in
	 * bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_STRING_BOLD_SQ = IPreferenceConstants.PHP_STRING_SQ
			+ EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that holds the color used to render php default text.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_JAVA_DEFAULT_COLOR = IPreferenceConstants.PHP_DEFAULT;

	/**
	 * A named preference that controls whether Java default text is rendered in
	 * bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_JAVA_DEFAULT_BOLD = IPreferenceConstants.PHP_DEFAULT
			+ EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that holds the color used to render task tags.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 2.1
	 */
	public final static String EDITOR_TASK_TAG_COLOR = IPreferenceConstants.TASK_TAG;

	/**
	 * A named preference that controls whether task tags are rendered in bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public final static String EDITOR_TASK_TAG_BOLD = IPreferenceConstants.TASK_TAG
			+ EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that holds the color used to render phpdoc keywords.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_JAVADOC_KEYWORD_COLOR = IPreferenceConstants.PHPDOC_KEYWORD;

	/**
	 * A named preference that controls whether phpdoc keywords are rendered in
	 * bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_JAVADOC_KEYWORD_BOLD = IPreferenceConstants.PHPDOC_KEYWORD
			+ EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that holds the color used to render phpdoc tags.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_JAVADOC_TAG_COLOR = IPreferenceConstants.PHPDOC_TAG;

	/**
	 * A named preference that controls whether phpdoc tags are rendered in
	 * bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_JAVADOC_TAG_BOLD = IPreferenceConstants.PHPDOC_TAG
			+ EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that holds the color used to render phpdoc links.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_JAVADOC_LINKS_COLOR = IPreferenceConstants.PHPDOC_LINK;

	/**
	 * A named preference that controls whether phpdoc links are rendered in
	 * bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_JAVADOC_LINKS_BOLD = IPreferenceConstants.PHPDOC_LINK
			+ EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that holds the color used to render phpdoc default
	 * text.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_JAVADOC_DEFAULT_COLOR = IPreferenceConstants.PHPDOC_DEFAULT;

	/**
	 * A named preference that controls whether phpdoc default text is rendered
	 * in bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_JAVADOC_DEFAULT_BOLD = IPreferenceConstants.PHPDOC_DEFAULT
			+ EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that holds the color used for 'linked-mode' underline.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 2.1
	 */
	public final static String EDITOR_LINK_COLOR = "linkColor"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether hover tooltips in the editor are
	 * turned on or off.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public static final String EDITOR_SHOW_HOVER = "net.sourceforge.phpdt.ui.editor.showHover"; //$NON-NLS-1$

	/**
	 * A named preference that defines the hover shown when no control key is
	 * pressed.
	 * <p>
	 * Value is of type <code>String</code>: possible values are <code>
	 * EDITOR_NO_HOVER_CONFIGURED_ID</code>
	 * or <code>EDITOR_DEFAULT_HOVER_CONFIGURED_ID</code> or the hover id of a
	 * hover contributed as <code>phpEditorTextHovers</code>.
	 * </p>
	 * 
	 * @see #EDITOR_NO_HOVER_CONFIGURED_ID
	 * @see #EDITOR_DEFAULT_HOVER_CONFIGURED_ID
	 * @see JavaUI
	 * @since 2.1
	 */
	public static final String EDITOR_NONE_HOVER = "noneHover"; //$NON-NLS-1$

	/**
	 * A named preference that defines the hover shown when the
	 * <code>CTRL</code> modifier key is pressed.
	 * <p>
	 * Value is of type <code>String</code>: possible values are <code>
	 * EDITOR_NO_HOVER_CONFIGURED_ID</code>
	 * or <code>EDITOR_DEFAULT_HOVER_CONFIGURED_ID</code> or the hover id of a
	 * hover contributed as <code>phpEditorTextHovers</code>.
	 * </p>
	 * 
	 * @see #EDITOR_NO_HOVER_CONFIGURED_ID
	 * @see #EDITOR_DEFAULT_HOVER_CONFIGURED_ID
	 * @see JavaUI
	 * @since 2.1
	 */
	public static final String EDITOR_CTRL_HOVER = "ctrlHover"; //$NON-NLS-1$

	/**
	 * A named preference that defines the hover shown when the
	 * <code>SHIFT</code> modifier key is pressed.
	 * <p>
	 * Value is of type <code>String</code>: possible values are <code>
	 * EDITOR_NO_HOVER_CONFIGURED_ID</code>
	 * or <code>EDITOR_DEFAULT_HOVER_CONFIGURED_ID</code> or the hover id of a
	 * hover contributed as <code>phpEditorTextHovers</code>.
	 * </p>
	 * 
	 * @see #EDITOR_NO_HOVER_CONFIGURED_ID
	 * @see #EDITOR_DEFAULT_HOVER_CONFIGURED_ID
	 * @see JavaUI ID_*_HOVER
	 * @since 2.1
	 */
	public static final String EDITOR_SHIFT_HOVER = "shiftHover"; //$NON-NLS-1$

	/**
	 * A named preference that defines the hover shown when the
	 * <code>CTRL + ALT</code> modifier keys is pressed.
	 * <p>
	 * Value is of type <code>String</code>: possible values are <code>
	 * EDITOR_NO_HOVER_CONFIGURED_ID</code>
	 * or <code>EDITOR_DEFAULT_HOVER_CONFIGURED_ID</code> or the hover id of a
	 * hover contributed as <code>phpEditorTextHovers</code>.
	 * </p>
	 * 
	 * @see #EDITOR_NO_HOVER_CONFIGURED_ID
	 * @see #EDITOR_DEFAULT_HOVER_CONFIGURED_ID
	 * @see JavaUI ID_*_HOVER
	 * @since 2.1
	 */
	public static final String EDITOR_CTRL_ALT_HOVER = "ctrlAltHover"; //$NON-NLS-1$

	/**
	 * A named preference that defines the hover shown when the
	 * <code>CTRL + ALT + SHIFT</code> modifier keys is pressed.
	 * <p>
	 * Value is of type <code>String</code>: possible values are <code>
	 * EDITOR_NO_HOVER_CONFIGURED_ID</code>
	 * or <code>EDITOR_DEFAULT_HOVER_CONFIGURED_ID</code> or the hover id of a
	 * hover contributed as <code>phpEditorTextHovers</code>.
	 * </p>
	 * 
	 * @see #EDITOR_NO_HOVER_CONFIGURED_ID
	 * @see #EDITOR_DEFAULT_HOVER_CONFIGURED_ID
	 * @see JavaUI ID_*_HOVER
	 * @since 2.1
	 */
	public static final String EDITOR_CTRL_ALT_SHIFT_HOVER = "ctrlAltShiftHover"; //$NON-NLS-1$

	/**
	 * A named preference that defines the hover shown when the
	 * <code>CTRL + SHIFT</code> modifier keys is pressed.
	 * <p>
	 * Value is of type <code>String</code>: possible values are <code>
	 * EDITOR_NO_HOVER_CONFIGURED_ID</code>
	 * or <code>EDITOR_DEFAULT_HOVER_CONFIGURED_ID</code> or the hover id of a
	 * hover contributed as <code>phpEditorTextHovers</code>.
	 * </p>
	 * 
	 * @see #EDITOR_NO_HOVER_CONFIGURED_ID
	 * @see #EDITOR_DEFAULT_HOVER_CONFIGURED_ID
	 * @see JavaUI ID_*_HOVER
	 * @since 2.1
	 */
	public static final String EDITOR_CTRL_SHIFT_HOVER = "ctrlShiftHover"; //$NON-NLS-1$

	/**
	 * A named preference that defines the hover shown when the <code>ALT</code>
	 * modifier key is pressed.
	 * <p>
	 * Value is of type <code>String</code>: possible values are <code>
	 * EDITOR_NO_HOVER_CONFIGURED_ID</code>,
	 * <code>EDITOR_DEFAULT_HOVER_CONFIGURED_ID</code> or the hover id of a
	 * hover contributed as <code>phpEditorTextHovers</code>.
	 * </p>
	 * 
	 * @see #EDITOR_NO_HOVER_CONFIGURED_ID
	 * @see #EDITOR_DEFAULT_HOVER_CONFIGURED_ID
	 * @see JavaUI ID_*_HOVER
	 * @since 2.1
	 */
	public static final String EDITOR_ALT_SHIFT_HOVER = "altShiftHover"; //$NON-NLS-1$

	/**
	 * A string value used by the named preferences for hover configuration to
	 * descibe that no hover should be shown for the given key modifiers.
	 * 
	 * @since 2.1
	 */
	public static final String EDITOR_NO_HOVER_CONFIGURED_ID = "noHoverConfiguredId"; //$NON-NLS-1$

	/**
	 * A string value used by the named preferences for hover configuration to
	 * descibe that the default hover should be shown for the given key
	 * modifiers. The default hover is described by the
	 * <code>EDITOR_DEFAULT_HOVER</code> property.
	 * 
	 * @since 2.1
	 */
	public static final String EDITOR_DEFAULT_HOVER_CONFIGURED_ID = "defaultHoverConfiguredId"; //$NON-NLS-1$

	/**
	 * A named preference that defines the hover named the 'default hover'.
	 * Value is of type <code>String</code>: possible values are <code>
	 * EDITOR_NO_HOVER_CONFIGURED_ID</code>
	 * or <code> the hover id of a hover
	 * contributed as <code>phpEditorTextHovers</code>.
	 * </p>
	 *@since 2.1
	 */
	public static final String EDITOR_DEFAULT_HOVER = "defaultHover"; //$NON-NLS-1$

	/**
	 * A named preference that controls if segmented view (show selected element
	 * only) is turned on or off.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public static final String EDITOR_SHOW_SEGMENTS = "net.sourceforge.phpdt.ui.editor.showSegments"; //$NON-NLS-1$

	/**
	 * A named preference that controls if browser like links are turned on or
	 * off.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public static final String EDITOR_BROWSER_LIKE_LINKS = "browserLikeLinks"; //$NON-NLS-1$

	/**
	 * A named preference that controls the key modifier for browser like links.
	 * <p>
	 * Value is of type <code>String</code>.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public static final String EDITOR_BROWSER_LIKE_LINKS_KEY_MODIFIER = "browserLikeLinksKeyModifier"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether occurrences are marked in the
	 * editor.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String EDITOR_MARK_OCCURRENCES = "markOccurrences"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether occurrences are sticky in the
	 * editor.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String EDITOR_STICKY_OCCURRENCES = "stickyOccurrences"; //$NON-NLS-1$

	/**
	 * A named preference that controls disabling of the overwrite mode.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String EDITOR_DISABLE_OVERWRITE_MODE = "disable_overwrite_mode"; //$NON-NLS-1$

	/**
	 * A named preference that controls saving of a file on loss of editor focus.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String EDITOR_SAVE_ON_BLUR = "save_on_blur"; //$NON-NLS-1$

	/**
	 * A named preference that controls the "smart semicolon" smart typing
	 * handler
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String EDITOR_SMART_SEMICOLON = "smart_semicolon"; //$NON-NLS-1$

	/**
	 * A named preference that controls the smart backspace behavior.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * 
	 * @since 3.0
	 */
	public static final String EDITOR_SMART_BACKSPACE = "smart_backspace"; //$NON-NLS-1$

	/**
	 * A named preference that controls the "smart opening brace" smart typing
	 * handler
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String EDITOR_SMART_OPENING_BRACE = "smart_opening_brace"; //$NON-NLS-1$

	/**
	 * A named preference that controls the smart tab behaviour.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * 
	 * @since 3.0
	 */
	public static final String EDITOR_SMART_TAB = "smart_tab"; //$NON-NLS-1$

	public static final String EDITOR_P_RTRIM_ON_SAVE = "editor_p_trim_on_save"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether Java comments should be
	 * spell-checked.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public final static String SPELLING_CHECK_SPELLING = ISpellCheckPreferenceKeys.SPELLING_CHECK_SPELLING;

	/**
	 * A named preference that controls whether words containing digits should
	 * be skipped during spell-checking.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public final static String SPELLING_IGNORE_DIGITS = ISpellCheckPreferenceKeys.SPELLING_IGNORE_DIGITS;

	/**
	 * A named preference that controls whether mixed case words should be
	 * skipped during spell-checking.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public final static String SPELLING_IGNORE_MIXED = ISpellCheckPreferenceKeys.SPELLING_IGNORE_MIXED;

	/**
	 * A named preference that controls whether sentence capitalization should
	 * be ignored during spell-checking.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public final static String SPELLING_IGNORE_SENTENCE = ISpellCheckPreferenceKeys.SPELLING_IGNORE_SENTENCE;

	/**
	 * A named preference that controls whether upper case words should be
	 * skipped during spell-checking.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public final static String SPELLING_IGNORE_UPPER = ISpellCheckPreferenceKeys.SPELLING_IGNORE_UPPER;

	/**
	 * A named preference that controls whether urls should be ignored during
	 * spell-checking.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public final static String SPELLING_IGNORE_URLS = ISpellCheckPreferenceKeys.SPELLING_IGNORE_URLS;

	/**
	 * A named preference that controls the locale used for spell-checking.
	 * <p>
	 * Value is of type <code>String</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public final static String SPELLING_LOCALE = ISpellCheckPreferenceKeys.SPELLING_LOCALE;

	/**
	 * A named preference that controls the number of proposals offered during
	 * spell-checking.
	 * <p>
	 * Value is of type <code>Integer</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public final static String SPELLING_PROPOSAL_THRESHOLD = ISpellCheckPreferenceKeys.SPELLING_PROPOSAL_THRESHOLD;

	/**
	 * A named preference that specifies the workspace user dictionary.
	 * <p>
	 * Value is of type <code>Integer</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public final static String SPELLING_USER_DICTIONARY = ISpellCheckPreferenceKeys.SPELLING_USER_DICTIONARY;

	/**
	 * A named preference that specifies whether spelling dictionaries are
	 * available to content assist.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public final static String SPELLING_ENABLE_CONTENTASSIST = ISpellCheckPreferenceKeys.SPELLING_ENABLE_CONTENTASSIST;

	/**
	 * A named preference that controls whether code snippets are formatted in
	 * Javadoc comments.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public final static String FORMATTER_COMMENT_FORMATSOURCE = "comment_format_source_code"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether description of Javadoc
	 * parameters are indented.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public final static String FORMATTER_COMMENT_INDENTPARAMETERDESCRIPTION = "comment_indent_parameter_description"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the header comment of a Java
	 * source file is formatted.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public final static String FORMATTER_COMMENT_FORMATHEADER = "comment_format_header"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether Javadoc root tags are indented.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public final static String FORMATTER_COMMENT_INDENTROOTTAGS = "comment_indent_root_tags"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether Javadoc comments are formatted
	 * by the content formatter.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public final static String FORMATTER_COMMENT_FORMAT = "comment_format_comments"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether a new line is inserted after
	 * Javadoc root tag parameters.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public final static String FORMATTER_COMMENT_NEWLINEFORPARAMETER = "comment_new_line_for_parameter"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether an empty line is inserted before
	 * the Javadoc root tag block.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public final static String FORMATTER_COMMENT_SEPARATEROOTTAGS = "comment_separate_root_tags"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether blank lines are cleared during
	 * formatting
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public final static String FORMATTER_COMMENT_CLEARBLANKLINES = "comment_clear_blank_lines"; //$NON-NLS-1$

	/**
	 * A named preference that controls the line length of comments.
	 * <p>
	 * Value is of type <code>Integer</code>. The value must be at least 4
	 * for reasonable formatting.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public final static String FORMATTER_COMMENT_LINELENGTH = "comment_line_length"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether html tags are formatted.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public final static String FORMATTER_COMMENT_FORMATHTML = "comment_format_html"; //$NON-NLS-1$

	/**
	 * A named preference that controls if the Java code assist gets auto
	 * activated.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String CODEASSIST_AUTOACTIVATION = "content_assist_autoactivation"; //$NON-NLS-1$

	/**
	 * A name preference that holds the auto activation delay time in milli
	 * seconds.
	 * <p>
	 * Value is of type <code>Int</code>.
	 * </p>
	 */
	public final static String CODEASSIST_AUTOACTIVATION_DELAY = "content_assist_autoactivation_delay"; //$NON-NLS-1$

	/**
	 * A named preference that controls if code assist contains only visible
	 * proposals.
	 * <p>
	 * Value is of type <code>Boolean</code>. if
	 * <code>true<code> code assist only contains visible members. If
	 * <code>false</code> all members are included.
	 * </p>
	 */
	public final static String CODEASSIST_SHOW_VISIBLE_PROPOSALS = "content_assist_show_visible_proposals"; //$NON-NLS-1$

	/**
	 * A named preference that controls if the Java code assist inserts a
	 * proposal automatically if only one proposal is available.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public final static String CODEASSIST_AUTOINSERT = "content_assist_autoinsert"; //$NON-NLS-1$

	/**
	 * A named preference that controls if the Java code assist adds import
	 * statements.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public final static String CODEASSIST_ADDIMPORT = "content_assist_add_import"; //$NON-NLS-1$

	/**
	 * A named preference that controls if the Java code assist only inserts
	 * completions. If set to false the proposals can also _replace_ code.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public final static String CODEASSIST_INSERT_COMPLETION = "content_assist_insert_completion"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether code assist proposals filtering
	 * is case sensitive or not.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String CODEASSIST_CASE_SENSITIVITY = "content_assist_case_sensitivity"; //$NON-NLS-1$

	/**
	 * A named preference that defines if code assist proposals are sorted in
	 * alphabetical order.
	 * <p>
	 * Value is of type <code>Boolean</code>. If <code>true</code> that are
	 * sorted in alphabetical order. If <code>false</code> that are unsorted.
	 * </p>
	 */
	public final static String CODEASSIST_ORDER_PROPOSALS = "content_assist_order_proposals"; //$NON-NLS-1$

	/**
	 * A named preference that controls if argument names are filled in when a
	 * method is selected from as list of code assist proposal.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String CODEASSIST_FILL_ARGUMENT_NAMES = "content_assist_fill_method_arguments"; //$NON-NLS-1$

	/**
	 * A named preference that controls if method arguments are guessed when a
	 * method is selected from as list of code assist proposal.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public final static String CODEASSIST_GUESS_METHOD_ARGUMENTS = "content_assist_guess_method_arguments"; //$NON-NLS-1$

	/**
	 * A named preference that holds the characters that auto activate code
	 * assist in PHP code.
	 * <p>
	 * Value is of type <code>Sring</code>. All characters that trigger auto
	 * code assist in PHP code.
	 * </p>
	 */
	public final static String CODEASSIST_AUTOACTIVATION_TRIGGERS_JAVA = "content_assist_autoactivation_triggers_php"; //$NON-NLS-1$

	/**
	 * A named preference that holds the characters that auto activate code
	 * assist in PHPDoc.
	 * <p>
	 * Value is of type <code>Sring</code>. All characters that trigger auto
	 * code assist in PHPDoc.
	 * </p>
	 */
	public final static String CODEASSIST_AUTOACTIVATION_TRIGGERS_JAVADOC = "content_assist_autoactivation_triggers_phpdoc"; //$NON-NLS-1$

	/**
	 * A named preference that holds the characters that auto activate code
	 * assist in HTML.
	 * <p>
	 * Value is of type <code>Sring</code>. All characters that trigger auto
	 * code assist in HTML.
	 * </p>
	 */
	public final static String CODEASSIST_AUTOACTIVATION_TRIGGERS_HTML = "content_assist_autoactivation_triggers_html"; //$NON-NLS-1$

	/**
	 * A named preference that holds the background color used in the code
	 * assist selection dialog.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String CODEASSIST_PROPOSALS_BACKGROUND = "content_assist_proposals_background"; //$NON-NLS-1$

	/**
	 * A named preference that holds the foreground color used in the code
	 * assist selection dialog.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String CODEASSIST_PROPOSALS_FOREGROUND = "content_assist_proposals_foreground"; //$NON-NLS-1$

	/**
	 * A named preference that holds the background color used for parameter
	 * hints.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String CODEASSIST_PARAMETERS_BACKGROUND = "content_assist_parameters_background"; //$NON-NLS-1$

	/**
	 * A named preference that holds the foreground color used in the code
	 * assist selection dialog
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String CODEASSIST_PARAMETERS_FOREGROUND = "content_assist_parameters_foreground"; //$NON-NLS-1$

	/**
	 * A named preference that holds the background color used in the code
	 * assist selection dialog to mark replaced code.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 2.1
	 */
	public final static String CODEASSIST_REPLACEMENT_BACKGROUND = "content_assist_completion_replacement_background"; //$NON-NLS-1$

	/**
	 * A named preference that holds the foreground color used in the code
	 * assist selection dialog to mark replaced code.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 2.1
	 */
	public final static String CODEASSIST_REPLACEMENT_FOREGROUND = "content_assist_completion_replacement_foreground"; //$NON-NLS-1$

	/**
	 * A named preference that controls the behaviour of the refactoring wizard
	 * for showing the error page.
	 * <p>
	 * Value is of type <code>String</code>. Valid values are:
	 * <code>REFACTOR_FATAL_SEVERITY</code>,
	 * <code>REFACTOR_ERROR_SEVERITY</code>,<code>REFACTOR_WARNING_SEVERITY</code>
	 * <code>REFACTOR_INFO_SEVERITY</code>,
	 * <code>REFACTOR_OK_SEVERITY</code>.
	 * </p>
	 * 
	 * @see #REFACTOR_FATAL_SEVERITY
	 * @see #REFACTOR_ERROR_SEVERITY
	 * @see #REFACTOR_WARNING_SEVERITY
	 * @see #REFACTOR_INFO_SEVERITY
	 * @see #REFACTOR_OK_SEVERITY
	 */
	public static final String REFACTOR_ERROR_PAGE_SEVERITY_THRESHOLD = "Refactoring.ErrorPage.severityThreshold"; //$NON-NLS-1$

	/**
	 * A string value used by the named preference
	 * <code>REFACTOR_ERROR_PAGE_SEVERITY_THRESHOLD</code>.
	 * 
	 * @see #REFACTOR_ERROR_PAGE_SEVERITY_THRESHOLD
	 */
	public static final String REFACTOR_FATAL_SEVERITY = "4"; //$NON-NLS-1$

	/**
	 * A string value used by the named preference
	 * <code>REFACTOR_ERROR_PAGE_SEVERITY_THRESHOLD</code>.
	 * 
	 * @see #REFACTOR_ERROR_PAGE_SEVERITY_THRESHOLD
	 */
	public static final String REFACTOR_ERROR_SEVERITY = "3"; //$NON-NLS-1$

	/**
	 * A string value used by the named preference
	 * <code>REFACTOR_ERROR_PAGE_SEVERITY_THRESHOLD</code>.
	 * 
	 * @see #REFACTOR_ERROR_PAGE_SEVERITY_THRESHOLD
	 */
	public static final String REFACTOR_WARNING_SEVERITY = "2"; //$NON-NLS-1$

	/**
	 * A string value used by the named preference
	 * <code>REFACTOR_ERROR_PAGE_SEVERITY_THRESHOLD</code>.
	 * 
	 * @see #REFACTOR_ERROR_PAGE_SEVERITY_THRESHOLD
	 */
	public static final String REFACTOR_INFO_SEVERITY = "1"; //$NON-NLS-1$

	/**
	 * A string value used by the named preference
	 * <code>REFACTOR_ERROR_PAGE_SEVERITY_THRESHOLD</code>.
	 * 
	 * @see #REFACTOR_ERROR_PAGE_SEVERITY_THRESHOLD
	 */
	public static final String REFACTOR_OK_SEVERITY = "0"; //$NON-NLS-1$

	/**
	 * A named preference thet controls whether all dirty editors are
	 * automatically saved before a refactoring is executed.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public static final String REFACTOR_SAVE_ALL_EDITORS = "Refactoring.savealleditors"; //$NON-NLS-1$

	/**
	 * A named preference that controls if the Java Browsing views are linked to
	 * the active editor.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @see #LINK_PACKAGES_TO_EDITOR
	 */
	public static final String BROWSING_LINK_VIEW_TO_EDITOR = "net.sourceforge.phpdt.ui.browsing.linktoeditor"; //$NON-NLS-1$

	/**
	 * A named preference that controls the layout of the Java Browsing views
	 * vertically. Boolean value.
	 * <p>
	 * Value is of type <code>Boolean</code>. If
	 * <code>true<code> the views are stacked vertical.
	 * If <code>false</code> they are stacked horizontal.
	 * </p>
	 */
	public static final String BROWSING_STACK_VERTICALLY = "net.sourceforge.phpdt.ui.browsing.stackVertically"; //$NON-NLS-1$

	/**
	 * A named preference that controls if templates are formatted when applied.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public static final String TEMPLATES_USE_CODEFORMATTER = "net.sourceforge.phpdt.ui.template.format"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether annotation roll over is used or
	 * not.
	 * <p>
	 * Value is of type <code>Boolean</code>. If
	 * <code>true<code> the annotation ruler column
	 * uses a roll over to display multiple annotations
	 * </p>
	 *
	 * @since 3.0
	 */
	public static final String EDITOR_ANNOTATION_ROLL_OVER = "editor_annotation_roll_over"; //$NON-NLS-1$

	/**
	 * A named preference that controls the key modifier mask for browser like
	 * links. The value is only used if the value of
	 * <code>EDITOR_BROWSER_LIKE_LINKS</code> cannot be resolved to valid SWT
	 * modifier bits.
	 * <p>
	 * Value is of type <code>String</code>.
	 * </p>
	 * 
	 * @see #EDITOR_BROWSER_LIKE_LINKS_KEY_MODIFIER
	 * @since 2.1.1
	 */
	public static final String EDITOR_BROWSER_LIKE_LINKS_KEY_MODIFIER_MASK = "browserLikeLinksKeyModifierMask"; //$NON-NLS-1$

	/**
	 * A named preference that defines the key for the hover modifier state
	 * masks. The value is only used if the value of
	 * <code>EDITOR_TEXT_HOVER_MODIFIERS</code> cannot be resolved to valid
	 * SWT modifier bits.
	 * 
	 * @see JavaUI
	 * @see #EDITOR_TEXT_HOVER_MODIFIERS
	 * @since 2.1.1
	 */
	public static final String EDITOR_TEXT_HOVER_MODIFIER_MASKS = "hoverModifierMasks"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether folding is enabled in the Java
	 * editor.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String EDITOR_FOLDING_ENABLED = "editor_folding_enabled"; //$NON-NLS-1$

	/**
	 * A named preference that stores the configured folding provider.
	 * <p>
	 * Value is of type <code>String</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String EDITOR_FOLDING_PROVIDER = "editor_folding_provider"; //$NON-NLS-1$

	/**
	 * A named preference that stores the value for Javadoc folding for the
	 * default folding provider.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String EDITOR_FOLDING_JAVADOC = "editor_folding_default_javadoc"; //$NON-NLS-1$

	/**
	 * A named preference that stores the value for inner type folding for the
	 * default folding provider.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String EDITOR_FOLDING_INNERTYPES = "editor_folding_default_innertypes"; //$NON-NLS-1$

	/**
	 * A named preference that stores the value for method folding for the
	 * default folding provider.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String EDITOR_FOLDING_METHODS = "editor_folding_default_methods"; //$NON-NLS-1$

	/**
	 * A named preference that stores the value for imports folding for the
	 * default folding provider.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	// public static final String EDITOR_FOLDING_IMPORTS =
	// "editor_folding_default_imports"; //$NON-NLS-1$
	/**
	 * A named preference that stores the value for header comment folding for
	 * the default folding provider.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.1
	 */
	public static final String EDITOR_FOLDING_HEADERS = "editor_folding_default_headers"; //$NON-NLS-1$

	public static void initializeDefaultValues(IPreferenceStore store) {
		store.setDefault(PreferenceConstants.EDITOR_SHOW_SEGMENTS, false);

		// JavaBasePreferencePage
		store.setDefault(PreferenceConstants.LINK_PACKAGES_TO_EDITOR, true);
		store.setDefault(PreferenceConstants.LINK_TYPEHIERARCHY_TO_EDITOR,
				false);
		store
				.setDefault(PreferenceConstants.LINK_BROWSING_VIEW_TO_EDITOR,
						true);
		store.setDefault(PreferenceConstants.OPEN_TYPE_HIERARCHY,
				PreferenceConstants.OPEN_TYPE_HIERARCHY_IN_VIEW_PART);
		store.setDefault(PreferenceConstants.DOUBLE_CLICK,
				PreferenceConstants.DOUBLE_CLICK_EXPANDS);
		store.setDefault(PreferenceConstants.UPDATE_JAVA_VIEWS,
				PreferenceConstants.UPDATE_WHILE_EDITING);

		// AppearancePreferencePage
		store.setDefault(PreferenceConstants.APPEARANCE_COMPRESS_PACKAGE_NAMES,
				false);
		store.setDefault(PreferenceConstants.APPEARANCE_METHOD_RETURNTYPE,
				false);
		store.setDefault(PreferenceConstants.SHOW_CU_CHILDREN, true);
		store.setDefault(PreferenceConstants.APPEARANCE_OVERRIDE_INDICATOR,
				true);
		store.setDefault(PreferenceConstants.BROWSING_STACK_VERTICALLY, false);
		store.setDefault(
				PreferenceConstants.APPEARANCE_PKG_NAME_PATTERN_FOR_PKG_VIEW,
				""); //$NON-NLS-1$
		store
				.setDefault(
						PreferenceConstants.APPEARANCE_FOLD_PACKAGES_IN_PACKAGE_EXPLORER,
						true);

		// ImportOrganizePreferencePage
		// store.setDefault(PreferenceConstants.ORGIMPORTS_IMPORTORDER,
		// "php;phpx;org;com"); //$NON-NLS-1$
		store.setDefault(PreferenceConstants.ORGIMPORTS_ONDEMANDTHRESHOLD, 99);
		store.setDefault(PreferenceConstants.ORGIMPORTS_IGNORELOWERCASE, true);

		// ClasspathVariablesPreferencePage
		// CodeFormatterPreferencePage
		// CompilerPreferencePage
		// no initialization needed

		// RefactoringPreferencePage
		store.setDefault(
				PreferenceConstants.REFACTOR_ERROR_PAGE_SEVERITY_THRESHOLD,
				PreferenceConstants.REFACTOR_ERROR_SEVERITY);
		store.setDefault(PreferenceConstants.REFACTOR_SAVE_ALL_EDITORS, false);
		store.setDefault("RefactoringUI", "dialog");

		// TemplatePreferencePage
		store.setDefault(PreferenceConstants.TEMPLATES_USE_CODEFORMATTER, true);

		// CodeGenerationPreferencePage
		store.setDefault(PreferenceConstants.CODEGEN_USE_GETTERSETTER_PREFIX,
				false);
		store.setDefault(PreferenceConstants.CODEGEN_USE_GETTERSETTER_SUFFIX,
				false);
		store.setDefault(PreferenceConstants.CODEGEN_GETTERSETTER_PREFIX,
				"fg, f, _$, _, m_"); //$NON-NLS-1$
		store.setDefault(PreferenceConstants.CODEGEN_GETTERSETTER_SUFFIX, "_"); //$NON-NLS-1$

		store.setDefault(PreferenceConstants.CODEGEN_KEYWORD_THIS, false);
		store.setDefault(PreferenceConstants.CODEGEN_IS_FOR_GETTERS, true);
		store.setDefault(PreferenceConstants.CODEGEN_EXCEPTION_VAR_NAME, "e"); //$NON-NLS-1$
		store.setDefault(PreferenceConstants.CODEGEN_ADD_COMMENTS, true);
		store.setDefault(PreferenceConstants.CODEGEN__NON_JAVADOC_COMMENTS,
				false);
		store.setDefault(PreferenceConstants.CODEGEN__FILE_COMMENTS, false);

		// MembersOrderPreferencePage
		store.setDefault(PreferenceConstants.APPEARANCE_MEMBER_SORT_ORDER,
				"T,SF,SI,SM,I,F,C,M"); //$NON-NLS-1$
		store.setDefault(PreferenceConstants.APPEARANCE_VISIBILITY_SORT_ORDER,
				"B,V,R,D"); //$NON-NLS-1$
		store.setDefault(
				PreferenceConstants.APPEARANCE_ENABLE_VISIBILITY_SORT_ORDER,
				false);
		// must add here to guarantee that it is the first in the listener list
		store.addPropertyChangeListener(PHPeclipsePlugin.getDefault()
				.getMemberOrderPreferenceCache());

		store.setDefault(PreferenceConstants.EDITOR_MATCHING_BRACKETS, true);
		PreferenceConverter.setDefault(store,
				PreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR, new RGB(
						192, 192, 192));

		store.setDefault(PreferenceConstants.EDITOR_CURRENT_LINE, true);
		PreferenceConverter.setDefault(store,
				PreferenceConstants.EDITOR_CURRENT_LINE_COLOR, new RGB(225,
						235, 224));

		store.setDefault(PreferenceConstants.EDITOR_PRINT_MARGIN, false);
		store.setDefault(PreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN, 80);
		PreferenceConverter.setDefault(store,
				PreferenceConstants.EDITOR_PRINT_MARGIN_COLOR, new RGB(176,
						180, 185));

		PreferenceConverter.setDefault(store,
				PreferenceConstants.EDITOR_FIND_SCOPE_COLOR, new RGB(185, 176,
						180));

		// store.setDefault(PreferenceConstants.EDITOR_PROBLEM_INDICATION,
		// true);
		// PreferenceConverter.setDefault(store,
		// PreferenceConstants.EDITOR_PROBLEM_INDICATION_COLOR, new RGB(255, 0,
		// 128));
		// store.setDefault(PreferenceConstants.EDITOR_ERROR_INDICATION_IN_OVERVIEW_RULER,
		// true);
		//
		// store.setDefault(PreferenceConstants.EDITOR_WARNING_INDICATION,
		// true);
		// PreferenceConverter.setDefault(store,
		// PreferenceConstants.EDITOR_WARNING_INDICATION_COLOR, new RGB(244,
		// 200, 45));
		// store.setDefault(PreferenceConstants.EDITOR_WARNING_INDICATION_IN_OVERVIEW_RULER,
		// true);
		//
		// store.setDefault(PreferenceConstants.EDITOR_TASK_INDICATION, false);
		// PreferenceConverter.setDefault(store,
		// PreferenceConstants.EDITOR_TASK_INDICATION_COLOR, new RGB(0, 128,
		// 255));
		// store.setDefault(PreferenceConstants.EDITOR_TASK_INDICATION_IN_OVERVIEW_RULER,
		// false);
		//
		// store.setDefault(PreferenceConstants.EDITOR_BOOKMARK_INDICATION,
		// false);
		// PreferenceConverter.setDefault(store,
		// PreferenceConstants.EDITOR_BOOKMARK_INDICATION_COLOR, new RGB(34,
		// 164, 99));
		// store.setDefault(PreferenceConstants.EDITOR_BOOKMARK_INDICATION_IN_OVERVIEW_RULER,
		// false);
		//
		// store.setDefault(PreferenceConstants.EDITOR_SEARCH_RESULT_INDICATION,
		// false);
		// PreferenceConverter.setDefault(store,
		// PreferenceConstants.EDITOR_SEARCH_RESULT_INDICATION_COLOR, new
		// RGB(192, 192, 192));
		// store.setDefault(PreferenceConstants.EDITOR_SEARCH_RESULT_INDICATION_IN_OVERVIEW_RULER,
		// false);
		//
		// store.setDefault(PreferenceConstants.EDITOR_UNKNOWN_INDICATION,
		// false);
		// PreferenceConverter.setDefault(store,
		// PreferenceConstants.EDITOR_UNKNOWN_INDICATION_COLOR, new RGB(0, 0,
		// 0));
		// store.setDefault(PreferenceConstants.EDITOR_UNKNOWN_INDICATION_IN_OVERVIEW_RULER,
		// false);

		store
				.setDefault(PreferenceConstants.EDITOR_CORRECTION_INDICATION,
						true);
		store.setDefault(
				PreferenceConstants.EDITOR_SYNC_OUTLINE_ON_CURSOR_MOVE, true);

		store.setDefault(PreferenceConstants.EDITOR_EVALUTE_TEMPORARY_PROBLEMS,
				true);

		store.setDefault(PreferenceConstants.EDITOR_OVERVIEW_RULER, true);

		// WorkbenchChainedTextFontFieldEditor.startPropagate(store,
		// JFaceResources.TEXT_FONT);

		PreferenceConverter.setDefault(store,
				PreferenceConstants.EDITOR_LINKED_POSITION_COLOR, new RGB(0,
						200, 100));
		PreferenceConverter.setDefault(store,
				PreferenceConstants.EDITOR_LINK_COLOR, new RGB(0, 0, 255));

		store.setDefault(PreferenceConstants.EDITOR_FOREGROUND_DEFAULT_COLOR,
				true);

		store.setDefault(PreferenceConstants.EDITOR_BACKGROUND_DEFAULT_COLOR,
				true);

		store.setDefault(PreferenceConstants.EDITOR_TAB_WIDTH, 4);
		store.setDefault(PreferenceConstants.EDITOR_SPACES_FOR_TABS, false);

		PreferenceConverter.setDefault(store,
				PreferenceConstants.EDITOR_MULTI_LINE_COMMENT_COLOR, new RGB(
						63, 127, 95));
		store.setDefault(PreferenceConstants.EDITOR_MULTI_LINE_COMMENT_BOLD,
				false);

		PreferenceConverter.setDefault(store,
				PreferenceConstants.EDITOR_SINGLE_LINE_COMMENT_COLOR, new RGB(
						63, 127, 95));
		store.setDefault(PreferenceConstants.EDITOR_SINGLE_LINE_COMMENT_BOLD,
				false);

		PreferenceConverter.setDefault(store,
				PreferenceConstants.EDITOR_PHP_TAG_COLOR, new RGB(255, 0, 128));
		store.setDefault(PreferenceConstants.EDITOR_PHP_TAG_BOLD, true);

		PreferenceConverter.setDefault(store,
				PreferenceConstants.EDITOR_JAVA_KEYWORD_COLOR, new RGB(127, 0,
						85));
		store.setDefault(PreferenceConstants.EDITOR_JAVA_KEYWORD_BOLD, true);

		PreferenceConverter.setDefault(store,
				PreferenceConstants.EDITOR_PHP_FUNCTIONNAME_COLOR, new RGB(127,
						127, 159));
		store.setDefault(PreferenceConstants.EDITOR_PHP_FUNCTIONNAME_BOLD,
				false);

		PreferenceConverter.setDefault(store,
				PreferenceConstants.EDITOR_PHP_VARIABLE_COLOR, new RGB(127,
						159, 191));
		store.setDefault(PreferenceConstants.EDITOR_PHP_VARIABLE_BOLD, false);

		PreferenceConverter.setDefault(store,
				PreferenceConstants.EDITOR_PHP_VARIABLE_DOLLAR_COLOR, new RGB(
						127, 159, 191));
		store.setDefault(PreferenceConstants.EDITOR_PHP_VARIABLE_DOLLAR_BOLD,
				false);

		PreferenceConverter.setDefault(store,
				PreferenceConstants.EDITOR_PHP_CONSTANT_COLOR, new RGB(127, 0,
						85));
		store.setDefault(PreferenceConstants.EDITOR_PHP_CONSTANT_BOLD, false);

		PreferenceConverter.setDefault(store,
				PreferenceConstants.EDITOR_PHP_TYPE_COLOR, new RGB(127, 0, 85));
		store.setDefault(PreferenceConstants.EDITOR_PHP_TYPE_BOLD, false);

		PreferenceConverter.setDefault(store,
				PreferenceConstants.EDITOR_STRING_COLOR_DQ,
				PHPColorProvider.STRING_DQ);
		store.setDefault(PreferenceConstants.EDITOR_STRING_BOLD_DQ, false);

		PreferenceConverter.setDefault(store,
				PreferenceConstants.EDITOR_STRING_COLOR_SQ,
				PHPColorProvider.STRING_SQ);
		store.setDefault(PreferenceConstants.EDITOR_STRING_BOLD_SQ, true);

		PreferenceConverter
				.setDefault(store,
						PreferenceConstants.EDITOR_JAVA_DEFAULT_COLOR, new RGB(
								0, 0, 0));
		store.setDefault(PreferenceConstants.EDITOR_JAVA_DEFAULT_BOLD, false);

		PreferenceConverter.setDefault(store,
				PreferenceConstants.EDITOR_JAVADOC_KEYWORD_COLOR, new RGB(127,
						159, 191));
		store.setDefault(PreferenceConstants.EDITOR_JAVADOC_KEYWORD_BOLD, true);

		PreferenceConverter.setDefault(store,
				PreferenceConstants.EDITOR_JAVADOC_TAG_COLOR, new RGB(127, 127,
						159));
		store.setDefault(PreferenceConstants.EDITOR_JAVADOC_TAG_BOLD, false);

		PreferenceConverter.setDefault(store,
				PreferenceConstants.EDITOR_JAVADOC_LINKS_COLOR, new RGB(63, 63,
						191));
		store.setDefault(PreferenceConstants.EDITOR_JAVADOC_LINKS_BOLD, false);

		PreferenceConverter.setDefault(store,
				PreferenceConstants.EDITOR_JAVADOC_DEFAULT_COLOR, new RGB(63,
						95, 191));
		store
				.setDefault(PreferenceConstants.EDITOR_JAVADOC_DEFAULT_BOLD,
						false);

		store.setDefault(PreferenceConstants.CODEASSIST_AUTOACTIVATION, true);
		store.setDefault(PreferenceConstants.CODEASSIST_AUTOACTIVATION_DELAY,
				500);

		store.setDefault(PreferenceConstants.CODEASSIST_AUTOINSERT, true);
		PreferenceConverter.setDefault(store,
				PreferenceConstants.CODEASSIST_PROPOSALS_BACKGROUND, new RGB(
						254, 241, 233));
		PreferenceConverter.setDefault(store,
				PreferenceConstants.CODEASSIST_PROPOSALS_FOREGROUND, new RGB(0,
						0, 0));
		PreferenceConverter.setDefault(store,
				PreferenceConstants.CODEASSIST_PARAMETERS_BACKGROUND, new RGB(
						254, 241, 233));
		PreferenceConverter.setDefault(store,
				PreferenceConstants.CODEASSIST_PARAMETERS_FOREGROUND, new RGB(
						0, 0, 0));
		PreferenceConverter.setDefault(store,
				PreferenceConstants.CODEASSIST_REPLACEMENT_BACKGROUND, new RGB(
						255, 255, 0));
		PreferenceConverter.setDefault(store,
				PreferenceConstants.CODEASSIST_REPLACEMENT_FOREGROUND, new RGB(
						255, 0, 0));
		store.setDefault(
				PreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS_JAVA,
				"$>"); //$NON-NLS-1$
		store.setDefault(
				PreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS_JAVADOC,
				"@"); //$NON-NLS-1$
		store.setDefault(
				PreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS_HTML,
				"<&#"); //$NON-NLS-1$
		store.setDefault(PreferenceConstants.CODEASSIST_SHOW_VISIBLE_PROPOSALS,
				true);
		store
				.setDefault(PreferenceConstants.CODEASSIST_CASE_SENSITIVITY,
						false);
		store.setDefault(PreferenceConstants.CODEASSIST_ORDER_PROPOSALS, false);
		store.setDefault(PreferenceConstants.CODEASSIST_ADDIMPORT, true);
		store
				.setDefault(PreferenceConstants.CODEASSIST_INSERT_COMPLETION,
						true);
		store.setDefault(PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES,
				false);
		store.setDefault(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS,
				true);
		store.setDefault(PreferenceConstants.CODEASSIST_PREFIX_COMPLETION,
				false);

		store.setDefault(PreferenceConstants.EDITOR_SMART_HOME_END, true);
		store.setDefault(PreferenceConstants.EDITOR_SUB_WORD_NAVIGATION, true);
		store.setDefault(PreferenceConstants.EDITOR_SMART_PASTE, true);
		store.setDefault(PreferenceConstants.EDITOR_CLOSE_STRINGS_DQ_PHP, true);
		store.setDefault(PreferenceConstants.EDITOR_CLOSE_STRINGS_SQ_PHP, true);
		store.setDefault(PreferenceConstants.EDITOR_CLOSE_BRACKETS_PHP, true);
		store.setDefault(PreferenceConstants.EDITOR_CLOSE_BRACES, true);
		store.setDefault(PreferenceConstants.EDITOR_CLOSE_JAVADOCS, true);
		store.setDefault(PreferenceConstants.EDITOR_WRAP_WORDS, false);
		store.setDefault(PreferenceConstants.EDITOR_WRAP_STRINGS_DQ, true);
		store.setDefault(PreferenceConstants.EDITOR_ESCAPE_STRINGS_DQ, false);
		store.setDefault(PreferenceConstants.EDITOR_WRAP_STRINGS_SQ, true);
		store.setDefault(PreferenceConstants.EDITOR_ESCAPE_STRINGS_SQ, false);
		store.setDefault(PreferenceConstants.EDITOR_ADD_JAVADOC_TAGS, true);
		store.setDefault(PreferenceConstants.EDITOR_FORMAT_JAVADOCS, false);
		store.setDefault(PreferenceConstants.EDITOR_DISABLE_OVERWRITE_MODE,
				false);

		store.setDefault(PreferenceConstants.EDITOR_CLOSE_STRINGS_HTML, true);
		store.setDefault(PreferenceConstants.EDITOR_CLOSE_BRACKETS_HTML, true);

		// store.setDefault(PreferenceConstants.EDITOR_DEFAULT_HOVER,
		// JavaPlugin.ID_BESTMATCH_HOVER);
		store.setDefault(PreferenceConstants.EDITOR_NONE_HOVER,
				PreferenceConstants.EDITOR_DEFAULT_HOVER_CONFIGURED_ID);
		// store.setDefault(PreferenceConstants.EDITOR_CTRL_HOVER,
		// JavaPlugin.ID_SOURCE_HOVER);
		store.setDefault(PreferenceConstants.EDITOR_SHIFT_HOVER,
				PreferenceConstants.EDITOR_DEFAULT_HOVER_CONFIGURED_ID);
		store.setDefault(PreferenceConstants.EDITOR_CTRL_SHIFT_HOVER,
				PreferenceConstants.EDITOR_DEFAULT_HOVER_CONFIGURED_ID);
		store.setDefault(PreferenceConstants.EDITOR_CTRL_ALT_HOVER,
				PreferenceConstants.EDITOR_DEFAULT_HOVER_CONFIGURED_ID);
		store.setDefault(PreferenceConstants.EDITOR_ALT_SHIFT_HOVER,
				PreferenceConstants.EDITOR_DEFAULT_HOVER_CONFIGURED_ID);
		store.setDefault(PreferenceConstants.EDITOR_CTRL_ALT_SHIFT_HOVER,
				PreferenceConstants.EDITOR_DEFAULT_HOVER_CONFIGURED_ID);

		int modifier = SWT.CTRL;
		if (Platform.getOS().equals(Platform.OS_MACOSX))
			modifier = SWT.COMMAND;
		String ctrl = Action.findModifierString(modifier);
		store
				.setDefault(
						PreferenceConstants.EDITOR_TEXT_HOVER_MODIFIERS,
						"net.sourceforge.phpdt.ui.BestMatchHover;0;net.sourceforge.phpdt.ui.JavaSourceHover;" + ctrl); //$NON-NLS-1$
		store
				.setDefault(
						PreferenceConstants.EDITOR_TEXT_HOVER_MODIFIER_MASKS,
						"net.sourceforge.phpdt.ui.BestMatchHover;0;net.sourceforge.phpdt.ui.JavaSourceHover;" + modifier); //$NON-NLS-1$
		store.setDefault(PreferenceConstants.EDITOR_SHOW_TEXT_HOVER_AFFORDANCE,
				true);

		store.setDefault(PreferenceConstants.EDITOR_BROWSER_LIKE_LINKS, true);
		store.setDefault(
				PreferenceConstants.EDITOR_BROWSER_LIKE_LINKS_KEY_MODIFIER,
				ctrl);
		store
				.setDefault(
						PreferenceConstants.EDITOR_BROWSER_LIKE_LINKS_KEY_MODIFIER_MASK,
						modifier);

		// mark occurrences
		store.setDefault(PreferenceConstants.EDITOR_MARK_OCCURRENCES, true);
		store.setDefault(PreferenceConstants.EDITOR_STICKY_OCCURRENCES, true);
		// store.setDefault(PreferenceConstants.EDITOR_MARK_TYPE_OCCURRENCES,
		// true);
		// store.setDefault(PreferenceConstants.EDITOR_MARK_METHOD_OCCURRENCES,
		// true);
		// store.setDefault(PreferenceConstants.EDITOR_MARK_CONSTANT_OCCURRENCES,
		// true);
		// store.setDefault(PreferenceConstants.EDITOR_MARK_FIELD_OCCURRENCES,
		// true);
		// store.setDefault(PreferenceConstants.EDITOR_MARK_LOCAL_VARIABLE_OCCURRENCES,
		// true);
		// store.setDefault(PreferenceConstants.EDITOR_MARK_EXCEPTION_OCCURRENCES,
		// true);
		// store.setDefault(PreferenceConstants.EDITOR_MARK_METHOD_EXIT_POINTS,
		// true);
		// store.setDefault(PreferenceConstants.EDITOR_MARK_IMPLEMENTORS, true);

		// spell checking
		store.setDefault(PreferenceConstants.SPELLING_CHECK_SPELLING, false);
		store.setDefault(PreferenceConstants.SPELLING_LOCALE, SpellCheckEngine
				.getDefaultLocale().toString());
		store.setDefault(PreferenceConstants.SPELLING_IGNORE_DIGITS, true);
		store.setDefault(PreferenceConstants.SPELLING_IGNORE_MIXED, true);
		store.setDefault(PreferenceConstants.SPELLING_IGNORE_SENTENCE, true);
		store.setDefault(PreferenceConstants.SPELLING_IGNORE_UPPER, true);
		store.setDefault(PreferenceConstants.SPELLING_IGNORE_URLS, true);
		store.setDefault(PreferenceConstants.SPELLING_USER_DICTIONARY, ""); //$NON-NLS-1$
		store.setDefault(PreferenceConstants.SPELLING_PROPOSAL_THRESHOLD, 20);
		store.setDefault(PreferenceConstants.SPELLING_ENABLE_CONTENTASSIST,
				false);

		// folding
		store.setDefault(PreferenceConstants.EDITOR_FOLDING_ENABLED, true);
		store.setDefault(PreferenceConstants.EDITOR_FOLDING_PROVIDER,
				"net.sourceforge.phpdt.ui.text.defaultFoldingProvider"); //$NON-NLS-1$
		store.setDefault(PreferenceConstants.EDITOR_FOLDING_JAVADOC, true);
		store.setDefault(PreferenceConstants.EDITOR_FOLDING_INNERTYPES, true);
		store.setDefault(PreferenceConstants.EDITOR_FOLDING_METHODS, false);
		// store.setDefault(PreferenceConstants.EDITOR_FOLDING_IMPORTS, false);
		store.setDefault(PreferenceConstants.EDITOR_FOLDING_HEADERS, true);

		store.setDefault(PreferenceConstants.EDITOR_SMART_BACKSPACE, true);
		store.setDefault(PreferenceConstants.EDITOR_P_RTRIM_ON_SAVE, false);
		// do more complicated stuff
		// NewJavaProjectPreferencePage.initDefaults(store);
	}

	/**
	 * Returns the JDT-UI preference store.
	 * 
	 * @return the JDT-UI preference store
	 */
	public static IPreferenceStore getPreferenceStore() {
		return PHPeclipsePlugin.getDefault().getPreferenceStore();
	}

	// /**
	// * Encodes a JRE library to be used in the named preference
	// <code>NEWPROJECT_JRELIBRARY_LIST</code>.
	// *
	// * @param description a string value describing the JRE library. The
	// description is used
	// * to indentify the JDR library in the UI
	// * @param entries an array of classpath entries to be encoded
	// *
	// * @return the encoded string.
	// */
	// public static String encodeJRELibrary(String description,
	// IClasspathEntry[] entries) {
	// return NewJavaProjectPreferencePage.encodeJRELibrary(description,
	// entries);
	// }
	//
	// /**
	// * Decodes an encoded JRE library and returns its description string.
	// *
	// * @return the description of an encoded JRE library
	// *
	// * @see #encodeJRELibrary(String, IClasspathEntry[])
	// */
	// public static String decodeJRELibraryDescription(String encodedLibrary) {
	// return
	// NewJavaProjectPreferencePage.decodeJRELibraryDescription(encodedLibrary);
	// }
	//
	// /**
	// * Decodes an encoded JRE library and returns its classpath entries.
	// *
	// * @return the array of classpath entries of an encoded JRE library.
	// *
	// * @see #encodeJRELibrary(String, IClasspathEntry[])
	// */
	// public static IClasspathEntry[] decodeJRELibraryClasspathEntries(String
	// encodedLibrary) {
	// return
	// NewJavaProjectPreferencePage.decodeJRELibraryClasspathEntries(encodedLibrary);
	// }
	//
	// /**
	// * Returns the current configuration for the JRE to be used as default in
	// new Java projects.
	// * This is a convenience method to access the named preference
	// <code>NEWPROJECT_JRELIBRARY_LIST
	// * </code> with the index defined by <code>
	// NEWPROJECT_JRELIBRARY_INDEX</code>.
	// *
	// * @return the current default set of classpath entries
	// *
	// * @see #NEWPROJECT_JRELIBRARY_LIST
	// * @see #NEWPROJECT_JRELIBRARY_INDEX
	// */
	// public static IClasspathEntry[] getDefaultJRELibrary() {
	// return NewJavaProjectPreferencePage.getDefaultJRELibrary();
	// }
}