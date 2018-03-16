/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package net.sourceforge.phpdt.internal.corext.textmanipulation;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.phpdt.internal.corext.util.Strings;
import net.sourceforge.phpdt.internal.ui.PHPStatusConstants;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ILineTracker;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.util.Assert;

// import net.sourceforge.phpdt.internal.ui.JavaPlugin;
// import net.sourceforge.phpdt.internal.ui.JavaStatusConstants;

/**
 * An implementation of a <code>TextBuffer</code> that is based on
 * <code>ITextSelection</code> and <code>IDocument</code>.
 */
public class TextBuffer {

	private static class DocumentRegion extends TextRegion {
		IRegion fRegion;

		public DocumentRegion(IRegion region) {
			fRegion = region;
		}

		public int getOffset() {
			return fRegion.getOffset();
		}

		public int getLength() {
			return fRegion.getLength();
		}
	}

	public class Block {
		public String content;

		public int offsetDelta;
	}

	private IDocument fDocument;

	private static final TextBufferFactory fgFactory = new TextBufferFactory();

	TextBuffer(IDocument document) {
		fDocument = document;
		Assert.isNotNull(fDocument);
	}

	/**
	 * Returns the number of characters in this text buffer.
	 * 
	 * @return the number of characters in this text buffer
	 */
	public int getLength() {
		return fDocument.getLength();
	}

	/**
	 * Returns the number of lines in this text buffer.
	 * 
	 * @return the number of lines in this text buffer
	 */
	public int getNumberOfLines() {
		return fDocument.getNumberOfLines();
	}

	/**
	 * Returns the character at the given offset in this text buffer.
	 * 
	 * @param offset
	 *            a text buffer offset
	 * @return the character at the offset
	 * @exception IndexOutOfBoundsException
	 *                if the <code>offset</code> argument is negative or not
	 *                less than the length of this text buffer.
	 */
	public char getChar(int offset) {
		try {
			return fDocument.getChar(offset);
		} catch (BadLocationException e) {
			throw new ArrayIndexOutOfBoundsException(e.getMessage());
		}
	}

	/**
	 * Returns the whole content of the text buffer.
	 * 
	 * @return the whole content of the text buffer
	 */
	public String getContent() {
		return fDocument.get();
	}

	/**
	 * Returns length characters starting from the specified position.
	 * 
	 * @return the characters specified by the given text region. Returns <code>
	 *  null</code>
	 *         if text range is illegal
	 */
	public String getContent(int start, int length) {
		try {
			return fDocument.get(start, length);
		} catch (BadLocationException e) {
			return null;
		}
	}

	public Block getBlockContent(int start, int length, int tabWidth) {
		Block result = new Block();
		StringBuffer buffer = new StringBuffer();
		int lineOffset = getLineInformationOfOffset(start).getOffset();
		if (start > lineOffset) {
			String line = getContent(lineOffset, start - lineOffset);
			String indent = Strings.getIndentString(line, tabWidth);
			result.offsetDelta = -indent.length();
			buffer.append(indent);
		}
		final int end = start + length;
		TextRegion region = getLineInformationOfOffset(end);
		lineOffset = region.getOffset();
		// Cursor is at beginning of next line
		if (lineOffset == end) {
			int lineNumber = getLineOfOffset(lineOffset);
			if (lineNumber > 0) {
				length = length - getLineDelimiter(lineNumber - 1).length();
			}
		}
		if (buffer.length() == 0) {
			result.content = getContent(start, length);
		} else {
			buffer.append(getContent(start, length));
			result.content = buffer.toString();
		}
		return result;
	}

	/**
	 * Returns the preferred line delimiter to be used for this text buffer.
	 * 
	 * @return the preferred line delimiter
	 */
	public String getLineDelimiter() {
		String lineDelimiter = getLineDelimiter(0);
		if (lineDelimiter == null)
			lineDelimiter = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		return lineDelimiter;
	}

	/**
	 * Returns the line delimiter used for the given line number. Returns <code>
	 * null</code>
	 * if the line number is out of range.
	 * 
	 * @return the line delimiter used by the given line number or
	 *         <code>null</code>
	 */
	public String getLineDelimiter(int line) {
		try {
			return fDocument.getLineDelimiter(line);
		} catch (BadLocationException e) {
			return null;
		}
	}

