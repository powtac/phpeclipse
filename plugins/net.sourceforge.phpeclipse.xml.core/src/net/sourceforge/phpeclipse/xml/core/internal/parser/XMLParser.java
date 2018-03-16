/*
 * Copyright (c) 2004 Christopher Lenz and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Christopher Lenz - initial API
 * 
 * $Id: XMLParser.java,v 1.2 2006-10-21 23:13:43 pombredanne Exp $
 */

package net.sourceforge.phpeclipse.xml.core.internal.parser;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sourceforge.phpeclipse.xml.core.internal.model.XMLDocument;
import net.sourceforge.phpeclipse.xml.core.internal.model.XMLElement;
import net.sourceforge.phpeclipse.xml.core.model.IXMLDocument;
import net.sourceforge.phpeclipse.xml.core.model.IXMLElement;
import net.sourceforge.phpeclipse.xml.core.parser.IProblem;
import net.sourceforge.phpeclipse.xml.core.parser.IProblemCollector;
import net.sourceforge.phpeclipse.xml.core.parser.IXMLParser;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX-based default implementation of the {@link IXMLParser} interface.
 * 
 * TODO This implementation doesn't do error recovery, as SAX doesn't allow it.
 * Maybe we partition the document and parse individual fragments so that errors
 * can be isolated to their source
 */
public class XMLParser implements IXMLParser {
	/**
	 * SAX content handler that builds a model of the XML document.
	 */
	class ModelBuilder extends DefaultHandler {
		/**
		 * The document model being built.
		 */
		XMLDocument document;

		/**
		 * The current top element. That is the element that has been most
		 * recently opened by a start tag.
		 */
		private XMLElement top;

		/**
		 * The SAX locator provided by the parser, used to calculate the source
		 * regions covered by elements.
		 */
		private Locator locator;

		/**
		 * Limits parsing time.
		 */
		private long timeout;

		/*
		 * @see org.xml.sax.ContentHandler#startDocument()
		 */
		public void startDocument() throws SAXException {
			timeout = System.currentTimeMillis() + 2000;
			document = new XMLDocument(source, systemId);
		}

		/*
		 * @see org.xml.sax.ContentHandler#startElement(String, String, String,
		 *      Attributes)
		 */
		public void startElement(String namespaceURI, String localName,
				String qName, Attributes atts) throws SAXException {
			if (System.currentTimeMillis() > timeout) {
				throw new SAXException("timeout");
			}

			XMLElement newTop = new XMLElement(source);
			newTop.setLocalName(localName);
			newTop.setNamespaceURI(namespaceURI);

			if (qName != null) {
				int colonIndex = qName.indexOf(':');
				if (colonIndex >= 0) {
					newTop.setPrefix(qName.substring(0, colonIndex));
				}
			}

			int offset = computeOffset(newTop, locator.getLineNumber(), locator
					.getColumnNumber());

			if (offset >= 0) {
				newTop.setSourceRegion(offset, 0);
			}
			if (top != null) {
				newTop.setParent(top);
			}
			top = newTop;
		}

		/*
		 * @see org.xml.sax.ContentHandler#endElement(String, String, String)
		 */
		public void endElement(String namespaceURI, String localName,
				String qName) throws SAXException {
			int length = computeLength(top, locator.getLineNumber(), locator
					.getColumnNumber());

			if (length >= 0) {
				top.setSourceRegion(top.getSourceRegion().getOffset(), length);
			}

			XMLElement previousTop = (XMLElement) top.getParent();
			if (previousTop != null) {
				previousTop.addChild(top);
			} else {
				// this is the root element
				document.setRoot(top);
			}
			top = previousTop;
		}

		/*
		 * @see org.xml.sax.ErrorHandler#error(SAXParseException)
		 */
		public void error(SAXParseException e) throws SAXException {
			if (problemCollector != null) {
				problemCollector.addProblem(createProblem(e, true));
			}
		}

