/***********************************************************************************************************************************
 * Copyright (c) 2000, 2002 IBM Corp. and others. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors: IBM Corporation - Initial implementation
 *               www.phpeclipse.de
 **********************************************************************************************************************************/
package net.sourceforge.phpeclipse.actions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import net.sourceforge.phpeclipse.PHPeclipsePlugin;
import net.sourceforge.phpeclipse.ui.IPreferenceConstants;
import net.sourceforge.phpeclipse.ui.editor.ShowExternalPreviewAction;
import net.sourceforge.phpeclipse.ui.overlaypages.ProjectPrefUtil;
import net.sourceforge.phpeclipse.webbrowser.IWebBrowser;
import net.sourceforge.phpeclipse.webbrowser.internal.BrowserManager;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class PHPEclipseShowAction implements IObjectActionDelegate {
	private IWorkbenchPart workbenchPart;

	/**
	 * Constructor for Action1.
	 */
	public PHPEclipseShowAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		workbenchPart = targetPart;
	}

	public void run(IAction action) {
		ISelectionProvider selectionProvider = null;
		selectionProvider = workbenchPart.getSite().getSelectionProvider();
		StructuredSelection selection = null;
		selection = (StructuredSelection) selectionProvider.getSelection();
		IPreferenceStore store = PHPeclipsePlugin.getDefault()
				.getPreferenceStore();
		Shell shell = null;
		Iterator iterator = null;
		iterator = selection.iterator();
		while (iterator.hasNext()) {
			// obj => selected object in the view
			Object obj = iterator.next();
			// is it a resource
			if (obj instanceof IResource) {
				IResource resource = (IResource) obj;
				// check if it's a file resource
				switch (resource.getType()) {
				case IResource.FILE:
					// single file:
					IFile previewFile = (IFile) resource;
					String extension = previewFile.getFileExtension()
							.toLowerCase();
					boolean bringToTopPreview = ProjectPrefUtil
							.getPreviewBooleanValue(
									previewFile,
									IPreferenceConstants.PHP_BRING_TO_TOP_PREVIEW_DEFAULT);
					// boolean showHTMLFilesLocal =
					// ProjectPrefUtil.getPreviewBooleanValue(previewFile,
					// IPreferenceConstants.PHP_SHOW_HTML_FILES_LOCAL);
					// boolean showXMLFilesLocal =
					// ProjectPrefUtil.getPreviewBooleanValue(previewFile,
					// IPreferenceConstants.PHP_SHOW_XML_FILES_LOCAL);
					boolean isHTMLFileName = "html".equals(extension)
							|| "htm".equals(extension)
							|| "xhtml".equals(extension);
					boolean isXMLFileName = "xml".equals(extension)
							|| "xsd".equals(extension)
							|| "dtd".equals(extension);

					String localhostURL;
					// if (showHTMLFilesLocal && isHTMLFileName) {
					// localhostURL =
					// "file://"+previewFile.getLocation().toString();
					// } else if (showXMLFilesLocal && isXMLFileName) {
					// localhostURL =
					// "file://"+previewFile.getLocation().toString();
					// } else
					if ((localhostURL = ShowExternalPreviewAction
							.getLocalhostURL(store, previewFile)) == null) {
						MessageDialog
								.openInformation(shell,
										"Couldn't create localhost URL",
										"Please configure your localhost and documentRoot");
						return;
					}

					try {
						// if
						// (store.getBoolean(PHPeclipsePlugin.USE_EXTERNAL_BROWSER_PREF))
						// {
						// String[] arguments = { localhostURL };
						// MessageFormat form = new
						// MessageFormat(store.getString(PHPeclipsePlugin.EXTERNAL_BROWSER_PREF));
						// Runtime runtime = Runtime.getRuntime();
						// String command = form.format(arguments);
						// // console.write("External Browser command: " +
						// command + "\n");
						// runtime.exec(command);
						// } else {
						open(new URL(localhostURL), shell, localhostURL);
						// }
					} catch (MalformedURLException e) {
						MessageDialog.openInformation(shell,
								"MalformedURLException: ", e.toString());
					}
				}
			}
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	public static void open(final URL url, final Shell shell,
			final String dialogTitle) {
		// if (WebBrowserUtil.canUseInternalWebBrowser()) {
		// IWorkbenchPage page = PHPeclipsePlugin.getActivePage();
		// try {
		// IViewPart part = page.findView(BrowserView.ID_BROWSER);
		// if (part == null) {
		// part = page.showView(BrowserView.ID_BROWSER);
		// } else {
		// page.bringToTop(part);
		// }
		// ((BrowserView) part).setUrl(url.toExternalForm());
		// } catch (Exception e) {
		// }
		// } else {
		BrowserManager manager = BrowserManager.getInstance();
		IWebBrowser browser = manager.getCurrentWebBrowser();
		browser.openURL(url);
		// }
	}
}