/*
 * Copyright (c) 2002-2004 Widespace, OU and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Igor Malinin - initial contribution
 *
 * $Id: DTDEditor.java,v 1.3 2006-10-21 23:14:14 pombredanne Exp $
 */

package net.sourceforge.phpeclipse.xml.ui.internal.editor;

import net.sourceforge.phpeclipse.xml.ui.XMLPlugin;
import net.sourceforge.phpeclipse.xml.ui.internal.text.DTDConfiguration;
import net.sourceforge.phpeclipse.xml.ui.internal.text.DTDDocumentProvider;
import net.sourceforge.phpeclipse.xml.ui.text.DTDTextTools;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

/**
 * DTD Editor.
 * 
 * @author Igor Malinin
 */
public class DTDEditor extends TextEditor {

	public DTDEditor() {
		setPreferenceStore(XMLPlugin.getDefault().getPreferenceStore());
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextEditor#initializeEditor()
	 */
	protected void initializeEditor() {
		super.initializeEditor();

		DTDTextTools dtdTextTools = XMLPlugin.getDefault().getDTDTextTools();

		setSourceViewerConfiguration(new DTDConfiguration(dtdTextTools));

		setDocumentProvider(new DTDDocumentProvider());
	}

	protected boolean affectsTextPresentation(PropertyChangeEvent event) {
		return XMLPlugin.getDefault().getDTDTextTools().affectsBehavior(event);
	}

	protected void createActions() {
		super.createActions();

		IAction action = new ContentAssistAction(XMLEditorMessages
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
}