	/**
	 * Returns the line for the given line number. If there isn't any line for
	 * the given line number, <code>null</code> is returned.
	 * 
	 * @return the line for the given line number or <code>null</code>
	 */
	public String getLineContent(int line) {
		try {
			IRegion region = fDocument.getLineInformation(line);
			return fDocument.get(region.getOffset(), region.getLength());
		} catch (BadLocationException e) {
			return null;
		}
	}

	/**
	 * Returns the line indent for the given line. If there isn't any line for
	 * the given line number, <code>-1</code> is returned.
	 * 
	 * @return the line indent for the given line number of <code>-1</code>
	 */
	public int getLineIndent(int lineNumber, int tabWidth) {
		return Strings.computeIndent(getLineContent(lineNumber), tabWidth);
	}

	/**
	 * Returns a region of the specified line. The region contains the offset
	 * and the length of the line excluding the line's delimiter. Returns
	 * <code>null</code> if the line doesn't exist.
	 * 
	 * @param line
	 *            the line of interest
	 * @return a line description or <code>null</code> if the given line
	 *         doesn't exist
	 */
	public TextRegion getLineInformation(int line) {
		try {
			return new DocumentRegion(fDocument.getLineInformation(line));
		} catch (BadLocationException e) {
			return null;
		}
	}

	/**
	 * Returns a line region of the specified offset. The region contains the
	 * offset and the length of the line excluding the line's delimiter. Returns
	 * <code>null</code> if the line doesn't exist.
	 * 
	 * @param offset
	 *            an offset into a line
	 * @return a line description or <code>null</code> if the given line
	 *         doesn't exist
	 */
	public TextRegion getLineInformationOfOffset(int offset) {
		try {
			return new DocumentRegion(fDocument
					.getLineInformationOfOffset(offset));
		} catch (BadLocationException e) {
			return null;
		}
	}

	/**
	 * Returns the line number that contains the given position. If there isn't
	 * any line that contains the position, <code>null</code> is returned. The
	 * returned string is a copy and doesn't contain the line delimiter.
	 * 
	 * @return the line that contains the given offset or <code>null</code> if
	 *         line doesn't exist
	 */
	public int getLineOfOffset(int offset) {
		try {
			return fDocument.getLineOfOffset(offset);
		} catch (BadLocationException e) {
			return -1;
		}
	}

	/**
	 * Returns the line that contains the given position. If there isn't any
	 * line that contains the position, <code>null</code> is returned. The
	 * returned string is a copy and doesn't contain the line delimiter.
	 * 
	 * @return the line that contains the given offset or <code>null</code> if
	 *         line doesn't exist
	 */
	public String getLineContentOfOffset(int offset) {
		try {
			IRegion region = fDocument.getLineInformationOfOffset(offset);
			return fDocument.get(region.getOffset(), region.getLength());
		} catch (BadLocationException e) {
			return null;
		}
	}

	/**
	 * Converts the text determined by the region [offset, length] into an array
	 * of lines. The lines are copies of the original lines and don't contain
	 * any line delimiter characters.
	 * 
	 * @return the text converted into an array of strings. Returns
	 *         <code>null</code> if the region lies outside the source.
	 */
	public String[] convertIntoLines(int offset, int length,
			boolean lastNewLineCreateEmptyLine) {
		try {
			String text = fDocument.get(offset, length);
			ILineTracker tracker = new DefaultLineTracker();
			tracker.set(text);
			int size = tracker.getNumberOfLines();
			int lastLine = size - 1;
			List result = new ArrayList(size);
			for (int i = 0; i < size; i++) {
				IRegion region = tracker.getLineInformation(i);
				String line = getContent(offset + region.getOffset(), region
						.getLength());
				if (i < lastLine
						|| !"".equals(line) || lastNewLineCreateEmptyLine) //$NON-NLS-1$
					result.add(line);
			}
			return (String[]) result.toArray(new String[result.size()]);
		} catch (BadLocationException e) {
			return null;
		}
	}

