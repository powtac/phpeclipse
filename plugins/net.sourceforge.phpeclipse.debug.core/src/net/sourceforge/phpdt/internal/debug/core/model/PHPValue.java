/**********************************************************************
 Copyright (c) 2000, 2002 IBM Corp. and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html

 Contributors:
 IBM Corporation - Initial implementation
 Vicente Fernando - www.alfersoft.com.ar
 Christian Perkonig - cperkonig@gmx.at
 **********************************************************************/
package net.sourceforge.phpdt.internal.debug.core.model;

import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

/**
 * PHPValue object belongs to a PHPVariable (is a member of PHPVariable).
 * A PHPValue itself can have PHPVariables as children.
 *
 */
public class PHPValue implements IValue {

	final static String[] PEV_NAMES = {
			"undefined", 											// 0
			"long", 												// 1
			"double", 												// 2
			"string", 												// 3
			"array", 												// 4
			"object", 												// 5
			"boolean", 												// 6
			"resource", 											// 7
			"reference", 											// 8
			"soft reference" }; 									// 9

	public final static int PEVT_UNKNOWN 		= 0;
	public final static int PEVT_LONG 		= 1;
	public final static int PEVT_DOUBLE 		= 2;
	public final static int PEVT_STRING 		= 3;
	public final static int PEVT_ARRAY 		= 4;
	public final static int PEVT_OBJECT 		= 5;
	public final static int PEVT_BOOLEAN 		= 6;
	public final static int PEVT_RESOURCE 	= 7;
	public final static int PEVT_REF 			= 8;
	public final static int PEVT_SOFTREF 		= 9;

	private int 			fValueType; 							// The type of this value (see the PEVT_... values)
	//private boolean 		hasChildren; 							// This value (variable) has children (more variables)
	private String 		fValueString; 							// The value of this variable as text
	private Vector 		fVariables; 							// The children of this variable (other variables) if any
	private PHPStackFrame 	fStackFrame; 							// The stackframe this value (variable) belongs to
	private boolean 		fHasChanged; 							// The value has changed between two suspends
																	// This variable was moved from PHPVariable due to the fact,
																	// that two PHPVariables can reference the same PHPValue,
																	// so only the first PHPVariable would win, when we check the variable tree
																	// for changed values.
	private boolean 		fSorted;

	/**
	 *
	 */
	PHPValue() {
		this(null, "", PEVT_UNKNOWN, null); 						// Creates an empty value
	}

	/**
	 *
	 * @param frame       The stackframe this value (and variable) belongs to.
	 * @param value       The value of this value.
	 * @param fValueType  The type of this value (see the PEVT_... values).
	 * @param subitems    This value has subitems.
	 */
	public PHPValue(PHPStackFrame frame, String value, int fValueType, Vector subitems) {
		this.fValueType = fValueType;
		this.fValueString = value;
		this.fStackFrame = frame;
		this.fHasChanged = false;
		this.fSorted = false;

		if (subitems != null) { 									// If there are children for this value (variable)
			this.fVariables = new Vector(subitems); 				// Then add the children to this value (variable)
		} else {
			this.fVariables = new Vector(); 						// Create an empty vector
		}
	}

	/**
	 *
	 * @param item
	 */
	public Vector addVariable(Vector item) {
		if (item != null) { 										// If there is something we want to add
			this.fVariables.addAll(item);
			this.fSorted = false;
		}

		return this.fVariables;
	}

	/**
	 *
	 * @param parent
	 */
	public void setParent(PHPVariable parent) {
		if (!fVariables.isEmpty()) { 								// If we have child variables
			Iterator iter = fVariables.iterator(); 					// Create an iterator for the children

			while (iter.hasNext()) { 								// As long as we have children
				((PHPVariable) iter.next()).setParent(parent); 		// Set all child's parent
			}
		}
	}

	/**
	 * @see org.eclipse.debug.core.model.IValue#getReferenceTypeName()
	 */
	public String getReferenceTypeName() {
		return PEV_NAMES[fValueType];
	}

	/**
	 *
	 */
	public int getReferenceType() {
		return fValueType;
	}

	/**
	 * @param type Set the reference type (see the PEVT_... values).
	 */
	public int setReferenceType(int type) {
		return fValueType = type;
	}

	/**
	 * This method is called whenever this value (variable) is changed.
	 *
	 * @param value The changed value for this variable.
	 */
	public void setValueString(String value) {
		fValueString = value;
	}

	/**
	 * @see org.eclipse.debug.core.model.IValue#getfValueString()
	 */
	public String getValueString() {
		return fValueString;
	}

	/**
	 * @see org.eclipse.debug.core.model.IValue#isAllocated()
	 */
	public boolean isAllocated() throws DebugException {
		return false;
	}

	/**
	 * @see org.eclipse.debug.core.model.IValue#getVariables()
	 *
	 * @return The array of child variable for this value (variable).
	 */
	public IVariable[] getVariables() {
		return (PHPVariable[]) getChildVariables().toArray(
				new PHPVariable[fVariables.size()]);
	}

	/**
	 *
	 */
	public Vector getChildVariables() {
		if (!fSorted) {
			Collections.sort(fVariables, new PHPVariableComparator());
			fSorted = true;
		}

		return fVariables;
	}

	/**
	 * @see org.eclipse.debug.core.model.IValue#hasVariables()
	 *
	 * @return
	 * <ul>
	 * <li> <code>true</code> if this value (variable) has child variables
	 * <li> <code>false</code> if no child variable available
	 * </ul>
	 */
	public boolean hasVariables() throws DebugException {
		// return (!fVariables.isEmpty());
		return (fVariables.size() != 0);
	}

	/**
	 * @see org.eclipse.debug.core.model.IDebugElement#getModelIdentifier()
	 */
	public String getModelIdentifier() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.eclipse.debug.core.model.IDebugElement#getDebugTarget()
	 */
	public IDebugTarget getDebugTarget() {
		return fStackFrame.getDebugTarget();
	}

	/**
	 * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
	 */
	public ILaunch getLaunch() {
		return getDebugTarget().getLaunch();
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		return null;
	}

	public boolean hasValueChanged() throws DebugException {
		return fHasChanged;
	}

	public void setValueChanged(boolean changed) {
		fHasChanged = changed;
	}

	/*
	 * ONLY FOR net.sourceforge.phpdt.internal.debug.core.model.PHPDBGEvalString#copyItems(PHPVariable, PHPValue)
	 */
	protected void setVariables(Vector variables) {
		fVariables = variables;
	}

	/*
	 * ONLY FOR net.sourceforge.phpdt.internal.debug.core.model.PHPDBGEvalString#copyItems(PHPVariable, PHPValue)
	 */
	protected Object clone() throws CloneNotSupportedException {
		PHPValue val = new PHPValue();
		val.fValueType = fValueType;
		val.fValueString = fValueString;
		val.fVariables = fVariables;
		val.fStackFrame = fStackFrame;
		val.fHasChanged = fHasChanged;
		val.fSorted = fSorted;
		return val;
	}

}
