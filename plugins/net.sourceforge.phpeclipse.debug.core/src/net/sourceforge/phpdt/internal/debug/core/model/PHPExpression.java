/**********************************************************************
 Copyright (c) 2000, 2002 IBM Corp. and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html

 Contributors:
 IBM Corporation - Initial implementation
 Vicente Fernando - www.alfersoft.com.ar
 **********************************************************************/
package net.sourceforge.phpdt.internal.debug.core.model;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;

public class PHPExpression implements IExpression {

	private PHPVariable inspectionResult;

	private String expression;

	public PHPExpression(String expression, PHPVariable inspectionResult) {
		this.inspectionResult = inspectionResult;
		this.expression = expression;

	}

	public String getExpressionText() {
		return expression;
	}

	public IValue getValue() {
		return inspectionResult.getValue();
	}

	public IDebugTarget getDebugTarget() {
		return inspectionResult.getDebugTarget();
	}

	public void dispose() {

	}

	public String getModelIdentifier() {
		return this.getDebugTarget().getModelIdentifier();
	}

	public ILaunch getLaunch() {
		return this.getDebugTarget().getLaunch();
	}

	public Object getAdapter(Class arg0) {
		return null;
	}

}
