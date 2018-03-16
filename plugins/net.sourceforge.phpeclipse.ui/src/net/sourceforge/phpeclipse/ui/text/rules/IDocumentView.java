/*
 * Copyright (c) 2002-2004 Widespace, OU and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://solareclipse.sourceforge.net/legal/cpl-v10.html
 * 
 * Contributors:
 *     Igor Malinin - initial contribution
 * 
 * $Id: IDocumentView.java,v 1.1 2004-09-02 18:26:29 jsurfer Exp $
 */

package net.sourceforge.phpeclipse.ui.text.rules;

import org.eclipse.jface.text.IDocument;

/**
 * View to part of parent document. Provides methods for translating character
 * offsets between this view and parent document.
 * 
 * @author Igor Malinin
 */
public interface IDocumentView extends IDocument {

	IDocument getParentDocument();

	int getParentOffset(int localOffset);

	int getLocalOffset(int parentOffset);
}
