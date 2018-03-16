package net.sourceforge.phpeclipse.xdebug.core.xdebug;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.phpeclipse.xdebug.core.PHPDebugUtils;
import net.sourceforge.phpeclipse.xdebug.core.XDebugCorePlugin;

import org.eclipse.core.runtime.IStatus;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class XDebugResponse {
	final public static String TYPE_INIT = "init";
	
	final public static String TYPE_RESPONSE = "response";
	
	final public static String TYPE_STREAM = "stream";

	private Node parentNode;
	private int fTransactionID = -1;
	private String fCommand = "";
	private String fStatus;
	private String fReason;
	private String fName;
	private boolean  fError;

	private String fValue;
	private String fType;
	private String fAddress;
	private String fIdeKey;
		
	public XDebugResponse(String XMLInput) {
		fTransactionID = -1;
		fCommand = "";
		fStatus = "";
		fReason = "";			
		fName= "";
		setParentNode(XMLInput);
	}

	private synchronized void setParentNode(String xmlInput) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		Document doc = null;
		
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		ByteArrayInputStream InputXMLStream = new ByteArrayInputStream(xmlInput.getBytes());
		try {
			doc = builder.parse(InputXMLStream);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		parentNode = doc.getFirstChild();
		
		String responseType = parentNode.getNodeName();
		if (responseType == TYPE_INIT) {
			fName = TYPE_INIT;
			parseInit(parentNode);
		} else if (responseType == TYPE_RESPONSE) {
			fName = TYPE_RESPONSE;
			parseResponse(parentNode);
		} else if (responseType == TYPE_STREAM) {
			fName = TYPE_STREAM;
			parseStream();
		} else {
			fName = null;
		}
	}
	
	private void parseInit(Node parentNode) {
		fIdeKey = getAttributeValue("idekey");
		
		/*int startIdx = initString.indexOf("idekey=\"");
		if (startIdx == -1)
			return;
		startIdx += 8;
		int endIdx=initString.indexOf('"',startIdx);
		if (endIdx==-1)
			return;
		fSessionID = initString.substring(startIdx,endIdx);*/
	}
	
	private void parseResponse(Node parentNode) {
		String idStr = getAttributeValue("transaction_id");
		if (!"".equals(idStr))
			fTransactionID = Integer.parseInt(idStr);
		fCommand = getAttributeValue("command");
		if (parentNode.hasChildNodes()) {
			Node child = parentNode.getFirstChild();
			if (child.getNodeName().equals("error")) {
				int code = Integer.parseInt(PHPDebugUtils.getAttributeValue(child, "code"));
				String text = (child.getFirstChild()).getNodeValue();
				XDebugCorePlugin.log(IStatus.ERROR," ERROR "+code+": "+text);
				fError = true;
				return;
			}
		}
		fError = false;
		
		fStatus = getAttributeValue("status");
		fReason = getAttributeValue("reason");

		if( fCommand.compareTo("eval") == 0 ) {
			try {
				Node property = parentNode.getFirstChild();

				NamedNodeMap listAttribute = property.getAttributes();
				Node attribute = listAttribute.getNamedItem("type");
				if (attribute !=null) {
					fType = attribute.getNodeValue();
				}

				Node attribute1 = listAttribute.getNamedItem("address");
				if (attribute1 !=null) {
					fAddress = attribute1.getNodeValue();
				}
				
				Node firstChild1 = (Node) property.getFirstChild();
				
				if( firstChild1 != null ) {
					fValue = firstChild1.getNodeValue();
				} else {
					fValue = "";
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		} else {
			try {
				CDATASection firstChild = (CDATASection) parentNode.getFirstChild();

				if( firstChild != null ) {
					fValue = parentNode.getFirstChild().getNodeValue();
				}
			} catch (Exception e) {
			}
		}
		
	}
	
	private void parseStream() {
		
	}
	
	
	public String getAttributeValue (String AttributeName) {
		String strValue = "";
		if (parentNode.hasAttributes()) {
			NamedNodeMap listAttribute = parentNode.getAttributes();
			Node attribute = listAttribute.getNamedItem(AttributeName);
			if (attribute !=null)
				strValue = attribute.getNodeValue();
		}
		return strValue;
	}
	
	public synchronized Node getParentNode(){
		return parentNode;
	}
	
	public /*synchronized*/ String getCommand() {
		return fCommand;
	}
	
	/*private*/public /*synchronized*/ String getName() {
		return fName;
	}
	
	public synchronized String getValue() {
		return fValue;
	}

	public synchronized String getReason() {
		return fReason;
	}

	public synchronized String getStatus() {
		return fStatus;
	}

	public synchronized int getTransactionID() {
		return fTransactionID;
	}
	
	public boolean isError() {
		return fError;
	}
}