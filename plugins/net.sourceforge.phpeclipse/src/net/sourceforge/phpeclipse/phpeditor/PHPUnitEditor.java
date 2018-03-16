package net.sourceforge.phpeclipse.phpeditor;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.phpdt.core.ICompilationUnit;
import net.sourceforge.phpdt.core.IJavaElement;
import net.sourceforge.phpdt.core.IJavaProject;
import net.sourceforge.phpdt.core.IMember;
import net.sourceforge.phpdt.core.ISourceRange;
import net.sourceforge.phpdt.core.ISourceReference;
import net.sourceforge.phpdt.core.JavaCore;
import net.sourceforge.phpdt.core.JavaModelException;
import net.sourceforge.phpdt.core.dom.CompilationUnit;
import net.sourceforge.phpdt.internal.compiler.parser.Scanner;
import net.sourceforge.phpdt.internal.corext.codemanipulation.StubUtility;
import net.sourceforge.phpdt.internal.ui.actions.AddBlockCommentAction;
import net.sourceforge.phpdt.internal.ui.actions.CompositeActionGroup;
import net.sourceforge.phpdt.internal.ui.actions.IndentAction;
import net.sourceforge.phpdt.internal.ui.actions.RemoveBlockCommentAction;
import net.sourceforge.phpdt.internal.ui.text.ContentAssistPreference;
import net.sourceforge.phpdt.internal.ui.text.IPHPPartitions;
import net.sourceforge.phpdt.internal.ui.text.JavaHeuristicScanner;
import net.sourceforge.phpdt.internal.ui.text.JavaIndenter;
import net.sourceforge.phpdt.internal.ui.text.PHPPairMatcher;
import net.sourceforge.phpdt.internal.ui.text.SmartBackspaceManager;
import net.sourceforge.phpdt.internal.ui.text.SmartSemicolonAutoEditStrategy;
import net.sourceforge.phpdt.internal.ui.text.comment.CommentFormattingContext;
import net.sourceforge.phpdt.internal.ui.text.java.IJavaReconcilingListener;
import net.sourceforge.phpdt.internal.ui.text.link.LinkedPositionManager;
import net.sourceforge.phpdt.internal.ui.text.link.LinkedPositionUI;
import net.sourceforge.phpdt.internal.ui.text.link.LinkedPositionUI.ExitFlags;
import net.sourceforge.phpdt.ui.IWorkingCopyManager;
import net.sourceforge.phpdt.ui.PreferenceConstants;
import net.sourceforge.phpdt.ui.actions.GenerateActionGroup;
import net.sourceforge.phpdt.ui.text.JavaTextTools;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;
import net.sourceforge.phpeclipse.phpeditor.actions.RTrimAction;
import net.sourceforge.phpeclipse.ui.editor.ShowExternalPreviewAction;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ILineTracker;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.IWidgetTokenKeeper;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IFormattingContext;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.editors.text.IStorageDocumentProvider;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.TextOperationAction;

/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corp. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors: IBM Corporation - Initial implementation
 * www.phpeclipse.de
 ******************************************************************************/
/**
 * PHP specific text editor.
 */
public class PHPUnitEditor extends PHPEditor { // implements
	// IJavaReconcilingListener {
	interface ITextConverter {
		void customizeDocumentCommand(IDocument document,
				DocumentCommand command);
	};

	// class AdaptedSourceViewer extends JavaSourceViewer {
	// private List fTextConverters;
	//
	// private boolean fIgnoreTextConverters = false;
	//
	// // private JavaCorrectionAssistant fCorrectionAssistant;
	// public AdaptedSourceViewer(Composite parent, IVerticalRuler
	// verticalRuler,
	// IOverviewRuler overviewRuler, boolean showAnnotationsOverview,
	// int styles, IPreferenceStore store) {
	// super(parent, verticalRuler, overviewRuler, showAnnotationsOverview,
	// styles, store);
	// }
	//
	// // public AdaptedSourceViewer(Composite parent,
	// // IVerticalRuler verticalRuler, IOverviewRuler overviewRuler,
	// // boolean showAnnotationsOverview, int styles) {
	// // super(parent, verticalRuler, overviewRuler,
	// // showAnnotationsOverview, styles);
	// // }
	// public IContentAssistant getContentAssistant() {
	// return fContentAssistant;
	// }
	//
	// /*
	// * @see ITextOperationTarget#doOperation(int)
	// */
	// public void doOperation(int operation) {
	// if (getTextWidget() == null)
	// return;
	// switch (operation) {
	// case CONTENTASSIST_PROPOSALS:
	// String msg = fContentAssistant.showPossibleCompletions();
	// setStatusLineErrorMessage(msg);
	// return;
	// // case CORRECTIONASSIST_PROPOSALS:
	// // fCorrectionAssistant.showPossibleCompletions();
	// // return;
	// case UNDO:
	// fIgnoreTextConverters = true;
	// break;
	// case REDO:
	// fIgnoreTextConverters = true;
	// break;
	// }
	// super.doOperation(operation);
	// }
	//
	// /*
	// * @see ITextOperationTarget#canDoOperation(int)
	// */
	// public boolean canDoOperation(int operation) {
	// // if (operation == CORRECTIONASSIST_PROPOSALS)
	// // return isEditable();
	// return super.canDoOperation(operation);
	// }
	//
	// /*
	// * @see TextViewer#handleDispose()
	// */
	// protected void handleDispose() {
	// // if (fCorrectionAssistant != null) {
	// // fCorrectionAssistant.uninstall();
	// // fCorrectionAssistant= null;
	// // }
	// super.handleDispose();
	// }
	//
	// public void insertTextConverter(ITextConverter textConverter, int index)
	// {
	// throw new UnsupportedOperationException();
	// }
	//
	// public void addTextConverter(ITextConverter textConverter) {
	// if (fTextConverters == null) {
	// fTextConverters = new ArrayList(1);
	// fTextConverters.add(textConverter);
	// } else if (!fTextConverters.contains(textConverter))
	// fTextConverters.add(textConverter);
	// }
	//
	// public void removeTextConverter(ITextConverter textConverter) {
	// if (fTextConverters != null) {
	// fTextConverters.remove(textConverter);
	// if (fTextConverters.size() == 0)
	// fTextConverters = null;
	// }
	// }
	//
	// /*
	// * @see TextViewer#customizeDocumentCommand(DocumentCommand)
	// */
	// protected void customizeDocumentCommand(DocumentCommand command) {
	// super.customizeDocumentCommand(command);
	// if (!fIgnoreTextConverters && fTextConverters != null) {
	// for (Iterator e = fTextConverters.iterator(); e.hasNext();)
	// ((ITextConverter) e.next()).customizeDocumentCommand(getDocument(),
	// command);
	// }
	// fIgnoreTextConverters = false;
	// }
	//
	// // http://dev.eclipse.org/bugs/show_bug.cgi?id=19270
	// public void updateIndentationPrefixes() {
	// SourceViewerConfiguration configuration = getSourceViewerConfiguration();
	// String[] types = configuration.getConfiguredContentTypes(this);
	// for (int i = 0; i < types.length; i++) {
	// String[] prefixes = configuration.getIndentPrefixes(this, types[i]);
	// if (prefixes != null && prefixes.length > 0)
	// setIndentPrefixes(prefixes, types[i]);
	// }
	// }
	//
	// /*
	// * @see IWidgetTokenOwner#requestWidgetToken(IWidgetTokenKeeper)
	// */
	// public boolean requestWidgetToken(IWidgetTokenKeeper requester) {
	// if (WorkbenchHelp.isContextHelpDisplayed())
	// return false;
	// return super.requestWidgetToken(requester);
	// }
	//
	// // /*
	// // * @see
	// org.eclipse.jface.text.source.ISourceViewer#configure(org.eclipse.jface.text.source.SourceViewerConfiguration)
	// // */
	// // public void configure(SourceViewerConfiguration configuration) {
	// // super.configure(configuration);
	// // // fCorrectionAssistant= new
	// // // JavaCorrectionAssistant(CompilationUnitEditor.this);
	// // // fCorrectionAssistant.install(this);
	// // //TODO install SmartBracesAutoEditStrategy
	// // // prependAutoEditStrategy(new SmartBracesAutoEditStrategy(this),
	// // // IDocument.DEFAULT_CONTENT_TYPE);
	// // }
	// public void configure(SourceViewerConfiguration configuration) {
	// super.configure(configuration);
	// // fCorrectionAssistant= new
	// JavaCorrectionAssistant(CompilationUnitEditor.this);
	// // fCorrectionAssistant.install(this);
	// IAutoEditStrategy smartSemi= new
	// SmartSemicolonAutoEditStrategy(IPHPPartitions.PHP_PARTITIONING);
	// prependAutoEditStrategy(smartSemi, IDocument.DEFAULT_CONTENT_TYPE);
	// prependAutoEditStrategy(smartSemi, IPHPPartitions.PHP_STRING_DQ);
	// prependAutoEditStrategy(smartSemi, IPHPPartitions.PHP_STRING_SQ);
	// // prependAutoEditStrategy(smartSemi, IPHPPartitions.JAVA_CHARACTER);
	// }
	// };
	class AdaptedSourceViewer extends JavaSourceViewer {

		private List fTextConverters;

		private boolean fIgnoreTextConverters = false;

		// private JavaCorrectionAssistant fCorrectionAssistant;

		public AdaptedSourceViewer(Composite parent,
				IVerticalRuler verticalRuler, IOverviewRuler overviewRuler,
				boolean showAnnotationsOverview, int styles,
				IPreferenceStore store) {
			super(parent, verticalRuler, overviewRuler,
					showAnnotationsOverview, styles, store);
		}

		public IContentAssistant getContentAssistant() {
			return fContentAssistant;
		}

		/*
		 * @see ITextOperationTarget#doOperation(int)
		 */
		public void doOperation(int operation) {

			if (getTextWidget() == null)
				return;

			switch (operation) {
			case CONTENTASSIST_PROPOSALS:
				String msg = fContentAssistant.showPossibleCompletions();
				setStatusLineErrorMessage(msg);
				return;
				// case CORRECTIONASSIST_PROPOSALS:
				// msg = fCorrectionAssistant.showPossibleCompletions();
				// setStatusLineErrorMessage(msg);
				// return;
			case UNDO:
				fIgnoreTextConverters = true;
				super.doOperation(operation);
				fIgnoreTextConverters = false;
				return;
			case REDO:
				fIgnoreTextConverters = true;
				super.doOperation(operation);
				fIgnoreTextConverters = false;
				return;
			}

			super.doOperation(operation);
		}

		/*
		 * @see ITextOperationTarget#canDoOperation(int)
		 */
		public boolean canDoOperation(int operation) {
			// if (operation == CORRECTIONASSIST_PROPOSALS)
			// return isEditable();

			return super.canDoOperation(operation);
		}

		/*
		 * @see org.eclipse.jface.text.source.ISourceViewerExtension2#unconfigure()
		 * @since 3.0
		 */
		public void unconfigure() {
			// if (fCorrectionAssistant != null) {
			// fCorrectionAssistant.uninstall();
			// fCorrectionAssistant = null;
			// }
			super.unconfigure();
		}

		public void insertTextConverter(ITextConverter textConverter, int index) {
			throw new UnsupportedOperationException();
		}

		public void addTextConverter(ITextConverter textConverter) {
			if (fTextConverters == null) {
				fTextConverters = new ArrayList(1);
				fTextConverters.add(textConverter);
			} else if (!fTextConverters.contains(textConverter))
				fTextConverters.add(textConverter);
		}

		public void removeTextConverter(ITextConverter textConverter) {
			if (fTextConverters != null) {
				fTextConverters.remove(textConverter);
				if (fTextConverters.size() == 0)
					fTextConverters = null;
			}
		}

		/*
		 * @see TextViewer#customizeDocumentCommand(DocumentCommand)
		 */
		protected void customizeDocumentCommand(DocumentCommand command) {
			super.customizeDocumentCommand(command);
			if (!fIgnoreTextConverters && fTextConverters != null) {
				for (Iterator e = fTextConverters.iterator(); e.hasNext();)
					((ITextConverter) e.next()).customizeDocumentCommand(
							getDocument(), command);
			}
		}

