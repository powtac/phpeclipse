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
package net.sourceforge.phpdt.internal.compiler.lookup;

import java.util.ArrayList;

import net.sourceforge.phpdt.core.compiler.CharOperation;
import net.sourceforge.phpdt.internal.compiler.ast.CompilationUnitDeclaration;
import net.sourceforge.phpdt.internal.compiler.ast.ImportReference;
import net.sourceforge.phpdt.internal.compiler.ast.TypeDeclaration;
import net.sourceforge.phpdt.internal.compiler.problem.ProblemReporter;
import net.sourceforge.phpdt.internal.compiler.util.CompoundNameVector;
import net.sourceforge.phpdt.internal.compiler.util.HashtableOfObject;
import net.sourceforge.phpdt.internal.compiler.util.HashtableOfType;
import net.sourceforge.phpdt.internal.compiler.util.ObjectVector;
import net.sourceforge.phpdt.internal.compiler.util.SimpleNameVector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;

public class CompilationUnitScope extends Scope {

	public LookupEnvironment environment;

	public CompilationUnitDeclaration referenceContext;

	public char[][] currentPackageName;

	public PackageBinding fPackage;

	public ImportBinding[] imports;

	public SourceTypeBinding[] topLevelTypes;

	private CompoundNameVector qualifiedReferences;

	private SimpleNameVector simpleNameReferences;

	private ObjectVector referencedTypes;

	HashtableOfType constantPoolNameUsage;

	public HashtableOfObject resolvedSingeTypeImports;

	public CompilationUnitScope(CompilationUnitDeclaration unit,
			LookupEnvironment environment) {
		super(COMPILATION_UNIT_SCOPE, null);
		this.environment = environment;
		this.referenceContext = unit;
		unit.scope = this;
		// this.currentPackageName = unit.currentPackage == null ?
		// CharOperation.NO_CHAR_CHAR : unit.currentPackage.tokens;
		this.currentPackageName = null;
		// if (environment.options.produceReferenceInfo) {
		// this.qualifiedReferences = new CompoundNameVector();
		// this.simpleNameReferences = new SimpleNameVector();
		// this.referencedTypes = new ObjectVector();
		// } else {
		this.qualifiedReferences = null; // used to test if dependencies
											// should be recorded
		this.simpleNameReferences = null;
		this.referencedTypes = null;
		// }
	}

	void buildFieldsAndMethods() {
		for (int i = 0, length = topLevelTypes.length; i < length; i++)
			topLevelTypes[i].scope.buildFieldsAndMethods();
	}

