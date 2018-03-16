/*
 * Copyright (c) 2003-2004 Christopher Lenz and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Christopher Lenz - initial API and implementation
 * 
 * $Id: XMLReconcileStep.java,v 1.2 2006-10-21 23:14:13 pombredanne Exp $
 */

package net.sourceforge.phpeclipse.xml.ui.internal.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.phpeclipse.xml.core.model.IXMLDocument;
import net.sourceforge.phpeclipse.xml.core.parser.IProblem;
import net.sourceforge.phpeclipse.xml.core.parser.IProblemCollector;
import net.sourceforge.phpeclipse.xml.ui.internal.editor.XMLDocumentProvider;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.AbstractReconcileStep;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilableModel;
import org.eclipse.jface.text.reconciler.IReconcileResult;
import org.eclipse.jface.text.reconciler.IReconcileStep;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Implementation of a reconcile step for building the XML parse tree on changes
 * to the editor content.
 */
public class XMLReconcileStep extends AbstractReconcileStep {

	// Inner Classes -----------------------------------------------------------

	/**
	 * Adapts an <code>IXMLDocument</code> to the
	 * <code>IReconcilableModel</code> interface.
	 */
	private class XMLDocumentAdapter implements IReconcilableModel {
		private IXMLDocument document;

		public XMLDocumentAdapter(IXMLDocument document) {
			this.document = document;
		}

		public IXMLDocument getDocument() {
			return document;
		}

	}

	/**
	 * Implementation of the problem collector interface for creating problem
	 * annotations when there are problems parsing the style sheet.
	 */
	private class ProblemCollector implements IProblemCollector {
		/**
		 * The list of problems added to this collector.
		 */
		private List collectedProblems = new ArrayList();

		/**
		 * @see IProblemCollector#addProblem(IProblem)
		 */
		public void addProblem(IProblem problem) {
			collectedProblems.add(problem);
		}

		/**
		 * Returns the list of problems collected while the CSS source has been
		 * parsed, in the order they were reported. The list returned is
		 * immutable.
		 * 
		 * @return the list of collected problems (of type {@link IProblem})
		 */
		public List getProblems() {
			return Collections.unmodifiableList(collectedProblems);
		}
	}

	/**
	 * Adapter that adapts an {@link IProblem} to an {@link Annotation}.
	 */
	private class ProblemAdapter extends AnnotationAdapter {
		private IProblem problem;

		private Position position;

		public ProblemAdapter(IProblem problem) {
			this.problem = problem;
		}

		public Position getPosition() {
			if (position == null) {
				position = createPositionFromProblem();
			}

			return position;
		}

		public Annotation createAnnotation() {
			int start = problem.getSourceStart();
			if (start < 0) {
				return null;
			}

			int length = problem.getSourceEnd() - start + 1;
			if (length < 0) {
				return null;
			}

			String type;
			if (problem.isWarning()) {
				type = XMLAnnotation.TYPE_ERROR;
			} else if (problem.isError()) {
				type = XMLAnnotation.TYPE_WARNING;
			} else {
				type = XMLAnnotation.TYPE_INFO;
			}

			return new XMLAnnotation(type, false, problem.getMessage());
		}

		private Position createPositionFromProblem() {
			int start = problem.getSourceStart();
			if (start < 0) {
				return null;
			}

			int length = problem.getSourceEnd() - problem.getSourceStart() + 1;
			if (length < 0) {
				return null;
			}

			return new Position(start, length);
		}

	}

	// Instance Variables ------------------------------------------------------

	private ITextEditor editor;

	private XMLDocumentAdapter xmlDocumentAdapter;

	// Constructors ------------------------------------------------------------

	/**
	 * Default constructor.
	 */
	public XMLReconcileStep(ITextEditor editor) {
		this.editor = editor;

		xmlDocumentAdapter = new XMLDocumentAdapter(getXMLDocument());
	}

	/**
	 * Constructor.
	 * 
	 * @param step
	 *            the step to add to the pipe
	 * @param editor
	 *            the associated text editor
	 */
	public XMLReconcileStep(IReconcileStep step, ITextEditor editor) {
		super(step);

		this.editor = editor;

		xmlDocumentAdapter = new XMLDocumentAdapter(getXMLDocument());
	}

	// AbstractReconcileStep Implementation ------------------------------------

	/*
	 * @see AbstractReconcileStep#reconcileModel(DirtyRegion, IRegion)
	 */
	protected IReconcileResult[] reconcileModel(DirtyRegion dirtyRegion,
			IRegion subRegion) {
		IXMLDocument model = xmlDocumentAdapter.getDocument();

		IEditorInput editorInput = null;
		IFile file = null;
		if (editor != null) {
			editorInput = editor.getEditorInput();
		}

		if (editorInput instanceof IFileEditorInput)
			file = ((IFileEditorInput) editorInput).getFile();
		ProblemCollector problemCollector = new ProblemCollector();
		model.reconcile(problemCollector, file);

		List problems = problemCollector.getProblems();
		IReconcileResult[] retVal = new IReconcileResult[problems.size()];
		for (int i = 0; i < problems.size(); i++) {
			IProblem problem = (IProblem) problems.get(i);
			retVal[i] = new ProblemAdapter(problem);
		}

		return retVal;
	}

	/*
	 * @see AbstractReconcileStep#getModel()
	 */
	public IReconcilableModel getModel() {
		return xmlDocumentAdapter;
	}

	// Private Methods Implementation ------------------------------------------

	/**
	 * Retrieve the style sheet associated with the editor input.
	 */
	private IXMLDocument getXMLDocument() {
		IDocumentProvider documentProvider = editor.getDocumentProvider();
		if (documentProvider instanceof XMLDocumentProvider) {
			XMLDocumentProvider xmlDocumentProvider = (XMLDocumentProvider) documentProvider;
			return xmlDocumentProvider.getModel(editor.getEditorInput());
		}

		return null;
	}
}