		// http://dev.eclipse.org/bugs/show_bug.cgi?id=19270
		public void updateIndentationPrefixes() {
			SourceViewerConfiguration configuration = getSourceViewerConfiguration();
			String[] types = configuration.getConfiguredContentTypes(this);
			for (int i = 0; i < types.length; i++) {
				String[] prefixes = configuration.getIndentPrefixes(this,
						types[i]);
				if (prefixes != null && prefixes.length > 0)
					setIndentPrefixes(prefixes, types[i]);
			}
		}

		/*
		 * @see IWidgetTokenOwner#requestWidgetToken(IWidgetTokenKeeper)
		 */
		public boolean requestWidgetToken(IWidgetTokenKeeper requester) {
			if (PlatformUI.getWorkbench().getHelpSystem()
					.isContextHelpDisplayed())
				return false;
			return super.requestWidgetToken(requester);
		}

		/*
		 * @see IWidgetTokenOwnerExtension#requestWidgetToken(IWidgetTokenKeeper,
		 *      int)
		 * @since 3.0
		 */
		public boolean requestWidgetToken(IWidgetTokenKeeper requester,
				int priority) {
			if (PlatformUI.getWorkbench().getHelpSystem()
					.isContextHelpDisplayed())
				return false;
			return super.requestWidgetToken(requester, priority);
		}

		/*
		 * @see org.eclipse.jface.text.source.ISourceViewer#configure(org.eclipse.jface.text.source.SourceViewerConfiguration)
		 */
		public void configure(SourceViewerConfiguration configuration) {
			super.configure(configuration);
			// fCorrectionAssistant = new
			// JavaCorrectionAssistant(CompilationUnitEditor.this);
			// fCorrectionAssistant.install(this);
			IAutoEditStrategy smartSemi = new SmartSemicolonAutoEditStrategy(
					IPHPPartitions.PHP_PARTITIONING);
			prependAutoEditStrategy(smartSemi, IDocument.DEFAULT_CONTENT_TYPE);
			prependAutoEditStrategy(smartSemi, IPHPPartitions.PHP_STRING_DQ);
			prependAutoEditStrategy(smartSemi, IPHPPartitions.PHP_STRING_SQ);
			prependAutoEditStrategy(smartSemi,
					IPHPPartitions.PHP_STRING_HEREDOC);
		}

		/*
		 * @see org.eclipse.jface.text.source.SourceViewer#createFormattingContext()
		 * @since 3.0
		 */
		public IFormattingContext createFormattingContext() {
			IFormattingContext context = new CommentFormattingContext();

			Map preferences;
			IJavaElement inputJavaElement = getInputJavaElement();
			IJavaProject javaProject = inputJavaElement != null ? inputJavaElement
					.getJavaProject()
					: null;
			if (javaProject == null)
				preferences = new HashMap(JavaCore.getOptions());
			else
				preferences = new HashMap(javaProject.getOptions(true));

			context.storeToMap(PreferenceConstants.getPreferenceStore(),
					preferences, false);
			context.setProperty(
					FormattingContextProperties.CONTEXT_PREFERENCES,
					preferences);

			return context;
		}
	}

	/**
	 * Remembers data related to the current selection to be able to restore it
	 * later.
	 * 
	 * @since 3.0
	 */
	private class RememberedSelection {
		/** The remembered selection start. */
		private RememberedOffset fStartOffset = new RememberedOffset();

		/** The remembered selection end. */
		private RememberedOffset fEndOffset = new RememberedOffset();

		/**
		 * Remember current selection.
		 */
		public void remember() {
			/*
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=52257 This method
			 * may be called inside an async call posted to the UI thread, so
			 * protect against intermediate disposal of the editor.
			 */
			ISourceViewer viewer = getSourceViewer();
			if (viewer != null) {
				IRegion selection = getSignedSelection(viewer);
				int startOffset = selection.getOffset();
				int endOffset = startOffset + selection.getLength();

				fStartOffset.setOffset(startOffset);
				fEndOffset.setOffset(endOffset);
			}
		}

		/**
		 * Restore remembered selection.
		 */
		public void restore() {
			/*
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=52257 This method
			 * may be called inside an async call posted to the UI thread, so
			 * protect against intermediate disposal of the editor.
			 */
			if (getSourceViewer() == null)
				return;

			try {

				int startOffset, endOffset;
				int revealStartOffset, revealEndOffset;
				if (showsHighlightRangeOnly()) {
					IJavaElement newStartElement = fStartOffset.getElement();
					startOffset = fStartOffset
							.getRememberedOffset(newStartElement);
					revealStartOffset = fStartOffset.getRevealOffset(
							newStartElement, startOffset);
					if (revealStartOffset == -1)
						startOffset = -1;

					IJavaElement newEndElement = fEndOffset.getElement();
					endOffset = fEndOffset.getRememberedOffset(newEndElement);
					revealEndOffset = fEndOffset.getRevealOffset(newEndElement,
							endOffset);
					if (revealEndOffset == -1)
						endOffset = -1;
				} else {
					startOffset = fStartOffset.getOffset();
					revealStartOffset = startOffset;
					endOffset = fEndOffset.getOffset();
					revealEndOffset = endOffset;
				}

				if (startOffset == -1) {
					startOffset = endOffset; // fallback to caret offset
					revealStartOffset = revealEndOffset;
				}

				if (endOffset == -1) {
					endOffset = startOffset; // fallback to other offset
					revealEndOffset = revealStartOffset;
				}

				IJavaElement element;
				if (endOffset == -1) {
					// fallback to element selection
					element = fEndOffset.getElement();
					if (element == null)
						element = fStartOffset.getElement();
					if (element != null)
						setSelection(element);
					return;
				}

				if (isValidSelection(revealStartOffset, revealEndOffset
						- revealStartOffset)
						&& isValidSelection(startOffset, endOffset
								- startOffset))
					selectAndReveal(startOffset, endOffset - startOffset,
							revealStartOffset, revealEndOffset
									- revealStartOffset);
			} finally {
				fStartOffset.clear();
				fEndOffset.clear();
			}
		}

		private boolean isValidSelection(int offset, int length) {
			IDocumentProvider provider = getDocumentProvider();
			if (provider != null) {
				IDocument document = provider.getDocument(getEditorInput());
				if (document != null) {
					int end = offset + length;
					int documentLength = document.getLength();
					return 0 <= offset && offset <= documentLength && 0 <= end
							&& end <= documentLength;
				}
			}
			return false;
		}

	}

	/**
	 * Remembers additional data for a given offset to be able restore it later.
	 * 
	 * @since 3.0
	 */
	private class RememberedOffset {
		/** Remembered line for the given offset */
		private int fLine;

		/** Remembered column for the given offset */
		private int fColumn;

		/** Remembered Java element for the given offset */
		private IJavaElement fElement;

		/** Remembered Java element line for the given offset */
		private int fElementLine;

		/**
		 * Store visual properties of the given offset.
		 * 
		 * @param offset
		 *            Offset in the document
		 */
		public void setOffset(int offset) {
			try {
				IDocument document = getSourceViewer().getDocument();
				fLine = document.getLineOfOffset(offset);
				fColumn = offset - document.getLineOffset(fLine);
				fElement = getElementAt(offset, true);

				fElementLine = -1;
				if (fElement instanceof IMember) {
					ISourceRange range = ((IMember) fElement).getNameRange();
					if (range != null)
						fElementLine = document.getLineOfOffset(range
								.getOffset());
				}
				if (fElementLine == -1)
					fElementLine = document
							.getLineOfOffset(getOffset(fElement));
			} catch (BadLocationException e) {
				// should not happen
				PHPeclipsePlugin.log(e);
				clear();
			} catch (JavaModelException e) {
				// should not happen
				PHPeclipsePlugin.log(e.getStatus());
				clear();
			}
		}

		/**
		 * Return offset recomputed from stored visual properties.
		 * 
		 * @return Offset in the document
		 */
		public int getOffset() {
			IJavaElement newElement = getElement();

			int offset = getRememberedOffset(newElement);

			if (offset != -1 && !containsOffset(newElement, offset)
					&& (offset == 0 || !containsOffset(newElement, offset - 1)))
				return -1;

			return offset;
		}

		/**
		 * Return offset recomputed from stored visual properties.
		 * 
		 * @param newElement
		 *            Enclosing element
		 * @return Offset in the document
		 */
		public int getRememberedOffset(IJavaElement newElement) {
			try {
				if (newElement == null)
					return -1;

				IDocument document = getSourceViewer().getDocument();
				int newElementLine = -1;
				if (newElement instanceof IMember) {
					ISourceRange range = ((IMember) newElement).getNameRange();
					if (range != null)
						newElementLine = document.getLineOfOffset(range
								.getOffset());
				}
				if (newElementLine == -1)
					newElementLine = document
							.getLineOfOffset(getOffset(newElement));
				if (newElementLine == -1)
					return -1;

				int newLine = fLine + newElementLine - fElementLine;
				if (newLine < 0 || newLine >= document.getNumberOfLines())
					return -1;
				int maxColumn = document.getLineLength(newLine);
				String lineDelimiter = document.getLineDelimiter(newLine);
				if (lineDelimiter != null)
					maxColumn = maxColumn - lineDelimiter.length();
				int offset;
				if (fColumn > maxColumn)
					offset = document.getLineOffset(newLine) + maxColumn;
				else
					offset = document.getLineOffset(newLine) + fColumn;

				return offset;
			} catch (BadLocationException e) {
				// should not happen
				PHPeclipsePlugin.log(e);
				return -1;
			} catch (JavaModelException e) {
				// should not happen
				PHPeclipsePlugin.log(e.getStatus());
				return -1;
			}
		}

		/**
		 * Returns the offset used to reveal the given element based on the
		 * given selection offset.
		 * 
		 * @param element
		 *            the element
		 * @param offset
		 *            the selection offset
		 * @return the offset to reveal the given element based on the given
		 *         selection offset
		 */
		public int getRevealOffset(IJavaElement element, int offset) {
			if (element == null || offset == -1)
				return -1;

			if (containsOffset(element, offset)) {
				if (offset > 0) {
					IJavaElement alternateElement = getElementAt(offset, false);
					if (element.getHandleIdentifier().equals(
							alternateElement.getParent().getHandleIdentifier()))
						return offset - 1; // Solves test case 2 from
											// https://bugs.eclipse.org/bugs/show_bug.cgi?id=47727#c3
				}
				return offset;
			} else if (offset > 0 && containsOffset(element, offset - 1))
				return offset - 1; // Solves test case 1 from
									// https://bugs.eclipse.org/bugs/show_bug.cgi?id=47727#c3

			return -1;
		}

		/**
		 * Return Java element recomputed from stored visual properties.
		 * 
		 * @return Java element
		 */
		public IJavaElement getElement() {
			if (fElement == null)
				return null;

			return findElement(fElement);
		}

		/**
		 * Clears the stored position
		 */
		public void clear() {
			fLine = -1;
			fColumn = -1;
			fElement = null;
			fElementLine = -1;
		}

		/**
		 * Does the given Java element contain the given offset?
		 * 
		 * @param element
		 *            Java element
		 * @param offset
		 *            Offset
		 * @return <code>true</code> iff the Java element contains the offset
		 */
		private boolean containsOffset(IJavaElement element, int offset) {
			int elementOffset = getOffset(element);
			int elementLength = getLength(element);
			return (elementOffset > -1 && elementLength > -1) ? (offset >= elementOffset && offset < elementOffset
					+ elementLength)
					: false;
		}

		/**
		 * Returns the offset of the given Java element.
		 * 
		 * @param element
		 *            Java element
		 * @return Offset of the given Java element
		 */
		private int getOffset(IJavaElement element) {
			if (element instanceof ISourceReference) {
				ISourceReference sr = (ISourceReference) element;
				try {
					ISourceRange srcRange = sr.getSourceRange();
					if (srcRange != null)
						return srcRange.getOffset();
				} catch (JavaModelException e) {
				}
			}
			return -1;
		}