	void buildTypeBindings() {
		if (referenceContext.compilationResult.compilationUnit != null) {
			char[][] expectedPackageName = referenceContext.compilationResult.compilationUnit
					.getPackageName();
			if (expectedPackageName != null
					&& !CharOperation.equals(currentPackageName,
							expectedPackageName)) {

				// only report if the unit isn't structurally empty
				// if (referenceContext.currentPackage != null
				// || referenceContext.types != null
				// || referenceContext.imports != null) {
				// problemReporter().packageIsNotExpectedPackage(referenceContext);
				// }
				currentPackageName = expectedPackageName.length == 0 ? CharOperation.NO_CHAR_CHAR
						: expectedPackageName;
			}
		}
		if (currentPackageName == CharOperation.NO_CHAR_CHAR) {
			if ((fPackage = environment.defaultPackage) == null) {
				problemReporter().mustSpecifyPackage(referenceContext);
				return;
			}
		} else {
			if ((fPackage = environment.createPackage(currentPackageName)) == null) {
				// problemReporter().packageCollidesWithType(referenceContext);
				return;
			}
			recordQualifiedReference(currentPackageName); // always dependent
															// on your own
															// package
		}

		// Skip typeDeclarations which know of previously reported errors
		ArrayList types = referenceContext.types;
		int typeLength = (types == null) ? 0 : types.size();
		topLevelTypes = new SourceTypeBinding[typeLength];
		int count = 0;
		nextType: for (int i = 0; i < typeLength; i++) {
			if (types.get(i) instanceof TypeDeclaration) {
				TypeDeclaration typeDecl = (TypeDeclaration) types.get(i);
				ReferenceBinding typeBinding = fPackage.getType0(typeDecl.name);
				recordSimpleReference(typeDecl.name); // needed to detect
														// collision cases
				if (typeBinding != null
						&& !(typeBinding instanceof UnresolvedReferenceBinding)) {
					// if a type exists, it must be a valid type - cannot be a
					// NotFound problem type
					// unless its an unresolved type which is now being defined
					problemReporter()
							.duplicateTypes(referenceContext, typeDecl);
					continue nextType;
				}
				if (fPackage != environment.defaultPackage
						&& fPackage.getPackage(typeDecl.name) != null) {
					// if a package exists, it must be a valid package - cannot
					// be a NotFound problem package
					problemReporter().typeCollidesWithPackage(referenceContext,
							typeDecl);
					continue nextType;
				}

				if ((typeDecl.modifiers & AccPublic) != 0) {
					char[] mainTypeName;
					if ((mainTypeName = referenceContext.getMainTypeName()) != null
							// mainTypeName == null means that implementor of
							// ICompilationUnit decided to return null
							&& !CharOperation.equals(mainTypeName,
									typeDecl.name)) {
						problemReporter().publicClassMustMatchFileName(
								referenceContext, typeDecl);
						continue nextType;
					}
				}

				ClassScope child = new ClassScope(this, typeDecl);
				SourceTypeBinding type = child.buildType(null, fPackage);
				if (type != null) {
					topLevelTypes[count++] = type;
				}
			}
		}

		// shrink topLevelTypes... only happens if an error was reported
		if (count != topLevelTypes.length)
			System.arraycopy(topLevelTypes, 0,
					topLevelTypes = new SourceTypeBinding[count], 0, count);
	}

	void checkAndSetImports() {
		// initialize the default imports if necessary... share the default
		// java.lang.* import
		if (environment.defaultImports == null) {
			Binding importBinding = environment.getTopLevelPackage(JAVA);
			if (importBinding != null)
				importBinding = ((PackageBinding) importBinding)
						.getTypeOrPackage(JAVA_LANG[1]);

			// abort if java.lang cannot be found...
			if (importBinding == null || !importBinding.isValidBinding())
				problemReporter().isClassPathCorrect(JAVA_LANG_OBJECT,
						referenceCompilationUnit());

			environment.defaultImports = new ImportBinding[] { new ImportBinding(
					JAVA_LANG, true, importBinding, null) };
		}
		if (referenceContext.imports == null) {
			imports = environment.defaultImports;
			return;
		}

		// allocate the import array, add java.lang.* by default
		int numberOfStatements = referenceContext.imports.length;
		// int numberOfImports = numberOfStatements + 1;
		int numberOfImports = numberOfStatements;
		// for (int i = 0; i < numberOfStatements; i++) {
		// ImportReference importReference = referenceContext.imports[i];
		// if (importReference.onDemand && CharOperation.equals(JAVA_LANG,
		// importReference.tokens)) {
		// numberOfImports--;
		// break;
		// }
		// }
		ImportBinding[] resolvedImports = new ImportBinding[numberOfImports];
		resolvedImports[0] = environment.defaultImports[0];
		int index = 1;

		nextImport: for (int i = 0; i < numberOfStatements; i++) {
			ImportReference importReference = referenceContext.imports[i];
			IFile file = importReference.getFile();
			SourceTypeBinding typeBinding;
			// char[][] compoundName = importReference.tokens;
			char[][] compoundName = null;
			if (file != null) {
				IPath path = file.getProjectRelativePath();
				String[] segs = path.segments();
				compoundName = new char[segs.length][];
				for (int j = 0; j < segs.length; j++) {
					compoundName[j] = segs[j].toCharArray();
				}
			}
			if (compoundName == null) {
				continue nextImport;
			}

			// skip duplicates or imports of the current package
			for (int j = 0; j < index; j++)
				if (resolvedImports[j].onDemand == importReference.onDemand)
					if (CharOperation.equals(compoundName,
							resolvedImports[j].compoundName))
						continue nextImport;
			if (importReference.onDemand == true)
				if (CharOperation.equals(compoundName, currentPackageName))
					continue nextImport;

			if (importReference.onDemand) {
				Binding importBinding = findOnDemandImport(compoundName);
				if (!importBinding.isValidBinding())
					continue nextImport; // we report all problems in
											// faultInImports()
				resolvedImports[index++] = new ImportBinding(compoundName,
						true, importBinding, importReference);
			} else {
				resolvedImports[index++] = new ImportBinding(compoundName,
						false, null, importReference);
			}
		}

		// shrink resolvedImports... only happens if an error was reported
		if (resolvedImports.length > index)
			System.arraycopy(resolvedImports, 0,
					resolvedImports = new ImportBinding[index], 0, index);
		imports = resolvedImports;
	}

