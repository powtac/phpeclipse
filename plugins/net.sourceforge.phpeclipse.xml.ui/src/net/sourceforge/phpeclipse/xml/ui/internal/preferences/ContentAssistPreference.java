package net.sourceforge.phpeclipse.xml.ui.internal.preferences;

import net.sourceforge.phpeclipse.ui.PreferenceConstants;
import net.sourceforge.phpeclipse.ui.templates.template.BasicCompletionProcessor;
import net.sourceforge.phpeclipse.xml.ui.internal.text.XMLPartitionScanner;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;

public class ContentAssistPreference {
	/** Preference key for html content assist auto activation triggers */
	private final static String AUTOACTIVATION_TRIGGERS_HTML = PreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS_HTML;

	/** Preference key for alphabetic ordering of proposals */
	private final static String ORDER_PROPOSALS = PreferenceConstants.CODEASSIST_ORDER_PROPOSALS;

	private static BasicCompletionProcessor getHTMLProcessor(
			ContentAssistant assistant) {
		IContentAssistProcessor p = assistant
				.getContentAssistProcessor(XMLPartitionScanner.XML_TAG);
		if (p instanceof BasicCompletionProcessor)
			return (BasicCompletionProcessor) p;
		return null;
	}

	private static void configureHTMLProcessor(ContentAssistant assistant,
			IPreferenceStore store) {
		BasicCompletionProcessor hcp = getHTMLProcessor(assistant);
		if (hcp == null)
			return;

		String triggers = store.getString(AUTOACTIVATION_TRIGGERS_HTML);
		if (triggers != null)
			hcp.setCompletionProposalAutoActivationCharacters(triggers
					.toCharArray());

		boolean enabled;
		// boolean enabled = store.getBoolean(CASE_SENSITIVITY);
		// jdcp.restrictProposalsToMatchingCases(enabled);

		enabled = store.getBoolean(ORDER_PROPOSALS);
		// hcp.orderProposalsAlphabetically(enabled);
	}

	private static void changeHTMLProcessor(ContentAssistant assistant,
			IPreferenceStore store, String key) {
		BasicCompletionProcessor jdcp = getHTMLProcessor(assistant);
		if (jdcp == null)
			return;

		if (AUTOACTIVATION_TRIGGERS_HTML.equals(key)) {
			String triggers = store.getString(AUTOACTIVATION_TRIGGERS_HTML);
			if (triggers != null)
				jdcp.setCompletionProposalAutoActivationCharacters(triggers
						.toCharArray());
			// } else if (CASE_SENSITIVITY.equals(key)) {
			// boolean enabled = store.getBoolean(CASE_SENSITIVITY);
			// jdcp.restrictProposalsToMatchingCases(enabled);
		} else if (ORDER_PROPOSALS.equals(key)) {
			boolean enable = store.getBoolean(ORDER_PROPOSALS);
			// jdcp.orderProposalsAlphabetically(enable);
		}
	}
}
