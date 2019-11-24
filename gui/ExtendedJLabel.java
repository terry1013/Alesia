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
import java.text.*;
import java.util.*;

import javax.swing.*;

import core.*;

/**
 * este componente extiende las funciones de <code>JLabel</code> para presentar el valor de distintos objetos dando
 * formato segun su clase. Ademas:
 * 
 * <ul>
 * <li>soporte distintos colores (si el valor es instancia de <code>Number</code>)
 * <li>soporte para cambio de patrones estandar de formato de datos.
 * <li>alineacion de valor (Derecha para nro e Izquierda para letras)
 * <li>Si o No para Boolean
 * </ul>
 * 
 */
public class ExtendedJLabel extends JLabel {

	private DateFormat dateF, datiF;
	private DecimalFormat decimalF;
	// private DecimalFormat integerF;
	private Object value;
	private Color disable_forecolor, enable_forecolor;

	/**
	 * nueva instancia
	 * 
	 * 
	 */
	public ExtendedJLabel() {
		this.dateF = DateFormat.getDateInstance(DateFormat.MEDIUM);
		this.datiF = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
		this.decimalF = new DecimalFormat("#,###,###,##0.00;-#,###,###,##0.00");
		// this.integerF = new DecimalFormat();
		this.disable_forecolor = UIManager.getColor("TextField.inactiveForeground");
		this.enable_forecolor = UIManager.getColor("TextField.foreground");
	}
	/**
	 * nueva instancia
	 * 
	 * @param v - valor
	 */
	public ExtendedJLabel(Object v) {
		this();
		setValue(v);
	}

	/**
	 * set the output format.
	 * 
	 * @param cls - Class to witch format is set
	 * @param patt - pattern
	 */
	public void setFormat(Class cls, String patt) {
		if (cls.equals(Integer.class) || cls.equals(Long.class)) {
			// integerF = new DecimalFormat(patt);
		}
		if (cls.equals(Double.class) || cls.equals(Float.class)) {
			decimalF = new DecimalFormat(patt);
		}
		if (cls.equals(Date.class) || cls.equals(java.sql.Date.class)) {
			dateF = new SimpleDateFormat(patt);
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		// si comienza con < asume texto en html
		if (getValue().toString().startsWith("<")) {
			setForeground((enabled) ? enable_forecolor : disable_forecolor);
		}
		// intersepta metodo para dar soporte a formatos html
	}

	/**
	 * establece el valor para este componente
	 * 
	 * @param v - valor
	 */
	public void setValue(Object v) {
		this.value = v;
		String f_value = value.toString();
		setHorizontalAlignment(LEFT);

		// si o no
		if (value instanceof Boolean) {
			boolean va = ((Boolean) value).booleanValue();
			f_value = (va) ? TStringUtils.getString("extendJlabel.yes") : TStringUtils
					.getString("extendJlabel.no");
		}

		// numeros
		if (value instanceof Number) {
			setHorizontalAlignment(JLabel.RIGHT);
			if (value instanceof Integer || value instanceof Long) {
				// f_value = integerF.format(value);
			}
			if (value instanceof Double || value instanceof Float) {
				f_value = decimalF.format(value);
			}
		}
		// fechas
		if (value instanceof Date) {
			f_value = dateF.format(value);
		}

		// fecha y hora
		if (value instanceof java.sql.Timestamp) {
			f_value = datiF.format(value);
		}
		setText(f_value);
	}

	/**
	 * retorna el valor interno es este componente esta presentando
	 * 
	 * @return - valor
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * validacion rapida que verifica si el objeto datos dentro de este componente tiene algun valor
	 * 
	 * @return - true si contiene algun valor, false de lo contrario
	 */
	public boolean isValueSet() {
		boolean set = false;
		if (value instanceof Number) {
			set = ((Number) value).doubleValue() != 0;
		} else {
			set = value.toString().trim().equals("");
		}
		return set;
	}
}
