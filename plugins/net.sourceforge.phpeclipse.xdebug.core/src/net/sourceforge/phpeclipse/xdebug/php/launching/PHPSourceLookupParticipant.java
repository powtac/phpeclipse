package net.sourceforge.phpeclipse.xdebug.php.launching;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.phpeclipse.xdebug.core.PathMapItem;
import net.sourceforge.phpeclipse.xdebug.php.model.XDebugStackFrame;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.internal.core.sourcelookup.SourceLookupMessages;

public class PHPSourceLookupParticipant extends AbstractSourceLookupParticipant {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant#getSourceName(Object)
	 */
	public String getSourceName(Object object) throws CoreException {
		if (object instanceof XDebugStackFrame) {
			return ((XDebugStackFrame) object).getSourceName();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant#findSourceElements(java.lang.Object)
	 */
	public Object[] findSourceElements(Object object) throws CoreException {
		if (object == null) {
			return new Object[] {};
		}
		XDebugStackFrame stackFrame = null;
		if (object instanceof XDebugStackFrame) {
			stackFrame = (XDebugStackFrame) object;
		} else {
			return new Object[] {};
		}

		List results = null;
		CoreException single = null;
		MultiStatus multiStatus = null;

		if (isFindDuplicates()) {
			results = new ArrayList();
		}

		String name = getSourceName(object);
		if (name == null || name.length() == 0) {
			return new Object[] {};
		}

		// here our stackframe is guaranteed not to be null
		IPath sLocalPath = null;

		if (((XDebugStackFrame) object).getThread() == null) {
			IPath sPath = new Path(stackFrame.getFullName().getPath());
			List pathMap = getDirector().getLaunchConfiguration()
					.getAttribute(IXDebugConstants.ATTR_PHP_PATHMAP, (List) null);
	
			PathMapItem pmi = null;
			for (int k = 0; k < pathMap.size(); k++) {
				pmi = new PathMapItem((String) pathMap.get(k));
	
				IPath local = new Path(pmi.getLocalPath().toString());
				IPath remote = new Path(pmi.getRemotePath().toString());
	
				if (remote.matchingFirstSegments(sPath) == remote.segmentCount()) {
					sLocalPath = local;
				}
			}
		} else {
			
		}

		String Type = stackFrame.getType();

		if (Type.equals("eval")) {
			results.add("pippo");
			return results.toArray();
		}

		ISourceContainer[] containers = getSourceContainers();
		for (int i = 0; i < containers.length; i++) {
			ISourceContainer container = getDelegateContainer(containers[i]);
			if (container == null) {
				continue;
			}

			try {
				Object[] objects = container.findSourceElements(name);
				if (objects.length > 0) {
					if (isFindDuplicates()) {
						if (((XDebugStackFrame) object).getThread() == null) {
							addMatching(results, sLocalPath, objects);
						} else {
							return objects;
						}
					} else {
						if (objects.length == 1) {
							return objects;
						}
						return new Object[] { objects[0] };
					}
				}
			} catch (CoreException e) {
				if (single == null) {
					single = e;
				} else if (multiStatus == null) {
					multiStatus = new MultiStatus(DebugPlugin
							.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR,
							new IStatus[] { single.getStatus() },
							SourceLookupMessages.DefaultSourceContainer_0/*CompositeSourceContainer_0*/,
							null);
					multiStatus.add(e.getStatus());
				} else {
					multiStatus.add(e.getStatus());
				}
			}
		}
		if (results == null) {
			if (multiStatus != null) {
				throw new CoreException(multiStatus);
			} else if (single != null) {
				throw single;
			}
			return EMPTY;
		}
		return results.toArray();
	}

	static void addMatching(List results, IPath localPath, Object[] objects) {
		if (results == null || localPath == null || objects == null) {
			return;
		}
		for (int j = 0; j < objects.length; j++) {
			if (objects[j] == null || !(objects[j] instanceof IFile)) {
				continue;
			}
			IFile file = (IFile) objects[j];

			IPath path = new Path(file.getLocation().toString());
			if (localPath.matchingFirstSegments(path) == localPath
					.segmentCount()) {
				results.add(objects[j]);
			}
		}
	}
}