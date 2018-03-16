package net.sourceforge.phpeclipse.wizards;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import net.sourceforge.phpdt.core.JavaCore;
import net.sourceforge.phpdt.ui.actions.OpenPHPPerspectiveAction;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

public class NewProjectCreationWizard extends BasicNewResourceWizard implements
		INewWizard, IExecutableExtension {
	protected WizardNewProjectCreationPage projectPage;

	protected IConfigurationElement configurationElement;

	protected IProject newProject;

	public NewProjectCreationWizard() {
		setWindowTitle(PHPWizardMessages
				.getString("NewProjectCreationWizard.windowTitle"));
	}

	public boolean performFinish() {
		IRunnableWithProgress projectCreationOperation = new WorkspaceModifyDelegatingOperation(
				getProjectCreationRunnable());

		try {
			getContainer().run(false, true, projectCreationOperation);
		} catch (Exception e) {
			PHPeclipsePlugin.log(e);
			return false;
		}

		BasicNewProjectResourceWizard.updatePerspective(configurationElement);
		selectAndReveal(newProject);
		// open the PHP perspective
		new OpenPHPPerspectiveAction().run();
		return true;
	}

	protected IRunnableWithProgress getProjectCreationRunnable() {
		return new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
				int remainingWorkUnits = 10;
				monitor
						.beginTask(
								PHPWizardMessages
										.getString("NewProjectCreationWizard.projectCreationMessage"),
								remainingWorkUnits);

				IWorkspace workspace = PHPeclipsePlugin.getWorkspace();
				String projectName = projectPage.getProjectHandle().getName();
				newProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				IProjectDescription description = workspace
						.newProjectDescription(projectName);
				
				URI uriPath = (!projectPage.useDefaults()) ? projectPage
		                .getLocationURI() : null;
				if (uriPath != null) {				    
					description.setLocationURI(uriPath);
				}

				try {
					if (!newProject.exists()) {
						newProject.create(description, new SubProgressMonitor(
								monitor, 1));
						remainingWorkUnits--;
					}
					if (!newProject.isOpen()) {
					    newProject.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(monitor, 1));
						remainingWorkUnits--;
					}
					JavaCore.addPHPNature(newProject, new SubProgressMonitor(
							monitor, remainingWorkUnits));

				} catch (CoreException e) {
				    System.out.println(e);
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
	}

	public void addPages() {
		super.addPages();

		projectPage = new WizardNewProjectCreationPage(PHPWizardMessages
				.getString("WizardNewProjectCreationPage.pageName"));
		projectPage.setTitle(PHPWizardMessages
				.getString("WizardNewProjectCreationPage.pageTitle"));
		projectPage.setDescription(PHPWizardMessages
				.getString("WizardNewProjectCreationPage.pageDescription"));

		addPage(projectPage);
	}

	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		configurationElement = config;
	}

}