package net.sourceforge.phpeclipse.xdebug.ui.php.launching;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;

public class PHPTabGroup extends AbstractLaunchConfigurationTabGroup {


	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		setTabs(new ILaunchConfigurationTab[] {
				new PHPMainTab(),
//				new XDebugTab(),
				new SourceLookupTab(),
				new PHPEnvironmentTab(),
				new CommonTab()
		});
	}
}
