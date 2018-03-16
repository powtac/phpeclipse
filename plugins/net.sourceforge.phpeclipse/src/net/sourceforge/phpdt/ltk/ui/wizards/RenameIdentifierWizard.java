// Copyright (c) 2005 by Leif Frenzel. All rights reserved.
// See http://leiffrenzel.de
package net.sourceforge.phpdt.ltk.ui.wizards;

import net.sourceforge.phpdt.ltk.core.RenameIdentifierInfo;
import net.sourceforge.phpdt.ltk.core.RenameIdentifierRefactoring;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

/**
 * <p>
 * The wizard that is shown to the user for entering the necessary information
 * for property renaming.
 * </p>
 * 
 * <p>
 * The wizard class is primarily needed for deciding which pages are shown to
 * the user. The actual user interface creation goes on the pages.
 * </p>
 * 
 */
public class RenameIdentifierWizard extends RefactoringWizard {

	private final RenameIdentifierInfo info;

	public RenameIdentifierWizard(
			final RenameIdentifierRefactoring refactoring,
			final RenameIdentifierInfo info) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE);
		this.info = info;
	}

	// interface methods of RefactoringWizard
	// ///////////////////////////////////////

	protected void addUserInputPages() {
		setDefaultPageTitle(getRefactoring().getName());
		addPage(new RenameLocalVariablePage(info));
	}
}
