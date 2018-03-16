/*
 * $Id: NewElementWizardPage.java,v 1.3 2006-10-21 23:18:43 pombredanne Exp $
 * Copyright Narushima Hironori. All rights reserved.
 */
package net.sourceforge.phpeclipse.wizards.html;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * 
 */
public class NewElementWizardPage extends EditElementWizardPage {

	Text elementName;

	EditElementWizardPage nextPage = null;

	public NewElementWizardPage() {
		super("NewElementPage");
		setTitle("Create HTML Element");
		setDescription("Specify new HTML tag (dl,ul,ol or table) and configure that tag.");
	}

	protected void createChildControl(Composite base) {
		// create foundation component
		base.setLayout(new GridLayout(1, false));

		// element input components
		new Label(base, SWT.NONE).setText("&Element Name:");

		elementName = new Text(base, SWT.BORDER | SWT.SINGLE);
		elementName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		elementName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String eName = elementName.getText();
				if (eName.indexOf(' ') != -1) {
					setErrorMessage("Don't contain blink in speicfied element name.");
				} else if (eName.length() == 0) {
					setErrorMessage("Need to specify element name.");
				} else {
					setErrorMessage(null);
					nextPage = ((EditElementWizard) getWizard())
							.createElementEditPage(eName);
					nextPage.setElementName(eName);
					if (nextPage instanceof UnknownElementWizardPage) {
						setMessage("This editor does not known element name.",
								WARNING);
					} else {
						setMessage(null, NONE);
					}
				}
				refreshPreview();
				getWizard().getContainer().updateButtons();
			}
		});
	}

	public String getPreviewText() {
		if (nextPage instanceof EditElementWizardPage) {
			return ((EditElementWizardPage) nextPage).getPreviewText();
		}
		return null;
	}

	public void setErrorMessage(String newMessage) {
		super.setErrorMessage(newMessage);
		if (newMessage != null) {
			nextPage = null;
		}
	}

	public IWizardPage getNextPage() {
		return nextPage;
	}

}
