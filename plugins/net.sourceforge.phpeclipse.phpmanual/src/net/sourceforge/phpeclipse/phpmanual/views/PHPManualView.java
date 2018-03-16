package net.sourceforge.phpeclipse.phpmanual.views;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.sourceforge.phpeclipse.phpmanual.PHPManualUiMessages;
//import net.sourceforge.phpdt.internal.debug.ui.PHPDebugUiMessages;
import net.sourceforge.phpdt.internal.ui.text.JavaWordFinder;
import net.sourceforge.phpdt.internal.ui.viewsupport.ISelectionListenerWithAST;
import net.sourceforge.phpdt.internal.ui.viewsupport.SelectionListenerWithASTManager;
import net.sourceforge.phpdt.phphelp.PHPHelpPlugin;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;
import net.sourceforge.phpeclipse.phpeditor.PHPEditor;
import net.sourceforge.phpeclipse.phpmanual.PHPManualUIPlugin;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.tags.Div;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.TagFindingVisitor;
import org.osgi.framework.Bundle;

/**
 * This ViewPart is the implementation of the idea of having the 
 * PHP Manual easily accessible while coding. It shows the
 * under-cursor function's reference inside a browser.
 * <p>
 * The view listens to selection changes both in the (1)workbench, to
 * know when the user changes between the instances of the PHPEditor
 * or when a new instance is created; and in the (2)PHPEditor, to know
 * when the user changes the cursor position. This explains the need
 * to implement both ISelectionListener and ISelectionListenerWithAST.
 * <p>
 * Up to now, the ViewPart show reference pages from HTML stored in the
 * doc.zip file from the net.sourceforge.phpeclipse.phphelp plugin. It
 * also depends on net.sourceforge.phpeclipse.phpmanual.htmlparser to
 * parse these HTML files.
 * <p>
 * @author scorphus
 */
public class PHPManualView extends ViewPart implements INullSelectionListener, ISelectionListenerWithAST {

	/**
	 * The ViewPart's browser
	 */
	private Browser browser;

	/**
	 * A reference to store last active editor to know when we've
	 * got a new instance of the PHPEditor
	 */
	private PHPEditor lastEditor;

	/**
	 * String that stores the last selected word
	 */
	private String lastOccurrence = null;

	/**
	 * The path to the doc.zip file containing the PHP Manual
	 * in HTML format
	 */
	private final Path docPath = new Path("doc.zip"); 

	/**
	 * The constructor.
	 */
	public PHPManualView() {
	}

	/**
	 * This method initializes the ViewPart. It instantiates components
	 * and add listeners
	 * 
	 * @param parent The parent control
	 */
	public void createPartControl(Composite parent) {
		browser = new Browser(parent, SWT.NONE);
		browser.addLocationListener(new LocationAdapter() {
			public void changing(LocationEvent event) {
				String loc = event.location.toString();
				if(!loc.equalsIgnoreCase("about:blank") && !loc.startsWith("jar:")) {
					String func = loc.replaceAll("file:///", "");
					func = func.replaceAll("#.+$", "");
					String[] afunc = loc.split("\\.");
					if(!afunc[1].equalsIgnoreCase(lastOccurrence)) {
						lastOccurrence = afunc[1];
						showReference(func);
						event.doit = false;
					}
				}
			}
		});
		parent.pack();
		if ((lastEditor = getJavaEditor()) != null) {
			SelectionListenerWithASTManager.getDefault().addListener(lastEditor, this);
		}
		getSite().getWorkbenchWindow().getSelectionService()
				.addPostSelectionListener(PHPeclipsePlugin.EDITOR_ID, this);
	}
	/**
	 * Cleanup to remove the selection listener
	 */
	public void dispose() {
		getSite().getWorkbenchWindow().getSelectionService()
				.removePostSelectionListener(PHPeclipsePlugin.EDITOR_ID, this);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		browser.setFocus();
	}

	/**
	 * Treats selection changes from the PHPEditor
	 */
	public void selectionChanged(IEditorPart part, ITextSelection selection) {
		IDocument document = ((PHPEditor)part).getViewer().getDocument();
		int offset = selection.getOffset();
		IRegion iRegion = JavaWordFinder.findWord(document, offset);
		if (document != null && iRegion != null) {
			try {
				final String wordStr = document.get(iRegion.getOffset(),
						iRegion.getLength());
				if (!wordStr.equalsIgnoreCase(lastOccurrence)) {
					showReference(wordStr);				
					lastOccurrence = wordStr;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Treats selection changes from the workbench. When part is new
	 * instance of PHPEditor it gets a listener attached
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (part != null && !((PHPEditor)part).equals(lastEditor)) {
			SelectionListenerWithASTManager.getDefault().addListener((PHPEditor)part, this);
			lastEditor = (PHPEditor)part;
		}
	}

	/**
	 * Updates the browser with the reference page for a given function
	 * 
	 * @param funcName Function name
	 */
	private void showReference(final String funcName) {
		new Thread(new Runnable() {
			public void run() {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						String html = getHtmlSource(funcName);
						browser.setText(html);
					}
				});
			}
		}).start();
	}
	
