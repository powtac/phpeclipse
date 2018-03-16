package net.sourceforge.phpdt.internal.compiler.parser;

import net.sourceforge.phpdt.internal.compiler.ast.SingleTypeReference;

public class VariableInfo {
	static final public int LEVEL_UNDEFINED = 0;

	static final public int LEVEL_GLOBAL_VAR = 3;

	static final public int LEVEL_STATIC_VAR = 4;

	static final public int LEVEL_CLASS_UNIT = 5;

	static final public int LEVEL_FUNCTION_DEFINITION = 6;

	static final public int LEVEL_METHOD_DEFINITION = 7;

	public int level = LEVEL_UNDEFINED;

	int startPosition;

	public SingleTypeReference reference = null;

	public char[] typeIdentifier = null;

	public VariableInfo(int startPosition) {
		this(startPosition, LEVEL_UNDEFINED);
	}

	public VariableInfo(int startPosition, int level) {
		this.startPosition = startPosition;
		this.level = level;
	}
}