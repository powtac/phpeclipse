package net.sourceforge.phpdt.internal.debug.ui.preferences;

import net.sourceforge.phpdt.internal.debug.ui.PHPDebugUiMessages;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PHPBasePreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	public PHPBasePreferencePage() {
		super();
	}

	public void init(IWorkbench workbench) {
	}

	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);

		new Label(composite, SWT.NONE).setText(PHPDebugUiMessages
				.getString("PHPBasePreferencePage.label")); //$NON-NLS-1$

		return composite;
	}
}
