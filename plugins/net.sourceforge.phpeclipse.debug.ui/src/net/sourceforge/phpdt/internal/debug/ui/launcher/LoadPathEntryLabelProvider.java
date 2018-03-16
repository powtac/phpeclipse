package net.sourceforge.phpdt.internal.debug.ui.launcher;

import net.sourceforge.phpdt.internal.debug.ui.PHPDebugUiPlugin;
import net.sourceforge.phpeclipse.LoadPathEntry;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

/**
 * @author xp4
 * 
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class LoadPathEntryLabelProvider implements ILabelProvider {

	/**
	 * @see ILabelProvider#getImage(Object)
	 */
	public Image getImage(Object element) {
		return null;
	}

	/**
	 * @see ILabelProvider#getText(Object)
	 */
	public String getText(Object element) {
		if (element != null && element.getClass() == LoadPathEntry.class)
			return ((LoadPathEntry) element).getProject().getFullPath()
					.toOSString();

		PHPDebugUiPlugin
				.log(new RuntimeException("Unable to render load path."));
		return null;
	}

	/**
	 * @see IBaseLabelProvider#addListener(ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
	}

	/**
	 * @see IBaseLabelProvider#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see IBaseLabelProvider#isLabelProperty(Object, String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/**
	 * @see IBaseLabelProvider#removeListener(ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
	}

}
