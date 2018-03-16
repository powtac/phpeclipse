/***********************************************************************************************************************************
 * Copyright (c) 2000, 2002 IBM Corp. and others. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors: IBM Corporation - Initial implementation Vicente Fernando - www.alfersoft.com.ar Christian Perkonig - remote debug
 **********************************************************************************************************************************/
package net.sourceforge.phpdt.internal.debug.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.Vector;

import net.sourceforge.phpdt.internal.core.JavaProject;
import net.sourceforge.phpdt.internal.debug.core.breakpoints.PHPLineBreakpoint;
import net.sourceforge.phpdt.internal.debug.core.model.PHPDebugTarget;
import net.sourceforge.phpdt.internal.debug.core.model.PHPStackFrame;
import net.sourceforge.phpdt.internal.debug.core.model.PHPThread;
import net.sourceforge.phpdt.internal.debug.core.model.PHPVariable;
import net.sourceforge.phpeclipse.PHPeclipsePlugin;
import net.sourceforge.phpeclipse.actions.PHPEclipseShowAction;

import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.swt.browser.Browser;

public class PHPDBGProxy {

  	private ServerSocket 		server 		= null;
  	private BufferedReader 		reader 		= null;
  	private PHPDBGInterface 	DBGInt 		= null;		// The DBG interface which is linked with the proxy
  	private PHPDebugTarget 		debugTarget = null;
  	private PHPDBGProxy 		thisProxy 	= null;
  	private PHPLoop 			phpLoop;
  	private PHPThread 			PHPMainThread;
  	private Socket 				socket;
  	private int 				port;
  	private boolean 			remote;
  	private boolean 			pathtranslation;
  	private Map 				pathmap;
  	private IPath 				remoteSourcePath;

  	/**
  	 */
  	public PHPDBGProxy () {
  	  thisProxy = this;
 	}
  	
  	/**
  	 * updateView
  	 * Clean up the view, but leave the Debug session running.
  	 * added by ed_mann
  	 */
  	public void updateView(){
  	  getDebugTarget().updateThreads(PHPMainThread);
  	  	}
  	
  	/**
  	 * @param remote
  	 * @param remoteSourcePath
  	 * @param pathTranslate
  	 * @param paths
  	 */
  	public PHPDBGProxy (boolean remote, String remoteSourcePath, boolean pathTranslate, Map paths) {
  	  	thisProxy             = this;
  	  	this.remote 		  = remote;
  	  	this.remoteSourcePath = new Path (remoteSourcePath);
  	  	this.pathmap          = paths;
  	  	this.pathtranslation  = pathTranslate;
  	}

  	/**
  	 *
  	 */
  	public void start () {
  	  	createServerSocket ();                                      // Create a server socket for communicatio with DBG

  	  	this.startPHPLoop (); 										//
  	}

  	/**
  	 *
  	 */
  	public void stop () {
  	  	phpLoop.setShouldStop ();                                   // Notify the thread's 'run loop' to stop
  	  	if (DBGInt != null) {                                       // If we have a DBG interface linked with this proxy
  	    	DBGInt.setShouldStop ();                                //  Notify the DBG interface to stop the waiting for response
  	  	}

//  	  	if (!remote) {                                              // If it's not a remote proxy session
//  	    	try {
//  	      		getDebugTarget ().getProcess ().terminate ();       //
//  	    	} catch (DebugException e) {
//  	      		e.printStackTrace ();
//  	    	}
//  	  	}

  	  	phpLoop.notifyWait ();
  	}
  	
  	public void setTerminated () {
  		try {
  			PHPMainThread.terminate ();
  		}
  		catch (DebugException e) {
  		}
  	}

  	/**
	 * TODO Is this method called from anywhere?
	 *
  	 * Returns a already created server socket, or
  	 * creates a server socket if none exists, and
  	 * returns the newly created one.
  	 *
  	 * @return A server socket
  	 */
  	protected ServerSocket getServerSocket () throws IOException {
  	  	if (server == null) {										// Do we have already a server socket
  	    	createServerSocket ();									//  No, then create one
  	  	}

  	  	return server;												// Return the server socket
  	}

