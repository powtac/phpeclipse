/**
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. � This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 �*
 * Contributors:
 *    IBM - Initial API and implementation
 */
package net.sourceforge.phpeclipse.webbrowser.internal;

import java.net.URL;

import net.sourceforge.phpeclipse.webbrowser.IWebBrowserEditorInput;
import net.sourceforge.phpeclipse.webbrowser.WebBrowserEditorInput;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.EditorPart;

/**
 * An integrated Web browser, defined as an editor to make better use of the
 * desktop.
 */
public class WebBrowserEditor extends EditorPart {
	public static final String WEB_BROWSER_EDITOR_ID = "net.sourceforge.phpeclipse.webbrowser";

	protected WebBrowser webBrowser;

	protected String initialURL;

	protected Image image;

	protected TextAction cutAction;

	protected TextAction copyAction;

	protected TextAction pasteAction;

	protected IResourceChangeListener resourceListener;

	/**
	 * WebBrowserEditor constructor comment.
	 */
	public WebBrowserEditor() {
		super();
	}

	/**
	 * Creates the SWT controls for this workbench part.
	 * <p>
	 * Clients should not call this method (the workbench calls this method at
	 * appropriate times).
	 * </p>
	 * <p>
	 * For implementors this is a multi-step process:
	 * <ol>
	 * <li>Create one or more controls within the parent.</li>
	 * <li>Set the parent layout as needed.</li>
	 * <li>Register any global actions with the <code>IActionService</code>.</li>
	 * <li>Register any popup menus with the <code>IActionService</code>.</li>
	 * <li>Register a selection provider with the
	 * <code>ISelectionService</code> (optional). </li>
	 * </ol>
	 * </p>
	 * 
	 * @param parent
	 *            the parent control
	 */
	public void createPartControl(Composite parent) {
		IWebBrowserEditorInput input = getWebBrowserEditorInput();

		if (input == null || input.isToolbarVisible() == false)
			webBrowser = new WebBrowser(parent, false, input
					.isStatusbarVisible());
		else {
			webBrowser = new WebBrowser(parent, true, input
					.isStatusbarVisible());
			cutAction = new TextAction(webBrowser, TextAction.CUT);
			copyAction = new TextAction(webBrowser, TextAction.COPY);
			pasteAction = new TextAction(webBrowser, TextAction.PASTE);
		}

		webBrowser.setURL(initialURL);
		webBrowser.editor = this;
	}

	public void dispose() {
		if (image != null && !image.isDisposed())
			image.dispose();
		image = null;

		if (resourceListener != null)
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(
					resourceListener);
	}

	/*
	 * (non-Javadoc) Saves the contents of this editor. <p> Subclasses must
	 * override this method to implement the open-save-close lifecycle for an
	 * editor. For greater details, see <code>IEditorPart</code> </p>
	 * 
	 * @see IEditorPart
	 */
	public void doSave(IProgressMonitor monitor) {
	}

	/*
	 * (non-Javadoc) Saves the contents of this editor to another object. <p>
	 * Subclasses must override this method to implement the open-save-close
	 * lifecycle for an editor. For greater details, see <code>IEditorPart</code>
	 * </p>
	 * 
	 * @see IEditorPart
	 */
	public void doSaveAs() {
	}

	/**
	 * Returns the copy action.
	 * 
	 * @return org.eclipse.jface.action.IAction
	 */
	public IAction getCopyAction() {
		return copyAction;
	}

	/**
	 * Returns the cut action.
	 * 
	 * @return org.eclipse.jface.action.IAction
	 */
	public IAction getCutAction() {
		return cutAction;
	}

	/**
	 * Returns the paste action.
	 * 
	 * @return org.eclipse.jface.action.IAction
	 */
	public IAction getPasteAction() {
		return pasteAction;
	}

	/**
	 * Returns the web editor input, if available.
	 * 
	 * @return net.sourceforge.phpeclipse.webbrowser.IWebBrowserEditorInput
	 */
	protected IWebBrowserEditorInput getWebBrowserEditorInput() {
		IEditorInput input = getEditorInput();
		if (input instanceof IWebBrowserEditorInput)
			return (IWebBrowserEditorInput) input;
		return null;
	}

	/*
	 * (non-Javadoc) Sets the cursor and selection state for this editor to the
	 * passage defined by the given marker. <p> Subclasses may override. For
	 * greater details, see <code>IEditorPart</code> </p>
	 * 
	 * @see IEditorPart
	 */
	public void gotoMarker(IMarker marker) {
	}

