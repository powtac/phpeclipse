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

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.phpdt.core.Flags;
import net.sourceforge.phpdt.core.IBuffer;
import net.sourceforge.phpdt.core.ICompilationUnit;
import net.sourceforge.phpdt.core.IJavaElement;
import net.sourceforge.phpdt.core.IPackageFragment;
import net.sourceforge.phpdt.core.ISourceRange;
import net.sourceforge.phpdt.core.IType;
import net.sourceforge.phpdt.core.ToolFactory;
import net.sourceforge.phpdt.core.compiler.IScanner;
import net.sourceforge.phpdt.core.compiler.ITerminalSymbols;
import net.sourceforge.phpdt.core.compiler.InvalidInputException;
import net.sourceforge.phpdt.externaltools.internal.ui.StatusInfo;
import net.sourceforge.phpdt.internal.corext.codemanipulation.StubUtility;
import net.sourceforge.phpdt.internal.corext.template.php.JavaContext;
import net.sourceforge.phpdt.internal.corext.template.php.Templates;
import net.sourceforge.phpdt.internal.corext.util.JavaModelUtil;
import net.sourceforge.phpdt.internal.ui.PHPUiImages;
import net.sourceforge.phpdt.internal.ui.util.SWTUtil;
import net.sourceforge.phpdt.internal.ui.wizards.NewWizardMessages;
import net.sourceforge.phpdt.internal.ui.wizards.dialogfields.DialogField;
import net.sourceforge.phpdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import net.sourceforge.phpdt.internal.ui.wizards.dialogfields.IListAdapter;
import net.sourceforge.phpdt.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import net.sourceforge.phpdt.internal.ui.wizards.dialogfields.LayoutUtil;
import net.sourceforge.phpdt.internal.ui.wizards.dialogfields.ListDialogField;
import net.sourceforge.phpdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import net.sourceforge.phpdt.internal.ui.wizards.dialogfields.SelectionButtonDialogFieldGroup;
import net.sourceforge.phpdt.internal.ui.wizards.dialogfields.Separator;
import net.sourceforge.phpdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import net.sourceforge.phpdt.internal.ui.wizards.dialogfields.StringButtonStatusDialogField;
import net.sourceforge.phpdt.internal.ui.wizards.dialogfields.StringDialogField;
import net.sourceforge.phpdt.ui.CodeGeneration;
import net.sourceforge.phpdt.ui.PreferenceConstants;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * The class <code>NewTypeWizardPage</code> contains controls and validation
 * routines for a 'New Type WizardPage'. Implementors decide which components to
 * add and to enable. Implementors can also customize the validation code.
 * <code>NewTypeWizardPage</code> is intended to serve as base class of all
 * wizards that create types like applets, servlets, classes, interfaces, etc.
 * <p>
 * See <code>NewClassWizardPage</code> or <code>NewInterfaceWizardPage</code>
 * for an example usage of <code>NewTypeWizardPage</code>.
 * </p>
 * 
 * @see net.sourceforge.phpdt.ui.wizards.NewClassWizardPage
 * @see net.sourceforge.phpdt.ui.wizards.NewInterfaceWizardPage
 * 
 * @since 2.0
 */
public abstract class NewTypeWizardPage extends NewContainerWizardPage {

	/**
	 * Class used in stub creation routines to add needed imports to a
	 * compilation unit.
	 */
	// public static class ImportsManager {
	//
	// private IImportsStructure fImportsStructure;
	//
	// /* package */ ImportsManager(IImportsStructure structure) {
	// fImportsStructure= structure;
	// }
	//
	// /* package */ IImportsStructure getImportsStructure() {
	// return fImportsStructure;
	// }
	//				
	// /**
	// * Adds a new import declaration that is sorted in the existing imports.
	// * If an import already exists or the import would conflict with another
	// import
	// * of an other type with the same simple name the import is not added.
	// *
	// * @param qualifiedTypeName The fully qualified name of the type to import
	// * (dot separated)
	// * @return Returns the simple type name that can be used in the code or
	// the
	// * fully qualified type name if an import conflict prevented the import
	// */
	// public String addImport(String qualifiedTypeName) {
	// return fImportsStructure.addImport(qualifiedTypeName);
	// }
	// }
	/**
	 * Public access flag. See The Java Virtual Machine Specification for more
	 * details.
	 */
	public int F_PUBLIC = Flags.AccPublic;

	/**
	 * Private access flag. See The Java Virtual Machine Specification for more
	 * details.
	 */
	public int F_PRIVATE = Flags.AccPrivate;

	/**
	 * Protected access flag. See The Java Virtual Machine Specification for
	 * more details.
	 */
	public int F_PROTECTED = Flags.AccProtected;

	/**
	 * Static access flag. See The Java Virtual Machine Specification for more
	 * details.
	 */
	public int F_STATIC = Flags.AccStatic;

	/**
	 * Final access flag. See The Java Virtual Machine Specification for more
	 * details.
	 */
	public int F_FINAL = Flags.AccFinal;

	/**
	 * Abstract property flag. See The Java Virtual Machine Specification for
	 * more details.
	 */
	// public int F_ABSTRACT = Flags.AccAbstract;
	private final static String PAGE_NAME = "NewTypeWizardPage"; //$NON-NLS-1$

	/** Field ID of the package input field */
	protected final static String PACKAGE = PAGE_NAME + ".package"; //$NON-NLS-1$

	/** Field ID of the eclosing type input field */
	protected final static String ENCLOSING = PAGE_NAME + ".enclosing"; //$NON-NLS-1$

	/** Field ID of the enclosing type checkbox */
	protected final static String ENCLOSINGSELECTION = ENCLOSING + ".selection"; //$NON-NLS-1$

	/** Field ID of the type name input field */
	protected final static String TYPENAME = PAGE_NAME + ".typename"; //$NON-NLS-1$

	/** Field ID of the super type input field */
	protected final static String SUPER = PAGE_NAME + ".superclass"; //$NON-NLS-1$

	/** Field ID of the super interfaces input field */
	protected final static String INTERFACES = PAGE_NAME + ".interfaces"; //$NON-NLS-1$

	/** Field ID of the modifier checkboxes */
	protected final static String MODIFIERS = PAGE_NAME + ".modifiers"; //$NON-NLS-1$

	/** Field ID of the method stubs checkboxes */
	protected final static String METHODS = PAGE_NAME + ".methods"; //$NON-NLS-1$

	private class InterfacesListLabelProvider extends LabelProvider {

		private Image fInterfaceImage;

		public InterfacesListLabelProvider() {
			super();
			fInterfaceImage = PHPUiImages.get(PHPUiImages.IMG_OBJS_INTERFACE);
		}

		public Image getImage(Object element) {
			return fInterfaceImage;
		}
	}

	private StringButtonStatusDialogField fPackageDialogField;

	private SelectionButtonDialogField fEnclosingTypeSelection;

	private StringButtonDialogField fEnclosingTypeDialogField;

	private boolean fCanModifyPackage;

	private boolean fCanModifyEnclosingType;

	private IPackageFragment fCurrPackage;

	// private IType fCurrEnclosingType;
	private StringDialogField fTypeNameDialogField;

	private StringButtonDialogField fSuperClassDialogField;

	private ListDialogField fSuperInterfacesDialogField;

	// private IType fSuperClass;

	private SelectionButtonDialogFieldGroup fAccMdfButtons;

	private SelectionButtonDialogFieldGroup fOtherMdfButtons;

	private IType fCreatedType;

	protected IStatus fEnclosingTypeStatus;

	protected IStatus fPackageStatus;

	protected IStatus fTypeNameStatus;

	// protected IStatus fSuperClassStatus;
	protected IStatus fModifierStatus;

	// protected IStatus fSuperInterfacesStatus;

	private boolean fIsClass;

	private int fStaticMdfIndex;

	private final int PUBLIC_INDEX = 0, DEFAULT_INDEX = 1, PRIVATE_INDEX = 2,
			PROTECTED_INDEX = 3;

	private final int ABSTRACT_INDEX = 0, FINAL_INDEX = 1;

