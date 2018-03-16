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
package net.sourceforge.phpdt.internal.compiler;

import net.sourceforge.phpdt.core.compiler.IProblem;
import net.sourceforge.phpdt.internal.compiler.ast.AND_AND_Expression;
import net.sourceforge.phpdt.internal.compiler.ast.AllocationExpression;
import net.sourceforge.phpdt.internal.compiler.ast.AnonymousLocalTypeDeclaration;
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
import net.sourceforge.phpdt.internal.compiler.ast.DefaultCase;
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
import net.sourceforge.phpdt.internal.compiler.ast.LocalTypeDeclaration;
import net.sourceforge.phpdt.internal.compiler.ast.LongLiteral;
import net.sourceforge.phpdt.internal.compiler.ast.MemberTypeDeclaration;
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
 * A visitor interface for interating through the parse tree.
 */
public interface IAbstractSyntaxTreeVisitor {
	void acceptProblem(IProblem problem);

	void endVisit(AllocationExpression allocationExpression, BlockScope scope);

	void endVisit(AND_AND_Expression and_and_Expression, BlockScope scope);

	void endVisit(AnonymousLocalTypeDeclaration anonymousTypeDeclaration,
			BlockScope scope);

	void endVisit(Argument argument, BlockScope scope);

	void endVisit(ArrayAllocationExpression arrayAllocationExpression,
			BlockScope scope);

	void endVisit(ArrayInitializer arrayInitializer, BlockScope scope);

	void endVisit(ArrayQualifiedTypeReference arrayQualifiedTypeReference,
			BlockScope scope);

	void endVisit(ArrayQualifiedTypeReference arrayQualifiedTypeReference,
			ClassScope scope);

	void endVisit(ArrayReference arrayReference, BlockScope scope);

	void endVisit(ArrayTypeReference arrayTypeReference, BlockScope scope);

	void endVisit(ArrayTypeReference arrayTypeReference, ClassScope scope);

	void endVisit(AssertStatement assertStatement, BlockScope scope);

	void endVisit(Assignment assignment, BlockScope scope);

	void endVisit(BinaryExpression binaryExpression, BlockScope scope);

	void endVisit(Block block, BlockScope scope);

	void endVisit(BreakStatement breakStatement, BlockScope scope);

	void endVisit(CaseStatement caseStatement, BlockScope scope);

	void endVisit(CastExpression castExpression, BlockScope scope);

	// void endVisit(CharLiteral charLiteral, BlockScope scope);
	// void endVisit(ClassLiteralAccess classLiteral, BlockScope scope);
	void endVisit(Clinit clinit, ClassScope scope);

	void endVisit(CompilationUnitDeclaration compilationUnitDeclaration,
			CompilationUnitScope scope);

	void endVisit(CompoundAssignment compoundAssignment, BlockScope scope);

	void endVisit(ConditionalExpression conditionalExpression, BlockScope scope);

	void endVisit(ConstructorDeclaration constructorDeclaration,
			ClassScope scope);

	void endVisit(ContinueStatement continueStatement, BlockScope scope);

	void endVisit(DefaultCase defaultCaseStatement, BlockScope scope);

	void endVisit(DoStatement doStatement, BlockScope scope);

	void endVisit(DoubleLiteral doubleLiteral, BlockScope scope);

	void endVisit(EqualExpression equalExpression, BlockScope scope);

	void endVisit(EmptyStatement statement, BlockScope scope);

	void endVisit(ExplicitConstructorCall explicitConstructor, BlockScope scope);

	void endVisit(ExtendedStringLiteral extendedStringLiteral, BlockScope scope);

	void endVisit(FalseLiteral falseLiteral, BlockScope scope);

	void endVisit(FieldDeclaration fieldDeclaration, MethodScope scope);

	void endVisit(FieldReference fieldReference, BlockScope scope);

	void endVisit(FloatLiteral floatLiteral, BlockScope scope);

	void endVisit(ForStatement forStatement, BlockScope scope);

	void endVisit(IfStatement ifStatement, BlockScope scope);

	void endVisit(ImportReference importRef, CompilationUnitScope scope);

	void endVisit(Initializer initializer, MethodScope scope);

	void endVisit(InstanceOfExpression instanceOfExpression, BlockScope scope);

