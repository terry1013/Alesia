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
package delete.gui.docking.view;

import gui.*;
import gui.docking.*;

import java.awt.*;
import java.beans.*;

import javax.swing.*;
import javax.swing.table.*;

import core.*;
import core.datasource.*;
import delete.*;

/**
 * visor de resultado de datos durante generacion de reporte dinamico
 * 
 */
public class DataView extends UIComponentPanel implements DockingComponent {

	private JTable table;
	private Font tableFont;

	/**
	 * nueva instancia
	 * 
	 * 
	 */
	public DataView() {
		super(null, false);
		// this.searchTextField = new SearchTextField(20, this, false);

		// setToolBar(new ExportToFileAction(this, "sle_people", "audit_track;pephoto"));

		this.table = new JTable();
		this.tableFont = new Font("Courier New", Font.PLAIN, 12);

		// TJTable
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setFont(tableFont);
		JScrollPane jsp = new JScrollPane(table);
		jsp.getViewport().setBackground(Color.WHITE);
		addWithoutBorder(jsp);
	}
	@Override
	public void init() {
		// addPropertyChangeListener(TConstants.SHOW_DATA, this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(TConstants.FIND_TEXT)) {

			TDRTableModel tmod = new TDRTableModel(DR.records);
			table.setModel(tmod);

			// ancho de las columnas. se toma el mayor entre el encabezado y la longitud del campo o
			// el valor salvado y el minimo entre este y 100
			TableColumnModel cm = table.getColumnModel();
			Record mod = DR.model;

			for (int c = 0; c < tmod.getColumnCount(); c++) {
				TableColumn tc = cm.getColumn(c);
				int fz = mod.getFieldSize(c);
				int fcz = mod.getFieldName(0).length();
				tc.setPreferredWidth(Math.min((fz > fcz ? fz : fcz) * 9, 100 * 9));
			}
			table.setVisible(true);
		}
	}
}
