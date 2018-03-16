package net.sourceforge.phpdt.internal.debug.ui.launcher;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sourceforge.phpdt.internal.debug.ui.PHPDebugUiMessages;
import net.sourceforge.phpdt.internal.debug.ui.PHPDebugUiPlugin;
import net.sourceforge.phpdt.internal.debug.ui.preferences.EditPathMapDialog;
import net.sourceforge.phpdt.internal.debug.ui.preferences.PHPInterpreterPreferencePage;
import net.sourceforge.phpdt.internal.launching.PHPInterpreter;
import net.sourceforge.phpdt.internal.launching.PHPLaunchConfigurationAttribute;
import net.sourceforge.phpdt.internal.launching.PHPRuntime;
import net.sourceforge.phpdt.internal.ui.PHPUiImages;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class PHPEnvironmentTab extends AbstractLaunchConfigurationTab {
	protected ListViewer loadPathListViewer;

	protected java.util.List installedInterpretersWorkingCopy;

	protected Combo interpreterCombo;

	// protected Button loadPathDefaultButton;
	protected Button fRemoteDebugCheckBox;

	protected Button fRemoteDebugTranslate;

	protected Button fOpenDBGSessionInBrowserCheckBox;

	protected Button fOpenDBGSessionInExternalBrowserCheckBox;

	protected Button fPathMapRemoveButton;

	protected Button fPathMapAddButton;

	protected Button fPathMapEditButton;

	protected Text fRemoteSourcePath;

	protected Table fRemoteDebugPathMapTable;

	protected TabFolder tabFolder;

	private Text targetFile;

	private String originalFileName = "";

	private class RemoteDebugTabListener extends SelectionAdapter implements
			ModifyListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
		 */
		public void modifyText(ModifyEvent e) {
			updateLaunchConfigurationDialog();
			makeupTargetFile();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == fRemoteDebugPathMapTable) {
				setPathMapButtonsEnableState();
			} else if (source == fPathMapAddButton) {
				handlePathMapAddButtonSelected();
			} else if (source == fPathMapEditButton) {
				handlePathMapEditButtonSelected();
			} else if (source == fPathMapRemoveButton) {
				handlePathMapRemoveButtonSelected();
			} else if (source == fRemoteDebugCheckBox) {
				setRemoteTabEnableState();
			} else if (source == fRemoteDebugTranslate) {
				setRemoteTabEnableState();
			} else if (source == fOpenDBGSessionInBrowserCheckBox) {
				setRemoteTabEnableState();
			} else {
				updateLaunchConfigurationDialog();
			}
			makeupTargetFile();
		}

	}

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private RemoteDebugTabListener fListener = new RemoteDebugTabListener();

	private static final boolean DEFAULT_REMOTE_DEBUG = false;

	private static final boolean DEFAULT_REMOTE_DEBUG_TRANSLATE = false;

	private static final boolean DEFAULT_OPEN_DBGSESSION_IN_BROWSER = true;

	private static final boolean DEFAULT_OPEN_DBGSESSION_IN_EXTERNAL_BROWSER = false;

	static String[] columnTitles = {
			PHPDebugUiMessages
					.getString("LaunchConfigurationTab.PHPEnvironment.remoteDebugTab.PathMapTableTitle.local"),
			PHPDebugUiMessages
					.getString("LaunchConfigurationTab.PHPEnvironment.remoteDebugTab.PathMapTableTitle.remote") };

	public PHPEnvironmentTab() {
		super();
	}

	public void createControl(Composite parent) {
		Composite composite = createPageRoot(parent);

		tabFolder = new TabFolder(composite, SWT.NONE);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		tabFolder.setLayoutData(gridData);

		// addLoadPathTab(tabFolder);
		addRemoteDebugTab(tabFolder);
		addInterpreterTab(tabFolder);
	}

	protected void addRemoteDebugTab(TabFolder tabFolder) {
		Label label;

		TabItem remoteDebugTab = new TabItem(tabFolder, SWT.NONE, 0);
		remoteDebugTab
				.setText(PHPDebugUiMessages
						.getString("LaunchConfigurationTab.PHPEnvironment.remoteDebugTab.label"));

		Composite comp = new Composite(tabFolder, SWT.NONE);
		comp.setLayout(new GridLayout());
		remoteDebugTab.setControl(comp);
		GridData gd;

		fRemoteDebugCheckBox = new Button(comp, SWT.CHECK);
		fRemoteDebugCheckBox
				.setText(PHPDebugUiMessages
						.getString("LaunchConfigurationTab.PHPEnvironment.remoteDebugTab.RemoteCheckBox.label"));
		fRemoteDebugCheckBox.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_BEGINNING));
		fRemoteDebugCheckBox.addSelectionListener(fListener);

		fRemoteDebugTranslate = new Button(comp, SWT.CHECK);
		fRemoteDebugTranslate
				.setText(PHPDebugUiMessages
						.getString("LaunchConfigurationTab.PHPEnvironment.remoteDebugTab.RemoteTranslate.label"));
		fRemoteDebugTranslate.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_BEGINNING));
		fRemoteDebugTranslate.addSelectionListener(fListener);

		fOpenDBGSessionInBrowserCheckBox = new Button(comp, SWT.CHECK);
		fOpenDBGSessionInBrowserCheckBox
				.setText(PHPDebugUiMessages
						.getString("LaunchConfigurationTab.PHPEnvironment.remoteDebugTab.OpenDBGSessionInBrowserCheckBox.label"));
		fOpenDBGSessionInBrowserCheckBox.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_BEGINNING));
		fOpenDBGSessionInBrowserCheckBox.addSelectionListener(fListener);

		// addendum
		fOpenDBGSessionInExternalBrowserCheckBox = new Button(comp, SWT.CHECK);
		fOpenDBGSessionInExternalBrowserCheckBox
				.setText(PHPDebugUiMessages
						.getString("LaunchConfigurationTab.PHPEnvironment.remoteDebugTab.OpenDBGSessionInExternalBrowserCheckBox.label"));
		fOpenDBGSessionInExternalBrowserCheckBox.setLayoutData(new GridData(
				SWT.BEGINNING));
		((GridData) fOpenDBGSessionInExternalBrowserCheckBox.getLayoutData()).horizontalIndent = 16;
		fOpenDBGSessionInExternalBrowserCheckBox
				.addSelectionListener(fListener);
		// addendum

		label = new Label(comp, SWT.NONE);
		label
				.setText(PHPDebugUiMessages
						.getString("LaunchConfigurationTab.PHPEnvironment.remoteDebugTab.RemoteSourcePath.label"));
		fRemoteSourcePath = new Text(comp, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fRemoteSourcePath.setLayoutData(gd);
		fRemoteSourcePath.addModifyListener(fListener);

		// addendum - make an effect of RemoteSourcePath clear
		Composite targetComp = new Composite(comp, SWT.NONE);
		targetComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout targetLayout = new GridLayout(4, false);
		targetLayout.marginHeight = 0;
		targetLayout.marginWidth = 3;
		targetComp.setLayout(targetLayout);
		Color targetColor = new Color(null, 160, 160, 160);
		Label label_lp = new Label(targetComp, SWT.NONE);
		label_lp.setText("(");
		label_lp.setForeground(targetColor);
		label_lp.setLayoutData(new GridData(GridData.BEGINNING));
		Label targetLabel = new Label(targetComp, SWT.NONE);
		targetLabel
				.setText(PHPDebugUiMessages
						.getString("LaunchConfigurationTab.PHPEnvironment.remoteDebugTab.TargetFile.label"));
		targetLabel.setForeground(targetColor);
		targetLabel.setLayoutData(new GridData(GridData.BEGINNING));
		targetFile = new Text(targetComp, SWT.SINGLE);
		targetFile.setForeground(targetColor);
		targetFile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		targetFile.setEditable(false);
		Label label_rp = new Label(targetComp, SWT.NONE);
		label_rp.setText(")");
		label_rp.setForeground(targetColor);
		label_rp.setLayoutData(new GridData(GridData.END));
		// addendum

		createVerticalSpacer(comp, 1);

		Composite pathMapComp = new Composite(comp, SWT.NONE);
		gd = new GridData(GridData.FILL_BOTH);
		pathMapComp.setLayoutData(gd);
		GridLayout parametersLayout = new GridLayout();
		parametersLayout.numColumns = 2;
		parametersLayout.marginHeight = 0;
		parametersLayout.marginWidth = 0;
		pathMapComp.setLayout(parametersLayout);

		Label pathMapLabel = new Label(pathMapComp, SWT.NONE);
		pathMapLabel
				.setText(PHPDebugUiMessages
						.getString("LaunchConfigurationTab.PHPEnvironment.remoteDebugTab.PathMap.label"));
		gd = new GridData();
		gd.horizontalSpan = 2;
		pathMapLabel.setLayoutData(gd);

		fRemoteDebugPathMapTable = new Table(pathMapComp, SWT.BORDER
				| SWT.MULTI);
		TableLayout tableLayout = new TableLayout();
		fRemoteDebugPathMapTable.setLayout(tableLayout);

		gd = new GridData(GridData.FILL_BOTH);
		fRemoteDebugPathMapTable.setLayoutData(gd);
		TableColumn column1 = new TableColumn(this.fRemoteDebugPathMapTable,
				SWT.NONE);
		column1
				.setText(PHPDebugUiMessages
						.getString("LaunchConfigurationTab.PHPEnvironment.remoteDebugTab.PathMap.Table.Title.local")); //$NON-NLS-1$
		TableColumn column2 = new TableColumn(this.fRemoteDebugPathMapTable,
				SWT.NONE);
		column2
				.setText(PHPDebugUiMessages
						.getString("LaunchConfigurationTab.PHPEnvironment.remoteDebugTab.PathMap.Table.Title.remote")); //$NON-NLS-1$
		tableLayout.addColumnData(new ColumnWeightData(100));
		tableLayout.addColumnData(new ColumnWeightData(100));
		fRemoteDebugPathMapTable.setHeaderVisible(true);
		fRemoteDebugPathMapTable.setLinesVisible(true);
		fRemoteDebugPathMapTable.addSelectionListener(fListener);
		fRemoteDebugPathMapTable.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				setPathMapButtonsEnableState();
				if (fPathMapEditButton.isEnabled()) {
					handlePathMapEditButtonSelected();
				}
			}
		});
		// fRemoteDebugPathMapTable.setEnabled(false);

		Composite envButtonComp = new Composite(pathMapComp, SWT.NONE);
		GridLayout envButtonLayout = new GridLayout();
		envButtonLayout.marginHeight = 0;
		envButtonLayout.marginWidth = 0;
		envButtonComp.setLayout(envButtonLayout);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING
				| GridData.HORIZONTAL_ALIGN_FILL);
		envButtonComp.setLayoutData(gd);

		fPathMapAddButton = createPushButton(
				envButtonComp,
				PHPDebugUiMessages
						.getString("LaunchConfigurationTab.PHPEnvironment.remoteDebugTab.PathMap.Button.Add.label"), null); //$NON-NLS-1$
		fPathMapAddButton.addSelectionListener(fListener);
		// fPathMapAddButton.setEnabled(false);

		fPathMapEditButton = createPushButton(
				envButtonComp,
				PHPDebugUiMessages
						.getString("LaunchConfigurationTab.PHPEnvironment.remoteDebugTab.PathMap.Button.Edit.label"), null); //$NON-NLS-1$
		fPathMapEditButton.addSelectionListener(fListener);
		// fPathMapEditButton.setEnabled(false);

		fPathMapRemoveButton = createPushButton(
				envButtonComp,
				PHPDebugUiMessages
						.getString("LaunchConfigurationTab.PHPEnvironment.remoteDebugTab.PathMap.Button.Remove.label"), null); //$NON-NLS-1$
		fPathMapRemoveButton.addSelectionListener(fListener);
		// fPathMapRemoveButton.setEnabled(false);

	}

	void handlePathMapAddButtonSelected() {
		EditPathMapDialog dialog = new EditPathMapDialog(getShell(),
				"Edit File Map", new String[] { EMPTY_STRING, EMPTY_STRING });
		openNewPathMapDialog(dialog, null);
		// dialog.create();
		// if (dialog.open()==EditPathMapDialog.OK)
		// {
		// TableItem item = new TableItem (fRemoteDebugPathMapTable, SWT.NONE);
		// item.setText(0,dialog.getLocalPath());
		// item.setText(1,dialog.getRemotePath());
		// updateLaunchConfigurationDialog();
		// }
		// updateLaunchConfigurationDialog();
		setPathMapButtonsEnableState();
	}

	void handlePathMapRemoveButtonSelected() {
		int[] selectedIndices = this.fRemoteDebugPathMapTable
				.getSelectionIndices();
		this.fRemoteDebugPathMapTable.remove(selectedIndices);
		setPathMapButtonsEnableState();
		updateLaunchConfigurationDialog();
	}

	void handlePathMapEditButtonSelected() {
		TableItem selectedItem = this.fRemoteDebugPathMapTable.getSelection()[0];
		String local = selectedItem.getText(0);
		String remote = selectedItem.getText(1);
		EditPathMapDialog dialog = new EditPathMapDialog(getShell(),
				"Edit File Map", new String[] { local, remote });
		openNewPathMapDialog(dialog, selectedItem);
	}

	/**
	 * Set the enabled state of whole tab.
	 */
	private void setRemoteTabEnableState() {
		boolean state = fRemoteDebugCheckBox.getSelection();
		fRemoteSourcePath.setEnabled(state);
		fRemoteDebugTranslate.setEnabled(state);

		fRemoteDebugPathMapTable.setEnabled(state);
		if (!state) {
			fPathMapEditButton.setEnabled(false);
			fPathMapRemoveButton.setEnabled(false);
			fPathMapAddButton.setEnabled(false);
			fOpenDBGSessionInBrowserCheckBox.setEnabled(false);
			fOpenDBGSessionInExternalBrowserCheckBox.setEnabled(false);
		} else {
			setPathMapButtonsEnableState();
		}

		updateLaunchConfigurationDialog();
	}

	/**
	 * Set the enabled state of the three environment variable-related buttons
	 * based on the selection in the PathMapTable widget.
	 */
	private void setPathMapButtonsEnableState() {
		// just do nothing for now
		//
		if (fRemoteDebugCheckBox.getSelection()) {
			fOpenDBGSessionInBrowserCheckBox.setEnabled(true);
			fOpenDBGSessionInExternalBrowserCheckBox
					.setEnabled(fOpenDBGSessionInBrowserCheckBox.getSelection());
			fRemoteDebugTranslate.setEnabled(true);
			int selectCount = this.fRemoteDebugPathMapTable
					.getSelectionIndices().length;
			if (selectCount < 1) {
				fPathMapEditButton.setEnabled(false);
				fPathMapRemoveButton.setEnabled(false);
			} else {
				fPathMapRemoveButton.setEnabled(true);
				if (selectCount == 1) {
					fPathMapEditButton.setEnabled(true);
				} else {
					fPathMapEditButton.setEnabled(false);
				}
			}
			fPathMapAddButton.setEnabled(true);
		}
	}

	/**
	 * Show the specified dialog and update the pathMapTable table based on its
	 * results.
	 * 
	 * @param updateItem
	 *            the item to update, or <code>null</code> if adding a new
	 *            item
	 */
	private void openNewPathMapDialog(EditPathMapDialog dialog,
			TableItem updateItem) {
		if (dialog.open() != EditPathMapDialog.OK) {
			return;
		}
		String[] pathPair = dialog.getPathPair();
		TableItem tableItem = updateItem;
		if (tableItem == null) {
			tableItem = getTableItemForName(pathPair[0]);
			if (tableItem == null) {
				tableItem = new TableItem(this.fRemoteDebugPathMapTable,
						SWT.NONE);
			}
		}
		tableItem.setText(pathPair);
		this.fRemoteDebugPathMapTable
				.setSelection(new TableItem[] { tableItem });
		updateLaunchConfigurationDialog();
	}

	/**
	 * Helper method that indicates whether the specified parameter name is
	 * already present in the parameters table.
	 */
	private TableItem getTableItemForName(String candidateName) {
		TableItem[] items = this.fRemoteDebugPathMapTable.getItems();
		for (int i = 0; i < items.length; i++) {
			String name = items[i].getText(0);
			if (name.equals(candidateName)) {
				return items[i];
			}
		}
		return null;
	}

	// protected void addLoadPathTab(TabFolder tabFolder) {
	// Composite loadPathComposite = new Composite(tabFolder, SWT.NONE);
	// loadPathComposite.setLayout(new GridLayout());
	//
	// loadPathListViewer = new ListViewer(loadPathComposite, SWT.BORDER |
	// SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
	// loadPathListViewer.setContentProvider(new ListContentProvider());
	// loadPathListViewer.setLabelProvider(new LoadPathEntryLabelProvider());
	// loadPathListViewer.getList().setLayoutData(new
	// GridData(GridData.FILL_BOTH));
	//
	// TabItem loadPathTab = new TabItem(tabFolder, SWT.NONE, 0);
	// loadPathTab.setText(PHPDebugUiMessages.getString("LaunchConfigurationTab.PHPEnvironment.loadPathTab.label"));
	// loadPathTab.setControl(loadPathComposite);
	// loadPathTab.setData(loadPathListViewer);

	// loadPathDefaultButton = new Button(loadPathComposite, SWT.CHECK);
	// loadPathDefaultButton.setText(PHPDebugUiMessages.getString("LaunchConfigurationTab.PHPEnvironment.loadPathDefaultButton.label"));
	// loadPathDefaultButton.setLayoutData(new
	// GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
	// loadPathDefaultButton.addSelectionListener(getLoadPathDefaultButtonSelectionListener());
	//		
	// loadPathDefaultButton.setEnabled(false); //for now, until the load path
	// is customizable on the configuration
	// }

	protected SelectionListener getLoadPathSelectionListener() {
		return new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				System.out.println("Loadpath list selection occurred: "
						+ e.getSource());
			}
		};
	}

	protected SelectionListener getLoadPathDefaultButtonSelectionListener() {
		return new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setUseLoadPathDefaults(((Button) e.getSource()).getSelection());
			}
		};
	}

	protected void addInterpreterTab(TabFolder tabFolder) {
		Composite interpreterComposite = new Composite(tabFolder, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		// layout.marginHeight = 0;
		// layout.marginWidth = 0;
		interpreterComposite.setLayout(layout);
		interpreterComposite.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));

		createVerticalSpacer(interpreterComposite, 2);

		interpreterCombo = new Combo(interpreterComposite, SWT.READ_ONLY);
		interpreterCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		initializeInterpreterCombo(interpreterCombo);
		interpreterCombo.addModifyListener(getInterpreterComboModifyListener());

		Button interpreterAddButton = new Button(interpreterComposite, SWT.PUSH);
		interpreterAddButton
				.setText(PHPDebugUiMessages
						.getString("LaunchConfigurationTab.PHPEnvironment.interpreterAddButton.label"));
		interpreterAddButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				PHPInterpreter newInterpreter = new PHPInterpreter(null);
				File phpRuntime = PHPInterpreterPreferencePage.getFile(
						getShell(), null);
				if (phpRuntime != null) {
					newInterpreter.setInstallLocation(phpRuntime);
					PHPRuntime.getDefault().addInstalledInterpreter(
							newInterpreter);
					interpreterCombo.add(newInterpreter.getInstallLocation()
							.toString());
					interpreterCombo.select(interpreterCombo
							.indexOf(newInterpreter.getInstallLocation()
									.toString()));
				}
			}
		});

		TabItem interpreterTab = new TabItem(tabFolder, SWT.NONE);
		interpreterTab
				.setText(PHPDebugUiMessages
						.getString("LaunchConfigurationTab.PHPEnvironment.interpreterTab.label"));
		interpreterTab.setControl(interpreterComposite);
	}

	protected ModifyListener getInterpreterComboModifyListener() {
		return new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		};
	}

	protected void createVerticalSpacer(Composite comp, int colSpan) {
		Label label = new Label(comp, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = colSpan;
		label.setLayoutData(gd);
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		PHPInterpreter selectedInterpreter = PHPRuntime.getDefault()
				.getSelectedInterpreter();
		if (selectedInterpreter != null) {
			String interpreterLocation = selectedInterpreter
					.getInstallLocation().toString();
			configuration.setAttribute(
					PHPLaunchConfigurationAttribute.SELECTED_INTERPRETER,
					interpreterLocation);
		}
		try {
			String projectName = configuration.getAttribute(
					PHPLaunchConfigurationAttribute.PROJECT_NAME, "");
			if (projectName != "") {
				IProject project = ResourcesPlugin.getWorkspace().getRoot()
						.getProject(projectName);
				if (project != null) {
					IPath remotePath = project.getFullPath();
					String fileName = configuration.getAttribute(
							PHPLaunchConfigurationAttribute.FILE_NAME, "");
					if (fileName != "") {
						Path filePath = new Path(fileName);
						remotePath = remotePath.append(filePath
								.removeLastSegments(1));
					}
					configuration.setAttribute(
							PHPLaunchConfigurationAttribute.REMOTE_PATH,
							remotePath.toOSString());
				}
			}
		} catch (CoreException e) {
			log(e);
		}
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		// initializeLoadPath(configuration);
		initializeInterpreterSelection(configuration);
		initializeRemoteDebug(configuration);
	}

	protected void initializeRemoteDebug(ILaunchConfiguration configuration) {
		try {
			fRemoteDebugCheckBox.setSelection(configuration.getAttribute(
					PHPLaunchConfigurationAttribute.REMOTE_DEBUG,
					DEFAULT_REMOTE_DEBUG));
		} catch (CoreException ce) {
			fRemoteDebugCheckBox.setSelection(DEFAULT_REMOTE_DEBUG);
		}
		tabFolder.setSelection(fRemoteDebugCheckBox.getSelection() ? 0 : 1);
		try {
			fRemoteDebugTranslate.setSelection(configuration.getAttribute(
					PHPLaunchConfigurationAttribute.REMOTE_DEBUG_TRANSLATE,
					DEFAULT_REMOTE_DEBUG_TRANSLATE));
		} catch (CoreException ce) {
			fRemoteDebugTranslate.setSelection(DEFAULT_REMOTE_DEBUG_TRANSLATE);
		}
		try {
			fOpenDBGSessionInBrowserCheckBox
					.setSelection(configuration
							.getAttribute(
									PHPLaunchConfigurationAttribute.OPEN_DBGSESSION_IN_BROWSER,
									DEFAULT_OPEN_DBGSESSION_IN_BROWSER));
		} catch (CoreException ce) {
			fOpenDBGSessionInBrowserCheckBox
					.setSelection(DEFAULT_OPEN_DBGSESSION_IN_BROWSER);
		}
		try {
			fOpenDBGSessionInExternalBrowserCheckBox
					.setSelection(configuration
							.getAttribute(
									PHPLaunchConfigurationAttribute.OPEN_DBGSESSION_IN_EXTERNAL_BROWSER,
									DEFAULT_OPEN_DBGSESSION_IN_EXTERNAL_BROWSER));
		} catch (CoreException ce) {
			fOpenDBGSessionInExternalBrowserCheckBox
					.setSelection(DEFAULT_OPEN_DBGSESSION_IN_EXTERNAL_BROWSER);
		}

		setRemoteTabEnableState();

		try {
			fRemoteSourcePath.setText(configuration.getAttribute(
					PHPLaunchConfigurationAttribute.REMOTE_PATH, ""));
		} catch (CoreException ce) {
			fRemoteSourcePath.setText("");
		}

		updatePathMapFromConfig(configuration);

		try {
			originalFileName = configuration.getAttribute(
					PHPLaunchConfigurationAttribute.FILE_NAME, "");
			makeupTargetFile();
		} catch (CoreException ce) {
			originalFileName = "";
		}

	}

	private void updatePathMapFromConfig(ILaunchConfiguration config) {
		Map envVars = null;
		try {
			if (config != null) {
				envVars = config.getAttribute(
						PHPLaunchConfigurationAttribute.FILE_MAP, (Map) null);
			}
			updatePathMapTable(envVars, this.fRemoteDebugPathMapTable);
			setPathMapButtonsEnableState();
		} catch (CoreException ce) {
			log(ce);
		}
	}

	private void updatePathMapTable(Map map, Table tableWidget) {
		tableWidget.removeAll();
		if (map == null) {
			return;
		}
		Iterator iterator = map.keySet().iterator();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			String value = (String) map.get(key);
			TableItem tableItem = new TableItem(tableWidget, SWT.NONE);
			tableItem.setText(new String[] { key, value });
		}
	}

	// protected void initializeLoadPath(ILaunchConfiguration configuration) {
	// boolean useDefaultLoadPath = true;
	// try {
	// useDefaultLoadPath =
	// configuration.getAttribute(PHPLaunchConfigurationAttribute.USE_DEFAULT_LOAD_PATH,
	// true);
	// setUseLoadPathDefaults(useDefaultLoadPath);
	// if (useDefaultLoadPath) {
	// String projectName =
	// configuration.getAttribute(PHPLaunchConfigurationAttribute.PROJECT_NAME,
	// "");
	// if (projectName != "") {
	// IProject aProject =
	// PHPeclipsePlugin.getWorkspace().getRoot().getProject(projectName);
	// if ((aProject != null) && JavaCore.isPHPProject(aProject)) {
	// JavaProject thePHPProject = new JavaProject();
	// thePHPProject.setProject(aProject);
	// List loadPathEntries = thePHPProject.getLoadPathEntries();
	// loadPathListViewer.setInput(loadPathEntries);
	// }
	// }
	// }
	// } catch (CoreException e) {
	// log(e);
	// }
	// }

	protected void setUseLoadPathDefaults(boolean useDefaults) {
		loadPathListViewer.getList().setEnabled(!useDefaults);
		// loadPathDefaultButton.setSelection(useDefaults);
	}

	protected void initializeInterpreterSelection(
			ILaunchConfiguration configuration) {
		String interpreterName = null;
		try {
			interpreterName = configuration.getAttribute(
					PHPLaunchConfigurationAttribute.SELECTED_INTERPRETER, "");
		} catch (CoreException e) {
			log(e);
		}
		if (interpreterName != null && !interpreterName.equals("")) {
			interpreterCombo.select(interpreterCombo.indexOf(interpreterName));
		}
		if (interpreterCombo.getSelectionIndex() < 0) {
			// previous definition had been deleted
			((ILaunchConfigurationWorkingCopy) configuration).setAttribute(
					PHPLaunchConfigurationAttribute.SELECTED_INTERPRETER, "");
			setErrorMessage(PHPDebugUiMessages
					.getString("LaunchConfigurationTab.PHPEnvironment.interpreter_not_selected_error_message"));
		}
	}

	protected void initializeInterpreterCombo(Combo interpreterCombo) {
		installedInterpretersWorkingCopy = new ArrayList();
		installedInterpretersWorkingCopy.addAll(PHPRuntime.getDefault()
				.getInstalledInterpreters());

		String[] interpreterNames = new String[installedInterpretersWorkingCopy
				.size()];
		for (int interpreterIndex = 0; interpreterIndex < installedInterpretersWorkingCopy
				.size(); interpreterIndex++) {
			PHPInterpreter interpreter = (PHPInterpreter) installedInterpretersWorkingCopy
					.get(interpreterIndex);
			interpreterNames[interpreterIndex] = interpreter
					.getInstallLocation().toString();
		}
		interpreterCombo.setItems(interpreterNames);

		PHPInterpreter selectedInterpreter = PHPRuntime.getDefault()
				.getSelectedInterpreter();
		if (selectedInterpreter != null)
			interpreterCombo.select(interpreterCombo
					.indexOf(selectedInterpreter.getInstallLocation()
							.toString()));
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		int selectionIndex = interpreterCombo.getSelectionIndex();
		if (selectionIndex >= 0)
			configuration.setAttribute(
					PHPLaunchConfigurationAttribute.SELECTED_INTERPRETER,
					interpreterCombo.getItem(selectionIndex));

		// configuration.setAttribute(PHPLaunchConfigurationAttribute.USE_DEFAULT_LOAD_PATH,
		// loadPathDefaultButton.getSelection());

		// if (!loadPathDefaultButton.getSelection()) {
		// List loadPathEntries = (List) loadPathListViewer.getInput();
		// List loadPathStrings = new ArrayList();
		// for (Iterator iterator = loadPathEntries.iterator();
		// iterator.hasNext();) {
		// LoadPathEntry entry = (LoadPathEntry) iterator.next();
		// loadPathStrings.add(entry.getPath().toString());
		// }
		// configuration.setAttribute(PHPLaunchConfigurationAttribute.CUSTOM_LOAD_PATH,
		// loadPathStrings);
		// }

		configuration.setAttribute(
				PHPLaunchConfigurationAttribute.REMOTE_DEBUG,
				fRemoteDebugCheckBox.getSelection());
		configuration.setAttribute(
				PHPLaunchConfigurationAttribute.REMOTE_DEBUG_TRANSLATE,
				fRemoteDebugTranslate.getSelection());
		configuration.setAttribute(PHPLaunchConfigurationAttribute.FILE_MAP,
				getMapFromPathMapTable());
		configuration.setAttribute(PHPLaunchConfigurationAttribute.REMOTE_PATH,
				fRemoteSourcePath.getText());
		configuration.setAttribute(
				PHPLaunchConfigurationAttribute.OPEN_DBGSESSION_IN_BROWSER,
				fOpenDBGSessionInBrowserCheckBox.getSelection());
		configuration
				.setAttribute(
						PHPLaunchConfigurationAttribute.OPEN_DBGSESSION_IN_EXTERNAL_BROWSER,
						fOpenDBGSessionInExternalBrowserCheckBox.getSelection());
	}

	protected Composite createPageRoot(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		createVerticalSpacer(composite, 2);
		setControl(composite);

		return composite;
	}

	private Map getMapFromPathMapTable() {
		TableItem[] items = fRemoteDebugPathMapTable.getItems();
		if (items.length == 0) {
			return null;
		}
		Map map = new HashMap(items.length);
		for (int i = 0; i < items.length; i++) {
			TableItem item = items[i];
			String key = item.getText(0);
			String value = item.getText(1);
			map.put(key, value);
		}
		return map;
	}

	public String getName() {
		return PHPDebugUiMessages
				.getString("LaunchConfigurationTab.PHPEnvironment.name");
	}

	public boolean isValid(ILaunchConfiguration launchConfig) {
		try {
			if (launchConfig.getAttribute(
					PHPLaunchConfigurationAttribute.SELECTED_INTERPRETER, "")
					.equals("")) {
				if (!launchConfig.getAttribute(
						PHPLaunchConfigurationAttribute.REMOTE_DEBUG, false)) {
					setErrorMessage(PHPDebugUiMessages
							.getString("LaunchConfigurationTab.PHPEnvironment.interpreter_not_selected_error_message"));
					return false;
				}
			}
		} catch (CoreException e) {
			log(e);
		}

		setErrorMessage(null);
		return true;
	}

	protected void log(Throwable t) {
		PHPDebugUiPlugin.log(t);
	}

	public Image getImage() {
		return PHPUiImages.get(PHPUiImages.IMG_CTOOLS_PHP);
	}

	private void makeupTargetFile() {
		if (!fRemoteDebugCheckBox.getSelection() || originalFileName.equals("")) {
			targetFile.setText("");
			return;
		}

		// see net.sourceforge.phpdt.internal.debug.core.PHPDBGProxy.MapPath(PHPLineBreakpoint)

		IPath remoteSourcePath = new Path(fRemoteSourcePath.getText());
		IPath filename = new Path(originalFileName);
		filename = remoteSourcePath.append(filename);
		String path = filename.toOSString();
		Map pathmap = getMapFromPathMapTable();

		if (pathmap != null) {
			Iterator it = pathmap.keySet().iterator();
			while (it.hasNext()) {
				String k = (String) it.next();
				if (path.startsWith(k)) {
					path = pathmap.get(k) + path.substring(k.length());
					break;
				}
			}
		}

		if (remoteSourcePath.isEmpty()) {
			if (pathmap != null) {
				Iterator it = pathmap.keySet().iterator();
				while (it.hasNext()) {
					String local = (String) it.next();
					IPath remotePath = new Path((String) pathmap.get(local));
					IPath localPath = new Path(local);
					if (localPath.isPrefixOf(filename)) {
						IPath newpath = filename.removeFirstSegments(localPath
								.matchingFirstSegments(filename));
						newpath = remotePath.append(newpath);
						path = newpath.toString();
						if (path.substring(0, 1).equals("/")) {
							path = path.replace('\\', '/');
						} else {
							path = path.replace('/', '\\');
						}
						break;
					}
				}
			}
		} else {
			if (fRemoteDebugTranslate.getSelection()) {
				if (remoteSourcePath.toString().substring(0, 1).equals("/")) {
					path = path.replace('\\', '/');
				} else {
					path = path.replace('/', '\\');
				}
			}
		}

		targetFile.setText(path);
	}

}