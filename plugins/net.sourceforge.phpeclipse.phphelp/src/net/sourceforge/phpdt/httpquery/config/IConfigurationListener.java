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
public interface IConfigurationListener {
	public void configurationAdded(IConfiguration monitor);

	public void configurationChanged(IConfiguration monitor);

	public void configurationRemoved(IConfiguration monitor);
}