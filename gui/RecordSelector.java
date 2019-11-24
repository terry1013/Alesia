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
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import core.*;
import core.datasource.*;

/**
 * Esta clase extiende <code>JComboBox</code> para presentar una lista de elementos. estos elementos representa un grupo
 * de registros retornados por la petision de servicion pasada como argumento al momento de contruir esta clase.
 * 
 */
public class RecordSelector extends JComboBox {

	private ServiceRequest serviceRequest;
	private String keyFN, valueFN;
	private Vector<Record> recordList;
	private ArrayList<TEntry> originalList, filterList, lastFilerList;
	protected Object selectedO;
	private DefaultComboBoxModel boxModel;
	private boolean concat;
	private Dimension orgDim;
	private String filterString;

	public RecordSelector(ServiceRequest sr, String kfn, String vfn, Object so) {
		this(sr, kfn, vfn, so, true);
	}

	public RecordSelector(ServiceRequest sr, String kfn, String vfn, Object so, boolean cf) {
		super();
		this.serviceRequest = sr;
		this.keyFN = kfn;
		this.valueFN = vfn;
		this.selectedO = so;
		this.filterString = "";
		this.concat = cf;
		reLoadRecords();
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();
				if (!e.isActionKey()) {
					filterString += c;
					filterModel(filterString);
				}
				// backspace if apply
				if (c == KeyEvent.VK_BACK_SPACE && filterString.length() > 0) {
					filterString = filterString.substring(0, filterString.length() - 1);
					filterModel(filterString);
				}

				// silent restore model
				if (c == KeyEvent.VK_ESCAPE) {
					filterString = "";
					hidePopup();
					setModel(boxModel);
				}
			}
		});
	}

	public void setConcatField(boolean cf) {
		if (concat != cf) {
			concat = cf;
			reLoadRecords();
		}
	}

	/**
	 * fileter the ComboBox model selecting only the element that contain <code>ftxt</code>. this method show popup
	 * window only if the list contains elements.
	 * 
	 * @param ftxt - String for filtering
	 * 
	 */
	private void filterModel(String ftxt) {
		orgDim = (orgDim == null) ? getSize() : orgDim;
		filterList.clear();
		for (int i = 0; i < originalList.size(); i++) {
			TEntry te = originalList.get(i);
			String tev = (String) te.getValue();
			if (tev.toLowerCase().contains(ftxt.toLowerCase())) {
				filterList.add(te);
			}
		}
		// if fileter list contains elements, update model. else, beep to user keeping the last succesd filter list
		if (filterList.size() > 0) {
			lastFilerList.clear();
			lastFilerList.addAll(filterList);
			setModel(new DefaultComboBoxModel(filterList.toArray()));
			setPreferredSize(orgDim);
			showPopup();
		} else {
			Toolkit.getDefaultToolkit().beep();
		}
	}
	
	public Record getRecordAt(int i) {
		return recordList.elementAt(i);
	}

	/**
	 * Reload internal model from from data provided by the ServiceRequest used to construct this instance. Use this
	 * method where, if a servicereque's parameter has changed due another event and the internal data from this
	 * instance needs to be update.
	 * 
	 */
	public void reLoadRecords() {
		Object o = ServiceConnection.sendTransaction(serviceRequest).getData();
		if (!(o instanceof Vector)) {
			throw new ClassCastException("Instance of java.util.Vector espected. Returned class: "
					+ o.getClass().getName());
		}

		recordList = (Vector) o;
		originalList = new ArrayList<TEntry>(recordList.size());
		filterList = new ArrayList<TEntry>(recordList.size());
		lastFilerList = new ArrayList<TEntry>(recordList.size());

		for (Record r : recordList) {
			String fv = r.getFieldValue(valueFN).toString();
			if (concat) {
				fv = r.getFieldValue(keyFN) + ": " + r.getFieldValue(valueFN);
			}
			originalList.add(new TEntry(r.getFieldValue(keyFN), fv));
		}
		boxModel = new DefaultComboBoxModel(originalList.toArray());
		setModel(boxModel);
		lookForSelecteditem();
	}

	/**
	 * override to detect if <code>anObject</code> is intanceof {@link TEntry}. if not, look in my internal list if
	 * <code>anObject</code> is key of any entry and dispatch to super
	 */
	@Override
	public void setSelectedItem(Object anObject) {
		if (anObject instanceof TEntry) {
			super.setSelectedItem(anObject);
		} else {
			TEntry te = null;
			for (int k = 0; k < boxModel.getSize(); k++) {
				TEntry lt = (TEntry) boxModel.getElementAt(k);
				Object val = lt.getKey();
				if (anObject != null && anObject.equals(val)) {
					te = lt;
				}
			}
			//if not object found, select first
			if (te == null) {
				super.setSelectedIndex(0);
			} else {
				super.setSelectedItem(te);
			}
		}
	}

	/**
	 * localiza el elemento que es igual al valor pasado para seleccionarlo dentro de la lista
	 * 
	 * 
	 */
	private void lookForSelecteditem() {
		int idx = 0;
		for (int k = 0; k < boxModel.getSize(); k++) {
			TEntry lt = (TEntry) boxModel.getElementAt(k);
			Object val = lt.getKey();
			if (selectedO != null && selectedO.equals(val)) {
				idx = k;
			}
		}
		if (getItemCount() > 0) {
			setSelectedIndex(idx);
		}
	}

	@Override
	public void insertItemAt(Object anObject, int index) {
		super.insertItemAt(anObject, index);
		lookForSelecteditem();
	}

	/**
	 * @return the serviceRequest
	 */
	public ServiceRequest getServiceRequest() {
		return serviceRequest;
	}
}