		/**
		 * Returns the length of the given Java element.
		 * 
		 * @param element
		 *            Java element
		 * @return Length of the given Java element
		 */
		private int getLength(IJavaElement element) {
			if (element instanceof ISourceReference) {
				ISourceReference sr = (ISourceReference) element;
				try {
					ISourceRange srcRange = sr.getSourceRange();
					if (srcRange != null)
						return srcRange.getLength();
				} catch (JavaModelException e) {
				}
			}
			return -1;
		}

		/**
		 * Returns the updated java element for the old java element.
		 * 
		 * @param element
		 *            Old Java element
		 * @return Updated Java element
		 */
		private IJavaElement findElement(IJavaElement element) {

			if (element == null)
				return null;

			IWorkingCopyManager manager = PHPeclipsePlugin.getDefault()
					.getWorkingCopyManager();
			ICompilationUnit unit = manager.getWorkingCopy(getEditorInput());

			if (unit != null) {
				try {

					synchronized (unit) {
						// unit.reconcile(ICompilationUnit.NO_AST, false, null,
						// null);
						unit.reconcile();
					}
					IJavaElement[] findings = unit.findElements(element);
					if (findings != null && findings.length > 0)
						return findings[0];

				} catch (JavaModelException x) {
					PHPeclipsePlugin.log(x.getStatus());
					// nothing found, be tolerant and go on
				}
			}

			return null;
		}

	}

	static class TabConverter implements ITextConverter {
		private int fTabRatio;

		private ILineTracker fLineTracker;

		public TabConverter() {
		}

		public void setNumberOfSpacesPerTab(int ratio) {
			fTabRatio = ratio;
		}

		public void setLineTracker(ILineTracker lineTracker) {
			fLineTracker = lineTracker;
		}

		private int insertTabString(StringBuffer buffer, int offsetInLine) {
			if (fTabRatio == 0)
				return 0;
			int remainder = offsetInLine % fTabRatio;
			remainder = fTabRatio - remainder;
			for (int i = 0; i < remainder; i++)
				buffer.append(' ');
			return remainder;
		}

		public void customizeDocumentCommand(IDocument document,
				DocumentCommand command) {
			String text = command.text;
			if (text == null)
				return;
			int index = text.indexOf('\t');
			if (index > -1) {
				StringBuffer buffer = new StringBuffer();
				fLineTracker.set(command.text);
				int lines = fLineTracker.getNumberOfLines();
				try {
					for (int i = 0; i < lines; i++) {
						int offset = fLineTracker.getLineOffset(i);
						int endOffset = offset + fLineTracker.getLineLength(i);
						String line = text.substring(offset, endOffset);
						int position = 0;
						if (i == 0) {
							IRegion firstLine = document
									.getLineInformationOfOffset(command.offset);
							position = command.offset - firstLine.getOffset();
						}
						int length = line.length();
						for (int j = 0; j < length; j++) {
							char c = line.charAt(j);
							if (c == '\t') {
								position += insertTabString(buffer, position);
							} else {
								buffer.append(c);
								++position;
							}
						}
					}
					command.text = buffer.toString();
				} catch (BadLocationException x) {
				}
			}
		}
	};

	private static class ExitPolicy implements LinkedPositionUI.ExitPolicy {
		final char fExitCharacter;

		public ExitPolicy(char exitCharacter) {
			fExitCharacter = exitCharacter;
		}

		/*
		 * @see org.phpeclipse.phpdt.internal.ui.text.link.LinkedPositionUI.ExitPolicy#doExit(org.phpeclipse.phpdt.internal.ui.text.link.LinkedPositionManager,
		 *      org.eclipse.swt.events.VerifyEvent, int, int)
		 */
		public ExitFlags doExit(LinkedPositionManager manager,
				VerifyEvent event, int offset, int length) {
			if (event.character == fExitCharacter) {
				if (manager.anyPositionIncludes(offset, length))
					return new ExitFlags(LinkedPositionUI.COMMIT
							| LinkedPositionUI.UPDATE_CARET, false);
				else
					return new ExitFlags(LinkedPositionUI.COMMIT, true);
			}
			// Fix for #1380415 (toshihiro) start 
		    switch (event.keyCode) {
		    case SWT.ARROW_UP:
		    case SWT.ARROW_DOWN:
		      return new ExitFlags(LinkedPositionUI.COMMIT, true);
		    case SWT.ARROW_LEFT:
		    case SWT.ARROW_RIGHT:
		       if (!manager.anyPositionIncludes(offset, length))
		         return new ExitFlags(LinkedPositionUI.COMMIT, true);
		       break;
		    }
		    // #1380415 end 
			switch (event.character) {
			case '\b':
				if (manager.getFirstPosition().length == 0)
					return new ExitFlags(0, false);
				else
					return null;
			case '\n':
			case '\r':
				return new ExitFlags(LinkedPositionUI.COMMIT, true);
			default:
				return null;
			}
		}
	}

	private static class BracketLevel {
		int fOffset;

		int fLength;

		LinkedPositionManager fManager;

		LinkedPositionUI fEditor;
	};

	private class BracketInserter implements VerifyKeyListener,
			LinkedPositionUI.ExitListener {
		private boolean fCloseBracketsPHP = true;

		private boolean fCloseStringsPHPDQ = true;

		private boolean fCloseStringsPHPSQ = true;

		private int fOffset;

		private int fLength;

		public void setCloseBracketsPHPEnabled(boolean enabled) {
			fCloseBracketsPHP = enabled;
		}

		public void setCloseStringsPHPDQEnabled(boolean enabled) {
			fCloseStringsPHPDQ = enabled;
		}

		public void setCloseStringsPHPSQEnabled(boolean enabled) {
			fCloseStringsPHPSQ = enabled;
		}

		private boolean hasIdentifierToTheRight(IDocument document, int offset) {
			try {
				int end = offset;
				IRegion endLine = document.getLineInformationOfOffset(end);
				int maxEnd = endLine.getOffset() + endLine.getLength();
				while (end != maxEnd
						&& Character.isWhitespace(document.getChar(end)))
					++end;
				return end != maxEnd
						&& Scanner.isPHPIdentifierPart(document.getChar(end));
			} catch (BadLocationException e) {
				// be conservative
				return true;
			}
		}

		private boolean hasIdentifierToTheLeft(IDocument document, int offset) {
			try {
				int start = offset;
				IRegion startLine = document.getLineInformationOfOffset(start);
				int minStart = startLine.getOffset();
				while (start != minStart
						&& Character.isWhitespace(document.getChar(start - 1)))
					--start;
				return start != minStart
						&& Scanner.isPHPIdentifierPart(document
								.getChar(start - 1));
			} catch (BadLocationException e) {
				return true;
			}
		}

		private boolean hasCharacterToTheLeft(IDocument document, int offset,
				char character) {
			try {
				int start = offset;
				IRegion startLine = document.getLineInformationOfOffset(start);
				int minStart = startLine.getOffset();
				while (start != minStart
						&& Character.isWhitespace(document.getChar(start - 1)))
					--start;
				return start != minStart
						&& document.getChar(start - 1) == character;
			} catch (BadLocationException e) {
				return false;
			}
		}

		private boolean hasCharacterToTheRight(IDocument document, int offset,
				char character) {
			try {
				int end = offset;
				IRegion endLine = document.getLineInformationOfOffset(end);
				int maxEnd = endLine.getOffset() + endLine.getLength();
				while (end != maxEnd
						&& Character.isWhitespace(document.getChar(end)))
					++end;
				return end != maxEnd && document.getChar(end) == character;
			} catch (BadLocationException e) {
				// be conservative
				return true;
			}
		}

		/*
		 * @see org.eclipse.swt.custom.VerifyKeyListener#verifyKey(org.eclipse.swt.events.VerifyEvent)
		 */
		public void verifyKey(VerifyEvent event) {
			if (!event.doit)
				return;
			final ISourceViewer sourceViewer = getSourceViewer();
			IDocument document = sourceViewer.getDocument();
			final Point selection = sourceViewer.getSelectedRange();
			final int offset = selection.x;
			final int length = selection.y;
			try {
				ITypedRegion partition = document.getPartition(offset);
				String type = partition.getType();
				if (type.equals(IPHPPartitions.PHP_PARTITIONING)
						|| type.equals(IDocument.DEFAULT_CONTENT_TYPE)) {
					// you will get IDocument.DEFAULT_CONTENT_TYPE for both PHP
					// and HTML area
					switch (event.character) {
					case '(':
						if (hasCharacterToTheRight(document, offset + length,
								'('))
							return;
						// fall through
					case '[':
						if (!fCloseBracketsPHP)
							return;
						if (hasIdentifierToTheRight(document, offset + length))
							return;
						// fall through
					case '{':
						if (!fCloseBracketsPHP)
							return;
						if (hasIdentifierToTheRight(document, offset + length))
							return;
						// fall through
					case '"':
						if (event.character == '"') {
							if (!fCloseStringsPHPDQ)
								return;
							// changed for statements like echo "" print ""
							// if (hasIdentifierToTheLeft(document, offset)
							// ||
							// hasIdentifierToTheRight(document, offset +
							// length))
							if (hasIdentifierToTheRight(document, offset
									+ length))
								return;
						}
						// ITypedRegion partition=
						// document.getPartition(offset);
						// if (!
						// IDocument.DEFAULT_CONTENT_TYPE.equals(partition.getType())
						// &&
						// (partition.getOffset() != offset))
						// return;
						final char characterDQ = event.character;
						final char closingCharacterDQ = getPeerCharacter(characterDQ);
						final StringBuffer bufferDQ = new StringBuffer();
						bufferDQ.append(characterDQ);
						bufferDQ.append(closingCharacterDQ);
						document.replace(offset, length, bufferDQ.toString());
						LinkedPositionManager managerDQ = new LinkedPositionManager(
								document);
						managerDQ.addPosition(offset + 1, 0);
						fOffset = offset;
						fLength = 2;
						LinkedPositionUI editorDQ = new LinkedPositionUI(
								sourceViewer, managerDQ);
						editorDQ.setCancelListener(this);
						editorDQ.setExitPolicy(new ExitPolicy(
								closingCharacterDQ));
						editorDQ.setFinalCaretOffset(offset + 2);
						editorDQ.enter();
						IRegion newSelectionDQ = editorDQ.getSelectedRegion();
						sourceViewer.setSelectedRange(newSelectionDQ
								.getOffset(), newSelectionDQ.getLength());
						event.doit = false;
						break;
					case '\'':
						if (event.character == '\'') {
							if (!fCloseStringsPHPSQ)
								return;
							// changed for statements like echo "" print ""
							// if (hasIdentifierToTheLeft(document, offset)
							// ||
							// hasIdentifierToTheRight(document, offset +
							// length))
							if (hasIdentifierToTheRight(document, offset
									+ length))
								return;
						}
						// ITypedRegion partition=
						// document.getPartition(offset);
						// if (!
						// IDocument.DEFAULT_CONTENT_TYPE.equals(partition.getType())
						// &&
						// (partition.getOffset() != offset))
						// return;
						final char characterSQ = event.character;
						final char closingCharacterSQ = getPeerCharacter(characterSQ);
						final StringBuffer bufferSQ = new StringBuffer();
						bufferSQ.append(characterSQ);
						bufferSQ.append(closingCharacterSQ);
						document.replace(offset, length, bufferSQ.toString());
						LinkedPositionManager managerSQ = new LinkedPositionManager(
								document);
						managerSQ.addPosition(offset + 1, 0);
						fOffset = offset;
						fLength = 2;
						LinkedPositionUI editorSQ = new LinkedPositionUI(
								sourceViewer, managerSQ);
						editorSQ.setCancelListener(this);
						editorSQ.setExitPolicy(new ExitPolicy(
								closingCharacterSQ));
						editorSQ.setFinalCaretOffset(offset + 2);
						editorSQ.enter();
						IRegion newSelectionSQ = editorSQ.getSelectedRegion();
						sourceViewer.setSelectedRange(newSelectionSQ
								.getOffset(), newSelectionSQ.getLength());
						event.doit = false;
					case '\r': { // insert linebreaks and new closing brace
									// after brace and return
						if (!fCloseBracketsPHP) {
							return;
						}
						if (hasCharacterToTheLeft(document, offset, '{')
								&& hasCharacterToTheRight(document, offset, '}')) {
							String lineDelimiter = StubUtility
									.getLineDelimiterFor(document);
							int caretPos = sourceViewer.getTextWidget()
									.getCaretOffset();
							final StringBuffer buffer = new StringBuffer(
									lineDelimiter);
							// get indentation
							IRegion line = document
									.getLineInformationOfOffset(offset);
							String currentLine = document.get(line.getOffset(),
									line.getLength());
							int index = 0;
							int max = currentLine.length();
							StringBuffer indent = new StringBuffer();
							while (index < max
									&& Character.isWhitespace(currentLine
											.charAt(index))) {
								indent.append(currentLine.charAt(index));
								index++;
							}
							buffer.append(indent);
							JavaHeuristicScanner scanner = new JavaHeuristicScanner(
									document);
							JavaIndenter indenter = new JavaIndenter(document,
									scanner);
							buffer.append(indenter.createIndent(1));
							int cursorPos = buffer.length();
							buffer.append(lineDelimiter);
							buffer.append(indent);
							document.replace(offset, length, buffer.toString());
							sourceViewer.getTextWidget().setCaretOffset(
									caretPos + cursorPos);
							event.doit = false;
						}
					}
					}
				}
			} catch (BadLocationException e) {
			}
		}

		/*
		 * @see org.phpeclipse.phpdt.internal.ui.text.link.LinkedPositionUI.ExitListener#exit(boolean)
		 */
		public void exit(boolean accept) {
			if (accept)
				return;
			// remove brackets
			try {
				final ISourceViewer sourceViewer = getSourceViewer();
				IDocument document = sourceViewer.getDocument();
				document.replace(fOffset, fLength, null);
			} catch (BadLocationException e) {
			}
		}
	}

