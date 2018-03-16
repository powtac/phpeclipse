package net.sourceforge.phpdt.internal.debug.core.watch;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IWatchExpressionResult;

public class PHPWatchExpressionResult implements IWatchExpressionResult {

	String text;

	IValue result;

	String[] err;

	public PHPWatchExpressionResult(String t, IValue v, String[] e) {
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