	/*
	 * INTERNAL USE-ONLY Innerclasses get their name computed as they are
	 * generated, since some may not be actually outputed if sitting inside
	 * unreachable code.
	 */
	public char[] computeConstantPoolName(LocalTypeBinding localType) {
		if (localType.constantPoolName() != null) {
			return localType.constantPoolName();
		}
		// delegates to the outermost enclosing classfile, since it is the only
		// one with a global vision of its innertypes.

		if (constantPoolNameUsage == null)
			constantPoolNameUsage = new HashtableOfType();

		ReferenceBinding outerMostEnclosingType = localType.scope
				.outerMostClassScope().enclosingSourceType();

		// ensure there is not already such a local type name defined by the
		// user
		int index = 0;
		char[] candidateName;
		while (true) {
			if (localType.isMemberType()) {
				if (index == 0) {
					candidateName = CharOperation.concat(localType
							.enclosingType().constantPoolName(),
							localType.sourceName, '$');
				} else {
					// in case of collision, then member name gets extra $1
					// inserted
					// e.g. class X { { class L{} new X(){ class L{} } } }
					candidateName = CharOperation.concat(localType
							.enclosingType().constantPoolName(), '$', String
							.valueOf(index).toCharArray(), '$',
							localType.sourceName);
				}
			} else if (localType.isAnonymousType()) {
				candidateName = CharOperation.concat(outerMostEnclosingType
						.constantPoolName(), String.valueOf(index + 1)
						.toCharArray(), '$');
			} else {
				candidateName = CharOperation.concat(outerMostEnclosingType
						.constantPoolName(), '$', String.valueOf(index + 1)
						.toCharArray(), '$', localType.sourceName);
			}
			if (constantPoolNameUsage.get(candidateName) != null) {
				index++;
			} else {
				constantPoolNameUsage.put(candidateName, localType);
				break;
			}
		}
		return candidateName;
	}

	void connectTypeHierarchy() {
		for (int i = 0, length = topLevelTypes.length; i < length; i++)
			topLevelTypes[i].scope.connectTypeHierarchy();
	}

