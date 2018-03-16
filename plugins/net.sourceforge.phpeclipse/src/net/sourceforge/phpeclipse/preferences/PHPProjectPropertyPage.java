package net.sourceforge.phpeclipse.preferences;

import net.sourceforge.phpdt.internal.core.JavaProject;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

public class PHPProjectPropertyPage extends PropertyPage implements
		IWorkbenchPropertyPage {
	protected PHPProjectLibraryPage projectsPage;

	protected JavaProject workingProject;

	public PHPProjectPropertyPage() {
	}

	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();

		workingProject = getPHPProject();
		if (workingProject == null || !workingProject.getProject().isOpen())
			return createClosedProjectPageContents(parent);

		return createProjectPageContents(parent);
	}

	protected JavaProject getPHPProject() {
		IAdaptable selectedElement = getElement();
		if (selectedElement == null)
			return null;

		if (selectedElement instanceof JavaProject)
			return (JavaProject) selectedElement;

		if (selectedElement instanceof IProject) {
			IProject simpleProject = (IProject) selectedElement;
			try {
				if (simpleProject.hasNature(PHPeclipsePlugin.PHP_NATURE_ID)) {
					JavaProject phpProject = new JavaProject();
					phpProject.setProject(simpleProject);
					return phpProject;
				}
			} catch (CoreException e) {
				PHPeclipsePlugin.log(e);
			}
		}

		return null;
	}

	protected Control createClosedProjectPageContents(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(PHPPreferencesMessages
				.getString("PHPProjectPropertyPage.phpProjectClosed")); //$NON-NLS-1$

		return label;
	}

	protected Control createProjectPageContents(Composite parent) {
		TabFolder tabFolder = new TabFolder(parent, SWT.NONE);
		tabFolder.setLayout(new GridLayout());
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
		tabFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// tabChanged(e.item);
			}
		});

		projectsPage = new PHPProjectLibraryPage(workingProject);
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(PHPPreferencesMessages
				.getString("PHPProjectLibraryPage.tabName")); //$NON-NLS-1$
		// tabItem.setData(projectsPage);
		tabItem.setControl(projectsPage.getControl(tabFolder));

		return tabFolder;
	}

	public boolean performOk() {
		try {
			projectsPage.getWorkingProject().save();
		} catch (CoreException e) {
			PHPeclipsePlugin.log(e);
		}
		return super.performOk();
	}

}
