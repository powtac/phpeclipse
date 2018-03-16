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
package net.sourceforge.phpdt.core.jdom;

import net.sourceforge.phpdt.internal.core.jdom.DOMBuilder;
import net.sourceforge.phpdt.internal.core.jdom.SimpleDOMBuilder;

/**
 * Standard implementation of <code>IDOMFactory</code>, and the only means of
 * creating JDOMs and document fragments.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class DOMFactory implements IDOMFactory {
	/**
	 * Creates a new DOM factory.
	 */
	public DOMFactory() {
	}

	/*
	 * (non-Javadoc) Method declared on IDOMFactory.
	 */
	public IDOMCompilationUnit createCompilationUnit() {
		return (new DOMBuilder()).createCompilationUnit();
	}

	/*
	 * (non-Javadoc) Method declared on IDOMFactory.
	 */
	public IDOMCompilationUnit createCompilationUnit(char[] sourceCode,
			String name) {
		if (sourceCode == null) {
			return null;
		}
		return (new SimpleDOMBuilder()).createCompilationUnit(sourceCode, name
				.toCharArray());
	}

	/*
	 * (non-Javadoc) Method declared on IDOMFactory.
	 */
	public IDOMCompilationUnit createCompilationUnit(String sourceCode,
			String name) {
		if (sourceCode == null) {
			return null;
		}
		return (new SimpleDOMBuilder()).createCompilationUnit(sourceCode
				.toCharArray(), name.toCharArray());
	}

	/*
	 * (non-Javadoc) Method declared on IDOMFactory.
	 */
	// public IDOMField createField() {
	// return createField("Object aField;"+ ProjectPrefUtil.LINE_SEPARATOR);
	// //$NON-NLS-1$
	// }
	// /* (non-Javadoc)
	// * Method declared on IDOMFactory.
	// */
	// public IDOMField createField(String sourceCode) {
	// if(sourceCode == null) {
	// return null;
	// }
	// return (new DOMBuilder()).createField(sourceCode.toCharArray());
	// }
	/*
	 * (non-Javadoc) Method declared on IDOMFactory.
	 */
	// public IDOMImport createImport() {
	// return (new DOMBuilder()).createImport();
	// }
	// /* (non-Javadoc)
	// * Method declared on IDOMFactory.
	// */
	// public IDOMImport createImport(String sourceCode) {
	// if(sourceCode == null) {
	// return null;
	// }
	// return (new DOMBuilder()).createImport(sourceCode.toCharArray());
	// }
	/*
	 * (non-Javadoc) Method declared on IDOMFactory.
	 */
	// public IDOMInitializer createInitializer() {
	// return createInitializer("static {}"+ ProjectPrefUtil.LINE_SEPARATOR);
	// //$NON-NLS-1$
	// }
	// /* (non-Javadoc)
	// * Method declared on IDOMFactory.
	// */
	// public IDOMInitializer createInitializer(String sourceCode) {
	// if(sourceCode == null) {
	// return null;
	// }
	// return (new DOMBuilder()).createInitializer(sourceCode.toCharArray());
	// }
	/*
	 * (non-Javadoc) Method declared on IDOMFactory.
	 */
	// public IDOMMethod createMethod() {
	// return createMethod("public void newMethod() {"+
	// ProjectPrefUtil.LINE_SEPARATOR+"}"+ ProjectPrefUtil.LINE_SEPARATOR);
	// //$NON-NLS-2$ //$NON-NLS-1$
	// }
	// /* (non-Javadoc)
	// * Method declared on IDOMFactory.
	// */
	// public IDOMMethod createMethod(String sourceCode) {
	// if(sourceCode == null) {
	// return null;
	// }
	// return (new DOMBuilder()).createMethod(sourceCode.toCharArray());
	// }
	/*
	 * (non-Javadoc) Method declared on IDOMFactory.
	 */
	public IDOMPackage createPackage() {
		return (new DOMBuilder()).createPackage();
	}
	/*
	 * (non-Javadoc) Method declared on IDOMFactory.
	 */
	// public IDOMPackage createPackage(String sourceCode) {
	// if(sourceCode == null) {
	// return null;
	// }
	// return (new DOMBuilder()).createPackage(sourceCode.toCharArray());
	// }
	// /* (non-Javadoc)
	// * Method declared on IDOMFactory.
	// */
	// public IDOMType createType() {
	// return createType("public class AClass {"+ ProjectPrefUtil.LINE_SEPARATOR
	// +"}"+ ProjectPrefUtil.LINE_SEPARATOR); //$NON-NLS-2$ //$NON-NLS-1$
	// }
	/*
	 * (non-Javadoc) Method declared on IDOMFactory.
	 */
	// public IDOMType createClass() {
	// return createType("public class AClass {"+ ProjectPrefUtil.LINE_SEPARATOR
	// +"}"+ ProjectPrefUtil.LINE_SEPARATOR); //$NON-NLS-2$ //$NON-NLS-1$
	// }
	// /* (non-Javadoc)
	// * Method declared on IDOMFactory.
	// */
	// public IDOMType createInterface() {
	// return createType("public interface AnInterface {"+
	// ProjectPrefUtil.LINE_SEPARATOR +"}"+ ProjectPrefUtil.LINE_SEPARATOR);
	// //$NON-NLS-2$ //$NON-NLS-1$
	// }
	/*
	 * (non-Javadoc) Method declared on IDOMFactory.
	 */
	// public IDOMType createType(String sourceCode) {
	// if(sourceCode == null) {
	// return null;
	// }
	// return (new DOMBuilder()).createType(sourceCode.toCharArray());
	// }
}
