package net.sourceforge.phpeclipse.xdebug.php.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IWatchExpressionResult;

public class XDebugWatchExpressionResult implements IWatchExpressionResult {
	private String text;
	private IValue result;
	private String[] err;

	public XDebugWatchExpressionResult(String t, IValue v, String[] e) {
		text = t;
		result = v;
		err = e;
	}

	public IValue getValue() {
		return result;
	}

	public boolean hasErrors() {
		return err != null;
	}

	public String[] getErrorMessages() {
		return err;
	}

	public String getExpressionText() {
		return text;
	}

	public DebugException getException() {
		return null;
	}
}