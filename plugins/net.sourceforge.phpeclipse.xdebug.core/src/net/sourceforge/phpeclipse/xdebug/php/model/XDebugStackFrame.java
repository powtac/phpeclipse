/*
 * Created on 23.11.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.sourceforge.phpeclipse.xdebug.php.model;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author PHPeclipse team
 * @author Axel
 *
 */
public class XDebugStackFrame  extends XDebugElement implements IStackFrame {
	private XDebugThread fThread;
	private URL fName;
	private int fLineNumber;
	private int fLevel;
	private String fType;
	private String fWhere;
	private IVariable[] fVariables;
	private int fStepCount = 0;
	
	/**
	 * Constructs a stack frame in the given thread with the given
	 * frame data.
	 * 
	 * @param thread
	 * @param data frame data
	 * @param id stack frame id (0 is the bottom of the stack)
	 */
	public XDebugStackFrame(XDebugThread thread, int id, String type, int lineNumber, String where, /*URL*/String filename) {
		super(/*thread == null ? null : */(XDebugTarget) thread.getDebugTarget());
		
		fLevel = id;
		fThread = thread;
		fType = type;
		fLineNumber = lineNumber;
		fWhere = where;
		
		try {
		fName = new URL(filename);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public void incrementStepCounter() {
		fStepCount++;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getThread()
	 */
	public IThread getThread() {
		return fThread;
	}
	
	public IVariable[] getVariables() throws DebugException {
		/* always read variables, poor performance
		 * but this fix bug #680.
		 * need to investigate on.
		 */
		
		//if (fVariables == null) {
			Node dfl = ((XDebugTarget) getDebugTarget()).getLocalVariables(fLevel);
			Node dfg = ((XDebugTarget) getDebugTarget()).getGlobalVariables(fLevel);
			parseVariable(dfl, dfg);
		//}

		return fVariables;
	}
	
	private void parseVariable(Node localVariables, Node globalVariables) throws DebugException {
		NodeList property = localVariables.getChildNodes();
		
		NodeList propertyGlobal = globalVariables.getChildNodes();
		
		fVariables = new IVariable[property.getLength() + propertyGlobal.getLength()];
		
		int length = property.getLength();
		for (int i = 0; i < length; i++) {
			XDebugVariable var = new XDebugVariable(this, property.item(i));
			fVariables[i] = var;
		}

		int globalLength = propertyGlobal.getLength();
		for (int k = 0; k < globalLength; k++) {
			XDebugVariable var = new XDebugVariable(this, propertyGlobal.item(k));
			fVariables[k + length] = var;
		}
	}
	
	/*public void evaluateChange(IStackFrame OldStackFrame) throws DebugException {
		IVariable[] OldVariable = ((XDebugStackFrame) OldStackFrame).getVariables();
		for (int i = 0; i < fVariables.length; i++) {
			((XDebugVariable) fVariables[i]).setChange(OldVariable[i]);
		}
	}*/
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#hasVariables()
	 */
	public boolean hasVariables() throws DebugException {
		/*return fVariables.length > 0;*/
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getLineNumber()
	 */
	public int getLineNumber() throws DebugException {
		return fLineNumber;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getCharStart()
	 */
	public int getCharStart() throws DebugException {
		return -1;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getCharEnd()
	 */
	public int getCharEnd() throws DebugException {
		return -1;
	}
	
	/* (non-Javadoc)fName
	 * @see org.eclipse.debug.core.model.IStackFrame#getName()
	 */
	public String getName() throws DebugException {
		return fName.toString()+"::"+fWhere+ " line: "+ fLineNumber;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#getRegisterGroups()
	 */
	public IRegisterGroup[] getRegisterGroups() throws DebugException {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStackFrame#hasRegisterGroups()
	 */
	public boolean hasRegisterGroups() throws DebugException {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepInto()
	 */
	public boolean canStepInto() {
		return fThread.canStepInto();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepOver()
	 */
	public boolean canStepOver() {
		return fThread.canStepOver();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepReturn()
	 */
	public boolean canStepReturn() {
		return fThread.canStepReturn();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#isStepping()
	 */
	public boolean isStepping() {
		return fThread.isStepping();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepInto()
	 */
	public void stepInto() throws DebugException {
		fThread.stepInto();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepOver()
	 */
	public void stepOver() throws DebugException {
		fThread.stepOver();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepReturn()
	 */
	public void stepReturn() throws DebugException {
		fThread.stepReturn();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canResume()
	 */
	public boolean canResume() {
		return fThread.canResume();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
	 */
	public boolean canSuspend() {
		return fThread.canSuspend();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
	 */
	public boolean isSuspended() {
		return fThread.isSuspended();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#resume()
	 */
	public void resume() throws DebugException {
		fThread.resume();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
	 */
	public void suspend() throws DebugException {
		fThread.suspend();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	public boolean canTerminate() {
		return fThread.canTerminate();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	public boolean isTerminated() {
		return fThread.isTerminated();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	public void terminate() throws DebugException {
		fThread.terminate();
	}
	
	/**
	 * Returns the name of the source file this stack frame is associated
	 * with.
	 * 
	 * @return the name of the source file this stack frame is associated
	 * with. If the file associated with this frame does not exists, it returns null.
	 */
	public String getSourceName() {
		if (fName == null) {
			return null;
		}
		IPath a = new Path(fName.getFile());
		return a.lastSegment();
	}

	public boolean isSameStackFrame(Object obj) {
		boolean isSameStackFrame = false;
		
		if (obj instanceof XDebugStackFrame) {
			XDebugStackFrame sf = (XDebugStackFrame)obj;
			isSameStackFrame = sf.getSourceName().equals(getSourceName()) &&
				sf.getType().equals(getType()) &&
				sf.getWhere().equals(getWhere()); //&&
		}

		return isSameStackFrame;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof XDebugStackFrame) {
			XDebugStackFrame sf = (XDebugStackFrame)obj;
			try {
				return sf.getSourceName().equals(new Path(fName.getFile()).lastSegment()) &&
					sf.getLineNumber() == fLineNumber &&
					sf.getLevel() == fLevel &&
					sf.getType().equals(fType) &&
					sf.getWhere().equals(fWhere);
			} catch (DebugException e) {
			}
		}

		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getSourceName().hashCode() + fLevel;
	}
	
	public URL getFullName() {
		return fName;
	}

	public int getLevel() {
		return fLevel;
	}

	public String getType() {
		return fType;
	}

	public String getWhere() {
		return fWhere;
	}

	public boolean setVariableValue(XDebugVariable variable, String expression)  throws DebugException {
		return ((XDebugTarget) getDebugTarget()).setVarValue("$" + variable.getName(), expression);
	}
	
	public Node eval(String expression) throws DebugException {
		return ((XDebugTarget) getDebugTarget()).eval(expression);
	}
}