	/**
	 * Creates a new <code>NewTypeWizardPage</code>
	 * 
	 * @param isClass
	 *            <code>true</code> if a new class is to be created; otherwise
	 *            an interface is to be created
	 * @param pageName
	 *            the wizard page's name
	 */
	public NewTypeWizardPage(boolean isClass, String pageName) {
		super(pageName);
		fCreatedType = null;

		fIsClass = isClass;

		TypeFieldsAdapter adapter = new TypeFieldsAdapter();

		fPackageDialogField = new StringButtonStatusDialogField(adapter);
		fPackageDialogField.setDialogFieldListener(adapter);
		fPackageDialogField.setLabelText(NewWizardMessages
				.getString("NewTypeWizardPage.package.label")); //$NON-NLS-1$
		fPackageDialogField.setButtonLabel(NewWizardMessages
				.getString("NewTypeWizardPage.package.button")); //$NON-NLS-1$
		fPackageDialogField.setStatusWidthHint(NewWizardMessages
				.getString("NewTypeWizardPage.default")); //$NON-NLS-1$

		fEnclosingTypeSelection = new SelectionButtonDialogField(SWT.CHECK);
		fEnclosingTypeSelection.setDialogFieldListener(adapter);
		fEnclosingTypeSelection.setLabelText(NewWizardMessages
				.getString("NewTypeWizardPage.enclosing.selection.label")); //$NON-NLS-1$

		fEnclosingTypeDialogField = new StringButtonDialogField(adapter);
		fEnclosingTypeDialogField.setDialogFieldListener(adapter);
		fEnclosingTypeDialogField.setButtonLabel(NewWizardMessages
				.getString("NewTypeWizardPage.enclosing.button")); //$NON-NLS-1$

		fTypeNameDialogField = new StringDialogField();
		fTypeNameDialogField.setDialogFieldListener(adapter);
		fTypeNameDialogField.setLabelText(NewWizardMessages
				.getString("NewTypeWizardPage.typename.label")); //$NON-NLS-1$

		fSuperClassDialogField = new StringButtonDialogField(adapter);
		fSuperClassDialogField.setDialogFieldListener(adapter);
		fSuperClassDialogField.setLabelText(NewWizardMessages
				.getString("NewTypeWizardPage.superclass.label")); //$NON-NLS-1$
		fSuperClassDialogField.setButtonLabel(NewWizardMessages
				.getString("NewTypeWizardPage.superclass.button")); //$NON-NLS-1$

		String[] addButtons = new String[] {
				/* 0 */
				NewWizardMessages.getString("NewTypeWizardPage.interfaces.add"), //$NON-NLS-1$
				/* 1 */
				null,
				/* 2 */
				NewWizardMessages
						.getString("NewTypeWizardPage.interfaces.remove") //$NON-NLS-1$
		};
		fSuperInterfacesDialogField = new ListDialogField(adapter, addButtons,
				new InterfacesListLabelProvider());
		fSuperInterfacesDialogField.setDialogFieldListener(adapter);
		String interfaceLabel = fIsClass ? NewWizardMessages
				.getString("NewTypeWizardPage.interfaces.class.label") : NewWizardMessages.getString("NewTypeWizardPage.interfaces.ifc.label"); //$NON-NLS-1$ //$NON-NLS-2$
		fSuperInterfacesDialogField.setLabelText(interfaceLabel);
		fSuperInterfacesDialogField.setRemoveButtonIndex(2);

		String[] buttonNames1 = new String[] {
				/* 0 == PUBLIC_INDEX */
				NewWizardMessages
						.getString("NewTypeWizardPage.modifiers.public"), //$NON-NLS-1$
				/* 1 == DEFAULT_INDEX */
				NewWizardMessages
						.getString("NewTypeWizardPage.modifiers.default"), //$NON-NLS-1$
				/* 2 == PRIVATE_INDEX */
				NewWizardMessages
						.getString("NewTypeWizardPage.modifiers.private"), //$NON-NLS-1$
				/* 3 == PROTECTED_INDEX */
				NewWizardMessages
						.getString("NewTypeWizardPage.modifiers.protected") //$NON-NLS-1$
		};
		fAccMdfButtons = new SelectionButtonDialogFieldGroup(SWT.RADIO,
				buttonNames1, 4);
		fAccMdfButtons.setDialogFieldListener(adapter);
		fAccMdfButtons.setLabelText(NewWizardMessages
				.getString("NewTypeWizardPage.modifiers.acc.label")); //$NON-NLS-1$
		fAccMdfButtons.setSelection(0, true);

		String[] buttonNames2;
		if (fIsClass) {
			buttonNames2 = new String[] {
					/* 0 == ABSTRACT_INDEX */
					NewWizardMessages
							.getString("NewTypeWizardPage.modifiers.abstract"), //$NON-NLS-1$
					/* 1 == FINAL_INDEX */
					NewWizardMessages
							.getString("NewTypeWizardPage.modifiers.final"), //$NON-NLS-1$
					/* 2 */
					NewWizardMessages
							.getString("NewTypeWizardPage.modifiers.static") //$NON-NLS-1$
			};
			fStaticMdfIndex = 2; // index of the static checkbox is 2
		} else {
			buttonNames2 = new String[] { NewWizardMessages
					.getString("NewTypeWizardPage.modifiers.static") //$NON-NLS-1$
			};
			fStaticMdfIndex = 0; // index of the static checkbox is 0
		}

		fOtherMdfButtons = new SelectionButtonDialogFieldGroup(SWT.CHECK,
				buttonNames2, 4);
		fOtherMdfButtons.setDialogFieldListener(adapter);

		fAccMdfButtons.enableSelectionButton(PRIVATE_INDEX, false);
		fAccMdfButtons.enableSelectionButton(PROTECTED_INDEX, false);
		fOtherMdfButtons.enableSelectionButton(fStaticMdfIndex, false);

		fPackageStatus = new StatusInfo();
		fEnclosingTypeStatus = new StatusInfo();

		fCanModifyPackage = true;
		fCanModifyEnclosingType = true;
		updateEnableState();

		fTypeNameStatus = new StatusInfo();
		// fSuperClassStatus= new StatusInfo();
		// fSuperInterfacesStatus= new StatusInfo();
		fModifierStatus = new StatusInfo();
	}

	/**
	 * Initializes all fields provided by the page with a given selection.
	 * 
	 * @param elem
	 *            the selection used to intialize this page or <code>
	 * null</code>
	 *            if no selection was available
	 */
	protected void initTypePage(IJavaElement elem) {
		String initSuperclass = "java.lang.Object"; //$NON-NLS-1$
		ArrayList initSuperinterfaces = new ArrayList(5);

		IPackageFragment pack = null;
		IType enclosingType = null;

		if (elem != null) {
			// evaluate the enclosing type
			pack = (IPackageFragment) elem
					.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
			IType typeInCU = (IType) elem.getAncestor(IJavaElement.TYPE);
			if (typeInCU != null) {
				if (typeInCU.getCompilationUnit() != null) {
					enclosingType = typeInCU;
				}
			} else {
				ICompilationUnit cu = (ICompilationUnit) elem
						.getAncestor(IJavaElement.COMPILATION_UNIT);
				if (cu != null) {
					// enclosingType= cu.findPrimaryType();
				}
			}

			// try {
			// IType type= null;
			// if (elem.getElementType() == IJavaElement.TYPE) {
			// type= (IType)elem;
			// if (type.exists()) {
			// String superName= JavaModelUtil.getFullyQualifiedName(type);
			// if (type.isInterface()) {
			// initSuperinterfaces.add(superName);
			// } else {
			// initSuperclass= superName;
			// }
			// }
			// }
			// } catch (JavaModelException e) {
			// PHPeclipsePlugin.log(e);
			// // ignore this exception now
			// }
		}

		setPackageFragment(pack, true);
		// setEnclosingType(enclosingType, true);
		setEnclosingTypeSelection(false, true);

		setTypeName("", true); //$NON-NLS-1$
		setSuperClass(initSuperclass, true);
		setSuperInterfaces(initSuperinterfaces, true);
	}

	// -------- UI Creation ---------

	/**
	 * Creates a separator line. Expects a <code>GridLayout</code> with at
	 * least 1 column.
	 * 
	 * @param composite
	 *            the parent composite
	 * @param nColumns
	 *            number of columns to span
	 */
	protected void createSeparator(Composite composite, int nColumns) {
		(new Separator(SWT.SEPARATOR | SWT.HORIZONTAL)).doFillIntoGrid(
				composite, nColumns, convertHeightInCharsToPixels(1));
	}

	/**
	 * Creates the controls for the package name field. Expects a
	 * <code>GridLayout</code> with at least 4 columns.
	 * 
	 * @param composite
	 *            the parent composite
	 * @param nColumns
	 *            number of columns to span
	 */
	protected void createPackageControls(Composite composite, int nColumns) {
		fPackageDialogField.doFillIntoGrid(composite, nColumns);
		LayoutUtil.setWidthHint(fPackageDialogField.getTextControl(null),
				getMaxFieldWidth());
		LayoutUtil.setHorizontalGrabbing(fPackageDialogField
				.getTextControl(null));
	}

