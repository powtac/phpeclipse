/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package net.sourceforge.phpdt.internal.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

/**
 * An exception to wrap a status. This is necessary to use the core's
 * IRunnableWithProgress support
 */

public class PHPUIException extends CoreException {

	public PHPUIException(IStatus status) {
		super(status);
	}
}