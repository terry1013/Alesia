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
/*			
 * Copyright (c) QQ - All right reserved

 */
package gui;

import java.awt.*;

import javax.swing.*;

/**
 * crea un icono en forma de cuadro con el color pasado como argumento
 * 
 */
public class ColorIcon implements Icon {

	private int heiht, width;
	private Color color;

	/**
	 * nueva instancia
	 * 
	 * @param c - color
	 */
	public ColorIcon(Color c) {
		this.width = 16;
		this.heiht = 16;
		this.color = c;
	}

	public void setColor(Color c) {
		color = c;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.Icon#getIconHeight()
	 */
	public int getIconHeight() {
		return heiht;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.Icon#getIconWidth()
	 */
	public int getIconWidth() {
		return width;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
	 */
	public void paintIcon(Component c, Graphics g, int x, int y) {
		JComponent jc = (JComponent) c;
		Insets bi = jc.getBorder().getBorderInsets(jc);
		g.setColor(color);
		g.fillRoundRect(1 + bi.left, 1 + bi.top, width - 2, heiht - 2, 2, 2);
		g.setColor(Color.GRAY);
		g.drawRoundRect(1 + bi.left, 1 + bi.top, width - 2, heiht - 2, 2, 2);
	}
}
