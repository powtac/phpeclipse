/*
 * Created on 14.07.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.sourceforge.phpdt.internal.debug.ui.launcher;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import net.sourceforge.phpdt.internal.debug.ui.PHPDebugUiMessages;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.MultipleInputDialog;
import org.eclipse.debug.internal.ui.launchConfigurations.EnvironmentVariable;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsMessages;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListSelectionDialog;

/**
 * @author Christian
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class PHPEnvironmentTab2 extends AbstractLaunchConfigurationTab {

	protected TableViewer environmentTable;

	protected String[] envTableColumnHeaders = {
			LaunchConfigurationsMessages.EnvironmentTab_Variable_1, //$NON-NLS-1$
			LaunchConfigurationsMessages.EnvironmentTab_Value_2 //$NON-NLS-1$
	};

	protected ColumnLayoutData[] envTableColumnLayouts = {
			new ColumnWeightData(50), new ColumnWeightData(50) };

	private static final String NAME_LABEL = LaunchConfigurationsMessages.EnvironmentTab_8; //$NON-NLS-1$

	private static final String VALUE_LABEL = LaunchConfigurationsMessages.EnvironmentTab_9; //$NON-NLS-1$

	protected static final String P_VARIABLE = "variable"; //$NON-NLS-1$

	protected static final String P_VALUE = "value"; //$NON-NLS-1$

	protected static String[] envTableColumnProperties = { P_VARIABLE, P_VALUE };

	protected Button envAddButton;

	protected Button envAddCGIButton;

	protected Button envEditButton;

	protected Button envRemoveButton;

	protected Button appendEnvironment;

	protected Button replaceEnvironment;

	protected Button envSelectButton;

	/**
	 * Content provider for the environment table
	 */
	protected class EnvironmentVariableContentProvider implements
			IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			EnvironmentVariable[] elements = new EnvironmentVariable[0];
			ILaunchConfiguration config = (ILaunchConfiguration) inputElement;
			Map m;
			try {
				m = config.getAttribute(
						ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, (Map) null);
			} catch (CoreException e) {
				DebugUIPlugin.log(new Status(IStatus.ERROR, DebugUIPlugin
						.getUniqueIdentifier(), IStatus.ERROR,
						"Error reading configuration", e)); //$NON-NLS-1$
				return elements;
			}
			if (m != null && !m.isEmpty()) {
				elements = new EnvironmentVariable[m.size()];
				String[] varNames = new String[m.size()];
				m.keySet().toArray(varNames);
				for (int i = 0; i < m.size(); i++) {
					elements[i] = new EnvironmentVariable(varNames[i],
							(String) m.get(varNames[i]));
				}
			}
			return elements;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput == null) {
				return;
			}
			if (viewer instanceof TableViewer) {
				TableViewer tableViewer = (TableViewer) viewer;
				if (tableViewer.getTable().isDisposed()) {
					return;
				}
				tableViewer.setSorter(new ViewerSorter() {
					public int compare(Viewer iviewer, Object e1, Object e2) {
						if (e1 == null) {
							return -1;
						} else if (e2 == null) {
							return 1;
						} else {
							return ((EnvironmentVariable) e1).getName()
									.compareToIgnoreCase(
											((EnvironmentVariable) e2)
													.getName());
						}
					}
				});
			}
		}
	}

	/**
	 * Label provider for the environment table
	 */
	public class EnvironmentVariableLabelProvider extends LabelProvider
			implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			String result = null;
			if (element != null) {
				EnvironmentVariable var = (EnvironmentVariable) element;
				switch (columnIndex) {
				case 0: // variable
					result = var.getName();
					break;
				case 1: // value
					result = var.getValue();
					break;
				}
			}
			return result;
		}

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		// Create main composite
		Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(
						getControl(),
						IDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_ENVIRONMENT_TAB);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(gridData);
		mainComposite.setFont(parent.getFont());

		createEnvironmentTable(mainComposite);
		createTableButtons(mainComposite);
		createAppendReplace(mainComposite);

		Dialog.applyDialogFont(mainComposite);
	}

	/**
	 * Creates and configures the widgets which allow the user to choose whether
	 * the specified environment should be appended to the native environment or
	 * if it should completely replace it.
	 * 
	 * @param parent
	 *            the composite in which the widgets should be created
	 */
	protected void createAppendReplace(Composite parent) {
		Composite appendReplaceComposite = new Composite(parent, SWT.NONE);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
		GridLayout layout = new GridLayout();
		appendReplaceComposite.setLayoutData(gridData);
		appendReplaceComposite.setLayout(layout);
		appendReplaceComposite.setFont(parent.getFont());

		appendEnvironment = createRadioButton(appendReplaceComposite,
				LaunchConfigurationsMessages.EnvironmentTab_16); //$NON-NLS-1$
		appendEnvironment.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		replaceEnvironment = createRadioButton(appendReplaceComposite,
				LaunchConfigurationsMessages.EnvironmentTab_17); //$NON-NLS-1$
	}

	/**
	 * Updates the enablement of the append/replace widgets. The widgets should
	 * disable when there are no environment variables specified.
	 */
	protected void updateAppendReplace() {
		boolean enable = environmentTable.getTable().getItemCount() > 0;
		appendEnvironment.setEnabled(enable);
		replaceEnvironment.setEnabled(enable);
	}

	/**
	 * Creates and configures the table that displayed the key/value pairs that
	 * comprise the environment.
	 * 
	 * @param parent
	 *            the composite in which the table should be created
	 */
	protected void createEnvironmentTable(Composite parent) {
		Font font = parent.getFont();
		// Create table composite
		Composite tableComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 1;
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 150;
		tableComposite.setLayout(layout);
		tableComposite.setLayoutData(gridData);
		tableComposite.setFont(font);
		// Create label
		Label label = new Label(tableComposite, SWT.NONE);
		label.setFont(font);
		label
				.setText(LaunchConfigurationsMessages.EnvironmentTab_Environment_variables_to_set__3); //$NON-NLS-1$
		// Create table
		environmentTable = new TableViewer(tableComposite, SWT.BORDER
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		Table table = environmentTable.getTable();
		TableLayout tableLayout = new TableLayout();
		table.setLayout(tableLayout);
		table.setHeaderVisible(true);
		table.setFont(font);
		gridData = new GridData(GridData.FILL_BOTH);
		environmentTable.getControl().setLayoutData(gridData);
		environmentTable
				.setContentProvider(new EnvironmentVariableContentProvider());
		environmentTable
				.setLabelProvider(new EnvironmentVariableLabelProvider());
		environmentTable.setColumnProperties(envTableColumnProperties);
		environmentTable
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						handleTableSelectionChanged(event);
					}
				});
		environmentTable.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				if (!environmentTable.getSelection().isEmpty()) {
					handleEnvEditButtonSelected();
				}
			}
		});
		// Create columns
		for (int i = 0; i < envTableColumnHeaders.length; i++) {
			tableLayout.addColumnData(envTableColumnLayouts[i]);
			TableColumn tc = new TableColumn(table, SWT.NONE, i);
			tc.setResizable(envTableColumnLayouts[i].resizable);
			tc.setText(envTableColumnHeaders[i]);
		}
	}

	/**
	 * Responds to a selection changed event in the environment table
	 * 
	 * @param event
	 *            the selection change event
	 */
	protected void handleTableSelectionChanged(SelectionChangedEvent event) {
		int size = ((IStructuredSelection) event.getSelection()).size();
		envEditButton.setEnabled(size == 1);
		envRemoveButton.setEnabled(size > 0);
	}

	/**
	 * Creates the add/edit/remove buttons for the environment table
	 * 
	 * @param parent
	 *            the composite in which the buttons should be created
	 */
	protected void createTableButtons(Composite parent) {
		// Create button composite
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		GridLayout glayout = new GridLayout();
		glayout.marginHeight = 0;
		glayout.marginWidth = 0;
		glayout.numColumns = 1;
		GridData gdata = new GridData(GridData.VERTICAL_ALIGN_BEGINNING
				| GridData.HORIZONTAL_ALIGN_END);
		buttonComposite.setLayout(glayout);
		buttonComposite.setLayoutData(gdata);
		buttonComposite.setFont(parent.getFont());

		createVerticalSpacer(buttonComposite, 1);
		// Create buttons
		envAddButton = createPushButton(buttonComposite,
				LaunchConfigurationsMessages.EnvironmentTab_New_4, null); //$NON-NLS-1$
		envAddButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				handleEnvAddButtonSelected();
			}
		});
		envAddCGIButton = createPushButton(
				buttonComposite,
				PHPDebugUiMessages
						.getString("LaunchConfigurationTab.PHPEnvironment2.CGIButton"), null); //$NON-NLS-1$
		envAddCGIButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				handleEnvAddCGIButtonSelected();
			}
		});

		envSelectButton = createPushButton(buttonComposite,
				LaunchConfigurationsMessages.EnvironmentTab_18, null); //$NON-NLS-1$
		envSelectButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				handleEnvSelectButtonSelected();
			}
		});
		envEditButton = createPushButton(buttonComposite,
				LaunchConfigurationsMessages.EnvironmentTab_Edit_5, null); //$NON-NLS-1$
		envEditButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				handleEnvEditButtonSelected();
			}
		});
		envEditButton.setEnabled(false);
		envRemoveButton = createPushButton(buttonComposite,
				LaunchConfigurationsMessages.EnvironmentTab_Remove_6, null); //$NON-NLS-1$
		envRemoveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				handleEnvRemoveButtonSelected();
			}
		});
		envRemoveButton.setEnabled(false);
	}

	/**
	 * Adds a new environment variable to the table.
	 */
	protected void handleEnvAddButtonSelected() {
		MultipleInputDialog dialog = new MultipleInputDialog(getShell(),
				LaunchConfigurationsMessages.EnvironmentTab_22); //$NON-NLS-1$
		dialog.addTextField(NAME_LABEL, null, false);
		dialog.addVariablesField(VALUE_LABEL, null, true);

		if (dialog.open() != Window.OK) {
			return;
		}

		String name = dialog.getStringValue(NAME_LABEL);
		String value = dialog.getStringValue(VALUE_LABEL);

		if (name != null && value != null && name.length() > 0
				&& value.length() > 0) {
			addVariable(new EnvironmentVariable(name.trim(), value.trim()));
			updateAppendReplace();
		}
	}

	/**
	 * Attempts to add the given variable. Returns whether the variable was
	 * added or not (as when the user answers not to overwrite an existing
	 * variable).
	 * 
	 * @param variable
	 *            the variable to add
	 * @return whether the variable was added
	 */
	protected boolean addVariable(EnvironmentVariable variable) {
		String name = variable.getName();
		TableItem[] items = environmentTable.getTable().getItems();
		for (int i = 0; i < items.length; i++) {
			EnvironmentVariable existingVariable = (EnvironmentVariable) items[i]
					.getData();
			if (existingVariable.getName().equals(name)) {
				boolean overWrite = MessageDialog.openQuestion(getShell(),
						LaunchConfigurationsMessages.EnvironmentTab_12,
						MessageFormat.format(
								LaunchConfigurationsMessages.EnvironmentTab_13,
								new String[] { name })); //$NON-NLS-1$ //$NON-NLS-2$
				if (!overWrite) {
					return false;
				}
				environmentTable.remove(existingVariable);
				break;
			}
		}
		environmentTable.add(variable);
		updateLaunchConfigurationDialog();
		return true;
	}

	/**
	 * Displays a dialog that allows user to select native environment variables
	 * to add to the table.
	 */
	private void handleEnvSelectButtonSelected() {
		// get Environment Variables from the OS
		Map envVariables = getNativeEnvironment();

		// get Environment Variables from the table
		TableItem[] items = environmentTable.getTable().getItems();
		for (int i = 0; i < items.length; i++) {
			EnvironmentVariable var = (EnvironmentVariable) items[i].getData();
			envVariables.remove(var.getName());
		}

		ListSelectionDialog dialog = new NativeEnvironmentDialog(getShell(),
				envVariables, createSelectionDialogContentProvider(),
				createSelectionDialogLabelProvider(),
				LaunchConfigurationsMessages.EnvironmentTab_19); //$NON-NLS-1$
		dialog.setTitle(LaunchConfigurationsMessages.EnvironmentTab_20); //$NON-NLS-1$

		int button = dialog.open();
		if (button == Window.OK) {
			Object[] selected = dialog.getResult();
			for (int i = 0; i < selected.length; i++) {
				environmentTable.add(selected[i]);
			}
		}

		updateAppendReplace();
		updateLaunchConfigurationDialog();
	}

	/**
	 * Displays a dialog that allows user to select native environment variables
	 * to add to the table.
	 */
	private void handleEnvAddCGIButtonSelected() {

		Map envVariables = new HashMap();

		envVariables.put("HTTP_COOKIE", new EnvironmentVariable("HTTP_COOKIE",
				"TestCookie=1"));
		envVariables.put("REDIRECT_QUERY_STRING", new EnvironmentVariable(
				"REDIRECT_QUERY_STRING", ""));
		envVariables.put("REDIRECT_STATUS", new EnvironmentVariable(
				"REDIRECT_STATUS", "200"));
		envVariables.put("REDIRECT_URL", new EnvironmentVariable(
				"REDIRECT_URL", ""));
		envVariables.put("SERVER_SOFTWARE", new EnvironmentVariable(
				"SERVER_SOFTWARE", "DBG / 2.1"));
		envVariables.put("SERVER_NAME", new EnvironmentVariable("SERVER_NAME",
				"localhost"));
		envVariables.put("SERVER_ADDR", new EnvironmentVariable("SERVER_ADDR",
				"127.0.0.1"));
		envVariables.put("SERVER_PORT", new EnvironmentVariable("SERVER_PORT",
				"80"));
		envVariables.put("REMOTE_ADDR", new EnvironmentVariable("REMOTE_ADDR",
				"127.0.0.1"));
		envVariables.put("GATEWAY_INTERFACE", new EnvironmentVariable(
				"GATEWAY_INTERFACE", "CGI / 1.1"));
		envVariables.put("SERVER_PROTOCOL", new EnvironmentVariable(
				"SERVER_PROTOCOL", "HTTP / 1.1"));
		envVariables.put("REQUEST_METHOD", new EnvironmentVariable(
				"REQUEST_METHOD", "GET"));
		envVariables.put("QUERY_STRING", new EnvironmentVariable(
				"QUERY_STRING", ""));
		envVariables.put("REDIRECT_QUERY_STRING", new EnvironmentVariable(
				"REDIRECT_QUERY_STRING", ""));
		// envVariables.put("REQUEST_URI" + OSFilePath;
		// envVariables.put("PATH_INFO=" + OSFilePath;
		// envVariables.put("PATH_TRANSLATED=" + OSFilePath;

		// get Environment Variables from the table
		TableItem[] items = environmentTable.getTable().getItems();
		for (int i = 0; i < items.length; i++) {
			EnvironmentVariable var = (EnvironmentVariable) items[i].getData();
			envVariables.remove(var.getName());
		}

		ListSelectionDialog dialog = new NativeEnvironmentDialog(getShell(),
				envVariables, createSelectionDialogContentProvider(),
				createSelectionDialogLabelProvider(),
				LaunchConfigurationsMessages.EnvironmentTab_19); //$NON-NLS-1$
		dialog.setTitle(LaunchConfigurationsMessages.EnvironmentTab_20); //$NON-NLS-1$

		int button = dialog.open();
		if (button == Window.OK) {
			Object[] selected = dialog.getResult();
			for (int i = 0; i < selected.length; i++) {
				environmentTable.add(selected[i]);
			}
		}

		updateAppendReplace();
		updateLaunchConfigurationDialog();
	}

	/**
	 * Creates a label provider for the native native environment variable
	 * selection dialog.
	 * 
	 * @return A label provider for the native native environment variable
	 *         selection dialog.
	 */
	private ILabelProvider createSelectionDialogLabelProvider() {
		return new ILabelProvider() {
			public Image getImage(Object element) {
				return null;
			}

			public String getText(Object element) {
				EnvironmentVariable var = (EnvironmentVariable) element;
				return var.getName() + " [" + var.getValue() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
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
		};
	}

	/**
	 * Creates a content provider for the native native environment variable
	 * selection dialog.
	 * 
	 * @return A content provider for the native native environment variable
	 *         selection dialog.
	 */
	private IStructuredContentProvider createSelectionDialogContentProvider() {
		return new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				EnvironmentVariable[] elements = null;
				if (inputElement instanceof HashMap) {
					Comparator comparator = new Comparator() {
						public int compare(Object o1, Object o2) {
							String s1 = (String) o1;
							String s2 = (String) o2;
							return s1.compareTo(s2);
						}

					};
					TreeMap envVars = new TreeMap(comparator);
					envVars.putAll((Map) inputElement);
					elements = new EnvironmentVariable[envVars.size()];
					int index = 0;
					for (Iterator iterator = envVars.keySet().iterator(); iterator
							.hasNext(); index++) {
						Object key = iterator.next();
						elements[index] = (EnvironmentVariable) envVars
								.get(key);
					}
				}
				return elements;
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}
		};
	}

	/**
	 * Gets native environment variable from the LaunchManager. Creates
	 * EnvironmentVariable objects.
	 * 
	 * @return Map of name - EnvironmentVariable pairs based on native
	 *         environment.
	 */
	private Map getNativeEnvironment() {
		Map stringVars = DebugPlugin.getDefault().getLaunchManager()
				.getNativeEnvironment();
		HashMap vars = new HashMap();
		for (Iterator i = stringVars.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			String value = (String) stringVars.get(key);
			vars.put(key, new EnvironmentVariable(key, value));
		}
		return vars;
	}

	/**
	 * Creates an editor for the value of the selected environment variable.
	 */
	private void handleEnvEditButtonSelected() {
		IStructuredSelection sel = (IStructuredSelection) environmentTable
				.getSelection();
		EnvironmentVariable var = (EnvironmentVariable) sel.getFirstElement();
		if (var == null) {
			return;
		}
		String originalName = var.getName();
		String value = var.getValue();
		MultipleInputDialog dialog = new MultipleInputDialog(getShell(),
				LaunchConfigurationsMessages.EnvironmentTab_11); //$NON-NLS-1$
		dialog.addTextField(NAME_LABEL, originalName, false);
		dialog.addVariablesField(VALUE_LABEL, value, true);

		if (dialog.open() != Window.OK) {
			return;
		}
		String name = dialog.getStringValue(NAME_LABEL);
		value = dialog.getStringValue(VALUE_LABEL);
		if (!originalName.equals(name)) {
			if (addVariable(new EnvironmentVariable(name, value))) {
				environmentTable.remove(var);
			}
		} else {
			var.setValue(value);
			environmentTable.update(var, null);
			updateLaunchConfigurationDialog();
		}
	}

	/**
	 * Removes the selected environment variable from the table.
	 */
	private void handleEnvRemoveButtonSelected() {
		IStructuredSelection sel = (IStructuredSelection) environmentTable
				.getSelection();
		environmentTable.getControl().setRedraw(false);
		for (Iterator i = sel.iterator(); i.hasNext();) {
			EnvironmentVariable var = (EnvironmentVariable) i.next();
			environmentTable.remove(var);
		}
		environmentTable.getControl().setRedraw(true);
		updateAppendReplace();
		updateLaunchConfigurationDialog();
	}

	/**
	 * Updates the environment table for the given launch configuration
	 * 
	 * @param configuration
	 */
	protected void updateEnvironment(ILaunchConfiguration configuration) {
		environmentTable.setInput(configuration);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		boolean append = true;
		try {
			append = configuration.getAttribute(
					ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);
		} catch (CoreException e) {
			DebugUIPlugin.log(e.getStatus());
		}
		if (append) {
			appendEnvironment.setSelection(true);
			replaceEnvironment.setSelection(false);
		} else {
			replaceEnvironment.setSelection(true);
			appendEnvironment.setSelection(false);
		}
		updateEnvironment(configuration);
		updateAppendReplace();
	}

	/**
	 * Stores the environment in the given configuration
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		// Convert the table's items into a Map so that this can be saved in the
		// configuration's attributes.
		TableItem[] items = environmentTable.getTable().getItems();
		Map map = new HashMap(items.length);
		for (int i = 0; i < items.length; i++) {
			EnvironmentVariable var = (EnvironmentVariable) items[i].getData();
			map.put(var.getName(), var.getValue());
		}
		if (map.size() == 0) {
			configuration.setAttribute(
					ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, (Map) null);
		} else {
			configuration.setAttribute(
					ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, map);
		}
		configuration.setAttribute(
				ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES,
				appendEnvironment.getSelection());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LaunchConfigurationsMessages.EnvironmentTab_Environment_7; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return DebugPluginImages
				.getImage(IDebugUIConstants.IMG_OBJS_ENVIRONMENT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#activated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		// do nothing when activated
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#deactivated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void deactivated(ILaunchConfigurationWorkingCopy workingCopy) {
		// do nothing when deactivated
	}

	private class NativeEnvironmentDialog extends ListSelectionDialog {
		public NativeEnvironmentDialog(Shell parentShell, Object input,
				IStructuredContentProvider contentProvider,
				ILabelProvider labelProvider, String message) {
			super(parentShell, input, contentProvider, labelProvider, message);
			setShellStyle(getShellStyle() | SWT.RESIZE);
		}

		protected IDialogSettings getDialogSettings() {
			IDialogSettings settings = DebugUIPlugin.getDefault()
					.getDialogSettings();
			IDialogSettings section = settings
					.getSection(getDialogSettingsSectionName());
			if (section == null) {
				section = settings
						.addNewSection(getDialogSettingsSectionName());
			}
			return section;
		}

		/**
		 * Returns the name of the section that this dialog stores its settings
		 * in
		 * 
		 * @return String
		 */
		protected String getDialogSettingsSectionName() {
			return IDebugUIConstants.PLUGIN_ID
					+ ".ENVIRONMENT_TAB.NATIVE_ENVIROMENT_DIALOG"; //$NON-NLS-1$
		}
	}
}
