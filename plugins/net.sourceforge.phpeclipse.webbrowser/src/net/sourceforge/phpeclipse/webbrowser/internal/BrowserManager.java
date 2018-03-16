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
package net.sourceforge.phpeclipse.webbrowser.internal;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.phpeclipse.webbrowser.IExternalWebBrowser;
import net.sourceforge.phpeclipse.webbrowser.IExternalWebBrowserWorkingCopy;
import net.sourceforge.phpeclipse.webbrowser.IInternalWebBrowser;
import net.sourceforge.phpeclipse.webbrowser.IWebBrowser;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;

/**
 * 
 */
public class BrowserManager {
	private static final int ADD = 0;

	private static final int CHANGE = 1;

	private static final int REMOVE = 2;

	protected List browsers;

	protected IWebBrowser currentBrowser;

	protected List browserListeners = new ArrayList();

	private Preferences.IPropertyChangeListener pcl;

	protected boolean ignorePreferenceChanges = false;

	protected static BrowserManager instance;

	public static BrowserManager getInstance() {
		if (instance == null)
			instance = new BrowserManager();
		return instance;
	}

	private BrowserManager() {
		pcl = new Preferences.IPropertyChangeListener() {
			public void propertyChange(Preferences.PropertyChangeEvent event) {
				if (ignorePreferenceChanges)
					return;
				String property = event.getProperty();
				if (property.equals("browsers")) {
					loadBrowsers();
				}
			}
		};

		WebBrowserUIPlugin.getInstance().getPluginPreferences()
				.addPropertyChangeListener(pcl);
	}

	protected void dispose() {
		WebBrowserUIPlugin.getInstance().getPluginPreferences()
				.removePropertyChangeListener(pcl);

		// clear the cache
		if (browsers != null) {
			Iterator iterator = browsers.iterator();
			while (iterator.hasNext()) {
				Object obj = iterator.next();
				if (obj instanceof IInternalWebBrowser) {
					IInternalWebBrowser wb = (IInternalWebBrowser) obj;
					if (wb.getClearHistoryOnExit())
						WebBrowserPreference.setInternalWebBrowserHistory(null);
				}
			}
		}
	}

	public IExternalWebBrowserWorkingCopy createExternalWebBrowser() {
		return new ExternalWebBrowserWorkingCopy();
	}

	public List getWebBrowsers() {
		if (browsers == null)
			loadBrowsers();
		return new ArrayList(browsers);
	}

	protected void loadBrowsers() {
		Trace.trace(Trace.FINEST, "Loading web browsers");

		Preferences prefs = WebBrowserUIPlugin.getInstance()
				.getPluginPreferences();
		String xmlString = prefs.getString("browsers");
		if (xmlString != null && xmlString.length() > 0) {
			browsers = new ArrayList();

			try {
				ByteArrayInputStream in = new ByteArrayInputStream(xmlString
						.getBytes());
				Reader reader = new InputStreamReader(in);
				IMemento memento = XMLMemento.createReadRoot(reader);

				IMemento child = memento.getChild("internal");
				if (child != null) {
					InternalWebBrowser browser = new InternalWebBrowser();
					browser.load(child);
					browsers.add(browser);
				}

				IMemento[] children = memento.getChildren("external");
				int size = children.length;
				for (int i = 0; i < size; i++) {
					ExternalWebBrowser browser = new ExternalWebBrowser();
					browser.load(children[i]);
					browsers.add(browser);
				}

				Integer current = memento.getInteger("current");
				if (current != null) {
					currentBrowser = (IWebBrowser) browsers.get(current
							.intValue());
				}
			} catch (Exception e) {
				Trace.trace(Trace.WARNING, "Could not load browsers: "
						+ e.getMessage());
			}
			addInternalBrowser(browsers);
			if (currentBrowser == null && browsers.size() > 0)
				currentBrowser = (IWebBrowser) browsers.get(0);
		} else {
			setupDefaultBrowsers();
			saveBrowsers();
			return;
		}
	}

