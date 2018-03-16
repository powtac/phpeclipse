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

package net.sourceforge.phpdt.internal.ui.text.java.hover;

import java.io.IOException;

import net.sourceforge.phpdt.core.IJavaElement;
import net.sourceforge.phpdt.core.IMember;
import net.sourceforge.phpdt.core.ISourceReference;
import net.sourceforge.phpdt.core.JavaModelException;
import net.sourceforge.phpdt.internal.ui.text.HTMLPrinter;
import net.sourceforge.phpdt.internal.ui.text.PHPCodeReader;
import net.sourceforge.phpdt.internal.ui.viewsupport.JavaElementLabels;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

/**
 * Provides source as hover info for Java elements.
 */
public class JavaSourceHover extends AbstractJavaEditorTextHover {

	private final int LABEL_FLAGS = JavaElementLabels.ALL_FULLY_QUALIFIED
			| JavaElementLabels.M_PRE_RETURNTYPE
			| JavaElementLabels.M_PARAMETER_TYPES
			| JavaElementLabels.M_PARAMETER_NAMES
			| JavaElementLabels.M_EXCEPTIONS
			| JavaElementLabels.F_PRE_TYPE_SIGNATURE;

	/*
	 * @see JavaElementHover
	 */
	protected String getHoverInfo(IJavaElement[] result) {
		int nResults = result.length;
		StringBuffer buffer = new StringBuffer();

		if (nResults > 1) {

			for (int i = 0; i < result.length; i++) {
				HTMLPrinter.startBulletList(buffer);
				IJavaElement curr = result[i];
				if (curr instanceof IMember)
					HTMLPrinter.addBullet(buffer, getInfoText((IMember) curr));
				HTMLPrinter.endBulletList(buffer);
			}

		} else {

			IJavaElement curr = result[0];
			if (curr instanceof IMember && curr instanceof ISourceReference) {
				HTMLPrinter.addSmallHeader(buffer,
						getInfoText(((IMember) curr)));
				try {
					String source = ((ISourceReference) curr).getSource();
					source = removeLeadingComments(source);
					HTMLPrinter.addParagraph(buffer, "<pre>"); //$NON-NLS-1$
					HTMLPrinter.addParagraph(buffer, source);
					HTMLPrinter.addParagraph(buffer, "</pre>"); //$NON-NLS-1$
				} catch (JavaModelException ex) {
					// only write small header
				}
			}
		}

		if (buffer.length() > 0) {
			HTMLPrinter.insertPageProlog(buffer, 0);
			HTMLPrinter.addPageEpilog(buffer);
			return buffer.toString();
		}

		return null;
	}

	private String getInfoText(IMember member) {
		return JavaElementLabels.getElementLabel(member, LABEL_FLAGS);
	}

	private String removeLeadingComments(String source) {
		PHPCodeReader reader = new PHPCodeReader();
		IDocument document = new Document(source);
		int i;
		try {
			reader.configureForwardReader(document, 0, document.getLength(),
					true, false);
			int c = reader.read();
			while (c != -1 && (c == '\r' || c == '\n')) {
				c = reader.read();
			}
			i = reader.getOffset();
			reader.close();
		} catch (IOException ex) {
			i = 0;
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException ex) {
				PHPeclipsePlugin.log(ex);
			}
		}

		if (i < 0)
			return source;
		return source.substring(i);
	}
}
