package net.sourceforge.phpdt.internal.debug.ui.preferences;

import net.sourceforge.phpdt.internal.launching.PHPInterpreter;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

public class PHPInterpreterLabelProvider implements ITableLabelProvider {

	public PHPInterpreterLabelProvider() {
		super();
	}

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		PHPInterpreter interpreter = (PHPInterpreter) element;
		switch (columnIndex) {
		case 0:
			return interpreter.getInstallLocation().toString();
			// case 1 :
			// IPath installLocation = interpreter.getInstallLocation();
			// return installLocation != null ? installLocation.toOSString() :
			// "In user path";
		default:
			return "Unknown Column Index";
		}
	}

	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
	}

}