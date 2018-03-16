/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 �*
 * Contributors:
 *    IBM - Initial API and implementation
 **********************************************************************/
package net.sourceforge.phpdt.httpquery.config;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.phpdt.phphelp.PHPHelpPlugin;

import org.eclipse.core.runtime.Preferences;

/**
 * 
 */
public class ConfigurationManager {
	private static final int ADD = 0;

	private static final int CHANGE = 1;

	private static final int REMOVE = 2;

	// configurations
	protected List configurations;

	protected Map threads = new HashMap();

	protected List configurationListeners = new ArrayList();

	private Preferences.IPropertyChangeListener pcl;

	protected boolean ignorePreferenceChanges = false;

	protected static ConfigurationManager instance;

	public static ConfigurationManager getInstance() {
		if (instance == null)
			instance = new ConfigurationManager();
		return instance;
	}

	private ConfigurationManager() {
		loadConfigurations();

		pcl = new Preferences.IPropertyChangeListener() {
			public void propertyChange(Preferences.PropertyChangeEvent event) {
				if (ignorePreferenceChanges)
					return;
				String property = event.getProperty();
				if (property.equals(PHPHelpPlugin.PREF_STRING_CONFIGURATIONS)) {
					loadConfigurations();
				}
			}
		};

		PHPHelpPlugin.getDefault().getPluginPreferences()
				.addPropertyChangeListener(pcl);
	}

	protected void dispose() {
		PHPHelpPlugin.getDefault().getPluginPreferences()
				.removePropertyChangeListener(pcl);
	}

	public IConfigurationWorkingCopy createConfiguration() {
		return new ConfigurationWorkingCopy();
	}

	public List getConfigurations() {
		return new ArrayList(configurations);
	}

	protected void addConfiguration(IConfiguration configuration) {
		if (!configurations.contains(configuration))
			configurations.add(configuration);
		fireConfigurationEvent(configuration, ADD);
		saveConfigurations();
	}

	protected boolean isActive(IConfiguration configuration) {
		return (threads.get(configuration) != null);
	}

	protected void removeConfiguration(IConfiguration configuration) {
		configurations.remove(configuration);
		fireConfigurationEvent(configuration, REMOVE);
		saveConfigurations();
	}

	protected void configurationChanged(IConfiguration configuration) {
		fireConfigurationEvent(configuration, CHANGE);
		saveConfigurations();
	}

	/**
	 * Add monitor listener.
	 * 
	 * @param listener
	 */
	public void addConfigurationListener(IConfigurationListener listener) {
		configurationListeners.add(listener);
	}

	/**
	 * Remove monitor listener.
	 * 
	 * @param listener
	 */
	public void removeConfigurationListener(IConfigurationListener listener) {
		configurationListeners.remove(listener);
	}

	/**
	 * Fire a monitor event.
	 * 
	 * @param rr
	 * @param fType
	 */
	protected void fireConfigurationEvent(IConfiguration configuration, int type) {
		Object[] obj = configurationListeners.toArray();

		int size = obj.length;
		for (int i = 0; i < size; i++) {
			IConfigurationListener listener = (IConfigurationListener) obj[i];
			if (type == ADD)
				listener.configurationAdded(configuration);
			else if (type == CHANGE)
				listener.configurationChanged(configuration);
			else if (type == REMOVE)
				listener.configurationRemoved(configuration);
		}
	}

	protected void loadConfigurations() {

		configurations = new ArrayList();
		Preferences prefs = PHPHelpPlugin.getDefault().getPluginPreferences();
		String xmlString = prefs
				.getString(PHPHelpPlugin.PREF_STRING_CONFIGURATIONS);
		if (xmlString != null && xmlString.length() > 0) {
			try {
				ByteArrayInputStream in = new ByteArrayInputStream(xmlString
						.getBytes());
				IMemento memento = XMLMemento.loadMemento(in);

				IMemento[] children = memento.getChildren("config");
				if (children != null) {
					int size = children.length;
					for (int i = 0; i < size; i++) {
						Configuration configuration = new ConfigurationWorkingCopy();
						configuration.load(children[i]);
						configurations.add(configuration);
					}
				}
			} catch (Exception e) {
			}
		}
	}

	protected void saveConfigurations() {
		try {
			ignorePreferenceChanges = true;
			XMLMemento memento = XMLMemento
					.createWriteRoot(PHPHelpPlugin.PREF_STRING_CONFIGURATIONS);

			Iterator iterator = configurations.iterator();
			while (iterator.hasNext()) {
				Configuration monitor = (Configuration) iterator.next();
				IMemento child = memento.createChild("config");
				monitor.save(child);
			}

			String xmlString = memento.saveToString();
			Preferences prefs = PHPHelpPlugin.getDefault()
					.getPluginPreferences();
			prefs.setValue(PHPHelpPlugin.PREF_STRING_CONFIGURATIONS, xmlString);
			PHPHelpPlugin.getDefault().savePluginPreferences();
		} catch (Exception e) {
		}
		ignorePreferenceChanges = false;
	}
}