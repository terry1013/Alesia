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

import javax.swing.*;

import core.*;

/**
 * extencion de <code>JList</code> que permite soporte adicional para multiples vistas y salvado de propiedades
 * 
 */
public class TJList extends JList {

	// public static int LIST_VIEW_STYLE = 0;
	// public static int MOSAIC_VIEW_STYLE = 1;
	private TAbstractListModel listModel;
	private String classname;
	private int style;

	/**
	 * nuena instancia
	 * 
	 * @param src - instancia de <code>UIListPanel</code> que crea este objeto
	 * @param lm - <code>TAbstractListModel</code>
	 */
	public TJList(UIListPanel src, TAbstractListModel lm) {
		super(lm);
		this.listModel = lm;
		this.classname = src.getClass().getName();
		// setLayoutOrientation(JList.HORIZONTAL_WRAP);
		// this.columns = (String) src.getClientProperty(PropertyNames.SHOW_COLUMNS);
		// loadStyle();
	}

	public void setStyle(int stl) {
		this.style = stl;
		TPreferences.setPreference(TPreferences.LIST_VIEW_STYLE, classname, "" + style);
	}

	@Override
	public ListModel getModel() {
		return listModel;
	}

	/**
	 * este metodo altera la instancia de <code>TableColumnsModel</code> para ajustar el ancho de la columna. el acncho
	 * es determinado seleccionando el mas largo entre el titulo de la columna o el maximo ancho de su contenido
	 * 
	 */
	private void loadStyle() {
		// String v = TPreferences.getProperty(TPreferences.LIST_VIEW_STYLE, classname);
		// this.style = v == null ? LIST_VIEW_STYLE : Integer.valueOf(v);
	}
}
