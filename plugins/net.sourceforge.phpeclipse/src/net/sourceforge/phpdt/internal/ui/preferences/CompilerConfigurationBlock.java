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
import java.util.Map;

import net.sourceforge.phpdt.core.IJavaProject;
import net.sourceforge.phpdt.core.JavaCore;
import net.sourceforge.phpdt.internal.ui.dialogs.StatusInfo;
import net.sourceforge.phpdt.internal.ui.dialogs.StatusUtil;
import net.sourceforge.phpdt.internal.ui.util.PixelConverter;
import net.sourceforge.phpdt.internal.ui.util.TabFolderLayout;
import net.sourceforge.phpdt.internal.ui.wizards.IStatusChangeListener;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

/**
 */
public class CompilerConfigurationBlock extends OptionsConfigurationBlock {

	// Preference store keys, see JavaCore.getOptions
	private static final String PREF_PB_PHP_VAR_DEPRECATED = JavaCore.COMPILER_PB_PHP_VAR_DEPRECATED;

	private static final String PREF_PB_PHP_KEYWORD = JavaCore.COMPILER_PB_PHP_KEYWORD;

	private static final String PREF_PB_PHP_UPPERCASE_IDENTIFIER = JavaCore.COMPILER_PB_PHP_UPPERCASE_IDENTIFIER;

	private static final String PREF_PB_PHP_FILE_NOT_EXIST = JavaCore.COMPILER_PB_PHP_FILE_NOT_EXIST;

	private static final String PREF_PB_UNREACHABLE_CODE = JavaCore.COMPILER_PB_UNREACHABLE_CODE;

	private static final String PREF_PB_UNINITIALIZED_LOCAL_VARIABLE = JavaCore.COMPILER_PB_UNINITIALIZED_LOCAL_VARIABLE;

	// private static final String PREF_LOCAL_VARIABLE_ATTR=
	// JavaCore.COMPILER_LOCAL_VARIABLE_ATTR;
	// private static final String PREF_LINE_NUMBER_ATTR=
	// JavaCore.COMPILER_LINE_NUMBER_ATTR;
	// private static final String PREF_SOURCE_FILE_ATTR=
	// JavaCore.COMPILER_SOURCE_FILE_ATTR;
	// private static final String PREF_CODEGEN_UNUSED_LOCAL=
	// JavaCore.COMPILER_CODEGEN_UNUSED_LOCAL;
	// private static final String PREF_CODEGEN_TARGET_PLATFORM=
	// JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM;
	// private static final String PREF_PB_UNREACHABLE_CODE=
	// JavaCore.COMPILER_PB_UNREACHABLE_CODE;
	// private static final String PREF_PB_INVALID_IMPORT=
	// JavaCore.COMPILER_PB_INVALID_IMPORT;
	// private static final String PREF_PB_OVERRIDING_PACKAGE_DEFAULT_METHOD=
	// JavaCore.COMPILER_PB_OVERRIDING_PACKAGE_DEFAULT_METHOD;
	// private static final String PREF_PB_METHOD_WITH_CONSTRUCTOR_NAME=
	// JavaCore.COMPILER_PB_METHOD_WITH_CONSTRUCTOR_NAME;
	// private static final String PREF_PB_DEPRECATION=
	// JavaCore.COMPILER_PB_DEPRECATION;
	// private static final String PREF_PB_DEPRECATION_WHEN_OVERRIDING=
	// JavaCore.COMPILER_PB_DEPRECATION_WHEN_OVERRIDING_DEPRECATED_METHOD;

	// private static final String PREF_PB_HIDDEN_CATCH_BLOCK=
	// JavaCore.COMPILER_PB_HIDDEN_CATCH_BLOCK;
	// private static final String PREF_PB_UNUSED_LOCAL=
	// JavaCore.COMPILER_PB_UNUSED_LOCAL;
	// private static final String PREF_PB_UNUSED_PARAMETER=
	// JavaCore.COMPILER_PB_UNUSED_PARAMETER;
	// private static final String PREF_PB_SIGNAL_PARAMETER_IN_OVERRIDING=
	// JavaCore.COMPILER_PB_UNUSED_PARAMETER_WHEN_OVERRIDING_CONCRETE;
	// private static final String PREF_PB_SIGNAL_PARAMETER_IN_ABSTRACT=
	// JavaCore.COMPILER_PB_UNUSED_PARAMETER_WHEN_IMPLEMENTING_ABSTRACT;
	// private static final String PREF_PB_SYNTHETIC_ACCESS_EMULATION=
	// JavaCore.COMPILER_PB_SYNTHETIC_ACCESS_EMULATION;
	// private static final String PREF_PB_NON_EXTERNALIZED_STRINGS=
	// JavaCore.COMPILER_PB_NON_NLS_STRING_LITERAL;
	// private static final String PREF_PB_ASSERT_AS_IDENTIFIER=
	// JavaCore.COMPILER_PB_ASSERT_IDENTIFIER;
	private static final String PREF_PB_MAX_PER_UNIT = JavaCore.COMPILER_PB_MAX_PER_UNIT;

