package net.sourceforge.phpeclipse.webbrowser.internal;

/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 �*
 * Contributors:
 *    IBM - Initial API and implementation
 **********************************************************************/
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.phpeclipse.webbrowser.IExternalWebBrowserWorkingCopy;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 */
public class BrowserSearcher {
	private static boolean cancelled;

	private BrowserSearcher() {
		super();
	}

	/**
	 * Search for installed VMs in the file system
	 */
	protected static List search(Shell shell) {
		final List foundBrowsers = new ArrayList();
		final List existingPaths = WebBrowserUtil.getExternalBrowserPaths();

		// select a target directory for the search
		DirectoryDialog dialog = new DirectoryDialog(shell);
		dialog.setMessage(WebBrowserUIPlugin.getResource("%selectDirectory"));
		dialog.setText(WebBrowserUIPlugin.getResource("%directoryDialogTitle"));

		String path = dialog.open();
		if (path == null)
			return null;

		cancelled = false;

		final File rootDir = new File(path);
		ProgressMonitorDialog pm = new ProgressMonitorDialog(shell);

		IRunnableWithProgress r = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				monitor.beginTask(WebBrowserUIPlugin
						.getResource("%searchingTaskName"),
						IProgressMonitor.UNKNOWN);
				search(rootDir, existingPaths, foundBrowsers, monitor);
				monitor.done();
				if (monitor.isCanceled())
					setCancelled(true);
			}
		};

		try {
			pm.run(true, true, r);
		} catch (InvocationTargetException e) {
			Trace.trace(Trace.SEVERE,
					"Invocation Exception occured running monitor: " + e);
		} catch (InterruptedException e) {
			Trace.trace(Trace.SEVERE,
					"Interrupted exception occured running monitor: " + e);
			return null;
		}

		if (cancelled)
			return null;

		return foundBrowsers;
	}

	protected static void setCancelled(boolean b) {
		cancelled = b;
	}

	protected static void search(File directory, List existingPaths,
			List foundBrowsers, IProgressMonitor monitor) {
		if (monitor.isCanceled())
			return;

		String[] names = directory.list();
		List subDirs = new ArrayList();

		for (int i = 0; i < names.length; i++) {
			if (monitor.isCanceled())
				return;

			File file = new File(directory, names[i]);

			if (existingPaths.contains(file.getAbsolutePath().toLowerCase()))
				continue;

			IExternalWebBrowserWorkingCopy wc = WebBrowserUtil
					.createExternalBrowser(file);
			if (wc != null)
				foundBrowsers.add(wc);

			try {
				monitor.subTask(MessageFormat.format(WebBrowserUIPlugin
						.getResource("%searching"), new String[] {
						Integer.toString(foundBrowsers.size()),
						file.getCanonicalPath() }));
			} catch (IOException ioe) {
			}

			if (file.isDirectory()) {
				if (monitor.isCanceled())
					return;
				subDirs.add(file);
			}
		}
		while (!subDirs.isEmpty()) {
			File subDir = (File) subDirs.remove(0);
			search(subDir, existingPaths, foundBrowsers, monitor);
			if (monitor.isCanceled()) {
				return;
			}
		}
	}
}