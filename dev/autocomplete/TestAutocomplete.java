package dev.autocomplete;

import java.util.*;

import javax.swing.*;

public class TestAutocomplete {

	private static final String COMMIT_ACTION = "commit";
	JTextField mainTextField = new JTextField(30);
	
	public TestAutocomplete() {

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Without this, cursor always leaves text field
		mainTextField.setFocusTraversalKeysEnabled(false);
		// Our words to complete
		ArrayList keywords = new ArrayList<String>(5);
		        keywords.add("example");
		        keywords.add("examterry");
		        keywords.add("autocomplete");
		        keywords.add("stackabuse");
		        keywords.add("java");
		Autocomplete autoComplete = new Autocomplete(mainTextField, keywords);
		mainTextField.getDocument().addDocumentListener(autoComplete);

		// Maps the tab key to the commit action, which finishes the autocomplete
		// when given a suggestion
		mainTextField.getInputMap().put(KeyStroke.getKeyStroke("TAB"), COMMIT_ACTION);
		mainTextField.getActionMap().put(COMMIT_ACTION, autoComplete.new CommitAction());
		
		JPanel p = new JPanel();

		p.add(mainTextField);

		frame.add(p);

		frame.pack();
		frame.setVisible(true);
	}
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new TestAutocomplete();
			}
		});
	}
}