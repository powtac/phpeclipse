/**********************************************************************
 Copyright (c) 2000, 2002 IBM Corp. and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html

 Contributors:
 IBM Corporation - Initial implementation
 www.phpeclipse.de
 **********************************************************************/
package net.sourceforge.phpeclipse.phpeditor.util;

import net.sourceforge.phpdt.internal.compiler.parser.Scanner;

import org.eclipse.jface.text.rules.IWordDetector;

/**
 * A PHP aware variable detector (i.e. a PHP identifier starting with a '$'
 * character).
 */
public class PHPVariableDetector implements IWordDetector {

	/*
	 * (non-Javadoc) Method declared on IWordDetector.
	 */
	public boolean isWordPart(char character) {
		return Scanner.isPHPIdentifierPart(character);
	}

	/*
	 * (non-Javadoc) Method declared on IWordDetector.
	 */
	public boolean isWordStart(char character) {
		return character == '$';
	}
}
