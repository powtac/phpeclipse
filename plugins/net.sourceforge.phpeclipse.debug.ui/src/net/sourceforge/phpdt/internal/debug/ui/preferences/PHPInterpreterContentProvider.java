package net.sourceforge.phpdt.internal.debug.ui.preferences;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class PHPInterpreterContentProvider implements
		IStructuredContentProvider {
	protected List interpreters;

	public PHPInterpreterContentProvider() {
		super();
	}

	public Object[] getElements(Object inputElement) {
		return interpreters.toArray();
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		interpreters = (List) newInput;
	}
}
