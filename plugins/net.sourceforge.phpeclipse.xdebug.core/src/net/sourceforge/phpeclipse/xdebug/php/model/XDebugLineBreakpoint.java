/*
 * Created on 25.11.2004
 */
package net.sourceforge.phpeclipse.xdebug.php.model;


import java.util.HashMap;
import java.util.Map;

import net.sourceforge.phpeclipse.xdebug.php.launching.IXDebugConstants;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
//import org.eclipse.debug.core.model.Breakpoint;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
//import org.eclipse.debug.core.model.LineBreakpoint;


/**
 * @author Axel
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class XDebugLineBreakpoint  extends XDebugBreakpoint implements ILineBreakpoint /*extends LineBreakpoint*/ {

	/**
	 * Default constructor is required for the breakpoint manager
	 * to re-create persisted breakpoints. After instantiating a breakpoint,
	 * the <code>setMarker(...)</code> method is called to restore
	 * this breakpoint's attributes.
	 */
	private static final String XDEBUG_LINE_BREAKPOINT = "net.sourceforge.phpeclipse.xdebug.core.XDebugLineBreakpoint"; //$NON-NLS-1$

	/**
	 * Breakpoint attribute storing the fully qualified name of the type
	 * this breakpoint is located in.
	 * (value <code>"net.sourceforge.phpeclipse.debug.typeName"</code>). This attribute is a <code>String</code>.
	 */
	protected static final String TYPE_NAME = "net.sourceforge.phpeclipse.debug.typeName"; //$NON-NLS-1$		

//	public PHPLineBreakpoint() {
//	}
//
//	public PHPLineBreakpoint(IResource resource, int lineNumber, int charStart, int charEnd, int hitCount, boolean add, Map attributes) throws DebugException {
//		this(resource, lineNumber, charStart, charEnd, hitCount, add, attributes, PHP_LINE_BREAKPOINT);
//	}
//	
//	public PHPLineBreakpoint(IResource resource, int lineNumber, int hitCount, boolean add, Map attributes) throws DebugException {
//		this(resource, lineNumber, -1, -1, hitCount, add, attributes, PHP_LINE_BREAKPOINT);
//	}

	
	public static final String BREAKPOINT_ID ="XDebugLineBreakpointID";
	
	public XDebugLineBreakpoint() {
	}
	
	/**
	 * Constructs a line breakpoint on the given resource at the given
	 * line number. The line number is 1-based (i.e. the first line of a
	 * file is line number 1).
	 * 
	 * @param resource file on which to set the breakpoint
	 * @param lineNumber 1-based line number of the breakpoint
	 * @throws CoreException if unable to create the breakpoint
	 */
	public XDebugLineBreakpoint(final IResource resource, final int lineNumber) throws CoreException {
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
	
				// create the marker
				setMarker(resource.createMarker(XDEBUG_LINE_BREAKPOINT));

				// add attributes
				Map attributes = new HashMap(10);
				addLineBreakpointAttributes(attributes, getModelIdentifier(), true, lineNumber, -1, -1);

				// set attributes
				ensureMarker().setAttributes(attributes);
				
				// add to breakpoint manager if requested
				register(true);		
			}
		};
		run(getMarkerRule(resource), wr);

	}
	
	protected void register(boolean register) throws CoreException {
		if (register) {
			DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(this);
		} else {
			setRegistered(false);
		}
	}
	
	public void addLineBreakpointAttributes(Map attributes, String modelIdentifier, boolean enabled, int lineNumber, int charStart, int charEnd) {
		attributes.put(IBreakpoint.ID, modelIdentifier);
		attributes.put(IBreakpoint.ENABLED, new Boolean(enabled));
		attributes.put(IMarker.LINE_NUMBER, new Integer(lineNumber));
		if (charStart!=-1)
		{
			attributes.put(IMarker.CHAR_START, new Integer(charStart));
			attributes.put(IMarker.CHAR_END, new Integer(charEnd));
		}
		attributes.put(TYPE_NAME, "typeName");
		attributes.put(BREAKPOINT_ID,new Integer(-1));
	}		

	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IBreakpoint#getModelIdentifier()
	 */
	public String getModelIdentifier() {
		return IXDebugConstants.ID_PHP_DEBUG_MODEL;
	}
	
	public void setID(int id) throws CoreException {
		ensureMarker().setAttribute(BREAKPOINT_ID,id);
	}
	
	public int getID() throws CoreException	{
		return ensureMarker().getAttribute(BREAKPOINT_ID,-1);
	}
	
	public int getHitCount() throws CoreException {
		return ensureMarker().getAttribute(HIT_COUNT,-1);
		//return fHitCount;
	}

	public void setHitCount(int newHitCount) throws CoreException {
		ensureMarker().setAttribute(HIT_COUNT,newHitCount);
		//fHitCount = newHitCount;
	}
	/**
	 * @see ILineBreakpoint#getLineNumber()
	 */
	public int getLineNumber() throws CoreException {
		IMarker m = getMarker();
		if (m != null) {
			return m.getAttribute(IMarker.LINE_NUMBER, -1);
		}
		return -1;
	}

	/**
	 * @see ILineBreakpoint#getCharStart()
	 */
	public int getCharStart() throws CoreException {
		IMarker m = getMarker();
		if (m != null) {
			return m.getAttribute(IMarker.CHAR_START, -1);
		}
		return -1;
	}

	/**
	 * @see ILineBreakpoint#getCharEnd()
	 */
	public int getCharEnd() throws CoreException {
		IMarker m = getMarker();
		if (m != null) {
			return m.getAttribute(IMarker.CHAR_END, -1);
		}
		return -1;
	}
}