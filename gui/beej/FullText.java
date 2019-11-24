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
package gui.beej;


import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import core.*;

public class FullText extends JFrame implements ActionListener {

	protected JTextArea code;
	protected BaseEditor editor;
	protected JScrollPane scrollPane;

	public FullText() {
		super("BEEJ Example - FullText");

		// Finish setting up the frame, and show it.
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try {
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					ObjectOutputStream out = new ObjectOutputStream(bos);
					out.writeObject(editor);
					TPreferences.setPreference(TPreferences.PRINT_FIELD_SELECTION, "", bos.toByteArray());
				} catch (Exception e2) {
					e2.printStackTrace();
				}
				System.exit(0);
			}
		});

		// expression editor (do not set it's size if it is within a scrollpane)
		Container pane = getContentPane();
		pane.setLayout(new FlowLayout());
		editor = new BaseEditor(new TextExpressionPanel("tp_people", "audit_track;photo",
				"document_type;pdocty;gender;persgen;marital_status;marsts;blood_group;bloodg;labor_status;labsts"));
		editor.addActionListener(this);

		scrollPane = new JScrollPane(editor);
		pane.add(scrollPane);

		// code text box
		code = new JTextArea(7, 20);
		pane.add(code);

		setPreferredSize(new Dimension(500, 500));

		pack();
		setVisible(true);

		updateCodeText();
	}

	// listen for editor changes
	public void actionPerformed(ActionEvent evt) {
		updateCodeText();
	}

	protected void updateCodeText() {
		String beginningCode = "select * from articles where \n";
		String endingCode = "\norder by date";
		code.setText(beginningCode + editor.toString() + endingCode);
	}
}