	/**
	 * Creates the controls for the enclosing type name field. Expects a
	 * <code>GridLayout</code> with at least 4 columns.
	 * 
	 * @param composite
	 *            the parent composite
	 * @param nColumns
	 *            number of columns to span
	 */
	protected void createEnclosingTypeControls(Composite composite, int nColumns) {
		// #6891
		Composite tabGroup = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		tabGroup.setLayout(layout);

		fEnclosingTypeSelection.doFillIntoGrid(tabGroup, 1);

		Control c = fEnclosingTypeDialogField.getTextControl(composite);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = getMaxFieldWidth();
		gd.horizontalSpan = 2;
		c.setLayoutData(gd);

		Button button = fEnclosingTypeDialogField.getChangeControl(composite);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.heightHint = SWTUtil.getButtonHeightHint(button);
		gd.widthHint = SWTUtil.getButtonWidthHint(button);
		button.setLayoutData(gd);
	}

	/**
	 * Creates the controls for the type name field. Expects a
	 * <code>GridLayout</code> with at least 2 columns.
	 * 
	 * @param composite
	 *            the parent composite
	 * @param nColumns
	 *            number of columns to span
	 */
	protected void createTypeNameControls(Composite composite, int nColumns) {
		fTypeNameDialogField.doFillIntoGrid(composite, nColumns - 1);
		DialogField.createEmptySpace(composite);

		LayoutUtil.setWidthHint(fTypeNameDialogField.getTextControl(null),
				getMaxFieldWidth());
	}

	/**
	 * Creates the controls for the modifiers radio/ceckbox buttons. Expects a
	 * <code>GridLayout</code> with at least 3 columns.
	 * 
	 * @param composite
	 *            the parent composite
	 * @param nColumns
	 *            number of columns to span
	 */
	protected void createModifierControls(Composite composite, int nColumns) {
		LayoutUtil.setHorizontalSpan(fAccMdfButtons.getLabelControl(composite),
				1);

		Control control = fAccMdfButtons.getSelectionButtonsGroup(composite);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = nColumns - 2;
		control.setLayoutData(gd);

		DialogField.createEmptySpace(composite);

		DialogField.createEmptySpace(composite);

		control = fOtherMdfButtons.getSelectionButtonsGroup(composite);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = nColumns - 2;
		control.setLayoutData(gd);

		DialogField.createEmptySpace(composite);
	}

	/**
	 * Creates the controls for the superclass name field. Expects a
	 * <code>GridLayout</code> with at least 3 columns.
	 * 
	 * @param composite
	 *            the parent composite
	 * @param nColumns
	 *            number of columns to span
	 */
	protected void createSuperClassControls(Composite composite, int nColumns) {
		fSuperClassDialogField.doFillIntoGrid(composite, nColumns);
		LayoutUtil.setWidthHint(fSuperClassDialogField.getTextControl(null),
				getMaxFieldWidth());
	}

	/**
	 * Creates the controls for the superclass name field. Expects a
	 * <code>GridLayout</code> with at least 3 columns.
	 * 
	 * @param composite
	 *            the parent composite
	 * @param nColumns
	 *            number of columns to span
	 */
	protected void createSuperInterfacesControls(Composite composite,
			int nColumns) {
		fSuperInterfacesDialogField.doFillIntoGrid(composite, nColumns);
		GridData gd = (GridData) fSuperInterfacesDialogField.getListControl(
				null).getLayoutData();
		if (fIsClass) {
			gd.heightHint = convertHeightInCharsToPixels(3);
		} else {
			gd.heightHint = convertHeightInCharsToPixels(6);
		}
		gd.grabExcessVerticalSpace = false;
		gd.widthHint = getMaxFieldWidth();
	}

	/**
	 * Sets the focus on the type name input field.
	 */
	protected void setFocus() {
		fTypeNameDialogField.setFocus();
	}

	// -------- TypeFieldsAdapter --------

	private class TypeFieldsAdapter implements IStringButtonAdapter,
			IDialogFieldListener, IListAdapter {

		// -------- IStringButtonAdapter
		public void changeControlPressed(DialogField field) {
			// typePageChangeControlPressed(field);
		}

		// -------- IListAdapter
		public void customButtonPressed(ListDialogField field, int index) {
			// typePageCustomButtonPressed(field, index);
		}

		public void selectionChanged(ListDialogField field) {
		}

		// -------- IDialogFieldListener
		public void dialogFieldChanged(DialogField field) {
			typePageDialogFieldChanged(field);
		}

		public void doubleClicked(ListDialogField field) {
		}
	}

	// private void typePageChangeControlPressed(DialogField field) {
	// if (field == fPackageDialogField) {
	// IPackageFragment pack= choosePackage();
	// if (pack != null) {
	// fPackageDialogField.setText(pack.getElementName());
	// }
	// } else if (field == fEnclosingTypeDialogField) {
	// IType type= chooseEnclosingType();
	// if (type != null) {
	// fEnclosingTypeDialogField.setText(JavaModelUtil.getFullyQualifiedName(type));
	// }
	// } else if (field == fSuperClassDialogField) {
	// IType type= chooseSuperType();
	// if (type != null) {
	// fSuperClassDialogField.setText(JavaModelUtil.getFullyQualifiedName(type));
	// }
	// }
	// }

	// private void typePageCustomButtonPressed(DialogField field, int index) {
	// if (field == fSuperInterfacesDialogField) {
	// chooseSuperInterfaces();
	// }
	// }

	/*
	 * A field on the type has changed. The fields' status and all dependend
	 * status are updated.
	 */
	private void typePageDialogFieldChanged(DialogField field) {
		String fieldName = null;
		if (field == fPackageDialogField) {
			fPackageStatus = packageChanged();
			updatePackageStatusLabel();
			fTypeNameStatus = typeNameChanged();
			// fSuperClassStatus= superClassChanged();
			fieldName = PACKAGE;
		} else if (field == fEnclosingTypeDialogField) {
			// fEnclosingTypeStatus= enclosingTypeChanged();
			fTypeNameStatus = typeNameChanged();
			// fSuperClassStatus= superClassChanged();
			fieldName = ENCLOSING;
		} else if (field == fEnclosingTypeSelection) {
			updateEnableState();
			boolean isEnclosedType = isEnclosingTypeSelected();
			if (!isEnclosedType) {
				if (fAccMdfButtons.isSelected(PRIVATE_INDEX)
						|| fAccMdfButtons.isSelected(PROTECTED_INDEX)) {
					fAccMdfButtons.setSelection(PRIVATE_INDEX, false);
					fAccMdfButtons.setSelection(PROTECTED_INDEX, false);
					fAccMdfButtons.setSelection(PUBLIC_INDEX, true);
				}
				if (fOtherMdfButtons.isSelected(fStaticMdfIndex)) {
					fOtherMdfButtons.setSelection(fStaticMdfIndex, false);
				}
			}
			fAccMdfButtons.enableSelectionButton(PRIVATE_INDEX, isEnclosedType
					&& fIsClass);
			fAccMdfButtons.enableSelectionButton(PROTECTED_INDEX,
					isEnclosedType && fIsClass);
			fOtherMdfButtons.enableSelectionButton(fStaticMdfIndex,
					isEnclosedType);
			fTypeNameStatus = typeNameChanged();
			// fSuperClassStatus= superClassChanged();
			fieldName = ENCLOSINGSELECTION;
		} else if (field == fTypeNameDialogField) {
			fTypeNameStatus = typeNameChanged();
			fieldName = TYPENAME;
		} else if (field == fSuperClassDialogField) {
			// fSuperClassStatus= superClassChanged();
			fieldName = SUPER;
		} else if (field == fSuperInterfacesDialogField) {
			// fSuperInterfacesStatus= superInterfacesChanged();
			fieldName = INTERFACES;
		} else if (field == fOtherMdfButtons) {
			fModifierStatus = modifiersChanged();
			fieldName = MODIFIERS;
		} else {
			fieldName = METHODS;
		}
		// tell all others
		handleFieldChanged(fieldName);
	}

	// -------- update message ----------------

	/*
	 * @see net.sourceforge.phpdt.ui.wizards.NewContainerWizardPage#handleFieldChanged(String)
	 */
	protected void handleFieldChanged(String fieldName) {
		super.handleFieldChanged(fieldName);
		if (fieldName == CONTAINER) {
			fPackageStatus = packageChanged();
			// fEnclosingTypeStatus= enclosingTypeChanged();
			fTypeNameStatus = typeNameChanged();
			// fSuperClassStatus= superClassChanged();
			// fSuperInterfacesStatus= superInterfacesChanged();
		}
	}

	// ---- set / get ----------------

	/**
	 * Returns the text of the package input field.
	 * 
	 * @return the text of the package input field
	 */
	public String getPackageText() {
		return fPackageDialogField.getText();
	}

