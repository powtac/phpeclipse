package net.sourceforge.phpdt.internal.ui.text.template;

import net.sourceforge.phpdt.internal.corext.template.TemplateMessages;
import net.sourceforge.phpdt.internal.corext.template.php.JavaContext;
import net.sourceforge.phpdt.internal.ui.text.link.LinkedPositionManager;
import net.sourceforge.phpdt.internal.ui.text.link.LinkedPositionUI;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.swt.graphics.Image;

/**
 * A PHP identifier proposal.
 */
public class IdentifierProposal extends AbstractProposal {
	private final TemplateContext fContext;

	private final Image fImage_fun;

	private final Image fImage_var;

	private final IRegion fRegion;

	private final String fTemplate;

	public IdentifierProposal(String template, TemplateContext context,
			IRegion region, ITextViewer viewer, Image image_fun, Image image_var) {
		super(viewer);
		fTemplate = template;
		fContext = context;

		fImage_fun = image_fun;
		fImage_var = image_var;
		fRegion = region;
	}

	public void apply(IDocument document) {
		try {
			int start = fRegion.getOffset();
			int end = fRegion.getOffset() + fRegion.getLength();
			document.replace(start, end - start, fTemplate);
			// translate positions
			LinkedPositionManager manager = new LinkedPositionManager(document);

			LinkedPositionUI editor = new LinkedPositionUI(fViewer, manager);
			editor.setFinalCaretOffset(fTemplate.length() + start);
			editor.enter();
			fSelectedRegion = editor.getSelectedRegion();
		} catch (BadLocationException e) {
			PHPeclipsePlugin.log(e);
			openErrorDialog(e);
		}
	}

	public String getAdditionalProposalInfo() {
		return textToHTML(fTemplate); 
	}

	public String getDisplayString() {
		return fTemplate
				+ TemplateMessages.getString("TemplateProposal.delimiter")
				+ fTemplate; // $NON-NLS-1$
	}

	public Image getImage() {
		if (fTemplate.charAt(0) == '$') {
			return fImage_var;
		}
		return fImage_fun;
	}

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