package net.sourceforge.phpeclipse.xdebug.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
//import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.IDebugEventFilter;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.jface.util.SafeRunnable;

import net.sourceforge.phpeclipse.xdebug.core.xdebug.XDebugConnection;
import net.sourceforge.phpeclipse.xdebug.php.model.XDebugTarget;

public class XDebugProxy {
	private XDebugConnection fConnection;
	private ServerSocket fProxyServerSocket;
	private ProxyListenerJob fProxyListener;
	private boolean fTerminate;
	private int fProxyPort;
	private ListenerMap fEventListeners;
	private boolean fIsRunning;
	
	class ProxyListenerJob extends Job {
		public ProxyListenerJob() {
			super("XDebug Proxy Connection Dispatch");
			setSystem(true);	
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		protected IStatus run(IProgressMonitor monitor) {
			boolean error;
			Socket socket = null;
			
			while (!fTerminate) {
				error = false;
				socket = null;
				if (monitor.isCanceled()) return Status.CANCEL_STATUS;
				try {
					socket = fProxyServerSocket.accept();
				} catch (IOException e) {
					error = true;
				}
				if (!error) {
					XDebugCorePlugin.log(IStatus.INFO,"Proxy: someone tries to connect");
					
					fConnection = new XDebugConnection(socket);	
					if (fConnection.isInitialized()) {
						String IdeKey = fConnection.getSessionID();
						
						Object a = getEventListener(IdeKey);
						
						if (a instanceof XDebugTarget) {
							XDebugCorePlugin.log(IStatus.INFO, "<init idekey \"" + IdeKey + "\">");
						
							fireProxyEvent(IdeKey);
						} else {
							fConnection.close();
							fConnection = null;
						}
					}
				}	
			}
			return Status.OK_STATUS;
		}
	}
	
	/**
	 * Filters and dispatches events in a safe runnable to handle any
	 * exceptions.
	 */
	class EventNotifier implements ISafeRunnable {
		
		private IProxyEventListener fListener;
		
		/**
		 * @see org.eclipse.core.runtime.ISafeRunnable#handleException(java.lang.Throwable)
		 */
		public void handleException(Throwable exception) {
			IStatus status = new Status(IStatus.ERROR, XDebugCorePlugin.getUniqueIdentifier(), IStatus.ERROR, "An exception occurred while dispatching proxy", exception); //$NON-NLS-1$
			XDebugCorePlugin.log(status);
		}

		/**
		 * @see org.eclipse.core.runtime.ISafeRunnable#run()
		 */
		public void run() throws Exception {
			fListener.handleProxyEvent(/*fIdeKey,*/ fConnection);
		}
		
		/**
		 * Filter and dispatch the given events. If an exception occurs in one
		 * listener, events are still fired to subsequent listeners.
		 * 
		 * @param events debug events
		 */
		public void dispatch(String id) {
			fListener = (IProxyEventListener) getEventListener(id/*fIdeKey*/);
			SafeRunnable.run(this);
			//Platform.run(this);
			fListener = null;
		}

	}

	public XDebugProxy(int port) {
		fProxyPort = port;
	}
	
	public void start() {
		if (!fIsRunning) {
			try {
				fProxyServerSocket = new ServerSocket(fProxyPort);
				XDebugCorePlugin.log(IStatus.INFO,"Proxy listens on port "+fProxyPort);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			fTerminate = false;
			fProxyListener = new ProxyListenerJob();
			fProxyListener.schedule();
			fIsRunning = true;
		}
	}
	
	public void stop() {
		if (fIsRunning) {
			fProxyListener.cancel();
			fTerminate = true;
			try {
				fProxyServerSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			fIsRunning = false;
			XDebugCorePlugin.log(IStatus.INFO,"Proxy stopped");
		}
	}

	/**
	 * Adds the given listener to the collection of registered proxy
	 * event listeners. Has no effect if an identical listener is already
	 * registered.
	 *
	 * @param listener the listener to add

	 */
	public void addProxyEventListener(IProxyEventListener listener, String key) {
		if (fEventListeners == null) {
			fEventListeners = new ListenerMap(5);
		}
		fEventListeners.add(listener, key);
	}
	
	/**
	 * Removes the given listener from the collection of registered proxy
	 * event listeners. Has no effect if an identical listener is not already
	 * registered.
	 *
	 * @param listener the listener to remove
	 */
	public void removeProxyEventListener(IProxyEventListener listener, String key) {
		if (fEventListeners != null) {
			//((XDebugTarget)listener).stopped();
			fEventListeners.remove(listener, key);
			if (fEventListeners.size() == 0) {
				stop();
			}
		}
	}	
	
	/**
	 * Notifies all registered proxy event set listeners of the given
	 * proxy events. Events which are filtered by a registered debug event
	 * filter are not fired.
	 * 
	 * @param events array of debug events to fire
	 * @see IDebugEventFilter
	 * @see IDebugEventSetListener
	 * @since 2.0
	 */
	public void fireProxyEvent(String id) {
		EventNotifier fNotifier = new EventNotifier();
		fNotifier.dispatch(id);
	}
	
	/**
	 * Returns the collection of registered proxy event listeners
	 * 
	 * @return list of registered proxy event listeners, instances
	 *  of <code>IProxyEventListeners</code>
	 */
	/*private Map getEventListeners() {
		return fEventListeners.getListeners();
	}*/
	
	private Object getEventListener(String ideKey) {
		return fEventListeners.getListener(ideKey);
	}
		
	/**
	 * @return Returns the fProxyPort.
	 */
	public int getProxyPort() {
		return fProxyPort;
	}
}