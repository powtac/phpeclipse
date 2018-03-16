package net.sourceforge.phpeclipse.wizards;

import java.lang.reflect.InvocationTargetException;

import net.sourceforge.phpdt.core.JavaCore;
import net.sourceforge.phpdt.internal.ui.util.ExceptionHandler;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

public class TempnewPHPProject extends BasicNewResourceWizard implements
		INewWizard {
	/*
	 * This class has been added to cvs to provide a project page that works
	 * correctly and doesn't freezde while i investigate the errors completely
	 */
	private WizardNewProjectCreationPage phpProjPage;

	private IConfigurationElement fConfigElement;

	public TempnewPHPProject() {
		setNeedsProgressMonitor(true);
		setWindowTitle("New Project creation"); //$NON-NLS-1$

	}

	public void addPages() {
		super.addPages();
		phpProjPage = new WizardNewProjectCreationPage(
				"NewProjectCreationWizard"); //$NON-NLS-1$
		phpProjPage.setTitle(PHPWizardMessages
				.getString("WizardNewProjectCreationPage.pageTitle")); //$NON-NLS-1$
		phpProjPage.setDescription(PHPWizardMessages
				.getString("WizardNewProjectCreationPage.pageDescription")); //$NON-NLS-1$
		addPage(phpProjPage);
	}

	public void setInitializationData(IConfigurationElement cfig,
			String propertyName, Object data) {
		fConfigElement = cfig;
	}

	protected void initializeDefaultPageImageDescriptor() {
		// not used yet
	}

	protected void finishPage() throws InterruptedException, CoreException {
		createProject(phpProjPage.getProjectHandle(), phpProjPage
				.getLocationPath(), new NullProgressMonitor());
		BasicNewProjectResourceWizard.updatePerspective(fConfigElement);
		selectAndReveal(phpProjPage.getProjectHandle());
	}

	protected void handleFinishException(Shell shell,
			InvocationTargetException e) {
		ExceptionHandler.handle(e, getShell(), "Error title", "Error message");
	}

	public boolean performFinish() {
		try {
			finishPage();
		} catch (InterruptedException e) {
		} catch (CoreException e) {
		}
		return true;
	}

	public void createProject(IProject project, IPath locationPath,
			IProgressMonitor monitor) throws CoreException {
		try {
			if (!project.exists()) {
				IProjectDescription desc = project.getWorkspace()
						.newProjectDescription(project.getName());
				if (Platform.getLocation().equals(locationPath)) {
					locationPath = null;
				}
				desc.setLocation(locationPath);
				project.create(desc, monitor);
				monitor = null;
			}
			if (!project.isOpen()) {
				project.open(monitor);
				monitor = null;
			}
			JavaCore.addPHPNature(project, new NullProgressMonitor());
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}
}