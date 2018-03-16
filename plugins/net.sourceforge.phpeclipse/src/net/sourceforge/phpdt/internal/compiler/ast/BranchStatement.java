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

import net.sourceforge.phpdt.internal.compiler.codegen.Label;
import net.sourceforge.phpdt.internal.compiler.lookup.BlockScope;

public abstract class BranchStatement extends Statement {
	public Expression expression;

	public Label targetLabel;

	public ASTNode[] subroutines;

	/**
	 * BranchStatement constructor comment.
	 */
	public BranchStatement(Expression expr, int s, int e) {
		expression = expr;
		sourceStart = s;
		sourceEnd = e;
	}

	/**
	 * Branch code generation
	 * 
	 * generate the finallyInvocationSequence.
	 */
	// public void generateCode(BlockScope currentScope, CodeStream codeStream)
	// {
	//
	// if ((bits & IsReachableMASK) == 0) {
	// return;
	// }
	// int pc = codeStream.position;
	//
	// // generation of code responsible for invoking the finally
	// // blocks in sequence
	// if (subroutines != null){
	// for (int i = 0, max = subroutines.length; i < max; i++){
	// ASTNode sub;
	// if ((sub = subroutines[i]) instanceof SynchronizedStatement){
	// codeStream.load(((SynchronizedStatement)sub).synchroVariable);
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
	// codeStream.goto_(targetLabel);
	// codeStream.recordPositionsFrom(pc, this.sourceStart);
	// }
	public void resetStateForCodeGeneration() {
		if (this.targetLabel != null) {
			this.targetLabel.resetStateForCodeGeneration();
		}
	}

	public void resolve(BlockScope scope) {
	}

}
