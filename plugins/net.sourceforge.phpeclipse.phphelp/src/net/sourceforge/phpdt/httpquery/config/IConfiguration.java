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
public interface IConfiguration {
	public String getId();

	public String getName();

	public String getURL();

	public String getType();

	public String getPassword();

	public String getUser();

	public IConfigurationWorkingCopy getWorkingCopy();

	public boolean isActive();

	public void delete();

}