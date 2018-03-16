/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package net.sourceforge.phpdt.internal.corext.template.php;

// import net.sourceforge.phpdt.core.ICompilationUnit;

import net.sourceforge.phpdt.core.ICompilationUnit;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateVariable;

/**
 * A context type for javadoc.
 */
public class HTMLContextType extends CompilationUnitContextType {

	/**
	 * Creates a java context type.
	 */
	public HTMLContextType() {
		super("html"); //$NON-NLS-1$

		// global
		addResolver(new GlobalTemplateVariables.Cursor());
		addResolver(new GlobalTemplateVariables.WordSelection());
		addResolver(new GlobalTemplateVariables.LineSelection());
		addResolver(new GlobalTemplateVariables.Dollar());
		addResolver(new GlobalTemplateVariables.Date());
		addResolver(new GlobalTemplateVariables.Year());
		addResolver(new GlobalTemplateVariables.Time());
		addResolver(new GlobalTemplateVariables.User());

		addResolver(new File());
	}

	/*
	 * @see ContextType#createContext()
	 */
	public CompilationUnitContext createContext(IDocument document, int offset,
			int length, ICompilationUnit compilationUnit) {
		return new HTMLUnitContext(this, document, offset, length,
				compilationUnit);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.phpdt.internal.corext.template.java.CompilationUnitContextType#createContext(org.eclipse.jface.text.IDocument,
	 *      int, int, net.sourceforge.phpdt.core.ICompilationUnit)
	 */
	// public CompilationUnitContext createContext(IDocument document, int
	// offset, int length, ICompilationUnit compilationUnit) {
	// return new JavaDocContext(this, document, offset, length,
	// compilationUnit);
	// }
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.phpdt.internal.corext.template.ContextType#validateVariables(net.sourceforge.phpdt.internal.corext.template.TemplateVariable[])
	 */
	protected void validateVariables(TemplateVariable[] variables)
			throws TemplateException {
		// check for multiple cursor variables
		for (int i = 0; i < variables.length; i++) {
			TemplateVariable var = variables[i];
			if (var.getType().equals(GlobalTemplateVariables.Cursor.NAME)) {
				if (var.getOffsets().length > 1) {
					throw new TemplateException(
							JavaTemplateMessages
									.getString("ContextType.error.multiple.cursor.variables")); //$NON-NLS-1$
				}
			}
		}
	}
}