	/** The editor's save policy */
	protected ISavePolicy fSavePolicy;

	/**
	 * Listener to annotation model changes that updates the error tick in the
	 * tab image
	 */
	private JavaEditorErrorTickUpdater fJavaEditorErrorTickUpdater;

	/** The editor's paint manager */
	// private PaintManager fPaintManager;
	/** The editor's bracket painter */
	// private BracketPainter fBracketPainter;
	/** The editor's bracket matcher */
	private PHPPairMatcher fBracketMatcher;

	/** The editor's line painter */
	// private LinePainter fLinePainter;
	/** The editor's print margin ruler painter */
	// private PrintMarginPainter fPrintMarginPainter;
	/** The editor's problem painter */
	// private ProblemPainter fProblemPainter;
	/** The editor's tab converter */
	private TabConverter fTabConverter;

	/** History for structure select action */
	// private SelectionHistory fSelectionHistory;
	/** The preference property change listener for php core. */
	// private IPropertyChangeListener fPropertyChangeListener = new
	// PropertyChangeListener();
	/** The remembered java element */
	private IJavaElement fRememberedElement;

	/**
	 * The remembered selection.
	 * 
	 * @since 3.0
	 */
	private RememberedSelection fRememberedSelection = new RememberedSelection();

	/** The remembered php element offset */
	private int fRememberedElementOffset;

	/** The bracket inserter. */
	private BracketInserter fBracketInserter = new BracketInserter();

	/** The standard action groups added to the menu */
	private GenerateActionGroup fGenerateActionGroup;

	private CompositeActionGroup fContextMenuGroup;

	// private class PropertyChangeListener implements IPropertyChangeListener {
	// /*
	// * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	// */
	// public void
	// propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent
	// event) {
	// handlePreferencePropertyChanged(event);
	// }
	// }
	/* Preference key for code formatter tab size */
	private final static String CODE_FORMATTER_TAB_SIZE = JavaCore.FORMATTER_TAB_SIZE;

	/** Preference key for matching brackets */
	// private final static String MATCHING_BRACKETS =
	// PreferenceConstants.EDITOR_MATCHING_BRACKETS;
	/** Preference key for matching brackets color */
	// private final static String MATCHING_BRACKETS_COLOR =
	// PreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR;
	/** Preference key for highlighting current line */
	// private final static String CURRENT_LINE =
	// PreferenceConstants.EDITOR_CURRENT_LINE;
	/** Preference key for highlight color of current line */
	// private final static String CURRENT_LINE_COLOR =
	// PreferenceConstants.EDITOR_CURRENT_LINE_COLOR;
	/** Preference key for showing print marging ruler */
	// private final static String PRINT_MARGIN =
	// PreferenceConstants.EDITOR_PRINT_MARGIN;
	/** Preference key for print margin ruler color */
	// private final static String PRINT_MARGIN_COLOR =
	// PreferenceConstants.EDITOR_PRINT_MARGIN_COLOR;
	/** Preference key for print margin ruler column */
	// private final static String PRINT_MARGIN_COLUMN =
	// PreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN;
	/** Preference key for inserting spaces rather than tabs */
	private final static String SPACES_FOR_TABS = PreferenceConstants.EDITOR_SPACES_FOR_TABS;

	/** Preference key for error indication */
	// private final static String ERROR_INDICATION =
	// PreferenceConstants.EDITOR_PROBLEM_INDICATION;
	/** Preference key for error color */
	// private final static String ERROR_INDICATION_COLOR =
	// PreferenceConstants.EDITOR_PROBLEM_INDICATION_COLOR;
	/** Preference key for warning indication */
	// private final static String WARNING_INDICATION =
	// PreferenceConstants.EDITOR_WARNING_INDICATION;
	/** Preference key for warning color */
	// private final static String WARNING_INDICATION_COLOR =
	// PreferenceConstants.EDITOR_WARNING_INDICATION_COLOR;
	/** Preference key for task indication */
	private final static String TASK_INDICATION = PreferenceConstants.EDITOR_TASK_INDICATION;

	/** Preference key for task color */
	private final static String TASK_INDICATION_COLOR = PreferenceConstants.EDITOR_TASK_INDICATION_COLOR;

	/** Preference key for bookmark indication */
	private final static String BOOKMARK_INDICATION = PreferenceConstants.EDITOR_BOOKMARK_INDICATION;

	/** Preference key for bookmark color */
	private final static String BOOKMARK_INDICATION_COLOR = PreferenceConstants.EDITOR_BOOKMARK_INDICATION_COLOR;

	/** Preference key for search result indication */
	private final static String SEARCH_RESULT_INDICATION = PreferenceConstants.EDITOR_SEARCH_RESULT_INDICATION;

	/** Preference key for search result color */
	private final static String SEARCH_RESULT_INDICATION_COLOR = PreferenceConstants.EDITOR_SEARCH_RESULT_INDICATION_COLOR;

	/** Preference key for unknown annotation indication */
	private final static String UNKNOWN_INDICATION = PreferenceConstants.EDITOR_UNKNOWN_INDICATION;

	/** Preference key for unknown annotation color */
	private final static String UNKNOWN_INDICATION_COLOR = PreferenceConstants.EDITOR_UNKNOWN_INDICATION_COLOR;

	/** Preference key for linked position color */
	private final static String LINKED_POSITION_COLOR = PreferenceConstants.EDITOR_LINKED_POSITION_COLOR;

	/** Preference key for shwoing the overview ruler */
	private final static String OVERVIEW_RULER = PreferenceConstants.EDITOR_OVERVIEW_RULER;

	/** Preference key for error indication in overview ruler */
	private final static String ERROR_INDICATION_IN_OVERVIEW_RULER = PreferenceConstants.EDITOR_ERROR_INDICATION_IN_OVERVIEW_RULER;

	/** Preference key for warning indication in overview ruler */
	private final static String WARNING_INDICATION_IN_OVERVIEW_RULER = PreferenceConstants.EDITOR_WARNING_INDICATION_IN_OVERVIEW_RULER;

	/** Preference key for task indication in overview ruler */
	private final static String TASK_INDICATION_IN_OVERVIEW_RULER = PreferenceConstants.EDITOR_TASK_INDICATION_IN_OVERVIEW_RULER;

	/** Preference key for bookmark indication in overview ruler */
	private final static String BOOKMARK_INDICATION_IN_OVERVIEW_RULER = PreferenceConstants.EDITOR_BOOKMARK_INDICATION_IN_OVERVIEW_RULER;

	/** Preference key for search result indication in overview ruler */
	private final static String SEARCH_RESULT_INDICATION_IN_OVERVIEW_RULER = PreferenceConstants.EDITOR_SEARCH_RESULT_INDICATION_IN_OVERVIEW_RULER;

	/** Preference key for unknown annotation indication in overview ruler */
	private final static String UNKNOWN_INDICATION_IN_OVERVIEW_RULER = PreferenceConstants.EDITOR_UNKNOWN_INDICATION_IN_OVERVIEW_RULER;

	/** Preference key for automatically closing double quoted strings */
	private final static String CLOSE_STRINGS_DQ_PHP = PreferenceConstants.EDITOR_CLOSE_STRINGS_DQ_PHP;

	/** Preference key for automatically closing single quoted strings */
	private final static String CLOSE_STRINGS_SQ_PHP = PreferenceConstants.EDITOR_CLOSE_STRINGS_SQ_PHP;

	/** Preference key for automatically wrapping Java strings */
	// private final static String WRAP_STRINGS =
	// PreferenceConstants.EDITOR_WRAP_STRINGS_DQ;
	/** Preference key for automatically closing brackets and parenthesis */
	private final static String CLOSE_BRACKETS_PHP = PreferenceConstants.EDITOR_CLOSE_BRACKETS_PHP;

	/** Preference key for automatically closing phpdocs and comments */
	private final static String CLOSE_JAVADOCS = PreferenceConstants.EDITOR_CLOSE_JAVADOCS;

	/** Preference key for automatically adding phpdoc tags */
	private final static String ADD_JAVADOC_TAGS = PreferenceConstants.EDITOR_ADD_JAVADOC_TAGS;

	/** Preference key for automatically formatting phpdocs */
	// private final static String FORMAT_JAVADOCS =
	// PreferenceConstants.EDITOR_FORMAT_JAVADOCS;
	/** Preference key for automatically closing strings */
	private final static String CLOSE_STRINGS_HTML = PreferenceConstants.EDITOR_CLOSE_STRINGS_HTML;

	/** Preference key for automatically closing brackets and parenthesis */
	private final static String CLOSE_BRACKETS_HTML = PreferenceConstants.EDITOR_CLOSE_BRACKETS_HTML;

	/** Preference key for smart paste */
	private final static String SMART_PASTE = PreferenceConstants.EDITOR_SMART_PASTE;

	// private final static class AnnotationInfo {
	// public String fColorPreference;
	// public String fOverviewRulerPreference;
	// public String fEditorPreference;
	// };
	// private final static Map ANNOTATION_MAP;
	// static {
	//
	// AnnotationInfo info;
	// ANNOTATION_MAP = new HashMap();
	//
	// info = new AnnotationInfo();
	// info.fColorPreference = TASK_INDICATION_COLOR;
	// info.fOverviewRulerPreference = TASK_INDICATION_IN_OVERVIEW_RULER;
	// info.fEditorPreference = TASK_INDICATION;
	// ANNOTATION_MAP.put(AnnotationType.TASK, info);
	//
	// info = new AnnotationInfo();
	// info.fColorPreference = ERROR_INDICATION_COLOR;
	// info.fOverviewRulerPreference = ERROR_INDICATION_IN_OVERVIEW_RULER;
	// info.fEditorPreference = ERROR_INDICATION;
	// ANNOTATION_MAP.put(AnnotationType.ERROR, info);
	//
	// info = new AnnotationInfo();
	// info.fColorPreference = WARNING_INDICATION_COLOR;
	// info.fOverviewRulerPreference = WARNING_INDICATION_IN_OVERVIEW_RULER;
	// info.fEditorPreference = WARNING_INDICATION;
	// ANNOTATION_MAP.put(AnnotationType.WARNING, info);
	//
	// info = new AnnotationInfo();
	// info.fColorPreference = BOOKMARK_INDICATION_COLOR;
	// info.fOverviewRulerPreference = BOOKMARK_INDICATION_IN_OVERVIEW_RULER;
	// info.fEditorPreference = BOOKMARK_INDICATION;
	// ANNOTATION_MAP.put(AnnotationType.BOOKMARK, info);
	//
	// info = new AnnotationInfo();
	// info.fColorPreference = SEARCH_RESULT_INDICATION_COLOR;
	// info.fOverviewRulerPreference =
	// SEARCH_RESULT_INDICATION_IN_OVERVIEW_RULER;
	// info.fEditorPreference = SEARCH_RESULT_INDICATION;
	// ANNOTATION_MAP.put(AnnotationType.SEARCH, info);
	//
	// info = new AnnotationInfo();
	// info.fColorPreference = UNKNOWN_INDICATION_COLOR;
	// info.fOverviewRulerPreference = UNKNOWN_INDICATION_IN_OVERVIEW_RULER;
	// info.fEditorPreference = UNKNOWN_INDICATION;
	// ANNOTATION_MAP.put(AnnotationType.UNKNOWN, info);
	// };
	//
	// private final static AnnotationType[] ANNOTATION_LAYERS =
	// new AnnotationType[] {
	// AnnotationType.UNKNOWN,
	// AnnotationType.BOOKMARK,
	// AnnotationType.TASK,
	// AnnotationType.SEARCH,
	// AnnotationType.WARNING,
	// AnnotationType.ERROR };
	/**
	 * Creates a new php unit editor.
	 */

