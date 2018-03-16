package net.sourceforge.phpeclipse.obfuscator;

/**
 * Object which holds an PHP identifier name (i.e. class, function,
 * variable,...)
 * 
 */
public class PHPIdentifier {

	public final static int CLASS = 1;

	public final static int FUNCTION = 2;

	public final static int VARIABLE = 3;

	public final static int METHOD = 4;

	public final static int DEFINE = 5;

	public final static int CONSTRUCTOR = 6;

	public final static int GLOBAL_VARIABLE = 7;

	public final static int EXTENDS = 8;

	public final static int IMPLEMENTS = 9;

	private String fIdentifier;

	private int fType;

	public PHPIdentifier(String identifier, int type) {
		fType = type;
		fIdentifier = identifier;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof PHPIdentifier)) {
			return false;
		}
		return ((PHPIdentifier) obj).fType == fType
				&& ((PHPIdentifier) obj).fIdentifier.equals(fIdentifier);
	}

	public String getIdentifier() {
		return fIdentifier;
	}

	public int getType() {
		return fType;
	}

	public boolean isClass() {
		return fType == CLASS;
	}

	public boolean isFunction() {
		return fType == FUNCTION;
	}

	public boolean isVariable() {
		return fType == VARIABLE;
	}

	public boolean isMethod() {
		return fType == METHOD;
	}

	public boolean isDefine() {
		return fType == DEFINE;
	}

	public boolean isGlobalVariable() {
		return fType == GLOBAL_VARIABLE;
	}

	public boolean isConstructor() {
		return fType == CONSTRUCTOR;
	}

	public void setIdentifier(String fIdentifier) {
		this.fIdentifier = fIdentifier;
	}

	public void setType(int fType) {
		this.fType = fType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		switch (fType) {
		case CLASS:
			return "class - ";
		case CONSTRUCTOR:
			return "constructor - ";
		case DEFINE:
			return "define - ";
		case FUNCTION:
			return "function - ";
		case GLOBAL_VARIABLE:
			return "global variable - ";
		case METHOD:
			return "method - ";
		case VARIABLE:
			return "variable - ";
		}
		return "";
	}

}