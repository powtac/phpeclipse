package net.sourceforge.phpeclipse.xdebug.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class PHPDebugUtils {
	public static String getAttributeValue(Node CurrentNode, String AttributeName) {
		String strValue = "";
		if (CurrentNode.hasAttributes()) {
			NamedNodeMap listAttribute = CurrentNode.getAttributes();
			Node attribute = listAttribute.getNamedItem(AttributeName);
			if (attribute != null)
				strValue = attribute.getNodeValue();
		}
		return strValue;
	}
	
	public static String escapeString(String string) {
		StringBuffer escString=new StringBuffer();
        Pattern pattern = Pattern.compile("[a-zA-Z0-9\\._-]");
        Matcher matcher;
		for (int i= 0; i<string.length(); i++) {
			char c=string.charAt(i);
			matcher = pattern.matcher(""+c);
			if(matcher.find())
				escString.append(c);
			else {
				int hexval=(byte)c;
				escString.append("%"+Integer.toHexString(hexval).toUpperCase());

			}
		}
		return escString.toString();
	}
	
	public static String unescapeString(String escString) {
		StringBuffer string=new StringBuffer();
		if (escString.indexOf('%')==-1)
			return escString;
		String[] s= escString.split("%");
		string.append(s[0]);
		for(int i=1 ; i<s.length;i++) {
			int c =Integer.parseInt(s[i].substring(0,2),16);
			string.append((char)c);
			if(s[i].length()>2)
			  string.append(s[i].substring(2));
			
		}
		return string.toString();

	}


}
