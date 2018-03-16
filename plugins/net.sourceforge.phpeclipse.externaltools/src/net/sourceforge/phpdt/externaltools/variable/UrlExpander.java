package net.sourceforge.phpdt.externaltools.variable;

import net.sourceforge.phpeclipse.externaltools.ExternalToolsPlugin;
import net.sourceforge.phpeclipse.ui.WebUI;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Expands a variable into a localhost/documentRoot URL string
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 */
public class UrlExpander extends ResourceExpander { // implements
													// IVariableTextExpander {

	/**
	 * Create an instance
	 */
	public UrlExpander() {
		super();
	}

	/**
	 * Returns a string representation to a localhost/documentRoot URL for the
	 * given variable tag and value or <code>null</code>.
	 * 
	 * @see IVariableTextExpander#getText(String, String, ExpandVariableContext)
	 */
	public String getText(String varTag, String varValue,
			ExpandVariableContext context) {
		IPath path = getPath(varTag, varValue, context);
		if (path != null) {
			IPreferenceStore store = ExternalToolsPlugin.getDefault()
					.getPreferenceStore();
			String localhostURL = path.toString();
			String lowerCaseFileName = localhostURL.toLowerCase();
			String documentRoot = store.getString(WebUI.PHP_DOCUMENTROOT_PREF);
			documentRoot = documentRoot.replace('\\', '/');
			documentRoot = documentRoot.toLowerCase();

			if (lowerCaseFileName.startsWith(documentRoot)) {
				localhostURL = localhostURL.substring(documentRoot.length());
				localhostURL = store.getString(WebUI.PHP_LOCALHOST_PREF)
						+ localhostURL;
			}
			return localhostURL;
		}
		return "<no file selected>";
	}

}