	/**
	 * Reconciling listeners.
	 * 
	 * @since 3.0
	 */
	private ListenerList fReconcilingListeners = new ListenerList();

	/**
	 * Mutex for the reconciler. See
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=63898 for a description of
	 * the problem.
	 * <p>
	 * TODO remove once the underlying problem is solved.
	 * </p>
	 */
	private final Object fReconcilerLock = new Object();

	public PHPUnitEditor() {
		super();
		setDocumentProvider(PHPeclipsePlugin.getDefault()
				.getCompilationUnitDocumentProvider());
		setEditorContextMenuId("#PHPEditorContext"); //$NON-NLS-1$
		setRulerContextMenuId("#PHPRulerContext"); //$NON-NLS-1$
		setOutlinerContextMenuId("#PHPOutlinerContext"); //$NON-NLS-1$
		// don't set help contextId, we install our own help context
		fSavePolicy = null;
		fJavaEditorErrorTickUpdater = new JavaEditorErrorTickUpdater(this);
	}

	/*
	 * @see AbstractTextEditor#createActions()
	 */
	protected void createActions() {
		super.createActions();
		Action action;
		// Action action= new
		// TextOperationAction(PHPEditorMessages.getResourceBundle(),
		// "CorrectionAssistProposal.", this, CORRECTIONASSIST_PROPOSALS);
		// //$NON-NLS-1$
		// action.setActionDefinitionId(PHPEditorActionDefinitionIds.CORRECTION_ASSIST_PROPOSALS);
		// setAction("CorrectionAssistProposal", action); //$NON-NLS-1$
		// markAsStateDependentAction("CorrectionAssistProposal", true);
		// //$NON-NLS-1$
		// // WorkbenchHelp.setHelp(action,
		// IJavaHelpContextIds.QUICK_FIX_ACTION);
		action = new ContentAssistAction(PHPEditorMessages.getResourceBundle(),
				"ContentAssistProposal.", this); //$NON-NLS-1$
		action
				.setActionDefinitionId(PHPEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction("ContentAssistProposal", action); //$NON-NLS-1$
		markAsStateDependentAction("ContentAssistProposal", true); //$NON-NLS-1$
		// WorkbenchHelp.setHelp(action,
		// IJavaHelpContextIds.CONTENT_ASSIST_ACTION);
		// action = new
		// TextOperationAction(PHPEditorMessages.getResourceBundle(),
		// "ContentAssistContextInformation.", this,
		// ISourceViewer.CONTENTASSIST_CONTEXT_INFORMATION); //$NON-NLS-1$
		// action
		// .setActionDefinitionId(PHPEditorActionDefinitionIds.CONTENT_ASSIST_CONTEXT_INFORMATION);
		// setAction("ContentAssistContextInformation", action); //$NON-NLS-1$
		// markAsStateDependentAction("ContentAssistContextInformation", true);
		// //$NON-NLS-1$
		// WorkbenchHelp.setHelp(action,
		// IJavaHelpContextIds.PARAMETER_HINTS_ACTION);
		// action= new
		// TextOperationAction(PHPEditorMessages.getResourceBundle(),
		// "ContentAssistCompletePrefix.", this, CONTENTASSIST_COMPLETE_PREFIX);
		// //$NON-NLS-1$
		// action.setActionDefinitionId(PHPEditorActionDefinitionIds.CONTENT_ASSIST_COMPLETE_PREFIX);
		// setAction("ContentAssistCompletePrefix", action); //$NON-NLS-1$
		// markAsStateDependentAction("ContentAssistCompletePrefix", true);
		// //$NON-NLS-1$
		// // WorkbenchHelp.setHelp(action,
		// IJavaHelpContextIds.PARAMETER_HINTS_ACTION);
		action = new TextOperationAction(PHPEditorMessages.getResourceBundle(),
				"Comment.", this, ITextOperationTarget.PREFIX); //$NON-NLS-1$
		action.setActionDefinitionId(PHPEditorActionDefinitionIds.COMMENT);
		setAction("Comment", action); //$NON-NLS-1$
		markAsStateDependentAction("Comment", true); //$NON-NLS-1$
		// WorkbenchHelp.setHelp(action, IJavaHelpContextIds.COMMENT_ACTION);
		action = new TextOperationAction(PHPEditorMessages.getResourceBundle(),
				"Uncomment.", this, ITextOperationTarget.STRIP_PREFIX); //$NON-NLS-1$
		action.setActionDefinitionId(PHPEditorActionDefinitionIds.UNCOMMENT);
		setAction("Uncomment", action); //$NON-NLS-1$
		markAsStateDependentAction("Uncomment", true); //$NON-NLS-1$
		// WorkbenchHelp.setHelp(action, IJavaHelpContextIds.UNCOMMENT_ACTION);

		action = new ToggleCommentAction(PHPEditorMessages.getResourceBundle(),
				"ToggleComment.", this); //$NON-NLS-1$
		action
				.setActionDefinitionId(PHPEditorActionDefinitionIds.TOGGLE_COMMENT);
		setAction("ToggleComment", action); //$NON-NLS-1$
		markAsStateDependentAction("ToggleComment", true); //$NON-NLS-1$
		// WorkbenchHelp.setHelp(action,
		// IJavaHelpContextIds.TOGGLE_COMMENT_ACTION);
		configureToggleCommentAction();

		action = new TextOperationAction(PHPEditorMessages.getResourceBundle(),
				"Format.", this, ISourceViewer.FORMAT); //$NON-NLS-1$
		action.setActionDefinitionId(PHPEditorActionDefinitionIds.FORMAT);
		setAction("Format", action); //$NON-NLS-1$
		markAsStateDependentAction("Format", true); //$NON-NLS-1$
		markAsSelectionDependentAction("Format", true); //$NON-NLS-1$
		// WorkbenchHelp.setHelp(action, IJavaHelpContextIds.FORMAT_ACTION);

		// action = new
		// AddBlockCommentAction(PHPEditorMessages.getResourceBundle(),
		// "AddBlockComment.", this); //$NON-NLS-1$
		// action
		// .setActionDefinitionId(PHPEditorActionDefinitionIds.ADD_BLOCK_COMMENT);
		// setAction("AddBlockComment", action); //$NON-NLS-1$
		// markAsStateDependentAction("AddBlockComment", true); //$NON-NLS-1$
		// markAsSelectionDependentAction("AddBlockComment", true);
		// //$NON-NLS-1$
		// // WorkbenchHelp.setHelp(action,
		// // IJavaHelpContextIds.ADD_BLOCK_COMMENT_ACTION);
		// action = new RemoveBlockCommentAction(
		// PHPEditorMessages.getResourceBundle(), "RemoveBlockComment.", this);
		// //$NON-NLS-1$
		// action
		// .setActionDefinitionId(PHPEditorActionDefinitionIds.REMOVE_BLOCK_COMMENT);
		// setAction("RemoveBlockComment", action); //$NON-NLS-1$
		// markAsStateDependentAction("RemoveBlockComment", true); //$NON-NLS-1$
		// markAsSelectionDependentAction("RemoveBlockComment", true);
		// //$NON-NLS-1$
		// WorkbenchHelp.setHelp(action,
		// IJavaHelpContextIds.ADD_BLOCK_COMMENT_ACTION);
		action = new IndentAction(PHPEditorMessages.getResourceBundle(),
				"Indent.", this, false); //$NON-NLS-1$
		action.setActionDefinitionId(PHPEditorActionDefinitionIds.INDENT);
		setAction("Indent", action); //$NON-NLS-1$
		markAsStateDependentAction("Indent", true); //$NON-NLS-1$
		markAsSelectionDependentAction("Indent", true); //$NON-NLS-1$
		// WorkbenchHelp.setHelp(action, IJavaHelpContextIds.INDENT_ACTION);
		//
		// action= new IndentAction(PHPEditorMessages.getResourceBundle(),
		// "Indent.", this, true); //$NON-NLS-1$
		// setAction("IndentOnTab", action); //$NON-NLS-1$
		// markAsStateDependentAction("IndentOnTab", true); //$NON-NLS-1$
		// markAsSelectionDependentAction("IndentOnTab", true); //$NON-NLS-1$
		//

		action = new AddBlockCommentAction(PHPEditorMessages
				.getResourceBundle(), "AddBlockComment.", this); //$NON-NLS-1$
		action
				.setActionDefinitionId(PHPEditorActionDefinitionIds.ADD_BLOCK_COMMENT);
		setAction("AddBlockComment", action); //$NON-NLS-1$
		markAsStateDependentAction("AddBlockComment", true); //$NON-NLS-1$
		markAsSelectionDependentAction("AddBlockComment", true); //$NON-NLS-1$
		// WorkbenchHelp.setHelp(action,
		// IJavaHelpContextIds.ADD_BLOCK_COMMENT_ACTION);

		action = new RemoveBlockCommentAction(PHPEditorMessages
				.getResourceBundle(), "RemoveBlockComment.", this); //$NON-NLS-1$
		action
				.setActionDefinitionId(PHPEditorActionDefinitionIds.REMOVE_BLOCK_COMMENT);
		setAction("RemoveBlockComment", action); //$NON-NLS-1$
		markAsStateDependentAction("RemoveBlockComment", true); //$NON-NLS-1$
		markAsSelectionDependentAction("RemoveBlockComment", true); //$NON-NLS-1$
		// WorkbenchHelp.setHelp(action,
		// IJavaHelpContextIds.REMOVE_BLOCK_COMMENT_ACTION);

		// action= new IndentAction(PHPEditorMessages.getResourceBundle(),
		// "Indent.", this, false); //$NON-NLS-1$
		// action.setActionDefinitionId(PHPEditorActionDefinitionIds.INDENT);
		// setAction("Indent", action); //$NON-NLS-1$
		// markAsStateDependentAction("Indent", true); //$NON-NLS-1$
		// markAsSelectionDependentAction("Indent", true); //$NON-NLS-1$
		// // WorkbenchHelp.setHelp(action, IJavaHelpContextIds.INDENT_ACTION);
		//
		action = new IndentAction(PHPEditorMessages.getResourceBundle(),
				"Indent.", this, true); //$NON-NLS-1$
		setAction("IndentOnTab", action); //$NON-NLS-1$
		markAsStateDependentAction("IndentOnTab", true); //$NON-NLS-1$
		markAsSelectionDependentAction("IndentOnTab", true); //$NON-NLS-1$

		if (getPreferenceStore().getBoolean(
				PreferenceConstants.EDITOR_SMART_TAB)) {
			// don't replace Shift Right - have to make sure their enablement is
			// mutually exclusive
			// removeActionActivationCode(ITextEditorActionConstants.SHIFT_RIGHT);
			setActionActivationCode("IndentOnTab", '\t', -1, SWT.NONE); //$NON-NLS-1$
		}
		fGenerateActionGroup = new GenerateActionGroup(this,
				ITextEditorActionConstants.GROUP_EDIT);
		// ActionGroup rg= new RefactorActionGroup(this,
		// ITextEditorActionConstants.GROUP_EDIT);

		// fActionGroups.addGroup(rg);
		fActionGroups.addGroup(fGenerateActionGroup);

		// We have to keep the context menu group separate to have better
		// control over positioning
		fContextMenuGroup = new CompositeActionGroup(
				new ActionGroup[] { fGenerateActionGroup
				// rg,
				// new LocalHistoryActionGroup(this,
				// ITextEditorActionConstants.GROUP_EDIT)
				});

	}

	/*
	 * @see JavaEditor#getElementAt(int)
	 */
	protected IJavaElement getElementAt(int offset) {
		return getElementAt(offset, true);
	}

	/**
	 * Returns the most narrow element including the given offset. If
	 * <code>reconcile</code> is <code>true</code> the editor's input
	 * element is reconciled in advance. If it is <code>false</code> this
	 * method only returns a result if the editor's input element does not need
	 * to be reconciled.
	 * 
	 * @param offset
	 *            the offset included by the retrieved element
	 * @param reconcile
	 *            <code>true</code> if working copy should be reconciled
	 */
	protected IJavaElement getElementAt(int offset, boolean reconcile) {
		IWorkingCopyManager manager = PHPeclipsePlugin.getDefault()
				.getWorkingCopyManager();
		ICompilationUnit unit = manager.getWorkingCopy(getEditorInput());
		if (unit != null) {
			try {
				if (reconcile) {
					synchronized (unit) {
						unit.reconcile();
					}
					return unit.getElementAt(offset);
				} else if (unit.isConsistent())
					return unit.getElementAt(offset);
			} catch (JavaModelException x) {
				PHPeclipsePlugin.log(x.getStatus());
				// nothing found, be tolerant and go on
			}
		}
		return null;
	}

	/*
	 * @see JavaEditor#getCorrespondingElement(IJavaElement)
	 */
	protected IJavaElement getCorrespondingElement(IJavaElement element) {
		try {
			return EditorUtility.getWorkingCopy(element, true);
		} catch (JavaModelException x) {
			PHPeclipsePlugin.log(x.getStatus());
			// nothing found, be tolerant and go on
		}
		return null;
	}

	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		// fPaintManager = new PaintManager(getSourceViewer());
		LinePainter linePainter;
		linePainter = new LinePainter(getSourceViewer());
		linePainter.setHighlightColor(new Color(Display.getCurrent(), 225, 235,
				224));
		// fPaintManager.addPainter(linePainter);
		// if (isBracketHighlightingEnabled())
		// startBracketHighlighting();
		// if (isLineHighlightingEnabled())
		// startLineHighlighting();
		// if (isPrintMarginVisible())
		// showPrintMargin();
		// Iterator e = ANNOTATION_MAP.keySet().iterator();
		// while (e.hasNext()) {
		// AnnotationType type = (AnnotationType) e.next();
		// if (isAnnotationIndicationEnabled(type))
		// startAnnotationIndication(type);
		// }
		if (isTabConversionEnabled())
			startTabConversion();
		// if (isOverviewRulerVisible())
		// showOverviewRuler();
		//
		// Preferences preferences =
		// PHPeclipsePlugin.getDefault().getPluginPreferences();
		// preferences.addPropertyChangeListener(fPropertyChangeListener);
		IPreferenceStore preferenceStore = getPreferenceStore();
		boolean closeBracketsPHP = preferenceStore
				.getBoolean(CLOSE_BRACKETS_PHP);
		boolean closeStringsPHPDQ = preferenceStore
				.getBoolean(CLOSE_STRINGS_DQ_PHP);
		boolean closeStringsPHPSQ = preferenceStore
				.getBoolean(CLOSE_STRINGS_SQ_PHP);
		fBracketInserter.setCloseBracketsPHPEnabled(closeBracketsPHP);
		fBracketInserter.setCloseStringsPHPDQEnabled(closeStringsPHPDQ);
		fBracketInserter.setCloseStringsPHPSQEnabled(closeStringsPHPSQ);
		ISourceViewer sourceViewer = getSourceViewer();
		if (sourceViewer instanceof ITextViewerExtension)
			((ITextViewerExtension) sourceViewer)
					.prependVerifyKeyListener(fBracketInserter);
	}

