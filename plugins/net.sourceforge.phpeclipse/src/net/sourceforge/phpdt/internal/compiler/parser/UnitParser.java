package net.sourceforge.phpdt.internal.compiler.parser;

import net.sourceforge.phpdt.core.IJavaModelMarker;
import net.sourceforge.phpdt.core.compiler.IProblem;
import net.sourceforge.phpdt.internal.compiler.CompilationResult;
import net.sourceforge.phpdt.internal.compiler.ast.ASTNode;
import net.sourceforge.phpdt.internal.compiler.ast.CompilationUnitDeclaration;
import net.sourceforge.phpdt.internal.compiler.ast.ConstructorDeclaration;
import net.sourceforge.phpdt.internal.compiler.ast.FieldDeclaration;
import net.sourceforge.phpdt.internal.compiler.ast.Initializer;
import net.sourceforge.phpdt.internal.compiler.ast.MethodDeclaration;
import net.sourceforge.phpdt.internal.compiler.ast.TypeDeclaration;
import net.sourceforge.phpdt.internal.compiler.env.ICompilationUnit;
import net.sourceforge.phpdt.internal.compiler.problem.AbortCompilation;
import net.sourceforge.phpdt.internal.compiler.problem.ProblemReporter;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * 
 * 
 */
public class UnitParser extends Parser {

	public UnitParser(ProblemReporter problemReporter) { // , boolean
															// optimizeStringLiterals,
															// boolean
															// assertMode) {
		super(problemReporter);
		nestedMethod = new int[30];

		// this.optimizeStringLiterals = optimizeStringLiterals;
		// this.assertMode = assertMode;
		// this.initializeScanner();
		astLengthStack = new int[50];
		// expressionLengthStack = new int[30];
		// intStack = new int[50];
		// identifierStack = new char[30][];
		// identifierLengthStack = new int[30];
		// nestedMethod = new int[30];
		// realBlockStack = new int[30];
		// identifierPositionStack = new long[30];
		// variablesCounter = new int[30];
	}

	public void goForConstructorBody() {
		// tells the scanner to go for compilation unit parsing

		firstToken = TokenNameEQUAL_EQUAL;
		scanner.recordLineSeparator = false;
	}

	public void goForExpression() {
		// tells the scanner to go for an expression parsing

		firstToken = TokenNameREMAINDER;
		scanner.recordLineSeparator = false;
	}

	public void goForCompilationUnit() {
		// tells the scanner to go for compilation unit parsing

		firstToken = TokenNamePLUS_PLUS;
		scanner.linePtr = -1;
		scanner.foundTaskCount = 0;
		scanner.recordLineSeparator = true;
		// scanner.currentLine= null;
	}

	public void goForInitializer() {
		// tells the scanner to go for initializer parsing

		firstToken = TokenNameRIGHT_SHIFT;
		scanner.recordLineSeparator = false;
	}

	public void goForMethodBody() {
		// tells the scanner to go for method body parsing

		firstToken = TokenNameMINUS_MINUS;
		scanner.recordLineSeparator = false;
	}

	public void initialize(boolean phpMode) {
		super.initialize(phpMode);
		// positionning the parser for a new compilation unit
		// avoiding stack reallocation and all that....
		// astPtr = -1;
		// astLengthPtr = -1;
		// expressionPtr = -1;
		// expressionLengthPtr = -1;
		// identifierPtr = -1;
		// identifierLengthPtr = -1;
		// intPtr = -1;
		// nestedMethod[nestedType = 0] = 0; // need to reset for further reuse
		// variablesCounter[nestedType] = 0;
		// dimensions = 0 ;
		// realBlockPtr = -1;
		// endStatementPosition = 0;

		// remove objects from stack too, while the same parser/compiler couple
		// is
		// re-used between two compilations ....

		// int astLength = astStack.length;
		// if (noAstNodes.length < astLength){
		// noAstNodes = new ASTNode[astLength];
		// //System.out.println("Resized AST stacks : "+ astLength);
		//		
		// }
		// System.arraycopy(noAstNodes, 0, astStack, 0, astLength);
		//
		// int expressionLength = expressionStack.length;
		// if (noExpressions.length < expressionLength){
		// noExpressions = new Expression[expressionLength];
		// //System.out.println("Resized EXPR stacks : "+ expressionLength);
		// }
		// System.arraycopy(noExpressions, 0, expressionStack, 0,
		// expressionLength);

		// reset scanner state
		scanner.commentPtr = -1;
		scanner.foundTaskCount = 0;
		scanner.eofPosition = Integer.MAX_VALUE;

		// resetModifiers();
		//
		// // recovery
		// lastCheckPoint = -1;
		// currentElement = null;
		// restartRecovery = false;
		// hasReportedError = false;
		// recoveredStaticInitializerStart = 0;
		// lastIgnoredToken = -1;
		// lastErrorEndPosition = -1;
		// listLength = 0;
	}

