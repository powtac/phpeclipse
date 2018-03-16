// Copyright (c) 2005 by Leif Frenzel. All rights reserved.
// See http://leiffrenzel.de
package net.sourceforge.phpdt.ltk.ui.wizards;

import net.sourceforge.phpdt.internal.compiler.parser.Scanner;
import net.sourceforge.phpdt.ltk.core.RenameIdentifierInfo;
import net.sourceforge.phpdt.ltk.ui.UITexts;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * <p>
 * the input page for the Rename Property refactoring, where users can control
 * the effects of the refactoring; to be shown in the wizard.
 * </p>
 * 
 * <p>
 * We let the user enter the new name for the property, and we let her decide
 * whether other property files in the bundle should be affected, and whether
 * the operation is supposed to span the entire workspace or only the current
 * project.
 * </p>
 * 
 */
public class RenameLocalVariablePage extends UserInputWizardPage {

	private static final String DS_KEY = RenameLocalVariablePage.class
			.getName();

	private static final String DS_RENAME_DQ_STRINGS = "RENAME_DQ_STRINGS"; //$NON-NLS-1$

	private static final String DS_RENAME_PHPDOC = "RENAME_PHPDOC"; //$NON-NLS-1$

	private static final String DS_RENAME_OTHER_COMMENTS = "RENAME_OTHER_COMMENTS"; //$NON-NLS-1$

	private final RenameIdentifierInfo info;

	private IDialogSettings dialogSettings;

	private Text txtNewName;

	private Button cbRenameDQStrings;

	private Button cbRenamePHPdoc;

	private Button cbRenameOtherComments;

	public RenameLocalVariablePage(final RenameIdentifierInfo info) {
		super(RenameLocalVariablePage.class.getName());
		this.info = info;
		initDialogSettings();
	}

	public void createControl(final Composite parent) {
		Composite composite = createRootComposite(parent);
		setControl(composite);

		createLblNewName(composite);
		createTxtNewName(composite);
		createCbDQStrings(composite);
		createCbPHPdoc(composite);
		createCbOtherComments(composite);

		validate();

		// TODO check if we can leave this step out in the future
		getRefactoringWizard().setForcePreviewReview(true);
	}

	private Composite createRootComposite(final Composite parent) {
		Composite result = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = 10;
		gridLayout.marginHeight = 10;
		result.setLayout(gridLayout);
		initializeDialogUnits(result);
		Dialog.applyDialogFont(result);
		return result;
	}

	private void createLblNewName(final Composite composite) {
		Label lblNewName = new Label(composite, SWT.NONE);
		lblNewName.setText(UITexts.renamePropertyInputPage_lblNewName);
	}

	private void createTxtNewName(Composite composite) {
		txtNewName = new Text(composite, SWT.BORDER);
		txtNewName.setText(info.getOldName());
		txtNewName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtNewName.selectAll();
		txtNewName.addKeyListener(new KeyAdapter() {
			public void keyReleased(final KeyEvent e) {
				info.setNewName(txtNewName.getText());
				validate();
			}
		});
	}

	private void createCbDQStrings(final Composite composite) {
		String texts = UITexts.renameLocalVariable_cbDQStrings;
		cbRenameDQStrings = createCheckbox(composite, texts);
		cbRenameDQStrings.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent event) {
				boolean selected = cbRenameDQStrings.getSelection();
				dialogSettings.put(DS_RENAME_DQ_STRINGS, selected);
				info.setRenameDQString(selected);
			}
		});
		initDQStringsOption();
	}

	private void createCbPHPdoc(final Composite composite) {
		String texts = UITexts.renameLocalVariable_cbPHPdoc;
		cbRenamePHPdoc = createCheckbox(composite, texts);
		cbRenamePHPdoc.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent event) {
				boolean selected = cbRenamePHPdoc.getSelection();
				dialogSettings.put(DS_RENAME_PHPDOC, selected);
				info.setRenamePHPdoc(selected);
			}
		});
		initPHPdocOption();
	}

	private void createCbOtherComments(final Composite composite) {
		String texts = UITexts.renameLocalVariable_cbOtherDoc;
		cbRenameOtherComments = createCheckbox(composite, texts);
		cbRenameOtherComments.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent event) {
				boolean selected = cbRenameOtherComments.getSelection();
				dialogSettings.put(DS_RENAME_OTHER_COMMENTS, selected);
				info.setRenameOtherComments(selected);
			}
		});
		initOtherCommentsOption();
	}

	private Button createCheckbox(final Composite composite, final String text) {
		Button result = new Button(composite, SWT.CHECK);
		result.setText(text);

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		result.setLayoutData(gridData);

		return result;
	}

	private void initDialogSettings() {
		IDialogSettings ds = PHPeclipsePlugin.getDefault().getDialogSettings();
		dialogSettings = ds.getSection(DS_KEY);
		if (dialogSettings == null) {
			dialogSettings = ds.addNewSection(DS_KEY);
			// init default values
			dialogSettings.put(DS_RENAME_DQ_STRINGS, true);
			dialogSettings.put(DS_RENAME_PHPDOC, true);
			dialogSettings.put(DS_RENAME_OTHER_COMMENTS, true);
		}
	}

	private static boolean isVariable(String txt) {
		if (txt.length() <= 1) {
			return false;
		}
		if (txt.charAt(0) != '$') {
			return false;
		}
		for (int i = 1; i < txt.length(); i++) {
			if (!Scanner.isPHPIdentifierPart(txt.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	private void validate() {
		String txt = txtNewName.getText();
		Scanner s;
		setPageComplete(isVariable(txt) && !txt.equals(info.getOldName()));
	}

	private void initDQStringsOption() {
		boolean refs = dialogSettings.getBoolean(DS_RENAME_DQ_STRINGS);
		cbRenameDQStrings.setSelection(refs);
		info.setRenameDQString(refs);
	}

	private void initPHPdocOption() {
		boolean refs = dialogSettings.getBoolean(DS_RENAME_PHPDOC);
		cbRenamePHPdoc.setSelection(refs);
		info.setRenamePHPdoc(refs);
	}

	private void initOtherCommentsOption() {
		boolean refs = dialogSettings.getBoolean(DS_RENAME_OTHER_COMMENTS);
		cbRenameOtherComments.setSelection(refs);
		info.setRenameOtherComments(refs);
	}

}
