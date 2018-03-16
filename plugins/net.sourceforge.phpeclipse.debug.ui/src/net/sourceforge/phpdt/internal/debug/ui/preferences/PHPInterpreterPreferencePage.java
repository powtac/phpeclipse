package net.sourceforge.phpdt.internal.debug.ui.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.phpdt.internal.debug.ui.PHPDebugUiMessages;
import net.sourceforge.phpdt.internal.launching.PHPInterpreter;
import net.sourceforge.phpdt.internal.launching.PHPRuntime;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PHPInterpreterPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {
	protected CheckboxTableViewer tableViewer;

	protected Button addButton, editButton, removeButton;

	public PHPInterpreterPreferencePage() {
		super();
	}

	public void init(IWorkbench workbench) {
	}

	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();

		Composite composite = createPageRoot(parent);
		Table table = createInstalledInterpretersTable(composite);
		createInstalledInterpretersTableViewer(table);
		createButtonGroup(composite);

		tableViewer
				.setInput(PHPRuntime.getDefault().getInstalledInterpreters());
		PHPInterpreter selectedInterpreter = PHPRuntime.getDefault()
				.getSelectedInterpreter();
		if (selectedInterpreter != null)
			tableViewer.setChecked(selectedInterpreter, true);

		enableButtons();

		return composite;
	}

	protected void createButtonGroup(Composite composite) {
		Composite buttons = new Composite(composite, SWT.NULL);
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttons.setLayout(layout);

		addButton = new Button(buttons, SWT.PUSH);
		addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addButton.setText(PHPDebugUiMessages
				.getString("PHPInterpreterPreferencePage.addButton.label")); //$NON-NLS-1$
		addButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event evt) {
				addInterpreter();
			}
		});

		editButton = new Button(buttons, SWT.PUSH);
		editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		editButton.setText(PHPDebugUiMessages
				.getString("PHPInterpreterPreferencePage.editButton.label")); //$NON-NLS-1$
		editButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event evt) {
				editInterpreter();
			}
		});

		removeButton = new Button(buttons, SWT.PUSH);
		removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		removeButton.setText(PHPDebugUiMessages
				.getString("PHPInterpreterPreferencePage.removeButton.label")); //$NON-NLS-1$
		removeButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event evt) {
				removeInterpreter();
			}
		});
	}

	protected void createInstalledInterpretersTableViewer(Table table) {
		tableViewer = new CheckboxTableViewer(table);

		tableViewer.setLabelProvider(new PHPInterpreterLabelProvider());
		tableViewer.setContentProvider(new PHPInterpreterContentProvider());

		tableViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent evt) {
						enableButtons();
					}
				});

		tableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				updateSelectedInterpreter(event.getElement());
			}
		});

		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent e) {
				editInterpreter();
			}
		});
	}

	protected Table createInstalledInterpretersTable(Composite composite) {
		Table table = new Table(composite, SWT.CHECK | SWT.BORDER
				| SWT.FULL_SELECTION);

		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = convertWidthInCharsToPixels(80);
		data.heightHint = convertHeightInCharsToPixels(10);
		table.setLayoutData(data);
		table.setHeaderVisible(true);
		table.setLinesVisible(false);

		TableColumn column = new TableColumn(table, SWT.NULL);
		column
				.setText(PHPDebugUiMessages
						.getString("PHPInterpreterPreferencePage.PHPInterpreterTable.interpreterPath")); //$NON-NLS-1$
		column.setWidth(400);

		// column = new TableColumn(table, SWT.NULL);
		// column.setText(PHPDebugUiMessages.getString("PHPInterpreterPreferencePage.PHPInterpreterTable.interpreterPath"));
		// //$NON-NLS-1$
		// column.setWidth(350);

		return table;
	}

	protected Composite createPageRoot(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		return composite;
	}

	protected void addInterpreter() {
		PHPInterpreter newInterpreter = new PHPInterpreter(null);
		File phpRuntime = getFile(getShell(), null);
		if (phpRuntime != null) {
			newInterpreter.setInstallLocation(phpRuntime);
			tableViewer.add(newInterpreter);
		}
	}

	protected void removeInterpreter() {
		tableViewer.remove(getSelectedInterpreter());
	}

	protected void enableButtons() {
		if (getSelectedInterpreter() != null) {
			editButton.setEnabled(true);
			removeButton.setEnabled(true);
		} else {
			editButton.setEnabled(false);
			removeButton.setEnabled(false);
		}
	}

	protected void updateSelectedInterpreter(Object interpreter) {
		Object[] checkedElements = tableViewer.getCheckedElements();
		for (int i = 0; i < checkedElements.length; i++) {
			tableViewer.setChecked(checkedElements[i], false);
		}

		tableViewer.setChecked(interpreter, true);
	}

	protected void editInterpreter() {
		PHPInterpreter anInterpreter = getSelectedInterpreter();
		File phpRuntime = anInterpreter.getInstallLocation();
		if (phpRuntime != null) {
			File parent = phpRuntime.getParentFile();
			phpRuntime = getFile(getShell(), parent);
		} else {
			phpRuntime = getFile(getShell(), null);
		}
		if (phpRuntime != null) {
			anInterpreter.setInstallLocation(phpRuntime);
			tableViewer.update(anInterpreter, null);
		}

	}

	protected PHPInterpreter getSelectedInterpreter() {
		IStructuredSelection selection = (IStructuredSelection) tableViewer
				.getSelection();
		return (PHPInterpreter) selection.getFirstElement();
	}

	public boolean performOk() {
		TableItem[] tableItems = tableViewer.getTable().getItems();
		List installedInterpreters = new ArrayList(tableItems.length);
		for (int i = 0; i < tableItems.length; i++)
			installedInterpreters.add(tableItems[i].getData());
		PHPRuntime.getDefault().setInstalledInterpreters(installedInterpreters);

		Object[] checkedElements = tableViewer.getCheckedElements();
		if (checkedElements.length > 0)
			PHPRuntime.getDefault().setSelectedInterpreter(
					(PHPInterpreter) checkedElements[0]);

		return super.performOk();
	}

	/**
	 * Helper to open the file chooser dialog.
	 * 
	 * @param startingDirectory
	 *            the directory to open the dialog on.
	 * @return File The File the user selected or <code>null</code> if they do
	 *         not.
	 */
	public static File getFile(Shell shell, File startingDirectory) {

		FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		if (startingDirectory != null) {
			dialog.setFileName(startingDirectory.getPath());
		}
		String operatingSystem = Platform.getOS();
		if (operatingSystem.equals(Platform.OS_WIN32)) {
			String[] extensions = { "*.exe" };
			dialog.setFilterExtensions(extensions);
		}
		String file = dialog.open();
		if (file != null) {
			file = file.trim();
			if (file.length() > 0)
				return new File(file);
		}

		return null;
	}
}