	// private static final String PREF_PB_UNUSED_IMPORT=
	// JavaCore.COMPILER_PB_UNUSED_IMPORT;
	// private static final String PREF_PB_UNUSED_PRIVATE=
	// JavaCore.COMPILER_PB_UNUSED_PRIVATE_MEMBER;
	// private static final String PREF_PB_STATIC_ACCESS_RECEIVER=
	// JavaCore.COMPILER_PB_STATIC_ACCESS_RECEIVER;
	// private static final String PREF_PB_NO_EFFECT_ASSIGNMENT=
	// JavaCore.COMPILER_PB_NO_EFFECT_ASSIGNMENT;
	// private static final String PREF_PB_CHAR_ARRAY_IN_CONCAT=
	// JavaCore.COMPILER_PB_CHAR_ARRAY_IN_STRING_CONCATENATION;
	// private static final String
	// PREF_PB_POSSIBLE_ACCIDENTAL_BOOLEAN_ASSIGNMENT=
	// JavaCore.COMPILER_PB_POSSIBLE_ACCIDENTAL_BOOLEAN_ASSIGNMENT;
	// private static final String PREF_PB_LOCAL_VARIABLE_HIDING=
	// JavaCore.COMPILER_PB_LOCAL_VARIABLE_HIDING;
	// private static final String PREF_PB_FIELD_HIDING=
	// JavaCore.COMPILER_PB_FIELD_HIDING;
	// private static final String PREF_PB_SPECIAL_PARAMETER_HIDING_FIELD=
	// JavaCore.COMPILER_PB_SPECIAL_PARAMETER_HIDING_FIELD;
	// private static final String PREF_PB_INDIRECT_STATIC_ACCESS=
	// JavaCore.COMPILER_PB_INDIRECT_STATIC_ACCESS;
	// private static final String PREF_PB_SUPERFLUOUS_SEMICOLON=
	// JavaCore.COMPILER_PB_SUPERFLUOUS_SEMICOLON;
	// private static final String PREF_PB_UNNECESSARY_TYPE_CHECK=
	// JavaCore.COMPILER_PB_UNNECESSARY_TYPE_CHECK;

	// private static final String PREF_PB_INVALID_JAVADOC=
	// JavaCore.COMPILER_PB_INVALID_JAVADOC;
	// private static final String PREF_PB_INVALID_JAVADOC_TAGS=
	// JavaCore.COMPILER_PB_INVALID_JAVADOC_TAGS;
	// private static final String PREF_PB_INVALID_JAVADOC_TAGS_VISIBILITY=
	// JavaCore.COMPILER_PB_INVALID_JAVADOC_TAGS_VISIBILITY;
	//
	// private static final String PREF_PB_MISSING_JAVADOC_TAGS=
	// JavaCore.COMPILER_PB_MISSING_JAVADOC_TAGS;
	// private static final String PREF_PB_MISSING_JAVADOC_TAGS_VISIBILITY=
	// JavaCore.COMPILER_PB_MISSING_JAVADOC_TAGS_VISIBILITY;
	// private static final String PREF_PB_MISSING_JAVADOC_TAGS_OVERRIDING=
	// JavaCore.COMPILER_PB_MISSING_JAVADOC_TAGS_OVERRIDING;
	//
	// private static final String PREF_PB_MISSING_JAVADOC_COMMENTS=
	// JavaCore.COMPILER_PB_MISSING_JAVADOC_COMMENTS;
	// private static final String PREF_PB_MISSING_JAVADOC_COMMENTS_VISIBILITY=
	// JavaCore.COMPILER_PB_MISSING_JAVADOC_COMMENTS_VISIBILITY;
	// private static final String PREF_PB_MISSING_JAVADOC_COMMENTS_OVERRIDING=
	// JavaCore.COMPILER_PB_MISSING_JAVADOC_COMMENTS_OVERRIDING;
	//	
	// private static final String PREF_SOURCE_COMPATIBILITY=
	// JavaCore.COMPILER_SOURCE;
	// private static final String PREF_COMPLIANCE=
	// JavaCore.COMPILER_COMPLIANCE;
	//
	// private static final String PREF_RESOURCE_FILTER=
	// JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER;
	// private static final String PREF_BUILD_INVALID_CLASSPATH=
	// JavaCore.CORE_JAVA_BUILD_INVALID_CLASSPATH;
	// private static final String PREF_BUILD_CLEAN_OUTPUT_FOLDER=
	// JavaCore.CORE_JAVA_BUILD_CLEAN_OUTPUT_FOLDER;
	// private static final String PREF_ENABLE_EXCLUSION_PATTERNS=
	// JavaCore.CORE_ENABLE_CLASSPATH_EXCLUSION_PATTERNS;
	// private static final String PREF_ENABLE_MULTIPLE_OUTPUT_LOCATIONS=
	// JavaCore.CORE_ENABLE_CLASSPATH_MULTIPLE_OUTPUT_LOCATIONS;
	//
	// private static final String PREF_PB_INCOMPLETE_BUILDPATH=
	// JavaCore.CORE_INCOMPLETE_CLASSPATH;
	// private static final String PREF_PB_CIRCULAR_BUILDPATH=
	// JavaCore.CORE_CIRCULAR_CLASSPATH;
	// // private static final String PREF_PB_INCOMPATIBLE_JDK_LEVEL=
	// JavaCore.CORE_INCOMPATIBLE_JDK_LEVEL;
	// private static final String PREF_PB_DEPRECATION_IN_DEPRECATED_CODE=
	// JavaCore.COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE;
	// private static final String PREF_PB_DUPLICATE_RESOURCE=
	// JavaCore.CORE_JAVA_BUILD_DUPLICATE_RESOURCE;
	// private static final String PREF_PB_INCOMPATIBLE_INTERFACE_METHOD=
	// JavaCore.COMPILER_PB_INCOMPATIBLE_NON_INHERITED_INTERFACE_METHOD;

