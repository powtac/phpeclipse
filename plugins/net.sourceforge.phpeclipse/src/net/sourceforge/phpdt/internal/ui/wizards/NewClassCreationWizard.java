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
package net.sourceforge.phpdt.internal.ui.wizards;

import net.sourceforge.phpdt.core.ICompilationUnit;
import net.sourceforge.phpdt.internal.corext.util.JavaModelUtil;
import net.sourceforge.phpdt.internal.ui.PHPUiImages;
import net.sourceforge.phpdt.ui.wizards.NewClassWizardPage;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class NewClassCreationWizard extends NewElementWizard {

	private NewClassWizardPage fPage;

	public NewClassCreationWizard() {
		super();
		setDefaultPageImageDescriptor(PHPUiImages.DESC_WIZBAN_NEWCLASS);
		setDialogSettings(PHPeclipsePlugin.getDefault().getDialogSettings());
		setWindowTitle(NewWizardMessages
				.getString("NewClassCreationWizard.title")); //$NON-NLS-1$
	}

	/*
	 * @see Wizard#createPages
	 */
	public void addPages() {
		super.addPages();
		fPage = new NewClassWizardPage();
		addPage(fPage);
		fPage.init(getSelection());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.phpdt.internal.ui.wizards.NewElementWizard#finishPage(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void finishPage(IProgressMonitor monitor)
			throws InterruptedException, CoreException {
		fPage.createType(monitor); // use the full progress monitor
		ICompilationUnit cu = JavaModelUtil.toOriginal(fPage.getCreatedType()
				.getCompilationUnit());
		if (cu != null) {
			IResource resource = cu.getResource();
			selectAndReveal(resource);
			openResource((IFile) resource);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish() {
		// warnAboutTypeCommentDeprecation();
		return super.performFinish();
	}

}
