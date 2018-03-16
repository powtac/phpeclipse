package net.sourceforge.phpdt.internal.core.builder;

import net.sourceforge.phpdt.internal.ui.util.PHPFileUtil;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;
import net.sourceforge.phpeclipse.builder.IdentifierIndexManager;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * 
 * @see org.eclipse.core.resources.IResourceDelta
 * @see org.eclipse.core.resources.IResourceDeltaVisitor
 */
public class ParserVisitor implements IResourceDeltaVisitor {
	final IProgressMonitor fMonitor;

	final IProject fProject;

	public ParserVisitor(IProject iProject, IProgressMonitor monitor) {
		fMonitor = monitor;
		fProject = iProject;
	}

	protected void checkCancel() {
		if (fMonitor.isCanceled()) {
			throw new OperationCanceledException();
		}
	}

	/**
	 * Visits the given resource delta.
	 * 
	 * @return <code>true</code> if the resource delta's children should be
	 *         visited; <code>false</code> if they should be skipped.
	 * @exception CoreException
	 *                if the visit fails for some reason.
	 */
	public boolean visit(IResourceDelta delta) throws CoreException {

		IResource resource = delta.getResource();
		int resourceType = resource.getType();
		checkCancel();

		final IdentifierIndexManager indexManager = PHPeclipsePlugin
				.getDefault().getIndexManager(fProject);

		switch (delta.getKind()) {
		case IResourceDelta.ADDED:
			if (resourceType == IResource.FILE) {
				if ((resource.getFileExtension() != null)
						&& PHPFileUtil.isPHPFile((IFile) resource)) {
					fMonitor.worked(1);
					fMonitor.subTask("Adding: " + resource.getFullPath());

					// check for parsing errors
					// PHPParserAction.parseFile((IFile) resource);
					// update indexfile for the project:
					indexManager.addFile((IFile) resource);
				}
			}
			break;

		case IResourceDelta.CHANGED:
			if (resourceType == IResource.FILE) {
				if ((resource.getFileExtension() != null)
						&& PHPFileUtil.isPHPFile((IFile) resource)) {
					fMonitor.worked(1);
					fMonitor.subTask("Changing: " + resource.getFullPath());

					// check for parsing errors
					// PHPParserAction.parseFile((IFile) resource);
					// update indexfile for the project:
					indexManager.changeFile((IFile) resource);
				}
			}
			break;

		case IResourceDelta.REMOVED:
			if (resourceType == IResource.FILE) {
				if ((resource.getFileExtension() != null)
						&& PHPFileUtil.isPHPFile((IFile) resource)) {
					fMonitor.worked(1);
					fMonitor.subTask("Removing: " + resource.getFullPath());

					// update indexfile for the project:
					indexManager.removeFile((IFile) resource);
				}
			}
			break;
		}
		return true; // carry on
	}

}