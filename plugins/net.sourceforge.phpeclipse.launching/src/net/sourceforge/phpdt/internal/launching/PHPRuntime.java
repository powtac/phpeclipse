package net.sourceforge.phpdt.internal.launching;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.IPath;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class PHPRuntime {
	protected static PHPRuntime runtime;

	protected List installedInterpreters;

	protected PHPInterpreter selectedInterpreter;

	protected PHPRuntime() {
		super();
	}

	public static PHPRuntime getDefault() {
		if (runtime == null) {
			runtime = new PHPRuntime();
		}
		return runtime;
	}

	public PHPInterpreter getSelectedInterpreter() {
		if (selectedInterpreter == null) {
			loadRuntimeConfiguration();
		}
		return selectedInterpreter;
	}

	public PHPInterpreter getInterpreter(String installLocation) {
		Iterator interpreters = getInstalledInterpreters().iterator();
		while (interpreters.hasNext()) {
			PHPInterpreter each = (PHPInterpreter) interpreters.next();
			if (each.getInstallLocation().toString().equals(installLocation))
				return each;
		}

		return getSelectedInterpreter();
	}

	public void setSelectedInterpreter(PHPInterpreter anInterpreter) {
		selectedInterpreter = anInterpreter;
		saveRuntimeConfiguration();
	}

	public void addInstalledInterpreter(PHPInterpreter anInterpreter) {
		getInstalledInterpreters().add(anInterpreter);
		if (getInstalledInterpreters().size() == 1)
			setSelectedInterpreter((PHPInterpreter) getInstalledInterpreters()
					.get(0));

		saveRuntimeConfiguration();
	}

	public List getInstalledInterpreters() {
		if (installedInterpreters == null)
			loadRuntimeConfiguration();
		return installedInterpreters;
	}

	public void setInstalledInterpreters(List newInstalledInterpreters) {
		installedInterpreters = newInstalledInterpreters;
		if (installedInterpreters.size() > 0)
			setSelectedInterpreter((PHPInterpreter) installedInterpreters
					.get(0));
		else
			setSelectedInterpreter(null);
	}

	protected void saveRuntimeConfiguration() {
		writeXML(getRuntimeConfigurationWriter());
	}

	protected Writer getRuntimeConfigurationWriter() {
		try {
			OutputStream stream = new BufferedOutputStream(
					new FileOutputStream(getRuntimeConfigurationFile()));
			return new OutputStreamWriter(stream);
		} catch (FileNotFoundException e) {
		}

		return null;
	}

	protected void loadRuntimeConfiguration() {
		installedInterpreters = new ArrayList();
		try {
			File file = getRuntimeConfigurationFile();
			if (file.exists()) {
				XMLReader reader = SAXParserFactory.newInstance()
						.newSAXParser().getXMLReader();
				reader
						.setContentHandler(getRuntimeConfigurationContentHandler());
				reader.parse(new InputSource(
						getRuntimeConfigurationReader(file)));
			}
		} catch (Exception e) {
			PHPLaunchingPlugin.log(e);
		}
	}

	protected Reader getRuntimeConfigurationReader(File file) {
		try {
			return new FileReader(file);
		} catch (FileNotFoundException e) {
		}
		return new StringReader("");
	}

	protected void writeXML(Writer writer) {
		try {
			writer
					.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><runtimeconfig>");
			Iterator interpretersIterator = installedInterpreters.iterator();
			while (interpretersIterator.hasNext()) {
				writer.write("<interpreter name=\"");

				PHPInterpreter entry = (PHPInterpreter) interpretersIterator
						.next();
				// writer.write(entry.getName());
				writer.write("\" path=\"");
				writer.write(entry.getInstallLocation().toString());
				writer.write("\"");
				if (entry.equals(selectedInterpreter))
					writer.write(" selected=\"true\"");

				writer.write("/>");
			}
			writer.write("</runtimeconfig>");
			writer.flush();
		} catch (IOException e) {
			PHPLaunchingPlugin.log(e);
		}
	}

	protected ContentHandler getRuntimeConfigurationContentHandler() {
		return new ContentHandler() {
			public void setDocumentLocator(Locator locator) {
			}

			public void startDocument() throws SAXException {
			}

			public void endDocument() throws SAXException {
			}

			public void startPrefixMapping(String prefix, String uri)
					throws SAXException {
			}

			public void endPrefixMapping(String prefix) throws SAXException {
			}

			public void startElement(String namespaceURI, String localName,
					String qName, Attributes atts) throws SAXException {
				if ("interpreter".equals(qName)) {
					String interpreterName = atts.getValue("name");
					java.io.File installLocation;
					if (interpreterName != null) {
						installLocation = new File(atts.getValue("path")
								+ File.separatorChar + interpreterName);
					} else {
						installLocation = new File(atts.getValue("path"));
					}
					PHPInterpreter interpreter = new PHPInterpreter(
							installLocation);
					installedInterpreters.add(interpreter);
					if (atts.getValue("selected") != null)
						selectedInterpreter = interpreter;
				}
			}

			public void endElement(String namespaceURI, String localName,
					String qName) throws SAXException {
			}

			public void characters(char[] ch, int start, int length)
					throws SAXException {
			}

			public void ignorableWhitespace(char[] ch, int start, int length)
					throws SAXException {
			}

			public void processingInstruction(String target, String data)
					throws SAXException {
			}

			public void skippedEntity(String name) throws SAXException {
			}
		};
	}

	protected File getRuntimeConfigurationFile() {
		IPath stateLocation = PHPLaunchingPlugin.getDefault()
				.getStateLocation();
		IPath fileLocation = stateLocation.append("runtimeConfiguration.xml");
		return new File(fileLocation.toOSString());
	}
}
