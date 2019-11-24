package dev.mikera.gui.demo;

import java.awt.*;

import javax.swing.*;

import dev.mikera.gui.*;

public class ConsoleApp {

	public static void main(String[] args) {

		JFrame frame = new JFrame("Swing Text Console");

		JConsole jc = new JConsole(100, 40);

		frame.setLayout(new BorderLayout());
		frame.add(jc, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.pack();
		frame.setVisible(true);

		jc.setCursorVisible(true);
		jc.setCursorBlink(true);
		jc.write("Hello World\n");
		jc.write("Hello World\n", Color.BLACK, Color.MAGENTA);
		jc.write("Hello World\n", Color.GREEN, Color.BLACK);

		System.out.println("Normal output");
		jc.captureStdOut();
		System.out.println("Captured output");

		// brown box
		jc.fillArea(' ', Color.WHITE, new Color(100, 70, 30), 20, 20, 3, 3);

		jc.setCursorPos(0, 0);
	}

}
