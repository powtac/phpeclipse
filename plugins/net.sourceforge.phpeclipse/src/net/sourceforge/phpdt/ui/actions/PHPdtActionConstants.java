/*******************************************************************************
 * Copyright (c) 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package net.sourceforge.phpdt.ui.actions;

/**
 * Action ids for standard actions, for groups in the menu bar, and for actions
 * in context menus of PHPDT views.
 * 
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @since 2.0
 */
public class PHPdtActionConstants {

	// Edit menu
	/**
	 * Edit menu: name of standard Show Javadoc global action (value
	 * <code>"net.sourceforge.phpdt.ui.actions.ShowJavaDoc"</code>).
	 */
	public static final String SHOW_JAVA_DOC = "net.sourceforge.phpeclipse.phpeditor.ShowJavaDoc"; //$NON-NLS-1$

	/**
	 * Edit menu: name of standard Code Assist global action (value
	 * <code>"org.phpeclipse.phpdt.ui.actions.ContentAssist"</code>).
	 */
	public static final String CONTENT_ASSIST = "net.sourceforge.phpeclipse.phpeditor.ContentAssist"; //$NON-NLS-1$

	// Source menu

	/**
	 * Source menu: name of standard Comment global action (value
	 * <code>"net.sourceforge.phpdt.ui.actions.Comment"</code>).
	 */
	public static final String COMMENT = "net.sourceforge.phpeclipse.phpeditor.Comment"; //$NON-NLS-1$

	/**
	 * Source menu: name of standard Uncomment global action (value
	 * <code>"net.sourceforge.phpdt.ui.actions.Uncomment"</code>).
	 */
	public static final String UNCOMMENT = "net.sourceforge.phpeclipse.phpeditor.Uncomment"; //$NON-NLS-1$

	/**
	 * Source menu: name of standard ToggleComment global action (value
	 * <code>"net.sourceforge.phpdt.ui.actions.ToggleComment"</code>).
	 * 
	 * @since 3.0
	 */
	public static final String TOGGLE_COMMENT = "net.sourceforge.phpeclipse.ui.actions.ToggleComment"; //$NON-NLS-1$

	/**
	 * Source menu: name of standard Comment global action (value
	 * <code>"net.sourceforge.phpdt.ui.actions.AddBlockComment"</code>).
	 * 
	 * @since 3.0
	 */
	public static final String ADD_BLOCK_COMMENT = "net.sourceforge.phpeclipse.ui.actions.AddBlockComment"; //$NON-NLS-1$

	/**
	 * Source menu: name of standard Uncomment global action (value
	 * <code>"net.sourceforge.phpdt.ui.actions.RemoveBlockComment"</code>).
	 * 
	 * @since 3.0
	 */
	public static final String REMOVE_BLOCK_COMMENT = "net.sourceforge.phpeclipse.ui.actions.RemoveBlockComment"; //$NON-NLS-1$

	/**
	 * Source menu: name of standard Indent global action (value
	 * <code>"net.sourceforge.phpdt.ui.actions.Indent"</code>).
	 * 
	 * @since 3.0
	 */
	public static final String INDENT = "net.sourceforge.phpeclipse.ui.actions.Indent"; //$NON-NLS-1$

	/**
	 * Source menu: name of standard Shift Rightl action (value
	 * <code>"net.sourceforge.phpeclipse.phpeditor.ShiftRight"</code>).
	 */
	public static final String SHIFT_RIGHT = "net.sourceforge.phpeclipse.phpeditor.ShiftRight"; //$NON-NLS-1$

	/**
	 * Source menu: name of standard Shift Left global action (value
	 * <code>"net.sourceforge.phpeclipse.phpeditor.ShiftLeft"</code>).
	 */
	public static final String SHIFT_LEFT = "net.sourceforge.phpeclipse.phpeditor.ShiftLeft"; //$NON-NLS-1$

	/**
	 * Source menu: name of standard Format global action (value <code>"org.
	 * phpeclipse.phpdt.ui.actions.Format"</code>).
	 */
	public static final String FORMAT = "net.sourceforge.phpeclipse.phpeditor.Format"; //$NON-NLS-1$

	/**
	 * Source menu: name of standard Convert Line Delimiters To Windows global
	 * action (value
	 * <code>"org.phpeclipse.phpdt.ui.actions.ConvertLineDelimitersToWindows"</code>).
	 */
	public static String CONVERT_LINE_DELIMITERS_TO_WINDOWS = "net.sourceforge.phpeclipse.ui.actions.ConvertLineDelimitersToWindows"; //$NON-NLS-1$

	/**
	 * Source menu: name of standard Convert Line Delimiters To UNIX global
	 * action (value
	 * <code>"org.phpeclipse.phpdt.ui.actions.ConvertLineDelimitersToUNIX"</code>).
	 */
	public static String CONVERT_LINE_DELIMITERS_TO_UNIX = "net.sourceforge.phpeclipse.ui.actions.ConvertLineDelimitersToUNIX"; //$NON-NLS-1$

	/**
	 * Source menu: name of standardConvert Line Delimiters ToMac global action
	 * (value
	 * <code>"org.phpeclipse.phpdt.ui.actions.ConvertLineDelimitersToMac"</code>).
	 */
	public static String CONVERT_LINE_DELIMITERS_TO_MAC = "net.sourceforge.phpeclipse.ui.actions.ConvertLineDelimitersToMac"; //$NON-NLS-1$

	/**
	 * Navigate menu: name of standard Open global action (value
	 * <code>"org.phpeclipse.phpdt.ui.actions.Open"</code>).
	 */
	public static final String OPEN = "net.sourceforge.phpeclipse.ui.actions.Open"; //$NON-NLS-1$

}
