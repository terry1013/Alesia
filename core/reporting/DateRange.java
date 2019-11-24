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

import java.awt.*;
import java.util.*;

import javax.swing.*;

import core.*;

/** este componente presenta la seleccion de periodos disponibles para un reporte que necesite
 * filtro por fecha. Este es adicionado como una nueva pesta;a en el dialogo de impresion. 
 * los valores a;adidos a la lista de parametros de impresion son <code>startDate y endDate</code>
 * 
 *
 */
public class DateRange extends AbstractDataInput {

	private static GregorianCalendar calendar = new GregorianCalendar();
	
	/** nueva instancia
	 * 
	 *
	 */
	public DateRange() {
		super(null);
		setVisibleMessagePanel(false);
		add(createPeriodList());
		preValidate(null);
	}

	private JPanel createPeriodList() {

		// checkbox
		JRadioButton jrb = TUIUtils.getJRadioButton(null, "p11", true);
		JRadioButton jrb1 = TUIUtils.getJRadioButton(null, "p15", false);
		ButtonGroup bg = new ButtonGroup();
		bg.add(jrb);
		bg.add(jrb1);

		// lista de periodos
		TEntry[] per = TStringUtils.getTEntryGroup("period");
		JComboBox jcb = TUIUtils.getJComboBox("ttp11", per, "alld");
		jcb.addActionListener(this);
		addInputComponent("period", jcb, true, true);
		Box b = TUIUtils.getInHorizontalBox("p11", getInputComponent("period"), false, true);
		ComponentTitledPane ctp = new ComponentTitledPane(jrb, b);

		// intervalo de fechas
		addInputComponent("startDate", TUIUtils.getJFormattedTextField("ttp13", new Date(), 10),
			true, true);
		addInputComponent("endDate", TUIUtils.getJFormattedTextField("ttp14", new Date(), 10),
			true, true);

		JPanel jp = TUIUtils.getInHorizontalBox(new JComponent[] {
				TUIUtils.getInHorizontalBox("p13", getInputComponent("startDate"), true, true),
				TUIUtils.getInHorizontalBox("p14", getInputComponent("endDate"), true, true) },
			FlowLayout.LEFT);
		ComponentTitledPane ctp1 = new ComponentTitledPane(jrb1, jp);

		JPanel jp1 = new JPanel(new GridLayout(2, 0, 4, 4));
		jp1.add(ctp);
		jp1.add(ctp1);
		return jp1;
	}

	/* (non-Javadoc)
	 * @see client.ui.AbstractDataInput#validateFields()
	 */
	public void validateFields() {

	}
	
	/* (non-Javadoc)
	 * @see client.ui.AbstractDataInput#getFields()
	 */
	public Hashtable getFields() {
		Hashtable ht = super.getFields();
		JComboBox perio = (JComboBox) getInputComponent("period");
		if (perio.isEnabled()) {
			TEntry lte = (TEntry) perio.getSelectedItem();
			appendPeriod(lte.getKey().toString(), ht);
		}
		// adiciona 2 campos en formato estandar de base de datos
		Date d1 = (Date) ht.get("startDate");
		Date d2 = (Date) ht.get("endDate");
		setHHmmss(d1, d2);
		ht.put("startDateInDBForm", TStringUtils.getStringDate(d1, "yyyyMMddHHmmss"));
		ht.put("endDateInDBForm", TStringUtils.getStringDate(d2, "yyyyMMddHHmmss"));
		return ht;
	}

	/**inicializa los campos HHmmss para las fechas pasadas como argumento
	 * 
	 * @param staD - fecha inicial
	 * @param endD - fecha final
	 */
	private static void setHHmmss(Date staD, Date endD) {
		calendar.setTime(staD);
		calendar.set(GregorianCalendar.HOUR_OF_DAY, 0);
		calendar.set(GregorianCalendar.MINUTE, 0);
		calendar.set(GregorianCalendar.SECOND, 1);
		staD.setTime(calendar.getTimeInMillis());

		calendar.setTime(endD);
		calendar.set(GregorianCalendar.HOUR_OF_DAY, 23);
		calendar.set(GregorianCalendar.MINUTE, 59);
		calendar.set(GregorianCalendar.SECOND, 59);
		endD.setTime(calendar.getTimeInMillis());
	}
	
	/** adiciona a la instancia de <code>Hashtable</code> (si es distinto de null) las entradas startDate y endDate
	 * con las fechas calculadas segun el periodo pasado como argumento 
	 * 
	 * @param lap - periodo de tiempo
	 * @param ht - tabla donde se adicionaran las entradas
	 * 
	 */
	private static void appendPeriod(String lap, Hashtable ht) {
		Date staD = new Date();
		Date endD = new Date();
		setHHmmss(staD, endD);

		// si restriccion			
		if (lap.equals("alld")) {
			staD = new Date(0);
			endD = new Date(4102459200000L); //01-01-2100
		}
		// hoy
		if (lap.equals("toda")) {
			// nada
		}

		// esta semana
		if (lap.equals("week")) {
			calendar.setTime(staD);
			calendar.set(GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.MONDAY);
			staD = calendar.getTime();
		}
		// esta quincena
		if (lap.equals("biwe")) {
			calendar.setTime(staD);
			int tod = calendar.get(GregorianCalendar.DATE);
			calendar.set(GregorianCalendar.DATE, (tod < 15) ? 1 : 15);
			staD = calendar.getTime();
		}
		// este mes
		if (lap.equals("mont")) {
			calendar.setTime(staD);
			calendar.set(GregorianCalendar.DATE, 1);
			staD = calendar.getTime();
			calendar.setTime(endD);
			calendar.add(GregorianCalendar.MONTH, 1);
			calendar.add(GregorianCalendar.DAY_OF_MONTH, -1);
			endD = calendar.getTime();
		}
		// la semana pasada
		if (lap.equals("lawe")) {
			calendar.setTime(endD);
			calendar.set(GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.MONDAY);
			calendar.add(GregorianCalendar.WEEK_OF_MONTH, -1);
			endD = calendar.getTime();
			calendar.setTime(staD);
			calendar.set(GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.MONDAY);
			staD = calendar.getTime();
		}
		// la quincena pasada
		if (lap.equals("labi")) {
			calendar.setTime(staD);
			int tod = calendar.get(GregorianCalendar.DATE);
			calendar.set(GregorianCalendar.DATE, 1);		
			if (tod < 15) {
				calendar.add(GregorianCalendar.MONTH, -1);		
				calendar.set(GregorianCalendar.DATE, 15);		
			}
			staD = calendar.getTime();
			calendar.setTime(endD);
			tod = calendar.get(GregorianCalendar.DATE);
			calendar.set(GregorianCalendar.DATE, 15);		
			if (tod < 15) {
				calendar.add(GregorianCalendar.MONTH, -1);		
				calendar.set(GregorianCalendar.DATE, 31);		
			}
			endD = calendar.getTime();
		}

		// el mes pasado
		if (lap.equals("lamo")) {
			calendar.setTime(endD);
			calendar.set(GregorianCalendar.DAY_OF_MONTH, 1);
			calendar.add(GregorianCalendar.DAY_OF_MONTH, -1);
			endD = calendar.getTime();
			calendar.setTime(staD);
			calendar.add(GregorianCalendar.MONTH, -1);
			calendar.set(GregorianCalendar.DAY_OF_MONTH, 1);
			staD = calendar.getTime();
		}
		ht.put("startDate", new java.sql.Date(staD.getTime()));
		ht.put("endDate", new java.sql.Date(endD.getTime()));
	}
}