	void faultInImports() {
		if (referenceContext.imports == null)
			return;
		//
		// // collect the top level type names if a single type import exists
		int numberOfStatements = referenceContext.imports.length;
		// HashtableOfType typesBySimpleNames = null;
		// for (int i = 0; i < numberOfStatements; i++) {
		// if (!referenceContext.imports[i].onDemand) {
		// typesBySimpleNames = new HashtableOfType(topLevelTypes.length +
		// numberOfStatements);
		// for (int j = 0, length = topLevelTypes.length; j < length; j++)
		// typesBySimpleNames.put(topLevelTypes[j].sourceName,
		// topLevelTypes[j]);
		// break;
		// }
		// }
		//
		// // allocate the import array, add java.lang.* by default
		// int numberOfImports = numberOfStatements + 1;
		// for (int i = 0; i < numberOfStatements; i++) {
		// ImportReference importReference = referenceContext.imports[i];
		// if (importReference.onDemand && CharOperation.equals(JAVA_LANG,
		// importReference.tokens)) {
		// numberOfImports--;
		// break;
		// }
		// }
		ImportBinding[] resolvedImports = new ImportBinding[numberOfStatements];
		// resolvedImports[0] = environment.defaultImports[0];
		// int index = 1;
		int index = 0;
		nextImport: for (int i = 0; i < numberOfStatements; i++) {
			ImportReference importReference = referenceContext.imports[i];
			// create the file name segments here:
			// char[][] compoundName = importReference.tokens;
			//
			// // skip duplicates or imports of the current package
			// for (int j = 0; j < index; j++)
			// if (resolvedImports[j].onDemand == importReference.onDemand)
			// if (CharOperation.equals(compoundName,
			// resolvedImports[j].compoundName)) {
			// problemReporter().unusedImport(importReference); // since
			// skipped, must be reported now
			// continue nextImport;
			// }
			// if (importReference.onDemand == true)
			// if (CharOperation.equals(compoundName, currentPackageName)) {
			// problemReporter().unusedImport(importReference); // since
			// skipped, must be reported now
			// continue nextImport;
			// }
			// if (importReference.onDemand) {
			// Binding importBinding = findOnDemandImport(compoundName);
			// if (!importBinding.isValidBinding()) {
			// problemReporter().importProblem(importReference, importBinding);
			// continue nextImport;
			// }
			// resolvedImports[index++] = new ImportBinding(compoundName, true,
			// importBinding, importReference);
			// } else {
			IFile file = importReference.getFile();
			SourceTypeBinding typeBinding;
			char[][] compoundName;
			if (file != null) {
				typeBinding = new SourceTypeBinding();
				// findSingleTypeImport(compoundName);
				IPath path = file.getProjectRelativePath();
				String[] segs = path.segments();
				compoundName = new char[segs.length][];
				for (int j = 0; j < segs.length; j++) {
					compoundName[j] = segs[j].toCharArray();
				}
				typeBinding.compoundName = compoundName; // compoundName;
				// this.fPackage = fPackage;
				typeBinding.fileName = file.getFullPath().toString()
						.toCharArray();
				// typeBinding.modifiers = scope.referenceContext.modifiers;
				// typeBinding.sourceName = scope.referenceContext.name;
				typeBinding.sourceName = path.lastSegment().toCharArray();
				// this.scope = scope;
			} else {
				// if (!typeBinding.isValidBinding()) {
				// problemReporter().importProblem(importReference,
				// typeBinding);
				continue nextImport;
				// }
			}
			// if (typeBinding instanceof PackageBinding) {
			// problemReporter().cannotImportPackage(importReference);
			// continue nextImport;
			// }
			// if (typeBinding instanceof ReferenceBinding) {
			// ReferenceBinding referenceBinding = (ReferenceBinding)
			// typeBinding;
			// if (importReference.isTypeUseDeprecated(referenceBinding, this))
			// {
			// problemReporter().deprecatedType((TypeBinding) typeBinding,
			// importReference);
			// }
			// }
			// ReferenceBinding existingType =
			// typesBySimpleNames.get(compoundName[compoundName.length - 1]);
			// if (existingType != null) {
			// // duplicate test above should have caught this case, but make
			// sure
			// if (existingType == typeBinding) {
			// continue nextImport;
			// }
			// // either the type collides with a top level type or another
			// imported type
			// for (int j = 0, length = topLevelTypes.length; j < length; j++) {
			// if (CharOperation.equals(topLevelTypes[j].sourceName,
			// existingType.sourceName)) {
			// problemReporter().conflictingImport(importReference);
			// continue nextImport;
			// }
			// }
			// problemReporter().duplicateImport(importReference);
			// continue nextImport;
			// }
			resolvedImports[index++] = new ImportBinding(compoundName, false,
					typeBinding, importReference);
			imports = resolvedImports;
			// typesBySimpleNames.put(compoundName[compoundName.length - 1],
			// (ReferenceBinding) typeBinding);
			// }
		}
		//
		// // shrink resolvedImports... only happens if an error was reported
		if (resolvedImports.length > index)
			System.arraycopy(resolvedImports, 0,
					resolvedImports = new ImportBinding[index], 0, index);
		imports = resolvedImports;

		int length = imports.length;
		resolvedSingeTypeImports = new HashtableOfObject(length);
		for (int i = 0; i < length; i++) {
			ImportBinding binding = imports[i];
			if (!binding.onDemand)
				resolvedSingeTypeImports.put(
						binding.compoundName[binding.compoundName.length - 1],
						binding);
		}
	}

