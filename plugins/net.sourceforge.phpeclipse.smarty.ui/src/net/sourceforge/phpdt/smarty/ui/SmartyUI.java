/*
 * Copyright (c) 2004 Christopher Lenz and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Christopher Lenz - initial API and implementation
 * 
 * $Id: SmartyUI.java,v 1.1 2004-09-03 17:31:18 jsurfer Exp $
 */

package net.sourceforge.phpdt.smarty.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main plugin class to be used in the desktop.
 */
public class SmartyUI extends AbstractUIPlugin {

	// Class Variables ---------------------------------------------------------

	/** The shared instance. */
	private static SmartyUI plugin;

	// Constructors ------------------------------------------------------------

	/**
	 * The constructor.
	 */
	public SmartyUI() {
		plugin = this;
	}

	// Public Methods ----------------------------------------------------------

	/**
	 * Returns the shared instance.
	 */
	public static SmartyUI getDefault() {
		return plugin;
	}
}
