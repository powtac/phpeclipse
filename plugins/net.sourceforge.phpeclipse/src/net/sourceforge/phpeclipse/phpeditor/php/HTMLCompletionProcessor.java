/**********************************************************************
 Copyright (c) 2000, 2002 IBM Corp. and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html

 Contributors:
 IBM Corporation - Initial implementation
 www.phpeclipse.de
 **********************************************************************/
package net.sourceforge.phpeclipse.phpeditor.php;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sourceforge.phpdt.core.ICompilationUnit;
import net.sourceforge.phpdt.internal.ui.text.java.IPHPCompletionProposal;
import net.sourceforge.phpdt.internal.ui.text.java.PHPCompletionProposalComparator;
import net.sourceforge.phpdt.internal.ui.text.template.contentassist.TemplateEngine;
import net.sourceforge.phpdt.ui.IWorkingCopyManager;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationExtension;
import org.eclipse.jface.text.contentassist.IContextInformationPresenter;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;

/**
 * HTML completion processor.
 */
public class HTMLCompletionProcessor implements IContentAssistProcessor {

	/**
	 * Simple content assist tip closer. The tip is valid in a range of 5
	 * characters around its popup location.
	 */
	protected static class Validator implements IContextInformationValidator,
			IContextInformationPresenter {

		protected int fInstallOffset;

		/*
		 * @see IContextInformationValidator#isContextInformationValid(int)
		 */
		public boolean isContextInformationValid(int offset) {
			return Math.abs(fInstallOffset - offset) < 5;
		}

		/*
		 * @see IContextInformationValidator#install(IContextInformation,
		 *      ITextViewer, int)
		 */
		public void install(IContextInformation info, ITextViewer viewer,
				int offset) {
			fInstallOffset = offset;
		}

		/*
		 * @see org.eclipse.jface.text.contentassist.IContextInformationPresenter#updatePresentation(int,
		 *      TextPresentation)
		 */
		public boolean updatePresentation(int documentPosition,
				TextPresentation presentation) {
			return false;
		}
	};

	private static class ContextInformationWrapper implements
			IContextInformation, IContextInformationExtension {

		private final IContextInformation fContextInformation;

		private int fPosition;

		public ContextInformationWrapper(IContextInformation contextInformation) {
			fContextInformation = contextInformation;
		}

		/*
		 * @see IContextInformation#getContextDisplayString()
		 */
		public String getContextDisplayString() {
			return fContextInformation.getContextDisplayString();
		}

		/*
		 * @see IContextInformation#getImage()
		 */
		public Image getImage() {
			return fContextInformation.getImage();
		}

		/*
		 * @see IContextInformation#getInformationDisplayString()
		 */
		public String getInformationDisplayString() {
			return fContextInformation.getInformationDisplayString();
		}

		/*
		 * @see IContextInformationExtension#getContextInformationPosition()
		 */
		public int getContextInformationPosition() {
			return fPosition;
		}

		public void setContextInformationPosition(int position) {
			fPosition = position;
		}
	};

	protected IContextInformationValidator fValidator = new Validator();

	private TemplateEngine fTemplateEngine;

	private char[] fProposalAutoActivationSet;

	private PHPCompletionProposalComparator fComparator;

	private int fNumberOfComputedResults = 0;

	private IEditorPart fEditor;

	protected IWorkingCopyManager fManager;

	public HTMLCompletionProcessor(IEditorPart editor) {
		fEditor = editor;
		fManager = PHPeclipsePlugin.getDefault().getWorkingCopyManager();

		TemplateContextType contextType = PHPeclipsePlugin.getDefault()
				.getTemplateContextRegistry().getContextType("html"); //$NON-NLS-1$
		if (contextType != null)
			fTemplateEngine = new TemplateEngine(contextType);

		fComparator = new PHPCompletionProposalComparator();
	}

