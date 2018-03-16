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
package net.sourceforge.phpeclipse.ui.templates.template;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.phpeclipse.ui.WebUI;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.swt.graphics.Image;

/**
 * A completion processor for XML templates.
 */
public class BasicCompletionProcessor extends TemplateCompletionProcessor {
	private static final String DEFAULT_IMAGE = "icons/template.gif"; //$NON-NLS-1$

	private char[] fProposalAutoActivationSet;

	// private PHPCompletionProposalComparator fComparator;
	public BasicCompletionProcessor() {
		super();
		// fComparator = new PHPCompletionProposalComparator();
	}

	/**
	 * We watch for angular brackets since those are often part of XML
	 * templates.
	 */
	protected String extractPrefix(ITextViewer viewer, int offset) {
		IDocument document = viewer.getDocument();
		int i = offset;
		if (i > document.getLength())
			return ""; //$NON-NLS-1$

		try {
			while (i > 0) {
				char ch = document.getChar(i - 1);
				if (ch != '<' && ch != '&' && ch != '{'
						&& !Character.isJavaIdentifierPart(ch))
					break;
				i--;
			}

			return document.get(i, offset - i);
		} catch (BadLocationException e) {
			return ""; //$NON-NLS-1$
		}
	}

	/**
	 * Cut out angular brackets for relevance sorting, since the template name
	 * does not contain the brackets.
	 */
	protected int getRelevance(Template template, String prefix) {
		// if (prefix.startsWith("<")) //$NON-NLS-1$
		// prefix= prefix.substring(1);
		if (template.getName().startsWith(prefix))
			return 90;
		return 0;
	}

	/**
	 * Simply return all templates.
	 */
	protected Template[] getTemplates(String contextTypeId) {
		return WebUI.getDefault().getTemplateStore().getTemplates();
	}

	/**
	 * Return the XML context type that is supported by this plugin.
	 */
	protected TemplateContextType getContextType(ITextViewer viewer,
			IRegion region) {
		return WebUI.getDefault().getContextTypeRegistry().getContextType(
				XMLContextType.XML_CONTEXT_TYPE);
	}

	/**
	 * Always return the default image.
	 */
	protected Image getImage(Template template) {
		ImageRegistry registry = WebUI.getDefault().getImageRegistry();
		Image image = registry.get(DEFAULT_IMAGE);
		if (image == null) {
			ImageDescriptor desc = WebUI.imageDescriptorFromPlugin(
					"org.eclipse.ui.examples.javaeditor", DEFAULT_IMAGE); //$NON-NLS-1$
			registry.put(DEFAULT_IMAGE, desc);
			image = registry.get(DEFAULT_IMAGE);
		}
		return image;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer,
	 *      int)
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int offset) {
		ITextSelection selection = (ITextSelection) viewer
				.getSelectionProvider().getSelection();

		// adjust offset to end of normalized selection
		if (selection.getOffset() == offset)
			offset = selection.getOffset() + selection.getLength();

		String prefix = extractPrefix(viewer, offset);
		prefix = prefix.toLowerCase();
		IRegion region = new Region(offset - prefix.length(), prefix.length());
		TemplateContext context = createContext(viewer, region);
		if (context == null)
			return new ICompletionProposal[0];

		context.setVariable("selection", selection.getText()); // name of the
																// selection
																// variables
																// {line,
																// word}_selection
																// //$NON-NLS-1$

		Template[] templates = getTemplates(context.getContextType().getId());

		List matches = new ArrayList();
		for (int i = 0; i < templates.length; i++) {
			Template template = templates[i];
			try {
				context.getContextType().validate(template.getPattern());
			} catch (TemplateException e) {
				continue;
			}

			if (template.getName().startsWith(prefix)) { // &&
															// template.matches(prefix,
															// context.getContextType().getId()))
				matches.add(createProposal(template, context, region,
						getRelevance(template, prefix)));
			}
		}

		return (ICompletionProposal[]) matches
				.toArray(new ICompletionProposal[matches.size()]);

	}

	/**
	 * @see IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
	 */
	public char[] getCompletionProposalAutoActivationCharacters() {
		return fProposalAutoActivationSet;
	}

	/**
	 * Sets this processor's set of characters triggering the activation of the
	 * completion proposal computation.
	 * 
	 * @param activationSet
	 *            the activation set
	 */
	public void setCompletionProposalAutoActivationCharacters(
			char[] activationSet) {
		fProposalAutoActivationSet = activationSet;
	}

	/**
	 * Order the given proposals.
	 */
	// private ICompletionProposal[] order(ICompletionProposal[] proposals) {
	// Arrays.sort(proposals, fComparator);
	// return proposals;
	// }
	/**
	 * Tells this processor to order the proposals alphabetically.
	 * 
	 * @param order
	 *            <code>true</code> if proposals should be ordered.
	 */
	// public void orderProposalsAlphabetically(boolean order) {
	// fComparator.setOrderAlphabetically(order);
	// }
}