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

import net.sourceforge.phpdt.core.compiler.CharOperation;
import net.sourceforge.phpdt.internal.compiler.ASTVisitor;
import net.sourceforge.phpdt.internal.compiler.CompilationResult;
import net.sourceforge.phpdt.internal.compiler.flow.FlowInfo;
import net.sourceforge.phpdt.internal.compiler.flow.InitializationFlowContext;
import net.sourceforge.phpdt.internal.compiler.impl.ReferenceContext;
import net.sourceforge.phpdt.internal.compiler.lookup.ClassScope;
import net.sourceforge.phpdt.internal.compiler.lookup.MethodBinding;
import net.sourceforge.phpdt.internal.compiler.lookup.MethodScope;
import net.sourceforge.phpdt.internal.compiler.lookup.ReferenceBinding;
import net.sourceforge.phpdt.internal.compiler.lookup.TypeBinding;
import net.sourceforge.phpdt.internal.compiler.parser.UnitParser;
import net.sourceforge.phpdt.internal.compiler.problem.AbortCompilation;
import net.sourceforge.phpdt.internal.compiler.problem.AbortCompilationUnit;
import net.sourceforge.phpdt.internal.compiler.problem.AbortMethod;
import net.sourceforge.phpdt.internal.compiler.problem.AbortType;
import net.sourceforge.phpdt.internal.compiler.problem.ProblemSeverities;

