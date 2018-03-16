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
package net.sourceforge.phpdt.smarty.ui.internal.text;

import net.sourceforge.phpeclipse.ui.WebUI;
import net.sourceforge.phpeclipse.ui.templates.template.BasicCompletionProcessor;
import net.sourceforge.phpeclipse.ui.templates.template.HTMLContextType;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.templates.TemplateContextType;

/**
 * A completion processor for XML templates.
 */
public class SmartyCompletionProcessor extends BasicCompletionProcessor {

	/**
	 * Return the XML context type that is supported by this plugin.
	 */
	protected TemplateContextType getContextType(ITextViewer viewer,
			IRegion region) {
		return WebUI.getDefault().getContextTypeRegistry().getContextType(
				HTMLContextType.HTML_CONTEXT_TYPE);
	}

}
