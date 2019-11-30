package gui.console;

import java.awt.*;
import java.io.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.text.*;

public final class TestLogFrame extends JPanel {

	private TestLogFrame() {
		super(new BorderLayout());

		// look for alesia loggin configuration file
		File logc = new File("logging.properties");
		System.out.println(logc);
		if (logc.exists()) {
			System.setProperty("java.util.logging.config.file", logc.toString());
		}
		Logger logger = Logger.getLogger("Alesia");
		// logger.setUseParentHandlers(false);

		JEditorPane textArea = new JEditorPane();
		textArea.setEditable(false);
		textArea.setEditorKit(new StyledEditorKit());

		OutputStream os = new TextAreaOutputStream(textArea);
		TextAreaHandler tah = new TextAreaHandler(os);
		logger.setLevel(Level.ALL);
//		tah.setLevel(Level.ALL);
		logger.addHandler(tah);

		logger.info("test, TEST");
		logger.warning("test, TEST");
		logger.severe("test, TEST");
		logger.finer("test, TEST");

		JButton button = new JButton("Clear");
		button.addActionListener(e -> textArea.setText(""));
		JTextField textField = new JTextField("aaa");

		Box box = Box.createHorizontalBox();
		box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		box.add(Box.createHorizontalGlue());
		box.add(textField);
		box.add(Box.createHorizontalStrut(5));
		box.add(new JButton(new EnterAction(textField)));
		box.add(Box.createHorizontalStrut(5));
		box.add(button);

		add(new JScrollPane(textArea));
		add(box, BorderLayout.SOUTH);
		setPreferredSize(new Dimension(420, 240));
	}

	public static void main(String... args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				createAndShowGui();
			}
		});
	}

	public static void createAndShowGui() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException ex) {
			ex.printStackTrace();
		}
		JFrame frame = new JFrame("TextAreaOutputStream");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(new TestLogFrame());
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
