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
package net.sourceforge.phpeclipse.phpeditor.php;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class PHPEditorMessages {

	private static final String RESOURCE_BUNDLE = "net.sourceforge.phpeclipse.phpeditor.PHPEditorMessages"; //$NON-NLS-1$

	// private static ResourceBundle fgResourceBundle = null;
	private static ResourceBundle fgResourceBundle = ResourceBundle
			.getBundle(RESOURCE_BUNDLE);

	// ResourceBundle.getBundle(RESOURCE_BUNDLE);

	private PHPEditorMessages() {
		// if (fgResourceBundle == null) {
		// try {
		// fgResourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE);
		// } catch (MissingResourceException x) {
		// fgResourceBundle = null;
		// }
		// }
	}

	public static String getString(String key) {
		try {
			return fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!"; //$NON-NLS-2$ //$NON-NLS-1$
		}
	}
}
