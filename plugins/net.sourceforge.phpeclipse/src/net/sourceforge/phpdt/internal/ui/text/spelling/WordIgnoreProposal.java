/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package net.sourceforge.phpdt.internal.ui.text.spelling;

import java.text.MessageFormat;

import net.sourceforge.phpdt.internal.ui.PHPUIMessages;
import net.sourceforge.phpdt.internal.ui.PHPUiImages;
import net.sourceforge.phpdt.internal.ui.text.java.IInvocationContext;
import net.sourceforge.phpdt.internal.ui.text.java.IPHPCompletionProposal;
import net.sourceforge.phpdt.internal.ui.text.spelling.engine.ISpellCheckEngine;
import net.sourceforge.phpdt.internal.ui.text.spelling.engine.ISpellChecker;
import net.sourceforge.phpdt.ui.PreferenceConstants;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * Proposal to ignore the word during the current editing session.
 * 
 * @since 3.0
 */
public class WordIgnoreProposal implements IPHPCompletionProposal {

	/** The invocation context */
	private IInvocationContext fContext;

	/** The word to ignore */
	private String fWord;

	/**
	 * Creates a new spell ignore proposal.
	 * 
	 * @param word
	 *            The word to ignore
	 * @param context
	 *            The invocation context
	 */
	public WordIgnoreProposal(final String word,
			final IInvocationContext context) {
		fWord = word;
		fContext = context;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#apply(org.eclipse.jface.text.IDocument)
	 */
	public final void apply(final IDocument document) {

		final ISpellCheckEngine engine = SpellCheckEngine.getInstance();
		final ISpellChecker checker = engine.createSpellChecker(engine
				.getLocale(), PreferenceConstants.getPreferenceStore());

		if (checker != null)
			checker.ignoreWord(fWord);
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getAdditionalProposalInfo()
	 */
	public String getAdditionalProposalInfo() {
		return MessageFormat
				.format(
						PHPUIMessages.getString("Spelling.ignore.info"), new String[] { WordCorrectionProposal.getHtmlRepresentation(fWord) }); //$NON-NLS-1$
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getContextInformation()
	 */
	public final IContextInformation getContextInformation() {
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getDisplayString()
	 */
	public String getDisplayString() {
		return MessageFormat.format(PHPUIMessages
				.getString("Spelling.ignore.label"), new String[] { fWord }); //$NON-NLS-1$
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getImage()
	 */
	public Image getImage() {
		return PHPUiImages.get(PHPUiImages.IMG_OBJS_NLS_NEVER_TRANSLATE);
	}

	/*
	 * @see net.sourceforge.phpdt.ui.text.java.IJavaCompletionProposal#getRelevance()
	 */
	public final int getRelevance() {
		return Integer.MIN_VALUE + 1;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getSelection(org.eclipse.jface.text.IDocument)
	 */
	public final Point getSelection(final IDocument document) {
		return new Point(fContext.getSelectionOffset(), fContext
				.getSelectionLength());
	}
}