  	/**
  	 * Find a free unused port between 10001 and 10101 if the current debug session
  	 * is for remote debugging, and a unused port 7869 if it is used as non remote debugging.
  	 * 
  	 * For remote debugging the used port is submitted with the URL.
  	 * E.g. http://localhost/index.php?DBGSESSID=1@localhost:10001
  	 * For non remote debugging (if PHPeclipse used e.g. php cli directly) no port
  	 * can be submitted by parameter, and only the default port (7869) can be used.
  	 * 
  	 * @note: The free dbg version doesn't allow to set the appropriate port within php.ini! 
  	 * 
  	 * 
  	 */
  	protected void createServerSocket () {
  		if (this.remote) {
  			port = SocketUtil.findUnusedLocalPort ("localhost", 10001, 10101);	// Get the first free port in the range from 10001 to 10101
  		}
  		else {
			port = SocketUtil.findUnusedLocalPort ("localhost", 7869, 7869);	// Get the first free port in the range from 7869 to 7869
  		}
  	  	
  	  	if (port == -1) {                                                   // Did we get a free port?
  	    	PHPDebugCorePlugin.log (5, "Cannot find free port!!!!");        //  No, output a error message

  	    	return;                                                         //  And return
  	  	}
  	  	try {
  	    	if (server == null) {                                           // If there is no server socket yet
  	      		server = new ServerSocket (port);                           //  create a server socket for the free port
  	      		//System.out.println("ServerSocket on port: " + port);
  	    	}
  	  	} catch (IOException e) {
  	    	PHPDebugCorePlugin.log (e);
  	    	stop ();
  	  	}
  	}

  	/**
  	 *
  	 */
  	public Socket getSocket () throws IOException {
  	  	return socket;                                                  	// Return the socket
  	}

  	/**
  	 * Set the DBG interface which is linked to this proxy
  	 *
  	 * @paran DBGInt The DGB interface which is linked with this proxy
  	 */
  	protected void setDBGInterface (PHPDBGInterface DBGInt) {
  	  	this.DBGInt = DBGInt;
  	}

  	/**
  	 * Get the DBG interface which is linked to this proxy
  	 *
  	 * @paran DBGInt The DGB interface which is linked with this proxy
  	 */
  	public PHPDBGInterface getDBGInterface () {
  	  	return DBGInt;
  	}
  	
  	/**
  	 * Give back a buffered input stream for the socket which is
  	 * linked with this proxy
  	 */
  	public BufferedReader getReader () throws IOException {
  	  	if (reader == null) {                                               // Do we already have a buffered input stream
  	    	reader = new BufferedReader (new InputStreamReader (this.getSocket ().getInputStream (),
  		                                                        "ISO8859_1"));
  	  	}

  	  return reader;                                                      // Return the buffered input stream
  	}

  	/**
  	 *
  	 */
  	public BufferedReader getReader (Socket socket) throws IOException {
  	  	if (socket != null) {												// Is a socket provided
  	    	return new BufferedReader (new InputStreamReader (socket.getInputStream (),
  		                   							    	  "ISO8859_1"));  // Then create a buffered input stream
	 	}
  	  	else {
  	    	return null;                                                      // Without a socket we can't create a input stream
	  	}
  	}

  	/**
  	 *
  	 * @return The output stream for this proxy's socket
  	 */
  	public OutputStream getOutputStream () throws IOException {
  	  	return this.getSocket ().getOutputStream ();
  	}

	/**
	 *
	 */
  	protected void setBreakPoints () throws IOException, CoreException {
  		IBreakpoint[] breakpoints = DebugPlugin.getDefault ().getBreakpointManager ().getBreakpoints ();

  		for (int i = 0; i < breakpoints.length; i++) {
  			if (breakpoints[i].isEnabled ()) {
  				addBreakpoint (breakpoints[i]);
  			}
  		}
   	}

	/**
	 *
	 */
  	private String MapPath (PHPLineBreakpoint phpLBP) {
   		IPath 	 filename;
		IPath    remotePath;
		IPath    newpath;
		IPath    localPath;
		String   local;

   		if (remote) {
  			filename = phpLBP.getMarker().getResource().getProjectRelativePath();
			filename = remoteSourcePath.append (filename);
		} else {
			filename = phpLBP.getMarker().getResource().getFullPath();
		}

  		String path = filename.toOSString();

  		if ((pathmap != null) && remote) {
  			java.util.Iterator i = pathmap.keySet().iterator();

  			while (i.hasNext()) {
  				String k = (String) i.next();
  				if (path.startsWith(k)) {
  					path = pathmap.get(k) + path.substring(k.length());
  					break;
  				}
  			}
  		}

		if (remoteSourcePath.isEmpty ()) {
			if ((pathmap != null) && remote) {
				java.util.Iterator iterator = pathmap.keySet().iterator();

				while (iterator.hasNext ()) {
					local      = (String) iterator.next ();                 // Get the local/client side path of the mapping
					remotePath = new Path ((String) pathmap.get (local));   // Get the remote/server side path of the mapping
					localPath  = new Path (local);                          // Get the remote/server side path of the mapping

					if (localPath.isPrefixOf (filename)) {                  // Starts the remote/server side file path with the remote/server side mapping path
																			// dann prefix abh�ngen und den remote path davorh�gen
						newpath = filename.removeFirstSegments (localPath.matchingFirstSegments (filename));
						newpath = remotePath.append (newpath);
						path    = newpath.toString ();

  						if (path.substring (0, 1).equals ("/")) {
  							path = path.replace ('\\', '/');
						}
  						else {
  							path = path.replace ('/', '\\');
						}

						return path;
					}
				}
			}
		}
		else {
  			if (pathtranslation && remote) {
  				if (remoteSourcePath.toString ().substring (0, 1).equals ("/")) {
  					path = path.replace ('\\', '/');
				}
  				else {
  					path = path.replace ('/', '\\');
				}
  			}
		}

  		return path;
  	}

