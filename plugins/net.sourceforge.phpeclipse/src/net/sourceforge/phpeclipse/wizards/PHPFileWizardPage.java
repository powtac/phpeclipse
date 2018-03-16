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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (cs).
 */

public class PHPFileWizardPage extends WizardPage {
	private static final String INITIAL_FILENAME = "file.php";

	private Text containerText;

	private Text fileText;

	private ISelection selection;

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public PHPFileWizardPage(ISelection selection) {
		super("wizardPage");
		setTitle(PHPWizardMessages.getString("WizardPage.title"));
		setDescription(PHPWizardMessages.getString("WizardPage.description"));
		this.selection = selection;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		Label label = new Label(container, SWT.NULL);
		label.setText(PHPWizardMessages.getString("WizardPage.containerLabel"));

		containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		containerText.setLayoutData(gd);
		containerText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		Button button = new Button(container, SWT.PUSH);
		button.setText(PHPWizardMessages
				.getString("WizardPage.browseButtonText"));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		label = new Label(container, SWT.NULL);
		label.setText(PHPWizardMessages.getString("WizardPage.fileLabel"));

		fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fileText.setLayoutData(gd);
		fileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		initialize();
		dialogChanged();
		setControl(container);
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */

	private void initialize() {
		if (selection != null && selection.isEmpty() == false
				&& selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() > 1)
				return;
			Object obj = ssel.getFirstElement();
			if (obj instanceof IResource) {
				IContainer container;
				if (obj instanceof IContainer)
					container = (IContainer) obj;
				else
					container = ((IResource) obj).getParent();
				containerText.setText(container.getFullPath().toString());
				fileText.setFocus();
			}
		}
		fileText.setText(INITIAL_FILENAME);
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */

	private void handleBrowse() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(
				getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
				PHPWizardMessages
						.getString("WizardPage.selectNewFileContainer"));
		if (dialog.open() == ContainerSelectionDialog.OK) {
			Object[] results = dialog.getResult();
			if (results.length == 1) {
				Object result = results[0];
				if (result instanceof IPath) {
					IPath ipath = (IPath) result;
					containerText.setText(ipath.toString());
				}
			}
		}
	}

	/**
	 * Ensures that both text fields are set.
	 */
	private void dialogChanged() {
		String container = getContainerName();
		String fileName = getFileName();

		if (container.length() == 0) {
			updateStatus(PHPWizardMessages
					.getString("WizardPage.containerMustBeSpecified"));
			return;
		}
		if (fileName.length() == 0) {
			updateStatus("WizardPage.nameMustBeSpecified");
			return;
		}

		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getContainerName() {
		return containerText.getText();
	}

	public String getFileName() {
		return fileText.getText();
	}

	/**
	 * @see WizardPage#isPageComplete()
	 */
	public boolean isPageComplete() {
		return !checkFolderForExistingFile() && super.isPageComplete();
	}

	/**
	 * Finds the current directory where the file should be created
	 */
	protected boolean checkFolderForExistingFile() {
		IContainer container = getFileContainer();
		if (container != null) {
			IResource file = container.getFile(new Path(fileText.getText()
					.trim()));
			if (file != null && file.exists()) {
				this.setErrorMessage(PHPWizardMessages
						.getString("WizardPage.fileAlreadyExists"));
				return true;
			}
		}
		return false;
	}

	private IContainer getFileContainer() {
		if (containerText.getText() != null) {
			IPath containerPath = new Path(containerText.getText().trim());
			IContainer container = null;
			if (containerPath.segmentCount() > 1) {
				container = ResourcesPlugin.getWorkspace().getRoot().getFolder(
						containerPath);
			} else {
				if (containerPath.segmentCount() == 1) {
					// this is a project
					container = ResourcesPlugin.getWorkspace().getRoot()
							.getProject(containerText.getText().trim());
				}
			}
			if (container != null && container.exists()) {
				return container;
			}
		}
		return null;
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			String fileName = fileText.getText().trim();
			if (getFileContainer() != null
					&& fileName.equalsIgnoreCase(INITIAL_FILENAME)) {
				fileText.setFocus();
				fileText.setText(fileName);
				fileText.setSelection(0, fileName.length()
						- (new Path(INITIAL_FILENAME)).getFileExtension()
								.length() - 1);
			}
		}
	}

}