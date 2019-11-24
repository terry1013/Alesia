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
package plugin.dbtweezer;

import gui.*;

import javax.swing.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

public class AlterColumn extends AbstractRecordDataInput {

	private JComboBox dataType;
	private Record scripRcd, oldColrcd;
	
	public AlterColumn(Record colrcd, Record scrcd) {
		super(null, colrcd);
		this.scripRcd = scrcd;
		oldColrcd = colrcd;
		
		// task related fields
		addInputComponent("s_scname", TUIUtils.getJTextField(scrcd, "s_scname"), true, true);
		addInputComponent("s_scstatus", TUIUtils.getJComboBox("scstatus_", scrcd, "s_scstatus"), false, true);

		//String sqlt = (String) colrcd.getFieldValue("data_type");
		//System.out.println(sqlt);
		addInputComponent("column_name", TUIUtils.getJTextField(colrcd, "column_name"), true, true);
		dataType = TUIUtils.getJComboBox("sql.datatypes_", colrcd, "data_type");
		addInputComponent("data_type", dataType, false, true);
		addInputComponent("column_size", TUIUtils.getJFormattedTextField(colrcd, "column_size"), true, true);
		addInputComponent("decimal_digits", TUIUtils.getJFormattedTextField(colrcd, "decimal_digits"), false, true);
		addInputComponent("column_def", TUIUtils.getJTextField(colrcd, "column_def"), false, true);
		
		boolean in = colrcd.getFieldValue("is_nullable").equals("YES");
		JCheckBox jcb = TUIUtils.getJCheckBox("is_nullable", in);
		addInputComponent("is_nullable", jcb, false, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, left:pref, 7dlu, left:pref, 3dlu, left:pref", // columns
				"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(getLabelFor("s_scname"), cc.xy(1, 1));
		build.add(getInputComponent("s_scname"), cc.xyw(3, 1, 5));
		build.add(getLabelFor("s_scstatus"), cc.xy(1, 3));
		build.add(getInputComponent("s_scstatus"), cc.xy(3, 3));

		build.addSeparator("Alterar columna", cc.xyw(1, 5, 7));
		build.add(getLabelFor("column_name"), cc.xy(1, 7));
		build.add(getInputComponent("column_name"), cc.xyw(3, 7, 5));
		build.add(getLabelFor("data_type"), cc.xy(1, 9));
		build.add(getInputComponent("data_type"), cc.xy(3, 9));
		build.add(getInputComponent("is_nullable"), cc.xy(5, 9));
		build.add(getLabelFor("column_size"), cc.xy(1, 11));
		build.add(getInputComponent("column_size"), cc.xy(3, 11));
		build.add(getLabelFor("decimal_digits"), cc.xy(5, 11));
		build.add(getInputComponent("decimal_digits"), cc.xy(7, 11));
		build.add(getLabelFor("column_def"), cc.xy(1, 13));
		build.add(getInputComponent("column_def"), cc.xyw(3, 13, 5));

		setDefaultActionBar();
		add(build.getPanel());
		preValidate(null);
	}

	@Override
	public Record getRecord() {
		Record ncrcd = super.getRecord();
		String sql = DBTConnectionManager.getSourceDB().getAlterTable(oldColrcd, ncrcd);
		scripRcd.setFieldValue("s_scname", getFields().get("s_scname"));
		scripRcd.setFieldValue("s_scstatus", getFields().get("s_scstatus"));
		scripRcd.setFieldValue("s_scscript", sql);
		return scripRcd;
	}
}
