/***********************************************************************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **********************************************************************************************************************************/

package net.sourceforge.phpdt.internal.ui.text.java;

import net.sourceforge.phpdt.core.ICompilationUnit;
import net.sourceforge.phpdt.core.JavaModelException;
import net.sourceforge.phpdt.ui.IWorkingCopyManager;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class JavaReconcilingStrategy implements IReconcilingStrategy,
		IReconcilingStrategyExtension {

	private ITextEditor fEditor;

	private IWorkingCopyManager fManager;

	private IDocumentProvider fDocumentProvider;

	private IProgressMonitor fProgressMonitor;

	private boolean fNotify = true;

	private IJavaReconcilingListener fJavaReconcilingListener;

	private boolean fIsJavaReconcilingListener;

	public JavaReconcilingStrategy(ITextEditor editor) {
		fEditor = editor;
		fManager = PHPeclipsePlugin.getDefault().getWorkingCopyManager();
		fDocumentProvider = PHPeclipsePlugin.getDefault()
				.getCompilationUnitDocumentProvider();
		fIsJavaReconcilingListener = fEditor instanceof IJavaReconcilingListener;
		if (fIsJavaReconcilingListener)
			fJavaReconcilingListener = (IJavaReconcilingListener) fEditor;
	}

	private IProblemRequestorExtension getProblemRequestorExtension() {
		IAnnotationModel model = fDocumentProvider.getAnnotationModel(fEditor
				.getEditorInput());
		if (model instanceof IProblemRequestorExtension)
			return (IProblemRequestorExtension) model;
		return null;
	}

	private void reconcile() {
		// // try {
		//
		// /* fix for missing cancel flag communication */
		// IProblemRequestorExtension extension =
		// getProblemRequestorExtension();
		// if (extension != null)
		// extension.setProgressMonitor(fProgressMonitor);
		//
		// // reconcile
		// // synchronized (unit) {
		// // unit.reconcile(true, fProgressMonitor);
		// // }
		//
		// Parser parser = new Parser();
		// parser.initializeScanner();
		// // actualParser.setFileToParse(fileToParse);
		// String text =
		// fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput()).get();
		// parser.init(text);
		// parser.reportSyntaxError();
		// // checkAndReportBracketAnomalies(parser.problemReporter());
		//
		// /* fix for missing cancel flag communication */
		// if (extension != null)
		// extension.setProgressMonitor(null);
		//
		// // update participants
		// try {
		// if (fEditor instanceof IReconcilingParticipant && fNotify &&
		// !fProgressMonitor.isCanceled()) {
		// IReconcilingParticipant p = (IReconcilingParticipant) fEditor;
		// p.reconciled();
		// }
		// } finally {
		// fNotify = true;
		// }

		// JDT implementation:
		try {
			ICompilationUnit unit = fManager.getWorkingCopy(fEditor
					.getEditorInput());
			if (unit != null) {
				try {

					/* fix for missing cancel flag communication */
					IProblemRequestorExtension extension = getProblemRequestorExtension();
					if (extension != null)
						extension.setProgressMonitor(fProgressMonitor);

					// reconcile
					synchronized (unit) {
						unit.reconcile(true, fProgressMonitor);
					}

					/* fix for missing cancel flag communication */
					if (extension != null)
						extension.setProgressMonitor(null);

					// update participants
					try {
						if (fEditor instanceof IReconcilingParticipant
								&& fNotify && !fProgressMonitor.isCanceled()) {
							IReconcilingParticipant p = (IReconcilingParticipant) fEditor;
							p.reconciled();
						}
					} finally {
						fNotify = true;
					}

				} catch (JavaModelException x) {
					// swallow exception
				}
			}
		} finally {
			// Always notify listeners, see
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=55969 for the final
			// solution
			try {
				if (fIsJavaReconcilingListener) {
					IProgressMonitor pm = fProgressMonitor;
					if (pm == null)
						pm = new NullProgressMonitor();
					fJavaReconcilingListener.reconciled(null, !fNotify, pm);
				}
			} finally {
				fNotify = true;
			}

		}
	}

	/*
	 * @see IReconcilingStrategy#reconcile(IRegion)
	 */
	public void reconcile(IRegion partition) {
		reconcile();
	}

	/*
	 * @see IReconcilingStrategy#reconcile(DirtyRegion, IRegion)
	 */
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		reconcile();
	}

	/*
	 * @see IReconcilingStrategy#setDocument(IDocument)
	 */
	public void setDocument(IDocument document) {
	}

	/*
	 * @see IReconcilingStrategyExtension#setProgressMonitor(IProgressMonitor)
	 */
	public void setProgressMonitor(IProgressMonitor monitor) {
		fProgressMonitor = monitor;
	}

	/*
	 * @see IReconcilingStrategyExtension#initialReconcile()
	 */
	public void initialReconcile() {
		reconcile();
	}

	/**
	 * Tells this strategy whether to inform its participants.
	 * 
	 * @param notify
	 *            <code>true</code> if participant should be notified
	 */
	public void notifyParticipants(boolean notify) {
		fNotify = notify;
	}

	/**
	 * Tells this strategy whether to inform its listeners.
	 * 
	 * @param notify
	 *            <code>true</code> if listeners should be notified
	 */
	public void notifyListeners(boolean notify) {
		fNotify = notify;
	}

	/**
	 * Called before reconciling is started.
	 * 
	 * @since 3.0
	 */
	public void aboutToBeReconciled() {
		if (fIsJavaReconcilingListener)
			fJavaReconcilingListener.aboutToBeReconciled();
	}
}