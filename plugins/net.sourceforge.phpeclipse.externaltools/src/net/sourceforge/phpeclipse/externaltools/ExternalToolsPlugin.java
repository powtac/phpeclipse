package net.sourceforge.phpeclipse.externaltools;

/**********************************************************************
 Copyright (c) 2002 IBM Corp. and others. All rights reserved.
 This file is made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html
 �
 Contributors:
 **********************************************************************/

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.PropertyResourceBundle;

import net.sourceforge.phpdt.externaltools.internal.model.ExternalToolsImages;
import net.sourceforge.phpdt.externaltools.internal.model.IPreferenceConstants;
import net.sourceforge.phpdt.externaltools.internal.model.VariableContextManager;
import net.sourceforge.phpdt.externaltools.internal.registry.ArgumentVariableRegistry;
import net.sourceforge.phpdt.externaltools.internal.registry.PathLocationVariableRegistry;
import net.sourceforge.phpdt.externaltools.internal.registry.RefreshScopeVariableRegistry;
import net.sourceforge.phpdt.externaltools.model.IExternalToolConstants;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * External tools plug-in class
 */
public final class ExternalToolsPlugin extends AbstractUIPlugin {
	public static final String XAMPP_START_PREF = "_xampp_start_pref";

	public static final String XAMPP_STOP_PREF = "_xampp_stop_pref";

	public static final String MYSQL_RUN_PREF = "_mysql_run_pref";

	public static final String MYSQL_START_BACKGROUND = "_mysql_start_background";

	public static final String MYSQL_PREF = "__mysql_start";

	public static final String APACHE_RUN_PREF = "_apache_run_pref";

	public static final String APACHE_START_BACKGROUND = "_apache_start_background";

	public static final String APACHE_START_PREF = "__apache_start";

	public static final String APACHE_STOP_BACKGROUND = "_apache_stop_background";

	public static final String APACHE_STOP_PREF = "__apache_stop";

	public static final String APACHE_RESTART_BACKGROUND = "_apache_restart_background";

	public static final String APACHE_RESTART_PREF = "__apache_restart";

	public static final String HTTPD_CONF_PATH_PREF = "__httpd_conf_path";

	public static final String ETC_HOSTS_PATH_PREF = "__etc_hosts_path";

	// public static final String SHOW_OUTPUT_IN_CONSOLE =
	// "_show_output_in_console";

	public static final String PHP_RUN_PREF = "_php_run_pref";

	public static final String EXTERNAL_PARSER_PREF = "_external_parser";

	/**
	 * Status representing no problems encountered during operation.
	 */
	public static final IStatus OK_STATUS = new Status(IStatus.OK,
			IExternalToolConstants.PLUGIN_ID, 0, "", null); //$NON-NLS-1$

	private static ExternalToolsPlugin plugin;

	private RefreshScopeVariableRegistry refreshVarRegistry;

	private PathLocationVariableRegistry fileLocVarRegistry;

	private PathLocationVariableRegistry dirLocVarRegistry;

	private ArgumentVariableRegistry argumentVarRegistry;

	/**
	 * This version is recommended for eclipse3.0 and above
	 */
	public ExternalToolsPlugin() {
		super();
		plugin = this;
	}

	/**
	 * Returns the default instance of the receiver. This represents the runtime
	 * plugin.
	 */
	public static ExternalToolsPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns a new <code>IStatus</code> for this plug-in
	 */
	public static IStatus newErrorStatus(String message, Throwable exception) {
		return new Status(Status.ERROR, IExternalToolConstants.PLUGIN_ID, 0,
				message, exception);
	}

	/**
	 * Returns a new <code>CoreException</code> for this plug-in
	 */
	public static CoreException newError(String message, Throwable exception) {
		return new CoreException(new Status(Status.ERROR,
				IExternalToolConstants.PLUGIN_ID, 0, message, exception));
	}

	/**
	 * Returns the registry of refresh scope variables.
	 */
	public ArgumentVariableRegistry getArgumentVariableRegistry() {
		if (argumentVarRegistry == null)
			argumentVarRegistry = new ArgumentVariableRegistry();
		return argumentVarRegistry;
	}

	/**
	 * Returns the registry of directory location variables.
	 */
	public PathLocationVariableRegistry getDirectoryLocationVariableRegistry() {
		if (dirLocVarRegistry == null)
			dirLocVarRegistry = new PathLocationVariableRegistry(
					IExternalToolConstants.EXTENSION_POINT_DIRECTORY_VARIABLES);
		return dirLocVarRegistry;
	}

	/**
	 * Returns the registry of file location variables.
	 */
	public PathLocationVariableRegistry getFileLocationVariableRegistry() {
		if (fileLocVarRegistry == null)
			fileLocVarRegistry = new PathLocationVariableRegistry(
					IExternalToolConstants.EXTENSION_POINT_FILE_VARIABLES);
		return fileLocVarRegistry;
	}

	/**
	 * Returns the registry of refresh scope variables.
	 */
	public RefreshScopeVariableRegistry getRefreshVariableRegistry() {
		if (refreshVarRegistry == null)
			refreshVarRegistry = new RefreshScopeVariableRegistry();
		return refreshVarRegistry;
	}

