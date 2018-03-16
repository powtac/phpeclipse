/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package net.sourceforge.phpdt.internal.ui.text.template;

import net.sourceforge.phpdt.internal.ui.text.link.LinkedPositionManager;
import net.sourceforge.phpdt.internal.ui.text.link.LinkedPositionUI;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.swt.graphics.Image;

/**
 * A PHP identifier proposal.
 */
public class SQLProposal extends AbstractProposal { 
	private final TemplateContext fContext;

	private final Image fImage_var;

	private final IRegion fRegion;

	private final String fColumnName;

	private final String fTableName;

	private int fRelevance;

	/**
	 * Creates a template proposal with a template and its context.
	 * 
	 * @param template
	 *            the template
	 * @param context
	 *            the context in which the template was requested.
	 * @param image
	 *            the icon of the proposal.
	 */
	public SQLProposal(String tableName, TemplateContext context,
			IRegion region, ITextViewer viewer, Image image_var) {
		super(viewer);
		fTableName = tableName;
		fColumnName = null;
		fContext = context;
		fImage_var = image_var;
		fRegion = region;
		fRelevance = 0;
	}

	public SQLProposal(String tableName, String columnName,
			TemplateContext context, IRegion region, ITextViewer viewer,
			Image image_var) {
		super(viewer);
		fTableName = tableName;
		fColumnName = columnName;
		fContext = context;
		fImage_var = image_var;
		fRegion = region;
		fRelevance = 0;
	}

	/*
	 * @see ICompletionProposal#apply(IDocument)
	 */
	public void apply(IDocument document) {
		try {
			int start = fRegion.getOffset();
			int end = fRegion.getOffset() + fRegion.getLength();
			String resultString = fTableName;
			if (fColumnName != null) {
				resultString = fColumnName;
			}
			// insert template string
			document.replace(start, end - start, resultString);
			// translate positions
			LinkedPositionManager manager = new LinkedPositionManager(document);
			LinkedPositionUI editor = new LinkedPositionUI(fViewer, manager);
			editor.setFinalCaretOffset(resultString.length() + start);
			editor.enter();
			fSelectedRegion = editor.getSelectedRegion();
		} catch (BadLocationException e) {
			PHPeclipsePlugin.log(e);
			openErrorDialog(e);
		}
	}

	/*
	 * @see ICompletionProposal#getAdditionalProposalInfo()
	 */
	public String getAdditionalProposalInfo() {
		if (fColumnName == null) {
			return textToHTML(fTableName);
		}
		return fColumnName + " (Table: " + fTableName + ")";
	}

	/*
	 * @see ICompletionProposal#getContextInformation()
	 */
	public IContextInformation getContextInformation() {
		return null;
	}

	/*
	 * @see ICompletionProposal#getDisplayString()
	 */
	public String getDisplayString() {
		if (fColumnName == null) {
			return fTableName;
		}
		return fColumnName + " (Table: " + fTableName + ")"; // $NON-NLS-1$
	}

	/*
	 * @see ICompletionProposal#getImage()
	 */
	public Image getImage() {
		return fImage_var;
	}

	/*
	 * @see IJavaCompletionProposal#getRelevance()
	 */
	public int getRelevance() {
		return fRelevance;
	}

	/**
	 * @param relevance
	 *            The relevance to set.
	 */
	public void setRelevance(int relevance) {
		fRelevance = relevance;
	}
}