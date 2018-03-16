package net.sourceforge.phpeclipse.ui.editor;

import java.net.MalformedURLException;
import java.net.URL;

import net.sourceforge.phpeclipse.ui.IPreferenceConstants;
import net.sourceforge.phpeclipse.ui.WebUI;
import net.sourceforge.phpeclipse.ui.internal.WebUIMessages;
import net.sourceforge.phpeclipse.ui.overlaypages.ProjectPrefUtil;
import net.sourceforge.phpeclipse.webbrowser.views.BrowserView;
import net.sourceforge.phpeclipse.webbrowser.views.ShowInContextBrowser;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.internal.Perspective;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;

public class BrowserUtil {

	public static ShowInContextBrowser getShowInContext(IFile previewFile,
			boolean forceDBGPreview, String postFix) {
		String extension = previewFile.getFileExtension().toLowerCase();
		// boolean showHTMLFilesLocal = false;
		// boolean showXMLFilesLocal = false;
		boolean isHTMLFileName = false;
		boolean isXMLFileName = false;
		String localhostURL;
		if (!forceDBGPreview) {
			// showHTMLFilesLocal =
			// ProjectPrefUtil.getPreviewBooleanValue(previewFile,
			// IPreferenceConstants.PHP_SHOW_HTML_FILES_LOCAL);
			// showXMLFilesLocal =
			// ProjectPrefUtil.getPreviewBooleanValue(previewFile,
			// IPreferenceConstants.PHP_SHOW_XML_FILES_LOCAL);
			isHTMLFileName = "html".equals(extension)
					|| "htm".equals(extension) || "xhtml".equals(extension);
			isXMLFileName = "xml".equals(extension) || "xsd".equals(extension)
					|| "dtd".equals(extension);
		}
		// if (showHTMLFilesLocal && isHTMLFileName) {
		// localhostURL = previewFile.getLocation().toString();
		// } else if (showXMLFilesLocal && isXMLFileName) {
		// localhostURL = previewFile.getLocation().toString();
		// } else
		if ((localhostURL = ShowExternalPreviewAction.getLocalhostURL(null,
				previewFile)) == null) {
			return new ShowInContextBrowser(previewFile, null, null);
		}
		localhostURL += postFix;
		return new ShowInContextBrowser(previewFile, null, localhostURL);
	}

	/**
	 * Returns the <code>IShowInTarget</code> for the given part, or
	 * <code>null</code> if it does not provide one.
	 * 
	 * @param targetPart
	 *            the target part
	 * @return the <code>IShowInTarget</code> or <code>null</code>
	 */
	private static IShowInTarget getShowInTarget(IWorkbenchPart targetPart) {
		if (targetPart instanceof IShowInTarget) {
			return (IShowInTarget) targetPart;
		}
		Object o = targetPart.getAdapter(IShowInTarget.class);
		if (o instanceof IShowInTarget) {
			return (IShowInTarget) o;
		}
		return null;
	}

