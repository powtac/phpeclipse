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
package net.sourceforge.phpdt.internal.ui.viewsupport;

import net.sourceforge.phpdt.ui.PreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;

/**
 * JavaUILabelProvider that respects settings from the Appearance preference
 * page. Triggers a viewer update when a preference changes.
 */
public class AppearanceAwareLabelProvider extends JavaUILabelProvider implements
		IPropertyChangeListener {

	public final static int DEFAULT_TEXTFLAGS = JavaElementLabels.ROOT_VARIABLE
			| JavaElementLabels.M_PARAMETER_TYPES
			| JavaElementLabels.M_APP_RETURNTYPE
			| JavaElementLabels.REFERENCED_ROOT_POST_QUALIFIED;

	public final static int DEFAULT_IMAGEFLAGS = JavaElementImageProvider.OVERLAY_ICONS;

	private int fTextFlagMask;

	private int fImageFlagMask;

	/**
	 * Constructor for AppearanceAwareLabelProvider.
	 */
	public AppearanceAwareLabelProvider(int textFlags, int imageFlags) {
		super(textFlags, imageFlags);
		initMasks();
		PreferenceConstants.getPreferenceStore()
				.addPropertyChangeListener(this);
	}

	/**
	 * Creates a labelProvider with DEFAULT_TEXTFLAGS and DEFAULT_IMAGEFLAGS
	 */
	public AppearanceAwareLabelProvider() {
		this(DEFAULT_TEXTFLAGS, DEFAULT_IMAGEFLAGS);
	}

	private void initMasks() {
		IPreferenceStore store = PreferenceConstants.getPreferenceStore();
		fTextFlagMask = -1;
		if (!store.getBoolean(PreferenceConstants.APPEARANCE_METHOD_RETURNTYPE)) {
			fTextFlagMask ^= JavaElementLabels.M_APP_RETURNTYPE;
		}
		if (!store
				.getBoolean(PreferenceConstants.APPEARANCE_COMPRESS_PACKAGE_NAMES)) {
			fTextFlagMask ^= JavaElementLabels.P_COMPRESSED;
		}

		fImageFlagMask = -1;
	}

	/*
	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if (property.equals(PreferenceConstants.APPEARANCE_METHOD_RETURNTYPE)
				|| property
						.equals(PreferenceConstants.APPEARANCE_PKG_NAME_PATTERN_FOR_PKG_VIEW)
				|| property
						.equals(PreferenceConstants.APPEARANCE_COMPRESS_PACKAGE_NAMES)) {
			initMasks();
			LabelProviderChangedEvent lpEvent = new LabelProviderChangedEvent(
					this, null); // refresh all
			fireLabelProviderChanged(lpEvent);
		}
	}

	/*
	 * @see IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		PreferenceConstants.getPreferenceStore().removePropertyChangeListener(
				this);
		super.dispose();
	}

	/*
	 * @see JavaUILabelProvider#evaluateImageFlags()
	 */
	protected int evaluateImageFlags(Object element) {
		return getImageFlags() & fImageFlagMask;
	}

	/*
	 * @see JavaUILabelProvider#evaluateTextFlags()
	 */
	protected int evaluateTextFlags(Object element) {
		return getTextFlags() & fTextFlagMask;
	}

}
