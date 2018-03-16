package net.sourceforge.phpdt.internal.debug.ui.actions;

import net.sourceforge.phpdt.internal.debug.core.breakpoints.PHPLineBreakpoint;
import net.sourceforge.phpdt.internal.debug.ui.properties.PHPBreakpointPropertiesDialog;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Enables the context menu entry if object is of type PHPLineBreakpoint. This
 * is used for Breakpoint properties menu. Properties menu let you set 'skip
 * count' and condition of a PHP breakpoint.
 * 
 */

public class PHPDebugBreakpointAction implements IViewActionDelegate {
	protected PHPLineBreakpoint fBreakpoint = null;

	public void init(IViewPart view) {

	}

	public void run(IAction action) {
		PHPBreakpointPropertiesDialog dialog = new PHPBreakpointPropertiesDialog(
				null, fBreakpoint);
		dialog.open();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		IStructuredSelection bpSelection;
		Object bpObject;

		if (selection instanceof IStructuredSelection) {
			bpSelection = (IStructuredSelection) selection;

			if (bpSelection.size() == 1) { // Do we have something selected
				bpObject = bpSelection.getFirstElement(); // Get the selected
															// object

				if (bpObject instanceof PHPLineBreakpoint) { // Is the object
																// of type
																// PHPLineBreakpoint?
					fBreakpoint = (PHPLineBreakpoint) bpObject;
					action.setEnabled(true); // Then enable the context menu
												// item
					return;
				}
			}
		}

		action.setEnabled(false); // It isn't a PHPLineBreakpoint, so disable
									// the menu item
	}
}
