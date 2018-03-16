/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 �*
 * Contributors:
 *    IBM - Initial API and implementation
 **********************************************************************/
package net.sourceforge.phpdt.httpquery.config;

/**
 * 
 */
public interface IConfigurationWorkingCopy extends IConfiguration {
	public void setId(String id);

	public void setName(String name);

	public void setURL(String url);

	public void setPassword(String port);

	public void setUser(String port);

	public void setType(String type);

	public IConfiguration save();
}