/**
 * 
 */
package net.sourceforge.phpeclipse.xdebug.php.model;

//import java.io.ByteArrayInputStream;
//import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.phpeclipse.xdebug.core.IPHPDebugEvent;
import net.sourceforge.phpeclipse.xdebug.core.IProxyEventListener;
import net.sourceforge.phpeclipse.xdebug.core.IXDebugPreferenceConstants;
import net.sourceforge.phpeclipse.xdebug.core.PHPDebugUtils;
import net.sourceforge.phpeclipse.xdebug.core.PathMapItem;
import net.sourceforge.phpeclipse.xdebug.core.XDebugCorePlugin;
import net.sourceforge.phpeclipse.xdebug.core.XDebugProxy;
import net.sourceforge.phpeclipse.xdebug.php.launching.IXDebugConstants;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
//import org.xml.sax.SAXException;

import net.sourceforge.phpeclipse.xdebug.core.xdebug.ResponseListener;
import net.sourceforge.phpeclipse.xdebug.core.xdebug.XDebugConnection;
import net.sourceforge.phpeclipse.xdebug.core.xdebug.XDebugResponse;

/**
 * @author Christian
 *
 */
public class XDebugTarget extends XDebugElement implements IDebugTarget, IDebugEventSetListener, IProxyEventListener {
	private IProcess fProcess;
	
	private ILaunch fLaunch;
	
	private int fDebugPort;
	
	private boolean fSuspended = false;
	
	private boolean fTerminated = false;
	
	private XDebugThread fThread;
	private IThread[] fThreads;
	
	private XDebugConnection fDebugConnection;

	private ResponseListener fResponseListener;

	private String fIdeKey;


