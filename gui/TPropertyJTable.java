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

import java.awt.event.*;

import javax.swing.*;


import core.*;

/**
 * small component that allow simple edit list of properties. 
 * 
 * @author terry
 * 
 */
public class TPropertyJTable extends JTable implements MouseListener {

	private TPropertyTableModel tableModel;

	/**
	 * new instance
	 * 
	 * @param prpl - String of properties in standar format key;value;key;value...
	 */
	public TPropertyJTable(String prpl) {
		setPropertys(prpl);
		setDefaultRenderer(Object.class, new TPropertyCellRenderer());
		getTableHeader().setReorderingAllowed(false);
		// jt.getTableHeader().setPreferredSize(headerSize);
		addMouseListener(this);
	}
	
	
	/** set propertys for this table 
	 * 
	 * @param prpl - String of properties in standar format key;value;key;value...
	 */
	public void setPropertys(String prpl) {
		this.tableModel = new TPropertyTableModel(TStringUtils.getPropertys(prpl));
		setModel(tableModel);
		getTableHeader().getColumnModel().getColumn(0).setMaxWidth(20);
		tableModel.fireTableDataChanged();
	}

	/** 
	 * return properties. Properties coming from this table, are not validated. key or value can be one space strings.
	 * 
	 * @return String of properties in standar format key;value;key;value...
	 */
	public String getProperties() {
		int r = tableModel.getRowCount() - 1;
		String prps = "";
		for (int i = 0; i < r; i++) {
			// add single space if either key or value are not present
			String k = tableModel.getValueAt(i, 1).toString().trim();
			k = k.equals("") ? " " : k;
			String v = tableModel.getValueAt(i, 2).toString().trim();
			v = v.equals("") ? " " : v;
			prps += k + ";" + v;
		}
//		return prps.length() == 0 ? prps : prps.substring(0, prps.length() - 1);
		return prps;
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		if (isEnabled()) {
			int col = columnAtPoint(e.getPoint());
			int row = rowAtPoint(e.getPoint());
			if (col == 0 && row > -1 && row < getRowCount()) {
				// add
				if (row == getRowCount() - 1) {
					tableModel.addProperty(new TEntry("", ""));
					// remove
				} else {
					tableModel.removeProperty(row);
				}
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {

	}

	@Override
	public void mouseReleased(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}
}
