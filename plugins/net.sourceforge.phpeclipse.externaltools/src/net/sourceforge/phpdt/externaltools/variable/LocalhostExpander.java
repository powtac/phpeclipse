package net.sourceforge.phpdt.externaltools.variable;

import net.sourceforge.phpeclipse.ui.WebUI;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Expands a variable into the predefined localhost.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 */
public class LocalhostExpander implements IVariableTextExpander {

	public String getText(String varTag, String varValue,
			ExpandVariableContext context) {
		final IPreferenceStore webUIStore = WebUI.getDefault()
				.getPreferenceStore();
		return webUIStore.getString(WebUI.PHP_LOCALHOST_PREF);
	}

}
