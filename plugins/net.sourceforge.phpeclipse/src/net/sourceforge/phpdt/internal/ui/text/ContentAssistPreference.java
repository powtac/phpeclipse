/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package net.sourceforge.phpdt.internal.ui.text;

import net.sourceforge.phpdt.internal.ui.text.phpdoc.PHPDocCompletionProcessor;
import net.sourceforge.phpdt.ui.PreferenceConstants;
import net.sourceforge.phpdt.ui.text.IColorManager;
import net.sourceforge.phpdt.ui.text.JavaTextTools;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;
import net.sourceforge.phpeclipse.phpeditor.php.HTMLCompletionProcessor;
import net.sourceforge.phpeclipse.phpeditor.php.PHPCompletionProcessor;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

public class ContentAssistPreference {

	/** Preference key for content assist auto activation */
	private final static String AUTOACTIVATION = PreferenceConstants.CODEASSIST_AUTOACTIVATION;

	/** Preference key for content assist auto activation delay */
	private final static String AUTOACTIVATION_DELAY = PreferenceConstants.CODEASSIST_AUTOACTIVATION_DELAY;

	/** Preference key for content assist proposal color */
	private final static String PROPOSALS_FOREGROUND = PreferenceConstants.CODEASSIST_PROPOSALS_FOREGROUND;

	/** Preference key for content assist proposal color */
	private final static String PROPOSALS_BACKGROUND = PreferenceConstants.CODEASSIST_PROPOSALS_BACKGROUND;

	/** Preference key for content assist parameters color */
	private final static String PARAMETERS_FOREGROUND = PreferenceConstants.CODEASSIST_PARAMETERS_FOREGROUND;

	/** Preference key for content assist parameters color */
	private final static String PARAMETERS_BACKGROUND = PreferenceConstants.CODEASSIST_PARAMETERS_BACKGROUND;

	/** Preference key for content assist completion replacement color */
	private final static String COMPLETION_REPLACEMENT_FOREGROUND = PreferenceConstants.CODEASSIST_REPLACEMENT_FOREGROUND;

	/** Preference key for content assist completion replacement color */
	private final static String COMPLETION_REPLACEMENT_BACKGROUND = PreferenceConstants.CODEASSIST_REPLACEMENT_BACKGROUND;

	/** Preference key for content assist auto insert */
	private final static String AUTOINSERT = PreferenceConstants.CODEASSIST_AUTOINSERT;

	/** Preference key for php content assist auto activation triggers */
	private final static String AUTOACTIVATION_TRIGGERS_JAVA = PreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS_JAVA;

	/** Preference key for phpdoc content assist auto activation triggers */
	private final static String AUTOACTIVATION_TRIGGERS_JAVADOC = PreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS_JAVADOC;

	/** Preference key for html content assist auto activation triggers */
	private final static String AUTOACTIVATION_TRIGGERS_HTML = PreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS_HTML;

	/** Preference key for visibility of proposals */
	private final static String SHOW_VISIBLE_PROPOSALS = PreferenceConstants.CODEASSIST_SHOW_VISIBLE_PROPOSALS;

	/** Preference key for alphabetic ordering of proposals */
	private final static String ORDER_PROPOSALS = PreferenceConstants.CODEASSIST_ORDER_PROPOSALS;

	/** Preference key for case sensitivity of propsals */
	private final static String CASE_SENSITIVITY = PreferenceConstants.CODEASSIST_CASE_SENSITIVITY;

	/** Preference key for adding imports on code assist */
	private final static String ADD_IMPORT = PreferenceConstants.CODEASSIST_ADDIMPORT;

	/** Preference key for inserting content assist */
	private static final String INSERT_COMPLETION = PreferenceConstants.CODEASSIST_INSERT_COMPLETION;

	/** Preference key for filling argument names on method completion */
	private static final String FILL_METHOD_ARGUMENTS = PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES;

	/** Preference key for guessing argument names on method completion */
	private static final String GUESS_METHOD_ARGUMENTS = PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS;

	private static Color getColor(IPreferenceStore store, String key,
			IColorManager manager) {
		RGB rgb = PreferenceConverter.getColor(store, key);
		return manager.getColor(rgb);
	}

	private static Color getColor(IPreferenceStore store, String key) {
		JavaTextTools textTools = PHPeclipsePlugin.getDefault()
				.getJavaTextTools();
		return getColor(store, key, textTools.getColorManager());
	}

