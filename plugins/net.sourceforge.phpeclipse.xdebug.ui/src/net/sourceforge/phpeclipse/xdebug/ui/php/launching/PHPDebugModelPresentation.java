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
package net.sourceforge.phpeclipse.xdebug.ui.php.launching;

import java.io.File;
import java.util.HashMap;

import net.sourceforge.phpeclipse.xdebug.php.model.XDebugLineBreakpoint;

import net.sourceforge.phpeclipse.xdebug.php.model.XDebugTarget;
import net.sourceforge.phpeclipse.xdebug.php.model.XDebugThread;
import net.sourceforge.phpeclipse.xdebug.php.model.XDebugStackFrame;
import net.sourceforge.phpeclipse.xdebug.php.model.XDebugVariable;
import net.sourceforge.phpeclipse.xdebug.php.model.XDebugValue;
import net.sourceforge.phpeclipse.xdebug.ui.XDebugUIPluginImages;
//import net.sourceforge.phpeclipse.xdebug.ui.XDebugUIPlugin;
//import net.sourceforge.phpeclipse.xdebug.ui.php.launching.CopyOfPHPDebugModelPresentation.StorageEditorInput;

//import net.sourceforge.phpdt.internal.debug.core.model.IPHPDebugTarget;

//import net.sourceforge.phpdt.internal.debug.core.model.PHPThread;
//import net.sourceforge.phpdt.internal.debug.core.model.PHPValue;
//import net.sourceforge.phpdt.internal.debug.core.model.PHPVariable;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
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
	public IEditorInput getEditorInput(Object element) {

		if (element instanceof IFile) {
			return new FileEditorInput((IFile)element);
		}
		if( element instanceof LocalFileStorage) {
			LocalFileStorage lfc= (LocalFileStorage)element;
			return new StorageEditorInput(lfc,lfc.getFile());
		}
		if (element instanceof ILineBreakpoint) {
			return new FileEditorInput((IFile)((ILineBreakpoint)element).getMarker().getResource());
		}
		return null;
	}
	
	/**
	 * @see IDebugModelPresentation#getImage(Object)
	 */
	public Image getImage(Object element) {
		if (element instanceof XDebugLineBreakpoint) {
			return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_BREAKPOINT);
		} else if (element instanceof IMarker) {
			if (((IMarker) element).getAttribute(IBreakpoint.ENABLED, false)) {
				return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_BREAKPOINT);
			} else {
				return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_BREAKPOINT_DISABLED);				
			}
		} else if (element instanceof XDebugStackFrame
				|| element instanceof XDebugThread
				|| element instanceof XDebugTarget) {
			return getDebugElementImage(element);
		} else if (element instanceof XDebugVariable) {
			return getVariableImage((XDebugVariable) element);
		} else if (element instanceof XDebugValue) {
			return getValueImage((XDebugValue) element);
		}
		return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_BREAKPOINT);
	}

	private Image getVariableImage(XDebugVariable phpVar) {
		if (phpVar.getVisibility().equals("protected")) {
			return XDebugUIPluginImages.get(XDebugUIPluginImages.IMG_FIELD_PROTECTED);			
		}  else if (phpVar.getVisibility().equals("private")) {
			return (XDebugUIPluginImages.get(XDebugUIPluginImages.IMG_FIELD_PRIVATE));			
		}

		return XDebugUIPluginImages.get(XDebugUIPluginImages.IMG_FIELD_PUBLIC);			
	}
	private Image getValueImage(XDebugValue phpVar) {
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
			if (element instanceof XDebugLineBreakpoint) {
				return getBreakpointText((IBreakpoint) element);
			} else if (element instanceof XDebugVariable) {
				XDebugVariable phpVar = (XDebugVariable) element;
				return phpVar.getName() + "= " + phpVar.getValueString();//toString();
			}
		} catch (CoreException e) {
			//return PHPDebugUiMessages
				//	.getString("PHPDebugModelPresentation.<not responding>"); //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * @see IDebugModelPresentation#computeDetail(IValue, IValueDetailListener)
	 */
	public void computeDetail(IValue value, IValueDetailListener listener) {
		String detail = "";
		try {
			detail = value.getValueString();
		} catch (DebugException e) {
		}
		listener.detailComputed(value, detail);
		//return;
	}

	protected IBreakpoint getBreakpoint(IMarker marker) {
		return DebugPlugin.getDefault().getBreakpointManager().getBreakpoint(
				marker);
	}

	protected String getBreakpointText(IBreakpoint breakpoint)
			throws CoreException {
		if (breakpoint instanceof XDebugLineBreakpoint) {
			return getLineBreakpointText((XDebugLineBreakpoint) breakpoint);
		}
		return ""; //$NON-NLS-1$
	}

	protected String getLineBreakpointText(XDebugLineBreakpoint breakpoint)
			throws CoreException {
		StringBuffer label = new StringBuffer();

		label.append(breakpoint.getMarker().getResource().getFullPath());
		label.append(" ["); //$NON-NLS-1$
		//label.append(PHPDebugUiMessages
			//	.getString("PHPDebugModelPresentation.line")); //$NON-NLS-1$
		label.append(' ');
		label.append(breakpoint.getLineNumber());
		label.append(']');

		/*if (breakpoint.getHitCount() > 0) {
			label.append(" [skip count ");
			label.append(breakpoint.getHitCount());
			label.append(']');
		}*/

		/*if (breakpoint.isConditionEnabled()) {
			label.append(" [conditional]");
		}*/

		return label.toString();
	}

	/**
	 * Returns the image associated with the given element or <code>null</code>
	 * if none is defined.
	 */
	protected Image getDebugElementImage(Object element) {
		Image image = null;
		if (element instanceof XDebugThread) {
			XDebugThread thread = (XDebugThread) element;
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
		} else if (element instanceof XDebugStackFrame) {
			image = DebugUITools
					.getImage(IDebugUIConstants.IMG_OBJS_STACKFRAME);
		} else if (element instanceof XDebugTarget) {
			XDebugTarget debugTarget = (XDebugTarget) element;
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

    class StorageEditorInput extends PlatformObject implements
	IStorageEditorInput {
private File fFile;

private IStorage fStorage;

public StorageEditorInput(IStorage storage, File file) {
	super();
	fStorage = storage;
	fFile = file;
}

public IStorage getStorage() {
	return fStorage;
}

public ImageDescriptor getImageDescriptor() {
	return null;
}

public String getName() {
	return getStorage().getName();
}

public IPersistableElement getPersistable() {
	return null;
}

public String getToolTipText() {
	return getStorage().getFullPath().toOSString();
}

public boolean equals(Object object) {
	return object instanceof StorageEditorInput
			&& getStorage().equals(
					((StorageEditorInput) object).getStorage());
}

public int hashCode() {
	return getStorage().hashCode();
}

public boolean exists() {
	return fFile.exists();
}
}
}