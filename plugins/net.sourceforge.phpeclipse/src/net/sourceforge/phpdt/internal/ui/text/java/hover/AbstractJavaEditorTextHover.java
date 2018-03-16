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

package net.sourceforge.phpdt.internal.ui.text.java.hover;

import java.util.List;

import net.sourceforge.phpdt.core.IJavaElement;
import net.sourceforge.phpdt.internal.ui.text.HTMLTextPresenter;
import net.sourceforge.phpdt.internal.ui.text.JavaWordFinder;
import net.sourceforge.phpdt.ui.PreferenceConstants;
import net.sourceforge.phpdt.ui.text.java.hover.IJavaEditorTextHover;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.ICommandManager;
import org.eclipse.ui.commands.IKeySequenceBinding;
import org.eclipse.ui.keys.KeySequence;

/**
 * Abstract class for providing hover information for Java elements.
 * 
 * @since 2.1
 */
public abstract class AbstractJavaEditorTextHover implements
		IJavaEditorTextHover {

	private IEditorPart fEditor;

	private ICommand fCommand;
	{
		ICommandManager commandManager = PlatformUI.getWorkbench()
				.getCommandSupport().getCommandManager();
		// fCommand=
		// commandManager.getCommand(PHPEditorActionDefinitionIds.SHOW_JAVADOC);
		// if (!fCommand.isDefined())
		fCommand = null;
	}

	/*
	 * @see IJavaEditorTextHover#setEditor(IEditorPart)
	 */
	public void setEditor(IEditorPart editor) {
		fEditor = editor;
	}

	protected IEditorPart getEditor() {
		return fEditor;
	}

	// protected ICodeAssist getCodeAssist() {
	// if (fEditor != null) {
	// IEditorInput input= fEditor.getEditorInput();
	// if (input instanceof IClassFileEditorInput) {
	// IClassFileEditorInput cfeInput= (IClassFileEditorInput) input;
	// return cfeInput.getClassFile();
	// }
	//
	// IWorkingCopyManager manager=
	// PHPeclipsePlugin.getDefault().getWorkingCopyManager();
	// return manager.getWorkingCopy(input);
	// }
	//
	// return null;
	// }

	/*
	 * @see ITextHover#getHoverRegion(ITextViewer, int)
	 */
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		return JavaWordFinder.findWord(textViewer.getDocument(), offset);
	}

	/*
	 * @see ITextHover#getHoverInfo(ITextViewer, IRegion)
	 */
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {

		// ICodeAssist resolve= getCodeAssist();
		// if (resolve != null) {
		// try {
		// IJavaElement[] result= null;
		//
		// synchronized (resolve) {
		// result= resolve.codeSelect(hoverRegion.getOffset(),
		// hoverRegion.getLength());
		// }
		//
		// if (result == null)
		// return null;
		//
		// int nResults= result.length;
		// if (nResults == 0)
		// return null;
		//
		// return getHoverInfo(result);
		//
		// } catch (JavaModelException x) {
		// PHPeclipsePlugin.log(x.getStatus());
		// }
		// }
		return null;
	}

	/**
	 * Provides hover information for the given Java elements.
	 * 
	 * @return the hover information string
	 * @since 2.1
	 */
	protected String getHoverInfo(IJavaElement[] javaElements) {
		return null;
	}

	/*
	 * @see ITextHoverExtension#getHoverControlCreator()
	 * @since 3.0
	 */
	public IInformationControlCreator getHoverControlCreator() {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent, SWT.NONE,
						new HTMLTextPresenter(true),
						getTooltipAffordanceString());
			}
		};
	}

	/**
	 * Returns the tool tip affordance string.
	 * 
	 * @return the affordance string or <code>null</code> if disabled or no
	 *         key binding is defined
	 * @since 3.0
	 */
	protected String getTooltipAffordanceString() {
		if (!PHPeclipsePlugin.getDefault().getPreferenceStore().getBoolean(
				PreferenceConstants.EDITOR_SHOW_TEXT_HOVER_AFFORDANCE))
			return null;

		KeySequence[] sequences = getKeySequences();
		if (sequences == null)
			return null;

		String keySequence = sequences[0].format();
		return JavaHoverMessages.getFormattedString(
				"JavaTextHover.makeStickyHint", keySequence); //$NON-NLS-1$
	}

	/**
	 * Returns the array of valid key sequence bindings for the show tool tip
	 * description command.
	 * 
	 * @return the array with the {@link KeySequence}s
	 * 
	 * @since 3.0
	 */
	private KeySequence[] getKeySequences() {
		if (fCommand != null) {
			List list = fCommand.getKeySequenceBindings();
			if (!list.isEmpty()) {
				KeySequence[] keySequences = new KeySequence[list.size()];
				for (int i = 0; i < keySequences.length; i++) {
					keySequences[i] = ((IKeySequenceBinding) list.get(i))
							.getKeySequence();
				}
				return keySequences;
			}
		}
		return null;
	}
}
