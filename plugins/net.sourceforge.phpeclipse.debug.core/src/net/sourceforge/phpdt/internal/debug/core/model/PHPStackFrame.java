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
package net.sourceforge.phpdt.internal.debug.core.model;

import java.util.Collections;
import java.util.Vector;

import net.sourceforge.phpdt.internal.debug.core.PHPDBGProxy;
import net.sourceforge.phpdt.internal.debug.core.PHPDebugCorePlugin;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;

/**
 *
 */
public class PHPStackFrame extends PHPDebugElement implements IStackFrame, Comparable {

	private PHPThread 		thread; 				// The thread to which this stackframe belongs
	private String 		file; 					// The file name???
	private int 			lineNumber;
	private int 			index;
	private int 			modno;
	private int 			scope_id; 				// scope id
	private Vector 		varList;				// Variables list
	private String 		description; 			// The source file name with the full path on target/remote system
	private boolean 		fUpToDate; 				// Indicates whether the variable list within this stackframe is
													// up-to-date
	private boolean 		fAvailable; 			// Needed when updating the stackframe list, shows whether the stackframe
													// is within the list which was received from dbg

	/**
	 *
	 * @param thread
	 * @param file
	 * @param line
	 * @param index
	 * @param desc
	 * @param modno
	 * @param scope_id
	 */
	public PHPStackFrame(PHPThread thread, String file, int line, int index,
			String desc, int modno, int scope_id) {
		super(null);

		this.lineNumber = line;
		this.index = index;
		this.file = file;
		this.thread = thread;
		this.description = desc;
		this.modno = modno;
		this.scope_id = scope_id;
		this.varList = new Vector();
		this.fUpToDate = false;
	}

//	/**
//	 *
//	 * @param thread
//	 * @param file
//	 * @param line
//	 * @param index
//	 */
//	public PHPStackFrame(PHPThread thread, String file, int line, int index) {
//		super(null);
//
//		this.lineNumber = line;
//		this.index = index;
//		this.file = file;
//		this.thread = thread;
//		this.fUpToDate = false;
//	}

	/**
	 * 
	 * @return scope id
	 */
	public int getScopeID() {
		return scope_id;
	}

	/**
	 * 
	 */
	public void setScopeID(int scope_id) {
		this.scope_id = scope_id;
		fUpToDate = false;
	}

	/**
	 *
	 */
	public IThread getThread() {
		return thread;
	}

	/**
	 * @param thread
	 */
	public void setThread(PHPThread thread) {
		this.thread = thread;
	}

//	/**
//	 *
//	 */
//	private void setUpToDate(boolean upToDate) {
//		fUpToDate = upToDate;
//	}

//	/**
//	 *
//	 */
//	private boolean isUpToDate() {
//		return fUpToDate;
//	}

	/**
	 *
	 */
	public void setAvailable(boolean available) {
		fAvailable = available;
	}

	/**
	 *
	 */
	public boolean isAvailable() {
		return fAvailable;
	}

	/**
	 * @see IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == PHPStackFrame.class) {
			return this;
		}

		return super.getAdapter(adapter);
	}

	/**
	 *
	 */
	private void resetHasChangedInfo(Vector varList) {
		int 		n;
		PHPVariable var;
		PHPValue 	val;

		for (n = 0; n < varList.size(); n++) { 						// For every variable in 'DBG list'
			var = (PHPVariable) varList.get(n); 					// Get the variable
			val = (PHPValue) var.getValue(); 						// Get the variable's value

			try {
				if (val.hasVariables()) { 							// Do we have other variables within the value
					if (!hasRecursion(var)) { 						// Is this variable (value) branch recursive?
						resetHasChangedInfo(val.getChildVariables()); //  No, go into branch
					}
				}
			} catch (DebugException e) { 							// That's, because of the hasVariables method
			}

			var.setValueChanged(false); 							// Reset the 'has changed' flag
		}
	}