	protected void saveBrowsers() {
		try {
			ignorePreferenceChanges = true;
			XMLMemento memento = XMLMemento.createWriteRoot("web-browsers");

			Iterator iterator = browsers.iterator();
			while (iterator.hasNext()) {
				Object obj = iterator.next();
				if (obj instanceof InternalWebBrowser) {
					InternalWebBrowser browser = (InternalWebBrowser) obj;
					IMemento child = memento.createChild("internal");
					browser.save(child);
				} else if (obj instanceof ExternalWebBrowser) {
					ExternalWebBrowser browser = (ExternalWebBrowser) obj;
					IMemento child = memento.createChild("external");
					browser.save(child);
				}
			}

			memento.putInteger("current", browsers.indexOf(currentBrowser));

			StringWriter writer = new StringWriter();
			memento.save(writer);
			String xmlString = writer.getBuffer().toString();
			Preferences prefs = WebBrowserUIPlugin.getInstance()
					.getPluginPreferences();
			prefs.setValue("browsers", xmlString);
			WebBrowserUIPlugin.getInstance().savePluginPreferences();
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Could not save browsers", e);
		}
		ignorePreferenceChanges = false;
	}

	protected void addInternalBrowser(List browserList) {
		if (browserList == null)
			return;

		Iterator iterator = browserList.iterator();
		while (iterator.hasNext()) {
			IWebBrowser browser = (IWebBrowser) iterator.next();
			if (browser instanceof IInternalWebBrowser)
				return;
		}

		// add the internal browser if we can
		// WebBrowserUIPlugin.getInstance().getLog().log(
		// new Status(IStatus.INFO, WebBrowserUIPlugin.PLUGIN_ID, 0,
		// WebBrowserUtil.canUseInternalWebBrowser() + "/"
		// + WebBrowserUtil.isInternalBrowserOperational(), null));
		// if (!WebBrowserUtil.canUseInternalWebBrowser() ||
		// !WebBrowserUtil.isInternalBrowserOperational())
		// return;
		WebBrowserUIPlugin.getInstance().getLog().log(
				new Status(IStatus.INFO, WebBrowserUIPlugin.PLUGIN_ID, 0, "-"
						+ WebBrowserUtil.isInternalBrowserOperational(), null));
		if (!WebBrowserUtil.isInternalBrowserOperational())
			return;
		browserList.add(0, new InternalWebBrowser());
	}

	private void setupDefaultBrowsers() {
		browsers = new ArrayList();

		addInternalBrowser(browsers);

		// handle all the EXTERNAL browsers by criteria and add those too at
		// startup
		WebBrowserUtil.addFoundBrowsers(browsers);

		// by default, if internal is there, that is current, else set the first
		// external one
		if (!browsers.isEmpty())
			currentBrowser = (IWebBrowser) browsers.get(0);
	}

	protected void addBrowser(IExternalWebBrowser browser) {
		if (browsers == null)
			loadBrowsers();
		if (!browsers.contains(browser))
			browsers.add(browser);
		fireWebBrowserEvent(browser, ADD);
		saveBrowsers();
	}

	protected void removeWebBrowser(IExternalWebBrowser browser) {
		if (browsers == null)
			loadBrowsers();
		browsers.remove(browser);
		fireWebBrowserEvent(browser, REMOVE);
	}

	// Internal Web browser CAN be "edited", just not created or removed
	protected void browserChanged(IWebBrowser browser) {
		fireWebBrowserEvent(browser, CHANGE);
		saveBrowsers();
	}

	/**
	 * Add Web browser listener.
	 * 
	 * @param listener
	 */
	public void addWebBrowserListener(IWebBrowserListener listener) {
		browserListeners.add(listener);
	}

	/**
	 * Remove Web browser listener.
	 * 
	 * @param listener
	 */
	public void removeWebBrowserListener(IWebBrowserListener listener) {
		browserListeners.remove(listener);
	}

	/**
	 * Fire a Web browser event.
	 * 
	 * @param browser
	 * @param type
	 */
	protected void fireWebBrowserEvent(IWebBrowser browser, int type) {
		Object[] obj = browserListeners.toArray();

		int size = obj.length;
		for (int i = 0; i < size; i++) {
			IWebBrowserListener listener = (IWebBrowserListener) obj[i];
			if (type == ADD)
				listener.browserAdded(browser);
			else if (type == CHANGE)
				listener.browserChanged(browser);
			else if (type == REMOVE)
				listener.browserRemoved(browser);
		}
	}

	public IWebBrowser getCurrentWebBrowser() {
		if (browsers == null)
			loadBrowsers();

		return currentBrowser;
	}

	public void setCurrentWebBrowser(IWebBrowser wb) {
		if (browsers.contains(wb))
			currentBrowser = wb;
		saveBrowsers();
	}
}