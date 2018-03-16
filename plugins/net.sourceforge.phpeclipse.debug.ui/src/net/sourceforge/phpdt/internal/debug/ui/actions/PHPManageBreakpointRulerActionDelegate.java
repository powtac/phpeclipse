/**********************************************************************
 Copyright (c) 2000, 2002 IBM Corp. and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html

 Contributors:
 IBM Corporation - Initial implementation
 Vicente Fernando - www.alfersoft.com.ar
 **********************************************************************/
package net.sourceforge.phpdt.internal.debug.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

public class PHPManageBreakpointRulerActionDelegate extends
		AbstractRulerActionDelegate {

	/**
	 * @see AbstractRulerActionDelegate#createAction()
	 */
	protected IAction createAction(ITextEditor editor,
			IVerticalRulerInfo rulerInfo) {
		return new PHPManageBreakpointRulerAction(rulerInfo, editor);
	}
}
