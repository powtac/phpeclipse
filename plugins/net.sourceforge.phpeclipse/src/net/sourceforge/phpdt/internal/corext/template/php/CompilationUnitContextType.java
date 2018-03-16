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
import net.sourceforge.phpdt.core.IJavaElement;
import net.sourceforge.phpdt.core.IMethod;
import net.sourceforge.phpdt.core.JavaModelException;
import net.sourceforge.phpdt.core.Signature;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

/**
 * Compilation unit context type.
 */
public abstract class CompilationUnitContextType extends TemplateContextType {

	protected static class ReturnType extends TemplateVariableResolver {
		public ReturnType() {
			super(
					"return_type", JavaTemplateMessages.getString("CompilationUnitContextType.variable.description.return.type")); //$NON-NLS-1$ //$NON-NLS-2$
		}

		protected String resolve(TemplateContext context) {
			IJavaElement element = ((CompilationUnitContext) context)
					.findEnclosingElement(IJavaElement.METHOD);
			if (element == null)
				return null;

			try {
				return Signature.toString(((IMethod) element).getReturnType());
			} catch (JavaModelException e) {
				return null;
			}
		}
	}

	protected static class File extends TemplateVariableResolver {
		public File() {
			super(
					"file", JavaTemplateMessages.getString("CompilationUnitContextType.variable.description.file")); //$NON-NLS-1$ //$NON-NLS-2$
		}

		protected String resolve(TemplateContext context) {
			ICompilationUnit unit = ((CompilationUnitContext) context)
					.getCompilationUnit();

			return (unit == null) ? null : unit.getElementName();
		}

		/*
		 * @see org.eclipse.jface.text.templates.TemplateVariableResolver#isUnambiguous(org.eclipse.jface.text.templates.TemplateContext)
		 */
		protected boolean isUnambiguous(TemplateContext context) {
			return resolve(context) != null;
		}
	}

	protected static class PrimaryTypeName extends TemplateVariableResolver {
		public PrimaryTypeName() {
			super(
					"primary_type_name", JavaTemplateMessages.getString("CompilationUnitContextType.variable.description.primary.type.name")); //$NON-NLS-1$ //$NON-NLS-2$

		}

		protected String resolve(TemplateContext context) {
			ICompilationUnit unit = ((CompilationUnitContext) context)
					.getCompilationUnit();
			if (unit == null)
				return null;
			String elementName = unit.getElementName();
			return elementName.substring(0, elementName.lastIndexOf('.'));
		}

		/*
		 * @see org.eclipse.jface.text.templates.TemplateVariableResolver#isUnambiguous(org.eclipse.jface.text.templates.TemplateContext)
		 */
		protected boolean isUnambiguous(TemplateContext context) {
			return resolve(context) != null;
		}
	}

	protected static class EnclosingJavaElement extends
			TemplateVariableResolver {
		protected final int fElementType;

		public EnclosingJavaElement(String name, String description,
				int elementType) {
			super(name, description);
			fElementType = elementType;
		}

		protected String resolve(TemplateContext context) {
			IJavaElement element = ((CompilationUnitContext) context)
					.findEnclosingElement(fElementType);
			return (element == null) ? null : element.getElementName();
		}

		/*
		 * @see org.eclipse.jface.text.templates.TemplateVariableResolver#isUnambiguous(org.eclipse.jface.text.templates.TemplateContext)
		 */
		protected boolean isUnambiguous(TemplateContext context) {
			return resolve(context) != null;
		}
	}

	protected static class Method extends EnclosingJavaElement {
		public Method() {
			super(
					"enclosing_method", JavaTemplateMessages.getString("CompilationUnitContextType.variable.description.enclosing.method"), IJavaElement.METHOD); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	protected static class Type extends EnclosingJavaElement {
		public Type() {
			super(
					"enclosing_type", JavaTemplateMessages.getString("CompilationUnitContextType.variable.description.enclosing.type"), IJavaElement.TYPE); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/*
	 * protected static class SuperClass extends EnclosingJavaElement { public
	 * Type() { super("super_class",
	 * TemplateMessages.getString("JavaContextType.variable.description.type"),
	 * IJavaElement.TYPE); } }
	 */
	protected static class Package extends EnclosingJavaElement {
		public Package() {
			super(
					"enclosing_package", JavaTemplateMessages.getString("CompilationUnitContextType.variable.description.enclosing.package"), IJavaElement.PACKAGE_FRAGMENT); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	protected static class Project extends EnclosingJavaElement {
		public Project() {
			super(
					"enclosing_project", JavaTemplateMessages.getString("CompilationUnitContextType.variable.description.enclosing.project"), IJavaElement.JAVA_PROJECT); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/*
	 * protected static class Project2 extends TemplateVariableResolver { public
	 * Project2() { super("project",
	 * TemplateMessages.getString("JavaContextType.variable.description.project")); }
	 * public String evaluate(TemplateContext context) { ICompilationUnit unit=
	 * ((JavaContext) context).getUnit(); return (unit == null) ? null :
	 * unit.getJavaProject().getElementName(); } }
	 */
	protected static class Arguments extends TemplateVariableResolver {
		public Arguments() {
			super(
					"enclosing_method_arguments", JavaTemplateMessages.getString("CompilationUnitContextType.variable.description.enclosing.method.arguments")); //$NON-NLS-1$ //$NON-NLS-2$
		}

		protected String resolve(TemplateContext context) {
			IJavaElement element = ((CompilationUnitContext) context)
					.findEnclosingElement(IJavaElement.METHOD);
			if (element == null)
				return null;

			IMethod method = (IMethod) element;

			try {
				String[] arguments = method.getParameterNames();
				StringBuffer buffer = new StringBuffer();

				for (int i = 0; i < arguments.length; i++) {
					if (i > 0)
						buffer.append(", "); //$NON-NLS-1$
					buffer.append(arguments[i]);
				}

				return buffer.toString();

			} catch (JavaModelException e) {
				return null;
			}
		}
	}

	/*
	 * protected static class Line extends TemplateVariableResolver { public
	 * Line() { super("line",
	 * TemplateMessages.getString("CompilationUnitContextType.variable.description.line")); }
	 * public String evaluate(TemplateContext context) { return
	 * ((JavaTemplateContext) context).guessLineNumber(); } }
	 */

	/*
	 * @see ContextType#ContextType(String)
	 */
	public CompilationUnitContextType(String name) {
		super(name);
	}

	public abstract CompilationUnitContext createContext(IDocument document,
			int completionPosition, int length, ICompilationUnit compilationUnit);

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
