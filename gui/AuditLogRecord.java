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

import java.text.*;
import java.util.*;

import javax.swing.*;



import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * Presenta registro de auditoria con la opcion de restaurar elemento
 * 
 */
public class AuditLogRecord extends AbstractRecordDataInput {

	private static DateFormat datiF = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
			DateFormat.MEDIUM);

	/**
	 * nueva instancia
	 * 
	 * @param rcd - registro
	 */
	public AuditLogRecord(Record rcd) {
		super("title_displayarecord", rcd);
		setVisibleMessagePanel(false);
		// informacion basica
		addInputComponent("t_altable_name",
				new JLabel((String) rcd.getFieldValue("t_altable_name")), false, true);
		addInputComponent("t_alaudit_track",
				new JLabel((rcd.getFieldValue("t_alaudit_track")).toString()), false, true);
		String dt = datiF.format((Date) rcd.getFieldValue("t_aldate_time"));
		addInputComponent("t_aldate_time", new JLabel(dt), false, true);

		Record rdta = DataBaseUtilities.getFromSerializedForm((String) rcd
				.getFieldValue("t_alrecord"));

		Vector vc = new Vector(2);
		vc.add("Field");
		vc.add("Value");
		Vector vr = new Vector();
		for (int i = 0; i < rdta.getFieldCount(); i++) {
			String fv = rdta.getFieldValue(i).toString();
			Vector rd = new Vector(2);
			rd.add(TStringUtils.getString(rdta.getFieldName(i)));
			rd.add(fv);
			vr.add(rd);
			// determina si es direccion
			/*
			 * if (fv.startsWith("Address")) { Record radd =
			 * DataBaseUtilities.getFromSerializedForm(fv); fv =
			 * ResourceUtilities.formatAddress(radd); }
			 */
			
		}
		JTable jt = new JTable(vr, vc);
		TUIUtils.fixTableColumn(jt, new int[] { 170, 300 });
		jt.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		jt.setEnabled(false);

		FormLayout lay = new FormLayout("left:pref, 3dlu, pref, 7dlu, left:pref, 3dlu, 200dlu", // columns
				"p, 3dlu, p, 3dlu, p, 3dlu, p, 150dlu"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(getLabelFor("t_altable_name"), cc.xy(1, 1));
		build.add(getInputComponent("t_altable_name"), cc.xy(3, 1));
		build.add(getLabelFor("t_alaudit_track"), cc.xy(1, 3));
		build.add(getInputComponent("t_alaudit_track"), cc.xy(3, 3));
		build.add(getLabelFor("t_aldate_time"), cc.xy(1, 5));
		build.add(getInputComponent("t_aldate_time"), cc.xyw(3, 5, 5));
		build.add(TUIUtils.getJLabel("t_alrecord", false, true), cc.xy(1, 7));
		build.add(new JScrollPane(jt), cc.xyw(1, 8, 7));

		setDefaultActionBar();
		add(build.getPanel());
		preValidate(null);
	}
}