	/**
	 * Go up the tree of PHPVariables
	 * look whether the PHPValue is a reference to a parent PHPValue
	 *
	 * TODO Check where this recursion can come from.
	 * Whether this back reference is legal or a bug.
	 * 
	 * Typically $GLOBALS contains $GLOBALS
	 *
	 * @param var
	 * @return
	 * <ul>
	 * <li> false if the PHPValue is not a child of itself
	 * <li> true if the PHPValue is
	 * </ul>
	 */
	private boolean hasRecursion(PHPVariable var) {
		PHPVariable parentVar;
		PHPValue 	val;

		val = (PHPValue) var.getValue(); 							// Get the PHPValue from the current PHPVariable

		while (var != null) { 										// As long as we have PHPVariable
			parentVar = var.getParent(); 							// Get the parent PHPVariable

			if (parentVar != null) { 								// Is there a parent?
				if (parentVar.getValue().equals(val)) { 			// Get the PHPValue for the parent PHPVariable and check
																	// whether it is the same
					return true; 									// Return, if we have recursion
				}
			}

			var = parentVar;
		}

		return false; 												// No recursion found
	}

	/**
	 * This method updates the 'static' variables list.
	 * It does a replication between the 'static' list (the variable list which
	 * is a member of this DBG interface object) and the DBG variable list
	 * (the list of variables which is received from PHP via DBG with the current suspend)
	 * Replication is done in the following way:
	 * <ul>
	 * <li> It looks for new variables within the DBG variables list and
	 *      adds them to the 'static' list.
	 * <li> It looks for changed variables copies the current value to the variable within
	 *		the 'static list' and mark these variables as 'hasChanged' (which uses the UI
	 *		for showing the variable with a different color).
	 * <li> It looks for variables within the 'static' list, and removes them
	 * 		from the 'static' list in case the do not appear within the DBG list.
	 * </ul>
	 *
	 * @param varListOld The 'static' list of variables which are to be updated.
	 * @param varListNew The new list of (current) variables from DBG.
	 */
	private void updateVariableList(Vector varListOld, Vector varListNew) {
		PHPVariable varOld; 										// The variable from the 'static' list
		PHPVariable varNew; 										// The variable from the DBG list
		PHPValue 	valOld; 										// The value of the current variable from 'static' list
		PHPValue 	valNew; 										// The value of the current variable from DBG list
		int 		n; 												// Index for the DBG list
		int 		o; 												// Index for the static list

		// Add the variables (and childs) to the static list if they are new
		//  and update the values of variables which are already existend within
		//  the 'static' list.

		for (n = 0; n < varListNew.size(); n++) { 					// For every variable in 'DBG list'
			varNew = (PHPVariable) varListNew.get(n); 				// Get the DBG variable

			for (o = 0; o < varListOld.size(); o++) { 				// For every variable in static list
				varOld = (PHPVariable) varListOld.get(o); 			// Get the static variable

				if (varNew.getName().equals(varOld.getName())) { 	// Did we found the variable within the 'static' list?
					valOld = (PHPValue) varOld.getValue(); 			// Get the value from 'static'
					valNew = (PHPValue) varNew.getValue(); 			// Get the value from DBG

					try {
						if (valOld.hasVariables() || 				// If the 'static' value has child variables
								valNew.hasVariables()) { 			//  or if the DBG value has child variables
							if (!hasRecursion(varOld) && !hasRecursion(varNew)) { // Both branches should not have a recursion
								updateVariableList(valOld.getChildVariables(), // Update the variable list for the child variables
										valNew.getChildVariables());
							}
						}
						if (!valOld.getValueString().equals(
								valNew.getValueString())) { 		// Has the value changed?
							valOld.setValueString(valNew.getValueString()); // Yes, set the 'static' value (variable) to the new value
							varOld.setValueChanged(true); 			// and set the 'has changed' flag, so that the variable view
																	// could show the user the changed status with a different
																	// color
						}
					} catch (DebugException e) { 					// That's, because of the hasVariables method
					}

					break; 											// Found the variable,
				}
			}

			if (o == varListOld.size()) { 							// Did we found the variable within the static list?
				varListOld.add(varNew); 							//  No, then add the DBG variable to the static list
			}
		}

		// Look for the variables we can remove from the 'static' list

		for (o = 0; o < varListOld.size(); o++) { 					// For every variable in 'static' list
			varOld = (PHPVariable) varListOld.get(o); 				// Get the static variable

			for (n = 0; n < varListNew.size(); n++) { 				// For all variables in 'DBG' list
				varNew = (PHPVariable) varListNew.get(n); 			// Get the variable from the 'DBG' list

				if (varNew.getName().equals(varOld.getName())) { 	// Did we found the 'static' list variable within the 'DBG' list?
					break; 											// Yes we found the variable, then leave the loop
				}
			}

			if (n == varListNew.size()) { 							// Did not find the 'static' list variable within the 'DBG' list?
				varListOld.remove(o--); 							// then remove the 'static' list variable from list
			}
		}
	}