	/*
	 * (non-Javadoc) Initializes the editor part with a site and input. <p>
	 * Subclasses of <code>EditorPart</code> must implement this method.
	 * Within the implementation subclasses should verify that the input type is
	 * acceptable and then save the site and input. Here is sample code: </p>
	 * <pre> if (!(input instanceof IFileEditorInput)) throw new
	 * PartInitException("Invalid Input: Must be IFileEditorInput");
	 * setSite(site); setInput(editorInput); </pre>
	 */
	public void init(IEditorSite site, IEditorInput input) {
		Trace.trace(Trace.FINEST, "Opening browser: " + input);
		if (input instanceof IFileEditorInput) {
			IFileEditorInput fei = (IFileEditorInput) input;
			IFile file = fei.getFile();
			URL url = null;
			try {
				if (file != null && file.exists())
					url = file.getFullPath().toFile().toURL();
			} catch (Exception e) {
				Trace.trace(Trace.SEVERE, "Error getting URL to file");
			}
			addResourceListener(file);
			input = new WebBrowserEditorInput(url,
					WebBrowserEditorInput.SHOW_ALL
							| WebBrowserEditorInput.SAVE_URL);
		}
		if (input instanceof IWebBrowserEditorInput) {
			IWebBrowserEditorInput wbei = (IWebBrowserEditorInput) input;
			initialURL = null;
			if (wbei.getURL() != null)
				initialURL = wbei.getURL().toExternalForm();
			if (webBrowser != null) {
				webBrowser.setURL(initialURL);
				site.getWorkbenchWindow().getActivePage().bringToTop(this);
			}

			setPartName(wbei.getName());
			setTitleToolTip(wbei.getToolTipText());

			Image oldImage = image;
			ImageDescriptor id = wbei.getImageDescriptor();
			image = id.createImage();

			setTitleImage(image);
			if (oldImage != null && !oldImage.isDisposed())
				oldImage.dispose();
		}
		setSite(site);
		setInput(input);
	}

	/*
	 * (non-Javadoc) Returns whether the contents of this editor have changed
	 * since the last save operation. <p> Subclasses must override this method
	 * to implement the open-save-close lifecycle for an editor. For greater
	 * details, see <code>IEditorPart</code> </p>
	 * 
	 * @see IEditorPart
	 */
	public boolean isDirty() {
		return false;
	}

	/*
	 * (non-Javadoc) Returns whether the "save as" operation is supported by
	 * this editor. <p> Subclasses must override this method to implement the
	 * open-save-close lifecycle for an editor. For greater details, see <code>IEditorPart</code>
	 * </p>
	 * 
	 * @see IEditorPart
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * Returns true if this editor has a toolbar.
	 * 
	 * @return boolean
	 */
	public boolean isToolbarVisible() {
		IWebBrowserEditorInput input = getWebBrowserEditorInput();
		if (input == null || input.isToolbarVisible())
			return true;
		else
			return false;
	}

	/**
	 * Open the input in the internal Web browser.
	 */
	public static void open(IWebBrowserEditorInput input) {
		IWorkbenchWindow workbenchWindow = WebBrowserUIPlugin.getInstance()
				.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = workbenchWindow.getActivePage();

		try {
			IEditorReference[] editors = page.getEditorReferences();
			int size = editors.length;
			for (int i = 0; i < size; i++) {
				if (WEB_BROWSER_EDITOR_ID.equals(editors[i].getId())) {
					IEditorPart editor = editors[i].getEditor(true);
					if (editor != null && editor instanceof WebBrowserEditor) {
						WebBrowserEditor webEditor = (WebBrowserEditor) editor;
						if (input.canReplaceInput(webEditor
								.getWebBrowserEditorInput())) {
							editor.init(editor.getEditorSite(), input);
							return;
						}
					}
				}
			}

			page.openEditor(input, WebBrowserEditor.WEB_BROWSER_EDITOR_ID);
		} catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Error opening Web browser", e);
		}
	}

	/**
	 * Asks this part to take focus within the workbench.
	 * <p>
	 * Clients should not call this method (the workbench calls this method at
	 * appropriate times).
	 * </p>
	 */
	public void setFocus() {
		if (webBrowser != null) {
			if (webBrowser.combo != null)
				webBrowser.combo.setFocus();
			else
				webBrowser.browser.setFocus();
			webBrowser.updateHistory();
		}
	}

	/**
	 * Update the actions.
	 */
	protected void updateActions() {
		if (cutAction != null)
			cutAction.update();
		if (copyAction != null)
			copyAction.update();
		if (pasteAction != null)
			pasteAction.update();
	}

	/**
	 * Close the editor correctly.
	 */
	protected void closeEditor() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				getEditorSite().getPage().closeEditor(WebBrowserEditor.this,
						false);
			}
		});
	}

	/**
	 * Adds a resource change listener to see if the file is deleted.
	 */
	protected void addResourceListener(final IResource resource) {
		if (resource == null)
			return;

		resourceListener = new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				try {
					event.getDelta().accept(new IResourceDeltaVisitor() {
						public boolean visit(IResourceDelta delta) {
							IResource res = delta.getResource();

							if (res == null || !res.equals(resource))
								return true;

							if (delta.getKind() != IResourceDelta.REMOVED)
								return true;

							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									String title = WebBrowserUIPlugin
											.getResource("%dialogResourceDeletedTitle");
									String message = WebBrowserUIPlugin
											.getResource(
													"%dialogResourceDeletedMessage",
													resource.getName());
									String[] labels = new String[] {
											WebBrowserUIPlugin
													.getResource("%dialogResourceDeletedIgnore"),
											IDialogConstants.CLOSE_LABEL };
									MessageDialog dialog = new MessageDialog(
											getEditorSite().getShell(), title,
											null, message,
											MessageDialog.INFORMATION, labels,
											0);

									if (dialog.open() != 0)
										closeEditor();
								}
							});
							return false;
						}
					});
				} catch (Exception e) {
					Trace.trace(Trace.SEVERE,
							"Error listening for resource deletion", e);
				}
			}
		};
		ResourcesPlugin.getWorkspace().addResourceChangeListener(
				resourceListener);
	}
}