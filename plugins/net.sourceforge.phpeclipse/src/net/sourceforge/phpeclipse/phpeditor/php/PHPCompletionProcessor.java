/**********************************************************************
 Copyright (c) 2000, 2002 IBM Corp. and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html

 Contributors:
 IBM Corporation - Initial implementation
 **********************************************************************/
package net.sourceforge.phpeclipse.phpeditor.php;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import net.sourceforge.phpdt.core.ICompilationUnit;
import net.sourceforge.phpdt.core.IJavaElement;
import net.sourceforge.phpdt.core.IMethod;
import net.sourceforge.phpdt.core.IType;
import net.sourceforge.phpdt.core.JavaCore;
import net.sourceforge.phpdt.core.ToolFactory;
import net.sourceforge.phpdt.core.compiler.ITerminalSymbols;
import net.sourceforge.phpdt.core.compiler.InvalidInputException;
import net.sourceforge.phpdt.internal.compiler.DefaultErrorHandlingPolicies;
import net.sourceforge.phpdt.internal.compiler.ast.CompilationUnitDeclaration;
import net.sourceforge.phpdt.internal.compiler.impl.CompilerOptions;
import net.sourceforge.phpdt.internal.compiler.parser.Scanner;
import net.sourceforge.phpdt.internal.compiler.parser.SyntaxError;
import net.sourceforge.phpdt.internal.compiler.parser.UnitParser;
import net.sourceforge.phpdt.internal.compiler.parser.VariableInfo;
import net.sourceforge.phpdt.internal.compiler.problem.DefaultProblemFactory;
import net.sourceforge.phpdt.internal.compiler.problem.ProblemReporter;
import net.sourceforge.phpdt.internal.corext.template.php.JavaContext;
import net.sourceforge.phpdt.internal.corext.template.php.JavaContextType;
import net.sourceforge.phpdt.internal.ui.text.PHPCodeReader;
import net.sourceforge.phpdt.internal.ui.text.java.IPHPCompletionProposal;
import net.sourceforge.phpdt.internal.ui.text.java.JavaParameterListValidator;
import net.sourceforge.phpdt.internal.ui.text.java.PHPCompletionProposalComparator;
import net.sourceforge.phpdt.internal.ui.text.template.BuiltInEngine;
import net.sourceforge.phpdt.internal.ui.text.template.DeclarationEngine;
import net.sourceforge.phpdt.internal.ui.text.template.LocalVariableProposal;
import net.sourceforge.phpdt.internal.ui.text.template.contentassist.TemplateEngine;
import net.sourceforge.phpdt.ui.IWorkingCopyManager;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;
import net.sourceforge.phpeclipse.builder.IdentifierIndexManager;
import net.sourceforge.phpeclipse.phpeditor.PHPEditor;
import net.sourceforge.phpeclipse.phpeditor.PHPSyntaxRdr;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationExtension;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;

/**
 * Example PHP completion processor.
 */
public class PHPCompletionProcessor implements IContentAssistProcessor {
	/**
	 * Simple content assist tip closer. The tip is valid in a range of 5
	 * characters around its popup location.
	 */
	// protected static class Validator implements IContextInformationValidator,
	// IContextInformationPresenter {
	// protected int fInstallOffset;
	//
	// /*
	// * @see IContextInformationValidator#isContextInformationValid(int)
	// */
	// public boolean isContextInformationValid(int offset) {
	// return Math.abs(fInstallOffset - offset) < 5;
	// }
	//
	// /*
	// * @see IContextInformationValidator#install(IContextInformation,
	// ITextViewer, int)
	// */
	// public void install(IContextInformation info, ITextViewer viewer, int
	// offset) {
	// fInstallOffset = offset;
	// }
	//
	// /*
	// * @see
	// org.eclipse.jface.text.contentassist.IContextInformationPresenter#updatePresentation(int,
	// TextPresentation)
	// */
	// public boolean updatePresentation(int documentPosition, TextPresentation
	// presentation) {
	// return false;
	// }
	// };
	private static class ContextInformationWrapper implements
			IContextInformation, IContextInformationExtension {
		private final IContextInformation fContextInformation;

		private int fPosition;

		public ContextInformationWrapper(IContextInformation contextInformation) {
			fContextInformation = contextInformation;
		}

		/*
		 * @see IContextInformation#getContextDisplayString()
		 */
		public String getContextDisplayString() {
			return fContextInformation.getContextDisplayString();
		}

		/*
		 * @see IContextInformation#getImage()
		 */
		public Image getImage() {
			return fContextInformation.getImage();
		}

		/*
		 * @see IContextInformation#getInformationDisplayString()
		 */
		public String getInformationDisplayString() {
			return fContextInformation.getInformationDisplayString();
		}

		/*
		 * @see IContextInformationExtension#getContextInformationPosition()
		 */
		public int getContextInformationPosition() {
			return fPosition;
		}

		public void setContextInformationPosition(int position) {
			fPosition = position;
		}
	};