	/**
	 *
	 * This function returns the array of PHPVariables for this stackframe
	 * The PHPVariables should not change (newly build up) between two steps
	 * (or breaks).
	 * A PHPVariable with the same name but with different object ID is
	 * handled as a new variable.
	 *
	 * @return The array of PHPVariables for this stackframe.
	 */
	public IVariable[] getVariables() throws DebugException {
		if (!fUpToDate) {
			resetHasChangedInfo(varList);
			updateVariableList(varList, this.getPHPDBGProxy().readVariables(this));
			fUpToDate = true;
			Collections.sort(varList, new PHPVariableComparator());
		}

		return (PHPVariable[]) varList.toArray(new PHPVariable[varList.size()]);
	}

	/**
	 *
	 */
	private PHPVariable findVariable(Vector varList, String varname) {
		PHPVariable variable;
		PHPValue 	value;
		int 		i;

		for (i = 0; i < varList.size(); i++) { 						// For all variables
			variable = (PHPVariable) varList.get(i); 				// Get the variable
			value = (PHPValue) variable.getValue(); 				// Get the value of the variable

			try {
				if (value.hasVariables()) { 						// Does the variable/value have children
					if (!hasRecursion(variable)) { 					// Don't follow recursive variable/values
						PHPVariable var = findVariable(value.getChildVariables(), varname);
						if (var != null) {
							return var;
						}
					}
				}
				if (variable.getName().equals(varname)) {
					return variable;
				}
			} catch (DebugException e) { 							// That's, because of the hasVariables method
			}
		}

		return null;
	}

	/**
	 * This method is called from the UI (e.g. from PHPDebugHover
	 * to find the variable the mouse is pointing to)
	 *
	 * @param s The variable name we are looking for.
	 * @return
	 */
	public IVariable findVariable(String s) throws DebugException {
		if (!fUpToDate) {
			getVariables();
		}

		return (findVariable(varList, s));							// Prefix the variable name with $
	}

	/**
	 *
	 */
	public boolean hasVariables() throws DebugException {
		if (!fUpToDate) {
			getVariables();
		}

		return (varList.size() > 0);
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int line) {
		lineNumber = line;
	}

	public int getCharStart() throws DebugException {
		// not supported
		return -1;
	}

	public int getCharEnd() throws DebugException {
		// not supported
		return -1;
	}

	public String getName() {
		StringBuffer name = new StringBuffer();

		if (!this.getDescription().equals("")) {
			name.append(this.getDescription());
		} else {
			name.append(this.getFileName());
		}

		name.append(" [line ");
		name.append(this.getLineNumber());
		name.append("]");

		return name.toString();
	}

	public String getFileName() {
		return file;
	}

	public void setDescription(String desc) {
		this.description = desc;
	}

	public String getDescription() {
		return this.description;
	}

	public IRegisterGroup[] getRegisterGroups() throws DebugException {
		return null;
	}

	public boolean hasRegisterGroups() throws DebugException {
		return false;
	}

	public String getModelIdentifier() {
		return this.getThread().getModelIdentifier();
	}

	public IDebugTarget getDebugTarget() {
		return this.getThread().getDebugTarget();
	}

	public ILaunch getLaunch() {
		return this.getDebugTarget().getLaunch();
	}

	public boolean canStepInto() {
		return canResume();
	}

	public boolean canStepOver() {
		return canResume();
	}

	public boolean canStepReturn() {
		return canResume();
	}

	public boolean isStepping() {
		return false;
	}

