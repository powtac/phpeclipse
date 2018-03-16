/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	Vicente Fernando - www.alfersoft.com.ar - Initial implementation
**********************************************************************/
package net.sourceforge.phpdt.internal.debug.core;

public class PHPDBGBase {

	// Constants
	// php-engine commands/events
	public static final int DBGC_REPLY			= 0x0000;				// reply to previous DBGA_REQUEST request
	public static final int DBGC_STARTUP			= 0x0001;				// script startup
	public static final int DBGC_END				= 0x0002;				// script done
	public static final int DBGC_BREAKPOINT		= 0x0003;				// user definded breakpoint occured
	public static final int DBGC_STEPINTO_DONE	= 0x0004;				// step to the next statement is completed
	public static final int DBGC_STEPOVER_DONE	= 0x0005;				// step to the next statement is completed
	public static final int DBGC_STEPOUT_DONE		= 0x0006;				// step to the next statement is completed
	public static final int DBGC_EMBEDDED_BREAK	= 0x0007;				// breakpoint caused by DebugBreak() function
	public static final int DBGC_ERROR			= 0x0010;				// error occured
	public static final int DBGC_LOG				= 0x0011;				// logging support
	public static final int DBGC_SID				= 0x0012;				// send SID
	public static final int DBGC_PAUSE			= 0x0013;				// pause current session as soon as possible

	public static final char[] DBGA_CONTINUE		= IntToChar4(0x8001);	// php should continue run
	public static final char[] DBGA_STOP			= IntToChar4(0x8002);
	public static final char[] DBGA_STEPINTO		= IntToChar4(0x8003);
	public static final char[] DBGA_STEPOVER		= IntToChar4(0x8004);
	public static final char[] DBGA_STEPOUT		= IntToChar4(0x8005);
	public static final char[] DBGA_IGNORE		= IntToChar4(0x8006);
	public static final char[] DBGA_REQUEST		= IntToChar4(0x8010);	// debugger client requests some information from PHP engine

	public static final int FRAME_STACK			= 100000;				// "call:stack" - e.g. backtrace
	public static final int FRAME_SOURCE			= 100100;				// source text
	public static final int FRAME_SRC_TREE		= 100200;				// tree of source files
	public static final int FRAME_RAWDATA			= 100300;				// raw data or string
	public static final int FRAME_ERROR			= 100400;				// error notification
	public static final int FRAME_EVAL			= 100500;				// evaluating/watching
	public static final int FRAME_BPS				= 100600;				// set/remove breakpoint
	public static final int FRAME_BPL				= 100700;				// breakpoint(s) request = get the list
	public static final int FRAME_VER				= 100800;				// version request
	public static final int FRAME_SID				= 100900;				// session id info
	public static final int FRAME_SRCLINESINFO	= 101000;				// source lines info
	public static final int FRAME_SRCCTXINFO		= 101100;				// source contexts info
	public static final int FRAME_LOG				= 101200;				// logging
	public static final int FRAME_PROF			= 101300;				// profiler
	public static final int FRAME_PROF_C			= 101400;				// profiler counter/accuracy
	public static final int FRAME_SET_OPT			= 101500;				// set/update options

	public static final int CURLOC_SCOPE_ID 		= 1; 					// nested locations are 2,3... and so on in backward order,
																			//  so 2 represents most out-standing stack context
	public static final int GLOBAL_SCOPE_ID 		= -1; 					// it is global context, not stacked

	public static final char[] DBGSYNC 			= { 0, 0, (char) 89, (char) 83 };	// DBG syncronization chars

	// Session Types
	public static final int DBG_COMPAT			= 0x0001;
	public static final int DBG_JIT				= 0x0002;
	public static final int DBG_REQ				= 0x0003;
	public static final int DBG_EMB				= 0x0004;

	public static final int BPS_DELETED			= 0;
	public static final int BPS_DISABLED			= 1;
	public static final int BPS_ENABLED			= 2;
	public static final int BPS_UNRESOLVED		= 0x100;

