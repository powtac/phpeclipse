package net.sourceforge.phpeclipse.ui;

import org.eclipse.jface.preference.IPreferenceStore;

public class PreferenceConstants {
	/**
	 * A named preference that holds the characters that auto activate code
	 * assist in XML/HTML.
	 * <p>
	 * Value is of type <code>Sring</code>. All characters that trigger auto
	 * code assist in XML/HTML.
	 * </p>
	 */
	public final static String CODEASSIST_AUTOACTIVATION_TRIGGERS_HTML = "content_assist_autoactivation_triggers_html"; //$NON-NLS-1$

	/**
	 * A named preference that defines if code assist proposals are sorted in
	 * alphabetical order.
	 * <p>
	 * Value is of type <code>Boolean</code>. If <code>true</code> that are
	 * sorted in alphabetical order. If <code>false</code> that are unsorted.
	 * </p>
	 */
	public final static String CODEASSIST_ORDER_PROPOSALS = "content_assist_order_proposals"; //$NON-NLS-1$

	public static void initializeDefaultValues(IPreferenceStore store) {
		store.setDefault(
				PreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS_HTML,
				"<&#"); //$NON-NLS-1$
		store.setDefault(PreferenceConstants.CODEASSIST_ORDER_PROPOSALS, false);

	}
}