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

import net.sourceforge.phpdt.core.ISourceRange;

/**
 * Element info for ISourceReference elements.
 */
/* package */class SourceRefElementInfo extends JavaElementInfo {
	protected int fSourceRangeStart, fSourceRangeEnd;

	protected SourceRefElementInfo() {
		setIsStructureKnown(true);
	}

	/**
	 * @see net.sourceforge.phpdt.internal.compiler.env.ISourceType#getDeclarationSourceEnd()
	 * @see net.sourceforge.phpdt.internal.compiler.env.ISourceMethod#getDeclarationSourceEnd()
	 * @see net.sourceforge.phpdt.internal.compiler.env.ISourceField#getDeclarationSourceEnd()
	 */
	public int getDeclarationSourceEnd() {
		return fSourceRangeEnd;
	}

	/**
	 * @see net.sourceforge.phpdt.internal.compiler.env.ISourceType#getDeclarationSourceStart()
	 * @see net.sourceforge.phpdt.internal.compiler.env.ISourceMethod#getDeclarationSourceStart()
	 * @see net.sourceforge.phpdt.internal.compiler.env.ISourceField#getDeclarationSourceStart()
	 */
	public int getDeclarationSourceStart() {
		return fSourceRangeStart;
	}

	protected ISourceRange getSourceRange() {
		return new SourceRange(fSourceRangeStart, fSourceRangeEnd
				- fSourceRangeStart + 1);
	}

	protected void setSourceRangeEnd(int end) {
		fSourceRangeEnd = end;
	}

	protected void setSourceRangeStart(int start) {
		fSourceRangeStart = start;
	}
}
