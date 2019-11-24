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

import javax.swing.*;

import core.*;



public class ColorComboRenderer extends DefaultListCellRenderer {

	protected TEntry tecolor;
	private ColorIcon icon;

	public ColorComboRenderer() {
		super();
		this.tecolor = new TEntry(Color.BLACK, ColorComboRenderer.getHexColor(Color.BLACK));
		icon = new ColorIcon(Color.BLACK);
		setIcon(icon);
		setText("");
	}

	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		tecolor = (TEntry) value;
		icon.setColor((Color) tecolor.getKey());
		setIcon(icon);
		setText((String) tecolor.getValue());
		return this;
	}

	public static String getHexColor(Color c) {
		return String.format("#%1$02X%2$02X%3$02X", c.getRed(), c.getGreen(), c.getBlue());
	}
}