	/**
	 *
	 */
  	public void addBreakpoint (IBreakpoint breakpoint) {
  	  	if (DBGInt == null) {
  	    	return;
	  	}

  	  	int bpNo = 0;

  	  	try {
  	    	PHPLineBreakpoint phpLBP;

  	    	if (breakpoint.getModelIdentifier() == PHPDebugCorePlugin.getUniqueIdentifier()) {
  	      		phpLBP = (PHPLineBreakpoint) breakpoint;

  	      		// 	bpNo= DBGInt.addBreakpoint(phpLBP.getMarker().getResource().getLocation().toOSString(), phpLBP.getLineNumber());
  	      		if (phpLBP.isConditionEnabled ()) {
  	      			bpNo = DBGInt.addBreakpoint (MapPath(phpLBP),
  	      										 phpLBP.getLineNumber(), 
  	      				                         phpLBP.getHitCount(),
  	      				                         phpLBP.getCondition ());
  	      		}
  	      		else {
  	      			bpNo = DBGInt.addBreakpoint (MapPath(phpLBP),
  	      										 phpLBP.getLineNumber(), 
  	      				                         phpLBP.getHitCount(),
  	      				                         "");
  	      		}
  	      		
  	      		phpLBP.setDBGBpNo(bpNo);
  	    	}
  	  	} catch (IOException e) {
	  	    PHPDebugCorePlugin.log(e);
  		    stop();
  	  	} catch (CoreException e) {
  	    	PHPDebugCorePlugin.log(e);
  	    	stop();
  	  	}
  	}

	/**
	 *
	 */
  	public void removeBreakpoint (IBreakpoint breakpoint) {
  	  	if (DBGInt == null) {
  	    	return;
		}

  	  	try {
  	    	PHPLineBreakpoint phpLBP;

  	    	if (breakpoint.getModelIdentifier() == PHPDebugCorePlugin.getUniqueIdentifier ()) {
  	      		phpLBP = (PHPLineBreakpoint) breakpoint;

  	      		// 	bpNo= DBGInt.addBreakpoint(filename.toOSString(), phpLBP.getLineNumber());

  	      		DBGInt.removeBreakpoint(MapPath(phpLBP), phpLBP.getLineNumber(), phpLBP.getDBGBpNo());
  	    	}
  	  	} catch (IOException e) {
  	    	PHPDebugCorePlugin.log (e);
  	    	stop ();
  	  	} catch (CoreException e) {
  	    	PHPDebugCorePlugin.log (e);
  	    	stop ();
  	  	}
  	}

	/**
	 *
	 */
  	public void phpLoopNotify () {
  	  	phpLoop.notifyWait ();
  	}

	/**
	 *
	 */
  	public void startPHPLoop () {
  	  	phpLoop = new PHPLoop ();									// Create a DBG communication loop object

  	  	phpLoop.start ();											// And start the communication loop
  	}

	/**
	 *
	 */
  	public void resume () {
  	  	try {
  	    	DBGInt.continueExecution();
  	    	phpLoop.notifyWait();
  	  	} catch (IOException e) {
  	    	PHPeclipsePlugin.log("Debugging session ended.", e);
  	    	stop();
  	  	}
  	}

	/**
	 *
	 */
  	public void pause () {
  	  	try {
  	    	if (null != DBGInt) {
  	  			DBGInt.pauseExecution();
			}
  	    	else {
  	       		// TODO Make sure the Suspend action is grayed out
  	    		// when DBGInt is null
  	    	}
  	  	} catch (IOException e) {
  	    	PHPDebugCorePlugin.log (e);
  	    	stop ();
  	  	}
  	}

