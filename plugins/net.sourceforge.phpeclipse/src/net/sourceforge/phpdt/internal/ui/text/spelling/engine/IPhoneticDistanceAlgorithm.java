/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package net.sourceforge.phpdt.internal.ui.text.spelling.engine;

/**
 * Interface of algorithms to compute the phonetic distance between two words.
 * 
 * @since 3.0
 */
public interface IPhoneticDistanceAlgorithm {

	/**
	 * Returns the non-negative phonetic distance between two words
	 * 
	 * @param from
	 *            The first word
	 * @param to
	 *            The second word
	 * @return The non-negative phonetic distance between the words.
	 */
	public int getDistance(String from, String to);
}
