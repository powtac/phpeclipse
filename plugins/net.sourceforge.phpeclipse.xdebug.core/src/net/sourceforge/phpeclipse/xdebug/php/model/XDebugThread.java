/*
 * Created on 23.11.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.sourceforge.phpeclipse.xdebug.php.model;

import net.sourceforge.phpeclipse.xdebug.core.PHPDebugUtils;
import net.sourceforge.phpeclipse.xdebug.core.xdebug.XDebugResponse;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Axel
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class XDebugThread extends XDebugElement implements IThread, IDebugEventSetListener {
	private XDebugStackFrame[]  fStackFrames;
	
	private IBreakpoint[] fBreakpoints;
	
	/* Whether this thread is stepping */
	private boolean fStepping = false;
	private boolean fTerminated = false;
	
	private int fStepCount = 0;
	private int fCurrentStepCount = 0;
	
	/**
	 * Constructs a new thread for the given target
	 * 
	 * @param target VM
	 */
	public XDebugThread(XDebugTarget target) {
		super(target);
		DebugPlugin.getDefault().addDebugEventListener(this);
		fStackFrames = null;
	}
	
	public void incrementStepCounter() {
		fStepCount++;
	}
	public IStackFrame[] getStackFrames() throws DebugException {
		if (!isSuspended()) {
			return new IStackFrame[0];
		}
		
		if (fStepCount > fCurrentStepCount) {
			XDebugResponse dr = ((XDebugTarget) getDebugTarget()).getStackFrames();
			XDebugStackFrame[] newStackFrames = _getStackFrames(dr);

			/*if (fStackFrames != null) {
				if (newStackFrames.length >= fStackFrames.length) {
					int delta = newStackFrames.length - fStackFrames.length + 1;
					
					for (int i = fStackFrames.length - 1; i >= 0; i--) {
						if (fStackFrames[i].equals(newStackFrames[newStackFrames.length - delta])) {
							int b = 2; b++;
							//((XDebugStackFrame) newStackFrames[newStackFrames.length - delta]).evaluateChange((XDebugStackFrame) fStackFrames[i]);								
						} else if (fStackFrames[i].isSameStackFrame(newStackFrames[newStackFrames.length - delta])) {
							int b = 2; b++;
							//((XDebugStackFrame) newStackFrames[newStackFrames.length - delta]).evaluateChange((XDebugStackFrame) fStackFrames[i]);								
						}
						
						delta ++;
					}
				} else {
					fStackFrames = newStackFrames;
				}
			} else {
				fStackFrames = newStackFrames;
			}*/

			fCurrentStepCount++;

			fStackFrames = newStackFrames;
		}

		return fStackFrames;
	}
	
	
	private XDebugStackFrame[] _getStackFrames(XDebugResponse lastResponse) {
		if (lastResponse.isError()) {
			return new XDebugStackFrame[0];
		}
		
		Node response = lastResponse.getParentNode();
		NodeList frames = response.getChildNodes();
		XDebugStackFrame[] theFrames = new XDebugStackFrame[frames.getLength()];
		
		for (int i = 0; i < frames.getLength(); i++) {
			Node stackNode = frames.item(i);
			String fileName=PHPDebugUtils.unescapeString(PHPDebugUtils.getAttributeValue(stackNode,"filename"));
			String lineNo = PHPDebugUtils.getAttributeValue(stackNode,"lineno");
	
			XDebugStackFrame frame = new XDebugStackFrame(this/*fThread*/, i, /*type*/PHPDebugUtils.getAttributeValue(stackNode,"type"), /*lineno*/Integer.parseInt(lineNo), /*where*/PHPDebugUtils.getAttributeValue(stackNode,"where"), fileName);
			
			frame.incrementStepCounter();
			
			theFrames[i] = frame;
		}

		return theFrames;
	}
			
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#hasStackFrames()
	 */
	public boolean hasStackFrames() throws DebugException {
		return isSuspended();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getPriority()
	 */
	public int getPriority() throws DebugException {
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getTopStackFrame()
	 */
	public IStackFrame getTopStackFrame() throws DebugException {
		IStackFrame[] frames = getStackFrames();
		if (frames.length > 0) {
			return frames[0];
		}
		
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getName()
	 */
	public String getName() throws DebugException {
		return "Thread[1]";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getBreakpoints()
	 */
	public IBreakpoint[] getBreakpoints() {
		if (fBreakpoints == null) {
			return new IBreakpoint[0];
		}
		return fBreakpoints;
	}
	
	/**
	 * Sets the breakpoints this thread is suspended at, or <code>null</code>
	 * if none.
	 * 
	 * @param breakpoints the breakpoints this thread is suspended at, or <code>null</code>
	 * if none
	 */
	protected void setBreakpoints(IBreakpoint[] breakpoints) {
		fBreakpoints = breakpoints;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canResume()
	 */
	public boolean canResume() {
		return isSuspended();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
	 */
	public boolean canSuspend() {
		return !isTerminated() && !isSuspended();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
	 */
	public boolean isSuspended() {
		return getDebugTarget().isSuspended();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#resume()
	 */
	public void resume() throws DebugException {
		fBreakpoints = null;
		getDebugTarget().resume();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
	 */
	public void suspend() throws DebugException {
		getDebugTarget().suspend();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepInto()
	 */
	public boolean canStepInto() {
		return isSuspended();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepOver()
	 */
	public boolean canStepOver() {
		return isSuspended();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepReturn()
	 */
	public boolean canStepReturn() {
		if (fStackFrames != null) {
			return (fStackFrames.length > 1);
		} else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#isStepping()
	 */
	public boolean isStepping() {
		return fStepping;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepInto()
	 */
	public void stepInto() throws DebugException {
		fBreakpoints = null;
		((XDebugTarget) getDebugTarget()).step_into();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepOver()
	 */
	public void stepOver() throws DebugException {
		fBreakpoints = null;
		((XDebugTarget) getDebugTarget()).step_over();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepReturn()
	 */
	public void stepReturn() throws DebugException {
		fBreakpoints = null;
		((XDebugTarget) getDebugTarget()).step_out();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	public boolean canTerminate() {
		return !isTerminated();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	public boolean isTerminated() {
		return fTerminated;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	public void terminate() throws DebugException {
		((XDebugTarget) getDebugTarget()).getDebugConnection().stop();
		fTerminated = true;
	}
	
	public void terminated() throws DebugException {
		fTerminated = true;
	}

	/**
	 * Sets whether this thread is stepping
	 * 
	 * @param stepping whether stepping
	 */
	protected void setStepping(boolean stepping) {
		fStepping = stepping;
	}

	public void handleDebugEvents(DebugEvent[] events) {
		DebugEvent de = events[0];
		System.out.println(de.toString());	
	}

	public void removeEventListeners() {
		DebugPlugin.getDefault().removeDebugEventListener(this);
	}
	
	/**
	 * Fires a <code>RESUME</code> event for this element with
	 * the given detail.
	 * 
	 * @param detail event detail code
	 */
	public void fireResumeEvent(int detail) {
		fireEvent(new DebugEvent(this, DebugEvent.RESUME, detail));
	}

	/**
	 * Fires a <code>SUSPEND</code> event for this element with
	 * the given detail.
	 * 
	 * @param detail event detail code
	 */
	public void fireSuspendEvent(int detail) {
		fireEvent(new DebugEvent(this, DebugEvent.SUSPEND, detail));
	}
}