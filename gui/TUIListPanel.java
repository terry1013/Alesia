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
import javax.swing.event.*;
import javax.swing.table.*;

import org.javalite.activejdbc.*;

import com.alee.laf.list.*;
import com.alee.laf.table.*;
import com.alee.laf.table.editors.*;

import action.*;
import core.*;

public abstract class TUIListPanel extends TUIPanel
		implements
			ListSelectionListener,
			ActionPerformer,
			TableModelListener {

	public class TTableCellEditor extends WebGenericEditor {
		private String[] showColumns;
		private Component editCmp;

		public TTableCellEditor(String cols) {
			this.showColumns = cols.split(";");
		}

		@Override
		public Object getCellEditorValue() {
			Object val = super.getCellEditorValue();
			// Override to return TEntry instance where key=FieldName, value=Object value
			TEntry te = new TEntry(editCmp.getName(), val);
			return te;
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			this.editCmp = super.getTableCellEditorComponent(table, value, isSelected, row, column);
			// TAbstractTableModel tatm = (TAbstractTableModel) table.getModel();
			String col = showColumns[column];
			// set the component name as a field name with is being editing
			editCmp.setName(col);
			return editCmp;
		}

	}
	public static int TABLE_VIEW = 0;
	public static int LIST_VIEW_VERTICAL = 1;
	public static int LIST_VIEW_MOSAIC = 2;
	/**
	 * elemento dentro de una lista seleccionado. la clse de propiedad: Record
	 * 
	 */
	public static final String MODEL_SELECTED = "ModelSelected";
	private int view;
	private JScrollPane js_pane;
	private TAbstractTableModel tableModel;
	private WebTable webTable;
	private TAbstractListModel listModel;
	private WebList tJlist;
	private String specialFieldID;
	private boolean cellEditable;

	private Hashtable<String, Hashtable> referenceColumns;

	private String iconParameters;

	private String tableColumns;

	/**
	 * nueva instancia
	 * 
	 * @param dname - nombre del documento
	 */
	public TUIListPanel() {
		this.js_pane = new JScrollPane();
		referenceColumns = new Hashtable();
		// better look for weblaf
		js_pane.setBorder(null);
		js_pane.getViewport().setBackground(Color.WHITE);

		this.view = TABLE_VIEW;

		createJTable();
		setBodyComponent(js_pane);
	}

	/**
	 * default implementation for {@link FilterAction}. this method set the {@link ServiceRequest#FILTER_FIELDS} and
	 * {@link ServiceRequest#FILTER_VALUE} parameters for this instance of {@link ServiceRequest} and send the
	 * transaction to retrive the filter result.
	 * <p>
	 * 180312: the previous implementatin based on ServiceRequest.FILTER_FIELDS and ServiceRequest.FILTER_VALUE are
	 * deprecated: in order to unify the future filter interface like tree does, the fielter action act over the lists
	 * of elements that already present on screen
	 * 
	 * @param txt - text to look for or "" to clear the tilter
	 */
	public void filterList(String txt) {
		//
		// // sortablemodel may be null if list depend on another component selection.
		// if (tableModel != null) {
		// // perform data filter
		// if (!txt.trim().toString().equals("")) {
		// Vector<Record> rlst = new Vector<Record>();
		// rlst.addAll(tableModel.getRecords());
		// Record rm = tableModel.getRecordModel();
		// TransactionsUtilities.filterList(rlst, tableColumns, txt);
		// filterRequest = new ServiceRequest(ServiceRequest.CLIENT_GENERATED_LIST, rm.getTableName(), rlst);
		// filterRequest.setParameter(ServiceResponse.RECORD_MODEL, rm);
		// tableModel.setServiceRequest(filterRequest);
		// } else {
		// // reset with the original model
		// tableModel.setServiceRequest(serviceRequest);
		// }
		// repaint();
		// }
	}

	public void freshen() {
		tableModel.freshen();
		listModel.freshen();
		listModel = new TAbstractListModel(tableModel);
		tJlist.setModel(listModel);
		// 171231: if no element to display, disable actions
		if (tableModel.getRowCount() == 0) {
			setEnableActions("scope", "element", false);
		}
		this.repaint();
	}

	public String getIconParameters() {
		return iconParameters;
	}

	public JTable getJTable() {
		return webTable;
	}

	/**
	 * Return the current selected model in the List. If no element has been selectcted, return <code>null</code>
	 * 
	 * @return the selected {@link Model}
	 */
	public Model getModel() {
		Model m = null;
		int sr = -1;
		sr = (view == TABLE_VIEW) ? webTable.getSelectedRow() : -1;
		sr = (view == LIST_VIEW_MOSAIC || view == LIST_VIEW_VERTICAL) ? tJlist.getSelectedIndex() : sr;
		// verifico sortablemodel.getrowcount() porque si el modelo cambia (evento tablechange)
		// el contenido de la table no ha cambiado y puede generar error
		// ArrayIndexOutOfBoundsException
		if (sr > -1 && sr < tableModel.getRowCount()) {
			m = tableModel.getModelAt(sr);
		}
		return m;
	}

	public Model[] getRecords() {
		int[] idxs = webTable.getSelectedRows();
		if (view == LIST_VIEW_MOSAIC || view == LIST_VIEW_VERTICAL) {
			idxs = tJlist.getSelectedIndices();
		}
		Model[] models = new Model[idxs.length];
		for (int rc = 0; rc < idxs.length; rc++) {
			models[rc] = tableModel.getModelAt(idxs[rc]);
		}
		return models;
	}

	public TAbstractTableModel getTableModel() {
		return tableModel;
	}

	abstract public TUIFormPanel getTUIFormPanel(Action action);

	/**
	 * metodo de inicializacion. subclases deben implementar este metodo para completar la contruccion de la misma. Ej
	 * las clases en el paquete gui.impl deben usar este metodo para establecer la solicitud de servicio.
	 * 
	 */
	abstract public void init();

	/**
	 * localiza el registro pasado como argumento y si este se encuentra dentro de la lista, lo selecciona
	 * 
	 * @param rcd - registro a seleccionar
	 */
	public void selectModel(Model model) {
		int idx = tableModel.indexOf(model);
		setEnableActions("scope", "element", idx > -1);
		if (idx > -1) {
			webTable.getSelectionModel().setValueIsAdjusting(true);
			webTable.getSelectionModel().removeIndexInterval(0, tableModel.getRowCount());
			webTable.getSelectionModel().addSelectionInterval(idx, idx);
			webTable.getSelectionModel().setValueIsAdjusting(false);
			tJlist.setSelectedIndex(idx);
		}
	}

	public void setCellEditable(boolean cee) {
		// columns must be setted
		if (tableColumns == null) {
			throw new NullPointerException("no columns set for this component. Call setColumns(String)");
		}
		this.cellEditable = cee;
	}

	/**
	 * Set a custom format pattern to a given column. This format is passed directly to the active instance of
	 * {@link TDefaultTableCellRenderer}
	 * 
	 * @param col - column id where the pattern need to be apply
	 * @param patt - String pattern to apply
	 * 
	 * @see TDefaultTableCellRenderer#getFormats()
	 */
	public void setColumnFormat(int col, String fmt) {
		TDefaultTableCellRenderer dtcr = (TDefaultTableCellRenderer) getJTable().getDefaultRenderer(Double.class);
		dtcr.setColumnFormat(col, fmt);
	}

	public void setColumns(String cols) {
		this.tableColumns = cols;
		// temp
		TDefaultListCellRenderer tdlcr = (TDefaultListCellRenderer) tJlist.getCellRenderer();
		tdlcr.setColumns(cols);
	}
	public void setDefaultRenderer(TDefaultTableCellRenderer tdcr) {
		webTable.setDefaultRenderer(TEntry.class, tdcr);
		webTable.setDefaultRenderer(String.class, tdcr);
		webTable.setDefaultRenderer(Date.class, tdcr);
		webTable.setDefaultRenderer(Integer.class, tdcr);
		webTable.setDefaultRenderer(Double.class, tdcr);
		webTable.setDefaultRenderer(Long.class, tdcr);
	}

	/**
	 * Indica los parametros necesarios para presentar el icono que adorna la celda. los parametros descritos en forma
	 * parm;parm;...
	 * 
	 * @param column - Numero de la columna donde se desea presentar el icono (vista tabla)
	 * @param icon - nombre del archivo icono o prefijo (si se especifica la columna valcol). si este valor es *, el
	 *        nombre especificado en <code>valcol</code> debe ser instancia de byte[] donde esta almacenado el icono.
	 * @param valcol - nombre de la columna donde se obtendra el valor que sera concatenado con el nombre especificado
	 *        en parametro <code>icon</code> para deterinar el nombre del archivo icono (puede no especificarse). si
	 *        <code>icon="*"</code> el campo especificado aqui, contiene los byte[] para crear la imagen
	 *        <p>
	 *        ejemplo:
	 *        <li>0;user_;t_usroll: idica que se desa colocar en la columna 0 el icono cuyo nombre comienza con user_ y
	 *        usar el valor de la columna t_usroll para concaternarlo con el nombre del archivo icono
	 *        <li>3;users4: colocar el icono llamado users4 en la 4ta columna
	 *        <li>0;*;userphoto: crea un icono usando los byte[] almacenados en la columan userphoto y lo coloca en la
	 *        columna 0
	 * 
	 * 
	 */
	public void setIconParameters(String ip) {
		TDefaultTableCellRenderer tdcr = (TDefaultTableCellRenderer) webTable.getDefaultRenderer(String.class);
		TDefaultListCellRenderer tdlcr = (TDefaultListCellRenderer) tJlist.getCellRenderer();
		if (ip != null) {
			String[] col_ico_val = ip.split(";");
			String vc = (col_ico_val.length > 2) ? col_ico_val[2] : null;
			tdcr.setIconParameters(Integer.parseInt(col_ico_val[0]), col_ico_val[1], vc);
			tdlcr.setIconParameters(col_ico_val[1], vc);
		} else {
			tdcr.setIconParameters(0, "document", null);
			tdlcr.setIconParameters("document", null);
		}
	}
	/**
	 * Asociate a sublist of values for the internal value in this model. when the column value is request by JTable,
	 * the internal value is mapped whit this list to return the meaning of the value instead the value itselft
	 * 
	 * @param fn - column name
	 * 
	 * @param telist array of TEntry
	 */
	public void setReferenceColumn(String fn, TEntry[] telist) {
		Hashtable ht = new Hashtable();
		for (TEntry te : telist) {
			ht.put(te.getKey(), te.getValue());
		}
		referenceColumns.put(fn.toUpperCase(), ht);
	}

	public void setServiceRequest(LazyList list) {
		if (list != null) {
			this.specialFieldID = (String) getClientProperty(TConstants.SPECIAL_COLUMN);

			// table model
			this.tableModel = new TAbstractTableModel(list);
			webTable.setModel(tableModel);
			setTableColumns(tableColumns);
			tableModel.setReferenceColumn(referenceColumns);
			tableModel.addTableModelListener(this);

			// cell editor for all my columns
			tableModel.setCellEditable(cellEditable);
			if (cellEditable) {
				// TTableCellEditor ttce = new TTableCellEditor(tableColumns);
				// String[] cls = tableColumns.split(";");
				// Record mod = tableModel.getRecordModel();
				// for (String fn : cls) {
				// webTable.setDefaultEditor(mod.getFieldValue(fn).getClass(), ttce);
				// }
			}

			// list model
			this.listModel = new TAbstractListModel(tableModel);
			tJlist.setModel(listModel);
			if (tableModel.getRowCount() > 0) {
				// tJlist.setPrototypeCellValue(tableModel.getRecordAt(0));
			}

			setView(view);
			setEnableActions("scope", "element", false);
			// setMessage(null);
			// TTaskManager.getListUpdater().add(this);
		} else {
			// TTaskManager.getListUpdater().remove(this);
			// setMessage("ui.msg11");
		}
	}

	public void setView(int nv) {
		this.view = nv;
		if (view == TABLE_VIEW) {
			js_pane.setViewportView(webTable);
		}
		if (view == LIST_VIEW_MOSAIC || view == LIST_VIEW_VERTICAL) {
			if (tJlist != null) {
				tJlist.setLayoutOrientation((view == LIST_VIEW_MOSAIC) ? JList.HORIZONTAL_WRAP : JList.VERTICAL);
				if (view == LIST_VIEW_MOSAIC) {
					// js_pane = new JScrollPane(tJlist, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
					js_pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					js_pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
					// js_pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					tJlist.revalidate();
				}
				js_pane.setViewportView(tJlist);
			}
		}
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		// puede venir desde varios listener
		if (e.getSource() == tableModel) {
			webTable.tableChanged(e);
			// tJlist.ensureIndexIsVisible(sortableModel.getRowCount());
			setEnableActions("scope", "element", !webTable.getSelectionModel().isSelectionEmpty());
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		// soporte basico para seleccion de elementos dentro de la tabla/lista
		if (e.getValueIsAdjusting()) {
			return;
		}
		setEnableActions("scope", "element", !((ListSelectionModel) e.getSource()).isSelectionEmpty());
		firePropertyChange(MODEL_SELECTED, null, getModel());
	}

	private void createJTable() {
		this.webTable = new WebTable();
		webTable.addMouseListener(new ListMouseProcessor(webTable));

		ListSelectionModel lsm = webTable.getSelectionModel();
		lsm.addListSelectionListener(this);

		webTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		webTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		webTable.setShowGrid(false);

		this.tJlist = new WebList();
		tJlist.addMouseListener(new ListMouseProcessor(tJlist));
		lsm = tJlist.getSelectionModel();
		lsm.addListSelectionListener(this);

		// cellrenderer
		TDefaultTableCellRenderer tdcr = new TDefaultTableCellRenderer();
		setDefaultRenderer(tdcr);
		TDefaultListCellRenderer tdlcr = new TDefaultListCellRenderer();
		tJlist.setCellRenderer(tdlcr);

		setIconParameters(null);
	}

	private void setTableColumns(String cols) {
		// this.init = true;
		// this.sortableModel = uiListPanel.getTableModel();
		String[] cls = cols.split(";");
		DefaultTableColumnModel dtcm = new DefaultTableColumnModel();
		// TODO: check for error in columnname??
		// Model mod = tableModel.getModel();
		for (int col = 0; col < cls.length; col++) {
			// TODO: put columwith using the 2 argument contstructor
			dtcm.addColumn(new TableColumn(col));
		}
		webTable.setColumnModel(dtcm);

		// row sorter
		TableRowSorter sorter = new TableRowSorter(tableModel);
		webTable.setRowSorter(sorter);
		tableModel.setTableRowSorter(sorter);

		// fixTableColumn();
		// this.columnModel = getColumnModel();
		// this.init = false;

	}
}
