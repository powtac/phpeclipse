package net.sourceforge.phpeclipse.phpeditor;

/**********************************************************************
 Copyright (c) 2000, 2002 IBM Corp. and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html

 Contributors:
 IBM Corporation - Initial implementation
 www.phpeclipse.de
 **********************************************************************/
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.BreakIterator;
import java.text.CharacterIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import net.sourceforge.phpdt.core.ICompilationUnit;
import net.sourceforge.phpdt.core.IImportContainer;
import net.sourceforge.phpdt.core.IImportDeclaration;
import net.sourceforge.phpdt.core.IJavaElement;
import net.sourceforge.phpdt.core.IJavaProject;
import net.sourceforge.phpdt.core.IMember;
import net.sourceforge.phpdt.core.ISourceRange;
import net.sourceforge.phpdt.core.ISourceReference;
import net.sourceforge.phpdt.core.JavaCore;
import net.sourceforge.phpdt.core.JavaModelException;
import net.sourceforge.phpdt.core.compiler.ITerminalSymbols;
import net.sourceforge.phpdt.core.compiler.InvalidInputException;
import net.sourceforge.phpdt.internal.compiler.parser.Scanner;
import net.sourceforge.phpdt.internal.compiler.parser.SyntaxError;
import net.sourceforge.phpdt.internal.ui.actions.CompositeActionGroup;
import net.sourceforge.phpdt.internal.ui.actions.FoldingActionGroup;
import net.sourceforge.phpdt.internal.ui.actions.SelectionConverter;
import net.sourceforge.phpdt.internal.ui.text.CustomSourceInformationControl;
import net.sourceforge.phpdt.internal.ui.text.DocumentCharacterIterator;
import net.sourceforge.phpdt.internal.ui.text.HTMLTextPresenter;
import net.sourceforge.phpdt.internal.ui.text.IPHPPartitions;
import net.sourceforge.phpdt.internal.ui.text.JavaWordFinder;
import net.sourceforge.phpdt.internal.ui.text.JavaWordIterator;
import net.sourceforge.phpdt.internal.ui.text.PHPPairMatcher;
import net.sourceforge.phpdt.internal.ui.text.PreferencesAdapter;
import net.sourceforge.phpdt.internal.ui.text.java.hover.JavaExpandHover;
import net.sourceforge.phpdt.internal.ui.viewsupport.ISelectionListenerWithAST;
import net.sourceforge.phpdt.internal.ui.viewsupport.IViewPartInputProvider;
import net.sourceforge.phpdt.internal.ui.viewsupport.SelectionListenerWithASTManager;
import net.sourceforge.phpdt.ui.IContextMenuConstants;
import net.sourceforge.phpdt.ui.JavaUI;
import net.sourceforge.phpdt.ui.PreferenceConstants;
import net.sourceforge.phpdt.ui.actions.GotoMatchingBracketAction;
import net.sourceforge.phpdt.ui.actions.OpenEditorActionGroup;
import net.sourceforge.phpdt.ui.text.JavaTextTools;
import net.sourceforge.phpdt.ui.text.PHPSourceViewerConfiguration;
import net.sourceforge.phpdt.ui.text.folding.IJavaFoldingStructureProvider;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;
import net.sourceforge.phpeclipse.builder.ExternalEditorInput;
import net.sourceforge.phpeclipse.ui.editor.BrowserUtil;
import net.sourceforge.phpeclipse.webbrowser.views.BrowserView;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ISelectionValidator;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextPresentationListener;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITextViewerExtension4;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationRulerColumn;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension2;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.text.source.OverviewRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BidiSegmentEvent;
import org.eclipse.swt.custom.BidiSegmentListener;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.editors.text.DefaultEncodingSupport;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.IEncodingSupport;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.eclipse.ui.texteditor.TextNavigationAction;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.ContentOutline;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.tasklist.TaskList;

/**
 * PHP specific text editor.
 */
