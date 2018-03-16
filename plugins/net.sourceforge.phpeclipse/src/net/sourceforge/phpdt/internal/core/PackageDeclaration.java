/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.phpdt.internal.core;

import net.sourceforge.phpdt.core.IJavaElement;
import net.sourceforge.phpdt.core.IPackageDeclaration;
import net.sourceforge.phpdt.core.JavaModelException;
import net.sourceforge.phpdt.core.jdom.IDOMNode;

/**
 * @see IPackageDeclaration
 */

/* package */class PackageDeclaration extends SourceRefElement implements
		IPackageDeclaration {
	protected PackageDeclaration(CompilationUnit parent, String name) {
		super(parent, name);
	}

	public boolean equals(Object o) {
		if (!(o instanceof PackageDeclaration))
			return false;
		return super.equals(o);
	}

	/**
	 * @see JavaElement#equalsDOMNode
	 */
	protected boolean equalsDOMNode(IDOMNode node) throws JavaModelException {
		return (node.getNodeType() == IDOMNode.PACKAGE)
				&& getElementName().equals(node.getName());
	}

	/**
	 * @see IJavaElement
	 */
	public int getElementType() {
		return PACKAGE_DECLARATION;
	}

	/**
	 * @see JavaElement#getHandleMemento()
	 */
	protected char getHandleMementoDelimiter() {
		return JavaElement.JEM_PACKAGEDECLARATION;
	}

	/**
	 * @private Debugging purposes
	 */
	protected void toStringInfo(int tab, StringBuffer buffer, Object info) {
		buffer.append(this.tabString(tab));
		buffer.append("package "); //$NON-NLS-1$
		buffer.append(getElementName());
		if (info == null) {
			buffer.append(" (not open)"); //$NON-NLS-1$
		}
	}
}
