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
/**
 * Copyright (c) Terry - All right reserved. PROPRIETARY/CONFIDENTIAL.
 * Use is subject to license terms.
 *
 * @author Terry
 *
 */
package gui.beej;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;


import core.*;

/**
 * panel que permite a instancias de <code>FullTextExpressionPane</code> persentar los componentes
 * de entrada indicados segun el tipo de objeto del campo de base de datos y/o el operador
 * 
 */
public class ValuesDataPanel extends JPanel implements DocumentListener, Serializable {

	private JTextField JTFString, JTFBetween1, JTFBetween2, JTFLike;
	private JFormattedTextField JFTFNumber;
	private JComboBox jcbBoolean, jcbConstants;
	private CardLayout layout;
	private String selLayout;
	private Hashtable<String, DefaultComboBoxModel> fielConstList;
	private TextExpressionPanel textExpressionPanel;

	private static String CONSTANTS = "CONSTANTS";

	/**
	 * nueva instancia
	 * 
	 * @param fc - lista de pares campo;constante;campo;constante... que indica que grupo de
	 *        constantes pertenece a cada campo
	 * @param ftep - instancia de panel para notificacion
	 * 
	 */
	public ValuesDataPanel(String fc, TextExpressionPanel ftep) {
		super(new FlowLayout(FlowLayout.CENTER, 0, 0));
		this.layout = new CardLayout(0, 0);
		this.textExpressionPanel = ftep;

		setLayout(layout);

		// String
		this.JTFString = TUIUtils.getJTextField("ttb.value", "", 20);
		JTFString.getDocument().addDocumentListener(this);
		add(JTFString, "String");

		// LIKE
		this.JTFLike = TUIUtils.getJTextField("ttb.value", "", 20);
		JTFLike.getDocument().addDocumentListener(this);
		add(JTFLike, "LIKE");

		// Number
		this.JFTFNumber = TUIUtils.getJFormattedTextField("ttb.value", new Float(0.0), 10);
		JFTFNumber.getDocument().addDocumentListener(this);
		add(JFTFNumber, "Number");

		// lista de campos de base de datos que son lista de constantes
		this.fielConstList = new Hashtable<String, DefaultComboBoxModel>();
		String f_c[] = fc.split(";");
		// fc can be ""
		if (f_c.length > 1) {
			for (int i = 0; i < f_c.length; i = i + 2) {
				fielConstList.put(f_c[i],
						new DefaultComboBoxModel(TStringUtils.getTEntryGroup(f_c[i + 1])));
			}
		}
		this.jcbConstants = new JComboBox();
		jcbConstants.addActionListener(textExpressionPanel);
		jcbConstants.setToolTipText("ttb.value");
		add(jcbConstants, CONSTANTS);

		// BETWEEN
		this.JTFBetween1 = TUIUtils.getJTextField("ttb.value", "", 10);
		JTFBetween1.getDocument().addDocumentListener(this);
		this.JTFBetween2 = TUIUtils.getJTextField("ttb.value", "", 10);
		JTFBetween2.getDocument().addDocumentListener(this);
		JPanel jp = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		jp.add(JTFBetween1);
		jp.add(Box.createHorizontalStrut(5));
		jp.add(new JLabel(TStringUtils.getString("beej1")));
		jp.add(Box.createHorizontalStrut(5));
		jp.add(JTFBetween2);
		add(jp, "BETWEEN");

		// boolean
		TEntry t[] = TStringUtils.getTEntryGroup("b.bol");
		this.jcbBoolean = TUIUtils.getJComboBox("ttb.value", t, t[0]);
		jcbBoolean.addActionListener(textExpressionPanel);
		add(jcbBoolean, "Boolean");
	}

	/**
	 * nuestra los componentes de entrada correctos segun el argumento de entrada. este puede ser el
	 * nombre del campo, la clase de objeto, o un nombre especial
	 * 
	 * @param id - id de componente.
	 */
	public void showLayoutFor(String id) {
		setVisible(false);
		// determina si el argumento es un nombre de campo a la cual esta asociado una lista de
		// constantes
		DefaultComboBoxModel dcbm = fielConstList.get(id);
		if (dcbm != null) {
			jcbConstants.setModel(dcbm);
			id = CONSTANTS;
		}
		// numero
		if (id.equals("Integer") || id.equals("Float")) {
			id = "Number";
		}
		selLayout = id;
		layout.show(this, id);
		setVisible(true);
	}

	/**
	 * retorna el valor del componente que se encuentra activo en este momento
	 * 
	 * @return String representando el valor
	 */
	public String getValue() {
		String rv = "";

		if (selLayout.equals("LIKE")) {
			rv = "'%" + JTFLike.getText() + "%'";
		}

		if (selLayout.equals("String")) {
			rv = "'" + JTFString.getText() + "'";
		}
		if (selLayout.equals("Number")) {
			rv = JFTFNumber.getValue().toString();
		}

		if (selLayout.equals(CONSTANTS)) {
			rv = "'" + ((TEntry) jcbConstants.getSelectedItem()).getKey().toString() + "'";
		}

		if (selLayout.equals("Boolean")) {
			rv = ((TEntry) jcbBoolean.getSelectedItem()).getKey().toString();
		}
		if (selLayout.equals("BETWEEN")) {
			rv = "'" + JTFBetween1.getText() + "' AND '" + JTFBetween2.getText() + "'";
		}

		return rv;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
	 */
	@Override
	public void insertUpdate(DocumentEvent e) {
		textExpressionPanel.actionPerformed(new ActionEvent(this, e.getLength(), ""));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
	 */
	@Override
	public void removeUpdate(DocumentEvent e) {
		textExpressionPanel.actionPerformed(new ActionEvent(this, e.getLength(), ""));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
	 */
	@Override
	public void changedUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub

	}
}
