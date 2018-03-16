/*
 * $Id: EditElementWizard.java,v 1.3 2006-10-21 23:18:43 pombredanne Exp $
 * Copyright Narushima Hironori. All rights reserved.
 */
package net.sourceforge.phpeclipse.wizards.html;

import net.sourceforge.phpdt.internal.ui.PHPUiImages;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * EditElementWizard. TODO: privides extension point element editor. pluggable
 * element edit page.
 */
public class EditElementWizard extends Wizard {

	static Object[] elementEditPages = new Object[] {
			// "a", AElementWizardPage.class,
			// "img", ImgElementWizardPage.class,
			"dl", ListElementWizardPage.class, "ul",
			ListElementWizardPage.class, "ol", ListElementWizardPage.class,
			"table", TableElementWizardPage.class };

	String targetElemName;

	ITextEditor htEditor;

	EditElementWizardPage rootPage;

	/**
	 * Second argument specify element name, If specify null, call new element
	 * edit wizard page.
	 */
	public EditElementWizard(ITextEditor editor, String targetElemName) {
		htEditor = editor;
		this.targetElemName = targetElemName;

		setWindowTitle("Edit HTML Element");
		setDefaultPageImageDescriptor(PHPUiImages
				.getDescriptor("wizban/editelem_wiz.gif"));

		setForcePreviousAndNextButtons(true);
	}

	public void addPages() {
		if (targetElemName == null) {
			rootPage = new NewElementWizardPage();
		} else {
			IDocument doc = getDocument();
			rootPage = createElementEditPage(targetElemName);
			rootPage.setEditType(EditElementWizardPage.MODIFY);
		}
		addPage(rootPage);
	}

	public boolean performFinish() {
		IWizardPage page = rootPage;
		for (IWizardPage p; (p = page.getNextPage()) != null;) {
			page = p;
		}
		if (page instanceof EditElementWizardPage) {
			((EditElementWizardPage) page).performFinish();
		}
		return true;
	}

	public IDocument getDocument() {
		return htEditor.getDocumentProvider().getDocument(
				htEditor.getEditorInput());
	}

	public ITextSelection getSelection() {
		return (ITextSelection) htEditor.getSelectionProvider().getSelection();
	}

	public void setSelection(ITextSelection sel) {
		htEditor.getSelectionProvider().setSelection(sel);
	}

	public IFile getCurrentEditFile() {
		IEditorInput input = htEditor.getEditorInput();
		return (input instanceof IFileEditorInput) ? ((IFileEditorInput) input)
				.getFile() : null;
	}

	/**
	 * If not edit target returns UnknownElementWizardPage.
	 */
	public EditElementWizardPage createElementEditPage(String elementName) {
		EditElementWizardPage page = null;
		try {
			for (int i = 0; i < elementEditPages.length; i += 2) {
				if (((String) elementEditPages[i])
						.equalsIgnoreCase(elementName)) {
					Class klass = (Class) elementEditPages[i + 1];
					page = (EditElementWizardPage) klass.newInstance();
				}
			}
		} catch (InstantiationException e) {
			PHPeclipsePlugin.log(e);
		} catch (IllegalAccessException e) {
			PHPeclipsePlugin.log(e);
		}
		if (page == null) {
			page = new UnknownElementWizardPage();
		}
		page.setElementName(elementName);
		page.setWizard(this);

		return page;
	}

}