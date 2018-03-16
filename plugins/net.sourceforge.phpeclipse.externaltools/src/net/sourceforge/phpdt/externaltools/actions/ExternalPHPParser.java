package net.sourceforge.phpdt.externaltools.actions;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Hashtable;

import net.sourceforge.phpdt.externaltools.util.StringUtil;
import net.sourceforge.phpeclipse.externaltools.ExternalToolsPlugin;
import net.sourceforge.phpeclipse.externaltools.PHPConsole;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.texteditor.MarkerUtilities;

/**
 * Calls the external parser and generates problem markers if necessary
 */
public class ExternalPHPParser {
	private final static String PROBLEM_ID = "net.sourceforge.phpeclipse.problem";

	// strings for external parser call
	private static final String PARSE_ERROR_STRING = "Parse error"; //$NON-NLS-1$

	private static final String PARSE_WARNING_STRING = "Warning"; //$NON-NLS-1$

	public static final int ERROR = 2;

	public static final int WARNING = 1;

	public static final int INFO = 0;

	public static final int TASK = 3;

	// TODO design error? Analyze why fileToParse must be static ???
	final protected IFile fFileToParse;

	public ExternalPHPParser(IFile file) {
		fFileToParse = file;
	}

	/**
	 * Call the php parse command ( php -l -f &lt;filename&gt; ) and create
	 * markers according to the external parser output.
	 * 
	 * @param file
	 *            the file that will be parsed
	 */
	public void phpExternalParse() {
		// IFile file = (IFile) resource;
		// final IPath path = file.getFullPath();
		final IPreferenceStore store = ExternalToolsPlugin.getDefault()
				.getPreferenceStore();
		final String filename = fFileToParse.getFullPath().toString();

		final String[] arguments = { filename };
		final MessageFormat form = new MessageFormat(store
				.getString(ExternalToolsPlugin.EXTERNAL_PARSER_PREF));
		final String command = form.format(arguments);

		final String parserResult = getParserOutput(command,
				"External parser: ");

		try {
			// parse the buffer to find the errors and warnings
			createMarkers(parserResult, fFileToParse);
		} catch (CoreException e) {
		}
	}

	/**
	 * Create markers according to the external parser output.
	 * 
	 * @param output
	 *            the external parser output
	 * @param file
	 *            the file that was parsed.
	 */
	protected void createMarkers(final String output, final IFile file)
			throws CoreException {
		// delete all markers
		file.deleteMarkers(PROBLEM_ID, false, 0);

		int indx = 0;
		int brIndx;
		boolean flag = true;
		while ((brIndx = output.indexOf("<br />", indx)) != -1) {
			// newer php error output (tested with 4.2.3)
			scanLine(output, file, indx, brIndx);
			indx = brIndx + 6;
			flag = false;
		}
		if (flag) {
			while ((brIndx = output.indexOf("<br>", indx)) != -1) {
				// older php error output (tested with 4.2.3)
				scanLine(output, file, indx, brIndx);
				indx = brIndx + 4;
			}
		}
	}

	private void scanLine(final String output, final IFile file,
			final int indx, final int brIndx) throws CoreException {
		String current;
		// String outLineNumberString; never used
		final StringBuffer lineNumberBuffer = new StringBuffer(10);
		char ch;
		current = output.substring(indx, brIndx);

		if (current.indexOf(PARSE_WARNING_STRING) != -1
				|| current.indexOf(PARSE_ERROR_STRING) != -1) {
			final int onLine = current.indexOf("on line <b>");
			if (onLine != -1) {
				lineNumberBuffer.delete(0, lineNumberBuffer.length());
				for (int i = onLine; i < current.length(); i++) {
					ch = current.charAt(i);
					if ('0' <= ch && '9' >= ch) {
						lineNumberBuffer.append(ch);
					}
				}

				final int lineNumber = Integer.parseInt(lineNumberBuffer
						.toString());

				final Hashtable attributes = new Hashtable();

				current = StringUtil.replaceAll(current, "\n", "");
				current = StringUtil.replaceAll(current, "<b>", "");
				current = StringUtil.replaceAll(current, "</b>", "");
				MarkerUtilities.setMessage(attributes, current);

				if (current.indexOf(PARSE_ERROR_STRING) != -1)
					attributes.put(IMarker.SEVERITY, new Integer(
							IMarker.SEVERITY_ERROR));
				else if (current.indexOf(PARSE_WARNING_STRING) != -1)
					attributes.put(IMarker.SEVERITY, new Integer(
							IMarker.SEVERITY_WARNING));
				else
					attributes.put(IMarker.SEVERITY, new Integer(
							IMarker.SEVERITY_INFO));
				MarkerUtilities.setLineNumber(attributes, lineNumber);
				MarkerUtilities.createMarker(file, attributes, PROBLEM_ID);
			}
		}
	}

