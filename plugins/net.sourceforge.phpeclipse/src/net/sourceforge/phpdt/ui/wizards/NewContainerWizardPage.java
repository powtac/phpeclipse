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
package net.sourceforge.phpdt.ui.wizards;

import net.sourceforge.phpdt.core.IJavaElement;
import net.sourceforge.phpdt.core.IJavaProject;
import net.sourceforge.phpdt.core.IPackageFragmentRoot;
import net.sourceforge.phpdt.core.JavaCore;
import net.sourceforge.phpdt.core.JavaModelException;
import net.sourceforge.phpdt.externaltools.internal.ui.StatusInfo;
import net.sourceforge.phpdt.internal.ui.viewsupport.IViewPartInputProvider;
import net.sourceforge.phpdt.internal.ui.wizards.NewWizardMessages;
import net.sourceforge.phpdt.internal.ui.wizards.dialogfields.DialogField;
import net.sourceforge.phpdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import net.sourceforge.phpdt.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import net.sourceforge.phpdt.internal.ui.wizards.dialogfields.LayoutUtil;
import net.sourceforge.phpdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.contentoutline.ContentOutline;

/**
 * Wizard page that acts as a base class for wizard pages that create new Java
 * elements. The class provides a input field for source folders (called
 * container in this class) and API to validate the enter source folder name.
 * 
 * @since 2.0
 */
public abstract class NewContainerWizardPage extends NewElementWizardPage {

	/** Id of the container field */
	protected static final String CONTAINER = "NewContainerWizardPage.container"; //$NON-NLS-1$

	/** The status of the last validation. */
	protected IStatus fContainerStatus;

	private StringButtonDialogField fContainerDialogField;

	/*
	 * package fragment root corresponding to the input type (can be null)
	 */
	private IPackageFragmentRoot fCurrRoot;

	private IWorkspaceRoot fWorkspaceRoot;

	/**
	 * Create a new <code>NewContainerWizardPage</code>
	 * 
	 * @param name
	 *            the wizard page's name
	 */
	public NewContainerWizardPage(String name) {
		super(name);
		fWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		ContainerFieldAdapter adapter = new ContainerFieldAdapter();

		fContainerDialogField = new StringButtonDialogField(adapter);
		fContainerDialogField.setDialogFieldListener(adapter);
		fContainerDialogField.setLabelText(NewWizardMessages
				.getString("NewContainerWizardPage.container.label")); //$NON-NLS-1$
		fContainerDialogField.setButtonLabel(NewWizardMessages
				.getString("NewContainerWizardPage.container.button")); //$NON-NLS-1$

		fContainerStatus = new StatusInfo();
		fCurrRoot = null;
	}

	/**
	 * Initializes the source folder field with a valid package fragement root.
	 * The package fragement root is computed from the given Java element.
	 * 
	 * @param elem
	 *            the Java element used to compute the initial package fragment
	 *            root used as the source folder
	 */
	protected void initContainerPage(IJavaElement elem) {
		IPackageFragmentRoot initRoot = null;
		// if (elem != null) {
		// initRoot= JavaModelUtil.getPackageFragmentRoot(elem);
		// if (initRoot == null || initRoot.isArchive()) {
		// IJavaProject jproject= elem.getJavaProject();
		// if (jproject != null) {
		// try {
		// initRoot= null;
		// if (jproject.exists()) {
		// IPackageFragmentRoot[] roots= jproject.getPackageFragmentRoots();
		// for (int i= 0; i < roots.length; i++) {
		// if (roots[i].getKind() == IPackageFragmentRoot.K_SOURCE) {
		// initRoot= roots[i];
		// break;
		// }
		// }
		// }
		// } catch (JavaModelException e) {
		// PHPeclipsePlugin.log(e);
		// }
		// if (initRoot == null) {
		// initRoot= jproject.getPackageFragmentRoot(jproject.getResource());
		// }
		// }
		// }
		// }
		// setPackageFragmentRoot(initRoot, true);
	}

