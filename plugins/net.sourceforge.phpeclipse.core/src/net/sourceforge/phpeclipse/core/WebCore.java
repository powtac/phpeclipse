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
 * $Id: WebCore.java,v 1.2 2006-10-21 23:14:29 pombredanne Exp $
 */

package net.sourceforge.phpeclipse.core;

import org.eclipse.core.runtime.Plugin;

// import net.sourceforge.phpeclipse.core.parser.ISourceParser;

/**
 * The main plugin class to be used in the desktop.
 */
public class WebCore extends Plugin {

	// Instance Variables ------------------------------------------------------

	/** The shared instance. */
	private static WebCore plugin;

	// Constructors ------------------------------------------------------------

	/**
	 * The constructor.
	 */
	public WebCore() {
		plugin = this;
	}

	// Public Methods ----------------------------------------------------------

	/**
	 * Returns the shared instance.
	 */
	public static WebCore getDefault() {
		return plugin;
	}

	/**
	 * Creates and returns a parser that can handle resources of the specified
	 * MIME type.
	 * 
	 * @param mimeType
	 *            the MIME type of the resource for which a parser should be
	 *            created
	 * @return the instantiated parser, or <tt>null</tt> if no parser for that
	 *         MIME type is registered
	 */
	// public ISourceParser createParser(String mimeType) {
	// return null;
	// }
}
