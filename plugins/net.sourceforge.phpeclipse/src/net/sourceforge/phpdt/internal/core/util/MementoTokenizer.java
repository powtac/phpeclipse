/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.phpdt.internal.core.util;

import net.sourceforge.phpdt.core.compiler.CharOperation;
import net.sourceforge.phpdt.internal.core.JavaElement;

public class MementoTokenizer {
	private static final String COUNT = Character
			.toString(JavaElement.JEM_COUNT);

	private static final String JAVAPROJECT = Character
			.toString(JavaElement.JEM_JAVAPROJECT);

	private static final String PACKAGEFRAGMENTROOT = Character
			.toString(JavaElement.JEM_PACKAGEFRAGMENTROOT);

	private static final String PACKAGEFRAGMENT = Character
			.toString(JavaElement.JEM_PACKAGEFRAGMENT);

	private static final String FIELD = Character
			.toString(JavaElement.JEM_FIELD);

	private static final String METHOD = Character
			.toString(JavaElement.JEM_METHOD);

	private static final String INITIALIZER = Character
			.toString(JavaElement.JEM_INITIALIZER);

	private static final String COMPILATIONUNIT = Character
			.toString(JavaElement.JEM_COMPILATIONUNIT);

	// private static final String CLASSFILE =
	// Character.toString(JavaElement.JEM_CLASSFILE);
	private static final String TYPE = Character.toString(JavaElement.JEM_TYPE);

	private static final String PACKAGEDECLARATION = Character
			.toString(JavaElement.JEM_PACKAGEDECLARATION);

	private static final String IMPORTDECLARATION = Character
			.toString(JavaElement.JEM_IMPORTDECLARATION);

	private static final String LOCALVARIABLE = Character
			.toString(JavaElement.JEM_LOCALVARIABLE);

	private final char[] memento;

	private final int length;

	private int index = 0;

	public MementoTokenizer(String memento) {
		this.memento = memento.toCharArray();
		this.length = this.memento.length;
	}

	public boolean hasMoreTokens() {
		return this.index < this.length;
	}

	public String nextToken() {
		int start = this.index;
		StringBuffer buffer = null;
		switch (this.memento[this.index++]) {
		case JavaElement.JEM_ESCAPE:
			buffer = new StringBuffer();
			buffer.append(this.memento[this.index]);
			start = ++this.index;
			break;
		case JavaElement.JEM_COUNT:
			return COUNT;
		case JavaElement.JEM_JAVAPROJECT:
			return JAVAPROJECT;
		case JavaElement.JEM_PACKAGEFRAGMENTROOT:
			return PACKAGEFRAGMENTROOT;
		case JavaElement.JEM_PACKAGEFRAGMENT:
			return PACKAGEFRAGMENT;
		case JavaElement.JEM_FIELD:
			return FIELD;
		case JavaElement.JEM_METHOD:
			return METHOD;
		case JavaElement.JEM_INITIALIZER:
			return INITIALIZER;
		case JavaElement.JEM_COMPILATIONUNIT:
			return COMPILATIONUNIT;
			// case JavaElement.JEM_CLASSFILE:
			// return CLASSFILE;
		case JavaElement.JEM_TYPE:
			return TYPE;
		case JavaElement.JEM_PACKAGEDECLARATION:
			return PACKAGEDECLARATION;
		case JavaElement.JEM_IMPORTDECLARATION:
			return IMPORTDECLARATION;
		case JavaElement.JEM_LOCALVARIABLE:
			return LOCALVARIABLE;
		}
		loop: while (this.index < this.length) {
			switch (this.memento[this.index]) {
			case JavaElement.JEM_ESCAPE:
				if (buffer == null)
					buffer = new StringBuffer();
				buffer.append(CharOperation.subarray(this.memento, start,
						this.index));
				start = ++this.index;
				break;
			case JavaElement.JEM_COUNT:
			case JavaElement.JEM_JAVAPROJECT:
			case JavaElement.JEM_PACKAGEFRAGMENTROOT:
			case JavaElement.JEM_PACKAGEFRAGMENT:
			case JavaElement.JEM_FIELD:
			case JavaElement.JEM_METHOD:
			case JavaElement.JEM_INITIALIZER:
			case JavaElement.JEM_COMPILATIONUNIT:
				// case JavaElement.JEM_CLASSFILE:
			case JavaElement.JEM_TYPE:
			case JavaElement.JEM_PACKAGEDECLARATION:
			case JavaElement.JEM_IMPORTDECLARATION:
			case JavaElement.JEM_LOCALVARIABLE:
				break loop;
			}
			this.index++;
		}
		if (buffer != null) {
			buffer.append(CharOperation.subarray(this.memento, start,
					this.index));
			return buffer.toString();
		} else {
			return new String(CharOperation.subarray(this.memento, start,
					this.index));
		}
	}

}