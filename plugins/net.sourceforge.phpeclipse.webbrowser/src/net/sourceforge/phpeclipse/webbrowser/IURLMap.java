/**
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 �*
 * Contributors:
 *    IBM - Initial API and implementation
 */
package net.sourceforge.phpeclipse.webbrowser;

/**
 * Interface for mapping special URLs to other locations.
 */
public interface IURLMap {
	/**
	 * Returns a mapped URL
	 * 
	 * @param url
	 *            java.lang.String
	 * @return java.lang.String
	 */
	public String getMappedURL(String url);
}