package net.sourceforge.phpeclipse.xdebug.ui.php.launching;

import net.sourceforge.phpeclipse.xdebug.php.launching.IXDebugConstants;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class XDebugTab extends AbstractLaunchConfigurationTab {

	private Label fPortLabel;
	private Button fUseDefaultPortButton;
	private Text fPortText;

	public XDebugTab() {
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
		createDebugPortEditor(comp);

	}
	
	private void createDebugPortEditor(Composite parent) {
		Font font= parent.getFont();
		Group debugGroup= new Group(parent, SWT.NONE);
		debugGroup.setText("Debug: "); 
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		debugGroup.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		debugGroup.setLayout(layout);
		debugGroup.setFont(font);
		
		fPortLabel = new Label(debugGroup, SWT.NONE);
		fPortLabel.setText("&DebugPort:");
//		gd = new GridData(GridData.BEGINNING);
//		fPortLabel.setLayoutData(gd);
		fPortLabel.setFont(font);

				
		fPortText = new Text(debugGroup, SWT.SINGLE | SWT.BORDER);
		gd= new GridData(GridData.FILL_HORIZONTAL);
		fPortText.setLayoutData(gd);
		fPortText.setFont(font);
		fPortText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});

		
		
		fUseDefaultPortButton = new Button(debugGroup,SWT.CHECK);
		fUseDefaultPortButton.setText("Use default interpreter");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fUseDefaultPortButton.setLayoutData(gd);
		fUseDefaultPortButton.setFont(font);
		fUseDefaultPortButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				handleDefaultSellected(event);
			}
		});
	}
	
	/**
	 * Set the appropriate enabled state for the appletviewqer text widget.
	 */
	protected void setDebugportEnabledState() {
		if (isDefaultInterpreter()) {
			fPortText.setEnabled(false);
			fPortLabel.setEnabled(false);
		} else {
			fPortText.setEnabled(true);
			fPortLabel.setEnabled(true);
		}
	}
	
	/**
	 * Returns whether the default appletviewer is to be used
	 */
	protected boolean isDefaultInterpreter() {
		return fUseDefaultPortButton.getSelection();
	}

	
	protected void handleDefaultSellected(SelectionEvent event) {
		setDebugportEnabledState();
		updateLaunchConfigurationDialog();
	}


	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// TODO Auto-generated method stub

	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			String portText=""+configuration.getAttribute(IXDebugConstants.ATTR_PHP_DEBUGPORT, 9000);
			fPortText.setText(portText);
			boolean selection=configuration.getAttribute(IXDebugConstants.ATTR_PHP_DEFAULT_DEBUGPORT, true);
			fUseDefaultPortButton.setSelection(selection);
			setDebugportEnabledState();
		} catch (CoreException e) {
			setErrorMessage(e.getMessage());
		}


	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IXDebugConstants.ATTR_PHP_DEFAULT_DEBUGPORT, this.fUseDefaultPortButton.getSelection());
		try {
			configuration.setAttribute(IXDebugConstants.ATTR_PHP_DEBUGPORT, Integer.parseInt(this.fPortText.getText().trim()));
		} catch (NumberFormatException nfe) {
		}

	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		try {
			Integer.parseInt(fPortText.getText().trim());
		} catch(NumberFormatException nfe) {
			setErrorMessage("Debugport is not a valid integer"); 
			return false;
		}
		return true;
	}

	public String getName() {
		return "XDebug";
	}

}
