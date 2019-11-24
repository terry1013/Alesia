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
package core.reporting;

import gui.*;
import gui.beej.*;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import core.datasource.*;

/**
 * panel de entrada para seleccion de camos y valores para filtrado de la base de datos a travez de una sentencia
 * complea de SQL
 * 
 * 
 */
public class SQLSelection extends AbstractDataInput {

	public static final String SQLSSELECTION = "SQLSelection";
	protected BaseEditor editor;
	private String tableName;
	private String orderby;
	private String SQLexpretion;

	/**
	 * nueva instancia
	 * 
	 * @param fn - nombre de archivo de base de datos
	 * @param nfl - lista de campos que no se desean dentro de la seleccion o "" si se desean presentar todos
	 * @param fc - lista de pares campo;constante;campo;constante... que indica que grupo de constantes pertenece a cada
	 *        campo
	 */
	public SQLSelection(ReportParameters dpl, String fn, String nfl, String fc) {
		super(null);
		this.tableName = fn;
		this.SQLexpretion = "";
		Record model = ConnectionManager.getAccessTo(tableName).getModel();
		this.orderby = "ORDER BY " + model.getOrderBy();
		setVisibleMessagePanel(false);
		this.editor = new BaseEditor(new TextExpressionPanel(tableName, nfl, fc));
		editor.addActionListener(this);
		JScrollPane sp = new JScrollPane(editor);
		addWithoutBorder(sp);
		preValidate(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gui.AbstractDataInput#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		super.actionPerformed(ae);
		SQLexpretion = "SELECT * FROM " + tableName + " WHERE " + editor.toString() + " " + orderby;
		System.out.println(SQLexpretion);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see client.ui.AbstractDataInput#validateFields()
	 */
	public void validateFields() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see client.ui.AbstractDataInput#getFields()
	 */
	public Hashtable getFields() {
		Hashtable ht = super.getFields();
		// para casos en donde existe instancia guardada, y se modifica valores y se presione
		// aceptar
		ActionEvent ae = new ActionEvent(this, ActionEvent.ACTION_FIRST, "");
		editor.actionPerformed(ae);

		ht.put(SQLSSELECTION, SQLexpretion);
		return ht;
	}
}
