package net.sourceforge.phpeclipse.xdebug.php.model;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.w3c.dom.Node;

public class XDebugBooleanValue extends XDebugAbstractValue {
	public XDebugBooleanValue(XDebugStackFrame variable, Node value) throws DebugException {
		super(variable, value);

		renderValueString(rowValue);
	}
	
	public boolean supportsValueModification() {
		return true;
	}

	public boolean setValue(String expression) throws DebugException {
		if (isValid(expression)) {
			renderValueString(expression);
			fireEvent(new DebugEvent(this, DebugEvent.CHANGE, DebugEvent.CONTENT));
			return true;
		}
		return false;
	}

	private void renderValueString(String data) {
		if (data.equals("0") || data.toLowerCase().equals("false")) { 
			setValueString("false");
		} else if (data.equals("1") || data.toLowerCase().equals("true")) {
			setValueString("true");
		}
	}

	private boolean isValid(String expression) {
		int value = -1;
		try {
			value = Integer.parseInt(expression);
		} catch (NumberFormatException e) {
			expression = expression.toLowerCase();
			if (expression.equals("true") || expression.equals("false"))
				return true;
			else
				return false;
		}
		if ((value >= 0)&& (value <= 1))
			return true;
		return false;
	}
	
	public boolean verifyValue(String expression) {
		return isValid(expression);
	}
}