	public void faultInTypes() {
		faultInImports();
		if (topLevelTypes == null) {
			topLevelTypes = new SourceTypeBinding[0];
		}
		for (int i = 0, length = topLevelTypes.length; i < length; i++)
			topLevelTypes[i].faultInTypesForFieldsAndMethods();
	}

	private Binding findOnDemandImport(char[][] compoundName) {
		recordQualifiedReference(compoundName);

		Binding binding = environment.getTopLevelPackage(compoundName[0]);
		int i = 1;
		int length = compoundName.length;
		foundNothingOrType: if (binding != null) {
			PackageBinding packageBinding = (PackageBinding) binding;
			while (i < length) {
				binding = packageBinding.getTypeOrPackage(compoundName[i++]);
				if (binding == null || !binding.isValidBinding()) {
					binding = null;
					break foundNothingOrType;
				}
				if (!(binding instanceof PackageBinding))
					break foundNothingOrType;

				packageBinding = (PackageBinding) binding;
			}
			return packageBinding;
		}

		ReferenceBinding type;
		if (binding == null) {
			// if (environment.defaultPackage == null
			// || environment.options.complianceLevel >=
			// CompilerOptions.JDK1_4){
			// return new ProblemReferenceBinding(
			// CharOperation.subarray(compoundName, 0, i),
			// NotFound);
			// }
			type = findType(compoundName[0], environment.defaultPackage,
					environment.defaultPackage);
			if (type == null || !type.isValidBinding())
				return new ProblemReferenceBinding(CharOperation.subarray(
						compoundName, 0, i), NotFound);
			i = 1; // reset to look for member types inside the default package
					// type
		} else {
			type = (ReferenceBinding) binding;
		}

		for (; i < length; i++) {
			if (!type.canBeSeenBy(fPackage)) {
				return new ProblemReferenceBinding(CharOperation.subarray(
						compoundName, 0, i), type, NotVisible);
			}
			// does not look for inherited member types on purpose
			if ((type = type.getMemberType(compoundName[i])) == null) {
				return new ProblemReferenceBinding(CharOperation.subarray(
						compoundName, 0, i + 1), NotFound);
			}
		}
		if (!type.canBeSeenBy(fPackage))
			return new ProblemReferenceBinding(compoundName, type, NotVisible);
		return type;
	}

	private Binding findSingleTypeImport(char[][] compoundName) {
		// if (compoundName.length == 1) {
		// findType records the reference
		// the name cannot be a package
		// if (environment.defaultPackage == null
		// || environment.options.complianceLevel >= CompilerOptions.JDK1_4)
		// return new ProblemReferenceBinding(compoundName, NotFound);
		ReferenceBinding typeBinding = findType(compoundName[0],
				environment.defaultPackage, fPackage);
		if (typeBinding == null)
			return new ProblemReferenceBinding(compoundName, NotFound);
		else
			return typeBinding;
		// }
		// return findOnDemandImport(compoundName);
	}

