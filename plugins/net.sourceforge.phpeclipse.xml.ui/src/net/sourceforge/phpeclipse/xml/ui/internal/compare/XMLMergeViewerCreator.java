/*
 * Copyright (c) 2002-2004 Widespace, OU and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Igor Malinin - initial contribution
 *
 * $Id: XMLMergeViewerCreator.java,v 1.1 2004-09-02 18:28:05 jsurfer Exp $
 */

package net.sourceforge.phpeclipse.xml.ui.internal.compare;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.IViewerCreator;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Igor Malinin
 */
public class XMLMergeViewerCreator implements IViewerCreator {

	/*
	 * @see IViewerCreator#createViewer(Composite, CompareConfiguration)
	 */
	public Viewer createViewer(Composite parent, CompareConfiguration config) {
		return new XMLMergeViewer(parent, SWT.NULL, config);
	}

}
