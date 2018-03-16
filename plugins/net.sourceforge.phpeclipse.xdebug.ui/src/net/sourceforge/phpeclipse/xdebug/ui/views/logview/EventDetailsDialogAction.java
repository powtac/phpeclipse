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
package net.sourceforge.phpeclipse.xdebug.ui.views.logview;

import java.util.Comparator;


import net.sourceforge.phpeclipse.xdebug.ui.XDebugUIPlugin;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.SelectionProviderAction;



public class EventDetailsDialogAction extends SelectionProviderAction{

	/**
	 * The shell in which to open the property dialog
	 */
	private Shell shell;
	private ISelectionProvider provider;
	private EventDetailsDialog propertyDialog;
	private Comparator comparator;
	/**
	 * Creates a new action for opening a property dialog
	 * on the elements from the given selection provider
	 * @param shell - the shell in which the dialog will open
	 * @param provider - the selection provider whose elements
	 * the property dialog will describe
	 */
	public EventDetailsDialogAction(Shell shell, ISelectionProvider provider){
       super(provider, "Test Text"); 
		Assert.isNotNull(shell);
		this.shell = shell;
		this.provider = provider;
		// setToolTipText
		//WorkbenchHelp.setHelp
	}
	
	public boolean resetSelection(byte sortType, int sortOrder){
		IAdaptable element = (IAdaptable) getStructuredSelection().getFirstElement();
		if (element == null)
			return false;
		if (propertyDialog != null && propertyDialog.isOpen()){
			propertyDialog.resetSelection(element, sortType, sortOrder);
			return true;
		}
		return false;
	}
	public void resetSelection(){
		IAdaptable element = (IAdaptable) getStructuredSelection().getFirstElement();
		if (element == null)
			return;
		if (propertyDialog != null && propertyDialog.isOpen())
			propertyDialog.resetSelection(element);
	}
	
	public void resetDialogButtons(){
		if (propertyDialog != null && propertyDialog.isOpen())
			propertyDialog.resetButtons();
	}
	
	public void setComparator(Comparator comparator){
		this.comparator = comparator;
	}
	public void run(){
		if (propertyDialog != null && propertyDialog.isOpen()){
			resetSelection();
			return;
		}
		
		//get initial selection
		IAdaptable element = (IAdaptable) getStructuredSelection().getFirstElement();
		if (element == null)
			return;
		
		propertyDialog = new EventDetailsDialog(shell, element, provider);
		propertyDialog.create();
		propertyDialog.getShell().setText(XDebugUIPlugin.getString("EventDetailsDialog.title")); //$NON-NLS-1$
		propertyDialog.setComparator(comparator);
		propertyDialog.open();
	}
}
