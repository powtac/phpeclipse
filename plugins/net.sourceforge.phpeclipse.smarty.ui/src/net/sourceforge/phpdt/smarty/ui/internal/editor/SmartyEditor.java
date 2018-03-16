/*
 * Copyright (c) 2004 Christopher Lenz and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Christopher Lenz - initial implementation
 * 
 * $Id: SmartyEditor.java,v 1.2 2006-10-21 23:19:32 pombredanne Exp $
 */

package net.sourceforge.phpdt.smarty.ui.internal.editor;

import net.sourceforge.phpdt.smarty.ui.internal.text.SmartyConfiguration;
import net.sourceforge.phpeclipse.ui.editor.ShowExternalPreviewAction;
import net.sourceforge.phpeclipse.xml.ui.XMLPlugin;
import net.sourceforge.phpeclipse.xml.ui.internal.editor.XMLDocumentProvider;
import net.sourceforge.phpeclipse.xml.ui.internal.editor.XMLEditor;
import net.sourceforge.phpeclipse.xml.ui.text.XMLTextTools;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

/**
 * HTML editor implementation.
 */
public class SmartyEditor extends XMLEditor {

	public SmartyEditor() {
		super(ShowExternalPreviewAction.SMARTY_TYPE);
	}

	// Instance Variables ------------------------------------------------------

	/** The associated preview page. */
	// private HTMLPreviewPage previewPage;
	// XMLEditor Implementation ------------------------------------------------

	/*
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class adapter) {
		// if (adapter == IBrowserPreviewPage.class) {
		// if (previewPage == null) {
		// previewPage = createPreviewPage();
		// }
		// return previewPage;
		// }
		return super.getAdapter(adapter);
	}

	// Private Methods ---------------------------------------------------------

	/**
	 * Creates the HTML preview page.
	 */
	// private HTMLPreviewPage createPreviewPage() {
	// IEditorInput input = getEditorInput();
	// if (input instanceof IFileEditorInput) {
	// IFile file = ((IFileEditorInput) input).getFile();
	// try {
	// URL location = file.getLocation().toFile().toURL();
	// return new HTMLPreviewPage(location);
	// } catch (MalformedURLException e) { }
	// }
	// return null;
	// }
	protected void createActions() {
		super.createActions();

		IAction action = new ContentAssistAction(SmartyEditorMessages
				.getResourceBundle(), "ContentAssistProposal.", this); //$NON-NLS-1$
		action
				.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction("ContentAssistProposal", action); //$NON-NLS-1$
		markAsStateDependentAction("ContentAssistProposal", true); //$NON-NLS-1$

		// IAction action= new TextOperationAction(
		// TemplateMessages.getResourceBundle(),
		// "Editor." + TEMPLATE_PROPOSALS + ".", //$NON-NLS-1$ //$NON-NLS-2$
		// this,
		// ISourceViewer.CONTENTASSIST_PROPOSALS);
		// action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		// setAction(TEMPLATE_PROPOSALS, action);
		// markAsStateDependentAction(TEMPLATE_PROPOSALS, true);
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextEditor#initializeEditor()
	 */
	protected void initializeEditor() {
		super.initializeEditor();

		XMLTextTools xmlTextTools = XMLPlugin.getDefault().getXMLTextTools();
		setSourceViewerConfiguration(new SmartyConfiguration(xmlTextTools, this));
		setDocumentProvider(new XMLDocumentProvider());
	}
}
