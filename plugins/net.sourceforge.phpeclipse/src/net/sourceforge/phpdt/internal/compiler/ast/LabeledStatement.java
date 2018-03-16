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
import net.sourceforge.phpdt.internal.compiler.codegen.Label;
import net.sourceforge.phpdt.internal.compiler.flow.FlowContext;
import net.sourceforge.phpdt.internal.compiler.flow.FlowInfo;
import net.sourceforge.phpdt.internal.compiler.flow.LabelFlowContext;
import net.sourceforge.phpdt.internal.compiler.lookup.BlockScope;

public class LabeledStatement extends Statement {

	public Statement statement;

	public char[] label;

	public Label targetLabel;

	// for local variables table attributes
	int mergedInitStateIndex = -1;

	/**
	 * LabeledStatement constructor comment.
	 */
	public LabeledStatement(char[] l, Statement st, int s, int e) {

		this.statement = st;
		this.label = l;
		this.sourceStart = s;
		this.sourceEnd = e;
	}

	public FlowInfo analyseCode(BlockScope currentScope,
			FlowContext flowContext, FlowInfo flowInfo) {

		// need to stack a context to store explicit label, answer inits in case
		// of normal completion merged
		// with those relative to the exit path from break statement occurring
		// inside the labeled statement.
		if (statement == null) {
			return flowInfo;
		} else {
			LabelFlowContext labelContext;
			FlowInfo mergedInfo = statement.analyseCode(
					currentScope,
					(labelContext = new LabelFlowContext(flowContext, this,
							label, (targetLabel = new Label()), currentScope)),
					flowInfo).mergedWith(labelContext.initsOnBreak);
			mergedInitStateIndex = currentScope.methodScope()
					.recordInitializationStates(mergedInfo);
			return mergedInfo;
		}
	}

	public ASTNode concreteStatement() {

		// return statement.concreteStatement(); // for supporting nested
		// labels: a:b:c: someStatement (see 21912)
		return statement;
	}

	/**
	 * Code generation for labeled statement
	 * 
	 * may not need actual source positions recording
	 * 
	 * @param currentScope
	 *            net.sourceforge.phpdt.internal.compiler.lookup.BlockScope
	 * @param codeStream
	 *            net.sourceforge.phpdt.internal.compiler.codegen.CodeStream
	 */
	// public void generateCode(BlockScope currentScope, CodeStream codeStream)
	// {
	//		
	// int pc = codeStream.position;
	// if (targetLabel != null) {
	// targetLabel.codeStream = codeStream;
	// if (statement != null) {
	// statement.generateCode(currentScope, codeStream);
	// }
	// targetLabel.place();
	// }
	// // May loose some local variable initializations : affecting the local
	// variable attributes
	// if (mergedInitStateIndex != -1) {
	// codeStream.removeNotDefinitelyAssignedVariables(
	// currentScope,
	// mergedInitStateIndex);
	// }
	// codeStream.recordPositionsFrom(pc, this.sourceStart);
	// }
	public StringBuffer printStatement(int tab, StringBuffer output) {

		printIndent(tab, output).append(label).append(": "); //$NON-NLS-1$
		if (this.statement == null)
			output.append(';');
		else
			this.statement.printStatement(0, output);
		return output;
	}

	public void resolve(BlockScope scope) {

		statement.resolve(scope);
	}

	public String toString(int tab) {

		String s = tabString(tab);
		s += new String(label) + ": " + statement.toString(0); //$NON-NLS-1$
		return s;
	}

	public void traverse(ASTVisitor visitor, BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			statement.traverse(visitor, blockScope);
		}
		visitor.endVisit(this, blockScope);
	}

	public void resetStateForCodeGeneration() {
		if (this.targetLabel != null) {
			this.targetLabel.resetStateForCodeGeneration();
		}
	}
}
