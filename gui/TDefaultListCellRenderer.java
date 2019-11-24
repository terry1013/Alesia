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

import com.alee.laf.list.*;

import core.*;
import core.datasource.*;

/**
 * implementacion de <code>DefaultListCellRenderer</code> para vista mosaico en Jlist. esta clase delega el trabajo a la
 * instancia de defaul
 * 
 * 
 */
public class TDefaultListCellRenderer extends DefaultListCellRenderer {

	private String valColumn;
	private ImageIcon imageIcon;
	private String iconName;
	private final String pattern2 = "<html>column0<FONT COLOR='#808080'><p>column1</p></font></html>";
	private final String pattern3 = "<html>column0<FONT COLOR='#808080'><p>column1</p><p>column2</p></font></html>";
	private String[] columns;
	private Dimension size;

	/**
	 * nueva instancia
	 * 
	 */

	public TDefaultListCellRenderer() {
		setIconParameters(null, null);
		this.size = new Dimension(200, 50);
	}
	
	public void setColumns(String cols) {
		this.columns = cols.split(";");
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		WebList st = (WebList) list;
		Record rcd = (Record) ((TAbstractListModel) st.getModel()).getElementAt(index);
		if (valColumn != null) {
			String val = rcd.getFieldValue(valColumn).toString();
			if (iconName.equals("*")){
				imageIcon = new ImageIcon((byte[]) rcd.getFieldValue(valColumn));
				imageIcon = new ImageIcon(imageIcon.getImage().getScaledInstance(42, 42, Image.SCALE_SMOOTH));
			} else {
				imageIcon = TResources.getIcon(iconName + val, 44);
			}
		}
		setIcon(imageIcon);

		String pat = columns.length > 2 ? pattern3 : pattern2;
		for (int i = 0; i < columns.length; i++) {
			String cv = rcd.getFieldValue(columns[i]).toString();
			cv = (cv.length() > 30) ? cv.substring(0, 30) + "..." : cv;
			pat = pat.replace("column" + i, cv);
		}
		setPreferredSize(size);
		setText(pat);
		return this;
	}
	/**
	 * set the icon parameters for this cell renderer.
	 * 
	 * 
	 * @param icon - icon file name, prefix or "*"
	 * @param valCol - field name to complete the icon file name, or contain data necesary to create an imageIcon with
	 *        it
	 * 
	 * @see TConstants#ICON_PARAMETERS
	 * 
	 */
	public void setIconParameters(String icon, String valCol) {
		this.iconName = icon;
		this.valColumn = valCol;
		if (valColumn == null) {
			imageIcon = TResources.getIcon(iconName, 42);
		}
	}
}
