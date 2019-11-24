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

import java.awt.event.*;
import java.util.*;

import javax.swing.*;


import action.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * Simple entry panel for edit basic task parameters.
 * 
 * @see SaveAsTaskAction
 * 
 */
public class TaskRecord extends AbstractRecordDataInput implements ActionListener {

	/**
	 * nueva instancia
	 * 
	 * @param rcd - registro
	 * @param newr - true si es nuevo registro
	 */
	public TaskRecord(Record rcd, boolean newr) {
		super("title.task", rcd);
		addInputComponent("t_taname", TUIUtils.getJTextField(rcd, "t_taname"), true, newr);
		addInputComponent("t_tadescription", TUIUtils.getJTextArea(rcd, "t_tadescription"), true, true);
		addInputComponent("t_tastatus", TUIUtils.getJComboBox("rcd_sts", rcd, "t_tastatus"), true, true);
	//	addInputComponent("t_taid", TUIUtils.getExtendedJLabel(rcd, "t_taid", false), true, false);

		FormLayout lay = new FormLayout("left:pref, 3dlu, pref, 7dlu, left:pref, 3dlu, 110dlu", // columns
				"p, 3dlu, p, p, 3dlu, p, 3dlu, p, 150dlu");// rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);
		
		build.add(getLabelFor("t_taname"), cc.xy(1, 1));
		build.add(getInputComponent("t_taname"), cc.xy(3, 1));
		build.add(getLabelFor("t_tadescription"), cc.xy(1, 3));
		build.add(getInputComponent("t_tadescription"), cc.xyw(1, 4, 3));
		build.add(getLabelFor("t_tastatus"), cc.xy(1, 6));
		build.add(getInputComponent("t_tastatus"), cc.xy(3, 6));
//		build.add(getLabelFor("t_taid"), cc.xy(5, 6));
//		build.add(getInputComponent("t_taid"), cc.xy(7, 6));
		build.add(TUIUtils.getJLabel("Parametros de la tares", false, true), cc.xyw(1, 8, 7));
		build.add(createTable(rcd), cc.xyw(1, 9, 7));

		setDefaultActionBar();
		add(build.getPanel());
		preValidate(null);
	}

	private JScrollPane createTable(Record rcd) {

		Hashtable ht = (Hashtable) TPreferences.getObjectFromByteArray((byte[]) rcd.getFieldValue("t_tacode"));
		Vector vc = new Vector(2);
		vc.add("Field");
		vc.add("Value");
		Vector vr = new Vector();
		Vector<String> vk = new Vector(ht.keySet());
		for (String fk : vk) {
			Vector rd = new Vector(2);
			rd.add(TStringUtils.getString(fk));
			rd.add(ht.get(fk));
			vr.add(rd);
		}
		JTable jt = new JTable(vr, vc);
		TUIUtils.fixTableColumn(jt, new int[]{170, 300});
		jt.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		jt.setEnabled(false);
		return new JScrollPane(jt);
	}
}
