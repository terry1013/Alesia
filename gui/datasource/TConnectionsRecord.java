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
package gui.datasource;

import gui.*;

import java.awt.*;

import javax.swing.*;

import action.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * record edition compoenet for T_CONNECTIONS file
 * 
 * @author terry
 *
 */
public class TConnectionsRecord extends AbstractRecordDataInput {

	private TPropertyJTable propertyJTable;
	private RecordSelector selector;

	public TConnectionsRecord(Record rcd, boolean newr) {
		super(null, rcd);
		setVisibleMessagePanel(false);

		// drivers
		ServiceRequest sr = new ServiceRequest(ServiceRequest.DB_QUERY, "t_drivers", null);
		selector = new RecordSelector(sr, "t_drclass", "t_drname", rcd.getFieldValue("t_cndriver"), false);

		this.propertyJTable = new TPropertyJTable((String) rcd.getFieldValue("t_cnextended_prp"));

		addInputComponent("t_cnname", TUIUtils.getJTextField(rcd, "t_cnname"), true, true);
		addInputComponent("t_cndriver", selector, false, true);
		addInputComponent("t_cnurl", TUIUtils.getJTextField(rcd, "t_cnurl"), true, true);
		addInputComponent("t_cnuser", TUIUtils.getJTextField(rcd, "t_cnuser"), false, true);
		addInputComponent("t_cnpassword", TUIUtils.getJPasswordField(rcd, "t_cnpassword"), false, true);

		JScrollPane jsp = TUIUtils.getJTextArea(rcd, "t_cntable_filter", 7);
		jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		addInputComponent("t_cntable_filter", jsp, false, true);

		JScrollPane prpjs = new JScrollPane(propertyJTable);
		prpjs.getViewport().setBackground(Color.WHITE);

		FormLayout lay = new FormLayout("left:pref, 3dlu, 200dlu", // columns
				"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 80dlu, 3dlu, p, 80dlu"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(getLabelFor("t_cnname"), cc.xy(1, 1));
		build.add(getInputComponent("t_cnname"), cc.xy(3, 1));
		build.add(getLabelFor("t_cndriver"), cc.xy(1, 3));
		build.add(getInputComponent("t_cndriver"), cc.xy(3, 3));
		build.add(getLabelFor("t_cnurl"), cc.xy(1, 5));
		build.add(getInputComponent("t_cnurl"), cc.xy(3, 5));
		build.add(getLabelFor("t_cnuser"), cc.xy(1, 7));
		build.add(getInputComponent("t_cnuser"), cc.xy(3, 7));
		build.add(getLabelFor("t_cnpassword"), cc.xy(1, 9));
		build.add(getInputComponent("t_cnpassword"), cc.xy(3, 9));
		build.add(TUIUtils.getJLabel("t_cnextended_prp", false, true), cc.xyw(1, 11,3));
		build.add(prpjs, cc.xyw(1, 12, 3));
		build.add(getLabelFor("t_cntable_filter"), cc.xy(1, 14));
		build.add(getInputComponent("t_cntable_filter"), cc.xyw(1, 15, 3));

		setActionBar(new ApplyAction(this));
		addWithoutBorder(build.getPanel());
		preValidate(null);
	}
	
	@Override
	public void setModel(Record mod) {
		super.setModel(mod);
		// tabla de propiedades 
		propertyJTable.setPropertys((String) mod.getFieldValue("t_cnextended_prp"));
	}

	@Override
	public Record getRecord() {
		Record rcd = super.getRecord();
		// update the driver name
		TEntry te = (TEntry) selector.getSelectedItem();
		rcd.setFieldValue("t_cndrname", te.getValue());
		// aditional properties
		rcd.setFieldValue("t_cnextended_prp", propertyJTable.getProperties());
		return rcd;
	}
}