	/**
	 * Returns the text of the enclosing type input field.
	 * 
	 * @return the text of the enclosing type input field
	 */
	public String getEnclosingTypeText() {
		return fEnclosingTypeDialogField.getText();
	}

	/**
	 * Returns the package fragment corresponding to the current input.
	 * 
	 * @return a package fragement or <code>null</code> if the input could not
	 *         be resolved.
	 */
	public IPackageFragment getPackageFragment() {
		if (!isEnclosingTypeSelected()) {
			return fCurrPackage;
		} else {
			// if (fCurrEnclosingType != null) {
			// return fCurrEnclosingType.getPackageFragment();
			// }
		}
		return null;
	}

	/**
	 * Sets the package fragment to the given value. The method updates the
	 * model and the text of the control.
	 * 
	 * @param pack
	 *            the package fragement to be set
	 * @param canBeModified
	 *            if <code>true</code> the package fragment is editable;
	 *            otherwise it is read-only.
	 */
	public void setPackageFragment(IPackageFragment pack, boolean canBeModified) {
		fCurrPackage = pack;
		fCanModifyPackage = canBeModified;
		String str = (pack == null) ? "" : pack.getElementName(); //$NON-NLS-1$
		fPackageDialogField.setText(str);
		updateEnableState();
	}

	/**
	 * Returns the enclosing type corresponding to the current input.
	 * 
	 * @return the enclosing type or <code>null</code> if the enclosing type
	 *         is not selected or the input could not be resolved
	 */
	public IType getEnclosingType() {
		// if (isEnclosingTypeSelected()) {
		// return fCurrEnclosingType;
		// }
		return null;
	}

	/**
	 * Sets the enclosing type. The method updates the underlying model and the
	 * text of the control.
	 * 
	 * @param type
	 *            the enclosing type
	 * @param canBeModified
	 *            if <code>true</code> the enclosing type field is editable;
	 *            otherwise it is read-only.
	 */
	// public void setEnclosingType(IType type, boolean canBeModified) {
	// fCurrEnclosingType= type;
	// fCanModifyEnclosingType= canBeModified;
	// String str= (type == null) ? "" :
	// JavaModelUtil.getFullyQualifiedName(type); //$NON-NLS-1$
	// fEnclosingTypeDialogField.setText(str);
	// updateEnableState();
	// }
	/**
	 * Returns the selection state of the enclosing type checkbox.
	 * 
	 * @return the seleciton state of the enclosing type checkbox
	 */
	public boolean isEnclosingTypeSelected() {
		return fEnclosingTypeSelection.isSelected();
	}

	/**
	 * Sets the enclosing type checkbox's selection state.
	 * 
	 * @param isSelected
	 *            the checkbox's selection state
	 * @param canBeModified
	 *            if <code>true</code> the enclosing type checkbox is
	 *            modifiable; otherwise it is read-only.
	 */
	public void setEnclosingTypeSelection(boolean isSelected,
			boolean canBeModified) {
		fEnclosingTypeSelection.setSelection(isSelected);
		fEnclosingTypeSelection.setEnabled(canBeModified);
		updateEnableState();
	}

	/**
	 * Returns the type name entered into the type input field.
	 * 
	 * @return the type name
	 */
	public String getTypeName() {
		return fTypeNameDialogField.getText();
	}

	/**
	 * Sets the type name input field's text to the given value. Method doesn't
	 * update the model.
	 * 
	 * @param name
	 *            the new type name
	 * @param canBeModified
	 *            if <code>true</code> the enclosing type name field is
	 *            editable; otherwise it is read-only.
	 */
	public void setTypeName(String name, boolean canBeModified) {
		fTypeNameDialogField.setText(name);
		fTypeNameDialogField.setEnabled(canBeModified);
	}

	/**
	 * Returns the selected modifiers.
	 * 
	 * @return the selected modifiers
	 * @see Flags
	 */
	public int getModifiers() {
		int mdf = 0;
		if (fAccMdfButtons.isSelected(PUBLIC_INDEX)) {
			mdf += F_PUBLIC;
		} else if (fAccMdfButtons.isSelected(PRIVATE_INDEX)) {
			mdf += F_PRIVATE;
		} else if (fAccMdfButtons.isSelected(PROTECTED_INDEX)) {
			mdf += F_PROTECTED;
		}
		// if (fOtherMdfButtons.isSelected(ABSTRACT_INDEX) && (fStaticMdfIndex
		// != 0)) {
		// mdf+= F_ABSTRACT;
		// }
		if (fOtherMdfButtons.isSelected(FINAL_INDEX)) {
			mdf += F_FINAL;
		}
		if (fOtherMdfButtons.isSelected(fStaticMdfIndex)) {
			mdf += F_STATIC;
		}
		return mdf;
	}

	/**
	 * Sets the modifiers.
	 * 
	 * @param modifiers
	 *            <code>F_PUBLIC</code>, <code>F_PRIVATE</code>,
	 *            <code>F_PROTECTED</code>, <code>F_ABSTRACT, F_FINAL</code>
	 *            or <code>F_STATIC</code> or, a valid combination.
	 * @param canBeModified
	 *            if <code>true</code> the modifier fields are editable;
	 *            otherwise they are read-only
	 * @see Flags
	 */
	public void setModifiers(int modifiers, boolean canBeModified) {
		if (Flags.isPublic(modifiers)) {
			fAccMdfButtons.setSelection(PUBLIC_INDEX, true);
		} else if (Flags.isPrivate(modifiers)) {
			fAccMdfButtons.setSelection(PRIVATE_INDEX, true);
		} else if (Flags.isProtected(modifiers)) {
			fAccMdfButtons.setSelection(PROTECTED_INDEX, true);
		} else {
			fAccMdfButtons.setSelection(DEFAULT_INDEX, true);
		}
		// if (Flags.isAbstract(modifiers)) {
		// fOtherMdfButtons.setSelection(ABSTRACT_INDEX, true);
		// }
		if (Flags.isFinal(modifiers)) {
			fOtherMdfButtons.setSelection(FINAL_INDEX, true);
		}
		if (Flags.isStatic(modifiers)) {
			fOtherMdfButtons.setSelection(fStaticMdfIndex, true);
		}

		fAccMdfButtons.setEnabled(canBeModified);
		fOtherMdfButtons.setEnabled(canBeModified);
	}

	/**
	 * Returns the content of the superclass input field.
	 * 
	 * @return the superclass name
	 */
	public String getSuperClass() {
		return fSuperClassDialogField.getText();
	}

	/**
	 * Sets the super class name.
	 * 
	 * @param name
	 *            the new superclass name
	 * @param canBeModified
	 *            if <code>true</code> the superclass name field is editable;
	 *            otherwise it is read-only.
	 */
	public void setSuperClass(String name, boolean canBeModified) {
		fSuperClassDialogField.setText(name);
		fSuperClassDialogField.setEnabled(canBeModified);
	}

	/**
	 * Returns the chosen super interfaces.
	 * 
	 * @return a list of chosen super interfaces. The list's elements are of
	 *         type <code>String</code>
	 */
	public List getSuperInterfaces() {
		return fSuperInterfacesDialogField.getElements();
	}

	/**
	 * Sets the super interfaces.
	 * 
	 * @param interfacesNames
	 *            a list of super interface. The method requires that the list's
	 *            elements are of type <code>String</code>
	 * @param canBeModified
	 *            if <code>true</code> the super interface field is editable;
	 *            otherwise it is read-only.
	 */
	public void setSuperInterfaces(List interfacesNames, boolean canBeModified) {
		fSuperInterfacesDialogField.setElements(interfacesNames);
		fSuperInterfacesDialogField.setEnabled(canBeModified);
	}

	// ----------- validation ----------

