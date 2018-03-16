package net.sourceforge.phpeclipse.xdebug.php.model;


import net.sourceforge.phpeclipse.xdebug.php.model.XDebugVariable;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IWatchExpressionDelegate;
import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.debug.core.model.IWatchExpressionResult;
import org.w3c.dom.Node;

public class XDebugWatchExpressionDelegate implements IWatchExpressionDelegate {
	public void evaluateExpression(String expression, IDebugElement context, IWatchExpressionListener listener) {
		IWatchExpressionResult x = new XDebugWatchExpressionResult(expression, null, null);

		/* Active debug session */
		if (context instanceof XDebugStackFrame) {
			XDebugStackFrame frame = (XDebugStackFrame)context;
			Node evalProperty = null;
				
			try {	
				evalProperty = frame.eval(expression);
			} catch (Exception e) {
				// 
				e.printStackTrace();
			}

			XDebugVariable variable = null;
			try {
				variable = new XDebugVariable(frame, evalProperty);
				x = new XDebugWatchExpressionResult(expression, variable.getValue(), null);
			} catch (DebugException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		listener.watchEvaluationFinished(x);
	}
}