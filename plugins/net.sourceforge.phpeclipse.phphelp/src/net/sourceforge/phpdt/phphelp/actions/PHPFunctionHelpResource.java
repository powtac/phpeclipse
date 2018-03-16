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
package net.sourceforge.phpdt.phphelp.actions;

import org.eclipse.help.IHelpResource;

/**
 * 
 */
public class PHPFunctionHelpResource implements IHelpResource {

	private String word;

	public PHPFunctionHelpResource(String word) {
		this.word = word;
	}

	/**
	 * Get standard PHPEclipse html help URL
	 * 
	 * @return String
	 */
	public String getHref() {
		return "/net.sourceforge.phpeclipse.phphelp/doc/function." + word
				+ ".html";
	}

	public String getLabel() {
		return "PHP Context Help";
	}

}