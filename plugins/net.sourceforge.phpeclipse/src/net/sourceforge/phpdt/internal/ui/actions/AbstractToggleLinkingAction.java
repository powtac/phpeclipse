/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.phpdt.internal.ui.actions;

import net.sourceforge.phpdt.internal.ui.IJavaHelpContextIds;
import net.sourceforge.phpdt.internal.ui.PHPUiImages;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

/**
 * This is an action template for actions that toggle whether it links its
 * selection to the active editor.
 * 
 * @since 3.0
 */
public abstract class AbstractToggleLinkingAction extends Action {

	/**
	 * Constructs a new action.
	 */
	public AbstractToggleLinkingAction() {
		super(ActionMessages.getString("ToggleLinkingAction.label")); //$NON-NLS-1$
		setDescription(ActionMessages
				.getString("ToggleLinkingAction.description")); //$NON-NLS-1$
		setToolTipText(ActionMessages.getString("ToggleLinkingAction.tooltip")); //$NON-NLS-1$
		PHPUiImages.setLocalImageDescriptors(this, "synced.gif"); //$NON-NLS-1$
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IJavaHelpContextIds.LINK_EDITOR_ACTION);
	}

	/**
	 * Runs the action.
	 */
	public abstract void run();
}
