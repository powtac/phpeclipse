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

import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class PHPThread extends PHPDebugElement implements IThread {

	private PHPStackFrame[] frames;    	// The stackframes which belongs to this thread
	private PHPDebugTarget  target;     //
	private String 			name;       //
	private int 			id;         // The port number through which we communicate to DBG

	private class State {
		private boolean isSuspended  = false;
		private boolean isTerminated = false;
		private boolean isStepping 	 = false;

		boolean isSuspended () {
			return isSuspended;
		}

		boolean isTerminated () {
			return isTerminated;
		}

		boolean isStepping () {
			return isStepping;
		}

		void setSuspended (boolean suspended) {
			if (isTerminated ()) {
				throw new IllegalStateException();
			}

			if (suspended && isStepping ()) {
				throw new IllegalStateException ();
			}

			isSuspended = suspended;
		}

		void setStepping (boolean stepping) {
			if (stepping && !isSuspended ()) {
				throw new IllegalStateException ();
			}

			if (isTerminated ()) {
				throw new IllegalStateException ();
			}

			isStepping = stepping;
		}

		void setTerminated(boolean terminated) {
			isTerminated = terminated;
		}
	}

	private final State state = new State ();

	/**
	 * @param target
	 * @param id		The port number through which we communicate to DBG
	 */
	public PHPThread (PHPDebugTarget target, int id) {
	  	super (target);

		this.target = target;
		this.setId (id);
	}

	/**
	 *
	 */
	public IStackFrame[] getStackFrames () throws DebugException {
		if (isSuspended()) {
			return ((PHPDebugTarget)getDebugTarget()).getStackFrames();
		} else {
			return new IStackFrame[0];
		}
	}

	public int getStackFramesSize () {
		return frames.length;
	}

	public boolean hasStackFrames () {
		if (frames == null) {
			return false;
		}

		return frames.length > 0;
	}

	public int getPriority () throws DebugException {
		return 0;
	}

	public IStackFrame getTopStackFrame () throws DebugException {
		if (frames == null || frames.length == 0) {
			return null;
		}
		return (IStackFrame) frames[0];
	}

	public IBreakpoint[] getBreakpoints() {
		return new IBreakpoint[0];
	}

	public String getModelIdentifier() {
		return this.getDebugTarget().getModelIdentifier();
	}

	public IDebugTarget getDebugTarget() {
		return target;
	}

	public void setDebugTarget(PHPDebugTarget target) {
		this.target = target;
	}

	public ILaunch getLaunch() {
		return this.getDebugTarget().getLaunch();
	}

	public synchronized boolean canResume() {
		return isSuspended();
	}

	public synchronized boolean canSuspend() {
		return !isSuspended();
	}

	public synchronized boolean isSuspended() {
		return state.isSuspended;
	}

	/**
	 *
	 * Is called from PHPstackframe whenever a stepInto, stepOver or stepReturn is
	 * to be performed
	 *
	 * @param de
	 */
	protected void prepareForResume (int de) {
		DebugEvent ev;

		state.setSuspended (false);                                 // We will leave the suspended state
		this.frames = null;                                         // Reset the stackframes
		ev          = new DebugEvent (this, DebugEvent.RESUME, de); // Create an event resume by stepping

	  	DebugPlugin.getDefault ().fireDebugEventSet (new DebugEvent[] { ev });	// Fire the event
	}

	/**
	 *
	 */
	public synchronized void resume () throws DebugException {
		if (!isSuspended ()) {										// Is the thread in suspended state?
			return;													// No, leave here
		}

		this.prepareForResume (DebugEvent.STEP_OVER);               // Use a STEP_OVER here because a 0 leads to a collapsing variable tree in UI

		((PHPDebugTarget) this.getDebugTarget ()).getPHPDBGProxy ().resume ();
	}

	/*
	 * public void doSuspend(SuspensionPoint suspensionPoint) { //
	 * this.getPHPDebuggerProxy().readFrames(this);
	 * this.createName(suspensionPoint) ; this.suspend() ; }
	 */

	public synchronized void suspend () throws DebugException {
		DebugEvent ev;

		if (isSuspended ()) {   									// Is the thread in suspend state?
			return; 												// Yes, leave here
		}

		state.setSuspended (true);                                  // Set thread to suspended state
		state.setStepping (false);                                  // Reset thread from stepping state

		getDebugTarget ().suspend ();								//

		ev = new DebugEvent (this, DebugEvent.SUSPEND, DebugEvent.BREAKPOINT);

		DebugPlugin.getDefault ().fireDebugEventSet (new DebugEvent[] { ev });
	}

	/**
	 *
	 */
	public boolean canStepInto () {
		return isSuspended () &&                                    // Is the thread in suspended mode (stopped)
			   isStepping () &&                                     // and ???
			   this.hasStackFrames ();                              // and does this thread have stack frames?
	}

	/**
	 *
	 */
	public boolean canStepOver () {
		return isSuspended () &&                                    // Is the thread in suspended mode (stopped)
			   isStepping () &&                                     // and ???
			   this.hasStackFrames ();                              // and does this thread have stack frames?
	}

	/**
	 *
	 */
	public boolean canStepReturn () {
		return isSuspended () &&                                    // Is the thread in suspended mode (stopped)
			   isStepping () &&                                     // and ???
			   this.hasStackFrames ();                              // and does this thread have stack frames?
	}

	/**
	 *
	 */
	public boolean isStepping () {
		return state.isStepping ();
	}

	/**
	 *
	 */
	public void stepInto () throws DebugException {
		try {
			state.setStepping (true);                               // Store the info about what we do
		}
		catch (IllegalStateException x) {
			throw new DebugException (PHPeclipsePlugin.error (x));
		}

		this.frames = null;

		frames[0].stepInto ();
	}

	/**
	 *
	 */
	public void stepOver () throws DebugException {
		state.setStepping (true);

		this.frames = null;

		frames[0].stepOver ();
	}

	/**
	 *
	 */
	public void stepReturn () throws DebugException {
	}

	/**
	 *
	 */
	public boolean canTerminate () {
		return !isTerminated ();
	}

	/**
	 *
	 */
	public boolean isTerminated () {
		return state.isTerminated ();
	}

	/**
	 *
	 */
	public synchronized void terminate () throws DebugException {
		if (isTerminated ()) {
			return;
		}

		state.setTerminated (true);
		this.frames = null;
		getDebugTarget ().terminate ();
		fireTerminateEvent ();
	}

	/**
	 *
	 * @param arg0
	 * @return
	 */
	public Object getAdapter (Class arg0) {
		if (IWorkbenchAdapter.class.equals (arg0)) {
			return new IWorkbenchAdapter() {
				public Object[] getChildren(Object o) {
					try {
						return getStackFrames ();
					} catch (DebugException x) {
						PHPeclipsePlugin.log ("Unable to get stack frames.", x);
 					}

					return new Object[0];
				}

				public ImageDescriptor getImageDescriptor(Object object) {
					return null;
				}

				public String getLabel(Object o) {
					throw new UnsupportedOperationException();
				}

				public Object getParent(Object o) {
					return getDebugTarget();
				}
			};
		}
		return super.getAdapter(arg0);
	}

	/**
	 *
	 */
	public void setStackFrames(PHPStackFrame[] frames) {
		this.frames = frames;
	}

	/**
	 *
	 */
	public String getName () {
		String name;

		name = this.name;

		if (isSuspended ()) {
			name = name + " (suspended)";
		}

		return name;
	}

	public void setName (String name) {
		this.name = name;
	}

	/*
	 * protected void createName(SuspensionPoint suspensionPoint) { this.name =
	 * "PHP Thread - " + this.getId() ; if (suspensionPoint != null) { this.name += " (" +
	 * suspensionPoint + ")" ; } }
	 */

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
