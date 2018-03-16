/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	Vicente Fernando - www.alfersoft.com.ar - Initial implementation
	Christian Perkonig - remote debug
**********************************************************************/
package net.sourceforge.phpdt.internal.debug.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Vector;

import net.sourceforge.phpdt.internal.debug.core.PHPDBGProxy.PHPLoop;
import net.sourceforge.phpdt.internal.debug.core.model.PHPDBGEvalString;
import net.sourceforge.phpdt.internal.debug.core.model.PHPStackFrame;
import net.sourceforge.phpdt.internal.debug.core.model.PHPValue;
import net.sourceforge.phpdt.internal.debug.core.model.PHPVariable;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;

/**
 * The interface object are created by the proxy
 *
 */
public class PHPDBGInterface {
	public boolean 			sessionEnded = false;
	public int 				sessType = -1;
	public int 				BPUnderHit = 0;
	public String 				sessID = new String();

	private int[] 				LastBPRead = new int[10];
	private Vector 			DBGBPList = new Vector();
	private Vector 			DBGVarList = new Vector();
	private PHPStackFrame[] 	DBGStackList = new PHPStackFrame[0];
	private Vector 			DBGMods = new Vector(); 			// The module names and their numbers
	private Vector 			stackListOld = new Vector();
	private BufferedReader 	in;
	private OutputStream 		os; 								// The stream which goes to DBG
	private boolean 			shouldStop = false;
	private String 			evalRet = new String("");
	private int 				rawCounter = 1000; 					// An rawData frame ID counter
	private PHPDBGProxy 		proxy = null;
	private int 				lastCmd = -1;
	private int 				sid = 0;
	private boolean 			stopOnError = false;
	private char[] 			lastCommand = new char[4];

	private static final String GlobalVariablesTitle = PHPDebugCorePlugin
			.getResourceString("VariablesView.GlobalVariables.title");

	/**
	 * @param in    The input stream (communication from DBG).
	 * @param os    The output stream (communication to DBG).
	 * @param proxy The proxy to which this interface belongs.
	 */
	public PHPDBGInterface (BufferedReader in, OutputStream os, PHPDBGProxy proxy) {
		DBGBPList.clear ();

		this.in		= in;
		this.os		= os;
		this.proxy	= proxy;
	}

	/**
	 *
	 * @param mod_name  The module (source file) to which we add the breakpoint.
	 * @param line      The line where the breakpoint is set.
	 * @param hitCount  The number of hit counts before suspend.
	 * @param condition The break condition
	 * @return          Breakpoint ID ???.
	 */
	public int addBreakpoint (String mod_name, int line, int hitCount, String condition) throws IOException {
		return setBreakpoint (mod_name, condition, line, PHPDBGBase.BPS_ENABLED + PHPDBGBase.BPS_UNRESOLVED, 0, hitCount, 0, 0, 0);
	}

	/**
	 *
	 * @param mod_name The module (source file) to which we add the breakpoint.
	 * @param line     The line where the breakpoint is set.
	 * @param bpNo     The breakpoint ID ???.
	 */
	public void removeBreakpoint (String mod_name, int line, int bpNo) throws IOException {
		setBreakpoint (mod_name, "", line, PHPDBGBase.BPS_DISABLED, 0, 0, 0, bpNo, 0);
	}

	/**
	 * Is this method used anywhere?
	 *
	 */
	public void requestDBGVersion () throws IOException {
		PHPDBGPacket DBGPacket;                                     // A DBG message packet
		PHPDBGFrame  DBGFrame;                                      // A frame within a DBG packet

		DBGPacket = new PHPDBGPacket (PHPDBGBase.DBGA_REQUEST);     // A request for DBG
		DBGFrame  = new PHPDBGFrame (PHPDBGBase.FRAME_VER);         // We want the version of DBG

		DBGPacket.addFrame (DBGFrame);                              // Add the 'what we want' to the DBG packet

		if (proxy.getSocket ().isClosed ()) {						// Can we communiate with DBG?
			return;                                                 //  No
		}

		DBGPacket.sendPacket (os);									// Send the request to DBG
	}

	/**
	 * Called by the proxy
	 *
	 */
	public void getSourceTree () throws IOException {
		PHPDBGPacket DBGPacket;                                     // A DBG message packet
		PHPDBGFrame  DBGFrame;                                      // A frame within a DBG packet

		DBGPacket = new PHPDBGPacket (PHPDBGBase.DBGA_REQUEST);     // A request for DBG
		DBGFrame  = new PHPDBGFrame (PHPDBGBase.FRAME_SRC_TREE);	// We want a source tree from DBG

		DBGPacket.addFrame (DBGFrame);                              // Add the 'what we want' to the DBG packet

		if (proxy.getSocket ().isClosed ()) {						// Can we communiate with DBG?
			return;													//  No
		}

		DBGPacket.sendPacket (os);                                  // Send the request to DBG

		waitResponse (1000);                                        // Wait for the DBG response (1 second)
		flushAllPackets ();   										// Read and process the response from DBG
	}

	/**
	 * Is this method called from anywhere?
	 *
	 * @param modName The modul (filename).
	 */
	public void addDBGModName (String modName) throws IOException {
		PHPDBGPacket DBGPacket;                                     // A DBG message packet
		PHPDBGFrame  DBGFrame;                                      // A frame within a DBG packet

		DBGPacket = new PHPDBGPacket (PHPDBGBase.DBGA_REQUEST);     // A request for DBG
		DBGFrame  = new PHPDBGFrame (PHPDBGBase.FRAME_RAWDATA);     // We want Module name from DBG

		rawCounter++;                                               // Increment the rawData ID counter
		DBGFrame.addInt (rawCounter);								// FRAME_RAWDATA ID
		DBGFrame.addInt (modName.length () + 1);					// The length of rawdata string (incl. null char termination)
		DBGFrame.addString (modName);								// The file name (module name)
		DBGFrame.addChar ('\0');					   				// Add the C-String null termination

		DBGPacket.addFrame (DBGFrame);

		if (proxy.getSocket ().isClosed ()) {						// Can we communiate with DBG?
			return;													//  No
		}

		DBGPacket.sendPacket (os);
	}

