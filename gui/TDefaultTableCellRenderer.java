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
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import org.javalite.activejdbc.*;

import core.*;

/**
 * Extension de <code>DefaultTableCellRenderer</code> que usa una instancia de <code>ExtendedJLabel</code> para
 * presentar diversos valores. adicionalmente esta clase establece 2 colores por omision para presentar las celdas pares
 * e impares en distintos colores permitiendo mejor visibilidad.
 * 
 * 
 */
public class TDefaultTableCellRenderer extends DefaultTableCellRenderer {

	protected Color odd_color, pair_color;
	private ExtendedJLabel extendedJLabel;
	private int newRendererColum, iconColum;
	private TableCellRenderer newRenderer;
	private ImageIcon imageIcon;
	private String iconName, valColumn;
	private java.util.Hashtable<Integer, String> formats;

	public TDefaultTableCellRenderer() {
		this.formats = new Hashtable<Integer, String>();
		// ExtendedJLabel is used for parsing values
		this.extendedJLabel = new ExtendedJLabel("");
		this.pair_color = UIManager.getColor("Table.background");
		this.odd_color = UIManager.getColor("Label.background");
		setIconParameters(-1, null, null);
		setNewCellRenderer(-1, null);
	}

	/**
	 * Set a custom format <code>fmt</code> to column <code>col</code>
	 * 
	 * @param col - table column
	 * @param fmt - format pattern
	 */
	public void setColumnFormat(int col, String fmt) {
		formats.put(col, fmt);
	}

	/**
	 * return the customs formtas setted to this renderer instance. This formats generaly came for
	 * {@link UIListPanel#setColumnFormat(int, String)}
	 * 
	 * @return custom formats.
	 */
	public Hashtable<Integer, String> getFormats() {
		return formats;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		TAbstractTableModel stm = (TAbstractTableModel) table.getModel();
		Model m = stm.getModelAt(row);

		// 171211: commented in order to enable reference by columns implemented in tablemodel.
		// FIXME: 171211 remove all external references. this class must only decarate the cell with the incoming values
		// String fn = showColumns[stm.isTranspose() ? row : column];
		// Object valuer = r.getExternalFieldValue(fn);

		// lookup for pattenr
		String patt = formats.get(column);
		if (patt != null) {
			extendedJLabel.setFormat(value.getClass(), patt);
		}
		extendedJLabel.setValue(value);
		setText(extendedJLabel.getText());
		setHorizontalAlignment(extendedJLabel.getHorizontalAlignment());
		setOpaque(true);

		if (!isSelected) {
			// setBackground((row % 2 == 0) ? pair_color : odd_color);
		}
		// redireccion de cellrenderer
		if (column == newRendererColum) {
			JComponent render = (JComponent) newRenderer.getTableCellRendererComponent(table, value, isSelected,
					hasFocus, row, column);
			render.setBackground(getBackground());
			return render;
		}
		// columna para presentar icono
		setIcon(null);
		if (column == iconColum) {
			// icono segun columna valor

			if (valColumn != null) {
				if (iconName.equals("*")) {
					imageIcon = new ImageIcon((byte[]) m.getBytes(valColumn));
					Image i = imageIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
					imageIcon = new ImageIcon(i);
				} else {
					String val = stm.getModelAt(row).getString(valColumn).toString();
					imageIcon = TResources.getSmallIcon(iconName + val);
				}
			}
			setIcon(imageIcon);
		}
		this.setBackgroud(table, value, isSelected, hasFocus, row, column);
		return this;
	}

	/**
	 * set the icon parameters for this cell renderer.
	 * 
	 * @param icol - identificador de la columna donde se presentara el icono
	 * @param icon - icon file name, prefix or "*"
	 * @param valCol - field name to complete the icon file name, or contain data necesary to create an imageIcon with
	 *        it
	 * 
	 * @see TConstants#ICON_PARAMETERS
	 * 
	 */
	public void setIconParameters(int icol, String icon, String valCol) {
		this.iconColum = icol;
		this.iconName = icon;
		this.valColumn = valCol;
		if (valCol == null) {
			imageIcon = TResources.getSmallIcon(iconName);
		}
	}

	/**
	 * Override this method to set be background color for this component. uset {@link #odd_color} to set the background
	 * color
	 * 
	 * @param table
	 * @param value
	 * @param isSelected
	 * @param hasFocus
	 * @param row
	 * @param column
	 */
	public void setBackgroud(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

	}

	/**
	 * establece una instancia diferente de <code>TableCellRenderer</code>. esto permita que una instancia de esta clase
	 * instalada dentro de una tabla, redirija la llamada a otra instancia de TableCellRenderer para entregar un nuevo
	 * componente sin alterar el aspecto general, principalmente los colores de celdas pare e impares y la alineacion
	 * 
	 * @param col - columna
	 * @param nren - instancia de TableCellRenderer
	 */
	public void setNewCellRenderer(int col, TableCellRenderer nren) {
		this.newRenderer = nren;
		this.newRendererColum = col;
	}
}