	void endVisit(IntLiteral intLiteral, BlockScope scope);

	void endVisit(LabeledStatement labeledStatement, BlockScope scope);

	void endVisit(LocalDeclaration localDeclaration, BlockScope scope);

	void endVisit(LocalTypeDeclaration localTypeDeclaration, BlockScope scope);

	void endVisit(LongLiteral longLiteral, BlockScope scope);

	void endVisit(MemberTypeDeclaration memberTypeDeclaration, ClassScope scope);

	void endVisit(MessageSend messageSend, BlockScope scope);

	void endVisit(MethodDeclaration methodDeclaration, ClassScope scope);

	void endVisit(NullLiteral nullLiteral, BlockScope scope);

	void endVisit(OR_OR_Expression or_or_Expression, BlockScope scope);

	void endVisit(PostfixExpression postfixExpression, BlockScope scope);

	void endVisit(PrefixExpression prefixExpression, BlockScope scope);

	void endVisit(QualifiedAllocationExpression qualifiedAllocationExpression,
			BlockScope scope);

	void endVisit(QualifiedNameReference qualifiedNameReference,
			BlockScope scope);

	void endVisit(QualifiedSuperReference qualifiedSuperReference,
			BlockScope scope);

	void endVisit(QualifiedThisReference qualifiedThisReference,
			BlockScope scope);

	void endVisit(QualifiedTypeReference qualifiedTypeReference,
			BlockScope scope);

	void endVisit(QualifiedTypeReference qualifiedTypeReference,
			ClassScope scope);

	void endVisit(ReturnStatement returnStatement, BlockScope scope);

	void endVisit(SingleNameReference singleNameReference, BlockScope scope);

	void endVisit(SingleTypeReference singleTypeReference, BlockScope scope);

	void endVisit(SingleTypeReference singleTypeReference, ClassScope scope);

	void endVisit(StringLiteral stringLiteral, BlockScope scope);

	void endVisit(SuperReference superReference, BlockScope scope);

	void endVisit(SwitchStatement switchStatement, BlockScope scope);

	// void endVisit(SynchronizedStatement synchronizedStatement, BlockScope
	// scope);
	void endVisit(ThisReference thisReference, BlockScope scope);

	void endVisit(ThrowStatement throwStatement, BlockScope scope);

	void endVisit(TrueLiteral trueLiteral, BlockScope scope);

	void endVisit(TryStatement tryStatement, BlockScope scope);

	void endVisit(TypeDeclaration typeDeclaration, CompilationUnitScope scope);

	void endVisit(UnaryExpression unaryExpression, BlockScope scope);

	void endVisit(WhileStatement whileStatement, BlockScope scope);

	boolean visit(AllocationExpression allocationExpression, BlockScope scope);

	boolean visit(AND_AND_Expression and_and_Expression, BlockScope scope);

	boolean visit(AnonymousLocalTypeDeclaration anonymousTypeDeclaration,
			BlockScope scope);

	boolean visit(Argument argument, BlockScope scope);

	boolean visit(ArrayAllocationExpression arrayAllocationExpression,
			BlockScope scope);

	boolean visit(ArrayInitializer arrayInitializer, BlockScope scope);

	boolean visit(ArrayQualifiedTypeReference arrayQualifiedTypeReference,
			BlockScope scope);

	boolean visit(ArrayQualifiedTypeReference arrayQualifiedTypeReference,
			ClassScope scope);

	boolean visit(ArrayReference arrayReference, BlockScope scope);

	boolean visit(ArrayTypeReference arrayTypeReference, BlockScope scope);

	boolean visit(ArrayTypeReference arrayTypeReference, ClassScope scope);

	boolean visit(AssertStatement assertStatement, BlockScope scope);

	boolean visit(Assignment assignment, BlockScope scope);

	boolean visit(BinaryExpression binaryExpression, BlockScope scope);

	boolean visit(Block block, BlockScope scope);

	boolean visit(BreakStatement breakStatement, BlockScope scope);

	boolean visit(CaseStatement caseStatement, BlockScope scope);

	boolean visit(CastExpression castExpression, BlockScope scope);

	// boolean visit(CharLiteral charLiteral, BlockScope scope);
	// boolean visit(ClassLiteralAccess classLiteral, BlockScope scope);
	boolean visit(Clinit clinit, ClassScope scope);

