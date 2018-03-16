/*
 * Copyright (c) 2004 Christopher Lenz and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Christopher Lenz - initial API and implementation
 * 
 * $Id: XMLOutlinePage.java,v 1.3 2006-10-21 23:14:14 pombredanne Exp $
 */

package net.sourceforge.phpeclipse.xml.ui.internal.outline;

import java.util.List;

import net.sourceforge.phpeclipse.core.model.ISourceReference;
import net.sourceforge.phpeclipse.ui.views.outline.ProblemsLabelDecorator;
import net.sourceforge.phpeclipse.xml.core.model.IXMLDocument;
import net.sourceforge.phpeclipse.xml.ui.internal.editor.XMLDocumentProvider;
import net.sourceforge.phpeclipse.xml.ui.internal.editor.XMLEditor;

import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

/**
 * Implements the outline page associated with the XML editor.
 */
public class XMLOutlinePage extends ContentOutlinePage {

	// Instance Variables ------------------------------------------------------

	/**
	 * The associated editor.
	 */
	private XMLEditor editor;

	// Constructors ------------------------------------------------------------

	/**
	 * Constructor.
	 * 
	 * @param editor
	 *            The associated text editor
	 */
	public XMLOutlinePage(XMLEditor editor) {
		this.editor = editor;
	}

	// ContentOutlinePage Implementation ---------------------------------------

	/*
	 * @see org.eclipse.ui.part.IPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		TreeViewer viewer = getTreeViewer();
		viewer.setContentProvider(new XMLOutlineContentProvider());
		viewer.setLabelProvider(new DecoratingLabelProvider(
				new XMLOutlineLabelProvider(), new ProblemsLabelDecorator(
						editor)));
		viewer.setInput(getDocument());
	}

	// Public Methods ----------------------------------------------------------

	/**
	 * Selects a specific element in the outline page.
	 * 
	 * @param element
	 *            the element to select
	 */
	public void select(ISourceReference element) {
		TreeViewer viewer = getTreeViewer();
		if (viewer != null) {
			ISelection selection = viewer.getSelection();
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				List elements = structuredSelection.toList();
				if (!elements.contains(element)) {
					if (element == null) {
						selection = StructuredSelection.EMPTY;
					} else {
						selection = new StructuredSelection(element);
					}
					viewer.setSelection(selection, true);
				}
			}
		}
	}

	/**
	 * Updates the outline page.
	 */
	public void update() {
		IXMLDocument document = getDocument();
		if (document != null) {
			TreeViewer viewer = getTreeViewer();
			if (viewer != null) {
				Control control = viewer.getControl();
				if ((control != null) && !control.isDisposed()) {
					control.setRedraw(false);
					viewer.setInput(document);
					viewer.expandAll();
					control.setRedraw(true);
				}
			}
		}
	}

	// Private Methods ---------------------------------------------------------

	/**
	 * Returns the parsed model of the XML document that is loaded into the
	 * associated editor.
	 * 
	 * @return the parsed XML document
	 */
	private IXMLDocument getDocument() {
		IDocumentProvider provider = editor.getDocumentProvider();
		if (provider instanceof XMLDocumentProvider) {
			XMLDocumentProvider xmlProvider = (XMLDocumentProvider) provider;
			return xmlProvider.getModel(editor.getEditorInput());
		}
		return null;
	}

}
