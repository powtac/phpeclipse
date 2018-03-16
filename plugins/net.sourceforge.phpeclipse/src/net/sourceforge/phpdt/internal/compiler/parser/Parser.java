/***********************************************************************************************************************************
 * Copyright (c) 2002 www.phpeclipse.de All rights reserved. This program and the accompanying material are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors: www.phpeclipse.de
 **********************************************************************************************************************************/
package net.sourceforge.phpdt.internal.compiler.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import net.sourceforge.phpdt.core.compiler.CharOperation;
import net.sourceforge.phpdt.core.compiler.ITerminalSymbols;
import net.sourceforge.phpdt.core.compiler.InvalidInputException;
import net.sourceforge.phpdt.internal.compiler.ast.AND_AND_Expression;
import net.sourceforge.phpdt.internal.compiler.ast.ASTNode;
import net.sourceforge.phpdt.internal.compiler.ast.AbstractMethodDeclaration;
import net.sourceforge.phpdt.internal.compiler.ast.BinaryExpression;
import net.sourceforge.phpdt.internal.compiler.ast.Block;
import net.sourceforge.phpdt.internal.compiler.ast.BreakStatement;
import net.sourceforge.phpdt.internal.compiler.ast.CompilationUnitDeclaration;
import net.sourceforge.phpdt.internal.compiler.ast.ConditionalExpression;
import net.sourceforge.phpdt.internal.compiler.ast.ContinueStatement;
import net.sourceforge.phpdt.internal.compiler.ast.EqualExpression;
import net.sourceforge.phpdt.internal.compiler.ast.Expression;
import net.sourceforge.phpdt.internal.compiler.ast.FieldDeclaration;
import net.sourceforge.phpdt.internal.compiler.ast.FieldReference;
import net.sourceforge.phpdt.internal.compiler.ast.IfStatement;
import net.sourceforge.phpdt.internal.compiler.ast.ImportReference;
import net.sourceforge.phpdt.internal.compiler.ast.InstanceOfExpression;
import net.sourceforge.phpdt.internal.compiler.ast.MethodDeclaration;
import net.sourceforge.phpdt.internal.compiler.ast.OR_OR_Expression;
import net.sourceforge.phpdt.internal.compiler.ast.OperatorIds;
import net.sourceforge.phpdt.internal.compiler.ast.ReturnStatement;
import net.sourceforge.phpdt.internal.compiler.ast.SingleTypeReference;
import net.sourceforge.phpdt.internal.compiler.ast.Statement;
import net.sourceforge.phpdt.internal.compiler.ast.StringLiteral;
import net.sourceforge.phpdt.internal.compiler.ast.StringLiteralDQ;
import net.sourceforge.phpdt.internal.compiler.ast.StringLiteralSQ;
import net.sourceforge.phpdt.internal.compiler.ast.TypeDeclaration;
import net.sourceforge.phpdt.internal.compiler.ast.TypeReference;
import net.sourceforge.phpdt.internal.compiler.impl.CompilerOptions;
import net.sourceforge.phpdt.internal.compiler.impl.ReferenceContext;
import net.sourceforge.phpdt.internal.compiler.lookup.CompilerModifiers;
import net.sourceforge.phpdt.internal.compiler.lookup.TypeConstants;
import net.sourceforge.phpdt.internal.compiler.problem.ProblemReporter;
import net.sourceforge.phpdt.internal.compiler.problem.ProblemSeverities;
import net.sourceforge.phpdt.internal.compiler.util.Util;
import net.sourceforge.phpdt.internal.ui.util.PHPFileUtil;
import net.sourceforge.phpeclipse.builder.IdentifierIndexManager;
import net.sourceforge.phpeclipse.ui.overlaypages.ProjectPrefUtil;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

