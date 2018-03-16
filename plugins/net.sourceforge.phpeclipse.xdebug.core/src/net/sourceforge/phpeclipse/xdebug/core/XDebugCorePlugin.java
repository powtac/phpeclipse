package net.sourceforge.phpeclipse.xdebug.core;


import net.sourceforge.phpeclipse.xdebug.php.launching.IXDebugConstants;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.framework.BundleContext;

public class XDebugCorePlugin extends Plugin {
	private static XDebugCorePlugin plugin;
	public static final String PLUGIN_ID = "net.sourceforge.phpeclipse.xdebug.core"; //$NON-NLS-1$
	
	private XDebugProxy fXDebugProxy;

	private ScopedPreferenceStore preferenceStore;

	
	/**
	 * The constructor.
	 */
	public XDebugCorePlugin() {
		super();
		plugin = this;
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
		if (fXDebugProxy != null)
			fXDebugProxy.stop();
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static XDebugCorePlugin getDefault() {
		return plugin;
	}
	
	public static IBreakpoint[] getBreakpoints() {
		return getBreakpointManager().getBreakpoints(IXDebugConstants.ID_PHP_DEBUG_MODEL);
	}
	
	public static IBreakpointManager getBreakpointManager() {
		return DebugPlugin.getDefault().getBreakpointManager();
	} 

	public static void log(int severity, String message) {
		Status status = new Status(severity, PLUGIN_ID, IStatus.OK, message, null) ;
		XDebugCorePlugin.log(status) ;
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, "PHPLaunchingPlugin.internalErrorOccurred", e)); //$NON-NLS-1$
	}
	
	public static String getUniqueIdentifier() {
		return PLUGIN_ID;
	}
	
	/*public void setProxyPort(int port) {
		if(fXDebugProxy!=null) {
			if (fXDebugProxy.isRunning()) {
				fXDebugProxy.stop();
			}
			fXDebugProxy=null;
		}
	}*/

	public XDebugProxy getXDebugProxy() {
		if (fXDebugProxy == null) {
			int debugPort=getPreferenceStore().getInt(IXDebugPreferenceConstants.DEBUGPORT_PREFERENCE);
			if (debugPort<1024)
				debugPort=IXDebugPreferenceConstants.DEFAULT_DEBUGPORT;
			fXDebugProxy= new XDebugProxy(debugPort);
		}
		return fXDebugProxy;
	}
	
    public IPreferenceStore getPreferenceStore() {
        // Create the preference store lazily.
        if (preferenceStore == null) {
            preferenceStore = new ScopedPreferenceStore(new InstanceScope(),getBundle().getSymbolicName());

        }
        return preferenceStore;
    }
}