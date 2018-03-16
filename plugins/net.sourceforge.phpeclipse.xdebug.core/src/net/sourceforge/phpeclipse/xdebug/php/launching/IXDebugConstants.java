package net.sourceforge.phpeclipse.xdebug.php.launching;

/**
 * Constants for the PDA debugger.
 */
public interface IXDebugConstants {
	/**
	 * Unique identifier for the PHP debug model (value 
	 * <code>et.sourceforge.phpeclipse.debug.</code>).
	 */
	public static final String ID_PHP_DEBUG_MODEL = "net.sourceforge.phpeclipse.xdebug.php";
	
	/**
	 * Launch configuration key. Value is a PHPProject name
	 * program. The path is a string representing a full path
	 * to a perl program in the workspace. 
	 */
	public static final String ATTR_PHP_PROJECT = ID_PHP_DEBUG_MODEL + ".ATTR_PDA_PROFECT";
	/**
	 * Launch configuration key. Value is a php program.
	 * The path is a string representing a relative path
	 * to a php program in the project. 
	 */
	public static final String ATTR_PHP_FILE = ID_PHP_DEBUG_MODEL + ".ATTR_PDA_FILE";

	public static final String ATTR_PHP_DEFAULT_INTERPRETER = ID_PHP_DEBUG_MODEL + ".ATTR_PHP_DEFAULT_INTERPRETER";

	public static final String ATTR_PHP_INTERPRETER = ID_PHP_DEBUG_MODEL + ".ATTR_PHP_INTERPRETER";

	public static final String ATTR_PHP_DEFAULT_DEBUGPORT = ID_PHP_DEBUG_MODEL + ".ATTR_PHP_DEFAULT_DEBUGPORT";

	public static final String ATTR_PHP_DEBUGPORT = ID_PHP_DEBUG_MODEL + ".ATTR_PHP_DEBUGPORT";

	public static final String ATTR_PHP_IDE_ID = ID_PHP_DEBUG_MODEL + ".ATTR_PHP_IDE_ID";

	public static final String ATTR_PHP_PATHMAP = ID_PHP_DEBUG_MODEL + ".ATTR_PHP_PATHMAP";
}