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
import java.beans.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import core.*;

/**
 * Use a <code>JTable</code> as a renderer for row numbers (or String, for row with names) of a given main table. This
 * table must be added to the row header of the <code>JScrollPane</code> that contains the main table.
 */
public class RowHeaderJTable extends JTable implements ChangeListener, PropertyChangeListener, TableModelListener {
	private JTable main;
	private Vector<String> columns;

	/**
	 * new instance
	 * 
	 * @param table - main table to render row number or row names
	 * @param cols - id for columns names in col;col;... format. if this param is <code>null</code> this table render
	 *        row numbers for main table
	 */
	public RowHeaderJTable(JTable table, String cols) {
		columns = null;
		if (cols != null) {
			String[] cols1 = cols.split(";");
			columns = new Vector<String>(cols1.length);
			for (String cl : cols1) {
				columns.add(TStringUtils.getString(cl));
			}
		}
		main = table;
		main.addPropertyChangeListener(this);
		main.getModel().addTableModelListener(this);

		setFocusable(false);
		setAutoCreateColumnsFromModel(false);
		setSelectionModel(main.getSelectionModel());

		TableColumn column = new TableColumn();
		column.setHeaderValue(" ");
		addColumn(column);
		column.setCellRenderer(new RowHeaderCellRenderer());
		
		// colum size 
		int csize = 50;
		if (columns != null) {
			String fms = "";
			for (String col : columns) {
				fms = (fms.length() < col.length()) ? col : fms;
			}
			JLabel jl = new JLabel(fms);
			FontMetrics fm = jl.getFontMetrics(jl.getFont().deriveFont(Font.BOLD));
			csize = fm.stringWidth(fms) + fms.length();
		}
		getColumnModel().getColumn(0).setPreferredWidth(csize);
		setPreferredScrollableViewportSize(getPreferredSize());
	}


	@Override
	public void addNotify() {
		super.addNotify();

		Component c = getParent();

		// Keep scrolling of the row table in sync with the main table.

		if (c instanceof JViewport) {
			JViewport viewport = (JViewport) c;
			viewport.addChangeListener(this);
		}
	}

	@Override
	public int getRowCount() {
		return columns.size();
		// return main.getRowCount();
	}

	@Override
	public int getRowHeight(int row) {
		int rowHeight = main.getRowHeight(row);

		if (rowHeight != super.getRowHeight(row)) {
			super.setRowHeight(row, rowHeight);
		}

		return rowHeight;
	}

	@Override
	public Object getValueAt(int row, int column) {
		Object val = null;
		if (columns == null) {
			val = row +1;
		} else {
			val = columns.get(row);
		}
		return val;
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		// Don't edit data in the main TableModel by mistake
		return false;
	}

	@Override
	public void setValueAt(Object value, int row, int column) {
		// Do nothing since the table ignores the model
	}
	@Override
	public void stateChanged(ChangeEvent e) {
		// Keep the scrolling of the row table in sync with main table

		JViewport viewport = (JViewport) e.getSource();
		JScrollPane scrollPane = (JScrollPane) viewport.getParent();
		scrollPane.getVerticalScrollBar().setValue(viewport.getViewPosition().y);
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		// Keep the row table in sync with the main table
		if ("selectionModel".equals(e.getPropertyName())) {
			setSelectionModel(main.getSelectionModel());
		}

		if ("rowHeight".equals(e.getPropertyName())) {
			repaint();
		}

		if ("model".equals(e.getPropertyName())) {
			main.getModel().addTableModelListener(this);
			revalidate();
		}
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		revalidate();
	}

	/**
	 * Attempt to mimic the table header renderer
	 */
	private static class RowHeaderCellRenderer extends DefaultTableCellRenderer {
		public RowHeaderCellRenderer() {
			setHorizontalAlignment(JLabel.CENTER);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			if (table != null) {
				JTableHeader header = table.getTableHeader();

				if (header != null) {
					setForeground(header.getForeground());
					setBackground(header.getBackground());
					setFont(header.getFont());
				}
			}

			if (isSelected) {
				setFont(getFont().deriveFont(Font.BOLD));
			}

			setText((value == null) ? "" : value.toString());
			setHorizontalAlignment((value instanceof String) ? LEFT : CENTER);
			// setBorder(UIManager.getBorder("TableHeader.cellBorder"));

			return this;
		}
	}
}
