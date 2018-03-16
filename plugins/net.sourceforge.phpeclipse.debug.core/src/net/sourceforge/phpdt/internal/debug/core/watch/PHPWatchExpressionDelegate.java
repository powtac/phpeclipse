package net.sourceforge.phpdt.internal.debug.core.watch;

import java.util.Vector;

import net.sourceforge.phpdt.internal.debug.core.PHPDBGProxy;
import net.sourceforge.phpdt.internal.debug.core.model.PHPDebugTarget;
import net.sourceforge.phpdt.internal.debug.core.model.PHPStackFrame;
import net.sourceforge.phpdt.internal.debug.core.model.PHPValue;
import net.sourceforge.phpdt.internal.debug.core.model.PHPVariable;

import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IWatchExpressionDelegate;
import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.debug.core.model.IWatchExpressionResult;

/**
 * 
 */
public class PHPWatchExpressionDelegate implements IWatchExpressionDelegate {

	public void evaluateExpression(String expression, IDebugElement context,
			IWatchExpressionListener listener) {
		IWatchExpressionResult x;
		PHPDBGProxy dbg;
		PHPStackFrame s;

		if (!(context instanceof PHPStackFrame)) {
			x = new PHPWatchExpressionResult(expression, null, null);
			listener.watchEvaluationFinished(x);
			return;
		}

		dbg = ((PHPDebugTarget) context.getDebugTarget()).getPHPDBGProxy();
		s = (PHPStackFrame) context;

		try {
			PHPVariable result[] = dbg.eval(s, expression);

			if (result.length == 0) {
				x = new PHPWatchExpressionResult(expression, null, null);
			} else {
				switch (result[0].getReferenceType()) {
				case PHPValue.PEVT_ARRAY:
				case PHPValue.PEVT_OBJECT:
					result[0].setName(expression);
					reset(result[0]);
					break;
				}
				x = new PHPWatchExpressionResult(expression, result[0]
						.getValue(), null);
			}
		} catch (Exception e) {
			String[] s1;

			s1 = new String[1];
			s1[0] = e.toString();
			x = new PHPWatchExpressionResult(expression, null, s1);
		}

		listener.watchEvaluationFinished(x);
	}

	private void reset(PHPVariable variable) {
		PHPValue value;
		Vector variables;

		switch (variable.getReferenceType()) {
		case PHPValue.PEVT_ARRAY:
			value = (PHPValue) variable.getValue();
			variables = value.getChildVariables();
			for (int i = 0; i < variables.size(); i++) {
				PHPVariable var = (PHPVariable) variables.get(i);
				String name = var.getName();
				if (var.getLongName().equals(name)) {
					var.setName(name);
				} else {
					var.setParent(variable);
				}
				reset(var);
			}
			break;
		case PHPValue.PEVT_OBJECT:
			value = (PHPValue) variable.getValue();
			variables = value.getChildVariables();
			for (int i = 0; i < variables.size(); i++) {
				PHPVariable var = (PHPVariable) variables.get(i);
				var.setParent(variable);
				reset(var);
			}
			break;
		}
	}

}
