/*
 * $Id: HTMLUtilities.java,v 1.2 2006-10-21 23:18:43 pombredanne Exp $
 * Copyright Narushima Hironori. All rights reserved.
 */
package net.sourceforge.phpeclipse.wizards.html;

/**
 * 
 */
public class HTMLUtilities {

	final static String[] specialMarks = { "&", "&amp;", "<", "&lt;", ">",
			"&gt;", "\"", "&quot;", };

	public static String escape(String text) {
		for (int i = 0; i < specialMarks.length; i += 2) {
			text = text.replaceAll(specialMarks[i], specialMarks[i + 1]);
		}
		return text;
	}

	public static String unescape(String text) {
		for (int i = specialMarks.length - 1; i >= 0; i -= 2) {
			text = text.replaceAll(specialMarks[i], specialMarks[i - 1]);
		}
		return text;
	}

}
