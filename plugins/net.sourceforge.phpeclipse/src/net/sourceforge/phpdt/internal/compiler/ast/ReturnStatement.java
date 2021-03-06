/***********************************************************************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **********************************************************************************************************************************/
package net.sourceforge.phpdt.internal.compiler.ast;

import net.sourceforge.phpdt.internal.compiler.ASTVisitor;
import net.sourceforge.phpdt.internal.compiler.flow.FlowContext;
import net.sourceforge.phpdt.internal.compiler.flow.FlowInfo;
import net.sourceforge.phpdt.internal.compiler.flow.InitializationFlowContext;
import net.sourceforge.phpdt.internal.compiler.lookup.BlockScope;
import net.sourceforge.phpdt.internal.compiler.lookup.LocalVariableBinding;
import net.sourceforge.phpdt.internal.compiler.lookup.MethodBinding;
import net.sourceforge.phpdt.internal.compiler.lookup.MethodScope;
import net.sourceforge.phpdt.internal.compiler.lookup.TypeBinding;

public class ReturnStatement extends Statement {
	public Expression expression;

	public TypeBinding expressionType;

	public boolean isSynchronized;

	public ASTNode[] subroutines;

	public LocalVariableBinding saveValueVariable;

	public ReturnStatement(Expression expr, int s, int e) {
		sourceStart = s;
		sourceEnd = e;
		expression = expr;
	}

	public FlowInfo analyseCode(BlockScope currentScope,
			FlowContext flowContext, FlowInfo flowInfo) { // here requires to
															// generate a
		// sequence of finally blocks
		// invocations depending
		// corresponding
		// to each of the traversed try statements, so that execution will
		// terminate properly.

		// lookup the label, this should answer the returnContext

		if (expression != null) {
			flowInfo = expression.analyseCode(currentScope, flowContext,
					flowInfo);
		}
		// compute the return sequence (running the finally blocks)
		FlowContext traversedContext = flowContext;
		int subIndex = 0, maxSub = 5;
		boolean saveValueNeeded = false;
		boolean hasValueToSave = expression != null
				&& expression.constant == NotAConstant;
		do {
			ASTNode sub;
			if ((sub = traversedContext.subRoutine()) != null) {
				if (this.subroutines == null) {
					this.subroutines = new ASTNode[maxSub];
				}
				if (subIndex == maxSub) {
					System.arraycopy(this.subroutines, 0,
							(this.subroutines = new ASTNode[maxSub *= 2]), 0,
							subIndex); // grow
				}
				this.subroutines[subIndex++] = sub;
				if (sub.cannotReturn()) {
					saveValueNeeded = false;
					break;
				}
			}
			traversedContext.recordReturnFrom(flowInfo.unconditionalInits());

			ASTNode node = traversedContext.associatedNode;
			// if ((node = traversedContext.associatedNode) instanceof
			// SynchronizedStatement) {
			// isSynchronized = true;
			//
			// } else
			if (node instanceof TryStatement) {
				TryStatement tryStatement = (TryStatement) node;
				flowInfo.addInitializationsFrom(tryStatement.subRoutineInits); // collect
																				// inits
				if (hasValueToSave) {
					if (this.saveValueVariable == null) { // closest
															// subroutine secret
															// variable is used
						prepareSaveValueLocation(tryStatement);
					}
					saveValueNeeded = true;
				}

			} else if (traversedContext instanceof InitializationFlowContext) {
				currentScope.problemReporter().cannotReturnInInitializer(this);
				return FlowInfo.DEAD_END;
			}
		} while ((traversedContext = traversedContext.parent) != null);

		// resize subroutines
		if ((subroutines != null) && (subIndex != maxSub)) {
			System.arraycopy(subroutines, 0,
					(subroutines = new ASTNode[subIndex]), 0, subIndex);
		}

		// secret local variable for return value (note that this can only occur
		// in a real method)
		if (saveValueNeeded) {
			if (this.saveValueVariable != null) {
				this.saveValueVariable.useFlag = LocalVariableBinding.USED;
			}
		} else {
			this.saveValueVariable = null;
			if ((!isSynchronized) && (expressionType == BooleanBinding)) {
				this.expression.bits |= ValueForReturnMASK;
			}
		}
		return FlowInfo.DEAD_END;
	}

