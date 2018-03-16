package net.sourceforge.phpeclipse.xdebug.core.xdebug;

import java.io.ByteArrayInputStream;
import java.io.IOException;

//import javax.print.attribute.standard.Fidelity;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.phpeclipse.xdebug.core.IPHPDebugEvent;
import net.sourceforge.phpeclipse.xdebug.core.PHPDebugUtils;
import net.sourceforge.phpeclipse.xdebug.core.XDebugCorePlugin;
import net.sourceforge.phpeclipse.xdebug.core.xdebug.XDebugResponse;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.w3c.dom.CDATASection;

/**
 * Listens to events from the XDebug and fires corresponding 
 * debug events.
 */

public class ResponseListener extends Job {
	private XDebugConnection fConnection;
	private ResponseList fResponseList;
	
	public ResponseListener(XDebugConnection connection) {
		super("XDebug Event Dispatch");
		setSystem(true);
		fConnection = connection;
		fResponseList = new ResponseList();
	}
		
	private void checkResponse(XDebugResponse response) {
		if (response.getStatus().equals("stopping") || response.getStatus().equals("stopped")) {
			this.cancel();
			fireEvent(IPHPDebugEvent.STOPPED, null);
		} else if (response.getStatus().equals("break") && response.getReason().equals("ok")){ 
			if (response.getCommand().equals("run")) {
				fireEvent(IPHPDebugEvent.BREAKPOINT_HIT, null);
			} else if (response.getCommand().equals("step_into")) {
				fireEvent(IPHPDebugEvent.STEP_END, null);
			} else if (response.getCommand().equals("step_over")) {
				fireEvent(IPHPDebugEvent.STEP_END, null);
			} else if (response.getCommand().equals("step_out")) {
				fireEvent(IPHPDebugEvent.STEP_END, null);
			} 
		} 
	}
	
	private void fireEvent(int detail, Object data) {
		DebugEvent event = new DebugEvent(this, DebugEvent.MODEL_SPECIFIC, detail);
		event.setData(data);
		DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] {event});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IStatus run(IProgressMonitor monitor) {
		String InputXML = "";
		while (!fConnection.isClosed()) {
			if (!monitor.isCanceled()) {
				try {
					InputXML = fConnection.readData();
				} catch (Exception e) {
					; //
				}
				if (InputXML != null) {
					XDebugCorePlugin.log(IStatus.INFO, InputXML);
					XDebugResponse response = new XDebugResponse(InputXML);
					if (response.getName() == "response") {
						fResponseList.add(response);
						checkResponse(response);
					}
				}
			}
		}
		return Status.OK_STATUS;
	}

	public XDebugResponse getResponse(int id) {
		return fResponseList.get(id);
	}
}