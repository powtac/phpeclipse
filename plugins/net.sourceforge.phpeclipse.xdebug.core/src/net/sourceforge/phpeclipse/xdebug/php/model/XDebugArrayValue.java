package net.sourceforge.phpeclipse.xdebug.php.model;

import net.sourceforge.phpeclipse.xdebug.core.PHPDebugUtils;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XDebugArrayValue extends XDebugAbstractValue {
	private int NumChildren;

	public XDebugArrayValue(XDebugStackFrame variable, Node value) throws DebugException {
		super(variable, value);

		NumChildren = 0;
		if (!PHPDebugUtils.getAttributeValue(value, "numchildren").equals("")) {
			NumChildren = Integer.parseInt(PHPDebugUtils.getAttributeValue(value, "numchildren"));
		}		

		if (NumChildren > 0) {
			NodeList property = value.getChildNodes();
			renderValueString(""+property.getLength());
			IVariable[] Variables = new IVariable[property.getLength()];
			
			for (int i = 0; i<property.getLength(); i++) {
				Node propertyNode = property.item(i);
				Variables[i] = new XDebugVariable(variable, propertyNode);
			}
			
			setChildren(Variables);
		}
	}

	private void renderValueString(String data) throws DebugException  {
		if (data.equals("")) {
			setValueString("empty");
		} else {
			if ("array".equals(getReferenceTypeName())) {
				setValueString("array(" + NumChildren + ")");
			} else {
				setValueString(data);
			}
		}
	}
}