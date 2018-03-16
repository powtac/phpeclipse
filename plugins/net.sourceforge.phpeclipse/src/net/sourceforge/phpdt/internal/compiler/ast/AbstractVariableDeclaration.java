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
package net.sourceforge.phpdt.internal.compiler.ast;

import net.sourceforge.phpdt.internal.compiler.flow.FlowContext;
import net.sourceforge.phpdt.internal.compiler.flow.FlowInfo;
import net.sourceforge.phpdt.internal.compiler.lookup.BlockScope;

public abstract class AbstractVariableDeclaration extends Statement {
	public int modifiers;

	public TypeReference type;

	public Expression initialization;

	public char[] name;

	public int declarationEnd;

	public int declarationSourceStart;

	public int declarationSourceEnd;

	public int modifiersSourceStart;

	public AbstractVariableDeclaration() {
	}

	public FlowInfo analyseCode(BlockScope currentScope,
			FlowContext flowContext, FlowInfo flowInfo) {
		return flowInfo;
	}

	public abstract String name();

	public StringBuffer printStatement(int indent, StringBuffer output) {

		printIndent(indent, output);
		printModifiers(this.modifiers, output);
		type.print(0, output).append(' ').append(this.name);
		if (initialization != null) {
			output.append(" = "); //$NON-NLS-1$
			initialization.printExpression(indent, output);
		}
		return output.append(';');
	}

	public void resolve(BlockScope scope) {
	}

	public String toString(int tab) {

		String s = tabString(tab);
		if (modifiers != AccDefault) {
			s += modifiersString(modifiers);
		}
		s += type.toString(0) + " " + new String(name()); //$NON-NLS-1$
		if (initialization != null)
			s += " = " + initialization.toStringExpression(tab); //$NON-NLS-1$
		return s;
	}
}