	/**
	 * This method is called for adding or removing breakpoints.
	 *
	 * @param mod_name  	The module name (file name).
	 * @param condition     Info about the condition when to break (not used at the moment).
	 * @param line          The breakpoints line.
	 * @param state         Info whether this breakpoint has to be dis- or enabled.
	 * @param istep         Always 0.
	 * @param hitcount      Always 0.
	 * @param skiphits      Always 0.
	 * @param bpno          The breakpoint ID.
	 * @param isunderhit    ???
	 * @return
	 */
	private int setBreakpoint (String mod_name, String condition, int line, int state, int istemp, int hitcount, int skiphits, int bpno, int isunderhit) throws IOException {
		PHPDBGPacket 	DBGPacket;
		PHPDBGFrame 	DBGFrame1;
		PHPDBGFrame 	DBGFrame2;
		PHPDBGFrame		DBGFrame3;
		int 			modNo;

		DBGPacket	= new PHPDBGPacket (PHPDBGBase.DBGA_REQUEST);
		DBGFrame1	= new PHPDBGFrame (PHPDBGBase.FRAME_BPS);
		DBGFrame2	= new PHPDBGFrame (PHPDBGBase.FRAME_RAWDATA);
		DBGFrame3	= new PHPDBGFrame (PHPDBGBase.FRAME_RAWDATA);

		modNo 		= getModByName (mod_name);						// Get the module ID by name

		if (modNo >= 0) {                                           // Did we find a module ID for the module name?
			DBGFrame1.addInt (modNo);								// Add the module ID to frame 1
		} else {
			DBGFrame1.addInt (0);									// mod number (0 use file name)
		}

		DBGFrame1.addInt (line);									// line number

		if (modNo >= 0) {                                           // Did we find a module ID for the module name?
			DBGFrame1.addInt (0);									// use mod number
		} else {
			rawCounter++;
			DBGFrame1.addInt (rawCounter);							// ID of FRAME_RAWDATA to send file name
		}

		if (modNo < 0) {                                            // Did we find a module ID for the module name?
			DBGFrame2.addInt (rawCounter);				            // FRAME_RAWDATA ID
			DBGFrame2.addInt (mod_name.length() + 1);	            // length of rawdata (+ null char)
			DBGFrame2.addString (mod_name);				            // file name
			DBGFrame2.addChar ('\0');					            // null char

			DBGPacket.addFrame (DBGFrame2);                         // First add file name data
		}

		DBGFrame1.addInt (state);	                            	// state BPS_*
		DBGFrame1.addInt (istemp);	                            	// istemp
		DBGFrame1.addInt (0);			                            // hit count; this is not supported as one might think
		DBGFrame1.addInt (hitcount);	                            // skip hits is what we think is hit count.

		if (!condition.equals ("")) {								// Do we have a condition for breakpoint
			rawCounter++;											// Set to new ID
			DBGFrame1.addInt (rawCounter);                          // ID of condition

			DBGFrame3.addInt (rawCounter);				            // FRAME_RAWDATA ID
			DBGFrame3.addInt (condition.length() + 1);	            // length of rawdata (+ null char)
			DBGFrame3.addString (condition);				        // The break condition
			DBGFrame3.addChar ('\0');					            // null char

			DBGPacket.addFrame (DBGFrame3);                         // First add break condition
		}
		else {
			DBGFrame1.addInt (0);                            		// ID of condition is 0, because there is no condition
		}

		DBGFrame1.addInt (bpno);		                            // breakpoint number
		DBGFrame1.addInt (isunderhit);                           	// is under hit

		DBGPacket.addFrame (DBGFrame1);								// Second add command data

		if (proxy.getSocket ().isClosed ()) {						// Can we communiate with DBG?
			return 0;                                               //  No
		}

		DBGPacket.sendPacket (os);                                  // Send the request to DBG

		clearLastBP ();

		waitResponse (1000);                                        // Wait for the DBG response (1 second)
		flushAllPackets ();                                         // Read and process the response from DBG

		return LastBPRead[8];										// Return what ???
	}

	/**
	 *
	 */
	private void clearLastBP () {
		int i;

		for (i = 0; i < LastBPRead.length; i++) {
			LastBPRead[i] = 0;
		}
	}

	/**
	 *
	 */
	private void copyToLastBP (int[] BPBody) {
		int i;

		for (i = 0; i < LastBPRead.length; i++) {
			LastBPRead[i] = BPBody[i];
		}
	}

	/**
	 *
	 */
	public void continueExecution () throws IOException {
		PHPDBGPacket DBGPacket;

		BPUnderHit = 0;
		DBGPacket  = new PHPDBGPacket (PHPDBGBase.DBGA_CONTINUE);

		if (proxy.getSocket ().isClosed ()) {						// Can we communiate with DBG?
			return;                                                 //  No
		}
		DBGPacket.sendPacket (os);                                  // Send the request to DBG

		lastCommand = PHPDBGBase.DBGA_CONTINUE;                     // Store the info about the command we sent
	}

	/**
	 *
	 */
	public void pauseExecution () throws IOException {
		PHPDBGPacket DBGPacket;

		DBGPacket = new PHPDBGPacket (PHPDBGBase.IntToChar4 (PHPDBGBase.DBGC_PAUSE));

		if (proxy.getSocket ().isClosed ()) {						// Can we communiate with DBG?
			 return;                                                //  No
		}

		DBGPacket.sendPacket (os);                                  // Send the request to DBG
	}

	/**
	 *
	 */
	private int getBPUnderHit () {
		int i;
		int BPUnder 		= 0;
		int[] dbg_bpl_body	= new int[10];

		for (i = 0; i < DBGBPList.size (); i++) {   				// look for bp under hit
			dbg_bpl_body = (int[]) DBGBPList.get (i);

			if (dbg_bpl_body[9] == 1) {
				BPUnder = dbg_bpl_body[8];
			}
		}

		return BPUnder;
	}

