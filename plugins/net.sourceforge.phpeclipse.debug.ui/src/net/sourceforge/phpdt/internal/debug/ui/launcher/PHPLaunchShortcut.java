package net.sourceforge.phpdt.internal.debug.ui.launcher;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.phpdt.debug.ui.PHPDebugUiConstants;
import net.sourceforge.phpdt.internal.debug.ui.PHPDebugUiMessages;
import net.sourceforge.phpdt.internal.debug.ui.PHPDebugUiPlugin;
import net.sourceforge.phpdt.internal.launching.PHPLaunchConfigurationAttribute;
import net.sourceforge.phpdt.internal.ui.util.PHPFileUtil;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

public class PHPLaunchShortcut implements ILaunchShortcut {
	public PHPLaunchShortcut() {
	}

	public void launch(ISelection selection, String mode) {
		if (selection instanceof IStructuredSelection) {
			Object firstSelection = ((IStructuredSelection) selection)
					.getFirstElement();
			if (firstSelection instanceof IFile) {
				if (PHPFileUtil.isPHPFile((IFile) firstSelection)) {
					ILaunchConfiguration config = findLaunchConfiguration(
							(IFile) firstSelection, mode);
					try {
						if (config != null)
							config.launch(mode, null);
					} catch (CoreException e) {
						log(e);
					}
					return;
				}
			}
		}

		log("The resource selected is not a PHP file.");
	}

	public void launch(IEditorPart editor, String mode) {
		IEditorInput input = editor.getEditorInput();
		ISelection selection = new StructuredSelection(input
				.getAdapter(IFile.class));
		launch(selection, mode);
	}

	protected ILaunchConfiguration findLaunchConfiguration(IFile phpFile,
			String mode) {
		ILaunchConfigurationType configType = getPHPLaunchConfigType();
		List candidateConfigs = null;
		try {
			ILaunchConfiguration[] configs = getLaunchManager()
					.getLaunchConfigurations(configType);
			candidateConfigs = new ArrayList(configs.length);
			for (int i = 0; i < configs.length; i++) {
				ILaunchConfiguration config = configs[i];
				if (config.getAttribute(
						PHPLaunchConfigurationAttribute.FILE_NAME, "").equals(
						phpFile.getFullPath().toString())) {
					candidateConfigs.add(config);
				}
			}
		} catch (CoreException e) {
			log(e);
		}

		switch (candidateConfigs.size()) {
		case 0:
			return createConfiguration(phpFile);
		case 1:
			return (ILaunchConfiguration) candidateConfigs.get(0);
		default:
			log(new RuntimeException(
					PHPDebugUiMessages
							.getString("LaunchConfigurationShortcut.PHP.multipleConfigurationsError")));
			return null;
		}
	}

	protected ILaunchConfiguration createConfiguration(IFile phpFile) {
		ILaunchConfiguration config = null;
		try {
			ILaunchConfigurationType configType = getPHPLaunchConfigType();
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(null,
					getLaunchManager()
							.generateUniqueLaunchConfigurationNameFrom(
									phpFile.getName()));
			wc.setAttribute(PHPLaunchConfigurationAttribute.PROJECT_NAME,
					phpFile.getProject().getName());
			wc.setAttribute(PHPLaunchConfigurationAttribute.FILE_NAME, phpFile
					.getProjectRelativePath().toString());
			wc.setAttribute(PHPLaunchConfigurationAttribute.WORKING_DIRECTORY,
					PHPDebugUiConstants.DEFAULT_WORKING_DIRECTORY);
			config = wc.doSave();
		} catch (CoreException ce) {
			log(ce);
		}
		return config;
	}

	protected ILaunchConfigurationType getPHPLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(
				PHPLaunchConfigurationAttribute.PHP_LAUNCH_CONFIGURATION_TYPE);
	}

	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	protected void log(String message) {
		PHPDebugUiPlugin.log(new Status(Status.INFO,
				PHPDebugUiPlugin.PLUGIN_ID, Status.INFO, message, null));
	}

	protected void log(Throwable t) {
		PHPDebugUiPlugin.log(t);
	}
}