	public static final int E_ERROR				= (1<<0L);
	public static final int E_WARNING				= (1<<1L);
	public static final int E_PARSE				= (1<<2L);
	public static final int E_NOTICE				= (1<<3L);
	public static final int E_CORE_ERROR			= (1<<4L);
	public static final int E_CORE_WARNING		= (1<<5L);
	public static final int E_COMPILE_ERROR		= (1<<6L);
	public static final int E_COMPILE_WARNING		= (1<<7L);
	public static final int E_USER_ERROR			= (1<<8L);
	public static final int E_USER_WARNING		= (1<<9L);
	public static final int E_USER_NOTICE			= (1<<10L);

	public PHPDBGBase() {
	}


	/**
	 * Copies the number of bytes from a source buffer to a destination buffer
	 * Destination index starts with 0 + tostart,
	 * and source index starts with 0.
	 *
	 * @param to        The destination buffer.
	 * @param from      The source buffer.
	 * @param bytes     The number of bytes which are to copy.
	 * @param tostart   The start index for the destination buffer.
	 */
	public static void copyCharsTo (char[] to, char[] from, int bytes, int tostart) {
		int i;

		for (i = 0; i < bytes; i++) {								// For the number of bytes which are to copy
			to[i + tostart] = from[i];								// Copy from destination to source (+startindex)
		}
	}

	/**
	 * Copies the number of bytes from a source buffer to a destination buffer
	 * Destination index starts with 0,
	 * and source index starts with 0.
	 *
	 * @param to        The destination buffer.
	 * @param from      The source buffer.
	 * @param bytes     The number of bytes which are to copy.
	 */
	public static void copyChars (char[] to, char[] from, int bytes) {
		copyCharsTo (to, from, bytes, 0);
	}

	/**
	 * Converts a four chars big endian value into
	 * an integer value
	 *
	 * @param ch        The buffer which contains the four chars which are to convert.
	 * @param startPos  The start position (of the four bytes) within the buffer.
	 */
	public static int Char4ToInt (char[] ch, int startPos) {
		int pos = startPos;
		int ret = 0;

		ret += CharToInt (ch[pos++]) << 24;
		ret += CharToInt (ch[pos++]) << 16;
		ret += CharToInt (ch[pos++]) <<  8;
		ret += CharToInt (ch[pos++]) <<  0;

		return ret;
	}

	/**
	 * @return The character which is converted to an integer value.
	 */
	public static int CharToInt (char ch) {
		return (int) (ch & 0x00FF);
	}

	/**
	 * Converts an integer value into a four char big endian number
	 *
	 * @param num The integer value which is to convert to a four char big endian number.
	 * @return    The four byte buffer with the big endian number.
	 */
	public static char[] IntToChar4 (int num) {
		char[] ret = new char[4];

		ret[0] = (char) ((num >> 24) & 0x00FF);
		ret[1] = (char) ((num >> 16) & 0x00FF);
		ret[2] = (char) ((num >>  8) & 0x00FF);
		ret[3] = (char) ((num >>  0) & 0x00FF);

		return ret;
	}

	/**
	 * Converts the chars of an array into a string in form of
	 * (byte value string) (byte value string) (byte value string) ...
	 *
	 * @param cha  The input buffer which contains the chars which are to convert.
	 * @return     The string which contains the bytes as strings.
	 *             E.g.: (123) (11) (46) (213) ...
	 */
	public static String CharArrayToString (char[] cha) {
		String ret = new String ();
		int    i;
		int    p;

		for (i = 0; i < cha.length; i++) {							// For all bytes within the input buffer
			p   = (int) cha[i];										// Convert the byte into an integer value
			ret = ret + "(" + String.valueOf (p) + ") ";			// Add the value
		}

		return ret;
	}

	/**
	 *
	 * @param cha  The input buffer which contains the chars which are to convert.
	 * @return     The byte array which contains the converted chars.
	 */
	public static byte[] CharArrayToByteArray (char[] cha) {
		byte[] ret = new byte[cha.length];							// The resulting byte array
		int    i;													// The index for the buffers

		for (i = 0; i < cha.length; i++) {							// For all chars within the input buffer
			ret[i] = (byte) cha[i];									//  Convert the character to a byte and store to buffer
		}

		return ret;													// Return the byte array
	}
}
