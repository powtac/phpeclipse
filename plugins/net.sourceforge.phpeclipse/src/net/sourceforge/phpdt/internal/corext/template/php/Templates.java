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
package net.sourceforge.phpdt.internal.corext.template.php;

import java.io.File;
import java.io.InputStream;
import java.util.ResourceBundle;

import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * <code>Templates</code> gives access to the available templates.
 * 
 * @deprecated As of 3.0, replaced by
 *             {@link org.eclipse.jface.text.templates.persistence.TemplateStore}
 */
public class Templates extends
		net.sourceforge.phpdt.internal.corext.template.php.TemplateSet {

	private static final String DEFAULT_FILE = "default-templates.xml"; //$NON-NLS-1$

	private static final String TEMPLATE_FILE = "templates.xml"; //$NON-NLS-1$

	private static final ResourceBundle fgResourceBundle = ResourceBundle
			.getBundle(JavaTemplateMessages.class.getName());

	/** Singleton. */
	private static Templates fgTemplates;

	/**
	 * Returns an instance of templates.
	 * 
	 * @deprecated As of 3.0, replaced by
	 *             {@link net.sourceforge.phpdt.internal.ui.JavaPlugin#getTemplateStore()}
	 */
	public static Templates getInstance() {
		if (fgTemplates == null)
			fgTemplates = new Templates();

		return fgTemplates;
	}

	public Templates() {
		super(
				"template", PHPeclipsePlugin.getDefault().getTemplateContextRegistry()); //$NON-NLS-1$
		create();
	}

	private void create() {

		try {
			File templateFile = getTemplateFile();
			if (templateFile.exists()) {
				addFromFile(templateFile, true, fgResourceBundle);
			}

		} catch (CoreException e) {
			PHPeclipsePlugin.log(e);
			clear();
		}

	}

	/**
	 * Resets the template set.
	 */
	public void reset() throws CoreException {
		clear();
		addFromFile(getTemplateFile(), true, fgResourceBundle);
	}

	/**
	 * Resets the template set with the default templates.
	 */
	public void restoreDefaults() throws CoreException {
		clear();
		addFromStream(getDefaultsAsStream(), true, true, fgResourceBundle);
	}

	/**
	 * Saves the template set.
	 */
	public void save() throws CoreException {
		saveToFile(getTemplateFile());
	}

	private static InputStream getDefaultsAsStream() {
		return Templates.class.getResourceAsStream(DEFAULT_FILE);
	}

	private static File getTemplateFile() {
		IPath path = PHPeclipsePlugin.getDefault().getStateLocation();
		path = path.append(TEMPLATE_FILE);

		return path.toFile();
	}
}
