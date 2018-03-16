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
package net.sourceforge.phpdt.debug.core;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.phpdt.internal.debug.core.PHPDebugCorePlugin;
import net.sourceforge.phpdt.internal.debug.core.breakpoints.PHPLineBreakpoint;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * Provides utility methods for creating debug targets and breakpoints specific
 * to the PHP debug model.
 * <p>
 * Clients are not intended to instantiate or subclass this class; this class
 * provides static utility methods only.
 * </p>
 */
public class PHPDebugModel {

	/**
	 * Not to be instantiated.
	 */
	private PHPDebugModel() {
		super();
	}

	/**
	 * Returns the identifier for the PHP debug model plug-in
	 * 
	 * @return plugin identifier
	 */
	// public static String getPluginIdentifier() {
	// return PHPDebugCorePlugin.getUniqueIdentifier();
	// }
	/**
	 * Creates and returns a line breakpoint in the type with at the given line
	 * number. The marker associated with the breakpoint will be created on the
	 * specified resource. If a character range within the line is known, it may
	 * be specified by charStart/charEnd. If hitCount is > 0, the breakpoint
	 * will suspend execution when it is "hit" the specified number of times.
	 * 
	 * @param resource
	 *            the resource on which to create the associated breakpoint
	 *            marker
	 * @param lineNumber
	 *            the lineNumber on which the breakpoint is set - line numbers
	 *            are 1 based, associated with the source file in which the
	 *            breakpoint is set
	 * @param charStart
	 *            the first character index associated with the breakpoint, or
	 *            -1 if unspecified, in the source file in which the breakpoint
	 *            is set
	 * @param charEnd
	 *            the last character index associated with the breakpoint, or -1
	 *            if unspecified, in the source file in which the breakpoint is
	 *            set
	 * @param hitCount
	 *            the number of times the breakpoint will be hit before
	 *            suspending execution - 0 if it should always suspend
	 * @param register
	 *            whether to add this breakpoint to the breakpoint manager
	 * @param attributes
	 *            a map of client defined attributes that should be assigned to
	 *            the underlying breakpoint marker on creation, or
	 *            <code>null</code> if none.
	 * @return a line breakpoint
	 * @exception CoreException
	 *                If this method fails. Reasons include:
	 *                <ul>
	 *                <li>Failure creating underlying marker. The exception's
	 *                status contains the underlying exception responsible for
	 *                the failure.</li>
	 *                </ul>
	 * @since 2.0
	 */
	public static void createLineBreakpoint(IResource resource, int lineNumber,
			int charStart, int charEnd, int hitCount, boolean register,
			Map attributes) throws CoreException {
		if (attributes == null) {
			attributes = new HashMap(10);
		}
		new PHPLineBreakpoint(resource, lineNumber, charStart, charEnd,
				hitCount, true, attributes);
	}

	public static PHPLineBreakpoint createLineBreakpoint(IResource resource,
			int lineNumber, int hitCount, boolean register, Map attributes)
			throws CoreException {
		if (attributes == null) {
			attributes = new HashMap(10);
		}
		return new PHPLineBreakpoint(resource, lineNumber, hitCount, true,
				attributes);
	}

	/**
	 * Returns true if line breakpoint is already registered with the breakpoint
	 * manager for the given line number.
	 * 
	 * @param typeName
	 *            fully qualified type name
	 * @param lineNumber
	 *            line number
	 * @return true if line breakpoint is already registered with the breakpoint
	 *         manager for the given line number or <code>false</code> if no
	 *         such breakpoint is registered
	 * @exception CoreException
	 *                If this method fails.
	 */
	public static PHPLineBreakpoint lineBreakpointExists(IResource resource, int lineNumber)
			throws CoreException {
		String modelId = PHPDebugCorePlugin.PLUGIN_ID; // getPluginIdentifier();
		String markerType = PHPLineBreakpoint.getMarkerType();
		IBreakpointManager manager = DebugPlugin.getDefault()
				.getBreakpointManager();
		IBreakpoint[] breakpoints = manager.getBreakpoints(modelId);
		for (int i = 0; i < breakpoints.length; i++) {
			if (!(breakpoints[i] instanceof PHPLineBreakpoint)) {
				continue;
			}
			PHPLineBreakpoint breakpoint = (PHPLineBreakpoint) breakpoints[i];
			if (breakpoint.getLineNumber() == lineNumber) {
				if (breakpoint.getMarker().getResource().equals(resource)) {
					return breakpoint;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the preference store for this plug-in or <code>null</code> if
	 * the store is not available.
	 * 
	 * @return the preference store for this plug-in
	 */
	public static Preferences getPreferences() {
		PHPDebugCorePlugin deflt = PHPDebugCorePlugin.getDefault();
		if (deflt != null) {
			return deflt.getPluginPreferences();
		}
		return null;
	}

	/**
	 * Saves the preference store for this plug-in.
	 * 
	 * @return the preference store for this plug-in
	 */
	public static void savePreferences() {
		PHPDebugCorePlugin.getDefault().savePluginPreferences();
	}

	/**
	 * Creates and returns a debug target for the given VM, with the specified
	 * name, and associates the debug target with the given process for console
	 * I/O. The allow terminate flag specifies whether the debug target will
	 * support termination (<code>ITerminate</code>). The allow disconnect
	 * flag specifies whether the debug target will support disconnection (<code>IDisconnect</code>).
	 * The resume flag specifies if the target VM should be resumed on startup
	 * (has no effect if the VM was already running when the connection to the
	 * VM was esatbished). Launching the actual VM is a client responsibility.
	 * The debug target is added to the given launch.
	 * 
	 * @param launch
	 *            the launch the new debug target will be contained in
	 * @param vm
	 *            the VM to create a debug target for
	 * @param name
	 *            the name to associate with the VM, which will be returned from
	 *            <code>IDebugTarget.getName</code>. If <code>null</code>
	 *            the name will be retrieved from the underlying VM.
	 * @param process
	 *            the process to associate with the debug target, which will be
	 *            returned from <code>IDebugTarget.getProcess</code>
	 * @param allowTerminate
	 *            whether the target will support termianation
	 * @param allowDisconnect
	 *            whether the target will support disconnection
	 * @param resume
	 *            whether the target is to be resumed on startup. Has no effect
	 *            if the target was already running when the connection to the
	 *            VM was established.
	 * @return a debug target
	 * @see org.eclipse.debug.core.model.ITerminate
	 * @see org.eclipse.debug.core.model.IDisconnect
	 * @since 2.0
	 */
	/*
	 * public static IDebugTarget newDebugTarget(final ILaunch launch, final
	 * String name, final IProcess process) { final IDebugTarget[] target = new
	 * IDebugTarget[1]; IWorkspaceRunnable r = new IWorkspaceRunnable() { public
	 * void run(IProgressMonitor m) { target[0]= new PHPDebugTarget(launch,
	 * process); } }; try { ResourcesPlugin.getWorkspace().run(r, null); } catch
	 * (CoreException e) { //PHPDebugPlugin.log(e); } return target[0]; }
	 */
}
