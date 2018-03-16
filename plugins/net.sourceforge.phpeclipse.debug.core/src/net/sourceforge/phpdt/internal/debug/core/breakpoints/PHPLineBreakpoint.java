/**********************************************************************
 Copyright (c) 2000, 2002 IBM Corp. and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html

 Contributors:
 Vicente Fernando - Initial implementation - www.alfersoft.com.ar
 **********************************************************************/
package net.sourceforge.phpdt.internal.debug.core.breakpoints;

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;

/**
 * A breakpoint that can be located at a specific line of source code.
 */
public class PHPLineBreakpoint extends PHPBreakpoint implements IBreakpoint,
		ILineBreakpoint {

	private static final String PHP_LINE_BREAKPOINT = "net.sourceforge.phpeclipse.debug.core.phpLineBreakpointMarker"; //$NON-NLS-1$

	public PHPLineBreakpoint() {
	}

	public PHPLineBreakpoint(IResource resource, int lineNumber, int charStart,
			int charEnd, int hitCount, boolean add, Map attributes)
			throws DebugException {
		this(resource, lineNumber, charStart, charEnd, hitCount, add,
				attributes, PHP_LINE_BREAKPOINT);
	}

	public PHPLineBreakpoint(IResource resource, int lineNumber, int hitCount,
			boolean add, Map attributes) throws DebugException {
		this(resource, lineNumber, -1, -1, hitCount, add, attributes,
				PHP_LINE_BREAKPOINT);
	}

	protected PHPLineBreakpoint(final IResource resource, final int lineNumber,
			final int charStart, final int charEnd, final int hitCount,
			final boolean add, final Map attributes, final String markerType)
			throws DebugException {
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {

				// create the marker
				setMarker(resource.createMarker(markerType));

				// add attributes
				addLineBreakpointAttributes(attributes, getModelIdentifier(),
						true, lineNumber, charStart, charEnd, hitCount);

				// set attributes
				ensureMarker().setAttributes(attributes);

				// add to breakpoint manager if requested
				register(add);
			}
		};
		run(wr);
	}

	public void addLineBreakpointAttributes(Map attributes,
			String modelIdentifier, boolean enabled, int lineNumber,
			int charStart, int charEnd, int hitCount) {
		attributes.put(IBreakpoint.ID, modelIdentifier);
		attributes.put(IBreakpoint.ENABLED, new Boolean(enabled));
		attributes.put(IMarker.LINE_NUMBER, new Integer(lineNumber));
		if (charStart != -1) {
			attributes.put(IMarker.CHAR_START, new Integer(charStart));
			attributes.put(IMarker.CHAR_END, new Integer(charEnd));
		}
		attributes.put(TYPE_NAME, "typeName");
		attributes.put(PHPBreakpoint.HIT_COUNT, new Integer(hitCount));
		attributes.put(PHPBreakpoint.CONDITION, new String(""));
		attributes.put(PHPBreakpoint.CONDITION_ENABLED, new Boolean(false));
		attributes.put(PHPBreakpoint.CHANGE_ID, new Integer(0));
	}

	/**
	 * @see ILineBreakpoint#getLineNumber()
	 */
	public int getLineNumber() throws CoreException {
		return ensureMarker().getAttribute(IMarker.LINE_NUMBER, -1);
	}

	/**
	 * @see ILineBreakpoint#getCharStart()
	 */
	public int getCharStart() throws CoreException {
		return ensureMarker().getAttribute(IMarker.CHAR_START, -1);
	}

	/**
	 * @see ILineBreakpoint#getCharEnd()
	 */
	public int getCharEnd() throws CoreException {
		return ensureMarker().getAttribute(IMarker.CHAR_END, -1);
	}

	/**
	 * Returns the type of marker associated with Java line breakpoints
	 */
	public static String getMarkerType() {
		return PHP_LINE_BREAKPOINT;
	}

	public int getHitCount() throws CoreException {
		return ensureMarker().getAttribute(PHPBreakpoint.HIT_COUNT, 1);
	}

	public int getChangeID() throws CoreException {
		return ensureMarker().getAttribute(CHANGE_ID, 1);
	}

	public void setChangeID(int changeID) throws CoreException {
		ensureMarker().setAttribute(CHANGE_ID, changeID);
	}

	public String getCondition() throws CoreException {
		return ensureMarker().getAttribute(CONDITION, "");
	}

	public void setCondition(String condition) throws CoreException {
		ensureMarker().setAttribute(CONDITION, condition);
	}

	public void setConditionEnabled(boolean enabled) throws CoreException {
		ensureMarker().setAttribute(CONDITION_ENABLED, enabled);
	}

	public boolean isConditionEnabled() throws CoreException {
		return ensureMarker().getAttribute(CONDITION_ENABLED, false);
	}
}