	// private class TableName {
	// String fTableName;
	//
	// TableName() {
	// fTableName = null;
	// }
	//
	// /**
	// * @return Returns the tableName.
	// */
	// public String getTableName() {
	// if (fTableName == null) {
	// return "<!--no-table-->";
	// }
	// return fTableName;
	// }
	//
	// /**
	// * @param tableName
	// * The tableName to set.
	// */
	// public void setTableName(String tableName) {
	// fTableName = tableName;
	// }
	// }

	private char[] fProposalAutoActivationSet;

	protected IContextInformationValidator fValidator = null;

	private TemplateEngine fTemplateEngine;

	private PHPCompletionProposalComparator fComparator;

	private IEditorPart fEditor;

	protected IWorkingCopyManager fManager;

	public PHPCompletionProcessor(IEditorPart editor) {
		fEditor = editor;
		fManager = PHPeclipsePlugin.getDefault().getWorkingCopyManager();
		TemplateContextType contextType = PHPeclipsePlugin.getDefault()
				.getTemplateContextRegistry().getContextType("php"); //$NON-NLS-1$
		if (contextType != null)
			fTemplateEngine = new TemplateEngine(contextType);
		fComparator = new PHPCompletionProposalComparator();
	}

	/**
	 * Tells this processor to order the proposals alphabetically.
	 * 
	 * @param order
	 *            <code>true</code> if proposals should be ordered.
	 */
	public void orderProposalsAlphabetically(boolean order) {
		fComparator.setOrderAlphabetically(order);
	}

	/**
	 * Sets this processor's set of characters triggering the activation of the
	 * completion proposal computation.
	 * 
	 * @param activationSet
	 *            the activation set
	 */
	public void setCompletionProposalAutoActivationCharacters(
			char[] activationSet) {
		fProposalAutoActivationSet = activationSet;
	}

