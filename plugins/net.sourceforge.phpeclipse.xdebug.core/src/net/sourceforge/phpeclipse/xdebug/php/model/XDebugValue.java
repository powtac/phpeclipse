package net.sourceforge.phpeclipse.xdebug.php.model;

import org.eclipse.debug.core.DebugException;
import org.w3c.dom.Node;

public class XDebugValue extends XDebugAbstractValue {
	public XDebugValue(XDebugStackFrame variable, Node value) throws DebugException {
		super(variable, value);

		setValueString("uninitialized");
	}
}