	public static void showPreview(IFile previewFile, boolean forceDBGPreview,
			String postFix) {
		if (previewFile == null) {
			// should never happen
			return;
		}
		IWorkbenchPage page = WebUI.getActivePage();
		if (page != null && page.isEditorAreaVisible()) {
			// String extension = previewFile.getFileExtension().toLowerCase();
			boolean autoPreview = forceDBGPreview;
			// boolean showHTMLFilesLocal = false;
			// boolean showXMLFilesLocal = false;
			// boolean isHTMLFileName = false;
			// boolean isXMLFileName = false;
			if (!forceDBGPreview) {
				autoPreview = ProjectPrefUtil.getPreviewBooleanValue(
						previewFile,
						IPreferenceConstants.PHP_AUTO_PREVIEW_DEFAULT);

				// showHTMLFilesLocal =
				// ProjectPrefUtil.getPreviewBooleanValue(previewFile,
				// IPreferenceConstants.PHP_SHOW_HTML_FILES_LOCAL);
				// showXMLFilesLocal =
				// ProjectPrefUtil.getPreviewBooleanValue(previewFile,
				// IPreferenceConstants.PHP_SHOW_XML_FILES_LOCAL);
				// isHTMLFileName = "html".equals(extension) ||
				// "htm".equals(extension)
				// || "xhtml".equals(extension);
				// isXMLFileName = "xml".equals(extension) ||
				// "xsd".equals(extension) ||
				// "dtd".equals(extension);
			}
			if (autoPreview) {
				// String localhostURL;
				// if (showHTMLFilesLocal && isHTMLFileName) {
				// localhostURL = previewFile.getLocation().toString();
				// } else if (showXMLFilesLocal && isXMLFileName) {
				// localhostURL = previewFile.getLocation().toString();
				// } else if ((localhostURL =
				// ShowExternalPreviewAction.getLocalhostURL(null, previewFile))
				// ==
				// null) {
				// return;
				// }
				// localhostURL += postFix;
				ShowInContext context = getShowInContext(previewFile,
						forceDBGPreview, postFix);
				IWorkbenchPart sourcePart = page.getActivePart();
				if (sourcePart == null && context != null) {
					return;
				}

				// try {
				Perspective persp = ((WorkbenchPage) page)
						.getActivePerspective();
				if (persp != null) {

					// If this view is already visible just return.
					IViewReference ref = persp.findView(BrowserView.ID_BROWSER,
							null);
					IViewPart view = null;
					if (ref != null) {
						view = ref.getView(true);
					}
					if (view == null && forceDBGPreview) {
						try {
							view = persp.showView(BrowserView.ID_BROWSER, null);
							persp.bringToTop(persp.findView(
									BrowserView.ID_BROWSER, null));
						} catch (PartInitException e) {
							WebUI.log(e);
						}
					}
					if (view != null) {
						IShowInTarget target = getShowInTarget(view);
						boolean stickyBrowserURL = ProjectPrefUtil.getPreviewBooleanValue(
								previewFile,
								IPreferenceConstants.PHP_STICKY_BROWSER_URL_DEFAULT);
						if (target != null) {
							if (stickyBrowserURL
									&& ((BrowserView) target).getUrl() != null
									&& ((BrowserView) target).getUrl().length() > 0) {
								((BrowserView) target).refresh();
							} else {
								target.show(context);
							}
						}
						((WorkbenchPage) page)
								.performedShowIn(BrowserView.ID_BROWSER);
					}
				}

				// IViewPart view = page.showView(BrowserView.ID_BROWSER);
				// IShowInTarget target = getShowInTarget(view);
				// if (target != null && target.show(new
				// ShowInContext(localhostURL,
				// null))) {
				// // success
				// }
				// ((WorkbenchPage)
				// page).performedShowIn(BrowserView.ID_BROWSER); //
				// TODO: move back up
				// } catch (PartInitException e) {
				// WorkbenchPlugin.log(
				// "Error showing view in ShowInAction.run", e.getStatus());
				// //$NON-NLS-1$
				// }

				// try {
				// IViewPart part = page.showView(BrowserView.ID_BROWSER, null,
				// IWorkbenchPage.VIEW_VISIBLE);
				// if (part == null) {
				// part = page.showView(BrowserView.ID_BROWSER);
				// } else {
				// page.bringToTop(part);
				// }
				// ((BrowserView) part).setUrl(localhostURL);
				//
				// } catch (Exception e) {
				// // PHPeclipsePlugin.log(e);
				// }
			}
		}
	}

	private static final String BROWSER_ID = "net.sourceforge.phpeclipse.browser";

	/**
	 * convenient method to show browser as Editor
	 * 
	 */
	public static void showBrowserAsEditor(IFile file, String queryString) {
		showBrowser(IWorkbenchBrowserSupport.AS_EDITOR, file, queryString);
	}

	/**
	 * convenient method to show browser as External Web Browser
	 * 
	 */
	public static void showBrowserAsExternal(IFile file, String queryString) {
		showBrowser(IWorkbenchBrowserSupport.AS_EXTERNAL, file, queryString);
	}

	/**
	 * convenient method to show browser as View
	 * 
	 */
	public static void showBrowserAsView(IFile file, String queryString) {
		showBrowser(IWorkbenchBrowserSupport.AS_VIEW, file, queryString);
	}

	/**
	 * Show browser according to General settings
	 * 
	 * See IWorkbenchBrowserSupport and DefaultWorkbenchBrowserSupport.
	 */
	public static void showBrowser(int style, IFile file, String queryString) {
		ShowInContextBrowser context = getShowInContext(file, true, queryString);
		String url = context.getLocalhostUrl();
		if (url == null) {
			String dialogTitle = WebUIMessages
					.getString("BrowserUtil.error.dialog.title");
			String message = WebUIMessages
					.getString("BrowserUtil.null.url.message");
			String reason = WebUIMessages
					.getString("BrowserUtil.null.url.reason");
			IStatus status = new Status(IStatus.ERROR, WebUI.PLUGIN_ID, 0,
					reason, null);
			ErrorDialog.openError(new Shell(), dialogTitle, message, status);
			return;
		}
		String id = BROWSER_ID;
		switch (style) {
		case IWorkbenchBrowserSupport.AS_EXTERNAL:
			id += ".x";
			break;
		case IWorkbenchBrowserSupport.AS_EDITOR:
			id += ".e";
			break;
		case IWorkbenchBrowserSupport.AS_VIEW:
			id += ".v";
			break;
		}
		style |= IWorkbenchBrowserSupport.LOCATION_BAR
				| IWorkbenchBrowserSupport.NAVIGATION_BAR
				| IWorkbenchBrowserSupport.STATUS;
		try {
			IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport()
					.createBrowser(style, id, null, url);
			browser.openURL(new URL(url));

		} catch (PartInitException e) {
			WebUI.log(e);
		} catch (MalformedURLException e) {
			WebUI.log(e);
		}
	}

}
