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

import net.sourceforge.phpdt.internal.ui.PHPUiImages;
import net.sourceforge.phpdt.ui.IContextMenuConstants;
import net.sourceforge.phpdt.ui.actions.PHPdtActionConstants;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.editors.text.EncodingActionGroup;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;

public class BasicEditorActionContributor extends
		BasicJavaEditorActionContributor {

	protected RetargetAction fRetargetContentAssist;

	protected RetargetTextEditorAction fContentAssist;

	// protected RetargetTextEditorAction fContextInformation;
	// protected RetargetTextEditorAction fCorrectionAssist;
	private EncodingActionGroup fEncodingActionGroup;

	public BasicEditorActionContributor() {

		fRetargetContentAssist = new RetargetAction(
				PHPdtActionConstants.CONTENT_ASSIST, PHPEditorMessages
						.getString("ContentAssistProposal.label")); //$NON-NLS-1$
		fRetargetContentAssist
				.setActionDefinitionId(PHPEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		markAsPartListener(fRetargetContentAssist);

		fContentAssist = new RetargetTextEditorAction(PHPEditorMessages
				.getResourceBundle(), "ContentAssistProposal."); //$NON-NLS-1$
		fContentAssist
				.setActionDefinitionId(PHPEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		fContentAssist.setImageDescriptor(PHPUiImages.DESC_CLCL_CODE_ASSIST);
		fContentAssist
				.setDisabledImageDescriptor(PHPUiImages.DESC_DLCL_CODE_ASSIST);

		// fContextInformation= new
		// RetargetTextEditorAction(PHPEditorMessages.getResourceBundle(),
		// "ContentAssistContextInformation."); //$NON-NLS-1$
		// fContextInformation.setActionDefinitionId(PHPEditorActionDefinitionIds.CONTENT_ASSIST_CONTEXT_INFORMATION);

		// fCorrectionAssist= new
		// RetargetTextEditorAction(PHPEditorMessages.getResourceBundle(),
		// "CorrectionAssistProposal."); //$NON-NLS-1$
		// fCorrectionAssist.setActionDefinitionId(PHPEditorActionDefinitionIds.CORRECTION_ASSIST_PROPOSALS);

		// character encoding
		fEncodingActionGroup = new EncodingActionGroup();
	}

	/*
	 * @see EditorActionBarContributor#contributeToMenu(IMenuManager)
	 */
	public void contributeToMenu(IMenuManager menu) {

		super.contributeToMenu(menu);

		IMenuManager editMenu = menu
				.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
		if (editMenu != null) {
			editMenu.appendToGroup(IContextMenuConstants.GROUP_GENERATE,
					fRetargetContentAssist);
			// editMenu.appendToGroup(IContextMenuConstants.GROUP_GENERATE,
			// fCorrectionAssist);
			// editMenu.appendToGroup(IContextMenuConstants.GROUP_GENERATE,
			// fContextInformation);
		}
	}

	/*
	 * @see IEditorActionBarContributor#setActiveEditor(IEditorPart)
	 */
	public void setActiveEditor(IEditorPart part) {
		super.setActiveEditor(part);

		ITextEditor textEditor = null;
		if (part instanceof ITextEditor)
			textEditor = (ITextEditor) part;

		fContentAssist
				.setAction(getAction(textEditor, "ContentAssistProposal")); //$NON-NLS-1$
		// fContextInformation.setAction(getAction(textEditor,
		// "ContentAssistContextInformation")); //$NON-NLS-1$
		// fCorrectionAssist.setAction(getAction(textEditor,
		// "CorrectionAssistProposal")); //$NON-NLS-1$

		IActionBars actionBars = getActionBars();
		actionBars.setGlobalActionHandler(PHPdtActionConstants.SHIFT_RIGHT,
				getAction(textEditor, "ShiftRight")); //$NON-NLS-1$
		actionBars.setGlobalActionHandler(PHPdtActionConstants.SHIFT_LEFT,
				getAction(textEditor, "ShiftLeft")); //$NON-NLS-1$

		actionBars.setGlobalActionHandler(IDEActionFactory.ADD_TASK.getId(),
				getAction(textEditor, IDEActionFactory.ADD_TASK.getId())); //$NON-NLS-1$
		actionBars.setGlobalActionHandler(IDEActionFactory.BOOKMARK.getId(),
				getAction(textEditor, IDEActionFactory.BOOKMARK.getId())); //$NON-NLS-1$

		// character encoding
		fEncodingActionGroup.retarget(textEditor);
	}

	/*
	 * @see IEditorActionBarContributor#init(IActionBars, IWorkbenchPage)
	 */
	public void init(IActionBars bars, IWorkbenchPage page) {
		super.init(bars, page);

		// register actions that have a dynamic editor.
		bars.setGlobalActionHandler(PHPdtActionConstants.CONTENT_ASSIST,
				fContentAssist);
		// character encoding
		fEncodingActionGroup.fillActionBars(bars);
	}
}
