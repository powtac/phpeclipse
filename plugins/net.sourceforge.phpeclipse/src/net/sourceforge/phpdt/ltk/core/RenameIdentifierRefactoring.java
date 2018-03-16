// Copyright (c) 2005 by Leif Frenzel. All rights reserved.
// See http://leiffrenzel.de
// modified for phpeclipse.de project by axelcl
package net.sourceforge.phpdt.ltk.core;

import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;

/**
 * <p>
 * Refactoring for renaming identifiers in PHP files.
 * </p>
 * 
 * <p>
 * All the actual work is done in the processor, so we just have to keep a
 * reference to one here.
 * <p>
 * 
 */
public class RenameIdentifierRefactoring extends ProcessorBasedRefactoring {

	private final RefactoringProcessor processor;

	public RenameIdentifierRefactoring(final RefactoringProcessor processor) {
		super(processor);
		this.processor = processor;
	}

	public RefactoringProcessor getProcessor() {
		return processor;
	}
}
