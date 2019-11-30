package gui.console;

import java.io.*;
import java.util.logging.*;

class TextAreaHandler extends StreamHandler {

	protected TextAreaHandler(OutputStream os) {
		super();
		setOutputStream(os);
	}

	@Override
	public synchronized void publish(LogRecord record) {
		super.publish(record);
		flush();
	}

	@Override
	public synchronized void close() {
		flush();
	}
}
