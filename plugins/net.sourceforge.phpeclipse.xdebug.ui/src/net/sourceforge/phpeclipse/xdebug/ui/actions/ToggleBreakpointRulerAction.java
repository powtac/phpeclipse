/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
    Vicente Fernando - www.alfersoft.com.ar
**********************************************************************/
package net.sourceforge.phpeclipse.xdebug.ui.actions;

import net.sourceforge.phpeclipse.xdebug.ui.XDebugUIPlugin;
import net.sourceforge.phpeclipse.xdebug.ui.php.model.PHPLineBreakpointAdapter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class ToggleBreakpointRulerAction extends Action {	
	
	static class EmptySelection implements ISelection {

		public boolean isEmpty() {
			return true;
		}		
	}
	
	private IVerticalRulerInfo fRuler;
	private IWorkbenchPart fTargetPart;
	private PHPLineBreakpointAdapter fBreakpointAdapter;
	private static final ISelection EMPTY_SELECTION = new EmptySelection();  
	
	public ToggleBreakpointRulerAction( IWorkbenchPart part, IVerticalRulerInfo ruler ) {
		super( "Toggle Breakpoint" ); //$NON-NLS-1$

		fRuler= ruler;
		fRuler = ruler;
		setTargetPart( part );
		fBreakpointAdapter = new PHPLineBreakpointAdapter();
//		part.getSite().getWorkbenchWindow().getWorkbench().getHelpSystem().setHelp( this, ICDebugHelpContextIds.TOGGLE_BREAKPOINT_ACTION );
//		setId( IInternalCDebugUIConstants.ACTION_TOGGLE_BREAKPOINT );
	}
	
	
	/**
	 * @see Action#run()
	 */
	public void run() {
		try {
				fBreakpointAdapter.toggleLineBreakpoints( getTargetPart(), getTargetSelection() );
		}
		catch( CoreException e ) {
			XDebugUIPlugin.errorDialog( getTargetPart().getSite().getShell(),"Error", "Operation failed" , e.getStatus() );
		}
	}
	
	/**
	 * Returns this action's vertical ruler info.
	 *
	 * @return this action's vertical ruler
	 */
	protected IVerticalRulerInfo getVerticalRulerInfo() {
		return fRuler;
	}

	private IWorkbenchPart getTargetPart() {
		return this.fTargetPart;
	}

	private void setTargetPart( IWorkbenchPart targetPart ) {
		this.fTargetPart = targetPart;
	}

	/**
	 * Returns the current selection in the active part, possibly
	 * and empty selection, but never <code>null</code>.
	 * 
	 * @return the selection in the active part, possibly empty
	 */
	private ISelection getTargetSelection() {
		IDocument doc = getDocument();
		if ( doc != null ) {
			int line = getVerticalRulerInfo().getLineOfLastMouseButtonActivity();
			try {
				IRegion region = doc.getLineInformation( line );
				return new TextSelection( doc, region.getOffset(), region.getLength() );
			}
			catch( BadLocationException e ) {
				DebugPlugin.log( e );
			} 
		}
		return EMPTY_SELECTION;
	}

	private IDocument getDocument() {
		IWorkbenchPart targetPart = getTargetPart();
		if ( targetPart instanceof ITextEditor ) {
			ITextEditor textEditor = (ITextEditor)targetPart; 
			IDocumentProvider provider = textEditor.getDocumentProvider();
			if ( provider != null )
				return provider.getDocument( textEditor.getEditorInput() );
		}
//		else if ( targetPart instanceof DisassemblyView ) {
//			DisassemblyView dv = (DisassemblyView)targetPart;
//			IDocumentProvider provider = dv.getDocumentProvider();
//			if ( provider != null )
//				return provider.getDocument( dv.getInput() );
//		}
		return null;
	}

}