	/**
	 * Constructs a new debug target in the given launch and waits until
	 * someone with the ideKey connects to the Debugproxy
	 *  
	 * 
	 * @param launch containing launch
	 * @param process process of the interpreter
	 * @param ideKey 
	 * @exception CoreException if unable to connect to host
	 */	
	public XDebugTarget(ILaunch launch, IProcess process, String ideKey) throws CoreException {
		fLaunch = launch;
		fProcess = process;
		fDebugConnection = null;
		fThread = null;
		fThreads = new IThread[0];
		fIdeKey = ideKey;
		
		fDebugPort = XDebugCorePlugin.getDefault().getPreferenceStore().getInt(IXDebugPreferenceConstants.DEBUGPORT_PREFERENCE);		
		if (fDebugPort == 0) {
			fDebugPort = IXDebugPreferenceConstants.DEFAULT_DEBUGPORT;
		}
		
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
		DebugPlugin.getDefault().addDebugEventListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#getProcess()
	 */
	public IProcess getProcess() {
		return fProcess;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#getThreads()
	 */
	public IThread[] getThreads() throws DebugException {
		return fThreads;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#hasThreads()
	 */
	public boolean hasThreads() throws DebugException {
		return (fThreads.length > 0);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#getName()
	 */
	public String getName() throws DebugException {
		return "PHP XDebug Client at localhost:" + fDebugPort;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugTarget#supportsBreakpoint(org.eclipse.debug.core.model.IBreakpoint)
	 */
	public boolean supportsBreakpoint(IBreakpoint breakpoint) {
		if (breakpoint.getModelIdentifier().equals(IXDebugConstants.ID_PHP_DEBUG_MODEL)) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getDebugTarget()
	 */
	public IDebugTarget getDebugTarget() {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
	 */
	public ILaunch getLaunch() {
		return fLaunch;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	public boolean canTerminate() {
		if (getProcess()!=null)  // ther is no running Process in remote debugging
			return getProcess().canTerminate();
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	public boolean isTerminated() {
//		return getProcess().isTerminated();
		return fTerminated;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	public void terminate() throws DebugException {
		if(fTerminated) {
			return;
		}
		
		if (XDebugCorePlugin.getDefault() != null) {
			XDebugProxy proxy = XDebugCorePlugin.getDefault().getXDebugProxy();
			proxy.removeProxyEventListener(this, fIdeKey);
			
			System.out.println("XDebug.Target: ProxyEventlistener removed");
			
			fTerminated = true;
			fSuspended = false;
			
			XDebugCorePlugin.getBreakpointManager().removeBreakpointListener(this);
			fireEvent(new DebugEvent(this, DebugEvent.TERMINATE));
			DebugPlugin.getDefault().removeDebugEventListener(this);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canResume()
	 */
	public boolean canResume() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
	 */
	public boolean canSuspend() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
	 */
	public boolean isSuspended() {
		return fSuspended;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#resume()
	 */
	public void resume() throws DebugException {
		if (fDebugConnection != null) {
			fThread.setBreakpoints(null);
			resumed(DebugEvent.RESUME);
			fDebugConnection.run();
		}		
	}
	
	/**
	 * Notification the target has resumed for the given reason
	 * 
	 * @param detail reason for the resume
	 */
	private void resumed(int detail) {
		fSuspended = false;
		fThread.fireResumeEvent(detail);
	}
	
	/**
	 * Notification the target has suspended for the given reason
	 * 
	 * @param detail reason for the suspend
	 */
	public void suspended(int detail) {
		fSuspended = true;
		fThread.fireSuspendEvent(detail);
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
	 */
	public void suspend() throws DebugException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointAdded(org.eclipse.debug.core.model.IBreakpoint)
	 */
	public void breakpointAdded(IBreakpoint breakpoint) {
		IMarker marker = breakpoint.getMarker();
		IPath path = marker.getResource().getLocation();
		IPath cp = path.removeLastSegments(1);
		List pathMap = null;
		try {
			pathMap = fLaunch.getLaunchConfiguration().getAttribute(IXDebugConstants.ATTR_PHP_PATHMAP,(List)null);
		} catch (CoreException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		if (fDebugConnection != null)
		if (!fDebugConnection.isClosed()) {
			if (fProcess == null) {
				PathMapItem pmi = null;
				for (int i = 0; i < pathMap.size(); i++) {
					pmi = new PathMapItem((String) pathMap.get(i));
					IPath local = (IPath)pmi.getLocalPath().clone();
					local = local.makeAbsolute();
					int matchedSegments = local.segmentCount();
					if (local.matchingFirstSegments(cp) == matchedSegments) {
						IPath newPath = pmi.getRemotePath();
						newPath = newPath.append(path.removeFirstSegments(matchedSegments));
						newPath = newPath.makeAbsolute();
						if (supportsBreakpoint(breakpoint)) {
							try {
								if (breakpoint.isEnabled()) {
									if (marker != null) {
										int id = fDebugConnection.breakpointSet(newPath.toString(), ((ILineBreakpoint)breakpoint).getLineNumber(), marker.getAttribute(XDebugBreakpoint.HIT_COUNT,-1));
										XDebugResponse dr = getResponse(id);
										
										String bpid = dr.getAttributeValue("id");
										
										if (!"".equals(bpid))
											marker.setAttribute(XDebugLineBreakpoint.BREAKPOINT_ID,Integer.parseInt(bpid));
									}
								}
							} catch (DebugException e) {
								e.printStackTrace();
							} catch (CoreException e) {
								e.printStackTrace();
							}
						}
					}
				}			
			} else {
				if (supportsBreakpoint(breakpoint)) {
					try {
						if (breakpoint.isEnabled()) {
							if (marker != null) {
								int id = fDebugConnection.breakpointSet(path.toString(), ((ILineBreakpoint)breakpoint).getLineNumber(), marker.getAttribute(XDebugBreakpoint.HIT_COUNT,-1));
								XDebugResponse dr = getResponse(id);
								String bpid = dr.getAttributeValue("id");
								
								if (!"".equals(bpid))
									marker.setAttribute(XDebugLineBreakpoint.BREAKPOINT_ID,Integer.parseInt(bpid));
							}
						}
					} catch (DebugException e) {
						e.printStackTrace();
					} catch (CoreException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointRemoved(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
	 */
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		if (supportsBreakpoint(breakpoint)) {
			try {
				int id =((XDebugLineBreakpoint)breakpoint).getID();
				if (id >0)
					fDebugConnection.breakpointRemove(id);
			} catch (CoreException e) {
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointChanged(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
	 */
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
//		if (supportsBreakpoint(breakpoint)) {
//			try {
//				if (breakpoint.isEnabled()) {
//					breakpointAdded(breakpoint);
//				} else {
//					breakpointRemoved(breakpoint, null);
//				}
//			} catch (CoreException e) {
//			}
//		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDisconnect#canDisconnect()
	 */
	public boolean canDisconnect() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDisconnect#disconnect()
	 */
	public void disconnect() throws DebugException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDisconnect#isDisconnected()
	 */
	public boolean isDisconnected() {
		return (false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockRetrieval#supportsStorageRetrieval()
	 */
	public boolean supportsStorageRetrieval() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlockRetrieval#getMemoryBlock(long, long)
	 */
	public IMemoryBlock getMemoryBlock(long startAddress, long length) throws DebugException {
		return null;
	}

	/**
	 * Notification we have connected to the PHP debugger and it has been started.
	 * Resume the the debugger.
	 */
	public void started() throws DebugException {
		fThread.setBreakpoints(null);
		fThread.setStepping(false);

		int id = fDebugConnection.featureGet("detach");

		XDebugResponse response = getResponse(id);

		Integer.parseInt(response.getValue());
		System.out.println("in Target.started()");

		// Dirty hack
		// Need to refactory plugin to get variables in lazy mode.
		int id1 = fDebugConnection.featureSet("max_depth", "1024" );
		XDebugResponse response1 = getResponse(id1);
		if (response1.getAttributeValue("success").equals("1") ) {
			System.out.println("Set depth to 1024 (hack)");
		}
		int id2 = fDebugConnection.featureSet("max_children", "1024" );
		XDebugResponse response2 = getResponse(id2);
		if (response2.getAttributeValue("success").equals("1") ) {
			System.out.println("Set children to 1024 (hack)");
		}
		
		installDeferredBreakpoints();
		try {
			resume();
		} catch (DebugException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Install breakpoints that are already registered with the breakpoint
	 * manager.
	 */
	private void installDeferredBreakpoints() {
		IBreakpoint[] breakpoints = XDebugCorePlugin.getBreakpoints();
		for (int i = 0; i < breakpoints.length; i++) {
			breakpointAdded(breakpoints[i]);
		}
	}
	
	/**
	 * Returns the current stack frames in the target.
	 * 
	 * @return the current stack frames in the target
	 * @throws DebugException if unable to perform the request
	 */
	public XDebugResponse getStackFrames() throws DebugException {
		int id = fDebugConnection.stackGet();
		XDebugResponse lastResponse = getResponse(id);
		return lastResponse;
	}
	
	/**
	 * Single step the interpreter.
	 * 
	 * @throws DebugException if the request fails
	 */
	protected void step_over() throws DebugException {
		fThread.setStepping(true);
		resumed(DebugEvent.STEP_OVER);
		fDebugConnection.stepOver();
	}
	
	/**
	 * Single step the interpreter.
	 * 
	 * @throws DebugException if the request fails
	 */
	protected void step_into() throws DebugException {
		fThread.setStepping(true);
		resumed(DebugEvent.STEP_INTO);
		fDebugConnection.stepInto();
	}
	
	/**
	 * Single step the interpreter.
	 * 
	 * @throws DebugException if the request fails
	 */
	protected void step_out() throws DebugException {
		fThread.setStepping(true);
		resumed(DebugEvent.STEP_RETURN);
		fDebugConnection.stepOut();
	}
	
	public boolean setVarValue(String name, String value) {
		int id = fDebugConnection.setVarValue(name,value);
		XDebugResponse response = getResponse(id);
		
		if ((response.getAttributeValue("success")).equals("1")) {
			return true;
		} else {
			return false;
		}
	}
	
	public Node eval(String expression) throws DebugException {
		Node evalProperty = null;
		if (fDebugConnection != null) {
			int id = fDebugConnection.eval(expression);
			//Node evalProperty = new Node("");
			//if (id > 0) {
				XDebugResponse response = getResponse(id);
		
				Node evalResponse = response.getParentNode();
				/*Node*/ evalProperty = evalResponse.getFirstChild();
			//} /*else {
				
			//}*/
		} else {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = null;
			Document doc = null;
			
			try {
				builder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
			//try {
				doc =  builder.newDocument(); // .parse("");
				evalProperty = doc.createElement("value");
			/*} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}*/
		}
		
		return evalProperty;
	}
	
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			
			if (fResponseListener != null) {
				Object s = null;
				s = event.getSource();
				if (s instanceof ResponseListener) {
					if (!fResponseListener.equals((ResponseListener) s)) {
						return;
					}
				}
			} else {
				return;
			}
			
			if (event.getKind() == DebugEvent.MODEL_SPECIFIC) {
				switch (event.getDetail()) {
					case IPHPDebugEvent.BREAKPOINT_HIT:
						int id = fDebugConnection.stackGet();
						XDebugResponse lastResponse = getResponse(id);

						IBreakpoint breakpoint = breakpointHit(lastResponse.getParentNode());
						
						if (breakpoint != null) {
							fThread.setBreakpoints(new IBreakpoint[]{breakpoint});
							fThread.incrementStepCounter();
							suspended(DebugEvent.BREAKPOINT);
						} else {
							try {
								resume();
							} catch (DebugException e ) {
								; //nothing to do
							}
						}
						break;
					case IPHPDebugEvent.STEP_END:
						fThread.incrementStepCounter();
						suspended(DebugEvent.STEP_END);
						break;
					case IPHPDebugEvent.STOPPED:
						stopped();
						break;
				}
			}
		}
	}
	
	public void stopped() {
		if(fDebugConnection == null) {
			return;
		}

		resumed(DebugEvent.TERMINATE);

		stopListener();
		fDebugConnection.close();

		fSuspended = false;

		// Dirty hack to check debugging mode (remote or local)
		if (fProcess != null) {
			try {
				terminate();
			} catch (DebugException e) {
				e.printStackTrace();
			}
		} else {
			fDebugConnection = null;
			fireEvent(new DebugEvent(this, DebugEvent.CHANGE, DebugEvent.CONTENT));
		}
		
		fThread.removeEventListeners();
		fThread = null;
		fThreads = new IThread[0];
	}
	
	public void handleProxyEvent(XDebugConnection connection) {
		//System.out.println("* New Connection - XDebug.Target: " + fDebugConnection.getSessionID());
		
		if (setDebugConnection(connection)) {
			fThread = new XDebugThread(this);
			fThreads = new IThread[] {fThread};
			fireEvent(new DebugEvent(this, DebugEvent.CHANGE, DebugEvent.CHANGE));
			try {
				started();
			} catch( DebugException e ){
				e.printStackTrace();		
			}
		}
	}

	private boolean setDebugConnection(XDebugConnection connection) {
		if (connection != null && fDebugConnection == null) {
			fDebugConnection = connection;
			fResponseListener = new ResponseListener(connection);
			startListener();
			
			return true;
		} else {
			connection.close();
			
			return false;
		}
	}
	
	/**
	 * @return Returns the fDebugConnection.
	 */
	public XDebugConnection getDebugConnection() {
		return fDebugConnection;
	}	
	
	public void addProcess(IProcess p) {
		fProcess = p;

	}
	public Node getLocalVariables(int level) throws DebugException {
		int id = fDebugConnection.contextGet(level, 0);
		XDebugResponse response = getResponse(id);
		
		return response.getParentNode();
	}
	
	public Node getGlobalVariables(int level) throws DebugException {
		int id = fDebugConnection.contextGet(level, 1);
		XDebugResponse response = getResponse(id);
		
		return response.getParentNode();
	}
	
	public void stop() {
		fDebugConnection.stop();
	}
	
	protected IBreakpoint breakpointHit(Node node) {
		Node child = node.getFirstChild();
		if (child.getNodeName().equals("stack")) {
			int lineNumber = Integer.parseInt(PHPDebugUtils.getAttributeValue(child, "lineno"));
			String filename = PHPDebugUtils.getAttributeValue(child, "filename");  
			IBreakpoint[] breakpoints = XDebugCorePlugin.getBreakpoints();
			for (int i = 0; i < breakpoints.length; i++) {
				IBreakpoint breakpoint = breakpoints[i];
				if (supportsBreakpoint(breakpoint)) {
					if (breakpoint instanceof ILineBreakpoint) {
						ILineBreakpoint lineBreakpoint = (ILineBreakpoint) breakpoint;
						try {						
							if (breakpoint.isEnabled()) {
								IMarker marker = breakpoint.getMarker();
								if (marker != null) {
									String endfilename;
									
									if (getProcess() == null) {
										endfilename = marker.getResource().getLocation().lastSegment(); 
									} else {
										endfilename = marker.getResource().getLocation().toOSString();
									}
									
									int id = fDebugConnection.breakpointGet(marker.getAttribute(XDebugLineBreakpoint.BREAKPOINT_ID,-1));
									XDebugResponse dr = getResponse(id);
									
									Node hitCo = dr.getParentNode().getFirstChild();
									int hitCount = 0;
									if (hitCo.hasAttributes()) {
										NamedNodeMap listAttribute = hitCo.getAttributes();
										Node attribute = listAttribute.getNamedItem("hit_count");
										if (attribute !=null) {
											hitCount = Integer.parseInt(attribute.getNodeValue());
										}
									}

									if(PHPDebugUtils.unescapeString(filename).endsWith(endfilename)
											&& (lineBreakpoint.getLineNumber() == lineNumber) ) {
										if (marker.getAttribute(XDebugLineBreakpoint.HIT_COUNT, 0) > 0) {
											if (marker.getAttribute(XDebugLineBreakpoint.HIT_COUNT, 0) == hitCount) {
												return (breakpoint);												
											}
										} else {
											return (breakpoint);
										}
									}
								}
							}
						} catch (CoreException e) {
						}
					}
				}
			}
		}
		
		return null;
	}
	
	public void startListener() {
		fResponseListener.schedule();
	}
	
	public void stopListener() {
		fResponseListener.cancel();
	}
	public XDebugResponse getResponse(int id) {
		XDebugResponse response = fResponseListener.getResponse(id);

		return response;
	}
}