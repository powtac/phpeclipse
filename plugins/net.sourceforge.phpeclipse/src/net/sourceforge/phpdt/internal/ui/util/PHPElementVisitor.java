package net.sourceforge.phpdt.internal.ui.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

public class PHPElementVisitor implements IResourceVisitor {
	protected List phpFiles = new ArrayList();

	public PHPElementVisitor() {
		super();
	}

	public boolean visit(IResource resource) throws CoreException {
		switch (resource.getType()) {
		case IResource.PROJECT:
			return true;

		case IResource.FOLDER:
			return true;

		case IResource.FILE:
			IFile fileResource = (IFile) resource;
			if (PHPFileUtil.isPHPFile(fileResource)) {
				phpFiles.add(fileResource);
				return true;
			}

		default:
			return false;
		}
	}

	public Object[] getCollectedPHPFiles() {
		return phpFiles.toArray();
	}
}
