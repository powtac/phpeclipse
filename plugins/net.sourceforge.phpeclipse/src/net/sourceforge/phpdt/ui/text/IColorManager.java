/*******************************************************************************
 * Copyright (c) 2000, 2001 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package net.sourceforge.phpdt.ui.text;

import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 * Manages SWT color objects for given color keys and given <code>RGB</code>
 * objects. Until the <code>dispose</code> method is called, the same color
 * object is returned for equal keys and equal <code>RGB</code> values.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 * 
 * @see IJavaColorConstants
 */
public interface IColorManager extends ISharedTextColors {

	/**
	 * Returns a color object for the given key. The color objects are
	 * remembered internally; the same color object is returned for equal keys.
	 * 
	 * @param key
	 *            the color key
	 * @return the color object for the given key
	 */
	Color getColor(String key);

	/**
	 * Returns the color object for the value represented by the given
	 * <code>RGB</code> object.
	 * 
	 * @param rgb
	 *            the rgb color specification
	 * @return the color object for the given rgb value
	 */
	Color getColor(RGB rgb);

	/**
	 * Disposes all color objects remembered by this color manager.
	 */
	void dispose();
}