	// private static final String PREF_PB_UNDOCUMENTED_EMPTY_BLOCK=
	// JavaCore.COMPILER_PB_UNDOCUMENTED_EMPTY_BLOCK;
	// private static final String PREF_PB_FINALLY_BLOCK_NOT_COMPLETING=
	// JavaCore.COMPILER_PB_FINALLY_BLOCK_NOT_COMPLETING;
	// private static final String PREF_PB_UNUSED_DECLARED_THROWN_EXCEPTION=
	// JavaCore.COMPILER_PB_UNUSED_DECLARED_THROWN_EXCEPTION;
	// private static final String
	// PREF_PB_UNUSED_DECLARED_THROWN_EXCEPTION_WHEN_OVERRIDING=
	// JavaCore.COMPILER_PB_UNUSED_DECLARED_THROWN_EXCEPTION_WHEN_OVERRIDING;
	// private static final String PREF_PB_UNQUALIFIED_FIELD_ACCESS=
	// JavaCore.COMPILER_PB_UNQUALIFIED_FIELD_ACCESS;

	// private static final String INTR_DEFAULT_COMPLIANCE=
	// "internal.default.compliance"; //$NON-NLS-1$

	// values
	private static final String GENERATE = JavaCore.GENERATE;

	private static final String DO_NOT_GENERATE = JavaCore.DO_NOT_GENERATE;

	private static final String PRESERVE = JavaCore.PRESERVE;

	private static final String OPTIMIZE_OUT = JavaCore.OPTIMIZE_OUT;

	private static final String VERSION_1_1 = JavaCore.VERSION_1_1;

	private static final String VERSION_1_2 = JavaCore.VERSION_1_2;

	private static final String VERSION_1_3 = JavaCore.VERSION_1_3;

	private static final String VERSION_1_4 = JavaCore.VERSION_1_4;

	private static final String ERROR = JavaCore.ERROR;

	private static final String WARNING = JavaCore.WARNING;

	private static final String IGNORE = JavaCore.IGNORE;

	private static final String ABORT = JavaCore.ABORT;

	private static final String CLEAN = JavaCore.CLEAN;

	private static final String ENABLED = JavaCore.ENABLED;

	private static final String DISABLED = JavaCore.DISABLED;

	// private static final String PUBLIC= JavaCore.PUBLIC;
	// private static final String PROTECTED= JavaCore.PROTECTED;
	// private static final String DEFAULT= JavaCore.DEFAULT;
	// private static final String PRIVATE= JavaCore.PRIVATE;

	private static final String DEFAULT_CONF = "default"; //$NON-NLS-1$

	private static final String USER_CONF = "user"; //$NON-NLS-1$

	private ArrayList fComplianceControls;

	private PixelConverter fPixelConverter;

	private IStatus fMaxNumberProblemsStatus;

	// private IStatus fComplianceStatus, fMaxNumberProblemsStatus,
	// fResourceFilterStatus;

	public CompilerConfigurationBlock(IStatusChangeListener context,
			IJavaProject project) {
		super(context, project, getKeys());

		fComplianceControls = new ArrayList();

		// fComplianceStatus= new StatusInfo();
		fMaxNumberProblemsStatus = new StatusInfo();
		// fResourceFilterStatus= new StatusInfo();

		// compatibilty code for the merge of the two option
		// PB_SIGNAL_PARAMETER:
		// if
		// (ENABLED.equals(fWorkingValues.get(PREF_PB_SIGNAL_PARAMETER_IN_ABSTRACT)))
		// {
		// fWorkingValues.put(PREF_PB_SIGNAL_PARAMETER_IN_OVERRIDING, ENABLED);
		// }

	}

