/**********************************************************************
 Copyright (c) 2000, 2002 IBM Corp. and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html

 Contributors:
 IBM Corporation - Initial implementation
 Vicente Fernando - www.alfersoft.com.ar
 **********************************************************************/
package net.sourceforge.phpdt.internal.debug.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.phpdt.debug.core.PHPDebugModel;
import net.sourceforge.phpdt.internal.debug.ui.PHPDebugUiMessages;
import net.sourceforge.phpdt.internal.debug.ui.PHPDebugUiPlugin;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.IUpdate;

public class PHPManageBreakpointRulerAction extends Action implements IUpdate {

	private IVerticalRulerInfo fRuler;

	private ITextEditor fTextEditor;

	private String fMarkerType;

	private List fMarkers;

	private String fAddLabel;

	private String fRemoveLabel;

	public PHPManageBreakpointRulerAction(IVerticalRulerInfo ruler,
			ITextEditor editor) {
		fRuler = ruler;
		fTextEditor = editor;
		fMarkerType = IBreakpoint.BREAKPOINT_MARKER;
		fAddLabel = PHPDebugUiMessages
				.getString("PHPManageBreakpointRulerAction.ToggleBreakpoint"); //$NON-NLS-1$
		fRemoveLabel = PHPDebugUiMessages
				.getString("PHPManageBreakpointRulerAction.ToggleBreakpoint"); //$NON-NLS-1$
	}

	/**
	 * Returns the resource for which to create the marker, or <code>null</code>
	 * if there is no applicable resource.
	 * 
	 * @return the resource for which to create the marker or <code>null</code>
	 */
	protected IResource getResource() {
		IEditorInput input = fTextEditor.getEditorInput();

		IResource resource = (IResource) input.getAdapter(IFile.class);

		if (resource == null) {
			resource = (IResource) input.getAdapter(IResource.class);
		}

		return resource;
	}

	/**
	 * Checks whether a position includes the ruler's line of activity.
	 * 
	 * @param position
	 *            the position to be checked
	 * @param document
	 *            the document the position refers to
	 * @return <code>true</code> if the line is included by the given position
	 */
	protected boolean includesRulerLine(Position position, IDocument document) {

		if (position != null) {
			try {
				int markerLine = document.getLineOfOffset(position.getOffset());
				int line = fRuler.getLineOfLastMouseButtonActivity();
				if (line == markerLine) {
					return true;
				}
			} catch (BadLocationException x) {
			}
		}

		return false;
	}

	/**
	 * Returns this action's vertical ruler info.
	 * 
	 * @return this action's vertical ruler
	 */
	protected IVerticalRulerInfo getVerticalRulerInfo() {
		return fRuler;
	}

	/**
	 * Returns this action's editor.
	 * 
	 * @return this action's editor
	 */
	protected ITextEditor getTextEditor() {
		return fTextEditor;
	}

	/**
	 * Returns the <code>AbstractMarkerAnnotationModel</code> of the editor's
	 * input.
	 * 
	 * @return the marker annotation model
	 */
	protected AbstractMarkerAnnotationModel getAnnotationModel() {
		IDocumentProvider provider = fTextEditor.getDocumentProvider();
		IAnnotationModel model = provider.getAnnotationModel(fTextEditor
				.getEditorInput());
		if (model instanceof AbstractMarkerAnnotationModel) {
			return (AbstractMarkerAnnotationModel) model;
		}
		return null;
	}

	/**
	 * Returns the <code>IDocument</code> of the editor's input.
	 * 
	 * @return the document of the editor's input
	 */
	protected IDocument getDocument() {
		IDocumentProvider provider = fTextEditor.getDocumentProvider();
		return provider.getDocument(fTextEditor.getEditorInput());
	}

	/**
	 * @see IUpdate#update()
	 */
	public void update() {
		fMarkers = getMarkers();
		setText(fMarkers.isEmpty() ? fAddLabel : fRemoveLabel);
	}

	/**
	 * @see Action#run()
	 */
	public void run() {
		if (fMarkers.isEmpty()) {
			addMarker();
		} else {
			removeMarkers(fMarkers);
		}
	}

