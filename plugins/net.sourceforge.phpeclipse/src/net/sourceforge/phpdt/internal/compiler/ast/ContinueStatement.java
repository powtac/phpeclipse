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

import net.sourceforge.phpdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import net.sourceforge.phpdt.internal.compiler.flow.FlowContext;
import net.sourceforge.phpdt.internal.compiler.flow.FlowInfo;
import net.sourceforge.phpdt.internal.compiler.lookup.BlockScope;

public class ContinueStatement extends BranchStatement {

	public ContinueStatement(Expression expr, int s, int e) {

		super(expr, s, e);
	}

	public FlowInfo analyseCode(BlockScope currentScope,
			FlowContext flowContext, FlowInfo flowInfo) {

		// here requires to generate a sequence of finally blocks invocations
		// depending corresponding
		// to each of the traversed try statements, so that execution will
		// terminate properly.

		// lookup the label, this should answer the returnContext
		// FlowContext targetContext = (expression == null)
		// ? flowContext.getTargetContextForDefaultContinue()
		// : flowContext.getTargetContextForContinueLabel(label);

		// if (targetContext == null) {
		// if (expression == null) {
		// currentScope.problemReporter().invalidContinue(this);
		// } else {
		// currentScope.problemReporter().undefinedLabel(this);
		// }
		// return flowInfo; // pretend it did not continue since no actual
		// target
		// }

		// if (targetContext == FlowContext.NotContinuableContext) {
		// currentScope.problemReporter().invalidContinue(this);
		// return flowInfo; // pretend it did not continue since no actual
		// target
		// }
		// targetLabel = targetContext.continueLabel();
		// FlowContext traversedContext = flowContext;
		// int subIndex = 0, maxSub = 5;
		// subroutines = new ASTNode[maxSub];
		//
		// do {
		// ASTNode sub;
		// if ((sub = traversedContext.subRoutine()) != null) {
		// if (subIndex == maxSub) {
		// System.arraycopy(subroutines, 0, (subroutines = new
		// ASTNode[maxSub*=2]), 0, subIndex); // grow
		// }
		// subroutines[subIndex++] = sub;
		// if (sub.cannotReturn()) {
		// break;
		// }
		// }
		// traversedContext.recordReturnFrom(flowInfo.unconditionalInits());
		//
		// ASTNode node;
		// if ((node = traversedContext.associatedNode) instanceof TryStatement)
		// {
		// TryStatement tryStatement = (TryStatement) node;
		// flowInfo.addInitializationsFrom(tryStatement.subRoutineInits); //
		// collect inits
		// } else if (traversedContext == targetContext) {
		// // only record continue info once accumulated through subroutines,
		// and only against target context
		// targetContext.recordContinueFrom(flowInfo);
		// break;
		// }
		// } while ((traversedContext = traversedContext.parent) != null);
		//		
		// // resize subroutines
		// if (subIndex != maxSub) {
		// System.arraycopy(subroutines, 0, (subroutines = new
		// ASTNode[subIndex]), 0, subIndex);
		// }
		return FlowInfo.DEAD_END;
	}

	public StringBuffer printStatement(int tab, StringBuffer output) {

		printIndent(tab, output).append("continue "); //$NON-NLS-1$
		if (expression != null)
			output.append(expression);
		return output.append(';');
	}

	public String toString(int tab) {

		String s = tabString(tab);
		s += "continue "; //$NON-NLS-1$
		if (expression != null)
			s += expression.toString();
		return s;
	}

	public void traverse(IAbstractSyntaxTreeVisitor visitor,
			BlockScope blockScope) {

		visitor.visit(this, blockScope);
		visitor.endVisit(this, blockScope);
	}
}
