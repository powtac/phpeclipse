package net.sourceforge.phpdt.internal.ui.text.template;

import net.sourceforge.phpdt.internal.ui.PHPUiImages;
import net.sourceforge.phpdt.internal.ui.text.link.LinkedPositionManager;
import net.sourceforge.phpdt.internal.ui.text.link.LinkedPositionUI;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;

/**
 * A PHP local identifier proposal.
 */
public class LocalVariableProposal extends AbstractProposal {

	private final String fIdentifierName;

	private final IRegion fRegion;

	private final int fRelevance;

	/**
	 * Creates a template proposal with a template and its context.
	 * 
	 * @param template
	 *            the template
	 * @param image
	 *            the icon of the proposal.
	 */
	public LocalVariableProposal(String identifierName, IRegion region,
			ITextViewer viewer) {
		this(identifierName, region, viewer, 99);
	}

	public LocalVariableProposal(String identifierName, IRegion region,
			ITextViewer viewer, int relevance) {
		super(viewer);
		fIdentifierName = identifierName;
		fRegion = region;
		fRelevance = relevance;
	}

	/*
	 * @see ICompletionProposal#apply(IDocument)
	 */
	public void apply(IDocument document) {
		try {
			// if (fTemplateBuffer == null)
			// fTemplateBuffer= fContext.evaluate(fTemplate);

			int start = fRegion.getOffset();
			int end = fRegion.getOffset() + fRegion.getLength();

			document.replace(start, end - start, fIdentifierName);

			// translate positions
			LinkedPositionManager manager = new LinkedPositionManager(document);

			LinkedPositionUI editor = new LinkedPositionUI(fViewer, manager);
			editor.setFinalCaretOffset(fIdentifierName.length() + start);
			editor.enter();

			fSelectedRegion = editor.getSelectedRegion();

		} catch (BadLocationException e) {
			PHPeclipsePlugin.log(e);
			openErrorDialog(e);

		}
		// catch (CoreException e) {
		// handleException(e);
		// }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof LocalVariableProposal) {
			return fIdentifierName
					.equals(((LocalVariableProposal) obj).fIdentifierName);
		}
		return false;
	}

	/*
	 * @see ICompletionProposal#getAdditionalProposalInfo()
	 */
	public String getAdditionalProposalInfo() {
		StringBuffer hoverInfoBuffer = new StringBuffer();
		if (fRelevance > 95) {
			hoverInfoBuffer.append("function source variable -");
		} else {
			hoverInfoBuffer.append("editor source variable -");
		}
		hoverInfoBuffer.append(fIdentifierName);
		return hoverInfoBuffer.toString();
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
		return fIdentifierName; // $NON-NLS-1$ //$NON-NLS-1$
	}

	/*
	 * @see ICompletionProposal#getImage()
	 */
	public Image getImage() {
		return PHPUiImages.get(PHPUiImages.IMG_VAR);
	}

	/*
	 * @see IJavaCompletionProposal#getRelevance()
	 */
	public int getRelevance() {
		return fRelevance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return fIdentifierName.hashCode();
	}

}