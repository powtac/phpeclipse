package net.sourceforge.phpdt.externaltools.variable;

import org.eclipse.core.runtime.IPath;

/**
 * Expands a variable into the last opened PHP file
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 */
public class FileExpander extends ResourceExpander { // implements
														// IVariableTextExpander
														// {

	/**
	 * Create an instance
	 */
	public FileExpander() {
		super();
	}

	/**
	 * Returns a string representation of the path to a file or directory for
	 * the given variable tag and value or <code>null</code>.
	 * 
	 * @see IVariableTextExpander#getText(String, String, ExpandVariableContext)
	 */
	public String getText(String varTag, String varValue,
			ExpandVariableContext context) {
		IPath path = getPath(varTag, varValue, context);
		if (path != null) {
			return path.toString();
		}
		return "<no file selected>";
	}

}
