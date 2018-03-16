/*
 * $Id: TableElementModel.java,v 1.2 2006-10-21 23:18:43 pombredanne Exp $
 * Copyright Narushima Hironori. All rights reserved.
 */
package net.sourceforge.phpeclipse.wizards.html;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * TableElementModel
 */
public class TableElementModel {

	final static char[] CHAR_TABLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

	StringDivider stringDivider = new StringDivider();

	ElementWriter writer;

	DocumentBuilder docBuilder;

	Document document;

	Element tableElement;

	String[] columnProperties;

	public TableElementModel(String content, boolean parse)
			throws FactoryConfigurationError, ParserConfigurationException,
			SAXException, IOException {
		docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		if (parse) {
			initAsParse(content);
		} else {
			initModel(content);
		}
		columnProperties = createColumnProperties();

		// create elementWriter
		writer = new ElementWriter(0, null);
		writer.setExpandOption("caption", ElementWriter.END_CHANGELINE);
		writer.setExpandOption("table", ElementWriter.BEGIN_CHANGELINE
				| ElementWriter.END_CHANGELINE);
		writer.setExpandOption("thead", ElementWriter.BEGIN_CHANGELINE
				| ElementWriter.END_CHANGELINE);
		writer.setExpandOption("tfoot", ElementWriter.BEGIN_CHANGELINE
				| ElementWriter.END_CHANGELINE);
		writer.setExpandOption("tbody", ElementWriter.BEGIN_CHANGELINE
				| ElementWriter.END_CHANGELINE);
		writer.setExpandOption("tr", ElementWriter.END_CHANGELINE);
	}

	void initModel(String content) throws ParserConfigurationException,
			SAXException, IOException {
		StringReader strReader = new StringReader(content);
		InputSource inputSrc = new InputSource(strReader);

		document = docBuilder.parse(inputSrc);
		tableElement = document.getDocumentElement();

		Element[] rows = getRows();
		for (int i = 0; i < rows.length; i++) {
			Element[] cells = chooseCellElements(rows[i]);
			for (int j = 0; j < cells.length; j++) {
				Element cell = cells[j];
				if (!cell.hasChildNodes()) {
					cell.appendChild(document.createTextNode(""));
				}
			}
		}
	}

	public void initAsParse(String content)
			throws ParserConfigurationException, FactoryConfigurationError {
		// create new table model.
		document = docBuilder.newDocument();
		tableElement = document.createElement("table");

		String[][] cells = stringDivider.divide(content);
		if (cells.length > 0) {
			for (int i = 0; i < cells.length; i++) {
				String[] rows = cells[i];
				Element tr = document.createElement("tr");
				for (int j = 0; j < rows.length; j++) {
					Element e = document.createElement("td");
					e.appendChild(document.createTextNode(rows[j]));
					tr.appendChild(e);
				}
				tableElement.appendChild(tr);
			}

			setColumnCount(cells[0].length);
		} else {
			Element tr = document.createElement("tr");
			Element td = document.createElement("td");
			td.appendChild(document.createTextNode(""));
			tr.appendChild(td);
			tableElement.appendChild(tr);

			setColumnCount(1);
		}
	}

	String[] createColumnProperties() {
		int len = getColumnCount();
		String[] props = new String[len];
		for (int i = 0; i < len; i++) {
			props[i] = toColumnName(i);
		}
		return props;
	}

	public void setRowCount(int rowCount) {
		Element[] rows = getRows();
		if (rowCount > rows.length) {
			for (int i = rows.length; i < rowCount; i++) {
				tableElement.appendChild(createRowElement());
			}
		} else if (rowCount < rows.length) {
			for (int i = rowCount; i < rows.length; i++) {
				tableElement.removeChild(rows[i]);
			}
		}
	}

	public Element[] getRows() {
		ArrayList rows = new ArrayList();
		NodeList nodes = tableElement.getElementsByTagName("tr");
		for (int i = 0; i < nodes.getLength(); i++) {
			rows.add(nodes.item(i));
		}
		return (Element[]) rows.toArray(new Element[rows.size()]);
	}

