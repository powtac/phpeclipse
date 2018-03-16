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
 * $Id: DTDMergeViewer.java,v 1.3 2006-10-21 23:14:13 pombredanne Exp $
 */

package net.sourceforge.phpeclipse.xml.ui.internal.compare;

import net.sourceforge.phpeclipse.xml.ui.XMLPlugin;
import net.sourceforge.phpeclipse.xml.ui.internal.text.DTDConfiguration;
import net.sourceforge.phpeclipse.xml.ui.text.DTDTextTools;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * @author Igor Malinin
 */
public class DTDMergeViewer extends TextMergeViewer {

	/**
	 * The preference store.
	 */
	private IPreferenceStore preferenceStore;

	/**
	 * The listener for changes to the preference store.
	 */
	private IPropertyChangeListener propertyChangeListener;

	/**
	 * The DTD text tools.
	 */
	private DTDTextTools textTools;

	/*
	 * @see TextMergeViewer#TextMergeViewer(Composite, int,
	 *      CompareConfiguration)
	 */
	public DTDMergeViewer(Composite parent, int style,
			CompareConfiguration configuration) {
		super(parent, style, configuration);
	}

	// TextMergeViewer Implementation ------------------------------------------

	/*
	 * @see TextMergeViewer#configureTextViewer()
	 */
	protected void configureTextViewer(TextViewer textViewer) {
		XMLPlugin plugin = XMLPlugin.getDefault();

		preferenceStore = plugin.getPreferenceStore();
		if (preferenceStore != null) {
			propertyChangeListener = new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					handlePreferenceStoreChanged(event);
				}
			};
			preferenceStore.addPropertyChangeListener(propertyChangeListener);
		}

		textTools = plugin.getDTDTextTools();

		if (textViewer instanceof SourceViewer) {
			SourceViewer sourceViewer = (SourceViewer) textViewer;
			sourceViewer.configure(new DTDConfiguration(textTools));
		}

		updateBackgroundColor();
	}

	/*
	 * @see TextMergeViewer#getDocumentPartitioner()
	 */
	protected IDocumentPartitioner getDocumentPartitioner() {
		return textTools.createDTDPartitioner();
	}

	/*
	 * @see org.eclipse.compare.contentmergeviewer.ContentMergeViewer#getTitle()
	 */
	public String getTitle() {
		return XMLPlugin.getResourceString("DTDMergeViewer.title"); //$NON-NLS-1$
	}

	/*
	 * @see org.eclipse.jface.viewers.ContentViewer#handleDispose(org.eclipse.swt.events.DisposeEvent)
	 */
	protected void handleDispose(DisposeEvent event) {
		if (propertyChangeListener != null) {
			if (preferenceStore != null) {
				preferenceStore
						.removePropertyChangeListener(propertyChangeListener);
			}

			propertyChangeListener = null;
		}

		super.handleDispose(event);
	}

	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		String p = event.getProperty();

		if (p.equals(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND)
				|| p
						.equals(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT)) {
			updateBackgroundColor();
		} else if (textTools.affectsBehavior(event)) {
			invalidateTextPresentation();
		}
	}

	private void updateBackgroundColor() {
		boolean defaultBackgroundColor = preferenceStore
				.getBoolean(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT);

		if (defaultBackgroundColor) {
			setBackgroundColor(null);
		} else {
			RGB backgroundColor = PreferenceConverter.getColor(preferenceStore,
					AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND);
			setBackgroundColor(backgroundColor);
		}
	}
}
