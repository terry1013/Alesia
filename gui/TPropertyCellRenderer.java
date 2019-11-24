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
import javax.swing.table.*;

/**
 * special implementation of {@link DefaultTableCellRenderer} to {@link TPropertyJTable}
 * 
 * @author terry
 * 
 */
public class TPropertyCellRenderer extends DefaultTableCellRenderer {

	private static final int ICON_SIZE = 10;
	private Icon expandIcon;
	private Icon collapseIcon;
	// private Color odd_color, pair_color;

	/**
	 * new instance
	 */
	public TPropertyCellRenderer() {
		// this.pair_color = UIManager.getColor("Table.background");
		// this.odd_color = new Color(230, 230, 255);

	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		if (row == table.getRowCount() - 1 || !table.isEnabled()) {
			hasFocus = false;
		}
		JLabel render = (JLabel) super.getTableCellRendererComponent(table, value, false, hasFocus, row, column);

		if (column == 0) {
			super.setBackground(table.getTableHeader().getBackground());
			render.setHorizontalAlignment(SwingConstants.CENTER);
			if (row == table.getRowCount() - 1) {
				render.setIcon(getExpandIcon());
			} else {
				render.setIcon(getCollapseIcon());
			}
		} else {
			render.setHorizontalAlignment(SwingConstants.LEFT);
			render.setBackground(UIManager.getColor("Table.background"));
			if (table.isEnabled() && row != table.getRowCount() - 1) {
				if (!isSelected) {
					// setBackground((row % 2 == 0) ? pair_color : odd_color);
				}
			}
			render.setIcon(null);
		}
		return render;
	}

	/**
	 * @return the collapseIcon
	 */
	private Icon getCollapseIcon() {
		if (collapseIcon == null) {
			collapseIcon = new CollapseIcon();
		}
		return collapseIcon;
	}

	/**
	 * @return the expandIcon
	 */
	private Icon getExpandIcon() {
		if (expandIcon == null) {
			expandIcon = new ExpandIcon();
		}
		return expandIcon;
	}

	private class CollapseIcon implements Icon {
		private int width = ICON_SIZE;
		private int height = ICON_SIZE;

		public int getIconHeight() {
			return height;
		}

		public int getIconWidth() {
			return height;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			Color cBackUp = g.getColor();
			g.setColor(Color.black);
			// g.drawRect(x + 1, y + 1, width - 2, height - 2);
			// g.drawLine(x+1, y+1+((height - 2)/2), x+1+(width-2), y+1+((height - 2)/2));//Vertical
			// lini
			g.drawLine(x + 3, y + 1 + ((height - 2) / 2), x + (width - 3), y + 1 + ((height - 2) / 2));// Verical line
			g.setColor(cBackUp);
		}

	}
	private class ExpandIcon implements Icon {
		private int width = ICON_SIZE;
		private int height = ICON_SIZE;

		public int getIconHeight() {
			return height;
		}

		public int getIconWidth() {
			return height;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			Color cBackUp = g.getColor();
			g.setColor(Color.black);
			// g.drawRect(x + 1, y + 1, width - 2, height - 2);
			g.drawLine(x + 3, y + 1 + ((height - 2) / 2), x + (width - 3), y + 1 + ((height - 2) / 2));// Horizontal
																										// line
			g.drawLine(x + 1 + (width - 2) / 2, y + 3, x + 1 + (width - 2) / 2, height - 1); // Vertical
																								// line
			g.setColor(cBackUp);
		}

	}

}
