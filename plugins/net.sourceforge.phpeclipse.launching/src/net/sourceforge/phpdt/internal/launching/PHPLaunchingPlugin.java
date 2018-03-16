package net.sourceforge.phpdt.internal.launching;

import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class PHPLaunchingPlugin extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "net.sourceforge.phpeclipse.launching"; //$NON-NLS-1$

	protected static PHPLaunchingPlugin plugin;

	public PHPLaunchingPlugin() {
		super();
		plugin = this;
	}

	public static PHPLaunchingPlugin getDefault() {
		return plugin;
	}

	public static IWorkspace getWorkspace() {
		return PHPeclipsePlugin.getWorkspace();
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void log(Throwable e) {
		log(new Status(
				IStatus.ERROR,
				PLUGIN_ID,
				IStatus.ERROR,
				PHPLaunchingMessages
						.getString("PHPLaunchingPlugin.internalErrorOccurred"), e)); //$NON-NLS-1$
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}
}
