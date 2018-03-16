package net.sourceforge.phpdt.internal.debug.ui.launcher;

import net.sourceforge.phpdt.internal.debug.ui.PHPDebugUiMessages;
import net.sourceforge.phpdt.internal.debug.ui.PHPDebugUiPlugin;
import net.sourceforge.phpdt.internal.launching.PHPLaunchConfigurationAttribute;
import net.sourceforge.phpdt.internal.ui.PHPUiImages;
import net.sourceforge.phpdt.internal.ui.util.PHPFileSelector;
import net.sourceforge.phpdt.internal.ui.util.PHPProjectSelector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;

public class PHPEntryPointTab extends AbstractLaunchConfigurationTab {
	protected String originalFileName, originalProjectName;

	protected PHPProjectSelector projectSelector;

	protected PHPFileSelector fileSelector;

	public PHPEntryPointTab() {
		super();
	}

	public void createControl(Composite parent) {
		Composite composite = createPageRoot(parent);

//		new Label(composite, SWT.NONE)
//				.setText(PHPDebugUiMessages
//						.getString("LaunchConfigurationTab.PHPEntryPoint.projectLabel"));
//		projectSelector = new PHPProjectSelector(composite);
		Group grpProject = new Group(composite, SWT.NONE);
		grpProject
				.setText(PHPDebugUiMessages
						.getString("LaunchConfigurationTab.PHPEntryPoint.projectLabel"));
		grpProject.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpProject.setLayout(new GridLayout());
		projectSelector = new PHPProjectSelector(grpProject);
		projectSelector
				.setBrowseDialogMessage(PHPDebugUiMessages
						.getString("LaunchConfigurationTab.PHPEntryPoint.projectSelectorMessage"));
		projectSelector.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		projectSelector.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});

//		new Label(composite, SWT.NONE).setText(PHPDebugUiMessages
//				.getString("LaunchConfigurationTab.PHPEntryPoint.fileLabel"));
//		fileSelector = new PHPFileSelector(composite, projectSelector);
		Group grpFile = new Group(composite, SWT.NONE);
		grpFile.setText(PHPDebugUiMessages
				.getString("LaunchConfigurationTab.PHPEntryPoint.fileLabel"));
		grpFile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpFile.setLayout(new GridLayout());
		fileSelector = new PHPFileSelector(grpFile, projectSelector);
		fileSelector
				.setBrowseDialogMessage(PHPDebugUiMessages
						.getString("LaunchConfigurationTab.PHPEntryPoint.fileSelectorMessage"));
		fileSelector.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fileSelector.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
	}

	protected IResource getContext() {
		IWorkbenchPage page = PHPDebugUiPlugin.getActivePage();
		if (page != null) {
			ISelection selection = page.getSelection();
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss = (IStructuredSelection) selection;
				if (!ss.isEmpty()) {
					Object obj = ss.getFirstElement();
					if (obj instanceof IResource)
						return ((IResource) obj);
				}
			}
			IEditorPart part = page.getActiveEditor();
			if (part != null) {
				IEditorInput input = part.getEditorInput();
				IResource file = (IResource) input.getAdapter(IResource.class);
				if (file != null) {
					return file;
				}
			}
		}
		return null;
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		IResource file = getContext();
		if (file != null) {
			configuration.setAttribute(
					PHPLaunchConfigurationAttribute.PROJECT_NAME, file
							.getProject().getName());
			configuration.setAttribute(
					PHPLaunchConfigurationAttribute.FILE_NAME, file
							.getProjectRelativePath().toOSString());
		}
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			originalProjectName = configuration.getAttribute(
					PHPLaunchConfigurationAttribute.PROJECT_NAME, "");
			originalFileName = configuration.getAttribute(
					PHPLaunchConfigurationAttribute.FILE_NAME, "");
		} catch (CoreException e) {
			log(e);
		}

		projectSelector.setSelectionText(originalProjectName);
		if (!"".equals(originalFileName))
			fileSelector.setSelectionText(new Path(originalFileName)
					.toOSString());
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(
				PHPLaunchConfigurationAttribute.PROJECT_NAME, projectSelector
						.getSelectionText());
		IFile file = fileSelector.getSelection();
		configuration.setAttribute(PHPLaunchConfigurationAttribute.FILE_NAME,
				file == null ? "" : file.getProjectRelativePath().toString());
	}

	protected Composite createPageRoot(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
//		layout.marginWidth = 0;
		composite.setLayout(layout);

		setControl(composite);
		return composite;
	}

	public String getName() {
		return PHPDebugUiMessages
				.getString("LaunchConfigurationTab.PHPEntryPoint.name");
	}

	public boolean isValid(ILaunchConfiguration launchConfig) {
		try {

			String projectName = launchConfig.getAttribute(
					PHPLaunchConfigurationAttribute.PROJECT_NAME, "");
			if (projectName.length() == 0) {
				setErrorMessage(PHPDebugUiMessages
						.getString("LaunchConfigurationTab.PHPEntryPoint.invalidProjectSelectionMessage"));
				return false;
			}

			String fileName = launchConfig.getAttribute(
					PHPLaunchConfigurationAttribute.FILE_NAME, "");
			if (fileName.length() == 0) {
				setErrorMessage(PHPDebugUiMessages
						.getString("LaunchConfigurationTab.PHPEntryPoint.invalidFileSelectionMessage"));
				return false;
			}
		} catch (CoreException e) {
			log(e);
		}

		setErrorMessage(null);
		return true;
	}

	protected void log(Throwable t) {
		PHPDebugUiPlugin.log(t);
	}

	public boolean canSave() {
		return getErrorMessage() == null;
	}

	public Image getImage() {
		return PHPUiImages.get(PHPUiImages.IMG_CTOOLS_PHP_PAGE);
	}

}