/*
 * Copyright (c) 2002-2004 Widespace, OU and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Igor Malinin - initial contribution
 *
 * $Id: XMLPlugin.java,v 1.2 2006-10-21 23:14:13 pombredanne Exp $
 */

package net.sourceforge.phpeclipse.xml.ui;

import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.sourceforge.phpeclipse.xml.ui.text.DTDTextTools;
import net.sourceforge.phpeclipse.xml.ui.text.XMLTextTools;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class XMLPlugin extends AbstractUIPlugin {
	public static final String ICON_ELEMENT = "element_obj.gif"; //$NON-NLS-1$

	// The shared instance.
	private static XMLPlugin plugin;

	// Resource bundle.
	private ResourceBundle resources;

	private XMLTextTools xmlTextTools;

	private DTDTextTools dtdTextTools;

	/**
	 * The constructor.
	 */
	public XMLPlugin() {
		plugin = this;

		try {
			resources = ResourceBundle
					.getBundle("net.sourceforge.phpeclipse.xml.ui.XMLPluginResources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
		}
	}

	/*
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		try {
			if (xmlTextTools != null) {
				xmlTextTools.dispose();
				xmlTextTools = null;
			}

			if (dtdTextTools != null) {
				dtdTextTools.dispose();
				dtdTextTools = null;
			}
		} finally {
			super.stop(context);
		}
	}

	/**
	 * Returns the shared instance.
	 */
	public static XMLPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not
	 * found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = XMLPlugin.getDefault().getResourceBundle();
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle.
	 */
	public ResourceBundle getResourceBundle() {
		return resources;
	}

	/**
	 * Returns instance of text tools for XML.
	 */
	public XMLTextTools getXMLTextTools() {
		if (xmlTextTools == null) {
			xmlTextTools = new XMLTextTools(getPreferenceStore());
		}
		return xmlTextTools;
	}

	/**
	 * Returns instance of text tools for DTD.
	 */
	public DTDTextTools getDTDTextTools() {
		if (dtdTextTools == null) {
			dtdTextTools = new DTDTextTools(getPreferenceStore());
		}
		return dtdTextTools;
	}

	/*
	 * @see AbstractUIPlugin#initializeImageRegistry(ImageRegistry)
	 */
	protected void initializeImageRegistry(ImageRegistry reg) {
		reg.put(ICON_ELEMENT, getImageDescriptor(ICON_ELEMENT));
	}

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

}