	private static char getPeerCharacter(char character) {
		switch (character) {
		case '(':
			return ')';
		case ')':
			return '(';
		case '[':
			return ']';
		case ']':
			return '[';
		case '"':
			return character;
		case '\'':
			return character;
		case '{':
			return '}';
		default:
			throw new IllegalArgumentException();
		}
	}

	// private void startBracketHighlighting() {
	// if (fBracketPainter == null) {
	// ISourceViewer sourceViewer = getSourceViewer();
	// fBracketPainter = new BracketPainter(sourceViewer);
	// fBracketPainter.setHighlightColor(getColor(MATCHING_BRACKETS_COLOR));
	// // fPaintManager.addPainter(fBracketPainter);
	// }
	// }
	//
	// private void stopBracketHighlighting() {
	// if (fBracketPainter != null) {
	// // fPaintManager.removePainter(fBracketPainter);
	// fBracketPainter.deactivate(true);
	// fBracketPainter.dispose();
	// fBracketPainter = null;
	// }
	// }

	// private boolean isBracketHighlightingEnabled() {
	// IPreferenceStore store = getPreferenceStore();
	// return store.getBoolean(MATCHING_BRACKETS);
	// }

	// private void startLineHighlighting() {
	// if (fLinePainter == null) {
	// ISourceViewer sourceViewer = getSourceViewer();
	// fLinePainter = new LinePainter(sourceViewer);
	// fLinePainter.setHighlightColor(getColor(CURRENT_LINE_COLOR));
	// // fPaintManager.addPainter(fLinePainter);
	// }
	// }

	// private void stopLineHighlighting() {
	// if (fLinePainter != null) {
	// // fPaintManager.removePainter(fLinePainter);
	// fLinePainter.deactivate(true);
	// fLinePainter.dispose();
	// fLinePainter = null;
	// }
	// }

	// private boolean isLineHighlightingEnabled() {
	// IPreferenceStore store = getPreferenceStore();
	// return store.getBoolean(CURRENT_LINE);
	// }

	// private void showPrintMargin() {
	// if (fPrintMarginPainter == null) {
	// fPrintMarginPainter = new PrintMarginPainter(getSourceViewer());
	// fPrintMarginPainter.setMarginRulerColor(getColor(PRINT_MARGIN_COLOR));
	// fPrintMarginPainter.setMarginRulerColumn(getPreferenceStore().getInt(PRINT_MARGIN_COLUMN));
	// // fPaintManager.addPainter(fPrintMarginPainter);
	// }
	// }

	// private void hidePrintMargin() {
	// if (fPrintMarginPainter != null) {
	// // fPaintManager.removePainter(fPrintMarginPainter);
	// fPrintMarginPainter.deactivate(true);
	// fPrintMarginPainter.dispose();
	// fPrintMarginPainter = null;
	// }
	// }

	// private boolean isPrintMarginVisible() {
	// IPreferenceStore store = getPreferenceStore();
	// return store.getBoolean(PRINT_MARGIN);
	// }

	private int getTabSize() {
		Preferences preferences = PHPeclipsePlugin.getDefault()
				.getPluginPreferences();
		return preferences.getInt(CODE_FORMATTER_TAB_SIZE);
	}

	private boolean isTabConversionEnabled() {
		IPreferenceStore store = getPreferenceStore();
		return store.getBoolean(SPACES_FOR_TABS);
	}

	private Color getColor(String key) {
		RGB rgb = PreferenceConverter.getColor(getPreferenceStore(), key);
		return getColor(rgb);
	}

	private Color getColor(RGB rgb) {
		JavaTextTools textTools = PHPeclipsePlugin.getDefault()
				.getJavaTextTools();
		return textTools.getColorManager().getColor(rgb);
	}

	// private Color getColor(AnnotationType annotationType) {
	// AnnotationInfo info = (AnnotationInfo)
	// ANNOTATION_MAP.get(annotationType);
	// if (info != null)
	// return getColor(info.fColorPreference);
	// return null;
	// }
	public void dispose() {
		ISourceViewer sourceViewer = getSourceViewer();
		if (sourceViewer instanceof ITextViewerExtension)
			((ITextViewerExtension) sourceViewer)
					.removeVerifyKeyListener(fBracketInserter);
		// if (fPropertyChangeListener != null) {
		// Preferences preferences =
		// PHPeclipsePlugin.getDefault().getPluginPreferences();
		// preferences.removePropertyChangeListener(fPropertyChangeListener);
		// fPropertyChangeListener = null;
		// }
		if (fJavaEditorErrorTickUpdater != null) {
			fJavaEditorErrorTickUpdater.dispose();
			fJavaEditorErrorTickUpdater = null;
		}
		// if (fSelectionHistory != null)
		// fSelectionHistory.dispose();
		// if (fPaintManager != null) {
		// fPaintManager.dispose();
		// fPaintManager = null;
		// }
		if (fActionGroups != null) {
			fActionGroups.dispose();
			fActionGroups = null;
		}
		super.dispose();
	}

