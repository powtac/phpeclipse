/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.phpeclipse.xdebug.core;

import java.util.HashMap;


/**
 * Local version of org.eclipse.jface.util.ListenerList (modified)s
 */
public class ListenerMap {
	/**
	 * The current number of listeners.
	 * Maintains invariant: 0 <= fSize <= listeners.length.
	 */
	private int fSize;

	/**
	 * The map of listeners.  Initially <code>null</code> but initialized
	 * to an array of size capacity the first time a listener is added.
	 * Maintains invariant: listeners != null if and only if fSize != 0
	 */
	private HashMap fListeners = null;
//	private HashMap<String,Object> fListeners = null;

	/**
	 * Creates a listener map with the given initial capacity.
	 *
	 * @param capacity the number of listeners which this list can initially accept 
	 *    without growing its internal representation; must be at least 1
	 */
	public ListenerMap(int capacity) {
		if (capacity < 1) {
			throw new IllegalArgumentException();
		}
		fListeners = new HashMap(capacity);
		fSize = 0;
	}

	/**
	 * Adds a listener to the Map.
	 * Overwrites an existing listener with the same IDE_Key.
	 *
	 * @param listener a listener
	 * @param ideKey IDE-key of the listener
	 */
	public synchronized void add(Object listener,String ideKey) {
		if (listener == null) {
			throw new IllegalArgumentException();
		}
		// check for duplicates using identity
		if (fListeners.get(ideKey) == null)
			fSize++;
		fListeners.put(ideKey,listener);
	}

	/**
	 * Returns a map containing all the registered listeners.
	 * The resulting map is unaffected by subsequent adds or removes.
	 * If there are no listeners registered, the result is an empty map.
	 * Use this method when notifying listeners, so that any modifications
	 * to the listener list during the notification will have no effect on the
	 * notification itself.
	 */
	public synchronized HashMap getListeners() {
		if (fSize == 0) {
			return null;
		}
		HashMap result = new HashMap(fListeners);
		return result;
	}
	
	/**
	 * Returns the listener associated with the ideKey.
	 * If there is no listener registered, the result is null.
	 * Use this method when notifying listeners, so that any modifications
	 * to the listener list during the notification will have no effect on the
	 * notification itself.
	 */
	public synchronized Object getListener(String ideKey) {
		return fListeners.get(ideKey);
	}

	/**
	 * Removes a listener from the list.
	 * Has no effect if an identical listener was not already registered.
	 *
	 * @param listener a listener
	 */
	public synchronized void remove(Object listener,String ideKey) {
		if (listener == null) {
			throw new IllegalArgumentException();
		}
		
		if (fListeners.get(ideKey) == listener) {
			fListeners.remove(ideKey);
			fSize--;
		}
	}

	/**
	 * Removes all the listeners from the list.
	 */
	public synchronized void removeAll() {
		fListeners.clear();
		fSize = 0;
	}

	/**
	 * Returns the number of registered listeners
	 *
	 * @return the number of registered listeners
	 */
	public int size() {
		return fSize;
	}
}