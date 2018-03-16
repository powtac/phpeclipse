/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package net.sourceforge.phpdt.internal.ui.text.template;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.SortedMap;

import net.sourceforge.phpdt.core.ICompilationUnit;
import net.sourceforge.phpdt.core.compiler.ITerminalSymbols;
import net.sourceforge.phpdt.internal.corext.template.php.CompilationUnitContextType;
import net.sourceforge.phpdt.internal.corext.template.php.JavaContext;
import net.sourceforge.phpdt.internal.corext.template.php.JavaContextType;
import net.sourceforge.phpdt.internal.ui.text.java.IPHPCompletionProposal;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;
import net.sourceforge.phpeclipse.builder.PHPIdentifierLocation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.graphics.Point;

public class DeclarationEngine {

	/** The context type. */
	private JavaContextType fContextType;

	/** The result proposals. */
	private ArrayList fProposals = new ArrayList();

	/** Token determines last which declarations are allowed for proposal */
	private int fLastSignificantToken;

	private IProject fProject;

	// private IFile fFile;
	private String fFileName;

	/**
	 * Creates the template engine for a particular context type. See
	 * <code>TemplateContext</code> for supported context types.
	 */
	public DeclarationEngine(IProject project, JavaContextType contextType,
			int lastSignificantToken, IFile file) {
		// Assert.isNotNull(contextType);
		fProject = project;
		fContextType = contextType;

		fLastSignificantToken = lastSignificantToken;
		// fFile = file;
		if (file != null) {
			fFileName = file.getProjectRelativePath().toString();
		} else {
			fFileName = "";
		}
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
	public void completeObject(ITextViewer viewer, int completionPosition,
			SortedMap map, ICompilationUnit compilationUnit) {
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

		JavaContext context = (JavaContext) fContextType.createContext(
				document, completionPosition, selection.y, compilationUnit);
		context.setVariable("selection", selectedText); //$NON-NLS-1$

		int start = context.getStart();
		int end = context.getEnd();
		String prefix = context.getKey();
		IRegion region = new Region(start, end - start);

		String identifier = null;

		SortedMap subMap = map.subMap(prefix, prefix + '\255');
		Iterator iter = subMap.keySet().iterator();
		PHPIdentifierLocation location;
		ArrayList list;
		int maxProposals = PHPeclipsePlugin.MAX_PROPOSALS;
		while (iter.hasNext()) {
			identifier = (String) iter.next();
			if (context.canEvaluate(identifier)) {
				list = (ArrayList) subMap.get(identifier);
				for (int i = 0; i < list.size(); i++) {
					location = (PHPIdentifierLocation) list.get(i);
					int type = location.getType();
					switch (fLastSignificantToken) {
					case ITerminalSymbols.TokenNameMINUS_GREATER:
						if (type != PHPIdentifierLocation.METHOD
								&& type != PHPIdentifierLocation.VARIABLE) {
							continue; // for loop
						}
						break;
					case ITerminalSymbols.TokenNameVariable:
						if (type != PHPIdentifierLocation.METHOD
								&& type != PHPIdentifierLocation.VARIABLE) {
							continue; // for loop
						}
						// check all filenames of the subclasses
						// if (fFileName.equals(location.getFilename())) {
						// continue; // for loop
						// }
						break;
					case ITerminalSymbols.TokenNamethis_PHP_COMPLETION:
						if (type != PHPIdentifierLocation.METHOD
								&& type != PHPIdentifierLocation.VARIABLE) {
							continue; // for loop
						}
						// check all filenames of the subclasses
						// if (!fFileName.equals(location.getFilename())) {
						// continue; // for loop
						// }
						break;
					case ITerminalSymbols.TokenNamenew:
						if (type != PHPIdentifierLocation.CLASS
								&& type != PHPIdentifierLocation.CONSTRUCTOR) {
							continue; // for loop
						}
						break;
					default:
						if (type == PHPIdentifierLocation.METHOD
								|| type == PHPIdentifierLocation.CONSTRUCTOR
								|| type == PHPIdentifierLocation.VARIABLE) {
							continue; // for loop
						}
					}
					if (maxProposals-- < 0) {
						return;
					}
					fProposals.add(new DeclarationProposal(fProject,
							identifier, location, context, region, viewer));
				}
			}
		}

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
			SortedMap map, ICompilationUnit compilationUnit) {
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
		// selection.y);

		// CompilationUnitContext context = (CompilationUnitContext)
		// fContextType.createContext();
		JavaContext context = (JavaContext) fContextType.createContext(
				document, completionPosition, selection.y, compilationUnit);
		context.setVariable("selection", selectedText); //$NON-NLS-1$

		int start = context.getStart();
		int end = context.getEnd();
		String prefix = context.getKey();
		IRegion region = new Region(start, end - start);

		String identifier = null;

		SortedMap subMap = map.subMap(prefix, prefix + '\255');
		Iterator iter = subMap.keySet().iterator();
		PHPIdentifierLocation location;
		ArrayList list;
		int maxProposals = PHPeclipsePlugin.MAX_PROPOSALS;
		while (iter.hasNext()) {
			identifier = (String) iter.next();
			if (context.canEvaluate(identifier)) {
				list = (ArrayList) subMap.get(identifier);
				for (int i = 0; i < list.size(); i++) {
					location = (PHPIdentifierLocation) list.get(i);
					int type = location.getType();
					switch (fLastSignificantToken) {
					case ITerminalSymbols.TokenNameMINUS_GREATER:
						if (type != PHPIdentifierLocation.METHOD
								&& type != PHPIdentifierLocation.VARIABLE) {
							continue; // for loop
						}
						break;
					case ITerminalSymbols.TokenNameVariable:
						if (type != PHPIdentifierLocation.METHOD
								&& type != PHPIdentifierLocation.VARIABLE) {
							continue; // for loop
						}
						// check all filenames of the subclasses
						if (fFileName.equals(location.getFilename())) {
							continue; // for loop
						}
						break;
					case ITerminalSymbols.TokenNamethis_PHP_COMPLETION:
						if (type != PHPIdentifierLocation.METHOD
								&& type != PHPIdentifierLocation.VARIABLE) {
							continue; // for loop
						}
						// check all filenames of the subclasses
						if (!fFileName.equals(location.getFilename())) {
							continue; // for loop
						}
						break;
					case ITerminalSymbols.TokenNamenew:
						if (type != PHPIdentifierLocation.CLASS
								&& type != PHPIdentifierLocation.CONSTRUCTOR) {
							continue; // for loop
						}
						break;
					default:
						if (type == PHPIdentifierLocation.METHOD
								|| type == PHPIdentifierLocation.CONSTRUCTOR
								|| type == PHPIdentifierLocation.VARIABLE) {
							continue; // for loop
						}
					}
					if (maxProposals-- < 0) {
						return;
					}
					fProposals.add(new DeclarationProposal(fProject,
							identifier, location, context, region, viewer));
				}
			}
		}

	}

}
