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
import java.text.*;
import java.util.*;

import javax.swing.*;

import core.*;

/**
 * clase para entrada de fechas o horas
 * 
 */
public class DateTimeSpinner extends JSpinner implements FocusListener {

	private static GregorianCalendar calendar = new GregorianCalendar();
	public static int TIME = 0;
	public static int DATE = 1;
	public SimpleDateFormat dateFormat;

	private int style;
	private Color backcolor, foreColor;
	private JFormattedTextField textField;

	/**
	 * nueva instancia
	 * 
	 * @param d - fecha
	 * @param stl - DateTimeSpinner.HOUR o DateTimeSpinner.DATE
	 * @param fmt - formato. ej: dd/MM/yyy para fechas o hh:mm:ss para horas (dependiendo del parametro slt
	 */
	public DateTimeSpinner(Date d, int stl, String fmt) {
		this.style = stl;
		calendar.setTime(new Date());
		calendar.add(GregorianCalendar.YEAR, 100);
		this.dateFormat = new SimpleDateFormat(fmt);
		int in = style == TIME ? GregorianCalendar.SECOND : GregorianCalendar.DAY_OF_YEAR;
		setModel(new SpinnerDateModel(d, null, null, in));
		JSpinner.DateEditor jsde = new JSpinner.DateEditor(this, fmt);
		this.textField = jsde.getTextField();
		this.backcolor = textField.getBackground();
		this.foreColor = textField.getForeground();
		textField.addFocusListener(this);
		setEditor(jsde);
		focusGained(null);
	}

	/**
	 * retorna el componente de texto de entrada asociado a esta clase
	 * 
	 * @return JFormattedTextField
	 */
	public JFormattedTextField getTextField() {
		return textField;
	}

	/**
	 * retorna fecha contenida dentro de este componente. dependiendo del estilo, retornara una instancia de
	 * <code>java.sql.Date</code> o una de <code>java.sql.Time</code>
	 * 
	 * @return fecha o hora
	 */
	public Date getDate() {
		Date d = (Date) getValue();
		calendar.setTime(d);
		d = null;
		if (style == DATE) {
			calendar.set(GregorianCalendar.HOUR, 0);
			calendar.set(GregorianCalendar.MINUTE, 0);
			calendar.set(GregorianCalendar.SECOND, 0);
			d = new java.sql.Date(calendar.getTimeInMillis());
		} else {
			calendar.set(GregorianCalendar.DAY_OF_YEAR, 0);
			calendar.set(GregorianCalendar.MONTH, 0);
			calendar.set(GregorianCalendar.YEAR, 0);
			d = new java.sql.Time(calendar.getTimeInMillis());
		}
		return d;
	}

	/**
	 * establece color de fondo y frente para el componente de edicion de fecha/hora al color pasado como argumento
	 * 
	 * @param ec - color a establecer
	 */
	public void setErrorColor(Color ec) {
		textField.setForeground(ec);
		textField.setBackground(ec);
	}

	/**
	 * restablece los colores por defecto para el componente de edicion
	 * 
	 */
	public void resetColor() {
		textField.setForeground(foreColor);
		textField.setBackground(backcolor);
	}

	@Override
	public void focusGained(FocusEvent e) {
		resetColor();
		if (textField.getValue().equals(TStringUtils.ZERODATE)) {
			textField.setText("");
			textField.setForeground(foreColor);
		}
	}
	@Override
	public void focusLost(FocusEvent e) {
		resetColor();
		String t = textField.getText();
		if (t.trim().equals("")) {
			textField.setValue(TStringUtils.ZERODATE);
			textField.setForeground(backcolor);
		}
	}
}
