/*
 * $Id: FormElementWizardPage.java,v 1.3 2006-10-21 23:18:43 pombredanne Exp $
 * Copyright Narushima Hironori. All rights reserved.
 */
package net.sourceforge.phpeclipse.wizards.html;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * 
 */
public class FormElementWizardPage extends EditElementWizardPage {

	Text actionText;

	Button postRadio, getRadio, multipartCheck;

	Combo charsetCombo;

	public FormElementWizardPage() {
		super("FormElementWizardPage");
	}

	protected void createChildControl(Composite parent) throws CoreException {
		postRadio = new Button(parent, SWT.RADIO);

	}

	public String getPreviewText() {
		boolean controlCreated = actionText != null;

		StringBuffer buff = new StringBuffer("<form action=\"");
		if (controlCreated) {
			buff.append(actionText.getText());
		}
		buff.append("\" method=\"");
		if (controlCreated && postRadio.getSelection()) {
			buff.append("POST\"");
			if (multipartCheck.getSelection()) {
				buff.append(" enctype=\"multipart/form-data\"");
			}

		} else {
			buff.append("GET\"");
		}

		if (controlCreated) {
			String charset = charsetCombo.getText();
			if (charset != null) {
				buff.append(" accept-charset=\"" + charset + "\"");
			}
		}

		buff.append(">\n");
		buff.append(getSelectionText());
		buff.append("\n</form>\n");

		return buff.toString();
	}

}