	public int getLastCmd()
	{
		return lastCmd;
	}

	public int getSID()
	{
	  return sid;
	}

	public void setLastCmd (int cmd)
	{
		lastCmd = cmd;
	}

	/**
	 *
	 */
	public void stepInto () throws IOException {
		PHPDBGPacket DBGPacket;

		BPUnderHit = 0;
		DBGPacket  = new PHPDBGPacket (PHPDBGBase.DBGA_STEPINTO);

		if (proxy.getSocket ().isClosed ()) {						// Can we communiate with DBG?
			return;                                                 //  No
		}

		DBGPacket.sendPacket (os);                                  // Send the request to DBG

		lastCommand = PHPDBGBase.DBGA_STEPINTO;						// Store the info about the command we sent
	}

	/**
	 *
	 */
	public void stepOver () throws IOException {
		PHPDBGPacket DBGPacket;

		BPUnderHit = 0;
		DBGPacket  = new PHPDBGPacket (PHPDBGBase.DBGA_STEPOVER);

		if (proxy.getSocket ().isClosed ()) {						// Can we communiate with DBG?
			return;                                                 //  No
		}

		DBGPacket.sendPacket (os);                                  // Send the request to DBG

		lastCommand = PHPDBGBase.DBGA_STEPOVER;                     // Store the info about the command we sent
	}

	/**
	 *
	 */
	public void stepOut () throws IOException {
		PHPDBGPacket DBGPacket;

		BPUnderHit = 0;
		DBGPacket  = new PHPDBGPacket (PHPDBGBase.DBGA_STEPOUT);

		if (proxy.getSocket ().isClosed ()) {						// Can we communiate with DBG?
			return;                                                 //  No
		}

		DBGPacket.sendPacket (os);                                  // Send the request to DBG

		lastCommand = PHPDBGBase.DBGA_STEPOUT;                      // Store the info about the command we sent
	}

	/**
	 *
	 */
	public void stopExecution () throws IOException {
		PHPDBGPacket DBGPacket;

		BPUnderHit = 0;
		DBGPacket  = new PHPDBGPacket (PHPDBGBase.DBGA_STOP);

		if (proxy.getSocket ().isClosed ()) {						// Can we communiate with DBG?
			return;                                                 //  No
		}

		DBGPacket.sendPacket (os);                                  // Send the request to DBG
	}

	/**
	 * This method is called by the proxy.
	 * It sends a request to DBG to get the current variables
	 * with their values. It waits for the response and processes
	 * the input from DBG.
	 *
	 * @param stack The stackframe for which we want the variables.
	 * @return      The array of variables
	 */
	public synchronized Vector getVariables(PHPStackFrame stack) throws IOException, DebugException {
		if (DBGStackList.length == 0) {
			DBGVarList.clear();
			return DBGVarList;
		}

		// get global variables (and assign them to 'main()' stackframe)
		int global_scope_id = (DBGStackList.length > 1) ? 2 : PHPDBGBase.GLOBAL_SCOPE_ID;
		// DBG 2.13.1 doesn't return Super Globals with GLOBAL_SCOPE_ID in nested stackframes,
		// so using 2(most out-standing stack context) instead of GLOBAL_SCOPE_ID.
		// Also note that 2.13.1 doesn't return $this in class context.
		// (You can inspect $this in Expressions View. And once it is shown, 2.13.1 comes to return $this.)
		Vector globalList = getVariables(DBGStackList[DBGStackList.length - 1], global_scope_id);
		if (!globalList.isEmpty()) {
			// remove unresolved '$this=?' variable
			removeUnresolvedThisVar(globalList);

			PHPVariable var = (PHPVariable) globalList.get(0);
			var.setName(GlobalVariablesTitle);
			var.setModifiable(false);
		}

		int scopeID = stack.getScopeID();
		if (!globalList.isEmpty()
				&& ((DBGStackList.length == 1)
						|| (scopeID == PHPDBGBase.CURLOC_SCOPE_ID + 1))) {
			// 'main()' stackframe
			PHPVariable var = (PHPVariable) globalList.get(0);
			PHPValue val = (PHPValue) var.getValue();
			DBGVarList = val.getChildVariables();
			return DBGVarList;

		} else if (scopeID == PHPDBGBase.CURLOC_SCOPE_ID) {
			// current stackframe
			DBGVarList = getVariables(stack, PHPDBGBase.CURLOC_SCOPE_ID);

		} else {
			// back-trace stackframe
			//DBGVarList = getVariables(stack, scopeID);
			//removeUnresolvedThisVar(DBGVarList);
			// DBG 2.15.5 causes Application Error (on win32) in *some* cases.
			DBGVarList.clear();
		}

		if (DBGVarList.size() > 0) {								// Did we get back variables?
			PHPVariable var = (PHPVariable) DBGVarList.get(0);		// Yes, then get the first PHPVariable
			PHPValue    val = (PHPValue) var.getValue();			// Get the value

			if (var.getName().equals("")) {							// Is the root node an empty node (usually it is)
				DBGVarList = val.getChildVariables();				// Then remove the empty node.
																	// With removing the empty root node, it wouldn't be necessary to
																	// set the name to an empty string. So the code below is just for
																	// info or in case the users want to have the empty root node.

																	// The eclipse variable view cannot handle Variables which have an empty name
																	// when it comes to variable tree restore operation. Without a name, no restore!
				//var.setName (" ");   								// Give a name to the variable root node. Even if it is only a space :-)
			}														// TO DO the best would be to remove the empty root node, but this would
																	// require a understanding and reworking of the PHPDBGEvalstring class.
		}

		if (!globalList.isEmpty()) {
			DBGVarList.add(globalList.get(0));
		}

		return DBGVarList;											// Return the variables as list
	}

