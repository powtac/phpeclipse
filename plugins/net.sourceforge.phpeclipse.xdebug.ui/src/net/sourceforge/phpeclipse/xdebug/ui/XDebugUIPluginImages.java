/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.phpeclipse.xdebug.ui;


import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class XDebugUIPluginImages {


	/* Declare Common paths */
	private static URL ICON_BASE_URL= null;

	static {
		String pathSuffix = "icons/"; //$NON-NLS-1$
		ICON_BASE_URL= XDebugUIPlugin.getDefault().getBundle().getEntry(pathSuffix);
	}
	
	// The plugin registry
	private static ImageRegistry fgImageRegistry = null;
	
	/*
	 * Set of predefined Image Descriptors.
	 */
	private static final String T_OBJ= "obj16/"; 		//$NON-NLS-1$
//	private static final String T_OVR= "ovr16/"; 		//$NON-NLS-1$
	private static final String T_EVIEW= "eview16/"; 	//$NON-NLS-1$
	private static final String T_LCL="elcl16/";

	public static final String IMG_EVIEW_ARGUMENTS_TAB = "IMG_EVIEW_ARGUMENTS_TAB";
	public static final String IMG_EVIEW_ENVIROMENT_TAB = "IMG_EVIEW_ENVIROMENT_TAB";

	
	public static final String IMG_PREV_EVENT="IMG_PREV_EVENT";
	public static final String DESC_NEXT_EVENT="DESC_NEXT_EVENT";
	public static final String IMG_ERROR_ST_OBJ="IMG_ERROR_ST_OBJ";
	public static final String IMG_WARNING_ST_OBJ="IMG_WARNING_ST_OBJ";
	public static final String IMG_INFO_ST_OBJ="IMG_INFO_ST_OBJ";
	public static final String IMG_ERROR_STACK_OBJ="IMG_ERROR_STACK_OBJ";

	public static final String IMG_FIELD_PUBLIC = "IMG_FIELD_PUBLIC";
	public static final String IMG_FIELD_PROTECTED = "IMG_FIELD_PROTECTED";
	public static final String IMG_FIELD_PRIVATE = "IMG_FIELD_PRIVATE";

	public static final String IMG_PROPERTIES          = "IMG_PROPERTIES";
//	public static final String IMG_PROPERTIES_DISABLED = "IMG_PROPERTIES_DISABLED";
	public static final String IMG_CLEAR               = "IMG_CLEAR";
//	public static final String IMG_CLEAR_DISABLED      = "IMG_CLEAR_DISABLED";
	public static final String IMG_READ_LOG            = "IMG_READ_LOG";
//	public static final String IMG_READ_LOG_DISABLED   = "IMG_READ_LOG_DISABLED";
	public static final String IMG_REMOVE_LOG          = "IMG_REMOVE_LOG";
//	public static final String IMG_REMOVE_LOG_DISABLED = "IMG_REMOVE_LOG_DISABLED";
	public static final String IMG_FILTER              = "IMG_FILTER";
//	public static final String IMG_FILTER_DISABLED     = "IMG_FILTER_DISABLED";
	public static final String IMG_EXPORT              = "IMG_EXPORT";
//	public static final String IMG_EXPORT_DISABLED     = "IMG_EXPORT_DISABLED";
	public static final String IMG_IMPORT              = "IMG_IMPORT";
//	public static final String IMG_IMPORT_DISABLED     = "IMG_IMPORT_DISABLED";
	public static final String IMG_OPEN_LOG			   = "IMG_OPEN_LOG";





	/**
	 * Returns the image managed under the given key in this registry.
	 * 
	 * @param key the image's key
	 * @return the image managed under the given key
	 */ 
	public static Image get(String key) {
		return getImageRegistry().get(key);
	}
	
	/**
	 * Returns the <code>ImageDescriptor</code> identified by the given key,
	 * or <code>null</code> if it does not exist.
	 */
	public static ImageDescriptor getImageDescriptor(String key) {
		return getImageRegistry().getDescriptor(key);
	}	
	
	/*
	 * Helper method to access the image registry from the XDebugUIPlugin class.
	 */
	public  static ImageRegistry getImageRegistry() {
		if (fgImageRegistry == null) {
			initializeImageRegistry();
		}
		return fgImageRegistry;
	}
	
	public static void initializeImageRegistry() {
		fgImageRegistry= new ImageRegistry(XDebugUIPlugin.getStandardDisplay());
		declareImages();
	}
	
	private static void declareImages() {
		declareRegistryImage(IMG_FIELD_PUBLIC, T_OBJ + "methpub_obj.gif");
		declareRegistryImage(IMG_FIELD_PROTECTED, T_OBJ + "methpro_obj.gif");
		declareRegistryImage(IMG_FIELD_PRIVATE, T_OBJ + "methpri_obj.gif");

		declareRegistryImage(IMG_EVIEW_ARGUMENTS_TAB, T_EVIEW + "arguments_tab.gif"); //$NON-NLS-1$
		declareRegistryImage(IMG_EVIEW_ENVIROMENT_TAB, T_EVIEW + "environment_tab.gif"); //$NON-NLS-1$

		declareRegistryImage(IMG_ERROR_ST_OBJ,T_OBJ+"error_st_obj.gif"); 
		declareRegistryImage(IMG_WARNING_ST_OBJ,T_OBJ + "warning_st_obj.gif");
		declareRegistryImage(IMG_INFO_ST_OBJ,T_OBJ +"info_st_obj.gif");
		declareRegistryImage(IMG_ERROR_STACK_OBJ,T_OBJ +"error_stack.gif");

		declareRegistryImage(IMG_PROPERTIES,T_LCL + "properties.gif");
//		declareRegistryImage(IMG_PROPERTIES_DISABLED
		declareRegistryImage(IMG_CLEAR,T_LCL + "clear_log.gif");
//		declareRegistryImage(IMG_CLEAR_DISABLED
		declareRegistryImage(IMG_READ_LOG ,T_LCL + "restore_log.gif");
//		declareRegistryImage(IMG_READ_LOG_DISABLED
		declareRegistryImage(IMG_REMOVE_LOG,T_LCL + "remove_log.gif");
//		declareRegistryImage(IMG_REMOVE_LOG_DISABLED
		declareRegistryImage(IMG_FILTER,T_LCL + "filter_log.gif");
//		declareRegistryImage(IMG_FILTER_DISABLED
		declareRegistryImage(IMG_EXPORT,T_LCL + "export_log.gif");
//		declareRegistryImage(IMG_EXPORT_DISABLED
		declareRegistryImage(IMG_IMPORT,T_LCL + "import_log.gif");
//		declareRegistryImage(IMG_IMPORT_DISABLED
		declareRegistryImage(IMG_OPEN_LOG,T_LCL + "open_log.gif");
		
	}	
	
	/**
	 * Declare an Image in the registry table.
	 * @param key 	The key to use when registering the image
	 * @param path	The path where the image can be found. This path is relative to where
	 *				this plugin class is found (i.e. typically the packages directory)
	 */
	private final static void declareRegistryImage(String key, String path) {
		ImageDescriptor desc= ImageDescriptor.getMissingImageDescriptor();
		try {
			desc= ImageDescriptor.createFromURL(makeIconFileURL(path));
		} catch (MalformedURLException me) {
			XDebugUIPlugin.log(me);
		}
		fgImageRegistry.put(key, desc);
	}	
	
	private static URL makeIconFileURL(String iconPath) throws MalformedURLException {
		if (ICON_BASE_URL == null) {
			throw new MalformedURLException();
		}
			
		return new URL(ICON_BASE_URL, iconPath);
	}	




}
