/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package net.sourceforge.phpeclipse.obfuscator;

import java.io.File;
import java.io.InputStream;

import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.ErrorDialog;

/**
 * <code>ObfuscatorIgnores</code> gives access to the available templates.
 */
public class ObfuscatorIgnores extends ObfuscatorIgnoreSet {

	private static final String DEFAULT_FILE = "default-obfuscator.xml"; //$NON-NLS-1$

	private static final String TEMPLATE_FILE = "obfuscator.xml"; //$NON-NLS-1$

	/** Singleton. */
	private static ObfuscatorIgnores fgIgnores;

	private IProject fProject;

	public ObfuscatorIgnores(IProject project) {
		fProject = project;
		try {
			File templateFile = getTemplateFile();
			if (templateFile.exists()) {
				addFromFile(templateFile);
			} else {
				addFromStream(getDefaultsAsStream());
				saveToFile(templateFile);
			}

		} catch (CoreException e) {
			PHPeclipsePlugin.log(e);
			ErrorDialog.openError(null, ObfuscatorMessages
					.getString("Obfuscator.error.title"), //$NON-NLS-1$
					e.getMessage(), e.getStatus());

			clear();
		}
	}

	/**
	 * Returns an instance of templates.
	 */
	// public static ObfuscatorIgnores getInstance() {
	// if (fgIgnores == null)
	// fgIgnores = create();
	//
	// return fgIgnores;
	// }
	//
	// private static ObfuscatorIgnores create() {
	// ObfuscatorIgnores templates = new ObfuscatorIgnores();
	//
	// try {
	// File templateFile = getTemplateFile();
	// if (templateFile.exists()) {
	// templates.addFromFile(templateFile);
	// } else {
	// templates.addFromStream(getDefaultsAsStream());
	// templates.saveToFile(templateFile);
	// }
	//
	// } catch (CoreException e) {
	// PHPeclipsePlugin.log(e);
	// ErrorDialog.openError(null,
	// ObfuscatorMessages.getString("Templates.error.title"), //$NON-NLS-1$
	// e.getMessage(), e.getStatus());
	//
	// templates.clear();
	// }
	//
	// return templates;
	// }
	/**
	 * Resets the template set.
	 */
	public void reset() throws CoreException {
		clear();
		addFromFile(getTemplateFile());
	}

	/**
	 * Resets the template set with the default templates.
	 */
	public void restoreDefaults() throws CoreException {
		clear();
		addFromStream(getDefaultsAsStream());
	}

	/**
	 * Saves the template set.
	 */
	public void save() throws CoreException {
		saveToFile(getTemplateFile());
	}

	private InputStream getDefaultsAsStream() {
		return ObfuscatorIgnores.class.getResourceAsStream(DEFAULT_FILE);
	}

	private File getTemplateFile() {
		IPath path = fProject.getFullPath();
		// PHPeclipsePlugin.getDefault().getStateLocation();
		path = path.append(TEMPLATE_FILE);

		return path.toFile();
	}
}