	/**
	 * Writes the message to the plug-in's log
	 * 
	 * @param message
	 *            the text to write to the log
	 */
	public void log(String message, Throwable exception) {
		IStatus status = newErrorStatus(message, exception);
		// getLog().log(status);
		ExternalToolsPlugin.log(status);
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	/**
	 * Returns the ImageDescriptor for the icon with the given path
	 * 
	 * @return the ImageDescriptor object
	 */
	public ImageDescriptor getImageDescriptor(String path) {
		try {
			Bundle bundle = ExternalToolsPlugin.getDefault().getBundle();
			URL installURL = bundle.getEntry("/"); //$NON-NLS-1$
			URL url = new URL(installURL, path);
			return ImageDescriptor.createFromURL(url);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/*
	 * (non-Javadoc) Method declared in AbstractUIPlugin.
	 */

	protected void initializeDefaultPreferences(IPreferenceStore store) {
		String operatingSystem = Platform.getOS();
		// maxosx, linux, solaris, win32,...
		try {
			InputStream is = getDefault()
					.openStream(
							new Path("prefs/default_" + operatingSystem
									+ ".properties"));
			PropertyResourceBundle resourceBundle = new PropertyResourceBundle(
					is);
			Enumeration e = resourceBundle.getKeys();
			String key;
			while (e.hasMoreElements()) {
				key = (String) e.nextElement();
				store.setDefault(key, resourceBundle.getString(key));
			}
		} catch (Exception e) {
			// no default properties found
			if (operatingSystem.equals(Platform.OS_WIN32)) {
				store.setDefault(PHP_RUN_PREF, "c:\\apache\\php\\php.exe");
				store.setDefault(EXTERNAL_PARSER_PREF,
						"c:\\apache\\php\\php -l -f {0}");
				store.setDefault(MYSQL_RUN_PREF,
						"c:\\apache\\mysql\\bin\\mysqld-nt.exe");
				store.setDefault(APACHE_RUN_PREF, "c:\\apache\\apache.exe");
				store
						.setDefault(XAMPP_START_PREF,
								"c:\\xampp\\xampp_start.exe");
				store.setDefault(XAMPP_STOP_PREF, "c:\\xampp\\xampp_stop.exe");
				store.setDefault(ETC_HOSTS_PATH_PREF,
						"c:\\windows\\system32\\drivers\\etc\\hosts");
			} else {
				store.setDefault(PHP_RUN_PREF, "/apache/php/php");
				store.setDefault(EXTERNAL_PARSER_PREF,
						"/apache/php/php -l -f {0}");
				store.setDefault(MYSQL_RUN_PREF, "/apache/mysql/bin/mysqld");
				store.setDefault(APACHE_RUN_PREF, "/apache/apache");
				store.setDefault(XAMPP_START_PREF, "xamp/xampp_start");
				store.setDefault(XAMPP_STOP_PREF, "xampp/xampp_stop");
			}
			store.setDefault(MYSQL_PREF, "--standalone");
			store.setDefault(APACHE_START_PREF, "-c \"DocumentRoot \"{0}\"\"");
			store.setDefault(APACHE_STOP_PREF, "-k shutdown");
			store.setDefault(APACHE_RESTART_PREF, "-k restart");
			store.setDefault(MYSQL_START_BACKGROUND, "true");
			store.setDefault(APACHE_START_BACKGROUND, "true");
			store.setDefault(APACHE_STOP_BACKGROUND, "true");
			store.setDefault(APACHE_RESTART_BACKGROUND, "true");
		}

		// store.setDefault(SHOW_OUTPUT_IN_CONSOLE, "true");

		store.setDefault(IPreferenceConstants.PROMPT_FOR_MIGRATION, true);

		PreferenceConverter.setDefault(store,
				IPreferenceConstants.CONSOLE_ERROR_RGB, new RGB(255, 0, 0)); // red
																				// -
																				// exactly
																				// the
																				// same
																				// as
		// debug Console
		PreferenceConverter.setDefault(store,
				IPreferenceConstants.CONSOLE_WARNING_RGB, new RGB(255, 100, 0)); // orange
		PreferenceConverter.setDefault(store,
				IPreferenceConstants.CONSOLE_INFO_RGB, new RGB(0, 0, 255)); // blue
		PreferenceConverter.setDefault(store,
				IPreferenceConstants.CONSOLE_VERBOSE_RGB, new RGB(0, 200, 125)); // green
		PreferenceConverter.setDefault(store,
				IPreferenceConstants.CONSOLE_DEBUG_RGB, new RGB(0, 0, 0)); // black
	}

	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return ExternalToolsPlugin.getDefault().getWorkbench()
				.getActiveWorkbenchWindow();
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

	/**
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#createImageRegistry()
	 */
	protected ImageRegistry createImageRegistry() {
		return ExternalToolsImages.initializeImageRegistry();
	}

	/**
	 * @throws Exception
	 * @see org.eclipse.core.runtime.Plugin#start(BundleContext context)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		getStandardDisplay().asyncExec(new Runnable() {
			public void run() {
				// initialize the variable context manager
				VariableContextManager.getDefault();
			}
		});
	}
}