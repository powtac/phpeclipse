/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package net.sourceforge.phpdt.internal.ui.text.template;

import net.sourceforge.phpdt.internal.corext.template.TemplateMessages;
import net.sourceforge.phpdt.internal.corext.template.php.JavaContext;
import net.sourceforge.phpdt.internal.ui.PHPUiImages;
import net.sourceforge.phpdt.internal.ui.text.link.LinkedPositionManager;
import net.sourceforge.phpdt.internal.ui.text.link.LinkedPositionUI;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;
import net.sourceforge.phpeclipse.phpeditor.php.PHPFunction;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.swt.graphics.Image;

// import net.sourceforge.phpdt.internal.ui.text.link.LinkedPositionManager;
// import net.sourceforge.phpdt.internal.ui.text.link.LinkedPositionUI;
// import net.sourceforge.phpdt.internal.ui.util.ExceptionHandler;

/**
 * A PHP identifier proposal.
 */
public class BuiltInProposal extends AbstractProposal {
	private final TemplateContext fContext;

	private final PHPFunction fFunction;

	private final IRegion fRegion;

	private final String fBuiltinFunctionName;

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
	public BuiltInProposal(String functionName, PHPFunction function,
			TemplateContext context, IRegion region, ITextViewer viewer) {
		super(viewer);
		fBuiltinFunctionName = functionName;
		fFunction = function;
		fContext = context;
		// fViewer = viewer;
		fRegion = region;
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

			// insert template string
			// String templateString = fTemplate; //
			// fTemplateBuffer.getString();
			document.replace(start, end - start, fBuiltinFunctionName + "()");

			// translate positions
			LinkedPositionManager manager = new LinkedPositionManager(document);
			// TemplatePosition[] variables= fTemplateBuffer.getVariables();
			// for (int i= 0; i != variables.length; i++) {
			// TemplatePosition variable= variables[i];
			//
			// if (variable.isResolved())
			// continue;
			//
			// int[] offsets= variable.getOffsets();
			// int length= variable.getLength();
			//
			// for (int j= 0; j != offsets.length; j++)
			// manager.addPosition(offsets[j] + start, length);
			// }

			LinkedPositionUI editor = new LinkedPositionUI(fViewer, manager);
			editor.setFinalCaretOffset(fBuiltinFunctionName.length() + start
					+ 1);
			// editor.setFinalCaretOffset(getCaretOffset(fTemplateBuffer) +
			// start);
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

	public String getAdditionalProposalInfo() {
		return fFunction.getHoverText();
	}

	public IContextInformation getContextInformation() {
		if (fContextInfo == null) {
			String contextInfoString = fFunction.getHoverText();
			if (contextInfoString != null && contextInfoString.length() > 0) {
				// extract the parameter context information for the function:
				int i0 = contextInfoString.indexOf('(');
				int newline = contextInfoString.indexOf('\n');
				if (i0 >= 0 && (i0 < newline || newline < 0)) {
					int i1 = contextInfoString.indexOf(')', i0 + 1);
					if (i1 > 0) {
						fContextInfo = new ContextInformation(null,
								contextInfoString.substring(i0 + 1, i1));
					} else {
						fContextInfo = new ContextInformation(null,
								contextInfoString);
					}
				} else {
					fContextInfo = new ContextInformation(null,
							contextInfoString);
				}
			}
		}
		return fContextInfo;
	}

	/*
	 * @see ICompletionProposal#getDisplayString()
	 */
	public String getDisplayString() {
		return fBuiltinFunctionName
				+ TemplateMessages.getString("TemplateProposal.delimiter") + fFunction.getUsage(); // $NON-NLS-1$
		// //$NON-NLS-1$
	}

	/*
	 * @see ICompletionProposal#getImage()
	 */
	public Image getImage() {
		return PHPUiImages.get(PHPUiImages.IMG_BUILTIN);
	}

	/*
	 * @see IJavaCompletionProposal#getRelevance()
	 */
	public int getRelevance() {

		if (fContext instanceof JavaContext) {
			JavaContext context = (JavaContext) fContext;
			switch (context.getCharacterBeforeStart()) {
			// high relevance after whitespace
			case ' ':
			case '\r':
			case '\n':
			case '\t':
				return 50;

			default:
				return 0;
			}
		} else {
			return 50;
		}
	}

}