	/**
	 * 
	 */
	private Vector getVariables(PHPStackFrame stack, int scope_id) throws IOException {
		PHPDBGPacket DBGPacket = new PHPDBGPacket(PHPDBGBase.DBGA_REQUEST);
		PHPDBGFrame DBGFrame1 = new PHPDBGFrame(PHPDBGBase.FRAME_EVAL);

		DBGFrame1.addInt(0);
		DBGFrame1.addInt(scope_id);

		DBGPacket.addFrame(DBGFrame1);
		evalRet = "";

		if (proxy.getSocket().isClosed()) {
			return new Vector();
		}
		DBGPacket.sendPacket(os);

		waitResponse(1000);
		flushAllPackets();

		PHPDBGEvalString evalStr = new PHPDBGEvalString(stack, evalRet);
		return evalStr.getVariables();
	}

	/**
	 * Remove unresolved $this variable
	 * 
	 * DBG returns $this=? in function's or intermediate stackframes.
	 * (In current method's stackframe, DBG returns $this=classname)
	 * 
	 * @param varList
	 */
	private void removeUnresolvedThisVar(Vector varList) {
		if (varList.size() > 0) {
			PHPVariable var = (PHPVariable) varList.get(0);
			PHPValue val = (PHPValue) var.getValue();
			Vector workList = val.getChildVariables();
			for (int i = 0; i < workList.size(); i++) {
				PHPVariable workvar = (PHPVariable) workList.get(i);
				if (workvar.getName().equals("$this")) {
					String workval = ((PHPValue) workvar.getValue()).getValueString();
					if (workval.equals("?") || workval.equals("NULL")) {
						workList.remove(i);
					}
					break;
				}
			}
		}
	}

	/**
	 *
	 * @param logString
	 */
	public void log(String logString) throws IOException, DebugException  {
		PHPDBGPacket DBGPacket= new PHPDBGPacket(PHPDBGBase.DBGA_REQUEST);
		PHPDBGFrame DBGFrame1= new PHPDBGFrame(PHPDBGBase.FRAME_LOG);
		PHPDBGFrame DBGFrame2= new PHPDBGFrame(PHPDBGBase.FRAME_RAWDATA);

		rawCounter++;
		DBGFrame1.addInt(rawCounter);				// ilog
		DBGFrame1.addInt(1);						// type
		DBGFrame1.addInt(0);						// mod_no
		DBGFrame1.addInt(0);						// line_no
		DBGFrame1.addInt(0);						// imod_name
		DBGFrame1.addInt(0);						// ext_info

		DBGFrame2.addInt(rawCounter);				// FRAME_RAWDATA ID
		DBGFrame2.addInt(logString.length() + 1);	// length of rawdata (+ null char)
		DBGFrame2.addString(logString);				// log string
		DBGFrame2.addChar('\0');					// null char

		// Add raw data first
		DBGPacket.addFrame(DBGFrame2);
		// Add command data
		DBGPacket.addFrame(DBGFrame1);

		if (proxy.getSocket ().isClosed ()) {                   // Do we have a socket for DBG communication?
			return;   											//  No, then leave here
		}

		DBGPacket.sendPacket(os);

		waitResponse(1000);
		flushAllPackets();
	}

	public synchronized PHPVariable[] evalBlock(PHPStackFrame stack, String evalString) throws IOException, DebugException {
		PHPDBGPacket DBGPacket= new PHPDBGPacket(PHPDBGBase.DBGA_REQUEST);
		PHPDBGFrame DBGFrame1= new PHPDBGFrame(PHPDBGBase.FRAME_EVAL);
		PHPDBGFrame DBGFrame2= new PHPDBGFrame(PHPDBGBase.FRAME_RAWDATA);

		rawCounter++;
		DBGFrame1.addInt(rawCounter);				// istr = raw data ID
		//DBGFrame1.addInt(1);						// scope_id = -1 means current location, 0 never used, +1 first depth
		int scope_id = stack.getScopeID();
		/* test code : unnecessary
		if (DBGStackList.length == 1 || scope_id == (PHPDBGBase.CURLOC_SCOPE_ID + 1)) {
			scope_id = PHPDBGBase.GLOBAL_SCOPE_ID;
		}
		*/
		DBGFrame1.addInt(scope_id);

		DBGFrame2.addInt(rawCounter);				// FRAME_RAWDATA ID
		DBGFrame2.addInt(evalString.length() + 1);	// length of rawdata (+ null char)
		DBGFrame2.addString(evalString);			// eval block
		DBGFrame2.addChar('\0');					// null char

		// Add raw data first
		DBGPacket.addFrame(DBGFrame2);
		// Add command data
		DBGPacket.addFrame(DBGFrame1);

		if (proxy.getSocket().isClosed()) {			// Do we have a socket for DBG communication?
			return null;							//  No, then leave here
		}
		DBGPacket.sendPacket(os);

		waitResponse(1000);
		flushAllPackets();

		PHPDBGEvalString evalStr=new PHPDBGEvalString(stack, evalRet);

		return evalStr.getVars();
	}

	/**
	 * Read and process everthing we got from DBG
	 */
	public void flushAllPackets () throws IOException {
		while (readResponse() != 0);
	}

	/**
	 * Get the modules name by its number
	 *
	 * @param modNo The number (id) of the module
	 * @return      The name of the module
	 */
	public String getModByNo (int modNo) {
		int       	i;
		PHPDBGMod 	dbg_mod;

		for (i = 0; i < DBGMods.size (); i++) {                         // For all the modules we have within the array
			dbg_mod = (PHPDBGMod) DBGMods.get (i);                      // Get the module

			if (dbg_mod.getNo () == modNo) {                            // Is the module from the array the module we want?
				return dbg_mod.getName ();                              //  Yes, return the name of the module
			}
		}

		return "";                                                  	// If nothing was found return emtpy string
	}

