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
import net.sourceforge.phpdt.internal.compiler.flow.FlowContext;
import net.sourceforge.phpdt.internal.compiler.flow.FlowInfo;
import net.sourceforge.phpdt.internal.compiler.impl.Constant;
import net.sourceforge.phpdt.internal.compiler.lookup.BlockScope;
import net.sourceforge.phpdt.internal.compiler.lookup.TypeBinding;

public abstract class Statement extends ASTNode {

	/**
	 * Statement constructor comment.
	 */
	public Statement() {
		super();
	}

	public abstract FlowInfo analyseCode(BlockScope currentScope,
			FlowContext flowContext, FlowInfo flowInfo);

	// public abstract void generateCode(BlockScope currentScope, CodeStream
	// codeStream);

	public boolean isEmptyBlock() {
		return false;
	}

	public boolean isValidJavaStatement() {
		// the use of this method should be avoid in most cases
		// and is here mostly for documentation purpose.....
		// while the parser is responsable for creating
		// welled formed expression statement, which results
		// in the fact that java-non-semantic-expression-used-as-statement
		// should not be parsable...thus not being built.
		// It sounds like the java grammar as help the compiler job in removing
		// -by construction- some statement that would have no effect....
		// (for example all expression that may do side-effects are valid
		// statement
		// -this is an appromative idea.....-)

		return true;
	}

	public StringBuffer print(int indent, StringBuffer output) {
		return printStatement(indent, output);
	}

	public abstract StringBuffer printStatement(int indent, StringBuffer output);

	public abstract void resolve(BlockScope scope);

	public Constant resolveCase(BlockScope scope, TypeBinding testType,
			SwitchStatement switchStatement) {
		// statement within a switch that are not case are treated as normal
		// statement....

		resolve(scope);
		return null;
	}

	public void resetStateForCodeGeneration() {
	}

	/**
	 * INTERNAL USE ONLY. Do nothing by default. This is used to redirect
	 * inter-statements jumps.
	 */
	public void branchChainTo(Label label) {
	}
}
