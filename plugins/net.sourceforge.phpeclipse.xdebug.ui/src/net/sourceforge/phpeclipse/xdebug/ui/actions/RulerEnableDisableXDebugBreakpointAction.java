/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     PHPEclipse team
 *     Mauro "Incastrix" Casciari
 *******************************************************************************/
package net.sourceforge.phpeclipse.xdebug.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import net.sourceforge.phpeclipse.xdebug.ui.XDebugUIPlugin;
import org.eclipse.debug.ui.actions.RulerBreakpointAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * @since 3.2
 *
 */
public class RulerEnableDisableXDebugBreakpointAction extends RulerBreakpointAction implements IUpdate {
	private static final String ENABLE_XDEBUG_BREAKPOINT_LABEL = "EnableXDebugBreakpoint.label"; //$NON-NLS-1$
	private static final String DISABLE_XDEBUG_BREAKPOINT_LABEL = "DisableXDebugBreakpoint.label"; //$NON-NLS-1$
	private static final String RULER_ENABLE_EDISABLE_BREAKPOINT_ACTION_0 = "RulerEnableDisableBreakpointAction_0"; //$NON-NLS-1$
	private static final String RULER_ENABLE_EDISABLE_BREAKPOINT_ACTION_1 = "RulerEnableDisableBreakpointAction_1"; //$NON-NLS-1$

	private IBreakpoint fBreakpoint;
	
	public RulerEnableDisableXDebugBreakpointAction(ITextEditor editor, IVerticalRulerInfo info) {
		super(editor, info);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		if (fBreakpoint != null) {
			try {
				fBreakpoint.setEnabled(!fBreakpoint.isEnabled());
			} catch (CoreException e) {
				XDebugUIPlugin.errorDialog(getEditor().getSite().getShell(), XDebugUIPlugin.getString(RULER_ENABLE_EDISABLE_BREAKPOINT_ACTION_0), XDebugUIPlugin.getString(RULER_ENABLE_EDISABLE_BREAKPOINT_ACTION_1), e.getStatus());
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		fBreakpoint = getBreakpoint();
		setEnabled(fBreakpoint != null);
		if (fBreakpoint != null) {
			try {
				if (fBreakpoint.isEnabled()) {
					setText(XDebugUIPlugin.getString(DISABLE_XDEBUG_BREAKPOINT_LABEL));
				} else {
					setText(XDebugUIPlugin.getString(ENABLE_XDEBUG_BREAKPOINT_LABEL));
				}
			} catch (CoreException e) {
			}
		} else {
			setText(XDebugUIPlugin.getString(ENABLE_XDEBUG_BREAKPOINT_LABEL));
		}
	}

}