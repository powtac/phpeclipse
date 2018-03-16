/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.phpdt.corext.refactoring.nls;

import org.eclipse.jface.text.Region;
import org.eclipse.jface.util.Assert;

public class NLSElement {

	public static final String TAG_PREFIX = "//$NON-NLS-"; //$NON-NLS-1$

	public static final int TAG_PREFIX_LENGTH = TAG_PREFIX.length();

	public static final String TAG_POSTFIX = "$"; //$NON-NLS-1$

	public static final int TAG_POSTFIX_LENGTH = TAG_POSTFIX.length();

	/** The original string denoted by the position */
	private String fValue;

	/** The position of the original string */
	private Region fPosition;

	/** Position of the // $NON_NLS_*$ tag */
	private Region fTagPosition;

	/** Index of the Element in an NLSLine */
	private int fIndex;

	/**
	 * Creates a new NLS element for the given string and position.
	 */
	public NLSElement(String value, int start, int length, int index) {
		fValue = value;
		fIndex = index;
		Assert.isNotNull(fValue);
		fPosition = new Region(start, length);
	}

	/**
	 * Returns the position of the string to be NLSed.
	 * 
	 * @return Returns the position of the string to be NLSed
	 */
	public Region getPosition() {
		return fPosition;
	}

	/**
	 * Returns the actual string value.
	 * 
	 * @return the actual string value
	 */
	public String getValue() {
		return fValue;
	}

	/**
	 * Sets the actual string value.
	 */
	public void setValue(String value) {
		fValue = value;
	}

	/**
	 * Sets the tag position if one is associated with the NLS element.
	 */
	public void setTagPosition(int start, int length) {
		fTagPosition = new Region(start, length);
	}

	/**
	 * Returns the tag position for this element. The method can return
	 * <code>null</code>. In this case no tag has been found for this NLS
	 * element.
	 */
	public Region getTagPosition() {
		return fTagPosition;
	}

	/**
	 * Returns <code>true</code> if the NLS element has an assicated
	 * $NON-NLS-*$ tag. Otherwise <code>false</code> is returned.
	 */
	public boolean hasTag() {
		return fTagPosition != null && fTagPosition.getLength() > 0;
	}

	public static String createTagText(int index) {
		return TAG_PREFIX + index + TAG_POSTFIX;
	}

	public String getTagText() {
		return TAG_PREFIX + (fIndex + 1) + TAG_POSTFIX;
	}

	/*
	 * (Non-Javadoc) Method declared in Object. only for debugging
	 */
	public String toString() {
		return fPosition + ": " + fValue + "    Tag position: " + //$NON-NLS-2$ //$NON-NLS-1$
				(hasTag() ? fTagPosition.toString() : "no tag found"); //$NON-NLS-1$
	}
}