	private final static String[] KEYS = new String[] {
			PREF_PB_PHP_FILE_NOT_EXIST, PREF_PB_PHP_VAR_DEPRECATED,
			PREF_PB_PHP_KEYWORD, PREF_PB_PHP_UPPERCASE_IDENTIFIER,
			PREF_PB_UNREACHABLE_CODE, PREF_PB_UNINITIALIZED_LOCAL_VARIABLE,
			// PREF_LOCAL_VARIABLE_ATTR,
			// PREF_LINE_NUMBER_ATTR, PREF_SOURCE_FILE_ATTR,
			// PREF_CODEGEN_UNUSED_LOCAL,
			// PREF_CODEGEN_TARGET_PLATFORM,
			// PREF_PB_OVERRIDING_PACKAGE_DEFAULT_METHOD,
			// PREF_PB_METHOD_WITH_CONSTRUCTOR_NAME,
			// PREF_PB_DEPRECATION,
			// PREF_PB_HIDDEN_CATCH_BLOCK, PREF_PB_UNUSED_LOCAL,
			// PREF_PB_UNUSED_PARAMETER,
			// PREF_PB_SYNTHETIC_ACCESS_EMULATION,
			// PREF_PB_NON_EXTERNALIZED_STRINGS,
			// PREF_PB_ASSERT_AS_IDENTIFIER,
			// PREF_PB_UNUSED_IMPORT,
			PREF_PB_MAX_PER_UNIT,
	// PREF_SOURCE_COMPATIBILITY,
	// PREF_COMPLIANCE,
	// PREF_RESOURCE_FILTER, PREF_BUILD_INVALID_CLASSPATH,
	// PREF_PB_STATIC_ACCESS_RECEIVER, PREF_PB_INCOMPLETE_BUILDPATH,
	// PREF_PB_CIRCULAR_BUILDPATH, PREF_PB_DEPRECATION_IN_DEPRECATED_CODE,
	// PREF_BUILD_CLEAN_OUTPUT_FOLDER,
	// PREF_PB_DUPLICATE_RESOURCE, PREF_PB_NO_EFFECT_ASSIGNMENT,
	// PREF_PB_INCOMPATIBLE_INTERFACE_METHOD,
	// PREF_PB_UNUSED_PRIVATE, PREF_PB_CHAR_ARRAY_IN_CONCAT,
	// PREF_ENABLE_EXCLUSION_PATTERNS, PREF_ENABLE_MULTIPLE_OUTPUT_LOCATIONS,
	// PREF_PB_POSSIBLE_ACCIDENTAL_BOOLEAN_ASSIGNMENT,
	// PREF_PB_LOCAL_VARIABLE_HIDING,
	// PREF_PB_FIELD_HIDING,
	// PREF_PB_SPECIAL_PARAMETER_HIDING_FIELD,
	// PREF_PB_INCOMPATIBLE_JDK_LEVEL,
	// PREF_PB_INDIRECT_STATIC_ACCESS,
	// PREF_PB_SUPERFLUOUS_SEMICOLON,
	// PREF_PB_SIGNAL_PARAMETER_IN_OVERRIDING,
	// PREF_PB_SIGNAL_PARAMETER_IN_ABSTRACT,
	// PREF_PB_UNNECESSARY_TYPE_CHECK,
	// PREF_PB_UNUSED_DECLARED_THROWN_EXCEPTION,
	// PREF_PB_UNQUALIFIED_FIELD_ACCESS,
	// PREF_PB_UNDOCUMENTED_EMPTY_BLOCK,
	// PREF_PB_FINALLY_BLOCK_NOT_COMPLETING,
	// PREF_PB_DEPRECATION_WHEN_OVERRIDING,
	// PREF_PB_UNUSED_DECLARED_THROWN_EXCEPTION_WHEN_OVERRIDING,

	// PREF_PB_INVALID_JAVADOC,
	// PREF_PB_INVALID_JAVADOC_TAGS_VISIBILITY,
	// PREF_PB_INVALID_JAVADOC_TAGS_VISIBILITY,
	// PREF_PB_MISSING_JAVADOC_TAGS,
	// PREF_PB_MISSING_JAVADOC_TAGS_VISIBILITY,
	// PREF_PB_MISSING_JAVADOC_TAGS_OVERRIDING,
	// PREF_PB_MISSING_JAVADOC_COMMENTS,
	// PREF_PB_MISSING_JAVADOC_COMMENTS_VISIBILITY,
	// PREF_PB_MISSING_JAVADOC_COMMENTS_OVERRIDING
	};

	private static String[] getKeys() {
		return KEYS;
	}

	protected final Map getOptions(boolean inheritJavaCoreOptions) {
		Map map = super.getOptions(inheritJavaCoreOptions);
		// map.put(INTR_DEFAULT_COMPLIANCE, getCurrentCompliance(map));
		return map;
	}

	protected final Map getDefaultOptions() {
		Map map = super.getDefaultOptions();
		// map.put(INTR_DEFAULT_COMPLIANCE, getCurrentCompliance(map));
		return map;
	}

