/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package net.sourceforge.phpdt.internal.ui.text.template;

import java.util.ArrayList;

import net.sourceforge.phpdt.core.ICompilationUnit;
import net.sourceforge.phpdt.internal.corext.template.php.CompilationUnitContextType;
import net.sourceforge.phpdt.internal.corext.template.php.JavaContext;
import net.sourceforge.phpdt.internal.corext.template.php.JavaContextType;
import net.sourceforge.phpdt.internal.ui.PHPUiImages;
import net.sourceforge.phpdt.internal.ui.text.java.IPHPCompletionProposal;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.graphics.Point;

public class IdentifierEngine {

	/** The context type. */
	private JavaContextType fContextType;

	/** The result proposals. */
	private ArrayList fProposals = new ArrayList();

	/**
	 * Creates the template engine for a particular context type. See
	 * <code>TemplateContext</code> for supported context types.
	 */
	public IdentifierEngine(JavaContextType contextType) {
		// Assert.isNotNull(contextType);
		fContextType = contextType;
	}

	/**
	 * Empties the collector.
	 * 
	 * @param viewer
	 *            the text viewer
	 * @param unit
	 *            the compilation unit (may be <code>null</code>)
	 */
	public void reset() {
		fProposals.clear();
	}

	/**
	 * Returns the array of matching templates.
	 */
	public IPHPCompletionProposal[] getResults() {
		return (IPHPCompletionProposal[]) fProposals
				.toArray(new IPHPCompletionProposal[fProposals.size()]);
	}

	/**
	 * Inspects the context of the compilation unit around
	 * <code>completionPosition</code> and feeds the collector with proposals.
	 * 
	 * @param viewer
	 *            the text viewer
	 * @param completionPosition
	 *            the context position in the document of the text viewer
	 * @param compilationUnit
	 *            the compilation unit (may be <code>null</code>)
	 */
	public void complete(ITextViewer viewer, int completionPosition,
			Object[] identifiers, ICompilationUnit compilationUnit)
	// hrows JavaModelException
	{
		IDocument document = viewer.getDocument();

		if (!(fContextType instanceof CompilationUnitContextType))
			return;

		Point selection = viewer.getSelectedRange();
		// remember selected text
		String selectedText = null;
		if (selection.y != 0) {
			try {
				selectedText = document.get(selection.x, selection.y);
			} catch (BadLocationException e) {
			}
		}

		// ((CompilationUnitContextType)
		// fContextType).setContextParameters(document, completionPosition,
		// selection.y); //mpilationUnit);

		// JavaContext context = (JavaContext) fContextType.createContext();
		JavaContext context = (JavaContext) fContextType.createContext(
				document, completionPosition, selection.y, compilationUnit);
		context.setVariable("selection", selectedText); //$NON-NLS-1$

		int start = context.getStart();
		int end = context.getEnd();
		IRegion region = new Region(start, end - start);

		// Template[] templates= Templates.getInstance().getTemplates();
		String identifier = null;
		int maxProposals = PHPeclipsePlugin.MAX_PROPOSALS;

		for (int i = 0; i != identifiers.length; i++) {
			identifier = (String) identifiers[i];
			if (context.canEvaluate(identifier)) {
				if (maxProposals-- < 0) {
					return;
				}
				fProposals.add(new IdentifierProposal(identifier, context,
						region, viewer, PHPUiImages.get(PHPUiImages.IMG_FUN),
						PHPUiImages.get(PHPUiImages.IMG_VAR)));
			}
		}
	}

}
