package net.sourceforge.phpdt.internal.debug.ui;

import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class PHPDebugUiPlugin extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "net.sourceforge.phpeclipse.debug.ui"; //$NON-NLS-1$

	protected static PHPDebugUiPlugin plugin;

	public PHPDebugUiPlugin() {
		super();
		plugin = this;
	}

	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	public static IWorkbenchPage getActivePage() {
		return PHPDebugUiPlugin.getActiveWorkbenchWindow().getActivePage();
	}

	public static PHPDebugUiPlugin getDefault() {
		return plugin;
	}

	public static IWorkspace getWorkspace() {
		return PHPeclipsePlugin.getWorkspace();
	}

	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 */
	// public static String getUniqueIdentifier()
	// {
	// if ( getDefault() == null )
	// {
	// // If the default instance is not yet initialized,
	// // return a static identifier. This identifier must
	// // match the plugin id defined in plugin.xml
	// return PLUGIN_ID;
	// }
	// return getDefault().getDescriptor().getUniqueIdentifier();
	// }
	/**
	 * Returns the standard display to be used. The method first checks, if the
	 * thread calling this method has an associated display. If so, this display
	 * is returned. Otherwise the method returns the default display.
	 */
	public static Display getStandardDisplay() {
		Display display;
		display = Display.getCurrent();
		if (display == null)
			display = Display.getDefault();
		return display;
	}

	/**
	 * Returns the active workbench shell or <code>null</code> if none
	 * 
	 * @return the active workbench shell or <code>null</code> if none
	 */
	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		if (window != null) {
			return window.getShell();
		}
		return null;
	}

	public static void errorDialog(String message, IStatus status) {
		log(status);
		Shell shell = getActiveWorkbenchShell();
		if (shell != null) {
			ErrorDialog.openError(shell, "Error", message, status);
		}
	}

	public static void errorDialog(String message, Throwable t) {
		log(t);
		Shell shell = getActiveWorkbenchShell();
		if (shell != null) {
			IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, /* ICDebugUIConstants.INTERNAL_ERROR */
					150, t.getMessage(), null); //$NON-NLS-1$	
			ErrorDialog.openError(shell, "Error", message, status);
		}
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR,
				PHPDebugUiMessages
						.getString("RdtDebugUiPlugin.internalErrorOccurred"), e)); //$NON-NLS-1$
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

	// protected void initializeDefaultPreferences(IPreferenceStore store) {
	// super.initializeDefaultPreferences(store);
	//		
	// store.setDefault(RdtDebugUiConstants.PREFERENCE_KEYWORDS,
	// getDefaultKeywords());
	// }

	// protected String getDefaultKeywords() {
	// return "class,def,end,if,module,new,puts,require,rescue,throw,while";
	// }
}
