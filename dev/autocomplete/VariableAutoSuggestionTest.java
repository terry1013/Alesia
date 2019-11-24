package dev.autocomplete;

import java.util.*;

import javax.swing.*;
import javax.swing.text.*;

import core.*;

public class VariableAutoSuggestionTest {

	JTextComponent mainTextField = new JTextField(30);
//	JTextComponent mainTextField = new JTextArea(2, 30);

	public VariableAutoSuggestionTest() {

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Without this, cursor always leaves text field
		// mainTextField.setFocusTraversalKeysEnabled(false);
		// Our words to complete
		ArrayList keywords = new ArrayList<TEntry>();
		keywords.add(new TEntry("Titulo", "${company.m_abtitle}"));
		keywords.add(new TEntry("Nombre", "${company.m_abname}"));
		keywords.add(new TEntry("Titulo", "${personal.m_abtitle}"));
		keywords.add(new TEntry("Nombre", "${personal.m_abname}"));
		VariableAutoSuggestion autoSuggestion = new VariableAutoSuggestion(keywords);
		autoSuggestion.setAutoSuggestionFor(mainTextField);

		JPanel p = new JPanel();

		p.add(mainTextField);

		frame.add(p);
		frame.setBounds(100, 100, 500, 500);
		// frame.pack();
		frame.setVisible(true);
	}
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new VariableAutoSuggestionTest();
			}
		});
	}
}