	/*
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		fPixelConverter = new PixelConverter(parent);
		setShell(parent.getShell());

		TabFolder folder = new TabFolder(parent, SWT.NONE);
		folder.setLayout(new TabFolderLayout());
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite commonComposite = createStyleTabContent(folder);
		// Composite unusedComposite= createUnusedCodeTabContent(folder);
		Composite advancedComposite = createAdvancedTabContent(folder);
		// Composite javadocComposite= createJavadocTabContent(folder);
		// Composite complianceComposite= createComplianceTabContent(folder);
		// Composite othersComposite= createBuildPathTabContent(folder);

		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText(PreferencesMessages
				.getString("CompilerConfigurationBlock.common.tabtitle")); //$NON-NLS-1$
		item.setControl(commonComposite);

		item = new TabItem(folder, SWT.NONE);
		item.setText(PreferencesMessages
				.getString("CompilerConfigurationBlock.advanced.tabtitle")); //$NON-NLS-1$
		item.setControl(advancedComposite);

		// item= new TabItem(folder, SWT.NONE);
		// item.setText(PreferencesMessages.getString("CompilerConfigurationBlock.unused.tabtitle"));
		// //$NON-NLS-1$
		// item.setControl(unusedComposite);

		// item= new TabItem(folder, SWT.NONE);
		// item.setText(PreferencesMessages.getString("CompilerConfigurationBlock.javadoc.tabtitle"));
		// //$NON-NLS-1$
		// item.setControl(javadocComposite);

		// item= new TabItem(folder, SWT.NONE);
		// item.setText(PreferencesMessages.getString("CompilerConfigurationBlock.compliance.tabtitle"));
		// //$NON-NLS-1$
		// item.setControl(complianceComposite);

		// item= new TabItem(folder, SWT.NONE);
		// item.setText(PreferencesMessages.getString("CompilerConfigurationBlock.others.tabtitle"));
		// //$NON-NLS-1$
		// item.setControl(othersComposite);

		validateSettings(null, null);

		return folder;
	}

	private Composite createStyleTabContent(Composite folder) {
		String[] errorWarningIgnore = new String[] { ERROR, WARNING, IGNORE };

		String[] errorWarningIgnoreLabels = new String[] {
				PreferencesMessages
						.getString("CompilerConfigurationBlock.error"), //$NON-NLS-1$
				PreferencesMessages
						.getString("CompilerConfigurationBlock.warning"), //$NON-NLS-1$
				PreferencesMessages
						.getString("CompilerConfigurationBlock.ignore") //$NON-NLS-1$
		};

		int nColumns = 3;

		GridLayout layout = new GridLayout();
		layout.numColumns = nColumns;

		Composite composite = new Composite(folder, SWT.NULL);
		composite.setLayout(layout);

		Label description = new Label(composite, SWT.WRAP);
		description.setText(PreferencesMessages
				.getString("CompilerConfigurationBlock.common.description")); //$NON-NLS-1$
		GridData gd = new GridData();
		gd.horizontalSpan = nColumns;
		gd.widthHint = fPixelConverter.convertWidthInCharsToPixels(50);
		description.setLayoutData(gd);

		String label = PreferencesMessages
				.getString("CompilerConfigurationBlock.pb_file_not_exist.label"); //$NON-NLS-1$
		addComboBox(composite, label, PREF_PB_PHP_FILE_NOT_EXIST,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = PreferencesMessages
				.getString("CompilerConfigurationBlock.pb_var_deprecated.label"); //$NON-NLS-1$
		addComboBox(composite, label, PREF_PB_PHP_VAR_DEPRECATED,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = PreferencesMessages
				.getString("CompilerConfigurationBlock.pb_keyword.label"); //$NON-NLS-1$
		addComboBox(composite, label, PREF_PB_PHP_KEYWORD, errorWarningIgnore,
				errorWarningIgnoreLabels, 0);

		label = PreferencesMessages
				.getString("CompilerConfigurationBlock.pb_uppercase_identifier.label"); //$NON-NLS-1$
		addComboBox(composite, label, PREF_PB_PHP_UPPERCASE_IDENTIFIER,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = PreferencesMessages
				.getString("CompilerConfigurationBlock.pb_unreachable_code.label"); //$NON-NLS-1$
		addComboBox(composite, label, PREF_PB_UNREACHABLE_CODE,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = PreferencesMessages
				.getString("CompilerConfigurationBlock.pb_unitialized_local_variable.label"); //$NON-NLS-1$
		addComboBox(composite, label, PREF_PB_UNINITIALIZED_LOCAL_VARIABLE,
				errorWarningIgnore, errorWarningIgnoreLabels, 0);

		label = PreferencesMessages
				.getString("CompilerConfigurationBlock.pb_overriding_pkg_dflt.label"); //$NON-NLS-1$
		// addComboBox(composite, label,
		// PREF_PB_OVERRIDING_PACKAGE_DEFAULT_METHOD, errorWarningIgnore,
		// errorWarningIgnoreLabels, 0);

		// label=
		// PreferencesMessages.getString("CompilerConfigurationBlock.pb_method_naming.label");
		// //$NON-NLS-1$
		// addComboBox(composite, label, PREF_PB_METHOD_WITH_CONSTRUCTOR_NAME,
		// errorWarningIgnore, errorWarningIgnoreLabels, 0);
		//
		// label=
		// PreferencesMessages.getString("CompilerConfigurationBlock.pb_hidden_catchblock.label");
		// //$NON-NLS-1$
		// addComboBox(composite, label, PREF_PB_HIDDEN_CATCH_BLOCK,
		// errorWarningIgnore, errorWarningIgnoreLabels, 0);
		//		
		// label=
		// PreferencesMessages.getString("CompilerConfigurationBlock.pb_static_access_receiver.label");
		// //$NON-NLS-1$
		// addComboBox(composite, label, PREF_PB_STATIC_ACCESS_RECEIVER,
		// errorWarningIgnore, errorWarningIgnoreLabels, 0);
		//		
		// label=
		// PreferencesMessages.getString("CompilerConfigurationBlock.pb_no_effect_assignment.label");
		// //$NON-NLS-1$
		// addComboBox(composite, label, PREF_PB_NO_EFFECT_ASSIGNMENT,
		// errorWarningIgnore, errorWarningIgnoreLabels, 0);

		// label=
		// PreferencesMessages.getString("CompilerConfigurationBlock.pb_indirect_access_to_static.label");
		// //$NON-NLS-1$
		// addComboBox(composite, label, PREF_PB_INDIRECT_STATIC_ACCESS,
		// errorWarningIgnore, errorWarningIgnoreLabels, 0);
		//
		// label=
		// PreferencesMessages.getString("CompilerConfigurationBlock.pb_accidential_assignement.label");
		// //$NON-NLS-1$
		// addComboBox(composite, label,
		// PREF_PB_POSSIBLE_ACCIDENTAL_BOOLEAN_ASSIGNMENT, errorWarningIgnore,
		// errorWarningIgnoreLabels, 0);
		//
		// label=
		// PreferencesMessages.getString("CompilerConfigurationBlock.pb_finally_block_not_completing.label");
		// //$NON-NLS-1$
		// addComboBox(composite, label, PREF_PB_FINALLY_BLOCK_NOT_COMPLETING,
		// errorWarningIgnore, errorWarningIgnoreLabels, 0);
		//
		// label=
		// PreferencesMessages.getString("CompilerConfigurationBlock.pb_undocumented_empty_block.label");
		// //$NON-NLS-1$
		// addComboBox(composite, label, PREF_PB_UNDOCUMENTED_EMPTY_BLOCK,
		// errorWarningIgnore, errorWarningIgnoreLabels, 0);

		return composite;
	}

	private Composite createAdvancedTabContent(TabFolder folder) {
		String[] errorWarningIgnore = new String[] { ERROR, WARNING, IGNORE };

		String[] errorWarningIgnoreLabels = new String[] {
				PreferencesMessages
						.getString("CompilerConfigurationBlock.error"), //$NON-NLS-1$
				PreferencesMessages
						.getString("CompilerConfigurationBlock.warning"), //$NON-NLS-1$
				PreferencesMessages
						.getString("CompilerConfigurationBlock.ignore") //$NON-NLS-1$
		};

		String[] enabledDisabled = new String[] { ENABLED, DISABLED };

		int nColumns = 3;

		GridLayout layout = new GridLayout();
		layout.numColumns = nColumns;

		Composite composite = new Composite(folder, SWT.NULL);
		composite.setLayout(layout);

		Label description = new Label(composite, SWT.WRAP);
		description.setText(PreferencesMessages
				.getString("CompilerConfigurationBlock.advanced.description")); //$NON-NLS-1$
		GridData gd = new GridData();
		gd.horizontalSpan = nColumns;
		gd.widthHint = fPixelConverter.convertWidthInCharsToPixels(50);
		description.setLayoutData(gd);

		// String label=
		// PreferencesMessages.getString("CompilerConfigurationBlock.pb_synth_access_emul.label");
		// //$NON-NLS-1$
		// addComboBox(composite, label, PREF_PB_SYNTHETIC_ACCESS_EMULATION,
		// errorWarningIgnore, errorWarningIgnoreLabels, 0);

		// label=
		// PreferencesMessages.getString("CompilerConfigurationBlock.pb_local_variable_hiding.label");
		// //$NON-NLS-1$
		// addComboBox(composite, label, PREF_PB_LOCAL_VARIABLE_HIDING,
		// errorWarningIgnore, errorWarningIgnoreLabels, 0);

		// int indent= fPixelConverter.convertWidthInCharsToPixels(2);
		// label=
		// PreferencesMessages.getString("CompilerConfigurationBlock.pb_special_param_hiding.label");
		// //$NON-NLS-1$
		// addCheckBox(composite, label, PREF_PB_SPECIAL_PARAMETER_HIDING_FIELD,
		// enabledDisabled, indent);

		// label=
		// PreferencesMessages.getString("CompilerConfigurationBlock.pb_field_hiding.label");
		// //$NON-NLS-1$
		// addComboBox(composite, label, PREF_PB_FIELD_HIDING,
		// errorWarningIgnore, errorWarningIgnoreLabels, 0);

		// label=
		// PreferencesMessages.getString("CompilerConfigurationBlock.pb_non_externalized_strings.label");
		// //$NON-NLS-1$
		// addComboBox(composite, label, PREF_PB_NON_EXTERNALIZED_STRINGS,
		// errorWarningIgnore, errorWarningIgnoreLabels, 0);
		//
		// label=
		// PreferencesMessages.getString("CompilerConfigurationBlock.pb_incompatible_interface_method.label");
		// //$NON-NLS-1$
		// addComboBox(composite, label, PREF_PB_INCOMPATIBLE_INTERFACE_METHOD,
		// errorWarningIgnore, errorWarningIgnoreLabels, 0);
		//
		// label=
		// PreferencesMessages.getString("CompilerConfigurationBlock.pb_char_array_in_concat.label");
		// //$NON-NLS-1$
		// addComboBox(composite, label, PREF_PB_CHAR_ARRAY_IN_CONCAT,
		// errorWarningIgnore, errorWarningIgnoreLabels, 0);

		// label=
		// PreferencesMessages.getString("CompilerConfigurationBlock.pb_unqualified_field_access.label");
		// //$NON-NLS-1$
		// addComboBox(composite, label, PREF_PB_UNQUALIFIED_FIELD_ACCESS,
		// errorWarningIgnore, errorWarningIgnoreLabels, 0);

		gd = new GridData();
		gd.widthHint = fPixelConverter.convertWidthInCharsToPixels(6);

		String label = PreferencesMessages
				.getString("CompilerConfigurationBlock.pb_max_per_unit.label"); //$NON-NLS-1$
		Text text = addTextField(composite, label, PREF_PB_MAX_PER_UNIT, 0, 0);
		text.setTextLimit(6);
		text.setLayoutData(gd);

		return composite;
	}

	/*
	 * (non-javadoc) Update fields and validate. @param changedKey Key that
	 * changed, or null, if all changed.
	 */
	protected void validateSettings(String changedKey, String newValue) {

		if (changedKey != null) {
			// if (INTR_DEFAULT_COMPLIANCE.equals(changedKey)) {
			// updateComplianceEnableState();
			// if (DEFAULT_CONF.equals(newValue)) {
			// updateComplianceDefaultSettings();
			// }
			// fComplianceStatus= validateCompliance();
			// } else if (PREF_COMPLIANCE.equals(changedKey)) {
			// if (checkValue(INTR_DEFAULT_COMPLIANCE, DEFAULT_CONF)) {
			// updateComplianceDefaultSettings();
			// }
			// fComplianceStatus= validateCompliance();
			// } else if (PREF_SOURCE_COMPATIBILITY.equals(changedKey) ||
			// PREF_CODEGEN_TARGET_PLATFORM.equals(changedKey) ||
			// PREF_PB_ASSERT_AS_IDENTIFIER.equals(changedKey)) {
			// fComplianceStatus= validateCompliance();
			// } else
			if (PREF_PB_MAX_PER_UNIT.equals(changedKey)) {
				fMaxNumberProblemsStatus = validateMaxNumberProblems();
				// } else if (PREF_RESOURCE_FILTER.equals(changedKey)) {
				// fResourceFilterStatus= validateResourceFilters();
				// } else if (S.equals(changedKey) ||
				// PREF_PB_DEPRECATION.equals(changedKey) ) { // ||
				// // PREF_PB_INVALID_JAVADOC.equals(changedKey) ||
				// // PREF_PB_MISSING_JAVADOC_TAGS.equals(changedKey) ||
				// // PREF_PB_MISSING_JAVADOC_COMMENTS.equals(changedKey) ||
				// // PREF_PB_MISSING_JAVADOC_COMMENTS.equals(changedKey) ||
				// //
				// PREF_PB_UNUSED_DECLARED_THROWN_EXCEPTION.equals(changedKey))
				// {
				// updateEnableStates();
				// } else if
				// (PREF_PB_SIGNAL_PARAMETER_IN_OVERRIDING.equals(changedKey)) {
				// // merging the two options
				// fWorkingValues.put(PREF_PB_SIGNAL_PARAMETER_IN_ABSTRACT,
				// newValue);
			} else {
				return;
			}
		} else {
			// updateEnableStates();
			// updateComplianceEnableState();
			// fComplianceStatus= validateCompliance();
			fMaxNumberProblemsStatus = validateMaxNumberProblems();
			// fResourceFilterStatus= validateResourceFilters();
		}
		// IStatus status= StatusUtil.getMostSevere(new IStatus[] {
		// fComplianceStatus, fMaxNumberProblemsStatus, fResourceFilterStatus
		// });
		IStatus status = StatusUtil
				.getMostSevere(new IStatus[] { fMaxNumberProblemsStatus });
		fContext.statusChanged(status);
	}

