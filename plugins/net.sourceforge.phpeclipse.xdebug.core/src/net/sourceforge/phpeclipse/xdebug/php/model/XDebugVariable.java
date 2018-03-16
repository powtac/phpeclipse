/*
 * Created on 23.11.2004
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
public class XDebugVariable extends XDebugElement implements IVariable {
	private String fName;
	private XDebugStackFrame fFrame;
	private XDebugAbstractValue fValue;
	private String fFacet;
	
	/**
	 * Constructs a variable contained in the given stack frame
	 * with the given name.
	 * 
	 * @param frame owning stack frame
	 * @param name variable name
	 */
	public XDebugVariable(XDebugStackFrame frame, Node property) throws DebugException {
		super((XDebugTarget) frame.getDebugTarget());
		if (frame != null ) {
			fFrame = frame;
		}

		fName = PHPDebugUtils.getAttributeValue(property,"name");
		if ("".equals(fName)) {
			fName = PHPDebugUtils.getAttributeValue(property,"address");
		}
		
		fFacet = PHPDebugUtils.getAttributeValue(property, "facet");

		String typeName = PHPDebugUtils.getAttributeValue(property, "type");

		if (typeName.equals("int") ) 
			fValue = new XDebugIntValue(frame, property);
		else if (typeName.equals("float") ) 
			fValue = new XDebugFloatValue(frame, property);
		else if (typeName.equals("bool") ) 
			fValue = new XDebugBooleanValue(frame, property);
		else if (typeName.equals("string") )
			fValue = new XDebugStringValue(frame, property);
		else if (typeName.equals("array") )
			fValue = new XDebugArrayValue(frame, property);
		else if (typeName.equals("object") )
			fValue = new XDebugObjectValue(frame, property);
		else if (typeName.equals("resource") )
			fValue = new XDebugResourceValue(frame, property);
		else
			fValue = new XDebugValue(frame, property);
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#getValue()
	 */
	public IValue getValue() throws DebugException {
		return fValue;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#getName()
	 */
	public String getName() throws DebugException {
		return fName;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException {
		return fValue.getReferenceTypeName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#hasValueChanged()
	 */
	public boolean hasValueChanged() throws DebugException {
		return fValue.hasChanged();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#setValue(java.lang.String)
	 */
	public void setValue(String expression) throws DebugException {
		if (fFrame.setVariableValue(this, expression)) {
			fValue.setValue(expression);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#setValue(org.eclipse.debug.core.model.IValue)
	 */
	public void setValue(IValue value) throws DebugException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#supportsValueModification()
	 */
	public boolean supportsValueModification() {
		return fValue.supportsValueModification();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#verifyValue(java.lang.String)
	 */
	public boolean verifyValue(String expression) throws DebugException {
		/*return true; */return fValue.verifyValue(expression);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#verifyValue(org.eclipse.debug.core.model.IValue)
	 */
	public boolean verifyValue(IValue value) throws DebugException {
		return false;
	}
	
	public String getValueString() throws DebugException {
		return fValue.getValueString();
	}
	
	public String getVisibility() {
		return fFacet;
	}
}