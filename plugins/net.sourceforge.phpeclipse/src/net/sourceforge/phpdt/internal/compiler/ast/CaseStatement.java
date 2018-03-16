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

import net.sourceforge.phpdt.internal.compiler.ASTVisitor;
import net.sourceforge.phpdt.internal.compiler.codegen.CaseLabel;
import net.sourceforge.phpdt.internal.compiler.flow.FlowContext;
import net.sourceforge.phpdt.internal.compiler.flow.FlowInfo;
import net.sourceforge.phpdt.internal.compiler.impl.Constant;
import net.sourceforge.phpdt.internal.compiler.lookup.BlockScope;
import net.sourceforge.phpdt.internal.compiler.lookup.TypeBinding;

public class CaseStatement extends Statement {

	public Expression constantExpression;

	public CaseLabel targetLabel;

	public CaseStatement(int sourceStart, Expression constantExpression) {
		this.constantExpression = constantExpression;
		this.sourceEnd = constantExpression.sourceEnd;
		this.sourceStart = sourceStart;
	}

	public FlowInfo analyseCode(BlockScope currentScope,
			FlowContext flowContext, FlowInfo flowInfo) {

		if (constantExpression.constant == NotAConstant)
			currentScope.problemReporter().caseExpressionMustBeConstant(
					constantExpression);

		this.constantExpression
				.analyseCode(currentScope, flowContext, flowInfo);
		return flowInfo;
	}

	/**
	 * Case code generation
	 * 
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
	// }
	public StringBuffer printStatement(int tab, StringBuffer output) {

		printIndent(tab, output);
		if (constantExpression == null) {
			output.append("default : "); //$NON-NLS-1$
		} else {
			output.append("case "); //$NON-NLS-1$
			constantExpression.printExpression(0, output).append(" : "); //$NON-NLS-1$
		}
		return output.append(';');
	}

	/**
	 * No-op : should use resolveCase(...) instead.
	 */
	public void resolve(BlockScope scope) {
	}

	public Constant resolveCase(BlockScope scope, TypeBinding switchType,
			SwitchStatement switchStatement) {

		// add into the collection of cases of the associated switch statement
		switchStatement.cases[switchStatement.caseCount++] = this;
		TypeBinding caseType = constantExpression.resolveType(scope);
		if (caseType == null || switchType == null)
			return null;
		if (constantExpression.isConstantValueOfTypeAssignableToType(caseType,
				switchType))
			return constantExpression.constant;
		if (caseType.isCompatibleWith(switchType))
			return constantExpression.constant;
		scope.problemReporter().typeMismatchErrorActualTypeExpectedType(
				constantExpression, caseType, switchType);
		return null;
	}

	public String toString(int tab) {

		String s = tabString(tab);
		s = s + "case " + constantExpression.toStringExpression() + " : "; //$NON-NLS-1$ //$NON-NLS-2$
		return s;
	}

	public void traverse(ASTVisitor visitor, BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			constantExpression.traverse(visitor, blockScope);
		}
		visitor.endVisit(this, blockScope);
	}
}
