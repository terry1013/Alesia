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
/*			
 * Copyright (c) QQ - All right reserved

 */
package gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import javax.swing.event.*;



import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

public class RecordSelectorList extends JPanel implements ActionListener, ListSelectionListener {

	private ServiceRequest request;
	private String keyFN, valueFN;
	private Object selectedO;
	private JList listBasicFull, listSelecFull;
	private JButton btnAgrU, btnAgrT, btnQutU, btnQutT;
	

	/** nueva instancia
	 * 
	 * @param sr - servicio para datos 
	 * @param kfn - campo clave
	 * @param vfn - campo del registro que sera usado para describir a cada elemento
	 * de la lista
	 * @param so - objeto seleccionado
	 */
	public RecordSelectorList(ServiceRequest sr, String kfn, String vfn, Object so) {
		super();
		this.request = sr;
		this.keyFN = kfn;
		this.valueFN = vfn;
		this.selectedO = so;
		reLoadRecords();
		createRecords();
	}

	public RecordSelectorList(ServiceRequest sr, String kfn, String vfn, String idf) {
		super();
		this.request = sr;
		this.keyFN = kfn;
		this.valueFN = vfn;
		this.selectedO = null;
		reLoadRecords();
		createRecords();
	}

	/** actualiza el contenido de este comopnente invocando nuevamente la solicitud de servicio
	 * 
	 *
	 */
	public void reLoadRecords() {
		Object o = ServiceConnection.sendTransaction(request).getData();
		if (o instanceof Vector) {
			Vector v = (Vector) o;
			v.remove(0);
		
			v.remove(0);
			TEntry[] lst = new TEntry[v.size()];

			// Lista de Elementos originales o Elementos no Selecionados
			DefaultListModel listBasic = new DefaultListModel();

			// Lista de Elementos Selecionados 
			DefaultListModel listSelec = new DefaultListModel();

			for (int k = 0; k < v.size(); k++) {
				Record r = (Record) v.elementAt(k);
				lst[k] = new TEntry(r.getFieldValue(keyFN), r.getFieldValue(valueFN));
				Object val = r.getFieldValue(keyFN);

				//Adiccion de elementos a la lista de Elemento originales
				listBasic.addElement(lst[k]);

				// seleccion por campo predeterminado
				/*if (isDftFieldName != null) {
				 boolean dft = ((Boolean) r.getFieldValue(isDftFieldName)).booleanValue();
				 if (dft) {
				 idx = k;
				 }
				 }*/
				
				// seleccion para objeto
				if (selectedO != null ){
					String[] cmd = ((String) selectedO).split(";");
					for (int i = 0; i < cmd.length; i++) {
						if (cmd[i].equals(val)) {
							//idx = k;
							// Adiccion de elementos a la lista de elemento seleccionado
							listSelec.addElement(lst[k]);
							listBasic.removeElement(lst[k]);
						}
					}
				}
				
				
			}

			listBasicFull = new JList();
			listBasicFull.setModel(listBasic);
			listBasicFull.add(new Scrollbar());

			listSelecFull = new JList();
			listSelecFull.setModel(listSelec);

		} else {
			throw new ClassCastException("Instance of java.util.Vector espected. Returned class: "
				+ o.getClass().getName());
		}
	}

	public void createRecords() {
		FormLayout lay = new FormLayout("100dlu, 3dlu, pref, 3dlu, 100dlu",// columns
			"10dlu, p, 3dlu, p, 3dlu, p, 3dlu, p ,10dlu");// rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		JScrollPane jcr = new JScrollPane(listBasicFull);
		listBasicFull.addListSelectionListener(this);
		JScrollPane jcr1 = new JScrollPane(listSelecFull);
		listSelecFull.addListSelectionListener(this);

		btnAgrU = new JButton();
		btnAgrU.setText(">");
		btnAgrU.addActionListener(this);

		btnAgrT = new JButton();
		btnAgrT.setText(">>");
		btnAgrT.addActionListener(this);

		btnQutU = new JButton();
		btnQutU.setText("<");
		btnQutU.addActionListener(this);

		btnQutT = new JButton();
		btnQutT.setText("<<");
		btnQutT.addActionListener(this);

		preValidate(null);
		build.add(jcr, cc.xywh(1, 1, 1, 9));

		build.add(btnAgrU, cc.xy(3, 2));
		build.add(btnAgrT, cc.xy(3, 4));
		build.add(btnQutU, cc.xy(3, 6));
		build.add(btnQutT, cc.xy(3, 8));

		build.add(jcr1, cc.xywh(5, 1, 1, 9));
		add(build.getPanel());

		this.setVisible(true);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		
		preValidate(e.getSource());

	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e) {
		preValidate(e.getSource());

	}

	/** Se encarga de la Validacion General del Panel
	 * 
	 * @param src - Objecto Instanciado
	 */
	private void preValidate(Object src) {
		if (src != null) {
			if (src instanceof JList) {
				validateJlist(src);
			}

			if (src instanceof JButton) {
				executeAction(src);
			}
		} else {
			validateJlist(src);
		}
	}

