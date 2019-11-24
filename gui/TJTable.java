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

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import core.*;
import core.datasource.*;

public class TJTable extends JTable {

	private TAbstractTableModel sortableModel;
	private TableColumnModel columnModel;
	protected String tosavewidth;
	private boolean init;
	private String classname;
	private UIListPanel uiListPanel;

	public TJTable(UIListPanel src) {
		super();
		this.uiListPanel = src;
		this.classname = src.getClass().getName();
	}

	public void setColumns(String cols) {
		this.init = true;
		this.sortableModel = uiListPanel.getTableModel();
		String[] cls = cols.split(";");
		DefaultTableColumnModel dtcm = new DefaultTableColumnModel();
		Record mod = sortableModel.getRecordModel();
		for (String col : cls) {
			dtcm.addColumn(new TableColumn(mod.getIndexOf(col)));
		}
		setColumnModel(dtcm);

		// row sorter
		TableRowSorter sorter = new TableRowSorter(sortableModel);
		setRowSorter(sorter);
		sortableModel.setTableRowSorter(sorter);

		fixTableColumn();
		this.columnModel = getColumnModel();
		this.init = false;

	}

	@Override
	public void setModel(TableModel dataModel) {
		super.setModel(dataModel);
		if (dataModel instanceof TAbstractTableModel) {
			this.sortableModel = (TAbstractTableModel) dataModel;
		}
	}

	@Override
	public void columnMarginChanged(ChangeEvent e) {
		super.columnMarginChanged(e);

		// actualiza valores a salvar (no en inicializacion)
		if (init) {
			return;
		}
		StringBuffer val = new StringBuffer();
		for (int k = 0; k < columnModel.getColumnCount(); k++) {
			TableColumn tc = columnModel.getColumn(k);
			val.append(tc.getWidth());
			val.append(k == columnModel.getColumnCount() - 1 ? "" : ";");
		}
		this.tosavewidth = val.toString();
		TPreferences.setPreference(TPreferences.TABLE_COLUMN_WIDTH, classname, tosavewidth);
	}

	/**
	 * este metodo altera la instancia de <code>TableColumnsModel</code> para ajustar el ancho de la columna. el acncho
	 * es determinado seleccionando el mas largo entre el titulo de la columna o el maximo ancho de su contenido
	 * 
	 */
	private void fixTableColumn() {
		TableColumnModel cm = getColumnModel();
		Record mod = sortableModel.getRecordModel();

		// verifica si exite una preferencia de ancho de columna salvada para esta clase
		String vals[] = null;
		String v = (String) TPreferences.getPreference(TPreferences.TABLE_COLUMN_WIDTH, classname, null);
		if (v != null) {
			this.tosavewidth = v;
			vals = tosavewidth.split(";");
		}

		// ancho de las columnas. se toma el mayor entre el encabezado y la longitud del campo o
		// el valor salvado y el minimo entre este y 100
		for (int c = 0; c < cm.getColumnCount(); c++) {
			TableColumn tc = cm.getColumn(c);
			String en = TStringUtils.getString(mod.getFieldName(tc.getModelIndex()));
			tc.setHeaderValue(en);
			if (vals == null || c > vals.length) {
				int siz = en.length() > mod.getFieldSize(c) ? en.length() : mod.getFieldSize(c);
				tc.setPreferredWidth(Math.min(siz * 9, 100 * 9));
			} else {
				if (c < vals.length) {
					tc.setPreferredWidth(Integer.parseInt(vals[c]));
				}
			}
		}
	}
}
