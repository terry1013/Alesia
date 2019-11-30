package gui.console;

import java.awt.*;
import java.io.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.text.*;

/**
 * set the text from the asociated {@link Logger} to the asociated {@link JEditorPane} to this class. this class by
 * difault check if the incomming message contain {@link Level#getName()} for {@link Level#WARNING},
 * {@link Level#SEVERE} and set the color for this tipes to Red
 * 
 * @author terry
 *
 */
class TextAreaOutputStream extends OutputStream {
	private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	private final JEditorPane textArea;
	private SimpleAttributeSet errAtributes;
	private SimpleAttributeSet outAtributes;

	protected TextAreaOutputStream(JEditorPane textArea) {
		super();
		this.textArea = textArea;
		errAtributes = new SimpleAttributeSet();
		StyleConstants.setForeground(errAtributes, Color.RED);
		outAtributes = new SimpleAttributeSet();
		StyleConstants.setForeground(outAtributes, Color.BLACK);
	}

	@Override
	public void flush() throws IOException {
		Document doc = textArea.getDocument();
		String line = buffer.toString("UTF-8");
		SimpleAttributeSet att = line.contains(Level.WARNING.getName()) || line.contains(Level.SEVERE.getName())
				? errAtributes
				: outAtributes;
		try {
			doc.insertString(doc.getLength(), line, att);
			textArea.setCaretPosition(doc.getLength());
		} catch (Exception e) {
			e.printStackTrace();
		}
		buffer.reset();
	}

	@Override
	public void write(int b) throws IOException {
		buffer.write(b);
	}
}
