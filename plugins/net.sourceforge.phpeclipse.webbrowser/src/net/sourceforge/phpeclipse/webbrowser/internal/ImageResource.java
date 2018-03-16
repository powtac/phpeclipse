/**
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 */
package net.sourceforge.phpeclipse.webbrowser.internal;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

/**
 * Utility class to handle image resources.
 */
public class ImageResource {
	// the image registry
	private static ImageRegistry imageRegistry;

	// map of image descriptors since these
	// will be lost by the image registry
	private static Map imageDescriptors;

	// base urls for images
	private static URL ICON_BASE_URL;

	static {
		try {
			String pathSuffix = "icons/";
			ICON_BASE_URL = WebBrowserUIPlugin.getInstance().getBundle()
					.getEntry(pathSuffix);
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Could not set icon base URL", e);
		}
	}

	private static Image[] busyImages;

	private static final String URL_CLCL = "clcl16/";

	private static final String URL_ELCL = "elcl16/";

	private static final String URL_DLCL = "dlcl16/";

	private static final String URL_OBJ = "obj16/";

	// --- constants for images ---
	// toolbar images
	public static final String IMG_CLCL_NAV_BACKWARD = "IMG_CLCL_NAV_BACKWARD";

	public static final String IMG_CLCL_NAV_FORWARD = "IMG_CLCL_NAV_FORWARD";

	public static final String IMG_CLCL_NAV_STOP = "IMG_CLCL_NAV_STOP";

	public static final String IMG_CLCL_NAV_REFRESH = "IMG_CLCL_NAV_REFRESH";

	public static final String IMG_CLCL_NAV_GO = "IMG_CLCL_NAV_GO";

	public static final String IMG_CLCL_NAV_FAVORITES = "cfavorites";

	public static final String IMG_CLCL_NAV_HOME = "IMG_CLCL_NAV_HOME";

	public static final String IMG_CLCL_NAV_PRINT = "IMG_CLCL_NAV_PRINT";

	public static final String IMG_ELCL_NAV_BACKWARD = "IMG_ELCL_NAV_BACKWARD";

	public static final String IMG_ELCL_NAV_FORWARD = "IMG_ELCL_NAV_FORWARD";

	public static final String IMG_ELCL_NAV_STOP = "IMG_ELCL_NAV_STOP";

	public static final String IMG_ELCL_NAV_REFRESH = "IMG_ELCL_NAV_REFRESH";

	public static final String IMG_ELCL_NAV_GO = "IMG_ELCL_NAV_GO";

	public static final String IMG_ELCL_NAV_FAVORITES = "efavorites";

	public static final String IMG_ELCL_NAV_HOME = "IMG_ELCL_NAV_HOME";

	public static final String IMG_ELCL_NAV_PRINT = "IMG_ELCL_NAV_PRINT";

	public static final String IMG_DLCL_NAV_BACKWARD = "IMG_DLCL_NAV_BACKWARD";

	public static final String IMG_DLCL_NAV_FORWARD = "IMG_DLCL_NAV_FORWARD";

	public static final String IMG_DLCL_NAV_STOP = "IMG_DLCL_NAV_STOP";

	public static final String IMG_DLCL_NAV_REFRESH = "IMG_DLCL_NAV_REFRESH";

	public static final String IMG_DLCL_NAV_GO = "IMG_DLCL_NAV_GO";

	public static final String IMG_DLCL_NAV_FAVORITES = "dfavorites";

	public static final String IMG_DLCL_NAV_HOME = "IMG_DLCL_NAV_HOME";

	public static final String IMG_DLCL_NAV_PRINT = "IMG_DLCL_NAV_PRINT";

	// general object images
	public static final String IMG_INTERNAL_BROWSER = "internalBrowser";

	public static final String IMG_EXTERNAL_BROWSER = "externalBrowser";

	public static final String IMG_FAVORITE = "favorite";

	/**
	 * Cannot construct an ImageResource. Use static methods only.
	 */
	private ImageResource() {
	}

	/**
	 * Returns the busy images for the Web browser.
	 * 
	 * @return org.eclipse.swt.graphics.Image[]
	 */
	public static Image[] getBusyImages() {
		return busyImages;
	}

	/**
	 * Return the image with the given key.
	 * 
	 * @param key
	 *            java.lang.String
	 * @return org.eclipse.swt.graphics.Image
	 */
	public static Image getImage(String key) {
		if (imageRegistry == null)
			initializeImageRegistry();
		return imageRegistry.get(key);
	}