	/**
	 *
	 * @param  modName The name of the module for which we want the ID
	 * @return
	 * - The ID of the module
	 * - -1 if nothing was found
	 */
	private int getModByName (String modName) {
		int       	i;
		PHPDBGMod 	dbg_mod;

		for (i = 0; i < DBGMods.size (); i++) {                         // For all the modules we have within the array
			dbg_mod = (PHPDBGMod) DBGMods.get (i);                      // Get the module

			if (dbg_mod.getName ().equalsIgnoreCase (modName)) { 		// Is the module from the array the module we want?
				return dbg_mod.getNo ();                                //  Yes, return the name of the module
			}
		}

		return -1;                                                      // If nothing was found return -1
	}

	/**
	 * Return the string for the given frame number
	 *
	 * @param framesInfo The buffer which is to read
	 * @param frameNo    The frame number
	 * @return
	 */
	private String getRawFrameData (char[] framesInfo, int frameNo) {
		int   nextFrame = 0;                                                    // The current read position within the buffer
		int[] dbg_frame = new int[2];                                           // The two frame header numbers

		while (nextFrame < framesInfo.length) {                                 // As long we have something within the buffer
			dbg_frame[0] = PHPDBGBase.Char4ToInt (framesInfo, nextFrame);		// The frame type
			dbg_frame[1] = PHPDBGBase.Char4ToInt (framesInfo, nextFrame + 4);	// The frame size

			nextFrame   += 8;                                                   // The current read position

			if (dbg_frame[1] == 0) {                                            // If frame size is 0
				return "";                                                      //  return an emtpy string
			}

			switch (dbg_frame[0]) {                                                     // Switch for the frame type
				case PHPDBGBase.FRAME_RAWDATA:                                  		// The only frame type we are interrested in
					if (frameNo == PHPDBGBase.Char4ToInt (framesInfo, nextFrame)) {		// Is it correct  number of the frame
						int toRead;                                                     //

						toRead = PHPDBGBase.Char4ToInt (framesInfo, nextFrame + 4); 	// The size of the string

						if ((int) framesInfo[nextFrame + 8 + toRead - 1] == 0) {				// Is there a string termination at the end?
							return String.copyValueOf (framesInfo, nextFrame + 8, toRead - 1);	// Then copy frame content to String without the \0 and return
						}

						return String.copyValueOf (framesInfo, nextFrame + 8, toRead);	// Copy frame content to String and return
					}
					break;
			}

			nextFrame += dbg_frame[1];                              					// Go for the next frame (add the length of the current one)
		}

		return "";																		// We did not found any FRAM_RAWDATA, so return an emtpy strin
	}

	/**
	 * Reset the availability flag for all stackframes in the list.
	 *
	 * @param list          The list of old stackframes
	 */
	private void resetAvailability (Vector list) {
		int             i;

		for (i = 0; i < list.size (); i++) {
			((PHPStackFrame) list.get(i)).setAvailable (false);    		        		//
		}
	}

	/**
	 * Check whether the new stackframe is in the list of old stackframes.
	 * Test for identical stackframe (identical means same description and same line number).
	 *
	 * @param stackFrameNew The stackframe to check whether he is already within the old stackframe list
	 * @param list          The list of old stackframes
	 * @return
	 *  - true if we have found the identical stackframe within the list
	 *  - false if we did not find the identical stackframe within the list
	 */
	private boolean isStackFrameInList (PHPStackFrame stackFrameNew, Vector list) {
		int             i;
		PHPStackFrame 	stackFrameOld;

		for (i = 0; i < list.size (); i++) {
			stackFrameOld = (PHPStackFrame) list.get (i);		        		      	//

 			if (stackFrameNew.getDescription ().equals (stackFrameOld.getDescription ()) &&
				stackFrameNew.getLineNumber () == stackFrameOld.getLineNumber ()) {     // Did we find the sent stackframe within the list of old stackframes?
				stackFrameOld.setAvailable (true);                                      // We found the new stackframe in the list of old stack frames
                stackFrameOld.setIndex (stackFrameNew.getIndex ());
                stackFrameOld.setScopeID(stackFrameNew.getScopeID());
				return true;                                                            // The stackframe was found in the list
			}
		}

		return false;
	}

	/**
	 * Check whether the new stackframe is in the list of old stackframes.
	 * Test for exact stackframe (exact means same description and same line number).
	 *
	 * @param stackFrameNew The stackframe to check whether he is already within the old stackframe list
	 * @param list          The list of old stackframes
	 * @return
	 *  - true if we have exactly this stackframe within the list
	 *  - false if we did not find the exact stackframe within the list
	 */
	private void markIdenticalStackFrames (Vector oldList, Vector newList) {
		int             i;
		PHPStackFrame 	stackFrameNew;

		resetAvailability (oldList);                                                    // Reset the availability flag of the old stack frames
		resetAvailability (newList);                                                    // Reset the availability flag of the old stack frames

		for (i = 0; i < newList.size (); i++) {											// For all stackList entries
			stackFrameNew = (PHPStackFrame) newList.get (i);

			if (isStackFrameInList (stackFrameNew, oldList)) {                          // Is this stackframe in the list
				stackFrameNew.setAvailable (true);										//
																						//
				break;
			}
		}
	}

