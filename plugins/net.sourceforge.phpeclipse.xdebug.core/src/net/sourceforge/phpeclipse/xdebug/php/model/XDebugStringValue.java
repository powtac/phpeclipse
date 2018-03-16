package net.sourceforge.phpeclipse.xdebug.php.model;

import net.sourceforge.phpeclipse.xdebug.core.Base64;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.w3c.dom.Node;

public class XDebugStringValue extends XDebugAbstractValue {
	public XDebugStringValue(XDebugStackFrame variable, Node value) throws DebugException {
		super(variable, value);

		rowValue = new String(Base64.decode(rowValue));

		setValueString(rowValue);
	}

	public boolean setValue(String expression) throws DebugException {
		setValueString(expression);
		fireEvent(new DebugEvent(this, DebugEvent.CHANGE, DebugEvent.CONTENT));
		return true;
	}

	public boolean supportsValueModification() {
		return true;
	}
}