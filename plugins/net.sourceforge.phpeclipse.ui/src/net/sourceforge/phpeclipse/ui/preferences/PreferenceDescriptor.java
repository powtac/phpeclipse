/*
 * Copyright (c) 2002-2004 Widespace, OU and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://solareclipse.sourceforge.net/legal/cpl-v10.html
 * 
 * Contributors:
 *     Igor Malinin - initial contribution
 * 
 * $Id: PreferenceDescriptor.java,v 1.2 2006-10-21 23:13:54 pombredanne Exp $
 */

package net.sourceforge.phpeclipse.ui.preferences;

/**
 * Preference descriptor.
 * 
 * @author Igor Malinin
 */
public final class PreferenceDescriptor {
	public static final Type BOOLEAN = new Type();

	public static final Type DOUBLE = new Type();

	public static final Type FLOAT = new Type();

	public static final Type INT = new Type();

	public static final Type LONG = new Type();

	public static final Type STRING = new Type();

	public static final class Type {
		Type() {
		}
	}

	public final Type type;

	public final String key;

	public PreferenceDescriptor(Type type, String key) {
		this.type = type;
		this.key = key;
	}
}
