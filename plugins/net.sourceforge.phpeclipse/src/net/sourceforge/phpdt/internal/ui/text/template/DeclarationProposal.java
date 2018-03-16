/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package net.sourceforge.phpdt.internal.ui.text.template;

import net.sourceforge.phpdt.internal.corext.phpdoc.PHPDocUtil;
import net.sourceforge.phpdt.internal.corext.template.TemplateMessages;
import net.sourceforge.phpdt.internal.corext.template.php.JavaContext;
import net.sourceforge.phpdt.internal.ui.PHPUiImages;
import net.sourceforge.phpdt.internal.ui.text.link.LinkedPositionManager;
import net.sourceforge.phpdt.internal.ui.text.link.LinkedPositionUI;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;
import net.sourceforge.phpeclipse.builder.PHPIdentifierLocation;

import org.eclipse.core.resources.IProject;
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
public class DeclarationProposal extends AbstractProposal { // implements
	// IPHPCompletionProposal
	// {
	private IProject fProject;

	private final TemplateContext fContext;

	private final PHPIdentifierLocation fLocation;

	String fInfo;

	// private TemplateBuffer fTemplateBuffer;
	// private String fOldText;
	// private final Image fImage_fun;
	// private final Image fImage_var;
	private final IRegion fRegion;

	// private IRegion fSelectedRegion; // initialized by apply()

	private final String fIdentifierName;

	// private final ITextViewer fViewer;

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
	public DeclarationProposal(IProject project, String identifierName,
			PHPIdentifierLocation location, TemplateContext context,
			IRegion region, ITextViewer viewer) {
		super(viewer);
		// Image image_fun,
		// Image image_var) {
		fProject = project;
		fIdentifierName = identifierName;
		fLocation = location;
		fContext = context;
		fRegion = region;
		fInfo = null;
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

			switch (fLocation.getType()) {
			case PHPIdentifierLocation.FUNCTION:
				document.replace(start, end - start, fIdentifierName + "()");
				break;
			case PHPIdentifierLocation.CONSTRUCTOR:
				document.replace(start, end - start, fIdentifierName + "()");
				break;
			case PHPIdentifierLocation.METHOD:
				document.replace(start, end - start, fIdentifierName + "()");
				break;

			default:
				document.replace(start, end - start, fIdentifierName);
			}

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
			switch (fLocation.getType()) {
			case PHPIdentifierLocation.FUNCTION:
				editor
						.setFinalCaretOffset(fIdentifierName.length() + start
								+ 1);
				break;
			case PHPIdentifierLocation.CONSTRUCTOR:
				editor
						.setFinalCaretOffset(fIdentifierName.length() + start
								+ 1);
				break;
			case PHPIdentifierLocation.METHOD:
				editor
						.setFinalCaretOffset(fIdentifierName.length() + start
								+ 1);
				break;

			default:
				editor.setFinalCaretOffset(fIdentifierName.length() + start);
			}
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
	 * @see ICompletionProposal#getAdditionalProposalInfo()
	 */
	public String getAdditionalProposalInfo() {
		if (fInfo == null) {
			fInfo = computeProposalInfo();
		}
		return fInfo;
	}

	private String computeProposalInfo() {
		StringBuffer hoverInfoBuffer = new StringBuffer();
		// String workspaceLocation =
		// PHPeclipsePlugin.getWorkspace().getRoot().getLocation().toString();
		String workspaceLocation;
		if (fProject != null) {
			workspaceLocation = fProject.getLocation().toString() + '/';
		} else {
			// should never happen?
			workspaceLocation = PHPeclipsePlugin.getWorkspace().getRoot()
					.getFullPath().toString();
		}
		String filename = workspaceLocation + fLocation.getFilename();
		PHPDocUtil.appendPHPDoc(hoverInfoBuffer, filename, fLocation);
		return hoverInfoBuffer.toString();
	}

	public IContextInformation getContextInformation() {
		if (fContextInfo == null) {
			if (fLocation != null) {
				fInfo = fLocation.getUsage();
				if (fInfo != null) {
					// extract the parameter context information for the
					// function:
					int i0 = fInfo.indexOf('(');
					int newline = fInfo.indexOf('\n');
					if (i0 >= 0 && (i0 < newline || newline < 0)) {
						int i1 = fInfo.indexOf(')', i0 + 1);
						if (i1 > 0) {

							fContextInfo = new ContextInformation(null, fInfo
									.substring(i0 + 1, i1));
						} else {
							fContextInfo = new ContextInformation(null, fInfo);
						}
					} else {
						fContextInfo = new ContextInformation(null, fInfo);
					}
				}
			}
		}
		return fContextInfo;
	}

	/*
	 * @see ICompletionProposal#getDisplayString()
	 */
	public String getDisplayString() {
		String workspaceLocation;
		String workspaceName;
		if (fProject != null) {
			workspaceLocation = fProject.getFullPath().toString() + '/';
			workspaceName = fProject.getName().toString() + '/';
		} else {
			// should never happen?
			workspaceLocation = PHPeclipsePlugin.getWorkspace().getRoot()
					.getFullPath().toString();
			workspaceName = workspaceLocation;
		}
		String filename = fLocation.getFilename();
		String usage = PHPDocUtil.getUsage(workspaceLocation + filename, fLocation);
		String result = fIdentifierName
				+ TemplateMessages.getString("TemplateProposal.delimiter");
		if (usage.length() > 0) {
			result += usage
					+ TemplateMessages.getString("TemplateProposal.delimiter");
		}
		result += workspaceName + filename;
		return result;
	}

	/*
	 * @see ICompletionProposal#getImage()
	 */
	public Image getImage() {
		switch (fLocation.getType()) {
		case PHPIdentifierLocation.FUNCTION:
			return PHPUiImages.get(PHPUiImages.IMG_FUN);
		case PHPIdentifierLocation.CLASS:
			return PHPUiImages.get(PHPUiImages.IMG_CLASS);
		case PHPIdentifierLocation.CONSTRUCTOR:
			return PHPUiImages.get(PHPUiImages.IMG_CLASS);
		case PHPIdentifierLocation.METHOD:
			return PHPUiImages.get(PHPUiImages.IMG_FUN);
		case PHPIdentifierLocation.DEFINE:
			return PHPUiImages.get(PHPUiImages.IMG_DEFINE);
		case PHPIdentifierLocation.VARIABLE:
			return PHPUiImages.get(PHPUiImages.IMG_VAR);
		case PHPIdentifierLocation.GLOBAL_VARIABLE:
			return PHPUiImages.get(PHPUiImages.IMG_VAR);
		}
		return PHPUiImages.get(PHPUiImages.IMG_FUN);
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
				return 80;
			case '>': // ->
			case ':': // ::
				return 85;
			default:
				return 0;
			}
		} else {
			return 80;
		}
	}

}