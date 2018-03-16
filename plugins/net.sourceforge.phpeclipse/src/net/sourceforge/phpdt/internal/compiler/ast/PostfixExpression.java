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
import net.sourceforge.phpdt.internal.compiler.lookup.BlockScope;

public class PostfixExpression extends CompoundAssignment {

	public PostfixExpression(Expression l, Expression e, int op, int pos) {

		super(l, e, op, pos);
		this.sourceStart = l.sourceStart;
		this.sourceEnd = pos;
	}

	/**
	 * Code generation for PostfixExpression
	 * 
	 * @param currentScope
	 *            net.sourceforge.phpdt.internal.compiler.lookup.BlockScope
	 * @param codeStream
	 *            net.sourceforge.phpdt.internal.compiler.codegen.CodeStream
	 * @param valueRequired
	 *            boolean
	 */
	// public void generateCode(
	// BlockScope currentScope,
	// CodeStream codeStream,
	// boolean valueRequired) {
	//
	// // various scenarii are possible, setting an array reference,
	// // a field reference, a blank final field reference, a field of an
	// enclosing instance or
	// // just a local variable.
	//
	// int pc = codeStream.position;
	// ((Reference) lhs).generatePostIncrement(currentScope, codeStream, this,
	// valueRequired);
	// if (valueRequired) {
	// codeStream.generateImplicitConversion(implicitConversion);
	// }
	// codeStream.recordPositionsFrom(pc, this.sourceStart);
	// }
	public String operatorToString() {
		switch (operator) {
		case PLUS:
			return "++"; //$NON-NLS-1$
		case MINUS:
			return "--"; //$NON-NLS-1$
		}
		return "unknown operator"; //$NON-NLS-1$
	}

	public boolean restrainUsageToNumericTypes() {

		return true;
	}

	public String toStringExpressionNoParenthesis() {

		return lhs.toStringExpression() + " " + operatorToString(); //$NON-NLS-1$
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {

		if (visitor.visit(this, scope)) {
			lhs.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}
