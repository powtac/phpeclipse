/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.phpeclipse.xdebug.ui.views.logview;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import net.sourceforge.phpeclipse.xdebug.ui.XDebugUIPlugin;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class LogEntry extends PlatformObject implements IWorkbenchAdapter {
	private static final String KEY_ERROR = "LogView.severity.error"; //$NON-NLS-1$
	private static final String KEY_WARNING = "LogView.severity.warning"; //$NON-NLS-1$
	private static final String KEY_INFO = "LogView.severity.info"; //$NON-NLS-1$
	private ArrayList children;
	private LogEntry parent;
	private String pluginId;
	private int severity;
	private int code;
	private String date;
	private String message;
	private String stack;
	private LogSession session;

	public LogEntry() {
	}

	public LogSession getSession() {
		return session;
	}

	void setSession(LogSession session) {
		this.session = session;
	}

	public LogEntry(IStatus status) {
		processStatus(status);
	}
	public int getSeverity() {
		return severity;
	}

	public boolean isOK() {
		return severity == IStatus.OK;
	}
	public int getCode() {
		return code;
	}
	public String getPluginId() {
		return pluginId;
	}
	public String getMessage() {
		return message;
	}
	public String getStack() {
		return stack;
	}
	public String getDate() {
		return date;
	}
	public String getSeverityText() {
		return getSeverityText(severity);
	}
	public boolean hasChildren() {
		return children != null && children.size() > 0;
	}
	public String toString() {
		return getSeverityText();
	}
	/**
	 * @see IWorkbenchAdapter#getChildren(Object)
	 */
	public Object[] getChildren(Object parent) {
		if (children == null)
			return new Object[0];
		return children.toArray();
	}

	/**
	 * @see IWorkbenchAdapter#getImageDescriptor(Object)
	 */
	public ImageDescriptor getImageDescriptor(Object arg0) {
		return null;
	}

	/**
	 * @see IWorkbenchAdapter#getLabel(Object)
	 */
	public String getLabel(Object obj) {
		return getSeverityText();
	}

	/**
	 * @see IWorkbenchAdapter#getParent(Object)
	 */
	public Object getParent(Object obj) {
		return parent;
	}

	void setParent(LogEntry parent) {
		this.parent = parent;
	}

	private String getSeverityText(int severity) {
		switch (severity) {
			case IStatus.ERROR :
				return XDebugUIPlugin.getString(KEY_ERROR);
			case IStatus.WARNING :
				return XDebugUIPlugin.getString(KEY_WARNING);
			case IStatus.INFO :
				return XDebugUIPlugin.getString(KEY_INFO);
		}
		return "?"; //$NON-NLS-1$
	}

	int processLogLine(String line, boolean root) {
		//!ENTRY <pluginID> <severity> <code> <date>
		//!SUBENTRY <depth> <pluginID> <severity> <code> <date>
		StringTokenizer stok = new StringTokenizer(line, " ", true); //$NON-NLS-1$
		StringBuffer dateBuffer = new StringBuffer();

		int dateCount = 5;
		int depth = 0;
		for (int i = 0; stok.hasMoreTokens();) {
			String token = stok.nextToken();
			if (i >= dateCount) {
				dateBuffer.append(token);
				continue;
			} else if (token.equals(" ")) //$NON-NLS-1$
				continue;
			switch (i) {
				case 0 : // entry or subentry
					if (root)
						i += 2;
					else
						i++;
					break;
				case 1 : // depth
					depth = parseInteger(token);
					i++;
					break;
				case 2 :
					pluginId = token;
					i++;
					break;
				case 3 : // severity
					severity = parseInteger(token);
					i++;
					break;
				case 4 : // code
					code = parseInteger(token);
					i++;
					break;
			}
		}
		date = dateBuffer.toString().trim();
		return depth;
	}

	private int parseInteger(String token) {
		try {
			return Integer.parseInt(token);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	void setStack(String stack) {
		this.stack = stack;
	}
	void setMessage(String message) {
		this.message = message;
	}

	private void processStatus(IStatus status) {
		pluginId = status.getPlugin();
		severity = status.getSeverity();
		code = status.getCode();
		DateFormat formatter = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss.SS"); //$NON-NLS-1$
		date = formatter.format(new Date());
		message = status.getMessage();
		Throwable throwable = status.getException();
		if (throwable != null) {
			StringWriter swriter = new StringWriter();
			PrintWriter pwriter = new PrintWriter(swriter);
			throwable.printStackTrace(pwriter);
			pwriter.flush();
			pwriter.close();
			stack = swriter.toString();
		}
		IStatus[] schildren = status.getChildren();
		if (schildren.length > 0) {
			children = new ArrayList();
			for (int i = 0; i < schildren.length; i++) {
				LogEntry child = new LogEntry(schildren[i]);
				addChild(child);
			}
		}
	}
	void addChild(LogEntry child) {
		if (children == null)
			children = new ArrayList();
		children.add(child);
		child.setParent(this);
	}
	public void write(PrintWriter writer) {
		writer.print(getSeverityText());
		if (date != null) {
			writer.print(" "); //$NON-NLS-1$
			writer.print(getDate());
		}
		if (message != null) {
			writer.print(" "); //$NON-NLS-1$
			writer.print(getMessage());
		}
		writer.println();
		if (stack != null)
			writer.println(stack);
	}
}
