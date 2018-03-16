/**
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. � This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 �*
 * Contributors:
 *    IBM - Initial API and implementation
 */
package net.sourceforge.phpeclipse.webbrowser.internal;

/**
 * 
 */
public class Favorite {
	protected String url;

	protected String name;

	public Favorite() {
	}

	public Favorite(String name, String url) {
		if (name == null)
			name = "";
		if (url == null)
			url = "";
		this.name = name;
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getURL() {
		return url;
	}

	public void setURL(String url) {
		this.url = url;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Favorite))
			return false;

		Favorite f = (Favorite) obj;
		return (name.equals(f.name) && url.equals(f.url));
	}

	public String toString() {
		return "(" + name + "/" + url + ")";
	}
}