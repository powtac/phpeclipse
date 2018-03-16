/***********************************************************************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **********************************************************************************************************************************/
package net.sourceforge.phpdt.internal.compiler.ast;

import net.sourceforge.phpdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import net.sourceforge.phpdt.internal.compiler.codegen.CaseLabel;
import net.sourceforge.phpdt.internal.compiler.flow.FlowContext;
import net.sourceforge.phpdt.internal.compiler.flow.FlowInfo;
import net.sourceforge.phpdt.internal.compiler.impl.Constant;
import net.sourceforge.phpdt.internal.compiler.lookup.BlockScope;
import net.sourceforge.phpdt.internal.compiler.lookup.TypeBinding;

public class DefaultCase extends Statement {

	public CaseLabel targetLabel;

	/**
	 * DefautCase constructor comment.
	 */
	public DefaultCase(int sourceEnd, int sourceStart) {

		this.sourceStart = sourceStart;
		this.sourceEnd = sourceEnd;
	}

	public FlowInfo analyseCode(BlockScope currentScope,
			FlowContext flowContext, FlowInfo flowInfo) {

		return flowInfo;
	}

	/**
	 * Default case code generation
	 * 
	 * @param currentScope
	 *            net.sourceforge.phpdt.internal.compiler.lookup.BlockScope
	 * @param codeStream
	 *            net.sourceforge.phpdt.internal.compiler.codegen.CodeStream
	 */
	// public void generateCode(BlockScope currentScope, CodeStream codeStream)
	// {
	//
	// if ((bits & IsReachableMASK) == 0) {
	// return;
	// }
	// int pc = codeStream.position;
	// targetLabel.place();
	// codeStream.recordPositionsFrom(pc, this.sourceStart);
	//
	// }
	/**
	 * No-op : should use resolveCase(...) instead.
	 */
	public void resolve(BlockScope scope) {
	}

	public Constant resolveCase(BlockScope scope, TypeBinding testType,
			SwitchStatement switchStatement) {

		// remember the default case into the associated switch statement
		if (switchStatement.defaultCase != null)
			scope.problemReporter().duplicateDefaultCase(this);

		// on error the last default will be the selected one .... (why not)
		// ....
		switchStatement.defaultCase = this;
		resolve(scope);
		return null;
	}

	public StringBuffer printStatement(int tab, StringBuffer output) {

		printIndent(tab, output);
		output.append("default : "); //$NON-NLS-1$
		return output.append(';');
	}

	public String toString(int tab) {

		String s = tabString(tab);
		s = s + "default : "; //$NON-NLS-1$
		return s;
	}

	public void traverse(IAbstractSyntaxTreeVisitor visitor,
			BlockScope blockScope) {

		visitor.visit(this, blockScope);
		visitor.endVisit(this, blockScope);
	}
}