	/*
	 * (non-Javadoc) Method declared on IContentAssistProcessor
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int documentOffset) {
		int contextInformationPosition = guessContextInformationPosition(
				viewer, documentOffset);
		return internalComputeCompletionProposals(viewer, documentOffset,
				contextInformationPosition);
	}

	private int getLastToken(List list, ITextViewer viewer,
			int completionPosition, JavaContext context) {
		// TableName tableName) {
		IDocument document = viewer.getDocument();
		int start = context.getStart();
		// int end = context.getEnd();
		String startText;
		int lastSignificantToken = ITerminalSymbols.TokenNameEOF;
		try {
			// begin search 2 lines behind of this
			int j = start;
			if (j != 0) {
				char ch;
				while (j > 0) {
					ch = document.getChar(--j);
					if (ch == '\n') {
						break;
					}
				}
				while (j > 0) {
					ch = document.getChar(--j);
					if (ch == '\n') {
						break;
					}
				}
			}
			if (j != start) {
				// scan the line for the dereferencing operator '->'
				startText = document.get(j, start - j);
				if (Scanner.DEBUG) {
					System.out.println(startText);
				}
				int token = ITerminalSymbols.TokenNameEOF;
				// token = getLastSQLToken(startText);
				// tableName.setTableName(getLastSQLTableName(startText));
				Scanner scanner = ToolFactory
						.createScanner(false, false, false);
				scanner.setSource(startText.toCharArray());
				scanner.setPHPMode(true);
				int beforeLastToken = ITerminalSymbols.TokenNameEOF;
				int lastToken = ITerminalSymbols.TokenNameEOF;
				char[] ident = null;
				try {
					token = scanner.getNextToken();
					lastToken = token;
					while (token != ITerminalSymbols.TokenNameERROR
							&& token != ITerminalSymbols.TokenNameEOF) {
						beforeLastToken = lastToken;
						if (token == ITerminalSymbols.TokenNameVariable) {
							ident = scanner.getCurrentTokenSource();
							if (ident.length == 5 && ident[0] == '$'
									&& ident[1] == 't' && ident[2] == 'h'
									&& ident[3] == 'i' && ident[4] == 's') {
								token = ITerminalSymbols.TokenNamethis_PHP_COMPLETION;
							}
						}
						lastToken = token;
						// System.out.println(scanner.toStringAction(lastToken));
						token = scanner.getNextToken();
					}
				} catch (InvalidInputException e1) {
				} catch (SyntaxError e) {
				}
				switch (lastToken) {
				case ITerminalSymbols.TokenNameMINUS_GREATER:
					// dereferencing operator '->' found
					lastSignificantToken = ITerminalSymbols.TokenNameMINUS_GREATER;
					if (beforeLastToken == ITerminalSymbols.TokenNameVariable) {
						lastSignificantToken = ITerminalSymbols.TokenNameVariable;
						list.set(0, ident);
					} else if (beforeLastToken == ITerminalSymbols.TokenNamethis_PHP_COMPLETION) {
						lastSignificantToken = ITerminalSymbols.TokenNamethis_PHP_COMPLETION;
						list.set(0, ident);
					}
					break;
				case ITerminalSymbols.TokenNamenew:
					lastSignificantToken = ITerminalSymbols.TokenNamenew;
					break;
				}
			}
		} catch (BadLocationException e) {
		}
		return lastSignificantToken;
	}

	String getSQLTableName(String sqlText, int start) {
		int tableNameStart = -1;
		int currentCharacterPosition = start + 1;
		char ch;
		try {
			while (true) {
				ch = sqlText.charAt(currentCharacterPosition++);
				if (tableNameStart == -1 && Scanner.isPHPIdentifierStart(ch)) {
					tableNameStart = currentCharacterPosition - 1;
				} else {
					if (!Scanner.isPHPIdentifierPart(ch)) {
						return sqlText.substring(tableNameStart,
								currentCharacterPosition - 1);
					}
				}
			}
		} catch (IndexOutOfBoundsException e) {
			if (tableNameStart >= 0) {
				return sqlText.substring(tableNameStart,
						currentCharacterPosition - 1);
			}
		}
		return "";
	}

	// private String getLastSQLTableName(String startText) {
	// // scan for sql identifiers
	// char ch = ' ';
	// int currentSQLPosition = startText.length();
	// int identEnd = -1;
	// String ident = null;
	// try {
	// while (true) {
	// ch = startText.charAt(--currentSQLPosition);
	// if (Scanner.isSQLIdentifierPart(ch)) {
	// // if (ch >= 'A' && ch <= 'Z') {
	// if (identEnd < 0) {
	// identEnd = currentSQLPosition + 1;
	// }
	// // } else if (ch >= 'a' && ch <= 'z') {
	// // if (identEnd < 0) {
	// // identEnd = currentSQLPosition + 1;
	// // }
	// } else if (identEnd >= 0) {
	// ident = startText.substring(currentSQLPosition + 1, identEnd);
	// // select -- from -- where --
	// // update -- set -- where --
	// // insert into -- ( -- ) values ( -- )
	// if (ident.length() >= 4 && ident.length() <= 6) {
	// ident = ident.toLowerCase();
	// switch (ident.length()) {
	// // case 3 :
	// // if (ident.equals("set")) {
	// // // System.out.println("set");
	// // token = ITerminalSymbols.TokenNameSQLset;
	// // return token;
	// // }
	// // break;
	// case 4:
	// if (ident.equals("from")) {
	// // System.out.println("from");
	// return getSQLTableName(startText, identEnd);
	// } else if (ident.equals("into")) {
	// // System.out.println("into");
	// return getSQLTableName(startText, identEnd);
	// }
	// break;
	// case 6:
	// if (ident.equals("update")) {
	// // System.out.println("update");
	// return getSQLTableName(startText, identEnd);
	// }
	// break;
	// }
	// }
	// identEnd = -1;
	// } else if (Character.isWhitespace(ch)) {
	// }
	// }
	// } catch (IndexOutOfBoundsException e) {
	// }
	// return "<!--no-table-->";
	// }

	/**
	 * Detect the last significant SQL token in the text before the completion
	 * 
	 * @param startText
	 */
	// private int getLastSQLToken(String startText) {
	// int token;
	// // scan for sql identifiers
	// char ch = ' ';
	// int currentSQLPosition = startText.length();
	// int identEnd = -1;
	// String ident = null;
	// try {
	// while (true) {
	// ch = startText.charAt(--currentSQLPosition);
	// if (ch >= 'A' && ch <= 'Z') {
	// if (identEnd < 0) {
	// identEnd = currentSQLPosition + 1;
	// }
	// } else if (ch >= 'a' && ch <= 'z') {
	// if (identEnd < 0) {
	// identEnd = currentSQLPosition + 1;
	// }
	// } else if (identEnd >= 0) {
	// ident = startText.substring(currentSQLPosition + 1, identEnd);
	// // select -- from -- where --
	// // update -- set -- where --
	// // insert into -- ( -- ) values ( -- )
	// if (ident.length() >= 3 && ident.length() <= 6) {
	// ident = ident.toLowerCase();
	// switch (ident.length()) {
	// case 3:
	// if (ident.equals("set")) {
	// // System.out.println("set");
	// token = ITerminalSymbols.TokenNameSQLset;
	// return token;
	// }
	// break;
	// case 4:
	// if (ident.equals("from")) {
	// // System.out.println("from");
	// token = ITerminalSymbols.TokenNameSQLfrom;
	// // getSQLTableName();
	// return token;
	// } else if (ident.equals("into")) {
	// // System.out.println("into");
	// token = ITerminalSymbols.TokenNameSQLinto;
	// return token;
	// }
	// break;
	// case 5:
	// if (ident.equals("where")) {
	// // System.out.println("where");
	// token = ITerminalSymbols.TokenNameSQLwhere;
	// return token;
	// }
	// break;
	// case 6:
	// if (ident.equals("select")) {
	// // System.out.println("select");
	// token = ITerminalSymbols.TokenNameSQLselect;
	// return token;
	// } else if (ident.equals("insert")) {
	// // System.out.println("insert");
	// token = ITerminalSymbols.TokenNameSQLinsert;
	// return token;
	// } else if (ident.equals("update")) {
	// // System.out.println("update");
	// token = ITerminalSymbols.TokenNameSQLupdate;
	// return token;
	// } else if (ident.equals("values")) {
	// // System.out.println("values");
	// token = ITerminalSymbols.TokenNameSQLvalues;
	// return token;
	// }
	// break;
	// }
	// }
	// identEnd = -1;
	// }
	// }
	// } catch (IndexOutOfBoundsException e) {
	// }
	// return ITerminalSymbols.TokenNameEOF;
	// }
	private ICompletionProposal[] internalComputeCompletionProposals(
			ITextViewer viewer, int offset, int contextOffset) {
		ICompilationUnit unit = fManager.getWorkingCopy(fEditor
				.getEditorInput());
		IDocument document = viewer.getDocument();
		IFile file = null;
		IProject project = null;
		if (offset > 0) {
			PHPEditor editor = null;
			if (fEditor != null && (fEditor instanceof PHPEditor)) {
				editor = (PHPEditor) fEditor;
				IEditorInput editorInput = editor.getEditorInput();
				if (editorInput instanceof IFileEditorInput) {
					file = ((IFileEditorInput) editorInput).getFile();
					project = file.getProject();
				} else {
					return new ICompletionProposal[0];
				}
			}
		}

		Point selection = viewer.getSelectedRange();
		// remember selected text
		String selectedText = null;
		if (selection.y != 0) {
			try {
				selectedText = document.get(selection.x, selection.y);
			} catch (BadLocationException e) {
			}
		}

		if (offset > 2 && fProposalAutoActivationSet != null) {
			// restrict auto activation for '>' character to '->' token

			try {
				char ch = document.getChar(offset - 1);
				if (ch == '>') {
					for (int i = 0; i < fProposalAutoActivationSet.length; i++) {
						ch = fProposalAutoActivationSet[i];
						if (ch == '>') { // auto activation enabled
							ch = document.getChar(offset - 2);
							if (ch != '-') {
								return new IPHPCompletionProposal[0];
							}
							break;
						}
					}
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}

		JavaContextType phpContextType = (JavaContextType) PHPeclipsePlugin
				.getDefault().getTemplateContextRegistry()
				.getContextType("php"); //$NON-NLS-1$
		JavaContext context = (JavaContext) phpContextType.createContext(
				document, offset, selection.y, unit);
		context.setVariable("selection", selectedText); //$NON-NLS-1$
		String prefix = context.getKey();

		HashMap methodVariables = null;
		// HashMap typeVariables = null;
		HashMap unitVariables = null;
		ICompilationUnit compilationUnit = (ICompilationUnit) context
				.findEnclosingElement(IJavaElement.COMPILATION_UNIT);
		// if (compilationUnit != null) {
		// unitVariables = ((CompilationUnit) compilationUnit).variables;
		// }
		IType type = (IType) context.findEnclosingElement(IJavaElement.TYPE);
		if (type != null) {
			// typeVariables = ((SourceType) type).variables;
		}
		IMethod method = (IMethod) context
				.findEnclosingElement(IJavaElement.METHOD);
		// if (method != null) {
		// methodVariables = ((SourceMethod) method).variables;
		// }

		boolean emptyPrefix = prefix == null || prefix.equals("");
		IPHPCompletionProposal[] localVariableResults = new IPHPCompletionProposal[0];

		if (!emptyPrefix && prefix.length() >= 1 && prefix.charAt(0) == '$') {
			// php Variable ?
			String lowerCasePrefix = prefix.toLowerCase();
			HashSet localVariables = new HashSet();
			if (compilationUnit != null) {
				unitVariables = getUnitVariables(unitVariables, compilationUnit);
				getVariableProposals(localVariables, viewer, project, context,
						unitVariables, lowerCasePrefix, 94);
			}
			if (method != null) {
				methodVariables = getMethodVariables(methodVariables, method);
				getVariableProposals(localVariables, viewer, project, context,
						methodVariables, lowerCasePrefix, 99);
			}
			if (!localVariables.isEmpty()) {
				localVariableResults = (IPHPCompletionProposal[]) localVariables
						.toArray(new IPHPCompletionProposal[localVariables
								.size()]);
			}
		}

		// TableName sqlTable = new TableName();
		ArrayList list = new ArrayList();
		list.add(null);
		int lastSignificantToken = getLastToken(list, viewer, offset, context); // ,
																				// sqlTable);
		boolean useClassMembers = (lastSignificantToken == ITerminalSymbols.TokenNameMINUS_GREATER)
				|| (lastSignificantToken == ITerminalSymbols.TokenNameVariable)
				|| (lastSignificantToken == ITerminalSymbols.TokenNamenew)
				|| (lastSignificantToken == ITerminalSymbols.TokenNamethis_PHP_COMPLETION);

		if (fTemplateEngine != null) {
			IPHPCompletionProposal[] templateResults = new IPHPCompletionProposal[0];
			ICompletionProposal[] results;
			if (!emptyPrefix) {
				fTemplateEngine.reset();
				fTemplateEngine.complete(viewer, offset, unit);
				templateResults = fTemplateEngine.getResults();
			}
			// TODO delete this
			IPHPCompletionProposal[] identifierResults = new IPHPCompletionProposal[0];

			// declarations stored in file project.index on project level
			IPHPCompletionProposal[] declarationResults = new IPHPCompletionProposal[0];
			if (project != null) {
				DeclarationEngine declarationEngine;
				JavaContextType contextType = (JavaContextType) PHPeclipsePlugin
						.getDefault().getTemplateContextRegistry()
						.getContextType("php"); //$NON-NLS-1$
				if (contextType != null) {
					IdentifierIndexManager indexManager = PHPeclipsePlugin
							.getDefault().getIndexManager(project);
					SortedMap sortedMap;
					declarationEngine = new DeclarationEngine(project,
							contextType, lastSignificantToken, file);
					if (lastSignificantToken == ITerminalSymbols.TokenNamethis_PHP_COMPLETION) {
						// complete '$this->'
						sortedMap = indexManager.getIdentifiers(file);
						declarationEngine.completeObject(viewer, offset,
								sortedMap, unit);
					} else {
						String typeRef = null;
						char[] varName = (char[]) list.get(0);
						if (varName != null) {
							if (method != null) {
								methodVariables = getMethodVariables(
										methodVariables, method);
								VariableInfo info = (VariableInfo) methodVariables
										.get(new String(varName));
								if (info != null && info.typeIdentifier != null) {
									typeRef = new String(info.typeIdentifier);
								}
							}
						}
						if (typeRef != null) {
							// complete '$variable->' with type information
							sortedMap = indexManager.getIdentifiers(typeRef);
							declarationEngine.completeObject(viewer, offset,
									sortedMap, unit);
						} else {
							// complete '$variable->' without type information
							sortedMap = indexManager.getIdentifierMap();
							declarationEngine.complete(viewer, offset,
									sortedMap, unit);
						}
					}
					declarationResults = declarationEngine.getResults();
				}
			}
			// built in function names from phpsyntax.xml
			ArrayList syntaxbuffer = PHPSyntaxRdr.getSyntaxData();
			IPHPCompletionProposal[] builtinResults = new IPHPCompletionProposal[0];
			if ((!useClassMembers) && syntaxbuffer != null) {
				BuiltInEngine builtinEngine;
				JavaContextType contextType = (JavaContextType) PHPeclipsePlugin
						.getDefault().getTemplateContextRegistry()
						.getContextType("php"); //$NON-NLS-1$
				if (contextType != null) {
					builtinEngine = new BuiltInEngine(contextType);
					builtinEngine.complete(viewer, offset, syntaxbuffer, unit);
					builtinResults = builtinEngine.getResults();
				}
			}
			// ICompletionProposal[] sqlResults = new ICompletionProposal[0];
			// if (project != null) {
			// sqlResults = getSQLProposals(viewer, project, context, prefix,
			// sqlTable);
			// }
			// concatenate the result arrays
			IPHPCompletionProposal[] total;
			total = new IPHPCompletionProposal[localVariableResults.length
					+ templateResults.length + identifierResults.length
					+ builtinResults.length + declarationResults.length];// +
			// sqlResults.length];
			System.arraycopy(templateResults, 0, total, 0,
					templateResults.length);
			System.arraycopy(identifierResults, 0, total,
					templateResults.length, identifierResults.length);
			System.arraycopy(builtinResults, 0, total, templateResults.length
					+ identifierResults.length, builtinResults.length);
			System.arraycopy(declarationResults, 0, total,
					templateResults.length + identifierResults.length
							+ builtinResults.length, declarationResults.length);
			// System.arraycopy(sqlResults, 0, total, templateResults.length +
			// identifierResults.length + builtinResults.length
			// + declarationResults.length, sqlResults.length);
			// System.arraycopy(localVariableResults, 0, total,
			// templateResults.length
			// + identifierResults.length + builtinResults.length
			// + declarationResults.length + sqlResults.length,
			// localVariableResults.length);
			System
					.arraycopy(localVariableResults, 0, total,
							templateResults.length + identifierResults.length
									+ builtinResults.length
									+ declarationResults.length,
							localVariableResults.length);
			results = total;
			// fNumberOfComputedResults = (results == null ? 0 :
			// results.length);
			/*
			 * Order here and not in result collector to make sure that the
			 * order applies to all proposals and not just those of the
			 * compilation unit.
			 */
			return order(results);
		}
		return new IPHPCompletionProposal[0];
	}

	/**
	 * @param unitVariables
	 * @param unit
	 */
	private HashMap getUnitVariables(HashMap unitVariables,
			ICompilationUnit unit) {
		if (unitVariables == null) {
			try {
				String unitText = unit.getSource();
				unitVariables = new HashMap();

				ProblemReporter problemReporter = new ProblemReporter(
						DefaultErrorHandlingPolicies.exitAfterAllProblems(),
						new CompilerOptions(JavaCore.getOptions()),
						new DefaultProblemFactory());
				UnitParser parser = new UnitParser(problemReporter);
				parser.compilationUnit = new CompilationUnitDeclaration(
						problemReporter, null, unitText.length());
				parser.parse(unitText, unitVariables);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				PHPeclipsePlugin.log(e);
			}
		}
		return unitVariables;
	}

	/**
	 * @param methodVariables
	 * @param method
	 */
	private HashMap getMethodVariables(HashMap methodVariables, IMethod method) {
		if (methodVariables == null) {
			try {
				String methodText = method.getSource();
				methodVariables = new HashMap();
				ProblemReporter problemReporter = new ProblemReporter(
						DefaultErrorHandlingPolicies.exitAfterAllProblems(),
						new CompilerOptions(JavaCore.getOptions()),
						new DefaultProblemFactory());
				UnitParser parser = new UnitParser(problemReporter);
				parser.compilationUnit = new CompilationUnitDeclaration(
						problemReporter, null, methodText.length());
				parser.parseFunction(methodText, methodVariables);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				PHPeclipsePlugin.log(e);
			}
		}
		return methodVariables;
	}

	/**
	 * @param viewer
	 * @param project
	 * @param context
	 * @param prefix
	 * @return
	 */
	private void getVariableProposals(HashSet localVariables,
			ITextViewer viewer, IProject project, JavaContext context,
			HashMap variables, String prefix, int relevance) {
		// try {
		int start = context.getStart();
		int end = context.getEnd();
		IRegion region = new Region(start, end - start);
		// IMethod method = (IMethod)
		// context.findEnclosingElement(IJavaElement.METHOD);
		// if (method != null && (method instanceof SourceMethod) &&
		// ((SourceMethod)
		// method).variables != null) {
		// HashMap map = ((SourceMethod) method).variables;
		Set set = variables.keySet();
		Iterator iter = set.iterator();
		String varName;
		boolean matchesVarName;
		while (iter.hasNext()) {
			varName = (String) iter.next();
			if (varName.length() >= prefix.length()) {
				matchesVarName = true;
				for (int i = 0; i < prefix.length(); i++) {
					if (prefix.charAt(i) != Character.toLowerCase(varName
							.charAt(i))) {
						matchesVarName = false;
						break;
					}
				}
				if (matchesVarName) {
					LocalVariableProposal prop;
					// if (varName.length == prefix.length()) {
					// prop = new LocalVariableProposal(new String(varName),
					// region,
					// viewer, relevance-10);
					// } else {
					prop = new LocalVariableProposal(new String(varName),
							region, viewer, relevance);
					// }
					localVariables.add(prop);
				}
			}
		}
		// }

		// char[] varName;
		// boolean matchesVarName;
		// if (method != null) {
		// ISourceRange range = method.getSourceRange();
		// char[] source = method.getSource().toCharArray();
		// Scanner scanner = new Scanner();
		// scanner.setSource(source);
		// scanner.phpMode = true;
		// int token = Scanner.TokenNameWHITESPACE;
		// while ((token = scanner.getNextToken()) != Scanner.TokenNameEOF) {
		// if (token == Scanner.TokenNameVariable) {
		// varName = scanner.getCurrentTokenSource();
		// if (varName.length >= prefix.length()) {
		// matchesVarName = true;
		// for (int i = 0; i < prefix.length(); i++) {
		// if (prefix.charAt(i) != varName[i]) {
		// matchesVarName = false;
		// break;
		// }
		// }
		// if (matchesVarName) {
		// LocalVariableProposal prop = new LocalVariableProposal(new
		// String(varName), region, viewer);
		// if (varName.length == prefix.length()) {
		// prop.setRelevance(98);
		// }
		// localVariables.add(prop);
		// }
		// }
		// }
		// }
		// }
		// } catch (Throwable e) {
		// // ignore - Syntax exceptions could occur, if there are syntax errors
		// !
		// }
	}

	/**
	 * @param viewer
	 * @param project
	 * @param context
	 * @param prefix
	 * @param sqlTable
	 * @param sqlResults
	 * @return
	 */
	// private ICompletionProposal[] getSQLProposals(ITextViewer viewer,
	// IProject
	// project, DocumentTemplateContext context,
	// String prefix, TableName sqlTable) {
	// ICompletionProposal[] sqlResults = new ICompletionProposal[0];
	// // Get The Database bookmark from the Quantum SQL plugin:
	// // BookmarkCollection sqlBookMarks = BookmarkCollection.getInstance();
	// // if (sqlBookMarks != null) {
	// String bookmarkString =
	// ProjectPrefUtil.getMiscProjectsPreferenceValue(project,
	// IPreferenceConstants.PHP_BOOKMARK_DEFAULT);
	// if (bookmarkString != null && !bookmarkString.equals("")) {
	// String[] bookmarks = ExternalInterface.getBookmarkNames();
	// boolean foundBookmark = false;
	// for (int i = 0; i < bookmarks.length; i++) {
	// if (bookmarks[i].equals(bookmarkString)) {
	// foundBookmark = true;
	// }
	// }
	// if (!foundBookmark) {
	// return sqlResults;
	// }
	// // Bookmark bookmark = sqlBookMarks.find(bookmarkString);
	// ArrayList sqlList = new ArrayList();
	// if (!ExternalInterface.isBookmarkConnected(bookmarkString)) {
	// ExternalInterface.connectBookmark(bookmarkString, null);
	// if (!ExternalInterface.isBookmarkConnected(bookmarkString)) {
	// return sqlResults;
	// }
	// }
	// // if (ExternalInterface.isBookmarkConnected(bookmarkString)) {
	// try {
	// int start = context.getStart();
	// int end = context.getEnd();
	// String foundSQLTableName = sqlTable.getTableName();
	// String tableName;
	// String columnName;
	// String prefixWithoutDollar = prefix;
	// boolean isDollarPrefix = false;
	// if (prefix.length() > 0 && prefix.charAt(0) == '$') {
	// prefixWithoutDollar = prefix.substring(1);
	// isDollarPrefix = true;
	// }
	// IRegion region = new Region(start, end - start);
	// ResultSet set;
	// if (!isDollarPrefix) {
	// String[] tableNames = ExternalInterface.getMatchingTableNames(null,
	// bookmarkString, prefixWithoutDollar, null, false);
	// for (int i = 0; i < tableNames.length; i++) {
	// sqlList.add(new SQLProposal(tableNames[i], context, region, viewer,
	// PHPUiImages.get(PHPUiImages.IMG_TABLE)));
	// }
	// }
	//
	// String[] columnNames = ExternalInterface.getMatchingColumnNames(null,
	// bookmarkString, prefixWithoutDollar, null, false);
	// for (int i = 0; i < columnNames.length; i++) {
	// sqlList.add(new SQLProposal(columnNames[i], context, region, viewer,
	// PHPUiImages.get(PHPUiImages.IMG_TABLE)));
	// }
	//
	// sqlResults = new IPHPCompletionProposal[sqlList.size()];
	// for (int i = 0; i < sqlList.size(); i++) {
	// sqlResults[i] = (SQLProposal) sqlList.get(i);
	// }
	// } catch (Exception /* NotConnectedException */ e) {
	//
	// }
	// // }
	// }
	// // }
	// return sqlResults;
	// }
	private boolean looksLikeMethod(PHPCodeReader reader) throws IOException {
		int curr = reader.read();
		while (curr != PHPCodeReader.EOF && Character.isWhitespace((char) curr))
			curr = reader.read();

		if (curr == PHPCodeReader.EOF)
			return false;

		return Scanner.isPHPIdentifierPart((char) curr);
	}

	private int guessContextInformationPosition(ITextViewer viewer, int offset) {
		int contextPosition = offset;
		IDocument document = viewer.getDocument();
		try {

			PHPCodeReader reader = new PHPCodeReader();
			reader.configureBackwardReader(document, offset, true, true);

			int nestingLevel = 0;

			int curr = reader.read();
			while (curr != PHPCodeReader.EOF) {

				if (')' == (char) curr)
					++nestingLevel;

				else if ('(' == (char) curr) {
					--nestingLevel;

					if (nestingLevel < 0) {
						int start = reader.getOffset();
						if (looksLikeMethod(reader))
							return start + 1;
					}
				}

				curr = reader.read();
			}
		} catch (IOException e) {
		}
		return contextPosition;
	}

	/**
	 * @see IContentAssistProcessor#computeContextInformation(ITextViewer, int)
	 */
	public IContextInformation[] computeContextInformation(ITextViewer viewer,
			int offset) {
		int contextInformationPosition = guessContextInformationPosition(
				viewer, offset);
		List result = addContextInformations(viewer, contextInformationPosition);
		return (IContextInformation[]) result
				.toArray(new IContextInformation[result.size()]);
	}

	private List addContextInformations(ITextViewer viewer, int offset) {
		ICompletionProposal[] proposals = internalComputeCompletionProposals(
				viewer, offset, -1);
		List result = new ArrayList();
		for (int i = 0; i < proposals.length; i++) {
			IContextInformation contextInformation = proposals[i]
					.getContextInformation();
			if (contextInformation != null) {
				ContextInformationWrapper wrapper = new ContextInformationWrapper(
						contextInformation);
				wrapper.setContextInformationPosition(offset);
				result.add(wrapper);
			}
		}
		return result;
	}

	/**
	 * Order the given proposals.
	 */
	private ICompletionProposal[] order(ICompletionProposal[] proposals) {
		Arrays.sort(proposals, fComparator);
		// int len = proposals.length;
		// if (len > 10) {
		// len = 10;
		// }
		// for (int i = 0; i < len; i++) {
		// System.out.println(proposals[i].getDisplayString());
		// }
		return proposals;
	}

	/*
	 * (non-Javadoc) Method declared on IContentAssistProcessor
	 */
	public char[] getCompletionProposalAutoActivationCharacters() {
		return fProposalAutoActivationSet;
		// return null; // new char[] { '$' };
	}

	/*
	 * (non-Javadoc) Method declared on IContentAssistProcessor
	 */
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	/*
	 * (non-Javadoc) Method declared on IContentAssistProcessor
	 */
	public IContextInformationValidator getContextInformationValidator() {
		if (fValidator == null)
			fValidator = new JavaParameterListValidator();
		return fValidator;
	}

	/*
	 * (non-Javadoc) Method declared on IContentAssistProcessor
	 */
	public String getErrorMessage() {
		return null;
	}
}