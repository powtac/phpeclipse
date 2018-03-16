package net.sourceforge.phpeclipse.builder;

import net.sourceforge.phpeclipse.obfuscator.PHPIdentifier;

/**
 * 
 */
public class PHPIdentifierLocation extends PHPIdentifier implements Comparable {
	final public static int UNDEFINED_MATCH = 0;

	final public static int PATTERN_MATCH = 1;

	final public static int EXACT_MATCH = 2;

	private int fMatch;

	private String fClassname;

	private String fFilename;

	private int fOffset;

	private int fPHPDocLength;

	private int fPHPDocOffset;

	private String fUsage;

	public PHPIdentifierLocation(String identifier, int type, String filename) {
		this(identifier, type, filename, null);
	}

	public PHPIdentifierLocation(String identifier, int type, String filename,
			String classname) {
		super(identifier, type);
		fFilename = filename;
		fClassname = classname;
		fOffset = -1;
		fPHPDocLength = -1;
		fPHPDocOffset = -1;
		fUsage = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof PHPIdentifierLocation)) {
			return false;
		}
		return super.equals(obj)
				&& fFilename.equals(((PHPIdentifierLocation) obj).fFilename);
	}

	/**
	 * @return
	 */
	public String getClassname() {
		return fClassname;
	}

	/**
	 * @return
	 */
	public String getFilename() {
		return fFilename;
	}

	/**
	 * @return
	 */
	public int getOffset() {
		return fOffset;
	}

	/**
	 * @return
	 */
	public int getPHPDocLength() {
		return fPHPDocLength;
	}

	/**
	 * @return
	 */
	public int getPHPDocOffset() {
		return fPHPDocOffset;
	}

	/**
	 * @return
	 */
	public String getUsage() {
		return fUsage;
	}

	/**
	 * @param string
	 */
	public void setClassname(String string) {
		fClassname = string;
	}

	/**
	 * @param string
	 */
	public void setFilename(String string) {
		fFilename = string;
	}

	/**
	 * @param i
	 */
	public void setOffset(int i) {
		fOffset = i;
	}

	/**
	 * @param i
	 */
	public void setPHPDocLength(int i) {
		fPHPDocLength = i;
	}

	/**
	 * @param i
	 */
	public void setPHPDocOffset(int i) {
		fPHPDocOffset = i;
	}

	/**
	 * @param string
	 */
	public void setUsage(String string) {
		fUsage = string;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String result = null;
		switch (fMatch) {
		case UNDEFINED_MATCH:
			result = " [";
			break;
		case PATTERN_MATCH:
			result = " [pattern include][";
			break;
		case EXACT_MATCH:
			result = " [exact include][";
			break;
		default:
			result = "";
		}
		return super.toString() + result + fFilename + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		PHPIdentifierLocation i = (PHPIdentifierLocation) o;
		if (fMatch > i.fMatch) {
			return -1;
		} else if (fMatch < i.fMatch) {
			return 1;
		}
		return fFilename.compareTo(i.fFilename);
	}

	/**
	 * @return Returns the match.
	 */
	public int getMatch() {
		return fMatch;
	}

	/**
	 * @param match
	 *            The match to set.
	 */
	public void setMatch(int match) {
		fMatch = match;
	}
}