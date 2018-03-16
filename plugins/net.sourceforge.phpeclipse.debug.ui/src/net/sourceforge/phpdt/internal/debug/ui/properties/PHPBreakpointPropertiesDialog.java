package net.sourceforge.phpdt.internal.debug.ui.properties;

import net.sourceforge.phpdt.internal.debug.core.breakpoints.PHPLineBreakpoint;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.actions.StatusInfo;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

public class PHPBreakpointPropertiesDialog extends StatusDialog {

	private PHPLineBreakpoint fBreakpoint;

	private SourceViewer fSnippetViewer;

	private Button fCheckBox;

	private Spinner fSpinner;

	public PHPBreakpointPropertiesDialog(Shell parentShell, PHPLineBreakpoint bp) {
		super(parentShell);

		fBreakpoint = bp;
	}

	protected Control createDialogArea(Composite parent) {
		Composite container;
		GridLayout layout;
		GridData gd;
		IDocument document;
		Control control;
		Label label;
		Spinner spinner;
		String condition = "";
		boolean enabled = false;
		int hitCount = 0;

		try {
			condition = fBreakpoint.getCondition();
			enabled = fBreakpoint.isConditionEnabled();
			hitCount = fBreakpoint.getHitCount();
		} catch (CoreException e) {
		}

		Font font = parent.getFont(); // Get the dialog's font
		container = new Composite(parent, SWT.NONE); // Create a new
														// container for our
														// controls
		layout = new GridLayout(); // Create a grid for control layouting

		container.setLayout(layout); // Set the grid to the container
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		container.setLayoutData(gd);

		label = new Label(container, SWT.NONE); // spinner label
		label.setText("Skip count"); // $NON-NLS-1$

		gd = new GridData(SWT.BEGINNING); // Left align of label text
		label.setLayoutData(gd); // 
		label.setFont(font); // Set the label's font

		fSpinner = new Spinner(container, SWT.BORDER);
		fSpinner.setMinimum(0);
		fSpinner.setMaximum(100000);
		fSpinner.setIncrement(1);
		fSpinner.setPageIncrement(100);
		fSpinner.setSelection(hitCount);
		gd = new GridData(SWT.BEGINNING);
		label.setLayoutData(gd); // 
		label.setFont(font); // Set the label's font

		label = new Label(container, SWT.NONE); // snippet label
		label.setText("Break Condition"); // $NON-NLS-1$

		gd = new GridData(SWT.BEGINNING); // Left align of label text
		label.setLayoutData(gd); // 
		label.setFont(font); // Set the label's font

		fSnippetViewer = new SourceViewer(container, null, SWT.BORDER
				| SWT.V_SCROLL | SWT.H_SCROLL);
		fSnippetViewer.setInput(this);

		document = new Document();

		// IDocumentPartitioner partitioner= new RuleBasedPartitioner(...);
		// document.setDocumentPartitioner(partitioner);
		// partitioner.connect(document);

		fSnippetViewer.configure(new SourceViewerConfiguration());
		fSnippetViewer.setEditable(true);
		fSnippetViewer.setDocument(document);

		document.addDocumentListener(new IDocumentListener() {
			public void documentAboutToBeChanged(DocumentEvent event) {
			}

			public void documentChanged(DocumentEvent event) {
				checkValues();
			}
		});

		fSnippetViewer.getTextWidget().setFont(JFaceResources.getTextFont());

		control = fSnippetViewer.getControl();
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = convertHeightInCharsToPixels(10);
		gd.widthHint = convertWidthInCharsToPixels(80);

		control.setLayoutData(gd);
		fSnippetViewer.getDocument().set(condition);

		// enable checkbox
		fCheckBox = new Button(container, SWT.CHECK | SWT.LEFT);
		fCheckBox.setText("Enable Condition"); //$NON-NLS-1$
		fCheckBox.setSelection(enabled);
		fCheckBox.setFont(font);

		applyDialogFont(container);
		fSnippetViewer.getControl().setFocus();

		checkValues();

		return container;
	}

	protected void okPressed() {
		try {
			fBreakpoint.setCondition(fSnippetViewer.getDocument().get());
			fBreakpoint.setConditionEnabled(fCheckBox.getSelection());
			fBreakpoint.setHitCount(fSpinner.getSelection());

			int id = fBreakpoint.getChangeID();
			id++;
			fBreakpoint.setChangeID(id);
		} catch (CoreException e) {
		}

		super.okPressed();
	}

	/**
	 * Check the field values and display a message in the status if needed.
	 */
	private void checkValues() {
		StatusInfo status;

		status = new StatusInfo();
		/*
		 * StatusInfo status = new StatusInfo(); if
		 * (fSnippetViewer.getDocument().get().trim().length() == 0) {
		 * status.setError(ActionMessages.WatchExpressionDialog_4);
		 * //$NON-NLS-1$ }
		 */
		updateStatus(status);
	}
}
