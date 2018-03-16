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

import net.sourceforge.phpdt.internal.debug.core.PHPDBGProxy;
import net.sourceforge.phpdt.internal.debug.core.PHPDebugCorePlugin;
import net.sourceforge.phpdt.internal.debug.core.breakpoints.PHPLineBreakpoint;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Debug target for PHP debug model.
 */
public class PHPDebugTarget extends PHPDebugElement implements IPHPDebugTarget, ILaunchListener,
		IDebugEventSetListener {

	private IProcess process;

	private ILaunch launch;

	private PHPThread[] threads = new PHPThread[0];

	private PHPDBGProxy phpDBGProxy;

	private class State {
		private boolean isTerminated = false;

		private boolean isSuspended = false;

		boolean isTerminated() {
			return isTerminated;
		}

		boolean isSuspended() {
			return isSuspended;
		}

		void setTerminated(boolean terminated) {
			this.isTerminated = terminated;
		}

		void setSuspended(boolean suspended) {
			if (isTerminated())
				throw new IllegalStateException();
			this.isSuspended = suspended;
		}
	}

	private final State state = new State();

	public PHPDebugTarget(ILaunch launch, IProcess process) {
		super (null);
		if (null == launch && null == process)
			throw new IllegalArgumentException();
		this.launch = launch;
		this.process = process;
		// TODO XXX remove breakpoint listener at termination to avoid live leak
		IBreakpointManager manager = DebugPlugin.getDefault()
				.getBreakpointManager();
		manager.addBreakpointListener(this);
		DebugPlugin.getDefault().addDebugEventListener(this);
		initialize();
	}

	protected synchronized void initialize() {
		DebugEvent ev = new DebugEvent(this, DebugEvent.CREATE);
		DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] { ev });
	}

	public void addThread(PHPThread phpThread) {
		int i;
		PHPThread[] updatedThreads = new PHPThread[threads.length + 1];

		for (i = 0; i < threads.length; i++) {
			updatedThreads[i] = threads[i];
		}
		updatedThreads[i] = phpThread;
		threads = updatedThreads;

		fireChangeEvent();
		fireThreadCreateEvent(phpThread);
	}
	
	public void updateThreads(PHPThread phpThread) {
		fireChangeEvent();
		fireThreadCreateEvent(phpThread);
	}

	private void fireChangeEvent() {
		DebugEvent ev = new DebugEvent(this, DebugEvent.CHANGE);
		DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] { ev });
	}

	private void fireThreadCreateEvent(PHPThread phpThread) {
		DebugEvent ev = new DebugEvent(phpThread, DebugEvent.CREATE);
		DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] { ev });
	}

	protected PHPThread getThreadById(int id) {
		for (int i = 0; i < threads.length; i++) {
			if (threads[i].getId() == id) {
				return threads[i];
			}
		}
		return null;
	}

	public IThread[] getThreads() {
		return threads;
	}

	public boolean hasThreads() throws DebugException {
		return threads.length > 0;
	}

	public String getName() throws DebugException {
		return "PHP Debugger at localhost:" + getPHPDBGProxy().getPort();
	}

	public boolean supportsBreakpoint(IBreakpoint arg0) {
		if (arg0.getModelIdentifier().equals(PHPDebugCorePlugin.PLUGIN_ID)) {
			return true;
		}
		return false;
	}

	public String getModelIdentifier() {
		return PHPDebugCorePlugin.PLUGIN_ID;
	}

	public IStackFrame[] getStackFrames () throws DebugException {
		return (IStackFrame[]) this.phpDBGProxy.getDBGInterface ().getStackList ();
	}

	public IDebugTarget getDebugTarget() {
		return this;
	}

	public ILaunch getLaunch() {
		return launch;
	}

	public synchronized boolean canTerminate() {
		return !isTerminated();
	}

	public synchronized boolean isTerminated() {
		return state.isTerminated();
	}
	
	private synchronized void terminateThreads () {
		int i;
		
		try {
			for (i = 0; i < threads.length; i++) {
				threads[i].terminate ();
			}
		} catch (DebugException e) {
	 	}
	}

	public synchronized void terminate() {
		// This method is synchronized to control a race condition between the
		// UI thread that terminates the debugging session, and the slave
		// thread that executes PHPLoop.run
		if (isTerminated())
			// Avoid terminating twice...
			return;
		state.setTerminated(true);
		phpDBGProxy.stop();
		terminateThreads ();
		this.threads = new PHPThread[0];
		fireChangeEvent();
		IBreakpointManager manager = DebugPlugin.getDefault()
				.getBreakpointManager();
		manager.removeBreakpointListener(this);
		DebugPlugin.getDefault().removeDebugEventListener(this);
	}

	public synchronized boolean canResume() {
		if (isTerminated())
			return false;
		return isSuspended();
	}

	public synchronized boolean canSuspend() {
		if (isTerminated())
			return false;
		return !isSuspended();
	}

	public synchronized boolean isSuspended() {
		return state.isSuspended();
	}

	public synchronized void resume() throws DebugException {
		if (!isSuspended())
			return;
		state.setSuspended(false);
		this.getPHPDBGProxy().resume();
		IThread[] threads = getThreads();
		for (int i = 0; i < threads.length; ++i)
			threads[i].resume();
	}

	public synchronized void suspend() throws DebugException {
		if (isSuspended())
			return;
		this.getPHPDBGProxy().pause();
		state.setSuspended(true);
		IThread[] threads = getThreads();
		for (int i = 0; i < threads.length; ++i)
			threads[i].suspend();
	}

	public void breakpointAdded(IBreakpoint breakpoint) {
		this.getPHPDBGProxy().addBreakpoint(breakpoint);
	}

	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta arg1) {
		this.getPHPDBGProxy().removeBreakpoint(breakpoint);
	}

	/**
	 * The method will be called when the user enables/disables
	 * breakpoints. In this case we add or remove the breakpoint.
	 * It's also called when leaving the breakpoint properties dialog
	 * (skip count and breakpoint condition) with the OK button.
	 *
	 * This method is also called whenever a source file has changed.
	 * In this case we terminate since the source will be out of sync with the debugger.
	 * TODO Is it correct to call this method when a sourcefile is modified?
	 *
	 */
	public void breakpointChanged (IBreakpoint breakpoint, IMarkerDelta arg1) {
		PHPLineBreakpoint bp;
		bp = (PHPLineBreakpoint) breakpoint;

		try {
			if (breakpoint.isEnabled ()	&&									// Check if breakpoint state changed from disabled to enabled
				!arg1.getAttribute ("org.eclipse.debug.core.enabled", false)) {
				this.getPHPDBGProxy().addBreakpoint(breakpoint);
			}
			else if (!breakpoint.isEnabled () &&							// Check if breakpoint state changed from enabled to disabled
			    arg1.getAttribute ("org.eclipse.debug.core.enabled", true)) {
				this.getPHPDBGProxy().removeBreakpoint(breakpoint);
			}
			else if (bp.getChangeID() != arg1.getAttribute ("net.sourceforge.phpeclipse.debug.changeID", 0)) {
				if (breakpoint.isEnabled()) {								// If the breakpoint is already enabled
					this.getPHPDBGProxy().removeBreakpoint(breakpoint);		// we remove this breakpoint first
					this.getPHPDBGProxy().addBreakpoint(breakpoint);		// and then we add again (else DBG would have two breakpoints!).
				}
				else {
					this.getPHPDBGProxy().removeBreakpoint(breakpoint);
				}
			}
			else {															// All other cases will terminate the debugger
				terminate ();
			}
		} catch (CoreException e) {
			// Do nothing
		}
	}

	public boolean canDisconnect() {
		return false;
	}

	public void disconnect() throws DebugException {
	}

	public boolean isDisconnected() {
		return false;
	}

	public boolean supportsStorageRetrieval() {
		return false;
	}

	public IMemoryBlock getMemoryBlock(long arg0, long arg1)
			throws DebugException {
		return null;
	}

	public Object getAdapter(Class arg0) {
		if (IWorkbenchAdapter.class.equals(arg0)) {
			return new IWorkbenchAdapter() {
				public Object[] getChildren(Object o) {
					Object[] children = null;
					IThread[] threads = getThreads();
					if (null != threads) {
						children = new Object[threads.length];
						for (int i = 0; i < threads.length; ++i)
							children[i] = threads[i];
					}
					return children;
				}

				public ImageDescriptor getImageDescriptor(Object object) {
					return null;
				}

				public String getLabel(Object o) {
					String label = "(Unable to look up name... check error log)";
					try {
						label = getName();
					} catch (DebugException x) {
						PHPeclipsePlugin.log(label, x);
					}
					return label;
				}

				public Object getParent(Object o) {
					return PHPDebugTarget.this.getLaunch();
				}
			};
		}
		else {
		    if (arg0 == PHPDebugElement.class) {
		    	return this;
		    }

		    return super.getAdapter(arg0);
		}
	}

	public IProcess getProcess() {
		return process;
	}

	public void setProcess(IProcess process) {
		this.process = process;
	}

	public PHPDBGProxy getPHPDBGProxy() {
		return phpDBGProxy;
	}

	public void setPHPDBGProxy(PHPDBGProxy phpDBGProxy) {
		this.phpDBGProxy = phpDBGProxy;
	}

	/**
	 * @see ILaunchListener#launchRemoved(ILaunch)
	 */
	public void launchRemoved(ILaunch launch) {
		if (!isTerminated()) {
			return;
		}
		if (launch.equals(getLaunch())) {
			// This target has been deregistered, but it hasn't successfully
			// terminated.
			// Update internal state to reflect that it is disconnected
			terminate();
		}
	}

	/**
	 * @see ILaunchListener#launchAdded(ILaunch)
	 */
	public void launchAdded(ILaunch launch) {
	}

	/**
	 * @see ILaunchListener#launchChanged(ILaunch)
	 */
	public void launchChanged(ILaunch launch) {
	}

	/**
	 * When a debug target or process terminates, terminate DBG Proxy.
	 *
	 * @see IDebugEventSetListener#handleDebugEvents(DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			if (event.getKind() == DebugEvent.TERMINATE) {
				Object source = event.getSource();
				if (source instanceof PHPDebugTarget
						|| source instanceof IDebugTarget) {
					getPHPDBGProxy().stop();
				} else if (source instanceof IProcess) {
					if (getDebugTarget().getProcess() == (IProcess) source) {
						getPHPDBGProxy().stop();
					}
				}
			} else if (event.getKind() == DebugEvent.SUSPEND) {
				getPHPDBGProxy().pause();
			}
		}
	}
}
