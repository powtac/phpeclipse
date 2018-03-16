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

package net.sourceforge.phpdt.internal.ui;

import net.sourceforge.phpdt.core.IJavaElement;

import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class JavaElementProperties implements IPropertySource {

	private IJavaElement fSource;

	// Property Descriptors
	static private IPropertyDescriptor[] fgPropertyDescriptors = new IPropertyDescriptor[1];
	{
		PropertyDescriptor descriptor;

		// resource name
		descriptor = new PropertyDescriptor(IBasicPropertyConstants.P_TEXT,
				PHPUIMessages.getString("JavaElementProperties.name")); //$NON-NLS-1$
		descriptor.setAlwaysIncompatible(true);
		fgPropertyDescriptors[0] = descriptor;
	}

	public JavaElementProperties(IJavaElement source) {
		fSource = source;
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {
		return fgPropertyDescriptors;
	}

	public Object getPropertyValue(Object name) {
		if (name.equals(IBasicPropertyConstants.P_TEXT)) {
			return fSource.getElementName();
		}
		return null;
	}

	public void setPropertyValue(Object name, Object value) {
	}

	public Object getEditableValue() {
		return this;
	}

	public boolean isPropertySet(Object property) {
		return false;
	}

	public void resetPropertyValue(Object property) {
	}
}
