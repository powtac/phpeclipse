/**********************************************************************
 Copyright (c) 2000, 2002 IBM Corp. and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html

 Contributors:
 Vicente Fernando - www.alfersoft.com.ar - Initial implementation
 **********************************************************************/
package net.sourceforge.phpdt.internal.debug.core;

public class PHPDBGMod {
	private int modNo;

	private String modName;

	public PHPDBGMod() {
	}

	public PHPDBGMod(int modNo, String modName) {
		this.modNo = modNo;
		this.modName = modName;
	}

	public int getNo() {
		return modNo;
	}

	public String getName() {
		return modName;
	}

	public void setNo(int modNo) {
		this.modNo = modNo;
	}

	public void setName(String modName) {
		this.modName = modName;
	}
}