/***********************************************************************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **********************************************************************************************************************************/
package net.sourceforge.phpdt.internal.compiler.ast;

import net.sourceforge.phpdt.internal.compiler.ASTVisitor;
import net.sourceforge.phpdt.internal.compiler.lookup.CompilationUnitScope;

import org.eclipse.core.resources.IFile;

public class ImportReference extends ASTNode {

	public char[][] tokens;

	// public long[] sourcePositions; //each entry is using the code :
	// (start<<32) + end
	public boolean onDemand = true; // most of the time

	public final char[] includeSource;

	public int declarationEnd;// doesn't include an potential trailing comment

	public int declarationSourceStart;

	public int declarationSourceEnd;

	public boolean used;

	private IFile fFile;

	public ImportReference(char[][] sources, char[] sourceString, int start,
			int end, boolean d) { // char[][] sources , long[] poss ,
		// boolean d) {
		tokens = sources;
		// sourcePositions = poss ;
		includeSource = sourceString;
		onDemand = d;
		sourceEnd = end;// (int)(sourcePositions[sourcePositions.length-1] &
						// 0x00000000FFFFFFFF);
		sourceStart = start;// (int)(sourcePositions[0]>>>32) ;
		fFile = null;
	}

	/**
	 * @return char[][]
	 */
	public char[][] getImportName() {
		return tokens;
	}

	public char[] getIncludeName() {
		return includeSource;
	}

	public StringBuffer print(int indent, StringBuffer output) {

		return print(indent, output, true);
	}

	public StringBuffer print(int tab, StringBuffer output, boolean withOnDemand) {

		/* when withOnDemand is false, only the name is printed */
		for (int i = 0; i < tokens.length; i++) {
			if (i > 0)
				output.append('.');
			output.append(tokens[i]);
		}
		if (withOnDemand && onDemand) {
			output.append(".*"); //$NON-NLS-1$
		}
		return output;
	}

	public String toString(int tab) {

		return toString(tab, true);
	}

	public String toString(int tab, boolean withOnDemand) {
		/* when withOnDemand is false, only the name is printed */
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < tokens.length; i++) {
			buffer.append(tokens[i]);
			if (i < (tokens.length - 1)) {
				buffer.append("."); //$NON-NLS-1$
			}
		}
		if (withOnDemand && onDemand) {
			buffer.append(".*"); //$NON-NLS-1$
		}
		buffer.append(" - ");
		buffer.append(includeSource);
		return buffer.toString();
		// return new String(includeSource);
	}

	public void traverse(ASTVisitor visitor, CompilationUnitScope scope) {
		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}

	/**
	 * @return Returns the filePath.
	 */
	public IFile getFile() {
		return fFile;
	}

	/**
	 * @param filePath
	 *            The filePath to set.
	 */
	public void setFile(IFile filePath) {
		fFile = filePath;
	}
}