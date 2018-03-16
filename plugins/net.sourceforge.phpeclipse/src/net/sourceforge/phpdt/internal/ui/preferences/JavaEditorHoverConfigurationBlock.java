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

import java.util.HashMap;
import java.util.StringTokenizer;

import net.sourceforge.phpdt.internal.ui.dialogs.StatusInfo;
import net.sourceforge.phpdt.internal.ui.dialogs.StatusUtil;
import net.sourceforge.phpdt.internal.ui.text.java.hover.JavaEditorTextHoverDescriptor;
import net.sourceforge.phpdt.internal.ui.util.PixelConverter;
import net.sourceforge.phpdt.ui.PreferenceConstants;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

/**
 * Configures Java Editor hover preferences.
 * 
 * @since 2.1
 */
class JavaEditorHoverConfigurationBlock {

	private static final String DELIMITER = PreferencesMessages
			.getString("JavaEditorHoverConfigurationBlock.delimiter"); //$NON-NLS-1$

	// Data structure to hold the values which are edited by the user
	private static class HoverConfig {

		private String fModifierString;

		private boolean fIsEnabled;

		private int fStateMask;

		private HoverConfig(String modifier, int stateMask, boolean enabled) {
			fModifierString = modifier;
			fIsEnabled = enabled;
			fStateMask = stateMask;
		}
	}

	private IPreferenceStore fStore;

	private HoverConfig[] fHoverConfigs;

	private Text fModifierEditor;

	private Button fEnableField;

	private List fHoverList;

	private Text fDescription;

	private Button fShowHoverAffordanceCheckbox;

	private JavaEditorPreferencePage fMainPreferencePage;

	private StatusInfo fStatus;

	public JavaEditorHoverConfigurationBlock(
			JavaEditorPreferencePage mainPreferencePage, IPreferenceStore store) {
		Assert.isNotNull(mainPreferencePage);
		Assert.isNotNull(store);
		fMainPreferencePage = mainPreferencePage;
		fStore = store;
	}

	/**
	 * Creates page for hover preferences.
	 */
	public Control createControl(Composite parent) {

		PixelConverter pixelConverter = new PixelConverter(parent);

		Composite hoverComposite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		hoverComposite.setLayout(layout);
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL
				| GridData.VERTICAL_ALIGN_FILL);
		hoverComposite.setLayoutData(gd);

		Label label = new Label(hoverComposite, SWT.NONE);
		label
				.setText(PreferencesMessages
						.getString("JavaEditorHoverConfigurationBlock.hoverPreferences")); //$NON-NLS-1$
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment = GridData.BEGINNING;
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		gd = new GridData(GridData.GRAB_HORIZONTAL
				| GridData.VERTICAL_ALIGN_FILL);

