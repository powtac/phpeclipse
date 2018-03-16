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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import net.sourceforge.phpdt.core.JavaCore;
import net.sourceforge.phpdt.internal.ui.IJavaHelpContextIds;
import net.sourceforge.phpdt.internal.ui.dialogs.StatusInfo;
import net.sourceforge.phpdt.internal.ui.dialogs.StatusUtil;
import net.sourceforge.phpdt.internal.ui.text.IPHPPartitions;
import net.sourceforge.phpdt.internal.ui.text.PreferencesAdapter;
import net.sourceforge.phpdt.internal.ui.util.TabFolderLayout;
import net.sourceforge.phpdt.ui.PreferenceConstants;
import net.sourceforge.phpdt.ui.text.JavaTextTools;
import net.sourceforge.phpdt.ui.text.PHPSourceViewerConfiguration;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;
import net.sourceforge.phpeclipse.phpeditor.EditorUtility;
import net.sourceforge.phpeclipse.phpeditor.JavaSourceViewer;
import net.sourceforge.phpeclipse.preferences.ColorEditor;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;


/**
 * The page for setting the editor options.
 */
public class JavaEditorPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {
	private static final String BOLD = PreferenceConstants.EDITOR_BOLD_SUFFIX;

	private static final String COMPILER_TASK_TAGS = JavaCore.COMPILER_TASK_TAGS;

	private static final String DELIMITER = PreferencesMessages
			.getString("JavaEditorPreferencePage.navigation.delimiter"); //$NON-NLS-1$

	/** The keys of the overlay store. */
	public final OverlayPreferenceStore.OverlayKey[] fKeys;

	private final String[][] fSyntaxColorListModel = new String[][] {
			{
					PreferencesMessages
							.getString("JavaEditorPreferencePage.multiLineComment"),
					PreferenceConstants.EDITOR_MULTI_LINE_COMMENT_COLOR },
			//$NON-NLS-1$
			{
					PreferencesMessages
							.getString("JavaEditorPreferencePage.singleLineComment"),
					PreferenceConstants.EDITOR_SINGLE_LINE_COMMENT_COLOR },
			//$NON-NLS-1$
			{ PreferencesMessages.getString("JavaEditorPreferencePage.tags"),
					PreferenceConstants.EDITOR_PHP_TAG_COLOR },
			//$NON-NLS-1$
			{
					PreferencesMessages
							.getString("JavaEditorPreferencePage.keywords"),
					PreferenceConstants.EDITOR_JAVA_KEYWORD_COLOR },
			{
					PreferencesMessages
							.getString("JavaEditorPreferencePage.functionNames"),
					PreferenceConstants.EDITOR_PHP_FUNCTIONNAME_COLOR },
			//$NON-NLS-1$
			{
					PreferencesMessages
							.getString("JavaEditorPreferencePage.variables"),
					PreferenceConstants.EDITOR_PHP_VARIABLE_COLOR },
			//$NON-NLS-1$
			{
					PreferencesMessages
							.getString("JavaEditorPreferencePage.variables_dollar"),
					PreferenceConstants.EDITOR_PHP_VARIABLE_DOLLAR_COLOR },
			//$NON-NLS-1$
			{
					PreferencesMessages
							.getString("JavaEditorPreferencePage.constants"),
					PreferenceConstants.EDITOR_PHP_CONSTANT_COLOR },
			//$NON-NLS-1$
			{ PreferencesMessages.getString("JavaEditorPreferencePage.types"),
					PreferenceConstants.EDITOR_PHP_TYPE_COLOR },
			//$NON-NLS-1$
			{
					PreferencesMessages
							.getString("JavaEditorPreferencePage.strings_dq"),
					PreferenceConstants.EDITOR_STRING_COLOR_DQ },
			{
					PreferencesMessages
							.getString("JavaEditorPreferencePage.strings_sq"),
					PreferenceConstants.EDITOR_STRING_COLOR_SQ },
			//$NON-NLS-1$
			{
					PreferencesMessages
							.getString("JavaEditorPreferencePage.others"), PreferenceConstants.EDITOR_JAVA_DEFAULT_COLOR }, //$NON-NLS-1$
			{
					PreferencesMessages
							.getString("JavaEditorPreferencePage.operators"),
					PreferenceConstants.EDITOR_PHP_OPERATOR_COLOR },
			//$NON-NLS-1$
			{
					PreferencesMessages
							.getString("JavaEditorPreferencePage.returnKeyword"),
					PreferenceConstants.EDITOR_PHP_KEYWORD_RETURN_COLOR },
			{ PreferencesMessages.getString("JavaEditorPreferencePage.braces"),
					PreferenceConstants.EDITOR_PHP_BRACE_OPERATOR_COLOR },
			//$NON-NLS-1$
			{
					PreferencesMessages
							.getString("JavaEditorPreferencePage.phpDocKeywords"),
					PreferenceConstants.EDITOR_JAVADOC_KEYWORD_COLOR },
			//$NON-NLS-1$
			{
					PreferencesMessages
							.getString("JavaEditorPreferencePage.phpDocHtmlTags"),
					PreferenceConstants.EDITOR_JAVADOC_TAG_COLOR },
			//$NON-NLS-1$
			{
					PreferencesMessages
							.getString("JavaEditorPreferencePage.phpDocLinks"),
					PreferenceConstants.EDITOR_JAVADOC_LINKS_COLOR },
			//$NON-NLS-1$
			{
					PreferencesMessages
							.getString("JavaEditorPreferencePage.phpDocOthers"), PreferenceConstants.EDITOR_JAVADOC_DEFAULT_COLOR } //$NON-NLS-1$
	};

	private final String[][] fAppearanceColorListModel = new String[][] {
			{
					PreferencesMessages
							.getString("JavaEditorPreferencePage.matchingBracketsHighlightColor2"),
					PreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR },
			//$NON-NLS-1$
			{
					PreferencesMessages
							.getString("JavaEditorPreferencePage.currentLineHighlighColor"),
					AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR },
			//$NON-NLS-1$
			{
					PreferencesMessages
							.getString("JavaEditorPreferencePage.printMarginColor2"),
					AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR },
			//$NON-NLS-1$
			{
					PreferencesMessages
							.getString("JavaEditorPreferencePage.findScopeColor2"),
					PreferenceConstants.EDITOR_FIND_SCOPE_COLOR },
			//$NON-NLS-1$
			{
					PreferencesMessages
							.getString("JavaEditorPreferencePage.linkColor2"), PreferenceConstants.EDITOR_LINK_COLOR }, //$NON-NLS-1$
	};

	// private final String[][] fAnnotationColorListModel;

	// private final String[][] fAnnotationDecorationListModel = new String[][]{
	// {
	// PreferencesMessages
	// .getString("JavaEditorPreferencePage.AnnotationDecoration.NONE"),
	// AnnotationPreference.STYLE_NONE},
	// //$NON-NLS-1$
	// {
	// PreferencesMessages
	// .getString("JavaEditorPreferencePage.AnnotationDecoration.SQUIGGLIES"),
	// AnnotationPreference.STYLE_SQUIGGLIES},
	// //$NON-NLS-1$
	// {
	// PreferencesMessages
	// .getString("JavaEditorPreferencePage.AnnotationDecoration.UNDERLINE"),
	// AnnotationPreference.STYLE_UNDERLINE},
	// //$NON-NLS-1$
	// {
	// PreferencesMessages
	// .getString("JavaEditorPreferencePage.AnnotationDecoration.BOX"),
	// AnnotationPreference.STYLE_BOX},
	// //$NON-NLS-1$
	// {
	// PreferencesMessages
	// .getString("JavaEditorPreferencePage.AnnotationDecoration.IBEAM"),
	// AnnotationPreference.STYLE_IBEAM} //$NON-NLS-1$
	// };
	private OverlayPreferenceStore fOverlayStore;

	private JavaTextTools fJavaTextTools;

	private JavaEditorHoverConfigurationBlock fJavaEditorHoverConfigurationBlock;

	private FoldingConfigurationBlock fFoldingConfigurationBlock;

	private Map fColorButtons = new HashMap();

	private Map fCheckBoxes = new HashMap();

	private SelectionListener fCheckBoxListener = new SelectionListener() {
		public void widgetDefaultSelected(SelectionEvent e) {
		}

		public void widgetSelected(SelectionEvent e) {
			Button button = (Button) e.widget;
			fOverlayStore.setValue((String) fCheckBoxes.get(button), button
					.getSelection());
		}
	};

	private Map fTextFields = new HashMap();

