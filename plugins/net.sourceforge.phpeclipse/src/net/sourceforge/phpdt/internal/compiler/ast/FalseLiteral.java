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
import net.sourceforge.phpdt.internal.compiler.impl.Constant;
import net.sourceforge.phpdt.internal.compiler.lookup.BlockScope;
import net.sourceforge.phpdt.internal.compiler.lookup.TypeBinding;

public class FalseLiteral extends MagicLiteral {
	static final char[] source = { 'f', 'a', 'l', 's', 'e' };

	public FalseLiteral(int s, int e) {
		super(s, e);
	}

	public void computeConstant() {

		constant = Constant.fromValue(false);
	}

	/**
	 * Code generation for false literal
	 * 
	 * @param currentScope
	 *            net.sourceforge.phpdt.internal.compiler.lookup.BlockScope
	 * @param codeStream
	 *            net.sourceforge.phpdt.internal.compiler.codegen.CodeStream
	 * @param valueRequired
	 *            boolean
	 */
	// public void generateCode(BlockScope currentScope, CodeStream codeStream,
	// boolean valueRequired) {
	// int pc = codeStream.position;
	// if (valueRequired)
	// codeStream.iconst_0();
	// codeStream.recordPositionsFrom(pc, this.sourceStart);
	// }
	// public void generateOptimizedBoolean(BlockScope currentScope, CodeStream
	// codeStream, Label trueLabel, Label falseLabel, boolean valueRequired) {
	//
	// // falseLabel being not nil means that we will not fall through into the
	// FALSE case
	//
	// int pc = codeStream.position;
	// if (valueRequired) {
	// if (falseLabel != null) {
	// // implicit falling through the TRUE case
	// if (trueLabel == null) {
	// codeStream.goto_(falseLabel);
	// }
	// }
	// }
	// codeStream.recordPositionsFrom(pc, this.sourceStart);
	// }
	public TypeBinding literalType(BlockScope scope) {
		return BooleanBinding;
	}

	/**
	 * 
	 */
	public char[] source() {
		return source;
	}

	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}
}
