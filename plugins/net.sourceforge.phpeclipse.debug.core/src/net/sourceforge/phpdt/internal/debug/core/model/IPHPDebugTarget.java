/**********************************************************************
 Copyright (c) 2000, 2002 IBM Corp. and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html

 Contributors:
 IBM Corporation - Initial implementation
 Vicente Fernando - www.alfersoft.com.ar
 **********************************************************************/
package net.sourceforge.phpdt.internal.debug.core.model;

import net.sourceforge.phpdt.internal.debug.core.PHPDBGProxy;

import org.eclipse.debug.core.model.IDebugTarget;

public interface IPHPDebugTarget extends IDebugTarget {

	public final static String MODEL_IDENTIFIER = "net.sourceforge.phpdt.debug.core";

	public void terminate();

	public void setPHPDBGProxy(PHPDBGProxy phpDBGProxy);

	public void addThread(PHPThread phpThread);
}
