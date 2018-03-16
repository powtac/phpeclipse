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

import java.util.Vector;

public class PHPDBGFrame {

	public static final int FRAME_HEADER_SIZE	= 8;				// Header consist of 4 byte frame type and 4 byte frame length
	private char[]          frameType			= new char[4];
	private Vector          frameData			= new Vector ();
	private int             frameSize			= 0;

	/**
	 * Construct a new frame.
	 *
	 * @param frameType The type of the frame which is created.
	 */
	public PHPDBGFrame (int frameType) {
		this.frameType = PHPDBGBase.IntToChar4 (frameType);
		frameSize     += FRAME_HEADER_SIZE;
	}

	/**
	 * Add an integer to the frame.
	 *
	 * @param num
	 */
	public void addInt (int num) {
		char[] newData = PHPDBGBase.IntToChar4 (num);               // Convert the integer to four bytes big endian
		frameData.add (newData);                                    // Add the four bytes to the frame data
		frameSize     += 4;                                         // Calculate the new fram size
	}

	/**
	 * Add a character to the frame.
	 *
	 * @param ch The character which is to add to the frame.
	 */
	public void addChar (char ch) {
		char[] newData = new char[1];

		newData[0] = ch;                                            //
		frameData.add (newData);                                    // Add the character to the frame data
		frameSize += 1;                                             // Calculate the new fram size
	}

	/**
	 * @param str
	 */
	public void addString (String str) {
		frameData.add (str);
		frameSize += str.length ();
	}

	/**
	 * Get the size of the frame, including the frame header.
	 *
	 * @return The size of the entire frame.
	 */
	public int getSize () {
		return frameSize;
	}

	/**
	 * Return the size of the frame, which is the number of all bytes
	 * without the 8 byte from frame header.
	 *
	 * @return The size of the frame (without the frame header).
	 */
	public int getSizeOfData () {
		return frameSize - FRAME_HEADER_SIZE;
	}

	/**
	 * Get the header of this frame.
	 *
	 * @return The eight char array which forms the header of this frame.
	 */
	public char[] getHeader () {
		char[] ret = new char[FRAME_HEADER_SIZE];                   // Allocate 8 chars for the header

		PHPDBGBase.copyChars (ret, frameType, 4);                   // The first four chars are the frame type
		PHPDBGBase.copyCharsTo (ret, PHPDBGBase.IntToChar4 (getSizeOfData ()), 4, 4); // The second four chars is for the size of the data area

		return ret;													// Return the header
	}

	/**
	 * Get the data array of this frame
	 *
	 * TODO Finish commenting
	 *
	 * @return The char array which holds the data of this frame.
	 */
	public char[] getFrameData () {
		char[] ret	= new char[getSizeOfData ()];                   						// The frame data (data without the frame header)
		int pos		= 0;																	// The current position for the 'ret' array
		int i;                                                                              // The current position for the frame data list

		for (i = 0; i < frameData.size (); i++) {                                           // For frame data entries within the list
			if (frameData.get (i).getClass ().getName ().equals ("[C")) {                   // What kind of type is the frame data
				char[] conv = (char[]) frameData.get (i);                                   //

				PHPDBGBase.copyCharsTo (ret, conv, conv.length, pos);                       //
				pos        += conv.length;
			} else {
				if (frameData.get (i).getClass ().getName ().equals ("java.lang.String")) { //
					String conv = (String) frameData.get (i);                               //

					PHPDBGBase.copyCharsTo (ret, conv.toCharArray (), conv.length (), pos); //
					pos        += conv.length ();                                           //
				}
			}
		}

		return ret;																			// Return the data frame array
	}
}
