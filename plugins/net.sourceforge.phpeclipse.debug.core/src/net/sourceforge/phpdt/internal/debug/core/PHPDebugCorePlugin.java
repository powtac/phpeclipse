package net.sourceforge.phpdt.internal.debug.core;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class PHPDebugCorePlugin extends AbstractUIPlugin {
	// The shared instance.
	protected static PHPDebugCorePlugin plugin;

	public static final String PLUGIN_ID = "net.sourceforge.phpeclipse.debug.core"; //$NON-NLS-1$

	public static IWorkbenchPage getActivePage() {
		return getDefault().internalGetActivePage();
	}

	public static Shell getActiveWorkbenchShell() {
		return getActiveWorkbenchWindow().getShell();
	}

	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	/**
	 * Returns the shared instance.
	 */
	public static PHPDebugCorePlugin getDefault() {
		return plugin;
	}

	public static String getFormattedMessage(String key, String arg) {
		String text = getResourceString(key);
		return java.text.MessageFormat.format(text, new Object[] { arg });
	}

	public static String getResourceString(String key) {
		ResourceBundle bundle = plugin.getResourceBundle();
		if (bundle != null) {
			try {
				String bundleString = bundle.getString(key);
				// return "$"+bundleString;
				return bundleString;
			} catch (MissingResourceException e) {
				// default actions is to return key, which is OK
			}
		}
		return key;
	}

	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 */
	public static String getUniqueIdentifier() {
		return PLUGIN_ID;
	}

	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return PHPeclipsePlugin.getWorkspace();
	}

	public static void log(int severity, String message) {
		Status status = new Status(severity, PLUGIN_ID, IStatus.OK, message,
				null);
		PHPDebugCorePlugin.log(status);
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR,
				"PHPLaunchingPlugin.internalErrorOccurred", e)); //$NON-NLS-1$
	}

	private ResourceBundle resourceBundle;

	/**
	 * The constructor.
	 */
	public PHPDebugCorePlugin() {
		super();
		plugin = this;
		try {
			resourceBundle = ResourceBundle
					.getBundle("net.sourceforge.phpdt.internal.debug.core.debugresources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	public java.util.ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	private IWorkbenchPage internalGetActivePage() {
		return getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}

	/**
	 * @see Plugin#shutdown()
	 */
	/*
	 * public void shutdown() throws CoreException { plugin = null;
	 * super.shutdown(); }
	 */
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
		plugin = null;
		super.stop(context);
	}
}
