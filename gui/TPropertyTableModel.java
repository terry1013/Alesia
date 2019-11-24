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

import java.util.*;

import javax.swing.table.*;

import core.*;



/**
 * implementacion de <code>AbstractTableModel</code> especificamente diseñada para la edicion de lista de propiedades en
 * general. esta tabla presenta 3 columnas:
 * <ol>
 * <li>columna que permite al usuario eleminar registros ya creados dentro de la lista o adicionar uno nuevo rellenando
 * la line en blanco que se presenta al final de la lista.
 * <li>Columna con las claves para la propiedad
 * <li>Columna con los valores para la propiedad.
 * </ol>
 * 
 * @author terry
 * 
 */
public class TPropertyTableModel extends AbstractTableModel {

	private static final String[] COLUMNS_NAMES = {"", "Property", "Value"};
	private final String emptyString = new String();
	private Vector<TEntry> properties;

	/**
	 * nuevo modelo de propiedades.
	 * 
	 * @param prp - Vector con elementos <code>TEntry</code> con los pares clave/valor
	 */
	public TPropertyTableModel(Vector<TEntry> prp) {
		this.properties = prp;
	}

	public void addProperty(TEntry it) {
		properties.add(it);
		fireTableDataChanged();
	}

	public void removeProperty(int row) {
		properties.remove(row);
		fireTableDataChanged();
	}

	@Override
	public String getColumnName(int column) {
		return COLUMNS_NAMES[column];
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return (columnIndex > 0 && columnIndex < COLUMNS_NAMES.length) && rowIndex < getRowCount() - 1;
	}

	@Override
	public int getColumnCount() {
		return COLUMNS_NAMES.length;
	}

	@Override
	public int getRowCount() {
		return properties.size() + 1;
	}
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (properties != null && rowIndex < getRowCount() - 1) {
			switch (columnIndex) {
				case 0 :
					return emptyString;
				case 1 :
					return properties.elementAt(rowIndex).getKey();
				case 2 :
					return properties.elementAt(rowIndex).getValue();
			}
		} else if (properties != null && rowIndex == getRowCount() - 1) {
			if (columnIndex == 0)
				return emptyString;
		}
		return null;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		// super.setValueAt(aValue, rowIndex, columnIndex);
		TEntry t = properties.elementAt(rowIndex);
		if (properties != null) {
			switch (columnIndex) {
				case 1 :
					t.setKey((aValue != null) ? aValue.toString() : new String());
					fireTableCellUpdated(rowIndex, columnIndex);
					break;
				case 2 :
					t.setValue((aValue != null) ? aValue.toString() : new String());
					fireTableCellUpdated(rowIndex, columnIndex);
					break;
			}
		}
	}
}
