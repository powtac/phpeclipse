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
package net.sourceforge.phpdt.internal.compiler;

import net.sourceforge.phpdt.core.compiler.IProblem;
import net.sourceforge.phpdt.internal.compiler.ast.AND_AND_Expression;
import net.sourceforge.phpdt.internal.compiler.ast.AllocationExpression;
import net.sourceforge.phpdt.internal.compiler.ast.Argument;
import net.sourceforge.phpdt.internal.compiler.ast.ArrayAllocationExpression;
import net.sourceforge.phpdt.internal.compiler.ast.ArrayInitializer;
import net.sourceforge.phpdt.internal.compiler.ast.ArrayQualifiedTypeReference;
import net.sourceforge.phpdt.internal.compiler.ast.ArrayReference;
import net.sourceforge.phpdt.internal.compiler.ast.ArrayTypeReference;
import net.sourceforge.phpdt.internal.compiler.ast.AssertStatement;
import net.sourceforge.phpdt.internal.compiler.ast.Assignment;
import net.sourceforge.phpdt.internal.compiler.ast.BinaryExpression;
import net.sourceforge.phpdt.internal.compiler.ast.Block;
import net.sourceforge.phpdt.internal.compiler.ast.BreakStatement;
import net.sourceforge.phpdt.internal.compiler.ast.CaseStatement;
import net.sourceforge.phpdt.internal.compiler.ast.CastExpression;
import net.sourceforge.phpdt.internal.compiler.ast.Clinit;
import net.sourceforge.phpdt.internal.compiler.ast.CompilationUnitDeclaration;
import net.sourceforge.phpdt.internal.compiler.ast.CompoundAssignment;
import net.sourceforge.phpdt.internal.compiler.ast.ConditionalExpression;
import net.sourceforge.phpdt.internal.compiler.ast.ConstructorDeclaration;
import net.sourceforge.phpdt.internal.compiler.ast.ContinueStatement;
import net.sourceforge.phpdt.internal.compiler.ast.DoStatement;
import net.sourceforge.phpdt.internal.compiler.ast.DoubleLiteral;
import net.sourceforge.phpdt.internal.compiler.ast.EmptyStatement;
import net.sourceforge.phpdt.internal.compiler.ast.EqualExpression;
import net.sourceforge.phpdt.internal.compiler.ast.ExplicitConstructorCall;
import net.sourceforge.phpdt.internal.compiler.ast.ExtendedStringLiteral;
import net.sourceforge.phpdt.internal.compiler.ast.FalseLiteral;
import net.sourceforge.phpdt.internal.compiler.ast.FieldDeclaration;
import net.sourceforge.phpdt.internal.compiler.ast.FieldReference;
import net.sourceforge.phpdt.internal.compiler.ast.FloatLiteral;
import net.sourceforge.phpdt.internal.compiler.ast.ForStatement;
import net.sourceforge.phpdt.internal.compiler.ast.IfStatement;
import net.sourceforge.phpdt.internal.compiler.ast.ImportReference;
import net.sourceforge.phpdt.internal.compiler.ast.Initializer;
import net.sourceforge.phpdt.internal.compiler.ast.InstanceOfExpression;
import net.sourceforge.phpdt.internal.compiler.ast.IntLiteral;
import net.sourceforge.phpdt.internal.compiler.ast.LabeledStatement;
import net.sourceforge.phpdt.internal.compiler.ast.LocalDeclaration;
import net.sourceforge.phpdt.internal.compiler.ast.LongLiteral;
import net.sourceforge.phpdt.internal.compiler.ast.MessageSend;
import net.sourceforge.phpdt.internal.compiler.ast.MethodDeclaration;
import net.sourceforge.phpdt.internal.compiler.ast.NullLiteral;
import net.sourceforge.phpdt.internal.compiler.ast.OR_OR_Expression;
import net.sourceforge.phpdt.internal.compiler.ast.PostfixExpression;
import net.sourceforge.phpdt.internal.compiler.ast.PrefixExpression;
import net.sourceforge.phpdt.internal.compiler.ast.QualifiedAllocationExpression;
import net.sourceforge.phpdt.internal.compiler.ast.QualifiedNameReference;
import net.sourceforge.phpdt.internal.compiler.ast.QualifiedSuperReference;
import net.sourceforge.phpdt.internal.compiler.ast.QualifiedThisReference;
import net.sourceforge.phpdt.internal.compiler.ast.QualifiedTypeReference;
import net.sourceforge.phpdt.internal.compiler.ast.ReturnStatement;
import net.sourceforge.phpdt.internal.compiler.ast.SingleNameReference;
import net.sourceforge.phpdt.internal.compiler.ast.SingleTypeReference;
import net.sourceforge.phpdt.internal.compiler.ast.StringLiteral;
import net.sourceforge.phpdt.internal.compiler.ast.SuperReference;
import net.sourceforge.phpdt.internal.compiler.ast.SwitchStatement;
import net.sourceforge.phpdt.internal.compiler.ast.ThisReference;
import net.sourceforge.phpdt.internal.compiler.ast.ThrowStatement;
import net.sourceforge.phpdt.internal.compiler.ast.TrueLiteral;
import net.sourceforge.phpdt.internal.compiler.ast.TryStatement;
import net.sourceforge.phpdt.internal.compiler.ast.TypeDeclaration;
import net.sourceforge.phpdt.internal.compiler.ast.UnaryExpression;
import net.sourceforge.phpdt.internal.compiler.ast.WhileStatement;
import net.sourceforge.phpdt.internal.compiler.lookup.BlockScope;
import net.sourceforge.phpdt.internal.compiler.lookup.ClassScope;
import net.sourceforge.phpdt.internal.compiler.lookup.CompilationUnitScope;
import net.sourceforge.phpdt.internal.compiler.lookup.MethodScope;

