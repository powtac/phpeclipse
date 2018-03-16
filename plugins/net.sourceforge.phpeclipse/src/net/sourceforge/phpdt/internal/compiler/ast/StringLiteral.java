/***********************************************************************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **********************************************************************************************************************************/
package net.sourceforge.phpdt.internal.compiler.ast;

import net.sourceforge.phpdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import net.sourceforge.phpdt.internal.compiler.impl.Constant;
import net.sourceforge.phpdt.internal.compiler.lookup.BlockScope;
import net.sourceforge.phpdt.internal.compiler.lookup.TypeBinding;

public class StringLiteral extends Literal {

	char[] source;

	public StringLiteral(char[] token, int s, int e) {

		this(s, e);
		source = token;
	}

	public StringLiteral(int s, int e) {

		super(s, e);
	}

	public void computeConstant() {

		constant = Constant.fromValue(String.valueOf(source));
	}

	// public ExtendedStringLiteral extendWith(CharLiteral lit) {
	//
	// //add the lit source to mine, just as if it was mine
	// return new ExtendedStringLiteral(this, lit);
	// }

	public ExtendedStringLiteral extendWith(StringLiteral lit) {

		// add the lit source to mine, just as if it was mine
		return new ExtendedStringLiteral(this, lit);
	}

	/**
	 * Code generation for string literal
	 */
	// public void generateCode(BlockScope currentScope, CodeStream codeStream,
	// boolean valueRequired) {
	//
	// int pc = codeStream.position;
	// if (valueRequired)
	// codeStream.ldc(constant.stringValue());
	// codeStream.recordPositionsFrom(pc, this.sourceStart);
	// }
	public TypeBinding literalType(BlockScope scope) {

		return scope.getJavaLangString();
	}

	public StringBuffer printExpression(int indent, StringBuffer output) {

		// handle some special char.....
		output.append('\"');
		for (int i = 0; i < source.length; i++) {
			switch (source[i]) {
			case '\b':
				output.append("\\b"); //$NON-NLS-1$
				break;
			case '\t':
				output.append("\\t"); //$NON-NLS-1$
				break;
			case '\n':
				output.append("\\n"); //$NON-NLS-1$
				break;
			case '\f':
				output.append("\\f"); //$NON-NLS-1$
				break;
			case '\r':
				output.append("\\r"); //$NON-NLS-1$
				break;
			case '\"':
				output.append("\\\""); //$NON-NLS-1$
				break;
			case '\'':
				output.append("\\'"); //$NON-NLS-1$
				break;
			case '\\': // take care not to display the escape as a potential
						// real char
				output.append("\\\\"); //$NON-NLS-1$
				break;
			default:
				output.append(source[i]);
			}
		}
		output.append('\"');
		return output;
	}

	public char[] source() {

		return source;
	}

	public String toStringExpression() {

		// handle some special char.....
		StringBuffer result = new StringBuffer("\""); //$NON-NLS-1$
		for (int i = 0; i < source.length; i++) {
			switch (source[i]) {
			case '\b':
				result.append("\\b"); //$NON-NLS-1$
				break;
			case '\t':
				result.append("\\t"); //$NON-NLS-1$
				break;
			case '\n':
				result.append("\\n"); //$NON-NLS-1$
				break;
			case '\f':
				result.append("\\f"); //$NON-NLS-1$
				break;
			case '\r':
				result.append("\\r"); //$NON-NLS-1$
				break;
			case '\"':
				result.append("\\\""); //$NON-NLS-1$
				break;
			case '\'':
				result.append("\\'"); //$NON-NLS-1$
				break;
			case '\\': // take care not to display the escape as a potential
						// real char
				result.append("\\\\"); //$NON-NLS-1$
				break;
			default:
				result.append(source[i]);
			}
		}
		result.append("\""); //$NON-NLS-1$
		return result.toString();
	}

	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}
}