public class Parser implements ITerminalSymbols, CompilerModifiers,
		ParserBasicInformation {
	protected final static int StackIncrement = 255;

	protected int stateStackTop;

	// protected int[] stack = new int[StackIncrement];

	public int firstToken; // handle for multiple parsing goals

	public int lastAct; // handle for multiple parsing goals

	// protected RecoveredElement currentElement;

	public static boolean VERBOSE_RECOVERY = false;

	protected boolean diet = false; // tells the scanner to jump over some

	/**
	 * the PHP token scanner
	 */
	public Scanner scanner;

	int token;

	protected int modifiers;

	protected int modifiersSourceStart;

	protected Parser(ProblemReporter problemReporter) {
		this.problemReporter = problemReporter;
		this.options = problemReporter.options;
		this.token = TokenNameEOF;
		this.initializeScanner();
	}

	public void setFileToParse(IFile fileToParse) {
		this.token = TokenNameEOF;
		this.initializeScanner();
	}

	/**
	 * ClassDeclaration Constructor.
	 * 
	 * @param s
	 * @param sess
	 *            Description of Parameter
	 * @see
	 */
	public Parser(IFile fileToParse) {
		// if (keywordMap == null) {
		// keywordMap = new HashMap();
		// for (int i = 0; i < PHP_KEYWORS.length; i++) {
		// keywordMap.put(PHP_KEYWORS[i], new Integer(PHP_KEYWORD_TOKEN[i]));
		// }
		// }
		// this.currentPHPString = 0;
		// PHPParserSuperclass.fileToParse = fileToParse;
		// this.phpList = null;
		this.includesList = null;
		// this.str = "";
		this.token = TokenNameEOF;
		// this.chIndx = 0;
		// this.rowCount = 1;
		// this.columnCount = 0;
		// this.phpEnd = false;
		// getNextToken();
		this.initializeScanner();
	}

	public void initializeScanner() {
		this.scanner = new Scanner(
				false /* comment */,
				false /* whitespace */,
				this.options.getSeverity(CompilerOptions.NonExternalizedString) != ProblemSeverities.Ignore /* nls */,
				false, false, this.options.taskTags/* taskTags */,
				this.options.taskPriorites/* taskPriorities */, true/* isTaskCaseSensitive */);
	}

	/**
	 * Create marker for the parse error
	 */
	// private void setMarker(String message, int charStart, int charEnd, int
	// errorLevel) {
	// setMarker(fileToParse, message, charStart, charEnd, errorLevel);
	// }
	/**
	 * This method will throw the SyntaxError. It will add the good lines and
	 * columns to the Error
	 * 
	 * @param error
	 *            the error message
	 * @throws SyntaxError
	 *             the error raised
	 */
	private void throwSyntaxError(String error) {
		int problemStartPosition = scanner.getCurrentTokenStartPosition();
		int problemEndPosition = scanner.getCurrentTokenEndPosition() + 1;
		if (scanner.source.length <= problemEndPosition
				&& problemEndPosition > 0) {
			problemEndPosition = scanner.source.length - 1;
			if (problemStartPosition > 0
					&& problemStartPosition >= problemEndPosition
					&& problemEndPosition > 0) {
				problemStartPosition = problemEndPosition - 1;
			}
		}
		throwSyntaxError(error, problemStartPosition, problemEndPosition);
	}

	/**
	 * This method will throw the SyntaxError. It will add the good lines and
	 * columns to the Error
	 * 
	 * @param error
	 *            the error message
	 * @throws SyntaxError
	 *             the error raised
	 */
	// private void throwSyntaxError(String error, int startRow) {
	// throw new SyntaxError(startRow, 0, " ", error);
	// }
	private void throwSyntaxError(String error, int problemStartPosition,
			int problemEndPosition) {
		if (referenceContext != null) {
			problemReporter.phpParsingError(new String[] { error },
					problemStartPosition, problemEndPosition, referenceContext,
					compilationUnit.compilationResult);
		}
		throw new SyntaxError(1, 0, " ", error);
	}

	private void reportSyntaxError(String error) {
		int problemStartPosition = scanner.getCurrentTokenStartPosition();
		int problemEndPosition = scanner.getCurrentTokenEndPosition();
		reportSyntaxError(error, problemStartPosition, problemEndPosition + 1);
	}

	private void reportSyntaxError(String error, int problemStartPosition,
			int problemEndPosition) {
		if (referenceContext != null) {
			problemReporter.phpParsingError(new String[] { error },
					problemStartPosition, problemEndPosition, referenceContext,
					compilationUnit.compilationResult);
		}
	}

	// private void reportSyntaxWarning(String error, int problemStartPosition,
	// int problemEndPosition) {
	// if (referenceContext != null) {
	// problemReporter.phpParsingWarning(new String[] { error },
	// problemStartPosition, problemEndPosition, referenceContext,
	// compilationUnit.compilationResult);
	// }
	// }

	/**
	 * gets the next token from input
	 */
	private void getNextToken() {
		try {
			token = scanner.getNextToken();
			if (Scanner.DEBUG) {
				int currentEndPosition = scanner.getCurrentTokenEndPosition();
				int currentStartPosition = scanner
						.getCurrentTokenStartPosition();
				System.out.print(currentStartPosition + ","
						+ currentEndPosition + ": ");
				System.out.println(scanner.toStringAction(token));
			}
		} catch (InvalidInputException e) {
			token = TokenNameERROR;
			String detailedMessage = e.getMessage();

			if (detailedMessage == Scanner.UNTERMINATED_STRING) {
				throwSyntaxError("Unterminated string.");
			} else if (detailedMessage == Scanner.UNTERMINATED_COMMENT) {
				throwSyntaxError("Unterminated commment.");
			}
		}
		return;
	}

	public void init(String s) {
		// this.str = s;
		this.token = TokenNameEOF;
		this.includesList = new ArrayList();
		// this.chIndx = 0;
		// this.rowCount = 1;
		// this.columnCount = 0;
		// this.phpEnd = false;
		// this.phpMode = false;
		/* scanner initialization */
		scanner.setSource(s.toCharArray());
		scanner.setPHPMode(false);
		astPtr = 0;
	}

	protected void initialize(boolean phpMode) {
		initialize(phpMode, null);
	}

	protected void initialize(boolean phpMode,
			IdentifierIndexManager indexManager) {
		compilationUnit = null;
		referenceContext = null;
		this.includesList = new ArrayList();
		// this.indexManager = indexManager;
		// this.str = "";
		this.token = TokenNameEOF;
		// this.chIndx = 0;
		// this.rowCount = 1;
		// this.columnCount = 0;
		// this.phpEnd = false;
		// this.phpMode = phpMode;
		scanner.setPHPMode(phpMode);
		astPtr = 0;
	}

	/**
	 * Parses a string with php tags i.e. '&lt;body&gt; &lt;?php phpinfo() ?&gt;
	 * &lt;/body&gt;'
	 */
	public void parse(String s) {
		parse(s, null);
	}

	/**
	 * Parses a string with php tags i.e. '&lt;body&gt; &lt;?php phpinfo() ?&gt;
	 * &lt;/body&gt;'
	 */
	public void parse(String s, HashMap variables) {
		fMethodVariables = variables;
		fStackUnassigned = new ArrayList();
		init(s);
		parse();
	}

	/**
	 * Parses a string with php tags i.e. '&lt;body&gt; &lt;?php phpinfo() ?&gt;
	 * &lt;/body&gt;'
	 */
	protected void parse() {
		if (scanner.compilationUnit != null) {
			IResource resource = scanner.compilationUnit.getResource();
			if (resource != null && resource instanceof IFile) {
				// set the package name
				consumePackageDeclarationName((IFile) resource);
			}
		}
		getNextToken();
		do {
			try {
				if (token != TokenNameEOF && token != TokenNameERROR) {
					statementList();
				}
				if (token != TokenNameEOF) {
					if (token == TokenNameERROR) {
						throwSyntaxError("Scanner error (Found unknown token: "
								+ scanner.toStringAction(token) + ")");
					}
					if (token == TokenNameRPAREN) {
						throwSyntaxError("Too many closing ')'; end-of-file not reached.");
					}
					if (token == TokenNameRBRACE) {
						throwSyntaxError("Too many closing '}'; end-of-file not reached.");
					}
					if (token == TokenNameRBRACKET) {
						throwSyntaxError("Too many closing ']'; end-of-file not reached.");
					}
					if (token == TokenNameLPAREN) {
						throwSyntaxError("Read character '('; end-of-file not reached.");
					}
					if (token == TokenNameLBRACE) {
						throwSyntaxError("Read character '{';  end-of-file not reached.");
					}
					if (token == TokenNameLBRACKET) {
						throwSyntaxError("Read character '[';  end-of-file not reached.");
					}
					throwSyntaxError("End-of-file not reached.");
				}
				break;
			} catch (SyntaxError syntaxError) {
				// syntaxError.printStackTrace();
				break;
			}
		} while (true);

		endParse(0);
	}

	/**
	 * Parses a string with php tags i.e. '&lt;body&gt; &lt;?php phpinfo() ?&gt;
	 * &lt;/body&gt;'
	 */
	public void parseFunction(String s, HashMap variables) {
		init(s);
		scanner.phpMode = true;
		parseFunction(variables);
	}

	/**
	 * Parses a string with php tags i.e. '&lt;body&gt; &lt;?php phpinfo() ?&gt;
	 * &lt;/body&gt;'
	 */
	protected void parseFunction(HashMap variables) {
		getNextToken();
		boolean hasModifiers = member_modifiers();
		if (token == TokenNamefunction) {
			if (!hasModifiers) {
				checkAndSetModifiers(AccPublic);
			}
			this.fMethodVariables = variables;

			MethodDeclaration methodDecl = new MethodDeclaration(null);
			methodDecl.declarationSourceStart = scanner
					.getCurrentTokenStartPosition();
			methodDecl.modifiers = this.modifiers;
			methodDecl.type = MethodDeclaration.METHOD_DEFINITION;
			try {
				getNextToken();
				functionDefinition(methodDecl);
			} catch (SyntaxError sytaxErr1) {
				return;
			} finally {
				int sourceEnd = methodDecl.sourceEnd;
				if (sourceEnd <= 0
						|| methodDecl.declarationSourceStart > sourceEnd) {
					sourceEnd = methodDecl.declarationSourceStart + 1;
				}
				methodDecl.sourceEnd = sourceEnd;
				methodDecl.declarationSourceEnd = sourceEnd;
			}
		}
	}

	protected CompilationUnitDeclaration endParse(int act) {

		this.lastAct = act;

		// if (currentElement != null) {
		// currentElement.topElement().updateParseTree();
		// if (VERBOSE_RECOVERY) {
		// System.out.print(Util.bind("parser.syntaxRecovery")); //$NON-NLS-1$
		// System.out.println("--------------------------"); //$NON-NLS-1$
		// System.out.println(compilationUnit);
		// System.out.println("----------------------------------");
		// //$NON-NLS-1$
		// }
		// } else {
		if (diet & VERBOSE_RECOVERY) {
			System.out.print(Util.bind("parser.regularParse")); //$NON-NLS-1$
			System.out.println("--------------------------"); //$NON-NLS-1$
			System.out.println(compilationUnit);
			System.out.println("----------------------------------"); //$NON-NLS-1$
		}
		// }
		if (scanner.recordLineSeparator) {
			compilationUnit.compilationResult.lineSeparatorPositions = scanner
					.getLineEnds();
		}
		if (scanner.taskTags != null) {
			for (int i = 0; i < scanner.foundTaskCount; i++) {
				problemReporter().task(
						new String(scanner.foundTaskTags[i]),
						new String(scanner.foundTaskMessages[i]),
						scanner.foundTaskPriorities[i] == null ? null
								: new String(scanner.foundTaskPriorities[i]),
						scanner.foundTaskPositions[i][0],
						scanner.foundTaskPositions[i][1]);
			}
		}
		compilationUnit.imports = new ImportReference[includesList.size()];
		for (int i = 0; i < includesList.size(); i++) {
			compilationUnit.imports[i] = (ImportReference) includesList.get(i);
		}
		return compilationUnit;
	}

	private Block statementList() {
		boolean branchStatement = false;
		Statement statement;
		int blockStart = scanner.getCurrentTokenStartPosition();
		ArrayList blockStatements = new ArrayList();
		do {
			try {
				statement = statement();
				blockStatements.add(statement);
				if (token == TokenNameEOF) {
					return null;
				}
				if (branchStatement && statement != null) {
					// reportSyntaxError("Unreachable code",
					// statement.sourceStart,
					// statement.sourceEnd);
					if (!(statement instanceof BreakStatement)) {
						/*
						 * don't give an error for break statement following
						 * return statement Technically it's unreachable code,
						 * but in switch-case it's recommended to avoid
						 * accidental fall-through later when editing the code
						 */
						problemReporter.unreachableCode(new String(scanner
								.getCurrentIdentifierSource()),
								statement.sourceStart, statement.sourceEnd,
								referenceContext,
								compilationUnit.compilationResult);
					}
				}
				if ((token == TokenNameRBRACE) || (token == TokenNamecase)
						|| (token == TokenNamedefault)
						|| (token == TokenNameelse)
						|| (token == TokenNameelseif)
						|| (token == TokenNameendif)
						|| (token == TokenNameendfor)
						|| (token == TokenNameendforeach)
						|| (token == TokenNameendwhile)
						|| (token == TokenNameendswitch)
						|| (token == TokenNameenddeclare)
						|| (token == TokenNameEOF) || (token == TokenNameERROR)) {
					return createBlock(blockStart, blockStatements);
				}
				branchStatement = checkUnreachableStatements(statement);
			} catch (SyntaxError sytaxErr1) {
				// if an error occured,
				// try to find keywords
				// to parse the rest of the string
				boolean tokenize = scanner.tokenizeStrings;
				if (!tokenize) {
					scanner.tokenizeStrings = true;
				}
				try {
					while (token != TokenNameEOF) {
						if ((token == TokenNameRBRACE)
								|| (token == TokenNamecase)
								|| (token == TokenNamedefault)
								|| (token == TokenNameelse)
								|| (token == TokenNameelseif)
								|| (token == TokenNameendif)
								|| (token == TokenNameendfor)
								|| (token == TokenNameendforeach)
								|| (token == TokenNameendwhile)
								|| (token == TokenNameendswitch)
								|| (token == TokenNameenddeclare)
								|| (token == TokenNameEOF)
								|| (token == TokenNameERROR)) {
							return createBlock(blockStart, blockStatements);
						}
						if (token == TokenNameif || token == TokenNameswitch
								|| token == TokenNamefor
								|| token == TokenNamewhile
								|| token == TokenNamedo
								|| token == TokenNameforeach
								|| token == TokenNamecontinue
								|| token == TokenNamebreak
								|| token == TokenNamereturn
								|| token == TokenNameexit
								|| token == TokenNameecho
								|| token == TokenNameECHO_INVISIBLE
								|| token == TokenNameglobal
								|| token == TokenNamestatic
								|| token == TokenNameunset
								|| token == TokenNamefunction
								|| token == TokenNamedeclare
								|| token == TokenNametry
								|| token == TokenNamecatch
								|| token == TokenNamethrow
								|| token == TokenNamefinal
								|| token == TokenNameabstract
								|| token == TokenNameclass
								|| token == TokenNameinterface) {
							break;
						}
						// System.out.println(scanner.toStringAction(token));
						getNextToken();
						// System.out.println(scanner.toStringAction(token));
					}
					if (token == TokenNameEOF) {
						throw sytaxErr1;
					}
				} finally {
					scanner.tokenizeStrings = tokenize;
				}
			}
		} while (true);
	}

	/**
	 * @param statement
	 * @return
	 */
	private boolean checkUnreachableStatements(Statement statement) {
		if (statement instanceof ReturnStatement
				|| statement instanceof ContinueStatement
				|| statement instanceof BreakStatement) {
			return true;
		} else if (statement instanceof IfStatement
				&& ((IfStatement) statement).checkUnreachable) {
			return true;
		}
		return false;
	}

	/**
	 * @param blockStart
	 * @param blockStatements
	 * @return
	 */
	private Block createBlock(int blockStart, ArrayList blockStatements) {
		int blockEnd = scanner.getCurrentTokenEndPosition();
		Block b = Block.EmptyWith(blockStart, blockEnd);
		b.statements = new Statement[blockStatements.size()];
		blockStatements.toArray(b.statements);
		return b;
	}

	private void functionBody(MethodDeclaration methodDecl) {
		// '{' [statement-list] '}'
		if (token == TokenNameLBRACE) {
			getNextToken();
		} else {
			methodDecl.sourceEnd = scanner.getCurrentTokenStartPosition() - 1;
			throwSyntaxError("'{' expected in compound-statement.");
		}
		if (token != TokenNameRBRACE) {
			statementList();
		}
		if (token == TokenNameRBRACE) {
			methodDecl.sourceEnd = scanner.getCurrentTokenEndPosition();
			getNextToken();
		} else {
			methodDecl.sourceEnd = scanner.getCurrentTokenStartPosition() - 1;
			throwSyntaxError("'}' expected in compound-statement.");
		}
	}

	private Statement statement() {
		Statement statement = null;
		Expression expression;
		int sourceStart = scanner.getCurrentTokenStartPosition();
		int sourceEnd;
		if (token == TokenNameif) {
			// T_IF '(' expr ')' statement elseif_list else_single
			// T_IF '(' expr ')' ':' inner_statement_list new_elseif_list
			// new_else_single T_ENDIF ';'
			getNextToken();
			if (token == TokenNameLPAREN) {
				getNextToken();
			} else {
				throwSyntaxError("'(' expected after 'if' keyword.");
			}
			expression = expr();
			if (token == TokenNameRPAREN) {
				getNextToken();
			} else {
				throwSyntaxError("')' expected after 'if' condition.");
			}
			// create basic IfStatement
			IfStatement ifStatement = new IfStatement(expression, null, null,
					sourceStart, -1);
			if (token == TokenNameCOLON) {
				getNextToken();
				ifStatementColon(ifStatement);
			} else {
				ifStatement(ifStatement);
			}
			return ifStatement;
		} else if (token == TokenNameswitch) {
			getNextToken();
			if (token == TokenNameLPAREN) {
				getNextToken();
			} else {
				throwSyntaxError("'(' expected after 'switch' keyword.");
			}
			expr();
			if (token == TokenNameRPAREN) {
				getNextToken();
			} else {
				throwSyntaxError("')' expected after 'switch' condition.");
			}
			switchStatement();
			return statement;
		} else if (token == TokenNamefor) {
			getNextToken();
			if (token == TokenNameLPAREN) {
				getNextToken();
			} else {
				throwSyntaxError("'(' expected after 'for' keyword.");
			}
			if (token == TokenNameSEMICOLON) {
				getNextToken();
			} else {
				expressionList();
				if (token == TokenNameSEMICOLON) {
					getNextToken();
				} else {
					throwSyntaxError("';' expected after 'for'.");
				}
			}
			if (token == TokenNameSEMICOLON) {
				getNextToken();
			} else {
				expressionList();
				if (token == TokenNameSEMICOLON) {
					getNextToken();
				} else {
					throwSyntaxError("';' expected after 'for'.");
				}
			}
			if (token == TokenNameRPAREN) {
				getNextToken();
			} else {
				expressionList();
				if (token == TokenNameRPAREN) {
					getNextToken();
				} else {
					throwSyntaxError("')' expected after 'for'.");
				}
			}
			forStatement();
			return statement;
		} else if (token == TokenNamewhile) {
			getNextToken();
			if (token == TokenNameLPAREN) {
				getNextToken();
			} else {
				throwSyntaxError("'(' expected after 'while' keyword.");
			}
			expr();
			if (token == TokenNameRPAREN) {
				getNextToken();
			} else {
				throwSyntaxError("')' expected after 'while' condition.");
			}
			whileStatement();
			return statement;
		} else if (token == TokenNamedo) {
			getNextToken();
			if (token == TokenNameLBRACE) {
				getNextToken();
				if (token != TokenNameRBRACE) {
					statementList();
				}
				if (token == TokenNameRBRACE) {
					getNextToken();
				} else {
					throwSyntaxError("'}' expected after 'do' keyword.");
				}
			} else {
				statement();
			}
			if (token == TokenNamewhile) {
				getNextToken();
				if (token == TokenNameLPAREN) {
					getNextToken();
				} else {
					throwSyntaxError("'(' expected after 'while' keyword.");
				}
				expr();
				if (token == TokenNameRPAREN) {
					getNextToken();
				} else {
					throwSyntaxError("')' expected after 'while' condition.");
				}
			} else {
				throwSyntaxError("'while' expected after 'do' keyword.");
			}
			if (token == TokenNameSEMICOLON) {
				getNextToken();
			} else {
				if (token != TokenNameINLINE_HTML) {
					throwSyntaxError("';' expected after do-while statement.");
				}
				getNextToken();
			}
			return statement;
		} else if (token == TokenNameforeach) {
			getNextToken();
			if (token == TokenNameLPAREN) {
				getNextToken();
			} else {
				throwSyntaxError("'(' expected after 'foreach' keyword.");
			}
			expr();
			if (token == TokenNameas) {
				getNextToken();
			} else {
				throwSyntaxError("'as' expected after 'foreach' exxpression.");
			}
			// variable();
			foreach_variable();
			foreach_optional_arg();
			if (token == TokenNameEQUAL_GREATER) {
				getNextToken();
				variable(false, false);
			}
			if (token == TokenNameRPAREN) {
				getNextToken();
			} else {
				throwSyntaxError("')' expected after 'foreach' expression.");
			}
			foreachStatement();
			return statement;
		} else if (token == TokenNamebreak) {
			expression = null;
			getNextToken();
			if (token != TokenNameSEMICOLON) {
				expression = expr();
			}
			if (token == TokenNameSEMICOLON) {
				sourceEnd = scanner.getCurrentTokenEndPosition();
				getNextToken();
			} else {
				if (token != TokenNameINLINE_HTML) {
					throwSyntaxError("';' expected after 'break'.");
				}
				sourceEnd = scanner.getCurrentTokenEndPosition();
				getNextToken();
			}
			return new BreakStatement(null, sourceStart, sourceEnd);
		} else if (token == TokenNamecontinue) {
			expression = null;
			getNextToken();
			if (token != TokenNameSEMICOLON) {
				expression = expr();
			}
			if (token == TokenNameSEMICOLON) {
				sourceEnd = scanner.getCurrentTokenEndPosition();
				getNextToken();
			} else {
				if (token != TokenNameINLINE_HTML) {
					throwSyntaxError("';' expected after 'continue'.");
				}
				sourceEnd = scanner.getCurrentTokenEndPosition();
				getNextToken();
			}
			return new ContinueStatement(null, sourceStart, sourceEnd);
		} else if (token == TokenNamereturn) {
			expression = null;
			getNextToken();
			if (token != TokenNameSEMICOLON) {
				expression = expr();
			}
			if (token == TokenNameSEMICOLON) {
				sourceEnd = scanner.getCurrentTokenEndPosition();
				getNextToken();
			} else {
				if (token != TokenNameINLINE_HTML) {
					throwSyntaxError("';' expected after 'return'.");
				}
				sourceEnd = scanner.getCurrentTokenEndPosition();
				getNextToken();
			}
			return new ReturnStatement(expression, sourceStart, sourceEnd);
		} else if (token == TokenNameecho) {
			getNextToken();
			expressionList();
			if (token == TokenNameSEMICOLON) {
				getNextToken();
			} else {
				if (token != TokenNameINLINE_HTML) {
					throwSyntaxError("';' expected after 'echo' statement.");
				}
				getNextToken();
			}
			return statement;
		} else if (token == TokenNameECHO_INVISIBLE) {
			// 0-length token directly after PHP short tag &lt;?=
			getNextToken();
			expressionList();
			if (token == TokenNameSEMICOLON) {
				getNextToken();
				// if (token != TokenNameINLINE_HTML) {
				// // TODO should this become a configurable warning?
				// reportSyntaxError("Probably '?>' expected after PHP short tag
				// expression (only the first expression will be echoed).");
				// }
			} else {
				if (token != TokenNameINLINE_HTML) {
					throwSyntaxError("';' expected after PHP short tag '<?=' expression.");
				}
				getNextToken();
			}
			return statement;
		} else if (token == TokenNameINLINE_HTML) {
			getNextToken();
			return statement;
		} else if (token == TokenNameglobal) {
			getNextToken();
			global_var_list();
			if (token == TokenNameSEMICOLON) {
				getNextToken();
			} else {
				if (token != TokenNameINLINE_HTML) {
					throwSyntaxError("';' expected after 'global' statement.");
				}
				getNextToken();
			}
			return statement;
		} else if (token == TokenNamestatic) {
			getNextToken();
			static_var_list();
			if (token == TokenNameSEMICOLON) {
				getNextToken();
			} else {
				if (token != TokenNameINLINE_HTML) {
					throwSyntaxError("';' expected after 'static' statement.");
				}
				getNextToken();
			}
			return statement;
		} else if (token == TokenNameunset) {
			getNextToken();
			if (token == TokenNameLPAREN) {
				getNextToken();
			} else {
				throwSyntaxError("'(' expected after 'unset' statement.");
			}
			unset_variables();
			if (token == TokenNameRPAREN) {
				getNextToken();
			} else {
				throwSyntaxError("')' expected after 'unset' statement.");
			}
			if (token == TokenNameSEMICOLON) {
				getNextToken();
			} else {
				if (token != TokenNameINLINE_HTML) {
					throwSyntaxError("';' expected after 'unset' statement.");
				}
				getNextToken();
			}
			return statement;
		} else if (token == TokenNamefunction) {
			MethodDeclaration methodDecl = new MethodDeclaration(
					this.compilationUnit.compilationResult);
			methodDecl.declarationSourceStart = scanner
					.getCurrentTokenStartPosition();
			methodDecl.modifiers = AccDefault;
			methodDecl.type = MethodDeclaration.FUNCTION_DEFINITION;
			try {
				getNextToken();
				functionDefinition(methodDecl);
			} finally {
				sourceEnd = methodDecl.sourceEnd;
				if (sourceEnd <= 0
						|| methodDecl.declarationSourceStart > sourceEnd) {
					sourceEnd = methodDecl.declarationSourceStart + 1;
				}
				methodDecl.declarationSourceEnd = sourceEnd;
				methodDecl.sourceEnd = sourceEnd;
			}
			return statement;
		} else if (token == TokenNamedeclare) {
			// T_DECLARE '(' declare_list ')' declare_statement
			getNextToken();
			if (token != TokenNameLPAREN) {
				throwSyntaxError("'(' expected in 'declare' statement.");
			}
			getNextToken();
			declare_list();
			if (token != TokenNameRPAREN) {
				throwSyntaxError("')' expected in 'declare' statement.");
			}
			getNextToken();
			declare_statement();
			return statement;
		} else if (token == TokenNametry) {
			getNextToken();
			if (token != TokenNameLBRACE) {
				throwSyntaxError("'{' expected in 'try' statement.");
			}
			getNextToken();
			statementList();
			if (token != TokenNameRBRACE) {
				throwSyntaxError("'}' expected in 'try' statement.");
			}
			getNextToken();
			return statement;
		} else if (token == TokenNamecatch) {
			getNextToken();
			if (token != TokenNameLPAREN) {
				throwSyntaxError("'(' expected in 'catch' statement.");
			}
			getNextToken();
			fully_qualified_class_name();
			if (token != TokenNameVariable) {
				throwSyntaxError("Variable expected in 'catch' statement.");
			}
			addVariableSet();
			getNextToken();
			if (token != TokenNameRPAREN) {
				throwSyntaxError("')' expected in 'catch' statement.");
			}
			getNextToken();
			if (token != TokenNameLBRACE) {
				throwSyntaxError("'{' expected in 'catch' statement.");
			}
			getNextToken();
			if (token != TokenNameRBRACE) {
				statementList();
				if (token != TokenNameRBRACE) {
					throwSyntaxError("'}' expected in 'catch' statement.");
				}
			}
			getNextToken();
			additional_catches();
			return statement;
		} else if (token == TokenNamethrow) {
			getNextToken();
			expr();
			if (token == TokenNameSEMICOLON) {
				getNextToken();
			} else {
				throwSyntaxError("';' expected after 'throw' exxpression.");
			}
			return statement;
		} else if (token == TokenNamefinal || token == TokenNameabstract
				|| token == TokenNameclass || token == TokenNameinterface) {
			try {
				TypeDeclaration typeDecl = new TypeDeclaration(
						this.compilationUnit.compilationResult);
				typeDecl.declarationSourceStart = scanner
						.getCurrentTokenStartPosition();
				typeDecl.declarationSourceEnd = scanner
						.getCurrentTokenEndPosition();
				typeDecl.name = new char[] { ' ' };
				// default super class
				typeDecl.superclass = new SingleTypeReference(
						TypeConstants.OBJECT, 0);
				compilationUnit.types.add(typeDecl);
				pushOnAstStack(typeDecl);
				unticked_class_declaration_statement(typeDecl);
			} finally {
				// reduce stack:
				astPtr--;
				astLengthPtr--;
			}
			return statement;
			// } else {
			// throwSyntaxError("Unexpected keyword '" + keyword + "'");
		} else if (token == TokenNameLBRACE) {
			getNextToken();
			if (token != TokenNameRBRACE) {
				statement = statementList();
			}
			if (token == TokenNameRBRACE) {
				getNextToken();
				return statement;
			} else {
				throwSyntaxError("'}' expected.");
			}
		} else {
			if (token != TokenNameSEMICOLON) {
				expr();
			}
			if (token == TokenNameSEMICOLON) {
				getNextToken();
				return statement;
			} else {
				if (token == TokenNameRBRACE) {
					reportSyntaxError("';' expected after expression (Found token: "
							+ scanner.toStringAction(token) + ")");
				} else {
					if (token != TokenNameINLINE_HTML && token != TokenNameEOF) {
						throwSyntaxError("';' expected after expression (Found token: "
								+ scanner.toStringAction(token) + ")");
					}
					getNextToken();
				}
			}
		}
		// may be null
		return statement;
	}

	private void declare_statement() {
		// statement
		// | ':' inner_statement_list T_ENDDECLARE ';'
		// ;
		if (token == TokenNameCOLON) {
			getNextToken();
			// TODO: implement inner_statement_list();
			statementList();
			if (token != TokenNameenddeclare) {
				throwSyntaxError("'enddeclare' expected in 'declare' statement.");
			}
			getNextToken();
			if (token != TokenNameSEMICOLON) {
				throwSyntaxError("';' expected after 'enddeclare' keyword.");
			}
			getNextToken();
		} else {
			statement();
		}
	}

	private void declare_list() {
		// T_STRING '=' static_scalar
		// | declare_list ',' T_STRING '=' static_scalar
		while (true) {
			if (token != TokenNameIdentifier) {
				throwSyntaxError("Identifier expected in 'declare' list.");
			}
			getNextToken();
			if (token != TokenNameEQUAL) {
				throwSyntaxError("'=' expected in 'declare' list.");
			}
			getNextToken();
			static_scalar();
			if (token != TokenNameCOMMA) {
				break;
			}
			getNextToken();
		}
	}

	private void additional_catches() {
		while (token == TokenNamecatch) {
			getNextToken();
			if (token != TokenNameLPAREN) {
				throwSyntaxError("'(' expected in 'catch' statement.");
			}
			getNextToken();
			fully_qualified_class_name();
			if (token != TokenNameVariable) {
				throwSyntaxError("Variable expected in 'catch' statement.");
			}
			addVariableSet();
			getNextToken();
			if (token != TokenNameRPAREN) {
				throwSyntaxError("')' expected in 'catch' statement.");
			}
			getNextToken();
			if (token != TokenNameLBRACE) {
				throwSyntaxError("'{' expected in 'catch' statement.");
			}
			getNextToken();
			if (token != TokenNameRBRACE) {
				statementList();
			}
			if (token != TokenNameRBRACE) {
				throwSyntaxError("'}' expected in 'catch' statement.");
			}
			getNextToken();
		}
	}

	private void foreach_variable() {
		// w_variable
		// | '&' w_variable
		if (token == TokenNameAND) {
			getNextToken();
		}
		w_variable(true);
	}

	private void foreach_optional_arg() {
		// /* empty */
		// | T_DOUBLE_ARROW foreach_variable
		if (token == TokenNameEQUAL_GREATER) {
			getNextToken();
			foreach_variable();
		}
	}

	private void global_var_list() {
		// global_var_list:
		// global_var_list ',' global_var
		// | global_var
		HashSet set = peekVariableSet();
		while (true) {
			global_var(set);
			if (token != TokenNameCOMMA) {
				break;
			}
			getNextToken();
		}
	}

	private void global_var(HashSet set) {
		// global_var:
		// T_VARIABLE
		// | '$' r_variable
		// | '$' '{' expr '}'
		if (token == TokenNameVariable) {
			if (fMethodVariables != null) {
				VariableInfo info = new VariableInfo(scanner
						.getCurrentTokenStartPosition(),
						VariableInfo.LEVEL_GLOBAL_VAR);
				fMethodVariables.put(new String(scanner
						.getCurrentIdentifierSource()), info);
			}
			addVariableSet(set);
			getNextToken();
		} else if (token == TokenNameDOLLAR) {
			getNextToken();
			if (token == TokenNameLBRACE) {
				getNextToken();
				expr();
				if (token != TokenNameRBRACE) {
					throwSyntaxError("'}' expected in global variable.");
				}
				getNextToken();
			} else {
				r_variable();
			}
		}
	}

	private void static_var_list() {
		// static_var_list:
		// static_var_list ',' T_VARIABLE
		// | static_var_list ',' T_VARIABLE '=' static_scalar
		// | T_VARIABLE
		// | T_VARIABLE '=' static_scalar,
		HashSet set = peekVariableSet();
		while (true) {
			if (token == TokenNameVariable) {
				if (fMethodVariables != null) {
					VariableInfo info = new VariableInfo(scanner
							.getCurrentTokenStartPosition(),
							VariableInfo.LEVEL_STATIC_VAR);
					fMethodVariables.put(new String(scanner
							.getCurrentIdentifierSource()), info);
				}
				addVariableSet(set);
				getNextToken();
				if (token == TokenNameEQUAL) {
					getNextToken();
					static_scalar();
				}
				if (token != TokenNameCOMMA) {
					break;
				}
				getNextToken();
			} else {
				break;
			}
		}
	}

	private void unset_variables() {
		// unset_variables:
		// unset_variable
		// | unset_variables ',' unset_variable
		// unset_variable:
		// variable
		while (true) {
			variable(false, false);
			if (token != TokenNameCOMMA) {
				break;
			}
			getNextToken();
		}
	}

	private final void initializeModifiers() {
		this.modifiers = 0;
		this.modifiersSourceStart = -1;
	}

	private final void checkAndSetModifiers(int flag) {
		this.modifiers |= flag;
		if (this.modifiersSourceStart < 0)
			this.modifiersSourceStart = this.scanner.startPosition;
	}

	private void unticked_class_declaration_statement(TypeDeclaration typeDecl) {
		initializeModifiers();
		if (token == TokenNameinterface) {
			// interface_entry T_STRING
			// interface_extends_list
			// '{' class_statement_list '}'
			checkAndSetModifiers(AccInterface);
			getNextToken();
			typeDecl.modifiers = this.modifiers;
			typeDecl.sourceStart = scanner.getCurrentTokenStartPosition();
			typeDecl.sourceEnd = scanner.getCurrentTokenEndPosition();
			if (token == TokenNameIdentifier || token > TokenNameKEYWORD) {
				typeDecl.name = scanner.getCurrentIdentifierSource();
				if (token > TokenNameKEYWORD) {
					problemReporter.phpKeywordWarning(new String[] { scanner
							.toStringAction(token) }, scanner
							.getCurrentTokenStartPosition(), scanner
							.getCurrentTokenEndPosition(), referenceContext,
							compilationUnit.compilationResult);
					// throwSyntaxError("Don't use a keyword for interface
					// declaration ["
					// + scanner.toStringAction(token) + "].",
					// typeDecl.sourceStart, typeDecl.sourceEnd);
				}
				getNextToken();
				interface_extends_list(typeDecl);
			} else {
				typeDecl.name = new char[] { ' ' };
				throwSyntaxError(
						"Interface name expected after keyword 'interface'.",
						typeDecl.sourceStart, typeDecl.sourceEnd);
				return;
			}
		} else {
			// class_entry_type T_STRING extends_from
			// implements_list
			// '{' class_statement_list'}'
			class_entry_type();
			typeDecl.modifiers = this.modifiers;
			typeDecl.sourceStart = scanner.getCurrentTokenStartPosition();
			typeDecl.sourceEnd = scanner.getCurrentTokenEndPosition();
			// identifier
			// identifier 'extends' identifier
			if (token == TokenNameIdentifier || token > TokenNameKEYWORD) {
				typeDecl.name = scanner.getCurrentIdentifierSource();
				if (token > TokenNameKEYWORD) {
					problemReporter.phpKeywordWarning(new String[] { scanner
							.toStringAction(token) }, scanner
							.getCurrentTokenStartPosition(), scanner
							.getCurrentTokenEndPosition(), referenceContext,
							compilationUnit.compilationResult);
					// throwSyntaxError("Don't use a keyword for class
					// declaration [" +
					// scanner.toStringAction(token) + "].",
					// typeDecl.sourceStart, typeDecl.sourceEnd);
				}
				getNextToken();
				// extends_from:
				// /* empty */
				// | T_EXTENDS fully_qualified_class_name
				if (token == TokenNameextends) {
					class_extends_list(typeDecl);
					// getNextToken();
					// if (token != TokenNameIdentifier) {
					// throwSyntaxError("Class name expected after keyword
					// 'extends'.",
					// scanner.getCurrentTokenStartPosition(), scanner
					// .getCurrentTokenEndPosition());
					// }
				}
				implements_list(typeDecl);
			} else {
				typeDecl.name = new char[] { ' ' };
				throwSyntaxError("Class name expected after keyword 'class'.",
						typeDecl.sourceStart, typeDecl.sourceEnd);
				return;
			}
		}
		// '{' class_statement_list '}'
		if (token == TokenNameLBRACE) {
			getNextToken();
			if (token != TokenNameRBRACE) {
				ArrayList list = new ArrayList();
				class_statement_list(list);
				typeDecl.fields = new FieldDeclaration[list.size()];
				for (int i = 0; i < list.size(); i++) {
					typeDecl.fields[i] = (FieldDeclaration) list.get(i);
				}
			}
			if (token == TokenNameRBRACE) {
				typeDecl.declarationSourceEnd = scanner
						.getCurrentTokenEndPosition();
				getNextToken();
			} else {
				throwSyntaxError("'}' expected at end of class body.");
			}
		} else {
			throwSyntaxError("'{' expected at start of class body.");
		}
	}

	private void class_entry_type() {
		// T_CLASS
		// | T_ABSTRACT T_CLASS
		// | T_FINAL T_CLASS
		if (token == TokenNameclass) {
			getNextToken();
		} else if (token == TokenNameabstract) {
			checkAndSetModifiers(AccAbstract);
			getNextToken();
			if (token != TokenNameclass) {
				throwSyntaxError("Keyword 'class' expected after keyword 'abstract'.");
			}
			getNextToken();
		} else if (token == TokenNamefinal) {
			checkAndSetModifiers(AccFinal);
			getNextToken();
			if (token != TokenNameclass) {
				throwSyntaxError("Keyword 'class' expected after keyword 'final'.");
			}
			getNextToken();
		} else {
			throwSyntaxError("Keyword 'class' 'final' or 'abstract' expected");
		}
	}

	// private void class_extends(TypeDeclaration typeDecl) {
	// // /* empty */
	// // | T_EXTENDS interface_list
	// if (token == TokenNameextends) {
	// getNextToken();
	//
	// if (token == TokenNameIdentifier) {
	// getNextToken();
	// } else {
	// throwSyntaxError("Class name expected after keyword 'extends'.");
	// }
	// }
	// }

	private void interface_extends_list(TypeDeclaration typeDecl) {
		// /* empty */
		// | T_EXTENDS interface_list
		if (token == TokenNameextends) {
			getNextToken();
			interface_list(typeDecl);
		}
	}

	private void class_extends_list(TypeDeclaration typeDecl) {
		// /* empty */
		// | T_EXTENDS interface_list
		if (token == TokenNameextends) {
			getNextToken();
			class_list(typeDecl);
		}
	}

	private void implements_list(TypeDeclaration typeDecl) {
		// /* empty */
		// | T_IMPLEMENTS interface_list
		if (token == TokenNameimplements) {
			getNextToken();
			interface_list(typeDecl);
		}
	}

	private void class_list(TypeDeclaration typeDecl) {
		// class_list:
		// fully_qualified_class_name
		do {
			if (token == TokenNameIdentifier) {
				char[] ident = scanner.getCurrentIdentifierSource();
				// TODO make this code working better:
				// SingleTypeReference ref =
				// ParserUtil.getTypeReference(scanner,
				// includesList, ident);
				// if (ref != null) {
				// typeDecl.superclass = ref;
				// }
				getNextToken();
			} else {
				throwSyntaxError("Classname expected after keyword 'extends'.");
			}
			if (token == TokenNameCOMMA) {
				reportSyntaxError("No multiple inheritance allowed. Expected token 'implements' or '{'.");
				getNextToken();
				continue;
			} else {
				break;
			}
		} while (true);
	}

	private void interface_list(TypeDeclaration typeDecl) {
		// interface_list:
		// fully_qualified_class_name
		// | interface_list ',' fully_qualified_class_name
		do {
			if (token == TokenNameIdentifier) {
				getNextToken();
			} else {
				throwSyntaxError("Interfacename expected after keyword 'implements'.");
			}
			if (token != TokenNameCOMMA) {
				return;
			}
			getNextToken();
		} while (true);
	}

	// private void classBody(TypeDeclaration typeDecl) {
	// //'{' [class-element-list] '}'
	// if (token == TokenNameLBRACE) {
	// getNextToken();
	// if (token != TokenNameRBRACE) {
	// class_statement_list();
	// }
	// if (token == TokenNameRBRACE) {
	// typeDecl.declarationSourceEnd = scanner.getCurrentTokenEndPosition();
	// getNextToken();
	// } else {
	// throwSyntaxError("'}' expected at end of class body.");
	// }
	// } else {
	// throwSyntaxError("'{' expected at start of class body.");
	// }
	// }
	private void class_statement_list(ArrayList list) {
		do {
			try {
				class_statement(list);
				if (token == TokenNamepublic || token == TokenNameprotected
						|| token == TokenNameprivate
						|| token == TokenNamestatic
						|| token == TokenNameabstract
						|| token == TokenNamefinal
						|| token == TokenNamefunction || token == TokenNamevar
						|| token == TokenNameconst) {
					continue;
				}
				if (token == TokenNameRBRACE) {
					break;
				}
				throwSyntaxError("'}' at end of class statement.");
			} catch (SyntaxError sytaxErr1) {
				boolean tokenize = scanner.tokenizeStrings;
				if (!tokenize) {
					scanner.tokenizeStrings = true;
				}
				try {
					// if an error occured,
					// try to find keywords
					// to parse the rest of the string
					while (token != TokenNameEOF) {
						if (token == TokenNamepublic
								|| token == TokenNameprotected
								|| token == TokenNameprivate
								|| token == TokenNamestatic
								|| token == TokenNameabstract
								|| token == TokenNamefinal
								|| token == TokenNamefunction
								|| token == TokenNamevar
								|| token == TokenNameconst) {
							break;
						}
						// System.out.println(scanner.toStringAction(token));
						getNextToken();
					}
					if (token == TokenNameEOF) {
						throw sytaxErr1;
					}
				} finally {
					scanner.tokenizeStrings = tokenize;
				}
			}
		} while (true);
	}

	private void class_statement(ArrayList list) {
		// class_statement:
		// variable_modifiers class_variable_declaration ';'
		// | class_constant_declaration ';'
		// | method_modifiers T_FUNCTION is_reference T_STRING
		// '(' parameter_list ')' method_body
		initializeModifiers();
		int declarationSourceStart = scanner.getCurrentTokenStartPosition();

		if (token == TokenNamevar) {
			checkAndSetModifiers(AccPublic);
			problemReporter.phpVarDeprecatedWarning(scanner
					.getCurrentTokenStartPosition(), scanner
					.getCurrentTokenEndPosition(), referenceContext,
					compilationUnit.compilationResult);
			getNextToken();
			class_variable_declaration(declarationSourceStart, list);
		} else if (token == TokenNameconst) {
			checkAndSetModifiers(AccFinal | AccPublic);
			class_constant_declaration(declarationSourceStart, list);
			if (token != TokenNameSEMICOLON) {
				throwSyntaxError("';' expected after class const declaration.");
			}
			getNextToken();
		} else {
			boolean hasModifiers = member_modifiers();
			if (token == TokenNamefunction) {
				if (!hasModifiers) {
					checkAndSetModifiers(AccPublic);
				}
				MethodDeclaration methodDecl = new MethodDeclaration(
						this.compilationUnit.compilationResult);
				methodDecl.declarationSourceStart = scanner
						.getCurrentTokenStartPosition();
				methodDecl.modifiers = this.modifiers;
				methodDecl.type = MethodDeclaration.METHOD_DEFINITION;
				try {
					getNextToken();
					functionDefinition(methodDecl);
				} finally {
					int sourceEnd = methodDecl.sourceEnd;
					if (sourceEnd <= 0
							|| methodDecl.declarationSourceStart > sourceEnd) {
						sourceEnd = methodDecl.declarationSourceStart + 1;
					}
					methodDecl.declarationSourceEnd = sourceEnd;
					methodDecl.sourceEnd = sourceEnd;
				}
			} else {
				if (!hasModifiers) {
					throwSyntaxError("'public' 'private' or 'protected' modifier expected for field declarations.");
				}
				class_variable_declaration(declarationSourceStart, list);
			}
		}
	}

	private void class_constant_declaration(int declarationSourceStart,
			ArrayList list) {
		// class_constant_declaration ',' T_STRING '=' static_scalar
		// | T_CONST T_STRING '=' static_scalar
		if (token != TokenNameconst) {
			throwSyntaxError("'const' keyword expected in class declaration.");
		} else {
			getNextToken();
		}
		while (true) {
			if (token != TokenNameIdentifier) {
				throwSyntaxError("Identifier expected in class const declaration.");
			}
			FieldDeclaration fieldDeclaration = new FieldDeclaration(scanner
					.getCurrentIdentifierSource(), scanner
					.getCurrentTokenStartPosition(), scanner
					.getCurrentTokenEndPosition());
			fieldDeclaration.modifiers = this.modifiers;
			fieldDeclaration.declarationSourceStart = declarationSourceStart;
			fieldDeclaration.declarationSourceEnd = scanner
					.getCurrentTokenEndPosition();
			fieldDeclaration.modifiersSourceStart = declarationSourceStart;
			// fieldDeclaration.type
			list.add(fieldDeclaration);
			getNextToken();
			if (token != TokenNameEQUAL) {
				throwSyntaxError("'=' expected in class const declaration.");
			}
			getNextToken();
			static_scalar();
			if (token != TokenNameCOMMA) {
				break; // while(true)-loop
			}
			getNextToken();
		}
	}

	// private void variable_modifiers() {
	// // variable_modifiers:
	// // non_empty_member_modifiers
	// //| T_VAR
	// initializeModifiers();
	// if (token == TokenNamevar) {
	// checkAndSetModifiers(AccPublic);
	// reportSyntaxError(
	// "Keyword 'var' is deprecated. Please use 'public' 'private' or
	// 'protected'
	// modifier for field declarations.",
	// scanner.getCurrentTokenStartPosition(), scanner
	// .getCurrentTokenEndPosition());
	// getNextToken();
	// } else {
	// if (!member_modifiers()) {
	// throwSyntaxError("'public' 'private' or 'protected' modifier expected for
	// field declarations.");
	// }
	// }
	// }
	// private void method_modifiers() {
	// //method_modifiers:
	// // /* empty */
	// //| non_empty_member_modifiers
	// initializeModifiers();
	// if (!member_modifiers()) {
	// checkAndSetModifiers(AccPublic);
	// }
	// }
	private boolean member_modifiers() {
		// T_PUBLIC
		// | T_PROTECTED
		// | T_PRIVATE
		// | T_STATIC
		// | T_ABSTRACT
		// | T_FINAL
		boolean foundToken = false;
		while (true) {
			if (token == TokenNamepublic) {
				checkAndSetModifiers(AccPublic);
				getNextToken();
				foundToken = true;
			} else if (token == TokenNameprotected) {
				checkAndSetModifiers(AccProtected);
				getNextToken();
				foundToken = true;
			} else if (token == TokenNameprivate) {
				checkAndSetModifiers(AccPrivate);
				getNextToken();
				foundToken = true;
			} else if (token == TokenNamestatic) {
				checkAndSetModifiers(AccStatic);
				getNextToken();
				foundToken = true;
			} else if (token == TokenNameabstract) {
				checkAndSetModifiers(AccAbstract);
				getNextToken();
				foundToken = true;
			} else if (token == TokenNamefinal) {
				checkAndSetModifiers(AccFinal);
				getNextToken();
				foundToken = true;
			} else {
				break;
			}
		}
		return foundToken;
	}

	private void class_variable_declaration(int declarationSourceStart,
			ArrayList list) {
		// class_variable_declaration:
		// class_variable_declaration ',' T_VARIABLE
		// | class_variable_declaration ',' T_VARIABLE '=' static_scalar
		// | T_VARIABLE
		// | T_VARIABLE '=' static_scalar
		char[] classVariable;
		do {
			if (token == TokenNameVariable) {
				classVariable = scanner.getCurrentIdentifierSource();
				// indexManager.addIdentifierInformation('v', classVariable,
				// buf, -1,
				// -1);
				FieldDeclaration fieldDeclaration = new FieldDeclaration(
						classVariable, scanner.getCurrentTokenStartPosition(),
						scanner.getCurrentTokenEndPosition());
				fieldDeclaration.modifiers = this.modifiers;
				fieldDeclaration.declarationSourceStart = declarationSourceStart;
				fieldDeclaration.declarationSourceEnd = scanner
						.getCurrentTokenEndPosition();
				fieldDeclaration.modifiersSourceStart = declarationSourceStart;
				list.add(fieldDeclaration);
				if (fTypeVariables != null) {
					VariableInfo info = new VariableInfo(scanner
							.getCurrentTokenStartPosition(),
							VariableInfo.LEVEL_CLASS_UNIT);
					fTypeVariables.put(new String(scanner
							.getCurrentIdentifierSource()), info);
				}
				getNextToken();
				if (token == TokenNameEQUAL) {
					getNextToken();
					static_scalar();
				}
			} else {
				// if (token == TokenNamethis) {
				// throwSyntaxError("'$this' not allowed after keyword 'public'
				// 'protected' 'private' 'var'.");
				// }
				throwSyntaxError("Variable expected after keyword 'public' 'protected' 'private' 'var'.");
			}
			if (token != TokenNameCOMMA) {
				break;
			}
			getNextToken();
		} while (true);
		if (token != TokenNameSEMICOLON) {
			throwSyntaxError("';' expected after field declaration.");
		}
		getNextToken();
	}

	private void functionDefinition(MethodDeclaration methodDecl) {
		boolean isAbstract = false;
		if (astPtr == 0) {
			if (compilationUnit != null) {
				compilationUnit.types.add(methodDecl);
			}
		} else {
			ASTNode node = astStack[astPtr];
			if (node instanceof TypeDeclaration) {
				TypeDeclaration typeDecl = ((TypeDeclaration) node);
				if (typeDecl.methods == null) {
					typeDecl.methods = new AbstractMethodDeclaration[] { methodDecl };
				} else {
					AbstractMethodDeclaration[] newMethods;
					System
							.arraycopy(
									typeDecl.methods,
									0,
									newMethods = new AbstractMethodDeclaration[typeDecl.methods.length + 1],
									0, typeDecl.methods.length);
					newMethods[typeDecl.methods.length] = methodDecl;
					typeDecl.methods = newMethods;
				}
				if ((typeDecl.modifiers & AccAbstract) == AccAbstract) {
					isAbstract = true;
				} else if ((typeDecl.modifiers & AccInterface) == AccInterface) {
					isAbstract = true;
				}
			}
		}
		try {
			pushFunctionVariableSet();
			functionDeclarator(methodDecl);
			if (token == TokenNameSEMICOLON) {
				if (!isAbstract) {
					methodDecl.sourceEnd = scanner
							.getCurrentTokenStartPosition() - 1;
					throwSyntaxError("Body declaration expected for method: "
							+ new String(methodDecl.selector));
				}
				getNextToken();
				return;
			}
			functionBody(methodDecl);
		} finally {
			if (!fStackUnassigned.isEmpty()) {
				fStackUnassigned.remove(fStackUnassigned.size() - 1);
			}
		}
	}

	private void functionDeclarator(MethodDeclaration methodDecl) {
		// identifier '(' [parameter-list] ')'
		if (token == TokenNameAND) {
			getNextToken();
		}
		methodDecl.sourceStart = scanner.getCurrentTokenStartPosition();
		methodDecl.sourceEnd = scanner.getCurrentTokenEndPosition();
		if (Scanner.isIdentifierOrKeyword(token)) {
			methodDecl.selector = scanner.getCurrentIdentifierSource();
			if (token > TokenNameKEYWORD) {
				problemReporter.phpKeywordWarning(new String[] { scanner
						.toStringAction(token) }, scanner
						.getCurrentTokenStartPosition(), scanner
						.getCurrentTokenEndPosition(), referenceContext,
						compilationUnit.compilationResult);
			}
			getNextToken();
			if (token == TokenNameLPAREN) {
				getNextToken();
			} else {
				methodDecl.sourceEnd = scanner.getCurrentTokenStartPosition() - 1;
				throwSyntaxError("'(' expected in function declaration.");
			}
			if (token != TokenNameRPAREN) {
				parameter_list(methodDecl);
			}
			if (token != TokenNameRPAREN) {
				methodDecl.sourceEnd = scanner.getCurrentTokenStartPosition() - 1;
				throwSyntaxError("')' expected in function declaration.");
			} else {
				methodDecl.bodyStart = scanner.getCurrentTokenEndPosition() + 1;
				getNextToken();
			}
		} else {
			methodDecl.selector = "<undefined>".toCharArray();
			methodDecl.sourceEnd = scanner.getCurrentTokenStartPosition() - 1;
			throwSyntaxError("Function name expected after keyword 'function'.");
		}
	}

	//
	private void parameter_list(MethodDeclaration methodDecl) {
		// non_empty_parameter_list
		// | /* empty */
		non_empty_parameter_list(methodDecl, true);
	}

	private void non_empty_parameter_list(MethodDeclaration methodDecl,
			boolean empty_allowed) {
		// optional_class_type T_VARIABLE
		// | optional_class_type '&' T_VARIABLE
		// | optional_class_type '&' T_VARIABLE '=' static_scalar
		// | optional_class_type T_VARIABLE '=' static_scalar
		// | non_empty_parameter_list ',' optional_class_type T_VARIABLE
		// | non_empty_parameter_list ',' optional_class_type '&' T_VARIABLE
		// | non_empty_parameter_list ',' optional_class_type '&' T_VARIABLE '='
		// static_scalar
		// | non_empty_parameter_list ',' optional_class_type T_VARIABLE '='
		// static_scalar
		char[] typeIdentifier = null;
		if (token == TokenNameIdentifier || token == TokenNamearray
				|| token == TokenNameVariable || token == TokenNameAND) {
			HashSet set = peekVariableSet();
			while (true) {
				if (token == TokenNameIdentifier || token == TokenNamearray) {// feature
																				// req.
																				// #1254275
					typeIdentifier = scanner.getCurrentIdentifierSource();
					getNextToken();
				}
				if (token == TokenNameAND) {
					getNextToken();
				}
				if (token == TokenNameVariable) {
					if (fMethodVariables != null) {
						VariableInfo info;
						if (methodDecl.type == MethodDeclaration.FUNCTION_DEFINITION) {
							info = new VariableInfo(scanner
									.getCurrentTokenStartPosition(),
									VariableInfo.LEVEL_FUNCTION_DEFINITION);
						} else {
							info = new VariableInfo(scanner
									.getCurrentTokenStartPosition(),
									VariableInfo.LEVEL_METHOD_DEFINITION);
						}
						info.typeIdentifier = typeIdentifier;
						fMethodVariables.put(new String(scanner
								.getCurrentIdentifierSource()), info);
					}
					addVariableSet(set);
					getNextToken();
					if (token == TokenNameEQUAL) {
						getNextToken();
						static_scalar();
					}
				} else {
					throwSyntaxError("Variable expected in parameter list.");
				}
				if (token != TokenNameCOMMA) {
					break;
				}
				getNextToken();
			}
			return;
		}
		if (!empty_allowed) {
			throwSyntaxError("Identifier expected in parameter list.");
		}
	}

	private void optional_class_type() {
		// /* empty */
		// | T_STRING
	}

	// private void parameterDeclaration() {
	// //variable
	// //variable-reference
	// if (token == TokenNameAND) {
	// getNextToken();
	// if (isVariable()) {
	// getNextToken();
	// } else {
	// throwSyntaxError("Variable expected after reference operator '&'.");
	// }
	// }
	// //variable '=' constant
	// if (token == TokenNameVariable) {
	// getNextToken();
	// if (token == TokenNameEQUAL) {
	// getNextToken();
	// static_scalar();
	// }
	// return;
	// }
	// // if (token == TokenNamethis) {
	// // throwSyntaxError("Reserved word '$this' not allowed in parameter
	// // declaration.");
	// // }
	// }

	private void labeledStatementList() {
		if (token != TokenNamecase && token != TokenNamedefault) {
			throwSyntaxError("'case' or 'default' expected.");
		}
		do {
			if (token == TokenNamecase) {
				getNextToken();
				expr(); // constant();
				if (token == TokenNameCOLON || token == TokenNameSEMICOLON) {
					getNextToken();
					if (token == TokenNameRBRACE) {
						// empty case; assumes that the '}' token belongs to the
						// wrapping
						// switch statement - #1371992
						break;
					}
					if (token == TokenNamecase || token == TokenNamedefault) {
						// empty case statement ?
						continue;
					}
					statementList();
				}
				// else if (token == TokenNameSEMICOLON) {
				// setMarker(
				// "':' expected after 'case' keyword (Found token: " +
				// scanner.toStringAction(token) + ")",
				// scanner.getCurrentTokenStartPosition(),
				// scanner.getCurrentTokenEndPosition(),
				// INFO);
				// getNextToken();
				// if (token == TokenNamecase) { // empty case statement ?
				// continue;
				// }
				// statementList();
				// }
				else {
					throwSyntaxError("':' character expected after 'case' constant (Found token: "
							+ scanner.toStringAction(token) + ")");
				}
			} else { // TokenNamedefault
				getNextToken();
				if (token == TokenNameCOLON || token == TokenNameSEMICOLON) {
					getNextToken();
					if (token == TokenNameRBRACE) {
						// empty default case; ; assumes that the '}' token
						// belongs to the
						// wrapping switch statement - #1371992
						break;
					}
					if (token != TokenNamecase) {
						statementList();
					}
				} else {
					throwSyntaxError("':' character expected after 'default'.");
				}
			}
		} while (token == TokenNamecase || token == TokenNamedefault);
	}

	private void ifStatementColon(IfStatement iState) {
		// T_IF '(' expr ')' ':' inner_statement_list new_elseif_list
		// new_else_single T_ENDIF ';'
		HashSet assignedVariableSet = null;
		try {
			Block b = inner_statement_list();
			iState.thenStatement = b;
			checkUnreachable(iState, b);
		} finally {
			assignedVariableSet = removeIfVariableSet();
		}
		if (token == TokenNameelseif) {
			try {
				pushIfVariableSet();
				new_elseif_list(iState);
			} finally {
				HashSet set = removeIfVariableSet();
				if (assignedVariableSet != null && set != null) {
					assignedVariableSet.addAll(set);
				}
			}
		}
		try {
			pushIfVariableSet();
			new_else_single(iState);
		} finally {
			HashSet set = removeIfVariableSet();
			if (assignedVariableSet != null) {
				HashSet topSet = peekVariableSet();
				if (topSet != null) {
					if (set != null) {
						topSet.addAll(set);
					}
					topSet.addAll(assignedVariableSet);
				}
			}
		}
		if (token != TokenNameendif) {
			throwSyntaxError("'endif' expected.");
		}
		getNextToken();
		if (token != TokenNameSEMICOLON && token != TokenNameINLINE_HTML) {
			reportSyntaxError("';' expected after if-statement.");
			iState.sourceEnd = scanner.getCurrentTokenStartPosition();
		} else {
			iState.sourceEnd = scanner.getCurrentTokenEndPosition();
			getNextToken();
		}
	}

	private void ifStatement(IfStatement iState) {
		// T_IF '(' expr ')' statement elseif_list else_single
		HashSet assignedVariableSet = null;
		try {
			pushIfVariableSet();
			Statement s = statement();
			iState.thenStatement = s;
			checkUnreachable(iState, s);
		} finally {
			assignedVariableSet = removeIfVariableSet();
		}

		if (token == TokenNameelseif) {
			try {
				pushIfVariableSet();
				elseif_list(iState);
			} finally {
				HashSet set = removeIfVariableSet();
				if (assignedVariableSet != null && set != null) {
					assignedVariableSet.addAll(set);
				}
			}
		}
		try {
			pushIfVariableSet();
			else_single(iState);
		} finally {
			HashSet set = removeIfVariableSet();
			if (assignedVariableSet != null) {
				HashSet topSet = peekVariableSet();
				if (topSet != null) {
					if (set != null) {
						topSet.addAll(set);
					}
					topSet.addAll(assignedVariableSet);
				}
			}
		}
	}

	private void elseif_list(IfStatement iState) {
		// /* empty */
		// | elseif_list T_ELSEIF '(' expr ')' statement
		ArrayList conditionList = new ArrayList();
		ArrayList statementList = new ArrayList();
		Expression e;
		Statement s;
		while (token == TokenNameelseif) {
			getNextToken();
			if (token == TokenNameLPAREN) {
				getNextToken();
			} else {
				throwSyntaxError("'(' expected after 'elseif' keyword.");
			}
			e = expr();
			conditionList.add(e);
			if (token == TokenNameRPAREN) {
				getNextToken();
			} else {
				throwSyntaxError("')' expected after 'elseif' condition.");
			}
			s = statement();
			statementList.add(s);
			checkUnreachable(iState, s);
		}
		iState.elseifConditions = new Expression[conditionList.size()];
		iState.elseifStatements = new Statement[statementList.size()];
		conditionList.toArray(iState.elseifConditions);
		statementList.toArray(iState.elseifStatements);
	}

	private void new_elseif_list(IfStatement iState) {
		// /* empty */
		// | new_elseif_list T_ELSEIF '(' expr ')' ':' inner_statement_list
		ArrayList conditionList = new ArrayList();
		ArrayList statementList = new ArrayList();
		Expression e;
		Block b;
		while (token == TokenNameelseif) {
			getNextToken();
			if (token == TokenNameLPAREN) {
				getNextToken();
			} else {
				throwSyntaxError("'(' expected after 'elseif' keyword.");
			}
			e = expr();
			conditionList.add(e);
			if (token == TokenNameRPAREN) {
				getNextToken();
			} else {
				throwSyntaxError("')' expected after 'elseif' condition.");
			}
			if (token == TokenNameCOLON) {
				getNextToken();
			} else {
				throwSyntaxError("':' expected after 'elseif' keyword.");
			}
			b = inner_statement_list();
			statementList.add(b);
			checkUnreachable(iState, b);
		}
		iState.elseifConditions = new Expression[conditionList.size()];
		iState.elseifStatements = new Statement[statementList.size()];
		conditionList.toArray(iState.elseifConditions);
		statementList.toArray(iState.elseifStatements);
	}

	private void else_single(IfStatement iState) {
		// /* empty */
		// T_ELSE statement
		if (token == TokenNameelse) {
			getNextToken();
			Statement s = statement();
			iState.elseStatement = s;
			checkUnreachable(iState, s);
		} else {
			iState.checkUnreachable = false;
		}
		iState.sourceEnd = scanner.getCurrentTokenStartPosition();
	}

	private void new_else_single(IfStatement iState) {
		// /* empty */
		// | T_ELSE ':' inner_statement_list
		if (token == TokenNameelse) {
			getNextToken();
			if (token == TokenNameCOLON) {
				getNextToken();
			} else {
				throwSyntaxError("':' expected after 'else' keyword.");
			}
			Block b = inner_statement_list();
			iState.elseStatement = b;
			checkUnreachable(iState, b);
		} else {
			iState.checkUnreachable = false;
		}
	}

	private Block inner_statement_list() {
		// inner_statement_list inner_statement
		// /* empty */
		return statementList();
	}

	/**
	 * @param iState
	 * @param b
	 */
	private void checkUnreachable(IfStatement iState, Statement s) {
		if (s instanceof Block) {
			Block b = (Block) s;
			if (b.statements == null || b.statements.length == 0) {
				iState.checkUnreachable = false;
			} else {
				int off = b.statements.length - 1;
				if (!(b.statements[off] instanceof ReturnStatement)
						&& !(b.statements[off] instanceof ContinueStatement)
						&& !(b.statements[off] instanceof BreakStatement)) {
					if (!(b.statements[off] instanceof IfStatement)
							|| !((IfStatement) b.statements[off]).checkUnreachable) {
						iState.checkUnreachable = false;
					}
				}
			}
		} else {
			if (!(s instanceof ReturnStatement)
					&& !(s instanceof ContinueStatement)
					&& !(s instanceof BreakStatement)) {
				if (!(s instanceof IfStatement)
						|| !((IfStatement) s).checkUnreachable) {
					iState.checkUnreachable = false;
				}
			}
		}
	}

	// private void elseifStatementList() {
	// do {
	// elseifStatement();
	// switch (token) {
	// case TokenNameelse:
	// getNextToken();
	// if (token == TokenNameCOLON) {
	// getNextToken();
	// if (token != TokenNameendif) {
	// statementList();
	// }
	// return;
	// } else {
	// if (token == TokenNameif) { //'else if'
	// getNextToken();
	// } else {
	// throwSyntaxError("':' expected after 'else'.");
	// }
	// }
	// break;
	// case TokenNameelseif:
	// getNextToken();
	// break;
	// default:
	// return;
	// }
	// } while (true);
	// }

	// private void elseifStatement() {
	// if (token == TokenNameLPAREN) {
	// getNextToken();
	// expr();
	// if (token != TokenNameRPAREN) {
	// throwSyntaxError("')' expected in else-if-statement.");
	// }
	// getNextToken();
	// if (token != TokenNameCOLON) {
	// throwSyntaxError("':' expected in else-if-statement.");
	// }
	// getNextToken();
	// if (token != TokenNameendif) {
	// statementList();
	// }
	// }
	// }

	private void switchStatement() {
		if (token == TokenNameCOLON) {
			// ':' [labeled-statement-list] 'endswitch' ';'
			getNextToken();
			labeledStatementList();
			if (token != TokenNameendswitch) {
				throwSyntaxError("'endswitch' expected.");
			}
			getNextToken();
			if (token != TokenNameSEMICOLON && token != TokenNameINLINE_HTML) {
				throwSyntaxError("';' expected after switch-statement.");
			}
			getNextToken();
		} else {
			// '{' [labeled-statement-list] '}'
			if (token != TokenNameLBRACE) {
				throwSyntaxError("'{' expected in switch statement.");
			}
			getNextToken();
			if (token != TokenNameRBRACE) {
				labeledStatementList();
			}
			if (token != TokenNameRBRACE) {
				throwSyntaxError("'}' expected in switch statement.");
			}
			getNextToken();
		}
	}

	private void forStatement() {
		if (token == TokenNameCOLON) {
			getNextToken();
			statementList();
			if (token != TokenNameendfor) {
				throwSyntaxError("'endfor' expected.");
			}
			getNextToken();
			if (token != TokenNameSEMICOLON && token != TokenNameINLINE_HTML) {
				throwSyntaxError("';' expected after for-statement.");
			}
			getNextToken();
		} else {
			statement();
		}
	}

	private void whileStatement() {
		// ':' statement-list 'endwhile' ';'
		if (token == TokenNameCOLON) {
			getNextToken();
			statementList();
			if (token != TokenNameendwhile) {
				throwSyntaxError("'endwhile' expected.");
			}
			getNextToken();
			if (token != TokenNameSEMICOLON && token != TokenNameINLINE_HTML) {
				throwSyntaxError("';' expected after while-statement.");
			}
			getNextToken();
		} else {
			statement();
		}
	}

	private void foreachStatement() {
		if (token == TokenNameCOLON) {
			getNextToken();
			statementList();
			if (token != TokenNameendforeach) {
				throwSyntaxError("'endforeach' expected.");
			}
			getNextToken();
			if (token != TokenNameSEMICOLON && token != TokenNameINLINE_HTML) {
				throwSyntaxError("';' expected after foreach-statement.");
			}
			getNextToken();
		} else {
			statement();
		}
	}

	// private void exitStatus() {
	// if (token == TokenNameLPAREN) {
	// getNextToken();
	// } else {
	// throwSyntaxError("'(' expected in 'exit-status'.");
	// }
	// if (token != TokenNameRPAREN) {
	// expression();
	// }
	// if (token == TokenNameRPAREN) {
	// getNextToken();
	// } else {
	// throwSyntaxError("')' expected after 'exit-status'.");
	// }
	// }
	private void expressionList() {
		do {
			expr();
			if (token == TokenNameCOMMA) {
				getNextToken();
			} else {
				break;
			}
		} while (true);
	}

	private Expression expr() {
		// r_variable
		// | expr_without_variable
		// if (token!=TokenNameEOF) {
		if (Scanner.TRACE) {
			System.out.println("TRACE: expr()");
		}
		return expr_without_variable(true, null);
		// }
	}

	private Expression expr_without_variable(boolean only_variable,
			UninitializedVariableHandler initHandler) {
		int exprSourceStart = scanner.getCurrentTokenStartPosition();
		int exprSourceEnd = scanner.getCurrentTokenEndPosition();
		Expression expression = new Expression();
		expression.sourceStart = exprSourceStart;
		// default, may be overwritten
		expression.sourceEnd = exprSourceEnd;
		try {
			// internal_functions_in_yacc
			// | T_CLONE expr
			// | T_PRINT expr
			// | '(' expr ')'
			// | '@' expr
			// | '+' expr
			// | '-' expr
			// | '!' expr
			// | '~' expr
			// | T_INC rw_variable
			// | T_DEC rw_variable
			// | T_INT_CAST expr
			// | T_DOUBLE_CAST expr
			// | T_STRING_CAST expr
			// | T_ARRAY_CAST expr
			// | T_OBJECT_CAST expr
			// | T_BOOL_CAST expr
			// | T_UNSET_CAST expr
			// | T_EXIT exit_expr
			// | scalar
			// | T_ARRAY '(' array_pair_list ')'
			// | '`' encaps_list '`'
			// | T_LIST '(' assignment_list ')' '=' expr
			// | T_NEW class_name_reference ctor_arguments
			// | variable '=' expr
			// | variable '=' '&' variable
			// | variable '=' '&' T_NEW class_name_reference ctor_arguments
			// | variable T_PLUS_EQUAL expr
			// | variable T_MINUS_EQUAL expr
			// | variable T_MUL_EQUAL expr
			// | variable T_DIV_EQUAL expr
			// | variable T_CONCAT_EQUAL expr
			// | variable T_MOD_EQUAL expr
			// | variable T_AND_EQUAL expr
			// | variable T_OR_EQUAL expr
			// | variable T_XOR_EQUAL expr
			// | variable T_SL_EQUAL expr
			// | variable T_SR_EQUAL expr
			// | rw_variable T_INC
			// | rw_variable T_DEC
			// | expr T_BOOLEAN_OR expr
			// | expr T_BOOLEAN_AND expr
			// | expr T_LOGICAL_OR expr
			// | expr T_LOGICAL_AND expr
			// | expr T_LOGICAL_XOR expr
			// | expr '|' expr
			// | expr '&' expr
			// | expr '^' expr
			// | expr '.' expr
			// | expr '+' expr
			// | expr '-' expr
			// | expr '*' expr
			// | expr '/' expr
			// | expr '%' expr
			// | expr T_SL expr
			// | expr T_SR expr
			// | expr T_IS_IDENTICAL expr
			// | expr T_IS_NOT_IDENTICAL expr
			// | expr T_IS_EQUAL expr
			// | expr T_IS_NOT_EQUAL expr
			// | expr '<' expr
			// | expr T_IS_SMALLER_OR_EQUAL expr
			// | expr '>' expr
			// | expr T_IS_GREATER_OR_EQUAL expr
			// | expr T_INSTANCEOF class_name_reference
			// | expr '?' expr ':' expr
			if (Scanner.TRACE) {
				System.out.println("TRACE: expr_without_variable() PART 1");
			}
			switch (token) {
			case TokenNameisset:
				// T_ISSET '(' isset_variables ')'
				getNextToken();
				if (token != TokenNameLPAREN) {
					throwSyntaxError("'(' expected after keyword 'isset'");
				}
				getNextToken();
				isset_variables();
				if (token != TokenNameRPAREN) {
					throwSyntaxError("')' expected after keyword 'isset'");
				}
				getNextToken();
				break;
			case TokenNameempty:
				getNextToken();
				if (token != TokenNameLPAREN) {
					throwSyntaxError("'(' expected after keyword 'empty'");
				}
				getNextToken();
				variable(true, false);
				if (token != TokenNameRPAREN) {
					throwSyntaxError("')' expected after keyword 'empty'");
				}
				getNextToken();
				break;
			case TokenNameeval:
			case TokenNameinclude:
			case TokenNameinclude_once:
			case TokenNamerequire:
			case TokenNamerequire_once:
				internal_functions_in_yacc();
				break;
			// | '(' expr ')'
			case TokenNameLPAREN:
				getNextToken();
				expr();
				if (token == TokenNameRPAREN) {
					getNextToken();
				} else {
					throwSyntaxError("')' expected in expression.");
				}
				break;
			// | T_CLONE expr
			// | T_PRINT expr
			// | '@' expr
			// | '+' expr
			// | '-' expr
			// | '!' expr
			// | '~' expr
			// | T_INT_CAST expr
			// | T_DOUBLE_CAST expr
			// | T_STRING_CAST expr
			// | T_ARRAY_CAST expr
			// | T_OBJECT_CAST expr
			// | T_BOOL_CAST expr
			// | T_UNSET_CAST expr
			case TokenNameclone:
			case TokenNameprint:
			case TokenNameAT:
			case TokenNamePLUS:
			case TokenNameMINUS:
			case TokenNameNOT:
			case TokenNameTWIDDLE:
			case TokenNameintCAST:
			case TokenNamedoubleCAST:
			case TokenNamestringCAST:
			case TokenNamearrayCAST:
			case TokenNameobjectCAST:
			case TokenNameboolCAST:
			case TokenNameunsetCAST:
				getNextToken();
				expr();
				break;
			case TokenNameexit:
				getNextToken();
				exit_expr();
				break;
			// scalar:
			// T_STRING
			// | T_STRING_VARNAME
			// | class_constant
			// | T_START_HEREDOC encaps_list T_END_HEREDOC
			// | '`' encaps_list '`'
			// | common_scalar
			// | '`' encaps_list '`'
			// case TokenNameEncapsedString0:
			// scanner.encapsedStringStack.push(new Character('`'));
			// getNextToken();
			// try {
			// if (token == TokenNameEncapsedString0) {
			// } else {
			// encaps_list();
			// if (token != TokenNameEncapsedString0) {
			// throwSyntaxError("\'`\' expected at end of string" + "(Found
			// token: " +
			// scanner.toStringAction(token) + " )");
			// }
			// }
			// } finally {
			// scanner.encapsedStringStack.pop();
			// getNextToken();
			// }
			// break;
			// // | '\'' encaps_list '\''
			// case TokenNameEncapsedString1:
			// scanner.encapsedStringStack.push(new Character('\''));
			// getNextToken();
			// try {
			// exprSourceStart = scanner.getCurrentTokenStartPosition();
			// if (token == TokenNameEncapsedString1) {
			// expression = new
			// StringLiteralSQ(scanner.getCurrentStringLiteralSource(exprSourceStart),
			// exprSourceStart, scanner
			// .getCurrentTokenEndPosition());
			// } else {
			// encaps_list();
			// if (token != TokenNameEncapsedString1) {
			// throwSyntaxError("\'\'\' expected at end of string" + "(Found
			// token: "
			// + scanner.toStringAction(token) + " )");
			// } else {
			// expression = new
			// StringLiteralSQ(scanner.getCurrentStringLiteralSource(exprSourceStart),
			// exprSourceStart, scanner
			// .getCurrentTokenEndPosition());
			// }
			// }
			// } finally {
			// scanner.encapsedStringStack.pop();
			// getNextToken();
			// }
			// break;
			// //| '"' encaps_list '"'
			// case TokenNameEncapsedString2:
			// scanner.encapsedStringStack.push(new Character('"'));
			// getNextToken();
			// try {
			// exprSourceStart = scanner.getCurrentTokenStartPosition();
			// if (token == TokenNameEncapsedString2) {
			// expression = new
			// StringLiteralDQ(scanner.getCurrentStringLiteralSource(exprSourceStart),
			// exprSourceStart, scanner
			// .getCurrentTokenEndPosition());
			// } else {
			// encaps_list();
			// if (token != TokenNameEncapsedString2) {
			// throwSyntaxError("'\"' expected at end of string" + "(Found
			// token: " +
			// scanner.toStringAction(token) + " )");
			// } else {
			// expression = new
			// StringLiteralDQ(scanner.getCurrentStringLiteralSource(exprSourceStart),
			// exprSourceStart, scanner
			// .getCurrentTokenEndPosition());
			// }
			// }
			// } finally {
			// scanner.encapsedStringStack.pop();
			// getNextToken();
			// }
			// break;
			case TokenNameStringDoubleQuote:
				expression = new StringLiteralDQ(scanner
						.getCurrentStringLiteralSource(), scanner
						.getCurrentTokenStartPosition(), scanner
						.getCurrentTokenEndPosition());
				common_scalar();
				break;
			case TokenNameStringSingleQuote:
				expression = new StringLiteralSQ(scanner
						.getCurrentStringLiteralSource(), scanner
						.getCurrentTokenStartPosition(), scanner
						.getCurrentTokenEndPosition());
				common_scalar();
				break;
			case TokenNameIntegerLiteral:
			case TokenNameDoubleLiteral:
			case TokenNameStringInterpolated:
			case TokenNameFILE:
			case TokenNameLINE:
			case TokenNameCLASS_C:
			case TokenNameMETHOD_C:
			case TokenNameFUNC_C:
				common_scalar();
				break;
			case TokenNameHEREDOC:
				getNextToken();
				break;
			case TokenNamearray:
				// T_ARRAY '(' array_pair_list ')'
				getNextToken();
				if (token == TokenNameLPAREN) {
					getNextToken();
					if (token == TokenNameRPAREN) {
						getNextToken();
						break;
					}
					array_pair_list();
					if (token != TokenNameRPAREN) {
						throwSyntaxError("')' or ',' expected after keyword 'array'"
								+ "(Found token: "
								+ scanner.toStringAction(token) + ")");
					}
					getNextToken();
				} else {
					throwSyntaxError("'(' expected after keyword 'array'"
							+ "(Found token: " + scanner.toStringAction(token)
							+ ")");
				}
				break;
			case TokenNamelist:
				// | T_LIST '(' assignment_list ')' '=' expr
				getNextToken();
				if (token == TokenNameLPAREN) {
					getNextToken();
					assignment_list();
					if (token != TokenNameRPAREN) {
						throwSyntaxError("')' expected after 'list' keyword.");
					}
					getNextToken();
					if (token != TokenNameEQUAL) {
						throwSyntaxError("'=' expected after 'list' keyword.");
					}
					getNextToken();
					expr();
				} else {
					throwSyntaxError("'(' expected after 'list' keyword.");
				}
				break;
			case TokenNamenew:
				// | T_NEW class_name_reference ctor_arguments
				getNextToken();
				Expression typeRef = class_name_reference();
				ctor_arguments();
				if (typeRef != null) {
					expression = typeRef;
				}
				break;
			// | T_INC rw_variable
			// | T_DEC rw_variable
			case TokenNamePLUS_PLUS:
			case TokenNameMINUS_MINUS:
				getNextToken();
				rw_variable();
				break;
			// | variable '=' expr
			// | variable '=' '&' variable
			// | variable '=' '&' T_NEW class_name_reference ctor_arguments
			// | variable T_PLUS_EQUAL expr
			// | variable T_MINUS_EQUAL expr
			// | variable T_MUL_EQUAL expr
			// | variable T_DIV_EQUAL expr
			// | variable T_CONCAT_EQUAL expr
			// | variable T_MOD_EQUAL expr
			// | variable T_AND_EQUAL expr
			// | variable T_OR_EQUAL expr
			// | variable T_XOR_EQUAL expr
			// | variable T_SL_EQUAL expr
			// | variable T_SR_EQUAL expr
			// | rw_variable T_INC
			// | rw_variable T_DEC
			case TokenNameIdentifier:
			case TokenNameVariable:
			case TokenNameDOLLAR:
				Expression lhs = null;
				boolean rememberedVar = false;
				if (token == TokenNameIdentifier) {
					lhs = identifier(true, true);
					if (lhs != null) {
						expression = lhs;
					}
				} else {
					lhs = variable(true, true);
					if (lhs != null) {
						expression = lhs;
					}
					if (lhs != null && lhs instanceof FieldReference
							&& token != TokenNameEQUAL
							&& token != TokenNamePLUS_EQUAL
							&& token != TokenNameMINUS_EQUAL
							&& token != TokenNameMULTIPLY_EQUAL
							&& token != TokenNameDIVIDE_EQUAL
							&& token != TokenNameDOT_EQUAL
							&& token != TokenNameREMAINDER_EQUAL
							&& token != TokenNameAND_EQUAL
							&& token != TokenNameOR_EQUAL
							&& token != TokenNameXOR_EQUAL
							&& token != TokenNameRIGHT_SHIFT_EQUAL
							&& token != TokenNameLEFT_SHIFT_EQUAL) {
						FieldReference ref = (FieldReference) lhs;
						if (!containsVariableSet(ref.token)) {
							if (null == initHandler
									|| initHandler.reportError()) {
								problemReporter.uninitializedLocalVariable(
										new String(ref.token), ref.sourceStart,
										ref.sourceEnd, referenceContext,
										compilationUnit.compilationResult);
							}
							addVariableSet(ref.token);
						}
					}
				}
				switch (token) {
				case TokenNameEQUAL:
					if (lhs != null && lhs instanceof FieldReference) {
						addVariableSet(((FieldReference) lhs).token);
					}
					getNextToken();
					if (token == TokenNameAND) {
						getNextToken();
						if (token == TokenNamenew) {
							// | variable '=' '&' T_NEW class_name_reference
							// ctor_arguments
							getNextToken();
							SingleTypeReference classRef = class_name_reference();
							ctor_arguments();
							if (classRef != null) {
								if (lhs != null
										&& lhs instanceof FieldReference) {
									// example:
									// $var = & new Object();
									if (fMethodVariables != null) {
										VariableInfo lhsInfo = new VariableInfo(
												((FieldReference) lhs).sourceStart);
										lhsInfo.reference = classRef;
										lhsInfo.typeIdentifier = classRef.token;
										fMethodVariables.put(new String(
												((FieldReference) lhs).token),
												lhsInfo);
										rememberedVar = true;
									}
								}
							}
						} else {
							Expression rhs = variable(false, false);
							if (rhs != null && rhs instanceof FieldReference
									&& lhs != null
									&& lhs instanceof FieldReference) {
								// example:
								// $var = &$ref;
								if (fMethodVariables != null) {
									VariableInfo rhsInfo = (VariableInfo) fMethodVariables
											.get(((FieldReference) rhs).token);
									if (rhsInfo != null
											&& rhsInfo.reference != null) {
										VariableInfo lhsInfo = new VariableInfo(
												((FieldReference) lhs).sourceStart);
										lhsInfo.reference = rhsInfo.reference;
										lhsInfo.typeIdentifier = rhsInfo.typeIdentifier;
										fMethodVariables.put(new String(
												((FieldReference) lhs).token),
												lhsInfo);
										rememberedVar = true;
									}
								}
							}
						}
					} else {
						Expression rhs = expr();
						if (lhs != null && lhs instanceof FieldReference) {
							if (rhs != null && rhs instanceof FieldReference) {
								// example:
								// $var = $ref;
								if (fMethodVariables != null) {
									VariableInfo rhsInfo = (VariableInfo) fMethodVariables
											.get(((FieldReference) rhs).token);
									if (rhsInfo != null
											&& rhsInfo.reference != null) {
										VariableInfo lhsInfo = new VariableInfo(
												((FieldReference) lhs).sourceStart);
										lhsInfo.reference = rhsInfo.reference;
										lhsInfo.typeIdentifier = rhsInfo.typeIdentifier;
										fMethodVariables.put(new String(
												((FieldReference) lhs).token),
												lhsInfo);
										rememberedVar = true;
									}
								}
							} else if (rhs != null
									&& rhs instanceof SingleTypeReference) {
								// example:
								// $var = new Object();
								if (fMethodVariables != null) {
									VariableInfo lhsInfo = new VariableInfo(
											((FieldReference) lhs).sourceStart);
									lhsInfo.reference = (SingleTypeReference) rhs;
									lhsInfo.typeIdentifier = ((SingleTypeReference) rhs).token;
									fMethodVariables.put(new String(
											((FieldReference) lhs).token),
											lhsInfo);
									rememberedVar = true;
								}
							}
						}
					}
					if (rememberedVar == false && lhs != null
							&& lhs instanceof FieldReference) {
						if (fMethodVariables != null) {
							VariableInfo lhsInfo = new VariableInfo(
									((FieldReference) lhs).sourceStart);
							fMethodVariables.put(new String(
									((FieldReference) lhs).token), lhsInfo);
						}
					}
					break;
				case TokenNamePLUS_EQUAL:
				case TokenNameMINUS_EQUAL:
				case TokenNameMULTIPLY_EQUAL:
				case TokenNameDIVIDE_EQUAL:
				case TokenNameDOT_EQUAL:
				case TokenNameREMAINDER_EQUAL:
				case TokenNameAND_EQUAL:
				case TokenNameOR_EQUAL:
				case TokenNameXOR_EQUAL:
				case TokenNameRIGHT_SHIFT_EQUAL:
				case TokenNameLEFT_SHIFT_EQUAL:
					if (lhs != null && lhs instanceof FieldReference) {
						addVariableSet(((FieldReference) lhs).token);
					}
					getNextToken();
					expr();
					break;
				case TokenNamePLUS_PLUS:
				case TokenNameMINUS_MINUS:
					getNextToken();
					break;
				default:
					if (!only_variable) {
						throwSyntaxError("Variable expression not allowed (found token '"
								+ scanner.toStringAction(token) + "').");
					}
					if (lhs != null) {
						expression = lhs;
					}
				}
				break;
			default:
				if (token != TokenNameINLINE_HTML) {
					if (token > TokenNameKEYWORD) {
						getNextToken();
						break;
					} else {
						// System.out.println(scanner.getCurrentTokenStartPosition());
						// System.out.println(scanner.getCurrentTokenEndPosition());

						throwSyntaxError("Error in expression (found token '"
								+ scanner.toStringAction(token) + "').");
					}
				}
				return expression;
			}
			if (Scanner.TRACE) {
				System.out.println("TRACE: expr_without_variable() PART 2");
			}
			// | expr T_BOOLEAN_OR expr
			// | expr T_BOOLEAN_AND expr
			// | expr T_LOGICAL_OR expr
			// | expr T_LOGICAL_AND expr
			// | expr T_LOGICAL_XOR expr
			// | expr '|' expr
			// | expr '&' expr
			// | expr '^' expr
			// | expr '.' expr
			// | expr '+' expr
			// | expr '-' expr
			// | expr '*' expr
			// | expr '/' expr
			// | expr '%' expr
			// | expr T_SL expr
			// | expr T_SR expr
			// | expr T_IS_IDENTICAL expr
			// | expr T_IS_NOT_IDENTICAL expr
			// | expr T_IS_EQUAL expr
			// | expr T_IS_NOT_EQUAL expr
			// | expr '<' expr
			// | expr T_IS_SMALLER_OR_EQUAL expr
			// | expr '>' expr
			// | expr T_IS_GREATER_OR_EQUAL expr
			while (true) {
				switch (token) {
				case TokenNameOR_OR:
					getNextToken();
					expression = new OR_OR_Expression(expression, expr(), token);
					break;
				case TokenNameAND_AND:
					getNextToken();
					expression = new AND_AND_Expression(expression, expr(),
							token);
					break;
				case TokenNameEQUAL_EQUAL:
					getNextToken();
					expression = new EqualExpression(expression, expr(), token);
					break;
				case TokenNameand:
				case TokenNameor:
				case TokenNamexor:
				case TokenNameAND:
				case TokenNameOR:
				case TokenNameXOR:
				case TokenNameDOT:
				case TokenNamePLUS:
				case TokenNameMINUS:
				case TokenNameMULTIPLY:
				case TokenNameDIVIDE:
				case TokenNameREMAINDER:
				case TokenNameLEFT_SHIFT:
				case TokenNameRIGHT_SHIFT:
				case TokenNameEQUAL_EQUAL_EQUAL:
				case TokenNameNOT_EQUAL_EQUAL:
				case TokenNameNOT_EQUAL:
				case TokenNameLESS:
				case TokenNameLESS_EQUAL:
				case TokenNameGREATER:
				case TokenNameGREATER_EQUAL:
					getNextToken();
					expression = new BinaryExpression(expression, expr(), token);
					break;
				// | expr T_INSTANCEOF class_name_reference
				// | expr '?' expr ':' expr
				case TokenNameinstanceof:
					getNextToken();
					TypeReference classRef = class_name_reference();
					if (classRef != null) {
						expression = new InstanceOfExpression(expression,
								classRef, OperatorIds.INSTANCEOF);
						expression.sourceStart = exprSourceStart;
						expression.sourceEnd = scanner
								.getCurrentTokenEndPosition();
					}
					break;
				case TokenNameQUESTION:
					getNextToken();
					Expression valueIfTrue = expr();
					if (token != TokenNameCOLON) {
						throwSyntaxError("':' expected in conditional expression.");
					}
					getNextToken();
					Expression valueIfFalse = expr();

					expression = new ConditionalExpression(expression,
							valueIfTrue, valueIfFalse);
					break;
				default:
					return expression;
				}
			}
		} catch (SyntaxError e) {
			// try to find next token after expression with errors:
			if (token == TokenNameSEMICOLON) {
				getNextToken();
				return expression;
			}
			if (token == TokenNameRBRACE || token == TokenNameRPAREN
					|| token == TokenNameRBRACKET) {
				getNextToken();
				return expression;
			}
			throw e;
		}
	}

	private SingleTypeReference class_name_reference() {
		// class_name_reference:
		// T_STRING
		// | dynamic_class_name_reference
		SingleTypeReference ref = null;
		if (Scanner.TRACE) {
			System.out.println("TRACE: class_name_reference()");
		}
		if (token == TokenNameIdentifier) {
			ref = new SingleTypeReference(scanner.getCurrentIdentifierSource(),
					scanner.getCurrentTokenStartPosition());
			int pos = scanner.currentPosition;
			getNextToken();
			if (token == TokenNamePAAMAYIM_NEKUDOTAYIM) {
				// Not terminated by T_STRING, reduce to dynamic_class_name_reference
				scanner.currentPosition = pos;
				token = TokenNameIdentifier;
				ref = null;
				dynamic_class_name_reference();
			}
		} else {
			ref = null;
			dynamic_class_name_reference();
		}
		return ref;
	}

	private void dynamic_class_name_reference() {
		// dynamic_class_name_reference:
		// base_variable T_OBJECT_OPERATOR object_property
		// dynamic_class_name_variable_properties
		// | base_variable
		if (Scanner.TRACE) {
			System.out.println("TRACE: dynamic_class_name_reference()");
		}
		base_variable(true);
		if (token == TokenNameMINUS_GREATER) {
			getNextToken();
			object_property();
			dynamic_class_name_variable_properties();
		}
	}

	private void dynamic_class_name_variable_properties() {
		// dynamic_class_name_variable_properties:
		// dynamic_class_name_variable_properties
		// dynamic_class_name_variable_property
		// | /* empty */
		if (Scanner.TRACE) {
			System.out
					.println("TRACE: dynamic_class_name_variable_properties()");
		}
		while (token == TokenNameMINUS_GREATER) {
			dynamic_class_name_variable_property();
		}
	}

	private void dynamic_class_name_variable_property() {
		// dynamic_class_name_variable_property:
		// T_OBJECT_OPERATOR object_property
		if (Scanner.TRACE) {
			System.out.println("TRACE: dynamic_class_name_variable_property()");
		}
		if (token == TokenNameMINUS_GREATER) {
			getNextToken();
			object_property();
		}
	}

	private void ctor_arguments() {
		// ctor_arguments:
		// /* empty */
		// | '(' function_call_parameter_list ')'
		if (token == TokenNameLPAREN) {
			getNextToken();
			if (token == TokenNameRPAREN) {
				getNextToken();
				return;
			}
			non_empty_function_call_parameter_list();
			if (token != TokenNameRPAREN) {
				throwSyntaxError("')' expected in ctor_arguments.");
			}
			getNextToken();
		}
	}

	private void assignment_list() {
		// assignment_list:
		// assignment_list ',' assignment_list_element
		// | assignment_list_element
		while (true) {
			assignment_list_element();
			if (token != TokenNameCOMMA) {
				break;
			}
			getNextToken();
		}
	}

	private void assignment_list_element() {
		// assignment_list_element:
		// variable
		// | T_LIST '(' assignment_list ')'
		// | /* empty */
		if (token == TokenNameVariable) {
			variable(true, false);
		} else if (token == TokenNameDOLLAR) {
			variable(false, false);
		} else if (token == TokenNameIdentifier) {
			identifier(true, true);
		} else {
			if (token == TokenNamelist) {
				getNextToken();
				if (token == TokenNameLPAREN) {
					getNextToken();
					assignment_list();
					if (token != TokenNameRPAREN) {
						throwSyntaxError("')' expected after 'list' keyword.");
					}
					getNextToken();
				} else {
					throwSyntaxError("'(' expected after 'list' keyword.");
				}
			}
		}
	}

	private void array_pair_list() {
		// array_pair_list:
		// /* empty */
		// | non_empty_array_pair_list possible_comma
		non_empty_array_pair_list();
		if (token == TokenNameCOMMA) {
			getNextToken();
		}
	}

	private void non_empty_array_pair_list() {
		// non_empty_array_pair_list:
		// non_empty_array_pair_list ',' expr T_DOUBLE_ARROW expr
		// | non_empty_array_pair_list ',' expr
		// | expr T_DOUBLE_ARROW expr
		// | expr
		// | non_empty_array_pair_list ',' expr T_DOUBLE_ARROW '&' w_variable
		// | non_empty_array_pair_list ',' '&' w_variable
		// | expr T_DOUBLE_ARROW '&' w_variable
		// | '&' w_variable
		while (true) {
			if (token == TokenNameAND) {
				getNextToken();
				variable(true, false);
			} else {
				expr();
				if (token == TokenNameAND) {
					getNextToken();
					variable(true, false);
				} else if (token == TokenNameEQUAL_GREATER) {
					getNextToken();
					if (token == TokenNameAND) {
						getNextToken();
						variable(true, false);
					} else {
						expr();
					}
				}
			}
			if (token != TokenNameCOMMA) {
				return;
			}
			getNextToken();
			if (token == TokenNameRPAREN) {
				return;
			}
		}
	}

	// private void variableList() {
	// do {
	// variable();
	// if (token == TokenNameCOMMA) {
	// getNextToken();
	// } else {
	// break;
	// }
	// } while (true);
	// }
	private Expression variable_without_objects(boolean lefthandside,
			boolean ignoreVar) {
		// variable_without_objects:
		// reference_variable
		// | simple_indirect_reference reference_variable
		if (Scanner.TRACE) {
			System.out.println("TRACE: variable_without_objects()");
		}
		while (token == TokenNameDOLLAR) {
			getNextToken();
		}
		return reference_variable(lefthandside, ignoreVar);
	}

	private Expression function_call(boolean lefthandside, boolean ignoreVar) {
		// function_call:
		// T_STRING '(' function_call_parameter_list ')'
		// | class_constant '(' function_call_parameter_list ')'
		// | static_member '(' function_call_parameter_list ')'
		// | variable_without_objects '(' function_call_parameter_list ')'
		char[] defineName = null;
		char[] ident = null;
		int startPos = 0;
		int endPos = 0;
		Expression ref = null;
		if (Scanner.TRACE) {
			System.out.println("TRACE: function_call()");
		}
		if (token == TokenNameIdentifier) {
			ident = scanner.getCurrentIdentifierSource();
			defineName = ident;
			startPos = scanner.getCurrentTokenStartPosition();
			endPos = scanner.getCurrentTokenEndPosition();
			getNextToken();
			switch (token) {
			case TokenNamePAAMAYIM_NEKUDOTAYIM:
				// static member:
				defineName = null;
				getNextToken();
				if (token == TokenNameIdentifier) {
					// class _constant
					getNextToken();
				} else {
					// static member:
					variable_without_objects(true, false);
				}
				break;
			}
		} else {
			ref = variable_without_objects(lefthandside, ignoreVar);
		}
		if (token != TokenNameLPAREN) {
			if (defineName != null) {
				// does this identifier contain only uppercase characters?
				if (defineName.length == 3) {
					if (defineName[0] == 'd' && defineName[1] == 'i'
							&& defineName[2] == 'e') {
						defineName = null;
					}
				} else if (defineName.length == 4) {
					if (defineName[0] == 't' && defineName[1] == 'r'
							&& defineName[2] == 'u' && defineName[3] == 'e') {
						defineName = null;
					} else if (defineName[0] == 'n' && defineName[1] == 'u'
							&& defineName[2] == 'l' && defineName[3] == 'l') {
						defineName = null;
					}
				} else if (defineName.length == 5) {
					if (defineName[0] == 'f' && defineName[1] == 'a'
							&& defineName[2] == 'l' && defineName[3] == 's'
							&& defineName[4] == 'e') {
						defineName = null;
					}
				}
				if (defineName != null) {
					for (int i = 0; i < defineName.length; i++) {
						if (Character.isLowerCase(defineName[i])) {
							problemReporter.phpUppercaseIdentifierWarning(
									startPos, endPos, referenceContext,
									compilationUnit.compilationResult);
							break;
						}
					}
				}
			}
		} else {
			getNextToken();
			if (token == TokenNameRPAREN) {
				getNextToken();
				return ref;
			}
			non_empty_function_call_parameter_list();
			if (token != TokenNameRPAREN) {
				String functionName;
				if (ident == null) {
					functionName = new String(" ");
				} else {
					functionName = new String(ident);
				}
				throwSyntaxError("')' expected in function call ("
						+ functionName + ").");
			}
			getNextToken();
		}
		return ref;
	}

	private void non_empty_function_call_parameter_list() {
		this.non_empty_function_call_parameter_list(null);
	}

	// private void function_call_parameter_list() {
	// function_call_parameter_list:
	// non_empty_function_call_parameter_list { $$ = $1; }
	// | /* empty */
	// }
	private void non_empty_function_call_parameter_list(String functionName) {
		// non_empty_function_call_parameter_list:
		// expr_without_variable
		// | variable
		// | '&' w_variable
		// | non_empty_function_call_parameter_list ',' expr_without_variable
		// | non_empty_function_call_parameter_list ',' variable
		// | non_empty_function_call_parameter_list ',' '&' w_variable
		if (Scanner.TRACE) {
			System.out
					.println("TRACE: non_empty_function_call_parameter_list()");
		}
		UninitializedVariableHandler initHandler = new UninitializedVariableHandler();
		initHandler.setFunctionName(functionName);
		while (true) {
			initHandler.incrementArgumentCount();
			if (token == TokenNameAND) {
				getNextToken();
				w_variable(true);
			} else {
				// if (token == TokenNameIdentifier || token ==
				// TokenNameVariable
				// || token == TokenNameDOLLAR) {
				// variable();
				// } else {
				expr_without_variable(true, initHandler);
				// }
			}
			if (token != TokenNameCOMMA) {
				break;
			}
			getNextToken();
		}
	}

	private void fully_qualified_class_name() {
		if (token == TokenNameIdentifier) {
			getNextToken();
		} else {
			throwSyntaxError("Class name expected.");
		}
	}

	private void static_member() {
		// static_member:
		// fully_qualified_class_name T_PAAMAYIM_NEKUDOTAYIM
		// variable_without_objects
		if (Scanner.TRACE) {
			System.out.println("TRACE: static_member()");
		}
		fully_qualified_class_name();
		if (token != TokenNamePAAMAYIM_NEKUDOTAYIM) {
			throwSyntaxError("'::' expected after class name (static_member).");
		}
		getNextToken();
		variable_without_objects(false, false);
	}

	private Expression base_variable_with_function_calls(boolean lefthandside,
			boolean ignoreVar) {
		// base_variable_with_function_calls:
		// base_variable
		// | function_call
		if (Scanner.TRACE) {
			System.out.println("TRACE: base_variable_with_function_calls()");
		}
		return function_call(lefthandside, ignoreVar);
	}

	private Expression base_variable(boolean lefthandside) {
		// base_variable:
		// reference_variable
		// | simple_indirect_reference reference_variable
		// | static_member
		Expression ref = null;
		if (Scanner.TRACE) {
			System.out.println("TRACE: base_variable()");
		}
		if (token == TokenNameIdentifier) {
			static_member();
		} else {
			while (token == TokenNameDOLLAR) {
				getNextToken();
			}
			reference_variable(lefthandside, false);
		}
		return ref;
	}

	// private void simple_indirect_reference() {
	// // simple_indirect_reference:
	// // '$'
	// //| simple_indirect_reference '$'
	// }
	private Expression reference_variable(boolean lefthandside,
			boolean ignoreVar) {
		// reference_variable:
		// reference_variable '[' dim_offset ']'
		// | reference_variable '{' expr '}'
		// | compound_variable
		Expression ref = null;
		if (Scanner.TRACE) {
			System.out.println("TRACE: reference_variable()");
		}
		ref = compound_variable(lefthandside, ignoreVar);
		while (true) {
			if (token == TokenNameLBRACE) {
				ref = null;
				getNextToken();
				expr();
				if (token != TokenNameRBRACE) {
					throwSyntaxError("'}' expected in reference variable.");
				}
				getNextToken();
			} else if (token == TokenNameLBRACKET) {
				// To remove "ref = null;" here, is probably better than the
				// patch
				// commented in #1368081 - axelcl
				getNextToken();
				if (token != TokenNameRBRACKET) {
					expr();
					// dim_offset();
					if (token != TokenNameRBRACKET) {
						throwSyntaxError("']' expected in reference variable.");
					}
				}
				getNextToken();
			} else {
				break;
			}
		}
		return ref;
	}

	private Expression compound_variable(boolean lefthandside, boolean ignoreVar) {
		// compound_variable:
		// T_VARIABLE
		// | '$' '{' expr '}'
		if (Scanner.TRACE) {
			System.out.println("TRACE: compound_variable()");
		}
		if (token == TokenNameVariable) {
			if (!lefthandside) {
				if (!containsVariableSet()) {
					// reportSyntaxError("The local variable " + new
					// String(scanner.getCurrentIdentifierSource())
					// + " may not have been initialized");
					problemReporter.uninitializedLocalVariable(new String(
							scanner.getCurrentIdentifierSource()), scanner
							.getCurrentTokenStartPosition(), scanner
							.getCurrentTokenEndPosition(), referenceContext,
							compilationUnit.compilationResult);
				}
			} else {
				if (!ignoreVar) {
					addVariableSet();
				}
			}
			FieldReference ref = new FieldReference(scanner
					.getCurrentIdentifierSource(), scanner
					.getCurrentTokenStartPosition());
			getNextToken();
			return ref;
		} else {
			// because of simple_indirect_reference
			while (token == TokenNameDOLLAR) {
				getNextToken();
			}
			if (token != TokenNameLBRACE) {
				reportSyntaxError("'{' expected after compound variable token '$'.");
				return null;
			}
			getNextToken();
			expr();
			if (token != TokenNameRBRACE) {
				throwSyntaxError("'}' expected after compound variable token '$'.");
			}
			getNextToken();
		}
		return null;
	} // private void dim_offset() { // // dim_offset: // // /* empty */

	// // | expr
	// expr();
	// }
	private void object_property() {
		// object_property:
		// object_dim_list
		// | variable_without_objects
		if (Scanner.TRACE) {
			System.out.println("TRACE: object_property()");
		}
		if (token == TokenNameVariable || token == TokenNameDOLLAR) {
			variable_without_objects(false, false);
		} else {
			object_dim_list();
		}
	}

	private void object_dim_list() {
		// object_dim_list:
		// object_dim_list '[' dim_offset ']'
		// | object_dim_list '{' expr '}'
		// | variable_name
		if (Scanner.TRACE) {
			System.out.println("TRACE: object_dim_list()");
		}
		variable_name();
		while (true) {
			if (token == TokenNameLBRACE) {
				getNextToken();
				expr();
				if (token != TokenNameRBRACE) {
					throwSyntaxError("'}' expected in object_dim_list.");
				}
				getNextToken();
			} else if (token == TokenNameLBRACKET) {
				getNextToken();
				if (token == TokenNameRBRACKET) {
					getNextToken();
					continue;
				}
				expr();
				if (token != TokenNameRBRACKET) {
					throwSyntaxError("']' expected in object_dim_list.");
				}
				getNextToken();
			} else {
				break;
			}
		}
	}

	private void variable_name() {
		// variable_name:
		// T_STRING
		// | '{' expr '}'
		if (Scanner.TRACE) {
			System.out.println("TRACE: variable_name()");
		}
		if (token == TokenNameIdentifier || token > TokenNameKEYWORD) {
			if (token > TokenNameKEYWORD) {
				// TODO show a warning "Keyword used as variable" ?
			}
			getNextToken();
		} else {
			if (token != TokenNameLBRACE) {
				throwSyntaxError("'{' expected in variable name.");
			}
			getNextToken();
			expr();
			if (token != TokenNameRBRACE) {
				throwSyntaxError("'}' expected in variable name.");
			}
			getNextToken();
		}
	}

	private void r_variable() {
		variable(false, false);
	}

	private void w_variable(boolean lefthandside) {
		variable(lefthandside, false);
	}

	private void rw_variable() {
		variable(false, false);
	}

	private Expression variable(boolean lefthandside, boolean ignoreVar) {
		// variable:
		// base_variable_with_function_calls T_OBJECT_OPERATOR
		// object_property method_or_not variable_properties
		// | base_variable_with_function_calls
		Expression ref = base_variable_with_function_calls(lefthandside,
				ignoreVar);
		if (token == TokenNameMINUS_GREATER) {
			ref = null;
			getNextToken();
			object_property();
			method_or_not();
			variable_properties();
		}
		return ref;
	}

	private void variable_properties() {
		// variable_properties:
		// variable_properties variable_property
		// | /* empty */
		while (token == TokenNameMINUS_GREATER) {
			variable_property();
		}
	}

	private void variable_property() {
		// variable_property:
		// T_OBJECT_OPERATOR object_property method_or_not
		if (Scanner.TRACE) {
			System.out.println("TRACE: variable_property()");
		}
		if (token == TokenNameMINUS_GREATER) {
			getNextToken();
			object_property();
			method_or_not();
		} else {
			throwSyntaxError("'->' expected in variable_property.");
		}
	}

	private Expression identifier(boolean lefthandside, boolean ignoreVar) {
		// variable:
		// base_variable_with_function_calls T_OBJECT_OPERATOR
		// object_property method_or_not variable_properties
		// | base_variable_with_function_calls

		// Expression ref = function_call(lefthandside, ignoreVar);

		// function_call:
		// T_STRING '(' function_call_parameter_list ')'
		// | class_constant '(' function_call_parameter_list ')'
		// | static_member '(' function_call_parameter_list ')'
		// | variable_without_objects '(' function_call_parameter_list ')'
		char[] defineName = null;
		char[] ident = null;
		int startPos = 0;
		int endPos = 0;
		Expression ref = null;
		if (Scanner.TRACE) {
			System.out.println("TRACE: function_call()");
		}
		if (token == TokenNameIdentifier) {
			ident = scanner.getCurrentIdentifierSource();
			defineName = ident;
			startPos = scanner.getCurrentTokenStartPosition();
			endPos = scanner.getCurrentTokenEndPosition();
			getNextToken();

			if (token == TokenNameEQUAL || token == TokenNamePLUS_EQUAL
					|| token == TokenNameMINUS_EQUAL
					|| token == TokenNameMULTIPLY_EQUAL
					|| token == TokenNameDIVIDE_EQUAL
					|| token == TokenNameDOT_EQUAL
					|| token == TokenNameREMAINDER_EQUAL
					|| token == TokenNameAND_EQUAL
					|| token == TokenNameOR_EQUAL
					|| token == TokenNameXOR_EQUAL
					|| token == TokenNameRIGHT_SHIFT_EQUAL
					|| token == TokenNameLEFT_SHIFT_EQUAL) {
				String error = "Assignment operator '"
						+ scanner.toStringAction(token)
						+ "' not allowed after identifier '"
						+ new String(ident)
						+ "' (use 'define(...)' to define constants).";
				reportSyntaxError(error);
			}

			switch (token) {
			case TokenNamePAAMAYIM_NEKUDOTAYIM:
				// static member:
				defineName = null;
				getNextToken();
				if (token == TokenNameIdentifier) {
					// class _constant
					getNextToken();
				} else {
					// static member:
					variable_without_objects(true, false);
				}
				break;
			}
		} else {
			ref = variable_without_objects(lefthandside, ignoreVar);
		}
		if (token != TokenNameLPAREN) {
			if (defineName != null) {
				// does this identifier contain only uppercase characters?
				if (defineName.length == 3) {
					if (defineName[0] == 'd' && defineName[1] == 'i'
							&& defineName[2] == 'e') {
						defineName = null;
					}
				} else if (defineName.length == 4) {
					if (defineName[0] == 't' && defineName[1] == 'r'
							&& defineName[2] == 'u' && defineName[3] == 'e') {
						defineName = null;
					} else if (defineName[0] == 'n' && defineName[1] == 'u'
							&& defineName[2] == 'l' && defineName[3] == 'l') {
						defineName = null;
					}
				} else if (defineName.length == 5) {
					if (defineName[0] == 'f' && defineName[1] == 'a'
							&& defineName[2] == 'l' && defineName[3] == 's'
							&& defineName[4] == 'e') {
						defineName = null;
					}
				}
				if (defineName != null) {
					for (int i = 0; i < defineName.length; i++) {
						if (Character.isLowerCase(defineName[i])) {
							problemReporter.phpUppercaseIdentifierWarning(
									startPos, endPos, referenceContext,
									compilationUnit.compilationResult);
							break;
						}
					}
				}
			}
			// TODO is this ok ?
			// return ref;
			// throwSyntaxError("'(' expected in function call.");
		} else {
			getNextToken();

			if (token == TokenNameRPAREN) {
				getNextToken();
				ref = null;
			} else {
				String functionName;
				if (ident == null) {
					functionName = new String(" ");
				} else {
					functionName = new String(ident);
				}
				non_empty_function_call_parameter_list(functionName);
				if (token != TokenNameRPAREN) {
					throwSyntaxError("')' expected in function call ("
							+ functionName + ").");
				}
				getNextToken();
			}
		}
		if (token == TokenNameMINUS_GREATER) {
			ref = null;
			getNextToken();
			object_property();
			method_or_not();
			variable_properties();
		}
		return ref;
	}

	private void method_or_not() {
		// method_or_not:
		// '(' function_call_parameter_list ')'
		// | /* empty */
		if (Scanner.TRACE) {
			System.out.println("TRACE: method_or_not()");
		}
		if (token == TokenNameLPAREN) {
			getNextToken();
			if (token == TokenNameRPAREN) {
				getNextToken();
				return;
			}
			non_empty_function_call_parameter_list();
			if (token != TokenNameRPAREN) {
				throwSyntaxError("')' expected in method_or_not.");
			}
			getNextToken();
		}
	}

	private void exit_expr() {
		// /* empty */
		// | '(' ')'
		// | '(' expr ')'
		if (token != TokenNameLPAREN) {
			return;
		}
		getNextToken();
		if (token == TokenNameRPAREN) {
			getNextToken();
			return;
		}
		expr();
		if (token != TokenNameRPAREN) {
			throwSyntaxError("')' expected after keyword 'exit'");
		}
		getNextToken();
	}

	// private void encaps_list() {
	// // encaps_list encaps_var
	// // | encaps_list T_STRING
	// // | encaps_list T_NUM_STRING
	// // | encaps_list T_ENCAPSED_AND_WHITESPACE
	// // | encaps_list T_CHARACTER
	// // | encaps_list T_BAD_CHARACTER
	// // | encaps_list '['
	// // | encaps_list ']'
	// // | encaps_list '{'
	// // | encaps_list '}'
	// // | encaps_list T_OBJECT_OPERATOR
	// // | /* empty */
	// while (true) {
	// switch (token) {
	// case TokenNameSTRING:
	// getNextToken();
	// break;
	// case TokenNameLBRACE:
	// // scanner.encapsedStringStack.pop();
	// getNextToken();
	// break;
	// case TokenNameRBRACE:
	// // scanner.encapsedStringStack.pop();
	// getNextToken();
	// break;
	// case TokenNameLBRACKET:
	// // scanner.encapsedStringStack.pop();
	// getNextToken();
	// break;
	// case TokenNameRBRACKET:
	// // scanner.encapsedStringStack.pop();
	// getNextToken();
	// break;
	// case TokenNameMINUS_GREATER:
	// // scanner.encapsedStringStack.pop();
	// getNextToken();
	// break;
	// case TokenNameVariable:
	// case TokenNameDOLLAR_LBRACE:
	// case TokenNameLBRACE_DOLLAR:
	// encaps_var();
	// break;
	// default:
	// char encapsedChar = ((Character)
	// scanner.encapsedStringStack.peek()).charValue();
	// if (encapsedChar == '$') {
	// scanner.encapsedStringStack.pop();
	// encapsedChar = ((Character)
	// scanner.encapsedStringStack.peek()).charValue();
	// switch (encapsedChar) {
	// case '`':
	// if (token == TokenNameEncapsedString0) {
	// return;
	// }
	// token = TokenNameSTRING;
	// continue;
	// case '\'':
	// if (token == TokenNameEncapsedString1) {
	// return;
	// }
	// token = TokenNameSTRING;
	// continue;
	// case '"':
	// if (token == TokenNameEncapsedString2) {
	// return;
	// }
	// token = TokenNameSTRING;
	// continue;
	// }
	// }
	// return;
	// }
	// }
	// }

	// private void encaps_var() {
	// // T_VARIABLE
	// // | T_VARIABLE '[' encaps_var_offset ']'
	// // | T_VARIABLE T_OBJECT_OPERATOR T_STRING
	// // | T_DOLLAR_OPEN_CURLY_BRACES expr '}'
	// // | T_DOLLAR_OPEN_CURLY_BRACES T_STRING_VARNAME '[' expr ']' '}'
	// // | T_CURLY_OPEN variable '}'
	// switch (token) {
	// case TokenNameVariable:
	// getNextToken();
	// if (token == TokenNameLBRACKET) {
	// getNextToken();
	// expr(); //encaps_var_offset();
	// if (token != TokenNameRBRACKET) {
	// throwSyntaxError("']' expected after variable.");
	// }
	// // scanner.encapsedStringStack.pop();
	// getNextToken();
	// // }
	// } else if (token == TokenNameMINUS_GREATER) {
	// getNextToken();
	// if (token != TokenNameIdentifier) {
	// throwSyntaxError("Identifier expected after '->'.");
	// }
	// // scanner.encapsedStringStack.pop();
	// getNextToken();
	// }
	// // else {
	// // // scanner.encapsedStringStack.pop();
	// // int tempToken = TokenNameSTRING;
	// // if (!scanner.encapsedStringStack.isEmpty()
	// // && (token == TokenNameEncapsedString0
	// // || token == TokenNameEncapsedString1
	// // || token == TokenNameEncapsedString2 || token ==
	// // TokenNameERROR)) {
	// // char encapsedChar = ((Character)
	// // scanner.encapsedStringStack.peek())
	// // .charValue();
	// // switch (token) {
	// // case TokenNameEncapsedString0 :
	// // if (encapsedChar == '`') {
	// // tempToken = TokenNameEncapsedString0;
	// // }
	// // break;
	// // case TokenNameEncapsedString1 :
	// // if (encapsedChar == '\'') {
	// // tempToken = TokenNameEncapsedString1;
	// // }
	// // break;
	// // case TokenNameEncapsedString2 :
	// // if (encapsedChar == '"') {
	// // tempToken = TokenNameEncapsedString2;
	// // }
	// // break;
	// // case TokenNameERROR :
	// // if (scanner.source[scanner.currentPosition - 1] == '\\') {
	// // scanner.currentPosition--;
	// // getNextToken();
	// // }
	// // break;
	// // }
	// // }
	// // token = tempToken;
	// // }
	// break;
	// case TokenNameDOLLAR_LBRACE:
	// getNextToken();
	// if (token == TokenNameDOLLAR_LBRACE) {
	// encaps_var();
	// } else if (token == TokenNameIdentifier) {
	// getNextToken();
	// if (token == TokenNameLBRACKET) {
	// getNextToken();
	// // if (token == TokenNameRBRACKET) {
	// // getNextToken();
	// // } else {
	// expr();
	// if (token != TokenNameRBRACKET) {
	// throwSyntaxError("']' expected after '${'.");
	// }
	// getNextToken();
	// // }
	// }
	// } else {
	// expr();
	// }
	// if (token != TokenNameRBRACE) {
	// throwSyntaxError("'}' expected.");
	// }
	// getNextToken();
	// break;
	// case TokenNameLBRACE_DOLLAR:
	// getNextToken();
	// if (token == TokenNameLBRACE_DOLLAR) {
	// encaps_var();
	// } else if (token == TokenNameIdentifier || token > TokenNameKEYWORD) {
	// getNextToken();
	// if (token == TokenNameLBRACKET) {
	// getNextToken();
	// // if (token == TokenNameRBRACKET) {
	// // getNextToken();
	// // } else {
	// expr();
	// if (token != TokenNameRBRACKET) {
	// throwSyntaxError("']' expected.");
	// }
	// getNextToken();
	// // }
	// } else if (token == TokenNameMINUS_GREATER) {
	// getNextToken();
	// if (token != TokenNameIdentifier && token != TokenNameVariable) {
	// throwSyntaxError("String or Variable token expected.");
	// }
	// getNextToken();
	// if (token == TokenNameLBRACKET) {
	// getNextToken();
	// // if (token == TokenNameRBRACKET) {
	// // getNextToken();
	// // } else {
	// expr();
	// if (token != TokenNameRBRACKET) {
	// throwSyntaxError("']' expected after '${'.");
	// }
	// getNextToken();
	// // }
	// }
	// }
	// // if (token != TokenNameRBRACE) {
	// // throwSyntaxError("'}' expected after '{$'.");
	// // }
	// // // scanner.encapsedStringStack.pop();
	// // getNextToken();
	// } else {
	// expr();
	// if (token != TokenNameRBRACE) {
	// throwSyntaxError("'}' expected.");
	// }
	// // scanner.encapsedStringStack.pop();
	// getNextToken();
	// }
	// break;
	// }
	// }

	// private void encaps_var_offset() {
	// // T_STRING
	// // | T_NUM_STRING
	// // | T_VARIABLE
	// switch (token) {
	// case TokenNameSTRING:
	// getNextToken();
	// break;
	// case TokenNameIntegerLiteral:
	// getNextToken();
	// break;
	// case TokenNameVariable:
	// getNextToken();
	// break;
	// case TokenNameIdentifier:
	// getNextToken();
	// break;
	// default:
	// throwSyntaxError("Variable or String token expected.");
	// break;
	// }
	// }

	private void internal_functions_in_yacc() {
		// int start = 0;
		switch (token) {
		// case TokenNameisset:
		// // T_ISSET '(' isset_variables ')'
		// getNextToken();
		// if (token != TokenNameLPAREN) {
		// throwSyntaxError("'(' expected after keyword 'isset'");
		// }
		// getNextToken();
		// isset_variables();
		// if (token != TokenNameRPAREN) {
		// throwSyntaxError("')' expected after keyword 'isset'");
		// }
		// getNextToken();
		// break;
		// case TokenNameempty:
		// // T_EMPTY '(' variable ')'
		// getNextToken();
		// if (token != TokenNameLPAREN) {
		// throwSyntaxError("'(' expected after keyword 'empty'");
		// }
		// getNextToken();
		// variable(false);
		// if (token != TokenNameRPAREN) {
		// throwSyntaxError("')' expected after keyword 'empty'");
		// }
		// getNextToken();
		// break;
		case TokenNameinclude:
			// T_INCLUDE expr
			checkFileName(token);
			break;
		case TokenNameinclude_once:
			// T_INCLUDE_ONCE expr
			checkFileName(token);
			break;
		case TokenNameeval:
			// T_EVAL '(' expr ')'
			getNextToken();
			if (token != TokenNameLPAREN) {
				throwSyntaxError("'(' expected after keyword 'eval'");
			}
			getNextToken();
			expr();
			if (token != TokenNameRPAREN) {
				throwSyntaxError("')' expected after keyword 'eval'");
			}
			getNextToken();
			break;
		case TokenNamerequire:
			// T_REQUIRE expr
			checkFileName(token);
			break;
		case TokenNamerequire_once:
			// T_REQUIRE_ONCE expr
			checkFileName(token);
			break;
		}
	}

	/**
	 * Parse and check the include file name
	 * 
	 * @param includeToken
	 */
	private void checkFileName(int includeToken) {
		// <include-token> expr
		int start = scanner.getCurrentTokenStartPosition();
		boolean hasLPAREN = false;
		getNextToken();
		if (token == TokenNameLPAREN) {
			hasLPAREN = true;
			getNextToken();
		}
		Expression expression = expr();
		if (hasLPAREN) {
			if (token == TokenNameRPAREN) {
				getNextToken();
			} else {
				throwSyntaxError("')' expected for keyword '"
						+ scanner.toStringAction(includeToken) + "'");
			}
		}
		char[] currTokenSource = scanner.getCurrentTokenSource(start);
		IFile file = null;
		if (scanner.compilationUnit != null) {
			IResource resource = scanner.compilationUnit.getResource();
			if (resource != null && resource instanceof IFile) {
				file = (IFile) resource;
			}
		}
		char[][] tokens;
		tokens = new char[1][];
		tokens[0] = currTokenSource;

		ImportReference impt = new ImportReference(tokens, currTokenSource,
				start, scanner.getCurrentTokenEndPosition(), false);
		impt.declarationSourceEnd = impt.sourceEnd;
		impt.declarationEnd = impt.declarationSourceEnd;
		// endPosition is just before the ;
		impt.declarationSourceStart = start;
		includesList.add(impt);

		if (expression instanceof StringLiteral) {
			StringLiteral literal = (StringLiteral) expression;
			char[] includeName = literal.source();
			if (includeName.length == 0) {
				reportSyntaxError("Empty filename after keyword '"
						+ scanner.toStringAction(includeToken) + "'",
						literal.sourceStart, literal.sourceStart + 1);
			}
			String includeNameString = new String(includeName);
			if (literal instanceof StringLiteralDQ) {
				if (includeNameString.indexOf('$') >= 0) {
					// assuming that the filename contains a variable => no
					// filename check
					return;
				}
			}
			if (includeNameString.startsWith("http://")) {
				// assuming external include location
				return;
			}
			if (file != null) {
				// check the filename:
				// System.out.println(new
				// String(compilationUnit.getFileName())+" - "+
				// expression.toStringExpression());
				IProject project = file.getProject();
				if (project != null) {
					IPath path = PHPFileUtil.determineFilePath(
							includeNameString, file, project);

					if (path == null) {
						// SyntaxError: "File: << >> doesn't exist in project."
						String[] args = { expression.toStringExpression(),
								project.getFullPath().toString() };
						problemReporter.phpIncludeNotExistWarning(args,
								literal.sourceStart, literal.sourceEnd,
								referenceContext,
								compilationUnit.compilationResult);
					} else {
						try {
							String filePath = path.toString();
							String ext = file.getRawLocation()
									.getFileExtension();
							int fileExtensionLength = ext == null ? 0 : ext
									.length() + 1;

							IFile f = PHPFileUtil.createFile(path, project);

							impt.tokens = CharOperation.splitOn('/', filePath
									.toCharArray(), 0, filePath.length()
									- fileExtensionLength);
							impt.setFile(f);
						} catch (Exception e) {
							// the file is outside of the workspace
						}
					}
				}
			}
		}
	}

	private void isset_variables() {
		// variable
		// | isset_variables ','
		if (token == TokenNameRPAREN) {
			throwSyntaxError("Variable expected after keyword 'isset'");
		}
		while (true) {
			variable(true, false);
			if (token == TokenNameCOMMA) {
				getNextToken();
			} else {
				break;
			}
		}
	}

	private boolean common_scalar() {
		// common_scalar:
		// T_LNUMBER
		// | T_DNUMBER
		// | T_CONSTANT_ENCAPSED_STRING
		// | T_LINE
		// | T_FILE
		// | T_CLASS_C
		// | T_METHOD_C
		// | T_FUNC_C
		switch (token) {
		case TokenNameIntegerLiteral:
			getNextToken();
			return true;
		case TokenNameDoubleLiteral:
			getNextToken();
			return true;
		case TokenNameStringDoubleQuote:
			getNextToken();
			return true;
		case TokenNameStringSingleQuote:
			getNextToken();
			return true;
		case TokenNameStringInterpolated:
			getNextToken();
			return true;
		case TokenNameFILE:
			getNextToken();
			return true;
		case TokenNameLINE:
			getNextToken();
			return true;
		case TokenNameCLASS_C:
			getNextToken();
			return true;
		case TokenNameMETHOD_C:
			getNextToken();
			return true;
		case TokenNameFUNC_C:
			getNextToken();
			return true;
		}
		return false;
	}

	private void scalar() {
		// scalar:
		// T_STRING
		// | T_STRING_VARNAME
		// | class_constant
		// | common_scalar
		// | '"' encaps_list '"'
		// | '\'' encaps_list '\''
		// | T_START_HEREDOC encaps_list T_END_HEREDOC
		throwSyntaxError("Not yet implemented (scalar).");
	}

	private void static_scalar() {
		// static_scalar: /* compile-time evaluated scalars */
		// common_scalar
		// | T_STRING
		// | '+' static_scalar
		// | '-' static_scalar
		// | T_ARRAY '(' static_array_pair_list ')'
		// | static_class_constant
		if (common_scalar()) {
			return;
		}
		switch (token) {
		case TokenNameIdentifier:
			getNextToken();
			// static_class_constant:
			// T_STRING T_PAAMAYIM_NEKUDOTAYIM T_STRING
			if (token == TokenNamePAAMAYIM_NEKUDOTAYIM) {
				getNextToken();
				if (token == TokenNameIdentifier) {
					getNextToken();
				} else {
					throwSyntaxError("Identifier expected after '::' operator.");
				}
			}
			break;
		case TokenNameEncapsedString0:
			try {
				scanner.currentCharacter = scanner.source[scanner.currentPosition++];
				while (scanner.currentCharacter != '`') {
					if (scanner.currentCharacter == '\\') {
						scanner.currentPosition++;
					}
					scanner.currentCharacter = scanner.source[scanner.currentPosition++];
				}
				getNextToken();
			} catch (IndexOutOfBoundsException e) {
				throwSyntaxError("'`' expected at end of static string.");
			}
			break;
		// case TokenNameEncapsedString1:
		// try {
		// scanner.currentCharacter = scanner.source[scanner.currentPosition++];
		// while (scanner.currentCharacter != '\'') {
		// if (scanner.currentCharacter == '\\') {
		// scanner.currentPosition++;
		// }
		// scanner.currentCharacter = scanner.source[scanner.currentPosition++];
		// }
		// getNextToken();
		// } catch (IndexOutOfBoundsException e) {
		// throwSyntaxError("'\'' expected at end of static string.");
		// }
		// break;
		// case TokenNameEncapsedString2:
		// try {
		// scanner.currentCharacter = scanner.source[scanner.currentPosition++];
		// while (scanner.currentCharacter != '"') {
		// if (scanner.currentCharacter == '\\') {
		// scanner.currentPosition++;
		// }
		// scanner.currentCharacter = scanner.source[scanner.currentPosition++];
		// }
		// getNextToken();
		// } catch (IndexOutOfBoundsException e) {
		// throwSyntaxError("'\"' expected at end of static string.");
		// }
		// break;
		case TokenNameStringSingleQuote:
			getNextToken();
			break;
		case TokenNameStringDoubleQuote:
			getNextToken();
			break;
		case TokenNamePLUS:
			getNextToken();
			static_scalar();
			break;
		case TokenNameMINUS:
			getNextToken();
			static_scalar();
			break;
		case TokenNamearray:
			getNextToken();
			if (token != TokenNameLPAREN) {
				throwSyntaxError("'(' expected after keyword 'array'");
			}
			getNextToken();
			if (token == TokenNameRPAREN) {
				getNextToken();
				break;
			}
			non_empty_static_array_pair_list();
			if (token != TokenNameRPAREN) {
				throwSyntaxError("')' or ',' expected after keyword 'array'");
			}
			getNextToken();
			break;
		// case TokenNamenull :
		// getNextToken();
		// break;
		// case TokenNamefalse :
		// getNextToken();
		// break;
		// case TokenNametrue :
		// getNextToken();
		// break;
		default:
			throwSyntaxError("Static scalar/constant expected.");
		}
	}

	private void non_empty_static_array_pair_list() {
		// non_empty_static_array_pair_list:
		// non_empty_static_array_pair_list ',' static_scalar T_DOUBLE_ARROW
		// static_scalar
		// | non_empty_static_array_pair_list ',' static_scalar
		// | static_scalar T_DOUBLE_ARROW static_scalar
		// | static_scalar
		while (true) {
			static_scalar();
			if (token == TokenNameEQUAL_GREATER) {
				getNextToken();
				static_scalar();
			}
			if (token != TokenNameCOMMA) {
				break;
			}
			getNextToken();
			if (token == TokenNameRPAREN) {
				break;
			}
		}
	}

	// public void reportSyntaxError() { //int act, int currentKind, int
	// // stateStackTop) {
	// /* remember current scanner position */
	// int startPos = scanner.startPosition;
	// int currentPos = scanner.currentPosition;
	//
	// this.checkAndReportBracketAnomalies(problemReporter());
	// /* reset scanner where it was */
	// scanner.startPosition = startPos;
	// scanner.currentPosition = currentPos;
	// }

	public static final int RoundBracket = 0;

	public static final int SquareBracket = 1;

	public static final int CurlyBracket = 2;

	public static final int BracketKinds = 3;

	protected int[] nestedMethod; // the ptr is nestedType

	protected int nestedType, dimensions;

	// variable set stack
	final static int VariableStackIncrement = 10;

	HashMap fTypeVariables = null;

	HashMap fMethodVariables = null;

	ArrayList fStackUnassigned = new ArrayList();

	// ast stack
	final static int AstStackIncrement = 100;

	protected int astPtr;

	protected ASTNode[] astStack = new ASTNode[AstStackIncrement];

	protected int astLengthPtr;

	protected int[] astLengthStack;

	ASTNode[] noAstNodes = new ASTNode[AstStackIncrement];

	public CompilationUnitDeclaration compilationUnit; /*
														 * the result from
														 * parse()
														 */

	protected ReferenceContext referenceContext;

	protected ProblemReporter problemReporter;

	protected CompilerOptions options;

	private ArrayList includesList;

	// protected CompilationResult compilationResult;
	/**
	 * Returns this parser's problem reporter initialized with its reference
	 * context. Also it is assumed that a problem is going to be reported, so
	 * initializes the compilation result's line positions.
	 */
	public ProblemReporter problemReporter() {
		if (scanner.recordLineSeparator) {
			compilationUnit.compilationResult.lineSeparatorPositions = scanner
					.getLineEnds();
		}
		problemReporter.referenceContext = referenceContext;
		return problemReporter;
	}

	/*
	 * Reconsider the entire source looking for inconsistencies in {} () []
	 */
	// public boolean checkAndReportBracketAnomalies(ProblemReporter
	// problemReporter) {
	// scanner.wasAcr = false;
	// boolean anomaliesDetected = false;
	// try {
	// char[] source = scanner.source;
	// int[] leftCount = { 0, 0, 0 };
	// int[] rightCount = { 0, 0, 0 };
	// int[] depths = { 0, 0, 0 };
	// int[][] leftPositions = new int[][] { new int[10], new int[10], new
	// int[10]
	// };
	// int[][] leftDepths = new int[][] { new int[10], new int[10], new int[10]
	// };
	// int[][] rightPositions = new int[][] { new int[10], new int[10], new
	// int[10] };
	// int[][] rightDepths = new int[][] { new int[10], new int[10], new int[10]
	// };
	// scanner.currentPosition = scanner.initialPosition; //starting
	// // point
	// // (first-zero-based
	// // char)
	// while (scanner.currentPosition < scanner.eofPosition) { //loop for
	// // jumping
	// // over
	// // comments
	// try {
	// // ---------Consume white space and handles
	// // startPosition---------
	// boolean isWhiteSpace;
	// do {
	// scanner.startPosition = scanner.currentPosition;
	// // if (((scanner.currentCharacter =
	// // source[scanner.currentPosition++]) == '\\') &&
	// // (source[scanner.currentPosition] == 'u')) {
	// // isWhiteSpace = scanner.jumpOverUnicodeWhiteSpace();
	// // } else {
	// if (scanner.recordLineSeparator && ((scanner.currentCharacter == '\r') ||
	// (scanner.currentCharacter == '\n'))) {
	// if (scanner.lineEnds[scanner.linePtr] < scanner.startPosition) {
	// // only record line positions we have not
	// // recorded yet
	// scanner.pushLineSeparator();
	// }
	// }
	// isWhiteSpace = CharOperation.isWhitespace(scanner.currentCharacter);
	// // }
	// } while (isWhiteSpace && (scanner.currentPosition <
	// scanner.eofPosition));
	// // -------consume token until } is found---------
	// switch (scanner.currentCharacter) {
	// case '{': {
	// int index = leftCount[CurlyBracket]++;
	// if (index == leftPositions[CurlyBracket].length) {
	// System.arraycopy(leftPositions[CurlyBracket], 0,
	// (leftPositions[CurlyBracket] = new int[index * 2]), 0, index);
	// System.arraycopy(leftDepths[CurlyBracket], 0, (leftDepths[CurlyBracket] =
	// new int[index * 2]), 0, index);
	// }
	// leftPositions[CurlyBracket][index] = scanner.startPosition;
	// leftDepths[CurlyBracket][index] = depths[CurlyBracket]++;
	// }
	// break;
	// case '}': {
	// int index = rightCount[CurlyBracket]++;
	// if (index == rightPositions[CurlyBracket].length) {
	// System.arraycopy(rightPositions[CurlyBracket], 0,
	// (rightPositions[CurlyBracket] = new int[index * 2]), 0, index);
	// System.arraycopy(rightDepths[CurlyBracket], 0, (rightDepths[CurlyBracket]
	// =
	// new int[index * 2]), 0, index);
	// }
	// rightPositions[CurlyBracket][index] = scanner.startPosition;
	// rightDepths[CurlyBracket][index] = --depths[CurlyBracket];
	// }
	// break;
	// case '(': {
	// int index = leftCount[RoundBracket]++;
	// if (index == leftPositions[RoundBracket].length) {
	// System.arraycopy(leftPositions[RoundBracket], 0,
	// (leftPositions[RoundBracket] = new int[index * 2]), 0, index);
	// System.arraycopy(leftDepths[RoundBracket], 0, (leftDepths[RoundBracket] =
	// new int[index * 2]), 0, index);
	// }
	// leftPositions[RoundBracket][index] = scanner.startPosition;
	// leftDepths[RoundBracket][index] = depths[RoundBracket]++;
	// }
	// break;
	// case ')': {
	// int index = rightCount[RoundBracket]++;
	// if (index == rightPositions[RoundBracket].length) {
	// System.arraycopy(rightPositions[RoundBracket], 0,
	// (rightPositions[RoundBracket] = new int[index * 2]), 0, index);
	// System.arraycopy(rightDepths[RoundBracket], 0, (rightDepths[RoundBracket]
	// =
	// new int[index * 2]), 0, index);
	// }
	// rightPositions[RoundBracket][index] = scanner.startPosition;
	// rightDepths[RoundBracket][index] = --depths[RoundBracket];
	// }
	// break;
	// case '[': {
	// int index = leftCount[SquareBracket]++;
	// if (index == leftPositions[SquareBracket].length) {
	// System.arraycopy(leftPositions[SquareBracket], 0,
	// (leftPositions[SquareBracket] = new int[index * 2]), 0, index);
	// System.arraycopy(leftDepths[SquareBracket], 0, (leftDepths[SquareBracket]
	// =
	// new int[index * 2]), 0, index);
	// }
	// leftPositions[SquareBracket][index] = scanner.startPosition;
	// leftDepths[SquareBracket][index] = depths[SquareBracket]++;
	// }
	// break;
	// case ']': {
	// int index = rightCount[SquareBracket]++;
	// if (index == rightPositions[SquareBracket].length) {
	// System.arraycopy(rightPositions[SquareBracket], 0,
	// (rightPositions[SquareBracket] = new int[index * 2]), 0, index);
	// System.arraycopy(rightDepths[SquareBracket], 0,
	// (rightDepths[SquareBracket]
	// = new int[index * 2]), 0, index);
	// }
	// rightPositions[SquareBracket][index] = scanner.startPosition;
	// rightDepths[SquareBracket][index] = --depths[SquareBracket];
	// }
	// break;
	// case '\'': {
	// if (scanner.getNextChar('\\')) {
	// scanner.scanEscapeCharacter();
	// } else { // consume next character
	// scanner.unicodeAsBackSlash = false;
	// // if (((scanner.currentCharacter =
	// // source[scanner.currentPosition++]) ==
	// // '\\') &&
	// // (source[scanner.currentPosition] ==
	// // 'u')) {
	// // scanner.getNextUnicodeChar();
	// // } else {
	// if (scanner.withoutUnicodePtr != 0) {
	// scanner.withoutUnicodeBuffer[++scanner.withoutUnicodePtr] =
	// scanner.currentCharacter;
	// }
	// // }
	// }
	// scanner.getNextChar('\'');
	// break;
	// }
	// case '"':
	// // consume next character
	// scanner.unicodeAsBackSlash = false;
	// // if (((scanner.currentCharacter =
	// // source[scanner.currentPosition++]) == '\\') &&
	// // (source[scanner.currentPosition] == 'u')) {
	// // scanner.getNextUnicodeChar();
	// // } else {
	// if (scanner.withoutUnicodePtr != 0) {
	// scanner.withoutUnicodeBuffer[++scanner.withoutUnicodePtr] =
	// scanner.currentCharacter;
	// }
	// // }
	// while (scanner.currentCharacter != '"') {
	// if (scanner.currentCharacter == '\r') {
	// if (source[scanner.currentPosition] == '\n')
	// scanner.currentPosition++;
	// break; // the string cannot go further that
	// // the line
	// }
	// if (scanner.currentCharacter == '\n') {
	// break; // the string cannot go further that
	// // the line
	// }
	// if (scanner.currentCharacter == '\\') {
	// scanner.scanEscapeCharacter();
	// }
	// // consume next character
	// scanner.unicodeAsBackSlash = false;
	// // if (((scanner.currentCharacter =
	// // source[scanner.currentPosition++]) == '\\')
	// // && (source[scanner.currentPosition] == 'u'))
	// // {
	// // scanner.getNextUnicodeChar();
	// // } else {
	// if (scanner.withoutUnicodePtr != 0) {
	// scanner.withoutUnicodeBuffer[++scanner.withoutUnicodePtr] =
	// scanner.currentCharacter;
	// }
	// // }
	// }
	// break;
	// case '/': {
	// int test;
	// if ((test = scanner.getNextChar('/', '*')) == 0) { //line
	// // comment
	// //get the next char
	// if (((scanner.currentCharacter = source[scanner.currentPosition++]) ==
	// '\\')
	// && (source[scanner.currentPosition] == 'u')) {
	// //-------------unicode traitement
	// // ------------
	// int c1 = 0, c2 = 0, c3 = 0, c4 = 0;
	// scanner.currentPosition++;
	// while (source[scanner.currentPosition] == 'u') {
	// scanner.currentPosition++;
	// }
	// if ((c1 = Character.getNumericValue(source[scanner.currentPosition++])) >
	// 15 || c1 < 0
	// || (c2 = Character.getNumericValue(source[scanner.currentPosition++])) >
	// 15
	// || c2 < 0
	// || (c3 = Character.getNumericValue(source[scanner.currentPosition++])) >
	// 15
	// || c3 < 0
	// || (c4 = Character.getNumericValue(source[scanner.currentPosition++])) >
	// 15
	// || c4 < 0) { //error
	// // don't
	// // care of the
	// // value
	// scanner.currentCharacter = 'A';
	// } //something different from \n and \r
	// else {
	// scanner.currentCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
	// }
	// }
	// while (scanner.currentCharacter != '\r' && scanner.currentCharacter !=
	// '\n') {
	// //get the next char
	// scanner.startPosition = scanner.currentPosition;
	// if (((scanner.currentCharacter = source[scanner.currentPosition++]) ==
	// '\\')
	// && (source[scanner.currentPosition] == 'u')) {
	// //-------------unicode traitement
	// // ------------
	// int c1 = 0, c2 = 0, c3 = 0, c4 = 0;
	// scanner.currentPosition++;
	// while (source[scanner.currentPosition] == 'u') {
	// scanner.currentPosition++;
	// }
	// if ((c1 = Character.getNumericValue(source[scanner.currentPosition++])) >
	// 15 || c1 < 0
	// || (c2 = Character.getNumericValue(source[scanner.currentPosition++])) >
	// 15
	// || c2 < 0
	// || (c3 = Character.getNumericValue(source[scanner.currentPosition++])) >
	// 15
	// || c3 < 0
	// || (c4 = Character.getNumericValue(source[scanner.currentPosition++])) >
	// 15
	// || c4 < 0) { //error
	// // don't
	// // care of the
	// // value
	// scanner.currentCharacter = 'A';
	// } //something different from \n
	// // and \r
	// else {
	// scanner.currentCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
	// }
	// }
	// }
	// if (scanner.recordLineSeparator && ((scanner.currentCharacter == '\r') ||
	// (scanner.currentCharacter == '\n'))) {
	// if (scanner.lineEnds[scanner.linePtr] < scanner.startPosition) {
	// // only record line positions we
	// // have not recorded yet
	// scanner.pushLineSeparator();
	// if (this.scanner.taskTags != null) {
	// this.scanner.checkTaskTag(this.scanner.getCurrentTokenStartPosition(),
	// this.scanner
	// .getCurrentTokenEndPosition());
	// }
	// }
	// }
	// break;
	// }
	// if (test > 0) { //traditional and annotation
	// // comment
	// boolean star = false;
	// // consume next character
	// scanner.unicodeAsBackSlash = false;
	// // if (((scanner.currentCharacter =
	// // source[scanner.currentPosition++]) ==
	// // '\\') &&
	// // (source[scanner.currentPosition] ==
	// // 'u')) {
	// // scanner.getNextUnicodeChar();
	// // } else {
	// if (scanner.withoutUnicodePtr != 0) {
	// scanner.withoutUnicodeBuffer[++scanner.withoutUnicodePtr] =
	// scanner.currentCharacter;
	// }
	// // }
	// if (scanner.currentCharacter == '*') {
	// star = true;
	// }
	// //get the next char
	// if (((scanner.currentCharacter = source[scanner.currentPosition++]) ==
	// '\\')
	// && (source[scanner.currentPosition] == 'u')) {
	// //-------------unicode traitement
	// // ------------
	// int c1 = 0, c2 = 0, c3 = 0, c4 = 0;
	// scanner.currentPosition++;
	// while (source[scanner.currentPosition] == 'u') {
	// scanner.currentPosition++;
	// }
	// if ((c1 = Character.getNumericValue(source[scanner.currentPosition++])) >
	// 15 || c1 < 0
	// || (c2 = Character.getNumericValue(source[scanner.currentPosition++])) >
	// 15
	// || c2 < 0
	// || (c3 = Character.getNumericValue(source[scanner.currentPosition++])) >
	// 15
	// || c3 < 0
	// || (c4 = Character.getNumericValue(source[scanner.currentPosition++])) >
	// 15
	// || c4 < 0) { //error
	// // don't
	// // care of the
	// // value
	// scanner.currentCharacter = 'A';
	// } //something different from * and /
	// else {
	// scanner.currentCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
	// }
	// }
	// //loop until end of comment */
	// while ((scanner.currentCharacter != '/') || (!star)) {
	// star = scanner.currentCharacter == '*';
	// //get next char
	// if (((scanner.currentCharacter = source[scanner.currentPosition++]) ==
	// '\\')
	// && (source[scanner.currentPosition] == 'u')) {
	// //-------------unicode traitement
	// // ------------
	// int c1 = 0, c2 = 0, c3 = 0, c4 = 0;
	// scanner.currentPosition++;
	// while (source[scanner.currentPosition] == 'u') {
	// scanner.currentPosition++;
	// }
	// if ((c1 = Character.getNumericValue(source[scanner.currentPosition++])) >
	// 15 || c1 < 0
	// || (c2 = Character.getNumericValue(source[scanner.currentPosition++])) >
	// 15
	// || c2 < 0
	// || (c3 = Character.getNumericValue(source[scanner.currentPosition++])) >
	// 15
	// || c3 < 0
	// || (c4 = Character.getNumericValue(source[scanner.currentPosition++])) >
	// 15
	// || c4 < 0) { //error
	// // don't
	// // care of the
	// // value
	// scanner.currentCharacter = 'A';
	// } //something different from * and
	// // /
	// else {
	// scanner.currentCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
	// }
	// }
	// }
	// if (this.scanner.taskTags != null) {
	// this.scanner.checkTaskTag(this.scanner.getCurrentTokenStartPosition(),
	// this.scanner.getCurrentTokenEndPosition());
	// }
	// break;
	// }
	// break;
	// }
	// default:
	// if (Scanner.isPHPIdentifierStart(scanner.currentCharacter)) {
	// scanner.scanIdentifierOrKeyword(false);
	// break;
	// }
	// if (Character.isDigit(scanner.currentCharacter)) {
	// scanner.scanNumber(false);
	// break;
	// }
	// }
	// //-----------------end switch while
	// // try--------------------
	// } catch (IndexOutOfBoundsException e) {
	// break; // read until EOF
	// } catch (InvalidInputException e) {
	// return false; // no clue
	// }
	// }
	// if (scanner.recordLineSeparator) {
	// compilationUnit.compilationResult.lineSeparatorPositions =
	// scanner.getLineEnds();
	// }
	// // check placement anomalies against other kinds of brackets
	// for (int kind = 0; kind < BracketKinds; kind++) {
	// for (int leftIndex = leftCount[kind] - 1; leftIndex >= 0; leftIndex--) {
	// int start = leftPositions[kind][leftIndex]; // deepest
	// // first
	// // find matching closing bracket
	// int depth = leftDepths[kind][leftIndex];
	// int end = -1;
	// for (int i = 0; i < rightCount[kind]; i++) {
	// int pos = rightPositions[kind][i];
	// // want matching bracket further in source with same
	// // depth
	// if ((pos > start) && (depth == rightDepths[kind][i])) {
	// end = pos;
	// break;
	// }
	// }
	// if (end < 0) { // did not find a good closing match
	// problemReporter.unmatchedBracket(start, referenceContext,
	// compilationUnit.compilationResult);
	// return true;
	// }
	// // check if even number of opening/closing other brackets
	// // in between this pair of brackets
	// int balance = 0;
	// for (int otherKind = 0; (balance == 0) && (otherKind < BracketKinds);
	// otherKind++) {
	// for (int i = 0; i < leftCount[otherKind]; i++) {
	// int pos = leftPositions[otherKind][i];
	// if ((pos > start) && (pos < end))
	// balance++;
	// }
	// for (int i = 0; i < rightCount[otherKind]; i++) {
	// int pos = rightPositions[otherKind][i];
	// if ((pos > start) && (pos < end))
	// balance--;
	// }
	// if (balance != 0) {
	// problemReporter.unmatchedBracket(start, referenceContext,
	// compilationUnit.compilationResult); //bracket
	// // anomaly
	// return true;
	// }
	// }
	// }
	// // too many opening brackets ?
	// for (int i = rightCount[kind]; i < leftCount[kind]; i++) {
	// anomaliesDetected = true;
	// problemReporter.unmatchedBracket(leftPositions[kind][leftCount[kind] - i
	// -
	// 1], referenceContext,
	// compilationUnit.compilationResult);
	// }
	// // too many closing brackets ?
	// for (int i = leftCount[kind]; i < rightCount[kind]; i++) {
	// anomaliesDetected = true;
	// problemReporter.unmatchedBracket(rightPositions[kind][i],
	// referenceContext,
	// compilationUnit.compilationResult);
	// }
	// if (anomaliesDetected)
	// return true;
	// }
	// return anomaliesDetected;
	// } catch (ArrayStoreException e) { // jdk1.2.2 jit bug
	// return anomaliesDetected;
	// } catch (NullPointerException e) { // jdk1.2.2 jit bug
	// return anomaliesDetected;
	// }
	// }
	protected void pushOnAstLengthStack(int pos) {
		try {
			astLengthStack[++astLengthPtr] = pos;
		} catch (IndexOutOfBoundsException e) {
			int oldStackLength = astLengthStack.length;
			int[] oldPos = astLengthStack;
			astLengthStack = new int[oldStackLength + StackIncrement];
			System.arraycopy(oldPos, 0, astLengthStack, 0, oldStackLength);
			astLengthStack[astLengthPtr] = pos;
		}
	}

	protected void pushOnAstStack(ASTNode node) {
		/*
		 * add a new obj on top of the ast stack
		 */
		try {
			astStack[++astPtr] = node;
		} catch (IndexOutOfBoundsException e) {
			int oldStackLength = astStack.length;
			ASTNode[] oldStack = astStack;
			astStack = new ASTNode[oldStackLength + AstStackIncrement];
			System.arraycopy(oldStack, 0, astStack, 0, oldStackLength);
			astPtr = oldStackLength;
			astStack[astPtr] = node;
		}
		try {
			astLengthStack[++astLengthPtr] = 1;
		} catch (IndexOutOfBoundsException e) {
			int oldStackLength = astLengthStack.length;
			int[] oldPos = astLengthStack;
			astLengthStack = new int[oldStackLength + AstStackIncrement];
			System.arraycopy(oldPos, 0, astLengthStack, 0, oldStackLength);
			astLengthStack[astLengthPtr] = 1;
		}
	}

	protected void resetModifiers() {
		this.modifiers = AccDefault;
		this.modifiersSourceStart = -1; // <-- see comment into
		// modifiersFlag(int)
		this.scanner.commentPtr = -1;
	}

	protected void consumePackageDeclarationName(IFile file) {
		// create a package name similar to java package names
		String projectPath = ProjectPrefUtil.getDocumentRoot(file.getProject())
				.toString();
		 String filePath = file.getFullPath().toString();
                
                String ext = file.getFileExtension();
                int fileExtensionLength = ext == null ? 0 : ext.length() + 1;
                ImportReference impt;
                char[][] tokens;
		if (filePath.startsWith(projectPath)) {
			tokens = CharOperation.splitOn('/', filePath.toCharArray(),
					projectPath.length() + 1, filePath.length()
							- fileExtensionLength);
		} else {
			String name = file.getName();
			tokens = new char[1][];
			tokens[0] = name.substring(0, name.length() - fileExtensionLength)
					.toCharArray();
		}

		this.compilationUnit.currentPackage = impt = new ImportReference(
				tokens, new char[0], 0, 0, true);

		impt.declarationSourceStart = 0;
		impt.declarationSourceEnd = 0;
		impt.declarationEnd = 0;
		// endPosition is just before the ;

	}

	public final static String[] GLOBALS = { "$this", "$_COOKIE", "$_ENV",
			"$_FILES", "$_GET", "$GLOBALS", "$_POST", "$_REQUEST", "$_SESSION",
			"$_SERVER" };

	/**
	 * 
	 */
	private void pushFunctionVariableSet() {
		HashSet set = new HashSet();
		if (fStackUnassigned.isEmpty()) {
			for (int i = 0; i < GLOBALS.length; i++) {
				set.add(GLOBALS[i]);
			}
		}
		fStackUnassigned.add(set);
	}

	private void pushIfVariableSet() {
		if (!fStackUnassigned.isEmpty()) {
			HashSet set = new HashSet();
			fStackUnassigned.add(set);
		}
	}

	private HashSet removeIfVariableSet() {
		if (!fStackUnassigned.isEmpty()) {
			return (HashSet) fStackUnassigned
					.remove(fStackUnassigned.size() - 1);
		}
		return null;
	}

	/**
	 * Returns the <i>set of assigned variables </i> returns null if no Set is
	 * defined at the current scanner position
	 */
	private HashSet peekVariableSet() {
		if (!fStackUnassigned.isEmpty()) {
			return (HashSet) fStackUnassigned.get(fStackUnassigned.size() - 1);
		}
		return null;
	}

	/**
	 * add the current identifier source to the <i>set of assigned variables
	 * </i>
	 * 
	 * @param set
	 */
	private void addVariableSet(HashSet set) {
		if (set != null) {
			set.add(new String(scanner.getCurrentTokenSource()));
		}
	}

	/**
	 * add the current identifier source to the <i>set of assigned variables
	 * </i>
	 * 
	 */
	private void addVariableSet() {
		HashSet set = peekVariableSet();
		if (set != null) {
			set.add(new String(scanner.getCurrentTokenSource()));
		}
	}

	/**
	 * add the current identifier source to the <i>set of assigned variables
	 * </i>
	 * 
	 */
	private void addVariableSet(char[] token) {
		HashSet set = peekVariableSet();
		if (set != null) {
			set.add(new String(token));
		}
	}

	/**
	 * check if the current identifier source is in the <i>set of assigned
	 * variables </i> Returns true, if no set is defined for the current scanner
	 * position
	 * 
	 */
	private boolean containsVariableSet() {
		return containsVariableSet(scanner.getCurrentTokenSource());
	}

	private boolean containsVariableSet(char[] token) {

		if (!fStackUnassigned.isEmpty()) {
			HashSet set;
			String str = new String(token);
			for (int i = 0; i < fStackUnassigned.size(); i++) {
				set = (HashSet) fStackUnassigned.get(i);
				if (set.contains(str)) {
					return true;
				}
			}
			return false;
		}
		return true;
	}
}