/**
 * A visitor for iterating through the parse tree.
 */
public abstract class ASTVisitor {

	public void acceptProblem(IProblem problem) {
		// do nothing by default
	}

	public void endVisit(AllocationExpression allocationExpression,
			BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(AND_AND_Expression and_and_Expression, BlockScope scope) {
		// do nothing by default
	}

	// public void endVisit(JavadocArrayQualifiedTypeReference typeRef,
	// BlockScope scope) {
	// // do nothing by default
	// }
	// public void endVisit(JavadocArraySingleTypeReference typeRef, BlockScope
	// scope) {
	// // do nothing by default
	// }
	// public void endVisit(JavadocArgumentExpression expression, BlockScope
	// scope) {
	// // do nothing by default
	// }
	// public void endVisit(JavadocFieldReference fieldRef, BlockScope scope) {
	// // do nothing by default
	// }
	// public void endVisit(JavadocMessageSend messageSend, BlockScope scope) {
	// // do nothing by default
	// }
	// public void endVisit(JavadocQualifiedTypeReference typeRef, BlockScope
	// scope) {
	// // do nothing by default
	// }
	// public void endVisit(JavadocReturnStatement statement, BlockScope scope)
	// {
	// // do nothing by default
	// }
	// public void endVisit(JavadocSingleNameReference argument, BlockScope
	// scope) {
	// // do nothing by default
	// }
	// public void endVisit(JavadocSingleTypeReference typeRef, BlockScope
	// scope) {
	// // do nothing by default
	// }
	public void endVisit(Argument argument, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(ArrayAllocationExpression arrayAllocationExpression,
			BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(ArrayInitializer arrayInitializer, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(
			ArrayQualifiedTypeReference arrayQualifiedTypeReference,
			BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(
			ArrayQualifiedTypeReference arrayQualifiedTypeReference,
			ClassScope scope) {
		// do nothing by default
	}

	public void endVisit(ArrayReference arrayReference, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(ArrayTypeReference arrayTypeReference, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(ArrayTypeReference arrayTypeReference, ClassScope scope) {
		// do nothing by default
	}

	public void endVisit(Assignment assignment, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(AssertStatement assertStatement, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(BinaryExpression binaryExpression, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(Block block, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(BreakStatement breakStatement, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(CaseStatement caseStatement, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(CastExpression castExpression, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(Clinit clinit, ClassScope scope) {
		// do nothing by default
	}

	public void endVisit(CompilationUnitDeclaration compilationUnitDeclaration,
			CompilationUnitScope scope) {
		// do nothing by default
	}

	public void endVisit(CompoundAssignment compoundAssignment, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(ConditionalExpression conditionalExpression,
			BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(ConstructorDeclaration constructorDeclaration,
			ClassScope scope) {
		// do nothing by default
	}

	public void endVisit(ContinueStatement continueStatement, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(DoStatement doStatement, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(DoubleLiteral doubleLiteral, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(EqualExpression equalExpression, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(ExplicitConstructorCall explicitConstructor,
			BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(ExtendedStringLiteral extendedStringLiteral,
			BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(FalseLiteral falseLiteral, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(FieldDeclaration fieldDeclaration, MethodScope scope) {
		// do nothing by default
	}

	public void endVisit(FieldReference fieldReference, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(FloatLiteral floatLiteral, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(EmptyStatement emptyStatement, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(ForStatement forStatement, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(IfStatement ifStatement, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(ImportReference importRef, CompilationUnitScope scope) {
		// do nothing by default
	}

	public void endVisit(Initializer initializer, MethodScope scope) {
		// do nothing by default
	}

	public void endVisit(InstanceOfExpression instanceOfExpression,
			BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(IntLiteral intLiteral, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(LabeledStatement labeledStatement, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(LocalDeclaration localDeclaration, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(LongLiteral longLiteral, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(MessageSend messageSend, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(MethodDeclaration methodDeclaration, ClassScope scope) {
		// do nothing by default
	}

	// public void endVisit(StringLiteralConcatenation literal, BlockScope
	// scope) {
	// // do nothing by default
	// }
	public void endVisit(NullLiteral nullLiteral, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(OR_OR_Expression or_or_Expression, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(PostfixExpression postfixExpression, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(PrefixExpression prefixExpression, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(
			QualifiedAllocationExpression qualifiedAllocationExpression,
			BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(QualifiedNameReference qualifiedNameReference,
			BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(QualifiedSuperReference qualifiedSuperReference,
			BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(QualifiedThisReference qualifiedThisReference,
			BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(QualifiedTypeReference qualifiedTypeReference,
			BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(QualifiedTypeReference qualifiedTypeReference,
			ClassScope scope) {
		// do nothing by default
	}

	public void endVisit(ReturnStatement returnStatement, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(SingleNameReference singleNameReference,
			BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(SingleTypeReference singleTypeReference,
			BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(SingleTypeReference singleTypeReference,
			ClassScope scope) {
		// do nothing by default
	}

	public void endVisit(StringLiteral stringLiteral, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(SuperReference superReference, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(SwitchStatement switchStatement, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(ThisReference thisReference, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(ThrowStatement throwStatement, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(TrueLiteral trueLiteral, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(TryStatement tryStatement, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(TypeDeclaration localTypeDeclaration, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(TypeDeclaration memberTypeDeclaration, ClassScope scope) {
		// do nothing by default
	}

	public void endVisit(TypeDeclaration typeDeclaration,
			CompilationUnitScope scope) {
		// do nothing by default
	}

	public void endVisit(UnaryExpression unaryExpression, BlockScope scope) {
		// do nothing by default
	}

	public void endVisit(WhileStatement whileStatement, BlockScope scope) {
		// do nothing by default
	}

	public boolean visit(AllocationExpression allocationExpression,
			BlockScope scope) {
		return true; // do nothing by default, keep traversing
		// do nothing by default
	}

	public boolean visit(AND_AND_Expression and_and_Expression, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	// public boolean visit(JavadocArrayQualifiedTypeReference typeRef,
	// BlockScope scope) {
	// return true; // do nothing by default, keep traversing
	// }
	// public boolean visit(JavadocArraySingleTypeReference typeRef, BlockScope
	// scope) {
	// return true; // do nothing by default, keep traversing
	// }
	// public boolean visit(JavadocArgumentExpression expression, BlockScope
	// scope) {
	// return true; // do nothing by default, keep traversing
	// }
	// public boolean visit(JavadocFieldReference fieldRef, BlockScope scope) {
	// return true; // do nothing by default, keep traversing
	// }
	// public boolean visit(JavadocMessageSend messageSend, BlockScope scope) {
	// return true; // do nothing by default, keep traversing
	// }
	// public boolean visit(JavadocQualifiedTypeReference typeRef, BlockScope
	// scope) {
	// return true; // do nothing by default, keep traversing
	// }
	// public boolean visit(JavadocReturnStatement statement, BlockScope scope)
	// {
	// return true; // do nothing by default, keep traversing
	// }
	// public boolean visit(JavadocSingleNameReference argument, BlockScope
	// scope) {
	// return true; // do nothing by default, keep traversing
	// }
	// public boolean visit(JavadocSingleTypeReference typeRef, BlockScope
	// scope) {
	// return true; // do nothing by default, keep traversing
	// }
	public boolean visit(Argument argument, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(ArrayAllocationExpression arrayAllocationExpression,
			BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(ArrayInitializer arrayInitializer, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(
			ArrayQualifiedTypeReference arrayQualifiedTypeReference,
			BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(
			ArrayQualifiedTypeReference arrayQualifiedTypeReference,
			ClassScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(ArrayReference arrayReference, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(ArrayTypeReference arrayTypeReference, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(ArrayTypeReference arrayTypeReference, ClassScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(Assignment assignment, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(AssertStatement assertStatement, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(BinaryExpression binaryExpression, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(Block block, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(BreakStatement breakStatement, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(CaseStatement caseStatement, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(CastExpression castExpression, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(Clinit clinit, ClassScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(CompilationUnitDeclaration compilationUnitDeclaration,
			CompilationUnitScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(CompoundAssignment compoundAssignment, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(ConditionalExpression conditionalExpression,
			BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(ConstructorDeclaration constructorDeclaration,
			ClassScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(ContinueStatement continueStatement, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(DoStatement doStatement, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(DoubleLiteral doubleLiteral, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(EqualExpression equalExpression, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(EmptyStatement emptyStatement, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(ExplicitConstructorCall explicitConstructor,
			BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(ExtendedStringLiteral extendedStringLiteral,
			BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(FalseLiteral falseLiteral, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(FieldReference fieldReference, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(FloatLiteral floatLiteral, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(ForStatement forStatement, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(IfStatement ifStatement, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(ImportReference importRef, CompilationUnitScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(Initializer initializer, MethodScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(InstanceOfExpression instanceOfExpression,
			BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(IntLiteral intLiteral, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(LabeledStatement labeledStatement, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(LocalDeclaration localDeclaration, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(LongLiteral longLiteral, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(MessageSend messageSend, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
		return true; // do nothing by default, keep traversing
	}

	// public boolean visit(
	// StringLiteralConcatenation literal,
	// BlockScope scope) {
	// return true; // do nothing by default, keep traversing
	// }
	public boolean visit(NullLiteral nullLiteral, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(OR_OR_Expression or_or_Expression, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(PostfixExpression postfixExpression, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(PrefixExpression prefixExpression, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(
			QualifiedAllocationExpression qualifiedAllocationExpression,
			BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(QualifiedNameReference qualifiedNameReference,
			BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(QualifiedSuperReference qualifiedSuperReference,
			BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(QualifiedThisReference qualifiedThisReference,
			BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(QualifiedTypeReference qualifiedTypeReference,
			BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(QualifiedTypeReference qualifiedTypeReference,
			ClassScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(ReturnStatement returnStatement, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(SingleNameReference singleNameReference,
			BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(SingleTypeReference singleTypeReference,
			BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(SingleTypeReference singleTypeReference,
			ClassScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(StringLiteral stringLiteral, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(SuperReference superReference, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(SwitchStatement switchStatement, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(ThisReference thisReference, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(ThrowStatement throwStatement, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(TrueLiteral trueLiteral, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(TryStatement tryStatement, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(TypeDeclaration localTypeDeclaration, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(TypeDeclaration memberTypeDeclaration, ClassScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(TypeDeclaration typeDeclaration,
			CompilationUnitScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(UnaryExpression unaryExpression, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}

	public boolean visit(WhileStatement whileStatement, BlockScope scope) {
		return true; // do nothing by default, keep traversing
	}
}
