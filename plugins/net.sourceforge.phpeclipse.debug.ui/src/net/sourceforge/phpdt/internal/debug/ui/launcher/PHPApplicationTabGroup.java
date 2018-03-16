package net.sourceforge.phpdt.internal.debug.ui.launcher;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;

public class PHPApplicationTabGroup extends AbstractLaunchConfigurationTabGroup {

	public PHPApplicationTabGroup() {
		super();
	}

	/**
	 * @see ILaunchConfigurationTabGroup#createTabs(ILaunchConfigurationDialog,
	 *      String)
	 */
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
				new PHPEntryPointTab(), new PHPArgumentsTab(),
				new PHPEnvironmentTab(), new PHPEnvironmentTab2(),
				new CommonTab() };
		setTabs(tabs);
	}

}
