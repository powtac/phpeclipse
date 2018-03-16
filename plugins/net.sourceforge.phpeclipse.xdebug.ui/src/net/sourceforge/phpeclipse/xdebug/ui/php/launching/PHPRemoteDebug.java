package net.sourceforge.phpeclipse.xdebug.ui.php.launching;

import net.sourceforge.phpdt.internal.ui.PHPUiImages;
import net.sourceforge.phpdt.internal.ui.util.PHPProjectSelector;
import net.sourceforge.phpeclipse.xdebug.php.launching.IXDebugConstants;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

public class PHPRemoteDebug extends AbstractLaunchConfigurationTab {

	private PHPProjectSelector projectSelector;
	private Text fIdeIDText;

	public void createControl(Composite parent) {
		Font font = parent.getFont();
		
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		GridLayout topLayout = new GridLayout();
		topLayout.verticalSpacing = 0;
		comp.setLayout(topLayout);
		comp.setFont(font);
		
		createProjectEditor(comp);
		createVerticalSpacer(comp, 1);
		createIdeIDEditor(comp);
		
	}
	
	/**
	 * Creates the widgets for specifying a main type.
	 * 
	 * @param parent the parent composite
	 */
	private void createProjectEditor(Composite parent) {
		Font font= parent.getFont();
		Group group= new Group(parent, SWT.NONE);
		group.setText("Project:");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		group.setLayout(layout);
		group.setFont(font);

		projectSelector = new PHPProjectSelector(group);
		projectSelector.setBrowseDialogMessage("Choose the project containing the application entry point:");
		projectSelector.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		projectSelector.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		
		gd= new GridData(GridData.FILL_HORIZONTAL);
	}
	
	private void createIdeIDEditor(Composite parent) {
		Font font= parent.getFont();
		Group group= new Group(parent, SWT.NONE);
		group.setText("Ide Identification String :"); 
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		group.setLayout(layout);
		group.setFont(font);
		
				
		fIdeIDText = new Text(group, SWT.SINGLE | SWT.BORDER);
		gd= new GridData(GridData.FILL_HORIZONTAL);
		fIdeIDText.setLayoutData(gd);
		fIdeIDText.setFont(font);
		fIdeIDText.setTextLimit(48);
		fIdeIDText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});

	}
	

	  
	protected IProject getContext() {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		if (page != null) {
			ISelection selection = page.getSelection();
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss = (IStructuredSelection) selection;
				if (!ss.isEmpty()) {
					Object obj = ss.getFirstElement();
					if (obj instanceof IResource)
						return ((IResource) obj).getProject();
				}
			}
			IEditorPart part = page.getActiveEditor();
			if (part != null) {
				IEditorInput input = part.getEditorInput();
				IResource file = (IResource) input.getAdapter(IResource.class);
				if (file != null) {
					return file.getProject();
				}
			}
		}
		return null;
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		IProject project = getContext();
		if (project != null)
			configuration.setAttribute(IXDebugConstants.ATTR_PHP_PROJECT, project.getName());
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			String project = configuration.getAttribute(IXDebugConstants.ATTR_PHP_PROJECT, (String)null);
			if (project != null) {
				projectSelector.setSelectionText(project);
			}
			String ideID = configuration.getAttribute(IXDebugConstants.ATTR_PHP_IDE_ID, "testID");
			fIdeIDText.setText(ideID);
		} catch (CoreException e) {
			setErrorMessage(e.getMessage());
		}



	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		String project = projectSelector.getSelectionText().trim();
		configuration.setAttribute(IXDebugConstants.ATTR_PHP_PROJECT, project);
		String ideID = fIdeIDText.getText().trim();
		configuration.setAttribute(IXDebugConstants.ATTR_PHP_IDE_ID, ideID);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		String projectName=projectSelector.getSelectionText().trim();
		IProject project=ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (!project.exists()) {
			setErrorMessage("Project does not exist");
			return false;
		}
		String ideID=fIdeIDText.getText();
		if (ideID.indexOf(' ')>0) { 
			setErrorMessage("No spaces in Identification String allowed");
			return false;
		}
		return true;
	}
	
	public Image getImage() {
		return PHPUiImages.get(PHPUiImages.IMG_CTOOLS_PHP_PAGE);
	}
	
	public String getName() {
		return "Main";
	}


}
