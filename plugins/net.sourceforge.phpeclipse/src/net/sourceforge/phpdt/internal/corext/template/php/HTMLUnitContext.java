package net.sourceforge.phpdt.internal.corext.template.php;

import net.sourceforge.phpdt.core.ICompilationUnit;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateTranslator;

/**
 * A context for javadoc.
 */
public class HTMLUnitContext extends CompilationUnitContext {

	// tags
	// private static final char HTML_TAG_BEGIN= '<';
	// private static final char HTML_TAG_END= '>';
	// private static final char JAVADOC_TAG_BEGIN= '@';
	/**
	 * special characters '&' for the start of HTML entities '<' for the start
	 * of HTML tags '#' for the start of colour attributes '{' for the start of
	 * smarty partitions inside HTML code
	 */
	private static final String specialChars = "&<#{";

	/**
	 * Creates a javadoc template context.
	 * 
	 * @param type
	 *            the context type.
	 * @param document
	 *            the document.
	 * @param completionOffset
	 *            the completion offset within the document.
	 * @param completionLength
	 *            the completion length within the document.
	 * @param compilationUnit
	 *            the compilation unit (may be <code>null</code>).
	 */
	public HTMLUnitContext(TemplateContextType type, IDocument document,
			int completionOffset, int completionLength,
			ICompilationUnit compilationUnit) {
		super(type, document, completionOffset, completionLength,
				compilationUnit);
	}

	/*
	 * @see TemplateContext#canEvaluate(Template templates)
	 */
	public boolean canEvaluate(Template template) {
		String key = getKey();

		if (fForceEvaluation)
			return true;

		return template.matches(key, getContextType().getId())
				&& (key.length() != 0)
				&& template.getName().toLowerCase().startsWith(
						key.toLowerCase());
	}

	/*
	 * @see DocumentTemplateContext#getCompletionPosition();
	 */
	public int getStart() {
		IDocument document = getDocument();
		try {
			int start = getCompletionOffset();
			char ch = ' ';
			while (start != 0) {
				ch = document.getChar(start - 1);
				if (specialChars.indexOf(ch) != (-1)) {
					return --start;
				}
				if (Character.isUnicodeIdentifierPart(ch)) {
					start--;
				} else {
					break;
				}
			}
			if ((start != 0)
					&& Character.isUnicodeIdentifierStart(document
							.getChar(start - 1))) {
				start--;
				if ((start != 0)
						&& specialChars.indexOf(document.getChar(start - 1)) != (-1)) {
					start--;
				}
			}

			// while (((start != 0)
			// && Character.isUnicodeIdentifierPart(document.getChar(start -
			// 1)))
			// || ((start != 0)
			// && specialChars.indexOf(document.getChar(start - 1)) != (-1))) {
			// start--;
			// }
			//
			// if (((start != 0)
			// && Character.isUnicodeIdentifierStart(document.getChar(start -
			// 1)))
			// || ((start != 0)
			// && specialChars.indexOf(document.getChar(start - 1)) != (-1))) {
			// start--;
			// }

			return start;

		} catch (BadLocationException e) {
			return getCompletionOffset();
		}
	}

	/*
	 * @see net.sourceforge.phpdt.internal.corext.template.DocumentTemplateContext#getEnd()
	 */
	public int getEnd() {

		if (getCompletionLength() == 0)
			return super.getEnd();

		try {
			IDocument document = getDocument();

			int start = getCompletionOffset();
			int end = getCompletionOffset() + getCompletionLength();

			while (start != end
					&& Character.isWhitespace(document.getChar(end - 1)))
				end--;

			return end;

		} catch (BadLocationException e) {
			return super.getEnd();
		}
	}

	/*
	 * @see net.sourceforge.phpdt.internal.corext.template.DocumentTemplateContext#getKey()
	 */
	public String getKey() {

		if (getCompletionLength() == 0)
			return super.getKey();

		try {
			IDocument document = getDocument();

			int start = getStart();
			int end = getCompletionOffset();
			return start <= end ? document.get(start, end - start) : ""; //$NON-NLS-1$

		} catch (BadLocationException e) {
			return super.getKey();
		}
	}

	/*
	 * @see TemplateContext#evaluate(Template)
	 */
	public TemplateBuffer evaluate(Template template)
			throws BadLocationException, TemplateException {
		TemplateTranslator translator = new TemplateTranslator();
		TemplateBuffer buffer = translator.translate(template);

		getContextType().resolve(buffer, this);

		return buffer;
	}

}