public abstract class AbstractMethodDeclaration extends ASTNode implements
		ProblemSeverities, ReferenceContext {

	public MethodScope scope;

	// it is not relevent for constructor but it helps to have the name of the
	// constructor here
	// which is always the name of the class.....parsing do extra work to fill
	// it up while it do not have to....
	public char[] selector;

	public int declarationSourceStart;

	public int declarationSourceEnd;

	public int modifiers;

	public int modifiersSourceStart;

	public Argument[] arguments;

	public TypeReference[] thrownExceptions;

	public Statement[] statements;

	public int explicitDeclarations;

	public MethodBinding binding;

	public boolean ignoreFurtherInvestigation = false;

	public boolean needFreeReturn = false;

	public int bodyStart;

	public int bodyEnd = -1;

	public CompilationResult compilationResult;

	AbstractMethodDeclaration(CompilationResult compilationResult) {
		this.compilationResult = compilationResult;
	}

	/*
	 * We cause the compilation task to abort to a given extent.
	 */
	public void abort(int abortLevel) {

		if (scope == null) {
			throw new AbortCompilation(); // cannot do better
		}

		CompilationResult compilationResult = scope.referenceCompilationUnit().compilationResult;

		switch (abortLevel) {
		case AbortCompilation:
			throw new AbortCompilation(compilationResult);
		case AbortCompilationUnit:
			throw new AbortCompilationUnit(compilationResult);
		case AbortType:
			throw new AbortType(compilationResult);
		default:
			throw new AbortMethod(compilationResult);
		}
	}

	public abstract void analyseCode(ClassScope scope,
			InitializationFlowContext initializationContext, FlowInfo info);

	/**
	 * Bind and add argument's binding into the scope of the method
	 */
	public void bindArguments() {

		if (arguments != null) {
			// by default arguments in abstract/native methods are considered to
			// be used (no complaint is expected)
			boolean used = binding == null || binding.isAbstract();// ||
																	// binding.isNative();

			int length = arguments.length;
			for (int i = 0; i < length; i++) {
				TypeBinding argType = binding == null ? null
						: binding.parameters[i];
				arguments[i].bind(scope, argType, used);
			}
		}
	}

	/**
	 * Record the thrown exception type bindings in the corresponding type
	 * references.
	 */
	public void bindThrownExceptions() {

		if (this.thrownExceptions != null && this.binding != null
				&& this.binding.thrownExceptions != null) {
			int thrownExceptionLength = this.thrownExceptions.length;
			int length = this.binding.thrownExceptions.length;
			if (length == thrownExceptionLength) {
				for (int i = 0; i < length; i++) {
					this.thrownExceptions[i].resolvedType = this.binding.thrownExceptions[i];
				}
			} else {
				int bindingIndex = 0;
				for (int i = 0; i < thrownExceptionLength
						&& bindingIndex < length; i++) {
					TypeReference thrownException = this.thrownExceptions[i];
					ReferenceBinding thrownExceptionBinding = this.binding.thrownExceptions[bindingIndex];
					char[][] bindingCompoundName = thrownExceptionBinding.compoundName;
					if (thrownException instanceof SingleTypeReference) {
						// single type reference
						int lengthName = bindingCompoundName.length;
						char[] thrownExceptionTypeName = thrownException
								.getTypeName()[0];
						if (CharOperation.equals(thrownExceptionTypeName,
								bindingCompoundName[lengthName - 1])) {
							thrownException.resolvedType = thrownExceptionBinding;
							bindingIndex++;
						}
					} else {
						// qualified type reference
						if (CharOperation.equals(thrownException.getTypeName(),
								bindingCompoundName)) {
							thrownException.resolvedType = thrownExceptionBinding;
							bindingIndex++;
						}
					}
				}
			}
		}
	}

	public CompilationResult compilationResult() {

		return this.compilationResult;
	}

	/**
	 * Bytecode generation for a method
	 */
	// public void generateCode(ClassScope classScope, ClassFile classFile) {
	//		
	// int problemResetPC = 0;
	// classFile.codeStream.wideMode = false; // reset wideMode to false
	// if (ignoreFurtherInvestigation) {
	// // method is known to have errors, dump a problem method
	// if (this.binding == null)
	// return; // handle methods with invalid signature or duplicates
	// int problemsLength;
	// IProblem[] problems =
	// scope.referenceCompilationUnit().compilationResult.getProblems();
	// IProblem[] problemsCopy = new IProblem[problemsLength = problems.length];
	// System.arraycopy(problems, 0, problemsCopy, 0, problemsLength);
	// classFile.addProblemMethod(this, binding, problemsCopy);
	// return;
	// }
	// // regular code generation
	// try {
	// problemResetPC = classFile.contentsOffset;
	// this.generateCode(classFile);
	// } catch (AbortMethod e) {
	// // a fatal error was detected during code generation, need to restart
	// code gen if possible
	// if (e.compilationResult == CodeStream.RESTART_IN_WIDE_MODE) {
	// // a branch target required a goto_w, restart code gen in wide mode.
	// try {
	// this.traverse(new ResetStateForCodeGenerationVisitor(), classScope);
	// classFile.contentsOffset = problemResetPC;
	// classFile.methodCount--;
	// classFile.codeStream.wideMode = true; // request wide mode
	// this.generateCode(classFile); // restart method generation
	// } catch (AbortMethod e2) {
	// int problemsLength;
	// IProblem[] problems =
	// scope.referenceCompilationUnit().compilationResult.getAllProblems();
	// IProblem[] problemsCopy = new IProblem[problemsLength = problems.length];
	// System.arraycopy(problems, 0, problemsCopy, 0, problemsLength);
	// classFile.addProblemMethod(this, binding, problemsCopy, problemResetPC);
	// }
	// } else {
	// // produce a problem method accounting for this fatal error
	// int problemsLength;
	// IProblem[] problems =
	// scope.referenceCompilationUnit().compilationResult.getAllProblems();
	// IProblem[] problemsCopy = new IProblem[problemsLength = problems.length];
	// System.arraycopy(problems, 0, problemsCopy, 0, problemsLength);
	// classFile.addProblemMethod(this, binding, problemsCopy, problemResetPC);
	// }
	// }
	// }
	//
	// private void generateCode(ClassFile classFile) {
	//
	// classFile.generateMethodInfoHeader(binding);
	// int methodAttributeOffset = classFile.contentsOffset;
	// int attributeNumber = classFile.generateMethodInfoAttribute(binding);
	// if ((!binding.isNative()) && (!binding.isAbstract())) {
	// int codeAttributeOffset = classFile.contentsOffset;
	// classFile.generateCodeAttributeHeader();
	// CodeStream codeStream = classFile.codeStream;
	// codeStream.reset(this, classFile);
	// // initialize local positions
	// this.scope.computeLocalVariablePositions(binding.isStatic() ? 0 : 1,
	// codeStream);
	//
	// // arguments initialization for local variable debug attributes
	// if (arguments != null) {
	// for (int i = 0, max = arguments.length; i < max; i++) {
	// LocalVariableBinding argBinding;
	// codeStream.addVisibleLocalVariable(argBinding = arguments[i].binding);
	// argBinding.recordInitializationStartPC(0);
	// }
	// }
	// if (statements != null) {
	// for (int i = 0, max = statements.length; i < max; i++)
	// statements[i].generateCode(scope, codeStream);
	// }
	// if (this.needFreeReturn) {
	// codeStream.return_();
	// }
	// // local variable attributes
	// codeStream.exitUserScope(scope);
	// codeStream.recordPositionsFrom(0, this.declarationSourceEnd);
	// classFile.completeCodeAttribute(codeAttributeOffset);
	// attributeNumber++;
	// } else {
	// checkArgumentsSize();
	// }
	// classFile.completeMethodInfo(methodAttributeOffset, attributeNumber);
	//
	// // if a problem got reported during code gen, then trigger problem method
	// creation
	// if (ignoreFurtherInvestigation) {
	// throw new
	// AbortMethod(scope.referenceCompilationUnit().compilationResult);
	// }
	// }
	// private void checkArgumentsSize() {
	// TypeBinding[] parameters = binding.parameters;
	// int size = 1; // an abstact method or a native method cannot be static
	// for (int i = 0, max = parameters.length; i < max; i++) {
	// TypeBinding parameter = parameters[i];
	// if (parameter == LongBinding || parameter == DoubleBinding) {
	// size += 2;
	// } else {
	// size++;
	// }
	// if (size > 0xFF) {
	// scope.problemReporter().noMoreAvailableSpaceForArgument(scope.locals[i],
	// scope.locals[i].declaration);
	// }
	// }
	// }
	public boolean hasErrors() {
		return this.ignoreFurtherInvestigation;
	}

	public boolean isAbstract() {

		if (binding != null)
			return binding.isAbstract();
		return (modifiers & AccAbstract) != 0;
	}

	public boolean isClinit() {

		return false;
	}

	public boolean isConstructor() {

		return false;
	}

	public boolean isDefaultConstructor() {

		return false;
	}

	public boolean isInitializationMethod() {

		return false;
	}

	// public boolean isNative() {
	//
	// if (binding != null)
	// return binding.isNative();
	// return (modifiers & AccNative) != 0;
	// }

	public boolean isStatic() {

		if (binding != null)
			return binding.isStatic();
		return (modifiers & AccStatic) != 0;
	}

	/**
	 * Fill up the method body with statement
	 */
	public abstract void parseStatements(UnitParser parser,
			CompilationUnitDeclaration unit);

	public StringBuffer print(int tab, StringBuffer output) {

		printIndent(tab, output);
		printModifiers(this.modifiers, output);
		printReturnType(0, output).append(this.selector).append('(');
		if (this.arguments != null) {
			for (int i = 0; i < this.arguments.length; i++) {
				if (i > 0)
					output.append(", "); //$NON-NLS-1$
				this.arguments[i].print(0, output);
			}
		}
		output.append(')');
		if (this.thrownExceptions != null) {
			output.append(" throws "); //$NON-NLS-1$
			for (int i = 0; i < this.thrownExceptions.length; i++) {
				if (i > 0)
					output.append(", "); //$NON-NLS-1$
				this.thrownExceptions[i].print(0, output);
			}
		}
		printBody(tab + 1, output);
		return output;
	}

	public StringBuffer printBody(int indent, StringBuffer output) {

		if (isAbstract() || (this.modifiers & AccSemicolonBody) != 0)
			return output.append(';');

		output.append(" {"); //$NON-NLS-1$
		if (this.statements != null) {
			for (int i = 0; i < this.statements.length; i++) {
				output.append('\n');
				this.statements[i].printStatement(indent, output);
			}
		}
		output.append('\n'); //$NON-NLS-1$
		printIndent(indent == 0 ? 0 : indent - 1, output).append('}');
		return output;
	}

	public StringBuffer printReturnType(int indent, StringBuffer output) {

		return output;
	}

	public void resolve(ClassScope upperScope) {

		if (binding == null) {
			ignoreFurtherInvestigation = true;
		}

		try {
			bindArguments();
			bindThrownExceptions();
			resolveStatements();
		} catch (AbortMethod e) { // ========= abort on fatal error
									// =============
			this.ignoreFurtherInvestigation = true;
		}
	}

	public void resolveStatements() {

		if (statements != null) {
			int i = 0, length = statements.length;
			while (i < length)
				statements[i++].resolve(scope);
		}
	}

	public String returnTypeToString(int tab) {

		return ""; //$NON-NLS-1$
	}

	public void tagAsHavingErrors() {

		ignoreFurtherInvestigation = true;
	}

	public String toString(int tab) {

		String s = tabString(tab);
		if (modifiers != AccDefault) {
			s += modifiersString(modifiers);
		}

		s += returnTypeToString(0);
		s += new String(selector) + "("; //$NON-NLS-1$
		if (arguments != null) {
			for (int i = 0; i < arguments.length; i++) {
				s += arguments[i].toString(0);
				if (i != (arguments.length - 1))
					s = s + ", "; //$NON-NLS-1$
			}
			;
		}
		;
		s += ")"; //$NON-NLS-1$
		if (thrownExceptions != null) {
			s += " throws "; //$NON-NLS-1$
			for (int i = 0; i < thrownExceptions.length; i++) {
				s += thrownExceptions[i].toString(0);
				if (i != (thrownExceptions.length - 1))
					s = s + ", "; //$NON-NLS-1$
			}
			;
		}
		;

		s += toStringStatements(tab + 1);
		return s;
	}

	public String toStringStatements(int tab) {

		if (isAbstract() || (this.modifiers & AccSemicolonBody) != 0)
			return ";"; //$NON-NLS-1$

		String s = " {"; //$NON-NLS-1$
		if (statements != null) {
			for (int i = 0; i < statements.length; i++) {
				s = s + "\n" + statements[i].toString(tab); //$NON-NLS-1$
				if (!(statements[i] instanceof Block)) {
					s += ";"; //$NON-NLS-1$
				}
			}
		}
		s += "\n" + tabString(tab == 0 ? 0 : tab - 1) + "}"; //$NON-NLS-2$ //$NON-NLS-1$
		return s;
	}

	public void traverse(ASTVisitor visitor, ClassScope classScope) {
	}
}