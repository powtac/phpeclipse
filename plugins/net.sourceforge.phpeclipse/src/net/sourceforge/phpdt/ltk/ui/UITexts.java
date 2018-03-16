// Copyright (c) 2005 by Leif Frenzel. All rights reserved.
// See http://leiffrenzel.de
// modified for phpeclipse.de project by axelcl
package net.sourceforge.phpdt.ltk.ui;

import org.eclipse.osgi.util.NLS;

/**
 * <p>
 * provides internationalized String messages for the UI.
 * </p>
 * 
 */
public class UITexts {

	private static final String BUNDLE_NAME = "net.sourceforge.phpdt.ltk.ui.uitexts"; //$NON-NLS-1$

	static {
		NLS.initializeMessages(BUNDLE_NAME, UITexts.class);
	}

	// message fields
	public static String renameProperty_refuseDlg_title;

	public static String renameProperty_refuseDlg_message;

	public static String renamePropertyInputPage_lblNewName;

	public static String renamePropertyInputPage_cbUpdateBundle;

	public static String renamePropertyInputPage_cbAllProjects;

	public static String renameLocalVariable_refuseDlg_title;

	public static String renameLocalVariable_refuseDlg_message;

	public static String renameLocalVariable_cbDQStrings;

	public static String renameLocalVariable_cbPHPdoc;

	public static String renameLocalVariable_cbOtherDoc;

}
