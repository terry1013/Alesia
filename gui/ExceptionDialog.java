/*******************************************************************************
 * Copyright (C) 2017 terry.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     terry - initial API and implementation
 ******************************************************************************/
package gui;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.border.*;

import core.*;

/**
 * Dialog with aditional information suport. similar to JOptionPane, this dialog present message with only two buttons.
 * Ok buton to close the dialog and Detail button to show/hide aditional information panel. For usage, see the Show
 * methots.
 * 
 * @author terry
 * 
 */
public class ExceptionDialog extends JDialog implements ActionListener {

	private static Dimension dialogDim;
	private boolean open = false; // is error panel opened up
	private JScrollPane exceptionTextAreaSP;
	private JButton okButton;
	private JButton viewButton;
	private String description, stackTrace, msgT;

	public static String ERROR = "OptionPane.errorIcon";
	public static String INFORMATION = "OptionPane.informationIcon";
	public static String QUESTION = "OptionPane.questionIcon";
	public static String WARNING = "OptionPane.warningIcon";

	private ExceptionDialog(String msgt, String tit, String txt, String etxt) {
		setTitle(tit);
		setResizable(false);
		setModal(true);

		this.description = txt;
		this.msgT = msgt;
		this.stackTrace = etxt;

		setupUI();
	}

	/**
	 * display this dialog instance with {@link #ERROR} icon, class name of exception as title, and stack trace
	 * 
	 * @param ex - {@link Exception} to show
	 */
	public static void showDialog(Exception ex) {
		showDialog(ERROR, ex.getClass().getSimpleName(), ex.getMessage(), ex);
	}

	/**
	 * display this dialog instance with <code>msgt</code> icon, title, text and detail info
	 * 
	 * @param msgt - dialog type. any of {@link #ERROR} {@link #WARNING} {@link #QUESTION} or {@link #INFORMATION}
	 * @param tit - title of dialog
	 * @param txt - message
	 * @param etxt - extended message info
	 */
	public static void showDialog(String msgt, String tit, String txt, String etxt) {
		JDialog jd = new ExceptionDialog(msgt, tit, txt, etxt);
		// try to center to main frame
		jd.pack();
		dialogDim = jd.getSize();
		jd.setLocationRelativeTo(Alesia.mainFrame);
		jd.setVisible(true);
	}

	/**
	 * /** display this dialog instance with <code>msgt</code> icon, title, text and detail info
	 * 
	 * @param msgt - dialog type. any of {@link #ERROR} {@link #WARNING} {@link #QUESTION} or {@link #INFORMATION}
	 * @param tit - title of dialog
	 * @param txt - message
	 * @param ex - exception
	 */
	public static void showDialog(String msgt, String tit, String txt, Throwable ex) {
		showDialog(msgt, tit, txt, getStackTrace(ex));
	}

	private static String getStackTrace(Throwable e) {
		StringWriter sr = new StringWriter();
		e.printStackTrace(new PrintWriter(sr));
		return sr.toString();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == okButton) {
			setVisible(false);
		}
		if (e.getSource() == viewButton) {
			if (open) {
				viewButton.setText("Detalles >>");
				exceptionTextAreaSP.setVisible(false);
				setSize(dialogDim);
				open = false;
			} else {
				viewButton.setText("<< Detalles");
				exceptionTextAreaSP.setVisible(true);
				setSize(new Dimension(dialogDim.width, dialogDim.height + 150));
				open = true;
			}
			revalidate();
		}
	}

	private void setupUI() {

		JLabel iconLabel = new JLabel(UIManager.getIcon(msgT));
		iconLabel.setBorder(new EmptyBorder(new Insets(0, 0, 0, 10)));

		okButton = new JButton("Ok");
		okButton.addActionListener(this);
		viewButton = new JButton("Detalles >>");
		viewButton.addActionListener(this);

		JTextArea errorTextArea = new JTextArea(description, 3, 60);
		errorTextArea.setLineWrap(true);
		errorTextArea.setWrapStyleWord(true);
		errorTextArea.setEditable(false);
		errorTextArea.setBorder(null);
		errorTextArea.setBackground(iconLabel.getBackground());

		JTextArea exceptionTextArea = new JTextArea(stackTrace, 10, 60);
		exceptionTextArea.setEditable(false);
		exceptionTextAreaSP = new JScrollPane(exceptionTextArea);
		exceptionTextAreaSP.setVisible(false);

		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(iconLabel, BorderLayout.WEST);
		topPanel.add(errorTextArea, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
		buttonPanel.add(okButton);
		buttonPanel.add(viewButton);

		JPanel jp2 = new JPanel(new BorderLayout(4, 4));
		jp2.setBorder(new EmptyBorder(new Insets(4, 4, 4, 4)));
		jp2.add(topPanel, BorderLayout.NORTH);
		jp2.add(exceptionTextAreaSP, BorderLayout.CENTER);
		jp2.add(buttonPanel, BorderLayout.SOUTH);

		add(jp2);
	}
}
