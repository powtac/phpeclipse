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
package net.sourceforge.phpdt.internal.core;

import net.sourceforge.phpdt.core.ICompilationUnit;
import net.sourceforge.phpdt.core.IPackageFragment;
import net.sourceforge.phpdt.core.IType;
import net.sourceforge.phpdt.core.JavaModelException;
import net.sourceforge.phpdt.internal.codeassist.ISearchRequestor;

/**
 * Implements <code>IJavaElementRequestor</code>, wrappering and forwarding
 * results onto a
 * <code>org.eclipse.jdt.internal.codeassist.api.ISearchRequestor</code>.
 */
class SearchableEnvironmentRequestor extends JavaElementRequestor implements
		IJavaElementRequestor {
	/**
	 * The <code>ISearchRequestor</code> this JavaElementRequestor wraps and
	 * forwards results to.
	 */
	protected ISearchRequestor fRequestor;

	/**
	 * The <code>ICompilationUNit</code> this JavaElementRequestor will not
	 * accept types within.
	 */
	protected ICompilationUnit fUnitToSkip;

	/**
	 * Constructs a SearchableEnvironmentRequestor that wraps the given
	 * SearchRequestor.
	 */
	public SearchableEnvironmentRequestor(ISearchRequestor requestor) {
		fRequestor = requestor;
		fUnitToSkip = null;
	}

	/**
	 * Constructs a SearchableEnvironmentRequestor that wraps the given
	 * SearchRequestor. The requestor will not accept types in the
	 * <code>unitToSkip</code>.
	 */
	public SearchableEnvironmentRequestor(ISearchRequestor requestor,
			ICompilationUnit unitToSkip) {
		fRequestor = requestor;
		fUnitToSkip = unitToSkip;
	}

	/**
	 * Do nothing, a SearchRequestor does not accept initializers so there is no
	 * need to forward this results.
	 * 
	 * @see IJavaElementRequestor
	 */
	// public void acceptInitializer(IInitializer initializer) {
	//
	// }
	/**
	 * @see IJavaElementRequestor
	 */
	public void acceptPackageFragment(IPackageFragment packageFragment) {
		fRequestor
				.acceptPackage(packageFragment.getElementName().toCharArray());
	}

	/**
	 * @see IJavaElementRequestor
	 */
	public void acceptType(IType type) {
		try {
			if (fUnitToSkip != null
					&& fUnitToSkip.equals(type.getCompilationUnit())) {
				return;
			}
			if (type.isClass()) {
				fRequestor.acceptClass(type.getPackageFragment()
						.getElementName().toCharArray(), type.getElementName()
						.toCharArray(), type.getFlags());
			} else {
				fRequestor.acceptInterface(type.getPackageFragment()
						.getElementName().toCharArray(), type.getElementName()
						.toCharArray(), type.getFlags());
			}
		} catch (JavaModelException npe) {
		}
	}
}
