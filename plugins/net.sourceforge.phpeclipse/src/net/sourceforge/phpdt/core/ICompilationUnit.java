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
package net.sourceforge.phpdt.core;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Represents an entire Java compilation unit (<code>.java</code> source
 * file). Compilation unit elements need to be opened before they can be
 * navigated or manipulated. The children are of type
 * <code>IPackageDeclaration</code>, <code>IImportContainer</code>, and
 * <code>IType</code>, and appear in the order in which they are declared in
 * the source. If a <code>.java</code> file cannot be parsed, its structure
 * remains unknown. Use <code>IJavaElement.isStructureKnown</code> to
 * determine whether this is the case.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface ICompilationUnit extends IJavaElement, ISourceReference,
		IParent, IOpenable, IWorkingCopy, ISourceManipulation {
	// extends IJavaElement, ISourceReference, IParent, IOpenable, IWorkingCopy,
	// ISourceManipulation, ICodeAssist {

	/**
	 * Constant indicating that a reconcile operation should not return an AST.
	 * 
	 * @since 3.0
	 */
	public static final int NO_AST = 0;

	/**
	 * Changes this compilation unit handle into a working copy. A new
	 * <code>IBuffer</code> is created using this compilation unit handle's
	 * owner. Uses the primary owner is none was specified when this compilation
	 * unit handle was created.
	 * <p>
	 * When switching to working copy mode, problems are reported to given
	 * <code>IProblemRequestor</code>.
	 * </p>
	 * <p>
	 * Once in working copy mode, changes to this compilation unit or its
	 * children are done in memory. Only the new buffer is affected. Using
	 * <code>commitWorkingCopy(boolean, IProgressMonitor)</code> will bring
	 * the underlying resource in sync with this compilation unit.
	 * </p>
	 * <p>
	 * If this compilation unit was already in working copy mode, an internal
	 * counter is incremented and no other action is taken on this compilation
	 * unit. To bring this compilation unit back into the original mode (where
	 * it reflects the underlying resource), <code>discardWorkingCopy</code>
	 * must be call as many times as <code>becomeWorkingCopy</code>.
	 * </p>
	 * 
	 * @param problemRequestor
	 *            a requestor which will get notified of problems detected
	 *            during reconciling as they are discovered. The requestor can
	 *            be set to <code>null</code> indicating that the client is
	 *            not interested in problems.
	 * @param monitor
	 *            a progress monitor used to report progress while opening this
	 *            compilation unit or <code>null</code> if no progress should
	 *            be reported
	 * @throws JavaModelException
	 *             if this compilation unit could not become a working copy.
	 * @see #discardWorkingCopy()
	 * @since 3.0
	 */
	void becomeWorkingCopy(IProblemRequestor problemRequestor,
			IProgressMonitor monitor) throws JavaModelException;

	/**
	 * Commits the contents of this working copy to its underlying resource.
	 * 
	 * <p>
	 * It is possible that the contents of the original resource have changed
	 * since this working copy was created, in which case there is an update
	 * conflict. The value of the <code>force</code> parameter effects the
	 * resolution of such a conflict:
	 * <ul>
	 * <li> <code>true</code> - in this case the contents of this working copy
	 * are applied to the underlying resource even though this working copy was
	 * created before a subsequent change in the resource</li>
	 * <li> <code>false</code> - in this case a
	 * <code>JavaModelException</code> is thrown</li>
	 * </ul>
	 * <p>
	 * Since 2.1, a working copy can be created on a not-yet existing
	 * compilation unit. In particular, such a working copy can then be
	 * committed in order to create the corresponding compilation unit.
	 * </p>
	 * 
	 * @param force
	 *            a flag to handle the cases when the contents of the original
	 *            resource have changed since this working copy was created
	 * @param monitor
	 *            the given progress monitor
	 * @throws JavaModelException
	 *             if this working copy could not commit. Reasons include:
	 *             <ul>
	 *             <li> A <code>CoreException</code> occurred while updating
	 *             an underlying resource
	 *             <li> This element is not a working copy
	 *             (INVALID_ELEMENT_TYPES)
	 *             <li> A update conflict (described above) (UPDATE_CONFLICT)
	 *             </ul>
	 * @since 3.0
	 */
	void commitWorkingCopy(boolean force, IProgressMonitor monitor)
			throws JavaModelException;

	/**
	 * Changes this compilation unit in working copy mode back to its original
	 * mode.
	 * <p>
	 * This has no effect if this compilation unit was not in working copy mode.
	 * </p>
	 * <p>
	 * If <code>becomeWorkingCopy</code> was called several times on this
	 * compilation unit, <code>discardWorkingCopy</code> must be called as
	 * many times before it switches back to the original mode.
	 * </p>
	 * 
	 * @throws JavaModelException
	 *             if this working copy could not return in its original mode.
	 * @see #becomeWorkingCopy(IProblemRequestor, IProgressMonitor)
	 * @since 3.0
	 */
	void discardWorkingCopy() throws JavaModelException;

	/**
	 * Creates and returns an import declaration in this compilation unit with
	 * the given name.
	 * <p>
	 * Optionally, the new element can be positioned before the specified
	 * sibling. If no sibling is specified, the element will be inserted as the
	 * last import declaration in this compilation unit.
	 * <p>
	 * If the compilation unit already includes the specified import
	 * declaration, the import is not generated (it does not generate
	 * duplicates). Note that it is valid to specify both a single-type import
	 * and an on-demand import for the same package, for example
	 * <code>"java.io.File"</code> and <code>"java.io.*"</code>, in which
	 * case both are preserved since the semantics of this are not the same as
	 * just importing <code>"java.io.*"</code>. Importing
	 * <code>"java.lang.*"</code>, or the package in which the compilation
	 * unit is defined, are not treated as special cases. If they are specified,
	 * they are included in the result.
	 * 
	 * @param name
	 *            the name of the import declaration to add as defined by JLS2
	 *            7.5. (For example: <code>"java.io.File"</code> or
	 *            <code>"java.awt.*"</code>)
	 * @param sibling
	 *            the existing element which the import declaration will be
	 *            inserted immediately before (if <code> null </code>, then
	 *            this import will be inserted as the last import declaration.
	 * @param monitor
	 *            the progress monitor to notify
	 * @return the newly inserted import declaration (or the previously existing
	 *         one in case attempting to create a duplicate)
	 * 
	 * @exception JavaModelException
	 *                if the element could not be created. Reasons include:
	 *                <ul>
	 *                <li> This Java element does not exist or the specified
	 *                sibling does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 *                <li> A <code>CoreException</code> occurred while
	 *                updating an underlying resource
	 *                <li> The specified sibling is not a child of this
	 *                compilation unit (INVALID_SIBLING)
	 *                <li> The name is not a valid import name (INVALID_NAME)
	 *                </ul>
	 */
	// IImportDeclaration createImport(String name, IJavaElement sibling,
	// IProgressMonitor monitor) throws JavaModelException;
	/**
	 * Creates and returns a package declaration in this compilation unit with
	 * the given package name.
	 * 
	 * <p>
	 * If the compilation unit already includes the specified package
	 * declaration, it is not generated (it does not generate duplicates).
	 * 
	 * @param name
	 *            the name of the package declaration to add as defined by JLS2
	 *            7.4. (For example, <code>"java.lang"</code>)
	 * @param monitor
	 *            the progress monitor to notify
	 * @return the newly inserted package declaration (or the previously
	 *         existing one in case attempting to create a duplicate)
	 * 
	 * @exception JavaModelException
	 *                if the element could not be created. Reasons include:
	 *                <ul>
	 *                <li>This Java element does not exist
	 *                (ELEMENT_DOES_NOT_EXIST)</li>
	 *                <li> A <code>CoreException</code> occurred while
	 *                updating an underlying resource
	 *                <li> The name is not a valid package name (INVALID_NAME)
	 *                </ul>
	 */
	// IPackageDeclaration createPackageDeclaration(String name,
	// IProgressMonitor monitor) throws JavaModelException;
	/**
	 * Creates and returns a type in this compilation unit with the given
	 * contents. If this compilation unit does not exist, one will be created
	 * with an appropriate package declaration.
	 * <p>
	 * Optionally, the new type can be positioned before the specified sibling.
	 * If <code>sibling</code> is <code>null</code>, the type will be
	 * appended to the end of this compilation unit.
	 * 
	 * <p>
	 * It is possible that a type with the same name already exists in this
	 * compilation unit. The value of the <code>force</code> parameter effects
	 * the resolution of such a conflict:
	 * <ul>
	 * <li> <code>true</code> - in this case the type is created with the new
	 * contents</li>
	 * <li> <code>false</code> - in this case a
	 * <code>JavaModelException</code> is thrown</li>
	 * </ul>
	 * 
	 * @param contents
	 *            the source contents of the type declaration to add.
	 * @param sibling
	 *            the existing element which the type will be inserted
	 *            immediately before (if <code> null </code>, then this type
	 *            will be inserted as the last type declaration.
	 * @param force
	 *            a <code> boolean </code> flag indicating how to deal with
	 *            duplicates
	 * @param monitor
	 *            the progress monitor to notify
	 * @return the newly inserted type
	 * 
	 * @exception JavaModelException
	 *                if the element could not be created. Reasons include:
	 *                <ul>
	 *                <li>The specified sibling element does not exist
	 *                (ELEMENT_DOES_NOT_EXIST)</li>
	 *                <li> A <code>CoreException</code> occurred while
	 *                updating an underlying resource
	 *                <li> The specified sibling is not a child of this
	 *                compilation unit (INVALID_SIBLING)
	 *                <li> The contents could not be recognized as a type
	 *                declaration (INVALID_CONTENTS)
	 *                <li> There was a naming collision with an existing type
	 *                (NAME_COLLISION)
	 *                </ul>
	 */
	// IType createType(String contents, IJavaElement sibling, boolean force,
	// IProgressMonitor monitor) throws JavaModelException;
	/**
	 * Returns all types declared in this compilation unit in the order in which
	 * they appear in the source. This includes all top-level types and nested
	 * member types. It does NOT include local types (types defined in methods).
	 * 
	 * @return the array of top-level and member types defined in a compilation
	 *         unit, in declaration order.
	 * @exception JavaModelException
	 *                if this element does not exist or if an exception occurs
	 *                while accessing its corresponding resource
	 */
	IType[] getAllTypes() throws JavaModelException;

	/**
	 * Returns the smallest element within this compilation unit that includes
	 * the given source position (that is, a method, field, etc.), or
	 * <code>null</code> if there is no element other than the compilation
	 * unit itself at the given position, or if the given position is not within
	 * the source range of this compilation unit.
	 * 
	 * @param position
	 *            a source position inside the compilation unit
	 * @return the innermost Java element enclosing a given source position or
	 *         <code>null</code> if none (excluding the compilation unit).
	 * @exception JavaModelException
	 *                if the compilation unit does not exist or if an exception
	 *                occurs while accessing its corresponding resource
	 */
	IJavaElement getElementAt(int position) throws JavaModelException;

	/**
	 * Returns the first import declaration in this compilation unit with the
	 * given name. This is a handle-only method. The import declaration may or
	 * may not exist. This is a convenience method - imports can also be
	 * accessed from a compilation unit's import container.
	 * 
	 * @param name
	 *            the name of the import to find as defined by JLS2 7.5. (For
	 *            example: <code>"java.io.File"</code> or
	 *            <code>"java.awt.*"</code>)
	 * @return a handle onto the corresponding import declaration. The import
	 *         declaration may or may not exist.
	 */
	IImportDeclaration getImport(String name);

	/**
	 * Returns the import declarations in this compilation unit in the order in
	 * which they appear in the source. This is a convenience method - import
	 * declarations can also be accessed from a compilation unit's import
	 * container.
	 * 
	 * @return the import declarations in this compilation unit
	 * @throws JavaModelException
	 *             if this element does not exist or if an exception occurs
	 *             while accessing its corresponding resource
	 */
	IImportDeclaration[] getImports() throws JavaModelException;

	/**
	 * Returns the import container for this compilation unit. This is a
	 * handle-only method. The import container may or may not exist. The import
	 * container can used to access the imports.
	 * 
	 * @return a handle onto the corresponding import container. The import
	 *         contain may or may not exist.
	 */
	IImportContainer getImportContainer();

	/**
	 * Returns the import declarations in this compilation unit in the order in
	 * which they appear in the source. This is a convenience method - import
	 * declarations can also be accessed from a compilation unit's import
	 * container.
	 * 
	 * @exception JavaModelException
	 *                if this element does not exist or if an exception occurs
	 *                while accessing its corresponding resource
	 */
	// IImportDeclaration[] getImports() throws JavaModelException;
	/**
	 * Returns the first package declaration in this compilation unit with the
	 * given package name (there normally is at most one package declaration).
	 * This is a handle-only method. The package declaration may or may not
	 * exist.
	 * 
	 * @param name
	 *            the name of the package declaration as defined by JLS2 7.4.
	 *            (For example, <code>"java.lang"</code>)
	 */
	IPackageDeclaration getPackageDeclaration(String name);

	/**
	 * Returns the package declarations in this compilation unit in the order in
	 * which they appear in the source. There normally is at most one package
	 * declaration.
	 * 
	 * @return an array of package declaration (normally of size one)
	 * 
	 * @exception JavaModelException
	 *                if this element does not exist or if an exception occurs
	 *                while accessing its corresponding resource
	 */
	IPackageDeclaration[] getPackageDeclarations() throws JavaModelException;

	/**
	 * Returns the primary compilation unit (whose owner is the primary owner)
	 * this working copy was created from, or this compilation unit if this a
	 * primary compilation unit.
	 * <p>
	 * Note that the returned primary compilation unit can be in working copy
	 * mode.
	 * </p>
	 * 
	 * @return the primary compilation unit this working copy was created from,
	 *         or this compilation unit if it is primary
	 * @since 3.0
	 */
	ICompilationUnit getPrimary();

	/**
	 * Returns the top-level type declared in this compilation unit with the
	 * given simple type name. The type name has to be a valid compilation unit
	 * name. This is a handle-only method. The type may or may not exist.
	 * 
	 * @param name
	 *            the simple name of the requested type in the compilation unit
	 * @return a handle onto the corresponding type. The type may or may not
	 *         exist.
	 * @see JavaConventions#validateCompilationUnitName(String name)
	 */
	IType getType(String name);

	/**
	 * Returns the top-level types declared in this compilation unit in the
	 * order in which they appear in the source.
	 * 
	 * @exception JavaModelException
	 *                if this element does not exist or if an exception occurs
	 *                while accessing its corresponding resource
	 */
	IType[] getTypes() throws JavaModelException;

}