	/**
	 * A hook method that gets called when the package field has changed. The
	 * method validates the package name and returns the status of the
	 * validation. The validation also updates the package fragment model.
	 * <p>
	 * Subclasses may extend this method to perform their own validation.
	 * </p>
	 * 
	 * @return the status of the validation
	 */
	protected IStatus packageChanged() {
		StatusInfo status = new StatusInfo();
		fPackageDialogField.enableButton(getPackageFragmentRoot() != null);

		// String packName= getPackageText();
		// if (packName.length() > 0) {
		// IStatus val= JavaConventions.validatePackageName(packName);
		// if (val.getSeverity() == IStatus.ERROR) {
		// status.setError(NewWizardMessages.getFormattedString("NewTypeWizardPage.error.InvalidPackageName",
		// val.getMessage())); //$NON-NLS-1$
		// return status;
		// } else if (val.getSeverity() == IStatus.WARNING) {
		// status.setWarning(NewWizardMessages.getFormattedString("NewTypeWizardPage.warning.DiscouragedPackageName",
		// val.getMessage())); //$NON-NLS-1$
		// // continue
		// }
		// }

		// IPackageFragmentRoot root= getPackageFragmentRoot();
		// if (root != null) {
		// if (root.getJavaProject().exists() && packName.length() > 0) {
		// try {
		// IPath rootPath= root.getPath();
		// IPath outputPath= root.getJavaProject().getOutputLocation();
		// if (rootPath.isPrefixOf(outputPath) && !rootPath.equals(outputPath))
		// {
		// // if the bin folder is inside of our root, dont allow to name a
		// package
		// // like the bin folder
		// IPath packagePath= rootPath.append(packName.replace('.', '/'));
		// if (outputPath.isPrefixOf(packagePath)) {
		// status.setError(NewWizardMessages.getString("NewTypeWizardPage.error.ClashOutputLocation"));
		// //$NON-NLS-1$
		// return status;
		// }
		// }
		// } catch (JavaModelException e) {
		// PHPeclipsePlugin.log(e);
		// // let pass
		// }
		// }
		//			
		// fCurrPackage= root.getPackageFragment(packName);
		// } else {
		// status.setError(""); //$NON-NLS-1$
		// }
		return status;
	}

	/*
	 * Updates the 'default' label next to the package field.
	 */
	private void updatePackageStatusLabel() {
		String packName = getPackageText();

		if (packName.length() == 0) {
			fPackageDialogField.setStatus(NewWizardMessages
					.getString("NewTypeWizardPage.default")); //$NON-NLS-1$
		} else {
			fPackageDialogField.setStatus(""); //$NON-NLS-1$
		}
	}

	/*
	 * Updates the enable state of buttons related to the enclosing type
	 * selection checkbox.
	 */
	private void updateEnableState() {
		boolean enclosing = isEnclosingTypeSelected();
		fPackageDialogField.setEnabled(fCanModifyPackage && !enclosing);
		fEnclosingTypeDialogField.setEnabled(fCanModifyEnclosingType
				&& enclosing);
	}

	/**
	 * Hook method that gets called when the enclosing type name has changed.
	 * The method validates the enclosing type and returns the status of the
	 * validation. It also updates the enclosing type model.
	 * <p>
	 * Subclasses may extend this method to perform their own validation.
	 * </p>
	 * 
	 * @return the status of the validation
	 */
	// protected IStatus enclosingTypeChanged() {
	// StatusInfo status= new StatusInfo();
	// fCurrEnclosingType= null;
	//		
	// IPackageFragmentRoot root= getPackageFragmentRoot();
	//		
	// fEnclosingTypeDialogField.enableButton(root != null);
	// if (root == null) {
	// status.setError(""); //$NON-NLS-1$
	// return status;
	// }
	//		
	// String enclName= getEnclosingTypeText();
	// if (enclName.length() == 0) {
	// status.setError(NewWizardMessages.getString("NewTypeWizardPage.error.EnclosingTypeEnterName"));
	// //$NON-NLS-1$
	// return status;
	// }
	// try {
	// IType type= findType(root.getJavaProject(), enclName);
	// if (type == null) {
	// status.setError(NewWizardMessages.getString("NewTypeWizardPage.error.EnclosingTypeNotExists"));
	// //$NON-NLS-1$
	// return status;
	// }
	//
	// if (type.getCompilationUnit() == null) {
	// status.setError(NewWizardMessages.getString("NewTypeWizardPage.error.EnclosingNotInCU"));
	// //$NON-NLS-1$
	// return status;
	// }
	// if (!JavaModelUtil.isEditable(type.getCompilationUnit())) {
	// status.setError(NewWizardMessages.getString("NewTypeWizardPage.error.EnclosingNotEditable"));
	// //$NON-NLS-1$
	// return status;
	// }
	//			
	// fCurrEnclosingType= type;
	// IPackageFragmentRoot enclosingRoot=
	// JavaModelUtil.getPackageFragmentRoot(type);
	// if (!enclosingRoot.equals(root)) {
	// status.setWarning(NewWizardMessages.getString("NewTypeWizardPage.warning.EnclosingNotInSourceFolder"));
	// //$NON-NLS-1$
	// }
	// return status;
	// } catch (JavaModelException e) {
	// status.setError(NewWizardMessages.getString("NewTypeWizardPage.error.EnclosingTypeNotExists"));
	// //$NON-NLS-1$
	// PHPeclipsePlugin.log(e);
	// return status;
	// }
	// }
	/**
	 * Hook method that gets called when the type name has changed. The method
	 * validates the type name and returns the status of the validation.
	 * <p>
	 * Subclasses may extend this method to perform their own validation.
	 * </p>
	 * 
	 * @return the status of the validation
	 */
	protected IStatus typeNameChanged() {
		StatusInfo status = new StatusInfo();
		String typeName = getTypeName();
		// must not be empty
		if (typeName.length() == 0) {
			status.setError(NewWizardMessages
					.getString("NewTypeWizardPage.error.EnterTypeName")); //$NON-NLS-1$
			return status;
		}
		if (typeName.indexOf('.') != -1) {
			status.setError(NewWizardMessages
					.getString("NewTypeWizardPage.error.QualifiedName")); //$NON-NLS-1$
			return status;
		}
		// IStatus val= JavaConventions.validateJavaTypeName(typeName);
		// if (val.getSeverity() == IStatus.ERROR) {
		// status.setError(NewWizardMessages.getFormattedString("NewTypeWizardPage.error.InvalidTypeName",
		// val.getMessage())); //$NON-NLS-1$
		// return status;
		// } else if (val.getSeverity() == IStatus.WARNING) {
		// status.setWarning(NewWizardMessages.getFormattedString("NewTypeWizardPage.warning.TypeNameDiscouraged",
		// val.getMessage())); //$NON-NLS-1$
		// // continue checking
		// }

		// must not exist
		if (!isEnclosingTypeSelected()) {
			IPackageFragment pack = getPackageFragment();
			if (pack != null) {
				ICompilationUnit cu = pack.getCompilationUnit(typeName
						+ ".java"); //$NON-NLS-1$
				if (cu.getResource().exists()) {
					status
							.setError(NewWizardMessages
									.getString("NewTypeWizardPage.error.TypeNameExists")); //$NON-NLS-1$
					return status;
				}
			}
		} else {
			IType type = getEnclosingType();
			if (type != null) {
				IType member = type.getType(typeName);
				if (member.exists()) {
					status
							.setError(NewWizardMessages
									.getString("NewTypeWizardPage.error.TypeNameExists")); //$NON-NLS-1$
					return status;
				}
			}
		}
		return status;
	}

