/*
 * Copyright (c) 2002-2004 Widespace, OU and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Igor Malinin - initial contribution
 *     Christopher Lenz - integrated outline page
 *
 * $Id: XMLEditor.java,v 1.4 2006-10-21 23:14:14 pombredanne Exp $
 */

package net.sourceforge.phpeclipse.xml.ui.internal.editor;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.phpeclipse.core.model.ISourceReference;
import net.sourceforge.phpeclipse.ui.editor.ShowExternalPreviewAction;
import net.sourceforge.phpeclipse.ui.text.IReconcilingParticipant;
import net.sourceforge.phpeclipse.xml.ui.XMLPlugin;
import net.sourceforge.phpeclipse.xml.ui.internal.outline.XMLOutlinePage;
import net.sourceforge.phpeclipse.xml.ui.internal.text.XMLConfiguration;
import net.sourceforge.phpeclipse.xml.ui.text.XMLTextTools;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * XML Editor.
 * 
 * @author Igor Malinin
 */
public class XMLEditor extends TextEditor implements IReconcilingParticipant {

	/**
	 * Listens to changes to the selection in the outline page, and changes the
	 * selection and highlight range in the editor accordingly.
	 */
	private class OutlineSelectionChangedListener implements
			ISelectionChangedListener {

		/*
		 * @see ISelectionChangedListener#selectionChanged(SelectionChangedEvent)
		 */
		public void selectionChanged(SelectionChangedEvent event) {
			IStructuredSelection selection = (IStructuredSelection) event
					.getSelection();
			if (selection.isEmpty()) {
				resetHighlightRange();
			} else {
				ISourceReference element = (ISourceReference) selection
						.getFirstElement();
				highlightElement(element, true);
			}
		}

	}

	/**
	 * The associated outline page.
	 */
	XMLOutlinePage outlinePage;

	int fType;

	/**
	 * Listens to changes in the outline page's selection to update the editor
	 * selection and highlight range.
	 */
	private ISelectionChangedListener outlineSelectionChangedListener;

	public XMLEditor() {
		this(ShowExternalPreviewAction.XML_TYPE);
	}

	/**
	 * Constructor.
	 */
	public XMLEditor(int type) {
		fType = type;
		List stores = new ArrayList(3);

		stores.add(XMLPlugin.getDefault().getPreferenceStore());
		stores.add(EditorsUI.getPreferenceStore());

		setPreferenceStore(new ChainedPreferenceStore(
				(IPreferenceStore[]) stores.toArray(new IPreferenceStore[stores
						.size()])));
	}

	/*
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IContentOutlinePage.class)) {
			if (outlinePage == null) {
				outlinePage = new XMLOutlinePage(this);
				outlineSelectionChangedListener = new OutlineSelectionChangedListener();
				outlinePage
						.addSelectionChangedListener(outlineSelectionChangedListener);
			}

			return outlinePage;
		}

		return super.getAdapter(adapter);
	}

	/*
	 * @see IReconcilingParticipant#reconciled()
	 */
	public void reconciled() {
		Shell shell = getSite().getShell();
		if ((shell != null) && !shell.isDisposed()) {
			shell.getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (outlinePage != null) {
						outlinePage.update();
					}
				}
			});
		}
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextEditor#initializeEditor()
	 */
	protected void initializeEditor() {
		super.initializeEditor();

		XMLTextTools xmlTextTools = XMLPlugin.getDefault().getXMLTextTools();
		setSourceViewerConfiguration(new XMLConfiguration(xmlTextTools, this));
		setDocumentProvider(new XMLDocumentProvider());

		ShowExternalPreviewAction fShowExternalPreviewAction = ShowExternalPreviewAction
				.getInstance();
		fShowExternalPreviewAction.setEditor(this);
		fShowExternalPreviewAction.update();
		if (fShowExternalPreviewAction != null)
			fShowExternalPreviewAction.doRun(fType);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#affectsTextPresentation(PropertyChangeEvent)
	 */
	protected boolean affectsTextPresentation(PropertyChangeEvent event) {
		return XMLPlugin.getDefault().getXMLTextTools().affectsBehavior(event);
	}

	void highlightElement(ISourceReference element, boolean moveCursor) {
		if (element != null) {
			IRegion highlightRegion = element.getSourceRegion();
			setHighlightRange(highlightRegion.getOffset(), highlightRegion
					.getLength(), moveCursor);
		} else {
			resetHighlightRange();
		}
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#editorSaved()
	 */
	protected void editorSaved() {
		super.editorSaved();
		ShowExternalPreviewAction a = ShowExternalPreviewAction.getInstance();
		if (a != null) {
			a.refresh(fType);
		}
	}
}