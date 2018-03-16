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
package net.sourceforge.phpdt.internal.debug.core.breakpoints;

import net.sourceforge.phpdt.internal.debug.core.PHPDebugCorePlugin;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.Breakpoint;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * A breakpoint is capable of suspending the execution of a program at a
 * specific location when a program is running in debug mode. Each breakpoint
 * has an associated marker which stores and persists all attributes associated
 * with a breakpoint.
 * <p>
 * A breakpoint is defined in two parts:
 * <ol>
 * <li>By an extension of kind
 * <code>"org.eclipse.debug.core.breakpoints"</code></li>
 * <li>By a marker definition that corresponds to the above breakpoint
 * extension</li>
 * </ol>
 * <p>
 * For example, following is a definition of corresponding breakpoint and
 * breakpoint marker definitions. Note that the <code>markerType</code>
 * attribute defined by the breakpoint extension corresponds to the type of the
 * marker definition.
 * 
 * <pre>
 *  &lt;extension point=&quot;org.eclipse.debug.core.breakpoints&quot;&gt;
 *    &lt;breakpoint 
 *       id=&quot;com.example.Breakpoint&quot;
 *       class=&quot;com.example.Breakpoint&quot;
 *       markerType=&quot;com.example.BreakpointMarker&quot;&gt;
 *    &lt;/breakpoint&gt;
 *  &lt;/extension&gt;
 *  &lt;extension point=&quot;org.eclipse.core.resources.markers&quot;&gt;
 *    &lt;marker 
 *       id=&quot;com.example.BreakpointMarker&quot;
 *       super type=&quot;org.eclipse.debug.core.breakpointMarker&quot;
 *       attribute name =&quot;exampleAttribute&quot;&gt;
 *    &lt;/marker&gt;
 *  &lt;/extension&gt;
 * </pre>
 * 
 * <p>
 * The breakpoint manager instantiates persisted breakpoints by traversing all
 * markers that are a subtype of
 * <code>"org.eclipse.debug.core.breakpointMarker"</code>, and instantiating
 * the class defined by the <code>class</code> attribute on the associated
 * breakpoint extension. The method <code>setMarker</code> is then called to
 * associate a marker with the breakpoint.
 * </p>
 * <p>
 * Breakpoints may or may not be registered with the breakpoint manager, and are
 * persisted and restored as such. Since marker definitions only allow all or
 * none of a specific marker type to be persisted, breakpoints define a
 * <code>PERSISTED</code> attribute for selective persistence of breakpoints
 * of the same type.
 * </p>
 * 
 * @since 2.0
 */

public abstract class PHPBreakpoint extends Breakpoint implements IBreakpoint {

	/**
	 * Breakpoint attribute storing a breakpoint's hit count value (value
	 * <code>"net.sourceforge.phpeclipse.debug.hitCount"</code>). This
	 * attribute is stored as an <code>int</code>.
	 * 
	 * For DBG the hit count is really a skip count. Explanation: A hit count of
	 * e.g. 4 would break on the fourth occurence of the breakpoint. A skip
	 * count means skip the first four occurences of the breakpoint, and break
	 * on the fifth occurence.
	 */
	protected static final String HIT_COUNT = "net.sourceforge.phpeclipse.debug.hitCount"; //$NON-NLS-1$

	/**
	 * Breakpoint attribute storing a breakpoint's changeID. This is used for
	 * checking whether the breakpoint properties menu was finished with a
	 * OK-button. Which means a possible change of breakpoint condition or skip
	 * count. This is necessary because in method breakpointChanged in class
	 * PHPDebugTarget we need to know, whether the breakpoint has changed or not
	 * (breakpointChanged is called also when a PHP source file is modified and
	 * saved).
	 */
	protected static final String CHANGE_ID = "net.sourceforge.phpeclipse.debug.changeID"; //$NON-NLS-1$

	/**
	 * Breakpoint attribute storing a breakpoint's condition (value
	 * <code>"net.sourceforge.phpeclipse.debug.condition"</code>). This
	 * attribute is stored as an <code>string</code>.
	 */
	protected static final String CONDITION = "net.sourceforge.phpeclipse.debug.condition"; //$NON-NLS-1$

	/**
	 * Breakpoint attribute storing whether a breakpoint's condition is enabled
	 * or not (value
	 * <code>"net.sourceforge.phpeclipse.debug.conditionEnabled"</code>).
	 * This attribute is stored as an <code>boolean</code>.
	 */
	protected static final String CONDITION_ENABLED = "net.sourceforge.phpeclipse.debug.conditionEnabled"; //$NON-NLS-1$