	/**
	 *
	 * The stackList contains the currently read stackframes which were sent
	 * from DBG. The DBG interface holds a list of the active stack frames.
	 * This method replicates the 'static' stackframe list with the DBG stackframe list
	 * Replication is done in the following way:
	 * <ul>
	 * <li> It looks for new stackframes within the DBG stackframe list and
	 *      adds them to the 'static' list.
	 * <li> It looks for stackframes within the 'static' list, and removes them
	 * 		from the 'static' list in case they do not appear within the DBG list.
	 * <li> It looks for stackframes which are already existent and replicates the
	 * 		line number and the index number.
	 * <li> At the end, the 'static' stackframe list has to be sorted by the stackframes
	 * 		index numbers.
	 * </ul>
	 *
	 * Removes the unused stackframes from the list, or adds stackframes which
	 * are not yet in the list.
	 *
	 *
	 * @param stackList
	 */
	private void updateStackFrameList (Vector stackList) {
		int 			i;
		int             n;
		PHPStackFrame 	stackFrameNew;
		PHPStackFrame   stackFrameOld;
		PHPStackFrame[] newStackList;

		markIdenticalStackFrames (stackListOld, stackList);                     // Check whether the newly send stack frames can be found in the list
																				// of old stack frames

		for (i = 0; i < stackList.size (); i++) {								// For all stackList entries
			stackFrameNew = (PHPStackFrame) stackList.get(i);

			for (n = 0; n < stackListOld.size (); n++) {          				// For all StackFrames in the StackFrame list
				stackFrameOld = (PHPStackFrame) stackListOld.get (n);		 	//

				if (stackFrameOld.isAvailable ()) {                             // If this stack frame was already found in the new list skip it
					continue;
				}

				if (stackFrameNew.getDescription ().equals (stackFrameOld.getDescription ())) {// Did we find the sent stackframe within the list of old stackframes?
					stackFrameOld.setLineNumber (stackFrameNew.getLineNumber ());
					stackFrameOld.setIndex (stackFrameNew.getIndex ());
					stackFrameOld.setScopeID(stackFrameNew.getScopeID());

					stackFrameOld.setAvailable (true);							// And mark this stack frame as available
					stackFrameNew.setAvailable (true); 							// And mark this stack frame as available

					break;                                         				//  Yes, then break;
				}
			}

			if (!stackFrameNew.isAvailable ()) {                                // Did not find the new stackframe within the list?
				 stackFrameNew.setAvailable (true);								// Mark the stack frame as available and
				 stackListOld.add (stackFrameNew);                              //  then add the new stackframe
			}
		}

		// And now for removing unused stackframes from list

		for (n = 0; n < stackListOld.size(); n++) {
			stackFrameOld = (PHPStackFrame) stackListOld.get(n);

			if (!stackFrameOld.isAvailable()) {
				stackListOld.remove(n--);
			}
		}

		Collections.sort (stackListOld);										// Sort the 'static' stackframe list by the stackframe index numbers.
																				//
		newStackList = new PHPStackFrame[stackListOld.size ()];
		newStackList = (PHPStackFrame[]) stackListOld.toArray (newStackList);
		DBGStackList = newStackList;
	}