	/*
	 * Answer the problem reporter to use for raising new problems.
	 * 
	 * Note that as a side-effect, this updates the current reference context
	 * (unit, type or method) in case the problem handler decides it is
	 * necessary to abort.
	 */

	public ProblemReporter problemReporter() {
		ProblemReporter problemReporter = referenceContext.problemReporter;
		problemReporter.referenceContext = referenceContext;
		return problemReporter;
	}

	/*
	 * What do we hold onto:
	 * 
	 * 1. when we resolve 'a.b.c', say we keep only 'a.b.c' & when we fail to
	 * resolve 'c' in 'a.b', lets keep 'a.b.c' THEN when we come across a
	 * new/changed/removed item named 'a.b.c', we would find all references to
	 * 'a.b.c' -> This approach fails because every type is resolved in every
	 * onDemand import to detect collision cases... so the references could be
	 * 10 times bigger than necessary.
	 * 
	 * 2. when we resolve 'a.b.c', lets keep 'a.b' & 'c' & when we fail to
	 * resolve 'c' in 'a.b', lets keep 'a.b' & 'c' THEN when we come across a
	 * new/changed/removed item named 'a.b.c', we would find all references to
	 * 'a.b' & 'c' -> This approach does not have a space problem but fails to
	 * handle collision cases. What happens if a type is added named 'a.b'? We
	 * would search for 'a' & 'b' but would not find a match.
	 * 
	 * 3. when we resolve 'a.b.c', lets keep 'a', 'a.b' & 'a', 'b', 'c' & when
	 * we fail to resolve 'c' in 'a.b', lets keep 'a', 'a.b' & 'a', 'b', 'c'
	 * THEN when we come across a new/changed/removed item named 'a.b.c', we
	 * would find all references to 'a.b' & 'c' OR 'a.b' -> 'a' & 'b' OR 'a' -> '' &
	 * 'a' -> As long as each single char[] is interned, we should not have a
	 * space problem and can handle collision cases.
	 * 
	 * 4. when we resolve 'a.b.c', lets keep 'a.b' & 'a', 'b', 'c' & when we
	 * fail to resolve 'c' in 'a.b', lets keep 'a.b' & 'a', 'b', 'c' THEN when
	 * we come across a new/changed/removed item named 'a.b.c', we would find
	 * all references to 'a.b' & 'c' OR 'a.b' -> 'a' & 'b' in the simple name
	 * collection OR 'a' -> 'a' in the simple name collection -> As long as each
	 * single char[] is interned, we should not have a space problem and can
	 * handle collision cases.
	 */
	void recordQualifiedReference(char[][] qualifiedName) {
		if (qualifiedReferences == null)
			return; // not recording dependencies

		int length = qualifiedName.length;
		if (length > 1) {
			while (!qualifiedReferences.contains(qualifiedName)) {
				qualifiedReferences.add(qualifiedName);
				if (length == 2) {
					recordSimpleReference(qualifiedName[0]);
					recordSimpleReference(qualifiedName[1]);
					return;
				}
				length--;
				recordSimpleReference(qualifiedName[length]);
				System.arraycopy(qualifiedName, 0,
						qualifiedName = new char[length][], 0, length);
			}
		} else if (length == 1) {
			recordSimpleReference(qualifiedName[0]);
		}
	}

	void recordReference(char[][] qualifiedEnclosingName, char[] simpleName) {
		recordQualifiedReference(qualifiedEnclosingName);
		recordSimpleReference(simpleName);
	}

	void recordSimpleReference(char[] simpleName) {
		if (simpleNameReferences == null)
			return; // not recording dependencies

		if (!simpleNameReferences.contains(simpleName))
			simpleNameReferences.add(simpleName);
	}

