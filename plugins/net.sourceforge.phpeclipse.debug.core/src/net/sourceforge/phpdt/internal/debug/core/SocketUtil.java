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
package net.sourceforge.phpdt.internal.debug.core;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Random;

/**
 * Utility class to find a port to debug on.
 */
public class SocketUtil {
	private static final Random fgRandom = new Random(System
			.currentTimeMillis());

	/**
	 * Returns a free port number on the specified host within the given range,
	 * or -1 if none found.
	 * 
	 * @param host
	 *            name or IP addres of host on which to find a free port
	 * @param searchFrom
	 *            the port number from which to start searching
	 * @param searchTo
	 *            the port number at which to stop searching
	 * @return a free port in the specified range, or -1 of none found
	 */
	public static int findUnusedLocalPort(String host, int searchFrom,
			int searchTo) {

		// First look at the five first ports starting on searchFrom
		for (int i = searchFrom; i <= searchFrom + 5; i++) {
			Socket s = null;
			int port = i;
			try {
				s = new Socket(host, port);
			} catch (ConnectException e) {
				return port;
			} catch (IOException e) {
			} finally {
				if (s != null) {
					try {
						s.close();
					} catch (IOException ioe) {
					}
				}
			}
		}
		// No free port found then look at 5 random ports numbers
		for (int i = 0; i < 5; i++) {
			Socket s = null;
			int port = getRandomPort(searchFrom, searchTo);
			try {
				s = new Socket(host, port);
			} catch (ConnectException e) {
				return port;
			} catch (IOException e) {
			} finally {
				if (s != null) {
					try {
						s.close();
					} catch (IOException ioe) {
					}
				}
			}
		}
		return -1;
	}

	private static int getRandomPort(int low, int high) {
		return (int) (fgRandom.nextFloat() * (high - low)) + low;
	}
}