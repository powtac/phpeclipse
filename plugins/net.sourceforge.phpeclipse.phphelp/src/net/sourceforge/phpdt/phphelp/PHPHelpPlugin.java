/**********************************************************************
 Copyright (c) 2000, 2002 IBM Corp. and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html

 Contributors:
 IBM Corporation - Initial implementation
 www.phpeclipse.de
 **********************************************************************/
package net.sourceforge.phpdt.phphelp;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.phpdt.httpquery.config.ConfigurationManager;
import net.sourceforge.phpdt.httpquery.config.IConfigurationWorkingCopy;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class PHPHelpPlugin extends AbstractUIPlugin {
	public static final String PHP_CHM_ENABLED = "_php_chm_enabled";

	public static final String PHP_CHM_FILE = "_php_chm_file";

	public static final String PHP_CHM_COMMAND = "_php_chm_command";

	public static final String HTTP_QUERY = "HTTP Query";

	public final static String PREF_STRING_CONFIGURATIONS = "__configurations1";

	public final static String CONFIG_MEMENTO = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<configurations>"
			+ "<config name=\"PHP Manual\" type-id=\"HTTP Query\" url=\"http://www.php.net/manual/en/function.$php.selection.php\"/>"
			+ "<config name=\"Google.com\" type-id=\"HTTP Query\" url=\"http://www.google.com/search?q=$text.selection\"/>"
			+ "<config name=\"Koders.com\" type-id=\"HTTP Query\" url=\"http://koders.com/?s=$text.selection\"/>"
			+ "<config name=\"Leo.org Deutsch/English\" type-id=\"HTTP Query\" url=\"http://dict.leo.org/?search=$text.selection\"/>"
			+ "<config name=\"Localhost\" type-id=\"HTTP Query\" url=\"http://localhost\"/>"
			+ "</configurations>";

	public static final ArrayList CONFIGURATION_TYPES = new ArrayList();

	/**
	 * The id of the PHP plugin (value
	 * <code>"net.sourceforge.phpeclipse.phphelp"</code>).
	 */
	public static final String PLUGIN_ID = "net.sourceforge.phpeclipse.phphelp"; //$NON-NLS-1$

	// The shared instance.
	private static PHPHelpPlugin plugin;

	private static ConfigurationManager manager;

	/**
	 * The constructor.
	 */
	public PHPHelpPlugin() {
		super();
		plugin = this;
	}

	/**
	 * Returns the shared instance.
	 */
	public static PHPHelpPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	public static IWorkbenchPage getActivePage() {
		return getDefault().internalGetActivePage();
	}

	private IWorkbenchPage internalGetActivePage() {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (window != null)
			return window.getActivePage();
		return null;
	}

	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}

	public static Shell getActiveWorkbenchShell() {
		return getActiveWorkbenchWindow().getShell();
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void log(int severity, String message) {
		Status status = new Status(severity, PLUGIN_ID, IStatus.OK, message,
				null);
		log(status);
	}

	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR,
				"PHPeclipsePlugin.internalErrorOccurred", e)); //$NON-NLS-1$
	}

	public static boolean isDebug() {
		return getDefault().isDebugging();
	}

	protected void initializeDefaultPreferences(IPreferenceStore store) {
		store.setDefault(PREF_STRING_CONFIGURATIONS, CONFIG_MEMENTO);
		addType(HTTP_QUERY);
		// windows preferences:
		String windowsSystem = Platform.getWS();

		if (windowsSystem.equals(Platform.WS_WIN32)) {
			store.setDefault(PHP_CHM_ENABLED, "false");
			store
					.setDefault(PHP_CHM_FILE,
							"c:\\wampp2\\php\\php_manual_en.chm");
			store.setDefault(PHP_CHM_COMMAND,
					"hh.exe \"mk:@MSITStore:{0}::/en/function.{1}.html\"");
		} else {
			store.setDefault(PHP_CHM_ENABLED, "false");
			store.setDefault(PHP_CHM_FILE, "");
			store.setDefault(PHP_CHM_COMMAND, "");
		}

	}

	/**
	 * Returns the standard display to be used. The method first checks, if the
	 * thread calling this method has an associated display. If so, this display
	 * is returned. Otherwise the method returns the default display.
	 */
	public static Display getStandardDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}

	// public void startup() throws CoreException {
	// super.startup();
	// IAdapterManager manager = Platform.getAdapterManager();
	// manager.registerAdapters(new PHPElementAdapterFactory(),
	// PHPElement.class);
	// manager.registerAdapters(new ResourceAdapterFactory(), IResource.class);
	// // externalTools.startUp();
	// getStandardDisplay().asyncExec(new Runnable() {
	// public void run() {
	// //initialize the variable context manager
	// VariableContextManager.getDefault();
	// }
	// });
	// }

	// /**
	// * @see org.eclipse.core.runtime.Plugin#shutdown()
	// */
	// public void shutdown() throws CoreException {
	// // externalTools.shutDown();
	// ColorManager.getDefault().dispose();
	// }

	public void start(BundleContext context) throws Exception {
		super.start(context);

		manager = ConfigurationManager.getInstance();
		// IAdapterManager manager = Platform.getAdapterManager();
		// manager.registerAdapters(new PHPElementAdapterFactory(),
		// PHPElement.class);
		// manager.registerAdapters(new ResourceAdapterFactory(),
		// IResource.class);
		// // externalTools.startUp();
		// getStandardDisplay().asyncExec(new Runnable() {
		// public void run() {
		// //initialize the variable context manager
		// VariableContextManager.getDefault();
		// }
		// });
	}

	public void stop(BundleContext context) throws Exception {
		// ColorManager.getDefault().dispose();
		super.stop(context);
	}

	/**
	 * Returns the translated String found with the given key.
	 * 
	 * @return java.lang.String
	 * @param key
	 *            java.lang.String
	 */
	public static String getResource(String key) {
		try {
			return Platform.getResourceString(getDefault().getBundle(), key);
		} catch (Exception e) {
			return key;
		}
	}

	/**
	 * Return a list of all the existing configurations.
	 * 
	 * @return java.util.List
	 */
	public static List getConfigurations() {
		return manager.getConfigurations();
	}

	/**
	 * Create a new monitor.
	 * 
	 * @return working copy
	 */
	public static IConfigurationWorkingCopy createConfiguration() {
		return manager.createConfiguration();
	}

	public static ArrayList getTypes() {
		return CONFIGURATION_TYPES;
	}

	public static void addType(String type) {
		CONFIGURATION_TYPES.add(type);
	}
}