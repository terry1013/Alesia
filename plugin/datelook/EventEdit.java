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
package plugin.datelook;

import gui.*;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.Date;

import javax.swing.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * crear/modifar evento en calendario
 * 
 * 
 */
public class EventEdit extends AbstractDataInput {

	private JCheckBox jcballday;
	private GregorianCalendar calendar;
	private JComboBox jcbfreq;
	private JCheckBox[] jcbdays;
	private JPanel jpdays;
	private Event event;
	private boolean newe;
	private JLabel jl;

	/**
	 * nueva instancia
	 * 
	 * 
	 */
	public EventEdit(Event e, boolean ne) {
		super("event.title01");
		this.calendar = new GregorianCalendar();
		this.event = e;
		this.newe = ne;

		addInputComponent("event.sumary", TUIUtils.getJTextField("ttevent.sumary", event.summary, 30), true, true);

		boolean alld = false;
		this.jcballday = TUIUtils.getJCheckBox("event.alldat", alld);
		addInputComponent("event.alldat", jcballday, false, true);

		Date ds = new Date(event.get_begin_UTC_ms());
		addInputComponent("event.date",
				TUIUtils.getDateTimeSpinner("ttevent.date", DateTimeSpinner.DATE, ds, "dd/MM/yyyy"), true, true);
		addInputComponent("event.time",
				TUIUtils.getDateTimeSpinner("ttevent.time", DateTimeSpinner.TIME, ds, "hh:mm"), true, true);

		TEntry[] t = TStringUtils.getTEntryGroup("event.freq_");
		this.jcbfreq = TUIUtils.getJComboBox("ttevent.freq", t, event.getPeriod());
		jcbfreq.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				preValidate(e.getSource());
				
			}
		});
		addInputComponent("event.freq", jcbfreq, false, true);

		addInputComponent("event.freq.count",
				TUIUtils.getJFormattedTextField("ttevent.freq.count", event.get_period_multiplier(), 2), false, true);
		addInputComponent("event.freq.interval",
				TUIUtils.getJFormattedTextField("ttevent.freq.interval", event.get_number_of_periods(), 2), false,
				true);

		t = TStringUtils.getTEntryGroup("event.status_");
		JComboBox jc = TUIUtils.getJComboBox("ttevent.status", t, event.status);
		addInputComponent("event.status", jc, false, true);

		JComboBox jcol = TUIUtils.getColorJComboBox("ttevent.color", event.rendererColor);
		addInputComponent("event.color", jcol, false, true);

		addInputComponent("event.alarm",
				TUIUtils.getJCheckBox("event.alarm", event.get_alarm_active()), false, true);

		ServiceRequest sr = new ServiceRequest(ServiceRequest.DB_QUERY, "t_tasks", null);
		RecordSelector rs = new RecordSelector(sr, "t_taname", "t_tadescription", event.task);
		rs.insertItemAt(TStringUtils.getTEntry("evet.task.none"), 0);
		addInputComponent("event.task", rs, false, true);

		addInputComponent("event.comment", TUIUtils.getJTextArea("ttevent.comment", event.comment, 80, 4), false,
				true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, pref, 7dlu, left:pref, 3dlu, pref", // x
				"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, p, 3dlu, p, p"); // y
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(getLabelFor("event.sumary"), cc.xy(1, 1));
		build.add(getInputComponent("event.sumary"), cc.xyw(3, 1, 5));
		build.add(getLabelFor("event.date"), cc.xy(1, 3));
		build.add(getInputComponent("event.date"), cc.xy(3, 3));
		build.add(getLabelFor("event.time"), cc.xy(5, 3));
		build.add(getInputComponent("event.time"), cc.xy(7, 3));

		// build.add(getLabelFor("event.alldat"), cc.xy(1, 5));
		build.add(getInputComponent("event.alldat"), cc.xy(1, 5));
		build.add(getLabelFor("event.freq"), cc.xy(5, 5));
		build.add(getInputComponent("event.freq"), cc.xy(7, 5));

		/*
		 * TEntry[] dya = ConstantUtilities.getTEntryGroup("event.byday_"); this.jcbdays = new JCheckBox[dya.length];
		 * this.jpdays = new JPanel(); jpdays.setLayout(new BoxLayout(jpdays, BoxLayout.X_AXIS));
		 * 
		 * // frecuencia por dias. si hay elementos, cambio frecuencia a WEEKDAY List<ByDay> byd = (recu == null) ? new
		 * ArrayList() : recu.getByDay(); if (byd.size() > 0) { TEntry te = ConstantUtilities.getTEntryByKey("WEEKDAY");
		 * jcbfreq.setSelectedItem(te); } jcbfreq.addActionListener(this);
		 * 
		 * jpdays.setBorder(new TitledBorder(ConstantUtilities.getBundleString("event.byday.title"))); for (int i = 0; i
		 * < 7; i++) { jcbdays[i] = new JCheckBox((String) dya[i].getValue()); jcbdays[i].addActionListener(this);
		 * String abr = (String) dya[i].getKey(); boolean b = false; for (int j = 0; j < byd.size(); j++) { DayOfWeek
		 * dow = byd.get(j).getDay(); b = (dow.getAbbr().equals(abr)) ? true : b; } jcbdays[i].setSelected(b);
		 * jcbdays[i].setName(abr); jpdays.add(jcbdays[i]); } build.add(jpdays, cc.xyw(1, 7, 7));
		 */

		this.jl = getLabelFor("event.freq.count");
		build.add(jl, cc.xy(1, 9));
		build.add(getInputComponent("event.freq.count"), cc.xy(3, 9));
		build.add(getLabelFor("event.freq.interval"), cc.xy(5, 9));
		build.add(getInputComponent("event.freq.interval"), cc.xy(7, 9));

		build.add(getLabelFor("event.status"), cc.xy(1, 11));
		build.add(getInputComponent("event.status"), cc.xy(3, 11));
		build.add(getLabelFor("event.color"), cc.xy(5, 11));
		build.add(getInputComponent("event.color"), cc.xy(7, 11));

		build.add(getInputComponent("event.alarm"), cc.xy(1, 13));

		build.add(getLabelFor("event.task"), cc.xy(1, 15));
		build.add(getInputComponent("event.task"), cc.xyw(1, 16, 7));

		build.add(getLabelFor("event.comment"), cc.xy(1, 18));
		build.add(getInputComponent("event.comment"), cc.xyw(1, 19, 7));

		setDefaultActionBar();
		add(build.getPanel());
		preValidate(null);
	}

	@Override
	public void preValidate(Object src) {
		super.preValidate(src);
		if (!isShowingError()) {
			TEntry t = (TEntry) jcbfreq.getSelectedItem();
			boolean onc = t.getKey().equals("ONCE");
			setEnabledInputComponent("event.freq.count", !onc);
			setEnabledInputComponent("event.freq.interval", !onc);
			
			// se debe seleccionar al menos 1 dia de la semana
			boolean wed = t.getKey().equals("WEEKDAY");
			// UIUtilities.setEnabled(jpdays, wed);
			if (wed) {
				boolean sel = false;
				for (JCheckBox jcb : jcbdays) {
					sel = (sel == true) ? sel : jcb.isSelected();
				}
				if (!sel) {
					showAplicationExceptionMsg("event.msg01");
				}
			}
		}
		setEnableDefaultButton(!isShowingError());
	}

	@Override
	public void validateFields() {

	}

	public Event getEvent() {
		Hashtable flds = getFields();
		// System.out.println("getevent: " + System.currentTimeMillis());

		event.summary = (String) flds.get("event.sumary");
		event.comment = (String) flds.get("event.comment");
		calendar.setTime((Date) flds.get("event.date"));
		Time t = (Time) flds.get("event.time");
		calendar.set(GregorianCalendar.HOUR_OF_DAY, Integer.valueOf(t.toString().substring(0, 2)));
		calendar.set(GregorianCalendar.MINUTE, Integer.valueOf(t.toString().substring(3, 5))); // hh:mm:ss
		// allday event controled by jcballday
		long dts = calendar.getTimeInMillis();
		event.alowAllDay = jcballday.isSelected();
		event.set_begin_UTC_ms(dts);
		event.task = (String) flds.get("event.task");
		boolean alarm = (Boolean) flds.get("event.alarm");

		// for task, end is one our duration & alarm setted
		if (!event.task.equals("NONE")) {
			calendar.add(GregorianCalendar.HOUR_OF_DAY, 1);
			event.set_end_UTC_ms(calendar.getTimeInMillis());
			alarm = true;
		}

		event.set_alarm_active(alarm);
		event.set_alarm_UTC_ms(dts);
		event.set_alarm_counter_to_next_after_now();

		event.status = (String) flds.get("event.status");
		event.rendererColor = (Color) flds.get("event.color");
		String freq = (String) flds.get("event.freq");
		event.setPeriod(freq);
		event.set_period_multiplier((Integer) flds.get("event.freq.count"));
		event.set_number_of_periods((Integer) flds.get("event.freq.interval"));
		if (!freq.equals("ONCE")) {
			String rec = "";

			if (freq.equals("WEEKDAY")) {
				rec = "FREQ=WEEKLY;BYDAY=";// MO,TU,WE,TH,FR
				for (JCheckBox jcb : jcbdays) {
					rec += jcb.isSelected() ? jcb.getName() + "," : "";
				}
				rec = rec.substring(0, rec.length() - 1);
			}
			rec = freq.equals("MONTHLY") ? "FREQ=MONTHLY" : rec;
			rec = freq.equals("YEARLY") ? "FREQ=YEARLY" : rec;

			int inte = (Integer) flds.get("event.freq.interval");
			rec += (inte > 1) ? ";INTERVAL=" + inte : "";
			int cnt = (Integer) flds.get("event.freq.count");
			rec += (cnt > 1) ? ";COUNT=" + cnt : "";
		}
		return event;
	}
}
