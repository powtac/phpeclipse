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

import java.util.Vector;

import net.sourceforge.phpdt.internal.debug.core.PHPDebugCorePlugin;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

/**
 *
 */
public class PHPVariable implements IVariable {

	private PHPValue 		fValue;								 // The value of this variable
	private String 		fName;								 // The name of the variable
	private PHPStackFrame 	fStackFrame;						 // The stackframe this variable belongs to
	private PHPVariable 	fParent;							 // The parent variable (a back link)
	private String 		fLongName;							 // The qualified name
	private boolean		fModifiable = true;

	/**
	 *
	 */
	PHPVariable() {
		this(null, "", null, "", PHPValue.PEVT_UNKNOWN, null); // create an empty variable (a simple dummy node?)
	}

	/**
	 *
	 * @param frame     The stackframe this variable belongs to
	 * @param name      The name for this variable
	 * @param parent    The parent variable if this is not the root
	 * @param value     The value of this variable which is a simple value or again a variable
	 * @param valueType The type of the value (e.g. int, double, string etc.) @see PHPValue
	 * @param subitems
	 */
	public PHPVariable(PHPStackFrame frame, String name, PHPVariable parent,
			String value, int valueType, Vector subitems) {
		this.fStackFrame = frame;
		this.fValue = new PHPValue(frame, value, valueType, subitems);
		this.fParent = parent;

		setName(name);
	}

	/**
	 *
	 * @param name
	 */
	public void setName(String name) {
		if ((fParent == null) ||								 // If we have no parent for this variable
				(fParent.getName() == "")) {					 //  or we have a parent which is just a simple node ???
			fLongName = name;									 // Set the long name
			fName = name;										 //  and set the name

			return;
		}

		switch (fParent.getReferenceType()) {					 // Get the type of the parent variable
		case PHPValue.PEVT_ARRAY:								 // It's an array
			fName = "['" + name + "']";							 // So set the variable name as [name]
			fLongName = fParent.getLongName() + fName;			 // Set the longname to parentVariableLongname[name]
			break;

		case PHPValue.PEVT_OBJECT:								 // It's an object
			fName = name;										 // Set the name to name
			fLongName = fParent.getLongName() + "->" + fName;	 // Set the longname to parentVariableLongname.name
			break;

		default:
			fName = name;										 // Set the name to name
			fLongName = name;									 // Set the Longname to name
			break;
		}
	}

	/**
	 * @see org.eclipse.debug.core.model.IVariable#getValue()
	 */
	public IValue getValue() {
		return fValue;
	}

	/**
	 * @see org.eclipse.debug.core.model.IVariable#getfName()
	 */
	public String getName() {
		return fName;
	}

	/**
	 *
	 */
	public PHPVariable getParent() {
		return fParent;
	}

	/**
	 *
	 */
	public void setParent(PHPVariable parent) {
		this.fParent = parent;

		switch (fParent.getReferenceType()) {
		case PHPValue.PEVT_ARRAY:
			fLongName = fParent.getLongName() + fName;
			break;
		case PHPValue.PEVT_OBJECT:
			fLongName = fParent.getLongName() + "->" + fName;
			break;
		default:
			fLongName = fName;
			break;
		}
	}

	/**
	 *
	 */
	public String getLongName() {
		return fLongName;
	}

	/**
	 * @see org.eclipse.debug.core.model.IVariable#getReferenceTypefName()
	 */
	public String getReferenceTypeName() {
		return fValue.getReferenceTypeName();
	}

	/**
	 *
	 */
	public int getReferenceType() {
		return fValue.getReferenceType();
	}

	/**
	 *
	 */
	public int setReferenceType(int type) {
		return ((PHPValue) getValue()).setReferenceType(type);
	}

	/**
	 * @see org.eclipse.debug.core.model.IVariable#hasValueChanged()
	 */
	public boolean hasValueChanged() throws DebugException {
		return fValue.hasValueChanged();
	}

	/**
	 *
	 * @param changed This method is called after a suspend when the list of
	 *                variables is updated, to mark that this variable has a changed
	 *                value. The variable view will show this variable in
	 *                a different color.
	 */
	public void setValueChanged(boolean changed) {
		fValue.setValueChanged(changed);
	}

