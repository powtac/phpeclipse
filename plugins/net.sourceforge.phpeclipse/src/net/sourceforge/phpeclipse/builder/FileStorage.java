package net.sourceforge.phpeclipse.builder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.sourceforge.phpdt.internal.ui.util.StreamUtil;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

/**
 * 
 * @see IStorage
 */
public class FileStorage extends PlatformObject implements IStorage {
	private boolean forceReadOnly;

	private final IPath path;

	private final File file;

	/**
	 * Two FileStorages are equal if their IPaths are equal.
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof FileStorage))
			return false;
		FileStorage other = (FileStorage) obj;
		return path.equals(other.path);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IStorage#getContents()
	 */
	public InputStream getContents() throws CoreException {
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					PHPeclipsePlugin.PLUGIN_ID, IStatus.ERROR, e.toString(), e));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IStorage#getFullPath()
	 */
	public IPath getFullPath() {
		return this.path;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IStorage#getName()
	 */
	public String getName() {
		return this.path.lastSegment();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IStorage#isReadOnly()
	 */
	public boolean isReadOnly() {
		return forceReadOnly || !file.canWrite();
	}

	/**
	 * Method FileStorage.
	 * 
	 * @param path
	 */
	public FileStorage(IPath path) {
		this.path = path;
		this.file = path.toFile();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return path.toOSString();
	}

	/**
	 * @param stream
	 * @param overwrite
	 * @param b
	 * @param monitor
	 */
	public void setContents(InputStream stream, boolean overwrite, boolean b,
			IProgressMonitor monitor) throws CoreException {
		try {
			StreamUtil.transferStreams(stream, new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					PHPeclipsePlugin.PLUGIN_ID, IStatus.ERROR, e.toString(), e));
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					PHPeclipsePlugin.PLUGIN_ID, IStatus.ERROR, e.toString(), e));
		}
	}

	/**
	 * Some document providers (notably CompilationUnitDocumentProvider) can't
	 * handle read/write storage.
	 */
	public void setReadOnly() {
		forceReadOnly = true;
	}
}
