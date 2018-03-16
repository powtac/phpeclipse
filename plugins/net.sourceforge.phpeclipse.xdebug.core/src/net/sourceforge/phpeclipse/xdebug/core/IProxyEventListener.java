package net.sourceforge.phpeclipse.xdebug.core;

import net.sourceforge.phpeclipse.xdebug.core.xdebug.XDebugConnection;


public interface IProxyEventListener {
	public void handleProxyEvent(/*String ideKey,*/ /*String initString,*/ XDebugConnection connectin);

}
