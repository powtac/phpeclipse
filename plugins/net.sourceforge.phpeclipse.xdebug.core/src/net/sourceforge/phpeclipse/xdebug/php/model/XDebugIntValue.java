package net.sourceforge.phpeclipse.xdebug.php.model;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.w3c.dom.Node;

public class XDebugIntValue extends XDebugAbstractValue {
	public XDebugIntValue(XDebugStackFrame stackFrame, Node value) throws DebugException {
		super(stackFrame, value);

		if (isValid(rowValue)) {
			setValueString(rowValue);
		}
	}
	
	public boolean supportsValueModification() {
		return true;
	}
		
	public boolean setValue(String expression) throws DebugException {
		if (isValid(expression)) {
			setValueString(expression);
			fireEvent(new DebugEvent(this, DebugEvent.CHANGE, DebugEvent.CONTENT));
			return true;
		}
		
		return false;
	}

	private boolean isValid(String expression) {
		try {
			Integer.parseInt(expression);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public boolean verifyValue(String expression) {
		return isValid(expression);
	}
}