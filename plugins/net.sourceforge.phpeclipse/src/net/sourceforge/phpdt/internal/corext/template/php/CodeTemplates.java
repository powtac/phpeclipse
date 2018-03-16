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
import org.eclipse.jface.text.templates.Template;

/**
 * <code>CodeTemplates</code> gives access to the available code templates.
 * 
 * @since 3.0
 * @deprecated use
 *             {@link net.sourceforge.phpdt.internal.ui.JavaPlugin#getCodeTemplateStore()}
 *             instead
 */
public class CodeTemplates extends
		net.sourceforge.phpdt.internal.corext.template.php.TemplateSet {

	private static final String DEFAULT_FILE = "default-codetemplates.xml"; //$NON-NLS-1$

	private static final String TEMPLATE_FILE = "codetemplates.xml"; //$NON-NLS-1$

	private static final ResourceBundle fgResourceBundle = ResourceBundle
			.getBundle(JavaTemplateMessages.class.getName());

	/** Singleton. */
	private static CodeTemplates fgTemplates;

	public static Template getCodeTemplate(String name) {
		return getInstance().getFirstTemplate(name);
	}

	/**
	 * Returns an instance of templates.
	 */
	public static CodeTemplates getInstance() {
		if (fgTemplates == null)
			fgTemplates = new CodeTemplates();

		return fgTemplates;
	}

	private CodeTemplates() {
		super(
				"codetemplate", PHPeclipsePlugin.getDefault().getCodeTemplateContextRegistry()); //$NON-NLS-1$
		create();
	}

	private void create() {

		try {
			addFromStream(getDefaultsAsStream(), false, true, fgResourceBundle);
			File templateFile = getTemplateFile();
			if (templateFile.exists()) {
				addFromFile(templateFile, false, fgResourceBundle);
			}
			saveToFile(templateFile);

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
		addFromFile(getTemplateFile(), false, fgResourceBundle);
	}

	/**
	 * Resets the template set with the default templates.
	 */
	public void restoreDefaults() throws CoreException {
		clear();
		addFromStream(getDefaultsAsStream(), false, true, fgResourceBundle);
	}

	/**
	 * Saves the template set.
	 */
	public void save() throws CoreException {
		saveToFile(getTemplateFile());
	}

	private static InputStream getDefaultsAsStream() {
		return CodeTemplates.class.getResourceAsStream(DEFAULT_FILE);
	}

	private static File getTemplateFile() {
		IPath path = PHPeclipsePlugin.getDefault().getStateLocation();
		path = path.append(TEMPLATE_FILE);

		return path.toFile();
	}

}
