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
package gui.beej;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;


import core.*;
import core.datasource.*;

/**
 * panel para seleccion de campos operadores y valores para dichos operadores.
 * 
 */
public class TextExpressionPanel extends ExpressionPanel implements ActionListener {

	protected JComboBox fields, operator;
	private String fileName;
	private String NodbFileds;
	private Record rcdModel;
	private ValuesDataPanel dataPanel;
	private String fieldConstList;

	/**
	 * nueva instancia
	 * 
	 * @param fn - nombre de archivo de base de datos
	 * @param nfl - lista de campos que no se desean dentro de la seleccion
	 * @param fc - lista de pares campo;constante;campo;constante... que indica que grupo de
	 *        constantes pertenece a cada campo
	 */
	public TextExpressionPanel(String fn, String nfl, String fc) {
		super();
		this.fileName = fn;
		this.NodbFileds = nfl;
		this.fieldConstList = fc;

		// campos de base de datos
		Vector<TEntry> jcbl = new Vector<TEntry>();
		DBAccess dba = ConnectionManager.getAccessTo(fileName);
		this.rcdModel = dba.getModel();
		for (int i = 0; i < rcdModel.getFieldCount(); i++) {
			// verifica si no esta en la lista de no deseados
			if (!NodbFileds.contains(rcdModel.getFieldName(i))) {
				TEntry t = new TEntry(rcdModel.getFieldName(i),
						TStringUtils.getString(rcdModel.getFieldName(i)));
				// String c = ConstantUtilities.getBundleString(r.getFieldName(i));
				// JLabel jl = new JLabel(c);
				// jl.setIcon(ResourceUtilities.getIcon(r.getFieldValue(i).getClass().getSimpleName()));
				jcbl.add(t);
			}
		}
		this.fields = new JComboBox(jcbl);
		fields.addActionListener(this);

		// operador
		TEntry[] val = TStringUtils.getTEntryGroup("b.oper");
		this.operator = TUIUtils.getJComboBox("", val, val[0]);
		operator.addActionListener(this);
		// valor
		this.dataPanel = new ValuesDataPanel(fieldConstList, this);

		add(fields);
		add(operator);
		add(dataPanel);

		fields.setSelectedIndex(0);
		// setBorder(new LineBorder(Color.GRAY));
	}

	/**
	 * retorna una nueva instancia de este objeto
	 * 
	 * @return nueva instancia
	 */
	public TextExpressionPanel getClone() {
		return new TextExpressionPanel(fileName, NodbFileds, fieldConstList);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.splike.beej.ExpressionPanel#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == fields || e.getSource() == operator) {
			// componente de entrada segun tipo de variable
			String field = ((TEntry) fields.getSelectedItem()).getKey().toString();
			String lay = rcdModel.getFieldValue(field).getClass().getSimpleName();
			// si el objeto valor es String y definido como constante entonces debe ser campo
			// asociado a lista de
			// constantes y paso el nombre del campo
//			if (lay.equals("String") && rcdModel.isConstantID(field)) {
//				lay = field;
//			}
			dataPanel.showLayoutFor(lay);

			// componente de entrada segun operador
			String sv = ((TEntry) operator.getSelectedItem()).getKey().toString();
			if (sv.equals("BETWEEN") || sv.equals("LIKE")) {
				dataPanel.showLayoutFor(sv);
			}
		}
		// proveniente de ValuesDataPanel
		if (e.getSource() == dataPanel) {

		}

		// notificacion
		ActionEvent ae = new ActionEvent(this, e.getID(), e.getActionCommand(), e.getWhen(),
				e.getModifiers());
		for (Enumeration en = listeners.elements(); en.hasMoreElements();)
			((ActionListener) en.nextElement()).actionPerformed(ae);
	}

	public String toString() {
		String field = (String) ((TEntry) fields.getSelectedItem()).getKey();
		String opera = (String) ((TEntry) operator.getSelectedItem()).getKey();
		String valu = dataPanel.getValue();
		return field + " " + opera + " " + valu;
	}
}