	// A P I

	public CompilationUnitDeclaration parse(ICompilationUnit sourceUnit,
			CompilationResult compilationResult, boolean phpMode) {
		// parses a compilation unit and manages error handling (even bugs....)

		CompilationUnitDeclaration unit;
		try {
			/* automaton initialization */
			initialize(phpMode);
			goForCompilationUnit();

			/* scanner initialization */
			scanner.setSource(sourceUnit, sourceUnit.getContents());

			/* unit creation */
			referenceContext = compilationUnit = new CompilationUnitDeclaration(
					problemReporter, compilationResult, scanner.source.length);
			// TODO TypeDeclaration test
			// TypeDeclaration typeDecl = new
			// TypeDeclaration(this.compilationUnit.compilationResult);
			// typeDecl.sourceStart = 0;
			// typeDecl.sourceEnd = 10;
			// typeDecl.name = new char[]{'t', 'e','s','t'};
			// this.compilationUnit.types = new ArrayList();
			// this.compilationUnit.types.add(typeDecl);
			/* run automaton */
			super.parse();
			// // TODO jsurfer start
			// if (sourceUnit instanceof BasicCompilationUnit) {
			// storeProblemsFor(((BasicCompilationUnit)sourceUnit).getResource(),
			// compilationResult.getAllProblems());
			// }
			// // jsurfer end

		} finally {
			unit = compilationUnit;
			compilationUnit = null; // reset parser
		}
		return unit;
	}

	/**
	 * Creates a marker from each problem and adds it to the resource. The
	 * marker is as follows: - its type is T_PROBLEM - its plugin ID is the
	 * JavaBuilder's plugin ID - its message is the problem's message - its
	 * priority reflects the severity of the problem - its range is the
	 * problem's range - it has an extra attribute "ID" which holds the
	 * problem's id
	 */
	protected void storeProblemsFor(IResource resource, IProblem[] problems)
			throws CoreException {
		if (resource == null || problems == null || problems.length == 0)
			return;

		for (int i = 0, l = problems.length; i < l; i++) {
			IProblem problem = problems[i];
			int id = problem.getID();
			if (id != IProblem.Task) {
				IMarker marker = resource
						.createMarker(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER);
				marker
						.setAttributes(
								new String[] { IMarker.MESSAGE,
										IMarker.SEVERITY, IJavaModelMarker.ID,
										IMarker.CHAR_START, IMarker.CHAR_END,
										IMarker.LINE_NUMBER,
										IJavaModelMarker.ARGUMENTS },
								new Object[] {
										problem.getMessage(),
										new Integer(
												problem.isError() ? IMarker.SEVERITY_ERROR
														: IMarker.SEVERITY_WARNING),
										new Integer(id),
										new Integer(problem.getSourceStart()),
										new Integer(problem.getSourceEnd() + 1),
										new Integer(problem
												.getSourceLineNumber()),
										net.sourceforge.phpdt.internal.core.util.Util
												.getProblemArgumentsForMarker(problem
														.getArguments()) });
			}

		}
	}

	// A P I

