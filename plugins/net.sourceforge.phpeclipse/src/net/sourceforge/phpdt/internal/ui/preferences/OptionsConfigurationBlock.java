/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.phpdt.internal.ui.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import net.sourceforge.phpdt.core.IJavaProject;
import net.sourceforge.phpdt.core.JavaCore;
import net.sourceforge.phpdt.internal.ui.wizards.IStatusChangeListener;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

/**
 * Abstract options configuration block providing a general implementation for
 * setting up an options configuration page.
 * 
 * @since 2.1
 */
public abstract class OptionsConfigurationBlock {

	protected static class ControlData {
		private String fKey;

		private String[] fValues;

		public ControlData(String key, String[] values) {
			fKey = key;
			fValues = values;
		}

		public String getKey() {
			return fKey;
		}

		public String getValue(boolean selection) {
			int index = selection ? 0 : 1;
			return fValues[index];
		}

		public String getValue(int index) {
			return fValues[index];
		}

		public int getSelection(String value) {
			if (value != null) {
				for (int i = 0; i < fValues.length; i++) {
					if (value.equals(fValues[i])) {
						return i;
					}
				}
			}
			return fValues.length - 1; // assume the last option is the least
										// severe
		}
	}

	protected Map fWorkingValues;

	protected ArrayList fCheckBoxes;

	protected ArrayList fComboBoxes;

	protected ArrayList fTextBoxes;

	protected HashMap fLabels;

	private SelectionListener fSelectionListener;

	private ModifyListener fTextModifyListener;

	protected IStatusChangeListener fContext;

	protected IJavaProject fProject; // project or null

	protected String[] fAllKeys;

	private Shell fShell;

	public OptionsConfigurationBlock(IStatusChangeListener context,
			IJavaProject project, String[] allKeys) {
		fContext = context;
		fProject = project;
		fAllKeys = allKeys;

		fWorkingValues = getOptions(true);
		testIfOptionsComplete(fWorkingValues, allKeys);

		fCheckBoxes = new ArrayList();
		fComboBoxes = new ArrayList();
		fTextBoxes = new ArrayList(2);
		fLabels = new HashMap();
	}

	private void testIfOptionsComplete(Map workingValues, String[] allKeys) {
		for (int i = 0; i < allKeys.length; i++) {
			if (workingValues.get(allKeys[i]) == null) {
				PHPeclipsePlugin
						.logErrorMessage("preference option missing: " + allKeys[i] + " (" + this.getClass().getName() + ')'); //$NON-NLS-1$//$NON-NLS-2$
			}
		}
	}

	protected Map getOptions(boolean inheritJavaCoreOptions) {
		if (fProject != null) {
			return fProject.getOptions(inheritJavaCoreOptions);
		} else {
			return JavaCore.getOptions();
		}
	}

	protected Map getDefaultOptions() {
		return JavaCore.getDefaultOptions();
	}

	public final boolean hasProjectSpecificOptions() {
		if (fProject != null) {
			Map settings = fProject.getOptions(false);
			String[] allKeys = fAllKeys;
			for (int i = 0; i < allKeys.length; i++) {
				if (settings.get(allKeys[i]) != null) {
					return true;
				}
			}
		}
		return false;
	}

	protected void setOptions(Map map) {
		if (fProject != null) {
			Map oldOptions = fProject.getOptions(false);
			fProject.setOptions(map);
			firePropertyChangeEvents(oldOptions, map);
		} else {
			JavaCore.setOptions((Hashtable) map);
		}
	}

	/**
	 * Computes the differences between the given old and new options and fires
	 * corresponding property change events on the Java plugin's mockup
	 * preference store.
	 * 
	 * @param oldOptions
	 *            The old options
	 * @param newOptions
	 *            The new options
	 */
	private void firePropertyChangeEvents(Map oldOptions, Map newOptions) {
		oldOptions = new HashMap(oldOptions);
		Object source = fProject.getProject();
		MockupPreferenceStore store = PHPeclipsePlugin.getDefault()
				.getMockupPreferenceStore();
		Iterator iter = newOptions.entrySet().iterator();
		while (iter.hasNext()) {
			Entry entry = (Entry) iter.next();

			String name = (String) entry.getKey();
			Object oldValue = oldOptions.get(name);
			Object newValue = entry.getValue();

			if ((oldValue != null && !oldValue.equals(newValue))
					|| (oldValue == null && newValue != null))
				store.firePropertyChangeEvent(source, name, oldValue, newValue);
			oldOptions.remove(name);
		}

		iter = oldOptions.entrySet().iterator();
		while (iter.hasNext()) {
			Entry entry = (Entry) iter.next();
			store.firePropertyChangeEvent(source, (String) entry.getKey(),
					entry.getValue(), null);
		}
	}

	protected Shell getShell() {
		return fShell;
	}

	protected void setShell(Shell shell) {
		fShell = shell;
	}

	protected abstract Control createContents(Composite parent);

