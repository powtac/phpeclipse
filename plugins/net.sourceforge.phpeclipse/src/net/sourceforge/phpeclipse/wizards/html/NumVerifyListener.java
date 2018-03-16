/*
 * $Id: NumVerifyListener.java,v 1.3 2006-10-21 23:18:43 pombredanne Exp $
 * Copyright Narushima Hironori. All rights reserved.
 */
package net.sourceforge.phpeclipse.wizards.html;

import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;

public class NumVerifyListener implements VerifyListener {

	Pattern numPattern = Pattern.compile("^\\d+$");

	public void verifyText(VerifyEvent ev) {
		ev.doit = numPattern.matcher(ev.text).matches()
				|| ev.keyCode == SWT.DEL || ev.character == SWT.BS;
	}
}