	/**
	 * Retrun statement code generation
	 * 
	 * generate the finallyInvocationSequence.
	 * 
	 * @param currentScope
	 *            net.sourceforge.phpdt.internal.compiler.lookup.BlockScope
	 * @param codeStream
	 *            net.sourceforge.phpdt.internal.compiler.codegen.CodeStream
	 */
	// public void generateCode(BlockScope currentScope, CodeStream codeStream)
	// {
	// if ((bits & IsReachableMASK) == 0) {
	// return;
	// }
	// int pc = codeStream.position;
	// // generate the expression
	// if ((expression != null) && (expression.constant == NotAConstant)) {
	// expression.generateCode(currentScope, codeStream, needValue()); // no
	// value needed if non-returning subroutine
	// generateStoreSaveValueIfNecessary(codeStream);
	// }
	//	
	// // generation of code responsible for invoking the finally blocks in
	// sequence
	// if (subroutines != null) {
	// for (int i = 0, max = subroutines.length; i < max; i++) {
	// ASTNode sub;
	// if ((sub = subroutines[i]) instanceof SynchronizedStatement) {
	// codeStream.load(((SynchronizedStatement) sub).synchroVariable);
	// codeStream.monitorexit();
	// } else {
	// TryStatement trySub = (TryStatement) sub;
	// if (trySub.subRoutineCannotReturn) {
	// codeStream.goto_(trySub.subRoutineStartLabel);
	// codeStream.recordPositionsFrom(pc, this.sourceStart);
	// return;
	// } else {
	// codeStream.jsr(trySub.subRoutineStartLabel);
	// }
	// }
	// }
	// }
	// if (saveValueVariable != null) codeStream.load(saveValueVariable);
	//	
	// if ((expression != null) && (expression.constant != NotAConstant)) {
	// codeStream.generateConstant(expression.constant,
	// expression.implicitConversion);
	// generateStoreSaveValueIfNecessary(codeStream);
	// }
	// // output the suitable return bytecode or wrap the value inside a
	// descriptor for doits
	// this.generateReturnBytecode(codeStream);
	//	
	// codeStream.recordPositionsFrom(pc, this.sourceStart);
	// }
	/**
	 * Dump the suitable return bytecode for a return statement
	 * 
	 */
	// public void generateReturnBytecode(CodeStream codeStream) {
	//
	// if (expression == null) {
	// codeStream.return_();
	// } else {
	// switch (expression.implicitConversion >> 4) {
	// case T_boolean :
	// case T_int :
	// codeStream.ireturn();
	// break;
	// case T_float :
	// codeStream.freturn();
	// break;
	// case T_long :
	// codeStream.lreturn();
	// break;
	// case T_double :
	// codeStream.dreturn();
	// break;
	// default :
	// codeStream.areturn();
	// }
	// }
	// }
	// public void generateStoreSaveValueIfNecessary(CodeStream codeStream){
	// if (saveValueVariable != null) codeStream.store(saveValueVariable,
	// false);
	// }
	public boolean needValue() {
		return (subroutines == null) || (saveValueVariable != null)
				|| isSynchronized;
	}

	public void prepareSaveValueLocation(TryStatement targetTryStatement) {

		this.saveValueVariable = targetTryStatement.secretReturnValue;
	}

	public void resolve(BlockScope scope) {
		MethodScope methodScope = scope.methodScope();
		MethodBinding methodBinding;
		TypeBinding methodType = (methodScope.referenceContext instanceof AbstractMethodDeclaration) ? ((methodBinding = ((AbstractMethodDeclaration) methodScope.referenceContext).binding) == null ? null
				: methodBinding.returnType)
				: VoidBinding;
		if (methodType == VoidBinding) {
			// the expression should be null
			if (expression == null)
				return;
			if ((expressionType = expression.resolveType(scope)) != null)
				scope.problemReporter().attemptToReturnNonVoidExpression(this,
						expressionType);
			return;
		}
		if (expression == null) {
			if (methodType != null)
				scope.problemReporter().shouldReturn(methodType, this);
			return;
		}
		if ((expressionType = expression.resolveType(scope)) == null)
			return;

		if (methodType != null
				&& expression.isConstantValueOfTypeAssignableToType(
						expressionType, methodType)) {
			// dealing with constant
			expression.implicitWidening(methodType, expressionType);
			return;
		}
		if (expressionType == VoidBinding) {
			scope.problemReporter().attemptToReturnVoidValue(this);
			return;
		}
		if (methodType != null && expressionType.isCompatibleWith(methodType)) {
			expression.implicitWidening(methodType, expressionType);
			return;
		}
		if (methodType != null) {
			scope.problemReporter().typeMismatchErrorActualTypeExpectedType(
					expression, expressionType, methodType);
		}
	}

	public StringBuffer printStatement(int tab, StringBuffer output) {

		printIndent(tab, output).append("return "); //$NON-NLS-1$
		if (expression != null)
			expression.printExpression(0, output);
		return output.append(';');
	}

	public String toString(int tab) {

		String s = tabString(tab);
		s = s + "return "; //$NON-NLS-1$
		if (expression != null)
			s = s + expression.toStringExpression();
		return s;
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			if (expression != null)
				expression.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}