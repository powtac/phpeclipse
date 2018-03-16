/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.phpdt.internal.ui.text.template.preferences;

import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.templates.TemplateVariableResolver;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

/**
 * A proposal for insertion of template variables.
 */
public class TemplateVariableProposal implements ICompletionProposal {

	private TemplateVariableResolver fVariable;

	private int fOffset;

	private int fLength;

	private ITextViewer fViewer;

	private Point fSelection;

	/**
	 * Creates a template variable proposal.
	 * 
	 * @param variable
	 *            the template variable
	 * @param offset
	 *            the offset to replace
	 * @param length
	 *            the length to replace
	 * @param viewer
	 *            the viewer
	 */
	public TemplateVariableProposal(TemplateVariableResolver variable,
			int offset, int length, ITextViewer viewer) {
		fVariable = variable;
		fOffset = offset;
		fLength = length;
		fViewer = viewer;
	}

	/*
	 * @see ICompletionProposal#apply(IDocument)
	 */
	public void apply(IDocument document) {

		try {
			String variable = fVariable.getType().equals("dollar") ? "$$" : "${" + fVariable.getType() + '}'; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			document.replace(fOffset, fLength, variable);
			fSelection = new Point(fOffset + variable.length(), 0);

		} catch (BadLocationException e) {
			PHPeclipsePlugin.log(e);

			Shell shell = fViewer.getTextWidget().getShell();
			MessageDialog
					.openError(
							shell,
							TemplatePreferencesMessages
									.getString("TemplateVariableProposal.error.title"), e.getMessage()); //$NON-NLS-1$
		}
	}

	/*
	 * @see ICompletionProposal#getSelection(IDocument)
	 */
	public Point getSelection(IDocument document) {
		return fSelection;
	}

	/*
	 * @see ICompletionProposal#getAdditionalProposalInfo()
	 */
	public String getAdditionalProposalInfo() {
		return null;
	}

	/*
	 * @see ICompletionProposal#getDisplayString()
	 */
	public String getDisplayString() {
		return fVariable.getType() + " - " + fVariable.getDescription(); //$NON-NLS-1$
	}

	/*
	 * @see ICompletionProposal#getImage()
	 */
	public Image getImage() {
		return null;
	}

	/*
	 * @see ICompletionProposal#getContextInformation()
	 */
	public IContextInformation getContextInformation() {
		return null;
	}
}
