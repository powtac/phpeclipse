package net.sourceforge.phpeclipse.builder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.StorageDocumentProvider;

/**
 * @author ed
 * @version 1.0, May 19, 2003
 */
public class ExternalStorageDocumentProvider extends StorageDocumentProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#doSaveDocument(org.eclipse.core.runtime.IProgressMonitor,
	 *      java.lang.Object, org.eclipse.jface.text.IDocument, boolean)
	 */
	protected void doSaveDocument(IProgressMonitor monitor, Object element,
			IDocument document, boolean overwrite) throws CoreException {
		if (element instanceof ExternalEditorInput) {
			ExternalEditorInput external = (ExternalEditorInput) element;
			FileStorage storage = (FileStorage) external.getStorage();
			String encoding = getEncoding(element);
			if (encoding == null)
				encoding = getDefaultEncoding();
			try {
				InputStream stream = new ByteArrayInputStream(document.get()
						.getBytes(encoding));
				try {
					// inform about the upcoming content change
					fireElementStateChanging(element);
					storage.setContents(stream, overwrite, true, monitor);
				} catch (RuntimeException e) {
					// inform about failure
					fireElementStateChangeFailed(element);
					throw e;
				}
			} catch (IOException e) {
				IStatus s = new Status(IStatus.ERROR,
						PHPeclipsePlugin.PLUGIN_ID, IStatus.OK, e.getMessage(),
						e);
				throw new CoreException(s);
			}

		} else {
			super.doSaveDocument(monitor, element, document, overwrite);
		}
	}

}
