package net.sourceforge.phpeclipse.xdebug.ui.php.launching;

import java.text.MessageFormat;
//import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
//import java.util.Map;
import java.util.Vector;

import net.sourceforge.phpeclipse.xdebug.core.PathMapItem;
import net.sourceforge.phpeclipse.xdebug.core.XDebugCorePlugin;
import net.sourceforge.phpeclipse.xdebug.php.launching.IXDebugConstants;
import net.sourceforge.phpeclipse.xdebug.ui.EditPathMapDialog;
/*import net.sourceforge.phpeclipse.xdebug.ui.EnvironmentVariable;
import net.sourceforge.phpeclipse.xdebug.ui.MultipleInputDialog;
import net.sourceforge.phpeclipse.xdebug.ui.php.launching.PHPEnvironmentTab.EnvironmentVariableContentProvider;
import net.sourceforge.phpeclipse.xdebug.ui.php.launching.PHPEnvironmentTab.EnvironmentVariableLabelProvider;
*/
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
//import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
//import org.eclipse.jface.viewers.ViewerSorter;
//import org.eclipse.jface.window.Window;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class PHPPathMapTab extends AbstractLaunchConfigurationTab {
	protected TableViewer fPathMapTable;
	protected Button fAddButton;
	protected Button envAddCGIButton;
	protected Button fEditButton;
	protected Button fRemoveButton;
	protected Button fUpButton;
	protected Button fDownButton;

	
	protected String[] fPathMapTableColumnHeaders = { "Local", "Remote" };

	protected ColumnLayoutData[] fPathMapTableColumnLayouts = {
			new ColumnWeightData(50), new ColumnWeightData(50) };
	
	protected static final String P_REMOTE = "remote"; //$NON-NLS-1$
	protected static final String P_LOCAL = "local"; //$NON-NLS-1$
	protected static String[] fPathMapTableColumnProperties = { P_REMOTE, P_LOCAL };

	
	/**
	 * Content provider for the environment table
	 */
	protected class PathMapContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			PathMapItem[] elements = new PathMapItem[0];
			ILaunchConfiguration config = (ILaunchConfiguration) inputElement;
			List l;
			try {
				l = config.getAttribute(IXDebugConstants.ATTR_PHP_PATHMAP, (List) null);
			} catch (CoreException e) {
				XDebugCorePlugin.log(new Status(IStatus.ERROR,
						XDebugCorePlugin.PLUGIN_ID, IStatus.ERROR,
						"Error reading configuration", e)); //$NON-NLS-1$
				return elements;
			}
			if (l != null && !l.isEmpty()) {
				elements = new PathMapItem[l.size()];
				for (int i = 0; i < l.size(); i++) {
					elements[i] = new PathMapItem((String) l.get(i));
				}
			}

			return elements;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	/**
	 * Label provider for the environment table
	 */
	public class PathMapItemLabelProvider extends LabelProvider
			implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			String result = null;
			if (element != null) {
				PathMapItem var = (PathMapItem) element;
				switch (columnIndex) {
				case 0: // local
					result = var.getLocalPath().toOSString();
					break;
				case 1: // remote
					result = var.getRemotePath().toString();
					break;
				}
			}
			return result;
		}

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}


	public void createControl(Composite parent) {
		// Create main composite
		Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		// WorkbenchHelp.setHelp(getControl(),
		// IDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_ENVIRONMENT_TAB);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(gridData);
		mainComposite.setFont(parent.getFont());

		createPathMapTable(mainComposite);
		createTableButtons(mainComposite);

		Dialog.applyDialogFont(mainComposite);
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
		fAddButton = createPushButton(buttonComposite, "New", null);
		fAddButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				handleAddButtonSelected();
			}
		});

		fEditButton = createPushButton(buttonComposite, "Edit", null);
		fEditButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				handleEditButtonSelected();
			}
		});
		fEditButton.setEnabled(false);

		fRemoveButton = createPushButton(buttonComposite, "Remove", null);
		fRemoveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				handleRemoveButtonSelected();
			}
		});
		fRemoveButton.setEnabled(false);
		
		fUpButton = createPushButton(buttonComposite, "Up", null);
		fUpButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				handleUpButtonSelected();
			}
		});
		fUpButton.setEnabled(false);
		
		fDownButton = createPushButton(buttonComposite, "Down", null);
		fDownButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				handleDownButtonSelected();
			}
		});
		fDownButton.setEnabled(false);
	}

	
	/**
	 * Creates and configures the table that displayed the key/value pairs that
	 * comprise the environment.
	 * 
	 * @param parent
	 *            the composite in which the table should be created
	 */
	protected void createPathMapTable(Composite parent) {
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
		label.setText("&Map remote path to local path");
		// Create table
		fPathMapTable = new TableViewer(tableComposite, SWT.BORDER
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		Table table = fPathMapTable.getTable();
		TableLayout tableLayout = new TableLayout();
		table.setLayout(tableLayout);
		table.setHeaderVisible(true);
		table.setFont(font);
		gridData = new GridData(GridData.FILL_BOTH);
		fPathMapTable.getControl().setLayoutData(gridData);
		fPathMapTable.setContentProvider(new PathMapContentProvider());
		fPathMapTable.setLabelProvider(new PathMapItemLabelProvider());
		fPathMapTable.setColumnProperties(fPathMapTableColumnProperties);
		fPathMapTable.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleTableSelectionChanged(event);
			}
		});
		fPathMapTable.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				if (!fPathMapTable.getSelection().isEmpty()) {
					handleEditButtonSelected();
				}
			}
		});
		// Create columns
		for (int i = 0; i < fPathMapTableColumnHeaders.length; i++) {
			tableLayout.addColumnData(fPathMapTableColumnLayouts[i]);
			TableColumn tc = new TableColumn(table, SWT.NONE, i);
			tc.setResizable(fPathMapTableColumnLayouts[i].resizable);
			tc.setText(fPathMapTableColumnHeaders[i]);
		}
	}
	
	/**
	 * Responds to a selection changed event in the environment table
	 * @param event the selection change event
	 */
	protected void handleTableSelectionChanged(SelectionChangedEvent event) {
		int size = ((IStructuredSelection)event.getSelection()).size();
		int idx = fPathMapTable.getTable().getSelectionIndex();
		int count = fPathMapTable.getTable().getItemCount();
		if (size==1) {
			fEditButton.setEnabled(idx>0);
			fUpButton.setEnabled(idx>0);
			fDownButton.setEnabled((idx>=0)&&(idx<count-1));
		}
		
		fRemoveButton.setEnabled(size > 0);
	}
	
	/**
	 * Creates an editor for the value of the selected environment variable.
	 */
	private void handleUpButtonSelected() {
		IStructuredSelection sel = (IStructuredSelection) fPathMapTable.getSelection();
		PathMapItem mapItem = (PathMapItem) sel.getFirstElement();
		boolean found=false;
		if (mapItem == null) {
			return;
		}
		IPath local = mapItem.getLocalPath();
		TableItem[] items = fPathMapTable.getTable().getItems();
		int i;
		for (i = 0; i < items.length; i++) {
			PathMapItem item = (PathMapItem) items[i].getData();
			if (item.getLocalPath().equals(local)) {
				found=true;
				break;
			}
		}
		if ((i>0) && found) {
			fPathMapTable.getControl().setRedraw(false);
			fPathMapTable.remove(mapItem);
			fPathMapTable.insert(mapItem,i-1);
			fPathMapTable.getControl().setRedraw(true);
			fPathMapTable.setSelection(new StructuredSelection(mapItem),true);
			updateLaunchConfigurationDialog();
		}
	}
	
	private void handleDownButtonSelected() {
		IStructuredSelection sel = (IStructuredSelection) fPathMapTable.getSelection();
		PathMapItem mapItem = (PathMapItem) sel.getFirstElement();
		boolean found=false;
		if (mapItem == null) {
			return;
		}
		IPath local = mapItem.getLocalPath();
		TableItem[] items = fPathMapTable.getTable().getItems();
		int i;
		for (i = 0; i < items.length; i++) {
			PathMapItem item = (PathMapItem) items[i].getData();
			if (item.getLocalPath().equals(local)) {
				found=true;
				break;
			}
		}

		if ((i<items.length-1) && found) {
			fPathMapTable.getControl().setRedraw(false);
			fPathMapTable.remove(mapItem);
			fPathMapTable.insert(mapItem,i+1);
			fPathMapTable.getControl().setRedraw(true);
			fPathMapTable.setSelection(new StructuredSelection(mapItem),true);
			updateLaunchConfigurationDialog();
		}
	}
	
	/**
	 * Creates an editor for the value of the selected environment variable.
	 */
	private void handleEditButtonSelected() {
		IStructuredSelection sel = (IStructuredSelection) fPathMapTable.getSelection();
		PathMapItem item = (PathMapItem) sel.getFirstElement();
		if (item == null) {
			return;
		}

		EditPathMapDialog dialog = new EditPathMapDialog(getShell(), "Edit pathmap", new String[] { item.getLocalPath().toString(),item.getRemotePath().toString() });

		if (dialog.open() != EditPathMapDialog.OK) {
			return;
		}
		String[] pathPair = dialog.getPathPair();
		String newLocalPath=pathPair[0];
		String newRemotePath=pathPair[1];
		
		if (!item.getLocalPath().toString().equals(newLocalPath)) {
			if (addVariable(new PathMapItem(newLocalPath,newRemotePath))) {
				fPathMapTable.remove(item);
			}
		} else {
			item.setRemotePath(newRemotePath);
			fPathMapTable.update(item, null);
			updateLaunchConfigurationDialog();
		}
	}
	
	/**
	 * Adds a new environment variable to the table.
	 */
	protected void handleAddButtonSelected() {
	    EditPathMapDialog dialog = new EditPathMapDialog(getShell(), "Edit File Map", new String[] { "", "" });
		if (dialog.open() != EditPathMapDialog.OK) {
			return;
		}
		String[] pathPair = dialog.getPathPair();
		
		Path local = new Path(pathPair[0]);
		Path remote = new Path(pathPair[1]);
		
		String strlocal = local.toString();
		String strremote = remote.toString();
		if (strlocal != null && strremote != null && strlocal.length() > 0 && strremote.length() > 0) {
				addVariable(new PathMapItem(strlocal,strremote));
		}
	}
	
	/**
	 * Removes the selected environment variable from the table.
	 */
	private void handleRemoveButtonSelected() {
		IStructuredSelection sel = (IStructuredSelection) fPathMapTable.getSelection();
		fPathMapTable.getControl().setRedraw(false);
		for (Iterator i = sel.iterator(); i.hasNext();) {
			PathMapItem item = (PathMapItem) i.next();
			fPathMapTable.remove(item);
		}
		fPathMapTable.getControl().setRedraw(true);
		updateLaunchConfigurationDialog();
	}

	/**
	 * Attempts to add the given variable. Returns whether the variable was
	 * added or not (as when the user answers not to overwrite an existing
	 * variable).
	 * 
	 * @param variable the variable to add
	 * @return whether the variable was added
	 */
	protected boolean addVariable(PathMapItem mapItem) {
		IPath local = mapItem.getLocalPath();
		TableItem[] items = fPathMapTable.getTable().getItems();
		for (int i = 0; i < items.length; i++) {
			PathMapItem item = (PathMapItem) items[i].getData();
			if (item.getLocalPath().equals(local)) {
				boolean overWrite = MessageDialog.openQuestion(getShell(),"Overwrite variable?",
						MessageFormat.format("A local path named {0} already exists. Overwrite?",new String[] { local.toString() }));
				if (!overWrite) {
					return false;
				}
				fPathMapTable.remove(item);
				break;
			}
		}
		fPathMapTable.add(mapItem);
		updateLaunchConfigurationDialog();
		return true;
	}
	


	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// TODO Auto-generated method stub

	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		fPathMapTable.setInput(configuration);
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		// Convert the table's items into a List so that this can be saved in the
		// configuration's attributes.
		TableItem[] items = fPathMapTable.getTable().getItems();
		List vec = new Vector(items.length);
		for (int i = 0; i < items.length; i++) {
			PathMapItem item = (PathMapItem) items[i].getData();
			vec.add(item.getStringData());
		}
		if (vec.size() == 0) {
			configuration.setAttribute(IXDebugConstants.ATTR_PHP_PATHMAP, (List) null);
		} else {
			configuration.setAttribute(IXDebugConstants.ATTR_PHP_PATHMAP, vec);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration launchConfig) {
		// need to use exception here!
		setErrorMessage(null);
		
		if (fPathMapTable.getTable().getItems().length == 0) {
			setErrorMessage("Mappath empty!");
			return false;
		}
		return true;
	}
		
	public String getName() {
		return "Pathmap";
	}
}