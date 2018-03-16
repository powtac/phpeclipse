/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.phpdt.internal.ui.text.folding;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.phpdt.ui.PreferenceConstants;
import net.sourceforge.phpdt.ui.text.folding.IJavaFoldingStructureProvider;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

/**
 * @since 3.0
 */
public class JavaFoldingStructureProviderRegistry {

	private static final String EXTENSION_POINT = "foldingStructureProviders"; //$NON-NLS-1$

	/** The map of descriptors, indexed by their identifiers. */
	private Map fDescriptors;

	/**
	 * Creates a new instance.
	 */
	public JavaFoldingStructureProviderRegistry() {
	}

	/**
	 * Returns an array of <code>IJavaFoldingProviderDescriptor</code>
	 * describing all extension to the <code>foldingProviders</code> extension
	 * point.
	 * 
	 * @return the list of extensions to the
	 *         <code>quickDiffReferenceProvider</code> extension point.
	 */
	public JavaFoldingStructureProviderDescriptor[] getFoldingProviderDescriptors() {
		synchronized (this) {
			ensureRegistered();
			return (JavaFoldingStructureProviderDescriptor[]) fDescriptors
					.values()
					.toArray(
							new JavaFoldingStructureProviderDescriptor[fDescriptors
									.size()]);
		}
	}

	/**
	 * Returns the folding provider with identifier <code>id</code> or
	 * <code>null</code> if no such provider is registered.
	 * 
	 * @param id
	 *            the identifier for which a provider is wanted
	 * @return the corresponding provider, or <code>null</code> if none can be
	 *         found
	 */
	public JavaFoldingStructureProviderDescriptor getFoldingProviderDescriptor(
			String id) {
		synchronized (this) {
			ensureRegistered();
			return (JavaFoldingStructureProviderDescriptor) fDescriptors
					.get(id);
		}
	}

	/**
	 * Instantiates and returns the provider that is currently configured in the
	 * preferences.
	 * 
	 * @return the current provider according to the preferences
	 */
	public IJavaFoldingStructureProvider getCurrentFoldingProvider() {
		String id = PHPeclipsePlugin.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.EDITOR_FOLDING_PROVIDER);
		JavaFoldingStructureProviderDescriptor desc = getFoldingProviderDescriptor(id);
		if (desc != null) {
			try {
				return desc.createProvider();
			} catch (CoreException e) {
				PHPeclipsePlugin.log(e);
			}
		}
		return null;
	}

	/**
	 * Ensures that the extensions are read and stored in
	 * <code>fDescriptors</code>.
	 */
	private void ensureRegistered() {
		if (fDescriptors == null)
			reloadExtensions();
	}

	/**
	 * Reads all extensions.
	 * <p>
	 * This method can be called more than once in order to reload from a
	 * changed extension registry.
	 * </p>
	 */
	public void reloadExtensions() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		Map map = new HashMap();

		IConfigurationElement[] elements = registry
				.getConfigurationElementsFor(PHPeclipsePlugin.getPluginId(),
						EXTENSION_POINT);
		for (int i = 0; i < elements.length; i++) {
			JavaFoldingStructureProviderDescriptor desc = new JavaFoldingStructureProviderDescriptor(
					elements[i]);
			map.put(desc.getId(), desc);
		}

		synchronized (this) {
			fDescriptors = Collections.unmodifiableMap(map);
		}
	}

}