	/**
	 * Return the image descriptor with the given key.
	 * 
	 * @param key
	 *            java.lang.String
	 * @return org.eclipse.jface.resource.ImageDescriptor
	 */
	public static ImageDescriptor getImageDescriptor(String key) {
		if (imageRegistry == null)
			initializeImageRegistry();
		return (ImageDescriptor) imageDescriptors.get(key);
	}

	/**
	 * Initialize the image resources.
	 */
	protected static void initializeImageRegistry() {
		imageRegistry = new ImageRegistry();
		imageDescriptors = new HashMap();

		// load Web browser images
		registerImage(IMG_ELCL_NAV_BACKWARD, URL_ELCL + "nav_backward.gif");
		registerImage(IMG_ELCL_NAV_FORWARD, URL_ELCL + "nav_forward.gif");
		registerImage(IMG_ELCL_NAV_STOP, URL_ELCL + "nav_stop.gif");
		registerImage(IMG_ELCL_NAV_REFRESH, URL_ELCL + "nav_refresh.gif");
		registerImage(IMG_ELCL_NAV_GO, URL_ELCL + "nav_go.gif");
		registerImage(IMG_ELCL_NAV_FAVORITES, URL_ELCL + "add_favorite.gif");
		registerImage(IMG_ELCL_NAV_HOME, URL_ELCL + "nav_home.gif");
		registerImage(IMG_ELCL_NAV_PRINT, URL_ELCL + "nav_print.gif");

		registerImage(IMG_CLCL_NAV_BACKWARD, URL_CLCL + "nav_backward.gif");
		registerImage(IMG_CLCL_NAV_FORWARD, URL_CLCL + "nav_forward.gif");
		registerImage(IMG_CLCL_NAV_STOP, URL_CLCL + "nav_stop.gif");
		registerImage(IMG_CLCL_NAV_REFRESH, URL_CLCL + "nav_refresh.gif");
		registerImage(IMG_CLCL_NAV_GO, URL_CLCL + "nav_go.gif");
		registerImage(IMG_CLCL_NAV_FAVORITES, URL_CLCL + "add_favorite.gif");
		registerImage(IMG_CLCL_NAV_HOME, URL_CLCL + "nav_home.gif");
		registerImage(IMG_CLCL_NAV_PRINT, URL_CLCL + "nav_print.gif");

		registerImage(IMG_DLCL_NAV_BACKWARD, URL_DLCL + "nav_backward.gif");
		registerImage(IMG_DLCL_NAV_FORWARD, URL_DLCL + "nav_forward.gif");
		registerImage(IMG_DLCL_NAV_STOP, URL_DLCL + "nav_stop.gif");
		registerImage(IMG_DLCL_NAV_REFRESH, URL_DLCL + "nav_refresh.gif");
		registerImage(IMG_DLCL_NAV_GO, URL_DLCL + "nav_go.gif");
		registerImage(IMG_DLCL_NAV_FAVORITES, URL_DLCL + "add_favorite.gif");
		registerImage(IMG_DLCL_NAV_HOME, URL_DLCL + "nav_home.gif");
		registerImage(IMG_DLCL_NAV_PRINT, URL_DLCL + "nav_print.gif");

		registerImage(IMG_INTERNAL_BROWSER, URL_OBJ + "internal_browser.gif");
		registerImage(IMG_EXTERNAL_BROWSER, URL_OBJ + "external_browser.gif");

		registerImage(IMG_FAVORITE, URL_OBJ + "favorite.gif");

		// busy images
		busyImages = new Image[13];
		for (int i = 0; i < 13; i++) {
			registerImage("busy" + i, URL_OBJ + "frames/frame" + (i + 1) + ".gif");
			busyImages[i] = getImage("busy" + i);
		}
	}

	/**
	 * Register an image with the registry.
	 * 
	 * @param key
	 *            java.lang.String
	 * @param partialURL
	 *            java.lang.String
	 */
	private static void registerImage(String key, String partialURL) {
		try {
			ImageDescriptor id = ImageDescriptor.createFromURL(new URL(
					ICON_BASE_URL, partialURL));
			imageRegistry.put(key, id);
			imageDescriptors.put(key, id);
		} catch (Exception e) {
			Trace.trace(Trace.WARNING, "Error registering image " + key
					+ " from " + partialURL, e);
		}
	}
}