	/**
	 * Filters the function's reference page extracting only parts of it
	 * 
	 * @param source HTML source of the reference page
	 * @return HTML source of reference page
	 */
	private String filterHtmlSource(String source) {
		try {
			Parser parser = new Parser(source);
			String[] tagsToBeFound = { "DIV" };
			// Common classes to be included for all page types
			ArrayList classList = new ArrayList(Arrays.asList(new String[] {
					"section", "sect1", "title", "partintro", "refnamediv",
					"refsect1 description", "refsect1 parameters",
					"refsect1 returnvalues", "refsect1 examples",
					"refsect1 seealso", "refsect1 u", "example-contents" }));
			// Grab all the tags for processing
			TagFindingVisitor visitor = new TagFindingVisitor(tagsToBeFound);
			parser.visitAllNodesWith(visitor);
			Node [] allPTags = visitor.getTags(0);
			StringBuffer output = new StringBuffer();
			for (int i = 0; i < allPTags.length; i++) {
				String tagClass = ((Div)allPTags[i]).getAttribute("class");
				if (classList.contains(tagClass)) {
					output.append(allPTags[i].toHtml());
				}
			}
			return output.toString().replaceAll("—", "-");
			//.replace("<h3 class=\"title\">Description</h3>", " ");
		} catch (ParserException e) {
			e.printStackTrace();
		}
		return "";
	}
	/**
	 * Reads the template that defines the style of the reference page
	 * shown inside the view's browser
	 * 
	 * @return HTML source of the template
	 */
	public String getRefPageTemplate() {
		Bundle bundle = Platform.getBundle(PHPManualUIPlugin.PLUGIN_ID);
		URL fileURL = FileLocator.find(bundle, new Path("templates"), null);
		StringBuffer contents = new StringBuffer();
		BufferedReader input = null;
		try {
			URL resolve = FileLocator.resolve(fileURL);
			input = new BufferedReader(new FileReader(resolve.getPath()+"/refpage.html"));
			String line = null;
			while ((line = input.readLine()) != null){
				contents.append(line);
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (input!= null) {
					input.close();
				}
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return contents.toString();
	}

	/**
	 * Replaces each substring of source string that matches the
	 * given pattern string with the given replace string
	 * 
	 * @param source The source string
	 * @param pattern The pattern string
	 * @param replace The replace string
	 * @return The resulting String
	 */
	public static String replace(String source, String pattern, String replace) {
		if (source != null) {
			final int len = pattern.length();
			StringBuffer sb = new StringBuffer();
			int found = -1;
			int start = 0;
			while ((found = source.indexOf(pattern, start)) != -1) {
				sb.append(source.substring(start, found));
				sb.append(replace);
				start = found + len;
			}
			sb.append(source.substring(start));
			return sb.toString();
		} else {
			return "";
		}
	}

	/**
	 * Looks for the function's reference page inside the doc.zip file and
	 * returns a filtered HTML source of it embedded in the template
	 * 
	 * @param funcName
	 *            Function name
	 * @return HTML source of reference page
	 */
	public String getHtmlSource(String funcName) {
		if (funcName.length() == 0) {
			// Don't bother ;-) 
			return null;
		}
		Bundle bundle = Platform.getBundle(PHPHelpPlugin.PLUGIN_ID);
		URL fileURL = FileLocator.find(bundle, docPath, null);
		ZipEntry entry = null;
		// List of prefixes to lookup HTML files by, ordered so that looping
		// is as minimal as possible.  The empty value matches links passed,
		// rather than function 
		String[] prefixes = { "", "function", "control-structures", "ref", "http", "imagick", "ming" };
		byte[] b = null;
		if (funcName.matches("^[a-z-]+\\.[a-z-0-9]+\\.html$")) {
			// funcName is actually a page reference, strip the prefix and suffix
			funcName = funcName.substring(0, funcName.lastIndexOf('.'));
		}
		try {
			URL resolve = FileLocator.resolve(fileURL);
			ZipFile docFile = new ZipFile(resolve.getPath());
			for (int i = 0; i < prefixes.length; i++) {
				if ((entry = docFile.getEntry("doc/" + prefixes[i] +
						(prefixes[i].length() == 0 ? "" : ".") +
						funcName.replace('_', '-') + ".html")) != null) {
					// Document was matched
					InputStream ref = docFile.getInputStream(entry);
					b = new byte[(int)entry.getSize()];
					ref.read(b, 0, (int)entry.getSize());
					if (b != null) {
						String reference = filterHtmlSource(new String(b));
						String refPageTpl = getRefPageTemplate();
						refPageTpl = refPageTpl.replaceAll("%title%", funcName);
						refPageTpl = replace(refPageTpl, "%reference%", reference);
						return refPageTpl;
					}
				}
			}
		} catch (IOException e) {
			return "<html>" + PHPManualUIPlugin.getString("LookupException") + "</html>";
		} catch (Exception e) {
			return null;
		}
		return null; // Keeps the last reference
	}

	/**
	 * Returns the currently active java editor, or <code>null</code> if it
	 * cannot be determined.
	 * 
	 * @return the currently active java editor, or <code>null</code>
	 */
	private PHPEditor getJavaEditor() {
		try {
			IEditorPart part = PHPeclipsePlugin.getActivePage().getActiveEditor();
			if (part instanceof PHPEditor)
				return (PHPEditor) part;
			else
				return null;
		} catch (Exception e) {
			return null;
		}
	}

}