	/**
	 * Breakpoint attribute storing the fully qualified name of the type this
	 * breakpoint is located in. (value
	 * <code>"net.sourceforge.phpeclipse.debug.typeName"</code>). This
	 * attribute is a <code>String</code>.
	 */
	protected static final String TYPE_NAME = "net.sourceforge.phpeclipse.debug.typeName"; //$NON-NLS-1$		

	/**
	 * Root breakpoint marker type (value
	 * <code>"org.eclipse.debug.core.breakpoint"</code>).
	 */
	public static final String BREAKPOINT_MARKER = DebugPlugin
			.getUniqueIdentifier()
			+ ".breakpointMarker"; //$NON-NLS-1$

	/**
	 * Line breakpoint marker type (value
	 * <code>"org.eclipse.debug.core.lineBreakpoint"</code>).
	 */
	public static final String LINE_BREAKPOINT_MARKER = DebugPlugin
			.getUniqueIdentifier()
			+ ".lineBreakpointMarker"; //$NON-NLS-1$

	/**
	 * Enabled breakpoint marker attribute (value
	 * <code>"org.eclipse.debug.core.enabled"</code>). The attribute is a
	 * <code>boolean</code> corresponding to the enabled state of a
	 * breakpoint.
	 * 
	 * @see org.eclipse.core.resources.IMarker#getAttribute(String, boolean)
	 */
	public static final String ENABLED = "org.eclipse.debug.core.enabled"; //$NON-NLS-1$

	/**
	 * Debug model identifier breakpoint marker attribute (value
	 * <code>"org.eclipse.debug.core.id"</code>). The attribute is a
	 * <code>String</code> corresponding to the identifier of the debug model
	 * a breakpoint is associated with.
	 */
	public static final String ID = "org.eclipse.debug.core.id"; //$NON-NLS-1$

	/**
	 * Registered breakpoint marker attribute (value
	 * <code>"org.eclipse.debug.core.registered"</code>). The attribute is a
	 * <code>boolean</code> corresponding to whether a breakpoint has been
	 * registered with the breakpoint manager.
	 * 
	 * @see org.eclipse.core.resources.IMarker#getAttribute(String, boolean)
	 */
	public static final String REGISTERED = "org.eclipse.debug.core.registered"; //$NON-NLS-1$	

	/**
	 * Persisted breakpoint marker attribute (value
	 * <code>"org.eclipse.debug.core.persisted"</code>). The attribute is a
	 * <code>boolean</code> corresponding to whether a breakpoint is to be
	 * persisted accross workspace invocations.
	 * 
	 * @see org.eclipse.core.resources.IMarker#getAttribute(String, boolean)
	 */
	public static final String PERSISTED = "org.eclipse.debug.core.persisted"; //$NON-NLS-1$		

	private int DBGBpNo = 0;

	public PHPBreakpoint() {
	}

	/**
	 * @see IBreakpoint#setMarker(IMarker)
	 */
	public void setMarker(IMarker marker) throws CoreException {
		super.setMarker(marker);
	}

	/**
	 * Add this breakpoint to the breakpoint manager, or sets it as
	 * unregistered.
	 */
	protected void register(boolean register) throws CoreException {
		if (register) {
			DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(this);
		} else {
			setRegistered(false);
		}
	}

	/**
	 * Execute the given workspace runnable
	 */
	protected void run(IWorkspaceRunnable wr) throws DebugException {
		try {
			ResourcesPlugin.getWorkspace().run(wr, null);
		} catch (CoreException e) {
			throw new DebugException(e.getStatus());
		}
	}

	/**
	 * @see IBreakpoint#getModelIdentifier()
	 */
	public String getModelIdentifier() {
		return PHPDebugCorePlugin.getUniqueIdentifier();
	}

	public void setDBGBpNo(int bpNo) {
		this.DBGBpNo = bpNo;
	}

	public int getDBGBpNo() {
		return this.DBGBpNo;
	}

	public int getHitCount() throws CoreException {
		return getMarker().getAttribute(HIT_COUNT, -1);
	}

	public void setHitCount(int hitCount) throws CoreException {
		if (hitCount > 0) {
			if (!isEnabled()) {
				getMarker().setAttribute(ENABLED, true);
			}
		}

		getMarker().setAttribute(HIT_COUNT, hitCount);
	}
}