	/**
	 * Hook method that gets called when the superclass name has changed. The
	 * method validates the superclass name and returns the status of the
	 * validation.
	 * <p>
	 * Subclasses may extend this method to perform their own validation.
	 * </p>
	 * 
	 * @return the status of the validation
	 */
	// protected IStatus superClassChanged() {
	// StatusInfo status= new StatusInfo();
	// IPackageFragmentRoot root= getPackageFragmentRoot();
	// fSuperClassDialogField.enableButton(root != null);
	//		
	// fSuperClass= null;
	//		
	// String sclassName= getSuperClass();
	// if (sclassName.length() == 0) {
	// // accept the empty field (stands for java.lang.Object)
	// return status;
	// }
	// IStatus val= JavaConventions.validateJavaTypeName(sclassName);
	// if (val.getSeverity() == IStatus.ERROR) {
	// status.setError(NewWizardMessages.getString("NewTypeWizardPage.error.InvalidSuperClassName"));
	// //$NON-NLS-1$
	// return status;
	// }
	// if (root != null) {
	// try {
	// IType type= resolveSuperTypeName(root.getJavaProject(), sclassName);
	// if (type == null) {
	// status.setWarning(NewWizardMessages.getString("NewTypeWizardPage.warning.SuperClassNotExists"));
	// //$NON-NLS-1$
	// return status;
	// } else {
	// if (type.isInterface()) {
	// status.setWarning(NewWizardMessages.getFormattedString("NewTypeWizardPage.warning.SuperClassIsNotClass",
	// sclassName)); //$NON-NLS-1$
	// return status;
	// }
	// int flags= type.getFlags();
	// if (Flags.isFinal(flags)) {
	// status.setWarning(NewWizardMessages.getFormattedString("NewTypeWizardPage.warning.SuperClassIsFinal",
	// sclassName)); //$NON-NLS-1$
	// return status;
	// } else if (!JavaModelUtil.isVisible(type, getPackageFragment())) {
	// status.setWarning(NewWizardMessages.getFormattedString("NewTypeWizardPage.warning.SuperClassIsNotVisible",
	// sclassName)); //$NON-NLS-1$
	// return status;
	// }
	// }
	// fSuperClass= type;
	// } catch (JavaModelException e) {
	// status.setError(NewWizardMessages.getString("NewTypeWizardPage.error.InvalidSuperClassName"));
	// //$NON-NLS-1$
	// PHPeclipsePlugin.log(e);
	// }
	// } else {
	// status.setError(""); //$NON-NLS-1$
	// }
	// return status;
	//		
	// }
	// private IType resolveSuperTypeName(IJavaProject jproject, String
	// sclassName) throws JavaModelException {
	// if (!jproject.exists()) {
	// return null;
	// }
	// IType type= null;
	// if (isEnclosingTypeSelected()) {
	// // search in the context of the enclosing type
	// IType enclosingType= getEnclosingType();
	// if (enclosingType != null) {
	// String[][] res= enclosingType.resolveType(sclassName);
	// if (res != null && res.length > 0) {
	// type= jproject.findType(res[0][0], res[0][1]);
	// }
	// }
	// } else {
	// IPackageFragment currPack= getPackageFragment();
	// if (type == null && currPack != null) {
	// String packName= currPack.getElementName();
	// // search in own package
	// if (!currPack.isDefaultPackage()) {
	// type= jproject.findType(packName, sclassName);
	// }
	// // search in java.lang
	// if (type == null && !"java.lang".equals(packName)) { //$NON-NLS-1$
	// type= jproject.findType("java.lang", sclassName); //$NON-NLS-1$
	// }
	// }
	// // search fully qualified
	// if (type == null) {
	// type= jproject.findType(sclassName);
	// }
	// }
	// return type;
	// }
	// private IType findType(IJavaProject project, String typeName) throws
	// JavaModelException {
	// if (project.exists()) {
	// return project.findType(typeName);
	// }
	// return null;
	// }
	/**
	 * Hook method that gets called when the list of super interface has
	 * changed. The method validates the superinterfaces and returns the status
	 * of the validation.
	 * <p>
	 * Subclasses may extend this method to perform their own validation.
	 * </p>
	 * 
	 * @return the status of the validation
	 */
	// protected IStatus superInterfacesChanged() {
	// StatusInfo status= new StatusInfo();
	//		
	// IPackageFragmentRoot root= getPackageFragmentRoot();
	// fSuperInterfacesDialogField.enableButton(0, root != null);
	//						
	// if (root != null) {
	// List elements= fSuperInterfacesDialogField.getElements();
	// int nElements= elements.size();
	// for (int i= 0; i < nElements; i++) {
	// String intfname= (String)elements.get(i);
	// try {
	// IType type= findType(root.getJavaProject(), intfname);
	// if (type == null) {
	// status.setWarning(NewWizardMessages.getFormattedString("NewTypeWizardPage.warning.InterfaceNotExists",
	// intfname)); //$NON-NLS-1$
	// return status;
	// } else {
	// if (type.isClass()) {
	// status.setWarning(NewWizardMessages.getFormattedString("NewTypeWizardPage.warning.InterfaceIsNotInterface",
	// intfname)); //$NON-NLS-1$
	// return status;
	// }
	// if (!JavaModelUtil.isVisible(type, getPackageFragment())) {
	// status.setWarning(NewWizardMessages.getFormattedString("NewTypeWizardPage.warning.InterfaceIsNotVisible",
	// intfname)); //$NON-NLS-1$
	// return status;
	// }
	// }
	// } catch (JavaModelException e) {
	// PHPeclipsePlugin.log(e);
	// // let pass, checking is an extra
	// }
	// }
	// }
	// return status;
	// }
	/**
	 * Hook method that gets called when the modifiers have changed. The method
	 * validates the modifiers and returns the status of the validation.
	 * <p>
	 * Subclasses may extend this method to perform their own validation.
	 * </p>
	 * 
	 * @return the status of the validation
	 */
	protected IStatus modifiersChanged() {
		StatusInfo status = new StatusInfo();
		int modifiers = getModifiers();
		// if (Flags.isFinal(modifiers) && Flags.isAbstract(modifiers)) {
		// status.setError(NewWizardMessages.getString("NewTypeWizardPage.error.ModifiersFinalAndAbstract"));
		// //$NON-NLS-1$
		// }
		return status;
	}

	// selection dialogs

	// private IPackageFragment choosePackage() {
	// IPackageFragmentRoot froot= getPackageFragmentRoot();
	// IJavaElement[] packages= null;
	// try {
	// if (froot != null && froot.exists()) {
	// packages= froot.getChildren();
	// }
	// } catch (JavaModelException e) {
	// PHPeclipsePlugin.log(e);
	// }
	// if (packages == null) {
	// packages= new IJavaElement[0];
	// }
	//		
	// ElementListSelectionDialog dialog= new
	// ElementListSelectionDialog(getShell(), new
	// JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT));
	// dialog.setIgnoreCase(false);
	// dialog.setTitle(NewWizardMessages.getString("NewTypeWizardPage.ChoosePackageDialog.title"));
	// //$NON-NLS-1$
	// dialog.setMessage(NewWizardMessages.getString("NewTypeWizardPage.ChoosePackageDialog.description"));
	// //$NON-NLS-1$
	// dialog.setEmptyListMessage(NewWizardMessages.getString("NewTypeWizardPage.ChoosePackageDialog.empty"));
	// //$NON-NLS-1$
	// dialog.setElements(packages);
	// IPackageFragment pack= getPackageFragment();
	// if (pack != null) {
	// dialog.setInitialSelections(new Object[] { pack });
	// }
	//
	// if (dialog.open() == ElementListSelectionDialog.OK) {
	// return (IPackageFragment) dialog.getFirstResult();
	// }
	// return null;
	// }

	// private IType chooseEnclosingType() {
	// IPackageFragmentRoot root= getPackageFragmentRoot();
	// if (root == null) {
	// return null;
	// }
	//		
	// IJavaSearchScope scope= SearchEngine.createJavaSearchScope(new
	// IJavaElement[] { root });
	//	
	// TypeSelectionDialog dialog= new TypeSelectionDialog(getShell(),
	// getWizard().getContainer(), IJavaSearchConstants.TYPE, scope);
	// dialog.setTitle(NewWizardMessages.getString("NewTypeWizardPage.ChooseEnclosingTypeDialog.title"));
	// //$NON-NLS-1$
	// dialog.setMessage(NewWizardMessages.getString("NewTypeWizardPage.ChooseEnclosingTypeDialog.description"));
	// //$NON-NLS-1$
	// dialog.setFilter(Signature.getSimpleName(getEnclosingTypeText()));
	//		
	// if (dialog.open() == TypeSelectionDialog.OK) {
	// return (IType) dialog.getFirstResult();
	// }
	// return null;
	// }

	// private IType chooseSuperType() {
	// IPackageFragmentRoot root= getPackageFragmentRoot();
	// if (root == null) {
	// return null;
	// }
	//		
	// IJavaElement[] elements= new IJavaElement[] { root.getJavaProject() };
	// IJavaSearchScope scope= SearchEngine.createJavaSearchScope(elements);
	//
	// TypeSelectionDialog dialog= new TypeSelectionDialog(getShell(),
	// getWizard().getContainer(), IJavaSearchConstants.CLASS, scope);
	// dialog.setTitle(NewWizardMessages.getString("NewTypeWizardPage.SuperClassDialog.title"));
	// //$NON-NLS-1$
	// dialog.setMessage(NewWizardMessages.getString("NewTypeWizardPage.SuperClassDialog.message"));
	// //$NON-NLS-1$
	// dialog.setFilter(getSuperClass());
	//
	// if (dialog.open() == TypeSelectionDialog.OK) {
	// return (IType) dialog.getFirstResult();
	// }
	// return null;
	// }

	// private void chooseSuperInterfaces() {
	// IPackageFragmentRoot root= getPackageFragmentRoot();
	// if (root == null) {
	// return;
	// }
	//
	// IJavaProject project= root.getJavaProject();
	// SuperInterfaceSelectionDialog dialog= new
	// SuperInterfaceSelectionDialog(getShell(), getWizard().getContainer(),
	// fSuperInterfacesDialogField, project);
	// dialog.setTitle(fIsClass ?
	// NewWizardMessages.getString("NewTypeWizardPage.InterfacesDialog.class.title")
	// :
	// NewWizardMessages.getString("NewTypeWizardPage.InterfacesDialog.interface.title"));
	// //$NON-NLS-1$ //$NON-NLS-2$
	// dialog.setMessage(NewWizardMessages.getString("NewTypeWizardPage.InterfacesDialog.message"));
	// //$NON-NLS-1$
	// dialog.open();
	// return;
	// }

	// ---- creation ----------------

