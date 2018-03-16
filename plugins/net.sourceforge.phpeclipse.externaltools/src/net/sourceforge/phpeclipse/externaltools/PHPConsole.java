package net.sourceforge.phpeclipse.externaltools;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class PHPConsole {
	private MessageConsole myConsole;

	private MessageConsoleStream stream;

	private boolean hasMessages;

	public PHPConsole() {
		hasMessages = false;
		myConsole = new MessageConsole("PHPeclipse Console", null);
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(
				new IConsole[] { myConsole });
		ConsolePlugin.getDefault().getConsoleManager().showConsoleView(
				myConsole);
		// layout.addView(IConsoleConstants.ID_CONSOLE_VIEW, IPageLayout.BOTTOM,
		// .5f,IPageLayout.ID_EDITOR_AREA);
		stream = myConsole.newMessageStream();
	}

	/**
	 * @return
	 */
	public Color getColor() {
		return stream.getColor();
	}

	/**
	 * @return
	 */
	public MessageConsole getConsole() {
		return stream.getConsole();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return stream.hashCode();
	}

	/**
	 * @param message
	 */
	public void print(String message) {
		hasMessages = true;
		stream.print(message);
	}

	/**
	 * 
	 */
	public void println() {
		hasMessages = true;
		stream.println();
	}

	/**
	 * @param message
	 */
	public void println(String message) {
		hasMessages = true;
		stream.println(message);
	}

	/**
	 * @param color
	 */
	public void setColor(Color color) {
		stream.setColor(color);
	}

	// public void reportError(String title, String message) {
	// if (hasMessages) {
	// WikiEditorPlugin.getDefault().reportError(title, message);
	// }
	// }

	// public void reportError() {
	// reportError("Problems listed", "Open console view for problems log!");
	// }
	/**
	 * Creates a string buffer from the given input stream
	 */
	public static String getStringFromStream(InputStream stream)
			throws IOException {
		StringBuffer buffer = new StringBuffer();
		byte[] b = new byte[100];
		int finished = 0;
		while (finished != -1) {
			finished = stream.read(b);
			if (finished != -1) {
				String current = new String(b, 0, finished);
				buffer.append(current);
			}
		}
		return buffer.toString();
	}
}