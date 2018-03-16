package net.sourceforge.phpdt.internal.ui.util;

import net.sourceforge.phpdt.core.JavaCore;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class PHPProjectSelector extends ResourceSelector {

	public PHPProjectSelector(Composite parent) {
		super(parent);

		browseDialogTitle = "Project Selection";
	}

	public IProject getSelection() {
		String projectName = getSelectionText();
		if (projectName != null && !projectName.equals(""))
			return PHPeclipsePlugin.getWorkspace().getRoot().getProject(
					projectName);

		return null;
	}

	protected void handleBrowseSelected() {
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(
				getShell(), new WorkbenchLabelProvider());
		dialog.setTitle(browseDialogTitle);
		dialog.setMessage(browseDialogMessage);
		dialog.setElements(JavaCore.getPHPProjects());

		if (dialog.open() == ElementListSelectionDialog.OK) {
			textField.setText(((IProject) dialog.getFirstResult()).getName());
		}
	}

	protected String validateResourceSelection() {
		IProject project = getSelection();
		return project == null ? EMPTY_STRING : project.getName();
	}
}