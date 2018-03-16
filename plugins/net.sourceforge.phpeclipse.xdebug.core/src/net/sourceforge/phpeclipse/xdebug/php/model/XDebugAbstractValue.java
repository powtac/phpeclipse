 /* Created on 23.11.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.sourceforge.phpeclipse.xdebug.php.model;

import net.sourceforge.phpeclipse.xdebug.core.PHPDebugUtils;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.w3c.dom.Node;

/**
 * @author Axel
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

public /*abstract*/ class XDebugAbstractValue  extends XDebugElement implements IValue {
	private IVariable[] fVariables;
	private String fValueString;
	private String fTypeName;
	private boolean fhasChanged;
	protected String rowValue;
	
	public XDebugAbstractValue(XDebugStackFrame frame, Node value) throws DebugException  {
		super(frame == null ? null : (XDebugTarget)frame.getDebugTarget());

		fTypeName = PHPDebugUtils.getAttributeValue(value, "type");
		
		fVariables = new IVariable[0];

		rowValue = "";
		try {
			rowValue = value.getFirstChild().getNodeValue();
		} catch (NullPointerException e) {
			rowValue = "";
		}
	}
	
	public boolean hasChanged() {
		return fhasChanged;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException {
		return fTypeName;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getValueString()
	 */
	public String getValueString() throws DebugException {
		return fValueString;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#isAllocated()
	 */
	public boolean isAllocated() throws DebugException {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getVariables()
	 */
	public IVariable[] getVariables() throws DebugException {
		return fVariables;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#hasVariables()
	 */
	public boolean hasVariables() throws DebugException {
		return (fVariables.length > 0);
	}
	
	public boolean setValue(String expression) throws DebugException {
		return true;
	};
	
	protected boolean verifyValue(String expression) {
		return true;
	}
	
	protected boolean supportsValueModification() {
		return false;
	}
	
	protected void setValueString(String valueString) {
		fValueString = valueString;
	}

	protected void setChildren(IVariable[] newChildren) {
		fVariables = newChildren;
	}
}