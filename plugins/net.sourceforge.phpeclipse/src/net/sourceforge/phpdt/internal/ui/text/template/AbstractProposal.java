/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package net.sourceforge.phpdt.internal.ui.text.template;

import net.sourceforge.phpdt.internal.corext.template.TemplateMessages;
import net.sourceforge.phpdt.internal.ui.text.java.IPHPCompletionProposal;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;


/**
 * A PHP identifier proposal.
 */
public abstract class AbstractProposal implements IPHPCompletionProposal {
	protected IRegion fSelectedRegion; // initialized by apply()

	protected final ITextViewer fViewer;

	protected ContextInformation fContextInfo;

	public AbstractProposal(ITextViewer viewer) {
		fContextInfo = null;
		fViewer = viewer;
	}

	protected static String textToHTML(String string) {
		StringBuffer buffer = new StringBuffer(string.length());
		buffer.append("<pre>"); //$NON-NLS-1$

		for (int i = 0; i != string.length(); i++) {
			char ch = string.charAt(i);

			switch (ch) {
			case '&':
				buffer.append("&amp;"); //$NON-NLS-1$
				break;

			case '<':
				buffer.append("&lt;"); //$NON-NLS-1$
				break;

			case '>':
				buffer.append("&gt;"); //$NON-NLS-1$
				break;

			case '\t':
				buffer.append("    "); //$NON-NLS-1$
				break;

			case '\n':
				buffer.append("<br>"); //$NON-NLS-1$
				break;

			default:
				buffer.append(ch);
				break;
			}
		}

		buffer.append("</pre>"); //$NON-NLS-1$
		return buffer.toString();
	}

	/*
	 * @see ICompletionProposal#getSelection(IDocument)
	 */
	public Point getSelection(IDocument document) {
		return new Point(fSelectedRegion.getOffset(), fSelectedRegion
				.getLength());
	}

	protected void handleException(CoreException e) {
		PHPeclipsePlugin.log(e);
	}

	protected void openErrorDialog(BadLocationException e) {
		Shell shell = fViewer.getTextWidget().getShell();
		MessageDialog.openError(shell, TemplateMessages
				.getString("TemplateEvaluator.error.title"), e.getMessage()); //$NON-NLS-1$
	}

	public IContextInformation getContextInformation() {
		return null;
	}

}