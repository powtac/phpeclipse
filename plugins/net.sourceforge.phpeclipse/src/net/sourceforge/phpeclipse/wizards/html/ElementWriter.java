/*
 * $Id: ElementWriter.java,v 1.3 2006-10-21 23:18:43 pombredanne Exp $
 * Copyright Narushima Hironori. All rights reserved.
 */
package net.sourceforge.phpeclipse.wizards.html;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * ElementWriter provides destribute xml code.
 */
public class ElementWriter {

	final public static int BEGIN_CHANGELINE = 1, END_CHANGELINE = 2;

	boolean trim = true;

	HashMap expandOptions = new HashMap();

	int defaultExpandOption;

	String indent;

	public ElementWriter() {
		this(0, "  ");
	}

	public ElementWriter(int defaultExpandOption, String indent) {
		this.defaultExpandOption = defaultExpandOption;
		this.indent = indent;
	}

	public void setExpandOption(String elementName, int value) {
		expandOptions.put(elementName, new Integer(value));
	}

	public int getExpandOption(String elementName) {
		if (expandOptions.containsKey(elementName)) {
			return ((Integer) expandOptions.get(elementName)).intValue();
		}
		return defaultExpandOption;
	}

	boolean isBeginChangeLine(String elementName) {
		return (getExpandOption(elementName) & BEGIN_CHANGELINE) != 0;
	}

	boolean isEndChangeLine(String elementName) {
		return (getExpandOption(elementName) & END_CHANGELINE) != 0;
	}

	public String expandTag(Element element) {
		StringBuffer buff = new StringBuffer();
		expandTag(element, 0, buff);
		return buff.toString();
	}

	public void writeTag(Element element, OutputStream out) throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(out);
		try {
			writer.write("<?xml version=\"1.0\"?>\n\n");
			writer.write(new ElementWriter().expandTag(element));
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	void expandTag(Element element, int level, StringBuffer buff) {
		expandIndent(level, buff);

		String elementName = element.getNodeName();
		buff.append('<' + elementName);
		NamedNodeMap attrs = element.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++) {
			Node n = attrs.item(i);
			String v = n.getNodeValue();
			if (v != null) {
				buff.append(' ' + n.getNodeName() + "=\""
						+ HTMLUtilities.escape(v) + "\"");
			}
		}

		boolean emptyElem = element.getChildNodes().getLength() == 0;
		if (emptyElem) {
			buff.append(" /");
		}
		buff.append('>');
		if (!emptyElem) {
			NodeList childElements = element.getChildNodes();
			if (isBeginChangeLine(elementName)) {
				buff.append('\n');
			}
			for (int i = 0; i < childElements.getLength(); i++) {
				Node node = childElements.item(i);
				if (node instanceof Element) {
					expandTag((Element) node, level + 1, buff);
				} else if (node instanceof Text) {
					String text = ((Text) node).getNodeValue();
					if (trim && (text = text.trim()).length() == 0) {
						continue;
					}
					buff.append(text);
				}
			}
			expandIndent(level, buff);
			buff.append("</" + elementName + '>');
		}
		// already inserted change line.
		if (isEndChangeLine(elementName)) {
			buff.append('\n');
		}
	}

	void expandIndent(int level, StringBuffer buff) {
		if (indent != null) {
			for (int i = 0; i < level; i++) {
				buff.append(indent);
			}
		}
	}

	public int getDefaultExpandOption() {
		return defaultExpandOption;
	}

	public void setDefaultExpandOption(int i) {
		defaultExpandOption = i;
	}

}