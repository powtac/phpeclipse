/***********************************************************************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **********************************************************************************************************************************/
package net.sourceforge.phpdt.internal.compiler.ast;

import net.sourceforge.phpdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import net.sourceforge.phpdt.internal.compiler.flow.FlowContext;
import net.sourceforge.phpdt.internal.compiler.flow.FlowInfo;
import net.sourceforge.phpdt.internal.compiler.lookup.BlockScope;
import net.sourceforge.phpdt.internal.compiler.lookup.MethodScope;
import net.sourceforge.phpdt.internal.compiler.lookup.TypeBinding;

public class ThisReference extends Reference {

	public static ThisReference implicitThis() {

		ThisReference implicitThis = new ThisReference(0, 0);
		implicitThis.bits |= IsImplicitThisMask;
		return implicitThis;
	}

	public ThisReference(int sourceStart, int sourceEnd) {

		this.sourceStart = sourceStart;
		this.sourceEnd = sourceEnd;
	}

	/*
	 * @see Reference#analyseAssignment(...)
	 */
	public FlowInfo analyseAssignment(BlockScope currentScope,
			FlowContext flowContext, FlowInfo flowInfo, Assignment assignment,
			boolean isCompound) {

		return flowInfo; // this cannot be assigned
	}

	public boolean checkAccess(MethodScope methodScope) {

		// this/super cannot be used in constructor call
		if (methodScope.isConstructorCall) {
			methodScope.problemReporter()
					.fieldsOrThisBeforeConstructorInvocation(this);
			return false;
		}

		// static may not refer to this/super
		if (methodScope.isStatic) {
			methodScope.problemReporter().errorThisSuperInStatic(this);
			return false;
		}
		return true;
	}

	/*
	 * @see Reference#generateAssignment(...)
	 */
	// public void generateAssignment(BlockScope currentScope, CodeStream
	// codeStream, Assignment assignment, boolean valueRequired) {
	//
	// // this cannot be assigned
	// }
	//
	// public void generateCode(BlockScope currentScope, CodeStream codeStream,
	// boolean valueRequired) {
	//	
	// int pc = codeStream.position;
	// if (valueRequired)
	// codeStream.aload_0();
	// if ((this.bits & IsImplicitThisMask) == 0)
	// codeStream.recordPositionsFrom(pc, this.sourceStart);
	// }
	//
	// /*
	// * @see Reference#generateCompoundAssignment(...)
	// */
	// public void generateCompoundAssignment(BlockScope currentScope,
	// CodeStream codeStream, Expression expression, int operator, int
	// assignmentImplicitConversion, boolean valueRequired) {
	//
	// // this cannot be assigned
	// }
	//	
	// /*
	// * @see
	// net.sourceforge.phpdt.internal.compiler.ast.Reference#generatePostIncrement()
	// */
	// public void generatePostIncrement(BlockScope currentScope, CodeStream
	// codeStream, CompoundAssignment postIncrement, boolean
	// valueRequired) {
	//
	// // this cannot be assigned
	// }
	public boolean isImplicitThis() {

		return (this.bits & IsImplicitThisMask) != 0;
	}

	public boolean isThis() {

		return true;
	}

	public TypeBinding resolveType(BlockScope scope) {

		constant = NotAConstant;
		if (!this.isImplicitThis() && !checkAccess(scope.methodScope()))
			return null;
		return this.resolvedType = scope.enclosingSourceType();
	}

	public StringBuffer printExpression(int indent, StringBuffer output) {

		if (this.isImplicitThis())
			return output;
		return output.append("this"); //$NON-NLS-1$
	}

	public String toStringExpression() {

		if (this.isImplicitThis())
			return ""; //$NON-NLS-1$
		return "this"; //$NON-NLS-1$
	}

	public void traverse(IAbstractSyntaxTreeVisitor visitor,
			BlockScope blockScope) {

		visitor.visit(this, blockScope);
		visitor.endVisit(this, blockScope);
	}
}