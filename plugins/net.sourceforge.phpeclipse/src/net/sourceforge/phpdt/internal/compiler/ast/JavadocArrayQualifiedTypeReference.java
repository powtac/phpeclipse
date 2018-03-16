/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import net.sourceforge.phpdt.internal.compiler.lookup.Scope;

public class JavadocArrayQualifiedTypeReference extends
		ArrayQualifiedTypeReference {

	public int tagSourceStart, tagSourceEnd;

	public JavadocArrayQualifiedTypeReference(
			JavadocQualifiedTypeReference typeRef, int dim) {
		super(typeRef.tokens, dim, typeRef.sourcePositions);
	}

	protected void reportInvalidType(Scope scope) {
		scope.problemReporter().javadocInvalidType(this, this.resolvedType,
				scope.getDeclarationModifiers());
	}

	protected void reportDeprecatedType(Scope scope) {
		scope.problemReporter().javadocDeprecatedType(this.resolvedType, this,
				scope.getDeclarationModifiers());
	}

	/*
	 * (non-Javadoc) Redefine to capture javadoc specific signatures
	 * 
	 * @see net.sourceforge.phpdt.internal.compiler.ast.ASTNode#traverse(net.sourceforge.phpdt.internal.compiler.ASTVisitor,
	 *      net.sourceforge.phpdt.internal.compiler.lookup.BlockScope)
	 */
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}
}
