/*****************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *****************************************************************************/

package net.sourceforge.phpdt.internal.ui.text.spelling;

import java.text.MessageFormat;
import java.util.Locale;

import net.sourceforge.phpdt.core.IProblemRequestor;
import net.sourceforge.phpdt.core.compiler.IProblem;
import net.sourceforge.phpdt.internal.ui.PHPUIMessages;
import net.sourceforge.phpdt.internal.ui.text.spelling.engine.ISpellCheckEngine;
import net.sourceforge.phpdt.internal.ui.text.spelling.engine.ISpellCheckPreferenceKeys;
import net.sourceforge.phpdt.internal.ui.text.spelling.engine.ISpellChecker;
import net.sourceforge.phpdt.internal.ui.text.spelling.engine.ISpellEvent;
import net.sourceforge.phpdt.internal.ui.text.spelling.engine.ISpellEventListener;
import net.sourceforge.phpeclipse.phpeditor.php.PHPDocumentPartitioner;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Reconcile strategy to spell-check comments.
 * 
 * @since 3.0
 */
public class SpellReconcileStrategy implements IReconcilingStrategy,
		IReconcilingStrategyExtension, ISpellEventListener {

	/**
	 * Spelling problem to be accepted by problem requestors.
	 */
	public class SpellProblem implements IProblem {

		/** The id of the problem */
		public static final int Spelling = 0x80000000;

		/** The end offset of the problem */
		private int fEnd = 0;

		/** The line number of the problem */
		private int fLine = 1;

		/** Was the word found in the dictionary? */
		private boolean fMatch;

		/** Does the word start a new sentence? */
		private boolean fSentence = false;

		/** The start offset of the problem */
		private int fStart = 0;

		/** The word which caused the problem */
		private final String fWord;

		/**
		 * Creates a new spelling problem
		 * 
		 * @param word
		 *            The word which caused the problem
		 */
		protected SpellProblem(final String word) {
			fWord = word;
		}

		/*
		 * @see net.sourceforge.phpdt.core.compiler.IProblem#getArguments()
		 */
		public String[] getArguments() {

			String prefix = ""; //$NON-NLS-1$
			String postfix = ""; //$NON-NLS-1$

			try {

				final IRegion line = fDocument
						.getLineInformationOfOffset(fStart);

				prefix = fDocument.get(line.getOffset(), fStart
						- line.getOffset());
				postfix = fDocument.get(fEnd + 1, line.getOffset()
						+ line.getLength() - fEnd);

			} catch (BadLocationException exception) {
				// Do nothing
			}
			return new String[] {
					fWord,
					prefix,
					postfix,
					fSentence ? Boolean.toString(true) : Boolean
							.toString(false),
					fMatch ? Boolean.toString(true) : Boolean.toString(false) };
		}

		/*
		 * @see net.sourceforge.phpdt.core.compiler.IProblem#getID()
		 */
		public int getID() {
			return Spelling;
		}

		/*
		 * @see net.sourceforge.phpdt.core.compiler.IProblem#getMessage()
		 */
		public String getMessage() {

			if (fSentence && fMatch)
				return MessageFormat
						.format(
								PHPUIMessages
										.getString("Spelling.error.case.label"), new String[] { fWord }); //$NON-NLS-1$

			return MessageFormat.format(PHPUIMessages
					.getString("Spelling.error.label"), new String[] { fWord }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		/*
		 * @see net.sourceforge.phpdt.core.compiler.IProblem#getOriginatingFileName()
		 */
		public char[] getOriginatingFileName() {
			return fEditor.getEditorInput().getName().toCharArray();
		}

		/*
		 * @see net.sourceforge.phpdt.core.compiler.IProblem#getSourceEnd()
		 */
		public final int getSourceEnd() {
			return fEnd;
		}

		/*
		 * @see net.sourceforge.phpdt.core.compiler.IProblem#getSourceLineNumber()
		 */
		public final int getSourceLineNumber() {
			return fLine;
		}

		/*
		 * @see net.sourceforge.phpdt.core.compiler.IProblem#getSourceStart()
		 */
		public final int getSourceStart() {
			return fStart;
		}

		/**
		 * Was the problem word found in the dictionary?
		 * 
		 * @return <code>true</code> iff the word was found,
		 *         <code>false</code> otherwise
		 */
		public final boolean isDictionaryMatch() {
			return fMatch;
		}

		/*
		 * @see net.sourceforge.phpdt.core.compiler.IProblem#isError()
		 */
		public final boolean isError() {
			return false;
		}

		/**
		 * Does the problem word start a new sentence?
		 * 
		 * @return <code>true</code> iff it starts a new sentence,
		 *         <code>false</code> otherwise
		 */
		public final boolean isSentenceStart() {
			return fSentence;
		}

		/*
		 * @see net.sourceforge.phpdt.core.compiler.IProblem#isWarning()
		 */
		public final boolean isWarning() {
			return true;
		}

		/**
		 * Sets whether the problem word was found in the dictionary.
		 * 
		 * @param match
		 *            <code>true</code> iff the word was found,
		 *            <code>false</code> otherwise
		 */
		public final void setDictionaryMatch(final boolean match) {
			fMatch = match;
		}

		/**
		 * Sets whether the problem word starts a new sentence.
		 * 
		 * @param sentence
		 *            <code>true</code> iff the word starts a new sentence,
		 *            <code>false</code> otherwise.
		 */
		public final void setSentenceStart(final boolean sentence) {
			fSentence = sentence;
		}

		/*
		 * @see net.sourceforge.phpdt.core.compiler.IProblem#setSourceEnd(int)
		 */
		public final void setSourceEnd(final int end) {
			fEnd = end;
		}

		/*
		 * @see net.sourceforge.phpdt.core.compiler.IProblem#setSourceLineNumber(int)
		 */
		public final void setSourceLineNumber(final int line) {
			fLine = line;
		}

		/*
		 * @see net.sourceforge.phpdt.core.compiler.IProblem#setSourceStart(int)
		 */
		public final void setSourceStart(final int start) {
			fStart = start;
		}
	}

	/** The document to operate on */
	private IDocument fDocument = null;

	/** The text editor to operate on */
	private final ITextEditor fEditor;

	/** The current locale */
	private Locale fLocale = SpellCheckEngine.getDefaultLocale();

	/** The partitioning of the document */
	private final String fPartitioning;

	/** The preference store to use */
	private final IPreferenceStore fPreferences;

	/** The problem requestor */
	private IProblemRequestor fRequestor;

	/**
	 * Creates a new comment reconcile strategy.
	 * 
	 * @param editor
	 *            The text editor to operate on
	 * @param partitioning
	 *            The partitioning of the document
	 * @param store
	 *            The preference store to get the preferences from
	 */
	public SpellReconcileStrategy(final ITextEditor editor,
			final String partitioning, final IPreferenceStore store) {
		fEditor = editor;
		fPartitioning = partitioning;
		fPreferences = store;

		updateProblemRequestor();
	}

	/**
	 * Returns the current locale of the spell checking preferences.
	 * 
	 * @return The current locale of the spell checking preferences
	 */
	public Locale getLocale() {

		final String locale = fPreferences
				.getString(ISpellCheckPreferenceKeys.SPELLING_LOCALE);
		if (locale.equals(fLocale.toString()))
			return fLocale;

		if (locale.length() >= 5)
			return new Locale(locale.substring(0, 2), locale.substring(3, 5));

		return SpellCheckEngine.getDefaultLocale();
	}

	/*
	 * @see net.sourceforge.phpdt.internal.ui.text.spelling.engine.ISpellEventListener#handle(net.sourceforge.phpdt.internal.ui.text.spelling.engine.ISpellEvent)
	 */
	public void handle(final ISpellEvent event) {

		if (fRequestor != null) {

			final SpellProblem problem = new SpellProblem(event.getWord());

			problem.setSourceStart(event.getBegin());
			problem.setSourceEnd(event.getEnd());
			problem.setSentenceStart(event.isStart());
			problem.setDictionaryMatch(event.isMatch());

			try {
				problem.setSourceLineNumber(fDocument.getLineOfOffset(event
						.getBegin()) + 1);
			} catch (BadLocationException x) {
				// Do nothing
			}

			fRequestor.acceptProblem(problem);
		}
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#initialReconcile()
	 */
	public void initialReconcile() {
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#reconcile(org.eclipse.jface.text.reconciler.DirtyRegion,org.eclipse.jface.text.IRegion)
	 */
	public void reconcile(final DirtyRegion dirtyRegion, final IRegion subRegion) {
		reconcile(subRegion);
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#reconcile(org.eclipse.jface.text.IRegion)
	 */
	public void reconcile(final IRegion region) {

		if (fPreferences
				.getBoolean(ISpellCheckPreferenceKeys.SPELLING_CHECK_SPELLING)
				&& fRequestor != null) {

			try {

				fRequestor.beginReporting();

				ITypedRegion partition = null;
				final ITypedRegion[] partitions = TextUtilities
						.computePartitioning(fDocument, fPartitioning, 0,
								fDocument.getLength(), false);

				final Locale locale = getLocale();
				final ISpellCheckEngine engine = SpellCheckEngine.getInstance();

				final ISpellChecker checker = engine.createSpellChecker(locale,
						fPreferences);
				if (checker != null) {
					try {
						checker.addListener(this);

						for (int index = 0; index < partitions.length; index++) {
							partition = partitions[index];
							if (!partition.getType().equals(
									IDocument.DEFAULT_CONTENT_TYPE)
									&& !partition
											.getType()
											.equals(
													PHPDocumentPartitioner.PHP_SCRIPT_CODE))
								checker.execute(new SpellCheckIterator(
										fDocument, partition, locale));
						}

					} finally {
						checker.removeListener(this);
					}
				}
			} catch (BadLocationException exception) {
				// Do nothing
			} finally {
				fRequestor.endReporting();
			}
		}
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#setDocument(org.eclipse.jface.text.IDocument)
	 */
	public final void setDocument(final IDocument document) {
		fDocument = document;

		updateProblemRequestor();
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#setProgressMonitor(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public final void setProgressMonitor(final IProgressMonitor monitor) {
		// Do nothing
	}

	/**
	 * Update the problem requestor based on the current editor
	 * 
	 * @since 3.0
	 */
	private void updateProblemRequestor() {
		final IAnnotationModel model = fEditor.getDocumentProvider()
				.getAnnotationModel(fEditor.getEditorInput());
		fRequestor = (model instanceof IProblemRequestor) ? (IProblemRequestor) model
				: null;
	}
}