	/**
	 * Creates the new type using the entered field values.
	 * 
	 * @param monitor
	 *            a progress monitor to report progress.
	 */
	public void createType(IProgressMonitor monitor) throws CoreException,
			InterruptedException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		monitor.beginTask(NewWizardMessages
				.getString("NewTypeWizardPage.operationdesc"), 10); //$NON-NLS-1$
		ICompilationUnit createdWorkingCopy = null;
		try {
			// IPackageFragmentRoot root = getPackageFragmentRoot();
			// IPackageFragment pack = getPackageFragment();
			// if (pack == null) {
			// pack = root.getPackageFragment(""); //$NON-NLS-1$
			// }
			//
			// if (!pack.exists()) {
			// String packName = pack.getElementName();
			// pack = root.createPackageFragment(packName, true, null);
			// }

			monitor.worked(1);

			String clName = getTypeName();

			boolean isInnerClass = isEnclosingTypeSelected();

			IType createdType;
			// ImportsStructure imports;
			int indent = 0;

			IPreferenceStore store = PreferenceConstants.getPreferenceStore();
			// String[] prefOrder =
			// JavaPreferencesSettings.getImportOrderPreference(store);
			// int threshold =
			// JavaPreferencesSettings.getImportNumberThreshold(store);
			//
			String lineDelimiter = null;
			// if (!isInnerClass) {
			lineDelimiter = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			//
			// ICompilationUnit parentCU = pack.createCompilationUnit(clName +
			// ".php", "", false, new SubProgressMonitor(monitor, 2));
			// //$NON-NLS-1$ //$NON-NLS-2$
			// createdWorkingCopy = (ICompilationUnit)
			// parentCU.getSharedWorkingCopy(null, JavaUI.getBufferFactory(),
			// null);
			//
			// imports = new ImportsStructure(createdWorkingCopy, prefOrder,
			// threshold, false);
			// // add an import that will be removed again. Having this import
			// solves 14661
			// imports.addImport(pack.getElementName(), getTypeName());
			//
			String typeContent = constructTypeStub(lineDelimiter);// new
																	// ImportsManager(imports),
																	// lineDelimiter);

			// String cuContent = constructCUContent(parentCU, typeContent,
			// lineDelimiter);

			// createdWorkingCopy.getBuffer().setContents(cuContent);
			//
			createdType = createdWorkingCopy.getType(clName);
			// } else {
			// IType enclosingType = getEnclosingType();
			//
			// // if we are working on a enclosed type that is open in an
			// editor,
			// // then replace the enclosing type with its working copy
			// IType workingCopy = (IType)
			// EditorUtility.getWorkingCopy(enclosingType);
			// if (workingCopy != null) {
			// enclosingType = workingCopy;
			// }
			//
			// ICompilationUnit parentCU = enclosingType.getCompilationUnit();
			// imports = new ImportsStructure(parentCU, prefOrder, threshold,
			// true);
			//
			// // add imports that will be removed again. Having the imports
			// solves 14661
			// IType[] topLevelTypes = parentCU.getTypes();
			// for (int i = 0; i < topLevelTypes.length; i++) {
			// imports.addImport(topLevelTypes[i].getFullyQualifiedName('.'));
			// }
			//
			// lineDelimiter = StubUtility.getLineDelimiterUsed(enclosingType);
			// StringBuffer content = new StringBuffer();
			// String comment = getTypeComment(parentCU);
			// if (comment != null) {
			// content.append(comment);
			// content.append(lineDelimiter);
			// }
			// content.append(constructTypeStub(new ImportsManager(imports),
			// lineDelimiter));
			// IJavaElement[] elems = enclosingType.getChildren();
			// IJavaElement sibling = elems.length > 0 ? elems[0] : null;
			//
			// createdType = enclosingType.createType(content.toString(),
			// sibling, false, new SubProgressMonitor(monitor, 1));
			//
			// indent = StubUtility.getIndentUsed(enclosingType) + 1;
			// }
			//
			// // add imports for superclass/interfaces, so types can be
			// resolved correctly
			// imports.create(false, new SubProgressMonitor(monitor, 1));
			//
			ICompilationUnit cu = createdType.getCompilationUnit();
			synchronized (cu) {
				cu.reconcile();
			}
			// createTypeMembers(createdType, new ImportsManager(imports), new
			// SubProgressMonitor(monitor, 1));
			//
			// // add imports
			// imports.create(false, new SubProgressMonitor(monitor, 1));

			synchronized (cu) {
				cu.reconcile();
			}
			ISourceRange range = createdType.getSourceRange();

			IBuffer buf = cu.getBuffer();
			String originalContent = buf.getText(range.getOffset(), range
					.getLength());
			String formattedContent = StubUtility.codeFormat(originalContent,
					indent, lineDelimiter);
			buf.replace(range.getOffset(), range.getLength(), formattedContent);
			if (!isInnerClass) {
				String fileComment = getFileComment(cu);
				if (fileComment != null && fileComment.length() > 0) {
					buf.replace(0, 0, fileComment + lineDelimiter);
				}
				cu.commit(false, new SubProgressMonitor(monitor, 1));
			} else {
				monitor.worked(1);
			}
			fCreatedType = createdType;
		} finally {
			if (createdWorkingCopy != null) {
				createdWorkingCopy.destroy();
			}
			monitor.done();
		}
	}

	/**
	 * Uses the New Java file template from the code template page to generate a
	 * compilation unit with the given type content.
	 * 
	 * @param cu
	 *            The new created compilation unit
	 * @param typeContent
	 *            The content of the type, including signature and type body.
	 * @param lineDelimiter
	 *            The line delimiter to be used.
	 * @return String Returns the result of evaluating the new file template
	 *         with the given type content.
	 * @throws CoreException
	 * @since 2.1
	 */
	// protected String constructCUContent(ICompilationUnit cu, String
	// typeContent, String lineDelimiter) throws CoreException {
	// StringBuffer typeQualifiedName= new StringBuffer();
	// if (isEnclosingTypeSelected()) {
	// typeQualifiedName.append(JavaModelUtil.getTypeQualifiedName(getEnclosingType())).append('.');
	// }
	// typeQualifiedName.append(getTypeName());
	// String typeComment= CodeGeneration.getTypeComment(cu,
	// typeQualifiedName.toString(), lineDelimiter);
	// IPackageFragment pack= (IPackageFragment) cu.getParent();
	// String content= CodeGeneration.getCompilationUnitContent(cu, typeComment,
	// typeContent, lineDelimiter);
	// if (content != null) {
	// CompilationUnit unit= AST.parseCompilationUnit(content.toCharArray());
	// if ((pack.isDefaultPackage() || unit.getPackage() != null) &&
	// !unit.types().isEmpty()) {
	// return content;
	// }
	// }
	// StringBuffer buf= new StringBuffer();
	// if (!pack.isDefaultPackage()) {
	// buf.append("package ").append(pack.getElementName()).append(';');
	// //$NON-NLS-1$
	// }
	// buf.append(lineDelimiter).append(lineDelimiter);
	// if (typeComment != null) {
	// buf.append(typeComment).append(lineDelimiter);
	// }
	// buf.append(typeContent);
	// return buf.toString();
	// }
	/**
	 * Returns the created type. The method only returns a valid type after
	 * <code>createType</code> has been called.
	 * 
	 * @return the created type
	 * @see #createType(IProgressMonitor)
	 */
	public IType getCreatedType() {
		return fCreatedType;
	}

	// ---- construct cu body----------------

	// private void writeSuperClass(StringBuffer buf, ImportsManager imports) {
	// String typename= getSuperClass();
	// if (fIsClass && typename.length() > 0 &&
	// !"java.lang.Object".equals(typename)) { //$NON-NLS-1$
	// buf.append(" extends "); //$NON-NLS-1$
	//			
	// String qualifiedName= fSuperClass != null ?
	// JavaModelUtil.getFullyQualifiedName(fSuperClass) : typename;
	// buf.append(imports.addImport(qualifiedName));
	// }
	// }

	// private void writeSuperInterfaces(StringBuffer buf, ImportsManager
	// imports) {
	// List interfaces= getSuperInterfaces();
	// int last= interfaces.size() - 1;
	// if (last >= 0) {
	// if (fIsClass) {
	// buf.append(" implements "); //$NON-NLS-1$
	// } else {
	// buf.append(" extends "); //$NON-NLS-1$
	// }
	// for (int i= 0; i <= last; i++) {
	// String typename= (String) interfaces.get(i);
	// buf.append(imports.addImport(typename));
	// if (i < last) {
	// buf.append(',');
	// }
	// }
	// }
	// }

	/*
	 * Called from createType to construct the source for this type
	 */
	private String constructTypeStub(String lineDelimiter) { // ImportsManager
																// imports,
																// String
																// lineDelimiter)
																// {
		StringBuffer buf = new StringBuffer();

		int modifiers = getModifiers();
		buf.append(Flags.toString(modifiers));
		if (modifiers != 0) {
			buf.append(' ');
		}
		buf.append(fIsClass ? "class " : "interface "); //$NON-NLS-2$ //$NON-NLS-1$
		buf.append(getTypeName());
		// writeSuperClass(buf, imports);
		// writeSuperInterfaces(buf, imports);
		buf.append('{');
		buf.append(lineDelimiter);
		buf.append(lineDelimiter);
		buf.append('}');
		buf.append(lineDelimiter);
		return buf.toString();
	}

	/**
	 * @deprecated Overwrite createTypeMembers(IType, IImportsManager,
	 *             IProgressMonitor) instead
	 */
	// protected void createTypeMembers(IType newType, IImportsStructure
	// imports, IProgressMonitor monitor) throws CoreException {
	// //deprecated
	// }
	/**
	 * Hook method that gets called from <code>createType</code> to support
	 * adding of unanticipated methods, fields, and inner types to the created
	 * type.
	 * <p>
	 * Implementers can use any methods defined on <code>IType</code> to
	 * manipulate the new type.
	 * </p>
	 * <p>
	 * The source code of the new type will be formtted using the platform's
	 * formatter. Needed imports are added by the wizard at the end of the type
	 * creation process using the given import manager.
	 * </p>
	 * 
	 * @param newType
	 *            the new type created via <code>createType</code>
	 * @param imports
	 *            an import manager which can be used to add new imports
	 * @param monitor
	 *            a progress monitor to report progress. Must not be
	 *            <code>null</code>
	 * 
	 * @see #createType(IProgressMonitor)
	 */
	// protected void createTypeMembers(IType newType, ImportsManager imports,
	// IProgressMonitor monitor) throws CoreException {
	// // call for compatibility
	// createTypeMembers(newType,
	// ((ImportsManager)imports).getImportsStructure(), monitor);
	//		
	// // default implementation does nothing
	// // example would be
	// // String mainMathod= "public void foo(Vector vec) {}"
	// // createdType.createMethod(main, null, false, null);
	// // imports.addImport("java.lang.Vector");
	// }
	/**
	 * @deprecated Instead of file templates, the new type code template
	 *             specifies the stub for a compilation unit.
	 */
	protected String getFileComment(ICompilationUnit parentCU) {
		return null;
	}

	private boolean isValidComment(String template) {
		IScanner scanner = ToolFactory.createScanner(true, false, false); // ,
																			// false);
		scanner.setSource(template.toCharArray());
		try {
			int next = scanner.getNextToken();
			while (next == ITerminalSymbols.TokenNameCOMMENT_LINE
					|| next == ITerminalSymbols.TokenNameCOMMENT_PHPDOC
					|| next == ITerminalSymbols.TokenNameCOMMENT_BLOCK) {
				next = scanner.getNextToken();
			}
			return next == ITerminalSymbols.TokenNameEOF;
		} catch (InvalidInputException e) {
		}
		return false;
	}

	/**
	 * Hook method that gets called from <code>createType</code> to retrieve a
	 * type comment. This default implementation returns the content of the
	 * 'typecomment' template.
	 * 
	 * @return the type comment or <code>null</code> if a type comment is not
	 *         desired
	 */
	protected String getTypeComment(ICompilationUnit parentCU) {
		if (PreferenceConstants.getPreferenceStore().getBoolean(
				PreferenceConstants.CODEGEN_ADD_COMMENTS)) {
			try {
				StringBuffer typeName = new StringBuffer();
				if (isEnclosingTypeSelected()) {
					typeName.append(
							JavaModelUtil
									.getTypeQualifiedName(getEnclosingType()))
							.append('.');
				}
				typeName.append(getTypeName());
				String comment = CodeGeneration.getTypeComment(parentCU,
						typeName.toString(), String.valueOf('\n'));
				if (comment != null && isValidComment(comment)) {
					return comment;
				}
			} catch (CoreException e) {
				PHPeclipsePlugin.log(e);
			}
		}
		return null;
	}

	/**
	 * @deprecated Use getTemplate(String,ICompilationUnit,int)
	 */
	protected String getTemplate(String name, ICompilationUnit parentCU) {
		return getTemplate(name, parentCU, 0);
	}

	/**
	 * Returns the string resulting from evaluation the given template in the
	 * context of the given compilation unit. This accesses the normal template
	 * page, not the code templates. To use code templates use
	 * <code>constructCUContent</code> to construct a compilation unit stub or
	 * getTypeComment for the comment of the type.
	 * 
	 * @param name
	 *            the template to be evaluated
	 * @param parentCU
	 *            the templates evaluation context
	 * @param pos
	 *            a source offset into the parent compilation unit. The template
	 *            is evalutated at the given source offset
	 */
	protected String getTemplate(String name, ICompilationUnit parentCU, int pos) {
		try {
			Template[] templates = Templates.getInstance().getTemplates(name);
			if (templates.length > 0) {
				return JavaContext
						.evaluateTemplate(templates[0], parentCU, pos);
			}
		} catch (CoreException e) {
			PHPeclipsePlugin.log(e);
		} catch (BadLocationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TemplateException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}

	/**
	 * @deprecated Use
	 *             createInheritedMethods(IType,boolean,boolean,IImportsManager,IProgressMonitor)
	 */
	// protected IMethod[] createInheritedMethods(IType type, boolean
	// doConstructors, boolean doUnimplementedMethods, IImportsStructure
	// imports, IProgressMonitor monitor) throws CoreException {
	// return createInheritedMethods(type, doConstructors,
	// doUnimplementedMethods, new ImportsManager(imports), monitor);
	// }
	/**
	 * Creates the bodies of all unimplemented methods and constructors and adds
	 * them to the type. Method is typically called by implementers of
	 * <code>NewTypeWizardPage</code> to add needed method and constructors.
	 * 
	 * @param type
	 *            the type for which the new methods and constructor are to be
	 *            created
	 * @param doConstructors
	 *            if <code>true</code> unimplemented constructors are created
	 * @param doUnimplementedMethods
	 *            if <code>true</code> unimplemented methods are created
	 * @param imports
	 *            an import manager to add all neded import statements
	 * @param monitor
	 *            a progress monitor to report progress
	 */
	// protected IMethod[] createInheritedMethods(IType type, boolean
	// doConstructors, boolean doUnimplementedMethods, ImportsManager imports,
	// IProgressMonitor monitor) throws CoreException {
	// ArrayList newMethods= new ArrayList();
	// ITypeHierarchy hierarchy= null;
	// CodeGenerationSettings settings=
	// JavaPreferencesSettings.getCodeGenerationSettings();
	//
	// if (doConstructors) {
	// hierarchy= type.newSupertypeHierarchy(monitor);
	// IType superclass= hierarchy.getSuperclass(type);
	// if (superclass != null) {
	// String[] constructors= StubUtility.evalConstructors(type, superclass,
	// settings, imports.getImportsStructure());
	// if (constructors != null) {
	// for (int i= 0; i < constructors.length; i++) {
	// newMethods.add(constructors[i]);
	// }
	// }
	//			
	// }
	// }
	// if (doUnimplementedMethods) {
	// if (hierarchy == null) {
	// hierarchy= type.newSupertypeHierarchy(monitor);
	// }
	// String[] unimplemented= StubUtility.evalUnimplementedMethods(type,
	// hierarchy, false, settings, null, imports.getImportsStructure());
	// if (unimplemented != null) {
	// for (int i= 0; i < unimplemented.length; i++) {
	// newMethods.add(unimplemented[i]);
	// }
	// }
	// }
	// IMethod[] createdMethods= new IMethod[newMethods.size()];
	// for (int i= 0; i < newMethods.size(); i++) {
	// String content= (String) newMethods.get(i) + '\n'; // content will be
	// formatted, ok to use \n
	// createdMethods[i]= type.createMethod(content, null, false, null);
	// }
	// return createdMethods;
	// }
	// ---- creation ----------------
	/**
	 * Returns the runnable that creates the type using the current settings.
	 * The returned runnable must be executed in the UI thread.
	 * 
	 * @return the runnable to create the new type
	 */
	// public IRunnableWithProgress getRunnable() {
	// return new IRunnableWithProgress() {
	// public void run(IProgressMonitor monitor) throws
	// InvocationTargetException, InterruptedException {
	// try {
	// if (monitor == null) {
	// monitor= new NullProgressMonitor();
	// }
	// createType(monitor);
	// } catch (CoreException e) {
	// throw new InvocationTargetException(e);
	// }
	// }
	// };
	// }
}
