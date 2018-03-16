/*
 * Copyright (c) 2004 Christopher Lenz and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Christopher Lenz - initial implementation
 *
 * $Id: WebUI.java,v 1.11 2007-11-08 01:37:06 scorphus Exp $
 */

package net.sourceforge.phpeclipse.ui;

import java.io.IOException;
import java.net.URL;

import net.sourceforge.phpeclipse.ui.templates.template.HTMLContextType;
import net.sourceforge.phpeclipse.ui.templates.template.JSContextType;
import net.sourceforge.phpeclipse.ui.templates.template.SmartyContextType;
import net.sourceforge.phpeclipse.ui.templates.template.XMLContextType;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The web development tools UI plugin.
 */
public class WebUI extends AbstractUIPlugin implements IPreferenceConstants {

	public static final String PLUGIN_ID = "net.sourceforge.phpeclipse.ui";

	private static final String CUSTOM_TEMPLATES_KEY = "net.sourceforge.phpeclipse.ui.templates"; //$NON-NLS-1$

	// Constants ---------------------------------------------------------------

	public static final String ICON_OVERLAY_ERROR = "full/ovr16/error_co.gif"; //$NON-NLS-1$

	public static final String ICON_OVERLAY_WARNING = "full/ovr16/warning_co.gif"; //$NON-NLS-1$

	// Instance Variables ------------------------------------------------------

	/** The shared instance. */
	private static WebUI plugin;

	public static IWorkbenchPage getActivePage() {
		return getDefault().internalGetActivePage();
	}

	private IWorkbenchPage internalGetActivePage() {
		return getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}

	public static Shell getActiveWorkbenchShell() {
		return getActiveWorkbenchWindow().getShell();
	}

	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	// Public Methods ----------------------------------------------------------

	/**
	 * Returns the shared instance.
	 */
	public static WebUI getDefault() {
		return plugin;
	}

	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/** The context type registry. */
	private ContributionContextTypeRegistry fRegistry;

	/** The template store. */
	private TemplateStore fStore;

	// Constructors ------------------------------------------------------------

	/**
	 * The constructor.
	 */
	public WebUI() {
		plugin = this;
	}

	/**
	 * Returns this plug-in's context type registry.
	 * 
	 * @return the context type registry for this plug-in instance
	 */
	public ContextTypeRegistry getContextTypeRegistry() {
		if (fRegistry == null) {
			// create an configure the contexts available in the editor
			fRegistry = new ContributionContextTypeRegistry();
			fRegistry.addContextType(XMLContextType.XML_CONTEXT_TYPE);
			fRegistry.addContextType(HTMLContextType.HTML_CONTEXT_TYPE);
			fRegistry.addContextType(SmartyContextType.SMARTY_CONTEXT_TYPE);
			fRegistry.addContextType(JSContextType.JS_CONTEXT_TYPE);
		}
		return fRegistry;
	}

	// Private Methods ---------------------------------------------------------

	/**
	 * Returns an image descriptor for the image corresponding to the specified
	 * key (which is the name of the image file).
	 * 
	 * @param key
	 *            The key of the image
	 * @return The descriptor for the requested image, or <code>null</code> if
	 *         the image could not be found
	 */
	private ImageDescriptor getImageDescriptor(String key) {
		try {
			URL url = getBundle().getEntry("/icons/" + key); //$NON-NLS-1$
			return ImageDescriptor.createFromURL(url);
		} catch (IllegalStateException e) {
			return null;
		}
	}

	/**
	 * Returns this plug-in's template store.
	 * 
	 * @return the template store of this plug-in instance
	 */
	public TemplateStore getTemplateStore() {
		if (fStore == null) {
			fStore = new ContributionTemplateStore(getContextTypeRegistry(),
					getDefault().getPreferenceStore(), CUSTOM_TEMPLATES_KEY);
			try {
				fStore.load();
			} catch (IOException e) {
				WebUI
						.getDefault()
						.getLog()
						.log(
								new Status(
										IStatus.ERROR,
										"net.sourceforge.phpeclipse.ui", IStatus.OK, "", e)); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return fStore;
	}

	protected void initializeDefaultPreferences(IPreferenceStore store) {
		store.setDefault(PHP_LOCALHOST_PREF, "http://localhost");
		store.setDefault(PHP_DOCUMENTROOT_PREF, getWorkspace().getRoot()
				.getFullPath().toString());
		// store.setDefault(PHP_BOOKMARK_DEFAULT, "");

		store.setDefault(PHP_AUTO_PREVIEW_DEFAULT, "false");
		store.setDefault(PHP_BRING_TO_TOP_PREVIEW_DEFAULT, "false");
		store.setDefault(PHP_STICKY_BROWSER_URL_DEFAULT, "false");
		// store.setDefault(PHP_SHOW_HTML_FILES_LOCAL, "true");
		// store.setDefault(PHP_SHOW_XML_FILES_LOCAL, "false");
	}

	/*
	 * @see AbstractUIPlugin#initializeImageRegistry(ImageRegistry)
	 */
	protected void initializeImageRegistry(ImageRegistry reg) {
		reg.put(ICON_OVERLAY_ERROR, getImageDescriptor(ICON_OVERLAY_ERROR));
		reg.put(ICON_OVERLAY_WARNING, getImageDescriptor(ICON_OVERLAY_WARNING));
	}

	// private IWorkbenchPage internalGetActivePage() {
	// IWorkbenchWindow window = getWorkbench().getActiveWorkbenchWindow();
	// if (window != null)
	// return window.getActivePage();
	// return null;
	// }

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, ""
				+ e.getLocalizedMessage(), e));
	}
}