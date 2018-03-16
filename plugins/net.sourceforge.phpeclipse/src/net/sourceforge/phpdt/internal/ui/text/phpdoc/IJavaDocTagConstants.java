/*****************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *****************************************************************************/

package net.sourceforge.phpdt.internal.ui.text.phpdoc;

/**
 * Javadoc tag constants.
 * 
 * @since 3.0
 */
public interface IJavaDocTagConstants {

	/** Javadoc break tags */
	public static final String[] JAVADOC_BREAK_TAGS = new String[] {
			"dd", "dt", "li", "td", "th", "tr", "h1", "h2", "h3", "h4", "h5", "h6", "q" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$

	/** Javadoc single break tag */
	public static final String[] JAVADOC_SINGLE_BREAK_TAG = new String[] { "br" }; //$NON-NLS-1$

	/** Javadoc code tags */
	public static final String[] JAVADOC_CODE_TAGS = new String[] { "pre" }; //$NON-NLS-1$

	/** Javadoc general tags */
	public static final String[] JAVADOC_GENERAL_TAGS = new String[] {
			"@author", "@deprecated", "@exception", "@link", "@param", "@return", "@see", "@since", "@throws", "@value", "@version", "@license", "@abstract", "@access", "@category",
			"@copyright", "@example", "@final", "@filesource", "@global", "@ignore", "@internal", "@link", "@method", "@name", "@package", "@param", "@property", "@static",
			"@staticvar", "@subpackage", "@todo", "@tutorial", "@uses", "@var","@id", "inheritdoc", "@property-read", "@property-write", "@source"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$ //$NON-NLS-14$ //$NON-NLS-15$ //$NON-NLS-16$ //$NON-NLS-17$ //$NON-NLS-18$

	/** Javadoc immutable tags */
	public static final String[] JAVADOC_IMMUTABLE_TAGS = new String[] {
			"code", "em", "pre", "q", "tt" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

	/** Javadoc link tags */
	public static final String[] JAVADOC_LINK_TAGS = new String[] {
			"@docRoot", "@inheritDoc", "@link", "@linkplain" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	/** Javadoc new line tags */
	public static final String[] JAVADOC_NEWLINE_TAGS = new String[] {
			"dd", "dt", "li", "td", "th", "tr", "h1", "h2", "h3", "h4", "h5", "h6", "q" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$

	/** Javadoc parameter tags */
	public static final String[] JAVADOC_PARAM_TAGS = new String[] {
			"@exception", "@param", "@serialField", "@throws" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	/** Javadoc reference tags */
	public static final String[] JAVADOC_REFERENCE_TAGS = new String[] { "@see" }; //$NON-NLS-1$

	/** Javadoc root tags */
	public static final String[] JAVADOC_ROOT_TAGS = new String[] {
			"@author", "@deprecated", "@return", "@see", "@serial", "@serialData", "@since", "@version", "@inheritDoc" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$

	/** Javadoc separator tags */
	public static final String[] JAVADOC_SEPARATOR_TAGS = new String[] {
			"dl", "hr", "nl", "p", "pre", "ul", "ol" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$

	/** Javadoc tag prefix */
	public static final char JAVADOC_TAG_PREFIX = '@';
}
