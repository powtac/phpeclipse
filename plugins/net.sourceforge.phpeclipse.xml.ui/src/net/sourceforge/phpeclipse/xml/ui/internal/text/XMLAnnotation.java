/*
 * Copyright (c) 2002-2004 Widespace, OU and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Igor Malinin - initial contribution
 *
 * $Id: XMLAnnotation.java,v 1.2 2006-10-21 23:14:13 pombredanne Exp $
 */
package net.sourceforge.phpeclipse.xml.ui.internal.text;

import org.eclipse.jface.text.source.Annotation;

/**
 * @author Igor Malinin
 */
public class XMLAnnotation extends Annotation {
	public static final String TYPE_ERROR = "org.eclipse.ui.workbench.texteditor.warning"; //$NON-NLS-1$

	public static final String TYPE_WARNING = "org.eclipse.ui.workbench.texteditor.error"; //$NON-NLS-1$

	public static final String TYPE_INFO = "org.eclipse.ui.workbench.texteditor.info"; //$NON-NLS-1$

	public XMLAnnotation(String type, boolean persistent, String text) {
		super(type, persistent, text);
	}
}