	// private void updateEnableStates() {
	// boolean enableUnusedParams= !checkValue(PREF_PB_UNUSED_PARAMETER,
	// IGNORE);
	// getCheckBox(PREF_PB_SIGNAL_PARAMETER_IN_OVERRIDING).setEnabled(enableUnusedParams);

	// boolean enableDeprecation= !checkValue(PREF_PB_DEPRECATION, IGNORE);
	// getCheckBox(PREF_PB_DEPRECATION_IN_DEPRECATED_CODE).setEnabled(enableDeprecation);
	// getCheckBox(PREF_PB_DEPRECATION_WHEN_OVERRIDING).setEnabled(enableDeprecation);
	//		
	// boolean enableThrownExceptions=
	// !checkValue(PREF_PB_UNUSED_DECLARED_THROWN_EXCEPTION, IGNORE);
	// getCheckBox(PREF_PB_UNUSED_DECLARED_THROWN_EXCEPTION_WHEN_OVERRIDING).setEnabled(enableThrownExceptions);
	//
	// boolean enableHiding= !checkValue(PREF_PB_LOCAL_VARIABLE_HIDING, IGNORE);
	// getCheckBox(PREF_PB_SPECIAL_PARAMETER_HIDING_FIELD).setEnabled(enableHiding);
	//
	// boolean enableInvalidTagsErrors= !checkValue(PREF_PB_INVALID_JAVADOC,
	// IGNORE);
	// getCheckBox(PREF_PB_INVALID_JAVADOC_TAGS).setEnabled(enableInvalidTagsErrors);
	// setComboEnabled(PREF_PB_INVALID_JAVADOC_TAGS_VISIBILITY,
	// enableInvalidTagsErrors);
	//		
	// boolean enableMissingTagsErrors=
	// !checkValue(PREF_PB_MISSING_JAVADOC_TAGS, IGNORE);
	// getCheckBox(PREF_PB_MISSING_JAVADOC_TAGS_OVERRIDING).setEnabled(enableMissingTagsErrors);
	// setComboEnabled(PREF_PB_MISSING_JAVADOC_TAGS_VISIBILITY,
	// enableMissingTagsErrors);
	//		
	// boolean enableMissingCommentsErrors=
	// !checkValue(PREF_PB_MISSING_JAVADOC_COMMENTS, IGNORE);
	// getCheckBox(PREF_PB_MISSING_JAVADOC_COMMENTS_OVERRIDING).setEnabled(enableMissingCommentsErrors);
	// setComboEnabled(PREF_PB_MISSING_JAVADOC_COMMENTS_VISIBILITY,
	// enableMissingCommentsErrors);
	// }

