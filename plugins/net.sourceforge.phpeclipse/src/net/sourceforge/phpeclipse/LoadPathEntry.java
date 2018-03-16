package net.sourceforge.phpeclipse;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

public class LoadPathEntry {
	public static final String TYPE_PROJECT = "project";

	protected IProject project;

	protected String type;

	public LoadPathEntry(IProject aProjectEntry) {
		project = aProjectEntry;
		type = TYPE_PROJECT;
	}

	public IPath getPath() {
		return project.getFullPath();
	}

	public IProject getProject() {
		return project;
	}

	public String getType() {
		return type;
	}

	public String toXML() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("<pathentry type=\"");
		buffer.append(type + "\" ");
		buffer.append("path=\"" + getPath() + "\"/>");

		return buffer.toString();
	}
}
