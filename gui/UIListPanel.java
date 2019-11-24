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

import action.*;

import com.alee.laf.list.*;
import com.alee.laf.table.editors.*;

import core.*;
import core.datasource.*;
import core.tasks.*;

/**
 * Centraliza el manejo de operaciones comunes a todas las subclases que tienen como funcion presentar una lista (o
 * conjuto) de registros para que el usuario pueda realizar operaciones con ellos.
 * 
 * si el usuario selecciona un elemento dentro de la tabla, se cambia la propiedad
 * putClientProperty(PropertyNames.RECORD_SELECTED, r);
 * 
 * NOTA: subclases DEBEN usar metodo init() para establecer solicitud de servicio dado que las autorizaciones ejecutan
 * cls.newInstance() y esto genera carga inesesaria en la base de datos
 * 
 * 
 */
public abstract class UIListPanel extends UIComponentPanel
		implements
			Exportable,
			EditableList,
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
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
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
	private int view;
	private JScrollPane js_pane;
	private TAbstractTableModel tableModel;
	private TJTable tJTable;
	private TAbstractListModel listModel;
	private WebList tJlist;
	private String specialFieldID;
	private boolean cellEditable;
	private Hashtable<String, Hashtable> referenceColumns;

	private ServiceRequest serviceRequest, filterRequest;

	private String iconParameters;

	private String tableColumns;

	/**
	 * nueva instancia
	 * 
	 * @param dname - nombre del documento
	 */
	public UIListPanel(String dname) {
		super(dname, false);
		this.js_pane = new JScrollPane();
		referenceColumns = new Hashtable();
		// better look for weblaf
		js_pane.setBorder(null);
		js_pane.getViewport().setBackground(Color.WHITE);

		this.view = TABLE_VIEW;

		createJTable();
		addWithoutBorder(js_pane);
	}

	@Override
	public boolean executeAction(TActionEvent event) {

		boolean ok = true;

		// si la accion de redireccion es alguna instancia de acciones de cancelacion, se
		// retorna sin hacer nada.

		if (event.getRedirectAction() instanceof DefaultCancelAction) {
			return true;
		}

		// estandar edicion
		if (event.getSource() instanceof EditRecord) {
			AbstractRecordDataInput ardi = (AbstractRecordDataInput) event.getData();
			Record r = ardi.getRecord();
			ServiceConnection.sendTransaction(ServiceRequest.DB_UPDATE, r.getTableName(), r);
		}

		// validacion y creacion de nuevo registro
		if (event.getSource() instanceof NewRecord) {
			AbstractRecordDataInput ardi = (AbstractRecordDataInput) event.getData();
			ardi.validateNewRecord();
			ok = !ardi.isShowingError();
			if (!ardi.isShowingError()) {
				Record r = ardi.getRecord();
				ServiceConnection.sendTransaction(ServiceRequest.DB_ADD, r.getTableName(), r);
			}
		}

		// estandar supresion de registro
		if (event.getSource() instanceof DeleteRecord) {
			Object[] options = {TStringUtils.getString("action.delete.confirm"),
					TStringUtils.getString("action.delete.cancel")};
			int o = JOptionPane.showOptionDialog(Alesia.mainFrame, TStringUtils.getString("action.delete.message"),
					TStringUtils.getString("action.delete.title"), JOptionPane.DEFAULT_OPTION,
					JOptionPane.WARNING_MESSAGE, null, options, options[1]);
			if (o == JOptionPane.YES_OPTION) {
				Record rcd = getRecord();
				ok = ConnectionManager.getAccessTo(rcd.getTableName()).delete(rcd);
				/*
				 * Record[] rcds = getRecords(); for (int rc = 0; rc < rcds.length; rc++) { ok =
				 * ConnectionManager.getAccessTo(rcds[rc].getTableName()).delete(rcds[rc]); }
				 */
			} else {
				ok = false;
			}
		}

		// estandar filtro por campos.

		// operacion exitosa? refrescar
		if (ok) {
			freshen();
		}
		return ok;
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

		// sortablemodel may be null if list depend on another component selection.
		if (tableModel != null) {
			// perform data filter
			if (!txt.trim().toString().equals("")) {
				Vector<Record> rlst = new Vector<Record>();
				rlst.addAll(tableModel.getRecords());
				Record rm = tableModel.getRecordModel();
				TransactionsUtilities.filterList(rlst, tableColumns, txt);
				filterRequest = new ServiceRequest(ServiceRequest.CLIENT_GENERATED_LIST, rm.getTableName(), rlst);
				filterRequest.setParameter(ServiceResponse.RECORD_MODEL, rm);
				tableModel.setServiceRequest(filterRequest);
			} else {
				// reset with the original model
				tableModel.setServiceRequest(serviceRequest);
			}
			repaint();
		}
	}

	@Override
	public void freshen() {
		tableModel.freshen();
		listModel.freshen();
		listModel = new TAbstractListModel(tableModel);
		tJlist.setModel(listModel);
		// 171231: if no element to display, disable actions
		if (tableModel.getRowCount() == 0) {
			enableRecordScopeActions(false);
		}
		this.repaint();
	}

	public String getIconParameters() {
		return iconParameters;
	}

	public JTable getJTable() {
		return tJTable;
	}

	@Override
	public Record getRecord() {
		Record r = null;
		int sr = -1;
		sr = (view == TABLE_VIEW) ? tJTable.getSelectedRow() : -1;
		sr = (view == LIST_VIEW_MOSAIC || view == LIST_VIEW_VERTICAL) ? tJlist.getSelectedIndex() : sr;
		// verifico sortablemodel.getrowcount() porque si el modelo cambia (evento tablechange)
		// el contenido de la table no ha cambiado y puede generar error
		// ArrayIndexOutOfBoundsException
		if (sr > -1 && sr < tableModel.getRowCount()) {
			r = tableModel.getRecordAt(sr);
		}
		return r;
	}

	/**
	 * atajo que retorna el registro modelo usado en <code>SortableTableModel</code> este metodo es igual a
	 * <code>getSortableTable().getSortableTableModel().getModel()</code>
	 * 
	 * @return registro
	 */
	public Record getRecordModel() {
		return tableModel.getRecordModel();
	}

	public Record[] getRecords() {
		int[] ridx = tJTable.getSelectedRows();
		if (view == LIST_VIEW_MOSAIC || view == LIST_VIEW_VERTICAL) {
			ridx = tJlist.getSelectedIndices();
		}
		Record[] rcds = new Record[ridx.length];
		for (int rc = 0; rc < ridx.length; rc++) {
			rcds[rc] = tableModel.getRecordAt(ridx[rc]);
		}
		return rcds;
	}

	@Override
	public ServiceRequest getServiceRequest() {
		return serviceRequest;// tableModel.getServiceRequest();
	}

	public TAbstractTableModel getTableModel() {
		return tableModel;
	}

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
	public void selectRecord(Record rcd) {
		int idx = tableModel.indexOf(rcd);
		enableRecordScopeActions(idx > -1);
		if (idx > -1) {
			tJTable.getSelectionModel().setValueIsAdjusting(true);
			tJTable.getSelectionModel().removeIndexInterval(0, tableModel.getRowCount());
			tJTable.getSelectionModel().addSelectionInterval(idx, idx);
			tJTable.getSelectionModel().setValueIsAdjusting(false);
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
		TDefaultTableCellRenderer tdcr = (TDefaultTableCellRenderer) tJTable.getDefaultRenderer(String.class);
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

	/**
	 * establece una nueva transaccion que se encargara de mantener actualizado el modelo de datos para esta tabla. use
	 * este metodo si desea modificar a voluntad el servicio que se desea presentar por esta tabla. si el valor para el
	 * servicio es null, se coloca un panel en blanco. y se inhabilitan todas las acciones.
	 * 
	 * @param sr - solicitud de servicio.
	 */
	public void setServiceRequest(ServiceRequest sr) {
		if (sr != null) {
			this.serviceRequest = sr;
			this.specialFieldID = (String) getClientProperty(TConstants.SPECIAL_COLUMN);

			// table model
			this.tableModel = new TAbstractTableModel(serviceRequest);
			tJTable.setModel(tableModel);
			tJTable.setColumns(tableColumns);
			tableModel.setReferenceColumn(referenceColumns);
			tableModel.addTableModelListener(this);

			// cell editor for all my columns
			tableModel.setCellEditable(cellEditable, false);
			if (cellEditable) {
				TTableCellEditor ttce = new TTableCellEditor(tableColumns);
				String[] cls = tableColumns.split(";");
				Record mod = tableModel.getRecordModel();
				for (String fn : cls) {
					tJTable.setDefaultEditor(mod.getFieldValue(fn).getClass(), ttce);
				}
			}

			// list model
			this.listModel = new TAbstractListModel(tableModel);
			tJlist.setModel(listModel);
			if (tableModel.getRowCount() > 0) {
				tJlist.setPrototypeCellValue(tableModel.getRecordAt(0));
			}

			setView(view);
			enableRecordScopeActions(false);
			setMessage(null);
			TTaskManager.getListUpdater().add(this);
		} else {
			TTaskManager.getListUpdater().remove(this);
			setMessage("ui.msg11");
		}
	}

	public void setView(int nv) {
		this.view = nv;
		if (view == TABLE_VIEW) {
			js_pane.setViewportView(tJTable);
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
			tJTable.tableChanged(e);
			// tJlist.ensureIndexIsVisible(sortableModel.getRowCount());
			enableRecordScopeActions(!tJTable.getSelectionModel().isSelectionEmpty());
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		// soporte basico para seleccion de elementos dentro de la tabla/lista
		if (e.getValueIsAdjusting()) {
			return;
		}
		enableRecordScopeActions(!((ListSelectionModel) e.getSource()).isSelectionEmpty());
		firePropertyChange(TConstants.RECORD_SELECTED, null, getRecord());
	}

	private void createJTable() {
		this.tJTable = new TJTable(this);
		tJTable.addMouseListener(new ListMouseProcessor(tJTable));

		ListSelectionModel lsm = tJTable.getSelectionModel();
		lsm.addListSelectionListener(this);

		tJTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		tJTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tJTable.setShowGrid(false);

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
	
	public void setDefaultRenderer(TDefaultTableCellRenderer tdcr) {
		tJTable.setDefaultRenderer(TEntry.class, tdcr);
		tJTable.setDefaultRenderer(String.class, tdcr);
		tJTable.setDefaultRenderer(Date.class, tdcr);
		tJTable.setDefaultRenderer(Integer.class, tdcr);
		tJTable.setDefaultRenderer(Double.class, tdcr);
		tJTable.setDefaultRenderer(Long.class, tdcr);
	}

	/**
	 * Habilita/desabilita todas las acciones de tipo <code>TAbstractAction.RECORD_SCOPE</code> disponibles dentro de la
	 * barra de herramientas y menus emergentes. este metodo adicionalmente: <li>verifica la columna para valores
	 * especiales. si esta fue especificada, y el parametro de entrada es <code>true</code> (habilitar acciones) las
	 * acciones que sean instancias de <code>NoActionForSpecialRecord</code> seran inhabilitadas <li>para vista en
	 * arbol, Si el registro seleccionado es un nodo con hijos, instancias de <code>NoActionForSpecialRecord</code> no
	 * seran habilitadas (Ej: no se puede suprimir nodos con hijos.)
	 * 
	 * 
	 * @param ena - true: habilitar, false: deshabilitar
	 */
	protected void enableRecordScopeActions(boolean ena) {
		TAbstractAction[] btns = getToolBarActions();
		if (btns != null) {
			// si se especifico columna especial y el registro seleccionado actualmente lo contiene
			boolean isSpetialRecord = false;
			if (ena) {
				Record selr = getRecord();
				isSpetialRecord = (specialFieldID != null)
						&& ((String) selr.getFieldValue(specialFieldID)).startsWith("*");
			}

			for (int j = 0; j < btns.length; j++) {
				TAbstractAction ac = (TAbstractAction) btns[j];
				if (ac.getScope() == TAbstractAction.RECORD_SCOPE) {
					// if action must be enabled, check for autorization
					// TODO: complete impl
					// ac.setEnabled(ena ? Session.isAutorizedForAction(ac) : false);
					ac.setEnabled(ena);
				}
			}
		}
	}
}
