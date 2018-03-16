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

import net.sourceforge.phpdt.internal.compiler.flow.FlowContext;
import net.sourceforge.phpdt.internal.compiler.flow.FlowInfo;
import net.sourceforge.phpdt.internal.compiler.impl.Constant;
import net.sourceforge.phpdt.internal.compiler.lookup.BlockScope;
import net.sourceforge.phpdt.internal.compiler.lookup.TypeBinding;

public abstract class Literal extends Expression {

	public Literal(int s, int e) {
		sourceStart = s;
		sourceEnd = e;
	}

	public FlowInfo analyseCode(BlockScope currentScope,
			FlowContext flowContext, FlowInfo flowInfo) {
		return flowInfo;
	}

	public abstract void computeConstant();

	// ON ERROR constant STAYS NULL
	public abstract TypeBinding literalType(BlockScope scope);

	public StringBuffer printExpression(int indent, StringBuffer output) {

		return output.append(source());
	}

	public TypeBinding resolveType(BlockScope scope) {
		// compute the real value, which must range its type's range

		computeConstant();
		if (constant == null) {
			scope.problemReporter().constantOutOfRange(this);
			constant = Constant.NotAConstant;
			return null;
		}
		this.resolvedType = literalType(scope);
		return this.resolvedType;
	}

	public abstract char[] source();
}
