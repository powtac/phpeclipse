package net.sourceforge.phpeclipse.wizards;

/**********************************************************************
 Copyright (c) 2000, 2002 IBM Corp. and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html

 Contributors:
 IBM Corporation - Initial implementation
 www.phpeclipse.de
 **********************************************************************/

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import net.sourceforge.phpdt.internal.corext.codemanipulation.StubUtility;
import net.sourceforge.phpdt.internal.corext.template.php.CodeTemplateContext;
import net.sourceforge.phpdt.internal.corext.template.php.CodeTemplateContextType;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * This wizard creates one file with the extension "php".
 */
public class PHPFileWizard extends Wizard implements INewWizard {

	private PHPFileWizardPage page;

	private ISelection selection;

	public PHPFileWizard() {
		super();
		setNeedsProgressMonitor(true);
		setWindowTitle(PHPWizardMessages
				.getString("WizardNewProjectCreationPage.windowTitle"));
	}

	/**
	 * Adding the page to the wizard.
	 */
	public void addPages() {
		page = new PHPFileWizardPage(selection);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We
	 * will create an operation and run it using wizard as execution context.
	 */
	public boolean performFinish() {
		final String containerName = page.getContainerName();
		final String fileName = page.getFileName();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
					throws InvocationTargetException {
				try {
					doFinish(containerName, fileName, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), PHPWizardMessages
					.getString("Wizard.error"), realException.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * The worker method. It will find the container, create the file if missing
	 * or just replace its contents, and open the editor on the newly created
	 * file.
	 */
	private void doFinish(String containerName, String fileName,
			IProgressMonitor monitor) throws CoreException {
		// create a sample file
		monitor.beginTask(PHPWizardMessages
				.getString("Wizard.Monitor.creating")
				+ " " + fileName, 2);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException(PHPWizardMessages
					.getString("Wizard.Monitor.containerDoesNotExistException"));
		}
		IContainer container = (IContainer) resource;
		final IFile file = container.getFile(new Path(fileName));
		IProject project = file.getProject();
		String projectName = project.getName();
		String className = getClassName(fileName);

		try {
			InputStream stream;
			if (className == null) {
				stream = openContentStream(fileName, projectName);
			} else {
				stream = openContentStreamClass(className);
			}
			if (file.exists()) {
				file.setContents(stream, true, true, monitor);
			} else {
				file.create(stream, true, monitor);
			}
			stream.close();
		} catch (IOException e) {
		}
		monitor.worked(1);
		monitor.setTaskName(PHPWizardMessages
				.getString("Wizard.Monitor.openingFile"));
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, file, true);
				} catch (PartInitException e) {
				}
			}
		});
		monitor.worked(1);
	}

	/**
	 * Check if the filename is like this anyname.class.php
	 * 
	 * @param fFileName
	 *            the filename
	 * @return the anyname or null
	 */
	private static final String getClassName(final String fileName) {
		final int lastDot = fileName.lastIndexOf('.');
		if (lastDot == -1)
			return null;
		final int precLastDot = fileName.lastIndexOf('.', lastDot - 1);
		if (precLastDot == -1)
			return null;
		if (!fileName.substring(precLastDot + 1, lastDot).toUpperCase().equals(
				"CLASS"))
			return null;
		return fileName.substring(0, precLastDot);
	}

	/**
	 * We will initialize file contents for a class
	 * 
	 * @param className
	 *            the classname
	 */
	private InputStream openContentStreamClass(final String className) {
		StringBuffer contents = new StringBuffer("<?php\n\n");
		contents.append("class ");
		contents.append(className);
		contents.append(" {\n\n");
		contents.append("    function ");
		contents.append(className);
		contents.append("() {\n");
		contents.append("    }\n}\n?>");
		return new ByteArrayInputStream(contents.toString().getBytes());
	}

	/**
	 * We will initialize file contents with a sample text.
	 */
	private InputStream openContentStream(String fileName, String projectname) {
		try {
			Template template = PHPeclipsePlugin.getDefault()
					.getCodeTemplateStore().findTemplate(
							CodeTemplateContextType.NEWTYPE);
			if (template == null) {
				return null;
			}
			String lineDelimiter = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			CodeTemplateContext context = new CodeTemplateContext(template
					.getContextTypeId(), null, lineDelimiter);
			context.setFileNameVariable(fileName, projectname);
			String content = StubUtility.evaluateTemplate(context, template);
			if (content == null) {
				content = "";
			}
			return new ByteArrayInputStream(content.getBytes());
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}

	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status = new Status(IStatus.ERROR,
				"net.sourceforge.phpeclipse.wizards", IStatus.OK, message, null);
		throw new CoreException(status);
	}

	/**
	 * We will accept the selection in the workbench to see if we can initialize
	 * from it.
	 * 
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

}