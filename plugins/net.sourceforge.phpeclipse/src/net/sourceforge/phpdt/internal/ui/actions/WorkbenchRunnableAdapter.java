/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.phpdt.internal.ui.actions;

import java.lang.reflect.InvocationTargetException;

import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.operation.IRunnableWithProgress;

/**
 * An <code>IRunnableWithProgress</code> that adapts and
 * <code>IWorkspaceRunnable</code> so that is can be executed inside
 * <code>IRunnableContext</code>. <code>OperationCanceledException</code>
 * thrown by the apapted runnabled are cought and rethrown as a
 * <code>InterruptedException</code>.
 */
public class WorkbenchRunnableAdapter implements IRunnableWithProgress {

	private IWorkspaceRunnable fWorkspaceRunnable;

	public WorkbenchRunnableAdapter(IWorkspaceRunnable runnable) {
		fWorkspaceRunnable = runnable;
	}

	/*
	 * @see IRunnableWithProgress#run(IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		try {
			PHPeclipsePlugin.run(fWorkspaceRunnable, monitor);
		} catch (OperationCanceledException e) {
			throw new InterruptedException(e.getMessage());
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		}
	}

}