	public int getRowCount() {
		return getRows().length;
	}

	Element createRowElement() {
		Element tr = document.createElement("tr");
		for (int i = 0, columnCount = getColumnCount(); i < columnCount; i++) {
			Element td = document.createElement("td");
			td.appendChild(document.createTextNode(""));
			tr.appendChild(td);
		}
		return tr;
	}

	public void setColumnCount(int newLength) {
		NodeList trs = tableElement.getElementsByTagName("tr");
		for (int i = 0; i < trs.getLength(); i++) {
			Element tr = (Element) trs.item(i);
			Element[] cells = chooseCellElements(tr);
			int colLen = cells.length;

			if (newLength > colLen) {
				for (int j = 0, len = newLength - colLen; j < len; j++) {
					Element cell = document.createElement("td");
					cell.appendChild(document.createTextNode(""));
					tr.appendChild(cell);
				}
			} else if (newLength < colLen) {
				for (int j = newLength; j < colLen; j++) {
					tr.removeChild(cells[j]);
				}
			}
		}
		columnProperties = createColumnProperties();
	}

	public int getColumnCount() {
		NodeList trs = tableElement.getElementsByTagName("tr");
		if (trs.getLength() > 0) {
			Element tr = (Element) trs.item(0);
			return chooseCellElements(tr).length;
		} else {
			return 0;
		}
	}

	public static Element[] chooseCellElements(Element tr) {
		NodeList nodeList = tr.getChildNodes();

		ArrayList result = new ArrayList();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node instanceof Element) {
				String nodeName = node.getNodeName();
				if (nodeName.equals("td") || nodeName.equals("th")) {
					result.add(node);
				}
			}
		}

		return (Element[]) result.toArray(new Element[result.size()]);
	}

	public String expandCodes() {
		return writer.expandTag(tableElement);
	}

	public static String toColumnName(int i) {
		StringBuffer buff = new StringBuffer();
		int u = i / CHAR_TABLE.length;
		if (u > 0) {
			buff.append(CHAR_TABLE[u - 1]);
		}
		buff.append(CHAR_TABLE[i % CHAR_TABLE.length]);
		return buff.toString();
	}

	/**
	 * Return index of char map. If can not parse values return -1.
	 */
	public static int toNumeric(String code) {
		int result = -1;
		for (int i = 0; i < code.length(); i++) {
			char c = code.charAt(i);
			int match = Arrays.binarySearch(CHAR_TABLE, c);
			if (match >= 0) {
				if (result == -1) {
					result = 0;
				}
				int v = match;
				int u = code.length() - 1 - i;
				if (u > 0) {
					v = CHAR_TABLE.length * u * (v + 1);
				}
				result += v;
			}
		}
		return result;
	}

	public void move(Element tr, int moveCount) {
		Element[] rows = getRows();
		int index = -1;
		for (int i = 0; i < rows.length; i++) {
			if (tr.equals(rows[i])) {
				index = i;
			}
		}
		if (index == -1) {
			throw new IllegalArgumentException(
					"Invalid row node (not countained in this table):" + tr);
		}
		if (moveCount > 0) {
			// move down;
			for (int i = index; i < moveCount + index && i < rows.length - 1; i++) {
				tableElement.insertBefore(rows[i + 1], rows[i]);
			}
		} else if (moveCount < 0) {
			// move up
			for (int i = index; i >= moveCount + index + 1 && i >= 1; i--) {
				tableElement.insertBefore(rows[index], rows[i - 1]);
			}
		} else {
			return;
		}
	}

	public void insertNewRowBefore(Element tr) {
		Element newRow = createRowElement();
		if (tr == null) {
			tableElement.appendChild(newRow);
		} else {
			tableElement.insertBefore(newRow, tr);
		}
	}

	public void removeRow(Element tr) {
		tableElement.removeChild(tr);
	}

	public String[] getColumnProperties() {
		return (String[]) columnProperties.clone();
	}

}
