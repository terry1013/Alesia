package dev.autosuggestor;

import java.awt.*;
import java.util.*;

import javax.swing.*;

/**
 * @author David
 */
public class Test {

	public Test() {

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		 //JTextField f = new JTextField(10);
		JTextArea f = new JTextArea(10, 10);
		// JEditorPane f = new JEditorPane();

		// create words for dictionary could also use null as parameter for AutoSuggestor(..,..,null,..,..,..,..) and
		// than call AutoSuggestor#setDictionary after AutoSuggestr insatnce has been created
		ArrayList<String> words = new ArrayList();
		words.add("hello");
		words.add("heritage");
		words.add("happiness");
		words.add("goodbye");
		words.add("cruel");
		words.add("car");
		words.add("war");
		words.add("will");
		words.add("world");
		words.add("wall");

		AutoSuggestor autoSuggestor = new AutoSuggestor(f, frame, words) {
			@Override
			boolean wordTyped(String typedWord) {
				System.out.println(typedWord);
				return super.wordTyped(typedWord);// checks for a match in dictionary and returns true or false if found
													// or not
			}
		};

		JPanel p = new JPanel();

		p.add(f);

		frame.add(p);

		frame.pack();
		frame.setVisible(true);
	}
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new Test();
			}
		});
	}
}