	/**
	 * Subsitutes the given text for the specified text position
	 * 
	 * @param offset
	 *            the starting offset of the text to be replaced
	 * @param length
	 *            the length of the text to be replaced
	 * @param text
	 *            the substitution text
	 * @exception CoreException
	 *                if the text position [offset, length] is invalid.
	 */
	public void replace(int offset, int length, String text)
			throws CoreException {
		try {
			fDocument.replace(offset, length, text);
		} catch (BadLocationException e) {
			IStatus s = new Status(IStatus.ERROR, PHPeclipsePlugin
					.getPluginId(), PHPStatusConstants.INTERNAL_ERROR,
					TextManipulationMessages.getFormattedString(
							"TextBuffer.wrongRange", //$NON-NLS-1$
							new Object[] { new Integer(offset),
									new Integer(length) }), e);
			throw new CoreException(s);
		}
	}

	public void replace(TextRange range, String text) throws CoreException {
		replace(range.fOffset, range.fLength, text);
	}

	// ---- Special methods used by the <code>TextBufferEditor</code>

	/**
	 * Releases this text buffer.
	 */
	/* package */void release() {
	}

	/* package */void registerUpdater(IDocumentListener listener) {
		fDocument.addDocumentListener(listener);
	}

	/* package */void unregisterUpdater(IDocumentListener listener) {
		fDocument.removeDocumentListener(listener);
	}

	// ---- Factory methods
	// ----------------------------------------------------------------

	/**
	 * Acquires a text buffer for the given file. If a text buffer for the given
	 * file already exists, then that one is returned.
	 * 
	 * @param file
	 *            the file for which a text buffer is requested
	 * @return a managed text buffer for the given file
	 * @exception CoreException
	 *                if it was not possible to acquire the text buffer
	 */
	public static TextBuffer acquire(IFile file) throws CoreException {
		return fgFactory.acquire(file);
	}

	/**
	 * Releases the given text buffer.
	 * 
	 * @param buffer
	 *            the text buffer to be released
	 */
	public static void release(TextBuffer buffer) {
		fgFactory.release(buffer);
	}

	/**
	 * Commits the changes made to the given text buffer to the underlying
	 * storage system.
	 * 
	 * @param buffer
	 *            the text buffer containing the changes to be committed.
	 * @param force
	 *            if <code>true</code> the text buffer is committed in any
	 *            case. If <code>false</code> the text buffer is <b>ONLY</b>
	 *            committed if the client is the last one that holds a reference
	 *            to the text buffer. Clients of this method must make sure that
	 *            they don't call this method from within an <code>
	 *  IWorkspaceRunnable</code>.
	 * @param pm
	 *            the progress monitor used to report progress if committing is
	 *            necessary
	 */
	public static void commitChanges(TextBuffer buffer, boolean force,
			IProgressMonitor pm) throws CoreException {
		fgFactory.commitChanges(buffer, force, pm);
	}

	/**
	 * Creates a new <code>TextBuffer</code> for the given file. The returned
	 * buffer will not be managed. Any subsequent call to <code>create</code>
	 * with the same file will return a different text buffer.
	 * <p>
	 * If the file is currently open in a text editor, the editors content is
	 * copied into the returned <code>TextBuffer</code>. Otherwise the
	 * content is read from disk.
	 * 
	 * @param file
	 *            the file for which a text buffer is to be created
	 * @return a new unmanaged text buffer
	 * @exception CoreException
	 *                if it was not possible to create the text buffer
	 */
	public static TextBuffer create(IFile file) throws CoreException {
		return fgFactory.create(file);
	}

	/**
	 * Creates a new <code>TextBuffer</code> for the string. The returned
	 * buffer will not be managed. Any subsequent call to <code>create</code>
	 * with the identical string will return a different text buffer.
	 * 
	 * @param content
	 *            the text buffer's content
	 * @return a new unmanaged text buffer
	 */
	public static TextBuffer create(String content) {
		return fgFactory.create(content);
	}

	// Unclear which methods are needed if we get the new save model. If optimal
	// no
	// save is needed at all.

	public static void save(TextBuffer buffer, IProgressMonitor pm)
			throws CoreException {
		fgFactory.save(buffer, pm);
	}

	public static void aboutToChange(TextBuffer buffer) throws CoreException {
		fgFactory.aboutToChange(buffer);
	}

	public static void changed(TextBuffer buffer) throws CoreException {
		fgFactory.changed(buffer);
	}
}