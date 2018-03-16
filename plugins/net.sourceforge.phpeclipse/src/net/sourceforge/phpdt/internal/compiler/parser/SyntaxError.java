package net.sourceforge.phpdt.internal.compiler.parser;

/**
 * Exception for a syntax error detected by the parser.
 */
public class SyntaxError extends Error {

	/** The line where the error start */
	int lineNumber;

	/** The column where the error start */
	int columnNumber;

	/** the current line. */
	String currentLine;

	/** The error message. */
	String error;

	/**
	 * SyntaxError exception
	 * 
	 * @param lineNumber
	 *            the line number where the error start
	 * @param columnNumber
	 *            the column where the error start
	 * @param currentLine
	 *            the line where the error end
	 * @param error
	 *            the error message
	 * @see
	 */
	public SyntaxError(int lineNumber, int columnNumber, String currentLine,
			String error) {
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;
		this.currentLine = currentLine;
		this.error = error;
	}

	/**
	 * Get the error message.
	 * 
	 * @return the error message
	 */
	public String getMessage() {
		// StringBuffer buf = new StringBuffer(256);
		// buf.append("Syntax error in line:");
		// buf.append(lineNumber+1);
		// buf.append(": "+ error + "\n");
		// buf.append( currentLine + "\n");
		// for (int i=0; i<(columnNumber-1); i++) {
		// buf.append(' ');
		// }
		// buf.append('^');
		// return buf.toString();

		// System.err.println(currentLine);
		// System.err.println(columnNumber);
		return error;
	}

	/**
	 * Get the line number where the error happens
	 * 
	 * @return the line number
	 */
	public int getLine() {
		return lineNumber;
	}
}
