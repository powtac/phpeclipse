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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.phpdt.core.ICompilationUnit;
import net.sourceforge.phpdt.core.IProblemRequestor;
import net.sourceforge.phpdt.core.JavaCore;
import net.sourceforge.phpdt.core.JavaModelException;
import net.sourceforge.phpdt.core.compiler.IProblem;
import net.sourceforge.phpdt.internal.ui.text.IPHPPartitions;
import net.sourceforge.phpdt.internal.ui.text.java.IProblemRequestorExtension;
import net.sourceforge.phpdt.internal.ui.text.spelling.SpellReconcileStrategy.SpellProblem;
import net.sourceforge.phpdt.ui.PreferenceConstants;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ILineTracker;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModelEvent;
import org.eclipse.jface.text.source.IAnnotationAccessExtension;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.IAnnotationModelListenerExtension;
import org.eclipse.jface.text.source.IAnnotationPresentation;
import org.eclipse.jface.text.source.ImageUtilities;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.ForwardingDocumentProvider;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.AnnotationPreferenceLookup;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;

/**
 * The PHPDocumentProvider provides the IDocuments used by java editors.
 */

public class PHPDocumentProvider extends TextFileDocumentProvider implements
		ICompilationUnitDocumentProvider {
	/**
	 * Here for visibility issues only.
	 */

	/**
	 * Bundle of all required informations to allow working copy management.
	 */
	/**
	 * Bundle of all required informations to allow working copy management.
	 */
	static protected class CompilationUnitInfo extends FileInfo {
		public ICompilationUnit fCopy;
	}

	/**
	 * Annotation model dealing with java marker annotations and temporary
	 * problems. Also acts as problem requestor for its compilation unit.
	 * Initialiy inactive. Must explicitly be activated.
	 */
	protected static class CompilationUnitAnnotationModel extends
			ResourceMarkerAnnotationModel implements IProblemRequestor,
			IProblemRequestorExtension {

		private static class ProblemRequestorState {
			boolean fInsideReportingSequence = false;

			List fReportedProblems;
		}

		private ThreadLocal fProblemRequestorState = new ThreadLocal();

		private int fStateCount = 0;

		private ICompilationUnit fCompilationUnit;

		private List fGeneratedAnnotations;

		private IProgressMonitor fProgressMonitor;

		private boolean fIsActive = false;

		private ReverseMap fReverseMap = new ReverseMap();

		private List fPreviouslyOverlaid = null;

		private List fCurrentlyOverlaid = new ArrayList();

		public CompilationUnitAnnotationModel(IResource resource) {
			super(resource);
		}

		public void setCompilationUnit(ICompilationUnit unit) {
			fCompilationUnit = unit;
		}

		protected MarkerAnnotation createMarkerAnnotation(IMarker marker) {
			String markerType = MarkerUtilities.getMarkerType(marker);
			if (markerType != null
					&& markerType
							.startsWith(JavaMarkerAnnotation.JAVA_MARKER_TYPE_PREFIX))
				return new JavaMarkerAnnotation(marker);
			return super.createMarkerAnnotation(marker);
		}

		/*
		 * @see org.eclipse.jface.text.source.AnnotationModel#createAnnotationModelEvent()
		 */
		protected AnnotationModelEvent createAnnotationModelEvent() {
			return new CompilationUnitAnnotationModelEvent(this, getResource());
		}

		protected Position createPositionFromProblem(IProblem problem) {
			int start = problem.getSourceStart();
			if (start < 0)
				return null;

			int length = problem.getSourceEnd() - problem.getSourceStart() + 1;
			if (length < 0)
				return null;

			return new Position(start, length);
		}

		/*
		 * @see IProblemRequestor#beginReporting()
		 */
		public void beginReporting() {
			ProblemRequestorState state = (ProblemRequestorState) fProblemRequestorState
					.get();
			if (state == null)
				internalBeginReporting(false);
		}

		/*
		 * @see net.sourceforge.phpdt.internal.ui.text.java.IProblemRequestorExtension#beginReportingSequence()
		 */
		public void beginReportingSequence() {
			ProblemRequestorState state = (ProblemRequestorState) fProblemRequestorState
					.get();
			if (state == null)
				internalBeginReporting(true);
		}

		/**
		 * Sets up the infrastructure necessary for problem reporting.
		 * 
		 * @param insideReportingSequence
		 *            <code>true</code> if this method call is issued from
		 *            inside a reporting sequence
		 */
		private void internalBeginReporting(boolean insideReportingSequence) {
			if (fCompilationUnit != null) {
				// &&
				// fCompilationUnit.getJavaProject().isOnClasspath(fCompilationUnit))
				// {
				ProblemRequestorState state = new ProblemRequestorState();
				state.fInsideReportingSequence = insideReportingSequence;
				state.fReportedProblems = new ArrayList();
				synchronized (getLockObject()) {
					fProblemRequestorState.set(state);
					++fStateCount;
				}
			}
		}

		/*
		 * @see IProblemRequestor#acceptProblem(IProblem)
		 */
		public void acceptProblem(IProblem problem) {
			if (isActive()) {
				ProblemRequestorState state = (ProblemRequestorState) fProblemRequestorState
						.get();
				if (state != null)
					state.fReportedProblems.add(problem);
			}
		}

		/*
		 * @see IProblemRequestor#endReporting()
		 */
		public void endReporting() {
			ProblemRequestorState state = (ProblemRequestorState) fProblemRequestorState
					.get();
			if (state != null && !state.fInsideReportingSequence)
				internalEndReporting(state);
		}

		/*
		 * @see net.sourceforge.phpdt.internal.ui.text.java.IProblemRequestorExtension#endReportingSequence()
		 */
		public void endReportingSequence() {
			ProblemRequestorState state = (ProblemRequestorState) fProblemRequestorState
					.get();
			if (state != null && state.fInsideReportingSequence)
				internalEndReporting(state);
		}

		private void internalEndReporting(ProblemRequestorState state) {
			int stateCount = 0;
			synchronized (getLockObject()) {
				--fStateCount;
				stateCount = fStateCount;
				fProblemRequestorState.set(null);
			}

			if (stateCount == 0 && isActive())
				reportProblems(state.fReportedProblems);
		}

		/**
		 * Signals the end of problem reporting.
		 */
		private void reportProblems(List reportedProblems) {
			if (fProgressMonitor != null && fProgressMonitor.isCanceled())
				return;

			boolean temporaryProblemsChanged = false;

			synchronized (getLockObject()) {

				boolean isCanceled = false;

				fPreviouslyOverlaid = fCurrentlyOverlaid;
				fCurrentlyOverlaid = new ArrayList();

				if (fGeneratedAnnotations.size() > 0) {
					temporaryProblemsChanged = true;
					removeAnnotations(fGeneratedAnnotations, false, true);
					fGeneratedAnnotations.clear();
				}

				if (reportedProblems != null && reportedProblems.size() > 0) {

					Iterator e = reportedProblems.iterator();
					while (e.hasNext()) {

						if (fProgressMonitor != null
								&& fProgressMonitor.isCanceled()) {
							isCanceled = true;
							break;
						}

						IProblem problem = (IProblem) e.next();
						Position position = createPositionFromProblem(problem);
						if (position != null) {

							try {
								ProblemAnnotation annotation = new ProblemAnnotation(
										problem, fCompilationUnit);
								overlayMarkers(position, annotation);
								addAnnotation(annotation, position, false);
								fGeneratedAnnotations.add(annotation);

								temporaryProblemsChanged = true;
							} catch (BadLocationException x) {
								// ignore invalid position
							}
						}
					}
				}

				removeMarkerOverlays(isCanceled);
				fPreviouslyOverlaid = null;
			}

			if (temporaryProblemsChanged)
				fireModelChanged();
		}

		private void removeMarkerOverlays(boolean isCanceled) {
			if (isCanceled) {
				fCurrentlyOverlaid.addAll(fPreviouslyOverlaid);
			} else if (fPreviouslyOverlaid != null) {
				Iterator e = fPreviouslyOverlaid.iterator();
				while (e.hasNext()) {
					JavaMarkerAnnotation annotation = (JavaMarkerAnnotation) e
							.next();
					annotation.setOverlay(null);
				}
			}
		}

		/**
		 * Overlays value with problem annotation.
		 * 
		 * @param problemAnnotation
		 */
		private void setOverlay(Object value,
				ProblemAnnotation problemAnnotation) {
			if (value instanceof JavaMarkerAnnotation) {
				JavaMarkerAnnotation annotation = (JavaMarkerAnnotation) value;
				if (annotation.isProblem()) {
					annotation.setOverlay(problemAnnotation);
					fPreviouslyOverlaid.remove(annotation);
					fCurrentlyOverlaid.add(annotation);
				}
			} else {
			}
		}

		private void overlayMarkers(Position position,
				ProblemAnnotation problemAnnotation) {
			Object value = getAnnotations(position);
			if (value instanceof List) {
				List list = (List) value;
				for (Iterator e = list.iterator(); e.hasNext();)
					setOverlay(e.next(), problemAnnotation);
			} else {
				setOverlay(value, problemAnnotation);
			}
		}

		/**
		 * Tells this annotation model to collect temporary problems from now
		 * on.
		 */
		private void startCollectingProblems() {
			fGeneratedAnnotations = new ArrayList();
		}

		/**
		 * Tells this annotation model to no longer collect temporary problems.
		 */
		private void stopCollectingProblems() {
			if (fGeneratedAnnotations != null)
				removeAnnotations(fGeneratedAnnotations, true, true);
			fGeneratedAnnotations = null;
		}

		/*
		 * @see IProblemRequestor#isActive()
		 */
		public boolean isActive() {
			return fIsActive;
		}

		/*
		 * @see IProblemRequestorExtension#setProgressMonitor(IProgressMonitor)
		 */
		public void setProgressMonitor(IProgressMonitor monitor) {
			fProgressMonitor = monitor;
		}

		/*
		 * @see IProblemRequestorExtension#setIsActive(boolean)
		 */
		public void setIsActive(boolean isActive) {
			if (fIsActive != isActive) {
				fIsActive = isActive;
				if (fIsActive)
					startCollectingProblems();
				else
					stopCollectingProblems();
			}
		}

		private Object getAnnotations(Position position) {
			return fReverseMap.get(position);
		}

		/*
		 * @see AnnotationModel#addAnnotation(Annotation, Position, boolean)
		 */
		protected void addAnnotation(Annotation annotation, Position position,
				boolean fireModelChanged) throws BadLocationException {
			super.addAnnotation(annotation, position, fireModelChanged);

			Object cached = fReverseMap.get(position);
			if (cached == null)
				fReverseMap.put(position, annotation);
			else if (cached instanceof List) {
				List list = (List) cached;
				list.add(annotation);
			} else if (cached instanceof Annotation) {
				List list = new ArrayList(2);
				list.add(cached);
				list.add(annotation);
				fReverseMap.put(position, list);
			}
		}

		/*
		 * @see AnnotationModel#removeAllAnnotations(boolean)
		 */
		protected void removeAllAnnotations(boolean fireModelChanged) {
			super.removeAllAnnotations(fireModelChanged);
			fReverseMap.clear();
		}

		/*
		 * @see AnnotationModel#removeAnnotation(Annotation, boolean)
		 */
		protected void removeAnnotation(Annotation annotation,
				boolean fireModelChanged) {
			Position position = getPosition(annotation);
			Object cached = fReverseMap.get(position);
			if (cached instanceof List) {
				List list = (List) cached;
				list.remove(annotation);
				if (list.size() == 1) {
					fReverseMap.put(position, list.get(0));
					list.clear();
				}
			} else if (cached instanceof Annotation) {
				fReverseMap.remove(position);
			}
			super.removeAnnotation(annotation, fireModelChanged);
		}
	}

	protected static class GlobalAnnotationModelListener implements
			IAnnotationModelListener, IAnnotationModelListenerExtension {

		private ListenerList fListenerList;

		public GlobalAnnotationModelListener() {
			fListenerList = new ListenerList();
		}

		public void addListener(IAnnotationModelListener listener) {
			fListenerList.add(listener);
		}

		/**
		 * @see IAnnotationModelListenerExtension#modelChanged(AnnotationModelEvent)
		 */
		public void modelChanged(AnnotationModelEvent event) {
			Object[] listeners = fListenerList.getListeners();
			for (int i = 0; i < listeners.length; i++) {
				Object curr = listeners[i];
				if (curr instanceof IAnnotationModelListenerExtension) {
					((IAnnotationModelListenerExtension) curr)
							.modelChanged(event);
				}
			}
		}

		/**
		 * @see IAnnotationModelListener#modelChanged(IAnnotationModel)
		 */
		public void modelChanged(IAnnotationModel model) {
			Object[] listeners = fListenerList.getListeners();
			for (int i = 0; i < listeners.length; i++) {
				((IAnnotationModelListener) listeners[i]).modelChanged(model);
			}
		}

		public void removeListener(IAnnotationModelListener listener) {
			fListenerList.remove(listener);
		}
	}

	/**
	 * Annotation representating an <code>IProblem</code>.
	 */
	static public class ProblemAnnotation extends Annotation implements
			IJavaAnnotation, IAnnotationPresentation {

		private static final String SPELLING_ANNOTATION_TYPE = "org.eclipse.ui.workbench.texteditor.spelling";

		// XXX: To be fully correct these constants should be non-static
		/**
		 * The layer in which task problem annotations are located.
		 */
		private static final int TASK_LAYER;

		/**
		 * The layer in which info problem annotations are located.
		 */
		private static final int INFO_LAYER;

		/**
		 * The layer in which warning problem annotations representing are
		 * located.
		 */
		private static final int WARNING_LAYER;

		/**
		 * The layer in which error problem annotations representing are
		 * located.
		 */
		private static final int ERROR_LAYER;

		static {
			AnnotationPreferenceLookup lookup = EditorsUI
					.getAnnotationPreferenceLookup();
			TASK_LAYER = computeLayer(
					"org.eclipse.ui.workbench.texteditor.task", lookup); //$NON-NLS-1$
			INFO_LAYER = computeLayer("net.sourceforge.phpdt.ui.info", lookup); //$NON-NLS-1$
			WARNING_LAYER = computeLayer(
					"net.sourceforge.phpdt.ui.warning", lookup); //$NON-NLS-1$
			ERROR_LAYER = computeLayer("net.sourceforge.phpdt.ui.error", lookup); //$NON-NLS-1$
		}

		private static int computeLayer(String annotationType,
				AnnotationPreferenceLookup lookup) {
			Annotation annotation = new Annotation(annotationType, false, null);
			AnnotationPreference preference = lookup
					.getAnnotationPreference(annotation);
			if (preference != null)
				return preference.getPresentationLayer() + 1;
			else
				return IAnnotationAccessExtension.DEFAULT_LAYER + 1;
		}

		// private static Image fgQuickFixImage;
		// private static Image fgQuickFixErrorImage;
		// private static boolean fgQuickFixImagesInitialized= false;

		private ICompilationUnit fCompilationUnit;

		private List fOverlaids;

		private IProblem fProblem;

		private Image fImage;

		private boolean fQuickFixImagesInitialized = false;

		private int fLayer = IAnnotationAccessExtension.DEFAULT_LAYER;

		public ProblemAnnotation(IProblem problem, ICompilationUnit cu) {

			fProblem = problem;
			fCompilationUnit = cu;

			if (SpellProblem.Spelling == fProblem.getID()) {
				setType(SPELLING_ANNOTATION_TYPE);
				fLayer = WARNING_LAYER;
			} else if (IProblem.Task == fProblem.getID()) {
				setType(JavaMarkerAnnotation.TASK_ANNOTATION_TYPE);
				fLayer = TASK_LAYER;
			} else if (fProblem.isWarning()) {
				setType(JavaMarkerAnnotation.WARNING_ANNOTATION_TYPE);
				fLayer = WARNING_LAYER;
			} else if (fProblem.isError()) {
				setType(JavaMarkerAnnotation.ERROR_ANNOTATION_TYPE);
				fLayer = ERROR_LAYER;
			} else {
				setType(JavaMarkerAnnotation.INFO_ANNOTATION_TYPE);
				fLayer = INFO_LAYER;
			}
		}

		/*
		 * @see org.eclipse.jface.text.source.IAnnotationPresentation#getLayer()
		 */
		public int getLayer() {
			return fLayer;
		}

		private void initializeImages() {
			// http://bugs.eclipse.org/bugs/show_bug.cgi?id=18936
			// if (!fQuickFixImagesInitialized) {
			// if (isProblem() && indicateQuixFixableProblems() &&
			// JavaCorrectionProcessor.hasCorrections(this)) { // no light bulb
			// for tasks
			// if (!fgQuickFixImagesInitialized) {
			// fgQuickFixImage=
			// JavaPluginImages.get(JavaPluginImages.IMG_OBJS_FIXABLE_PROBLEM);
			// fgQuickFixErrorImage=
			// JavaPluginImages.get(JavaPluginImages.IMG_OBJS_FIXABLE_ERROR);
			// fgQuickFixImagesInitialized= true;
			// }
			// if (JavaMarkerAnnotation.ERROR_ANNOTATION_TYPE.equals(getType()))
			// fImage= fgQuickFixErrorImage;
			// else
			// fImage= fgQuickFixImage;
			// }
			// fQuickFixImagesInitialized= true;
			// }
		}

		private boolean indicateQuixFixableProblems() {
			return PreferenceConstants.getPreferenceStore().getBoolean(
					PreferenceConstants.EDITOR_CORRECTION_INDICATION);
		}

		/*
		 * @see Annotation#paint
		 */
		public void paint(GC gc, Canvas canvas, Rectangle r) {
			initializeImages();
			if (fImage != null)
				ImageUtilities.drawImage(fImage, gc, canvas, r, SWT.CENTER,
						SWT.TOP);
		}

		/*
		 * @see IJavaAnnotation#getImage(Display)
		 */
		public Image getImage(Display display) {
			initializeImages();
			return fImage;
		}

		/*
		 * @see IJavaAnnotation#getMessage()
		 */
		public String getText() {
			return fProblem.getMessage();
		}

		/*
		 * @see IJavaAnnotation#getArguments()
		 */
		public String[] getArguments() {
			return isProblem() ? fProblem.getArguments() : null;
		}

		/*
		 * @see IJavaAnnotation#getId()
		 */
		public int getId() {
			return fProblem.getID();
		}

		/*
		 * @see IJavaAnnotation#isProblem()
		 */
		public boolean isProblem() {
			String type = getType();
			return JavaMarkerAnnotation.WARNING_ANNOTATION_TYPE.equals(type)
					|| JavaMarkerAnnotation.ERROR_ANNOTATION_TYPE.equals(type)
					|| SPELLING_ANNOTATION_TYPE.equals(type);
		}

		/*
		 * @see IJavaAnnotation#hasOverlay()
		 */
		public boolean hasOverlay() {
			return false;
		}

		/*
		 * @see net.sourceforge.phpdt.internal.ui.javaeditor.IJavaAnnotation#getOverlay()
		 */
		public IJavaAnnotation getOverlay() {
			return null;
		}

		/*
		 * @see IJavaAnnotation#addOverlaid(IJavaAnnotation)
		 */
		public void addOverlaid(IJavaAnnotation annotation) {
			if (fOverlaids == null)
				fOverlaids = new ArrayList(1);
			fOverlaids.add(annotation);
		}

		/*
		 * @see IJavaAnnotation#removeOverlaid(IJavaAnnotation)
		 */
		public void removeOverlaid(IJavaAnnotation annotation) {
			if (fOverlaids != null) {
				fOverlaids.remove(annotation);
				if (fOverlaids.size() == 0)
					fOverlaids = null;
			}
		}

		/*
		 * @see IJavaAnnotation#getOverlaidIterator()
		 */
		public Iterator getOverlaidIterator() {
			if (fOverlaids != null)
				return fOverlaids.iterator();
			return null;
		}

		/*
		 * @see net.sourceforge.phpdt.internal.ui.javaeditor.IJavaAnnotation#getCompilationUnit()
		 */
		public ICompilationUnit getCompilationUnit() {
			return fCompilationUnit;
		}
	}

	/**
	 * Internal structure for mapping positions to some value. The reason for
	 * this specific structure is that positions can change over time. Thus a
	 * lookup is based on value and not on hash value.
	 */
	protected static class ReverseMap {

		static class Entry {
			Position fPosition;

			Object fValue;
		}

		private int fAnchor = 0;

		private List fList = new ArrayList(2);

		public ReverseMap() {
		}

		public void clear() {
			fList.clear();
		}

		public Object get(Position position) {

			Entry entry;

			// behind anchor
			int length = fList.size();
			for (int i = fAnchor; i < length; i++) {
				entry = (Entry) fList.get(i);
				if (entry.fPosition.equals(position)) {
					fAnchor = i;
					return entry.fValue;
				}
			}

			// before anchor
			for (int i = 0; i < fAnchor; i++) {
				entry = (Entry) fList.get(i);
				if (entry.fPosition.equals(position)) {
					fAnchor = i;
					return entry.fValue;
				}
			}

			return null;
		}

		private int getIndex(Position position) {
			Entry entry;
			int length = fList.size();
			for (int i = 0; i < length; i++) {
				entry = (Entry) fList.get(i);
				if (entry.fPosition.equals(position))
					return i;
			}
			return -1;
		}

		public void put(Position position, Object value) {
			int index = getIndex(position);
			if (index == -1) {
				Entry entry = new Entry();
				entry.fPosition = position;
				entry.fValue = value;
				fList.add(entry);
			} else {
				Entry entry = (Entry) fList.get(index);
				entry.fValue = value;
			}
		}

		public void remove(Position position) {
			int index = getIndex(position);
			if (index > -1)
				fList.remove(index);
		}
	}

	/**
	 * Document that can also be used by a background reconciler.
	 */
	protected static class PartiallySynchronizedDocument extends Document {

		/*
		 * @see IDocumentExtension#startSequentialRewrite(boolean)
		 */
		synchronized public void startSequentialRewrite(boolean normalized) {
			super.startSequentialRewrite(normalized);
		}

		/*
		 * @see IDocumentExtension#stopSequentialRewrite()
		 */
		synchronized public void stopSequentialRewrite() {
			super.stopSequentialRewrite();
		}

		/*
		 * @see IDocument#get()
		 */
		synchronized public String get() {
			return super.get();
		}

		/*
		 * @see IDocument#get(int, int)
		 */
		synchronized public String get(int offset, int length)
				throws BadLocationException {
			return super.get(offset, length);
		}

		/*
		 * @see IDocument#getChar(int)
		 */
		synchronized public char getChar(int offset)
				throws BadLocationException {
			return super.getChar(offset);
		}

		/*
		 * @see IDocument#replace(int, int, String)
		 */
		synchronized public void replace(int offset, int length, String text)
				throws BadLocationException {
			super.replace(offset, length, text);
		}

		/*
		 * @see IDocument#set(String)
		 */
		synchronized public void set(String text) {
			super.set(text);
		}
	};

	//
	// private static PHPPartitionScanner HTML_PARTITION_SCANNER = null;
	//
	// private static PHPPartitionScanner PHP_PARTITION_SCANNER = null;
	// private static PHPPartitionScanner SMARTY_PARTITION_SCANNER = null;
	//
	// // private final static String[] TYPES= new String[] {
	// PHPPartitionScanner.PHP, PHPPartitionScanner.JAVA_DOC,
	// PHPPartitionScanner.JAVA_MULTILINE_COMMENT };
	// private final static String[] TYPES =
	// new String[] {
	// IPHPPartitionScannerConstants.PHP,
	// IPHPPartitionScannerConstants.PHP_MULTILINE_COMMENT,
	// IPHPPartitionScannerConstants.HTML,
	// IPHPPartitionScannerConstants.HTML_MULTILINE_COMMENT,
	// IPHPPartitionScannerConstants.JAVASCRIPT,
	// IPHPPartitionScannerConstants.CSS,
	// IPHPPartitionScannerConstants.SMARTY,
	// IPHPPartitionScannerConstants.SMARTY_MULTILINE_COMMENT };
	// private static PHPPartitionScanner XML_PARTITION_SCANNER = null;

	/* Preference key for temporary problems */
	private final static String HANDLE_TEMPORARY_PROBLEMS = PreferenceConstants.EDITOR_EVALUTE_TEMPORARY_PROBLEMS;

	/** Indicates whether the save has been initialized by this provider */
	private boolean fIsAboutToSave = false;

	/** The save policy used by this provider */
	private ISavePolicy fSavePolicy;

	/** Internal property changed listener */
	private IPropertyChangeListener fPropertyListener;

	/** annotation model listener added to all created CU annotation models */
	private GlobalAnnotationModelListener fGlobalAnnotationModelListener;

	public PHPDocumentProvider() {
		// IDocumentProvider provider= new TextFileDocumentProvider(new
		// JavaStorageDocumentProvider());
		IDocumentProvider provider = new TextFileDocumentProvider();
		provider = new ForwardingDocumentProvider(
				IPHPPartitions.PHP_PARTITIONING,
				new JavaDocumentSetupParticipant(), provider);
		setParentDocumentProvider(provider);

		fGlobalAnnotationModelListener = new GlobalAnnotationModelListener();
		fPropertyListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (HANDLE_TEMPORARY_PROBLEMS.equals(event.getProperty()))
					enableHandlingTemporaryProblems();
			}
		};
		PHPeclipsePlugin.getDefault().getPreferenceStore()
				.addPropertyChangeListener(fPropertyListener);

	}

	/**
	 * Sets the document provider's save policy.
	 */
	public void setSavePolicy(ISavePolicy savePolicy) {
		fSavePolicy = savePolicy;
	}

	/**
	 * Creates a compilation unit from the given file.
	 * 
	 * @param file
	 *            the file from which to create the compilation unit
	 */
	protected ICompilationUnit createCompilationUnit(IFile file) {
		Object element = JavaCore.create(file);
		if (element instanceof ICompilationUnit)
			return (ICompilationUnit) element;
		return null;
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider#createEmptyFileInfo()
	 */
	protected FileInfo createEmptyFileInfo() {
		return new CompilationUnitInfo();
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider#createAnnotationModel(org.eclipse.core.resources.IFile)
	 */
	protected IAnnotationModel createAnnotationModel(IFile file) {
		return new CompilationUnitAnnotationModel(file);
	}

	/*
	 * @see AbstractDocumentProvider#createElementInfo(Object)
	 */
	// protected ElementInfo createElementInfo(Object element) throws
	// CoreException {
	//
	// if (!(element instanceof IFileEditorInput))
	// return super.createElementInfo(element);
	//
	// IFileEditorInput input = (IFileEditorInput) element;
	// ICompilationUnit original = createCompilationUnit(input.getFile());
	// if (original != null) {
	//
	// try {
	//
	// try {
	// refreshFile(input.getFile());
	// } catch (CoreException x) {
	// handleCoreException(x,
	// PHPEditorMessages.getString("PHPDocumentProvider.error.createElementInfo"));
	// //$NON-NLS-1$
	// }
	//
	// IAnnotationModel m = createCompilationUnitAnnotationModel(input);
	// IProblemRequestor r = m instanceof IProblemRequestor ?
	// (IProblemRequestor) m : null;
	// ICompilationUnit c = (ICompilationUnit)
	// original.getSharedWorkingCopy(getProgressMonitor(), fBufferFactory, r);
	//
	// DocumentAdapter a = null;
	// try {
	// a = (DocumentAdapter) c.getBuffer();
	// } catch (ClassCastException x) {
	// IStatus status = new Status(IStatus.ERROR, PHPeclipsePlugin.PLUGIN_ID,
	// PHPStatusConstants.TEMPLATE_IO_EXCEPTION, "Shared working copy has wrong
	// buffer", x); //$NON-NLS-1$
	// throw new CoreException(status);
	// }
	//
	// _FileSynchronizer f = new _FileSynchronizer(input);
	// f.install();
	//
	// CompilationUnitInfo info = new CompilationUnitInfo(a.getDocument(), m, f,
	// c);
	// info.setModificationStamp(computeModificationStamp(input.getFile()));
	// info.fStatus = a.getStatus();
	// info.fEncoding = getPersistedEncoding(input);
	//
	// if (r instanceof IProblemRequestorExtension) {
	// IProblemRequestorExtension extension = (IProblemRequestorExtension) r;
	// extension.setIsActive(isHandlingTemporaryProblems());
	// }
	// m.addAnnotationModelListener(fGlobalAnnotationModelListener);
	//
	// return info;
	//
	// } catch (JavaModelException x) {
	// throw new CoreException(x.getStatus());
	// }
	// } else {
	// return super.createElementInfo(element);
	// }
	// }
	/*
	 * @see AbstractDocumentProvider#disposeElementInfo(Object, ElementInfo)
	 */
	// protected void disposeElementInfo(Object element, ElementInfo info) {
	//
	// if (info instanceof CompilationUnitInfo) {
	// CompilationUnitInfo cuInfo = (CompilationUnitInfo) info;
	// cuInfo.fCopy.destroy();
	// cuInfo.fModel.removeAnnotationModelListener(fGlobalAnnotationModelListener);
	// }
	//
	// super.disposeElementInfo(element, info);
	// }
	/*
	 * @see AbstractDocumentProvider#doSaveDocument(IProgressMonitor, Object,
	 *      IDocument, boolean)
	 */
	// protected void doSaveDocument(IProgressMonitor monitor, Object element,
	// IDocument document, boolean overwrite)
	// throws CoreException {
	//
	// ElementInfo elementInfo = getElementInfo(element);
	// if (elementInfo instanceof CompilationUnitInfo) {
	// CompilationUnitInfo info = (CompilationUnitInfo) elementInfo;
	//
	// // update structure, assumes lock on info.fCopy
	// info.fCopy.reconcile();
	//
	// ICompilationUnit original = (ICompilationUnit)
	// info.fCopy.getOriginalElement();
	// IResource resource = original.getResource();
	//
	// if (resource == null) {
	// // underlying resource has been deleted, just recreate file, ignore the
	// rest
	// super.doSaveDocument(monitor, element, document, overwrite);
	// return;
	// }
	//
	// if (resource != null && !overwrite)
	// checkSynchronizationState(info.fModificationStamp, resource);
	//
	// if (fSavePolicy != null)
	// fSavePolicy.preSave(info.fCopy);
	//
	// // inform about the upcoming content change
	// fireElementStateChanging(element);
	// try {
	// fIsAboutToSave = true;
	// // commit working copy
	// info.fCopy.commit(overwrite, monitor);
	// } catch (CoreException x) {
	// // inform about the failure
	// fireElementStateChangeFailed(element);
	// throw x;
	// } catch (RuntimeException x) {
	// // inform about the failure
	// fireElementStateChangeFailed(element);
	// throw x;
	// } finally {
	// fIsAboutToSave = false;
	// }
	//
	// // If here, the dirty state of the editor will change to "not dirty".
	// // Thus, the state changing flag will be reset.
	//
	// AbstractMarkerAnnotationModel model = (AbstractMarkerAnnotationModel)
	// info.fModel;
	// model.updateMarkers(info.fDocument);
	//
	// if (resource != null)
	// info.setModificationStamp(computeModificationStamp(resource));
	//
	// if (fSavePolicy != null) {
	// ICompilationUnit unit = fSavePolicy.postSave(original);
	// if (unit != null) {
	// IResource r = unit.getResource();
	// IMarker[] markers = r.findMarkers(IMarker.MARKER, true,
	// IResource.DEPTH_ZERO);
	// if (markers != null && markers.length > 0) {
	// for (int i = 0; i < markers.length; i++)
	// model.updateMarker(markers[i], info.fDocument, null);
	// }
	// }
	// }
	//
	// } else {
	// super.doSaveDocument(monitor, element, document, overwrite);
	// }
	// }
	/*
	 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider#createFileInfo(java.lang.Object)
	 */
	protected FileInfo createFileInfo(Object element) throws CoreException {
		if (!(element instanceof IFileEditorInput))
			return null;

		IFileEditorInput input = (IFileEditorInput) element;
		ICompilationUnit original = createCompilationUnit(input.getFile());
		if (original == null)
			return null;

		FileInfo info = super.createFileInfo(element);
		if (!(info instanceof CompilationUnitInfo))
			return null;

		CompilationUnitInfo cuInfo = (CompilationUnitInfo) info;
		setUpSynchronization(cuInfo);

		IProblemRequestor requestor = cuInfo.fModel instanceof IProblemRequestor ? (IProblemRequestor) cuInfo.fModel
				: null;

		original.becomeWorkingCopy(requestor, getProgressMonitor());
		cuInfo.fCopy = original;

		if (cuInfo.fModel instanceof CompilationUnitAnnotationModel) {
			CompilationUnitAnnotationModel model = (CompilationUnitAnnotationModel) cuInfo.fModel;
			model.setCompilationUnit(cuInfo.fCopy);
		}

		if (cuInfo.fModel != null)
			cuInfo.fModel
					.addAnnotationModelListener(fGlobalAnnotationModelListener);

		if (requestor instanceof IProblemRequestorExtension) {
			IProblemRequestorExtension extension = (IProblemRequestorExtension) requestor;
			extension.setIsActive(isHandlingTemporaryProblems());
		}

		return cuInfo;
	}

	private void setUpSynchronization(CompilationUnitInfo cuInfo) {
		IDocument document = cuInfo.fTextFileBuffer.getDocument();
		IAnnotationModel model = cuInfo.fModel;

		if (document instanceof ISynchronizable
				&& model instanceof ISynchronizable) {
			Object lock = ((ISynchronizable) document).getLockObject();
			((ISynchronizable) model).setLockObject(lock);
		}
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider#disposeFileInfo(java.lang.Object,
	 *      org.eclipse.ui.editors.text.TextFileDocumentProvider.FileInfo)
	 */
	protected void disposeFileInfo(Object element, FileInfo info) {
		if (info instanceof CompilationUnitInfo) {
			CompilationUnitInfo cuInfo = (CompilationUnitInfo) info;

			try {
				cuInfo.fCopy.discardWorkingCopy();
			} catch (JavaModelException x) {
				handleCoreException(x, x.getMessage());
			}

			if (cuInfo.fModel != null)
				cuInfo.fModel
						.removeAnnotationModelListener(fGlobalAnnotationModelListener);
		}
		super.disposeFileInfo(element, info);
	}

	protected void commitWorkingCopy(IProgressMonitor monitor, Object element,
			CompilationUnitInfo info, boolean overwrite) throws CoreException {
		synchronized (info.fCopy) {
			info.fCopy.reconcile();
		}

		IDocument document = info.fTextFileBuffer.getDocument();
		IResource resource = info.fCopy.getResource();

		Assert.isTrue(resource instanceof IFile);
		if (!resource.exists()) {
			// underlying resource has been deleted, just recreate file, ignore
			// the rest
			createFileFromDocument(monitor, (IFile) resource, document);
			return;
		}

		if (fSavePolicy != null)
			fSavePolicy.preSave(info.fCopy);

		try {

			fIsAboutToSave = true;
			info.fCopy.commitWorkingCopy(overwrite, monitor);

		} catch (CoreException x) {
			// inform about the failure
			fireElementStateChangeFailed(element);
			throw x;
		} catch (RuntimeException x) {
			// inform about the failure
			fireElementStateChangeFailed(element);
			throw x;
		} finally {
			fIsAboutToSave = false;
		}

		// If here, the dirty state of the editor will change to "not dirty".
		// Thus, the state changing flag will be reset.
		if (info.fModel instanceof AbstractMarkerAnnotationModel) {
			AbstractMarkerAnnotationModel model = (AbstractMarkerAnnotationModel) info.fModel;
			model.updateMarkers(document);
		}

		if (fSavePolicy != null) {
			ICompilationUnit unit = fSavePolicy.postSave(info.fCopy);
			if (unit != null
					&& info.fModel instanceof AbstractMarkerAnnotationModel) {
				IResource r = unit.getResource();
				IMarker[] markers = r.findMarkers(IMarker.MARKER, true,
						IResource.DEPTH_ZERO);
				if (markers != null && markers.length > 0) {
					AbstractMarkerAnnotationModel model = (AbstractMarkerAnnotationModel) info.fModel;
					for (int i = 0; i < markers.length; i++)
						model.updateMarker(document, markers[i], null);
				}
			}
		}

	}

	/*
	 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider#createSaveOperation(java.lang.Object,
	 *      org.eclipse.jface.text.IDocument, boolean)
	 */
	protected DocumentProviderOperation createSaveOperation(
			final Object element, final IDocument document,
			final boolean overwrite) throws CoreException {
		// final FileInfo info= getFileInfo(element);
		// if (info instanceof CompilationUnitInfo) {
		// return new DocumentProviderOperation() {
		// /*
		// * @see
		// org.eclipse.ui.editors.text.TextFileDocumentProvider.DocumentProviderOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
		// */
		// protected void execute(IProgressMonitor monitor) throws CoreException
		// {
		// commitWorkingCopy(monitor, element, (CompilationUnitInfo) info,
		// overwrite);
		// }
		// /*
		// * @see
		// org.eclipse.ui.editors.text.TextFileDocumentProvider.DocumentProviderOperation#getSchedulingRule()
		// */
		// public ISchedulingRule getSchedulingRule() {
		// if (info.fElement instanceof IFileEditorInput) {
		// IFile file= ((IFileEditorInput) info.fElement).getFile();
		// IResourceRuleFactory ruleFactory=
		// ResourcesPlugin.getWorkspace().getRuleFactory();
		// if (file == null || !file.exists())
		// return ruleFactory.createRule(file);
		// else
		// return ruleFactory.modifyRule(file);
		// } else
		// return null;
		// }
		// };
		// }
		// return null;
		final FileInfo info = getFileInfo(element);
		if (info instanceof CompilationUnitInfo) {
			return new DocumentProviderOperation() {
				/*
				 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider.DocumentProviderOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
				 */
				protected void execute(IProgressMonitor monitor)
						throws CoreException {
					commitWorkingCopy(monitor, element,
							(CompilationUnitInfo) info, overwrite);
				}

				/*
				 * @see org.eclipse.ui.editors.text.TextFileDocumentProvider.DocumentProviderOperation#getSchedulingRule()
				 */
				public ISchedulingRule getSchedulingRule() {
					if (info.fElement instanceof IFileEditorInput) {
						IFile file = ((IFileEditorInput) info.fElement)
								.getFile();
						return computeSchedulingRule(file);
					} else
						return null;
				}
			};
		}
		return null;
	}

	/*
	 * (non-Javadoc) Method declared on AbstractDocumentProvider
	 */
	// protected IDocument createDocument(Object element) throws CoreException {
	// if (element instanceof IEditorInput) {
	// Document document = new PartiallySynchronizedDocument();
	// if (setDocumentContent(document, (IEditorInput) element,
	// getEncoding(element))) {
	// initializeDocument(document, (IEditorInput) element);
	//
	// //
	// // IDocument document = super.createDocument(element);
	// // if (document != null) {
	// // IDocumentPartitioner partitioner = null;
	// // if (element instanceof FileEditorInput) {
	// // IFile file = (IFile) ((FileEditorInput)
	// element).getAdapter(IFile.class);
	// // String filename = file.getLocation().toString();
	// // String extension = filename.substring(filename.lastIndexOf("."),
	// filename.length());
	// // // System.out.println(extension);
	// // if (extension.equalsIgnoreCase(".html") ||
	// extension.equalsIgnoreCase(".htm")) {
	// // // html
	// // partitioner = createHTMLPartitioner();
	// // } else if (extension.equalsIgnoreCase(".xml")) {
	// // // xml
	// // partitioner = createXMLPartitioner();
	// // } else if (extension.equalsIgnoreCase(".js")) {
	// // // javascript
	// // partitioner = createJavaScriptPartitioner();
	// // } else if (extension.equalsIgnoreCase(".css")) {
	// // // cascading style sheets
	// // partitioner = createCSSPartitioner();
	// // } else if (extension.equalsIgnoreCase(".tpl")) {
	// // // smarty ?
	// // partitioner = createSmartyPartitioner();
	// // } else if (extension.equalsIgnoreCase(".inc")) {
	// // // php include files ?
	// // partitioner = createIncludePartitioner();
	// // }
	// // }
	// //
	// // if (partitioner == null) {
	// // partitioner = createPHPPartitioner();
	// // }
	// // document.setDocumentPartitioner(partitioner);
	// // partitioner.connect(document);
	// }
	// return document;
	// }
	// return null;
	// }
	// /**
	// * Return a partitioner for .html files.
	// */
	// private IDocumentPartitioner createHTMLPartitioner() {
	// return new DefaultPartitioner(getHTMLPartitionScanner(), TYPES);
	// }
	//
	// private IDocumentPartitioner createIncludePartitioner() {
	// return new DefaultPartitioner(getPHPPartitionScanner(), TYPES);
	// }
	//
	// private IDocumentPartitioner createJavaScriptPartitioner() {
	// return new DefaultPartitioner(getHTMLPartitionScanner(), TYPES);
	// }
	/**
	 * Creates a line tracker working with the same line delimiters as the
	 * document of the given element. Assumes the element to be managed by this
	 * document provider.
	 * 
	 * @param element
	 *            the element serving as blue print
	 * @return a line tracker based on the same line delimiters as the element's
	 *         document
	 */
	public ILineTracker createLineTracker(Object element) {
		return new DefaultLineTracker();
	}

	// /**
	// * Return a partitioner for .php files.
	// */
	// private IDocumentPartitioner createPHPPartitioner() {
	// return new DefaultPartitioner(getPHPPartitionScanner(), TYPES);
	// }
	//
	// private IDocumentPartitioner createSmartyPartitioner() {
	// return new DefaultPartitioner(getSmartyPartitionScanner(), TYPES);
	// }
	//
	// private IDocumentPartitioner createXMLPartitioner() {
	// return new DefaultPartitioner(getXMLPartitionScanner(), TYPES);
	// }
	//
	// /**
	// * Return a scanner for creating html partitions.
	// */
	// private PHPPartitionScanner getHTMLPartitionScanner() {
	// if (HTML_PARTITION_SCANNER == null)
	// HTML_PARTITION_SCANNER = new
	// PHPPartitionScanner(IPHPPartitionScannerConstants.HTML_FILE);
	// return HTML_PARTITION_SCANNER;
	// }
	// /**
	// * Return a scanner for creating php partitions.
	// */
	// private PHPPartitionScanner getPHPPartitionScanner() {
	// if (PHP_PARTITION_SCANNER == null)
	// PHP_PARTITION_SCANNER = new
	// PHPPartitionScanner(IPHPPartitionScannerConstants.PHP_FILE);
	// return PHP_PARTITION_SCANNER;
	// }
	//
	// /**
	// * Return a scanner for creating smarty partitions.
	// */
	// private PHPPartitionScanner getSmartyPartitionScanner() {
	// if (SMARTY_PARTITION_SCANNER == null)
	// SMARTY_PARTITION_SCANNER = new
	// PHPPartitionScanner(IPHPPartitionScannerConstants.SMARTY_FILE);
	// return SMARTY_PARTITION_SCANNER;
	// }
	//
	// /**
	// * Return a scanner for creating xml partitions.
	// */
	// private PHPPartitionScanner getXMLPartitionScanner() {
	// if (XML_PARTITION_SCANNER == null)
	// XML_PARTITION_SCANNER = new
	// PHPPartitionScanner(IPHPPartitionScannerConstants.XML_FILE);
	// return XML_PARTITION_SCANNER;
	// }

	// protected void initializeDocument(IDocument document, IEditorInput
	// editorInput) {
	// if (document != null) {
	// JavaTextTools tools = PHPeclipsePlugin.getDefault().getJavaTextTools();
	// IDocumentPartitioner partitioner = null;
	// if (editorInput != null && editorInput instanceof FileEditorInput) {
	// IFile file = (IFile) ((FileEditorInput)
	// editorInput).getAdapter(IFile.class);
	// String filename = file.getLocation().toString();
	// String extension = filename.substring(filename.lastIndexOf("."),
	// filename.length());
	// partitioner = tools.createDocumentPartitioner(extension);
	// } else {
	// partitioner = tools.createDocumentPartitioner(".php");
	// }
	// document.setDocumentPartitioner(partitioner);
	// partitioner.connect(document);
	// }
	// }

	/*
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#doResetDocument(java.lang.Object,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	// protected void doResetDocument(Object element, IProgressMonitor monitor)
	// throws CoreException {
	// if (element == null)
	// return;
	//
	// ElementInfo elementInfo= getElementInfo(element);
	// if (elementInfo instanceof CompilationUnitInfo) {
	// CompilationUnitInfo info= (CompilationUnitInfo) elementInfo;
	//
	// IDocument document;
	// IStatus status= null;
	//
	// try {
	//
	// ICompilationUnit original= (ICompilationUnit)
	// info.fCopy.getOriginalElement();
	// IResource resource= original.getResource();
	// if (resource instanceof IFile) {
	//
	// IFile file= (IFile) resource;
	//
	// try {
	// refreshFile(file, monitor);
	// } catch (CoreException x) {
	// handleCoreException(x,
	// PHPEditorMessages.getString("CompilationUnitDocumentProvider.error.resetDocument"));
	// //$NON-NLS-1$
	// }
	//
	// IFileEditorInput input= new FileEditorInput(file);
	// document= super.createDocument(input);
	//
	// } else {
	// document= createEmptyDocument();
	// }
	//
	// } catch (CoreException x) {
	// document= createEmptyDocument();
	// status= x.getStatus();
	// }
	//
	// fireElementContentAboutToBeReplaced(element);
	//
	// removeUnchangedElementListeners(element, info);
	// info.fDocument.set(document.get());
	// info.fCanBeSaved= false;
	// info.fStatus= status;
	// addUnchangedElementListeners(element, info);
	//
	// fireElementContentReplaced(element);
	// fireElementDirtyStateChanged(element, false);
	//
	// } else {
	// super.doResetDocument(element, monitor);
	// }
	// }
	/*
	 * @see AbstractDocumentProvider#resetDocument(Object)
	 */
	// public void resetDocument(Object element) throws CoreException {
	// if (element == null)
	// return;
	//
	// ElementInfo elementInfo = getElementInfo(element);
	// if (elementInfo instanceof CompilationUnitInfo) {
	// CompilationUnitInfo info = (CompilationUnitInfo) elementInfo;
	//
	// IDocument document;
	// IStatus status = null;
	//
	// try {
	//
	// ICompilationUnit original = (ICompilationUnit)
	// info.fCopy.getOriginalElement();
	// IResource resource = original.getResource();
	// if (resource instanceof IFile) {
	//
	// IFile file = (IFile) resource;
	//
	// try {
	// refreshFile(file);
	// } catch (CoreException x) {
	// handleCoreException(x,
	// PHPEditorMessages.getString("PHPDocumentProvider.error.resetDocument"));
	// //$NON-NLS-1$
	// }
	//
	// IFileEditorInput input = new FileEditorInput(file);
	// document = super.createDocument(input);
	//
	// } else {
	// document = new Document();
	// }
	//
	// } catch (CoreException x) {
	// document = new Document();
	// status = x.getStatus();
	// }
	//
	// fireElementContentAboutToBeReplaced(element);
	//
	// removeUnchangedElementListeners(element, info);
	// info.fDocument.set(document.get());
	// info.fCanBeSaved = false;
	// info.fStatus = status;
	// addUnchangedElementListeners(element, info);
	//
	// fireElementContentReplaced(element);
	// fireElementDirtyStateChanged(element, false);
	//
	// } else {
	// super.resetDocument(element);
	// }
	// }
	/**
	 * Saves the content of the given document to the given element. This is
	 * only performed when this provider initiated the save.
	 * 
	 * @param monitor
	 *            the progress monitor
	 * @param element
	 *            the element to which to save
	 * @param document
	 *            the document to save
	 * @param overwrite
	 *            <code>true</code> if the save should be enforced
	 */
	public void saveDocumentContent(IProgressMonitor monitor, Object element,
			IDocument document, boolean overwrite) throws CoreException {
		if (!fIsAboutToSave)
			return;
		super.saveDocument(monitor, element, document, overwrite);
		// if (!fIsAboutToSave)
		// return;
		//
		// if (element instanceof IFileEditorInput) {
		// IFileEditorInput input = (IFileEditorInput) element;
		// try {
		// String encoding = getEncoding(element);
		// if (encoding == null)
		// encoding = ResourcesPlugin.getEncoding();
		// InputStream stream = new
		// ByteArrayInputStream(document.get().getBytes(encoding));
		// IFile file = input.getFile();
		// file.setContents(stream, overwrite, true, monitor);
		// } catch (IOException x) {
		// IStatus s = new Status(IStatus.ERROR, PHPeclipsePlugin.PLUGIN_ID,
		// IStatus.OK, x.getMessage(), x);
		// throw new CoreException(s);
		// }
		// }
	}

	/**
	 * Returns the underlying resource for the given element.
	 * 
	 * @param the
	 *            element
	 * @return the underlying resource of the given element
	 */
	// public IResource getUnderlyingResource(Object element) {
	// if (element instanceof IFileEditorInput) {
	// IFileEditorInput input = (IFileEditorInput) element;
	// return input.getFile();
	// }
	// return null;
	// }
	/*
	 * @see net.sourceforge.phpdt.internal.ui.javaeditor.ICompilationUnitDocumentProvider#getWorkingCopy(java.lang.Object)
	 */
	public ICompilationUnit getWorkingCopy(Object element) {
		FileInfo fileInfo = getFileInfo(element);
		if (fileInfo instanceof CompilationUnitInfo) {
			CompilationUnitInfo info = (CompilationUnitInfo) fileInfo;
			return info.fCopy;
		}
		return null;
	}

	/*
	 * @see net.sourceforge.phpdt.internal.ui.javaeditor.ICompilationUnitDocumentProvider#shutdown()
	 */
	public void shutdown() {
		PHPeclipsePlugin.getDefault().getPreferenceStore()
				.removePropertyChangeListener(fPropertyListener);
		Iterator e = getConnectedElementsIterator();
		while (e.hasNext())
			disconnect(e.next());
	}

	/**
	 * Returns the preference whether handling temporary problems is enabled.
	 */
	protected boolean isHandlingTemporaryProblems() {
		IPreferenceStore store = PHPeclipsePlugin.getDefault()
				.getPreferenceStore();
		return store.getBoolean(HANDLE_TEMPORARY_PROBLEMS);
	}

	/**
	 * Switches the state of problem acceptance according to the value in the
	 * preference store.
	 */
	protected void enableHandlingTemporaryProblems() {
		boolean enable = isHandlingTemporaryProblems();
		for (Iterator iter = getFileInfosIterator(); iter.hasNext();) {
			FileInfo info = (FileInfo) iter.next();
			if (info.fModel instanceof IProblemRequestorExtension) {
				IProblemRequestorExtension extension = (IProblemRequestorExtension) info.fModel;
				extension.setIsActive(enable);
			}
		}
	}

	/**
	 * Adds a listener that reports changes from all compilation unit annotation
	 * models.
	 */
	public void addGlobalAnnotationModelListener(
			IAnnotationModelListener listener) {
		fGlobalAnnotationModelListener.addListener(listener);
	}

	/**
	 * Removes the listener.
	 */
	public void removeGlobalAnnotationModelListener(
			IAnnotationModelListener listener) {
		fGlobalAnnotationModelListener.removeListener(listener);
	}

	/**
	 * Computes the scheduling rule needed to create or modify a resource. If
	 * the resource exists, its modify rule is returned. If it does not, the
	 * resource hierarchy is iterated towards the workspace root to find the
	 * first parent of <code>toCreateOrModify</code> that exists. Then the
	 * 'create' rule for the last non-existing resource is returned.
	 * <p>
	 * XXX This is a workaround for
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=67601
	 * IResourceRuleFactory.createRule should iterate the hierarchy itself.
	 * </p>
	 * <p>
	 * XXX to be replaced by call to
	 * TextFileDocumentProvider.computeSchedulingRule after 3.0
	 * </p>
	 * 
	 * @param toCreateOrModify
	 *            the resource to create or modify
	 * @return the minimal scheduling rule needed to modify or create a resource
	 */
	protected ISchedulingRule computeSchedulingRule(IResource toCreateOrModify) {
		IResourceRuleFactory factory = ResourcesPlugin.getWorkspace()
				.getRuleFactory();
		if (toCreateOrModify.exists()) {
			return factory.modifyRule(toCreateOrModify);
		} else {
			IResource parent = toCreateOrModify;
			do {
				toCreateOrModify = parent;
				parent = toCreateOrModify.getParent();
			} while (parent != null && !parent.exists());

			return factory.createRule(toCreateOrModify);
		}
	}
}
