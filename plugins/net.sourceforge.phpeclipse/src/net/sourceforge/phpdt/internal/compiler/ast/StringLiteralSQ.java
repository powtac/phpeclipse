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

/**
 * 
 * single quoted string literal
 */
public class StringLiteralSQ extends StringLiteral {
	public StringLiteralSQ(char[] token, int s, int e) {
		super(token, s, e);
	}

	public StringLiteralSQ(int s, int e) {

		super(s, e);
	}

	public String toStringExpression() {

		// handle some special char.....
		StringBuffer result = new StringBuffer("\'"); //$NON-NLS-1$
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
		result.append("\'"); //$NON-NLS-1$
		return result.toString();
	}

}