	void recordTypeReference(TypeBinding type) {
		if (referencedTypes == null)
			return; // not recording dependencies

		if (type.isArrayType())
			type = ((ArrayBinding) type).leafComponentType;
		if (!type.isBaseType() && !referencedTypes.containsIdentical(type))
			referencedTypes.add(type);
	}

	void recordTypeReferences(TypeBinding[] types) {
		if (qualifiedReferences == null)
			return; // not recording dependencies
		if (types == null || types.length == 0)
			return;

		for (int i = 0, max = types.length; i < max; i++) {
			// No need to record supertypes of method arguments & thrown
			// exceptions, just the compoundName
			// If a field/method is retrieved from such a type then a separate
			// call does the job
			TypeBinding type = types[i];
			if (type.isArrayType())
				type = ((ArrayBinding) type).leafComponentType;
			if (!type.isBaseType()) {
				ReferenceBinding actualType = (ReferenceBinding) type;
				if (!actualType.isLocalType())
					recordQualifiedReference(actualType.isMemberType() ? CharOperation
							.splitOn('.', actualType.readableName())
							: actualType.compoundName);
			}
		}
	}

	Binding resolveSingleTypeImport(ImportBinding importBinding) {
		if (importBinding.resolvedImport == null) {
			importBinding.resolvedImport = findSingleTypeImport(importBinding.compoundName);
			if (!importBinding.resolvedImport.isValidBinding()
					|| importBinding.resolvedImport instanceof PackageBinding) {
				if (this.imports != null) {
					ImportBinding[] newImports = new ImportBinding[imports.length - 1];
					for (int i = 0, n = 0, max = this.imports.length; i < max; i++)
						if (this.imports[i] != importBinding) {
							newImports[n++] = this.imports[i];
						}
					this.imports = newImports;
				}
				return null;
			}
		}
		return importBinding.resolvedImport;
	}

	public void storeDependencyInfo() {
		// add the type hierarchy of each referenced type
		// cannot do early since the hierarchy may not be fully resolved
		for (int i = 0; i < referencedTypes.size; i++) { // grows as more
															// types are added
			ReferenceBinding type = (ReferenceBinding) referencedTypes
					.elementAt(i);
			if (!type.isLocalType()) {
				recordQualifiedReference(type.isMemberType() ? CharOperation
						.splitOn('.', type.readableName()) : type.compoundName);
				ReferenceBinding enclosing = type.enclosingType();
				if (enclosing != null
						&& !referencedTypes.containsIdentical(enclosing))
					referencedTypes.add(enclosing); // to record its supertypes
			}
			ReferenceBinding superclass = type.superclass();
			if (superclass != null
					&& !referencedTypes.containsIdentical(superclass))
				referencedTypes.add(superclass); // to record its supertypes
			ReferenceBinding[] interfaces = type.superInterfaces();
			if (interfaces != null && interfaces.length > 0)
				for (int j = 0, length = interfaces.length; j < length; j++)
					if (!referencedTypes.containsIdentical(interfaces[j]))
						referencedTypes.add(interfaces[j]); // to record its
															// supertypes
		}

		int size = qualifiedReferences.size;
		char[][][] qualifiedRefs = new char[size][][];
		for (int i = 0; i < size; i++)
			qualifiedRefs[i] = qualifiedReferences.elementAt(i);
		referenceContext.compilationResult.qualifiedReferences = qualifiedRefs;

		size = simpleNameReferences.size;
		char[][] simpleRefs = new char[size][];
		for (int i = 0; i < size; i++)
			simpleRefs[i] = simpleNameReferences.elementAt(i);
		referenceContext.compilationResult.simpleNameReferences = simpleRefs;
	}

	public String toString() {
		return "--- CompilationUnit Scope : " + new String(referenceContext.getFileName()); //$NON-NLS-1$
	}

	public void verifyMethods(MethodVerifier verifier) {
		for (int i = 0, length = topLevelTypes.length; i < length; i++)
			topLevelTypes[i].verifyMethods(verifier);
	}
}
