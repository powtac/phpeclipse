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
package net.sourceforge.phpdt.internal.corext.template.php;

import net.sourceforge.phpdt.core.ICompilationUnit;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;

/**
 * A context type for javadoc.
 */
public class JavaDocContextType extends CompilationUnitContextType {

	public static final String NAME = "phpdoc"; //$NON-NLS-1$

	/**
	 * Creates a java context type.
	 */
	public JavaDocContextType() {
		super(NAME);

		// global
		addResolver(new GlobalTemplateVariables.Cursor());
		addResolver(new GlobalTemplateVariables.LineSelection());
		addResolver(new GlobalTemplateVariables.WordSelection());
		addResolver(new GlobalTemplateVariables.Dollar());
		addResolver(new GlobalTemplateVariables.Date());
		addResolver(new GlobalTemplateVariables.Year());
		addResolver(new GlobalTemplateVariables.Time());
		addResolver(new GlobalTemplateVariables.User());

		// compilation unit
		addResolver(new File());
		addResolver(new PrimaryTypeName());
		addResolver(new Method());
		addResolver(new ReturnType());
		addResolver(new Arguments());
		addResolver(new Type());
		addResolver(new Package());
		addResolver(new Project());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.phpdt.internal.corext.template.java.CompilationUnitContextType#createContext(org.eclipse.jface.text.IDocument,
	 *      int, int, net.sourceforge.phpdt.core.ICompilationUnit)
	 */
	public CompilationUnitContext createContext(IDocument document, int offset,
			int length, ICompilationUnit compilationUnit) {
		return new JavaDocContext(this, document, offset, length,
				compilationUnit);
	}

}
