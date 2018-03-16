/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 �*
 * Contributors:
 *    IBM - Initial API and implementation
 **********************************************************************/
package net.sourceforge.phpdt.httpquery.config;

import net.sourceforge.phpdt.phphelp.PHPHelpPlugin;

/**
 * 
 */
public class Configuration implements IConfiguration, Comparable {
	private static final String MEMENTO_ID = "id";

	private static final String MEMENTO_NAME = "name";

	private static final String MEMENTO_USER = "user";

	private static final String MEMENTO_URL = "url";

	private static final String MEMENTO_PASSWORD = "password";

	private static final String MEMENTO_TYPE_ID = "type-id";

	protected String fId = "";

	protected String fName = "";

	protected String fUrl = "";

	protected String fPassword = "";

	protected String fUser = "";

	protected String fType = "";

	private static final char[] SCRAMBLING_TABLE = new char[] { 0, 1, 2, 3, 4,
			5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22,
			23, 24, 25, 26, 27, 28, 29, 30, 31, 114, 120, 53, 79, 96, 109, 72,
			108, 70, 64, 76, 67, 116, 74, 68, 87, 111, 52, 75, 119, 49, 34, 82,
			81, 95, 65, 112, 86, 118, 110, 122, 105, 41, 57, 83, 43, 46, 102,
			40, 89, 38, 103, 45, 50, 42, 123, 91, 35, 125, 55, 54, 66, 124,
			126, 59, 47, 92, 71, 115, 78, 88, 107, 106, 56, 36, 121, 117, 104,
			101, 100, 69, 73, 99, 63, 94, 93, 39, 37, 61, 48, 58, 113, 32, 90,
			44, 98, 60, 51, 33, 97, 62, 77, 84, 80, 85, 223, 225, 216, 187,
			166, 229, 189, 222, 188, 141, 249, 148, 200, 184, 136, 248, 190,
			199, 170, 181, 204, 138, 232, 218, 183, 255, 234, 220, 247, 213,
			203, 226, 193, 174, 172, 228, 252, 217, 201, 131, 230, 197, 211,
			145, 238, 161, 179, 160, 212, 207, 221, 254, 173, 202, 146, 224,
			151, 140, 196, 205, 130, 135, 133, 143, 246, 192, 159, 244, 239,
			185, 168, 215, 144, 139, 165, 180, 157, 147, 186, 214, 176, 227,
			231, 219, 169, 175, 156, 206, 198, 129, 164, 150, 210, 154, 177,
			134, 127, 182, 128, 158, 208, 162, 132, 167, 209, 149, 241, 153,
			251, 237, 236, 171, 195, 243, 233, 253, 240, 194, 250, 191, 155,
			142, 137, 245, 235, 163, 242, 178, 152 };

	/**
	 * Construct a Configuration with the defult type:
	 * <code>net.sourceforge.phpeclipse.wiki.editor.WikiEditorPlugin.HTTP_QUERY</code>
	 * 
	 */
	public Configuration() {
		this(PHPHelpPlugin.HTTP_QUERY); // default type
	}

	/**
	 * Construct a Configuration with a type
	 * 
	 * @param type
	 *            Example:
	 *            <code>net.sourceforge.phpeclipse.wiki.editor.WikiEditorPlugin.HTTP_QUERY</code>
	 * 
	 * @see net.sourceforge.phpeclipse.wiki.editor.WikiEditorPlugin
	 */
	public Configuration(String type) {
		this.fType = type;
	}

	public String getId() {
		return fId;
	}

	public String getName() {
		return fName;
	}

	public String getURL() {
		return fUrl;
	}

	public String getPassword() {
		return fPassword;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.monitor.internal.IConfiguration#getLocalPort()
	 */
	public String getUser() {
		return fUser;
	}

	/**
	 */
	public String getType() {
		return fType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.monitor.internal.IConfiguration#isRunning()
	 */
	public boolean isActive() {
		return ConfigurationManager.getInstance().isActive(this);
	}

	public void delete() {
		ConfigurationManager.getInstance().removeConfiguration(this);
	}

	public boolean isWorkingCopy() {
		return false;
	}

	public IConfigurationWorkingCopy getWorkingCopy() {
		return new ConfigurationWorkingCopy(this);
	}

	protected void setInternal(IConfiguration monitor) {
		fId = monitor.getId();
		fName = monitor.getName();
		fUrl = monitor.getURL();
		fPassword = monitor.getPassword();
		fUser = monitor.getUser();
		fType = monitor.getType();
	}

	protected void save(IMemento memento) {
		memento.putString(MEMENTO_ID, fId);
		memento.putString(MEMENTO_NAME, fName);
		memento.putString(MEMENTO_TYPE_ID, fType);
		memento.putString(MEMENTO_USER, fUser);
		memento.putString(MEMENTO_URL, fUrl);
		String result = 'A' + scramblePassword(fPassword);
		memento.putString(MEMENTO_PASSWORD, result);
	}

	protected void load(IMemento memento) {
		fId = memento.getString(MEMENTO_ID);
		if (fId == null) {
			fId = "";
		}
		fName = memento.getString(MEMENTO_NAME);
		if (fName == null) {
			fName = "";
		}
		fType = memento.getString(MEMENTO_TYPE_ID);
		if (fType == null) {
			fType = "";
		}
		fUser = memento.getString(MEMENTO_USER);
		if (fUser == null) {
			fUser = "";
		}
		fUrl = memento.getString(MEMENTO_URL);
		if (fUrl == null) {
			fUrl = "";
		}
		String result = memento.getString(MEMENTO_PASSWORD);

		if (result == null) {
			fPassword = "";
		} else {
			fPassword = scramblePassword(result.substring(1));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(fName);
		buffer.append(" - ");
		buffer.append(fUser);
		buffer.append(" - ");
		buffer.append(fUrl);
		buffer.append(" - ");
		buffer.append(fType);
		return buffer.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		if (o instanceof IConfiguration) {
			return fName.compareTo(((IConfiguration) o).getName());
		}
		return 1;
	}

	private static String scramblePassword(String password) {
		int length = password.length();
		char[] out = new char[length];
		for (int i = 0; i < length; i++) {
			char value = password.charAt(i);
			out[i] = SCRAMBLING_TABLE[value];
		}
		return new String(out);
	}

	public boolean isUserComplete() {
		if (fUser == null || fUser.equals("")) {
			return false;
		}
		if (fPassword == null || fPassword.equals("")) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof Configuration) {
			if (fName == null || ((Configuration) obj).fName == null) {
				return false;
			}
			return fName.equals(((Configuration) obj).fName);
		}
		return false;
	}
}