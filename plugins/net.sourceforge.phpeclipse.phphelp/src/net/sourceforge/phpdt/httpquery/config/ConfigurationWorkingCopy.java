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
public class ConfigurationWorkingCopy extends Configuration implements
		IConfigurationWorkingCopy {
	protected Configuration configuration;

	// creation
	public ConfigurationWorkingCopy() {
	}

	// working copy
	public ConfigurationWorkingCopy(Configuration configuration) {
		this.configuration = configuration;
		setInternal(configuration);
	}

	public void setId(String newId) {
		fId = newId;
	}

	public void setName(String name) {
		fName = name;
	}

	public void setURL(String url) {
		fUrl = url;
	}

	public void setPassword(String password) {
		fPassword = password;
	}

	public void setUser(String user) {
		fUser = user;
	}

	public void setType(String t) {
		fType = t;
	}

	public boolean isWorkingCopy() {
		return true;
	}

	public IConfigurationWorkingCopy getWorkingCopy() {
		return this;
	}

	public IConfiguration save() {
		ConfigurationManager mm = ConfigurationManager.getInstance();
		if (configuration != null) {
			configuration.setInternal(this);
			mm.configurationChanged(configuration);
		} else {
			configuration = new Configuration();
			configuration.setInternal(this);
			mm.addConfiguration(configuration);
		}
		return configuration;
	}
}