		/*
		 * @see org.xml.sax.ErrorHandler#fatalError(SAXParseException)
		 */
		public void fatalError(SAXParseException e) throws SAXException {
			if (problemCollector != null) {
				problemCollector.addProblem(createProblem(e, true));
			}
		}

		/*
		 * @see org.xml.sax.ErrorHandler#warning(SAXParseException)
		 */
		public void warning(SAXParseException e) throws SAXException {
			if (problemCollector != null) {
				problemCollector.addProblem(createProblem(e, false));
			}
		}

		/*
		 * @see org.xml.sax.ContentHandler#setDocumentLocator(Locator)
		 */
		public void setDocumentLocator(Locator locator) {
			this.locator = locator;
		}

		/**
		 * Creates a <tt>IProblem</tt> instance based on the information
		 * accessible from the parse exception. This method estimates the exact
		 * location of the error based on the line and column numbers provided
		 * with the exception.
		 * 
		 * TODO Limit the location to the current top element
		 * 
		 * @param e
		 *            the SAX parse exception
		 * @param error
		 *            whether the problem is an error or a warning
		 * @return the created problem object
		 */
		private IProblem createProblem(SAXParseException e, boolean error) {
			int line = e.getLineNumber();
			int column = e.getColumnNumber();
			if (line < 0) {
				line = 0;
			}
			if (column < 1) {
				column = 1;
			}

			int offset = 0, length = 1;
			try {
				offset = getOffset(line, column);
				length = getLastCharColumn(line) - column;
			} catch (BadLocationException ble) {
				ble.printStackTrace();
			}

			return new DefaultProblem(e.getLocalizedMessage(), offset, offset
					+ length, line, error);
		}
	}

	// Instance Variables ------------------------------------------------------

	/**
	 * The associated problem collector.
	 */
	IProblemCollector problemCollector;

	/**
	 * The document containing the source that should be parsed.
	 */
	IDocument source;

	/**
	 * The system ID of the document to parse, if available. This is necessary
	 * to resolve relative external entities. Can be <tt>null</tt>.
	 */
	String systemId;

	// IXMLParser Implementation -----------------------------------------------

	/*
	 * @see IXMLParser#parse()
	 */
	public IXMLDocument parse() {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setValidating(false);

		try {
			SAXParser parser = factory.newSAXParser();

			InputSource in = new InputSource(new StringReader(source.get()));
			if (systemId != null) {
				in.setSystemId(systemId);
			}

			ModelBuilder builder = new ModelBuilder();
			parser.parse(in, builder);
			return builder.document;
		} catch (ParserConfigurationException e) {
			// TODO Throw CoreException or at least log the error
		} catch (SAXParseException e) {
			// Already handled by the ModelBuilder
		} catch (SAXException e) {
			// SAX exceptions that are not parsing errors
			// TODO Throw CoreException or at least log the error
		} catch (IOException e) {
			// TODO Throw CoreException or at least log the error
		}

		return null;
	}

	/*
	 * @see IProblemReporter#setProblemCollector(IProblemCollector)
	 */
	public void setProblemCollector(IProblemCollector problemCollector) {
		this.problemCollector = problemCollector;
	}

	/*
	 * @see IXMLParser#setSource(IDocument)
	 */
	public void setSource(IDocument source) {
		this.source = source;
	}

	/*
	 * @see IXMLParser#setSystemId(String)
	 */
	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	// Private Methods ---------------------------------------------------------

	/**
	 * Computes the exact length of the given element by searching for the
	 * offset of the last character of the end tag.
	 */
	int computeLength(XMLElement element, int line, int column) {
		try {
			int offset;
			if (column <= 0) {
				int lineOffset = source.getLineOffset(line);
				String endTag = getEndTag(element);

				IRegion result = findStringForward(lineOffset, endTag);
				if (result != null) {
					offset = result.getOffset() + endTag.length();
				} else {
					result = findStringForward(lineOffset, "/>"); //$NON-NLS-1$
					if (result == null) {
						offset = -1;
					} else {
						offset = result.getOffset() + 2;
					}
				}

				if ((offset < 0) || (getLine(offset) != line)) {
					offset = lineOffset;
				} else {
					offset++;
				}
			} else {
				offset = getOffset(line, column);
			}

			return offset - element.getSourceRegion().getOffset();
		} catch (BadLocationException e) {
			// ignore as the parser may be out of sync with the document during
			// reconciliation
		}

		return -1;
	}

