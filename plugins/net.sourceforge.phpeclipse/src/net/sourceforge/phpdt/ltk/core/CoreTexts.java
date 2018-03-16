// Copyright (c) 2005 by Leif Frenzel. All rights reserved.
// See http://leiffrenzel.de
// modified for phpeclipse.de project by axelcl
package net.sourceforge.phpdt.ltk.core;

import org.eclipse.osgi.util.NLS;

/**
 * <p>
 * provides internationalized messages Strings from the coretexts resource
 * bundle.
 * </p>
 * 
 */
public class CoreTexts extends NLS {

	private static final String BUNDLE_NAME = "net.sourceforge.phpdt.ltk.core.coretexts"; //$NON-NLS-1$

	static {
		NLS.initializeMessages(BUNDLE_NAME, CoreTexts.class);
	}

	// message fields
	public static String renamePropertyProcessor_name;

	public static String renamePropertyDelegate_noSourceFile;

	public static String renamePropertyDelegate_roFile;

	public static String renamePropertyDelegate_noPHPKey;

	public static String renamePropertyDelegate_collectingChanges;

	public static String renamePropertyDelegate_checking;
}