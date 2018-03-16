/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.phpdt.internal.core;

import net.sourceforge.phpdt.core.IJavaElement;
import net.sourceforge.phpdt.core.IJavaModelStatus;
import net.sourceforge.phpdt.core.IJavaModelStatusConstants;
import net.sourceforge.phpdt.core.IProblemRequestor;
import net.sourceforge.phpdt.core.JavaModelException;
import net.sourceforge.phpdt.core.WorkingCopyOwner;
import net.sourceforge.phpdt.internal.compiler.ast.CompilationUnitDeclaration;
import net.sourceforge.phpdt.internal.core.util.Util;

/**
 * Reconcile a working copy and signal the changes through a delta.
 */
public class ReconcileWorkingCopyOperation extends JavaModelOperation {

	boolean createAST;

	int astLevel;

	boolean forceProblemDetection;

	WorkingCopyOwner workingCopyOwner;

	// net.sourceforge.phpdt.core.dom.CompilationUnit ast;

	public ReconcileWorkingCopyOperation(IJavaElement workingCopy,
			boolean forceProblemDetection) {
		super(new IJavaElement[] { workingCopy });
		this.forceProblemDetection = forceProblemDetection;
	}

	public ReconcileWorkingCopyOperation(IJavaElement workingCopy,
			boolean creatAST, int astLevel, boolean forceProblemDetection,
			WorkingCopyOwner workingCopyOwner) {
		super(new IJavaElement[] { workingCopy });
		this.createAST = creatAST;
		this.astLevel = astLevel;
		this.forceProblemDetection = forceProblemDetection;
		this.workingCopyOwner = workingCopyOwner;
	}

	/**
	 * @exception JavaModelException
	 *                if setting the source of the original compilation unit
	 *                fails
	 */
	// protected void executeOperation() throws JavaModelException {
	// if (fMonitor != null){
	// if (fMonitor.isCanceled()) return;
	// fMonitor.beginTask(ProjectPrefUtil.bind("element.reconciling"), 10);
	// //$NON-NLS-1$
	// }
	//	
	// WorkingCopy workingCopy = getWorkingCopy();
	// boolean wasConsistent = workingCopy.isConsistent();
	// JavaElementDeltaBuilder deltaBuilder = null;
	//	
	// try {
	// // create the delta builder (this remembers the current content of the
	// cu)
	// if (!wasConsistent){
	// deltaBuilder = new JavaElementDeltaBuilder(workingCopy);
	//				
	// // update the element infos with the content of the working copy
	// workingCopy.makeConsistent(fMonitor);
	// deltaBuilder.buildDeltas();
	//		
	// }
	//	
	// if (fMonitor != null) fMonitor.worked(2);
	//			
	// // force problem detection? - if structure was consistent
	// if (forceProblemDetection && wasConsistent){
	// if (fMonitor != null && fMonitor.isCanceled()) return;
	//		
	// IProblemRequestor problemRequestor = workingCopy.problemRequestor;
	// if (problemRequestor != null && problemRequestor.isActive()){
	// problemRequestor.beginReporting();
	// CompilationUnitProblemFinder.process(workingCopy, problemRequestor,
	// fMonitor);
	// problemRequestor.endReporting();
	// }
	// }
	//			
	// // register the deltas
	// if (deltaBuilder != null){
	// if ((deltaBuilder.delta != null) &&
	// (deltaBuilder.delta.getAffectedChildren().length > 0)) {
	// addReconcileDelta(workingCopy, deltaBuilder.delta);
	// }
	// }
	// } finally {
	// if (fMonitor != null) fMonitor.done();
	// }
	// }
	protected void executeOperation() throws JavaModelException {
		// TODO jsurfer optimize for PHP
		if (progressMonitor != null) {
			if (progressMonitor.isCanceled())
				return;
			progressMonitor.beginTask(Util.bind("element.reconciling"), 10); //$NON-NLS-1$
		}

		CompilationUnit workingCopy = getWorkingCopy();
		boolean wasConsistent = workingCopy.isConsistent();
		JavaElementDeltaBuilder deltaBuilder = null;

		try {
			// create the delta builder (this remembers the current content of
			// the cu)
			if (!wasConsistent) {
				deltaBuilder = new JavaElementDeltaBuilder(workingCopy);

				// update the element infos with the content of the working copy
				workingCopy.makeConsistent(progressMonitor);
				deltaBuilder.buildDeltas();
			}

			if (progressMonitor != null)
				progressMonitor.worked(2);

			// force problem detection? - if structure was consistent
			if (forceProblemDetection) {
				if (progressMonitor != null && progressMonitor.isCanceled())
					return;
				CompilationUnitDeclaration unit = null;
				try {
					IProblemRequestor problemRequestor = workingCopy
							.getPerWorkingCopyInfo();
					if (problemRequestor != null && problemRequestor.isActive()) {
						problemRequestor.beginReporting();
						char[] contents = workingCopy.getContents();
						unit = CompilationUnitProblemFinder.process(
								workingCopy, contents, this.workingCopyOwner,
								problemRequestor, false/* don't cleanup cu */,
								this.progressMonitor);
						CompilationUnitProblemFinder.process(workingCopy,
								problemRequestor, progressMonitor);
						problemRequestor.endReporting();
					}
					if (progressMonitor != null)
						progressMonitor.worked(1);
					if (this.createAST && unit != null) {
						// Map options =
						// workingCopy.getJavaProject().getOptions(true);
						// this.ast = AST.convertCompilationUnit(this.astLevel,
						// unit, contents, options, this.progressMonitor);
						if (progressMonitor != null)
							progressMonitor.worked(1);
					}
				} finally {
					if (unit != null) {
						unit.cleanUp();
					}
				}
			}

			// register the deltas
			if (deltaBuilder != null) {
				if ((deltaBuilder.delta != null)
						&& (deltaBuilder.delta.getAffectedChildren().length > 0)) {
					addReconcileDelta(workingCopy, deltaBuilder.delta);
				}
			}
		} finally {
			if (progressMonitor != null)
				progressMonitor.done();
		}
	}

	/**
	 * Returns the working copy this operation is working on.
	 */
	protected CompilationUnit getWorkingCopy() {
		return (CompilationUnit) getElementToProcess();
	}

	/**
	 * @see JavaModelOperation#isReadOnly
	 */
	public boolean isReadOnly() {
		return true;
	}

	protected IJavaModelStatus verify() {
		IJavaModelStatus status = super.verify();
		if (!status.isOK()) {
			return status;
		}
		CompilationUnit workingCopy = getWorkingCopy();
		if (!workingCopy.isWorkingCopy()) {
			return new JavaModelStatus(
					IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST,
					workingCopy); // was destroyed
		}
		return status;
	}

}
