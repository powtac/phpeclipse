/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.phpdt.internal.compiler.util;

public interface SuffixConstants {
	// public final static String EXTENSION_class = "class"; //$NON-NLS-1$
	// public final static String EXTENSION_CLASS = "CLASS"; //$NON-NLS-1$
	public final static String EXTENSION_php = "php"; //$NON-NLS-1$

	public final static String EXTENSION_PHP = "PHP"; //$NON-NLS-1$

	// public final static String SUFFIX_STRING_class = "." + EXTENSION_class;
	// //$NON-NLS-1$
	// public final static String SUFFIX_STRING_CLASS = "." + EXTENSION_CLASS;
	// //$NON-NLS-1$
	public final static String SUFFIX_STRING_php = "." + EXTENSION_php; //$NON-NLS-1$

	public final static String SUFFIX_STRING_PHP = "." + EXTENSION_PHP; //$NON-NLS-1$

	// public final static char[] SUFFIX_class =
	// SUFFIX_STRING_class.toCharArray();
	// public final static char[] SUFFIX_CLASS =
	// SUFFIX_STRING_CLASS.toCharArray();
	public final static char[] SUFFIX_php = SUFFIX_STRING_php.toCharArray();

	public final static char[] SUFFIX_PHP = SUFFIX_STRING_PHP.toCharArray();

	// public final static String EXTENSION_jar = "jar"; //$NON-NLS-1$
	// public final static String EXTENSION_JAR = "JAR"; //$NON-NLS-1$
	// public final static String EXTENSION_zip = "zip"; //$NON-NLS-1$
	// public final static String EXTENSION_ZIP = "ZIP"; //$NON-NLS-1$

	// public final static String SUFFIX_STRING_jar = "." + EXTENSION_jar;
	// //$NON-NLS-1$
	// public final static String SUFFIX_STRING_JAR = "." + EXTENSION_JAR;
	// //$NON-NLS-1$
	// public final static String SUFFIX_STRING_zip = "." + EXTENSION_zip;
	// //$NON-NLS-1$
	// public final static String SUFFIX_STRING_ZIP = "." + EXTENSION_ZIP;
	// //$NON-NLS-1$

	// public final static char[] SUFFIX_jar = SUFFIX_STRING_jar.toCharArray();
	// public final static char[] SUFFIX_JAR = SUFFIX_STRING_JAR.toCharArray();
	// public final static char[] SUFFIX_zip = SUFFIX_STRING_zip.toCharArray();
	// public final static char[] SUFFIX_ZIP = SUFFIX_STRING_ZIP.toCharArray();
}