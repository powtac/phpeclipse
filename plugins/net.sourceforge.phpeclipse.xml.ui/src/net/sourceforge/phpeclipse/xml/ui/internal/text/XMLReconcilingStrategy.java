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
 * $Id: XMLReconcilingStrategy.java,v 1.3 2006-10-21 23:14:13 pombredanne Exp $
 */
package net.sourceforge.phpeclipse.xml.ui.internal.text;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import net.sourceforge.phpeclipse.ui.text.IReconcilingParticipant;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcileResult;
import org.eclipse.jface.text.reconciler.IReconcileStep;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Reconciling strategy for XML document. This class is responsible for keeping
 * the parsed model in sync with the text.
 */
public class XMLReconcilingStrategy implements IReconcilingStrategy,
		IReconcilingStrategyExtension {

	// Instance Variables ------------------------------------------------------

	/**
	 * The associated text editor.
	 */
	private ITextEditor editor;

	/**
	 * A progress monitor that should be used for long-running operations.
	 */
	IProgressMonitor progressMonitor;

	/**
	 * The first (and only) reconcile step is the parsing of the style sheet.
	 */
	private IReconcileStep firstStep;

	// Constructors ------------------------------------------------------------

	public XMLReconcilingStrategy(ITextEditor editor) {
		this.editor = editor;
		firstStep = new XMLReconcileStep(editor);
	}

	// IReconcilingStrategy Implementation -------------------------------------

	/**
	 * @see IReconcilingStrategy#reconcile(DirtyRegion, IRegion)
	 */
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		removeTemporaryAnnotations();
		process(firstStep.reconcile(dirtyRegion, subRegion));
	}

	/**
	 * @see IReconcilingStrategy#reconcile(IRegion)
	 */
	public void reconcile(IRegion partition) {
		removeTemporaryAnnotations();
		process(firstStep.reconcile(partition));
	}

	/**
	 * @see IReconcilingStrategy#setDocument(IDocument)
	 */
	public void setDocument(IDocument document) {
		// FIXME
		firstStep.setInputModel(null); // new DocumentAdapter(document);
	}

	// IReconcilingStrategyExtension Implementation ----------------------------

	/**
	 * @see IReconcilingStrategyExtension#initialReconcile()
	 */
	public void initialReconcile() {
		process(firstStep.reconcile(null));
	}

	/**
	 * @see IReconcilingStrategyExtension#setProgressMonitor(IProgressMonitor)
	 */
	public void setProgressMonitor(IProgressMonitor monitor) {
		firstStep.setProgressMonitor(monitor);
		progressMonitor = monitor;
	}

	// Private Methods ---------------------------------------------------------

	/**
	 * Returns the annotation model for the editor input.
	 * 
	 * @return the annotation model
	 */
	IAnnotationModel getAnnotationModel() {
		IEditorInput input = editor.getEditorInput();
		return editor.getDocumentProvider().getAnnotationModel(input);
	}

	/**
	 * Adds results of the reconcilation to the annotation model.
	 */
	private void process(final IReconcileResult[] results) {
		if (results == null) {
			return;
		}

		IRunnableWithProgress runnable = new WorkspaceModifyOperation() {
			protected void execute(IProgressMonitor monitor) {
				for (int i = 0; i < results.length; i++) {
					if ((progressMonitor != null)
							&& (progressMonitor.isCanceled())) {
						return;
					}

					if (results[i] instanceof AnnotationAdapter) {
						AnnotationAdapter result = (AnnotationAdapter) results[i];
						Position pos = result.getPosition();
						Annotation annotation = result.createAnnotation();
						getAnnotationModel().addAnnotation(annotation, pos);
					}
				}
			}
		};

		try {
			runnable.run(null);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (editor instanceof IReconcilingParticipant) {
			((IReconcilingParticipant) editor).reconciled();
		}
	}

	/*
	 * TODO A "real" implementation must be smarter, i.e. don't remove and add
	 * the annotations which are the same.
	 */
	private void removeTemporaryAnnotations() {
		Iterator i = getAnnotationModel().getAnnotationIterator();
		while (i.hasNext()) {
			Annotation annotation = (Annotation) i.next();
			if (!annotation.isPersistent()) {
				getAnnotationModel().removeAnnotation(annotation);
			}
		}
	}
}
