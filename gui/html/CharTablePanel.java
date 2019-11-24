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
package gui.html;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

public class CharTablePanel extends JPanel {

	private JEditorPane editor;

	String[] chars = { "\u00A9", "\u00AE", "\u2122", "\u00AB\u00BB", "\u201C\u201D",
			"\u2018\u2019", "\u2013", "\u2014", "\u2020", "\u2021", "\u00A7", "\u2116", "\u20AC",
			"\u00A2", "\u00A3", "\u00A4", "\u00A5", "\u00B7", "\u2022", "\u25E6", "\u25AA",
			"\u25AB", "\u25CF", "\u25CB", "\u25A0", "\u25A1", "\u263A", "\u00A0" };

	Vector buttons = new Vector();

	public CharTablePanel(JEditorPane ed) {
		editor = ed;
		this.setFocusable(false);
		this.setPreferredSize(new Dimension(200, 45));
		this.setToolTipText("");
		this.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		createButtons();
	}

	void createButtons() {
		for (int i = 0; i < chars.length; i++) {
			JButton button = new JButton(new CharAction(chars[i]));
			button.setPreferredSize(new Dimension(20, 20));
			button.setRequestFocusEnabled(false);
			button.setFocusable(false);
			button.setBorderPainted(false);
			button.setOpaque(false);
			button.setMargin(new Insets(0, 0, 0, 0));
//			button.setFont(new Font("serif", 0, 14));
			this.add(button, null);
		}
	}

	class CharAction extends AbstractAction {
		CharAction(String name) {
			super(name);
		}

		public void actionPerformed(ActionEvent e) {
			String s = this.getValue(Action.NAME).toString();
			editor.replaceSelection(s);
			if (s.length() == 2)
				editor.setCaretPosition(editor.getCaretPosition() - 1);
		}
	}

}