	// private IStatus validateCompliance() {
	// StatusInfo status= new StatusInfo();
	// if (checkValue(PREF_COMPLIANCE, VERSION_1_3)) {
	// if (checkValue(PREF_SOURCE_COMPATIBILITY, VERSION_1_4)) {
	// status.setError(PreferencesMessages.getString("CompilerConfigurationBlock.cpl13src14.error"));
	// //$NON-NLS-1$
	// return status;
	// } else if (checkValue(PREF_CODEGEN_TARGET_PLATFORM, VERSION_1_4)) {
	// status.setError(PreferencesMessages.getString("CompilerConfigurationBlock.cpl13trg14.error"));
	// //$NON-NLS-1$
	// return status;
	// }
	// }
	// if (checkValue(PREF_SOURCE_COMPATIBILITY, VERSION_1_4)) {
	// if (!checkValue(PREF_PB_ASSERT_AS_IDENTIFIER, ERROR)) {
	// status.setError(PreferencesMessages.getString("CompilerConfigurationBlock.src14asrterr.error"));
	// //$NON-NLS-1$
	// return status;
	// }
	// }
	// if (checkValue(PREF_SOURCE_COMPATIBILITY, VERSION_1_4)) {
	// if (!checkValue(PREF_CODEGEN_TARGET_PLATFORM, VERSION_1_4)) {
	// status.setError(PreferencesMessages.getString("CompilerConfigurationBlock.src14tgt14.error"));
	// //$NON-NLS-1$
	// return status;
	// }
	// }
	// return status;
	// }