	/**
	 *
	 */
  	protected PHPDebugTarget getDebugTarget() {
  	  	return debugTarget;
  	}

	/**
	 * Is called by the DebuggerRunner
	 * 
	 * @param debugTarget
	 */
  	public void setDebugTarget (PHPDebugTarget debugTarget) {
  	  	this.debugTarget = debugTarget;
  	  	debugTarget.setPHPDBGProxy(this);
  	}
  	
	/**
	 * This method is called by a stackframe.
	 * It reads the variables from PHP via DBG
	 *
	 * @param frame The stackframe which wants the variables.
	 * @return      The list of variables for this stackframe.
	 */
  	public Vector readVariables (PHPStackFrame frame) {
  	  	try {
  	    	return DBGInt.getVariables (frame);						// Get the variables from DBG interface
  	  	} catch (IOException ioex) {
  	    	ioex.printStackTrace ();
  	    	throw new RuntimeException (ioex.getMessage ());
  	  	} catch (DebugException ex) {
	  	    ex.printStackTrace ();
  		    throw new RuntimeException (ex.getMessage ());
  	  	}
  	}

	/**
	 *
	 * @param frame
	 * @param evalString
	 * @return
	 */
  	public PHPVariable[] eval (PHPStackFrame frame, String evalString) {
  	  	try {
  	    	return DBGInt.evalBlock (frame, evalString);
  	    	//return DBGInt.getVariables(frame);
  	  	} catch (IOException ioex) {
  	    	ioex.printStackTrace();
  	    	throw new RuntimeException(ioex.getMessage());
  	  	} catch (DebugException ex) {
  	    	ex.printStackTrace();
  	    	throw new RuntimeException(ex.getMessage());
  	  	}
  	}

  	public void readStepOverEnd (PHPStackFrame stackFrame) {
  	  	try {
  	    	DBGInt.stepOver();
  	    	phpLoop.notifyWait();
  	  	} catch (Exception e) {
  	    	PHPDebugCorePlugin.log(e);
  	  	}
  	}

  	public void readStepReturnEnd (PHPStackFrame stackFrame) {
  	  	try {
  	    	DBGInt.stepOut();
	  	    phpLoop.notifyWait();
  		} catch (Exception e) {
  	    	PHPDebugCorePlugin.log(e);
  	  	}
  	}

  	public void readStepIntoEnd (PHPStackFrame stackFrame) {
  	  	try {
  	    	DBGInt.stepInto();
  	    	phpLoop.notifyWait();
  	  	} catch (Exception e) {
  	    	PHPDebugCorePlugin.log(e);
  	  	}
  	}

  	/*
  	 * public PHPStackFrame[] readFrames(PHPThread thread) { //try { //this.println("th " + thread.getId() + " ; f "); //return new
  	 * FramesReader(getMultiReaderStrategy()).readFrames(thread); return null; //} catch (IOException e) { //
  	 * PHPDebugCorePlugin.log(e); // return null; //}
  	 *  }
  	 */

  	public void closeSocket() throws IOException {
  	  	if (socket != null) {
  	    	socket.close();
  	  	}
  	}

  	public void closeServerSocket() throws IOException {
  	  	if (server != null) {
  	    	server.close();
  	  	}
  	}

  	public int getPort() {
  	  	return port;
  	}

	/**
	 *
	 *
	 */
  	class PHPLoop extends Thread {
  	  	private boolean shouldStop;

  	  	public PHPLoop () {
  	    	shouldStop = false;
  	    	this.setName ("PHPDebuggerLoop");
  	  	}
  	  	
  		/**
  		 *
  		 */
  	  	public synchronized void setShouldStop () {
  			shouldStop = true;											// The run loop should stop

  			try {
  				// If the loop thread is blocked on the server socket,
  				// forcibly unblock it to avoid leaking the thread,
  				// the socket and the port
  				closeServerSocket ();
  			} catch (IOException x) {
  				// Log this as a warning?
  				PHPDebugCorePlugin.log (x);
  			}
  		}

  		/**
  		 *
  		 */
  	  	public synchronized void notifyWait () {
  	    	notify ();
  	  	}

