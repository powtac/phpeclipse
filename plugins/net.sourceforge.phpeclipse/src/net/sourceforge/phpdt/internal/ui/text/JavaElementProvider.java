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
package net.sourceforge.phpdt.internal.ui.text;

import net.sourceforge.phpdt.core.IJavaElement;
import net.sourceforge.phpdt.core.JavaModelException;
import net.sourceforge.phpdt.internal.ui.actions.SelectionConverter;
import net.sourceforge.phpeclipse.phpeditor.PHPEditor;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.IInformationProviderExtension;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;

/**
 * Provides a Java element to be displayed in by an information presenter.
 */
public class JavaElementProvider implements IInformationProvider,
		IInformationProviderExtension {

	private PHPEditor fEditor;

	private boolean fUseCodeResolve;

	public JavaElementProvider(IEditorPart editor) {
		fUseCodeResolve = false;
		if (editor instanceof PHPEditor)
			fEditor = (PHPEditor) editor;
	}

	public JavaElementProvider(IEditorPart editor, boolean useCodeResolve) {
		this(editor);
		fUseCodeResolve = useCodeResolve;
	}

	/*
	 * @see IInformationProvider#getSubject(ITextViewer, int)
	 */
	public IRegion getSubject(ITextViewer textViewer, int offset) {
		if (textViewer != null && fEditor != null) {
			IRegion region = JavaWordFinder.findWord(textViewer.getDocument(),
					offset);
			if (region != null)
				return region;
			else
				return new Region(offset, 0);
		}
		return null;
	}

	/*
	 * @see IInformationProvider#getInformation(ITextViewer, IRegion)
	 */
	public String getInformation(ITextViewer textViewer, IRegion subject) {
		return getInformation2(textViewer, subject).toString();
	}

	/*
	 * @see IInformationProviderExtension#getElement(ITextViewer, IRegion)
	 */
	public Object getInformation2(ITextViewer textViewer, IRegion subject) {
		if (fEditor == null)
			return null;

		try {
			if (fUseCodeResolve) {
				IStructuredSelection sel = SelectionConverter
						.getStructuredSelection(fEditor);
				if (!sel.isEmpty())
					return sel.getFirstElement();
			}
			IJavaElement element = SelectionConverter
					.getElementAtOffset(fEditor);
			if (element != null)
				return element;
			return SelectionConverter.getInput(fEditor);
		} catch (JavaModelException e) {
			return null;
		}
	}
}
