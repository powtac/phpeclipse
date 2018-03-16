package net.sourceforge.phpdt.httpquery;

import net.sourceforge.phpdt.httpquery.config.Configuration;
import net.sourceforge.phpeclipse.webbrowser.views.BrowserView;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public abstract class AbstractHTTPQueryAction implements IEditorActionDelegate {

	private AbstractTextEditor editor;

	public AbstractHTTPQueryAction() {
		super();
	}

	abstract protected Configuration getConfiguration(String name);

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor != null
				&& (targetEditor instanceof AbstractTextEditor)) {
			editor = (AbstractTextEditor) targetEditor;
		}
	}

	public void run(IAction action) {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (editor == null) {
			IEditorPart targetEditor = window.getActivePage().getActiveEditor();
			if (targetEditor != null
					&& (targetEditor instanceof AbstractTextEditor)) {
				editor = (AbstractTextEditor) targetEditor;
			}
		}

		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			try {
				IViewPart part = page.findView(BrowserView.ID_BROWSER);
				if (part == null) {
					part = page.showView(BrowserView.ID_BROWSER);
				} else {
					page.bringToTop(part);
				}
				Configuration config = getConfiguration(null);
				String templateString = generateUrl(config, config.getURL());
				if (templateString != null && !templateString.equals("")) {
					((BrowserView) part).setUrl(templateString);
				} else {
					((BrowserView) part).setUrl(config.getURL());
				}
			} catch (Exception e) {
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

	public IDocument getDocument() {
		IDocument doc = editor.getDocumentProvider().getDocument(
				editor.getEditorInput());
		return doc;
	}

	public static String getSelectedText(AbstractTextEditor editor,
			IDocument document, int initialPos) {
		try {
			int pos = initialPos;
			int line = document.getLineOfOffset(pos);
			int start = document.getLineOffset(line);
			int end = start + document.getLineInformation(line).getLength();

			/*
			 * The line does not include \n or \r so pos can be > end. Making
			 * pos = end in this case is safe for the purposes of determining
			 * the TextRegion at the cursor position
			 */
			if (pos > end) {
				pos = end;
			}

			int offsetInLine = pos - start;
			String word = document.get(start, end - start);
			int wordlen = word.length();
			int textStart = -1;
			int textEnd = -1;

			for (int i = offsetInLine; i < wordlen; i++) {
				if (!Character.isJavaIdentifierPart(word.charAt(i))) {
					textEnd = i;
					break;
				}
			}
			for (int i = offsetInLine; i >= 0; i--) {
				if (!Character.isJavaIdentifierPart(word.charAt(i))) {
					textStart = i + 1;
					break;
				}
			}
			if (textStart != (-1) && textEnd != (-1) && textStart < textEnd) {
				return new String(word.toCharArray(), textStart, textEnd
						- textStart);
			}
		} catch (Exception e) {

		}
		return null;
	}

	public String generateUrl(Configuration config, String template) {
		IDocument doc = getDocument();
		ITextSelection selection = (ITextSelection) editor
				.getSelectionProvider().getSelection();
		int pos = selection.getOffset();
		int len = selection.getLength();
		String wikiTitle;
		if (len > 0) {
			try {
				wikiTitle = doc.get(pos, len);
			} catch (BadLocationException e) {
				wikiTitle = null;
			}
		} else {
			wikiTitle = getSelectedText(editor, doc, pos);
		}

		if (wikiTitle != null && !wikiTitle.equals("")) {
			template = template.replaceAll("\\$text.selection", wikiTitle);
			wikiTitle = wikiTitle.replaceAll("_", "-");
			template = template.replaceAll("\\$php.selection", wikiTitle);
			return template;
		}
		return null;
	}
}