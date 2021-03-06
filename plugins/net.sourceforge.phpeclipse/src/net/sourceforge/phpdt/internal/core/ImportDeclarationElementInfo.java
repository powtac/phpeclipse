/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.phpdt.internal.core;

import net.sourceforge.phpdt.internal.compiler.env.ISourceImport;

/**
 * Element info for IImportDeclaration elements.
 * 
 * @see net.sourceforge.phpdt.core.IImportDeclaration
 */
public class ImportDeclarationElementInfo extends MemberElementInfo implements
		ISourceImport {

	// record if import is on demand, the import name doesn't have trailing
	// start
	boolean onDemand;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.phpdt.internal.compiler.env.ISourceImport#onDemand()
	 */
	public boolean onDemand() {
		return this.onDemand;
	}

	public void setOnDemand(boolean onDemand) {
		this.onDemand = onDemand;
	}
}
