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
import net.sourceforge.phpdt.internal.compiler.lookup.ClassScope;
import net.sourceforge.phpdt.internal.compiler.lookup.Scope;
import net.sourceforge.phpdt.internal.compiler.lookup.TypeBinding;

public class QualifiedTypeReference extends TypeReference {
	public char[][] tokens;

	public long[] sourcePositions;

	public QualifiedTypeReference(char[][] sources, long[] poss) {
		tokens = sources;
		sourcePositions = poss;
		sourceStart = (int) (sourcePositions[0] >>> 32);
		sourceEnd = (int) (sourcePositions[sourcePositions.length - 1] & 0x00000000FFFFFFFFL);
	}

	public QualifiedTypeReference(char[][] sources, TypeBinding type,
			long[] poss) {
		this(sources, poss);
		this.resolvedType = type;
	}

	public TypeReference copyDims(int dim) {
		// return a type reference copy of me with some dimensions
		// warning : the new type ref has a null binding

		return new ArrayQualifiedTypeReference(tokens, null, dim,
				sourcePositions);
	}

	public TypeBinding getTypeBinding(Scope scope) {
		if (this.resolvedType != null)
			return this.resolvedType;
		return scope.getType(tokens);
	}

	public char[][] getTypeName() {

		return tokens;
	}

	public StringBuffer printExpression(int indent, StringBuffer output) {

		for (int i = 0; i < tokens.length; i++) {
			if (i > 0)
				output.append('.');
			output.append(tokens[i]);
		}
		return output;
	}

	public String toStringExpression(int tab) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < tokens.length; i++) {
			buffer.append(tokens[i]);
			if (i < (tokens.length - 1)) {
				buffer.append("."); //$NON-NLS-1$
			}
		}
		return buffer.toString();
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {
		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}

	public void traverse(ASTVisitor visitor, ClassScope scope) {
		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}
}