    /**
     * Read the response from DBG and process the frame
     *
	 * @return
	 * - The received command
	 * - or 0 if something was wrong
     */
	public int readResponse () throws IOException {
        int     bytesToRead            = 0;                         // The number of byte to read for the current DBG block
        int     nextFrame              = 0;                         // The current read position within entirePack
        int     i                      = 0;
        int     cmdReceived            = 0;
        int     stackIndex             = 0;
        boolean errorStack             = false;
        char[]  dbg_header_struct_read = new char[16];              // The buffer for the first 16 bytes of a block
        int[]   dbg_header_struct      = new int[4];                // The first four numbers (long) of a block
        int[]   dbg_bpl_tmp            = new int[10];
        int[]   dbg_frame              = new int[2];
        int[]   dbg_eval_tmp           = new int[3];
        int[]   dbg_src_tree_tmp       = new int[4];                //
        int[]   dbg_error_tmp          = new int[2];
        Vector  rawList                = new Vector();
        Vector  stackList              = new Vector();              // Intermediate stacklist which is build up in FRAME_STACK frame

        rawList.clear ();
        stackList.clear ();

		// Read from input
        while (readInput (dbg_header_struct_read, 16) != 0) {                               // Read 16 byte from input stream
			dbg_header_struct[0] = PHPDBGBase.Char4ToInt (dbg_header_struct_read, 0);  		// Debug sync header
            dbg_header_struct[1] = PHPDBGBase.Char4ToInt (dbg_header_struct_read, 4);  		// Command
            dbg_header_struct[2] = PHPDBGBase.Char4ToInt (dbg_header_struct_read, 8);       //
            dbg_header_struct[3] = PHPDBGBase.Char4ToInt (dbg_header_struct_read, 12);      // Bytes within this block

            if (dbg_header_struct[0] != 0x5953) {                                           // Check DBG sync bytes
                return 0;                                                                   // Wrong header
            }

            cmdReceived = dbg_header_struct[1];                                             // Get the command
            setLastCmd (cmdReceived);														// Store the info about the current command
            bytesToRead = dbg_header_struct[3];  											// Get the number of bytes to read for this block

			//System.out.println("Response Received: " + cmdReceived);
			char[] entirePack = new char[bytesToRead];                                      // Store the block data into buffer 'entirePack'

            if (bytesToRead > 0) {                                                          // If there is something within the frame
                if (readInput (entirePack, bytesToRead) < bytesToRead) {                    // Read the frame into the buffer
                    return 0;                                                               // We did not read enough bytes, error
                }
			}
			
			nextFrame = 0;                                                                  // Start with the first frame

			while (nextFrame < bytesToRead) {                                               // As long as we have something within this block
				dbg_frame[0] = PHPDBGBase.Char4ToInt (entirePack, nextFrame);				// The name of the frame
				dbg_frame[1] = PHPDBGBase.Char4ToInt (entirePack, nextFrame + 4);          	// The size of the frame
				nextFrame   += 8;                                                           // The next read position

				if (dbg_frame[1] == 0) {                                                    // Something within the frame?
					return 0;                                                               //  Nothing to read, error
				}

				switch (dbg_frame[0]) {
					case PHPDBGBase.FRAME_STACK:
						int[] dbg_stack_new = new int[4];                                       //

						dbg_stack_new[0] = PHPDBGBase.Char4ToInt (entirePack, nextFrame + 0);	// Source line number
						dbg_stack_new[1] = PHPDBGBase.Char4ToInt (entirePack, nextFrame + 4);	// Module number
						dbg_stack_new[2] = PHPDBGBase.Char4ToInt (entirePack, nextFrame + 8);	// Scope id
						dbg_stack_new[3] = PHPDBGBase.Char4ToInt (entirePack, nextFrame + 12);	// ID of description string

						if ((dbg_stack_new[1] != 0) && !errorStack) {
							PHPStackFrame newStack;

							stackIndex++;
							newStack = new PHPStackFrame (null,                                 // The thread
							                              getModByNo (dbg_stack_new[1]),        // The name of the module (file)
							                              dbg_stack_new[0],                     // The source line number
														  stackIndex,
														  getRawFrameData (entirePack,          // Get the string from this packet
														                   dbg_stack_new[3]),   // The frame ID for which we want the string
														  dbg_stack_new[1],   					// The module number
														  dbg_stack_new[2]);
							stackList.add (newStack);
						}

						errorStack = false;
						break;

					case PHPDBGBase.FRAME_SOURCE:                                                  	// Nothing to be done here
						break;                                                                      // TODO: what's with that frame? Something interesting

					case PHPDBGBase.FRAME_SRC_TREE:                                                 //
						dbg_src_tree_tmp[0] = PHPDBGBase.Char4ToInt (entirePack, nextFrame + 0);	// The parent module number
						dbg_src_tree_tmp[1] = PHPDBGBase.Char4ToInt (entirePack, nextFrame + 4);	// The parent line number (not used)
						dbg_src_tree_tmp[2] = PHPDBGBase.Char4ToInt (entirePack, nextFrame + 8);	// The module number
						dbg_src_tree_tmp[3] = PHPDBGBase.Char4ToInt (entirePack, nextFrame + 12);	// The filename number

						if (getModByNo (dbg_src_tree_tmp[2]).equals ("")) {
							String fileName;

							fileName = new String (getRawFrameData (entirePack, dbg_src_tree_tmp[3])); 	// Get the filename

							if (dbg_src_tree_tmp[2] != 0) {                                             // If there is a module number
								PHPDBGMod modNew;

								modNew = new PHPDBGMod (dbg_src_tree_tmp[2], fileName);                 // Create a module object

								DBGMods.add (modNew);                                                   // And store it to array
							}
						}
						break;

					case PHPDBGBase.FRAME_RAWDATA:                                                      // Nothing to be done here
						break;                                                                          //  FRAME_RAWDATA are processed within getRawFrameData

					case PHPDBGBase.FRAME_ERROR:														// An error frame
						errorStack       = true; 		  												// Yes, we have an error stack
						dbg_error_tmp[0] = PHPDBGBase.Char4ToInt (entirePack, nextFrame + 0);			// Error type
						dbg_error_tmp[1] = PHPDBGBase.Char4ToInt (entirePack, nextFrame + 4);			// Error message ID

						String error = "\n";                                                            //

						switch (dbg_error_tmp[0]) {														// Switch on error type
							case PHPDBGBase.E_ERROR: 	   		error += "[Error]"; 	   		break;
							case PHPDBGBase.E_WARNING:	   		error += "[Warning]";	   		break;
							case PHPDBGBase.E_PARSE:	   		error += "[Parse Error]";  		break;
							case PHPDBGBase.E_NOTICE:	  		error += "[Notice]";	   		break;
							case PHPDBGBase.E_CORE_ERROR: 		error += "[Core Error]";   		break;
							case PHPDBGBase.E_CORE_WARNING:		error += "[Core Warning]"; 		break;
							case PHPDBGBase.E_COMPILE_ERROR:	error += "[Compile Error]";		break;
							case PHPDBGBase.E_COMPILE_WARNING:	error += "[Compile Warning]";	break;
							case PHPDBGBase.E_USER_ERROR:		error += "[User Error]";		break;
							case PHPDBGBase.E_USER_WARNING:		error += "[User Warning]";		break;
							case PHPDBGBase.E_USER_NOTICE:		error += "[User Notice]";		break;
							default:							error += "[Unexpected Error]";	break;
						}

						error += ": ";
						error += new String (getRawFrameData (entirePack, dbg_error_tmp[1])); 			// Add the error string for this error message ID
						error += "\n";                                                                  // Append a CR

						PHPDebugCorePlugin.log (new DebugException (new Status (IStatus.WARNING,
						                                                        PHPDebugCorePlugin.PLUGIN_ID,
																				IStatus.OK,
																				error, null)));

						// To print errors on the console, I must execute a code in the
						// php context, that write the stderr... I didn't found a better way
						// TODO: Find a better way????

//						String codeExec= "";
//						codeExec= "fwrite(fopen('php://stderr', 'w'),\\\"" + error + "\\\");";
//						try {
//							evalBlock("eval(\"" + codeExec + "\");");
//						} catch (DebugException e) {
//							PHPDebugCorePlugin.log(e);
//						}
//
						if (!stopOnError) {                                                             // Is always false (Did not see where this is set to true!?)
							if (lastCommand.equals (PHPDBGBase.DBGA_CONTINUE)) {                        // If last command for PHP was a 'continue',
								continueExecution ();                                                   //  send continue again
							} else if (lastCommand.equals (PHPDBGBase.DBGA_STEPINTO)) {                 // If last command for PHP was a 'step into',
								stepInto ();                                                            //  send 'step into' again
							} else if (lastCommand.equals (PHPDBGBase.DBGA_STEPOUT)) {                  // If last command for PHP was a 'step out',
								stepOut ();                                                             //  send 'step out' again
							} else if (lastCommand.equals (PHPDBGBase.DBGA_STEPOVER)) {                 // If last command for PHP was a 'step over',
								stepOver ();                                                            //  send 'step over' again
							}
						}
						break;

					case PHPDBGBase.FRAME_EVAL:
						//String evalString;

						//evalString      = new String ("");
						dbg_eval_tmp[0] = PHPDBGBase.Char4ToInt (entirePack, nextFrame + 0); 			// istr
						dbg_eval_tmp[1] = PHPDBGBase.Char4ToInt (entirePack, nextFrame + 4); 			// iresult
						dbg_eval_tmp[2] = PHPDBGBase.Char4ToInt (entirePack, nextFrame + 8); 			// ierror

						evalRet    		= getRawFrameData (entirePack, dbg_eval_tmp[1]);                //
						//evalString 	= getRawFrameData (entirePack, dbg_eval_tmp[0]);                //
						break;

					case PHPDBGBase.FRAME_BPS:                                                          //
						break;                                                                          //

					case PHPDBGBase.FRAME_BPL:
						int[] dbg_bpl_new;

						dbg_bpl_new	   = new int[10];
						dbg_bpl_new[0] = PHPDBGBase.Char4ToInt (entirePack, nextFrame + 0);
						dbg_bpl_new[1] = PHPDBGBase.Char4ToInt (entirePack, nextFrame + 4);
						dbg_bpl_new[2] = PHPDBGBase.Char4ToInt (entirePack, nextFrame + 8);
						dbg_bpl_new[3] = PHPDBGBase.Char4ToInt (entirePack, nextFrame + 12);
						dbg_bpl_new[4] = PHPDBGBase.Char4ToInt (entirePack, nextFrame + 16);
						dbg_bpl_new[5] = PHPDBGBase.Char4ToInt (entirePack, nextFrame + 20);
						dbg_bpl_new[6] = PHPDBGBase.Char4ToInt (entirePack, nextFrame + 24);
						dbg_bpl_new[7] = PHPDBGBase.Char4ToInt (entirePack, nextFrame + 28);
						dbg_bpl_new[8] = PHPDBGBase.Char4ToInt (entirePack, nextFrame + 32);
						dbg_bpl_new[9] = PHPDBGBase.Char4ToInt (entirePack, nextFrame + 36);

						// look if breakpoint already exists in vector
						for (i = 0; i < DBGBPList.size (); i++) {
							dbg_bpl_tmp = (int[]) DBGBPList.get (i);

							if (dbg_bpl_tmp[8] == dbg_bpl_new[8]) {
								DBGBPList.remove (i);

								break;
							}
						}

						// add breakpoint to vector
						DBGBPList.add (dbg_bpl_new);
						copyToLastBP (dbg_bpl_new);

						// mod no returned?
						if (getModByNo (dbg_bpl_new[0]).equals ("")) {
							String fileName;

							fileName = new String (getRawFrameData (entirePack, dbg_bpl_new[2]));

							if (dbg_bpl_new[0] != 0) {
								PHPDBGMod modNew;

								modNew = new PHPDBGMod (dbg_bpl_new[0], fileName);

								DBGMods.add (modNew);
							}
						}
						break;

					case PHPDBGBase.FRAME_VER:
						break;

					case PHPDBGBase.FRAME_SID:
					  	sid = PHPDBGBase.Char4ToInt(entirePack, nextFrame + 0);
						break;

					case PHPDBGBase.FRAME_SRCLINESINFO:
						break;

					case PHPDBGBase.FRAME_SRCCTXINFO:
						break;

					case PHPDBGBase.FRAME_LOG:
						break;

					case PHPDBGBase.FRAME_PROF:
						break;

					case PHPDBGBase.FRAME_PROF_C:
						break;

					case PHPDBGBase.FRAME_SET_OPT:
						break;
				}

				nextFrame += dbg_frame[1];							// go to next frame
			}

			// Now process command
			switch(cmdReceived) {
				case PHPDBGBase.DBGC_REPLY:
					break;

				case PHPDBGBase.DBGC_STARTUP:
					break;

				case PHPDBGBase.DBGC_END:
					 sessionEnded = true; 
					 this.proxy.setTerminated(); 
					break;

				case PHPDBGBase.DBGC_BREAKPOINT:
					BPUnderHit   = getBPUnderHit ();
					updateStackFrameList (stackList);
					break;

				case PHPDBGBase.DBGC_STEPINTO_DONE:
				case PHPDBGBase.DBGC_STEPOVER_DONE:
				case PHPDBGBase.DBGC_STEPOUT_DONE:
				case PHPDBGBase.DBGC_EMBEDDED_BREAK:
				case PHPDBGBase.DBGC_PAUSE:
					BPUnderHit   = 1;
					updateStackFrameList (stackList);
					break;

				case PHPDBGBase.DBGC_ERROR:
					stackList.clear ();
					updateStackFrameList (stackList);
					break;

				case PHPDBGBase.DBGC_LOG:
					break;

				case PHPDBGBase.DBGC_SID:
					break;
			}
		}

		return cmdReceived;                                         // Return the command we received with this block
	}

