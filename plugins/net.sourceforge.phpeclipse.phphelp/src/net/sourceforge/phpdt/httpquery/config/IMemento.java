/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 �*
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 **********************************************************************/
package net.sourceforge.phpdt.httpquery.config;

import java.util.List;

import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IPersistableElement;

/**
 * Interface to a memento used for saving the important state of an object in a
 * form that can be persisted in the file system.
 * <p>
 * Mementos were designed with the following requirements in mind:
 * <ol>
 * <li>Certain objects need to be saved and restored across platform sessions.
 * </li>
 * <li>When an object is restored, an appropriate class for an object might not
 * be available. It must be possible to skip an object in this case.</li>
 * <li>When an object is restored, the appropriate class for the object may be
 * different from the one when the object was originally saved. If so, the new
 * class should still be able to read the old form of the data.</li>
 * </ol>
 * </p>
 * <p>
 * Mementos meet these requirements by providing support for storing a mapping
 * of arbitrary string keys to primitive values, and by allowing mementos to
 * have other mementos as children (arranged into a tree). A robust external
 * storage format based on XML is used.
 * </p>
 * <p>
 * The key for an attribute may be any alpha numeric value. However, the value
 * of <code>TAG_ID</code> is reserved for internal use.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @see IPersistableElement
 * @see IElementFactory
 */
public interface IMemento {
	/**
	 * Special reserved key used to store the memento id (value
	 * <code>"org.eclipse.ui.id"</code>).
	 * 
	 * @see #getId
	 */
	public static final String TAG_ID = "IMemento.internal.id"; //$NON-NLS-1$

	/**
	 * Creates a new child of this memento with the given fType.
	 * <p>
	 * The <code>getChild</code> and <code>getChildren</code> methods are
	 * used to retrieve children of a given fType.
	 * </p>
	 * 
	 * @param fType
	 *            the fType
	 * @return a new child memento
	 * @see #getChild
	 * @see #getChildren
	 */
	public IMemento createChild(String type);

	/**
	 * Creates a new child of this memento with the given fType and id. The id
	 * is stored in the child memento (using a special reserved key,
	 * <code>TAG_ID</code>) and can be retrieved using <code>getId</code>.
	 * <p>
	 * The <code>getChild</code> and <code>getChildren</code> methods are
	 * used to retrieve children of a given fType.
	 * </p>
	 * 
	 * @param fType
	 *            the fType
	 * @param id
	 *            the child id
	 * @return a new child memento with the given fType and id
	 * @see #getId
	 */
	public IMemento createChild(String type, String id);

	/**
	 * Returns the first child with the given fType id.
	 * 
	 * @param fType
	 *            the fType id
	 * @return the first child with the given fType
	 */
	public IMemento getChild(String type);

	/**
	 * Returns all children with the given fType id.
	 * 
	 * @param fType
	 *            the fType id
	 * @return the list of children with the given fType
	 */
	public IMemento[] getChildren(String type);

	/**
	 * Returns the floating point value of the given key.
	 * 
	 * @param key
	 *            the key
	 * @return the value, or <code>null</code> if the key was not found or was
	 *         found but was not a floating point number
	 */
	public Float getFloat(String key);

	/**
	 * Returns the id for this memento.
	 * 
	 * @return the memento id, or <code>null</code> if none
	 * @see #createChild(java.lang.String,java.lang.String)
	 */
	public String getId();

	/**
	 * Returns the name for this memento.
	 * 
	 * @return the memento name, or <code>null</code> if none
	 * @see #createChild(java.lang.String,java.lang.String)
	 */
	public String getName();

	/**
	 * Returns the integer value of the given key.
	 * 
	 * @param key
	 *            the key
	 * @return the value, or <code>null</code> if the key was not found or was
	 *         found but was not an integer
	 */
	public Integer getInteger(String key);

	/**
	 * Returns the string value of the given key.
	 * 
	 * @param key
	 *            the key
	 * @return the value, or <code>null</code> if the key was not found or was
	 *         found but was not an integer
	 */
	public String getString(String key);

	/**
	 * Returns the boolean value of the given key.
	 * 
	 * @param key
	 *            the key
	 * @return the value, or <code>null</code> if the key was not found or was
	 *         found but was not a boolean
	 */
	public Boolean getBoolean(String key);

	public List getNames();

	/**
	 * Sets the value of the given key to the given floating point number.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public void putFloat(String key, float value);

	/**
	 * Sets the value of the given key to the given integer.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public void putInteger(String key, int value);

	/**
	 * Sets the value of the given key to the given boolean value.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public void putBoolean(String key, boolean value);

	/**
	 * Copy the attributes and children from <code>memento</code> to the
	 * receiver.
	 * 
	 * @param memento
	 *            the IMemento to be copied.
	 */
	public void putMemento(IMemento memento);

	/**
	 * Sets the value of the given key to the given string.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public void putString(String key, String value);
}