	protected Button addCheckBox(Composite parent, String label, String key,
			String[] values, int indent) {
		ControlData data = new ControlData(key, values);

		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 3;
		gd.horizontalIndent = indent;

		Button checkBox = new Button(parent, SWT.CHECK);
		checkBox.setText(label);
		checkBox.setData(data);
		checkBox.setLayoutData(gd);
		checkBox.addSelectionListener(getSelectionListener());

		String currValue = (String) fWorkingValues.get(key);
		checkBox.setSelection(data.getSelection(currValue) == 0);

		fCheckBoxes.add(checkBox);

		return checkBox;
	}

	protected Combo addComboBox(Composite parent, String label, String key,
			String[] values, String[] valueLabels, int indent) {
		ControlData data = new ControlData(key, values);

		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent = indent;

		Label labelControl = new Label(parent, SWT.LEFT | SWT.WRAP);
		labelControl.setText(label);
		labelControl.setLayoutData(gd);

		Combo comboBox = new Combo(parent, SWT.READ_ONLY);
		comboBox.setItems(valueLabels);
		comboBox.setData(data);
		comboBox.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		comboBox.addSelectionListener(getSelectionListener());

		fLabels.put(comboBox, labelControl);

		Label placeHolder = new Label(parent, SWT.NONE);
		placeHolder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		String currValue = (String) fWorkingValues.get(key);
		comboBox.select(data.getSelection(currValue));

		fComboBoxes.add(comboBox);
		return comboBox;
	}

	protected void addInversedComboBox(Composite parent, String label,
			String key, String[] values, String[] valueLabels, int indent) {
		ControlData data = new ControlData(key, values);

		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent = indent;
		gd.horizontalSpan = 3;

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		composite.setLayout(layout);
		composite.setLayoutData(gd);

		Combo comboBox = new Combo(composite, SWT.READ_ONLY);
		comboBox.setItems(valueLabels);
		comboBox.setData(data);
		comboBox.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		comboBox.addSelectionListener(getSelectionListener());

		Label labelControl = new Label(composite, SWT.LEFT | SWT.WRAP);
		labelControl.setText(label);
		labelControl.setLayoutData(new GridData());

		fLabels.put(comboBox, labelControl);

		String currValue = (String) fWorkingValues.get(key);
		comboBox.select(data.getSelection(currValue));

		fComboBoxes.add(comboBox);
	}

	protected Text addTextField(Composite parent, String label, String key,
			int indent, int widthHint) {
		Label labelControl = new Label(parent, SWT.NONE);
		labelControl.setText(label);
		labelControl.setLayoutData(new GridData());

		Text textBox = new Text(parent, SWT.BORDER | SWT.SINGLE);
		textBox.setData(key);
		textBox.setLayoutData(new GridData());

		fLabels.put(textBox, labelControl);

		String currValue = (String) fWorkingValues.get(key);
		if (currValue != null) {
			textBox.setText(currValue);
		}
		textBox.addModifyListener(getTextModifyListener());

		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		if (widthHint != 0) {
			data.widthHint = widthHint;
		}
		data.horizontalIndent = indent;
		data.horizontalSpan = 2;
		textBox.setLayoutData(data);

		fTextBoxes.add(textBox);
		return textBox;
	}

