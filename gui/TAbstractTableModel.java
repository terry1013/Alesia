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
import core.datasource.*;

/**
 * extencion de <code>AbstractTableModel</code> con soporte varios para aplicacion
 * 
 */
public class TAbstractTableModel extends AbstractTableModel {

	private TableRowSorter rowSorter;
	private Record rcdModel;
	private Vector records;
	private ServiceRequest sRequest;
	private boolean isTranspose, allowCellEdit;
	private String transposeColumnName;
	private String[] showColumns;
	private Hashtable<Integer, Hashtable> referenceColumns;

	public TAbstractTableModel(ServiceRequest s) {
		super();
		this.records = new Vector();
		this.rowSorter = null;
		this.referenceColumns = new Hashtable<Integer, Hashtable>();

		setServiceRequest(s);
	}

	public Vector<Record> getRecords() {
		return records;
	}

	public void freshen() {
		if (sRequest == null) {
			return;
		}
		int bef_rc = records.size();
		setServiceRequest(sRequest);
		int aft_rc = records.size();

		// System.out.println("aft_rc " + aft_rc);
		// int rc = rowSorter.getViewRowCount();
		// System.out.println("getViewRowCount " + rc);

		rowSorter.allRowsChanged();
		if (aft_rc == 0) {
			return;
		}
		if (bef_rc != aft_rc) {
			// temporal: eliminar registro seleccionado
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
		return isTranspose ? Object.class : rcdModel.getFieldValue(idx).getClass();
	}

	@Override
	public int getColumnCount() {
		return isTranspose ? records.size() : rcdModel.getFieldCount();
	}

	@Override
	public String getColumnName(int col) {
		return isTranspose ? transposeColumnName + " " + col : rcdModel.getFieldName(col);
	}

	public Record getRecordModel() {
		return new Record(rcdModel);
	}

	/**
	 * return the Record found in <code>row</code> position
	 * 
	 * @param row - row
	 * 
	 * @return Record
	 */
	public Record getRecordAt(int row) {
		int row1 = rowSorter == null ? row : rowSorter.convertRowIndexToModel(row);
		return (Record) records.elementAt(row1);
	}

	@Override
	public int getRowCount() {
		return isTranspose ? showColumns.length : records.size();
	}

	/**
	 * rturn the {@link ServiceRequest} used to buld data model
	 * 
	 * @return ServiceRequest
	 */
	public ServiceRequest getServiceRequest() {
		return sRequest;
	}

	/**
	 * Asociate a sublist of values for the internal value in this model. when the column value is request by JTable,
	 * the internal value is mapped whit this list to return the meaning of the value instead the value itselft
	 * 
	 * @param col - column
	 * @param dom - {@link Hashtable} with the sublist of element to map
	 */
	// public void setReferenceColumn(int col, Hashtable dom) {
	public void setReferenceColumn(Hashtable refc) {
		referenceColumns = refc;
		// referenceColumns.put(col, dom);
	}

	@Override
	public Object getValueAt(int row, int col) {
		Record r = (Record) records.elementAt(isTranspose ? col : row);
		Object rv = null;
		if (isTranspose) {
			String fn = showColumns[row];
			rv = r.getFieldValue(fn);
		} else {
			rv = r.getFieldValue(col);
		}

		// check the reference by column
		// 180311 TODO: move to celrenderer (but if its moved to cellrenderer the column sort maybe show some
		// "sort error" because the internal value will be different form the external representation.)
		Hashtable ht = referenceColumns.get(r.getFieldName(col));
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
	/**
	 * retrona el indice donde se encuentra el registro parado como argumento o -1 si no esta dentro de la lista. esete
	 * elemento verifica <code>Record.toString()</code> que retorna solo los valores de la clave de registro.
	 * 
	 * @param rcd - registro a localizar
	 */
	public int indexOf(Record rcd) {
		return records.indexOf(rcd);
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		// cell is editable iff allow cell edit and is not a keyfield
		boolean kf = rcdModel.isKeyField(isTranspose ? row : column);
		return allowCellEdit && !kf;
	}

	public boolean isTranspose() {
		return isTranspose;
	}

	public void setCellEditable(boolean ce, boolean ke) {
		allowCellEdit = ce;
	}

	/**
	 * set the ServiceRequest to this model and process the request reloading the data
	 * 
	 * @param sr - ServiceRequest
	 */
	public void setServiceRequest(ServiceRequest sr) {
		sRequest = sr;
		// special treatment for Clientgeneratedlist because clear destroy the generated data
		if (!sr.getName().equals(ServiceRequest.CLIENT_GENERATED_LIST)) {
			records.clear();
		}
		if (sRequest != null) {
			ServiceResponse res = ServiceConnection.sendTransaction(sRequest);
			records = (Vector) res.getData();
			rcdModel = (Record) res.getParameter(ServiceResponse.RECORD_MODEL);
		}
	}

	public void setTableRowSorter(TableRowSorter trs) {
		this.rowSorter = trs;
	}

	public void setTransporseParameters(String cid, String cols) {
		isTranspose = true;
		transposeColumnName = TStringUtils.getString(cid);
		showColumns = cols.split(";");
	}
}
