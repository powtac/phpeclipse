package net.sourceforge.phpdt.externaltools.variable;

/**********************************************************************
 Copyright (c) 2002 IBM Corp. and others. All rights reserved.
 This file is made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html
 �
 Contributors:
 **********************************************************************/

import org.eclipse.core.resources.IResource;

/**
 * Expands a workspace variable into the desired result format.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 */
public class WorkspaceExpander extends ResourceExpander {

	/**
	 * Create an instance
	 */
	public WorkspaceExpander() {
		super();
	}

	/*
	 * (non-Javadoc) Method declared on ResourceExpander.
	 */
	/* package */IResource expandUsingContext(ExpandVariableContext context) {
		return getWorkspaceRoot();
	}
}