	private static PHPCompletionProcessor getJavaProcessor(
			ContentAssistant assistant) {
		IContentAssistProcessor p = assistant
				.getContentAssistProcessor(IPHPPartitions.PHP_PARTITIONING);
		if (p instanceof PHPCompletionProcessor)
			return (PHPCompletionProcessor) p;
		return null;
	}

	private static PHPDocCompletionProcessor getJavaDocProcessor(
			ContentAssistant assistant) {
		IContentAssistProcessor p = assistant
				.getContentAssistProcessor(IPHPPartitions.PHP_PHPDOC_COMMENT);
		if (p instanceof PHPDocCompletionProcessor)
			return (PHPDocCompletionProcessor) p;
		return null;
	}

	private static HTMLCompletionProcessor getHTMLProcessor(
			ContentAssistant assistant) {
		IContentAssistProcessor p = assistant
				.getContentAssistProcessor(IPHPPartitions.HTML);
		if (p instanceof HTMLCompletionProcessor)
			return (HTMLCompletionProcessor) p;
		return null;
	}

	private static void configureJavaProcessor(ContentAssistant assistant,
			IPreferenceStore store) {
		PHPCompletionProcessor pcp = getJavaProcessor(assistant);
		if (pcp == null)
			return;

		String triggers = store.getString(AUTOACTIVATION_TRIGGERS_JAVA);
		if (triggers != null)
			pcp.setCompletionProposalAutoActivationCharacters(triggers
					.toCharArray());
		boolean enabled;
		// boolean enabled= store.getBoolean(SHOW_VISIBLE_PROPOSALS);
		// jcp.restrictProposalsToVisibility(enabled);
		//		
		// enabled= store.getBoolean(CASE_SENSITIVITY);
		// jcp.restrictProposalsToMatchingCases(enabled);
		//		
		enabled = store.getBoolean(ORDER_PROPOSALS);
		pcp.orderProposalsAlphabetically(enabled);
		//		
		// enabled= store.getBoolean(ADD_IMPORT);
		// jcp.allowAddingImports(enabled);
	}

	private static void configureJavaDocProcessor(ContentAssistant assistant,
			IPreferenceStore store) {
		PHPDocCompletionProcessor pdcp = getJavaDocProcessor(assistant);
		if (pdcp == null)
			return;

		String triggers = store.getString(AUTOACTIVATION_TRIGGERS_JAVADOC);
		if (triggers != null)
			pdcp.setCompletionProposalAutoActivationCharacters(triggers
					.toCharArray());

		boolean enabled = store.getBoolean(CASE_SENSITIVITY);
		pdcp.restrictProposalsToMatchingCases(enabled);

		enabled = store.getBoolean(ORDER_PROPOSALS);
		pdcp.orderProposalsAlphabetically(enabled);
	}

	private static void configureHTMLProcessor(ContentAssistant assistant,
			IPreferenceStore store) {
		HTMLCompletionProcessor hcp = getHTMLProcessor(assistant);
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
		hcp.orderProposalsAlphabetically(enabled);
	}

	/**
	 * Configure the given content assistant from the given store.
	 */
	public static void configure(ContentAssistant assistant,
			IPreferenceStore store) {

		JavaTextTools textTools = PHPeclipsePlugin.getDefault()
				.getJavaTextTools();
		IColorManager manager = textTools.getColorManager();

		boolean enabled = store.getBoolean(AUTOACTIVATION);
		assistant.enableAutoActivation(enabled);

		int delay = store.getInt(AUTOACTIVATION_DELAY);
		assistant.setAutoActivationDelay(delay);

		Color c = getColor(store, PROPOSALS_FOREGROUND, manager);
		assistant.setProposalSelectorForeground(c);

		c = getColor(store, PROPOSALS_BACKGROUND, manager);
		assistant.setProposalSelectorBackground(c);

		c = getColor(store, PARAMETERS_FOREGROUND, manager);
		assistant.setContextInformationPopupForeground(c);
		assistant.setContextSelectorForeground(c);

		c = getColor(store, PARAMETERS_BACKGROUND, manager);
		assistant.setContextInformationPopupBackground(c);
		assistant.setContextSelectorBackground(c);

		enabled = store.getBoolean(AUTOINSERT);
		assistant.enableAutoInsert(enabled);

		configureJavaProcessor(assistant, store);
		configureJavaDocProcessor(assistant, store);
		configureHTMLProcessor(assistant, store);
	}