	/*
	 * (non-Javadoc) Method declared on IContentAssistProcessor
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int documentOffset) {
		int contextInformationPosition = guessContextInformationPosition(
				viewer, documentOffset);
		return internalComputeCompletionProposals(viewer, documentOffset,
				contextInformationPosition);
	}

	private ICompletionProposal[] internalComputeCompletionProposals(
			ITextViewer viewer, int offset, int contextOffset) {
		IDocument document = viewer.getDocument();
		ICompilationUnit unit = fManager.getWorkingCopy(fEditor
				.getEditorInput());

		if (fTemplateEngine != null) {
			ICompletionProposal[] results;
			// try {
			fTemplateEngine.reset();
			fTemplateEngine.complete(viewer, offset, unit);
			// } catch (JavaModelException x) {
			// Shell shell= viewer.getTextWidget().getShell();
			// ErrorDialog.openError(shell,
			// JavaTextMessages.getString("CompletionProcessor.error.accessing.title"),
			// JavaTextMessages.getString("CompletionProcessor.error.accessing.message"),
			// x.getStatus()); //$NON-NLS-2$ //$NON-NLS-1$
			// }

			IPHPCompletionProposal[] templateResults = fTemplateEngine
					.getResults();

			// concatenate arrays
			IPHPCompletionProposal[] total;
			total = new IPHPCompletionProposal[templateResults.length];
			System.arraycopy(templateResults, 0, total, 0,
					templateResults.length);
			results = total;

			fNumberOfComputedResults = (results == null ? 0 : results.length);
			/*
			 * Order here and not in result collector to make sure that the
			 * order applies to all proposals and not just those of the
			 * compilation unit.
			 */
			return order(results);
		}
		return new IPHPCompletionProposal[0];
	}

	private int guessContextInformationPosition(ITextViewer viewer, int offset) {
		int contextPosition = offset;
		IDocument document = viewer.getDocument();
		return contextPosition;
	}

	/*
	 * (non-Javadoc) Method declared on IContentAssistProcessor
	 */
	// public IContextInformation[] computeContextInformation(ITextViewer
	// viewer, int documentOffset) {
	// IContextInformation[] result = new IContextInformation[5];
	// for (int i = 0; i < result.length; i++)
	// result[i] = new
	// ContextInformation(MessageFormat.format(PHPEditorMessages.getString("CompletionProcessor.ContextInfo.display.pattern"),
	// new Object[] { new Integer(i), new Integer(documentOffset)}),
	// //$NON-NLS-1$
	// MessageFormat.format(PHPEditorMessages.getString("CompletionProcessor.ContextInfo.value.pattern"),
	// new Object[] { new Integer(i), new Integer(documentOffset - 5), new
	// Integer(documentOffset + 5)})); //$NON-NLS-1$
	// return result;
	// }
	/**
	 * @see IContentAssistProcessor#computeContextInformation(ITextViewer, int)
	 */
	public IContextInformation[] computeContextInformation(ITextViewer viewer,
			int offset) {
		int contextInformationPosition = guessContextInformationPosition(
				viewer, offset);
		List result = addContextInformations(viewer, contextInformationPosition);
		return (IContextInformation[]) result
				.toArray(new IContextInformation[result.size()]);
	}

	private List addContextInformations(ITextViewer viewer, int offset) {
		ICompletionProposal[] proposals = internalComputeCompletionProposals(
				viewer, offset, -1);

		List result = new ArrayList();
		for (int i = 0; i < proposals.length; i++) {
			IContextInformation contextInformation = proposals[i]
					.getContextInformation();
			if (contextInformation != null) {
				ContextInformationWrapper wrapper = new ContextInformationWrapper(
						contextInformation);
				wrapper.setContextInformationPosition(offset);
				result.add(wrapper);
			}
		}
		return result;
	}

	/**
	 * Order the given proposals.
	 */
	private ICompletionProposal[] order(ICompletionProposal[] proposals) {
		Arrays.sort(proposals, fComparator);
		return proposals;
	}

	/**
	 * Tells this processor to order the proposals alphabetically.
	 * 
	 * @param order
	 *            <code>true</code> if proposals should be ordered.
	 */
	public void orderProposalsAlphabetically(boolean order) {
		fComparator.setOrderAlphabetically(order);
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

	/*
	 * (non-Javadoc) Method declared on IContentAssistProcessor
	 */
	public char[] getContextInformationAutoActivationCharacters() {
		return new char[] {};
	}

	/*
	 * (non-Javadoc) Method declared on IContentAssistProcessor
	 */
	public IContextInformationValidator getContextInformationValidator() {
		return fValidator;
	}

	/*
	 * (non-Javadoc) Method declared on IContentAssistProcessor
	 */
	public String getErrorMessage() {
		return null;
	}
}
