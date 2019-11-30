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
package gui.prueckl.draw;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import core.*;

/**
 * show dialog for edit figure properties.
 * 
 * @author terry
 *
 */
public class FigureProperties extends AbstractAction {

	private JTextArea jTextArea;
	private DrawingEditor editor;
	private Properties commonp = new Properties();

	public FigureProperties(DrawingEditor ed) {
		this.editor = ed;
		putValue(Action.SMALL_ICON, TResources.getSmallIcon("task-list-edit"));
	}

	private void actionPerformed2() {
		try {
			// Copy propertys from text area to figure properties
			ByteArrayInputStream bais = new ByteArrayInputStream(jTextArea.getText().getBytes());
			commonp.clear();
			commonp.load(bais);
			bais.close();

			// 191709: my first modification of hero plugin in refuge camp in germany !!!
			int fs = editor.getDrawingPanel().getFigures().size();
			int sel = 0;
			for (int i = 0; i < fs; i++) {
				Figure f = (Figure) editor.getDrawingPanel().getFigures().elementAt(i);
				sel += f.isSelected() ? 1 : 0;
			}

			for (int i = 0; i < fs; i++) {
				Figure f = (Figure) editor.getDrawingPanel().getFigures().elementAt(i);
				if (f.isSelected()) {
					Properties fp = f.getProperties();
					// for only one figure selected, old properties are replaced for new properties
					if (sel == 1) {
						fp.clear();
					}
					// set the figure bounds
					String bo[] = ((String) commonp.remove("bounds")).split("[,]");
					f.setBounds(new Rectangle(Integer.parseInt(bo[0]), Integer.parseInt(bo[1]), Integer.parseInt(bo[2]),
							Integer.parseInt(bo[3])));

					fp.putAll(commonp);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		try {
			commonp.clear();
			for (int i = 0; i < editor.getDrawingPanel().getFigures().size(); i++) {
				Figure f = (Figure) editor.getDrawingPanel().getFigures().elementAt(i);
				if (f.isSelected()) {
					commonp.putAll(f.getProperties());
					// append bound property
					Rectangle rec = f.getBounds();
					commonp.put("bounds", rec.x + "," + rec.y + "," + rec.width + "," + rec.height);
				}
			}
			byte[] prps = null;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			commonp.store(baos, null);
			prps = baos.toByteArray();
			baos.close();
			jTextArea = new JTextArea(new String(prps));
			// jTextArea.setPreferredSize(new Dimension(200, 200));
			JScrollPane jsp = new JScrollPane(jTextArea);

			int resp = JOptionPane.showConfirmDialog(Alesia.mainFrame, jsp, "Edit Properties",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (resp == JOptionPane.OK_OPTION) {
				actionPerformed2();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