	/**
	 * @see org.eclipse.debug.core.model.IDebugElement#getModelIdentifier()
	 */
	public String getModelIdentifier() {
		return getDebugTarget().getModelIdentifier();
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
	 * @see org.eclipse.debug.core.model.IValueModification#setValue(java.lang.String)
	 */
	public void setValue(String expression) throws DebugException {
		String evalString;
		if (fValue.getReferenceType() == PHPValue.PEVT_STRING)
			evalString = fLongName + "=\"" + expression + "\"";
		else
			evalString = fLongName + "=" + expression;
		PHPVariable[] vars = fStackFrame.getPHPDBGProxy().eval(fStackFrame,
				evalString);

		if (vars == null || vars.length == 0) {
			vars = fStackFrame.getPHPDBGProxy().eval(fStackFrame, fLongName);
			if (vars == null || vars.length == 0) {
				int code = 0;
				String msg = "Could not set " + expression + " to " + fLongName;
				Status status = new Status(Status.ERROR,
						PHPDebugCorePlugin.PLUGIN_ID, code, msg, null);
				PHPDebugCorePlugin.log(status);
				throw new DebugException(status);
			}
		}

		fValue = vars[0].fValue;

		// set parent if new value has children
		if (fValue.hasVariables()) {
			Vector variables = fValue.getChildVariables();
			for (int i = 0; i < variables.size(); i++) {
				PHPVariable var = (PHPVariable) variables.get(i);
				var.setParent(this);
				// adjust name if value type is array
				// (still bare name. make "['name']")
				if (fValue.getReferenceType() == PHPValue.PEVT_ARRAY) {
					var.setName(var.getName());
				}
			}
		}

		DebugPlugin.getDefault().fireDebugEventSet(
				new DebugEvent[] { new DebugEvent(this, DebugEvent.CHANGE,
						DebugEvent.CONTENT) });
	}

	/**
	 * @see org.eclipse.debug.core.model.IValueModification#setValue(org.eclipse.debug.core.model.IValue)
	 */
	public void setValue(IValue value) throws DebugException {
		this.fValue = (PHPValue) value;
	}

	/**
	 * @see org.eclipse.debug.core.model.IValueModification#supportsValueModification()
	 */
	public boolean supportsValueModification() {
		return fModifiable;
	}

	/**
	 * Set whether this variable can be modified (default is true)
	 * 
	 * for Global Variables root element only
	 */
	public void setModifiable(boolean modifiable) {
		fModifiable = modifiable;
	}

	/**
	 * @see org.eclipse.debug.core.model.IValueModification#verifyValue(java.lang.String)
	 */
	public boolean verifyValue(String expression) throws DebugException {
		// TODO Auto-generated method stub
		return true;
	}

	/**
	 * @see org.eclipse.debug.core.model.IValueModification#verifyValue(org.eclipse.debug.core.model.IValue)
	 */
	public boolean verifyValue(IValue value) throws DebugException {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class adapter) {
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

	/**
	 * This method is called from variable view and denominates the variables
	 * with a type specific explanation.
	 */
	public String toString() {
		String str = "";

		switch (getReferenceType()) {
		case PHPValue.PEVT_ARRAY:												 // Variable is an array
			int elements = fValue.getVariables().length;						 // Get the number of child elements

			switch (elements) {													 // Switch for the number of child elements
			case 0:																 // We have no child element
				str = this.getName() + " [no elements]";						 // string => 'varname [no elements]'
				break;

			case 1:																 // We have exactly one child element
				str = this.getName() + " [1 element]";							 // string => 'varname [1 element]'
				break;

			default:															 // We have more than one element
				str = this.getName() + " [" + elements + " elements]";			 // string => 'varname [x elements]'
				break;
			}
			break;

		case PHPValue.PEVT_OBJECT:												 // Variable is an object
			str = this.getName() + " [class: " + fValue.getValueString() + "]";	 // string => 'varname [class: varvalue]'
			break;

		case PHPValue.PEVT_STRING:												 // Variable is a string
			str = this.getName() + " = \"" + fValue.getValueString() + "\"";	 // string => 'varname = "varvalue"'
			break;

		case PHPValue.PEVT_SOFTREF:												 // Variable is a soft reference
		default:																 // or anything else
			str = this.getName() + " = " + fValue.getValueString();				 // string => 'varname = varvalue'
			break;
		}

		return str;
	}

	/*
	 * ONLY FOR net.sourceforge.phpdt.internal.debug.core.model.PHPDBGEvalString#copyItems(PHPVariable, PHPValue)
	 */
	protected Object clone() throws CloneNotSupportedException {
		PHPVariable var = new PHPVariable();
		var.fValue = fValue;
		var.fName = fName;
		var.fStackFrame = fStackFrame;
		var.fParent = fParent;
		var.fLongName = fLongName;
		var.fModifiable = fModifiable;
		return var;
	}

}