public abstract class PHPEditor extends AbstractDecoratedTextEditor implements
		IViewPartInputProvider, IShowInTargetList, IShowInSource {
	// extends StatusTextEditor implements IViewPartInputProvider { // extends
	// TextEditor {

	/**
	 * Internal implementation class for a change listener.
	 * 
	 * @since 3.0
	 */
	protected abstract class AbstractSelectionChangedListener implements
			ISelectionChangedListener {

		/**
		 * Installs this selection changed listener with the given selection
		 * provider. If the selection provider is a post selection provider,
		 * post selection changed events are the preferred choice, otherwise
		 * normal selection changed events are requested.
		 * 
		 * @param selectionProvider
		 */
		public void install(ISelectionProvider selectionProvider) {
			if (selectionProvider == null)
				return;

			if (selectionProvider instanceof IPostSelectionProvider) {
				IPostSelectionProvider provider = (IPostSelectionProvider) selectionProvider;
				provider.addPostSelectionChangedListener(this);
			} else {
				selectionProvider.addSelectionChangedListener(this);
			}
		}

		/**
		 * Removes this selection changed listener from the given selection
		 * provider.
		 * 
		 * @param selectionProvider
		 *            the selection provider
		 */
		public void uninstall(ISelectionProvider selectionProvider) {
			if (selectionProvider == null)
				return;

			if (selectionProvider instanceof IPostSelectionProvider) {
				IPostSelectionProvider provider = (IPostSelectionProvider) selectionProvider;
				provider.removePostSelectionChangedListener(this);
			} else {
				selectionProvider.removeSelectionChangedListener(this);
			}
		}
	}

	/**
	 * Updates the Java outline page selection and this editor's range
	 * indicator.
	 * 
	 * @since 3.0
	 */
	private class EditorSelectionChangedListener extends
			AbstractSelectionChangedListener {

		/*
		 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
		 */
		public void selectionChanged(SelectionChangedEvent event) {
			// XXX: see https://bugs.eclipse.org/bugs/show_bug.cgi?id=56161
			PHPEditor.this.selectionChanged();
		}
	}

	/**
	 * "Smart" runnable for updating the outline page's selection.
	 */
	// class OutlinePageSelectionUpdater implements Runnable {
	//
	// /** Has the runnable already been posted? */
	// private boolean fPosted = false;
	//
	// public OutlinePageSelectionUpdater() {
	// }
	//
	// /*
	// * @see Runnable#run()
	// */
	// public void run() {
	// synchronizeOutlinePageSelection();
	// fPosted = false;
	// }
	//
	// /**
	// * Posts this runnable into the event queue.
	// */
	// public void post() {
	// if (fPosted)
	// return;
	//
	// Shell shell = getSite().getShell();
	// if (shell != null & !shell.isDisposed()) {
	// fPosted = true;
	// shell.getDisplay().asyncExec(this);
	// }
	// }
	// };
	class SelectionChangedListener implements ISelectionChangedListener {
		public void selectionChanged(SelectionChangedEvent event) {
			doSelectionChanged(event);
		}
	};

	/**
	 * Adapts an options {@link java.util.Map}to
	 * {@link org.eclipse.jface.preference.IPreferenceStore}.
	 * <p>
	 * This preference store is read-only i.e. write access throws an
	 * {@link java.lang.UnsupportedOperationException}.
	 * </p>
	 * 
	 * @since 3.0
	 */
	private static class OptionsAdapter implements IPreferenceStore {

		/**
		 * A property change event filter.
		 */
		public interface IPropertyChangeEventFilter {

			/**
			 * Should the given event be filtered?
			 * 
			 * @param event
			 *            The property change event.
			 * @return <code>true</code> iff the given event should be
			 *         filtered.
			 */
			public boolean isFiltered(PropertyChangeEvent event);

		}

		/**
		 * Property change listener. Listens for events in the options Map and
		 * fires a {@link org.eclipse.jface.util.PropertyChangeEvent}on this
		 * adapter with arguments from the received event.
		 */
		private class PropertyChangeListener implements IPropertyChangeListener {

			/**
			 * {@inheritDoc}
			 */
			public void propertyChange(PropertyChangeEvent event) {
				if (getFilter().isFiltered(event))
					return;

				if (event.getNewValue() == null)
					fOptions.remove(event.getProperty());
				else
					fOptions.put(event.getProperty(), event.getNewValue());

				firePropertyChangeEvent(event.getProperty(), event
						.getOldValue(), event.getNewValue());
			}
		}

		/** Listeners on this adapter */
		private ListenerList fListeners = new ListenerList();

		/** Listener on the adapted options Map */
		private IPropertyChangeListener fListener = new PropertyChangeListener();

		/** Adapted options Map */
		private Map fOptions;

		/** Preference store through which events are received. */
		private IPreferenceStore fMockupPreferenceStore;

		/** Property event filter. */
		private IPropertyChangeEventFilter fFilter;

		/**
		 * Initialize with the given options.
		 * 
		 * @param options
		 *            The options to wrap
		 * @param mockupPreferenceStore
		 *            the mock-up preference store
		 * @param filter
		 *            the property change filter
		 */
		public OptionsAdapter(Map options,
				IPreferenceStore mockupPreferenceStore,
				IPropertyChangeEventFilter filter) {
			fMockupPreferenceStore = mockupPreferenceStore;
			fOptions = options;
			setFilter(filter);
		}

		/**
		 * {@inheritDoc}
		 */
		public void addPropertyChangeListener(IPropertyChangeListener listener) {
			if (fListeners.size() == 0)
				fMockupPreferenceStore.addPropertyChangeListener(fListener);
			fListeners.add(listener);
		}

		/**
		 * {@inheritDoc}
		 */
		public void removePropertyChangeListener(
				IPropertyChangeListener listener) {
			fListeners.remove(listener);
			if (fListeners.size() == 0)
				fMockupPreferenceStore.removePropertyChangeListener(fListener);
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean contains(String name) {
			return fOptions.containsKey(name);
		}

		/**
		 * {@inheritDoc}
		 */
		public void firePropertyChangeEvent(String name, Object oldValue,
				Object newValue) {
			PropertyChangeEvent event = new PropertyChangeEvent(this, name,
					oldValue, newValue);
			Object[] listeners = fListeners.getListeners();
			for (int i = 0; i < listeners.length; i++)
				((IPropertyChangeListener) listeners[i]).propertyChange(event);
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean getBoolean(String name) {
			boolean value = BOOLEAN_DEFAULT_DEFAULT;
			String s = (String) fOptions.get(name);
			if (s != null)
				value = s.equals(TRUE);
			return value;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean getDefaultBoolean(String name) {
			return BOOLEAN_DEFAULT_DEFAULT;
		}

		/**
		 * {@inheritDoc}
		 */
		public double getDefaultDouble(String name) {
			return DOUBLE_DEFAULT_DEFAULT;
		}

		/**
		 * {@inheritDoc}
		 */
		public float getDefaultFloat(String name) {
			return FLOAT_DEFAULT_DEFAULT;
		}

		/**
		 * {@inheritDoc}
		 */
		public int getDefaultInt(String name) {
			return INT_DEFAULT_DEFAULT;
		}

		/**
		 * {@inheritDoc}
		 */
		public long getDefaultLong(String name) {
			return LONG_DEFAULT_DEFAULT;
		}

		/**
		 * {@inheritDoc}
		 */
		public String getDefaultString(String name) {
			return STRING_DEFAULT_DEFAULT;
		}

		/**
		 * {@inheritDoc}
		 */
		public double getDouble(String name) {
			double value = DOUBLE_DEFAULT_DEFAULT;
			String s = (String) fOptions.get(name);
			if (s != null) {
				try {
					value = new Double(s).doubleValue();
				} catch (NumberFormatException e) {
				}
			}
			return value;
		}

		/**
		 * {@inheritDoc}
		 */
		public float getFloat(String name) {
			float value = FLOAT_DEFAULT_DEFAULT;
			String s = (String) fOptions.get(name);
			if (s != null) {
				try {
					value = new Float(s).floatValue();
				} catch (NumberFormatException e) {
				}
			}
			return value;
		}

		/**
		 * {@inheritDoc}
		 */
		public int getInt(String name) {
			int value = INT_DEFAULT_DEFAULT;
			String s = (String) fOptions.get(name);
			if (s != null) {
				try {
					value = new Integer(s).intValue();
				} catch (NumberFormatException e) {
				}
			}
			return value;
		}

		/**
		 * {@inheritDoc}
		 */
		public long getLong(String name) {
			long value = LONG_DEFAULT_DEFAULT;
			String s = (String) fOptions.get(name);
			if (s != null) {
				try {
					value = new Long(s).longValue();
				} catch (NumberFormatException e) {
				}
			}
			return value;
		}

		/**
		 * {@inheritDoc}
		 */
		public String getString(String name) {
			String value = (String) fOptions.get(name);
			if (value == null)
				value = STRING_DEFAULT_DEFAULT;
			return value;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean isDefault(String name) {
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean needsSaving() {
			return !fOptions.isEmpty();
		}

		/**
		 * {@inheritDoc}
		 */
		public void putValue(String name, String value) {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 */
		public void setDefault(String name, double value) {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 */
		public void setDefault(String name, float value) {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 */
		public void setDefault(String name, int value) {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 */
		public void setDefault(String name, long value) {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 */
		public void setDefault(String name, String defaultObject) {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 */
		public void setDefault(String name, boolean value) {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 */
		public void setToDefault(String name) {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 */
		public void setValue(String name, double value) {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 */
		public void setValue(String name, float value) {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 */
		public void setValue(String name, int value) {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 */
		public void setValue(String name, long value) {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 */
		public void setValue(String name, String value) {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 */
		public void setValue(String name, boolean value) {
			throw new UnsupportedOperationException();
		}

		/**
		 * Returns the adapted options Map.
		 * 
		 * @return Returns the adapted options Map.
		 */
		public Map getOptions() {
			return fOptions;
		}

		/**
		 * Returns the mock-up preference store, events are received through
		 * this preference store.
		 * 
		 * @return Returns the mock-up preference store.
		 */
		public IPreferenceStore getMockupPreferenceStore() {
			return fMockupPreferenceStore;
		}

		/**
		 * Set the event filter to the given filter.
		 * 
		 * @param filter
		 *            The new filter.
		 */
		public void setFilter(IPropertyChangeEventFilter filter) {
			fFilter = filter;
		}

		/**
		 * Returns the event filter.
		 * 
		 * @return The event filter.
		 */
		public IPropertyChangeEventFilter getFilter() {
			return fFilter;
		}
	}

	/*
	 * Link mode.
	 */
	// class MouseClickListener implements KeyListener, MouseListener,
	// MouseMoveListener, FocusListener, PaintListener,
	// IPropertyChangeListener, IDocumentListener, ITextInputListener {
	//
	// /** The session is active. */
	// private boolean fActive;
	//
	// /** The currently active style range. */
	// private IRegion fActiveRegion;
	//
	// /** The currently active style range as position. */
	// private Position fRememberedPosition;
	//
	// /** The hand cursor. */
	// private Cursor fCursor;
	//
	// /** The link color. */
	// private Color fColor;
	//
	// /** The key modifier mask. */
	// private int fKeyModifierMask;
	//
	// public void deactivate() {
	// deactivate(false);
	// }
	//
	// public void deactivate(boolean redrawAll) {
	// if (!fActive)
	// return;
	//
	// repairRepresentation(redrawAll);
	// fActive = false;
	// }
	//
	// public void install() {
	//
	// ISourceViewer sourceViewer = getSourceViewer();
	// if (sourceViewer == null)
	// return;
	//
	// StyledText text = sourceViewer.getTextWidget();
	// if (text == null || text.isDisposed())
	// return;
	//
	// updateColor(sourceViewer);
	//
	// sourceViewer.addTextInputListener(this);
	//
	// IDocument document = sourceViewer.getDocument();
	// if (document != null)
	// document.addDocumentListener(this);
	//
	// text.addKeyListener(this);
	// text.addMouseListener(this);
	// text.addMouseMoveListener(this);
	// text.addFocusListener(this);
	// text.addPaintListener(this);
	//
	// updateKeyModifierMask();
	//
	// IPreferenceStore preferenceStore = getPreferenceStore();
	// preferenceStore.addPropertyChangeListener(this);
	// }
	//
	// private void updateKeyModifierMask() {
	// String modifiers =
	// getPreferenceStore().getString(BROWSER_LIKE_LINKS_KEY_MODIFIER);
	// fKeyModifierMask = computeStateMask(modifiers);
	// if (fKeyModifierMask == -1) {
	// // Fallback to stored state mask
	// fKeyModifierMask =
	// getPreferenceStore().getInt(BROWSER_LIKE_LINKS_KEY_MODIFIER_MASK);
	// }
	// ;
	// }
	//
	// private int computeStateMask(String modifiers) {
	// if (modifiers == null)
	// return -1;
	//
	// if (modifiers.length() == 0)
	// return SWT.NONE;
	//
	// int stateMask = 0;
	// StringTokenizer modifierTokenizer = new StringTokenizer(modifiers,
	// ",;.:+-*
	// "); //$NON-NLS-1$
	// while (modifierTokenizer.hasMoreTokens()) {
	// int modifier =
	// EditorUtility.findLocalizedModifier(modifierTokenizer.nextToken());
	// if (modifier == 0 || (stateMask & modifier) == modifier)
	// return -1;
	// stateMask = stateMask | modifier;
	// }
	// return stateMask;
	// }
	//
	// public void uninstall() {
	//
	// if (fColor != null) {
	// fColor.dispose();
	// fColor = null;
	// }
	//
	// if (fCursor != null) {
	// fCursor.dispose();
	// fCursor = null;
	// }
	//
	// ISourceViewer sourceViewer = getSourceViewer();
	// if (sourceViewer == null)
	// return;
	//
	// sourceViewer.removeTextInputListener(this);
	//
	// IDocument document = sourceViewer.getDocument();
	// if (document != null)
	// document.removeDocumentListener(this);
	//
	// IPreferenceStore preferenceStore = getPreferenceStore();
	// if (preferenceStore != null)
	// preferenceStore.removePropertyChangeListener(this);
	//
	// StyledText text = sourceViewer.getTextWidget();
	// if (text == null || text.isDisposed())
	// return;
	//
	// text.removeKeyListener(this);
	// text.removeMouseListener(this);
	// text.removeMouseMoveListener(this);
	// text.removeFocusListener(this);
	// text.removePaintListener(this);
	// }
	//
	// /*
	// * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	// */
	// public void propertyChange(PropertyChangeEvent event) {
	// if (event.getProperty().equals(PHPEditor.LINK_COLOR)) {
	// ISourceViewer viewer = getSourceViewer();
	// if (viewer != null)
	// updateColor(viewer);
	// } else if (event.getProperty().equals(BROWSER_LIKE_LINKS_KEY_MODIFIER)) {
	// updateKeyModifierMask();
	// }
	// }
	//
	// private void updateColor(ISourceViewer viewer) {
	// if (fColor != null)
	// fColor.dispose();
	//
	// StyledText text = viewer.getTextWidget();
	// if (text == null || text.isDisposed())
	// return;
	//
	// Display display = text.getDisplay();
	// fColor = createColor(getPreferenceStore(), PHPEditor.LINK_COLOR,
	// display);
	// }
	//
	// /**
	// * Creates a color from the information stored in the given preference
	// store. Returns <code>null</code> if there is no such
	// * information available.
	// */
	// private Color createColor(IPreferenceStore store, String key, Display
	// display) {
	//
	// RGB rgb = null;
	//
	// if (store.contains(key)) {
	//
	// if (store.isDefault(key))
	// rgb = PreferenceConverter.getDefaultColor(store, key);
	// else
	// rgb = PreferenceConverter.getColor(store, key);
	//
	// if (rgb != null)
	// return new Color(display, rgb);
	// }
	//
	// return null;
	// }
	//
	// private void repairRepresentation() {
	// repairRepresentation(false);
	// }
	//
	// private void repairRepresentation(boolean redrawAll) {
	//
	// if (fActiveRegion == null)
	// return;
	//
	// ISourceViewer viewer = getSourceViewer();
	// if (viewer != null) {
	// resetCursor(viewer);
	//
	// int offset = fActiveRegion.getOffset();
	// int length = fActiveRegion.getLength();
	//
	// // remove style
	// if (!redrawAll && viewer instanceof ITextViewerExtension2)
	// ((ITextViewerExtension2) viewer).invalidateTextPresentation(offset,
	// length);
	// else
	// viewer.invalidateTextPresentation();
	//
	// // remove underline
	// if (viewer instanceof ITextViewerExtension3) {
	// ITextViewerExtension3 extension = (ITextViewerExtension3) viewer;
	// offset = extension.modelOffset2WidgetOffset(offset);
	// } else {
	// offset -= viewer.getVisibleRegion().getOffset();
	// }
	//
	// StyledText text = viewer.getTextWidget();
	// try {
	// text.redrawRange(offset, length, true);
	// } catch (IllegalArgumentException x) {
	// PHPeclipsePlugin.log(x);
	// }
	// }
	//
	// fActiveRegion = null;
	// }
	//
	// // will eventually be replaced by a method provided by jdt.core
	// private IRegion selectWord(IDocument document, int anchor) {
	//
	// try {
	// int offset = anchor;
	// char c;
	//
	// while (offset >= 0) {
	// c = document.getChar(offset);
	// if (!Scanner.isPHPIdentifierPart(c))
	// break;
	// --offset;
	// }
	//
	// int start = offset;
	//
	// offset = anchor;
	// int length = document.getLength();
	//
	// while (offset < length) {
	// c = document.getChar(offset);
	// if (!Scanner.isPHPIdentifierPart(c))
	// break;
	// ++offset;
	// }
	//
	// int end = offset;
	//
	// if (start == end)
	// return new Region(start, 0);
	// else
	// return new Region(start + 1, end - start - 1);
	//
	// } catch (BadLocationException x) {
	// return null;
	// }
	// }
	//
	// IRegion getCurrentTextRegion(ISourceViewer viewer) {
	//
	// int offset = getCurrentTextOffset(viewer);
	// if (offset == -1)
	// return null;
	//
	// return null;
	// // IJavaElement input= SelectionConverter.getInput(PHPEditor.this);
	// // if (input == null)
	// // return null;
	// //
	// // try {
	// //
	// // IJavaElement[] elements= null;
	// // synchronized (input) {
	// // elements= ((ICodeAssist) input).codeSelect(offset, 0);
	// // }
	// //
	// // if (elements == null || elements.length == 0)
	// // return null;
	// //
	// // return selectWord(viewer.getDocument(), offset);
	// //
	// // } catch (JavaModelException e) {
	// // return null;
	// // }
	// }
	//
	// private int getCurrentTextOffset(ISourceViewer viewer) {
	//
	// try {
	// StyledText text = viewer.getTextWidget();
	// if (text == null || text.isDisposed())
	// return -1;
	//
	// Display display = text.getDisplay();
	// Point absolutePosition = display.getCursorLocation();
	// Point relativePosition = text.toControl(absolutePosition);
	//
	// int widgetOffset = text.getOffsetAtLocation(relativePosition);
	// if (viewer instanceof ITextViewerExtension3) {
	// ITextViewerExtension3 extension = (ITextViewerExtension3) viewer;
	// return extension.widgetOffset2ModelOffset(widgetOffset);
	// } else {
	// return widgetOffset + viewer.getVisibleRegion().getOffset();
	// }
	//
	// } catch (IllegalArgumentException e) {
	// return -1;
	// }
	// }
	//
	// private void highlightRegion(ISourceViewer viewer, IRegion region) {
	//
	// if (region.equals(fActiveRegion))
	// return;
	//
	// repairRepresentation();
	//
	// StyledText text = viewer.getTextWidget();
	// if (text == null || text.isDisposed())
	// return;
	//
	// // highlight region
	// int offset = 0;
	// int length = 0;
	//
	// if (viewer instanceof ITextViewerExtension3) {
	// ITextViewerExtension3 extension = (ITextViewerExtension3) viewer;
	// IRegion widgetRange = extension.modelRange2WidgetRange(region);
	// if (widgetRange == null)
	// return;
	//
	// offset = widgetRange.getOffset();
	// length = widgetRange.getLength();
	//
	// } else {
	// offset = region.getOffset() - viewer.getVisibleRegion().getOffset();
	// length = region.getLength();
	// }
	//
	// StyleRange oldStyleRange = text.getStyleRangeAtOffset(offset);
	// Color foregroundColor = fColor;
	// Color backgroundColor = oldStyleRange == null ? text.getBackground() :
	// oldStyleRange.background;
	// StyleRange styleRange = new StyleRange(offset, length, foregroundColor,
	// backgroundColor);
	// text.setStyleRange(styleRange);
	//
	// // underline
	// text.redrawRange(offset, length, true);
	//
	// fActiveRegion = region;
	// }
	//
	// private void activateCursor(ISourceViewer viewer) {
	// StyledText text = viewer.getTextWidget();
	// if (text == null || text.isDisposed())
	// return;
	// Display display = text.getDisplay();
	// if (fCursor == null)
	// fCursor = new Cursor(display, SWT.CURSOR_HAND);
	// text.setCursor(fCursor);
	// }
	//
	// private void resetCursor(ISourceViewer viewer) {
	// StyledText text = viewer.getTextWidget();
	// if (text != null && !text.isDisposed())
	// text.setCursor(null);
	//
	// if (fCursor != null) {
	// fCursor.dispose();
	// fCursor = null;
	// }
	// }
	//
	// /*
	// * @see
	// org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
	// */
	// public void keyPressed(KeyEvent event) {
	//
	// if (fActive) {
	// deactivate();
	// return;
	// }
	//
	// if (event.keyCode != fKeyModifierMask) {
	// deactivate();
	// return;
	// }
	//
	// fActive = true;
	//
	// // removed for #25871
	// //
	// // ISourceViewer viewer= getSourceViewer();
	// // if (viewer == null)
	// // return;
	// //
	// // IRegion region= getCurrentTextRegion(viewer);
	// // if (region == null)
	// // return;
	// //
	// // highlightRegion(viewer, region);
	// // activateCursor(viewer);
	// }
	//
	// /*
	// * @see
	// org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
	// */
	// public void keyReleased(KeyEvent event) {
	//
	// if (!fActive)
	// return;
	//
	// deactivate();
	// }
	//
	// /*
	// * @see
	// org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
	// */
	// public void mouseDoubleClick(MouseEvent e) {
	// }
	//
	// /*
	// * @see
	// org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
	// */
	// public void mouseDown(MouseEvent event) {
	//
	// if (!fActive)
	// return;
	//
	// if (event.stateMask != fKeyModifierMask) {
	// deactivate();
	// return;
	// }
	//
	// if (event.button != 1) {
	// deactivate();
	// return;
	// }
	// }
	//
	// /*
	// * @see
	// org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
	// */
	// public void mouseUp(MouseEvent e) {
	//
	// if (!fActive)
	// return;
	//
	// if (e.button != 1) {
	// deactivate();
	// return;
	// }
	//
	// boolean wasActive = fCursor != null;
	//
	// deactivate();
	//
	// if (wasActive) {
	// IAction action = getAction("OpenEditor"); //$NON-NLS-1$
	// if (action != null)
	// action.run();
	// }
	// }
	//
	// /*
	// * @see
	// org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
	// */
	// public void mouseMove(MouseEvent event) {
	//
	// if (event.widget instanceof Control && !((Control)
	// event.widget).isFocusControl()) {
	// deactivate();
	// return;
	// }
	//
	// if (!fActive) {
	// if (event.stateMask != fKeyModifierMask)
	// return;
	// // modifier was already pressed
	// fActive = true;
	// }
	//
	// ISourceViewer viewer = getSourceViewer();
	// if (viewer == null) {
	// deactivate();
	// return;
	// }
	//
	// StyledText text = viewer.getTextWidget();
	// if (text == null || text.isDisposed()) {
	// deactivate();
	// return;
	// }
	//
	// if ((event.stateMask & SWT.BUTTON1) != 0 && text.getSelectionCount() !=
	// 0)
	// {
	// deactivate();
	// return;
	// }
	//
	// IRegion region = getCurrentTextRegion(viewer);
	// if (region == null || region.getLength() == 0) {
	// repairRepresentation();
	// return;
	// }
	//
	// highlightRegion(viewer, region);
	// activateCursor(viewer);
	// }
	//
	// /*
	// * @see
	// org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
	// */
	// public void focusGained(FocusEvent e) {
	// }
	//
	// /*
	// * @see
	// org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
	// */
	// public void focusLost(FocusEvent event) {
	// deactivate();
	// }
	//
	// /*
	// * @see
	// org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
	// */
	// public void documentAboutToBeChanged(DocumentEvent event) {
	// if (fActive && fActiveRegion != null) {
	// fRememberedPosition = new Position(fActiveRegion.getOffset(),
	// fActiveRegion.getLength());
	// try {
	// event.getDocument().addPosition(fRememberedPosition);
	// } catch (BadLocationException x) {
	// fRememberedPosition = null;
	// }
	// }
	// }
	//
	// /*
	// * @see
	// org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
	// */
	// public void documentChanged(DocumentEvent event) {
	// if (fRememberedPosition != null && !fRememberedPosition.isDeleted()) {
	// event.getDocument().removePosition(fRememberedPosition);
	// fActiveRegion = new Region(fRememberedPosition.getOffset(),
	// fRememberedPosition.getLength());
	// }
	// fRememberedPosition = null;
	//
	// ISourceViewer viewer = getSourceViewer();
	// if (viewer != null) {
	// StyledText widget = viewer.getTextWidget();
	// if (widget != null && !widget.isDisposed()) {
	// widget.getDisplay().asyncExec(new Runnable() {
	// public void run() {
	// deactivate();
	// }
	// });
	// }
	// }
	// }
	//
	// /*
	// * @see
	// org.eclipse.jface.text.ITextInputListener#inputDocumentAboutToBeChanged(org.eclipse.jface.text.IDocument,
	// * org.eclipse.jface.text.IDocument)
	// */
	// public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument
	// newInput) {
	// if (oldInput == null)
	// return;
	// deactivate();
	// oldInput.removeDocumentListener(this);
	// }
	//
	// /*
	// * @see
	// org.eclipse.jface.text.ITextInputListener#inputDocumentChanged(org.eclipse.jface.text.IDocument,
	// * org.eclipse.jface.text.IDocument)
	// */
	// public void inputDocumentChanged(IDocument oldInput, IDocument newInput)
	// {
	// if (newInput == null)
	// return;
	// newInput.addDocumentListener(this);
	// }
	//
	// /*
	// * @see PaintListener#paintControl(PaintEvent)
	// */
	// public void paintControl(PaintEvent event) {
	// if (fActiveRegion == null)
	// return;
	//
	// ISourceViewer viewer = getSourceViewer();
	// if (viewer == null)
	// return;
	//
	// StyledText text = viewer.getTextWidget();
	// if (text == null || text.isDisposed())
	// return;
	//
	// int offset = 0;
	// int length = 0;
	//
	// if (viewer instanceof ITextViewerExtension3) {
	//
	// ITextViewerExtension3 extension = (ITextViewerExtension3) viewer;
	// IRegion widgetRange = extension.modelRange2WidgetRange(new Region(offset,
	// length));
	// if (widgetRange == null)
	// return;
	//
	// offset = widgetRange.getOffset();
	// length = widgetRange.getLength();
	//
	// } else {
	//
	// IRegion region = viewer.getVisibleRegion();
	// if (!includes(region, fActiveRegion))
	// return;
	//
	// offset = fActiveRegion.getOffset() - region.getOffset();
	// length = fActiveRegion.getLength();
	// }
	//
	// // support for bidi
	// Point minLocation = getMinimumLocation(text, offset, length);
	// Point maxLocation = getMaximumLocation(text, offset, length);
	//
	// int x1 = minLocation.x;
	// int x2 = minLocation.x + maxLocation.x - minLocation.x - 1;
	// int y = minLocation.y + text.getLineHeight() - 1;
	//
	// GC gc = event.gc;
	// if (fColor != null && !fColor.isDisposed())
	// gc.setForeground(fColor);
	// gc.drawLine(x1, y, x2, y);
	// }
	//
	// private boolean includes(IRegion region, IRegion position) {
	// return position.getOffset() >= region.getOffset()
	// && position.getOffset() + position.getLength() <= region.getOffset() +
	// region.getLength();
	// }
	//
	// private Point getMinimumLocation(StyledText text, int offset, int length)
	// {
	// Point minLocation = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
	//
	// for (int i = 0; i <= length; i++) {
	// Point location = text.getLocationAtOffset(offset + i);
	//
	// if (location.x < minLocation.x)
	// minLocation.x = location.x;
	// if (location.y < minLocation.y)
	// minLocation.y = location.y;
	// }
	//
	// return minLocation;
	// }
	//
	// private Point getMaximumLocation(StyledText text, int offset, int length)
	// {
	// Point maxLocation = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);
	//
	// for (int i = 0; i <= length; i++) {
	// Point location = text.getLocationAtOffset(offset + i);
	//
	// if (location.x > maxLocation.x)
	// maxLocation.x = location.x;
	// if (location.y > maxLocation.y)
	// maxLocation.y = location.y;
	// }
	//
	// return maxLocation;
	// }
	// };
	/*
	 * Link mode.
	 */
	class MouseClickListener implements KeyListener, MouseListener,
			MouseMoveListener, FocusListener, PaintListener,
			IPropertyChangeListener, IDocumentListener, ITextInputListener,
			ITextPresentationListener {

		/** The session is active. */
		private boolean fActive;

		/** The currently active style range. */
		private IRegion fActiveRegion;

		/** The currently active style range as position. */
		private Position fRememberedPosition;

		/** The hand cursor. */
		private Cursor fCursor;

		/** The link color. */
		private Color fColor;

		/** The key modifier mask. */
		private int fKeyModifierMask;

		public void deactivate() {
			deactivate(false);
		}

		public void deactivate(boolean redrawAll) {
			if (!fActive)
				return;

			repairRepresentation(redrawAll);
			fActive = false;
		}

		public void install() {
			ISourceViewer sourceViewer = getSourceViewer();
			if (sourceViewer == null)
				return;

			StyledText text = sourceViewer.getTextWidget();
			if (text == null || text.isDisposed())
				return;

			updateColor(sourceViewer);

			sourceViewer.addTextInputListener(this);

			IDocument document = sourceViewer.getDocument();
			if (document != null)
				document.addDocumentListener(this);

			text.addKeyListener(this);
			text.addMouseListener(this);
			text.addMouseMoveListener(this);
			text.addFocusListener(this);
			text.addPaintListener(this);

			((ITextViewerExtension4) sourceViewer)
					.addTextPresentationListener(this);

			updateKeyModifierMask();

			IPreferenceStore preferenceStore = getPreferenceStore();
			preferenceStore.addPropertyChangeListener(this);
		}

		private void updateKeyModifierMask() {
			String modifiers = getPreferenceStore().getString(
					BROWSER_LIKE_LINKS_KEY_MODIFIER);
			fKeyModifierMask = computeStateMask(modifiers);
			if (fKeyModifierMask == -1) {
				// Fall back to stored state mask
				fKeyModifierMask = getPreferenceStore().getInt(
						BROWSER_LIKE_LINKS_KEY_MODIFIER_MASK);
			}
		}

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

		public void uninstall() {

			if (fColor != null) {
				fColor.dispose();
				fColor = null;
			}

			if (fCursor != null) {
				fCursor.dispose();
				fCursor = null;
			}

			ISourceViewer sourceViewer = getSourceViewer();
			if (sourceViewer != null)
				sourceViewer.removeTextInputListener(this);

			IDocumentProvider documentProvider = getDocumentProvider();
			if (documentProvider != null) {
				IDocument document = documentProvider
						.getDocument(getEditorInput());
				if (document != null)
					document.removeDocumentListener(this);
			}

			IPreferenceStore preferenceStore = getPreferenceStore();
			if (preferenceStore != null)
				preferenceStore.removePropertyChangeListener(this);

			if (sourceViewer == null)
				return;

			StyledText text = sourceViewer.getTextWidget();
			if (text == null || text.isDisposed())
				return;

			text.removeKeyListener(this);
			text.removeMouseListener(this);
			text.removeMouseMoveListener(this);
			text.removeFocusListener(this);
			text.removePaintListener(this);

			((ITextViewerExtension4) sourceViewer)
					.removeTextPresentationListener(this);
		}

		/*
		 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(PHPEditor.LINK_COLOR)) {
				ISourceViewer viewer = getSourceViewer();
				if (viewer != null)
					updateColor(viewer);
			} else if (event.getProperty().equals(
					BROWSER_LIKE_LINKS_KEY_MODIFIER)) {
				updateKeyModifierMask();
			}
		}

		private void updateColor(ISourceViewer viewer) {
			if (fColor != null)
				fColor.dispose();

			StyledText text = viewer.getTextWidget();
			if (text == null || text.isDisposed())
				return;

			Display display = text.getDisplay();
			fColor = createColor(getPreferenceStore(), PHPEditor.LINK_COLOR,
					display);
		}

		/**
		 * Creates a color from the information stored in the given preference
		 * store.
		 * 
		 * @param store
		 *            the preference store
		 * @param key
		 *            the key
		 * @param display
		 *            the display
		 * @return the color or <code>null</code> if there is no such
		 *         information available
		 */
		private Color createColor(IPreferenceStore store, String key,
				Display display) {

			RGB rgb = null;

			if (store.contains(key)) {

				if (store.isDefault(key))
					rgb = PreferenceConverter.getDefaultColor(store, key);
				else
					rgb = PreferenceConverter.getColor(store, key);

				if (rgb != null)
					return new Color(display, rgb);
			}

			return null;
		}

		private void repairRepresentation() {
			repairRepresentation(false);
		}

		private void repairRepresentation(boolean redrawAll) {

			if (fActiveRegion == null)
				return;

			int offset = fActiveRegion.getOffset();
			int length = fActiveRegion.getLength();
			fActiveRegion = null;

			ISourceViewer viewer = getSourceViewer();
			if (viewer != null) {

				resetCursor(viewer);

				// Invalidate ==> remove applied text presentation
				if (!redrawAll && viewer instanceof ITextViewerExtension2)
					((ITextViewerExtension2) viewer)
							.invalidateTextPresentation(offset, length);
				else
					viewer.invalidateTextPresentation();

				// Remove underline
				if (viewer instanceof ITextViewerExtension5) {
					ITextViewerExtension5 extension = (ITextViewerExtension5) viewer;
					offset = extension.modelOffset2WidgetOffset(offset);
				} else {
					offset -= viewer.getVisibleRegion().getOffset();
				}
				try {
					StyledText text = viewer.getTextWidget();

					text.redrawRange(offset, length, false);
				} catch (IllegalArgumentException x) {
					// JavaPlugin.log(x);
				}
			}
		}

		// will eventually be replaced by a method provided by jdt.core
		private IRegion selectWord(IDocument document, int anchor) {

			try {
				int offset = anchor;
				char c;

				while (offset >= 0) {
					c = document.getChar(offset);
					if (!Scanner.isPHPIdentifierPart(c) && c != '$')
						break;
					--offset;
				}

				int start = offset;

				offset = anchor;
				int length = document.getLength();

				while (offset < length) {
					c = document.getChar(offset);
					if (!Scanner.isPHPIdentifierPart(c) && c != '$')
						break;
					++offset;
				}

				int end = offset;

				if (start == end)
					return new Region(start, 0);
				else
					return new Region(start + 1, end - start - 1);

			} catch (BadLocationException x) {
				return null;
			}
		}

		IRegion getCurrentTextRegion(ISourceViewer viewer) {

			int offset = getCurrentTextOffset(viewer);
			if (offset == -1)
				return null;

			IJavaElement input = SelectionConverter.getInput(PHPEditor.this);
			if (input == null)
				return null;

			// try {

			// IJavaElement[] elements= null;
			// synchronized (input) {
			// elements= ((ICodeAssist) input).codeSelect(offset, 0);
			// }
			//
			// if (elements == null || elements.length == 0)
			// return null;

			return selectWord(viewer.getDocument(), offset);

			// } catch (JavaModelException e) {
			// return null;
			// }
		}

		private int getCurrentTextOffset(ISourceViewer viewer) {

			try {
				StyledText text = viewer.getTextWidget();
				if (text == null || text.isDisposed())
					return -1;

				Display display = text.getDisplay();
				Point absolutePosition = display.getCursorLocation();
				Point relativePosition = text.toControl(absolutePosition);

				int widgetOffset = text.getOffsetAtLocation(relativePosition);
				if (viewer instanceof ITextViewerExtension5) {
					ITextViewerExtension5 extension = (ITextViewerExtension5) viewer;
					return extension.widgetOffset2ModelOffset(widgetOffset);
				} else {
					return widgetOffset + viewer.getVisibleRegion().getOffset();
				}

			} catch (IllegalArgumentException e) {
				return -1;
			}
		}

		public void applyTextPresentation(TextPresentation textPresentation) {
			if (fActiveRegion == null)
				return;
			IRegion region = textPresentation.getExtent();
			if (fActiveRegion.getOffset() + fActiveRegion.getLength() >= region
					.getOffset()
					&& region.getOffset() + region.getLength() > fActiveRegion
							.getOffset())
				textPresentation.mergeStyleRange(new StyleRange(fActiveRegion
						.getOffset(), fActiveRegion.getLength(), fColor, null));
		}

		private void highlightRegion(ISourceViewer viewer, IRegion region) {

			if (region.equals(fActiveRegion))
				return;

			repairRepresentation();

			StyledText text = viewer.getTextWidget();
			if (text == null || text.isDisposed())
				return;

			// Underline
			int offset = 0;
			int length = 0;
			if (viewer instanceof ITextViewerExtension5) {
				ITextViewerExtension5 extension = (ITextViewerExtension5) viewer;
				IRegion widgetRange = extension.modelRange2WidgetRange(region);
				if (widgetRange == null)
					return;

				offset = widgetRange.getOffset();
				length = widgetRange.getLength();

			} else {
				offset = region.getOffset()
						- viewer.getVisibleRegion().getOffset();
				length = region.getLength();
			}
			text.redrawRange(offset, length, false);

			// Invalidate region ==> apply text presentation
			fActiveRegion = region;
			if (viewer instanceof ITextViewerExtension2)
				((ITextViewerExtension2) viewer).invalidateTextPresentation(
						region.getOffset(), region.getLength());
			else
				viewer.invalidateTextPresentation();
		}

		private void activateCursor(ISourceViewer viewer) {
			StyledText text = viewer.getTextWidget();
			if (text == null || text.isDisposed())
				return;
			Display display = text.getDisplay();
			if (fCursor == null)
				fCursor = new Cursor(display, SWT.CURSOR_HAND);
			text.setCursor(fCursor);
		}

		private void resetCursor(ISourceViewer viewer) {
			StyledText text = viewer.getTextWidget();
			if (text != null && !text.isDisposed())
				text.setCursor(null);

			if (fCursor != null) {
				fCursor.dispose();
				fCursor = null;
			}
		}

		/*
		 * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
		 */
		public void keyPressed(KeyEvent event) {

			if (fActive) {
				deactivate();
				return;
			}

			if (event.keyCode != fKeyModifierMask) {
				deactivate();
				return;
			}

			fActive = true;

			// removed for #25871
			//
			// ISourceViewer viewer= getSourceViewer();
			// if (viewer == null)
			// return;
			//
			// IRegion region= getCurrentTextRegion(viewer);
			// if (region == null)
			// return;
			//
			// highlightRegion(viewer, region);
			// activateCursor(viewer);
		}

		/*
		 * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
		 */
		public void keyReleased(KeyEvent event) {

			if (!fActive)
				return;

			deactivate();
		}

		/*
		 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseDoubleClick(MouseEvent e) {
		}

		/*
		 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseDown(MouseEvent event) {

			if (!fActive)
				return;

			if (event.stateMask != fKeyModifierMask) {
				deactivate();
				return;
			}

			if (event.button != 1) {
				deactivate();
				return;
			}
		}

		/*
		 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseUp(MouseEvent e) {

			if (!fActive)
				return;

			if (e.button != 1) {
				deactivate();
				return;
			}

			boolean wasActive = fCursor != null;

			deactivate();

			if (wasActive) {
				IAction action = getAction("OpenEditor"); //$NON-NLS-1$
				if (action != null)
					action.run();
			}
		}

		/*
		 * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseMove(MouseEvent event) {

			if (event.widget instanceof Control
					&& !((Control) event.widget).isFocusControl()) {
				deactivate();
				return;
			}

			if (!fActive) {
				if (event.stateMask != fKeyModifierMask)
					return;
				// modifier was already pressed
				fActive = true;
			}

			ISourceViewer viewer = getSourceViewer();
			if (viewer == null) {
				deactivate();
				return;
			}

			StyledText text = viewer.getTextWidget();
			if (text == null || text.isDisposed()) {
				deactivate();
				return;
			}

			if ((event.stateMask & SWT.BUTTON1) != 0
					&& text.getSelectionCount() != 0) {
				deactivate();
				return;
			}

			IRegion region = getCurrentTextRegion(viewer);
			if (region == null || region.getLength() == 0) {
				repairRepresentation();
				return;
			}

			highlightRegion(viewer, region);
			activateCursor(viewer);
		}

		/*
		 * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
		 */
		public void focusGained(FocusEvent e) {
		}

		/*
		 * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
		 */
		public void focusLost(FocusEvent event) {
			deactivate();
		}

		/*
		 * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
		 */
		public void documentAboutToBeChanged(DocumentEvent event) {
			if (fActive && fActiveRegion != null) {
				fRememberedPosition = new Position(fActiveRegion.getOffset(),
						fActiveRegion.getLength());
				try {
					event.getDocument().addPosition(fRememberedPosition);
				} catch (BadLocationException x) {
					fRememberedPosition = null;
				}
			}
		}

		/*
		 * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
		 */
		public void documentChanged(DocumentEvent event) {
			if (fRememberedPosition != null) {
				if (!fRememberedPosition.isDeleted()) {

					event.getDocument().removePosition(fRememberedPosition);
					fActiveRegion = new Region(fRememberedPosition.getOffset(),
							fRememberedPosition.getLength());
					fRememberedPosition = null;

					ISourceViewer viewer = getSourceViewer();
					if (viewer != null) {
						StyledText widget = viewer.getTextWidget();
						if (widget != null && !widget.isDisposed()) {
							widget.getDisplay().asyncExec(new Runnable() {
								public void run() {
									deactivate();
								}
							});
						}
					}

				} else {
					fActiveRegion = null;
					fRememberedPosition = null;
					deactivate();
				}
			}
		}

		/*
		 * @see org.eclipse.jface.text.ITextInputListener#inputDocumentAboutToBeChanged(org.eclipse.jface.text.IDocument,
		 *      org.eclipse.jface.text.IDocument)
		 */
		public void inputDocumentAboutToBeChanged(IDocument oldInput,
				IDocument newInput) {
			if (oldInput == null)
				return;
			deactivate();
			oldInput.removeDocumentListener(this);
		}

		/*
		 * @see org.eclipse.jface.text.ITextInputListener#inputDocumentChanged(org.eclipse.jface.text.IDocument,
		 *      org.eclipse.jface.text.IDocument)
		 */
		public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
			if (newInput == null)
				return;
			newInput.addDocumentListener(this);
		}

		/*
		 * @see PaintListener#paintControl(PaintEvent)
		 */
		public void paintControl(PaintEvent event) {
			if (fActiveRegion == null)
				return;

			ISourceViewer viewer = getSourceViewer();
			if (viewer == null)
				return;

			StyledText text = viewer.getTextWidget();
			if (text == null || text.isDisposed())
				return;

			int offset = 0;
			int length = 0;

			if (viewer instanceof ITextViewerExtension5) {

				ITextViewerExtension5 extension = (ITextViewerExtension5) viewer;
				IRegion widgetRange = extension
						.modelRange2WidgetRange(fActiveRegion);
				if (widgetRange == null)
					return;

				offset = widgetRange.getOffset();
				length = widgetRange.getLength();

			} else {

				IRegion region = viewer.getVisibleRegion();
				if (!includes(region, fActiveRegion))
					return;

				offset = fActiveRegion.getOffset() - region.getOffset();
				length = fActiveRegion.getLength();
			}

			// support for bidi
			Point minLocation = getMinimumLocation(text, offset, length);
			Point maxLocation = getMaximumLocation(text, offset, length);

			int x1 = minLocation.x;
			int x2 = minLocation.x + maxLocation.x - minLocation.x - 1;
			int y = minLocation.y + text.getLineHeight() - 1;

			GC gc = event.gc;
			if (fColor != null && !fColor.isDisposed())
				gc.setForeground(fColor);
			gc.drawLine(x1, y, x2, y);
		}

		private boolean includes(IRegion region, IRegion position) {
			return position.getOffset() >= region.getOffset()
					&& position.getOffset() + position.getLength() <= region
							.getOffset()
							+ region.getLength();
		}

		private Point getMinimumLocation(StyledText text, int offset, int length) {
			Point minLocation = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);

			for (int i = 0; i <= length; i++) {
				Point location = text.getLocationAtOffset(offset + i);

				if (location.x < minLocation.x)
					minLocation.x = location.x;
				if (location.y < minLocation.y)
					minLocation.y = location.y;
			}

			return minLocation;
		}

		private Point getMaximumLocation(StyledText text, int offset, int length) {
			Point maxLocation = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);

			for (int i = 0; i <= length; i++) {
				Point location = text.getLocationAtOffset(offset + i);

				if (location.x > maxLocation.x)
					maxLocation.x = location.x;
				if (location.y > maxLocation.y)
					maxLocation.y = location.y;
			}

			return maxLocation;
		}
	}

	/**
	 * This action dispatches into two behaviours: If there is no current text
	 * hover, the javadoc is displayed using information presenter. If there is
	 * a current text hover, it is converted into a information presenter in
	 * order to make it sticky.
	 */
	class InformationDispatchAction extends TextEditorAction {

		/** The wrapped text operation action. */
		private final TextOperationAction fTextOperationAction;

		/**
		 * Creates a dispatch action.
		 */
		public InformationDispatchAction(ResourceBundle resourceBundle,
				String prefix, final TextOperationAction textOperationAction) {
			super(resourceBundle, prefix, PHPEditor.this);
			if (textOperationAction == null)
				throw new IllegalArgumentException();
			fTextOperationAction = textOperationAction;
		}

		/*
		 * @see org.eclipse.jface.action.IAction#run()
		 */
		public void run() {

			ISourceViewer sourceViewer = getSourceViewer();
			if (sourceViewer == null) {
				fTextOperationAction.run();
				return;
			}

			if (!(sourceViewer instanceof ITextViewerExtension2)) {
				fTextOperationAction.run();
				return;
			}

			ITextViewerExtension2 textViewerExtension2 = (ITextViewerExtension2) sourceViewer;

			// does a text hover exist?
			ITextHover textHover = textViewerExtension2.getCurrentTextHover();
			if (textHover == null) {
				fTextOperationAction.run();
				return;
			}

			Point hoverEventLocation = textViewerExtension2
					.getHoverEventLocation();
			int offset = computeOffsetAtLocation(sourceViewer,
					hoverEventLocation.x, hoverEventLocation.y);
			if (offset == -1) {
				fTextOperationAction.run();
				return;
			}

			try {
				// get the text hover content
				IDocument document = sourceViewer.getDocument();
				String contentType = document.getContentType(offset);

				final IRegion hoverRegion = textHover.getHoverRegion(
						sourceViewer, offset);
				if (hoverRegion == null)
					return;

				final String hoverInfo = textHover.getHoverInfo(sourceViewer,
						hoverRegion);

				// with information provider
				IInformationProvider informationProvider = new IInformationProvider() {
					/*
					 * @see org.eclipse.jface.text.information.IInformationProvider#getSubject(org.eclipse.jface.text.ITextViewer,
					 *      int)
					 */
					public IRegion getSubject(ITextViewer textViewer, int offset) {
						return hoverRegion;
					}

					/*
					 * @see org.eclipse.jface.text.information.IInformationProvider#getInformation(org.eclipse.jface.text.ITextViewer,
					 *      org.eclipse.jface.text.IRegion)
					 */
					public String getInformation(ITextViewer textViewer,
							IRegion subject) {
						return hoverInfo;
					}
				};

				fInformationPresenter.setOffset(offset);
				fInformationPresenter.setInformationProvider(
						informationProvider, contentType);
				fInformationPresenter.showInformation();

			} catch (BadLocationException e) {
			}
		}

		// modified version from TextViewer
		private int computeOffsetAtLocation(ITextViewer textViewer, int x, int y) {

			StyledText styledText = textViewer.getTextWidget();
			IDocument document = textViewer.getDocument();

			if (document == null)
				return -1;

			try {
				int widgetLocation = styledText.getOffsetAtLocation(new Point(
						x, y));
				if (textViewer instanceof ITextViewerExtension5) {
					ITextViewerExtension5 extension = (ITextViewerExtension5) textViewer;
					return extension.widgetOffset2ModelOffset(widgetLocation);
				} else {
					IRegion visibleRegion = textViewer.getVisibleRegion();
					return widgetLocation + visibleRegion.getOffset();
				}
			} catch (IllegalArgumentException e) {
				return -1;
			}

		}
	};

	/**
	 * This action implements smart home.
	 * 
	 * Instead of going to the start of a line it does the following: - if smart
	 * home/end is enabled and the caret is after the line's first
	 * non-whitespace then the caret is moved directly before it, taking JavaDoc
	 * and multi-line comments into account. - if the caret is before the line's
	 * first non-whitespace the caret is moved to the beginning of the line - if
	 * the caret is at the beginning of the line see first case.
	 * 
	 * @since 3.0
	 */
	protected class SmartLineStartAction extends LineStartAction {

		/**
		 * Creates a new smart line start action
		 * 
		 * @param textWidget
		 *            the styled text widget
		 * @param doSelect
		 *            a boolean flag which tells if the text up to the beginning
		 *            of the line should be selected
		 */
		public SmartLineStartAction(final StyledText textWidget,
				final boolean doSelect) {
			super(textWidget, doSelect);
		}

		/*
		 * @see org.eclipse.ui.texteditor.AbstractTextEditor.LineStartAction#getLineStartPosition(java.lang.String,
		 *      int, java.lang.String)
		 */
		protected int getLineStartPosition(final IDocument document,
				final String line, final int length, final int offset) {

			String type = IDocument.DEFAULT_CONTENT_TYPE;
			try {
				type = TextUtilities.getContentType(document,
						IPHPPartitions.PHP_PARTITIONING, offset, true);
			} catch (BadLocationException exception) {
				// Should not happen
			}

			int index = super.getLineStartPosition(document, line, length,
					offset);
			if (type.equals(IPHPPartitions.PHP_PHPDOC_COMMENT)
					|| type.equals(IPHPPartitions.PHP_MULTILINE_COMMENT)) {
				if (index < length - 1 && line.charAt(index) == '*'
						&& line.charAt(index + 1) != '/') {
					do {
						++index;
					} while (index < length
							&& Character.isWhitespace(line.charAt(index)));
				}
			} else {
				if (index < length - 1 && line.charAt(index) == '/'
						&& line.charAt(index + 1) == '/') {
					index++;
					do {
						++index;
					} while (index < length
							&& Character.isWhitespace(line.charAt(index)));
				}
			}
			return index;
		}
	}

	/**
	 * Text navigation action to navigate to the next sub-word.
	 * 
	 * @since 3.0
	 */
	protected abstract class NextSubWordAction extends TextNavigationAction {

		protected JavaWordIterator fIterator = new JavaWordIterator();

		/**
		 * Creates a new next sub-word action.
		 * 
		 * @param code
		 *            Action code for the default operation. Must be an action
		 *            code from
		 * @see org.eclipse.swt.custom.ST.
		 */
		protected NextSubWordAction(int code) {
			super(getSourceViewer().getTextWidget(), code);
		}

		/*
		 * @see org.eclipse.jface.action.IAction#run()
		 */
		public void run() {
			// Check whether we are in a java code partition and the preference
			// is
			// enabled
			final IPreferenceStore store = getPreferenceStore();
			if (!store
					.getBoolean(PreferenceConstants.EDITOR_SUB_WORD_NAVIGATION)) {
				super.run();
				return;
			}

			final ISourceViewer viewer = getSourceViewer();
			final IDocument document = viewer.getDocument();
			fIterator
					.setText((CharacterIterator) new DocumentCharacterIterator(
							document));
			int position = widgetOffset2ModelOffset(viewer, viewer
					.getTextWidget().getCaretOffset());
			if (position == -1)
				return;

			int next = findNextPosition(position);
			if (next != BreakIterator.DONE) {
				setCaretPosition(next);
				getTextWidget().showSelection();
				fireSelectionChanged();
			}

		}
		
		/**
		 * Finds the next position after the given position.
		 * 
		 * @param position
		 *            the current position
		 * @return the next position
		 */
		protected int findNextPosition(int position) {
			ISourceViewer viewer = getSourceViewer();
			int widget = -1;
			while (position != BreakIterator.DONE && widget == -1) { // TODO:
				// optimize
				position = fIterator.following(position);
				if (position != BreakIterator.DONE)
					widget = modelOffset2WidgetOffset(viewer, position);
			}
			return position;
		}

		/**
		 * Sets the caret position to the sub-word boundary given with
		 * <code>position</code>.
		 * 
		 * @param position
		 *            Position where the action should move the caret
		 */
		protected abstract void setCaretPosition(int position);
	}

	/**
	 * Text navigation action to navigate to the next sub-word.
	 * 
	 * @since 3.0
	 */
	protected class NavigateNextSubWordAction extends NextSubWordAction {

		/**
		 * Creates a new navigate next sub-word action.
		 */
		public NavigateNextSubWordAction() {
			super(ST.WORD_NEXT);
		}

		/*
		 * @see net.sourceforge.phpdt.internal.ui.javaeditor.JavaEditor.NextSubWordAction#setCaretPosition(int)
		 */
		protected void setCaretPosition(final int position) {
			getTextWidget().setCaretOffset(
					modelOffset2WidgetOffset(getSourceViewer(), position));
		}
	}

	/**
	 * Text operation action to delete the next sub-word.
	 * 
	 * @since 3.0
	 */
	protected class DeleteNextSubWordAction extends NextSubWordAction implements
			IUpdate {

		/**
		 * Creates a new delete next sub-word action.
		 */
		public DeleteNextSubWordAction() {
			super(ST.DELETE_WORD_NEXT);
		}

		/*
		 * @see net.sourceforge.phpdt.internal.ui.javaeditor.JavaEditor.NextSubWordAction#setCaretPosition(int)
		 */
		protected void setCaretPosition(final int position) {
			if (!validateEditorInputState())
				return;

			final ISourceViewer viewer = getSourceViewer();
			final int caret = widgetOffset2ModelOffset(viewer, viewer
					.getTextWidget().getCaretOffset());

			try {
				viewer.getDocument().replace(caret, position - caret, ""); //$NON-NLS-1$
			} catch (BadLocationException exception) {
				// Should not happen
			}
		}

		/*
		 * @see net.sourceforge.phpdt.internal.ui.javaeditor.JavaEditor.NextSubWordAction#findNextPosition(int)
		 */
		protected int findNextPosition(int position) {
			return fIterator.following(position);
		}

		/*
		 * @see org.eclipse.ui.texteditor.IUpdate#update()
		 */
		public void update() {
			setEnabled(isEditorInputModifiable());
		}
	}

	/**
	 * Text operation action to select the next sub-word.
	 * 
	 * @since 3.0
	 */
	protected class SelectNextSubWordAction extends NextSubWordAction {

		/**
		 * Creates a new select next sub-word action.
		 */
		public SelectNextSubWordAction() {
			super(ST.SELECT_WORD_NEXT);
		}

		/*
		 * @see net.sourceforge.phpdt.internal.ui.javaeditor.JavaEditor.NextSubWordAction#setCaretPosition(int)
		 */
		protected void setCaretPosition(final int position) {
			final ISourceViewer viewer = getSourceViewer();

			final StyledText text = viewer.getTextWidget();
			if (text != null && !text.isDisposed()) {

				final Point selection = text.getSelection();
				final int caret = text.getCaretOffset();
				final int offset = modelOffset2WidgetOffset(viewer, position);

				if (caret == selection.x)
					text.setSelectionRange(selection.y, offset - selection.y);
				else
					text.setSelectionRange(selection.x, offset - selection.x);
			}
		}
	}

	/**
	 * Text navigation action to navigate to the previous sub-word.
	 * 
	 * @since 3.0
	 */
	protected abstract class PreviousSubWordAction extends TextNavigationAction {

		protected JavaWordIterator fIterator = new JavaWordIterator();

		/**
		 * Creates a new previous sub-word action.
		 * 
		 * @param code
		 *            Action code for the default operation. Must be an action
		 *            code from
		 * @see org.eclipse.swt.custom.ST.
		 */
		protected PreviousSubWordAction(final int code) {
			super(getSourceViewer().getTextWidget(), code);
		}

		/*
		 * @see org.eclipse.jface.action.IAction#run()
		 */
		public void run() {
			// Check whether we are in a java code partition and the preference
			// is
			// enabled
			final IPreferenceStore store = getPreferenceStore();
			if (!store
					.getBoolean(PreferenceConstants.EDITOR_SUB_WORD_NAVIGATION)) {
				super.run();
				return;
			}

			final ISourceViewer viewer = getSourceViewer();
			final IDocument document = viewer.getDocument();
			fIterator
					.setText((CharacterIterator) new DocumentCharacterIterator(
							document));
			int position = widgetOffset2ModelOffset(viewer, viewer
					.getTextWidget().getCaretOffset());
			if (position == -1)
				return;

			int previous = findPreviousPosition(position);
			if (previous != BreakIterator.DONE) {
				setCaretPosition(previous);
				getTextWidget().showSelection();
				fireSelectionChanged();
			}

		}

		/**
		 * Finds the previous position before the given position.
		 * 
		 * @param position
		 *            the current position
		 * @return the previous position
		 */
		protected int findPreviousPosition(int position) {
			ISourceViewer viewer = getSourceViewer();
			int widget = -1;
			while (position != BreakIterator.DONE && widget == -1) { // TODO:
				// optimize
				position = fIterator.preceding(position);
				if (position != BreakIterator.DONE)
					widget = modelOffset2WidgetOffset(viewer, position);
			}
			return position;
		}

		/**
		 * Sets the caret position to the sub-word boundary given with
		 * <code>position</code>.
		 * 
		 * @param position
		 *            Position where the action should move the caret
		 */
		protected abstract void setCaretPosition(int position);
	}

	/**
	 * Text navigation action to navigate to the previous sub-word.
	 * 
	 * @since 3.0
	 */
	protected class NavigatePreviousSubWordAction extends PreviousSubWordAction {

		/**
		 * Creates a new navigate previous sub-word action.
		 */
		public NavigatePreviousSubWordAction() {
			super(ST.WORD_PREVIOUS);
		}

		/*
		 * @see net.sourceforge.phpdt.internal.ui.javaeditor.JavaEditor.PreviousSubWordAction#setCaretPosition(int)
		 */
		protected void setCaretPosition(final int position) {
			getTextWidget().setCaretOffset(
					modelOffset2WidgetOffset(getSourceViewer(), position));
		}
	}

	/**
	 * Text operation action to delete the previous sub-word.
	 * 
	 * @since 3.0
	 */
	protected class DeletePreviousSubWordAction extends PreviousSubWordAction
			implements IUpdate {

		/**
		 * Creates a new delete previous sub-word action.
		 */
		public DeletePreviousSubWordAction() {
			super(ST.DELETE_WORD_PREVIOUS);
		}

		/*
		 * @see net.sourceforge.phpdt.internal.ui.javaeditor.JavaEditor.PreviousSubWordAction#setCaretPosition(int)
		 */
		protected void setCaretPosition(final int position) {
			if (!validateEditorInputState())
				return;

			final ISourceViewer viewer = getSourceViewer();
			final int caret = widgetOffset2ModelOffset(viewer, viewer
					.getTextWidget().getCaretOffset());

			try {
				viewer.getDocument().replace(position, caret - position, ""); //$NON-NLS-1$
			} catch (BadLocationException exception) {
				// Should not happen
			}
		}

		/*
		 * @see net.sourceforge.phpdt.internal.ui.javaeditor.JavaEditor.PreviousSubWordAction#findPreviousPosition(int)
		 */
		protected int findPreviousPosition(int position) {
			return fIterator.preceding(position);
		}

		/*
		 * @see org.eclipse.ui.texteditor.IUpdate#update()
		 */
		public void update() {
			setEnabled(isEditorInputModifiable());
		}
	}

	/**
	 * Text operation action to select the previous sub-word.
	 * 
	 * @since 3.0
	 */
	protected class SelectPreviousSubWordAction extends PreviousSubWordAction {

		/**
		 * Creates a new select previous sub-word action.
		 */
		public SelectPreviousSubWordAction() {
			super(ST.SELECT_WORD_PREVIOUS);
		}

		/*
		 * @see net.sourceforge.phpdt.internal.ui.javaeditor.JavaEditor.PreviousSubWordAction#setCaretPosition(int)
		 */
		protected void setCaretPosition(final int position) {
			final ISourceViewer viewer = getSourceViewer();

			final StyledText text = viewer.getTextWidget();
			if (text != null && !text.isDisposed()) {

				final Point selection = text.getSelection();
				final int caret = text.getCaretOffset();
				final int offset = modelOffset2WidgetOffset(viewer, position);

				if (caret == selection.x)
					text.setSelectionRange(selection.y, offset - selection.y);
				else
					text.setSelectionRange(selection.x, offset - selection.x);
			}
		}
	}

	// static protected class AnnotationAccess implements IAnnotationAccess {
	// /*
	// * @see
	// org.eclipse.jface.text.source.IAnnotationAccess#getType(org.eclipse.jface.text.source.Annotation)
	// */
	// public Object getType(Annotation annotation) {
	// if (annotation instanceof IJavaAnnotation) {
	// IJavaAnnotation javaAnnotation = (IJavaAnnotation) annotation;
	// // if (javaAnnotation.isRelevant())
	// // return javaAnnotation.getAnnotationType();
	// }
	// return null;
	// }
	//
	// /*
	// * @see
	// org.eclipse.jface.text.source.IAnnotationAccess#isMultiLine(org.eclipse.jface.text.source.Annotation)
	// */
	// public boolean isMultiLine(Annotation annotation) {
	// return true;
	// }
	//
	// /*
	// * @see
	// org.eclipse.jface.text.source.IAnnotationAccess#isTemporary(org.eclipse.jface.text.source.Annotation)
	// */
	// public boolean isTemporary(Annotation annotation) {
	// if (annotation instanceof IJavaAnnotation) {
	// IJavaAnnotation javaAnnotation = (IJavaAnnotation) annotation;
	// if (javaAnnotation.isRelevant())
	// return javaAnnotation.isTemporary();
	// }
	// return false;
	// }
	// };

	private class PropertyChangeListener implements
			org.eclipse.core.runtime.Preferences.IPropertyChangeListener {
		/*
		 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		public void propertyChange(
				org.eclipse.core.runtime.Preferences.PropertyChangeEvent event) {
			handlePreferencePropertyChanged(event);
		}
	};

	/**
	 * Finds and marks occurrence annotations.
	 * 
	 * @since 3.0
	 */
	class OccurrencesFinderJob extends Job {

		private IDocument fDocument;

		private ISelection fSelection;

		private ISelectionValidator fPostSelectionValidator;

		private boolean fCanceled = false;

		private IProgressMonitor fProgressMonitor;

		private Position[] fPositions;

		public OccurrencesFinderJob(IDocument document, Position[] positions,
				ISelection selection) {
			super(PHPEditorMessages.JavaEditor_markOccurrences_job_name);
			fDocument = document;
			fSelection = selection;
			fPositions = positions;

			if (getSelectionProvider() instanceof ISelectionValidator)
				fPostSelectionValidator = (ISelectionValidator) getSelectionProvider();
		}

		// cannot use cancel() because it is declared final
		void doCancel() {
			fCanceled = true;
			cancel();
		}

		private boolean isCanceled() {
			return fCanceled
					|| fProgressMonitor.isCanceled()
					|| fPostSelectionValidator != null
					&& !(fPostSelectionValidator.isValid(fSelection) || fForcedMarkOccurrencesSelection == fSelection)
					|| LinkedModeModel.hasInstalledModel(fDocument);
		}

		/*
		 * @see Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		public IStatus run(IProgressMonitor progressMonitor) {

			fProgressMonitor = progressMonitor;

			if (isCanceled())
				return Status.CANCEL_STATUS;

			ITextViewer textViewer = getViewer();
			if (textViewer == null)
				return Status.CANCEL_STATUS;

			IDocument document = textViewer.getDocument();
			if (document == null)
				return Status.CANCEL_STATUS;

			IDocumentProvider documentProvider = getDocumentProvider();
			if (documentProvider == null)
				return Status.CANCEL_STATUS;

			IAnnotationModel annotationModel = documentProvider
					.getAnnotationModel(getEditorInput());
			if (annotationModel == null)
				return Status.CANCEL_STATUS;

			// Add occurrence annotations
			int length = fPositions.length;
			Map annotationMap = new HashMap(length);
			for (int i = 0; i < length; i++) {

				if (isCanceled())
					return Status.CANCEL_STATUS;

				String message;
				Position position = fPositions[i];

				// Create & add annotation
				try {
					message = document.get(position.offset, position.length);
				} catch (BadLocationException ex) {
					// Skip this match
					continue;
				}
				annotationMap
						.put(
								new Annotation(
										"net.sourceforge.phpdt.ui.occurrences", false, message), //$NON-NLS-1$
								position);
			}

			if (isCanceled())
				return Status.CANCEL_STATUS;

			synchronized (getLockObject(annotationModel)) {
				if (annotationModel instanceof IAnnotationModelExtension) {
					((IAnnotationModelExtension) annotationModel)
							.replaceAnnotations(fOccurrenceAnnotations,
									annotationMap);
				} else {
					removeOccurrenceAnnotations();
					Iterator iter = annotationMap.entrySet().iterator();
					while (iter.hasNext()) {
						Map.Entry mapEntry = (Map.Entry) iter.next();
						annotationModel.addAnnotation((Annotation) mapEntry
								.getKey(), (Position) mapEntry.getValue());
					}
				}
				fOccurrenceAnnotations = (Annotation[]) annotationMap.keySet()
						.toArray(new Annotation[annotationMap.keySet().size()]);
			}

			return Status.OK_STATUS;
		}
	}

	/**
	 * Cancels the occurrences finder job upon document changes.
	 * 
	 * @since 3.0
	 */
	class OccurrencesFinderJobCanceler implements IDocumentListener,
			ITextInputListener {

		public void install() {
			ISourceViewer sourceViewer = getSourceViewer();
			if (sourceViewer == null)
				return;

			StyledText text = sourceViewer.getTextWidget();
			if (text == null || text.isDisposed())
				return;

			sourceViewer.addTextInputListener(this);

			IDocument document = sourceViewer.getDocument();
			if (document != null)
				document.addDocumentListener(this);
		}

		public void uninstall() {
			ISourceViewer sourceViewer = getSourceViewer();
			if (sourceViewer != null)
				sourceViewer.removeTextInputListener(this);

			IDocumentProvider documentProvider = getDocumentProvider();
			if (documentProvider != null) {
				IDocument document = documentProvider
						.getDocument(getEditorInput());
				if (document != null)
					document.removeDocumentListener(this);
			}
		}

		/*
		 * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
		 */
		public void documentAboutToBeChanged(DocumentEvent event) {
			if (fOccurrencesFinderJob != null)
				fOccurrencesFinderJob.doCancel();
		}

		/*
		 * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
		 */
		public void documentChanged(DocumentEvent event) {
		}

		/*
		 * @see org.eclipse.jface.text.ITextInputListener#inputDocumentAboutToBeChanged(org.eclipse.jface.text.IDocument,
		 *      org.eclipse.jface.text.IDocument)
		 */
		public void inputDocumentAboutToBeChanged(IDocument oldInput,
				IDocument newInput) {
			if (oldInput == null)
				return;

			oldInput.removeDocumentListener(this);
		}

		/*
		 * @see org.eclipse.jface.text.ITextInputListener#inputDocumentChanged(org.eclipse.jface.text.IDocument,
		 *      org.eclipse.jface.text.IDocument)
		 */
		public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
			if (newInput == null)
				return;
			newInput.addDocumentListener(this);
		}
	}

	/**
	 * Internal activation listener.
	 * 
	 * @since 3.0
	 */
	private class ActivationListener implements IWindowListener {

		/*
		 * @see org.eclipse.ui.IWindowListener#windowActivated(org.eclipse.ui.IWorkbenchWindow)
		 * @since 3.1
		 */
		public void windowActivated(IWorkbenchWindow window) {
			if (window == getEditorSite().getWorkbenchWindow()
					&& fMarkOccurrenceAnnotations && isActivePart()) {
				fForcedMarkOccurrencesSelection = getSelectionProvider()
						.getSelection();
				SelectionListenerWithASTManager
						.getDefault()
						.forceSelectionChange(
								PHPEditor.this,
								(ITextSelection) fForcedMarkOccurrencesSelection);
			}
		}

		/*
		 * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.IWorkbenchWindow)
		 * @since 3.1
		 */
		public void windowDeactivated(IWorkbenchWindow window) {
			if (window == getEditorSite().getWorkbenchWindow()
					&& fMarkOccurrenceAnnotations && isActivePart())
				removeOccurrenceAnnotations();
		}

		/*
		 * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow)
		 * @since 3.1
		 */
		public void windowClosed(IWorkbenchWindow window) {
		}

		/*
		 * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow)
		 * @since 3.1
		 */
		public void windowOpened(IWorkbenchWindow window) {
		}
	}

	/**
	 * Updates the selection in the editor's widget with the selection of the
	 * outline page.
	 */
	class OutlineSelectionChangedListener extends
			AbstractSelectionChangedListener {
		public void selectionChanged(SelectionChangedEvent event) {
			doSelectionChanged(event);
		}
	}

	/**
	 * The internal shell activation listener for updating occurrences.
	 * 
	 * @since 3.0
	 */
	private ActivationListener fActivationListener = new ActivationListener();

	private ISelectionListenerWithAST fPostSelectionListenerWithAST;

	private OccurrencesFinderJob fOccurrencesFinderJob;

	/** The occurrences finder job canceler */
	private OccurrencesFinderJobCanceler fOccurrencesFinderJobCanceler;

	/**
	 * Holds the current occurrence annotations.
	 * 
	 * @since 3.0
	 */
	private Annotation[] fOccurrenceAnnotations = null;

	/**
	 * Tells whether all occurrences of the element at the current caret
	 * location are automatically marked in this editor.
	 * 
	 * @since 3.0
	 */
	private boolean fMarkOccurrenceAnnotations;

	/**
	 * The selection used when forcing occurrence marking through code.
	 * 
	 * @since 3.0
	 */
	private ISelection fForcedMarkOccurrencesSelection;

	/**
	 * The document modification stamp at the time when the last occurrence
	 * marking took place.
	 * 
	 * @since 3.1
	 */
	private long fMarkOccurrenceModificationStamp = IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;

	/**
	 * The region of the word under the caret used to when computing the current
	 * occurrence markings.
	 * 
	 * @since 3.1
	 */
	private IRegion fMarkOccurrenceTargetRegion;

	/**
	 * Tells whether the occurrence annotations are sticky i.e. whether they
	 * stay even if there's no valid Java element at the current caret position.
	 * Only valid if {@link #fMarkOccurrenceAnnotations} is <code>true</code>.
	 * 
	 * @since 3.0
	 */
	private boolean fStickyOccurrenceAnnotations;

	/** Preference key for the link color */
	private final static String LINK_COLOR = PreferenceConstants.EDITOR_LINK_COLOR;

	/** Preference key for compiler task tags */
	private final static String COMPILER_TASK_TAGS = JavaCore.COMPILER_TASK_TAGS;

	// protected PHPActionGroup fActionGroups;
	// /** The outline page */
	// private AbstractContentOutlinePage fOutlinePage;
	/** The outline page */
	protected JavaOutlinePage fOutlinePage;

	/** Outliner context menu Id */
	protected String fOutlinerContextMenuId;

	/**
	 * Indicates whether this editor should react on outline page selection
	 * changes
	 */
	private int fIgnoreOutlinePageSelection;

	/** The outline page selection updater */
	// private OutlinePageSelectionUpdater fUpdater;
	// protected PHPSyntaxParserThread fValidationThread = null;
	// private IPreferenceStore fPHPPrefStore;
	/** The selection changed listener */
	// protected ISelectionChangedListener fSelectionChangedListener = new
	// SelectionChangedListener();
	/**
	 * The editor selection changed listener.
	 * 
	 * @since 3.0
	 */
	private EditorSelectionChangedListener fEditorSelectionChangedListener;

	/** The selection changed listener */
	protected AbstractSelectionChangedListener fOutlineSelectionChangedListener = new OutlineSelectionChangedListener();

	/** The editor's bracket matcher */
	private PHPPairMatcher fBracketMatcher = new PHPPairMatcher(BRACKETS);

	/** The line number ruler column */
	// private LineNumberRulerColumn fLineNumberRulerColumn;
	/** This editor's encoding support */
	private DefaultEncodingSupport fEncodingSupport;

	/** The mouse listener */
	private MouseClickListener fMouseListener;

	/**
	 * Indicates whether this editor is about to update any annotation views.
	 * 
	 * @since 3.0
	 */
	private boolean fIsUpdatingAnnotationViews = false;

	/**
	 * The marker that served as last target for a goto marker request.
	 * 
	 * @since 3.0
	 */
	private IMarker fLastMarkerTarget = null;

	protected CompositeActionGroup fActionGroups;

	protected CompositeActionGroup fContextMenuGroup;

	/**
	 * This editor's projection support
	 * 
	 * @since 3.0
	 */
	private ProjectionSupport fProjectionSupport;

	/**
	 * This editor's projection model updater
	 * 
	 * @since 3.0
	 */
	private IJavaFoldingStructureProvider fProjectionModelUpdater;

	/**
	 * The override and implements indicator manager for this editor.
	 * 
	 * @since 3.0
	 */
	// protected OverrideIndicatorManager fOverrideIndicatorManager;
	/**
	 * The action group for folding.
	 * 
	 * @since 3.0
	 */
	private FoldingActionGroup fFoldingGroup;

	/** The information presenter. */
	private InformationPresenter fInformationPresenter;

	/** The annotation access */
	// protected IAnnotationAccess fAnnotationAccess = new AnnotationAccess();
	/** The overview ruler */
	protected OverviewRuler isOverviewRulerVisible;

	/** The source viewer decoration support */
	// protected SourceViewerDecorationSupport fSourceViewerDecorationSupport;
	/** The overview ruler */
	// protected OverviewRuler fOverviewRuler;
	/** The preference property change listener for java core. */
	private org.eclipse.core.runtime.Preferences.IPropertyChangeListener fPropertyChangeListener = new PropertyChangeListener();

	/**
	 * Returns the most narrow java element including the given offset
	 * 
	 * @param offset
	 *            the offset inside of the requested element
	 */
	abstract protected IJavaElement getElementAt(int offset);

	/**
	 * Returns the java element of this editor's input corresponding to the
	 * given IJavaElement
	 */
	abstract protected IJavaElement getCorrespondingElement(IJavaElement element);

	/**
	 * Sets the input of the editor's outline page.
	 */
	abstract protected void setOutlinePageInput(JavaOutlinePage page,
			IEditorInput input);

	/**
	 * Default constructor.
	 */
	public PHPEditor() {
		super();		
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#initializeKeyBindingScopes()
	 */
	protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(new String[] { "net.sourceforge.phpdt.ui.phpEditorScope" }); //$NON-NLS-1$
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#initializeEditor()
	 */
	protected void initializeEditor() {
		// jsurfer old code
		// JavaTextTools textTools =
		// PHPeclipsePlugin.getDefault().getJavaTextTools();
		// setSourceViewerConfiguration(new
		// PHPSourceViewerConfiguration(textTools,
		// this, IPHPPartitions.PHP_PARTITIONING)); //,
		// IJavaPartitions.JAVA_PARTITIONING));
		IPreferenceStore store = createCombinedPreferenceStore(null);
		setPreferenceStore(store);
		JavaTextTools textTools = PHPeclipsePlugin.getDefault()
				.getJavaTextTools();
		setSourceViewerConfiguration(new PHPSourceViewerConfiguration(textTools
				.getColorManager(), store, this,
				IPHPPartitions.PHP_PARTITIONING));
		
		// TODO changed in 3.x ?
		// setRangeIndicator(new DefaultRangeIndicator());
		// if
		// (PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_SYNC_OUTLINE_ON_CURSOR_MOVE))
		// fUpdater = new OutlinePageSelectionUpdater();
		// jsurfer end

		// IPreferenceStore store= createCombinedPreferenceStore(null);
		// setPreferenceStore(store);
		// JavaTextTools textTools=
		// PHPeclipsePlugin.getDefault().getJavaTextTools();
		// setSourceViewerConfiguration(new
		// JavaSourceViewerConfiguration(textTools.getColorManager(), store,
		// this, IJavaPartitions.JAVA_PARTITIONING));
		fMarkOccurrenceAnnotations = store
				.getBoolean(PreferenceConstants.EDITOR_MARK_OCCURRENCES);
		fStickyOccurrenceAnnotations = store
				.getBoolean(PreferenceConstants.EDITOR_STICKY_OCCURRENCES);
		// fMarkTypeOccurrences=
		// store.getBoolean(PreferenceConstants.EDITOR_MARK_TYPE_OCCURRENCES);
		// fMarkMethodOccurrences=
		// store.getBoolean(PreferenceConstants.EDITOR_MARK_METHOD_OCCURRENCES);
		// fMarkConstantOccurrences=
		// store.getBoolean(PreferenceConstants.EDITOR_MARK_CONSTANT_OCCURRENCES);
		// fMarkFieldOccurrences=
		// store.getBoolean(PreferenceConstants.EDITOR_MARK_FIELD_OCCURRENCES);
		// fMarkLocalVariableypeOccurrences=
		// store.getBoolean(PreferenceConstants.EDITOR_MARK_LOCAL_VARIABLE_OCCURRENCES);
		// fMarkExceptions=
		// store.getBoolean(PreferenceConstants.EDITOR_MARK_EXCEPTION_OCCURRENCES);
		// fMarkImplementors=
		// store.getBoolean(PreferenceConstants.EDITOR_MARK_IMPLEMENTORS);
		// fMarkMethodExitPoints=
		// store.getBoolean(PreferenceConstants.EDITOR_MARK_METHOD_EXIT_POINTS);

	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#updatePropertyDependentActions()
	 */
	protected void updatePropertyDependentActions() {
		super.updatePropertyDependentActions();
		if (fEncodingSupport != null)
			fEncodingSupport.reset();
	}

	/*
	 * Update the hovering behavior depending on the preferences.
	 */
	private void updateHoverBehavior() {
		SourceViewerConfiguration configuration = getSourceViewerConfiguration();
		String[] types = configuration
				.getConfiguredContentTypes(getSourceViewer());

		for (int i = 0; i < types.length; i++) {

			String t = types[i];

			int[] stateMasks = configuration.getConfiguredTextHoverStateMasks(
					getSourceViewer(), t);

			ISourceViewer sourceViewer = getSourceViewer();
			if (sourceViewer instanceof ITextViewerExtension2) {
				if (stateMasks != null) {
					for (int j = 0; j < stateMasks.length; j++) {
						int stateMask = stateMasks[j];
						ITextHover textHover = configuration.getTextHover(
								sourceViewer, t, stateMask);
						((ITextViewerExtension2) sourceViewer).setTextHover(
								textHover, t, stateMask);
					}
				} else {
					ITextHover textHover = configuration.getTextHover(
							sourceViewer, t);
					((ITextViewerExtension2) sourceViewer).setTextHover(
							textHover, t,
							ITextViewerExtension2.DEFAULT_HOVER_STATE_MASK);
				}
			} else
				sourceViewer.setTextHover(configuration.getTextHover(
						sourceViewer, t), t);
		}
	}

	public void updatedTitleImage(Image image) {
		setTitleImage(image);
	}

	/*
	 * @see net.sourceforge.phpdt.internal.ui.viewsupport.IViewPartInputProvider#getViewPartInput()
	 */
	public Object getViewPartInput() {
		return getEditorInput().getAdapter(IResource.class);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#doSetSelection(ISelection)
	 */
	protected void doSetSelection(ISelection selection) {
		super.doSetSelection(selection);
		synchronizeOutlinePageSelection();
	}

	boolean isFoldingEnabled() {
		return PHPeclipsePlugin.getDefault().getPreferenceStore().getBoolean(
				PreferenceConstants.EDITOR_FOLDING_ENABLED);
	}

	/*
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.
	 *      widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		// fSourceViewerDecorationSupport.install(getPreferenceStore());

		ProjectionViewer projectionViewer = (ProjectionViewer) getSourceViewer();

		fProjectionSupport = new ProjectionSupport(projectionViewer,
				getAnnotationAccess(), getSharedColors());
		fProjectionSupport
				.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error"); //$NON-NLS-1$
		fProjectionSupport
				.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning"); //$NON-NLS-1$
		fProjectionSupport
				.setHoverControlCreator(new IInformationControlCreator() {
					public IInformationControl createInformationControl(
							Shell shell) {
						return new CustomSourceInformationControl(shell,
								IDocument.DEFAULT_CONTENT_TYPE);
					}
				});
		fProjectionSupport.install();

		fProjectionModelUpdater = PHPeclipsePlugin.getDefault()
				.getFoldingStructureProviderRegistry()
				.getCurrentFoldingProvider();
		if (fProjectionModelUpdater != null)
			fProjectionModelUpdater.install(this, projectionViewer);

		if (isFoldingEnabled())
			projectionViewer.doOperation(ProjectionViewer.TOGGLE);
		Preferences preferences = PHPeclipsePlugin.getDefault()
				.getPluginPreferences();
		preferences.addPropertyChangeListener(fPropertyChangeListener);
		IInformationControlCreator informationControlCreator = new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				boolean cutDown = false;
				int style = cutDown ? SWT.NONE : (SWT.V_SCROLL | SWT.H_SCROLL);
				return new DefaultInformationControl(parent, SWT.RESIZE, style,
						new HTMLTextPresenter(cutDown));
			}
		};

		fInformationPresenter = new InformationPresenter(
				informationControlCreator);
		fInformationPresenter.setSizeConstraints(60, 10, true, true);
		fInformationPresenter.install(getSourceViewer());

		fEditorSelectionChangedListener = new EditorSelectionChangedListener();
		fEditorSelectionChangedListener.install(getSelectionProvider());

		if (isBrowserLikeLinks())
			enableBrowserLikeLinks();

		if (PreferenceConstants.getPreferenceStore().getBoolean(
				PreferenceConstants.EDITOR_DISABLE_OVERWRITE_MODE))
			enableOverwriteMode(false);

		if (fMarkOccurrenceAnnotations)
			installOccurrencesFinder();

		PlatformUI.getWorkbench().addWindowListener(fActivationListener);
		
		/*
		 * start of EDITOR_SAVE_ON_BLUR
		 * ed_mann
		 */
		final PHPEditor editor = this;
		FocusListener focusListener = new FocusListener() {
			
		      public void focusGained(FocusEvent e) {
		          return;
		        }

		        public void focusLost(FocusEvent e) {
		        	//viewer.get
		        	if(editor.isDirty() && PHPeclipsePlugin.getDefault().getPreferenceStore()
							.getBoolean(PreferenceConstants.EDITOR_SAVE_ON_BLUR)){
		        		editor.doSave(null);
		        	}
		        }
		      };
		  projectionViewer.getTextWidget().addFocusListener(focusListener);
		  	/*
			 * end of EDITOR_SAVE_ON_BLUR
			 * ed_mann
			 */
		  
		setWordWrap();
	}

	private void setWordWrap() {
		if (getSourceViewer() != null) {
			getSourceViewer().getTextWidget().setWordWrap(
					PHPeclipsePlugin.getDefault().getPreferenceStore()
							.getBoolean(PreferenceConstants.EDITOR_WRAP_WORDS));
		}
	}

	protected void configureSourceViewerDecorationSupport(
			SourceViewerDecorationSupport support) {

		support.setCharacterPairMatcher(fBracketMatcher);
		support.setMatchingCharacterPainterPreferenceKeys(MATCHING_BRACKETS,
				MATCHING_BRACKETS_COLOR);

		super.configureSourceViewerDecorationSupport(support);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#gotoMarker(org.eclipse.core.resources.IMarker)
	 */
	public void gotoMarker(IMarker marker) {
		fLastMarkerTarget = marker;
		if (!fIsUpdatingAnnotationViews) {
			super.gotoMarker(marker);
		}
	}

	/**
	 * Jumps to the next enabled annotation according to the given direction. An
	 * annotation type is enabled if it is configured to be in the Next/Previous
	 * tool bar drop down menu and if it is checked.
	 * 
	 * @param forward
	 *            <code>true</code> if search direction is forward,
	 *            <code>false</code> if backward
	 */
	public Annotation gotoAnnotation(boolean forward) {
		ITextSelection selection = (ITextSelection) getSelectionProvider()
				.getSelection();
		Position position = new Position(0, 0);
		Annotation annotation = null;
		if (false /* delayed - see bug 18316 */) {
			annotation = getNextAnnotation(selection.getOffset(), selection
					.getLength(), forward, position);
			selectAndReveal(position.getOffset(), position.getLength());
		} else /* no delay - see bug 18316 */{
			annotation = getNextAnnotation(selection.getOffset(), selection
					.getLength(), forward, position);
			setStatusLineErrorMessage(null);
			setStatusLineMessage(null);
			if (annotation != null) {
				updateAnnotationViews(annotation);
				selectAndReveal(position.getOffset(), position.getLength());
				setStatusLineMessage(annotation.getText());
			}
		}
		return annotation;
	}

	/**
	 * Returns the lock object for the given annotation model.
	 * 
	 * @param annotationModel
	 *            the annotation model
	 * @return the annotation model's lock object
	 * @since 3.0
	 */
	private Object getLockObject(IAnnotationModel annotationModel) {
		if (annotationModel instanceof ISynchronizable)
			return ((ISynchronizable) annotationModel).getLockObject();
		else
			return annotationModel;
	}

	/**
	 * Updates the annotation views that show the given annotation.
	 * 
	 * @param annotation
	 *            the annotation
	 */
	private void updateAnnotationViews(Annotation annotation) {
		IMarker marker = null;
		if (annotation instanceof MarkerAnnotation)
			marker = ((MarkerAnnotation) annotation).getMarker();
		else if (annotation instanceof IJavaAnnotation) {
			Iterator e = ((IJavaAnnotation) annotation).getOverlaidIterator();
			if (e != null) {
				while (e.hasNext()) {
					Object o = e.next();
					if (o instanceof MarkerAnnotation) {
						marker = ((MarkerAnnotation) o).getMarker();
						break;
					}
				}
			}
		}

		if (marker != null && !marker.equals(fLastMarkerTarget)) {
			try {
				boolean isProblem = marker.isSubtypeOf(IMarker.PROBLEM);
				IWorkbenchPage page = getSite().getPage();
				IViewPart view = page
						.findView(isProblem ? IPageLayout.ID_PROBLEM_VIEW
								: IPageLayout.ID_TASK_LIST); //$NON-NLS-1$  //$NON-NLS-2$
				if (view != null) {
					Method method = view
							.getClass()
							.getMethod(
									"setSelection", new Class[] { IStructuredSelection.class, boolean.class }); //$NON-NLS-1$
					method.invoke(view, new Object[] {
							new StructuredSelection(marker), Boolean.TRUE });
				}
			} catch (CoreException x) {
			} catch (NoSuchMethodException x) {
			} catch (IllegalAccessException x) {
			} catch (InvocationTargetException x) {
			}
			// ignore exceptions, don't update any of the lists, just set status
			// line
		}
	}

	/**
	 * Returns this document's complete text.
	 * 
	 * @return the document's complete text
	 */
	public String get() {
		IDocument doc = this.getDocumentProvider().getDocument(
				this.getEditorInput());
		return doc.get();
	}

	/**
	 * Sets the outliner's context menu ID.
	 */
	protected void setOutlinerContextMenuId(String menuId) {
		fOutlinerContextMenuId = menuId;
	}

	/**
	 * Returns the standard action group of this editor.
	 */
	protected ActionGroup getActionGroup() {
		return fActionGroups;
	}

	// public JavaOutlinePage getfOutlinePage() {
	// return fOutlinePage;
	// }

	/**
	 * The <code>PHPEditor</code> implementation of this
	 * <code>AbstractTextEditor</code> method extend the actions to add those
	 * specific to the receiver
	 */
	protected void createActions() {
		super.createActions();

		ActionGroup oeg, ovg, jsg, sg;
		fActionGroups = new CompositeActionGroup(
				new ActionGroup[] { oeg = new OpenEditorActionGroup(this),
				// sg= new ShowActionGroup(this),
				// ovg= new OpenViewActionGroup(this),
				// jsg= new JavaSearchActionGroup(this)
				});
		fContextMenuGroup = new CompositeActionGroup(new ActionGroup[] { oeg });
		// , ovg, sg, jsg});

		fFoldingGroup = new FoldingActionGroup(this, getViewer());

		// ResourceAction resAction = new
		// TextOperationAction(PHPEditorMessages.getResourceBundle(),
		// "ShowJavaDoc.", this, ISourceViewer.INFORMATION, true); //$NON-NLS-1$
		// resAction = new
		// InformationDispatchAction(PHPEditorMessages.getResourceBundle(),
		// "ShowJavaDoc.", (TextOperationAction) resAction); //$NON-NLS-1$
		// resAction.setActionDefinitionId(net.sourceforge.phpdt.ui.actions.PHPEditorActionDefinitionIds.SHOW_JAVADOC);
		// setAction("ShowJavaDoc", resAction); //$NON-NLS-1$

		// WorkbenchHelp.setHelp(resAction,
		// IJavaHelpContextIds.SHOW_JAVADOC_ACTION);

		Action action = new GotoMatchingBracketAction(this);
		action
				.setActionDefinitionId(PHPEditorActionDefinitionIds.GOTO_MATCHING_BRACKET);
		setAction(GotoMatchingBracketAction.GOTO_MATCHING_BRACKET, action);

		// action= new
		// TextOperationAction(PHPEditorMessages.getResourceBundle(),"ShowOutline.",
		// this, JavaSourceViewer.SHOW_OUTLINE, true); //$NON-NLS-1$
		// action.setActionDefinitionId(PHPEditorActionDefinitionIds.SHOW_OUTLINE);
		// setAction(PHPEditorActionDefinitionIds.SHOW_OUTLINE, action);
		// // WorkbenchHelp.setHelp(action,
		// IJavaHelpContextIds.SHOW_OUTLINE_ACTION);
		//
		// action= new
		// TextOperationAction(PHPEditorMessages.getResourceBundle(),"OpenStructure.",
		// this, JavaSourceViewer.OPEN_STRUCTURE, true); //$NON-NLS-1$
		// action.setActionDefinitionId(PHPEditorActionDefinitionIds.SHOW_OUTLINE.OPEN_STRUCTURE);
		// setAction(PHPEditorActionDefinitionIds.SHOW_OUTLINE.OPEN_STRUCTURE,
		// action);
		// // WorkbenchHelp.setHelp(action,
		// IJavaHelpContextIds.OPEN_STRUCTURE_ACTION);
		//
		// action= new
		// TextOperationAction(PHPEditorMessages.getResourceBundle(),"OpenHierarchy.",
		// this, JavaSourceViewer.SHOW_HIERARCHY, true); //$NON-NLS-1$
		// action.setActionDefinitionId(PHPEditorActionDefinitionIds.SHOW_OUTLINE.OPEN_HIERARCHY);
		// setAction(PHPEditorActionDefinitionIds.SHOW_OUTLINE.OPEN_HIERARCHY,
		// action);
		// // WorkbenchHelp.setHelp(action,
		// IJavaHelpContextIds.OPEN_HIERARCHY_ACTION);

		fEncodingSupport = new DefaultEncodingSupport();
		fEncodingSupport.initialize(this);

		// fSelectionHistory= new SelectionHistory(this);
		//
		// action= new StructureSelectEnclosingAction(this, fSelectionHistory);
		// action.setActionDefinitionId(PHPEditorActionDefinitionIds.SELECT_ENCLOSING);
		// setAction(StructureSelectionAction.ENCLOSING, action);
		//
		// action= new StructureSelectNextAction(this, fSelectionHistory);
		// action.setActionDefinitionId(PHPEditorActionDefinitionIds.SELECT_NEXT);
		// setAction(StructureSelectionAction.NEXT, action);
		//
		// action= new StructureSelectPreviousAction(this, fSelectionHistory);
		// action.setActionDefinitionId(PHPEditorActionDefinitionIds.SELECT_PREVIOUS);
		// setAction(StructureSelectionAction.PREVIOUS, action);
		//
		// StructureSelectHistoryAction historyAction= new
		// StructureSelectHistoryAction(this, fSelectionHistory);
		// historyAction.setActionDefinitionId(PHPEditorActionDefinitionIds.SELECT_LAST);
		// setAction(StructureSelectionAction.HISTORY, historyAction);
		// fSelectionHistory.setHistoryAction(historyAction);
		//
		// action= GoToNextPreviousMemberAction.newGoToNextMemberAction(this);
		// action.setActionDefinitionId(PHPEditorActionDefinitionIds.GOTO_NEXT_MEMBER);
		// setAction(GoToNextPreviousMemberAction.NEXT_MEMBER, action);
		//
		// action=
		// GoToNextPreviousMemberAction.newGoToPreviousMemberAction(this);
		// action.setActionDefinitionId(PHPEditorActionDefinitionIds.GOTO_PREVIOUS_MEMBER);
		// setAction(GoToNextPreviousMemberAction.PREVIOUS_MEMBER, action);
		//
		// action= new QuickFormatAction();
		// action.setActionDefinitionId(PHPEditorActionDefinitionIds.QUICK_FORMAT);
		// setAction(IJavaEditorActionDefinitionIds.QUICK_FORMAT, action);
		//
		// action= new RemoveOccurrenceAnnotations(this);
		// action.setActionDefinitionId(PHPEditorActionDefinitionIds.REMOVE_OCCURRENCE_ANNOTATIONS);
		// setAction("RemoveOccurrenceAnnotations", action); //$NON-NLS-1$

		// add annotation actions
		action = new JavaSelectMarkerRulerAction2(PHPEditorMessages
				.getResourceBundle(), "Editor.RulerAnnotationSelection.", this); //$NON-NLS-1$
		setAction("AnnotationAction", action); //$NON-NLS-1$
	}

	private void internalDoSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);

		if (getSourceViewer() instanceof JavaSourceViewer) {
			JavaSourceViewer viewer = (JavaSourceViewer) getSourceViewer();
			if (viewer.getReconciler() == null) {
				IReconciler reconciler = getSourceViewerConfiguration()
						.getReconciler(viewer);
				if (reconciler != null) {
					reconciler.install(viewer);
					viewer.setReconciler(reconciler);
				}
			}
		}

		if (fEncodingSupport != null)
			fEncodingSupport.reset();

		setOutlinePageInput(fOutlinePage, input);

		if (fProjectionModelUpdater != null)
			fProjectionModelUpdater.initialize();

		// if (isShowingOverrideIndicators())
		// installOverrideIndicator(false);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#setPreferenceStore(org.eclipse.jface.preference.IPreferenceStore)
	 * @since 3.0
	 */
	protected void setPreferenceStore(IPreferenceStore store) {
		super.setPreferenceStore(store);
		if (getSourceViewerConfiguration() instanceof PHPSourceViewerConfiguration) {
			JavaTextTools textTools = PHPeclipsePlugin.getDefault()
					.getJavaTextTools();
			setSourceViewerConfiguration(new PHPSourceViewerConfiguration(
					textTools.getColorManager(), store, this,
					IPHPPartitions.PHP_PARTITIONING));
		}
		if (getSourceViewer() instanceof JavaSourceViewer)
			((JavaSourceViewer) getSourceViewer()).setPreferenceStore(store);
	}

	/**
	 * The <code>PHPEditor</code> implementation of this
	 * <code>AbstractTextEditor</code> method performs any extra disposal
	 * actions required by the php editor.
	 */
	public void dispose() {
		if (fProjectionModelUpdater != null) {
			fProjectionModelUpdater.uninstall();
			fProjectionModelUpdater = null;
		}

		if (fProjectionSupport != null) {
			fProjectionSupport.dispose();
			fProjectionSupport = null;
		}
		// PHPEditorEnvironment.disconnect(this);
		if (fOutlinePage != null)
			fOutlinePage.setInput(null);

		if (fActionGroups != null)
			fActionGroups.dispose();

		if (isBrowserLikeLinks())
			disableBrowserLikeLinks();

		// cancel possible running computation
		fMarkOccurrenceAnnotations = false;
		uninstallOccurrencesFinder();

		uninstallOverrideIndicator();

		if (fActivationListener != null) {
			PlatformUI.getWorkbench().removeWindowListener(fActivationListener);
			fActivationListener = null;
		}

		if (fEncodingSupport != null) {
			fEncodingSupport.dispose();
			fEncodingSupport = null;
		}

		if (fPropertyChangeListener != null) {
			Preferences preferences = PHPeclipsePlugin.getDefault()
					.getPluginPreferences();
			preferences.removePropertyChangeListener(fPropertyChangeListener);
			fPropertyChangeListener = null;
		}

		// if (fSourceViewerDecorationSupport != null) {
		// fSourceViewerDecorationSupport.dispose();
		// fSourceViewerDecorationSupport = null;
		// }

		if (fBracketMatcher != null) {
			fBracketMatcher.dispose();
			fBracketMatcher = null;
		}

		if (fEditorSelectionChangedListener != null) {
			fEditorSelectionChangedListener.uninstall(getSelectionProvider());
			fEditorSelectionChangedListener = null;
		}

		super.dispose();
	}

	/**
	 * The <code>PHPEditor</code> implementation of this
	 * <code>AbstractTextEditor</code> method performs any extra revert
	 * behavior required by the php editor.
	 */
	// public void doRevertToSaved() {
	// super.doRevertToSaved();
	// if (fOutlinePage != null)
	// fOutlinePage.update();
	// }
	/**
	 * The <code>PHPEditor</code> implementation of this
	 * <code>AbstractTextEditor</code> method performs any extra save behavior
	 * required by the php editor.
	 */
	// public void doSave(IProgressMonitor monitor) {
	// super.doSave(monitor);
	// compile or not, according to the user preferences
	// IPreferenceStore store = getPreferenceStore();
	// the parse on save was changed to the eclipse "builders" concept
	// if (store.getBoolean(PHPeclipsePlugin.PHP_PARSE_ON_SAVE)) {
	// IAction a = PHPParserAction.getInstance();
	// if (a != null)
	// a.run();
	// }
	// if (SWT.getPlatform().equals("win32")) {
	// IAction a = ShowExternalPreviewAction.getInstance();
	// if (a != null)
	// a.run();
	// }
	// if (fOutlinePage != null)
	// fOutlinePage.update();
	// }
	/**
	 * The <code>PHPEditor</code> implementation of this
	 * <code>AbstractTextEditor</code> method performs any extra save as
	 * behavior required by the php editor.
	 */
	// public void doSaveAs() {
	// super.doSaveAs();
	// if (fOutlinePage != null)
	// fOutlinePage.update();
	// }
	/*
	 * @see StatusTextEditor#getStatusHeader(IStatus)
	 */
	protected String getStatusHeader(IStatus status) {
		if (fEncodingSupport != null) {
			String message = fEncodingSupport.getStatusHeader(status);
			if (message != null)
				return message;
		}
		return super.getStatusHeader(status);
	}

	/*
	 * @see StatusTextEditor#getStatusBanner(IStatus)
	 */
	protected String getStatusBanner(IStatus status) {
		if (fEncodingSupport != null) {
			String message = fEncodingSupport.getStatusBanner(status);
			if (message != null)
				return message;
		}
		return super.getStatusBanner(status);
	}

	/*
	 * @see StatusTextEditor#getStatusMessage(IStatus)
	 */
	protected String getStatusMessage(IStatus status) {
		if (fEncodingSupport != null) {
			String message = fEncodingSupport.getStatusMessage(status);
			if (message != null)
				return message;
		}
		return super.getStatusMessage(status);
	}

	/**
	 * The <code>PHPEditor</code> implementation of this
	 * <code>AbstractTextEditor</code> method performs sets the input of the
	 * outline page after AbstractTextEditor has set input.
	 */
	// protected void doSetInput(IEditorInput input) throws CoreException {
	// super.doSetInput(input);
	// if (fEncodingSupport != null)
	// fEncodingSupport.reset();
	// setOutlinePageInput(fOutlinePage, input);
	// }
	/*
	 * @see AbstractTextEditor#doSetInput
	 */
	protected void doSetInput(IEditorInput input) throws CoreException {
		ISourceViewer sourceViewer = getSourceViewer();
		if (!(sourceViewer instanceof ISourceViewerExtension2)) {
			setPreferenceStore(createCombinedPreferenceStore(input));
			internalDoSetInput(input);
			return;
		}

		// uninstall & unregister preference store listener
		if (isBrowserLikeLinks())
			disableBrowserLikeLinks();
		getSourceViewerDecorationSupport(sourceViewer).uninstall();
		((ISourceViewerExtension2) sourceViewer).unconfigure();

		setPreferenceStore(createCombinedPreferenceStore(input));

		// install & register preference store listener
		sourceViewer.configure(getSourceViewerConfiguration());
		getSourceViewerDecorationSupport(sourceViewer).install(
				getPreferenceStore());
		if (isBrowserLikeLinks())
			enableBrowserLikeLinks();

		internalDoSetInput(input);
	}

	/*
	 * @see org.phpeclipse.phpdt.internal.ui.viewsupport.IViewPartInputProvider#getViewPartInput()
	 */
	// public Object getViewPartInput() {
	// return getEditorInput().getAdapter(IFile.class);
	// }
	/**
	 * The <code>PHPEditor</code> implementation of this
	 * <code>AbstractTextEditor</code> method adds any PHPEditor specific
	 * entries.
	 */
	public void editorContextMenuAboutToShow(MenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		menu.appendToGroup(ITextEditorActionConstants.GROUP_UNDO,
				new Separator(IContextMenuConstants.GROUP_OPEN));
		menu.insertAfter(IContextMenuConstants.GROUP_OPEN, new GroupMarker(
				IContextMenuConstants.GROUP_SHOW));

		ActionContext context = new ActionContext(getSelectionProvider()
				.getSelection());
		fContextMenuGroup.setContext(context);
		fContextMenuGroup.fillContextMenu(menu);
		fContextMenuGroup.setContext(null);
		// addAction(menu, ITextEditorActionConstants.GROUP_EDIT, "Format");
		// //$NON-NLS-1$
		//
		// ActionContext context =
		// new ActionContext(getSelectionProvider().getSelection());
		// fContextMenuGroup.setContext(context);
		// fContextMenuGroup.fillContextMenu(menu);
		// fContextMenuGroup.setContext(null);
	}

	/**
	 * Creates the outline page used with this editor.
	 */
	protected JavaOutlinePage createOutlinePage() {
		JavaOutlinePage page = new JavaOutlinePage(fOutlinerContextMenuId, this);
		fOutlineSelectionChangedListener.install(page);
		setOutlinePageInput(page, getEditorInput());
		return page;
	}

	/**
	 * Informs the editor that its outliner has been closed.
	 */
	public void outlinePageClosed() {
		if (fOutlinePage != null) {
			fOutlineSelectionChangedListener.uninstall(fOutlinePage);
			fOutlinePage = null;
			resetHighlightRange();
		}
	}

	/**
	 * Synchronizes the outliner selection with the given element position in
	 * the editor.
	 * 
	 * @param element
	 *            the java element to select
	 */
	protected void synchronizeOutlinePage(ISourceReference element) {
		synchronizeOutlinePage(element, true);
	}

	/**
	 * Synchronizes the outliner selection with the given element position in
	 * the editor.
	 * 
	 * @param element
	 *            the java element to select
	 * @param checkIfOutlinePageActive
	 *            <code>true</code> if check for active outline page needs to
	 *            be done
	 */
	protected void synchronizeOutlinePage(ISourceReference element,
			boolean checkIfOutlinePageActive) {
		if (fOutlinePage != null && element != null
				&& !(checkIfOutlinePageActive && isJavaOutlinePageActive())) {
			fOutlineSelectionChangedListener.uninstall(fOutlinePage);
			fOutlinePage.select(element);
			fOutlineSelectionChangedListener.install(fOutlinePage);
		}
	}

	/**
	 * Synchronizes the outliner selection with the actual cursor position in
	 * the editor.
	 */
	public void synchronizeOutlinePageSelection() {
		synchronizeOutlinePage(computeHighlightRangeSourceReference());

		// ISourceViewer sourceViewer = getSourceViewer();
		// if (sourceViewer == null || fOutlinePage == null)
		// return;
		//
		// StyledText styledText = sourceViewer.getTextWidget();
		// if (styledText == null)
		// return;
		//
		// int caret = 0;
		// if (sourceViewer instanceof ITextViewerExtension3) {
		// ITextViewerExtension3 extension = (ITextViewerExtension3)
		// sourceViewer;
		// caret =
		// extension.widgetOffset2ModelOffset(styledText.getCaretOffset());
		// } else {
		// int offset = sourceViewer.getVisibleRegion().getOffset();
		// caret = offset + styledText.getCaretOffset();
		// }
		//
		// IJavaElement element = getElementAt(caret);
		// if (element instanceof ISourceReference) {
		// fOutlinePage.removeSelectionChangedListener(fSelectionChangedListener);
		// fOutlinePage.select((ISourceReference) element);
		// fOutlinePage.addSelectionChangedListener(fSelectionChangedListener);
		// }
	}

	protected void setSelection(ISourceReference reference, boolean moveCursor) {

		ISelection selection = getSelectionProvider().getSelection();
		if (selection instanceof TextSelection) {
			TextSelection textSelection = (TextSelection) selection;
			if (textSelection.getOffset() != 0
					|| textSelection.getLength() != 0)
				markInNavigationHistory();
		}

		if (reference != null) {

			StyledText textWidget = null;

			ISourceViewer sourceViewer = getSourceViewer();
			if (sourceViewer != null)
				textWidget = sourceViewer.getTextWidget();

			if (textWidget == null)
				return;

			try {

				ISourceRange range = reference.getSourceRange();
				if (range == null)
					return;

				int offset = range.getOffset();
				int length = range.getLength();

				if (offset < 0 || length < 0)
					return;

				textWidget.setRedraw(false);

				setHighlightRange(offset, length, moveCursor);

				if (!moveCursor)
					return;

				offset = -1;
				length = -1;

				if (reference instanceof IMember) {
					range = ((IMember) reference).getNameRange();
					if (range != null) {
						offset = range.getOffset();
						length = range.getLength();
					}
				}
				// else if (reference instanceof IImportDeclaration) {
				// String name= ((IImportDeclaration)
				// reference).getElementName();
				// if (name != null && name.length() > 0) {
				// String content= reference.getSource();
				// if (content != null) {
				// offset= range.getOffset() + content.indexOf(name);
				// length= name.length();
				// }
				// }
				// } else if (reference instanceof IPackageDeclaration) {
				// String name= ((IPackageDeclaration)
				// reference).getElementName();
				// if (name != null && name.length() > 0) {
				// String content= reference.getSource();
				// if (content != null) {
				// offset= range.getOffset() + content.indexOf(name);
				// length= name.length();
				// }
				// }
				// }

				if (offset > -1 && length > 0) {
					sourceViewer.revealRange(offset, length);
					sourceViewer.setSelectedRange(offset, length);
				}

			} catch (JavaModelException x) {
			} catch (IllegalArgumentException x) {
			} finally {
				if (textWidget != null)
					textWidget.setRedraw(true);
			}

		} else if (moveCursor) {
			resetHighlightRange();
		}

		markInNavigationHistory();
	}

	public void setSelection(IJavaElement element) {
		if (element == null || element instanceof ICompilationUnit) { // ||
			// element
			// instanceof
			// IClassFile)
			// {
			/*
			 * If the element is an ICompilationUnit this unit is either the
			 * input of this editor or not being displayed. In both cases,
			 * nothing should happened.
			 * (http://dev.eclipse.org/bugs/show_bug.cgi?id=5128)
			 */
			return;
		}

		IJavaElement corresponding = getCorrespondingElement(element);
		if (corresponding instanceof ISourceReference) {
			ISourceReference reference = (ISourceReference) corresponding;
			// set highlight range
			setSelection(reference, true);
			// set outliner selection
			if (fOutlinePage != null) {
				fOutlineSelectionChangedListener.uninstall(fOutlinePage);
				fOutlinePage.select(reference);
				fOutlineSelectionChangedListener.install(fOutlinePage);
			}
		}
	}

	public synchronized void editingScriptStarted() {
		++fIgnoreOutlinePageSelection;
	}

	public synchronized void editingScriptEnded() {
		--fIgnoreOutlinePageSelection;
	}

	public synchronized boolean isEditingScriptRunning() {
		return (fIgnoreOutlinePageSelection > 0);
	}

	/**
	 * The <code>PHPEditor</code> implementation of this
	 * <code>AbstractTextEditor</code> method performs gets the java content
	 * outline page if request is for a an outline page.
	 */
	public Object getAdapter(Class required) {

		if (IContentOutlinePage.class.equals(required)) {
			if (fOutlinePage == null)
				fOutlinePage = createOutlinePage();
			return fOutlinePage;
		}

		if (IEncodingSupport.class.equals(required))
			return fEncodingSupport;

		if (required == IShowInTargetList.class) {
			return new IShowInTargetList() {
				public String[] getShowInTargetIds() {
					return new String[] { JavaUI.ID_PACKAGES,
							IPageLayout.ID_OUTLINE, IPageLayout.ID_RES_NAV };
				}

			};
		}
		if (fProjectionSupport != null) {
			Object adapter = fProjectionSupport.getAdapter(getSourceViewer(),
					required);
			if (adapter != null)
				return adapter;
		}

		return super.getAdapter(required);
	}

	// public Object getAdapter(Class required) {
	// if (IContentOutlinePage.class.equals(required)) {
	// if (fOutlinePage == null) {
	// fOutlinePage = new PHPContentOutlinePage(getDocumentProvider(), this);
	// if (getEditorInput() != null)
	// fOutlinePage.setInput(getEditorInput());
	// }
	// return fOutlinePage;
	// }
	//
	// if (IEncodingSupport.class.equals(required))
	// return fEncodingSupport;
	//
	// return super.getAdapter(required);
	// }

	protected void doSelectionChanged(SelectionChangedEvent event) {
		ISourceReference reference = null;

		ISelection selection = event.getSelection();
		Iterator iter = ((IStructuredSelection) selection).iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			if (o instanceof ISourceReference) {
				reference = (ISourceReference) o;
				break;
			}
		}

		if (!isActivePart() && PHPeclipsePlugin.getActivePage() != null)
			PHPeclipsePlugin.getActivePage().bringToTop(this);

		try {
			editingScriptStarted();
			setSelection(reference, !isActivePart());
		} finally {
			editingScriptEnded();
		}
	}

	/*
	 * @see AbstractTextEditor#adjustHighlightRange(int, int)
	 */
	protected void adjustHighlightRange(int offset, int length) {

		try {

			IJavaElement element = getElementAt(offset);
			while (element instanceof ISourceReference) {
				ISourceRange range = ((ISourceReference) element)
						.getSourceRange();
				if (offset < range.getOffset() + range.getLength()
						&& range.getOffset() < offset + length) {

					ISourceViewer viewer = getSourceViewer();
					if (viewer instanceof ITextViewerExtension5) {
						ITextViewerExtension5 extension = (ITextViewerExtension5) viewer;
						extension.exposeModelRange(new Region(
								range.getOffset(), range.getLength()));
					}

					setHighlightRange(range.getOffset(), range.getLength(),
							true);
					if (fOutlinePage != null) {
						fOutlineSelectionChangedListener
								.uninstall(fOutlinePage);
						fOutlinePage.select((ISourceReference) element);
						fOutlineSelectionChangedListener.install(fOutlinePage);
					}

					return;
				}
				element = element.getParent();
			}

		} catch (JavaModelException x) {
			PHPeclipsePlugin.log(x.getStatus());
		}

		ISourceViewer viewer = getSourceViewer();
		if (viewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension = (ITextViewerExtension5) viewer;
			extension.exposeModelRange(new Region(offset, length));
		} else {
			resetHighlightRange();
		}

	}

	protected boolean isActivePart() {
		IWorkbenchWindow window = getSite().getWorkbenchWindow();
		IPartService service = window.getPartService();
		IWorkbenchPart part = service.getActivePart();
		return part != null && part.equals(this);
	}

	// public void openContextHelp() {
	// IDocument doc =
	// this.getDocumentProvider().getDocument(this.getEditorInput());
	// ITextSelection selection = (ITextSelection)
	// this.getSelectionProvider().getSelection();
	// int pos = selection.getOffset();
	// String word = getFunctionName(doc, pos);
	// openContextHelp(word);
	// }
	//
	// private void openContextHelp(String word) {
	// open(word);
	// }
	//
	// public static void open(String word) {
	// IHelp help = WorkbenchHelp.getHelpSupport();
	// if (help != null) {
	// IHelpResource helpResource = new PHPFunctionHelpResource(word);
	// WorkbenchHelp.getHelpSupport().displayHelpResource(helpResource);
	// } else {
	// // showMessage(shell, dialogTitle, ActionMessages.getString("Open help
	// not available"), false); //$NON-NLS-1$
	// }
	// }

	// private String getFunctionName(IDocument doc, int pos) {
	// Point word = PHPWordExtractor.findWord(doc, pos);
	// if (word != null) {
	// try {
	// return doc.get(word.x, word.y).replace('_', '-');
	// } catch (BadLocationException e) {
	// }
	// }
	// return "";
	// }

	/*
	 * @see AbstractTextEditor#handlePreferenceStoreChanged(PropertyChangeEvent)
	 */
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {

		try {

			ISourceViewer sourceViewer = getSourceViewer();
			if (sourceViewer == null)
				return;

			String property = event.getProperty();

			if (PreferenceConstants.EDITOR_TAB_WIDTH.equals(property)) {
				Object value = event.getNewValue();
				if (value instanceof Integer) {
					sourceViewer.getTextWidget().setTabs(
							((Integer) value).intValue());
				} else if (value instanceof String) {
					try {
						sourceViewer.getTextWidget().setTabs(
								Integer.parseInt((String) value));
					} catch (NumberFormatException e) {
						// bug #1038071 - set default tab:
						sourceViewer.getTextWidget().setTabs(80);
					}
				}
				return;
			}

			// if (OVERVIEW_RULER.equals(property)) {
			// if (isOverviewRulerVisible())
			// showOverviewRuler();
			// else
			// hideOverviewRuler();
			// return;
			// }

			// if (LINE_NUMBER_RULER.equals(property)) {
			// if (isLineNumberRulerVisible())
			// showLineNumberRuler();
			// else
			// hideLineNumberRuler();
			// return;
			// }

			// if (fLineNumberRulerColumn != null
			// && (LINE_NUMBER_COLOR.equals(property) ||
			// PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT.equals(property) ||
			// PREFERENCE_COLOR_BACKGROUND.equals(property))) {
			//
			// initializeLineNumberRulerColumn(fLineNumberRulerColumn);
			// }

			if (isJavaEditorHoverProperty(property))
				updateHoverBehavior();

			if (BROWSER_LIKE_LINKS.equals(property)) {
				if (isBrowserLikeLinks())
					enableBrowserLikeLinks();
				else
					disableBrowserLikeLinks();
				return;
			}

			if (PreferenceConstants.EDITOR_DISABLE_OVERWRITE_MODE
					.equals(property)) {
				if (event.getNewValue() instanceof Boolean) {
					Boolean disable = (Boolean) event.getNewValue();
					enableOverwriteMode(!disable.booleanValue());
				}
				return;
			}

			boolean newBooleanValue = false;
			Object newValue = event.getNewValue();
			if (newValue != null)
				newBooleanValue = Boolean.valueOf(newValue.toString())
						.booleanValue();

			if (PreferenceConstants.EDITOR_SYNC_OUTLINE_ON_CURSOR_MOVE
					.equals(property)) {
				if (newBooleanValue)
					selectionChanged();
				return;
			}

			if (PreferenceConstants.EDITOR_MARK_OCCURRENCES.equals(property)) {
				if (newBooleanValue != fMarkOccurrenceAnnotations) {
					fMarkOccurrenceAnnotations = newBooleanValue;
					if (!fMarkOccurrenceAnnotations)
						uninstallOccurrencesFinder();
					else
						installOccurrencesFinder();
				}
				return;
			}

			if (PreferenceConstants.EDITOR_STICKY_OCCURRENCES.equals(property)) {
				fStickyOccurrenceAnnotations = newBooleanValue;
				return;
			}
			// }
			// }
			// if
			// (PreferenceConstants.EDITOR_STICKY_OCCURRENCES.equals(property))
			// {
			// if (event.getNewValue() instanceof Boolean) {
			// boolean stickyOccurrenceAnnotations=
			// ((Boolean)event.getNewValue()).booleanValue();
			// if (stickyOccurrenceAnnotations != fStickyOccurrenceAnnotations)
			// {

			((PHPSourceViewerConfiguration) getSourceViewerConfiguration())
					.handlePropertyChangeEvent(event);

			// if (affectsOverrideIndicatorAnnotations(event)) {
			// if (isShowingOverrideIndicators()) {
			// if (fOverrideIndicatorManager == null)
			// installOverrideIndicator(true);
			// } else {
			// if (fOverrideIndicatorManager != null)
			// uninstallOverrideIndicator();
			// }
			// return;
			// }

			if (PreferenceConstants.EDITOR_FOLDING_PROVIDER.equals(property)) {
				if (sourceViewer instanceof ProjectionViewer) {
					ProjectionViewer projectionViewer = (ProjectionViewer) sourceViewer;
					if (fProjectionModelUpdater != null)
						fProjectionModelUpdater.uninstall();
					// either freshly enabled or provider changed
					fProjectionModelUpdater = PHPeclipsePlugin.getDefault()
							.getFoldingStructureProviderRegistry()
							.getCurrentFoldingProvider();
					if (fProjectionModelUpdater != null) {
						fProjectionModelUpdater.install(this, projectionViewer);
					}
				}
				return;
			}
		} finally {
			super.handlePreferenceStoreChanged(event);
		}
	}

	// /*
	// * @see
	// AbstractTextEditor#handlePreferenceStoreChanged(PropertyChangeEvent)
	// */
	// protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
	//
	// try {
	//
	// ISourceViewer sourceViewer = getSourceViewer();
	// if (sourceViewer == null)
	// return;
	//
	// String property = event.getProperty();
	//
	// // if
	// (JavaSourceViewerConfiguration.PREFERENCE_TAB_WIDTH.equals(property)) {
	// // Object value= event.getNewValue();
	// // if (value instanceof Integer) {
	// // sourceViewer.getTextWidget().setTabs(((Integer) value).intValue());
	// // } else if (value instanceof String) {
	// // sourceViewer.getTextWidget().setTabs(Integer.parseInt((String)
	// value));
	// // }
	// // return;
	// // }
	//
	// if (IPreferenceConstants.LINE_NUMBER_RULER.equals(property)) {
	// if (isLineNumberRulerVisible())
	// showLineNumberRuler();
	// else
	// hideLineNumberRuler();
	// return;
	// }
	//
	// if (fLineNumberRulerColumn != null
	// && (IPreferenceConstants.LINE_NUMBER_COLOR.equals(property)
	// || PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT.equals(property)
	// || PREFERENCE_COLOR_BACKGROUND.equals(property))) {
	//
	// initializeLineNumberRulerColumn(fLineNumberRulerColumn);
	// }
	//
	// } finally {
	// super.handlePreferenceStoreChanged(event);
	// }
	// }

	// private boolean isJavaEditorHoverProperty(String property) {
	// return PreferenceConstants.EDITOR_DEFAULT_HOVER.equals(property)
	// || PreferenceConstants.EDITOR_NONE_HOVER.equals(property)
	// || PreferenceConstants.EDITOR_CTRL_HOVER.equals(property)
	// || PreferenceConstants.EDITOR_SHIFT_HOVER.equals(property)
	// || PreferenceConstants.EDITOR_CTRL_ALT_HOVER.equals(property)
	// || PreferenceConstants.EDITOR_CTRL_SHIFT_HOVER.equals(property)
	// || PreferenceConstants.EDITOR_CTRL_ALT_SHIFT_HOVER.equals(property)
	// || PreferenceConstants.EDITOR_ALT_SHIFT_HOVER.equals(property);
	// }

	/**
	 * Shows the line number ruler column.
	 */
	// private void showLineNumberRuler() {
	// IVerticalRuler v = getVerticalRuler();
	// if (v instanceof CompositeRuler) {
	// CompositeRuler c = (CompositeRuler) v;
	// c.addDecorator(1, createLineNumberRulerColumn());
	// }
	// }
	private boolean isJavaEditorHoverProperty(String property) {
		return PreferenceConstants.EDITOR_TEXT_HOVER_MODIFIERS.equals(property);
	}

	/**
	 * Return whether the browser like links should be enabled according to the
	 * preference store settings.
	 * 
	 * @return <code>true</code> if the browser like links should be enabled
	 */
	private boolean isBrowserLikeLinks() {
		IPreferenceStore store = getPreferenceStore();
		return store.getBoolean(BROWSER_LIKE_LINKS);
	}

	/**
	 * Enables browser like links.
	 */
	private void enableBrowserLikeLinks() {
		if (fMouseListener == null) {
			fMouseListener = new MouseClickListener();
			fMouseListener.install();
		}
	}

	/**
	 * Disables browser like links.
	 */
	private void disableBrowserLikeLinks() {
		if (fMouseListener != null) {
			fMouseListener.uninstall();
			fMouseListener = null;
		}
	}

	/**
	 * Handles a property change event describing a change of the java core's
	 * preferences and updates the preference related editor properties.
	 * 
	 * @param event
	 *            the property change event
	 */
	protected void handlePreferencePropertyChanged(
			org.eclipse.core.runtime.Preferences.PropertyChangeEvent event) {
		if (COMPILER_TASK_TAGS.equals(event.getProperty())) {
			ISourceViewer sourceViewer = getSourceViewer();
			if (sourceViewer != null
					&& affectsTextPresentation(new PropertyChangeEvent(event
							.getSource(), event.getProperty(), event
							.getOldValue(), event.getNewValue())))
				sourceViewer.invalidateTextPresentation();
		}
		if (PreferenceConstants.EDITOR_WRAP_WORDS.equals(event.getProperty())) {
			setWordWrap();
		}
	}

	/**
	 * Return whether the line number ruler column should be visible according
	 * to the preference store settings.
	 * 
	 * @return <code>true</code> if the line numbers should be visible
	 */
	// protected boolean isLineNumberRulerVisible() {
	// IPreferenceStore store = getPreferenceStore();
	// return store.getBoolean(LINE_NUMBER_RULER);
	// }
	/**
	 * Hides the line number ruler column.
	 */
	// private void hideLineNumberRuler() {
	// IVerticalRuler v = getVerticalRuler();
	// if (v instanceof CompositeRuler) {
	// CompositeRuler c = (CompositeRuler) v;
	// try {
	// c.removeDecorator(1);
	// } catch (Throwable e) {
	// }
	// }
	// }
	/*
	 * @see AbstractTextEditor#handleCursorPositionChanged()
	 */
	// protected void handleCursorPositionChanged() {
	// super.handleCursorPositionChanged();
	// if (!isEditingScriptRunning() && fUpdater != null)
	// fUpdater.post();
	// }
	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#handleElementContentReplaced()
	 */
	protected void handleElementContentReplaced() {
		super.handleElementContentReplaced();
		if (fProjectionModelUpdater != null)
			fProjectionModelUpdater.initialize();
	}

	/**
	 * Initializes the given line number ruler column from the preference store.
	 * 
	 * @param rulerColumn
	 *            the ruler column to be initialized
	 */
	// protected void initializeLineNumberRulerColumn(LineNumberRulerColumn
	// rulerColumn) {
	// JavaTextTools textTools =
	// PHPeclipsePlugin.getDefault().getJavaTextTools();
	// IColorManager manager = textTools.getColorManager();
	//
	// IPreferenceStore store = getPreferenceStore();
	// if (store != null) {
	//
	// RGB rgb = null;
	// // foreground color
	// if (store.contains(LINE_NUMBER_COLOR)) {
	// if (store.isDefault(LINE_NUMBER_COLOR))
	// rgb = PreferenceConverter.getDefaultColor(store, LINE_NUMBER_COLOR);
	// else
	// rgb = PreferenceConverter.getColor(store, LINE_NUMBER_COLOR);
	// }
	// rulerColumn.setForeground(manager.getColor(rgb));
	//
	// rgb = null;
	// // background color
	// if (!store.getBoolean(PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT)) {
	// if (store.contains(PREFERENCE_COLOR_BACKGROUND)) {
	// if (store.isDefault(PREFERENCE_COLOR_BACKGROUND))
	// rgb = PreferenceConverter.getDefaultColor(store,
	// PREFERENCE_COLOR_BACKGROUND);
	// else
	// rgb = PreferenceConverter.getColor(store, PREFERENCE_COLOR_BACKGROUND);
	// }
	// }
	// rulerColumn.setBackground(manager.getColor(rgb));
	// }
	// }
	/**
	 * Creates a new line number ruler column that is appropriately initialized.
	 */
	// protected IVerticalRulerColumn createLineNumberRulerColumn() {
	// fLineNumberRulerColumn = new LineNumberRulerColumn();
	// initializeLineNumberRulerColumn(fLineNumberRulerColumn);
	// return fLineNumberRulerColumn;
	// }
	/*
	 * @see AbstractTextEditor#createVerticalRuler()
	 */
	// protected IVerticalRuler createVerticalRuler() {
	// CompositeRuler ruler = new CompositeRuler();
	// ruler.addDecorator(0, new AnnotationRulerColumn(VERTICAL_RULER_WIDTH));
	// if (isLineNumberRulerVisible())
	// ruler.addDecorator(1, createLineNumberRulerColumn());
	// return ruler;
	// }
	// private static IRegion getSignedSelection(ITextViewer viewer) {
	//
	// StyledText text = viewer.getTextWidget();
	// int caretOffset = text.getCaretOffset();
	// Point selection = text.getSelection();
	//
	// // caret left
	// int offset, length;
	// if (caretOffset == selection.x) {
	// offset = selection.y;
	// length = selection.x - selection.y;
	//
	// // caret right
	// } else {
	// offset = selection.x;
	// length = selection.y - selection.x;
	// }
	//
	// return new Region(offset, length);
	// }
	protected IRegion getSignedSelection(ISourceViewer sourceViewer) {
		StyledText text = sourceViewer.getTextWidget();
		Point selection = text.getSelectionRange();

		if (text.getCaretOffset() == selection.x) {
			selection.x = selection.x + selection.y;
			selection.y = -selection.y;
		}

		selection.x = widgetOffset2ModelOffset(sourceViewer, selection.x);

		return new Region(selection.x, selection.y);
	}

	/** Preference key for matching brackets */
	protected final static String MATCHING_BRACKETS = PreferenceConstants.EDITOR_MATCHING_BRACKETS;

	/** Preference key for matching brackets color */
	protected final static String MATCHING_BRACKETS_COLOR = PreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR;

	/** Preference key for highlighting current line */
	// protected final static String CURRENT_LINE =
	// PreferenceConstants.EDITOR_CURRENT_LINE;
	/** Preference key for highlight color of current line */
	// protected final static String CURRENT_LINE_COLOR =
	// PreferenceConstants.EDITOR_CURRENT_LINE_COLOR;
	/** Preference key for showing print marging ruler */
	// protected final static String PRINT_MARGIN =
	// PreferenceConstants.EDITOR_PRINT_MARGIN;
	/** Preference key for print margin ruler color */
	// protected final static String PRINT_MARGIN_COLOR =
	// PreferenceConstants.EDITOR_PRINT_MARGIN_COLOR;
	/** Preference key for print margin ruler column */
	// protected final static String PRINT_MARGIN_COLUMN =
	// PreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN;
	/** Preference key for error indication */
	// protected final static String ERROR_INDICATION =
	// PreferenceConstants.EDITOR_PROBLEM_INDICATION;
	/** Preference key for error color */
	// protected final static String ERROR_INDICATION_COLOR =
	// PreferenceConstants.EDITOR_PROBLEM_INDICATION_COLOR;
	/** Preference key for warning indication */
	// protected final static String WARNING_INDICATION =
	// PreferenceConstants.EDITOR_WARNING_INDICATION;
	/** Preference key for warning color */
	// protected final static String WARNING_INDICATION_COLOR =
	// PreferenceConstants.EDITOR_WARNING_INDICATION_COLOR;
	/** Preference key for task indication */
	protected final static String TASK_INDICATION = PreferenceConstants.EDITOR_TASK_INDICATION;

	/** Preference key for task color */
	protected final static String TASK_INDICATION_COLOR = PreferenceConstants.EDITOR_TASK_INDICATION_COLOR;

	/** Preference key for bookmark indication */
	protected final static String BOOKMARK_INDICATION = PreferenceConstants.EDITOR_BOOKMARK_INDICATION;

	/** Preference key for bookmark color */
	protected final static String BOOKMARK_INDICATION_COLOR = PreferenceConstants.EDITOR_BOOKMARK_INDICATION_COLOR;

	/** Preference key for search result indication */
	protected final static String SEARCH_RESULT_INDICATION = PreferenceConstants.EDITOR_SEARCH_RESULT_INDICATION;

	/** Preference key for search result color */
	protected final static String SEARCH_RESULT_INDICATION_COLOR = PreferenceConstants.EDITOR_SEARCH_RESULT_INDICATION_COLOR;

	/** Preference key for unknown annotation indication */
	protected final static String UNKNOWN_INDICATION = PreferenceConstants.EDITOR_UNKNOWN_INDICATION;

	/** Preference key for unknown annotation color */
	protected final static String UNKNOWN_INDICATION_COLOR = PreferenceConstants.EDITOR_UNKNOWN_INDICATION_COLOR;

	/** Preference key for shwoing the overview ruler */
	protected final static String OVERVIEW_RULER = PreferenceConstants.EDITOR_OVERVIEW_RULER;

	/** Preference key for error indication in overview ruler */
	protected final static String ERROR_INDICATION_IN_OVERVIEW_RULER = PreferenceConstants.EDITOR_ERROR_INDICATION_IN_OVERVIEW_RULER;

	/** Preference key for warning indication in overview ruler */
	protected final static String WARNING_INDICATION_IN_OVERVIEW_RULER = PreferenceConstants.EDITOR_WARNING_INDICATION_IN_OVERVIEW_RULER;

	/** Preference key for task indication in overview ruler */
	protected final static String TASK_INDICATION_IN_OVERVIEW_RULER = PreferenceConstants.EDITOR_TASK_INDICATION_IN_OVERVIEW_RULER;

	/** Preference key for bookmark indication in overview ruler */
	protected final static String BOOKMARK_INDICATION_IN_OVERVIEW_RULER = PreferenceConstants.EDITOR_BOOKMARK_INDICATION_IN_OVERVIEW_RULER;

	/** Preference key for search result indication in overview ruler */
	protected final static String SEARCH_RESULT_INDICATION_IN_OVERVIEW_RULER = PreferenceConstants.EDITOR_SEARCH_RESULT_INDICATION_IN_OVERVIEW_RULER;

	/** Preference key for unknown annotation indication in overview ruler */
	protected final static String UNKNOWN_INDICATION_IN_OVERVIEW_RULER = PreferenceConstants.EDITOR_UNKNOWN_INDICATION_IN_OVERVIEW_RULER;

	// /** Preference key for compiler task tags */
	// private final static String COMPILER_TASK_TAGS=
	// JavaCore.COMPILER_TASK_TAGS;
	/** Preference key for browser like links */
	private final static String BROWSER_LIKE_LINKS = PreferenceConstants.EDITOR_BROWSER_LIKE_LINKS;

	/** Preference key for key modifier of browser like links */
	private final static String BROWSER_LIKE_LINKS_KEY_MODIFIER = PreferenceConstants.EDITOR_BROWSER_LIKE_LINKS_KEY_MODIFIER;

	/**
	 * Preference key for key modifier mask of browser like links. The value is
	 * only used if the value of <code>EDITOR_BROWSER_LIKE_LINKS</code> cannot
	 * be resolved to valid SWT modifier bits.
	 * 
	 * @since 2.1.1
	 */
	private final static String BROWSER_LIKE_LINKS_KEY_MODIFIER_MASK = PreferenceConstants.EDITOR_BROWSER_LIKE_LINKS_KEY_MODIFIER_MASK;

	private final static char[] BRACKETS = { '{', '}', '(', ')', '[', ']' };

	private static boolean isBracket(char character) {
		for (int i = 0; i != BRACKETS.length; ++i)
			if (character == BRACKETS[i])
				return true;
		return false;
	}

	private static boolean isSurroundedByBrackets(IDocument document, int offset) {
		if (offset == 0 || offset == document.getLength())
			return false;

		try {
			return isBracket(document.getChar(offset - 1))
					&& isBracket(document.getChar(offset));

		} catch (BadLocationException e) {
			return false;
		}
	}

	// protected void configureSourceViewerDecorationSupport() {
	//
	// fSourceViewerDecorationSupport.setCharacterPairMatcher(fBracketMatcher);
	//
	// fSourceViewerDecorationSupport.setAnnotationPainterPreferenceKeys(
	// AnnotationType.UNKNOWN,
	// UNKNOWN_INDICATION_COLOR,
	// UNKNOWN_INDICATION,
	// UNKNOWN_INDICATION_IN_OVERVIEW_RULER,
	// 0);
	// fSourceViewerDecorationSupport.setAnnotationPainterPreferenceKeys(
	// AnnotationType.BOOKMARK,
	// BOOKMARK_INDICATION_COLOR,
	// BOOKMARK_INDICATION,
	// BOOKMARK_INDICATION_IN_OVERVIEW_RULER,
	// 1);
	// fSourceViewerDecorationSupport.setAnnotationPainterPreferenceKeys(
	// AnnotationType.TASK,
	// TASK_INDICATION_COLOR,
	// TASK_INDICATION,
	// TASK_INDICATION_IN_OVERVIEW_RULER,
	// 2);
	// fSourceViewerDecorationSupport.setAnnotationPainterPreferenceKeys(
	// AnnotationType.SEARCH,
	// SEARCH_RESULT_INDICATION_COLOR,
	// SEARCH_RESULT_INDICATION,
	// SEARCH_RESULT_INDICATION_IN_OVERVIEW_RULER,
	// 3);
	// fSourceViewerDecorationSupport.setAnnotationPainterPreferenceKeys(
	// AnnotationType.WARNING,
	// WARNING_INDICATION_COLOR,
	// WARNING_INDICATION,
	// WARNING_INDICATION_IN_OVERVIEW_RULER,
	// 4);
	// fSourceViewerDecorationSupport.setAnnotationPainterPreferenceKeys(
	// AnnotationType.ERROR,
	// ERROR_INDICATION_COLOR,
	// ERROR_INDICATION,
	// ERROR_INDICATION_IN_OVERVIEW_RULER,
	// 5);
	//
	// fSourceViewerDecorationSupport.setCursorLinePainterPreferenceKeys(CURRENT_LINE,
	// CURRENT_LINE_COLOR);
	// fSourceViewerDecorationSupport.setMarginPainterPreferenceKeys(PRINT_MARGIN,
	// PRINT_MARGIN_COLOR, PRINT_MARGIN_COLUMN);
	// fSourceViewerDecorationSupport.setMatchingCharacterPainterPreferenceKeys(MATCHING_BRACKETS,
	// MATCHING_BRACKETS_COLOR);
	//
	// fSourceViewerDecorationSupport.setSymbolicFontName(getFontPropertyPreferenceKey());
	//
	// }
	/**
	 * Returns the Java element wrapped by this editors input.
	 * 
	 * @return the Java element wrapped by this editors input.
	 * @since 3.0
	 */
	abstract protected IJavaElement getInputJavaElement();

	protected void updateStatusLine() {
		ITextSelection selection = (ITextSelection) getSelectionProvider()
				.getSelection();
		Annotation annotation = getAnnotation(selection.getOffset(), selection
				.getLength());
		setStatusLineErrorMessage(null);
		setStatusLineMessage(null);
		if (annotation != null) {
			try {
				fIsUpdatingAnnotationViews = true;
				updateAnnotationViews(annotation);
			} finally {
				fIsUpdatingAnnotationViews = false;
			}
			if (annotation instanceof IJavaAnnotation
					&& ((IJavaAnnotation) annotation).isProblem())
				setStatusLineMessage(annotation.getText());
		}
	}

	/**
	 * Jumps to the matching bracket.
	 */
	public void gotoMatchingBracket() {

		ISourceViewer sourceViewer = getSourceViewer();
		IDocument document = sourceViewer.getDocument();
		if (document == null)
			return;

		IRegion selection = getSignedSelection(sourceViewer);

		int selectionLength = Math.abs(selection.getLength());
		if (selectionLength > 1) {
			setStatusLineErrorMessage(PHPEditorMessages
					.getString("GotoMatchingBracket.error.invalidSelection")); //$NON-NLS-1$
			sourceViewer.getTextWidget().getDisplay().beep();
			return;
		}

		// #26314
		int sourceCaretOffset = selection.getOffset() + selection.getLength();
		if (isSurroundedByBrackets(document, sourceCaretOffset))
			sourceCaretOffset -= selection.getLength();

		IRegion region = fBracketMatcher.match(document, sourceCaretOffset);
		if (region == null) {
			setStatusLineErrorMessage(PHPEditorMessages
					.getString("GotoMatchingBracket.error.noMatchingBracket")); //$NON-NLS-1$
			sourceViewer.getTextWidget().getDisplay().beep();
			return;
		}

		int offset = region.getOffset();
		int length = region.getLength();

		if (length < 1)
			return;

		int anchor = fBracketMatcher.getAnchor();
		int targetOffset = (PHPPairMatcher.RIGHT == anchor) ? offset : offset
				+ length - 1;

		boolean visible = false;
		if (sourceViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension = (ITextViewerExtension5) sourceViewer;
			visible = (extension.modelOffset2WidgetOffset(targetOffset) > -1);
		} else {
			IRegion visibleRegion = sourceViewer.getVisibleRegion();
			visible = (targetOffset >= visibleRegion.getOffset() && targetOffset < visibleRegion
					.getOffset()
					+ visibleRegion.getLength());
		}

		if (!visible) {
			setStatusLineErrorMessage(PHPEditorMessages
					.getString("GotoMatchingBracket.error.bracketOutsideSelectedElement")); //$NON-NLS-1$
			sourceViewer.getTextWidget().getDisplay().beep();
			return;
		}

		if (selection.getLength() < 0)
			targetOffset -= selection.getLength();

		sourceViewer.setSelectedRange(targetOffset, selection.getLength());
		sourceViewer.revealRange(targetOffset, selection.getLength());
	}

	/**
	 * Ses the given message as error message to this editor's status line.
	 * 
	 * @param msg
	 *            message to be set
	 */
	protected void setStatusLineErrorMessage(String msg) {
		IEditorStatusLine statusLine = (IEditorStatusLine) getAdapter(IEditorStatusLine.class);
		if (statusLine != null)
			statusLine.setMessage(true, msg, null);
	}

	/**
	 * Sets the given message as message to this editor's status line.
	 * 
	 * @param msg
	 *            message to be set
	 * @since 3.0
	 */
	protected void setStatusLineMessage(String msg) {
		IEditorStatusLine statusLine = (IEditorStatusLine) getAdapter(IEditorStatusLine.class);
		if (statusLine != null)
			statusLine.setMessage(false, msg, null);
	}

	/**
	 * Returns the annotation closest to the given range respecting the given
	 * direction. If an annotation is found, the annotations current position is
	 * copied into the provided annotation position.
	 * 
	 * @param offset
	 *            the region offset
	 * @param length
	 *            the region length
	 * @param forward
	 *            <code>true</code> for forwards, <code>false</code> for
	 *            backward
	 * @param annotationPosition
	 *            the position of the found annotation
	 * @return the found annotation
	 */
	private Annotation getNextAnnotation(final int offset, final int length,
			boolean forward, Position annotationPosition) {

		Annotation nextAnnotation = null;
		Position nextAnnotationPosition = null;
		Annotation containingAnnotation = null;
		Position containingAnnotationPosition = null;
		boolean currentAnnotation = false;

		IDocument document = getDocumentProvider()
				.getDocument(getEditorInput());
		int endOfDocument = document.getLength();
		int distance = Integer.MAX_VALUE;

		IAnnotationModel model = getDocumentProvider().getAnnotationModel(
				getEditorInput());
		Iterator e = new JavaAnnotationIterator(model, true, true);
		while (e.hasNext()) {
			Annotation a = (Annotation) e.next();
			if ((a instanceof IJavaAnnotation)
					&& ((IJavaAnnotation) a).hasOverlay()
					|| !isNavigationTarget(a))
				continue;

			Position p = model.getPosition(a);
			if (p == null)
				continue;

			if (forward && p.offset == offset || !forward
					&& p.offset + p.getLength() == offset + length) {// ||
				// p.includes(offset))
				// {
				if (containingAnnotation == null
						|| (forward
								&& p.length >= containingAnnotationPosition.length || !forward
								&& p.length >= containingAnnotationPosition.length)) {
					containingAnnotation = a;
					containingAnnotationPosition = p;
					currentAnnotation = p.length == length;
				}
			} else {
				int currentDistance = 0;

				if (forward) {
					currentDistance = p.getOffset() - offset;
					if (currentDistance < 0)
						currentDistance = endOfDocument + currentDistance;

					if (currentDistance < distance
							|| currentDistance == distance
							&& p.length < nextAnnotationPosition.length) {
						distance = currentDistance;
						nextAnnotation = a;
						nextAnnotationPosition = p;
					}
				} else {
					currentDistance = offset + length
							- (p.getOffset() + p.length);
					if (currentDistance < 0)
						currentDistance = endOfDocument + currentDistance;

					if (currentDistance < distance
							|| currentDistance == distance
							&& p.length < nextAnnotationPosition.length) {
						distance = currentDistance;
						nextAnnotation = a;
						nextAnnotationPosition = p;
					}
				}
			}
		}
		if (containingAnnotationPosition != null
				&& (!currentAnnotation || nextAnnotation == null)) {
			annotationPosition.setOffset(containingAnnotationPosition
					.getOffset());
			annotationPosition.setLength(containingAnnotationPosition
					.getLength());
			return containingAnnotation;
		}
		if (nextAnnotationPosition != null) {
			annotationPosition.setOffset(nextAnnotationPosition.getOffset());
			annotationPosition.setLength(nextAnnotationPosition.getLength());
		}

		return nextAnnotation;
	}

	/**
	 * Returns the annotation overlapping with the given range or
	 * <code>null</code>.
	 * 
	 * @param offset
	 *            the region offset
	 * @param length
	 *            the region length
	 * @return the found annotation or <code>null</code>
	 * @since 3.0
	 */
	private Annotation getAnnotation(int offset, int length) {
		IAnnotationModel model = getDocumentProvider().getAnnotationModel(
				getEditorInput());
		Iterator e = new JavaAnnotationIterator(model, true, true);
		while (e.hasNext()) {
			Annotation a = (Annotation) e.next();
			if (!isNavigationTarget(a))
				continue;

			Position p = model.getPosition(a);
			if (p != null && p.overlapsWith(offset, length))
				return a;
		}

		return null;
	}

	/**
	 * Returns whether the given annotation is configured as a target for the
	 * "Go to Next/Previous Annotation" actions
	 * 
	 * @param annotation
	 *            the annotation
	 * @return <code>true</code> if this is a target, <code>false</code>
	 *         otherwise
	 * @since 3.0
	 */
	protected boolean isNavigationTarget(Annotation annotation) {
		Preferences preferences = EditorsUI.getPluginPreferences();
		AnnotationPreference preference = getAnnotationPreferenceLookup()
				.getAnnotationPreference(annotation);
		// See bug 41689
		// String key= forward ? preference.getIsGoToNextNavigationTargetKey() :
		// preference.getIsGoToPreviousNavigationTargetKey();
		String key = preference == null ? null : preference
				.getIsGoToNextNavigationTargetKey();
		return (key != null && preferences.getBoolean(key));
	}

	/**
	 * Returns a segmentation of the line of the given document appropriate for
	 * bidi rendering. The default implementation returns only the string
	 * literals of a php code line as segments.
	 * 
	 * @param document
	 *            the document
	 * @param lineOffset
	 *            the offset of the line
	 * @return the line's bidi segmentation
	 * @throws BadLocationException
	 *             in case lineOffset is not valid in document
	 */
	public static int[] getBidiLineSegments(IDocument document, int lineOffset)
			throws BadLocationException {

		IRegion line = document.getLineInformationOfOffset(lineOffset);
		ITypedRegion[] linePartitioning = document.computePartitioning(
				lineOffset, line.getLength());

		List segmentation = new ArrayList();
		for (int i = 0; i < linePartitioning.length; i++) {
			if (IPHPPartitions.PHP_STRING_DQ.equals(linePartitioning[i]
					.getType())) {
				segmentation.add(linePartitioning[i]);
			} else if (IPHPPartitions.PHP_STRING_HEREDOC
					.equals(linePartitioning[i].getType())) {
				segmentation.add(linePartitioning[i]);
			}
		}

		if (segmentation.size() == 0)
			return null;

		int size = segmentation.size();
		int[] segments = new int[size * 2 + 1];

		int j = 0;
		for (int i = 0; i < size; i++) {
			ITypedRegion segment = (ITypedRegion) segmentation.get(i);

			if (i == 0)
				segments[j++] = 0;

			int offset = segment.getOffset() - lineOffset;
			if (offset > segments[j - 1])
				segments[j++] = offset;

			if (offset + segment.getLength() >= line.getLength())
				break;

			segments[j++] = offset + segment.getLength();
		}

		if (j < segments.length) {
			int[] result = new int[j];
			System.arraycopy(segments, 0, result, 0, j);
			segments = result;
		}

		return segments;
	}

	/**
	 * Returns a segmentation of the given line appropriate for bidi rendering.
	 * The default implementation returns only the string literals of a php code
	 * line as segments.
	 * 
	 * @param lineOffset
	 *            the offset of the line
	 * @param line
	 *            the content of the line
	 * @return the line's bidi segmentation
	 */
	protected int[] getBidiLineSegments(int lineOffset, String line) {
		IDocumentProvider provider = getDocumentProvider();
		if (provider != null && line != null && line.length() > 0) {
			IDocument document = provider.getDocument(getEditorInput());
			if (document != null)
				try {
					return getBidiLineSegments(document, lineOffset);
				} catch (BadLocationException x) {
					// ignore
				}
		}
		return null;
	}

	/*
	 * @see AbstractTextEditor#createSourceViewer(Composite, IVerticalRuler,
	 *      int)
	 */
	// protected final ISourceViewer createSourceViewer(
	// Composite parent,
	// IVerticalRuler ruler,
	// int styles) {
	// ISourceViewer viewer = createJavaSourceViewer(parent, ruler, styles);
	// StyledText text = viewer.getTextWidget();
	// text.addBidiSegmentListener(new BidiSegmentListener() {
	// public void lineGetSegments(BidiSegmentEvent event) {
	// event.segments = getBidiLineSegments(event.lineOffset, event.lineText);
	// }
	// });
	// // JavaUIHelp.setHelp(this, text, IJavaHelpContextIds.JAVA_EDITOR);
	// return viewer;
	// }
	public final ISourceViewer getViewer() {
		return getSourceViewer();
	}

	// protected void showOverviewRuler() {
	// if (fOverviewRuler != null) {
	// if (getSourceViewer() instanceof ISourceViewerExtension) {
	// ((ISourceViewerExtension)
	// getSourceViewer()).showAnnotationsOverview(true);
	// fSourceViewerDecorationSupport.updateOverviewDecorations();
	// }
	// }
	// }
	//
	// protected void hideOverviewRuler() {
	// if (getSourceViewer() instanceof ISourceViewerExtension) {
	// fSourceViewerDecorationSupport.hideAnnotationOverview();
	// ((ISourceViewerExtension)
	// getSourceViewer()).showAnnotationsOverview(false);
	// }
	// }

	// protected boolean isOverviewRulerVisible() {
	// IPreferenceStore store = getPreferenceStore();
	// return store.getBoolean(OVERVIEW_RULER);
	// }
	/*
	 * @see AbstractTextEditor#createSourceViewer(Composite, IVerticalRuler,
	 *      int)
	 */
	// protected ISourceViewer createJavaSourceViewer(
	// Composite parent,
	// IVerticalRuler ruler,
	// IOverviewRuler overviewRuler,
	// boolean isOverviewRulerVisible,
	// int styles) {
	// return new SourceViewer(parent, ruler, overviewRuler,
	// isOverviewRulerVisible(), styles);
	// }
	/*
	 * @see AbstractTextEditor#createSourceViewer(Composite, IVerticalRuler,
	 *      int)
	 */
	protected ISourceViewer createJavaSourceViewer(Composite parent,
			IVerticalRuler verticalRuler, IOverviewRuler overviewRuler,
			boolean isOverviewRulerVisible, int styles, IPreferenceStore store) {
		return new JavaSourceViewer(parent, verticalRuler, getOverviewRuler(),
				isOverviewRulerVisible(), styles, store);
	}

	/*
	 * @see AbstractTextEditor#createSourceViewer(Composite, IVerticalRuler,
	 *      int)
	 */
	protected final ISourceViewer createSourceViewer(Composite parent,
			IVerticalRuler verticalRuler, int styles) {

		ISourceViewer viewer = createJavaSourceViewer(parent, verticalRuler,
				getOverviewRuler(), isOverviewRulerVisible(), styles,
				getPreferenceStore());

		StyledText text = viewer.getTextWidget();
		text.addBidiSegmentListener(new BidiSegmentListener() {
			public void lineGetSegments(BidiSegmentEvent event) {
				event.segments = getBidiLineSegments(event.lineOffset,
						event.lineText);
			}
		});

		// JavaUIHelp.setHelp(this, text, IJavaHelpContextIds.JAVA_EDITOR);

		// ensure source viewer decoration support has been created and
		// configured
		getSourceViewerDecorationSupport(viewer);

		return viewer;
	}

	/*
	 * @see AbstractTextEditor#affectsTextPresentation(PropertyChangeEvent)
	 */
	protected boolean affectsTextPresentation(PropertyChangeEvent event) {
		return ((PHPSourceViewerConfiguration) getSourceViewerConfiguration())
				.affectsTextPresentation(event)
				|| super.affectsTextPresentation(event);
	}

	//
	// protected boolean affectsTextPresentation(PropertyChangeEvent event) {
	// JavaTextTools textTools =
	// PHPeclipsePlugin.getDefault().getJavaTextTools();
	// return textTools.affectsBehavior(event);
	// }
	/**
	 * Creates and returns the preference store for this Java editor with the
	 * given input.
	 * 
	 * @param input
	 *            The editor input for which to create the preference store
	 * @return the preference store for this editor
	 * 
	 * @since 3.0
	 */
	private IPreferenceStore createCombinedPreferenceStore(IEditorInput input) {
		List stores = new ArrayList(3);

		IJavaProject project = EditorUtility.getJavaProject(input);
		if (project != null)
			stores.add(new OptionsAdapter(project.getOptions(false),
					PHPeclipsePlugin.getDefault().getMockupPreferenceStore(),
					new OptionsAdapter.IPropertyChangeEventFilter() {

						public boolean isFiltered(PropertyChangeEvent event) {
							IJavaElement inputJavaElement = getInputJavaElement();
							IJavaProject javaProject = inputJavaElement != null ? inputJavaElement
									.getJavaProject()
									: null;
							if (javaProject == null)
								return true;

							return !javaProject.getProject().equals(
									event.getSource());
						}

					}));

		stores.add(PHPeclipsePlugin.getDefault().getPreferenceStore());
		stores.add(new PreferencesAdapter(JavaCore.getPlugin()
				.getPluginPreferences()));
		stores.add(EditorsUI.getPreferenceStore());

		return new ChainedPreferenceStore((IPreferenceStore[]) stores
				.toArray(new IPreferenceStore[stores.size()]));
	}

	/**
	 * Jumps to the error next according to the given direction.
	 */
	public void gotoError(boolean forward) {

		ISelectionProvider provider = getSelectionProvider();

		ITextSelection s = (ITextSelection) provider.getSelection();
		Position errorPosition = new Position(0, 0);
		IJavaAnnotation nextError = getNextError(s.getOffset(), forward,
				errorPosition);

		if (nextError != null) {

			IMarker marker = null;
			if (nextError instanceof MarkerAnnotation)
				marker = ((MarkerAnnotation) nextError).getMarker();
			else {
				Iterator e = nextError.getOverlaidIterator();
				if (e != null) {
					while (e.hasNext()) {
						Object o = e.next();
						if (o instanceof MarkerAnnotation) {
							marker = ((MarkerAnnotation) o).getMarker();
							break;
						}
					}
				}
			}

			if (marker != null) {
				IWorkbenchPage page = getSite().getPage();
				IViewPart view = view = page
						.findView("org.eclipse.ui.views.TaskList"); //$NON-NLS-1$
				if (view instanceof TaskList) {
					StructuredSelection ss = new StructuredSelection(marker);
					((TaskList) view).setSelection(ss, true);
				}
			}

			selectAndReveal(errorPosition.getOffset(), errorPosition
					.getLength());
			// setStatusLineErrorMessage(nextError.getMessage());

		} else {

			setStatusLineErrorMessage(null);

		}
	}

	private IJavaAnnotation getNextError(int offset, boolean forward,
			Position errorPosition) {

		IJavaAnnotation nextError = null;
		Position nextErrorPosition = null;

		IDocument document = getDocumentProvider()
				.getDocument(getEditorInput());
		int endOfDocument = document.getLength();
		int distance = 0;

		IAnnotationModel model = getDocumentProvider().getAnnotationModel(
				getEditorInput());
		Iterator e = new JavaAnnotationIterator(model, false);
		while (e.hasNext()) {

			IJavaAnnotation a = (IJavaAnnotation) e.next();
			if (a.hasOverlay() || !a.isProblem())
				continue;

			Position p = model.getPosition((Annotation) a);
			if (!p.includes(offset)) {

				int currentDistance = 0;

				if (forward) {
					currentDistance = p.getOffset() - offset;
					if (currentDistance < 0)
						currentDistance = endOfDocument - offset
								+ p.getOffset();
				} else {
					currentDistance = offset - p.getOffset();
					if (currentDistance < 0)
						currentDistance = offset + endOfDocument
								- p.getOffset();
				}

				if (nextError == null || currentDistance < distance) {
					distance = currentDistance;
					nextError = a;
					nextErrorPosition = p;
				}
			}
		}

		if (nextErrorPosition != null) {
			errorPosition.setOffset(nextErrorPosition.getOffset());
			errorPosition.setLength(nextErrorPosition.getLength());
		}

		return nextError;
	}

	protected void uninstallOverrideIndicator() {
		// if (fOverrideIndicatorManager != null) {
		// fOverrideIndicatorManager.removeAnnotations();
		// fOverrideIndicatorManager= null;
		// }
	}

	protected void installOverrideIndicator(boolean waitForReconcilation) {
		uninstallOverrideIndicator();
		IAnnotationModel model = getDocumentProvider().getAnnotationModel(
				getEditorInput());
		final IJavaElement inputElement = getInputJavaElement();

		if (model == null || inputElement == null)
			return;

		// fOverrideIndicatorManager= new OverrideIndicatorManager(model,
		// inputElement, null);
		//
		// if (provideAST) {
		// Job job= new
		// Job(JavaEditorMessages.getString("OverrideIndicatorManager.intallJob"))
		// {
		// //$NON-NLS-1$
		// /*
		// * @see
		// org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		// * @since 3.0
		// */
		// protected IStatus run(IProgressMonitor monitor) {
		// CompilationUnit ast=
		// JavaPlugin.getDefault().getASTProvider().getAST(inputElement, true,
		// null);
		// if (fOverrideIndicatorManager != null) // editor might have been
		// closed
		// in the meanwhile
		// fOverrideIndicatorManager.reconciled(ast, true, monitor);
		// return Status.OK_STATUS;
		// }
		// };
		// job.setPriority(Job.DECORATE);
		// job.setSystem(true);
		// job.schedule();
		// }
	}

	/**
	 * Tells whether override indicators are shown.
	 * 
	 * @return <code>true</code> if the override indicators are shown
	 * @since 3.0
	 */
	// protected boolean isShowingOverrideIndicators() {
	// AnnotationPreference preference=
	// getAnnotationPreferenceLookup().getAnnotationPreference(OverrideIndicatorManager.ANNOTATION_TYPE);
	// IPreferenceStore store= getPreferenceStore();
	// return getBoolean(store, preference.getHighlightPreferenceKey())
	// || getBoolean(store, preference.getVerticalRulerPreferenceKey())
	// || getBoolean(store, preference.getOverviewRulerPreferenceKey())
	// || getBoolean(store, preference.getTextPreferenceKey());
	// }
	/**
	 * Returns the boolean preference for the given key.
	 * 
	 * @param store
	 *            the preference store
	 * @param key
	 *            the preference key
	 * @return <code>true</code> if the key exists in the store and its value
	 *         is <code>true</code>
	 * @since 3.0
	 */
	private boolean getBoolean(IPreferenceStore store, String key) {
		return key != null && store.getBoolean(key);
	}

	protected boolean isPrefQuickDiffAlwaysOn() {
		return false; // never show change ruler for the non-editable java
						// editor.
		// Overridden in subclasses like PHPUnitEditor
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#createNavigationActions()
	 */
	protected void createNavigationActions() {
		super.createNavigationActions();

		final StyledText textWidget = getSourceViewer().getTextWidget();

		IAction action = new SmartLineStartAction(textWidget, false);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.LINE_START);
		setAction(ITextEditorActionDefinitionIds.LINE_START, action);

		action = new SmartLineStartAction(textWidget, true);
		action
				.setActionDefinitionId(ITextEditorActionDefinitionIds.SELECT_LINE_START);
		setAction(ITextEditorActionDefinitionIds.SELECT_LINE_START, action);

		action = new NavigatePreviousSubWordAction();
		action
				.setActionDefinitionId(ITextEditorActionDefinitionIds.WORD_PREVIOUS);
		setAction(ITextEditorActionDefinitionIds.WORD_PREVIOUS, action);
		textWidget.setKeyBinding(SWT.CTRL | SWT.ARROW_LEFT, SWT.NULL);

		action = new NavigateNextSubWordAction();
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.WORD_NEXT);
		setAction(ITextEditorActionDefinitionIds.WORD_NEXT, action);
		textWidget.setKeyBinding(SWT.CTRL | SWT.ARROW_RIGHT, SWT.NULL);

		action = new SelectPreviousSubWordAction();
		action
				.setActionDefinitionId(ITextEditorActionDefinitionIds.SELECT_WORD_PREVIOUS);
		setAction(ITextEditorActionDefinitionIds.SELECT_WORD_PREVIOUS, action);
		textWidget.setKeyBinding(SWT.CTRL | SWT.SHIFT | SWT.ARROW_LEFT,
				SWT.NULL);

		action = new SelectNextSubWordAction();
		action
				.setActionDefinitionId(ITextEditorActionDefinitionIds.SELECT_WORD_NEXT);
		setAction(ITextEditorActionDefinitionIds.SELECT_WORD_NEXT, action);
		textWidget.setKeyBinding(SWT.CTRL | SWT.SHIFT | SWT.ARROW_RIGHT,
				SWT.NULL);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createCompositeRuler()
	 */
	// protected CompositeRuler createCompositeRuler() {
	// if
	// (!getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_ANNOTATION_ROLL_OVER))
	// return super.createCompositeRuler();
	//
	// CompositeRuler ruler = new CompositeRuler();
	// AnnotationRulerColumn column = new
	// AnnotationRulerColumn(VERTICAL_RULER_WIDTH, getAnnotationAccess());
	// column.setHover(new JavaExpandHover(ruler, getAnnotationAccess(), new
	// IDoubleClickListener() {
	//
	// public void doubleClick(DoubleClickEvent event) {
	// // for now: just invoke ruler double click action
	// triggerAction(ITextEditorActionConstants.RULER_DOUBLE_CLICK);
	// }
	//
	// private void triggerAction(String actionID) {
	// IAction action = getAction(actionID);
	// if (action != null) {
	// if (action instanceof IUpdate)
	// ((IUpdate) action).update();
	// // hack to propagate line change
	// if (action instanceof ISelectionListener) {
	// ((ISelectionListener) action).selectionChanged(null, null);
	// }
	// if (action.isEnabled())
	// action.run();
	// }
	// }
	//
	// }));
	// ruler.addDecorator(0, column);
	//
	// if (isLineNumberRulerVisible())
	// ruler.addDecorator(1, createLineNumberRulerColumn());
	// else if (isPrefQuickDiffAlwaysOn())
	// ruler.addDecorator(1, createChangeRulerColumn());
	//
	// return ruler;
	// }
	/*
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createAnnotationRulerColumn(org.eclipse.jface.text.source.CompositeRuler)
	 * @since 3.2
	 */
	protected IVerticalRulerColumn createAnnotationRulerColumn(
			CompositeRuler ruler) {
		if (!getPreferenceStore().getBoolean(
				PreferenceConstants.EDITOR_ANNOTATION_ROLL_OVER))
			return super.createAnnotationRulerColumn(ruler);

		AnnotationRulerColumn column = new AnnotationRulerColumn(
				VERTICAL_RULER_WIDTH, getAnnotationAccess());
		column.setHover(new JavaExpandHover(ruler, getAnnotationAccess(),
				new IDoubleClickListener() {

					public void doubleClick(DoubleClickEvent event) {
						// for now: just invoke ruler double click action
						triggerAction(ITextEditorActionConstants.RULER_DOUBLE_CLICK);
					}

					private void triggerAction(String actionID) {
						IAction action = getAction(actionID);
						if (action != null) {
							if (action instanceof IUpdate)
								((IUpdate) action).update();
							// hack to propagate line change
							if (action instanceof ISelectionListener) {
								((ISelectionListener) action).selectionChanged(
										null, null);
							}
							if (action.isEnabled())
								action.run();
						}
					}

				}));

		return column;
	}

	/**
	 * Returns the folding action group, or <code>null</code> if there is
	 * none.
	 * 
	 * @return the folding action group, or <code>null</code> if there is none
	 * @since 3.0
	 */
	protected FoldingActionGroup getFoldingActionGroup() {
		return fFoldingGroup;
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#performRevert()
	 */
	protected void performRevert() {
		ProjectionViewer projectionViewer = (ProjectionViewer) getSourceViewer();
		projectionViewer.setRedraw(false);
		try {

			boolean projectionMode = projectionViewer.isProjectionMode();
			if (projectionMode) {
				projectionViewer.disableProjection();
				if (fProjectionModelUpdater != null)
					fProjectionModelUpdater.uninstall();
			}

			super.performRevert();

			if (projectionMode) {
				if (fProjectionModelUpdater != null)
					fProjectionModelUpdater.install(this, projectionViewer);
				projectionViewer.enableProjection();
			}

		} finally {
			projectionViewer.setRedraw(true);
		}
	}

	/**
	 * React to changed selection.
	 * 
	 * @since 3.0
	 */
	protected void selectionChanged() {
		if (getSelectionProvider() == null)
			return;
		ISourceReference element = computeHighlightRangeSourceReference();
		if (getPreferenceStore().getBoolean(
				PreferenceConstants.EDITOR_SYNC_OUTLINE_ON_CURSOR_MOVE))
			synchronizeOutlinePage(element);
		setSelection(element, false);
		updateStatusLine();
	}

	private boolean isJavaOutlinePageActive() {
		IWorkbenchPart part = getActivePart();
		return part instanceof ContentOutline
				&& ((ContentOutline) part).getCurrentPage() == fOutlinePage;
	}

	private IWorkbenchPart getActivePart() {
		IWorkbenchWindow window = getSite().getWorkbenchWindow();
		IPartService service = window.getPartService();
		IWorkbenchPart part = service.getActivePart();
		return part;
	}

	/**
	 * Computes and returns the source reference that includes the caret and
	 * serves as provider for the outline page selection and the editor range
	 * indication.
	 * 
	 * @return the computed source reference
	 * @since 3.0
	 */
	protected ISourceReference computeHighlightRangeSourceReference() {
		ISourceViewer sourceViewer = getSourceViewer();
		if (sourceViewer == null)
			return null;

		StyledText styledText = sourceViewer.getTextWidget();
		if (styledText == null)
			return null;

		int caret = 0;
		if (sourceViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension = (ITextViewerExtension5) sourceViewer;
			caret = extension.widgetOffset2ModelOffset(styledText
					.getCaretOffset());
		} else {
			int offset = sourceViewer.getVisibleRegion().getOffset();
			caret = offset + styledText.getCaretOffset();
		}

		IJavaElement element = getElementAt(caret, false);

		if (!(element instanceof ISourceReference))
			return null;

		if (element.getElementType() == IJavaElement.IMPORT_DECLARATION) {

			IImportDeclaration declaration = (IImportDeclaration) element;
			IImportContainer container = (IImportContainer) declaration
					.getParent();
			ISourceRange srcRange = null;

			try {
				srcRange = container.getSourceRange();
			} catch (JavaModelException e) {
			}

			if (srcRange != null && srcRange.getOffset() == caret)
				return container;
		}

		return (ISourceReference) element;
	}

	/**
	 * Returns the most narrow java element including the given offset.
	 * 
	 * @param offset
	 *            the offset inside of the requested element
	 * @param reconcile
	 *            <code>true</code> if editor input should be reconciled in
	 *            advance
	 * @return the most narrow java element
	 * @since 3.0
	 */
	protected IJavaElement getElementAt(int offset, boolean reconcile) {
		return getElementAt(offset);
	}

	public ShowInContext getShowInContext() {
		IFile file = null;
		if(getEditorInput() instanceof FileStoreEditorInput){
			FileStoreEditorInput fei = (FileStoreEditorInput) getEditorInput();
			file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(fei.getURI().getPath()));
		} else if (getEditorInput() instanceof FileEditorInput) {
			FileEditorInput fei = (FileEditorInput) getEditorInput();
			// added to fix ticket 637
			file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(fei.getURI().getPath()));
		} else if (getEditorInput() instanceof ExternalEditorInput) {
			ExternalEditorInput fei = (ExternalEditorInput) getEditorInput();
			file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(fei.getFullPath()/* .getURI().getPath()*/));
		}

		ShowInContext context = BrowserUtil.getShowInContext(file,
				false, "");
		if (context != null) {
			return context;
		}
		return new ShowInContext(file, null);
	}

	public String[] getShowInTargetIds() {
		return new String[] { BrowserView.ID_BROWSER };
	}

	/**
	 * Updates the occurrences annotations based on the current selection.
	 * 
	 * @param selection
	 *            the text selection
	 * @param astRoot
	 *            the compilation unit AST
	 * @since 3.0
	 */
	protected void updateOccurrenceAnnotations(ITextSelection selection) {// ,
		// CompilationUnit
		// astRoot)
		// {

		if (fOccurrencesFinderJob != null)
			fOccurrencesFinderJob.cancel();

		if (!fMarkOccurrenceAnnotations)
			return;

		// if (astRoot == null || selection == null)
		if (selection == null)
			return;

		IDocument document = getSourceViewer().getDocument();
		if (document == null)
			return;

		fMarkOccurrenceTargetRegion = null;
		if (document instanceof IDocumentExtension4) {
			int offset = selection.getOffset();
			long currentModificationStamp = ((IDocumentExtension4) document)
					.getModificationStamp();
			if (fMarkOccurrenceTargetRegion != null
					&& currentModificationStamp == fMarkOccurrenceModificationStamp) {
				if (fMarkOccurrenceTargetRegion.getOffset() <= offset
						&& offset <= fMarkOccurrenceTargetRegion.getOffset()
								+ fMarkOccurrenceTargetRegion.getLength())
					return;
			}
			fMarkOccurrenceTargetRegion = JavaWordFinder.findWord(document,
					offset);
			fMarkOccurrenceModificationStamp = currentModificationStamp;
		}

		if (fMarkOccurrenceTargetRegion == null
				|| fMarkOccurrenceTargetRegion.getLength() == 0) {
			return;
		}

		List matches = null;

		if (matches == null) {
			try {
				matches = new ArrayList();

				Scanner fScanner = new Scanner();
				fScanner.setSource(document.get().toCharArray());
				fScanner.setPHPMode(false);
				String wordStr;
				char[] word;

				wordStr = document.get(fMarkOccurrenceTargetRegion.getOffset(),
						fMarkOccurrenceTargetRegion.getLength());
				if (wordStr != null) {
					word = wordStr.toCharArray();
					int fToken = ITerminalSymbols.TokenNameEOF;
					try {
						fToken = fScanner.getNextToken();
						while (fToken != ITerminalSymbols.TokenNameEOF) { // &&
																			// fToken
																			// !=
							// TokenNameERROR) {
							if (fToken == ITerminalSymbols.TokenNameVariable
									|| fToken == ITerminalSymbols.TokenNameIdentifier) {
								// global variable
								if (fScanner.equalsCurrentTokenSource(word)) {
									matches
											.add(new Region(
													fScanner
															.getCurrentTokenStartPosition(),
													fScanner
															.getCurrentTokenEndPosition()
															- fScanner
																	.getCurrentTokenStartPosition()
															+ 1));
								}
							}
							fToken = fScanner.getNextToken();
						}
					} catch (InvalidInputException e) {
						// ignore errors
					} catch (SyntaxError e) {
						// ignore errors
					}
				}
			} catch (BadLocationException e1) {
				// ignore errors
			} catch (Exception e) {
				e.printStackTrace();
				// ignore errors
			}

		}

		if (matches == null || matches.size() == 0) {
			if (!fStickyOccurrenceAnnotations)
				removeOccurrenceAnnotations();
			return;
		}

		Position[] positions = new Position[matches.size()];
		int i = 0;
		for (Iterator each = matches.iterator(); each.hasNext();) {
			IRegion currentNode = (IRegion) each.next();
			positions[i++] = new Position(currentNode.getOffset(), currentNode
					.getLength());
		}

		fOccurrencesFinderJob = new OccurrencesFinderJob(document, positions,
				selection);
		// fOccurrencesFinderJob.setPriority(Job.DECORATE);
		// fOccurrencesFinderJob.setSystem(true);
		// fOccurrencesFinderJob.schedule();
		fOccurrencesFinderJob.run(new NullProgressMonitor());
	}

	protected void installOccurrencesFinder() {
		fMarkOccurrenceAnnotations = true;

		fPostSelectionListenerWithAST = new ISelectionListenerWithAST() {
			public void selectionChanged(IEditorPart part,
					ITextSelection selection) { // ,
				// CompilationUnit
				// astRoot)
				// {
				updateOccurrenceAnnotations(selection);// , astRoot);
			}
		};
		SelectionListenerWithASTManager.getDefault().addListener(this,
				fPostSelectionListenerWithAST);
		if (getSelectionProvider() != null) {
			fForcedMarkOccurrencesSelection = getSelectionProvider()
					.getSelection();
			SelectionListenerWithASTManager.getDefault().forceSelectionChange(
					this, (ITextSelection) fForcedMarkOccurrencesSelection);
		}

		if (fOccurrencesFinderJobCanceler == null) {
			fOccurrencesFinderJobCanceler = new OccurrencesFinderJobCanceler();
			fOccurrencesFinderJobCanceler.install();
		}
	}

	protected void uninstallOccurrencesFinder() {
		fMarkOccurrenceAnnotations = false;

		if (fOccurrencesFinderJob != null) {
			fOccurrencesFinderJob.cancel();
			fOccurrencesFinderJob = null;
		}

		if (fOccurrencesFinderJobCanceler != null) {
			fOccurrencesFinderJobCanceler.uninstall();
			fOccurrencesFinderJobCanceler = null;
		}

		if (fPostSelectionListenerWithAST != null) {
			SelectionListenerWithASTManager.getDefault().removeListener(this,
					fPostSelectionListenerWithAST);
			fPostSelectionListenerWithAST = null;
		}

		removeOccurrenceAnnotations();
	}

	protected boolean isMarkingOccurrences() {
		return fMarkOccurrenceAnnotations;
	}

	void removeOccurrenceAnnotations() {
		fMarkOccurrenceModificationStamp = IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;
		fMarkOccurrenceTargetRegion = null;

		IDocumentProvider documentProvider = getDocumentProvider();
		if (documentProvider == null)
			return;

		IAnnotationModel annotationModel = documentProvider
				.getAnnotationModel(getEditorInput());
		if (annotationModel == null || fOccurrenceAnnotations == null)
			return;

		synchronized (getLockObject(annotationModel)) {
			if (annotationModel instanceof IAnnotationModelExtension) {
				((IAnnotationModelExtension) annotationModel)
						.replaceAnnotations(fOccurrenceAnnotations, null);
			} else {
				for (int i = 0, length = fOccurrenceAnnotations.length; i < length; i++)
					annotationModel.removeAnnotation(fOccurrenceAnnotations[i]);
			}
			fOccurrenceAnnotations = null;
		}
	}
}