	public void parse(ConstructorDeclaration cd, CompilationUnitDeclaration unit) {
		// only parse the method body of cd
		// fill out its statements

		// convert bugs into parse error

		initialize(false);
		goForConstructorBody();
		nestedMethod[nestedType]++;

		referenceContext = cd;
		compilationUnit = unit;

		scanner.resetTo(cd.sourceEnd + 1, cd.declarationSourceEnd);
		try {
			parse();
		} catch (AbortCompilation ex) {
			lastAct = ERROR_ACTION;

		} finally {
			nestedMethod[nestedType]--;
		}

		if (lastAct == ERROR_ACTION) {
			initialize(false);
			return;
		}

		// statements
		// cd.explicitDeclarations = realBlockStack[realBlockPtr--];
		// int length;
		// if ((length = astLengthStack[astLengthPtr--]) != 0) {
		// astPtr -= length;
		// if (astStack[astPtr + 1] instanceof ExplicitConstructorCall)
		// //avoid a isSomeThing that would only be used here BUT what is faster
		// between two alternatives ?
		// {
		// System.arraycopy(
		// astStack,
		// astPtr + 2,
		// cd.statements = new Statement[length - 1],
		// 0,
		// length - 1);
		// cd.constructorCall = (ExplicitConstructorCall) astStack[astPtr + 1];
		// } else { //need to add explicitly the super();
		// System.arraycopy(
		// astStack,
		// astPtr + 1,
		// cd.statements = new Statement[length],
		// 0,
		// length);
		// cd.constructorCall = SuperReference.implicitSuperConstructorCall();
		// }
		// } else {
		// cd.constructorCall = SuperReference.implicitSuperConstructorCall();
		// }
		//
		// if (cd.constructorCall.sourceEnd == 0) {
		// cd.constructorCall.sourceEnd = cd.sourceEnd;
		// cd.constructorCall.sourceStart = cd.sourceStart;
		// }
	}

	// A P I

	public void parse(FieldDeclaration field, TypeDeclaration type,
			CompilationUnitDeclaration unit, char[] initializationSource) {
		// only parse the initializationSource of the given field

		// convert bugs into parse error

		initialize(false);
		goForExpression();
		nestedMethod[nestedType]++;

		referenceContext = type;
		compilationUnit = unit;

		scanner.setSource(initializationSource);
		scanner.resetTo(0, initializationSource.length - 1);
		try {
			parse();
		} catch (AbortCompilation ex) {
			lastAct = ERROR_ACTION;
		} finally {
			nestedMethod[nestedType]--;
		}

		// if (lastAct == ERROR_ACTION) {
		// return;
		// }
		//
		// field.initialization = expressionStack[expressionPtr];
		//	
		// // mark field with local type if one was found during parsing
		// if ((type.bits & ASTNode.HasLocalTypeMASK) != 0) {
		// field.bits |= ASTNode.HasLocalTypeMASK;
		// }
	}

	// A P I

	public void parse(Initializer ini, TypeDeclaration type,
			CompilationUnitDeclaration unit) {
		// only parse the method body of md
		// fill out method statements

		// convert bugs into parse error

		initialize(false);
		goForInitializer();
		nestedMethod[nestedType]++;

		referenceContext = type;
		compilationUnit = unit;

		scanner.resetTo(ini.sourceStart, ini.sourceEnd); // just on the
															// beginning {
		try {
			parse();
		} catch (AbortCompilation ex) {
			lastAct = ERROR_ACTION;
		} finally {
			nestedMethod[nestedType]--;
		}

		// if (lastAct == ERROR_ACTION) {
		// return;
		// }
		//
		// ini.block = ((Initializer) astStack[astPtr]).block;
		//	
		// // mark initializer with local type if one was found during parsing
		// if ((type.bits & ASTNode.HasLocalTypeMASK) != 0) {
		// ini.bits |= ASTNode.HasLocalTypeMASK;
		// }
	}

	// A P I

