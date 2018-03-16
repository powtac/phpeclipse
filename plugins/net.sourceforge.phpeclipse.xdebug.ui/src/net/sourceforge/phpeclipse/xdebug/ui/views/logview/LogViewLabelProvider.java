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

import net.sourceforge.phpeclipse.xdebug.ui.XDebugUIPluginImages;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class LogViewLabelProvider
	extends LabelProvider
	implements ITableLabelProvider {
	private Image infoImage;
	private Image errorImage;
	private Image warningImage;
	private Image errorWithStackImage;

	public LogViewLabelProvider() {
		errorImage = XDebugUIPluginImages.get(XDebugUIPluginImages.IMG_ERROR_ST_OBJ);
		warningImage = XDebugUIPluginImages.get(XDebugUIPluginImages.IMG_WARNING_ST_OBJ);
		infoImage = XDebugUIPluginImages.get(XDebugUIPluginImages.IMG_INFO_ST_OBJ);
		errorWithStackImage = XDebugUIPluginImages.get(XDebugUIPluginImages.IMG_ERROR_STACK_OBJ);
	}
	public void dispose() {
		errorImage.dispose();
		infoImage.dispose();
		warningImage.dispose();
		errorWithStackImage.dispose();
		super.dispose();
	}
	public Image getColumnImage(Object element, int columnIndex) {
		LogEntry entry = (LogEntry) element;
		if (columnIndex == 1) {
			switch (entry.getSeverity()) {
				case IStatus.INFO :
					return infoImage;
				case IStatus.WARNING :
					return warningImage;
				case IStatus.ERROR :
					return (entry.getStack() == null ? errorImage : errorWithStackImage);
			}
		}
		return null;
	}
	
	public String getColumnText(Object element, int columnIndex) {
		LogEntry entry = (LogEntry) element;
		switch (columnIndex) {
			case 2 :
				return entry.getMessage();
			case 3 :
				return entry.getPluginId();
			case 4 :
				return entry.getDate();
		}
		return ""; //$NON-NLS-1$
	}
}