	/**
	 * Utility method to inspect a selection to find a Java element.
	 * 
	 * @param selection
	 *            the selection to be inspected
	 * @return a Java element to be used as the initial selection, or
	 *         <code>null</code>, if no Java element exists in the given
	 *         selection
	 */
	protected IJavaElement getInitialJavaElement(IStructuredSelection selection) {
		IJavaElement jelem = null;
		if (selection != null && !selection.isEmpty()) {
			Object selectedElement = selection.getFirstElement();
			if (selectedElement instanceof IAdaptable) {
				IAdaptable adaptable = (IAdaptable) selectedElement;

				jelem = (IJavaElement) adaptable.getAdapter(IJavaElement.class);
				if (jelem == null) {
					IResource resource = (IResource) adaptable
							.getAdapter(IResource.class);
					if (resource != null
							&& resource.getType() != IResource.ROOT) {
						while (jelem == null
								&& resource.getType() != IResource.PROJECT) {
							resource = resource.getParent();
							jelem = (IJavaElement) resource
									.getAdapter(IJavaElement.class);
						}
						if (jelem == null) {
							jelem = JavaCore.create(resource); // java project
						}
					}
				}
			}
		}
		if (jelem == null) {
			IWorkbenchPart part = PHPeclipsePlugin.getActivePage()
					.getActivePart();
			if (part instanceof ContentOutline) {
				part = PHPeclipsePlugin.getActivePage().getActiveEditor();
			}

			if (part instanceof IViewPartInputProvider) {
				Object elem = ((IViewPartInputProvider) part)
						.getViewPartInput();
				if (elem instanceof IJavaElement) {
					jelem = (IJavaElement) elem;
				}
			}
		}

		if (jelem == null || jelem.getElementType() == IJavaElement.JAVA_MODEL) {
			try {
				IJavaProject[] projects = JavaCore.create(getWorkspaceRoot())
						.getJavaProjects();
				if (projects.length == 1) {
					jelem = projects[0];
				}
			} catch (JavaModelException e) {
				PHPeclipsePlugin.log(e);
			}
		}
		return jelem;
	}

	/**
	 * Returns the recommended maximum width for text fields (in pixels). This
	 * method requires that createContent has been called before this method is
	 * call. Subclasses may override to change the maximum width for text
	 * fields.
	 * 
	 * @return the recommended maximum width for text fields.
	 */
	protected int getMaxFieldWidth() {
		return convertWidthInCharsToPixels(40);
	}

	/**
	 * Creates the necessary controls (label, text field and browse button) to
	 * edit the source folder location. The method expects that the parent
	 * composite uses a <code>GridLayout</code> as its layout manager and that
	 * the grid layout has at least 3 columns.
	 * 
	 * @param parent
	 *            the parent composite
	 * @param nColumns
	 *            the number of columns to span. This number must be greater or
	 *            equal three
	 */
	protected void createContainerControls(Composite parent, int nColumns) {
		fContainerDialogField.doFillIntoGrid(parent, nColumns);
		LayoutUtil.setWidthHint(fContainerDialogField.getTextControl(null),
				getMaxFieldWidth());
	}

	/**
	 * Sets the focus to the source folder's text field.
	 */
	protected void setFocusOnContainer() {
		fContainerDialogField.setFocus();
	}

	// -------- ContainerFieldAdapter --------

	private class ContainerFieldAdapter implements IStringButtonAdapter,
			IDialogFieldListener {

		// -------- IStringButtonAdapter
		public void changeControlPressed(DialogField field) {
			containerChangeControlPressed(field);
		}

		// -------- IDialogFieldListener
		public void dialogFieldChanged(DialogField field) {
			containerDialogFieldChanged(field);
		}
	}

	private void containerChangeControlPressed(DialogField field) {
		// take the current jproject as init element of the dialog
		// IPackageFragmentRoot root= getPackageFragmentRoot();
		// root= chooseSourceContainer(root);
		// if (root != null) {
		// setPackageFragmentRoot(root, true);
		// }
	}

	private void containerDialogFieldChanged(DialogField field) {
		if (field == fContainerDialogField) {
			fContainerStatus = containerChanged();
		}
		// tell all others
		handleFieldChanged(CONTAINER);
	}

