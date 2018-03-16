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
import net.sourceforge.phpdt.internal.compiler.lookup.BlockScope;
import net.sourceforge.phpdt.internal.compiler.lookup.TypeBinding;

public class NullLiteral extends MagicLiteral {

	static final char[] source = { 'n', 'u', 'l', 'l' };

	public NullLiteral(int s, int e) {

		super(s, e);
	}

	public void computeConstant() {

		constant = NotAConstant;
	}

	/**
	 * Code generation for the null literal
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
	// codeStream.aconst_null();
	// codeStream.recordPositionsFrom(pc, this.sourceStart);
	// }
	public TypeBinding literalType(BlockScope scope) {
		return NullBinding;
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
