package net.sourceforge.phpdt.internal.corext.phpdoc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import net.sourceforge.phpeclipse.PHPeclipsePlugin;
import net.sourceforge.phpeclipse.builder.PHPIdentifierLocation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

/**
 * Utility class for static PHPdoc helper mehods
 */
public class PHPDocUtil {

	/**
	 * Generate a PHPDoc hover text if possible
	 * 
	 * @param hoverInfoBuffer
	 * @param filename
	 * @param location
	 */
	public static void appendPHPDoc(StringBuffer hoverInfoBuffer,
			String filename, PHPIdentifierLocation location) {
		hoverInfoBuffer.append(location.toString());
		hoverInfoBuffer.append(" - <b>");
		try {
			hoverInfoBuffer.append(getUsage(filename, location));
			hoverInfoBuffer.append("</b><br>");

			// read the phpdoc for the function
			if (location.getPHPDocOffset() >= 0) {
				InputStreamReader phpFileReader = createReader(filename);
				if (phpFileReader == null)
					return;
				char[] phpDocDeclarationCharArray = new char[location
						.getPHPDocLength()];
				phpFileReader.skip(location.getPHPDocOffset());
				phpFileReader.read(phpDocDeclarationCharArray, 0, location
						.getPHPDocLength());
				PHPDocCharArrayCommentReader phpdocConverter = new PHPDocCharArrayCommentReader(
						phpDocDeclarationCharArray);
				hoverInfoBuffer.append(phpdocConverter.getString());
				phpFileReader.close();
			}

		} catch (IOException e) {
			// TODO: smell
			return;
		}
	}

	static String getEncoding(String filename) {
		String encoding = null;
		IFile file = PHPeclipsePlugin.getWorkspace().getRoot()
				.getFileForLocation(new Path(filename));
		if (file != null) {
			try {
				encoding = file.getCharset();
			} catch (CoreException e) {
				// TODO: should log the fact that we could not get the encoding?
			}
		}
		return encoding;
	}

	public static String getUsage(String filename,
			PHPIdentifierLocation location) {
		String usage = location.getUsage();
		if (usage != null) {
			return usage;
		}
		usage = "";
		try {

			InputStreamReader phpFileReader = createReader(filename);
			if (phpFileReader == null)
				return "";
			// read the function declaration
			if (location.getOffset() >= 0
					&& (location.isMethod() || location.isConstructor()
							|| location.isFunction() || location.isDefine())) {
				char[] functionDeclarationCharArray = new char[256];
				int offset = location.getOffset();
				phpFileReader.skip(offset);
				int length = phpFileReader.read(functionDeclarationCharArray,
						0, 256);
				if (length == -1) {
					length = 256;
				}
				if (location.isDefine()) {
					length = getClosingParenthesis(functionDeclarationCharArray);
					if (length < 0)
						return "";
				} else {
					for (int i = 0; i < length; i++) {
						if (functionDeclarationCharArray[i] == ')') {
							length = i + 1;
							break;
						}
						if (functionDeclarationCharArray[i] == '{'
								|| functionDeclarationCharArray[i] == '}') {
							length = i;
							break;
						}
					}
				}
				usage = new String(functionDeclarationCharArray, 0, length);
				// cache the usage string:
				location.setUsage(usage);
			}
			phpFileReader.close();

		} catch (IOException e) {
			// do nothing
		}
		return usage;
	}

	private static InputStreamReader createReader(String filename) {
		IFile file = PHPeclipsePlugin.getWorkspace().getRoot()
				.getFileForLocation(new Path(filename));
		if (file != null) {
			try {
				return new InputStreamReader(new FileInputStream(file
						.getLocation().toString()), file.getCharset());
			} catch (UnsupportedEncodingException e) {
				// do nothing
			} catch (FileNotFoundException e) {
				// do nothing
			} catch (CoreException e) {
				// do nothing
			}
		}
		return null;
	}

	private static int getClosingParenthesis(char[] buffer) {
		int p = 0;
		boolean dq = false;
		boolean sq = false;

		for (int i = 0; i < buffer.length; i++) {
			if (buffer[i] == '\\') {
				i++;
				continue;
			}
			if (dq) {
				dq = (buffer[i] != '"');
				continue;
			}
			if (sq) {
				sq = (buffer[i] != '\'');
				continue;
			}
			switch (buffer[i]) {
			case '(':
				p++;
				break;
			case ')':
				p--;
				if (p < 0)
					return i;
				break;
			case '"':
				dq = true;
				break;
			case '\'':
				sq = true;
				break;
			}
		}
		return -1;
	}

}