	boolean visit(CompilationUnitDeclaration compilationUnitDeclaration,
			CompilationUnitScope scope);

	boolean visit(CompoundAssignment compoundAssignment, BlockScope scope);

	boolean visit(ConditionalExpression conditionalExpression, BlockScope scope);

	boolean visit(ConstructorDeclaration constructorDeclaration,
			ClassScope scope);

	boolean visit(ContinueStatement continueStatement, BlockScope scope);

	boolean visit(DefaultCase defaultCaseStatement, BlockScope scope);

	boolean visit(DoStatement doStatement, BlockScope scope);

	boolean visit(DoubleLiteral doubleLiteral, BlockScope scope);

	boolean visit(EqualExpression equalExpression, BlockScope scope);

	boolean visit(EmptyStatement statement, BlockScope scope);

	boolean visit(ExplicitConstructorCall explicitConstructor, BlockScope scope);

	boolean visit(ExtendedStringLiteral extendedStringLiteral, BlockScope scope);

	boolean visit(FalseLiteral falseLiteral, BlockScope scope);

	boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope);

	boolean visit(FieldReference fieldReference, BlockScope scope);

	boolean visit(FloatLiteral floatLiteral, BlockScope scope);

	boolean visit(ForStatement forStatement, BlockScope scope);

	boolean visit(IfStatement ifStatement, BlockScope scope);

	boolean visit(ImportReference importRef, CompilationUnitScope scope);

	boolean visit(Initializer initializer, MethodScope scope);

	boolean visit(InstanceOfExpression instanceOfExpression, BlockScope scope);

	boolean visit(IntLiteral intLiteral, BlockScope scope);

	boolean visit(LabeledStatement labeledStatement, BlockScope scope);

	boolean visit(LocalDeclaration localDeclaration, BlockScope scope);

	boolean visit(LocalTypeDeclaration localTypeDeclaration, BlockScope scope);

	boolean visit(LongLiteral longLiteral, BlockScope scope);

	boolean visit(MemberTypeDeclaration memberTypeDeclaration, ClassScope scope);

	boolean visit(MessageSend messageSend, BlockScope scope);

	boolean visit(MethodDeclaration methodDeclaration, ClassScope scope);

	boolean visit(NullLiteral nullLiteral, BlockScope scope);

	boolean visit(OR_OR_Expression or_or_Expression, BlockScope scope);

	boolean visit(PostfixExpression postfixExpression, BlockScope scope);

	boolean visit(PrefixExpression prefixExpression, BlockScope scope);

	boolean visit(QualifiedAllocationExpression qualifiedAllocationExpression,
			BlockScope scope);

	boolean visit(QualifiedNameReference qualifiedNameReference,
			BlockScope scope);

	boolean visit(QualifiedSuperReference qualifiedSuperReference,
			BlockScope scope);

	boolean visit(QualifiedThisReference qualifiedThisReference,
			BlockScope scope);

	boolean visit(QualifiedTypeReference qualifiedTypeReference,
			BlockScope scope);

	boolean visit(QualifiedTypeReference qualifiedTypeReference,
			ClassScope scope);

	boolean visit(ReturnStatement returnStatement, BlockScope scope);

	boolean visit(SingleNameReference singleNameReference, BlockScope scope);

	boolean visit(SingleTypeReference singleTypeReference, BlockScope scope);

	boolean visit(SingleTypeReference singleTypeReference, ClassScope scope);

	boolean visit(StringLiteral stringLiteral, BlockScope scope);

	boolean visit(SuperReference superReference, BlockScope scope);

	boolean visit(SwitchStatement switchStatement, BlockScope scope);

	// boolean visit(SynchronizedStatement synchronizedStatement, BlockScope
	// scope);
	boolean visit(ThisReference thisReference, BlockScope scope);

	boolean visit(ThrowStatement throwStatement, BlockScope scope);

	boolean visit(TrueLiteral trueLiteral, BlockScope scope);

	boolean visit(TryStatement tryStatement, BlockScope scope);

	boolean visit(TypeDeclaration typeDeclaration, CompilationUnitScope scope);

	boolean visit(UnaryExpression unaryExpression, BlockScope scope);

	boolean visit(WhileStatement whileStatement, BlockScope scope);
}
