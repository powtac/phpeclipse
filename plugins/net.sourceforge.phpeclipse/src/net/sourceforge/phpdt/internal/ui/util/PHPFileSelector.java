package net.sourceforge.phpdt.internal.ui.util;

import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class PHPFileSelector extends ResourceSelector {
	static class FileLabelProvider extends LabelProvider {
		/**
		 * Returns the implementation of IWorkbenchAdapter for the given object.
		 * 
		 * @param o
		 *            the object to look up.
		 * @return IWorkbenchAdapter or <code>null</code> if the adapter is
		 *         not defined or the object is not adaptable.
		 */
		protected final IWorkbenchAdapter getAdapter(Object o) {
			if (!(o instanceof IAdaptable)) {
				return null;
			}
			return (IWorkbenchAdapter) ((IAdaptable) o)
					.getAdapter(IWorkbenchAdapter.class);
		}

		/*
		 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object element) {
			if (element instanceof IFile) {
				// query the element for its label
				IWorkbenchAdapter adapter = getAdapter(element);
				if (adapter == null) {
					return ""; //$NON-NLS-1$
				}
				String filename = adapter.getLabel(element);
				IPath path = ((IFile) element).getFullPath();
				String filePathname = path != null ? path.toString() : ""; //$NON-NLS-1$
				return filename + " (" + filePathname + ")";
			}
			return super.getText(element);
		}
	}

	protected PHPProjectSelector phpProjectSelector;

	public PHPFileSelector(Composite parent, PHPProjectSelector aProjectSelector) {
		super(parent);
		Assert.isNotNull(aProjectSelector);
		phpProjectSelector = aProjectSelector;

		browseDialogTitle = "File Selection";
	}

	protected Object[] getPHPFiles() {
		IProject phpProject = phpProjectSelector.getSelection();
		if (phpProject == null)
			return new Object[0];

		PHPElementVisitor visitor = new PHPElementVisitor();
		try {
			phpProject.accept(visitor);
		} catch (CoreException e) {
			PHPeclipsePlugin.log(e);
		}
		return visitor.getCollectedPHPFiles();
	}

	public IFile getSelection() {
		String fileName = getSelectionText();
		if (fileName != null && !fileName.equals("")) {
			IPath filePath = new Path(fileName);
			IProject project = phpProjectSelector.getSelection();
			if (project != null && project.exists(filePath))
				return project.getFile(filePath);
		}

		return null;
	}

	protected void handleBrowseSelected() {
		// ElementListSelectionDialog dialog = new
		// ElementListSelectionDialog(getShell(), new WorkbenchLabelProvider());
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(
				getShell(), new FileLabelProvider());

		dialog.setTitle(browseDialogTitle);
		dialog.setMessage(browseDialogMessage);
		dialog.setElements(getPHPFiles());

		if (dialog.open() == ElementListSelectionDialog.OK) {
			textField.setText(((IResource) dialog.getFirstResult())
					.getProjectRelativePath().toString());
		}
	}

	protected String validateResourceSelection() {
		IFile selection = getSelection();
		return selection == null ? EMPTY_STRING : selection
				.getProjectRelativePath().toString();
	}
}