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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

/**
 *
 * TODO: Added a try catch block in sendPacket to avoid an execption in case the PHP script
 * has finished and DBG isn't ready for receiving any new frame.
 * (This happens when mouse is hovering over variable or user switches to watch expression)
 * It is not clear why DBG isn't sending any SCRIPT_END event (or something similar),
 * or whether there is another way to check whether DBG is listening or not.
 * For the moment this is only a workaround.
 *
 */
public class PHPDBGPacket {

	private static final int 	PACKET_HEADER_SIZE	= 16;
	private char[]           	packetHeader		= new char[PACKET_HEADER_SIZE];
	private int 				packetSize;
	private Vector 				frames				= new Vector ();

	public PHPDBGPacket (char[] packetType) {
		PHPDBGBase.copyChars (packetHeader, PHPDBGBase.DBGSYNC, 4);
		PHPDBGBase.copyCharsTo (packetHeader, packetType, 4, 4);
	}

	public void addFrame (PHPDBGFrame frame) {
		frames.add (frame);
		packetSize += frame.getSize ();
	}

	public void sendPacket (OutputStream out) throws IOException {
		int 		i;
		PHPDBGFrame frame;

		PHPDBGBase.copyCharsTo (packetHeader, PHPDBGBase.IntToChar4 (packetSize), 4, 12);

		try {
			out.write (PHPDBGBase.CharArrayToByteArray (packetHeader));     			// Send packet header
			out.flush ();                                                               //

			for (i = 0; i < frames.size (); i++) {                         				// Send Frames
				frame = (PHPDBGFrame) frames.get (i);                      				// Header of frame

				out.write (PHPDBGBase.CharArrayToByteArray (frame.getHeader ()));       // Convert the char buffer to a byte buffer and send
				out.flush ();

				if (frame.getSizeOfData () > 0) {                                       // If there is a data frame
					out.write (PHPDBGBase.CharArrayToByteArray (frame.getFrameData ()));// Convert the data char buffer to a byte buffer and send
					out.flush ();
				}
			}
		}
		catch (Exception e) {
		}
	}
}