  		/**
  		 *
  		 *
  		 */
  	  	public void run () {
  	    	try {
  	      		int 			i;
  	      		long  			interval 	= 200;					// Wait 200 ms maximum for a DBG response
  	      		boolean 		newconnect 	= false;				//
  	      		Socket 			newSocket 	= null;
  	      		PHPStackFrame[] StackList;
  	      		PHPDBGInterface newDBGInt;

  	      		//				synchronized (this) {
  	      		//					wait();
  	      		//				}

  	      		PHPMainThread = new PHPThread (getDebugTarget (), getPort ());
  	      		PHPMainThread.setName ("Thread [main]");

  	      		//				while ((getDebugTarget() == null) && (timeout < 100)) {
  	      		//					sleep(100);
  	      		//					timeout++;
  	      		//				}
  	      		// Be sure debug target is set
  	      		//				PHPMainThread.setDebugTarget(getDebugTarget());

	  			getDebugTarget ().addThread (PHPMainThread);

  		      	//System.out.println("Waiting for breakpoints.");

  	       		while (!shouldStop) {								// As long as nobody will stop us
  	        		newconnect = true;                              // The first time

  	        		try {
  	          			newSocket = server.accept();				// Waits until DBG want to connect
  	          			//System.out.println("Accepted! : " + socket.toString());
  	        		} catch (SocketTimeoutException e) {
  	          			newconnect = false;							// No one wants to connect (connection already done)
  	        		} catch (IOException e) {
  	          			PHPDebugCorePlugin.log(e);
  	          			return;
  	        		}

  	        		if (newconnect) {								// Is it just after a new connection
  	          			if (DBGInt == null) {						// Do we have a DBG interface?
  	            			server.setSoTimeout(1);					// ???
  						}

  	          			newDBGInt = new PHPDBGInterface (getReader (newSocket),			// Create a new interface
														 newSocket.getOutputStream (),
														 thisProxy);
  	          			newDBGInt.waitResponse (1000);   			// Wait for the initial DBG response
  	          			newDBGInt.flushAllPackets ();               // Read and process the DBG response

  	          			// Check version and session ID
  	          			if ((DBGInt == null) ||               			// If we have no interface
  				    		(DBGInt.getSID () == newDBGInt.getSID ())) {// or the new session ID is different to the old one
  	            			DBGInt = newDBGInt;							// Set the new interface as current one

  	            			try {
  	              				closeSocket ();
  	            			}
  				  			catch (IOException e) {
  	              				PHPDebugCorePlugin.log (e);
  	              				shouldStop = true;
  	            			}

  	            			socket = newSocket;
  	            			setBreakPoints ();
  	            			DBGInt.continueExecution ();            	// Notify DBG that PHP should continue
  	          			}
  						else {
  	            			newDBGInt.continueExecution ();				// Notify DBG that PHP should continue
  	            			newSocket.close ();
  	          			}
  	        		}

  	        		if (DBGInt.waitResponse (interval)) {						// Wait for a DBG response (200 ms)
  	          			DBGInt.flushAllPackets ();								// If we got something, read and process it

  	          			if (DBGInt.BPUnderHit != 0) {							// ???
  	            			StackList = DBGInt.getStackList ();                 // Get the stack list from DBGInterface

			  	            if (StackList.length > 0) {                         // If there is something in stack list
  	        			      	for (i = 0; i < StackList.length; i++) {        // For all stack list
  	                				StackList[i].setThread (PHPMainThread);     // Set the PHPTread for all PHPStackFrames

  	                				if (DBGInt.getModByNo (StackList[i].getModNo ()).equals ("")) {
  	                  					DBGInt.getSourceTree ();
  	                				}

  	                				StackList[i].setFile (DBGInt.getModByNo (StackList[i].getModNo ()));
  	              				}

  	              				PHPMainThread.setStackFrames (StackList);
  	            			}

  	            			PHPMainThread.suspend ();                             // Fire debug event

			  	            synchronized (this) {
  	        			      	wait ();
  	            			}
  	          			}
  	        		}

  	        		if (remote) {
  	          			if (PHPMainThread.isTerminated ()) {
  	            			shouldStop = true;

			  	            break;                                                // Go for terminating the thread
  	        	  		}
  	        		} else {
  	          			if (PHPMainThread.isTerminated () ||
  				    		getDebugTarget ().getProcess ().isTerminated ()) {
  	            			shouldStop = true;

  	            			break;                                                // Go for terminating the thread
  	          			}
  	        		}
  	      		}
  	    	} catch (Exception ex) {
  	      		PHPDebugCorePlugin.log (ex);
  	      		System.out.println (ex);
  	    	} finally {
  	      		try {
  	        		getDebugTarget ().terminate ();
  	        		closeSocket();
  	        		closeServerSocket ();
  	      		} catch (IOException e) {
  	        		PHPDebugCorePlugin.log (e);

  	        		return;
  	      		}

  	      		//System.out.println("Socket loop finished.");
  	    	}
  	  	}
  	}
}