	public void parse(MethodDeclaration md, CompilationUnitDeclaration unit) {
		// TODO jsurfer - make the parse process work on methods ?
		return;

		// //only parse the method body of md
		// //fill out method statements
		//
		// //convert bugs into parse error
		//
		// if (md.isAbstract())
		// return;
		// // if (md.isNative())
		// // return;
		// // if ((md.modifiers & AccSemicolonBody) != 0)
		// // return;
		//
		// initialize(false);
		// goForMethodBody();
		// nestedMethod[nestedType]++;
		//
		// referenceContext = md;
		// compilationUnit = unit;
		//
		// scanner.resetTo(md.sourceEnd + 1, md.declarationSourceEnd);
		//   
		// // reset the scanner to parser from { down to }
		// try {
		// parse();
		// } catch (AbortCompilation ex) {
		// lastAct = ERROR_ACTION;
		// } finally {
		// nestedMethod[nestedType]--;
		// }
		//
		// // if (lastAct == ERROR_ACTION) {
		// // return;
		// // }
		// //
		// // //refill statements
		// // md.explicitDeclarations = realBlockStack[realBlockPtr--];
		// // int length;
		// // if ((length = astLengthStack[astLengthPtr--]) != 0)
		// // System.arraycopy(
		// // astStack,
		// // (astPtr -= length) + 1,
		// // md.statements = new Statement[length],
		// // 0,
		// // length);
	}

	// A P I

	public CompilationUnitDeclaration parse(ICompilationUnit sourceUnit,
			CompilationResult compilationResult, int start, int end) {
		// parses a compilation unit and manages error handling (even bugs....)

		CompilationUnitDeclaration unit;
		try {
			/* automaton initialization */
			initialize(false);
			goForCompilationUnit();

			/* scanner initialization */
			scanner.setSource(sourceUnit, sourceUnit.getContents());
			scanner.resetTo(start, end);
			/* unit creation */
			referenceContext = compilationUnit = new CompilationUnitDeclaration(
					problemReporter, compilationResult, scanner.source.length);

			/* run automaton */
			parse();
		} catch (SyntaxError syntaxError) {
			// 
		} finally {
			unit = compilationUnit;
			compilationUnit = null; // reset parser
		}
		return unit;
	}

	public CompilationUnitDeclaration dietParse(ICompilationUnit sourceUnit,
			CompilationResult compilationResult) {
		return dietParse(sourceUnit, compilationResult, false);
	}

	public CompilationUnitDeclaration dietParse(ICompilationUnit sourceUnit,
			CompilationResult compilationResult, boolean phpMode) {

		CompilationUnitDeclaration parsedUnit;
		boolean old = diet;
		try {
			diet = true;
			parsedUnit = parse(sourceUnit, compilationResult, phpMode);
		} finally {
			diet = old;
		}
		return parsedUnit;
	}

	public void getMethodBodies(CompilationUnitDeclaration unit) {
		// fill the methods bodies in order for the code to be generated

		if (unit == null)
			return;

		if (unit.ignoreMethodBodies) {
			unit.ignoreFurtherInvestigation = true;
			return;
			// if initial diet parse did not work, no need to dig into method
			// bodies.
		}

		if ((unit.bits & ASTNode.HasAllMethodBodies) != 0)
			return; // work already done ...

		// real parse of the method....
		char[] contents = unit.compilationResult.compilationUnit.getContents();
		this.scanner.setSource(contents);

		// save existing values to restore them at the end of the parsing
		// process
		// see bug 47079 for more details
		int[] oldLineEnds = this.scanner.lineEnds;
		int oldLinePtr = this.scanner.linePtr;

		final int[] lineSeparatorPositions = unit.compilationResult.lineSeparatorPositions;
		this.scanner.lineEnds = lineSeparatorPositions;
		this.scanner.linePtr = lineSeparatorPositions.length - 1;

		// if (this.javadocParser != null && this.javadocParser.checkDocComment)
		// {
		// this.javadocParser.scanner.setSource(contents);
		// }
		if (unit.types != null) {
			for (int i = unit.types.size(); --i >= 0;)
				((TypeDeclaration) unit.types.get(i)).parseMethod(this, unit);
		}

		// tag unit has having read bodies
		unit.bits |= ASTNode.HasAllMethodBodies;

		// this is done to prevent any side effects on the compilation unit
		// result
		// line separator positions array.
		this.scanner.lineEnds = oldLineEnds;
		this.scanner.linePtr = oldLinePtr;
	}
}
