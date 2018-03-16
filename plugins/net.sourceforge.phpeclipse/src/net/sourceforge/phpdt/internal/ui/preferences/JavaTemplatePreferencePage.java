/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.phpdt.internal.ui.preferences;

import net.sourceforge.phpdt.internal.ui.IJavaHelpContextIds;
import net.sourceforge.phpdt.internal.ui.text.IPHPPartitions;
import net.sourceforge.phpdt.internal.ui.text.template.preferences.TemplateVariableProcessor;
import net.sourceforge.phpdt.ui.PreferenceConstants;
import net.sourceforge.phpdt.ui.text.JavaTextTools;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;
import net.sourceforge.phpeclipse.phpeditor.JavaSourceViewer;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;

public class JavaTemplatePreferencePage extends TemplatePreferencePage
		implements IWorkbenchPreferencePage {

	private TemplateVariableProcessor fTemplateProcessor;

	public JavaTemplatePreferencePage() {
		setPreferenceStore(PHPeclipsePlugin.getDefault().getPreferenceStore());
		setTemplateStore(PHPeclipsePlugin.getDefault().getTemplateStore());
		setContextTypeRegistry(PHPeclipsePlugin.getDefault()
				.getTemplateContextRegistry());
		fTemplateProcessor = new TemplateVariableProcessor();
	}

	/*
	 * @see PreferencePage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
				IJavaHelpContextIds.JAVA_EDITOR_PREFERENCE_PAGE);
	}

	/*
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		boolean ok = super.performOk();

		PHPeclipsePlugin.getDefault().savePluginPreferences();

		return ok;
	}

	/*
	 * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage#getFormatterPreferenceKey()
	 */
	protected String getFormatterPreferenceKey() {
		return PreferenceConstants.TEMPLATES_USE_CODEFORMATTER;
	}

	/*
	 * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage#createTemplateEditDialog(org.eclipse.jface.text.templates.Template,
	 *      boolean, boolean)
	 */
	protected Dialog createTemplateEditDialog(Template template, boolean edit,
			boolean isNameModifiable) {
		return new EditTemplateDialog(getShell(), template, edit,
				isNameModifiable, getContextTypeRegistry());
	}

	/*
	 * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage#createViewer(org.eclipse.swt.widgets.Composite)
	 */
	protected SourceViewer createViewer(Composite parent) {
		GridData data = new GridData();
		IDocument document = new Document();
		JavaTextTools tools = PHPeclipsePlugin.getDefault().getJavaTextTools();
		tools.setupJavaDocumentPartitioner(document,
				IPHPPartitions.PHP_PARTITIONING);
		IPreferenceStore store = PHPeclipsePlugin.getDefault()
				.getCombinedPreferenceStore();
		SourceViewer viewer = new JavaSourceViewer(parent, null, null, false,
				SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL, store);
		TemplateEditorSourceViewerConfiguration configuration = new TemplateEditorSourceViewerConfiguration(
				tools.getColorManager(), store, null, fTemplateProcessor);
		viewer.configure(configuration);
		viewer.setEditable(false);
		viewer.setDocument(document);

		Font font = JFaceResources
				.getFont(PreferenceConstants.EDITOR_TEXT_FONT);
		viewer.getTextWidget().setFont(font);
		new JavaSourcePreviewerUpdater(viewer, configuration, store);

		Control control = viewer.getControl();
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.FILL_VERTICAL);
		control.setLayoutData(data);

		return viewer;
	}

	/*
	 * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage#updateViewerInput()
	 */
	protected void updateViewerInput() {
		IStructuredSelection selection = (IStructuredSelection) getTableViewer()
				.getSelection();
		SourceViewer viewer = getViewer();

		if (selection.size() == 1
				&& selection.getFirstElement() instanceof TemplatePersistenceData) {
			TemplatePersistenceData data = (TemplatePersistenceData) selection
					.getFirstElement();
			Template template = data.getTemplate();
			String contextId = template.getContextTypeId();
			TemplateContextType type = PHPeclipsePlugin.getDefault()
					.getTemplateContextRegistry().getContextType(contextId);
			fTemplateProcessor.setContextType(type);

			IDocument doc = viewer.getDocument();

			String start = null;
			if ("javadoc".equals(contextId)) { //$NON-NLS-1$
				start = "/**" + doc.getLegalLineDelimiters()[0]; //$NON-NLS-1$
			} else
				start = ""; //$NON-NLS-1$

			doc.set(start + template.getPattern());
			int startLen = start.length();
			viewer.setDocument(doc, startLen, doc.getLength() - startLen);

		} else {
			viewer.getDocument().set(""); //$NON-NLS-1$
		}
	}
}
