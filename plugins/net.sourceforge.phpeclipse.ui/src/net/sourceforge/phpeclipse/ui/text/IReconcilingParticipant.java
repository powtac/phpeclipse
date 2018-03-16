/*
 * Copyright (c) 2004 Christopher Lenz and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Christopher Lenz - initial API
 * 
 * $Id: IReconcilingParticipant.java,v 1.2 2006-10-21 23:13:54 pombredanne Exp $
 */

package net.sourceforge.phpeclipse.ui.text;

/**
 * Interface for classes participating in reconciling.
 */
public interface IReconcilingParticipant {

	/**
	 * Called after reconciling has been finished.
	 */
	void reconciled();

}
