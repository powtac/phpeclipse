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

import java.util.ArrayList;

import net.sourceforge.phpdt.core.compiler.CharOperation;
import net.sourceforge.phpdt.internal.compiler.ASTVisitor;
import net.sourceforge.phpdt.internal.compiler.CompilationResult;
import net.sourceforge.phpdt.internal.compiler.impl.ReferenceContext;
import net.sourceforge.phpdt.internal.compiler.lookup.CompilationUnitScope;
import net.sourceforge.phpdt.internal.compiler.lookup.LocalTypeBinding;
import net.sourceforge.phpdt.internal.compiler.problem.AbortCompilationUnit;
import net.sourceforge.phpdt.internal.compiler.problem.AbortMethod;
import net.sourceforge.phpdt.internal.compiler.problem.AbortType;
import net.sourceforge.phpdt.internal.compiler.problem.ProblemReporter;
import net.sourceforge.phpdt.internal.compiler.problem.ProblemSeverities;

public class CompilationUnitDeclaration extends ASTNode implements
		ProblemSeverities, ReferenceContext {

	public ImportReference currentPackage;

	public ImportReference[] imports;

	// public TypeDeclaration[] types;
	public ArrayList types;

	// public char[][] name;
	public int[][] comments;

	public boolean ignoreFurtherInvestigation = false; // once pointless to
														// investigate due to
														// errors

	public boolean ignoreMethodBodies = false;

	public CompilationUnitScope scope;

	public ProblemReporter problemReporter;

	public CompilationResult compilationResult;

	private LocalTypeBinding[] localTypes;

	int localTypeCount = 0;

	public boolean isPropagatingInnerClassEmulation;

	public CompilationUnitDeclaration(ProblemReporter problemReporter,
			CompilationResult compilationResult, int sourceLength) {

		this.problemReporter = problemReporter;
		this.compilationResult = compilationResult;
		this.types = new ArrayList();
		// by definition of a compilation unit....
		sourceStart = 0;
		sourceEnd = sourceLength - 1;
	}

	/*
	 * We cause the compilation task to abort to a given extent.
	 */
	public void abort(int abortLevel) {

		switch (abortLevel) {
		case AbortType:
			throw new AbortType(compilationResult);
		case AbortMethod:
			throw new AbortMethod(compilationResult);
		default:
			throw new AbortCompilationUnit(compilationResult);
		}
	}

	/*
	 * Dispatch code analysis AND request saturation of inner emulation
	 */
	public void analyseCode() {

		if (ignoreFurtherInvestigation)
			return;
		try {
			if (types != null) {
				for (int i = 0, count = types.size(); i < count; i++) {
					if (types.get(i) instanceof TypeDeclaration)
						((TypeDeclaration) types.get(i)).analyseCode(scope);
				}
			}
			// request inner emulation propagation
			propagateInnerEmulationForAllLocalTypes();
		} catch (AbortCompilationUnit e) {
			this.ignoreFurtherInvestigation = true;
			return;
		}
	}

	/*
	 * When unit result is about to be accepted, removed back pointers to
	 * compiler structures.
	 */
	public void cleanUp() {
		if (this.types != null) {
			for (int i = 0, max = this.types.size(); i < max; i++) {
				if (this.types.get(i) instanceof TypeDeclaration) {
					cleanUp((TypeDeclaration) this.types.get(i));
				}
			}
			for (int i = 0, max = this.localTypeCount; i < max; i++) {
				// null out the type's scope backpointers
				localTypes[i].scope = null; // local members are already in the
											// list
			}
		}
		// ClassFile[] classFiles = compilationResult.getClassFiles();
		// for (int i = 0, max = classFiles.length; i < max; i++) {
		// // clear the classFile back pointer to the bindings
		// ClassFile classFile = classFiles[i];
		// // null out the classfile backpointer to a type binding
		// classFile.referenceBinding = null;
		// classFile.codeStream = null; // codeStream holds onto ast and scopes
		// classFile.innerClassesBindings = null;
		// }
	}

	private void cleanUp(TypeDeclaration type) {
		if (type.memberTypes != null) {
			for (int i = 0, max = type.memberTypes.length; i < max; i++) {
				cleanUp(type.memberTypes[i]);
			}
		}
		if (type.binding != null) {
			// null out the type's scope backpointers
			type.binding.scope = null;
		}
	}

	// public void checkUnusedImports() {
	//
	// if (this.scope.imports != null) {
	// for (int i = 0, max = this.scope.imports.length; i < max; i++) {
	// ImportBinding importBinding = this.scope.imports[i];
	// ImportReference importReference = importBinding.reference;
	// if (importReference != null && !importReference.used) {
	// scope.problemReporter().unusedImport(importReference);
	// }
	// }
	// }
	// }

	public CompilationResult compilationResult() {
		return compilationResult;
	}

	/*
	 * Finds the matching type amoung this compilation unit types. Returns null
	 * if no type with this name is found. The type name is a compound name eg.
	 * if we're looking for X.A.B then a type name would be {X, A, B}
	 */
	public TypeDeclaration declarationOfType(char[][] typeName) {

		for (int i = 0; i < this.types.size(); i++) {
			if (this.types.get(i) instanceof TypeDeclaration) {
				TypeDeclaration typeDecl = ((TypeDeclaration) this.types.get(i))
						.declarationOfType(typeName);
				if (typeDecl != null) {
					return typeDecl;
				}
			}
		}
		return null;
	}

	/**
	 * Bytecode generation
	 */
	// public void generateCode() {
	//
	// if (ignoreFurtherInvestigation) {
	// if (types != null) {
	// for (int i = 0, count = types.length; i < count; i++) {
	// types[i].ignoreFurtherInvestigation = true;
	// // propagate the flag to request problem type creation
	// types[i].generateCode(scope);
	// }
	// }
	// return;
	// }
	// try {
	// if (types != null) {
	// for (int i = 0, count = types.length; i < count; i++)
	// types[i].generateCode(scope);
	// }
	// } catch (AbortCompilationUnit e) {
	// }
	// }
	public char[] getFileName() {

		return compilationResult.getFileName();
	}

	public char[] getMainTypeName() {

		if (compilationResult.compilationUnit == null) {
			char[] fileName = compilationResult.getFileName();

			int start = CharOperation.lastIndexOf('/', fileName) + 1;
			if (start == 0 || start < CharOperation.lastIndexOf('\\', fileName))
				start = CharOperation.lastIndexOf('\\', fileName) + 1;

			int end = CharOperation.lastIndexOf('.', fileName);
			if (end == -1)
				end = fileName.length;

			return CharOperation.subarray(fileName, start, end);
		} else {
			return compilationResult.compilationUnit.getMainTypeName();
		}
	}

	public boolean isEmpty() {

		return (currentPackage == null) && (imports == null) && (types == null);
	}

	public boolean hasErrors() {
		return this.ignoreFurtherInvestigation;
	}

	public StringBuffer print(int indent, StringBuffer output) {

		if (currentPackage != null) {
			printIndent(indent, output).append("package "); //$NON-NLS-1$
			currentPackage.print(0, output, false).append(";\n"); //$NON-NLS-1$
		}
		if (imports != null)
			for (int i = 0; i < imports.length; i++) {
				printIndent(indent, output).append("import "); //$NON-NLS-1$
				imports[i].print(0, output).append(";\n"); //$NON-NLS-1$ 
			}

		if (types != null) {
			for (int i = 0; i < types.size(); i++) {
				((ASTNode) types.get(i)).print(indent, output).append("\n"); //$NON-NLS-1$
			}
		}
		return output;
	}

	/*
	 * Force inner local types to update their innerclass emulation
	 */
	public void propagateInnerEmulationForAllLocalTypes() {

		isPropagatingInnerClassEmulation = true;
		for (int i = 0, max = this.localTypeCount; i < max; i++) {

			LocalTypeBinding localType = localTypes[i];
			// only propagate for reachable local types
			if ((localType.scope.referenceType().bits & IsReachableMASK) != 0) {
				localType.updateInnerEmulationDependents();
			}
		}
	}

	/*
	 * Keep track of all local types, so as to update their innerclass emulation
	 * later on.
	 */
	public void record(LocalTypeBinding localType) {

		if (this.localTypeCount == 0) {
			this.localTypes = new LocalTypeBinding[5];
		} else if (this.localTypeCount == this.localTypes.length) {
			System
					.arraycopy(
							this.localTypes,
							0,
							(this.localTypes = new LocalTypeBinding[this.localTypeCount * 2]),
							0, this.localTypeCount);
		}
		this.localTypes[this.localTypeCount++] = localType;
	}

	public void resolve() {

		try {
			if (types != null) {
				for (int i = 0, count = types.size(); i < count; i++) {
					if (types.get(i) instanceof TypeDeclaration) {
						((TypeDeclaration) types.get(i)).resolve(scope);
					}
				}
			}
			// if (!this.compilationResult.hasSyntaxError())
			// checkUnusedImports();
		} catch (AbortCompilationUnit e) {
			this.ignoreFurtherInvestigation = true;
			return;
		}
	}

	public void tagAsHavingErrors() {
		ignoreFurtherInvestigation = true;
	}

	public String toString(int tab) {

		String s = ""; //$NON-NLS-1$
		if (currentPackage != null)
			s = tabString(tab)
					+ "package " + currentPackage.toString(0, false) + ";\n"; //$NON-NLS-1$ //$NON-NLS-2$

		if (imports != null)
			for (int i = 0; i < imports.length; i++) {
				s += tabString(tab)
						+ "include " + imports[i].toString() + ";\n"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		;

		if (types != null)
			for (int i = 0; i < types.size(); i++) {
				s += ((ASTNode) types.get(i)).toString(tab) + "\n"; //$NON-NLS-1$
			}
		return s;
	}

	public void traverse(ASTVisitor visitor, CompilationUnitScope scope) {

		if (ignoreFurtherInvestigation)
			return;
		try {
			if (visitor.visit(this, scope)) {
				if (currentPackage != null) {
					currentPackage.traverse(visitor, scope);
				}
				if (imports != null) {
					int importLength = imports.length;
					for (int i = 0; i < importLength; i++) {
						imports[i].traverse(visitor, scope);
					}
				}
				if (types != null) {
					int typesLength = types.size();
					for (int i = 0; i < typesLength; i++) {
						if (types.get(i) instanceof TypeDeclaration) {
							((TypeDeclaration) types.get(i)).traverse(visitor,
									scope);
						}
					}
				}
			}
			visitor.endVisit(this, scope);
		} catch (AbortCompilationUnit e) {
		}
	}
}