	// ----------- validation ----------

	/**
	 * This method is a hook which gets called after the source folder's text
	 * input field has changed. This default implementation updates the model
	 * and returns an error status. The underlying model is only valid if the
	 * returned status is OK.
	 * 
	 * @return the model's error status
	 */
	protected IStatus containerChanged() {
		StatusInfo status = new StatusInfo();

		fCurrRoot = null;
		String str = getPackageFragmentRootText();
		if (str.length() == 0) {
			status
					.setError(NewWizardMessages
							.getString("NewContainerWizardPage.error.EnterContainerName")); //$NON-NLS-1$
			return status;
		}
		IPath path = new Path(str);
		IResource res = fWorkspaceRoot.findMember(path);
		if (res != null) {
			int resType = res.getType();
			if (resType == IResource.PROJECT || resType == IResource.FOLDER) {
				IProject proj = res.getProject();
				if (!proj.isOpen()) {
					status
							.setError(NewWizardMessages
									.getFormattedString(
											"NewContainerWizardPage.error.ProjectClosed", proj.getFullPath().toString())); //$NON-NLS-1$
					return status;
				}
				IJavaProject jproject = JavaCore.create(proj);
				// fCurrRoot= jproject.getPackageFragmentRoot(res);
				// if (res.exists()) {
				// try {
				// if (!proj.hasNature(JavaCore.NATURE_ID)) {
				// if (resType == IResource.PROJECT) {
				// status.setError(NewWizardMessages.getString("NewContainerWizardPage.warning.NotAJavaProject"));
				// //$NON-NLS-1$
				// } else {
				// status.setWarning(NewWizardMessages.getString("NewContainerWizardPage.warning.NotInAJavaProject"));
				// //$NON-NLS-1$
				// }
				// return status;
				// }
				// } catch (CoreException e) {
				// status.setWarning(NewWizardMessages.getString("NewContainerWizardPage.warning.NotAJavaProject"));
				// //$NON-NLS-1$
				// }
				// if (!jproject.isOnClasspath(fCurrRoot)) {
				// status.setWarning(NewWizardMessages.getFormattedString("NewContainerWizardPage.warning.NotOnClassPath",
				// str)); //$NON-NLS-1$
				// }
				// if (fCurrRoot.isArchive()) {
				// status.setError(NewWizardMessages.getFormattedString("NewContainerWizardPage.error.ContainerIsBinary",
				// str)); //$NON-NLS-1$
				// return status;
				// }
				// }
				return status;
			} else {
				status.setError(NewWizardMessages.getFormattedString(
						"NewContainerWizardPage.error.NotAFolder", str)); //$NON-NLS-1$
				return status;
			}
		} else {
			status.setError(NewWizardMessages.getFormattedString(
					"NewContainerWizardPage.error.ContainerDoesNotExist", str)); //$NON-NLS-1$
			return status;
		}
	}

	// -------- update message ----------------

	/**
	 * Hook method that gets called when a field on this page has changed. For
	 * this page the method gets called when the source folder field changes.
	 * <p>
	 * Every sub type is responsible to call this method when a field on its
	 * page has changed. Subtypes override (extend) the method to add
	 * verification when a own field has a dependency to an other field. For
	 * example the class name input must be verified again when the package
	 * field changes (check for duplicated class names).
	 * 
	 * @param fieldName
	 *            The name of the field that has changed (field id). For the
	 *            source folder the field id is <code>CONTAINER</code>
	 */
	protected void handleFieldChanged(String fieldName) {
	}

	// ---- get ----------------

	/**
	 * Returns the workspace root.
	 * 
	 * @return the workspace root
	 */
	protected IWorkspaceRoot getWorkspaceRoot() {
		return fWorkspaceRoot;
	}

	/**
	 * Returns the <code>IPackageFragmentRoot</code> that corresponds to the
	 * current value of the source folder field.
	 * 
	 * @return the IPackageFragmentRoot or <code>null</code> if the current
	 *         source folder value is not a valid package fragment root
	 * 
	 */
	public IPackageFragmentRoot getPackageFragmentRoot() {
		return fCurrRoot;
	}

