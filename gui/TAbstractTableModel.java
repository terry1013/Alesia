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

import org.javalite.activejdbc.*;

public class TAbstractTableModel extends AbstractTableModel {

	private LazyList<Model> lazyList;
	private Model model;
	private TableRowSorter rowSorter;
	private boolean allowCellEdit;
	private Hashtable<String, Hashtable> referenceColumns;
	private Vector<String> attributeNames;

	public TAbstractTableModel(Class model, LazyList list) {
		this.rowSorter = null;
		this.referenceColumns = new Hashtable<String, Hashtable>();
		this.attributeNames = new Vector<>();
		this.lazyList = list;
		attributeNames = new Vector<String>(model.attributeNames());
		System.out.println("TAbstractTableModel.TAbstractTableModel()");
	}

	/**
	 * Return the indix of the model pass as argument. if the model is not in the list, return -1
	 * 
	 * @param model - {@link Model} to find
	 * 
	 * @return index of the model
	 */
	public int indexOf(Model model) {
		return lazyList.indexOf(model);
	}

	public void freshen() {
		if (lazyList == null) {
			return;
		}
		int bef_rc = lazyList.size();
		lazyList.load();
		int aft_rc = lazyList.size();
		rowSorter.allRowsChanged();
		if (aft_rc == 0) {
			return;
		}
		if (bef_rc != aft_rc) {
			fireTableDataChanged();
		} else {
			try {
				rowSorter.allRowsChanged();
				fireTableRowsUpdated(0, aft_rc - 1);
			} catch (Exception e) {
				// temporal
				fireTableDataChanged();
				System.out.println("--------");
				// e.printStackTrace();
			}
		}
	}

	@Override
	public Class getColumnClass(int idx) {
		String atn = attributeNames.get(idx);
		return model.get(atn).getClass();
	}

	@Override
	public int getColumnCount() {
		return attributeNames.size();
	}

	@Override
	public String getColumnName(int col) {
		return attributeNames.elementAt(col);
	}

	public Model getModel() {
		return model;
	}

	/**
	 * return the Record found in <code>row</code> position
	 * 
	 * @param row - row
	 * 
	 * @return Record
	 */
	public Model getModelAt(int row) {
		int row1 = rowSorter == null ? row : rowSorter.convertRowIndexToModel(row);
		return lazyList.get(row1);
	}

	@Override
	public int getRowCount() {
		return lazyList.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		Model model = lazyList.get(row);
		String atn = attributeNames.elementAt(col);
		Object rv = model.get(atn);

		// check the reference by column
		// 180311 TODO: move to celrenderer (but if its moved to cellrenderer the column sort maybe show some
		// "sort error" because the internal value will be different form the external representation.)
		Hashtable ht = referenceColumns.get(atn);
		if (ht != null) {
			Object rv1 = ht.get(rv);
			// 180311: rv.tostring to allow from number, bool to key from TEntrys ( that are generaly created from
			// propeties files) only if previous request is null
			rv1 = (rv1 == null) ? ht.get(rv.toString()) : rv1;
			// 180416: if no reference found, remark the value (NOTE: temp: not fully tested. wath about numbers or
			// tentry columns??
			rv = (rv1 == null) ? "<html><FONT COLOR='#FF0000'><i>" + rv + "</html>" : rv1;
		}
		return rv;
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		String atn = attributeNames.get(column);
		String keys[] = model.getCompositeKeys();
		boolean ispk = false;
		for (String k : keys) {
			ispk = atn.equals(k) ? true : ispk;
		}
		// cell is editable iff allow cell edit and is not a keyfield
		return allowCellEdit && !ispk;
	}

	public void setCellEditable(boolean ce) {
		allowCellEdit = ce;
	}

	public void setModel(Model model) {
		this.model = model;
		attributeNames = new Vector<String>(model.attributeNames());
		lazyList.load();
	}

	/**
	 * Asociate a sublist of values for the internal value in this model. when the column value is request by JTable,
	 * the internal value is mapped whit this list to return the meaning of the value instead the value itselft
	 * 
	 * @param refc - an instance of {@link Hashtable}
	 */
	public void setReferenceColumn(Hashtable refc) {
		referenceColumns = refc;
	}

	public void setTableRowSorter(TableRowSorter trs) {
		this.rowSorter = trs;
	}
}
