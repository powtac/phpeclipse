/*
 * $Id: TableElementCellModifier.java,v 1.3 2006-10-21 23:18:43 pombredanne Exp $
 * Copyright Narushima Hironori. All rights reserved.
 */
package net.sourceforge.phpeclipse.wizards.html;

import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.Item;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * 
 */
public class TableElementCellModifier implements ICellModifier {

	DocumentBuilderFactory builderFactory;

	IPropertyChangeListener listener;

	public TableElementCellModifier(IPropertyChangeListener listener) {
		builderFactory = DocumentBuilderFactory.newInstance();
		this.listener = listener;
	}

	public boolean canModify(Object element, String property) {
		return getValue(element, property) != null;
	}

	public Object getValue(Object trElem, String property) {
		if (trElem instanceof Element) {
			Element e = (Element) trElem;
			if (e.getNodeName().equals("tr")) {
				int v = TableElementModel.toNumeric(property);
				Element[] cells = TableElementModel.chooseCellElements(e);
				if (v >= 0 && v < cells.length) {
					NodeList nodes = cells[v].getChildNodes();
					if (nodes.getLength() == 1) {
						Node n = nodes.item(0);
						if (n instanceof Text) {
							return n.getNodeValue();
						}
					}
				}
			}
		}
		return null;
	}

	public void modify(Object element, String property, Object value) {
		if (element instanceof Item) {
			element = ((Item) element).getData();
		}
		Element trElem = (Element) element;
		int index = TableElementModel.toNumeric(property);
		Element cellElem = TableElementModel.chooseCellElements(trElem)[index];

		NodeList nodes = cellElem.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			cellElem.removeChild(nodes.item(i));
		}
		Document doc = cellElem.getOwnerDocument();

		if (value instanceof String) {
			cellElem.appendChild(doc.createTextNode((String) value));
		}
		// notify listener
		if (listener != null) {
			String oldValue = nodes.item(0).getNodeValue();
			listener.propertyChange(new PropertyChangeEvent(this, property,
					(String) value, oldValue));
		}
	}

}