	private static void changeJavaProcessor(ContentAssistant assistant,
			IPreferenceStore store, String key) {
		PHPCompletionProcessor jcp = getJavaProcessor(assistant);
		if (jcp == null)
			return;

		if (AUTOACTIVATION_TRIGGERS_JAVA.equals(key)) {
			String triggers = store.getString(AUTOACTIVATION_TRIGGERS_JAVA);
			if (triggers != null)
				jcp.setCompletionProposalAutoActivationCharacters(triggers
						.toCharArray());
		}
		// else if (SHOW_VISIBLE_PROPOSALS.equals(key)) {
		// boolean enabled= store.getBoolean(SHOW_VISIBLE_PROPOSALS);
		// jcp.restrictProposalsToVisibility(enabled);
		// } else if (CASE_SENSITIVITY.equals(key)) {
		// boolean enabled= store.getBoolean(CASE_SENSITIVITY);
		// jcp.restrictProposalsToMatchingCases(enabled); }
		else if (ORDER_PROPOSALS.equals(key)) {
			boolean enable = store.getBoolean(ORDER_PROPOSALS);
			jcp.orderProposalsAlphabetically(enable);
			// } else if (ADD_IMPORT.equals(key)) {
			// boolean enabled= store.getBoolean(ADD_IMPORT);
			// jcp.allowAddingImports(enabled);
		}
	}

	private static void changeJavaDocProcessor(ContentAssistant assistant,
			IPreferenceStore store, String key) {
		PHPDocCompletionProcessor jdcp = getJavaDocProcessor(assistant);
		if (jdcp == null)
			return;

		if (AUTOACTIVATION_TRIGGERS_JAVADOC.equals(key)) {
			String triggers = store.getString(AUTOACTIVATION_TRIGGERS_JAVADOC);
			if (triggers != null)
				jdcp.setCompletionProposalAutoActivationCharacters(triggers
						.toCharArray());
		} else if (CASE_SENSITIVITY.equals(key)) {
			boolean enabled = store.getBoolean(CASE_SENSITIVITY);
			jdcp.restrictProposalsToMatchingCases(enabled);
		} else if (ORDER_PROPOSALS.equals(key)) {
			boolean enable = store.getBoolean(ORDER_PROPOSALS);
			jdcp.orderProposalsAlphabetically(enable);
		}
	}

	private static void changeHTMLProcessor(ContentAssistant assistant,
			IPreferenceStore store, String key) {
		HTMLCompletionProcessor jdcp = getHTMLProcessor(assistant);
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
			jdcp.orderProposalsAlphabetically(enable);
		}
	}

	/**
	 * Changes the configuration of the given content assistant according to the
	 * given property change event and the given preference store.
	 */
	public static void changeConfiguration(ContentAssistant assistant,
			IPreferenceStore store, PropertyChangeEvent event) {

		String p = event.getProperty();

		if (AUTOACTIVATION.equals(p)) {
			boolean enabled = store.getBoolean(AUTOACTIVATION);
			assistant.enableAutoActivation(enabled);
		} else if (AUTOACTIVATION_DELAY.equals(p)) {
			int delay = store.getInt(AUTOACTIVATION_DELAY);
			assistant.setAutoActivationDelay(delay);
		} else if (PROPOSALS_FOREGROUND.equals(p)) {
			Color c = getColor(store, PROPOSALS_FOREGROUND);
			assistant.setProposalSelectorForeground(c);
		} else if (PROPOSALS_BACKGROUND.equals(p)) {
			Color c = getColor(store, PROPOSALS_BACKGROUND);
			assistant.setProposalSelectorBackground(c);
		} else if (PARAMETERS_FOREGROUND.equals(p)) {
			Color c = getColor(store, PARAMETERS_FOREGROUND);
			assistant.setContextInformationPopupForeground(c);
			assistant.setContextSelectorForeground(c);
		} else if (PARAMETERS_BACKGROUND.equals(p)) {
			Color c = getColor(store, PARAMETERS_BACKGROUND);
			assistant.setContextInformationPopupBackground(c);
			assistant.setContextSelectorBackground(c);
		} else if (AUTOINSERT.equals(p)) {
			boolean enabled = store.getBoolean(AUTOINSERT);
			assistant.enableAutoInsert(enabled);
		}

		changeJavaProcessor(assistant, store, p);
		changeJavaDocProcessor(assistant, store, p);
		changeHTMLProcessor(assistant, store, p);
	}

	public static boolean fillArgumentsOnMethodCompletion(IPreferenceStore store) {
		return store.getBoolean(FILL_METHOD_ARGUMENTS);
	}
}