	/**
	 * Returns the current text of source folder text field.
	 * 
	 * @return the text of the source folder text field
	 */
	public String getPackageFragmentRootText() {
		return fContainerDialogField.getText();
	}

	/**
	 * Sets the current source folder (model and text field) to the given
	 * package fragment root.
	 * 
	 * @param canBeModified
	 *            if <code>false</code> the source folder field can not be
	 *            changed by the user. If <code>true</code> the field is
	 *            editable
	 */
	// public void setPackageFragmentRoot(IPackageFragmentRoot root, boolean
	// canBeModified) {
	// fCurrRoot= root;
	// String str= (root == null) ? "" :
	// root.getPath().makeRelative().toString(); //$NON-NLS-1$
	// fContainerDialogField.setText(str);
	// fContainerDialogField.setEnabled(canBeModified);
	// }
	// ------------- choose source container dialog
	// private IPackageFragmentRoot chooseSourceContainer(IJavaElement
	// initElement) {
	// Class[] acceptedClasses= new Class[] { IPackageFragmentRoot.class,
	// IJavaProject.class };
	// TypedElementSelectionValidator validator= new
	// TypedElementSelectionValidator(acceptedClasses, false) {
	// public boolean isSelectedValid(Object element) {
	// try {
	// if (element instanceof IJavaProject) {
	// IJavaProject jproject= (IJavaProject)element;
	// IPath path= jproject.getProject().getFullPath();
	// return (jproject.findPackageFragmentRoot(path) != null);
	// } else if (element instanceof IPackageFragmentRoot) {
	// return (((IPackageFragmentRoot)element).getKind() ==
	// IPackageFragmentRoot.K_SOURCE);
	// }
	// return true;
	// } catch (JavaModelException e) {
	// PHPeclipsePlugin.log(e.getStatus()); // just log, no ui in validation
	// }
	// return false;
	// }
	// };
	//		
	// acceptedClasses= new Class[] { IJavaModel.class,
	// IPackageFragmentRoot.class, IJavaProject.class };
	// ViewerFilter filter= new TypedViewerFilter(acceptedClasses) {
	// public boolean select(Viewer viewer, Object parent, Object element) {
	// if (element instanceof IPackageFragmentRoot) {
	// try {
	// return (((IPackageFragmentRoot)element).getKind() ==
	// IPackageFragmentRoot.K_SOURCE);
	// } catch (JavaModelException e) {
	// PHPeclipsePlugin.log(e.getStatus()); // just log, no ui in validation
	// return false;
	// }
	// }
	// return super.select(viewer, parent, element);
	// }
	// };
	//
	// StandardJavaElementContentProvider provider= new
	// StandardJavaElementContentProvider();
	// ILabelProvider labelProvider= new
	// JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
	// ElementTreeSelectionDialog dialog= new
	// ElementTreeSelectionDialog(getShell(), labelProvider, provider);
	// dialog.setValidator(validator);
	// dialog.setSorter(new JavaElementSorter());
	// dialog.setTitle(NewWizardMessages.getString("NewContainerWizardPage.ChooseSourceContainerDialog.title"));
	// //$NON-NLS-1$
	// dialog.setMessage(NewWizardMessages.getString("NewContainerWizardPage.ChooseSourceContainerDialog.description"));
	// //$NON-NLS-1$
	// dialog.addFilter(filter);
	// dialog.setInput(JavaCore.create(fWorkspaceRoot));
	// dialog.setInitialSelection(initElement);
	//		
	// if (dialog.open() == ElementTreeSelectionDialog.OK) {
	// Object element= dialog.getFirstResult();
	// if (element instanceof IJavaProject) {
	// IJavaProject jproject= (IJavaProject)element;
	// return jproject.getPackageFragmentRoot(jproject.getProject());
	// } else if (element instanceof IPackageFragmentRoot) {
	// return (IPackageFragmentRoot)element;
	// }
	// return null;
	// }
	// return null;
	// }
}
