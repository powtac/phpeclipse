/*
 * $Id: SomeItemInputDialog.java,v 1.3 2006-10-21 23:18:43 pombredanne Exp $
 * Copyright Narushima Hironori. All rights reserved.
 */
package net.sourceforge.phpeclipse.wizards.html;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 
 */
public class SomeItemInputDialog extends Dialog {

	String dialogTitle;

	String[] inputMessages;

	IInputValidator[] validators;

	Text[] texts;

	Text error;

	String[] errorMsgs;

	String[] resultValues;

	public SomeItemInputDialog(Shell parentShell, String dialogTitle,
			String[] inputMessages, IInputValidator[] validators) {
		super(parentShell);
		if (inputMessages.length != validators.length) {
			throw new IllegalArgumentException(
					"Specify validator counts and input message count is not same.");
		}

		this.dialogTitle = dialogTitle;
		this.inputMessages = (String[]) inputMessages.clone();
		this.validators = (IInputValidator[]) validators.clone();
		this.errorMsgs = new String[validators.length];

		setShellStyle(SWT.RESIZE | getShellStyle());
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(dialogTitle);
	}

	protected Control createDialogArea(Composite parent) {
		Composite base = (Composite) super.createDialogArea(parent);
		GridLayout gl = new GridLayout(2, false);
		gl.marginWidth = 4;
		gl.marginHeight = 6;
		base.setLayout(gl);

		texts = new Text[inputMessages.length];
		for (int i = 0; i < inputMessages.length; i++) {
			new Label(base, SWT.NONE).setText(inputMessages[i] + ":");

			final int index = i;
			Text t = new Text(base, SWT.BORDER);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			t.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					refreshValidator(index);
				}
			});
			texts[i] = t;
		}

		error = new Text(base, SWT.READ_ONLY);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		error.setLayoutData(gd);

		return base;
	}

	void refreshValidator(int index) {
		String data = texts[index].getText();
		IInputValidator validator = validators[index];
		if (validator != null) {
			errorMsgs[index] = validator.isValid(data);
		}

		Button okButton = getButton(IDialogConstants.OK_ID);
		for (int i = 0; i < errorMsgs.length; i++) {
			String msg = errorMsgs[i];
			if (msg != null) {
				error.setText(msg);
				okButton.setEnabled(false);
				return;
			}
		}
		error.setText("");
		okButton.setEnabled(true);
	}

	public String[] getValues() {
		return (String[]) resultValues.clone();
	}

	protected Point getInitialSize() {
		Point p = super.getInitialSize();
		return new Point(p.x * 2, (int) (p.y * 1.25));
	}

	protected void okPressed() {
		resultValues = new String[texts.length];
		for (int i = 0; i < texts.length; i++) {
			resultValues[i] = texts[i].getText();
		}
		super.okPressed();
	}

}
