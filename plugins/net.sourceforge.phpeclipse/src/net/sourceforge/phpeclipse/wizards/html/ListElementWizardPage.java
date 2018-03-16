/*
 * $Id: ListElementWizardPage.java,v 1.3 2006-10-21 23:18:43 pombredanne Exp $
 * Copyright Narushima Hironori. All rights reserved.
 */
package net.sourceforge.phpeclipse.wizards.html;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * 
 */
public class ListElementWizardPage extends EditElementWizardPage {

	final static String[] LIST_TYPES = { "ul", "ol", "dl" };

	Combo types;

	public ListElementWizardPage() {
		super("ListElementWizardPage");
		setTitle("List");
		setDescription("Editing list element.");
	}

	protected void createChildControl(Composite parent) {
		parent.setLayout(new GridLayout(2, false));
		Label labe = new Label(parent, SWT.NONE);
		labe.setText("List &Type:");

		types = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);

		for (int i = 0; i < LIST_TYPES.length; i++) {
			String type = LIST_TYPES[i];
			types.add(type);
			if (getElementName().equals(type)) {
				types.select(i);
			}
		}

		types.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		types.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				setElementName(types.getText());
				refreshPreview();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	public String getPreviewText() {
		String content = ((EditElementWizard) getWizard()).getSelection()
				.getText().trim();

		String elemName = getElementName();
		switch (getEditType()) {
		case MODIFY:
			content = chooseContent(content).trim();
			break;

		case NEW:
			String[] lines = content.split("\n+");
			StringBuffer result = new StringBuffer();
			for (int i = 0; i < lines.length; i++) {
				String itemElemName;
				if (elemName.equals("dl")) {
					itemElemName = (i % 2 == 0) ? "dt" : "dd";
				} else {
					itemElemName = "li";
				}
				result.append("<" + itemElemName + ">" + lines[i].trim() + "</"
						+ itemElemName + ">\n");
			}
			content = result.toString();
			break;
		}

		return "<" + elemName + ">\n" + content + "</" + elemName + ">\n";
	}

}