	protected List getMarkers() {

		List breakpoints = new ArrayList();

		IResource resource = getResource();
		IDocument document = getDocument();
		AbstractMarkerAnnotationModel model = getAnnotationModel();

		if (model != null) {
			try {

				IMarker[] markers = null;
				if (resource instanceof IFile)
					markers = resource.findMarkers(
							IBreakpoint.BREAKPOINT_MARKER, true,
							IResource.DEPTH_INFINITE);
				else {
					IWorkspaceRoot root = ResourcesPlugin.getWorkspace()
							.getRoot();
					markers = root.findMarkers(IBreakpoint.BREAKPOINT_MARKER,
							true, IResource.DEPTH_INFINITE);
				}

				if (markers != null) {
					IBreakpointManager breakpointManager = DebugPlugin
							.getDefault().getBreakpointManager();
					int iFe = 0;
					for (iFe = 0; iFe < markers.length; iFe++) {
						IBreakpoint breakpoint = breakpointManager
								.getBreakpoint(markers[iFe]);
						if (breakpoint != null
								&& breakpointManager.isRegistered(breakpoint)
								&& includesRulerLine(model
										.getMarkerPosition(markers[iFe]),
										document))
							breakpoints.add(markers[iFe]);
					}
				}
			} catch (CoreException x) {
				System.out.println(x.getStatus());
				// JDIDebugUIPlugin.log(x.getStatus());
			}
		}
		return breakpoints;
	}

	protected void addMarker() {

		// IResource resource= getResource();
		IEditorInput editorInput = getTextEditor().getEditorInput();
		IDocument document = getDocument();
		// IBreakpointManager breakpointManager=
		// DebugPlugin.getDefault().getBreakpointManager();

		int rulerLine = getVerticalRulerInfo()
				.getLineOfLastMouseButtonActivity();
		// create the marker
		try {
			// Falta verificar si la ubicaci?½n del Breakpoint es v?½lida
			int lineNumber = rulerLine + 1;

			if (lineNumber > 0) {
				IResource resource = ((IFileEditorInput) editorInput).getFile();
				if (PHPDebugModel.lineBreakpointExists(resource, lineNumber) == null) {
					// Map attributes = new HashMap(10);
					IRegion line = document.getLineInformation(lineNumber - 1);
					int start = line.getOffset();
					int lenline = line.getLength();
					// int end= start + ((lenline > 0)?lenline:0);
					int end = start + lenline;

					// PHPDebugModel.createLineBreakpoint(getResource(),
					// lineNumber, start, end, 0, true, attributes);
					PHPDebugModel.createLineBreakpoint(
							resource, lineNumber, start, end, 0, true, null);
					// PHPDebugModel.createLineBreakpoint(((IFileEditorInput)
					// editorInput).getFile(), lineNumber, 0, true, attributes);

				}
			}
		} catch (DebugException e) {
			System.out.println("Error");
		} catch (CoreException e) {
			System.out.println("Error");
		} catch (BadLocationException e) {
			System.out.println("Error");
		}
	}

	protected void removeMarkers(List markers) {
		IBreakpointManager breakpointManager = DebugPlugin.getDefault()
				.getBreakpointManager();
		try {
			Iterator e = markers.iterator();
			while (e.hasNext()) {
				IBreakpoint breakpoint = breakpointManager
						.getBreakpoint((IMarker) e.next());
				breakpointManager.removeBreakpoint(breakpoint, true);
			}
		} catch (CoreException e) {
		}
	}

	public IResource getUnderlyingResource(String fName) {
		IResource parentResource = getResource(); // fParent.getUnderlyingResource();
		if (parentResource == null) {
			return null;
		}
		int type = parentResource.getType();
		if (type == IResource.FOLDER || type == IResource.PROJECT) {
			IContainer folder = (IContainer) parentResource;
			IResource resource = folder.findMember(fName);
			if (resource == null) {
				// throw newNotPresentException();
				return null;
			} else {
				return resource;
			}
		} else {
			return parentResource;
		}
	}

}
