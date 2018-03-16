package net.sourceforge.phpeclipse.xdebug.core;

//import java.net.MalformedURLException;
//import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class PathMapItem {
	private IPath local;
	private IPath remote;

	// String.split() uses regular expressions
	static final String DELIMITER_REGEX = "\\+\\*\\+";
	static final String DELIMITER = "+*+";
	
	static private String MakeStringData(IPath local, IPath remote) {
		//IPath b = new Path("file:/var/www/index.php");
		
		if(local.getDevice() == "file://") {
			local = local.setDevice("");
		}
		String data = local.toString()+DELIMITER+remote.toString();
		return data;
	}
	
	public PathMapItem(String newLocal, String newRemote) {
		IPath localPath = sanitizePath(newLocal);
		IPath remotePath = sanitizePath(newRemote);
		local = localPath;
		remote = remotePath;
	}

	private IPath sanitizePath(String newPath) {
		IPath sanitizePath = null;
		IPath computePath = new Path(newPath);
		/*if(computePath.getDevice() == null) {
			sanitizePath = new Path("file://", newPath);
		} else {
			if(computePath.getDevice() == "file:") {
				sanitizePath = computePath;
			} else {*/
				sanitizePath = computePath;				
			/*}
		}*/
		return sanitizePath;
	}
	
	public PathMapItem(String data) {
		String[] strData = data.split(DELIMITER_REGEX);
		IPath localPath = sanitizePath(strData[0]);
		IPath remotePath = sanitizePath(strData[1]);
		local = localPath;
		remote = remotePath;
	}
	
	public String getStringData() {
		return PathMapItem.MakeStringData(local, remote);
	}
	
	/**
	 * Returns the local path, which serves as the key in the local/remote
	 * pair this variable represents
	 * 
	 * @return local path
	 */
	public IPath getLocalPath() {
		return new Path(local.toString());
	}
	
	/**
	 * Returns the remote path.
	 * 
	 * @return remote path
	 */
	public IPath getRemotePath() {
		return new Path(remote.toString());
	}

	/**
	 * Sets the local path
	 * @param local path
	 * @throws MalformedURLException 
	 */
	public void setLocalPath(String path) {
		this.local = sanitizePath(path);
	}
	
	/**
	 * Sets the local path
	 * @param local path
	 */
	public void setLocalPath(IPath path) {
		this.local = path;
	}

	/**
	 * Sets the remote path
	 * @param remote path
	 */
	public void setRemotePath(IPath path) {
		this.remote = path;
	}

	/**
	 * Sets the remote path
	 * @param remote path
	 * @throws MalformedURLException 
	 */
	public void setRemotePath(String path) {
		this.remote = sanitizePath(path);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return local.toString()+"->"+remote.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		boolean equal = false;
		if (obj instanceof PathMapItem) {
			PathMapItem var = (PathMapItem) obj;
			equal = var.getLocalPath().equals(local);
		}
		return equal;		
	}
}