	private ModifyListener fTextFieldListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			Text text = (Text) e.widget;
			fOverlayStore.setValue((String) fTextFields.get(text), text
					.getText());
		}
	};

	private ArrayList fNumberFields = new ArrayList();

	private ModifyListener fNumberFieldListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			numberFieldChanged((Text) e.widget);
		}
	};

	private List fSyntaxColorList;

	private List fAppearanceColorList;

	// private List fContentAssistColorList;
	private List fAnnotationList;

	private ColorEditor fSyntaxForegroundColorEditor;

	private ColorEditor fAppearanceColorEditor;

	private ColorEditor fAnnotationForegroundColorEditor;

	private ColorEditor fContentAssistColorEditor;

	private ColorEditor fBackgroundColorEditor;

	private Button fBackgroundDefaultRadioButton;

	private Button fBackgroundCustomRadioButton;

	private Button fBackgroundColorButton;

	private Button fBoldCheckBox;

	// private Button fAddJavaDocTagsButton;

	private Button fEscapeStringsButtonDQ;

	private Button fEscapeStringsButtonSQ;

	// private Button fGuessMethodArgumentsButton;
	private SourceViewer fPreviewViewer;

	private Color fBackgroundColor;

	private Control fAutoInsertDelayText;

	private Control fAutoInsertJavaTriggerText;

	private Control fAutoInsertJavaDocTriggerText;

	private Label fAutoInsertDelayLabel;

	private Label fAutoInsertJavaTriggerLabel;

	private Label fAutoInsertJavaDocTriggerLabel;

	private Button fShowInTextCheckBox;

	private Combo fDecorationStyleCombo;

	private Button fHighlightInTextCheckBox;

	private Button fShowInOverviewRulerCheckBox;

	private Button fShowInVerticalRulerCheckBox;

	private Text fBrowserLikeLinksKeyModifierText;

	private Button fBrowserLikeLinksCheckBox;

	private StatusInfo fBrowserLikeLinksKeyModifierStatus;

	// private Button fCompletionInsertsRadioButton;
	// private Button fCompletionOverwritesRadioButton;
	// private Button fStickyOccurrencesButton;
	/**
	 * Creates a new preference page.
	 */
	public JavaEditorPreferencePage() {
		setDescription(PreferencesMessages
				.getString("JavaEditorPreferencePage.description")); //$NON-NLS-1$
		setPreferenceStore(PHPeclipsePlugin.getDefault().getPreferenceStore());
		MarkerAnnotationPreferences markerAnnotationPreferences = new MarkerAnnotationPreferences();
		fKeys = createOverlayStoreKeys(markerAnnotationPreferences);
		fOverlayStore = new OverlayPreferenceStore(getPreferenceStore(), fKeys);
		// fAnnotationColorListModel =
		// createAnnotationTypeListModel(markerAnnotationPreferences);
	}

	private OverlayPreferenceStore.OverlayKey[] createOverlayStoreKeys(
			MarkerAnnotationPreferences preferences) {
		ArrayList overlayKeys = new ArrayList();
		Iterator e = preferences.getAnnotationPreferences().iterator();
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.EDITOR_FOREGROUND_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_FOREGROUND_DEFAULT_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.EDITOR_BACKGROUND_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_BACKGROUND_DEFAULT_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.INT,
				PreferenceConstants.EDITOR_TAB_WIDTH));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.EDITOR_MULTI_LINE_COMMENT_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_MULTI_LINE_COMMENT_BOLD));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.EDITOR_SINGLE_LINE_COMMENT_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_SINGLE_LINE_COMMENT_BOLD));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.EDITOR_JAVA_KEYWORD_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_JAVA_KEYWORD_BOLD));

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.EDITOR_PHP_TAG_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_PHP_TAG_BOLD));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.EDITOR_PHP_FUNCTIONNAME_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_PHP_FUNCTIONNAME_BOLD));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.EDITOR_PHP_VARIABLE_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_PHP_VARIABLE_BOLD));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.EDITOR_PHP_VARIABLE_DOLLAR_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_PHP_VARIABLE_DOLLAR_BOLD));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.EDITOR_PHP_CONSTANT_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_PHP_CONSTANT_BOLD));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.EDITOR_PHP_TYPE_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_PHP_TYPE_BOLD));

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.EDITOR_STRING_COLOR_DQ));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_STRING_BOLD_DQ));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.EDITOR_STRING_COLOR_SQ));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_STRING_BOLD_SQ));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.EDITOR_JAVA_DEFAULT_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_JAVA_DEFAULT_BOLD));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.EDITOR_TASK_TAG_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_TASK_TAG_BOLD));
		// overlayKeys.add(new
		// OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING_DQ,
		// PreferenceConstants.EDITOR_JAVA_METHOD_NAME_COLOR));
		// overlayKeys.add(new
		// OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN,
		// PreferenceConstants.EDITOR_JAVA_METHOD_NAME_BOLD));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.EDITOR_PHP_OPERATOR_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_PHP_OPERATOR_BOLD));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.EDITOR_PHP_KEYWORD_RETURN_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_PHP_KEYWORD_RETURN_BOLD));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.EDITOR_PHP_BRACE_OPERATOR_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_PHP_BRACE_OPERATOR_BOLD));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.EDITOR_JAVADOC_KEYWORD_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_JAVADOC_KEYWORD_BOLD));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.EDITOR_JAVADOC_TAG_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_JAVADOC_TAG_BOLD));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.EDITOR_JAVADOC_LINKS_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_JAVADOC_LINKS_BOLD));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.EDITOR_JAVADOC_DEFAULT_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_JAVADOC_DEFAULT_BOLD));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_MATCHING_BRACKETS));
		overlayKeys
				.add(new OverlayPreferenceStore.OverlayKey(
						OverlayPreferenceStore.STRING,
						AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR));
		overlayKeys
				.add(new OverlayPreferenceStore.OverlayKey(
						OverlayPreferenceStore.BOOLEAN,
						AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE));
		overlayKeys
				.add(new OverlayPreferenceStore.OverlayKey(
						OverlayPreferenceStore.STRING,
						AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR));
		overlayKeys
				.add(new OverlayPreferenceStore.OverlayKey(
						OverlayPreferenceStore.INT,
						AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN));
		overlayKeys
				.add(new OverlayPreferenceStore.OverlayKey(
						OverlayPreferenceStore.BOOLEAN,
						AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN));
		// overlayKeys.add(new
		// OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN,
		// PreferenceConstants.EDITOR_MARK_OCCURRENCES));
		// overlayKeys.add(new
		// OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN,
		// PreferenceConstants.EDITOR_STICKY_OCCURRENCES));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.EDITOR_FIND_SCOPE_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.EDITOR_LINK_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_CORRECTION_INDICATION));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_EVALUTE_TEMPORARY_PROBLEMS));
		overlayKeys
				.add(new OverlayPreferenceStore.OverlayKey(
						OverlayPreferenceStore.BOOLEAN,
						AbstractDecoratedTextEditorPreferenceConstants.EDITOR_OVERVIEW_RULER));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_SPACES_FOR_TABS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.CODEASSIST_AUTOACTIVATION));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.INT,
				PreferenceConstants.CODEASSIST_AUTOACTIVATION_DELAY));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.CODEASSIST_AUTOINSERT));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.CODEASSIST_PROPOSALS_BACKGROUND));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.CODEASSIST_PROPOSALS_FOREGROUND));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.CODEASSIST_PARAMETERS_BACKGROUND));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.CODEASSIST_PARAMETERS_FOREGROUND));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.CODEASSIST_REPLACEMENT_BACKGROUND));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.CODEASSIST_REPLACEMENT_FOREGROUND));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS_JAVA));
		overlayKeys
				.add(new OverlayPreferenceStore.OverlayKey(
						OverlayPreferenceStore.STRING,
						PreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS_JAVADOC));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.CODEASSIST_SHOW_VISIBLE_PROPOSALS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.CODEASSIST_ORDER_PROPOSALS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.CODEASSIST_CASE_SENSITIVITY));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.CODEASSIST_ADDIMPORT));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.CODEASSIST_INSERT_COMPLETION));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_SMART_PASTE));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_CLOSE_STRINGS_DQ_PHP));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_CLOSE_STRINGS_SQ_PHP));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_CLOSE_BRACKETS_PHP));
		// overlayKeys
		// .add(new OverlayPreferenceStore.OverlayKey(
		// OverlayPreferenceStore.BOOLEAN,
		// PreferenceConstants.EDITOR_CLOSE_BRACES));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_CLOSE_JAVADOCS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_WRAP_WORDS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_WRAP_STRINGS_DQ));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_ESCAPE_STRINGS_DQ));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_WRAP_STRINGS_SQ));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_ESCAPE_STRINGS_SQ));

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_ADD_JAVADOC_TAGS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_FORMAT_JAVADOCS));

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_SMART_HOME_END));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_SUB_WORD_NAVIGATION));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_DISABLE_OVERWRITE_MODE));
		// overlayKeys.add(new
		// OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN,
		// PreferenceConstants.EDITOR_SHOW_TEXT_HOVER_AFFORDANCE));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.EDITOR_TEXT_HOVER_MODIFIERS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.EDITOR_TEXT_HOVER_MODIFIER_MASKS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.EDITOR_BROWSER_LIKE_LINKS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
				OverlayPreferenceStore.STRING,
				PreferenceConstants.EDITOR_BROWSER_LIKE_LINKS_KEY_MODIFIER));
		overlayKeys
				.add(new OverlayPreferenceStore.OverlayKey(
						OverlayPreferenceStore.STRING,
						PreferenceConstants.EDITOR_BROWSER_LIKE_LINKS_KEY_MODIFIER_MASK));
		while (e.hasNext()) {
			AnnotationPreference info = (AnnotationPreference) e.next();
			overlayKeys
					.add(new OverlayPreferenceStore.OverlayKey(
							OverlayPreferenceStore.STRING, info
									.getColorPreferenceKey()));
			overlayKeys
					.add(new OverlayPreferenceStore.OverlayKey(
							OverlayPreferenceStore.BOOLEAN, info
									.getTextPreferenceKey()));
			if (info.getHighlightPreferenceKey() != null)
				overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
						OverlayPreferenceStore.BOOLEAN, info
								.getHighlightPreferenceKey()));
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
					OverlayPreferenceStore.BOOLEAN, info
							.getOverviewRulerPreferenceKey()));
			if (info.getVerticalRulerPreferenceKey() != null)
				overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
						OverlayPreferenceStore.BOOLEAN, info
								.getVerticalRulerPreferenceKey()));
			if (info.getTextStylePreferenceKey() != null)
				overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
						OverlayPreferenceStore.STRING, info
								.getTextStylePreferenceKey()));
		}
		OverlayPreferenceStore.OverlayKey[] keys = new OverlayPreferenceStore.OverlayKey[overlayKeys
				.size()];
		overlayKeys.toArray(keys);
		return keys;
	} /*
		 * @see IWorkbenchPreferencePage#init()
		 */

	public void init(IWorkbench workbench) {
	}

	/*
	 * @see PreferencePage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
				IJavaHelpContextIds.JAVA_EDITOR_PREFERENCE_PAGE);
	}

	private void handleSyntaxColorListSelection() {
		int i = fSyntaxColorList.getSelectionIndex();
		String key = fSyntaxColorListModel[i][1];
		RGB rgb = PreferenceConverter.getColor(fOverlayStore, key);
		fSyntaxForegroundColorEditor.setColorValue(rgb);
		fBoldCheckBox.setSelection(fOverlayStore.getBoolean(key + BOLD));
	}

	private void handleAppearanceColorListSelection() {
		int i = fAppearanceColorList.getSelectionIndex();
		String key = fAppearanceColorListModel[i][1];
		RGB rgb = PreferenceConverter.getColor(fOverlayStore, key);
		fAppearanceColorEditor.setColorValue(rgb);
	}

	// private void handleAnnotationListSelection() {
	// int i = fAnnotationList.getSelectionIndex();
	// String key = fAnnotationColorListModel[i][1];
	// RGB rgb = PreferenceConverter.getColor(fOverlayStore, key);
	// fAnnotationForegroundColorEditor.setColorValue(rgb);
	// key = fAnnotationColorListModel[i][2];
	// boolean showInText = fOverlayStore.getBoolean(key);
	// fShowInTextCheckBox.setSelection(showInText);
	// key = fAnnotationColorListModel[i][6];
	// if (key != null) {
	// fDecorationStyleCombo.setEnabled(showInText);
	// for (int j = 0; j < fAnnotationDecorationListModel.length; j++) {
	// String value = fOverlayStore.getString(key);
	// if (fAnnotationDecorationListModel[j][1].equals(value)) {
	// fDecorationStyleCombo.setText(fAnnotationDecorationListModel[j][0]);
	// break;
	// }
	// }
	// } else {
	// fDecorationStyleCombo.setEnabled(false);
	// fDecorationStyleCombo.setText(fAnnotationDecorationListModel[1][0]); //
	// set
	// // selection
	// // to
	// // squigglies
	// // if
	// // the
	// // key
	// // is
	// // not
	// // there
	// // (legacy
	// // support)
	// }
	// key = fAnnotationColorListModel[i][3];
	// fShowInOverviewRulerCheckBox.setSelection(fOverlayStore.getBoolean(key));
	// key = fAnnotationColorListModel[i][4];
	// if (key != null) {
	// fHighlightInTextCheckBox.setSelection(fOverlayStore.getBoolean(key));
	// fHighlightInTextCheckBox.setEnabled(true);
	// } else
	// fHighlightInTextCheckBox.setEnabled(false);
	// key = fAnnotationColorListModel[i][5];
	// if (key != null) {
	// fShowInVerticalRulerCheckBox.setSelection(fOverlayStore.getBoolean(key));
	// fShowInVerticalRulerCheckBox.setEnabled(true);
	// } else {
	// fShowInVerticalRulerCheckBox.setSelection(true);
	// fShowInVerticalRulerCheckBox.setEnabled(false);
	// }
	// }
	private Control createSyntaxPage(Composite parent) {
		Composite colorComposite = new Composite(parent, SWT.NULL);
		colorComposite.setLayout(new GridLayout());
		Group backgroundComposite = new Group(colorComposite,
				SWT.SHADOW_ETCHED_IN);
		backgroundComposite.setLayout(new RowLayout());
		backgroundComposite.setText(PreferencesMessages
				.getString("JavaEditorPreferencePage.backgroundColor"));//$NON-NLS-1$
		SelectionListener backgroundSelectionListener = new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				boolean custom = fBackgroundCustomRadioButton.getSelection();
				fBackgroundColorButton.setEnabled(custom);
				fOverlayStore.setValue(
						PreferenceConstants.EDITOR_BACKGROUND_DEFAULT_COLOR,
						!custom);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		};
		fBackgroundDefaultRadioButton = new Button(backgroundComposite,
				SWT.RADIO | SWT.LEFT);
		fBackgroundDefaultRadioButton.setText(PreferencesMessages
				.getString("JavaEditorPreferencePage.systemDefault")); //$NON-NLS-1$
		fBackgroundDefaultRadioButton
				.addSelectionListener(backgroundSelectionListener);
		fBackgroundCustomRadioButton = new Button(backgroundComposite,
				SWT.RADIO | SWT.LEFT);
		fBackgroundCustomRadioButton.setText(PreferencesMessages
				.getString("JavaEditorPreferencePage.custom")); //$NON-NLS-1$
		fBackgroundCustomRadioButton
				.addSelectionListener(backgroundSelectionListener);
		fBackgroundColorEditor = new ColorEditor(backgroundComposite);
		fBackgroundColorButton = fBackgroundColorEditor.getButton();
		Label label = new Label(colorComposite, SWT.LEFT);
		label.setText(PreferencesMessages
				.getString("JavaEditorPreferencePage.foreground")); //$NON-NLS-1$
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Composite editorComposite = new Composite(colorComposite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		editorComposite.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		editorComposite.setLayoutData(gd);
		fSyntaxColorList = new List(editorComposite, SWT.SINGLE | SWT.V_SCROLL
				| SWT.BORDER);
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = convertHeightInCharsToPixels(5);
		fSyntaxColorList.setLayoutData(gd);
		Composite stylesComposite = new Composite(editorComposite, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		stylesComposite.setLayout(layout);
		stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		label = new Label(stylesComposite, SWT.LEFT);
		label.setText(PreferencesMessages
				.getString("JavaEditorPreferencePage.color")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		label.setLayoutData(gd);
		fSyntaxForegroundColorEditor = new ColorEditor(stylesComposite);
		Button foregroundColorButton = fSyntaxForegroundColorEditor.getButton();
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment = GridData.BEGINNING;
		foregroundColorButton.setLayoutData(gd);
		fBoldCheckBox = new Button(stylesComposite, SWT.CHECK);
		fBoldCheckBox.setText(PreferencesMessages
				.getString("JavaEditorPreferencePage.bold")); //$NON-NLS-1$
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment = GridData.BEGINNING;
		gd.horizontalSpan = 2;
		fBoldCheckBox.setLayoutData(gd);
		label = new Label(colorComposite, SWT.LEFT);
		label.setText(PreferencesMessages
				.getString("JavaEditorPreferencePage.preview")); //$NON-NLS-1$
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Control previewer = createPreviewer(colorComposite);
		gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = convertWidthInCharsToPixels(20);
		gd.heightHint = convertHeightInCharsToPixels(5);
		previewer.setLayoutData(gd);
		fSyntaxColorList.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}

			public void widgetSelected(SelectionEvent e) {
				handleSyntaxColorListSelection();
			}
		});
		foregroundColorButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}

			public void widgetSelected(SelectionEvent e) {
				int i = fSyntaxColorList.getSelectionIndex();
				String key = fSyntaxColorListModel[i][1];
				PreferenceConverter.setValue(fOverlayStore, key,
						fSyntaxForegroundColorEditor.getColorValue());
			}
		});
		fBackgroundColorButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}

			public void widgetSelected(SelectionEvent e) {
				PreferenceConverter.setValue(fOverlayStore,
						PreferenceConstants.EDITOR_BACKGROUND_COLOR,
						fBackgroundColorEditor.getColorValue());
			}
		});
		fBoldCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}

			public void widgetSelected(SelectionEvent e) {
				int i = fSyntaxColorList.getSelectionIndex();
				String key = fSyntaxColorListModel[i][1];
				fOverlayStore
						.setValue(key + BOLD, fBoldCheckBox.getSelection());
			}
		});
		return colorComposite;
	}

	private Control createPreviewer(Composite parent) {
		Preferences coreStore = createTemporaryCorePreferenceStore();
		fJavaTextTools = new JavaTextTools(fOverlayStore, coreStore, false);
		IPreferenceStore generalTextStore = EditorsUI.getPreferenceStore();
		IPreferenceStore store = new ChainedPreferenceStore(
				new IPreferenceStore[] {
						fOverlayStore,
						new PreferencesAdapter(
								createTemporaryCorePreferenceStore()),
						generalTextStore });

		fPreviewViewer = new JavaSourceViewer(parent, null, null, false,
				SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER, store);
		JavaTextTools tools = PHPeclipsePlugin.getDefault().getJavaTextTools();
		PHPSourceViewerConfiguration configuration = new PHPSourceViewerConfiguration(
				tools.getColorManager(), store, null,
				IPHPPartitions.PHP_PARTITIONING);
		// PHPSourceViewerConfiguration configuration =new
		// PHPSourceViewerConfiguration(fJavaTextTools, null,
		// IPHPPartitions.PHP_PARTITIONING);
		fPreviewViewer.configure(configuration);

		Font font = JFaceResources
				.getFont(PreferenceConstants.EDITOR_TEXT_FONT);
		fPreviewViewer.getTextWidget().setFont(font);
		new JavaSourcePreviewerUpdater(fPreviewViewer, configuration, store);
		fPreviewViewer.setEditable(false);
		String content = loadPreviewContentFromFile("ColorSettingPreviewCode.txt"); //$NON-NLS-1$
		IDocument document = new Document(content);
		fJavaTextTools.setupJavaDocumentPartitioner(document,
				IPHPPartitions.PHP_PARTITIONING);
		fPreviewViewer.setDocument(document);
		return fPreviewViewer.getControl();
	}

	private Preferences createTemporaryCorePreferenceStore() {
		Preferences result = new Preferences();
		result.setValue(COMPILER_TASK_TAGS, "TASK"); //$NON-NLS-1$
		return result;
	}

	private Control createAppearancePage(Composite parent) {
		Composite appearanceComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		appearanceComposite.setLayout(layout);
		
		// Inserts a hyper-link to the General Editor preferences page
		// TODO Can probably be removed post 1.5.0?
		String label = PreferencesMessages
				.getString("JavaEditorPreferencePage.appearanceTabLink");
		Link link = new Link(appearanceComposite, SWT.NONE);
		GridData gridPosition = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridPosition.horizontalSpan = 2;
		link.setLayoutData(gridPosition);
		
		link.setText(label);
		link.addListener(SWT.Selection, new Listener () {
			public void handleEvent(Event event) {
				String u = event.text;
				PreferencesUtil.createPreferenceDialogOn(getShell(), u, null, null);
			}
		});
		String tooltip = PreferencesMessages
				.getString("JavaEditorPreferencePage.appearanceTabTooltip");
		link.setToolTipText(tooltip);		
			
		label = PreferencesMessages
				.getString("JavaEditorPreferencePage.displayedTabWidth"); //$NON-NLS-1$
		addTextField(appearanceComposite, label,
				PreferenceConstants.EDITOR_TAB_WIDTH, 3, 0, true);
		label = PreferencesMessages
				.getString("JavaEditorPreferencePage.printMarginColumn"); //$NON-NLS-1$
		addTextField(
				appearanceComposite,
				label,
				AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN,
				3, 0, true);
		label = PreferencesMessages
				.getString("JavaEditorPreferencePage.showOverviewRuler"); //$NON-NLS-1$
		addCheckBox(
				appearanceComposite,
				label,
				AbstractDecoratedTextEditorPreferenceConstants.EDITOR_OVERVIEW_RULER,
				0);
		label = PreferencesMessages
				.getString("JavaEditorPreferencePage.highlightMatchingBrackets"); //$NON-NLS-1$
		addCheckBox(appearanceComposite, label,
				PreferenceConstants.EDITOR_MATCHING_BRACKETS, 0);
		label = PreferencesMessages
				.getString("JavaEditorPreferencePage.highlightCurrentLine"); //$NON-NLS-1$
		addCheckBox(
				appearanceComposite,
				label,
				AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE,
				0);
		label = PreferencesMessages
				.getString("JavaEditorPreferencePage.showPrintMargin"); //$NON-NLS-1$
		addCheckBox(
				appearanceComposite,
				label,
				AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN,
				0);
		label = PreferencesMessages
				.getString("JavaEditorPreferencePage.markOccurrences"); //$NON-NLS-1$
		// Button master= addCheckBox(appearanceComposite, label,
		// PreferenceConstants.EDITOR_MARK_OCCURRENCES, 0); //$NON-NLS-1$
		label = PreferencesMessages
				.getString("JavaEditorPreferencePage.stickyOccurrences"); //$NON-NLS-1$
		// fStickyOccurrencesButton= addCheckBox(appearanceComposite, label,
		// PreferenceConstants.EDITOR_STICKY_OCCURRENCES, 0); //$NON-NLS-1$
		// createDependency(master, fStickyOccurrencesButton);
		Label l = new Label(appearanceComposite, SWT.LEFT);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		gd.heightHint = convertHeightInCharsToPixels(1) / 2;
		l.setLayoutData(gd);
		l = new Label(appearanceComposite, SWT.LEFT);
		l.setText(PreferencesMessages
				.getString("JavaEditorPreferencePage.appearanceOptions")); //$NON-NLS-1$
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		l.setLayoutData(gd);
		Composite editorComposite = new Composite(appearanceComposite, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		editorComposite.setLayout(layout);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.FILL_VERTICAL);
		gd.horizontalSpan = 2;
		editorComposite.setLayoutData(gd);
		fAppearanceColorList = new List(editorComposite, SWT.SINGLE
				| SWT.V_SCROLL | SWT.BORDER);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING
				| GridData.FILL_HORIZONTAL);
		gd.heightHint = convertHeightInCharsToPixels(8);
		fAppearanceColorList.setLayoutData(gd);
		Composite stylesComposite = new Composite(editorComposite, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		stylesComposite.setLayout(layout);
		stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		l = new Label(stylesComposite, SWT.LEFT);
		l.setText(PreferencesMessages
				.getString("JavaEditorPreferencePage.color")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		l.setLayoutData(gd);
		fAppearanceColorEditor = new ColorEditor(stylesComposite);
		Button foregroundColorButton = fAppearanceColorEditor.getButton();
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment = GridData.BEGINNING;
		foregroundColorButton.setLayoutData(gd);
		fAppearanceColorList.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}

			public void widgetSelected(SelectionEvent e) {
				handleAppearanceColorListSelection();
			}
		});
		foregroundColorButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}

			public void widgetSelected(SelectionEvent e) {
				int i = fAppearanceColorList.getSelectionIndex();
				String key = fAppearanceColorListModel[i][1];
				PreferenceConverter.setValue(fOverlayStore, key,
						fAppearanceColorEditor.getColorValue());
			}
		});
		return appearanceComposite;
	}

	// private Control createAnnotationsPage(Composite parent) {
	// Composite composite = new Composite(parent, SWT.NULL);
	// GridLayout layout = new GridLayout();
	// layout.numColumns = 2;
	// composite.setLayout(layout);
	// String text = PreferencesMessages
	// .getString("JavaEditorPreferencePage.analyseAnnotationsWhileTyping");
	// //$NON-NLS-1$
	// addCheckBox(composite, text,
	// PreferenceConstants.EDITOR_EVALUTE_TEMPORARY_PROBLEMS, 0);
	// text = PreferencesMessages
	// .getString("JavaEditorPreferencePage.showQuickFixables"); //$NON-NLS-1$
	// addCheckBox(composite, text,
	// PreferenceConstants.EDITOR_CORRECTION_INDICATION, 0);
	// addFiller(composite);
	// Label label = new Label(composite, SWT.LEFT);
	// label.setText(PreferencesMessages
	// .getString("JavaEditorPreferencePage.annotationPresentationOptions"));
	// //$NON-NLS-1$
	// GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
	// gd.horizontalSpan = 2;
	// label.setLayoutData(gd);
	// Composite editorComposite = new Composite(composite, SWT.NONE);
	// layout = new GridLayout();
	// layout.numColumns = 2;
	// layout.marginHeight = 0;
	// layout.marginWidth = 0;
	// editorComposite.setLayout(layout);
	// gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL |
	// GridData.FILL_VERTICAL);
	// gd.horizontalSpan = 2;
	// editorComposite.setLayoutData(gd);
	// fAnnotationList = new List(editorComposite, SWT.SINGLE | SWT.V_SCROLL
	// | SWT.BORDER);
	// gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING
	// | GridData.FILL_HORIZONTAL);
	// gd.heightHint = convertHeightInCharsToPixels(10);
	// fAnnotationList.setLayoutData(gd);
	// Composite optionsComposite = new Composite(editorComposite, SWT.NONE);
	// layout = new GridLayout();
	// layout.marginHeight = 0;
	// layout.marginWidth = 0;
	// layout.numColumns = 2;
	// optionsComposite.setLayout(layout);
	// optionsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
	// fShowInTextCheckBox = new Button(optionsComposite, SWT.CHECK);
	// fShowInTextCheckBox.setText(PreferencesMessages
	// .getString("JavaEditorPreferencePage.annotations.showInText"));
	// //$NON-NLS-1$
	// gd = new GridData(GridData.FILL_HORIZONTAL);
	// gd.horizontalAlignment = GridData.BEGINNING;
	// gd.horizontalSpan = 2;
	// fShowInTextCheckBox.setLayoutData(gd);
	// fDecorationStyleCombo = new Combo(optionsComposite, SWT.READ_ONLY);
	// for (int i = 0; i < fAnnotationDecorationListModel.length; i++)
	// fDecorationStyleCombo.add(fAnnotationDecorationListModel[i][0]);
	// gd = new GridData(GridData.FILL_HORIZONTAL);
	// gd.horizontalAlignment = GridData.BEGINNING;
	// gd.horizontalSpan = 2;
	// gd.horizontalIndent = 20;
	// fDecorationStyleCombo.setLayoutData(gd);
	// fHighlightInTextCheckBox = new Button(optionsComposite, SWT.CHECK);
	// fHighlightInTextCheckBox.setText(PreferencesMessages
	// .getString("TextEditorPreferencePage.annotations.highlightInText"));
	// //$NON-NLS-1$
	// gd = new GridData(GridData.FILL_HORIZONTAL);
	// gd.horizontalAlignment = GridData.BEGINNING;
	// gd.horizontalSpan = 2;
	// fHighlightInTextCheckBox.setLayoutData(gd);
	// fShowInOverviewRulerCheckBox = new Button(optionsComposite, SWT.CHECK);
	// fShowInOverviewRulerCheckBox.setText(PreferencesMessages
	// .getString("JavaEditorPreferencePage.annotations.showInOverviewRuler"));
	// //$NON-NLS-1$
	// gd = new GridData(GridData.FILL_HORIZONTAL);
	// gd.horizontalAlignment = GridData.BEGINNING;
	// gd.horizontalSpan = 2;
	// fShowInOverviewRulerCheckBox.setLayoutData(gd);
	// fShowInVerticalRulerCheckBox = new Button(optionsComposite, SWT.CHECK);
	// fShowInVerticalRulerCheckBox.setText(PreferencesMessages
	// .getString("JavaEditorPreferencePage.annotations.showInVerticalRuler"));
	// //$NON-NLS-1$
	// gd = new GridData(GridData.FILL_HORIZONTAL);
	// gd.horizontalAlignment = GridData.BEGINNING;
	// gd.horizontalSpan = 2;
	// fShowInVerticalRulerCheckBox.setLayoutData(gd);
	// label = new Label(optionsComposite, SWT.LEFT);
	// label.setText(PreferencesMessages
	// .getString("JavaEditorPreferencePage.annotations.color")); //$NON-NLS-1$
	// gd = new GridData();
	// gd.horizontalAlignment = GridData.BEGINNING;
	// label.setLayoutData(gd);
	// fAnnotationForegroundColorEditor = new ColorEditor(optionsComposite);
	// Button foregroundColorButton =
	// fAnnotationForegroundColorEditor.getButton();
	// gd = new GridData(GridData.FILL_HORIZONTAL);
	// gd.horizontalAlignment = GridData.BEGINNING;
	// foregroundColorButton.setLayoutData(gd);
	// fAnnotationList.addSelectionListener(new SelectionListener() {
	// public void widgetDefaultSelected(SelectionEvent e) {
	// // do nothing
	// }
	// public void widgetSelected(SelectionEvent e) {
	// handleAnnotationListSelection();
	// }
	// });
	// fShowInTextCheckBox.addSelectionListener(new SelectionListener() {
	// public void widgetDefaultSelected(SelectionEvent e) {
	// // do nothing
	// }
	// public void widgetSelected(SelectionEvent e) {
	// int i = fAnnotationList.getSelectionIndex();
	// String key = fAnnotationColorListModel[i][2];
	// fOverlayStore.setValue(key, fShowInTextCheckBox.getSelection());
	// String decorationKey = fAnnotationColorListModel[i][6];
	// fDecorationStyleCombo.setEnabled(decorationKey != null
	// && fShowInTextCheckBox.getSelection());
	// }
	// });
	// fHighlightInTextCheckBox.addSelectionListener(new SelectionListener() {
	// public void widgetDefaultSelected(SelectionEvent e) {
	// // do nothing
	// }
	// public void widgetSelected(SelectionEvent e) {
	// int i = fAnnotationList.getSelectionIndex();
	// String key = fAnnotationColorListModel[i][4];
	// fOverlayStore.setValue(key, fHighlightInTextCheckBox.getSelection());
	// }
	// });
	// fShowInOverviewRulerCheckBox.addSelectionListener(new SelectionListener()
	// {
	// public void widgetDefaultSelected(SelectionEvent e) {
	// // do nothing
	// }
	// public void widgetSelected(SelectionEvent e) {
	// int i = fAnnotationList.getSelectionIndex();
	// String key = fAnnotationColorListModel[i][3];
	// fOverlayStore
	// .setValue(key, fShowInOverviewRulerCheckBox.getSelection());
	// }
	// });
	// fShowInVerticalRulerCheckBox.addSelectionListener(new SelectionListener()
	// {
	// public void widgetDefaultSelected(SelectionEvent e) {
	// // do nothing
	// }
	// public void widgetSelected(SelectionEvent e) {
	// int i = fAnnotationList.getSelectionIndex();
	// String key = fAnnotationColorListModel[i][5];
	// fOverlayStore
	// .setValue(key, fShowInVerticalRulerCheckBox.getSelection());
	// }
	// });
	// foregroundColorButton.addSelectionListener(new SelectionListener() {
	// public void widgetDefaultSelected(SelectionEvent e) {
	// // do nothing
	// }
	// public void widgetSelected(SelectionEvent e) {
	// int i = fAnnotationList.getSelectionIndex();
	// String key = fAnnotationColorListModel[i][1];
	// PreferenceConverter.setValue(fOverlayStore, key,
	// fAnnotationForegroundColorEditor.getColorValue());
	// }
	// });
	// fDecorationStyleCombo.addSelectionListener(new SelectionListener() {
	// /**
	// * {@inheritdoc}
	// */
	// public void widgetDefaultSelected(SelectionEvent e) {
	// // do nothing
	// }
	// /**
	// * {@inheritdoc}
	// */
	// public void widgetSelected(SelectionEvent e) {
	// int i = fAnnotationList.getSelectionIndex();
	// String key = fAnnotationColorListModel[i][6];
	// if (key != null) {
	// for (int j = 0; j < fAnnotationDecorationListModel.length; j++) {
	// if (fAnnotationDecorationListModel[j][0]
	// .equals(fDecorationStyleCombo.getText())) {
	// fOverlayStore.setValue(key, fAnnotationDecorationListModel[j][1]);
	// break;
	// }
	// }
	// }
	// }
	// });
	// return composite;
	// }
	private String[][] createAnnotationTypeListModel(
			MarkerAnnotationPreferences preferences) {
		ArrayList listModelItems = new ArrayList();
		SortedSet sortedPreferences = new TreeSet(new Comparator() {
			/*
			 * @see java.util.Comparator#compare(java.lang.Object,
			 *      java.lang.Object)
			 */
			public int compare(Object o1, Object o2) {
				if (!(o2 instanceof AnnotationPreference))
					return -1;
				if (!(o1 instanceof AnnotationPreference))
					return 1;
				AnnotationPreference a1 = (AnnotationPreference) o1;
				AnnotationPreference a2 = (AnnotationPreference) o2;
				return Collator.getInstance().compare(a1.getPreferenceLabel(),
						a2.getPreferenceLabel());
			}
		});
		sortedPreferences.addAll(preferences.getAnnotationPreferences());
		Iterator e = sortedPreferences.iterator();
		while (e.hasNext()) {
			AnnotationPreference info = (AnnotationPreference) e.next();
			listModelItems.add(new String[] { info.getPreferenceLabel(),
					info.getColorPreferenceKey(), info.getTextPreferenceKey(),
					info.getOverviewRulerPreferenceKey(),
					info.getHighlightPreferenceKey(),
					info.getVerticalRulerPreferenceKey(),
					info.getTextStylePreferenceKey() });
		}
		String[][] items = new String[listModelItems.size()][];
		listModelItems.toArray(items);
		return items;
	}

	private Control createTypingPage(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);
		String label;
		// label = PreferencesMessages
		// .getString("JavaEditorPreferencePage.overwriteMode");
		// //$NON-NLS-1$
		// addCheckBox(composite, label,
		// PreferenceConstants.EDITOR_DISABLE_OVERWRITE_MODE, 1);
		// addFiller(composite);
		//
		// label = PreferencesMessages
		// .getString("JavaEditorPreferencePage.smartHomeEnd");
		// //$NON-NLS-1$
		// addCheckBox(composite, label,
		// PreferenceConstants.EDITOR_SMART_HOME_END, 1);
		//
		// label = PreferencesMessages
		// .getString("JavaEditorPreferencePage.subWordNavigation");
		// //$NON-NLS-1$
		// addCheckBox(composite, label,
		// PreferenceConstants.EDITOR_SUB_WORD_NAVIGATION, 1);
		// addFiller(composite);
		Group group = new Group(composite, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setText(PreferencesMessages
				.getString("JavaEditorPreferencePage.typing.description")); //$NON-NLS-1$

		label = PreferencesMessages
				.getString("JavaEditorPreferencePage.wrapWords");//$NON-NLS-1$
		addCheckBox(group, label, PreferenceConstants.EDITOR_WRAP_WORDS, 1);

		label = PreferencesMessages
				.getString("JavaEditorPreferencePage.wrapStringsDQ");//$NON-NLS-1$
		Button button = addCheckBox(group, label,
				PreferenceConstants.EDITOR_WRAP_STRINGS_DQ, 1);

		label = PreferencesMessages
				.getString("JavaEditorPreferencePage.escapeStringsDQ");//$NON-NLS-1$
		fEscapeStringsButtonDQ = addCheckBox(group, label,
				PreferenceConstants.EDITOR_ESCAPE_STRINGS_DQ, 1);
		createDependency(button, fEscapeStringsButtonDQ);

		label = PreferencesMessages
				.getString("JavaEditorPreferencePage.wrapStringsSQ");//$NON-NLS-1$
		button = addCheckBox(group, label,
				PreferenceConstants.EDITOR_WRAP_STRINGS_SQ, 1);

		label = PreferencesMessages
				.getString("JavaEditorPreferencePage.escapeStringsSQ");
		//$NON-NLS-1$
		fEscapeStringsButtonSQ = addCheckBox(group, label,
				PreferenceConstants.EDITOR_ESCAPE_STRINGS_SQ, 1);
		createDependency(button, fEscapeStringsButtonSQ);

		label = PreferencesMessages
				.getString("JavaEditorPreferencePage.smartPaste");
		//$NON-NLS-1$
		addCheckBox(group, label, PreferenceConstants.EDITOR_SMART_PASTE, 1);

		label = PreferencesMessages
				.getString("JavaEditorPreferencePage.insertSpaceForTabs");
		//$NON-NLS-1$
		addCheckBox(group, label, PreferenceConstants.EDITOR_SPACES_FOR_TABS, 1);

		label = PreferencesMessages
				.getString("JavaEditorPreferencePage.closeStringsDQ");
		//$NON-NLS-1$
		addCheckBox(group, label,
				PreferenceConstants.EDITOR_CLOSE_STRINGS_DQ_PHP, 1);
		label = PreferencesMessages
				.getString("JavaEditorPreferencePage.closeStringsSQ");
		//$NON-NLS-1$
		addCheckBox(group, label,
				PreferenceConstants.EDITOR_CLOSE_STRINGS_SQ_PHP, 1);

		label = PreferencesMessages
				.getString("JavaEditorPreferencePage.closeBrackets");
		//$NON-NLS-1$
		addCheckBox(group, label,
				PreferenceConstants.EDITOR_CLOSE_BRACKETS_PHP, 1);

		// label = PreferencesMessages
		// .getString("JavaEditorPreferencePage.closeBraces");
		// //$NON-NLS-1$
		// addCheckBox(group, label, PreferenceConstants.EDITOR_CLOSE_BRACES,
		// 1);

		label = PreferencesMessages
				.getString("JavaEditorPreferencePage.closeJavaDocs");
		//$NON-NLS-1$
		button = addCheckBox(group, label,
				PreferenceConstants.EDITOR_CLOSE_JAVADOCS, 1);
		label = PreferencesMessages
				.getString("JavaEditorPreferencePage.formatJavaDocs");
		//$NON-NLS-1$
		button = addCheckBox(group, label,
				PreferenceConstants.EDITOR_FORMAT_JAVADOCS, 1);

		//
		// label = PreferencesMessages
		// .getString("JavaEditorPreferencePage.addJavaDocTags");
		// //$NON-NLS-1$
		// fAddJavaDocTagsButton = addCheckBox(group, label,
		// PreferenceConstants.EDITOR_ADD_JAVADOC_TAGS, 1);
		// createDependency(button, fAddJavaDocTagsButton);
		return composite;
	}

	private void addFiller(Composite composite) {
		Label filler = new Label(composite, SWT.LEFT);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		gd.heightHint = convertHeightInCharsToPixels(1) / 2;
		filler.setLayoutData(gd);
	}

	private static void indent(Control control) {
		GridData gridData = new GridData();
		gridData.horizontalIndent = 20;
		control.setLayoutData(gridData);
	}

	private static void createDependency(final Button master,
			final Control slave) {
		indent(slave);
		master.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				slave.setEnabled(master.getSelection());
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	private void addCompletionRadioButtons(Composite contentAssistComposite) {
		Composite completionComposite = new Composite(contentAssistComposite,
				SWT.NONE);
		GridData ccgd = new GridData();
		ccgd.horizontalSpan = 2;
		completionComposite.setLayoutData(ccgd);
		GridLayout ccgl = new GridLayout();
		ccgl.marginWidth = 0;
		ccgl.numColumns = 2;
		completionComposite.setLayout(ccgl);
		// SelectionListener completionSelectionListener= new SelectionAdapter()
		// {
		// public void widgetSelected(SelectionEvent e) {
		// boolean insert= fCompletionInsertsRadioButton.getSelection();
		// fOverlayStore.setValue(PreferenceConstants.CODEASSIST_INSERT_COMPLETION,
		// insert);
		// }
		// };
		//
		// fCompletionInsertsRadioButton= new Button(completionComposite,
		// SWT.RADIO
		// | SWT.LEFT);
		// fCompletionInsertsRadioButton.setText(PreferencesMessages.getString("JavaEditorPreferencePage.completionInserts"));
		// //$NON-NLS-1$
		// fCompletionInsertsRadioButton.setLayoutData(new GridData());
		// fCompletionInsertsRadioButton.addSelectionListener(completionSelectionListener);
		//
		// fCompletionOverwritesRadioButton= new Button(completionComposite,
		// SWT.RADIO | SWT.LEFT);
		// fCompletionOverwritesRadioButton.setText(PreferencesMessages.getString("JavaEditorPreferencePage.completionOverwrites"));
		// //$NON-NLS-1$
		// fCompletionOverwritesRadioButton.setLayoutData(new GridData());
		// fCompletionOverwritesRadioButton.addSelectionListener(completionSelectionListener);
	}

	private Control createNavigationPage(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		String text = PreferencesMessages
				.getString("JavaEditorPreferencePage.navigation.browserLikeLinks");
		//$NON-NLS-1$
		fBrowserLikeLinksCheckBox = addCheckBox(composite, text,
				PreferenceConstants.EDITOR_BROWSER_LIKE_LINKS, 0);
		fBrowserLikeLinksCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				boolean state = fBrowserLikeLinksCheckBox.getSelection();
				fBrowserLikeLinksKeyModifierText.setEnabled(state);
				handleBrowserLikeLinksKeyModifierModified();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		// Text field for modifier string
		text = PreferencesMessages
				.getString("JavaEditorPreferencePage.navigation.browserLikeLinksKeyModifier");
		//$NON-NLS-1$
		fBrowserLikeLinksKeyModifierText = addTextField(composite, text,
				PreferenceConstants.EDITOR_BROWSER_LIKE_LINKS_KEY_MODIFIER, 20,
				0, false);
		fBrowserLikeLinksKeyModifierText.setTextLimit(Text.LIMIT);

		if (computeStateMask(fOverlayStore
				.getString(PreferenceConstants.EDITOR_BROWSER_LIKE_LINKS_KEY_MODIFIER)) == -1) {
			// Fix possible illegal modifier string
			int stateMask = fOverlayStore
					.getInt(PreferenceConstants.EDITOR_BROWSER_LIKE_LINKS_KEY_MODIFIER_MASK);
			if (stateMask == -1)
				fBrowserLikeLinksKeyModifierText.setText(""); //$NON-NLS-1$
			else
				fBrowserLikeLinksKeyModifierText.setText(EditorUtility
						.getModifierString(stateMask));
		}
		fBrowserLikeLinksKeyModifierText.addKeyListener(new KeyListener() {
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
					String modifierString = fBrowserLikeLinksKeyModifierText
							.getText();
					Point selection = fBrowserLikeLinksKeyModifierText
							.getSelection();
					int i = selection.x - 1;
					while (i > -1
							&& Character.isWhitespace(modifierString.charAt(i))) {
						i--;
					}
					boolean needsPrefixDelimiter = i > -1
							&& !String.valueOf(modifierString.charAt(i))
									.equals(DELIMITER);

					i = selection.y;
					while (i < modifierString.length()
							&& Character.isWhitespace(modifierString.charAt(i))) {
						i++;
					}
					boolean needsPostfixDelimiter = i < modifierString.length()
							&& !String.valueOf(modifierString.charAt(i))
									.equals(DELIMITER);

					String insertString;

					if (needsPrefixDelimiter && needsPostfixDelimiter)
						insertString = PreferencesMessages
								.getFormattedString(
										"JavaEditorPreferencePage.navigation.insertDelimiterAndModifierAndDelimiter",
										new String[] { Action
												.findModifierString(e.stateMask) }); //$NON-NLS-1$
					else if (needsPrefixDelimiter)
						insertString = PreferencesMessages
								.getFormattedString(
										"JavaEditorPreferencePage.navigation.insertDelimiterAndModifier",
										new String[] { Action
												.findModifierString(e.stateMask) }); //$NON-NLS-1$
					else if (needsPostfixDelimiter)
						insertString = PreferencesMessages
								.getFormattedString(
										"JavaEditorPreferencePage.navigation.insertModifierAndDelimiter",
										new String[] { Action
												.findModifierString(e.stateMask) }); //$NON-NLS-1$
					else
						insertString = Action.findModifierString(e.stateMask);

					fBrowserLikeLinksKeyModifierText.insert(insertString);
				}
			}
		});

		fBrowserLikeLinksKeyModifierText
				.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						handleBrowserLikeLinksKeyModifierModified();
					}
				});
		return composite;
	}

	private void handleBrowserLikeLinksKeyModifierModified() {
		String modifiers = fBrowserLikeLinksKeyModifierText.getText();
		int stateMask = computeStateMask(modifiers);
		if (fBrowserLikeLinksCheckBox.getSelection()
				&& (stateMask == -1 || (stateMask & SWT.SHIFT) != 0)) {
			if (stateMask == -1)
				fBrowserLikeLinksKeyModifierStatus = new StatusInfo(
						IStatus.ERROR,
						PreferencesMessages
								.getFormattedString(
										"JavaEditorPreferencePage.navigation.modifierIsNotValid", modifiers)); //$NON-NLS-1$
			else
				fBrowserLikeLinksKeyModifierStatus = new StatusInfo(
						IStatus.ERROR,
						PreferencesMessages
								.getString("JavaEditorPreferencePage.navigation.shiftIsDisabled"));
			//$NON-NLS-1$
			setValid(false);
			StatusUtil.applyToStatusLine(this,
					fBrowserLikeLinksKeyModifierStatus);
		} else {
			fBrowserLikeLinksKeyModifierStatus = new StatusInfo();
			updateStatus(fBrowserLikeLinksKeyModifierStatus);
		}
	}

	private IStatus getBrowserLikeLinksKeyModifierStatus() {
		if (fBrowserLikeLinksKeyModifierStatus == null)
			fBrowserLikeLinksKeyModifierStatus = new StatusInfo();
		return fBrowserLikeLinksKeyModifierStatus;
	}

	/**
	 * Computes the state mask for the given modifier string.
	 * 
	 * @param modifiers
	 *            the string with the modifiers, separated by '+', '-', ';', ','
	 *            or '.'
	 * @return the state mask or -1 if the input is invalid
	 */
	private int computeStateMask(String modifiers) {
		if (modifiers == null)
			return -1;
		if (modifiers.length() == 0)
			return SWT.NONE;
		int stateMask = 0;
		StringTokenizer modifierTokenizer = new StringTokenizer(modifiers,
				",;.:+-* "); //$NON-NLS-1$
		while (modifierTokenizer.hasMoreTokens()) {
			int modifier = EditorUtility
					.findLocalizedModifier(modifierTokenizer.nextToken());
			if (modifier == 0 || (stateMask & modifier) == modifier)
				return -1;
			stateMask = stateMask | modifier;
		}
		return stateMask;
	}

	/*
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		initializeDefaultColors();
		fFoldingConfigurationBlock = new FoldingConfigurationBlock(
				fOverlayStore);
		fOverlayStore.load();
		fOverlayStore.start();
		TabFolder folder = new TabFolder(parent, SWT.NONE);
		folder.setLayout(new TabFolderLayout());
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText(PreferencesMessages
				.getString("JavaEditorPreferencePage.general")); //$NON-NLS-1$
		item.setControl(createAppearancePage(folder));
		item = new TabItem(folder, SWT.NONE);
		item.setText(PreferencesMessages
				.getString("JavaEditorPreferencePage.colors")); //$NON-NLS-1$
		item.setControl(createSyntaxPage(folder));

		// item = new TabItem(folder, SWT.NONE);
		// item.setText(PreferencesMessages
		// .getString("JavaEditorPreferencePage.annotationsTab.title"));
		// //$NON-NLS-1$
		// item.setControl(createAnnotationsPage(folder));
		item = new TabItem(folder, SWT.NONE);
		item.setText(PreferencesMessages
				.getString("JavaEditorPreferencePage.typing.tabTitle"));
		//$NON-NLS-1$
		item.setControl(createTypingPage(folder));

		item = new TabItem(folder, SWT.NONE);
		item.setText(PreferencesMessages
				.getString("JavaEditorPreferencePage.hoverTab.title"));
		//$NON-NLS-1$
		fJavaEditorHoverConfigurationBlock = new JavaEditorHoverConfigurationBlock(
				this, fOverlayStore);
		item.setControl(fJavaEditorHoverConfigurationBlock
				.createControl(folder));
		item = new TabItem(folder, SWT.NONE);
		item.setText(PreferencesMessages
				.getString("JavaEditorPreferencePage.navigationTab.title"));
		// //$NON-NLS-1$
		item.setControl(createNavigationPage(folder));
		item = new TabItem(folder, SWT.NONE);
		item.setText(PreferencesMessages
				.getString("JavaEditorPreferencePage.folding.title")); //$NON-NLS-1$
		item.setControl(fFoldingConfigurationBlock.createControl(folder));

		initialize();
		Dialog.applyDialogFont(folder);
		return folder;
	}

	private void initialize() {
		initializeFields();
		for (int i = 0; i < fSyntaxColorListModel.length; i++)
			fSyntaxColorList.add(fSyntaxColorListModel[i][0]);
		fSyntaxColorList.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (fSyntaxColorList != null && !fSyntaxColorList.isDisposed()) {
					fSyntaxColorList.select(0);
					handleSyntaxColorListSelection();
				}
			}
		});
		for (int i = 0; i < fAppearanceColorListModel.length; i++)
			fAppearanceColorList.add(fAppearanceColorListModel[i][0]);
		fAppearanceColorList.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (fAppearanceColorList != null
						&& !fAppearanceColorList.isDisposed()) {
					fAppearanceColorList.select(0);
					handleAppearanceColorListSelection();
				}
			}
		});
		// for (int i = 0; i < fAnnotationColorListModel.length; i++)
		// fAnnotationList.add(fAnnotationColorListModel[i][0]);
		// fAnnotationList.getDisplay().asyncExec(new Runnable() {
		// public void run() {
		// if (fAnnotationList != null && !fAnnotationList.isDisposed()) {
		// fAnnotationList.select(0);
		// handleAnnotationListSelection();
		// }
		// }
		// });
		// for (int i= 0; i < fContentAssistColorListModel.length; i++)
		// fContentAssistColorList.add(fContentAssistColorListModel[i][0]);
		// fContentAssistColorList.getDisplay().asyncExec(new Runnable() {
		// public void run() {
		// if (fContentAssistColorList != null &&
		// !fContentAssistColorList.isDisposed()) {
		// fContentAssistColorList.select(0);
		// handleContentAssistColorListSelection();
		// }
		// }
		// });
		fFoldingConfigurationBlock.initialize();
	}

	private void initializeFields() {
		Iterator e = fColorButtons.keySet().iterator();
		while (e.hasNext()) {
			ColorEditor c = (ColorEditor) e.next();
			String key = (String) fColorButtons.get(c);
			RGB rgb = PreferenceConverter.getColor(fOverlayStore, key);
			c.setColorValue(rgb);
		}
		e = fCheckBoxes.keySet().iterator();
		while (e.hasNext()) {
			Button b = (Button) e.next();
			String key = (String) fCheckBoxes.get(b);
			b.setSelection(fOverlayStore.getBoolean(key));
		}
		e = fTextFields.keySet().iterator();
		while (e.hasNext()) {
			Text t = (Text) e.next();
			String key = (String) fTextFields.get(t);
			t.setText(fOverlayStore.getString(key));
		}
		RGB rgb = PreferenceConverter.getColor(fOverlayStore,
				PreferenceConstants.EDITOR_BACKGROUND_COLOR);
		fBackgroundColorEditor.setColorValue(rgb);
		boolean default_ = fOverlayStore
				.getBoolean(PreferenceConstants.EDITOR_BACKGROUND_DEFAULT_COLOR);
		fBackgroundDefaultRadioButton.setSelection(default_);
		fBackgroundCustomRadioButton.setSelection(!default_);
		fBackgroundColorButton.setEnabled(!default_);
		boolean closeJavaDocs = fOverlayStore
				.getBoolean(PreferenceConstants.EDITOR_CLOSE_JAVADOCS);
		// fAddJavaDocTagsButton.setEnabled(closeJavaDocs);
		fEscapeStringsButtonDQ.setEnabled(fOverlayStore
				.getBoolean(PreferenceConstants.EDITOR_WRAP_STRINGS_DQ));
		fEscapeStringsButtonSQ.setEnabled(fOverlayStore
				.getBoolean(PreferenceConstants.EDITOR_WRAP_STRINGS_SQ));
		// boolean fillMethodArguments=
		// fOverlayStore.getBoolean(PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES);
		// fGuessMethodArgumentsButton.setEnabled(fillMethodArguments);
		// boolean completionInserts=
		// fOverlayStore.getBoolean(PreferenceConstants.CODEASSIST_INSERT_COMPLETION);
		// fCompletionInsertsRadioButton.setSelection(completionInserts);
		// fCompletionOverwritesRadioButton.setSelection(! completionInserts);
		//
		fBrowserLikeLinksKeyModifierText.setEnabled(fBrowserLikeLinksCheckBox
				.getSelection());
		// boolean markOccurrences=
		// fOverlayStore.getBoolean(PreferenceConstants.EDITOR_MARK_OCCURRENCES);
		// fStickyOccurrencesButton.setEnabled(markOccurrences);
		updateAutoactivationControls();
	}

	private void initializeDefaultColors() {
		if (!getPreferenceStore().contains(
				PreferenceConstants.EDITOR_BACKGROUND_COLOR)) {
			RGB rgb = getControl().getDisplay().getSystemColor(
					SWT.COLOR_LIST_BACKGROUND).getRGB();
			PreferenceConverter.setDefault(fOverlayStore,
					PreferenceConstants.EDITOR_BACKGROUND_COLOR, rgb);
			PreferenceConverter.setDefault(getPreferenceStore(),
					PreferenceConstants.EDITOR_BACKGROUND_COLOR, rgb);
		}
		if (!getPreferenceStore().contains(
				PreferenceConstants.EDITOR_FOREGROUND_COLOR)) {
			RGB rgb = getControl().getDisplay().getSystemColor(
					SWT.COLOR_LIST_FOREGROUND).getRGB();
			PreferenceConverter.setDefault(fOverlayStore,
					PreferenceConstants.EDITOR_FOREGROUND_COLOR, rgb);
			PreferenceConverter.setDefault(getPreferenceStore(),
					PreferenceConstants.EDITOR_FOREGROUND_COLOR, rgb);
		}
	}

	private void updateAutoactivationControls() {
		// boolean autoactivation=
		// fOverlayStore.getBoolean(PreferenceConstants.CODEASSIST_AUTOACTIVATION);
		// fAutoInsertDelayText.setEnabled(autoactivation);
		// fAutoInsertDelayLabel.setEnabled(autoactivation);
		// fAutoInsertJavaTriggerText.setEnabled(autoactivation);
		// fAutoInsertJavaTriggerLabel.setEnabled(autoactivation);
		//
		// fAutoInsertJavaDocTriggerText.setEnabled(autoactivation);
		// fAutoInsertJavaDocTriggerLabel.setEnabled(autoactivation);
	}

	/*
	 * @see PreferencePage#performOk()
	 */
	public boolean performOk() {
		// fJavaEditorHoverConfigurationBlock.performOk();
		fFoldingConfigurationBlock.performOk();
		fOverlayStore
				.setValue(
						PreferenceConstants.EDITOR_BROWSER_LIKE_LINKS_KEY_MODIFIER_MASK,
						computeStateMask(fBrowserLikeLinksKeyModifierText
								.getText()));
		fOverlayStore.propagate();
		PHPeclipsePlugin.getDefault().savePluginPreferences();
		return true;
	}

	/*
	 * @see PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		fOverlayStore.loadDefaults();
		initializeFields();
		handleSyntaxColorListSelection();
		handleAppearanceColorListSelection();
		// handleAnnotationListSelection();
		// handleContentAssistColorListSelection();
		// fJavaEditorHoverConfigurationBlock.performDefaults();
		fFoldingConfigurationBlock.performDefaults();
		super.performDefaults();
		fPreviewViewer.invalidateTextPresentation();
	}

	/*
	 * @see DialogPage#dispose()
	 */
	public void dispose() {
		fFoldingConfigurationBlock.dispose();

		if (fJavaTextTools != null) {
			fJavaTextTools.dispose();
			fJavaTextTools = null;
		}
		if (fOverlayStore != null) {
			fOverlayStore.stop();
			fOverlayStore = null;
		}
		if (fBackgroundColor != null && !fBackgroundColor.isDisposed())
			fBackgroundColor.dispose();
		super.dispose();
	}

	private Button addCheckBox(Composite parent, String label, String key,
			int indentation) {
		Button checkBox = new Button(parent, SWT.CHECK);
		checkBox.setText(label);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent = indentation;
		gd.horizontalSpan = 2;
		checkBox.setLayoutData(gd);
		checkBox.addSelectionListener(fCheckBoxListener);
		fCheckBoxes.put(checkBox, key);
		return checkBox;
	}

	private Text addTextField(Composite composite, String label, String key,
			int textLimit, int indentation, boolean isNumber) {
		return getTextControl(addLabelledTextField(composite, label, key,
				textLimit, indentation, isNumber));
	}

	private static Label getLabelControl(Control[] labelledTextField) {
		return (Label) labelledTextField[0];
	}

	private static Text getTextControl(Control[] labelledTextField) {
		return (Text) labelledTextField[1];
	}

	/**
	 * Returns an array of size 2: - first element is of type <code>Label</code>-
	 * second element is of type <code>Text</code> Use
	 * <code>getLabelControl</code> and <code>getTextControl</code> to get
	 * the 2 controls.
	 */
	private Control[] addLabelledTextField(Composite composite, String label,
			String key, int textLimit, int indentation, boolean isNumber) {
		Label labelControl = new Label(composite, SWT.NONE);
		labelControl.setText(label);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent = indentation;
		labelControl.setLayoutData(gd);
		Text textControl = new Text(composite, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.widthHint = convertWidthInCharsToPixels(textLimit + 1);
		textControl.setLayoutData(gd);
		textControl.setTextLimit(textLimit);
		fTextFields.put(textControl, key);
		if (isNumber) {
			fNumberFields.add(textControl);
			textControl.setText("0");
			textControl.addModifyListener(fNumberFieldListener);
		} else {
			textControl.addModifyListener(fTextFieldListener);
		}
		return new Control[] { labelControl, textControl };
	}

	private String loadPreviewContentFromFile(String filename) {
		String line;
		String separator = System.getProperty("line.separator"); //$NON-NLS-1$
		StringBuffer buffer = new StringBuffer(512);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(getClass()
					.getResourceAsStream(filename)));
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
				buffer.append(separator);
			}
		} catch (IOException io) {
			PHPeclipsePlugin.log(io);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
		return buffer.toString();
	}

	private void numberFieldChanged(Text textControl) {
		String number = textControl.getText();
		IStatus status = validatePositiveNumber(number);
		if (!status.matches(IStatus.ERROR))
			fOverlayStore.setValue((String) fTextFields.get(textControl),
					number);
		updateStatus(status);
	}

	private IStatus validatePositiveNumber(String number) {
		StatusInfo status = new StatusInfo();
		if (number.length() == 0) {
			status.setError(PreferencesMessages
					.getString("JavaEditorPreferencePage.empty_input")); //$NON-NLS-1$
		} else {
			try {
				int value = Integer.parseInt(number);
				if (value < 0)
					status.setError(PreferencesMessages.getFormattedString(
							"JavaEditorPreferencePage.invalid_input", number)); //$NON-NLS-1$
			} catch (NumberFormatException e) {
				status.setError(PreferencesMessages.getFormattedString(
						"JavaEditorPreferencePage.invalid_input", number)); //$NON-NLS-1$
			}
		}
		return status;
	}

	void updateStatus(IStatus status) {
		if (!status.matches(IStatus.ERROR)) {
			for (int i = 0; i < fNumberFields.size(); i++) {
				Text text = (Text) fNumberFields.get(i);
				IStatus s = validatePositiveNumber(text.getText());
				status = StatusUtil.getMoreSevere(s, status);
			}
		}
		// status=
		// StatusUtil.getMoreSevere(fJavaEditorHoverConfigurationBlock.getStatus(),
		// status);
		// status=
		// StatusUtil.getMoreSevere(getBrowserLikeLinksKeyModifierStatus(),
		// status);
		setValid(!status.matches(IStatus.ERROR));
		StatusUtil.applyToStatusLine(this, status);
	}
}