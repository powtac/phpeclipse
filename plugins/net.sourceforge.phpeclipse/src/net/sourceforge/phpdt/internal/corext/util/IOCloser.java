/*******************************************************************************
 * Copyright (c) 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package net.sourceforge.phpdt.internal.corext.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import net.sourceforge.phpeclipse.PHPeclipsePlugin;

// import net.sourceforge.phpdt.internal.ui.JavaPlugin;

public class IOCloser {
	public static void perform(Reader reader, InputStream stream) {
		try {
			rethrows(reader, stream);
		} catch (IOException e) {
			PHPeclipsePlugin.log(e);
		}
	}

	public static void rethrows(Reader reader, InputStream stream)
			throws IOException {
		if (reader != null) {
			reader.close();
			return;
		}
		if (stream != null) {
			stream.close();
			return;
		}
	}
}