	// protected AnnotationType getAnnotationType(String preferenceKey) {
	// Iterator e = ANNOTATION_MAP.keySet().iterator();
	// while (e.hasNext()) {
	// AnnotationType type = (AnnotationType) e.next();
	// AnnotationInfo info = (AnnotationInfo) ANNOTATION_MAP.get(type);
	// if (info != null) {
	// if (preferenceKey.equals(info.fColorPreference)
	// || preferenceKey.equals(info.fEditorPreference)
	// || preferenceKey.equals(info.fOverviewRulerPreference))
	// return type;
	// }
	// }
	// return null;
	// }
	/*
	 * @see AbstractTextEditor#handlePreferenceStoreChanged(PropertyChangeEvent)
	 */
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		try {
			AdaptedSourceViewer asv = (AdaptedSourceViewer) getSourceViewer();
			if (asv != null) {
				String p = event.getProperty();
				if (CLOSE_BRACKETS_PHP.equals(p)) {
					fBracketInserter
							.setCloseBracketsPHPEnabled(getPreferenceStore()
									.getBoolean(p));
					return;
				}
				if (CLOSE_STRINGS_DQ_PHP.equals(p)) {
					fBracketInserter
							.setCloseStringsPHPDQEnabled(getPreferenceStore()
									.getBoolean(p));
					return;
				}
				if (CLOSE_STRINGS_SQ_PHP.equals(p)) {
					fBracketInserter
							.setCloseStringsPHPSQEnabled(getPreferenceStore()
									.getBoolean(p));
					return;
				}
				if (SPACES_FOR_TABS.equals(p)) {
					if (isTabConversionEnabled())
						startTabConversion();
					else
						stopTabConversion();
					return;
				}
				// if (MATCHING_BRACKETS.equals(p)) {
				// if (isBracketHighlightingEnabled())
				// startBracketHighlighting();
				// else
				// stopBracketHighlighting();
				// return;
				// }
				// if (MATCHING_BRACKETS_COLOR.equals(p)) {
				// if (fBracketPainter != null)
				// fBracketPainter.setHighlightColor(getColor(MATCHING_BRACKETS_COLOR));
				// return;
				// }
				// if (CURRENT_LINE.equals(p)) {
				// if (isLineHighlightingEnabled())
				// startLineHighlighting();
				// else
				// stopLineHighlighting();
				// return;
				// }
				// if (CURRENT_LINE_COLOR.equals(p)) {
				// if (fLinePainter != null) {
				// stopLineHighlighting();
				// startLineHighlighting();
				// }
				// return;
				// }
				// if (PRINT_MARGIN.equals(p)) {
				// if (isPrintMarginVisible())
				// showPrintMargin();
				// else
				// hidePrintMargin();
				// return;
				// }
				// if (PRINT_MARGIN_COLOR.equals(p)) {
				// if (fPrintMarginPainter != null)
				// fPrintMarginPainter.setMarginRulerColor(getColor(PRINT_MARGIN_COLOR));
				// return;
				// }
				// if (PRINT_MARGIN_COLUMN.equals(p)) {
				// if (fPrintMarginPainter != null)
				// fPrintMarginPainter.setMarginRulerColumn(getPreferenceStore().getInt(PRINT_MARGIN_COLUMN));
				// return;
				// }
				// if (OVERVIEW_RULER.equals(p)) {
				// if (isOverviewRulerVisible())
				// showOverviewRuler();
				// else
				// hideOverviewRuler();
				// return;
				// }
				// AnnotationType type = getAnnotationType(p);
				// if (type != null) {
				//
				// AnnotationInfo info = (AnnotationInfo)
				// ANNOTATION_MAP.get(type);
				// if (info.fColorPreference.equals(p)) {
				// Color color = getColor(type);
				// if (fProblemPainter != null) {
				// fProblemPainter.setColor(type, color);
				// fProblemPainter.paint(IPainter.CONFIGURATION);
				// }
				// setColorInOverviewRuler(type, color);
				// return;
				// }
				//
				// if (info.fEditorPreference.equals(p)) {
				// if (isAnnotationIndicationEnabled(type))
				// startAnnotationIndication(type);
				// else
				// stopAnnotationIndication(type);
				// return;
				// }
				//
				// if (info.fOverviewRulerPreference.equals(p)) {
				// if (isAnnotationIndicationInOverviewRulerEnabled(type))
				// showAnnotationIndicationInOverviewRuler(type, true);
				// else
				// showAnnotationIndicationInOverviewRuler(type, false);
				// return;
				// }
				// }
				IContentAssistant c = asv.getContentAssistant();
				if (c instanceof ContentAssistant)
					ContentAssistPreference.changeConfiguration(
							(ContentAssistant) c, getPreferenceStore(), event);
			}
		} finally {
			super.handlePreferenceStoreChanged(event);
		}
	}

	/*
	 * @see net.sourceforge.phpdt.internal.ui.javaeditor.JavaEditor#handlePreferencePropertyChanged(org.eclipse.core.runtime.Preferences.PropertyChangeEvent)
	 */
	protected void handlePreferencePropertyChanged(
			org.eclipse.core.runtime.Preferences.PropertyChangeEvent event) {
		AdaptedSourceViewer asv = (AdaptedSourceViewer) getSourceViewer();
		if (asv != null) {
			String p = event.getProperty();
			if (CODE_FORMATTER_TAB_SIZE.equals(p)) {
				asv.updateIndentationPrefixes();
				if (fTabConverter != null)
					fTabConverter.setNumberOfSpacesPerTab(getTabSize());
			}
		}
		super.handlePreferencePropertyChanged(event);
	}

	/**
	 * Handles a property change event describing a change of the php core's
	 * preferences and updates the preference related editor properties.
	 * 
	 * @param event
	 *            the property change event
	 */
	// protected void
	// handlePreferencePropertyChanged(org.eclipse.core.runtime.Preferences.PropertyChangeEvent
	// event) {
	// AdaptedSourceViewer asv = (AdaptedSourceViewer) getSourceViewer();
	// if (asv != null) {
	// String p = event.getProperty();
	// if (CODE_FORMATTER_TAB_SIZE.equals(p)) {
	// asv.updateIndentationPrefixes();
	// if (fTabConverter != null)
	// fTabConverter.setNumberOfSpacesPerTab(getTabSize());
	// }
	// }
	// }
	/*
	 * @see net.sourceforge.phpdt.internal.ui.javaeditor.JavaEditor#createJavaSourceViewer(org.eclipse.swt.widgets.Composite,
	 *      org.eclipse.jface.text.source.IVerticalRuler,
	 *      org.eclipse.jface.text.source.IOverviewRuler, boolean, int)
	 */
	protected ISourceViewer createJavaSourceViewer(Composite parent,
			IVerticalRuler verticalRuler, IOverviewRuler overviewRuler,
			boolean isOverviewRulerVisible, int styles, IPreferenceStore store) {
		return new AdaptedSourceViewer(parent, verticalRuler, overviewRuler,
				isOverviewRulerVisible, styles, store);
	}

	// protected ISourceViewer createJavaSourceViewer(Composite parent,
	// IVerticalRuler ruler, int styles) {
	// return new AdaptedSourceViewer(parent, ruler, styles);
	// }
	private boolean isValidSelection(int offset, int length) {
		IDocumentProvider provider = getDocumentProvider();
		if (provider != null) {
			IDocument document = provider.getDocument(getEditorInput());
			if (document != null) {
				int end = offset + length;
				int documentLength = document.getLength();
				return 0 <= offset && offset <= documentLength && 0 <= end
						&& end <= documentLength;
			}
		}
		return false;
	}

	/*
	 * @see net.sourceforge.phpdt.internal.ui.javaeditor.JavaEditor#getInputElement()
	 */
	protected IJavaElement getInputJavaElement() {
		return PHPeclipsePlugin.getDefault().getWorkingCopyManager()
				.getWorkingCopy(getEditorInput());
	}

	/*
	 * @see AbstractTextEditor#editorContextMenuAboutToShow(IMenuManager)
	 */
	public void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		ActionContext context = new ActionContext(getSelectionProvider()
				.getSelection());
		fContextMenuGroup.setContext(context);
		fContextMenuGroup.fillContextMenu(menu);
		fContextMenuGroup.setContext(null);
	}

	/*
	 * @see JavaEditor#setOutlinePageInput(JavaOutlinePage, IEditorInput)
	 */
	protected void setOutlinePageInput(JavaOutlinePage page, IEditorInput input) {
		if (page != null) {
			IWorkingCopyManager manager = PHPeclipsePlugin.getDefault()
					.getWorkingCopyManager();
			page.setInput(manager.getWorkingCopy(input));
		}
	}

	/*
	 * @see AbstractTextEditor#performSaveOperation(WorkspaceModifyOperation,
	 *      IProgressMonitor)
	 */
	// protected void performSaveOperation(WorkspaceModifyOperation operation,
	// IProgressMonitor progressMonitor) {
	// IDocumentProvider p = getDocumentProvider();
	// if (p instanceof PHPDocumentProvider) {
	// PHPDocumentProvider cp = (PHPDocumentProvider) p;
	// cp.setSavePolicy(fSavePolicy);
	// }
	//
	// try {
	// super.performSaveOperation(operation, progressMonitor);
	// } finally {
	// if (p instanceof PHPDocumentProvider) {
	// PHPDocumentProvider cp = (PHPDocumentProvider) p;
	// cp.setSavePolicy(null);
	// }
	// }
	// }
	/*
	 * @see AbstractTextEditor#doSave(IProgressMonitor)
	 */
	public void doSave(IProgressMonitor progressMonitor) {

		IDocumentProvider p = getDocumentProvider();
		if (p == null) {
			// editor has been closed
			return;
		}

		if (p.isDeleted(getEditorInput())) {

			if (isSaveAsAllowed()) {

				/*
				 * 1GEUSSR: ITPUI:ALL - User should never loose changes made in
				 * the editors. Changed Behavior to make sure that if called
				 * inside a regular save (because of deletion of input element)
				 * there is a way to report back to the caller.
				 */
				performSaveAs(progressMonitor);

			} else {

				/*
				 * 1GF5YOX: ITPJUI:ALL - Save of delete file claims it's still
				 * there Missing resources.
				 */
				Shell shell = getSite().getShell();
				MessageDialog
						.openError(
								shell,
								PHPEditorMessages
										.getString("PHPUnitEditor.error.saving.title1"), PHPEditorMessages.getString("PHPUnitEditor.error.saving.message1")); //$NON-NLS-1$ //$NON-NLS-2$
			}

		} else {
			if (getPreferenceStore().getBoolean(
					PreferenceConstants.EDITOR_P_RTRIM_ON_SAVE)) {
				RTrimAction trimAction = new RTrimAction();
				trimAction.setActiveEditor(null, getSite().getPage()
						.getActiveEditor());
				trimAction.run(null);
			}

			setStatusLineErrorMessage(null);

			updateState(getEditorInput());
			validateState(getEditorInput());

			IWorkingCopyManager manager = PHPeclipsePlugin.getDefault()
					.getWorkingCopyManager();
			ICompilationUnit unit = manager.getWorkingCopy(getEditorInput());

			if (unit != null) {
				synchronized (unit) {
					performSave(false, progressMonitor);
				}
			} else
				performSave(false, progressMonitor);
		}
	}

	public boolean isSaveAsAllowed() {
		return true;
	}

	/**
	 * The compilation unit editor implementation of this
	 * <code>AbstractTextEditor</code> method asks the user for the workspace
	 * path of a file resource and saves the document there. See
	 * http://dev.eclipse.org/bugs/show_bug.cgi?id=6295
	 * 
	 * @param progressMonitor
	 *            the progress monitor
	 */
	protected void performSaveAs(IProgressMonitor progressMonitor) {

		Shell shell = getSite().getShell();
		IEditorInput input = getEditorInput();

		SaveAsDialog dialog = new SaveAsDialog(shell);

		IFile original = (input instanceof IFileEditorInput) ? ((IFileEditorInput) input)
				.getFile()
				: null;
		if (original != null)
			dialog.setOriginalFile(original);

		dialog.create();

		IDocumentProvider provider = getDocumentProvider();
		if (provider == null) {
			// editor has been programmatically closed while the dialog was open
			return;
		}

		if (provider.isDeleted(input) && original != null) {
			String message = PHPEditorMessages
					.getFormattedString(
							"CompilationUnitEditor.warning.save.delete", new Object[] { original.getName() }); //$NON-NLS-1$
			dialog.setErrorMessage(null);
			dialog.setMessage(message, IMessageProvider.WARNING);
		}

		if (dialog.open() == Window.CANCEL) {
			if (progressMonitor != null)
				progressMonitor.setCanceled(true);
			return;
		}

		IPath filePath = dialog.getResult();
		if (filePath == null) {
			if (progressMonitor != null)
				progressMonitor.setCanceled(true);
			return;
		}

		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IFile file = workspaceRoot.getFile(filePath);
		final IEditorInput newInput = new FileEditorInput(file);

		boolean success = false;
		try {

			provider.aboutToChange(newInput);
			getDocumentProvider().saveDocument(progressMonitor, newInput,
					getDocumentProvider().getDocument(getEditorInput()), true);
			success = true;

		} catch (CoreException x) {
			IStatus status = x.getStatus();
			if (status == null || status.getSeverity() != IStatus.CANCEL)
				ErrorDialog
						.openError(
								shell,
								PHPEditorMessages
										.getString("CompilationUnitEditor.error.saving.title2"), PHPEditorMessages.getString("CompilationUnitEditor.error.saving.message2"), x.getStatus()); //$NON-NLS-1$ //$NON-NLS-2$
		} finally {
			provider.changed(newInput);
			if (success)
				setInput(newInput);
		}

		if (progressMonitor != null)
			progressMonitor.setCanceled(!success);
	}

	/*
	 * @see AbstractTextEditor#doSetInput(IEditorInput)
	 */
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		configureTabConverter();
		configureToggleCommentAction();
	}

	// /*
	// * @see
	// net.sourceforge.phpdt.internal.ui.javaeditor.JavaEditor#installOverrideIndicator(boolean)
	// * @since 3.0
	// */
	// protected void installOverrideIndicator(boolean waitForReconcilation) {
	// IAnnotationModel model=
	// getDocumentProvider().getAnnotationModel(getEditorInput());
	// if (!waitForReconcilation)
	// super.installOverrideIndicator(false);
	// else {
	// uninstallOverrideIndicator();
	// IJavaElement inputElement= getInputJavaElement();
	// if (model == null || inputElement == null)
	// return;
	//
	// fOverrideIndicatorManager= new OverrideIndicatorManager(model,
	// inputElement, null);
	// addReconcileListener(fOverrideIndicatorManager);
	// }
	// }
	//
	// /*
	// * @see
	// net.sourceforge.phpdt.internal.ui.javaeditor.JavaEditor#uninstallOverrideIndicator()
	// * @since 3.0
	// */
	// protected void uninstallOverrideIndicator() {
	// if (fOverrideIndicatorManager != null)
	// removeReconcileListener(fOverrideIndicatorManager);
	// super.uninstallOverrideIndicator();
	// }

	/**
	 * Configures the toggle comment action
	 * 
	 * @since 3.0
	 */
	private void configureToggleCommentAction() {
		IAction action = getAction("ToggleComment"); //$NON-NLS-1$
		if (action instanceof ToggleCommentAction) {
			ISourceViewer sourceViewer = getSourceViewer();
			SourceViewerConfiguration configuration = getSourceViewerConfiguration();
			((ToggleCommentAction) action).configure(sourceViewer,
					configuration);
		}
	}

	// private void configureTabConverter() {
	// if (fTabConverter != null) {
	// IDocumentProvider provider = getDocumentProvider();
	// if (provider instanceof PHPDocumentProvider) {
	// PHPDocumentProvider cup = (PHPDocumentProvider) provider;
	// fTabConverter.setLineTracker(cup.createLineTracker(getEditorInput()));
	// }
	// }
	// }
	private void configureTabConverter() {
		if (fTabConverter != null) {
			IDocumentProvider provider = getDocumentProvider();
			if (provider instanceof ICompilationUnitDocumentProvider) {
				ICompilationUnitDocumentProvider cup = (ICompilationUnitDocumentProvider) provider;
				fTabConverter.setLineTracker(cup
						.createLineTracker(getEditorInput()));
			}
		}
	}

	private void startTabConversion() {
		if (fTabConverter == null) {
			fTabConverter = new TabConverter();
			configureTabConverter();
			fTabConverter.setNumberOfSpacesPerTab(getTabSize());
			AdaptedSourceViewer asv = (AdaptedSourceViewer) getSourceViewer();
			asv.addTextConverter(fTabConverter);
			// http://dev.eclipse.org/bugs/show_bug.cgi?id=19270
			asv.updateIndentationPrefixes();
		}
	}

	private void stopTabConversion() {
		if (fTabConverter != null) {
			AdaptedSourceViewer asv = (AdaptedSourceViewer) getSourceViewer();
			asv.removeTextConverter(fTabConverter);
			// http://dev.eclipse.org/bugs/show_bug.cgi?id=19270
			asv.updateIndentationPrefixes();
			fTabConverter = null;
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#performSave(boolean,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void performSave(boolean overwrite,
			IProgressMonitor progressMonitor) {
		// IDocumentProvider p = getDocumentProvider();
		// if (p instanceof PHPDocumentProvider) {
		// PHPDocumentProvider cp = (PHPDocumentProvider) p;
		// cp.setSavePolicy(fSavePolicy);
		// }
		// try {
		// super.performSave(overwrite, progressMonitor);
		// } finally {
		// if (p instanceof PHPDocumentProvider) {
		// PHPDocumentProvider cp = (PHPDocumentProvider) p;
		// cp.setSavePolicy(null);
		// }
		// }

		IDocumentProvider p = getDocumentProvider();
		if (p instanceof ICompilationUnitDocumentProvider) {
			ICompilationUnitDocumentProvider cp = (ICompilationUnitDocumentProvider) p;
			cp.setSavePolicy(fSavePolicy);
		}
		try {
			super.performSave(overwrite, progressMonitor);
		} finally {
			if (p instanceof ICompilationUnitDocumentProvider) {
				ICompilationUnitDocumentProvider cp = (ICompilationUnitDocumentProvider) p;
				cp.setSavePolicy(null);
			}
		}
	}

	/*
	 * @see AbstractTextEditor#doSaveAs
	 */
	public void doSaveAs() {
		if (askIfNonWorkbenchEncodingIsOk()) {
			super.doSaveAs();
		}
	}

	/**
	 * Asks the user if it is ok to store in non-workbench encoding.
	 * 
	 * @return <true>if the user wants to continue
	 */
	private boolean askIfNonWorkbenchEncodingIsOk() {
		IDocumentProvider provider = getDocumentProvider();
		if (provider instanceof IStorageDocumentProvider) {
			IEditorInput input = getEditorInput();
			IStorageDocumentProvider storageProvider = (IStorageDocumentProvider) provider;
			String encoding = storageProvider.getEncoding(input);
			String defaultEncoding = storageProvider.getDefaultEncoding();
			if (encoding != null && !encoding.equals(defaultEncoding)) {
				Shell shell = getSite().getShell();
				String title = PHPEditorMessages
						.getString("PHPUnitEditor.warning.save.nonWorkbenchEncoding.title"); //$NON-NLS-1$
				String msg;
				if (input != null)
					msg = MessageFormat
							.format(
									PHPEditorMessages
											.getString("PHPUnitEditor.warning.save.nonWorkbenchEncoding.message1"),
									new String[] { input.getName(), encoding }); //$NON-NLS-1$
				else
					msg = MessageFormat
							.format(
									PHPEditorMessages
											.getString("PHPUnitEditor.warning.save.nonWorkbenchEncoding.message2"),
									new String[] { encoding }); //$NON-NLS-1$
				return MessageDialog.openQuestion(shell, title, msg);
			}
		}
		return true;
	}

	/*
	 * @see net.sourceforge.phpdt.internal.ui.text.java.IJavaReconcilingListener#aboutToBeReconciled()
	 * @since 3.0
	 */
	public void aboutToBeReconciled() {

		// Notify AST provider
		// PHPeclipsePlugin.getDefault().getASTProvider().aboutToBeReconciled(getInputJavaElement());

		// Notify listeners
		Object[] listeners = fReconcilingListeners.getListeners();
		for (int i = 0, length = listeners.length; i < length; ++i)
			((IJavaReconcilingListener) listeners[i]).aboutToBeReconciled();
	}

	/*
	 * @see net.sourceforge.phpdt.internal.ui.text.java.IJavaReconcilingListener#reconciled(CompilationUnit,
	 *      boolean, IProgressMonitor)
	 * @since 3.0
	 */
	public void reconciled(CompilationUnit ast, boolean forced,
			IProgressMonitor progressMonitor) {

		// Always notify AST provider
		// PHPeclipsePlugin.getDefault().getASTProvider().reconciled(ast,
		// getInputJavaElement());

		// Notify listeners
		// Object[] listeners = fReconcilingListeners.getListeners();
		// for (int i = 0, length= listeners.length; i < length; ++i)
		// ((IJavaReconcilingListener)listeners[i]).reconciled(ast, forced,
		// progressMonitor);

		// Update Java Outline page selection
		if (!forced && !progressMonitor.isCanceled()) {
			Shell shell = getSite().getShell();
			if (shell != null && !shell.isDisposed()) {
				shell.getDisplay().asyncExec(new Runnable() {
					public void run() {
						selectionChanged();
					}
				});
			}
		}
	}

	/**
	 * Returns the updated java element for the old java element.
	 */
	private IJavaElement findElement(IJavaElement element) {
		if (element == null)
			return null;
		IWorkingCopyManager manager = PHPeclipsePlugin.getDefault()
				.getWorkingCopyManager();
		ICompilationUnit unit = manager.getWorkingCopy(getEditorInput());
		if (unit != null) {
			try {
				synchronized (unit) {
					unit.reconcile();
				}
				IJavaElement[] findings = unit.findElements(element);
				if (findings != null && findings.length > 0)
					return findings[0];
			} catch (JavaModelException x) {
				PHPeclipsePlugin.log(x.getStatus());
				// nothing found, be tolerant and go on
			}
		}
		return null;
	}

	/**
	 * Returns the offset of the given Java element.
	 */
	private int getOffset(IJavaElement element) {
		if (element instanceof ISourceReference) {
			ISourceReference sr = (ISourceReference) element;
			try {
				ISourceRange srcRange = sr.getSourceRange();
				if (srcRange != null)
					return srcRange.getOffset();
			} catch (JavaModelException e) {
			}
		}
		return -1;
	}

	/*
	 * @see AbstractTextEditor#restoreSelection()
	 */
	// protected void restoreSelection() {
	// try {
	// if (getSourceViewer() == null || fRememberedSelection == null)
	// return;
	// IJavaElement newElement = findElement(fRememberedElement);
	// int newOffset = getOffset(newElement);
	// int delta = (newOffset > -1 && fRememberedElementOffset > -1) ? newOffset
	// - fRememberedElementOffset : 0;
	// if (isValidSelection(delta + fRememberedSelection.getOffset(),
	// fRememberedSelection.getLength()))
	// selectAndReveal(delta + fRememberedSelection.getOffset(),
	// fRememberedSelection.getLength());
	// } finally {
	// fRememberedSelection = null;
	// fRememberedElement = null;
	// fRememberedElementOffset = -1;
	// }
	// }
	/**
	 * Tells whether this is the active editor in the active page.
	 * 
	 * @return <code>true</code> if this is the active editor in the active
	 *         page
	 * @see IWorkbenchPage#getActiveEditor();
	 */
	protected final boolean isActiveEditor() {
		IWorkbenchWindow window = getSite().getWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		if (page == null)
			return false;
		IEditorPart activeEditor = page.getActiveEditor();
		return activeEditor != null && activeEditor.equals(this);
	}

	/**
	 * Adds the given listener. Has no effect if an identical listener was not
	 * already registered.
	 * 
	 * @param listener
	 *            The reconcile listener to be added
	 * @since 3.0
	 */
	final void addReconcileListener(IJavaReconcilingListener listener) {
		synchronized (fReconcilingListeners) {
			fReconcilingListeners.add(listener);
		}
	}

	/**
	 * Removes the given listener. Has no effect if an identical listener was
	 * not already registered.
	 * 
	 * @param listener
	 *            the reconcile listener to be removed
	 * @since 3.0
	 */
	final void removeReconcileListener(IJavaReconcilingListener listener) {
		synchronized (fReconcilingListeners) {
			fReconcilingListeners.remove(listener);
		}
	}

	protected void updateStateDependentActions() {
		super.updateStateDependentActions();
		fGenerateActionGroup.editorStateChanged();
	}

	/*
	 * @see AbstractTextEditor#rememberSelection()
	 */
	protected void rememberSelection() {
		fRememberedSelection.remember();
	}

	/*
	 * @see AbstractTextEditor#restoreSelection()
	 */
	protected void restoreSelection() {
		fRememberedSelection.restore();
	}

	/*
	 * @see AbstractTextEditor#canHandleMove(IEditorInput, IEditorInput)
	 */
	protected boolean canHandleMove(IEditorInput originalElement,
			IEditorInput movedElement) {

		String oldExtension = ""; //$NON-NLS-1$
		if (originalElement instanceof IFileEditorInput) {
			IFile file = ((IFileEditorInput) originalElement).getFile();
			if (file != null) {
				String ext = file.getFileExtension();
				if (ext != null)
					oldExtension = ext;
			}
		}

		String newExtension = ""; //$NON-NLS-1$
		if (movedElement instanceof IFileEditorInput) {
			IFile file = ((IFileEditorInput) movedElement).getFile();
			if (file != null)
				newExtension = file.getFileExtension();
		}

		return oldExtension.equals(newExtension);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#isPrefQuickDiffAlwaysOn()
	 */
	protected boolean isPrefQuickDiffAlwaysOn() {
		// reestablishes the behaviour from AbstractDecoratedTextEditor which
		// was hacked by JavaEditor
		// to disable the change bar for the class file (attached source) java
		// editor.
		IPreferenceStore store = getPreferenceStore();
		return store
				.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_ALWAYS_ON);
	}

	/*
	 * @see net.sourceforge.phpdt.internal.ui.javaeditor.JavaEditor#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class required) {
		if (SmartBackspaceManager.class.equals(required)) {
			if (getSourceViewer() instanceof JavaSourceViewer) {
				return ((JavaSourceViewer) getSourceViewer())
						.getBackspaceManager();
			}
		}

		return super.getAdapter(required);
	}

	/**
	 * Returns the mutex for the reconciler. See
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=63898 for a description of
	 * the problem.
	 * <p>
	 * TODO remove once the underlying problem is solved.
	 * </p>
	 * 
	 * @return the lock reconcilers may use to synchronize on
	 */
	public Object getReconcilerLock() {
		return fReconcilerLock;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#editorSaved()
	 */
	protected void editorSaved() {
		super.editorSaved();
		ShowExternalPreviewAction a = ShowExternalPreviewAction.getInstance();
		if (a != null) {
			//a.refresh(ShowExternalPreviewAction.PHP_TYPE);
			a.doRun(ShowExternalPreviewAction.PHP_TYPE);
		}
	}
}