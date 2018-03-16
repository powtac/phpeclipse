/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

package net.sourceforge.phpdt.ui.text;

import java.util.List;

import net.sourceforge.phpdt.internal.ui.text.AbstractJavaScanner;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * 
 */
public final class SingleTokenPHPScanner extends AbstractJavaScanner {

	private String[] fProperty;

	public SingleTokenPHPScanner(IColorManager manager, IPreferenceStore store,
			String property) {
		super(manager, store);
		fProperty = new String[] { property };
		initialize();
	}

	/*
	 * @see AbstractJavaScanner#getTokenProperties()
	 */
	protected String[] getTokenProperties() {
		return fProperty;
	}

	/*
	 * @see AbstractJavaScanner#createRules()
	 */
	protected List createRules() {
		setDefaultReturnToken(getToken(fProperty[0]));
		return null;
	}
}
