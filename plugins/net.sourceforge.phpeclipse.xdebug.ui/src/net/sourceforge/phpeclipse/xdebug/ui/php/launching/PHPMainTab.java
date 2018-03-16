package net.sourceforge.phpeclipse.xdebug.ui.php.launching;

import java.io.File;
//import java.text.MessageFormat;

import net.sourceforge.phpdt.internal.ui.PHPUiImages;
import net.sourceforge.phpdt.internal.ui.util.PHPFileSelector;
import net.sourceforge.phpdt.internal.ui.util.PHPProjectSelector;
//import net.sourceforge.phpeclipse.xdebug.core.IXDebugPreferenceConstants;
//import net.sourceforge.phpeclipse.xdebug.core.XDebugCorePlugin;
import net.sourceforge.phpeclipse.xdebug.php.launching.IXDebugConstants;

import org.eclipse.core.resources.IFile;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

public class PHPMainTab extends AbstractLaunchConfigurationTab {

	// Project UI widgets
	protected Text fProjText;
	protected Button fProjButton;

	// Main class UI widgets
	protected Text fMainText;
	protected Button fSearchButton;
	protected PHPProjectSelector projectSelector;
	protected PHPFileSelector fileSelector;
	private Button fUseDefaultInterpreterButton;
	private Button fInterpreterButton;
	private Text fInterpreterText;

	public PHPMainTab() {
		super();
	}

	public void createControl(Composite parent) {
		Font font = parent.getFont();
		
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IJavaDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_MAIN_TAB);
		GridLayout topLayout = new GridLayout();
		topLayout.verticalSpacing = 0;
		comp.setLayout(topLayout);
		comp.setFont(font);
		
		createProjectEditor(comp);
		createVerticalSpacer(comp, 1);
		createMainTypeEditor(comp);
		createVerticalSpacer(comp, 1);
		createInterpreterEditor(comp);
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
		layout.numColumns = 2;
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
	}	

	
	/**
	 * Creates the widgets for specifying a php file.
	 * 
	 * @param parent the parent composite
	 */
	private void createMainTypeEditor(Composite parent) {
		Font font= parent.getFont();
		Group mainGroup= new Group(parent, SWT.NONE);
		mainGroup.setText("File: "); 
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		mainGroup.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		mainGroup.setLayout(layout);
		mainGroup.setFont(font);

		fileSelector = new PHPFileSelector(mainGroup, projectSelector);
		fileSelector.setBrowseDialogMessage("Choose the PHP file that represents the application entry point:");
		fileSelector.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fileSelector.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
	}
	
	/**
	 * Creates the widgets for specifying debug settings.
	 * 
	 * @param parent the parent composite
	 */
	private void createInterpreterEditor(Composite parent) {
		Font font= parent.getFont();
		Group interpreterGroup= new Group(parent, SWT.NONE);
		interpreterGroup.setText("Interpreter: "); 
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		interpreterGroup.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		interpreterGroup.setLayout(layout);
		interpreterGroup.setFont(font);
				
		fInterpreterText= new Text(interpreterGroup, SWT.SINGLE | SWT.BORDER);
		gd= new GridData(GridData.FILL_HORIZONTAL);
		fInterpreterText.setLayoutData(gd);
		fInterpreterText.setFont(font);
		fInterpreterText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});

		
		fInterpreterButton= createPushButton(interpreterGroup,"Browse..", null);
		fInterpreterButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				handleBrowseSellected(event);
			}
		});
		
		fUseDefaultInterpreterButton = new Button(interpreterGroup,SWT.CHECK);
		fUseDefaultInterpreterButton.setText("Use default interpreter");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fUseDefaultInterpreterButton.setLayoutData(gd);
		fUseDefaultInterpreterButton.setFont(font);
		fUseDefaultInterpreterButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				handleDefaultSellected(event);
			}
		});

	}
	
	/**
	 * Set the appropriate enabled state for the appletviewqer text widget.
	 */
	protected void setInterpreterTextEnabledState() {
		if (isDefaultInterpreter()) {
			fInterpreterText.setEnabled(false);
			fInterpreterButton.setEnabled(false);
		} else {
			fInterpreterText.setEnabled(true);
			fInterpreterButton.setEnabled(true);
		}
	}
	
	/**
	 * Returns whether the default appletviewer is to be used
	 */
	protected boolean isDefaultInterpreter() {
		return fUseDefaultInterpreterButton.getSelection();
	}



	protected void handleDefaultSellected(SelectionEvent event) {
		setInterpreterTextEnabledState();
		updateLaunchConfigurationDialog();
//		if (isDefaultInterpreter()) {
//			fInterpreterText.setText("default Interpreter");
//		}

	}

	protected void handleBrowseSellected(SelectionEvent event) {
		FileDialog dlg=new FileDialog(getShell(),SWT.OPEN);
		String fileName=dlg.open();
		if (fileName!=null) {
			fInterpreterText.setText(fileName);
			updateLaunchConfigurationDialog();
		}
	}

	protected IProject getContext() {
		IWorkbenchPage page= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
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
			String file = configuration.getAttribute(IXDebugConstants.ATTR_PHP_FILE, (String)null);
			if (file != null) {
				fileSelector.setSelectionText(file);
			}
			
			String interpreterFile=configuration.getAttribute(IXDebugConstants.ATTR_PHP_INTERPRETER, (String) null);
			if(interpreterFile!=null)
				fInterpreterText.setText(interpreterFile);
			boolean selection=configuration.getAttribute(IXDebugConstants.ATTR_PHP_DEFAULT_INTERPRETER, true);
			fUseDefaultInterpreterButton.setSelection(selection);
			setInterpreterTextEnabledState();

		} catch (CoreException e) {
			setErrorMessage(e.getMessage());
		}
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		String project = projectSelector.getSelectionText().trim();
		if (project.length() == 0) {
			project = null;
		}
		configuration.setAttribute(IXDebugConstants.ATTR_PHP_PROJECT, project);

		IFile file = fileSelector.getSelection();
		configuration.setAttribute(IXDebugConstants.ATTR_PHP_FILE, file == null ? "" : file.getProjectRelativePath()
				.toString());
		configuration.setAttribute(IXDebugConstants.ATTR_PHP_DEFAULT_INTERPRETER, this.fUseDefaultInterpreterButton.getSelection());
		configuration.setAttribute(IXDebugConstants.ATTR_PHP_INTERPRETER, this.fInterpreterText.getText());

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
		String fileString=fileSelector.getSelectionText().trim();
		if (!"".equals(fileString)) {
			IFile file=project.getFile(fileSelector.getSelectionText().trim());
			if (!file.exists()) {
				setErrorMessage("File does not exist");
				return false;
			}
		} else {
			setErrorMessage("File does not exist");
			return false;
		}
		if (!fUseDefaultInterpreterButton.getSelection()) {		
			File exe = new File(fInterpreterText.getText());
			System.out.println(exe.toString());
			if (!exe.exists()) {
				setErrorMessage("Invalid Interpreter");
				return false;
			}
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