	private IStatus validateMaxNumberProblems() {
		String number = (String) fWorkingValues.get(PREF_PB_MAX_PER_UNIT);
		StatusInfo status = new StatusInfo();
		if (number.length() == 0) {
			status.setError(PreferencesMessages
					.getString("CompilerConfigurationBlock.empty_input")); //$NON-NLS-1$
		} else {
			try {
				int value = Integer.parseInt(number);
				if (value <= 0) {
					status
							.setError(PreferencesMessages
									.getFormattedString(
											"CompilerConfigurationBlock.invalid_input", number)); //$NON-NLS-1$
				}
			} catch (NumberFormatException e) {
				status.setError(PreferencesMessages.getFormattedString(
						"CompilerConfigurationBlock.invalid_input", number)); //$NON-NLS-1$
			}
		}
		return status;
	}

	// private IStatus validateResourceFilters() {
	// String text= (String) fWorkingValues.get(PREF_RESOURCE_FILTER);
	//		
	// IWorkspace workspace= ResourcesPlugin.getWorkspace();
	//
	// String[] filters= getTokens(text, ","); //$NON-NLS-1$
	// for (int i= 0; i < filters.length; i++) {
	// String fileName= filters[i].replace('*', 'x');
	// int resourceType= IResource.FILE;
	// int lastCharacter= fileName.length() - 1;
	// if (lastCharacter >= 0 && fileName.charAt(lastCharacter) == '/') {
	// fileName= fileName.substring(0, lastCharacter);
	// resourceType= IResource.FOLDER;
	// }
	// IStatus status= workspace.validateName(fileName, resourceType);
	// if (status.matches(IStatus.ERROR)) {
	// String message=
	// PreferencesMessages.getFormattedString("CompilerConfigurationBlock.filter.invalidsegment.error",
	// status.getMessage()); //$NON-NLS-1$
	// return new StatusInfo(IStatus.ERROR, message);
	// }
	// }
	// return new StatusInfo();
	// }

	protected String[] getFullBuildDialogStrings(boolean workspaceSettings) {
		String title = PreferencesMessages
				.getString("CompilerConfigurationBlock.needsbuild.title"); //$NON-NLS-1$
		String message;
		if (workspaceSettings) {
			message = PreferencesMessages
					.getString("CompilerConfigurationBlock.needsfullbuild.message"); //$NON-NLS-1$
		} else {
			message = PreferencesMessages
					.getString("CompilerConfigurationBlock.needsprojectbuild.message"); //$NON-NLS-1$
		}
		return new String[] { title, message };
	}

}
