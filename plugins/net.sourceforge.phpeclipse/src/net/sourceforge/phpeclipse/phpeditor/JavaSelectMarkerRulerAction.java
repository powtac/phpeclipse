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
package net.sourceforge.phpeclipse.phpeditor;

import java.util.Iterator;
import java.util.ResourceBundle;

import net.sourceforge.phpdt.core.ICompilationUnit;
import net.sourceforge.phpdt.core.IJavaElement;
import net.sourceforge.phpdt.core.JavaCore;
import net.sourceforge.phpdt.internal.ui.IJavaHelpContextIds;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorExtension;
import org.eclipse.ui.texteditor.SelectMarkerRulerAction;

/**
 * A special select marker ruler action which activates quick fix if clicked on
 * a quick fixable problem.
 */
public class JavaSelectMarkerRulerAction extends SelectMarkerRulerAction {

	private ITextEditor fTextEditor;

	private Position fPosition;

	public JavaSelectMarkerRulerAction(ResourceBundle bundle, String prefix,
			ITextEditor editor, IVerticalRulerInfo ruler) {
		super(bundle, prefix, editor, ruler);
		fTextEditor = editor;
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IJavaHelpContextIds.JAVA_SELECT_MARKER_RULER_ACTION);
	}

	public void run() {
		// if
		// (PHPeclipsePlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_ANNOTATION_ROLL_OVER))
		// return;

		if (fPosition != null) {
			ITextOperationTarget operation = (ITextOperationTarget) fTextEditor
					.getAdapter(ITextOperationTarget.class);
			// final int opCode= PHPUnitEditor.CORRECTIONASSIST_PROPOSALS;
			// if (operation != null && operation.canDoOperation(opCode)) {
			// fTextEditor.selectAndReveal(fPosition.getOffset(),
			// fPosition.getLength());
			// operation.doOperation(opCode);
			// return;
			// }
			return;
		}
		super.run();
	}

	public void update() {
		// Begin Fix for http://dev.eclipse.org/bugs/show_bug.cgi?id=20114
		if (!(fTextEditor instanceof ITextEditorExtension)
				|| ((ITextEditorExtension) fTextEditor).isEditorInputReadOnly()) {
			fPosition = null;
			super.update();
			return;
		}
		// End Fix for http://dev.eclipse.org/bugs/show_bug.cgi?id=20114
		fPosition = getJavaAnnotationPosition();
		if (fPosition != null)
			setEnabled(true);
		else
			super.update();
	}

	private Position getJavaAnnotationPosition() {
		AbstractMarkerAnnotationModel model = getAnnotationModel();
		IDocument document = getDocument();
		if (model == null)
			return null;
		ICompilationUnit cu = getCompilationUnit();
		if (cu == null) {
			return null;
		}

		// boolean hasAssistLightbulb=
		// PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.APPEARANCE_QUICKASSIST_LIGHTBULB);
		Annotation assistAnnotation = null;

		Iterator iter = model.getAnnotationIterator();
		while (iter.hasNext()) {
			Annotation annotation = (Annotation) iter.next();
			if (annotation instanceof IJavaAnnotation) {
				IJavaAnnotation javaAnnotation = (IJavaAnnotation) annotation;
				if (!javaAnnotation.isMarkedDeleted()) {
					Position position = model.getPosition(annotation);
					// if (includesRulerLine(position, document) &&
					// JavaCorrectionProcessor.hasCorrections(javaAnnotation))
					// return position;
				}
			}
			// else if (hasAssistLightbulb && annotation instanceof
			// AssistAnnotation) {
			// // there is only one AssistAnnotation at a time
			// assistAnnotation= annotation;
			// }
		}
		if (assistAnnotation != null) {
			Position position = model.getPosition(assistAnnotation);
			// no need to check 'JavaCorrectionProcessor.hasAssists': annotation
			// only created when
			// there are assists
			if (includesRulerLine(position, document))
				return position;
		}
		return null;
	}

	private ICompilationUnit getCompilationUnit() {
		IEditorInput input = fTextEditor.getEditorInput();
		if (input instanceof IFileEditorInput) {
			IFile file = ((IFileEditorInput) input).getFile();
			IJavaElement element = JavaCore.create(file);
			if (element instanceof ICompilationUnit)
				return (ICompilationUnit) element;
		}
		return null;
	}
}