		// Hover list
		fHoverList = new List(hoverComposite, SWT.SINGLE | SWT.V_SCROLL
				| SWT.BORDER);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING
				| GridData.FILL_HORIZONTAL);
		int listHeight = 10 * fHoverList.getItemHeight();
		gd.heightHint = listHeight;
		fHoverList.setLayoutData(gd);
		fHoverList.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				handleHoverListSelection();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		Composite stylesComposite = new Composite(hoverComposite, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		stylesComposite.setLayout(layout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = listHeight + (2 * fHoverList.getBorderWidth());
		stylesComposite.setLayoutData(gd);

		// Enabled checkbox
		fEnableField = new Button(stylesComposite, SWT.CHECK);
		fEnableField.setText(PreferencesMessages
				.getString("JavaEditorHoverConfigurationBlock.enabled")); //$NON-NLS-1$
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment = GridData.BEGINNING;
		gd.horizontalSpan = 2;
		fEnableField.setLayoutData(gd);
		fEnableField.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				int i = fHoverList.getSelectionIndex();
				boolean state = fEnableField.getSelection();
				fModifierEditor.setEnabled(state);
				fHoverConfigs[i].fIsEnabled = state;
				handleModifierModified();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Text field for modifier string
		label = new Label(stylesComposite, SWT.LEFT);
		label.setText(PreferencesMessages
				.getString("JavaEditorHoverConfigurationBlock.keyModifier")); //$NON-NLS-1$
		fModifierEditor = new Text(stylesComposite, SWT.BORDER);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		fModifierEditor.setLayoutData(gd);

		fModifierEditor.addKeyListener(new KeyListener() {
			private boolean isModifierCandidate;

			public void keyPressed(KeyEvent e) {
				isModifierCandidate = e.keyCode > 0 && e.character == 0
						&& e.stateMask == 0;
			}

			public void keyReleased(KeyEvent e) {
				if (isModifierCandidate && e.stateMask > 0
						&& e.stateMask == e.stateMask && e.character == 0) {// &&
																			// e.time
																			// -time
																			// <
																			// 1000)
																			// {
					String text = fModifierEditor.getText();
					Point selection = fModifierEditor.getSelection();
					int i = selection.x - 1;
					while (i > -1 && Character.isWhitespace(text.charAt(i))) {
						i--;
					}
					boolean needsPrefixDelimiter = i > -1
							&& !String.valueOf(text.charAt(i))
									.equals(DELIMITER);

					i = selection.y;
					while (i < text.length()
							&& Character.isWhitespace(text.charAt(i))) {
						i++;
					}
					boolean needsPostfixDelimiter = i < text.length()
							&& !String.valueOf(text.charAt(i))
									.equals(DELIMITER);

					String insertString;

					if (needsPrefixDelimiter && needsPostfixDelimiter)
						insertString = PreferencesMessages
								.getFormattedString(
										"JavaEditorHoverConfigurationBlock.insertDelimiterAndModifierAndDelimiter", new String[] { Action.findModifierString(e.stateMask) }); //$NON-NLS-1$
					else if (needsPrefixDelimiter)
						insertString = PreferencesMessages
								.getFormattedString(
										"JavaEditorHoverConfigurationBlock.insertDelimiterAndModifier", new String[] { Action.findModifierString(e.stateMask) }); //$NON-NLS-1$
					else if (needsPostfixDelimiter)
						insertString = PreferencesMessages
								.getFormattedString(
										"JavaEditorHoverConfigurationBlock.insertModifierAndDelimiter", new String[] { Action.findModifierString(e.stateMask) }); //$NON-NLS-1$
					else
						insertString = Action.findModifierString(e.stateMask);

					if (insertString != null)
						fModifierEditor.insert(insertString);
				}
			}
		});

		fModifierEditor.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				handleModifierModified();
			}
		});

		// Description
		Label descriptionLabel = new Label(stylesComposite, SWT.LEFT);
		descriptionLabel.setText(PreferencesMessages
				.getString("JavaEditorHoverConfigurationBlock.description")); //$NON-NLS-1$
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
		descriptionLabel.setLayoutData(gd);
		fDescription = new Text(stylesComposite, SWT.LEFT | SWT.WRAP
				| SWT.MULTI | SWT.READ_ONLY | SWT.BORDER);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		fDescription.setLayoutData(gd);

		// Vertical filler
		Label filler = new Label(hoverComposite, SWT.LEFT);
		gd = new GridData(GridData.BEGINNING | GridData.VERTICAL_ALIGN_FILL);
		gd.heightHint = pixelConverter.convertHeightInCharsToPixels(1) / 3;
		filler.setLayoutData(gd);

		// Affordance checkbox
		fShowHoverAffordanceCheckbox = new Button(hoverComposite, SWT.CHECK);
		fShowHoverAffordanceCheckbox.setText(PreferencesMessages
				.getString("JavaEditorHoverConfigurationBlock.showAffordance")); //$NON-NLS-1$
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent = 0;
		gd.horizontalSpan = 2;
		fShowHoverAffordanceCheckbox.setLayoutData(gd);

		initialize();

		Dialog.applyDialogFont(hoverComposite);
		return hoverComposite;
	}

	private JavaEditorTextHoverDescriptor[] getContributedHovers() {
		return PHPeclipsePlugin.getDefault()
				.getJavaEditorTextHoverDescriptors();
	}

	void initialize() {
		JavaEditorTextHoverDescriptor[] hoverDescs = getContributedHovers();
		fHoverConfigs = new HoverConfig[hoverDescs.length];
		for (int i = 0; i < hoverDescs.length; i++) {
			fHoverConfigs[i] = new HoverConfig(hoverDescs[i]
					.getModifierString(), hoverDescs[i].getStateMask(),
					hoverDescs[i].isEnabled());
			fHoverList.add(hoverDescs[i].getLabel());
		}
		initializeFields();
	}

	void initializeFields() {
		fHoverList.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (fHoverList != null && !fHoverList.isDisposed()) {
					fHoverList.select(0);
					handleHoverListSelection();
				}
			}
		});
		fShowHoverAffordanceCheckbox
				.setSelection(fStore
						.getBoolean(PreferenceConstants.EDITOR_SHOW_TEXT_HOVER_AFFORDANCE));
	}

	void performOk() {
		StringBuffer buf = new StringBuffer();
		StringBuffer maskBuf = new StringBuffer();
		for (int i = 0; i < fHoverConfigs.length; i++) {
			buf.append(getContributedHovers()[i].getId());
			buf.append(JavaEditorTextHoverDescriptor.VALUE_SEPARATOR);
			if (!fHoverConfigs[i].fIsEnabled)
				buf.append(JavaEditorTextHoverDescriptor.DISABLED_TAG);
			String modifier = fHoverConfigs[i].fModifierString;
			if (modifier == null || modifier.length() == 0)
				modifier = JavaEditorTextHoverDescriptor.NO_MODIFIER;
			buf.append(modifier);
			buf.append(JavaEditorTextHoverDescriptor.VALUE_SEPARATOR);

			maskBuf.append(getContributedHovers()[i].getId());
			maskBuf.append(JavaEditorTextHoverDescriptor.VALUE_SEPARATOR);
			maskBuf.append(fHoverConfigs[i].fStateMask);
			maskBuf.append(JavaEditorTextHoverDescriptor.VALUE_SEPARATOR);
		}
		fStore.setValue(PreferenceConstants.EDITOR_TEXT_HOVER_MODIFIERS, buf
				.toString());
		fStore.setValue(PreferenceConstants.EDITOR_TEXT_HOVER_MODIFIER_MASKS,
				maskBuf.toString());

		fStore.setValue(PreferenceConstants.EDITOR_SHOW_TEXT_HOVER_AFFORDANCE,
				fShowHoverAffordanceCheckbox.getSelection());

		PHPeclipsePlugin.getDefault().resetJavaEditorTextHoverDescriptors();
	}

	void performDefaults() {
		restoreFromPreferences();
		initializeFields();
	}

	private void restoreFromPreferences() {

		fShowHoverAffordanceCheckbox
				.setSelection(fStore
						.getBoolean(PreferenceConstants.EDITOR_SHOW_TEXT_HOVER_AFFORDANCE));

		String compiledTextHoverModifiers = fStore
				.getString(PreferenceConstants.EDITOR_TEXT_HOVER_MODIFIERS);

		StringTokenizer tokenizer = new StringTokenizer(
				compiledTextHoverModifiers,
				JavaEditorTextHoverDescriptor.VALUE_SEPARATOR);
		HashMap idToModifier = new HashMap(tokenizer.countTokens() / 2);

		while (tokenizer.hasMoreTokens()) {
			String id = tokenizer.nextToken();
			if (tokenizer.hasMoreTokens())
				idToModifier.put(id, tokenizer.nextToken());
		}

		String compiledTextHoverModifierMasks = PHPeclipsePlugin.getDefault()
				.getPreferenceStore().getString(
						PreferenceConstants.EDITOR_TEXT_HOVER_MODIFIER_MASKS);

		tokenizer = new StringTokenizer(compiledTextHoverModifierMasks,
				JavaEditorTextHoverDescriptor.VALUE_SEPARATOR);
		HashMap idToModifierMask = new HashMap(tokenizer.countTokens() / 2);

		while (tokenizer.hasMoreTokens()) {
			String id = tokenizer.nextToken();
			if (tokenizer.hasMoreTokens())
				idToModifierMask.put(id, tokenizer.nextToken());
		}

		for (int i = 0; i < fHoverConfigs.length; i++) {
			String modifierString = (String) idToModifier
					.get(getContributedHovers()[i].getId());
			boolean enabled = true;
			if (modifierString == null)
				modifierString = JavaEditorTextHoverDescriptor.DISABLED_TAG;

			if (modifierString
					.startsWith(JavaEditorTextHoverDescriptor.DISABLED_TAG)) {
				enabled = false;
				modifierString = modifierString.substring(1);
			}

			if (modifierString
					.equals(JavaEditorTextHoverDescriptor.NO_MODIFIER))
				modifierString = ""; //$NON-NLS-1$

			fHoverConfigs[i].fModifierString = modifierString;
			fHoverConfigs[i].fIsEnabled = enabled;
			fHoverConfigs[i].fStateMask = JavaEditorTextHoverDescriptor
					.computeStateMask(modifierString);

			if (fHoverConfigs[i].fStateMask == -1) {
				try {
					fHoverConfigs[i].fStateMask = Integer
							.parseInt((String) idToModifierMask
									.get(getContributedHovers()[i].getId()));
				} catch (NumberFormatException ex) {
					fHoverConfigs[i].fStateMask = -1;
				}
			}
		}
	}

	private void handleModifierModified() {
		int i = fHoverList.getSelectionIndex();
		String modifiers = fModifierEditor.getText();
		fHoverConfigs[i].fModifierString = modifiers;
		fHoverConfigs[i].fStateMask = JavaEditorTextHoverDescriptor
				.computeStateMask(modifiers);
		if (fHoverConfigs[i].fIsEnabled && fHoverConfigs[i].fStateMask == -1)
			fStatus = new StatusInfo(
					IStatus.ERROR,
					PreferencesMessages
							.getFormattedString(
									"JavaEditorHoverConfigurationBlock.modifierIsNotValid", fHoverConfigs[i].fModifierString)); //$NON-NLS-1$
		else
			fStatus = new StatusInfo();
		updateStatus();
	}

	private void handleHoverListSelection() {
		int i = fHoverList.getSelectionIndex();
		boolean enabled = fHoverConfigs[i].fIsEnabled;
		fEnableField.setSelection(enabled);
		fModifierEditor.setEnabled(enabled);
		fModifierEditor.setText(fHoverConfigs[i].fModifierString);
		String description = getContributedHovers()[i].getDescription();
		if (description == null)
			description = ""; //$NON-NLS-1$
		fDescription.setText(description);
	}

	IStatus getStatus() {
		if (fStatus == null)
			fStatus = new StatusInfo();
		return fStatus;
	}

	private void updateStatus() {
		int i = 0;
		HashMap stateMasks = new HashMap(fHoverConfigs.length);
		while (fStatus.isOK() && i < fHoverConfigs.length) {
			if (fHoverConfigs[i].fIsEnabled) {
				String label = getContributedHovers()[i].getLabel();
				Integer stateMask = new Integer(fHoverConfigs[i].fStateMask);
				if (fHoverConfigs[i].fStateMask == -1)
					fStatus = new StatusInfo(
							IStatus.ERROR,
							PreferencesMessages
									.getFormattedString(
											"JavaEditorHoverConfigurationBlock.modifierIsNotValidForHover", new String[] { fHoverConfigs[i].fModifierString, label })); //$NON-NLS-1$
				else if (stateMasks.containsKey(stateMask))
					fStatus = new StatusInfo(
							IStatus.ERROR,
							PreferencesMessages
									.getFormattedString(
											"JavaEditorHoverConfigurationBlock.duplicateModifier", new String[] { label, (String) stateMasks.get(stateMask) })); //$NON-NLS-1$
				else
					stateMasks.put(stateMask, label);
			}
			i++;
		}

		if (fStatus.isOK())
			fMainPreferencePage.updateStatus(fStatus);
		else {
			fMainPreferencePage.setValid(false);
			StatusUtil.applyToStatusLine(fMainPreferencePage, fStatus);
		}
	}
}
