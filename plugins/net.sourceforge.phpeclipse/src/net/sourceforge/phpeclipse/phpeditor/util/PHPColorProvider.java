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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Manager for colors used in the Java editor
 */
public class PHPColorProvider {

	public static final RGB MULTI_LINE_COMMENT = new RGB(63, 127, 95);

	public static final RGB SINGLE_LINE_COMMENT = new RGB(63, 127, 95);

	public static final RGB TAG = new RGB(255, 0, 128);

	public static final RGB KEYWORD = new RGB(127, 0, 85);

	public static final RGB VARIABLE = new RGB(127, 159, 191);

	public static final RGB FUNCTION_NAME = new RGB(127, 127, 159);

	public static final RGB STRING_DQ = new RGB(42, 0, 255);

	public static final RGB STRING_SQ = new RGB(42, 0, 255);

	public static final RGB DEFAULT = new RGB(0, 0, 0);

	public static final RGB TYPE = new RGB(127, 0, 85);

	public static final RGB CONSTANT = new RGB(127, 0, 85);

	public static final RGB BACKGROUND = new RGB(255, 255, 255);

	// public static final RGB LINKED_POSITION_COLOR = new RGB(0, 0, 0);

	// public static final RGB LINE_NUMBER_COLOR = new RGB(0, 0, 0);
	// public static final RGB BACKGROUND_COLOR = new RGB(255, 255, 255);

	public static final RGB PHPDOC_TAG = new RGB(63, 127, 95);

	public static final RGB PHPDOC_LINK = new RGB(63, 63, 191);

	public static final RGB PHPDOC_DEFAULT = new RGB(63, 95, 191);

	public static final RGB PHPDOC_KEYWORD = new RGB(127, 159, 191);

	protected Map fColorTable = new HashMap(10);

	/**
	 * Release all of the color resources held onto by the receiver.
	 */
	public void dispose() {
		Iterator e = fColorTable.values().iterator();
		while (e.hasNext())
			((Color) e.next()).dispose();
	}

	/**
	 * Return the Color that is stored in the Color table as rgb.
	 */
	public Color getColor(RGB rgb) {
		Color color = (Color) fColorTable.get(rgb);
		if (color == null) {
			color = new Color(Display.getCurrent(), rgb);
			fColorTable.put(rgb, color);
		}
		return color;
	}
}
