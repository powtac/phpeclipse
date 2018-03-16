package net.sourceforge.phpeclipse.xdebug.php.model;

import org.eclipse.debug.core.DebugException;
import org.w3c.dom.Node;

public class XDebugResourceValue extends XDebugAbstractValue {
	public XDebugResourceValue(XDebugStackFrame variable, Node value) throws DebugException {
		super(variable, value);
	}

	public void renderValueString(String data) {
		setValueString("\"" + data + "\"");
	}
}