	/** Se encarga de habilitar los botones y colocar el foco en  cada una de las listas
	 * 
	 * @param sr - Instacia del Objecto Seleccionado
	 */
	public void validateJlist(Object sr) {
		boolean enableAT = true;
		boolean enableQT = true;
		boolean enableAU = true;
		boolean enableQU = true;

		//		Habilita y desabilita los botones de agregar y quitar
		if (listBasicFull.isSelectionEmpty()) {
			enableAU = false;
		}
		if (listSelecFull.isSelectionEmpty() || listSelecFull.getComponentCount() == 0) {
			enableQU = false;
		}
		if (listBasicFull.getModel().getSize() == 0) {
			enableAT = false;
		}
		if (listSelecFull.getModel().getSize() == 0) {
			enableQT = false;
		}
		btnAgrU.setEnabled(enableAU);
		btnQutU.setEnabled(enableQU);
		btnAgrT.setEnabled(enableAT);
		btnQutT.setEnabled(enableQT);

		if (sr != null) {
			//			 Quita y coloca foco sobre un elemento de una de las dos lista
			if (sr.equals(listBasicFull)) {
				listSelecFull.clearSelection();
			}
			if (sr.equals(listSelecFull)) {
				listBasicFull.clearSelection();
			}
		}

	}

	/** Accion de los Botones agregar y quitar elementos de las listas
	 * 
	 * @param sr - Instacia del Objecto Seleccionado 
	 */
	public void executeAction(Object sr) {
		DefaultListModel listBasicO = (DefaultListModel) listBasicFull.getModel();
		DefaultListModel listSelecO = (DefaultListModel) listSelecFull.getModel();
		DefaultListModel listBasicN = new DefaultListModel();
		DefaultListModel listSelecN = new DefaultListModel();
		//		Agregar todos los elementos para la lista de seleccionados
		if (sr.equals(btnAgrT)) {
			for (int i = 0; i < listBasicO.getSize(); i++) {
				TEntry lt = (TEntry) listBasicO.getElementAt(i);
				listSelecO.addElement(lt);
			}

			listBasicFull.setModel(listBasicN);
			listSelecFull.setModel(listSelecO);
		}
		//		Quitar todos los elementos para la lista de seleccionados
		if (sr.equals(btnQutT)) {
			for (int i = 0; i < listSelecO.getSize(); i++) {
				TEntry lt = (TEntry) listSelecO.getElementAt(i);
				listBasicO.addElement(lt);
			}

			listBasicFull.setModel(listBasicO);
			listSelecFull.setModel(listSelecN);
		}

		//Agregar un elemento para la lista de seleccionados
		if (sr.equals(btnAgrU)) {
			int vec = listBasicFull.getSelectedIndices().length;
			int vect[] = new int[vec];
			vect = listBasicFull.getSelectedIndices();
			
			for (int i = 0; i < vec; i++) {
				int num = vect[i];
				//LTEntry lt=(LTEntry) listBasicFull.getSelectedValue();
				TEntry lt = (TEntry) listBasicO.getElementAt(num);
				listSelecO.addElement(lt);
				listBasicO.removeElement(lt);
				listBasicO.add(num, null);
			}

			for (int i = 0; i < listBasicO.getSize(); i++) {
				if (listBasicO.getElementAt(i) != null) {
					TEntry lt = (TEntry) listBasicO.getElementAt(i);
					listBasicN.addElement(lt);
				}
			}
			listBasicFull.setModel(listBasicN);
			listSelecFull.setModel(listSelecO);
		}
		
//		Quitar un elemento para la lista de seleccionados
		if (sr.equals(btnQutU)) {
			int vec = listSelecFull.getSelectedIndices().length;
			int vect[] = new int[vec];
			vect = listSelecFull.getSelectedIndices();
			
			for (int i = 0; i < vec; i++) {
				int num = vect[i];
				//LTEntry lt=(LTEntry) listBasicFull.getSelectedValue();
				TEntry lt = (TEntry) listSelecO.getElementAt(num);
				listBasicO.addElement(lt);
				listSelecO.removeElement(lt);
				listSelecO.add(num, null);
			}

			for (int i = 0; i < listSelecO.getSize(); i++) {
				if (listSelecO.getElementAt(i) != null) {
					TEntry lt = (TEntry) listSelecO.getElementAt(i);
					listSelecN.addElement(lt);
				}
			}
			listSelecFull.setModel(listSelecN);
			listBasicFull.setModel(listBasicO);
		}
		
		
		preValidate(null);
	}
	
	/** retorna las claves de todos los elementos seleccionados separados por ;
	 * 
	 * @return elementos seleccionados
	 */
	public String getSelectedElement() {
		String valor = "*free";
		DefaultListModel listSelecO = (DefaultListModel) listSelecFull.getModel();
		for (int k = 0; k < listSelecO.size(); k++) {
			TEntry lt = (TEntry)  listSelecO.elementAt(k);
			if(!lt.equals("*free")){
				valor+=";"+lt.getKey().toString();
			}
		}
		return valor;
	}
}
