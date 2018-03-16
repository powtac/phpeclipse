package net.sourceforge.phpdt.externaltools.internal.program.launchConfigurations;

import net.sourceforge.phpdt.externaltools.internal.ui.FileSelectionDialog;
import net.sourceforge.phpdt.externaltools.launchConfigurations.ExternalToolsMainTab;
import net.sourceforge.phpdt.externaltools.model.IExternalToolConstants;
import net.sourceforge.phpdt.externaltools.model.ToolUtil;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;

public class ProgramMainTab extends ExternalToolsMainTab {

	/**
	 * Prompts the user for a program location within the workspace and sets the
	 * location as a String containing the workspace_loc variable or
	 * <code>null</code> if no location was obtained from the user.
	 */
	protected void handleWorkspaceLocationButtonSelected() {
		FileSelectionDialog dialog;
		dialog = new FileSelectionDialog(getShell(), ResourcesPlugin
				.getWorkspace().getRoot(), "&Select a program");
		dialog.open();
		IFile file = dialog.getResult();
		if (file == null) {
			return;
		}
		StringBuffer buf = new StringBuffer();
		ToolUtil.buildVariableTag(IExternalToolConstants.VAR_WORKSPACE_LOC,
				file.getFullPath().toString(), buf);
		String text = buf.toString();
		if (text != null) {
			locationField.setText(text);
		}
	}

}