	protected SelectionListener getSelectionListener() {
		if (fSelectionListener == null) {
			fSelectionListener = new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
				}

				public void widgetSelected(SelectionEvent e) {
					controlChanged(e.widget);
				}
			};
		}
		return fSelectionListener;
	}

	protected ModifyListener getTextModifyListener() {
		if (fTextModifyListener == null) {
			fTextModifyListener = new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					textChanged((Text) e.widget);
				}
			};
		}
		return fTextModifyListener;
	}

	protected void controlChanged(Widget widget) {
		ControlData data = (ControlData) widget.getData();
		String newValue = null;
		if (widget instanceof Button) {
			newValue = data.getValue(((Button) widget).getSelection());
		} else if (widget instanceof Combo) {
			newValue = data.getValue(((Combo) widget).getSelectionIndex());
		} else {
			return;
		}
		fWorkingValues.put(data.getKey(), newValue);

		validateSettings(data.getKey(), newValue);
	}

	protected void textChanged(Text textControl) {
		String key = (String) textControl.getData();
		String number = textControl.getText();
		fWorkingValues.put(key, number);
		validateSettings(key, number);
	}

	protected boolean checkValue(String key, String value) {
		return value.equals(fWorkingValues.get(key));
	}

	/*
	 * (non-javadoc) Update fields and validate. @param changedKey Key that
	 * changed, or null, if all changed.
	 */
	protected abstract void validateSettings(String changedKey, String newValue);

	protected String[] getTokens(String text, String separator) {
		StringTokenizer tok = new StringTokenizer(text, separator); //$NON-NLS-1$
		int nTokens = tok.countTokens();
		String[] res = new String[nTokens];
		for (int i = 0; i < res.length; i++) {
			res[i] = tok.nextToken().trim();
		}
		return res;
	}

	public boolean performOk(boolean enabled) {
		String[] allKeys = fAllKeys;
		Map actualOptions = getOptions(false);

		// preserve other options
		boolean hasChanges = false;
		for (int i = 0; i < allKeys.length; i++) {
			String key = allKeys[i];
			String oldVal = (String) actualOptions.get(key);
			String val = null;
			if (enabled) {
				val = (String) fWorkingValues.get(key);
				if (val != null && !val.equals(oldVal)) {
					hasChanges = true;
					actualOptions.put(key, val);
				}
			} else {
				if (oldVal != null) {
					actualOptions.remove(key);
					hasChanges = true;
				}
			}
		}

		if (hasChanges) {
			boolean doBuild = false;
			String[] strings = getFullBuildDialogStrings(fProject == null);
			if (strings != null) {
				MessageDialog dialog = new MessageDialog(getShell(),
						strings[0], null, strings[1], MessageDialog.QUESTION,
						new String[] { IDialogConstants.YES_LABEL,
								IDialogConstants.NO_LABEL,
								IDialogConstants.CANCEL_LABEL }, 2);
				int res = dialog.open();
				if (res == 0) {
					doBuild = true;
				} else if (res != 1) {
					return false; // cancel pressed
				}
			}
			setOptions(actualOptions);
			if (doBuild) {
				boolean res = doFullBuild();
				if (!res) {
					return false;
				}
			}
		}
		return true;
	}

	protected abstract String[] getFullBuildDialogStrings(
			boolean workspaceSettings);

	protected boolean doFullBuild() {

		Job buildJob = new Job(PreferencesMessages
				.getString("OptionsConfigurationBlock.job.title")) { //$NON-NLS-1$
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
			 */
			protected IStatus run(IProgressMonitor monitor) {
				try {
					if (fProject != null) {
						monitor
								.setTaskName(PreferencesMessages
										.getFormattedString(
												"OptionsConfigurationBlock.buildproject.taskname", fProject.getElementName())); //$NON-NLS-1$
						fProject.getProject().build(
								IncrementalProjectBuilder.FULL_BUILD,
								new SubProgressMonitor(monitor, 1));
						PHPeclipsePlugin.getWorkspace().build(
								IncrementalProjectBuilder.INCREMENTAL_BUILD,
								new SubProgressMonitor(monitor, 1));
					} else {
						monitor
								.setTaskName(PreferencesMessages
										.getString("OptionsConfigurationBlock.buildall.taskname")); //$NON-NLS-1$
						PHPeclipsePlugin.getWorkspace().build(
								IncrementalProjectBuilder.FULL_BUILD,
								new SubProgressMonitor(monitor, 2));
					}
				} catch (CoreException e) {
					return e.getStatus();
				} catch (OperationCanceledException e) {
					return Status.CANCEL_STATUS;
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}

			public boolean belongsTo(Object family) {
				return ResourcesPlugin.FAMILY_MANUAL_BUILD == family;
			}
		};

		buildJob.setRule(ResourcesPlugin.getWorkspace().getRuleFactory()
				.buildRule());
		buildJob.setUser(true);
		buildJob.schedule();
		return true;
	}

	public void performDefaults() {
		fWorkingValues = getDefaultOptions();
		updateControls();
		validateSettings(null, null);
	}

	protected void updateControls() {
		// update the UI
		for (int i = fCheckBoxes.size() - 1; i >= 0; i--) {
			updateCheckBox((Button) fCheckBoxes.get(i));
		}
		for (int i = fComboBoxes.size() - 1; i >= 0; i--) {
			updateCombo((Combo) fComboBoxes.get(i));
		}
		for (int i = fTextBoxes.size() - 1; i >= 0; i--) {
			updateText((Text) fTextBoxes.get(i));
		}
	}

	protected void updateCombo(Combo curr) {
		ControlData data = (ControlData) curr.getData();

		String currValue = (String) fWorkingValues.get(data.getKey());
		curr.select(data.getSelection(currValue));
	}

	protected void updateCheckBox(Button curr) {
		ControlData data = (ControlData) curr.getData();

		String currValue = (String) fWorkingValues.get(data.getKey());
		curr.setSelection(data.getSelection(currValue) == 0);
	}

	protected void updateText(Text curr) {
		String key = (String) curr.getData();

		String currValue = (String) fWorkingValues.get(key);
		if (currValue != null) {
			curr.setText(currValue);
		}
	}

	protected Button getCheckBox(String key) {
		for (int i = fCheckBoxes.size() - 1; i >= 0; i--) {
			Button curr = (Button) fCheckBoxes.get(i);
			ControlData data = (ControlData) curr.getData();
			if (key.equals(data.getKey())) {
				return curr;
			}
		}
		return null;
	}

	protected Combo getComboBox(String key) {
		for (int i = fComboBoxes.size() - 1; i >= 0; i--) {
			Combo curr = (Combo) fComboBoxes.get(i);
			ControlData data = (ControlData) curr.getData();
			if (key.equals(data.getKey())) {
				return curr;
			}
		}
		return null;
	}

	protected void setComboEnabled(String key, boolean enabled) {
		Combo combo = getComboBox(key);
		Label label = (Label) fLabels.get(combo);
		combo.setEnabled(enabled);
		label.setEnabled(enabled);
	}

}
