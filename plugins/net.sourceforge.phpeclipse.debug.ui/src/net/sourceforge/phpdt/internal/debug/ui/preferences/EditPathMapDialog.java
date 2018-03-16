/*
 * Created on 12.02.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.sourceforge.phpdt.internal.debug.ui.preferences;

import net.sourceforge.phpdt.internal.debug.ui.PHPDebugUiMessages;
import net.sourceforge.phpdt.internal.ui.dialogs.StatusDialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author Christian
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class EditPathMapDialog extends StatusDialog {

	private Text fLocalPathText;

	private Text fRemotePathText;

	private String[] fInitialValues;

	private String fLocalPath;

	private String fRemotePath;

	public EditPathMapDialog(Shell parentShell, String aDialogTitle,
			String[] initialValues) {
		super(parentShell);
		setTitle(aDialogTitle);
		fInitialValues = initialValues;
	}

	protected void okPressed() {
		fLocalPath = fLocalPathText.getText();
		fRemotePath = fRemotePathText.getText();
		super.okPressed();
	}

	protected Control createDialogArea(Composite composite) {
		Composite comp = new Composite(composite, SWT.NONE);
		comp.setLayout(new GridLayout());

		Composite fileComp = new Composite(comp, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		// gridLayout.marginHeight = 0;
		// gridLayout.marginWidth = 0;
		fileComp.setLayout(gridLayout);

		Label label = new Label(fileComp, SWT.NONE);
		label
				.setText(PHPDebugUiMessages
						.getString("EditPathDialog.Local_Path"));//$NON-NLS-1$

		fLocalPathText = new Text(fileComp, SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData();
		gd.widthHint = 250;
		fLocalPathText.setLayoutData(gd);
		fLocalPathText.setText(fInitialValues[0]);
		Button button = new Button(fileComp, SWT.PUSH);
		button
				.setText(PHPDebugUiMessages
						.getString("EditPathMapDialog.Browse")); //$NON-NLS-1$
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowseButtonSelected();
			}
		});
		label = new Label(fileComp, SWT.NONE);
		label.setText(PHPDebugUiMessages
				.getString("EditPathMapDialog.Remote_Path")); //$NON-NLS-1$
		fRemotePathText = new Text(fileComp, SWT.SINGLE | SWT.BORDER);
		fRemotePathText.setText(fInitialValues[1]);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		fRemotePathText.setLayoutData(gd);

		return composite;
	}

	public String[] getPathPair() {
		return new String[] { fLocalPath, fRemotePath };
	}

	private void handleBrowseButtonSelected() {
		DirectoryDialog dd = new DirectoryDialog(getShell(), SWT.OPEN);
		dd.setMessage(PHPDebugUiMessages
				.getString("EditPathMapDialog.Select_the_directory_to_map")); //$NON-NLS-1$
		String path = dd.open();

		if (path != null)
			fLocalPathText.setText(path);

	}

}
