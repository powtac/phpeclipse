/*
 * $Id: EditElementWizardPage.java,v 1.2 2006-10-21 23:18:43 pombredanne Exp $
 * Copyright Narushima Hironori. All rights reserved.
 */
package net.sourceforge.phpeclipse.wizards.html;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * 
 * 
 */
public abstract class EditElementWizardPage extends WizardPage implements
		IPreviewer {

	final public static int NEW = 0, MODIFY = 1;

	private static DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
			.newInstance();

	Composite extendComp;

	Text preview;

	private String elementName = null;

	int editType = NEW;

	protected EditElementWizardPage(String pageName) {
		super(pageName);
	}

	public void createControl(Composite parent) {
		Composite base = new Composite(parent, SWT.NONE);
		setControl(base);
		base.setLayout(new GridLayout(1, false));

		// create child control.
		Composite childControlBase = new Composite(base, SWT.NONE);
		childControlBase.setLayoutData(new GridData(GridData.FILL_BOTH));
		try {
			createChildControl(childControlBase);
		} catch (CoreException e) {
			PHPeclipsePlugin.log(e);
			return;
		}

		// preview components.
		Composite previewBase = new Composite(base, SWT.NONE);

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		previewBase.setLayoutData(gd);
		previewBase.setLayout(new GridLayout(1, false));

		Label labe = new Label(previewBase, SWT.NONE);
		labe.setText("Preview:");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		labe.setLayoutData(gd);

		preview = new Text(previewBase, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY
				| SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 0;
		gd.heightHint = preview.getLineHeight() * 4;
		preview.setLayoutData(gd);

		refreshPreview();
	}

	abstract protected void createChildControl(Composite parent)
			throws CoreException;

	public abstract String getPreviewText();

	public void refreshPreview() {
		if (preview != null) {
			String text = getPreviewText();
			preview.setText(text == null ? "" : text);
		}
	}

	public String getElementName() {
		return elementName;
	}

	public void setElementName(String string) {
		elementName = string;
	}

	protected IFile getEditFile() {
		IFile file = null;
		IWizard wiz = getWizard();
		if (wiz instanceof EditElementWizard) {
			file = ((EditElementWizard) wiz).getCurrentEditFile();
		}
		return file;
	}

	protected void performFinish() {
		EditElementWizard wiz = (EditElementWizard) getWizard();
		ITextSelection sel = wiz.getSelection();
		IDocument doc = wiz.getDocument();
		int offset = sel.getOffset();
		try {
			doc.replace(offset, sel.getLength(), getPreviewText());
		} catch (BadLocationException e) {
			PHPeclipsePlugin.log(e);
		}
		int index = doc.get().indexOf('>', offset);
		if (index != -1) {
			wiz.setSelection(new TextSelection(index + 1, 0));
		}
	}

	/**
	 * Returns edit type.
	 */
	public int getEditType() {
		return editType;
	}

	/**
	 * Sets edit type that types are EditElementWizardPage.NEW,
	 * EditElementWizardPage.MODIFY. Default value is NEW.
	 */
	public void setEditType(int i) {
		editType = i;
	}

	protected String getSelectionText() {
		return ((EditElementWizard) getWizard()).getSelection().getText();
	}

	protected Element getParsedSelectionText() {
		String selText = getSelectionText();
		try {
			InputSource source = new InputSource(new StringReader(selText));
			Document doc = docBuilderFactory.newDocumentBuilder().parse(source);
			return doc.getDocumentElement();
		} catch (SAXException e) {
		} catch (IOException e) {
		} catch (ParserConfigurationException e) {
		}
		return null;

	}

	protected static String chooseContent(String text) {
		int b = -1, e = -1, len = text.length();
		for (int i = 0; i < len; i++) {
			if (text.charAt(i) == '>') {
				b = i + 1;
				break;
			}
		}
		for (int i = len - 1; i >= 0; i--) {
			if (text.charAt(i) == '<') {
				e = i;
				break;
			}
		}
		return (b != -1 && e != -1 && b < len && e < len) ? text
				.substring(b, e) : "";

	}

}