    /**
     *
     */

	public PHPStackFrame[] getStackList() {
		return DBGStackList;
	}

	/**
	 * Reads from input buffer (response sent from DBG) the given number of chars
	 * into frame buffer.
	 *
	 * @param buffer  The frame buffer where to store the read data from DBG.
	 * @param bytes   The number of bytes (chars) which are to read from input stream.
	 * @return        The number of bytes actually read.
	 */
	private int readInput (char[] buffer, int bytes) throws IOException {
		int bytesRead = 0;											// Reset the bytes read counter

		for (int i = 0; i < bytes; i++) {                           // For the number of bytes we should read
			if (in.ready ()) {										// If input stream is ready for reading
				buffer[i] = (char) (in.read () & 0x00FF);           // Read a char and store only the least significant 8-bits
				bytesRead++;                                        // Increment the bytes read counter
			}
			else {                                                  // Input stream is not ready
				break;                                              // Break the loop
			}
		}

		return bytesRead;											// Return the number of bytes actually read
	}

	/**
	 * PHPProxy could stop the waiting for a response with this method.
	 *
	 */
	public void setShouldStop () {
		this.shouldStop = true;
	}

	/**
	 * @param milliseconds The maximum time in milliseconds we wait for something
	 *                     to be send from DBG.
	 * @return             - true if something was received from DBG
	 * 					   - false if nothing was send from DBG within the given time
	 *
	 */
	public boolean waitResponse (long milliseconds) throws IOException {
		long timeout;

		timeout = System.currentTimeMillis () + milliseconds;		// Calculate the system time till we wait.

		while (System.currentTimeMillis () < timeout) {             // Is waiting time running out?
			if (in.ready () || shouldStop) {                        //  No, so did we get something or should we stop now
				break;                                              //   Yes, break the waiting
			}
		}

		return in.ready ();                                         // true if we got something from DBG
	}
}
