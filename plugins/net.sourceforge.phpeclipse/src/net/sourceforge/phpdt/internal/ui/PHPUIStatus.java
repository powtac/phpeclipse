/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package net.sourceforge.phpdt.internal.ui;

import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Convenience class for error exceptions thrown inside PHPeclipse plugin.
 */
public class PHPUIStatus extends Status {

	public PHPUIStatus(int code) {
		this(code, ""); //$NON-NLS-1$
	}

	private PHPUIStatus(int severity, int code, String message,
			Throwable throwable) {
		super(severity, PHPeclipsePlugin.getPluginId(), code, message,
				throwable);
	}

	public PHPUIStatus(int code, String message) {
		this(code, message, null);
	}

	public PHPUIStatus(int code, String message, Throwable throwable) {
		super(IStatus.ERROR, PHPeclipsePlugin.getPluginId(), code, message,
				throwable);
	}

	public static IStatus createError(int code, Throwable throwable) {
		String message = throwable.getMessage();
		if (message == null) {
			message = throwable.getClass().getName();
		}
		return new PHPUIStatus(IStatus.ERROR, code, message, throwable);
	}

	public static IStatus createError(int code, String message,
			Throwable throwable) {
		return new PHPUIStatus(IStatus.ERROR, code, message, throwable);
	}

	public static IStatus createInfo(int code, String message,
			Throwable throwable) {
		return new PHPUIStatus(IStatus.INFO, code, message, throwable);
	}

	public static IStatus createWarning(int code, String message,
			Throwable throwable) {
		return new PHPUIStatus(IStatus.WARNING, code, message, throwable);
	}
}
