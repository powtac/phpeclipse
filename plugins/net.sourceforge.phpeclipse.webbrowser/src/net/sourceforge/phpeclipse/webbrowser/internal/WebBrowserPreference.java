/**
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. � This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 �*
 * Contributors:
 *    IBM - Initial API and implementation
 */
package net.sourceforge.phpeclipse.webbrowser.internal;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Preferences for the Web browser.
 */
public class WebBrowserPreference {
	protected static final String PREF_BROWSER_HISTORY = "webBrowserHistory";

	protected static final String PREF_INTERNAL_WEB_BROWSER_HISTORY = "internalWebBrowserHistory";

	protected static final String PREF_INTERNAL_WEB_BROWSER_FAVORITES = "internalWebBrowserFavorites";

	protected static final String PREF_INTERNAL_WEB_BROWSER_OLD_FAVORITES = "internalWebBrowserOldFavorites";

	protected static final String URL_PARAMETER = "%URL%";

	/**
	 * WebBrowserPreference constructor comment.
	 */
	private WebBrowserPreference() {
		super();
	}

	/**
	 * Returns the URL to the homepage.
	 * 
	 * @return java.lang.String
	 */
	public static String getHomePageURL() {
		try {
			// get the default home page
			URL url = WebBrowserUIPlugin.getInstance().getBundle().getEntry(
					"home/home.html");
			url = Platform.resolve(url);
			return url.toExternalForm();
		} catch (Exception e) {
			return "http://www.eclipse.org";
		}
	}

	/**
	 * Returns the preference store.
	 * 
	 * @return org.eclipse.jface.preference.IPreferenceStore
	 */
	protected static IPreferenceStore getPreferenceStore() {
		return WebBrowserUIPlugin.getInstance().getPreferenceStore();
	}

	/**
	 * Returns the Web browser history list.
	 * 
	 * @return java.util.List
	 */
	public static List getInternalWebBrowserHistory() {
		String temp = getPreferenceStore().getString(
				PREF_INTERNAL_WEB_BROWSER_HISTORY);
		StringTokenizer st = new StringTokenizer(temp, "|*|");
		List l = new ArrayList();
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			l.add(s);
		}
		return l;
	}

	/**
	 * Returns the Web browser favorites.
	 * 
	 * @return java.util.List
	 */
	public static List getInternalWebBrowserFavorites() {
		String temp = getPreferenceStore().getString(
				PREF_INTERNAL_WEB_BROWSER_FAVORITES);
		StringTokenizer st = new StringTokenizer(temp, "|*|");
		List l = new ArrayList();
		try {
			while (st.hasMoreTokens()) {
				l.add(new Favorite(st.nextToken(), st.nextToken()));
			}
		} catch (Exception e) {
			// ignore
		}
		return l;
	}

	/**
	 * Initialize the default preferences.
	 */
	public static void initializeDefaultPreferences() {
		IPreferenceStore store = getPreferenceStore();

		String temp = store.getString(PREF_INTERNAL_WEB_BROWSER_OLD_FAVORITES);
		StringTokenizer st = new StringTokenizer(temp, "|*|");
		List def = new ArrayList();
		try {
			while (st.hasMoreTokens()) {
				def.add(new Favorite(st.nextToken(), st.nextToken()));
			}
		} catch (Exception e) {
			// ignore
		}
		List list = getInternalWebBrowserFavorites();
		Iterator iterator = WebBrowserUtil.getUnlockedFavorites().iterator();
		while (iterator.hasNext()) {
			Favorite f = (Favorite) iterator.next();
			if (!def.contains(f))
				list.add(f);
		}
		setInternalWebBrowserFavorites(list);

		StringBuffer sb = new StringBuffer();
		iterator = WebBrowserUtil.getUnlockedFavorites().iterator();
		while (iterator.hasNext()) {
			Favorite f = (Favorite) iterator.next();
			sb.append(f.getName());
			sb.append("|*|");
			sb.append(f.getURL());
			sb.append("|*|");
		}
		store.setValue(PREF_INTERNAL_WEB_BROWSER_OLD_FAVORITES, sb.toString());
		WebBrowserUIPlugin.getInstance().savePluginPreferences();
	}

	/**
	 * Sets the Web browser history.
	 * 
	 * @param java.util.List
	 */
	public static void setInternalWebBrowserHistory(List list) {
		StringBuffer sb = new StringBuffer();
		if (list != null) {
			Iterator iterator = list.iterator();
			while (iterator.hasNext()) {
				String s = (String) iterator.next();
				sb.append(s);
				sb.append("|*|");
			}
		}
		getPreferenceStore().setValue(PREF_INTERNAL_WEB_BROWSER_HISTORY,
				sb.toString());
		WebBrowserUIPlugin.getInstance().savePluginPreferences();
	}

	/**
	 * Sets the Web browser favorites.
	 * 
	 * @param java.util.List
	 */
	public static void setInternalWebBrowserFavorites(List list) {
		StringBuffer sb = new StringBuffer();
		if (list != null) {
			Iterator iterator = list.iterator();
			while (iterator.hasNext()) {
				Favorite f = (Favorite) iterator.next();
				sb.append(f.getName());
				sb.append("|*|");
				sb.append(f.getURL());
				sb.append("|*|");
			}
		}
		getPreferenceStore().setValue(PREF_INTERNAL_WEB_BROWSER_FAVORITES,
				sb.toString());
		WebBrowserUIPlugin.getInstance().savePluginPreferences();
	}
}