	/**
	 * Computes the offset at which the specified elements start tag begins in
	 * the source.
	 */
	int computeOffset(XMLElement element, int line, int column) {
		try {
			int offset;
			String prefix = "<"; //$NON-NLS-1$
			if (column <= 0) {
				offset = getOffset(line, 0);
				int lastCharColumn = getLastCharColumn(line);
				String lineText = source.get(source.getLineOffset(line - 1),
						lastCharColumn);
				String startTag = getStartTag(element);

				int lastIndex = lineText.indexOf(startTag);
				if (lastIndex > -1) {
					offset += lastIndex + 1;
				} else {
					offset = getOffset(line, lastCharColumn);
					IRegion result = findStringBackward(offset - 1, prefix);
					offset = result.getOffset();
				}
			} else {
				offset = getOffset(line, column);
				IRegion result = findStringForward(offset - 1, prefix);
				offset = result.getOffset();
			}

			return offset;
		} catch (BadLocationException e) {
			// ignore as the parser may be out of sync with the document during
			// reconciliation
		}

		return -1;
	}

	private IRegion findStringBackward(int startOffset, String string)
			throws BadLocationException {
		int offset = startOffset;
		int length = string.length();

		String match;
		while (offset >= 0) {
			match = source.get(offset, length);
			if (match.equals(string)) {
				return new Region(offset, length);
			}
			offset -= 1;
		}

		return null;
	}

	private IRegion findStringForward(int startOffset, String string)
			throws BadLocationException {
		int offset = startOffset;
		int length = string.length();

		String match;
		int sourceLength = source.getLength();
		while (offset + length <= sourceLength) {
			match = source.get(offset, length);
			if (match.equals(string)) {
				return new Region(offset, length);
			}
			offset += 1;
		}

		return null;
	}

	/**
	 * Given an XML element, this method reconstructs the corresponding end tag
	 * of the element, including the namespace prefix if there was one.
	 * 
	 * @param element
	 *            the XML element for which the end tag should be contructed
	 * @return the end tag as string
	 */
	private String getEndTag(IXMLElement element) {
		StringBuffer buf = new StringBuffer("</"); //$NON-NLS-1$
		if (element.getPrefix() != null) {
			buf.append(element.getPrefix());
			buf.append(':');
		}
		buf.append(element.getLocalName());
		buf.append('>');

		return buf.toString();
	}

	/**
	 * Reconstructs and returns the start tag corresponding to the given XML
	 * element, excluding any attribute specifications or the closing
	 * <tt>&gt;</tt> character.
	 * 
	 * @param element
	 *            the XML element for which the start tag should be constructed
	 * @return the start tag as string, excluding everything after the tag name
	 *         itself
	 */
	private String getStartTag(IXMLElement element) {
		StringBuffer buf = new StringBuffer("<"); //$NON-NLS-1$
		if (element.getPrefix() != null) {
			buf.append(element.getPrefix());
			buf.append(':');
		}
		buf.append(element.getLocalName());

		return buf.toString();
	}

	int getOffset(int line, int column) throws BadLocationException {
		return source.getLineOffset(line - 1) + column - 1;
	}

	private int getLine(int offset) throws BadLocationException {
		return source.getLineOfOffset(offset) + 1;
	}

	int getLastCharColumn(int line) throws BadLocationException {
		String lineDelimiter = source.getLineDelimiter(line - 1);
		int lineDelimiterLength = (lineDelimiter != null) ? lineDelimiter
				.length() : 0;

		return source.getLineLength(line - 1) - lineDelimiterLength;
	}
}
