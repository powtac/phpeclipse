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
package net.sourceforge.phpdt.internal.debug.ui;

import java.util.HashMap;

import net.sourceforge.phpdt.internal.debug.core.breakpoints.PHPLineBreakpoint;
import net.sourceforge.phpdt.internal.debug.core.model.IPHPDebugTarget;
import net.sourceforge.phpdt.internal.debug.core.model.PHPStackFrame;
import net.sourceforge.phpdt.internal.debug.core.model.PHPThread;
import net.sourceforge.phpdt.internal.debug.core.model.PHPValue;
import net.sourceforge.phpdt.internal.debug.core.model.PHPVariable;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 * @see IDebugModelPresentation
 */
public class PHPDebugModelPresentation extends LabelProvider implements
		IDebugModelPresentation {

	protected HashMap fAttributes = new HashMap(3);

	public PHPDebugModelPresentation() {
		super();
	}

	/**
	 * @see IDebugModelPresentation#getEditorId(IEditorInput, Object)
	 */
	public String getEditorId(IEditorInput input, Object inputObject) {
		IEditorRegistry registry = PlatformUI.getWorkbench()
				.getEditorRegistry();
		IEditorDescriptor descriptor = registry.getDefaultEditor(input
				.getName());
		if (descriptor != null)
			return descriptor.getId();

		return null;
	}

	/**
	 * @see IDebugModelPresentation#setAttribute(String, Object)
	 */
	public void setAttribute(String id, Object value) {
		if (value == null) {
			return;
		}
		fAttributes.put(id, value);
	}

	/**
	 * @see IDebugModelPresentation#getEditorInput(Object)
	 */
	public IEditorInput getEditorInput(Object item) {

		if (item instanceof PHPLineBreakpoint) {
			IBreakpoint bp = (IBreakpoint) item;
			IMarker ma = bp.getMarker();
			//IFile eclipseFile = PHPDebugUiPlugin.getWorkspace().getRoot()
			//		.getFileForLocation(ma.getResource().getLocation());
			IFile eclipseFile = PHPDebugUiPlugin.getWorkspace().getRoot()
					.getFile(ma.getResource().getFullPath());
			if (eclipseFile == null) {
				return null;
			}
			return new FileEditorInput(eclipseFile);
		}
		return null;
	}

	/**
	 * @see IDebugModelPresentation#getImage(Object)
	 */
	public Image getImage(Object element) {
		if (element instanceof PHPLineBreakpoint) {
			return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_BREAKPOINT);
		} else if (element instanceof IMarker) {
			return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_BREAKPOINT);
		} else if (element instanceof PHPStackFrame
				|| element instanceof PHPThread
				|| element instanceof IPHPDebugTarget) {
			return getDebugElementImage(element);
		} else if (element instanceof PHPVariable) {
			return getVariableImage((PHPVariable) element);
		} else if (element instanceof PHPValue) {
			return getValueImage((PHPValue) element);
		}
		return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_BREAKPOINT);
	}

	private Image getVariableImage(PHPVariable phpVar) {
		/*
		 * if (phpVar != null) { if (phpVar.isLocal()) return
		 * DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_VARIABLE); if
		 * (phpVar.isHashValue()) return
		 * DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_VARIABLE); }
		 */
		return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_VARIABLE);
	}

	private Image getValueImage(PHPValue phpVar) {
		if (phpVar != null) {
			return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_VARIABLE);
		}
		return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_VARIABLE);
	}

	/**
	 * @see IDebugModelPresentation#getText(Object)
	 */
	public String getText(Object element) {
		try {
			if (element instanceof PHPLineBreakpoint) {
				return getBreakpointText((IBreakpoint) element);
			} else if (element instanceof PHPVariable) {
				PHPVariable phpVar = (PHPVariable) element;
				return phpVar.toString();
			}
		} catch (CoreException e) {
			return PHPDebugUiMessages
					.getString("PHPDebugModelPresentation.<not responding>"); //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * @see IDebugModelPresentation#computeDetail(IValue, IValueDetailListener)
	 */
	public void computeDetail(IValue value, IValueDetailListener listener) {
		return;
	}

	protected IBreakpoint getBreakpoint(IMarker marker) {
		return DebugPlugin.getDefault().getBreakpointManager().getBreakpoint(
				marker);
	}

	protected String getBreakpointText(IBreakpoint breakpoint)
			throws CoreException {
		if (breakpoint instanceof PHPLineBreakpoint) {
			return getLineBreakpointText((PHPLineBreakpoint) breakpoint);
		}
		return ""; //$NON-NLS-1$
	}

	protected String getLineBreakpointText(PHPLineBreakpoint breakpoint)
			throws CoreException {
		StringBuffer label = new StringBuffer();

		label.append(breakpoint.getMarker().getResource().getFullPath());
		label.append(" ["); //$NON-NLS-1$
		label.append(PHPDebugUiMessages
				.getString("PHPDebugModelPresentation.line")); //$NON-NLS-1$
		label.append(' ');
		label.append(breakpoint.getLineNumber());
		label.append(']');

		if (breakpoint.getHitCount() > 0) {
			label.append(" [skip count ");
			label.append(breakpoint.getHitCount());
			label.append(']');
		}

		if (breakpoint.isConditionEnabled()) {
			label.append(" [conditional]");
		}

		return label.toString();
	}

	/**
	 * Returns the image associated with the given element or <code>null</code>
	 * if none is defined.
	 */
	protected Image getDebugElementImage(Object element) {
		Image image = null;
		if (element instanceof PHPThread) {
			PHPThread thread = (PHPThread) element;
			if (thread.isSuspended()) {
				image = DebugUITools
						.getImage(IDebugUIConstants.IMG_OBJS_THREAD_SUSPENDED);
			} else if (thread.isTerminated()) {
				image = DebugUITools
						.getImage(IDebugUIConstants.IMG_OBJS_THREAD_TERMINATED);
			} else {
				image = DebugUITools
						.getImage(IDebugUIConstants.IMG_OBJS_THREAD_RUNNING);
			}
		} else if (element instanceof PHPStackFrame) {
			image = DebugUITools
					.getImage(IDebugUIConstants.IMG_OBJS_STACKFRAME);
		} else if (element instanceof IPHPDebugTarget) {
			IPHPDebugTarget debugTarget = (IPHPDebugTarget) element;
			if (debugTarget.isTerminated()) {
				image = DebugUITools
						.getImage(IDebugUIConstants.IMG_OBJS_DEBUG_TARGET_TERMINATED);
			} else {
				image = DebugUITools
						.getImage(IDebugUIConstants.IMG_OBJS_DEBUG_TARGET);
			}
		}
		return image;
	}
}