	/**
	 *
	 */
	public void stepInto() throws DebugException {
		fUpToDate = false;

		thread.prepareForResume(DebugEvent.STEP_INTO); 				// Don't know why, but this is necessary
		this.getPHPDBGProxy().readStepIntoEnd(PHPStackFrame.this);
		
        // Commented out sending the RESUME event because it was already sent by prepareForResume.
        // The second RESUME event leads only to a little flickering within the variables view.
        // It is also not clear why this event was necessary in eclipse < 3.2
        // Also sending a SUSPEND event here leads to a total rebuild of the variables view.
        // (eclipse 3.2 has a build in timeout of 500 ms which leads to a auto suspend, with
        // no flickering... but why???)
        // 
		//ev = new DebugEvent (this.getThread (), DebugEvent.RESUME, DebugEvent.STEP_INTO);
		//DebugPlugin.getDefault().fireDebugEventSet (new DebugEvent[] { ev });
	}

	/**
	 *
	 */
	public void stepOver() throws DebugException {
		fUpToDate = false;

		thread.prepareForResume(DebugEvent.STEP_OVER);
		this.getPHPDBGProxy().readStepOverEnd(PHPStackFrame.this);

        // See comment within the previous stepInto method.
        // 
		//ev = new DebugEvent (this.getThread (), DebugEvent.RESUME, DebugEvent.STEP_OVER);
		//DebugPlugin.getDefault ().fireDebugEventSet (new DebugEvent[] { ev });
	}

	/**
	 *
	 */
	public void stepReturn() throws DebugException {
		fUpToDate = false;

		thread.prepareForResume(DebugEvent.STEP_RETURN);
		this.getPHPDBGProxy().readStepReturnEnd(PHPStackFrame.this);

		//ev = new DebugEvent (this.getThread (), DebugEvent.RESUME, DebugEvent.STEP_RETURN);
		//DebugPlugin.getDefault ().fireDebugEventSet (new DebugEvent[] { ev });
	}

	public boolean canResume() {
		return this.getThread().canResume();
	}

	public boolean canSuspend() {
		return this.getThread().canSuspend();
	}

	public boolean isSuspended() {
		return this.getThread().isSuspended();
	}

	public void resume() throws DebugException {
		fUpToDate = false;

		this.getThread().resume();
	}

	public void suspend() throws DebugException {
	}

	public boolean canTerminate() {
		return this.getThread().canTerminate();
	}

	public boolean isTerminated() {
		return this.getThread().isTerminated();
	}

	public void terminate() throws DebugException {
		getPHPDBGProxy().stop();
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public PHPDBGProxy getPHPDBGProxy() {
		PHPDebugTarget DebugTarget;

		DebugTarget = (PHPDebugTarget) thread.getDebugTarget();

		return DebugTarget.getPHPDBGProxy();
	}

	public void setFile(String file) {
		this.file = file;

		final String COMPILED_EVAL = "eval()'d code";
		final String COMPILED_LAMBDA = "runtime-created function";

		int i = 0;
		if (file.endsWith(COMPILED_EVAL)) {
			i = file.length() - COMPILED_EVAL.length();
		} else if (file.endsWith(COMPILED_LAMBDA)) {
			i = file.length() - COMPILED_LAMBDA.length();
		}
		if (i > 0) {
			// assume COMPILED_STRING_DESCRIPTION_FORMAT
			// "filename(linenumber) : string"
			int j = i;
			while (--i > 0) {
				switch (file.charAt(i)) {
				case ')':
					j = i;
					break;
				case '(':
					this.file = file.substring(0, i);
					try {
						lineNumber = Integer.parseInt(file.substring(i + 1, j));
					} catch (NumberFormatException e) {
						PHPDebugCorePlugin.log(e);
					}
					return;
				}
			}
		}
	}

	public int getModNo() {
		return modno;
	}

	/**
	 * This function is needed when sorting the stackframes by their index numbers.
	 *
	 * @param obj The stackframe which this one is compared to.
	 * @return
	 * <ul>
	 * <li> -1 if the index of this stackframe is less.
	 * <li> 0 if the index of both stackframes are equal (should no happen).
	 * <li> 1 if the index of this stackframe is greater.
	 * </ul>
	 */
	public int compareTo(Object obj) {
		if (!(obj instanceof PHPStackFrame)) {
			throw new IllegalArgumentException("A PHPStackFrame can only be compared with another PHPStackFrame");
		}
		int frameIndex = ((PHPStackFrame) obj).getIndex();
		if (index < frameIndex) {
			return -1;
		} else if (index > frameIndex) {
			return 1;
		}
		return 0;
	}
}