	/**
	 * This will set a marker.
	 * 
	 * @param file
	 *            the file that generated the marker
	 * @param message
	 *            the message
	 * @param charStart
	 *            the starting character
	 * @param charEnd
	 *            the end character
	 * @param errorLevel
	 *            the error level ({@link ExternalPHPParser#ERROR},{@link ExternalPHPParser#INFO},{@link ExternalPHPParser#WARNING}),
	 *            {@link ExternalPHPParser#TASK})
	 * @throws CoreException
	 *             an exception throwed by the MarkerUtilities
	 */
	private void setMarker(final IFile file, final String message,
			final int charStart, final int charEnd, final int errorLevel)
			throws CoreException {
		if (file != null) {
			final Hashtable attributes = new Hashtable();
			MarkerUtilities.setMessage(attributes, message);
			switch (errorLevel) {
			case ERROR:
				attributes.put(IMarker.SEVERITY, new Integer(
						IMarker.SEVERITY_ERROR));
				break;
			case WARNING:
				attributes.put(IMarker.SEVERITY, new Integer(
						IMarker.SEVERITY_WARNING));
				break;
			case INFO:
				attributes.put(IMarker.SEVERITY, new Integer(
						IMarker.SEVERITY_INFO));
				break;
			case TASK:
				attributes.put(IMarker.SEVERITY, new Integer(IMarker.TASK));
				break;
			}
			MarkerUtilities.setCharStart(attributes, charStart);
			MarkerUtilities.setCharEnd(attributes, charEnd);
			MarkerUtilities.createMarker(file, attributes, PROBLEM_ID);
		}
	}

	/**
	 * This will set a marker.
	 * 
	 * @param file
	 *            the file that generated the marker
	 * @param message
	 *            the message
	 * @param line
	 *            the line number
	 * @param errorLevel
	 *            the error level ({@link ExternalPHPParser#ERROR},{@link ExternalPHPParser#INFO},{@link ExternalPHPParser#WARNING})
	 * @throws CoreException
	 *             an exception throwed by the MarkerUtilities
	 */
	private void setMarker(final IFile file, final String message,
			final int line, final int errorLevel, final String location)
			throws CoreException {
		if (file != null) {
			String markerKind = PROBLEM_ID;
			final Hashtable attributes = new Hashtable();
			MarkerUtilities.setMessage(attributes, message);
			switch (errorLevel) {
			case ERROR:
				attributes.put(IMarker.SEVERITY, new Integer(
						IMarker.SEVERITY_ERROR));
				break;
			case WARNING:
				attributes.put(IMarker.SEVERITY, new Integer(
						IMarker.SEVERITY_WARNING));
				break;
			case INFO:
				attributes.put(IMarker.SEVERITY, new Integer(
						IMarker.SEVERITY_INFO));
				break;
			case TASK:
				attributes.put(IMarker.SEVERITY, new Integer(
						IMarker.SEVERITY_INFO));
				markerKind = IMarker.TASK;
				break;
			}
			attributes.put(IMarker.LOCATION, location);
			MarkerUtilities.setLineNumber(attributes, line);
			MarkerUtilities.createMarker(file, attributes, markerKind);
		}
	}

	/**
	 * This will set a marker.
	 * 
	 * @param message
	 *            the message
	 * @param charStart
	 *            the starting character
	 * @param charEnd
	 *            the end character
	 * @param errorLevel
	 *            the error level ({@link ExternalPHPParser#ERROR},{@link ExternalPHPParser#INFO},{@link ExternalPHPParser#WARNING})
	 * @throws CoreException
	 *             an exception throwed by the MarkerUtilities
	 */
	private void setMarker(final String message, final int charStart,
			final int charEnd, final int errorLevel, final String location)
			throws CoreException {
		if (fFileToParse != null) {
			setMarker(fFileToParse, message, charStart, charEnd, errorLevel,
					location);
		}
	}

	/**
	 * This will set a marker.
	 * 
	 * @param file
	 *            the file that generated the marker
	 * @param message
	 *            the message
	 * @param charStart
	 *            the starting character
	 * @param charEnd
	 *            the end character
	 * @param errorLevel
	 *            the error level ({@link ExternalPHPParser#ERROR},{@link ExternalPHPParser#INFO},{@link ExternalPHPParser#WARNING})
	 * @param location
	 *            the location of the error
	 * @throws CoreException
	 *             an exception throwed by the MarkerUtilities
	 */
	private void setMarker(final IFile file, final String message,
			final int charStart, final int charEnd, final int errorLevel,
			final String location) throws CoreException {
		if (file != null) {
			final Hashtable attributes = new Hashtable();
			MarkerUtilities.setMessage(attributes, message);
			switch (errorLevel) {
			case ERROR:
				attributes.put(IMarker.SEVERITY, new Integer(
						IMarker.SEVERITY_ERROR));
				break;
			case WARNING:
				attributes.put(IMarker.SEVERITY, new Integer(
						IMarker.SEVERITY_WARNING));
				break;
			case INFO:
				attributes.put(IMarker.SEVERITY, new Integer(
						IMarker.SEVERITY_INFO));
				break;
			case TASK:
				attributes.put(IMarker.SEVERITY, new Integer(IMarker.TASK));
				break;
			}
			attributes.put(IMarker.LOCATION, location);
			MarkerUtilities.setCharStart(attributes, charStart);
			MarkerUtilities.setCharEnd(attributes, charEnd);
			MarkerUtilities.createMarker(file, attributes, PROBLEM_ID); // IMarker.PROBLEM);
		}
	}

	private String getParserOutput(String command, String consoleMessage) {
		try {
			PHPConsole console = new PHPConsole();
			try {
				console.println(consoleMessage + command);
			} catch (Throwable th) {

			}

			Runtime runtime = Runtime.getRuntime();

			// runs the command
			Process p = runtime.exec(command);

			// gets the input stream to have the post-compile-time information
			InputStream stream = p.getInputStream();

			// get the string from Stream
			String consoleOutput = PHPConsole.getStringFromStream(stream);

			// prints out the information
			if (console != null) {
				console.print(consoleOutput);
			}
			return consoleOutput;

		} catch (IOException e) {
			MessageDialog
					.openInformation(null, "IOException: ", e.getMessage());
		}
		return "";
	}
}