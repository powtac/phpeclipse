package net.sourceforge.phpeclipse.ui.editor;

import net.sourceforge.phpeclipse.ui.IPreferenceConstants;
import net.sourceforge.phpeclipse.ui.WebUI;
import net.sourceforge.phpeclipse.ui.overlaypages.ProjectPrefUtil;
import net.sourceforge.phpeclipse.webbrowser.views.BrowserView;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

/**
 * ClassDeclaration that defines the action for parsing the current PHP file
 */
public class ShowExternalPreviewAction extends TextEditorAction {
	public final static int XML_TYPE = 1;

	public final static int HTML_TYPE = 2;

	public final static int SMARTY_TYPE = 3;

	public final static int PHP_TYPE = 4;

	private static ShowExternalPreviewAction instance = new ShowExternalPreviewAction();

	/**
	 * Constructs and updates the action.
	 */
	private ShowExternalPreviewAction() {
		super(EditorMessages.getResourceBundle(), "ParserAction.", null); //$NON-NLS-1$
		update();
	}

	public static ShowExternalPreviewAction getInstance() {
		return instance;
	}

	/**
	 * Code called when the action is fired.
	 */
	public void run() {
		doRun(PHP_TYPE);
	}

	public void doRun(int type) {
		IFile previewFile = getFile();
		BrowserUtil.showPreview(previewFile, false, "");
	}

	public void refresh(int type) {
		IFile previewFile = getFile();
		if (previewFile == null) {
			// should never happen
			return;
		}
		boolean autoPreview = ProjectPrefUtil.getPreviewBooleanValue(
				previewFile, IPreferenceConstants.PHP_AUTO_PREVIEW_DEFAULT);
		boolean bringToTopPreview = ProjectPrefUtil.getPreviewBooleanValue(
				previewFile,
				IPreferenceConstants.PHP_BRING_TO_TOP_PREVIEW_DEFAULT);
		boolean stickyBrowserURL = ProjectPrefUtil.getPreviewBooleanValue(
				previewFile,
				IPreferenceConstants.PHP_STICKY_BROWSER_URL_DEFAULT);

		if (autoPreview) {
			IWorkbenchPage page = WebUI.getActivePage();
			if (page == null) {
				// startup stage
				return;
			}
			try {
				// IViewPart part = page.findView(BrowserView.ID_BROWSER);
				// if (part == null) {
				// part = page.showView(BrowserView.ID_BROWSER);
				// }
				IViewPart part = page.showView(BrowserView.ID_BROWSER, null,
						IWorkbenchPage.VIEW_CREATE);
				if (part != null) {
					if (bringToTopPreview) {
						// page.bringToTop(part);
						new WorkbenchJob(getClass().getName()) {
							public IStatus runInUIThread(
									IProgressMonitor monitor) {
								IWorkbenchPage page = WebUI.getActivePage();
								if (page != null) {
									IViewPart part = page
											.findView(BrowserView.ID_BROWSER);
									if (part != null) {
										page.bringToTop(part);
									}
								}
								return Status.OK_STATUS;
							}
						}.schedule();
					}
					// ((BrowserView) part).refresh();
					if (stickyBrowserURL
							&& ((BrowserView) part).getUrl() != null
							&& ((BrowserView) part).getUrl().length() > 0) {
						((BrowserView) part).refresh();
					} else {
						String localhostURL = getLocalhostURL(null, previewFile);
						((BrowserView) part).refresh(localhostURL);
					}
				}
			} catch (PartInitException e) {
				// ad hoc
				WebUI.getDefault().getLog().log(
						new Status(IStatus.ERROR,
								"net.sourceforge.phpeclipse.ui", IStatus.OK,
								"Failed to show Browser View", e));
				// PHPeclipsePlugin.log(e);
			}
		}
	}

	/**
	 * Finds the file that's currently opened in the PHP Text Editor
	 */
	protected IFile getFile() {
		ITextEditor editor = getTextEditor();
		IEditorInput editorInput = null;
		if (editor != null) {
			editorInput = editor.getEditorInput();
		}
		if (editorInput instanceof IFileEditorInput)
			return ((IFileEditorInput) editorInput).getFile();
		// if nothing was found, which should never happen
		return null;
	}

	public static String getLocalhostURL(IPreferenceStore store, IFile file) {
		if (file != null) {
			if (store == null) {
				store = WebUI.getDefault().getPreferenceStore();
			}
			// IPath path = file.getFullPath();
			String localhostURL = file.getFullPath().toString();
			String lowerCaseFileName = localhostURL.toLowerCase();
			//removed by ed_mann for RSE fixes testing
			// String documentRoot =
			// store.getString(PHPeclipsePlugin.DOCUMENTROOT_PREF);
			//IPath documentRootPath = ProjectPrefUtil.getDocumentRoot(file
			//		.getProject());
			IPath documentRootPath = file.getProject().getFullPath();
			String documentRoot = documentRootPath.toString().toLowerCase();
			if (lowerCaseFileName.startsWith(documentRoot)) {
				localhostURL = localhostURL.substring(documentRoot.length());
			} else {
				return null;
			}
			// return store.getString(PHPeclipsePlugin.LOCALHOST_PREF) +
			// localhostURL;
			String projectPath = ProjectPrefUtil.getMiscProjectsPreferenceValue(file
                    .getProject(), IPreferenceConstants.PHP_LOCALHOST_PREF);
			if(projectPath.endsWith("/") && localhostURL.startsWith("/")) {
			    localhostURL = localhostURL.substring(1);
			}
			return projectPath + localhostURL;
		}
		return "http://localhost";
	}
}