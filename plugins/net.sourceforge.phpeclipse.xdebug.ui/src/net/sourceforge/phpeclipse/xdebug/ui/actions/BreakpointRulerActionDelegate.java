package net.sourceforge.phpeclipse.xdebug.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;



public class BreakpointRulerActionDelegate extends AbstractRulerActionDelegate {

	private ToggleBreakpointRulerAction fTargetAction;

	protected IAction createAction(ITextEditor editor,
			IVerticalRulerInfo rulerInfo) {
		fTargetAction = new ToggleBreakpointRulerAction( editor, rulerInfo );
		return fTargetAction;
	}

}
