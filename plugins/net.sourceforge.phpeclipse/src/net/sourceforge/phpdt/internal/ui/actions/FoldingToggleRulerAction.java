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
package net.sourceforge.phpdt.internal.ui.actions;

import net.sourceforge.phpeclipse.phpeditor.PHPEditor;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.editors.text.IFoldingCommandIds;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextOperationAction;

/**
 * Groups the JDT folding actions.
 * 
 * @since 3.0
 */
public class FoldingToggleRulerAction extends AbstractRulerActionDelegate {

	private IAction fUIAction;

	private TextOperationAction fAction;

	private ITextEditor fTextEditor;

	/*
	 * @see org.eclipse.ui.texteditor.AbstractRulerActionDelegate#createAction(org.eclipse.ui.texteditor.ITextEditor,
	 *      org.eclipse.jface.text.source.IVerticalRulerInfo)
	 */
	protected IAction createAction(ITextEditor editor,
			IVerticalRulerInfo rulerInfo) {
		fTextEditor = editor;
		fAction = new TextOperationAction(ActionMessages.getResourceBundle(),
				"Projection.Toggle.", editor, ProjectionViewer.TOGGLE, true); //$NON-NLS-1$
		fAction.setActionDefinitionId(IFoldingCommandIds.FOLDING_TOGGLE);

		return fAction;
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractRulerActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction,
	 *      org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor(IAction callerAction, IEditorPart targetEditor) {
		fUIAction = callerAction;
		super.setActiveEditor(callerAction, targetEditor);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractRulerActionDelegate#menuAboutToShow(org.eclipse.jface.action.IMenuManager)
	 */
	public void menuAboutToShow(IMenuManager manager) {
		update();
		super.menuAboutToShow(manager);
	}

	private void update() {
		if (fTextEditor instanceof PHPEditor) {
			ISourceViewer viewer = ((PHPEditor) fTextEditor).getViewer();
			if (viewer instanceof ProjectionViewer) {
				boolean enabled = ((ProjectionViewer) viewer)
						.getProjectionAnnotationModel() != null;
				fUIAction.setChecked(enabled);
			}
		}
	}
}
