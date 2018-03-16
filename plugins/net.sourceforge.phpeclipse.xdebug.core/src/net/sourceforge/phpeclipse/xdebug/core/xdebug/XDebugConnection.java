package net.sourceforge.phpeclipse.xdebug.core.xdebug;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import net.sourceforge.phpeclipse.xdebug.core.Base64;
import net.sourceforge.phpeclipse.xdebug.core.PHPDebugUtils;
import net.sourceforge.phpeclipse.xdebug.core.XDebugCorePlugin;

import org.eclipse.core.runtime.IStatus;

/**
 * @author Christian Perkonig
 *
 */
public class XDebugConnection {
	private int fTransactionID;
	private Socket fDebugSocket;
	private OutputStreamWriter fDebugWriter;
	private DataInputStream fDebugReader;
	private boolean fInitialized;
	private boolean fIsClosed;
	private String fSessionID;
	
	public String getSessionID() {
		return fSessionID;
	}
	
	public boolean isInitialized() {
		return fInitialized;
	}
	
	public boolean isClosed() {
		return fIsClosed;
	}

	public XDebugConnection(Socket debugSocket) {
		fDebugSocket = debugSocket;
		fTransactionID = 0;
		fInitialized = false;
		try {
			fDebugWriter = new OutputStreamWriter(debugSocket.getOutputStream(), "UTF8");
			fDebugReader = new DataInputStream(debugSocket.getInputStream()); 
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		fIsClosed = false;

		String initString = readData();
		XDebugCorePlugin.log(IStatus.INFO,initString);

		int startIdx = initString.indexOf("idekey=\"");
		if (startIdx == -1)
			return;
		startIdx += 8;
		int endIdx=initString.indexOf('"',startIdx);
		if (endIdx==-1)
			return;
		fSessionID = initString.substring(startIdx,endIdx);
		fInitialized = true;
	}
	
	protected String readData()	{
		if (fIsClosed)
			return null;
		
        byte byteBuffer[]=null,b;
		int count=0;
		
		try {
			while ((b =fDebugReader.readByte()) != 0) {
				count = count * 10 + b - '0';
			}
			byteBuffer = new byte[count];
			int readCount=0;
			int attempts=0;
			while ((count >0) && (attempts <5)) {
				int rc=fDebugReader.read(byteBuffer,readCount,count);
				count-=rc;
				readCount+=rc;
				if (count>65530)
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
					}
				else
					attempts++;
			}
			
			fDebugReader.readFully(byteBuffer,readCount,count);
			
			if((b= fDebugReader.readByte())!=0) // reads the NULL Byte at the end;
				System.out.println("NULL-Byte missing!!"); 
		} catch (IOException e) {
			if (e instanceof EOFException == false) {
				if (!fIsClosed) {
					e.printStackTrace();
				}
			}
			return null;
		}
		return new String(byteBuffer);
	}
	
	private /*XDebugResponse*/ int sendRequest(String command, String arguments) {
		return _sendRequest(command, arguments);
	}

	private synchronized int _sendRequest(String command, String arguments) {
		if (fDebugSocket == null) {
			return 0;
		}
		
		XDebugCorePlugin.log(IStatus.INFO,command+" -i "+fTransactionID+" "+arguments);
		synchronized (fDebugSocket) {
			try {
				fDebugWriter.write(command);
				fDebugWriter.write(" -i " + fTransactionID);
				if (!"".equals(arguments))
					fDebugWriter.write(" " + arguments);
				fDebugWriter.write(0);
				fDebugWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
	        }
		}

		return fTransactionID++;
	}

	public /*XDebugResponse*/ int eval(String Expression) {
		String encoded = Base64.encodeBytes(Expression.getBytes());
		
		return sendRequest("eval", "-- "+encoded);
	}

	public /*XDebugResponse*/ int featureGet(String featureName) {
		return sendRequest("feature_get","-n "+featureName);
	}

	public int  featureSet(String featureName, String value) {
		return sendRequest("feature_set","-n "+featureName + " -v " + value);
	}

	public /*XDebugResponse*/ int  breakpointSetOld(String file, int lineNumber) {
		String arg = "-t line -f file://"+PHPDebugUtils.escapeString(file)+" -n " + lineNumber;
		return sendRequest("breakpoint_set", arg);		
	}
	
	public /*XDebugResponse*/ int  breakpointSet(String file, int lineNumber, int hitCount) {
		String arg = "-t line -f file://"+PHPDebugUtils.escapeString(file)+" -n " + lineNumber;
		if (hitCount > 0) {
			arg += " -h " + hitCount;	
		}
		return sendRequest("breakpoint_set", arg);		
	}
	
	public int  breakpointGet(int id) {
		String arg = "-d " + id;
		
		return sendRequest("breakpoint_get", arg);		
	}
	
	public /*XDebugResponse*/ int  breakpointRemove(int id) {
		return sendRequest("breakpoint_set", "-d " + id);
	}

	public /*XDebugResponse*/ int  stackGet(/*int Level*/) {
		return sendRequest("stack_get", "");			
	}
	
	public void stepOver() {
		sendRequest("step_over", "");
	}

	public void stepInto() {
		sendRequest("step_into", "");
	}

	public void stepOut() {
		sendRequest("step_out", "");
	}

	public void run() {
		sendRequest("run", "");
	}

	public void stop() {
		sendRequest("stop", "");
	}

	public /*XDebugResponse*/ int  propertySet(String Name, String Value) {
		String str = Base64.encodeBytes(Value.getBytes());
		int len = str.length();

		return sendRequest("property_set", "-n " + Name + " -d 0 -l " + len + " -- " + str);
	}

	public /*XDebugResponse*/ int  contextGet(int Level, int Type) {
		return sendRequest("context_get", "-d " + Level + " -c " + Type);
	}

	public int setVarValue(String Name, String Value) {
		return propertySet(Name, Value);
	}
	
	public void close() {
		if (!fIsClosed) {
			fIsClosed = true;
			try {
				fDebugSocket.close();
				fDebugReader.close();
				//fDebugReader = null;
				fDebugWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}	
}