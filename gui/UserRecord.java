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

import javax.swing.*;
import javax.swing.border.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

/**
 * panel de entrada para mantenimiento de usuarios de aplicacion.
 * 
 */
public class UserRecord extends AbstractRecordDataInput {

	private JComboBox rcdT;

	/**
	 * nueva instancia
	 * 
	 * @param rcd - registro
	 * @param newr - true si es para crear un nuevo registro
	 * @param uid - id de tipo de usuario a crear (constant.prperties)
	 */
	public UserRecord(Record rcd, boolean newr) {
		super(null, rcd);

		ServiceRequest sr = new ServiceRequest(ServiceRequest.DB_QUERY, "t_users",
				"t_usrcd_type = 'grou'");
		RecordSelector rcds = new RecordSelector(sr, "t_ususer_id", "t_usname",
				rcd.getFieldValue("t_usroll"));
		// para cualquier usuario exepto *master
		if (!rcd.getFieldValue("t_ususer_id").equals("*master")) {
//			rcds.insertItemAt(ConstantUtilities.getConstant("none"), 0);
			rcds.insertItemAt(TStringUtils.getTEntry("usrrol.none"), 0);
		} else {
			// para master
			rcds.removeAllItems();
			rcds.insertItemAt(TStringUtils.getTEntry("usrrol.master"), 0);
//			rcds.insertItemAt(ConstantUtilities.getConstant("*master"), 0);
		}

		rcdT = TUIUtils.getJComboBox("t_usrcd_type", rcd, "t_usrcd_type");
		rcdT.addActionListener(this);

		addInputComponent("t_usrcd_type", rcdT, false, newr);
		addInputComponent("t_ususer_id", TUIUtils.getJTextField(rcd, "t_ususer_id"), true, newr);
		addInputComponent("t_usname", TUIUtils.getJTextField(rcd, "t_usname"), true, true);
		addInputComponent("t_usdescrip", TUIUtils.getJTextArea(rcd, "t_usdescrip"), false, true);
		addInputComponent("t_uspassword", TUIUtils.getJTextField(rcd, "t_uspassword"), true,
				true);
		addInputComponent("t_usroll", rcds, false, true);

		addInputComponent("t_usmc_upper", TUIUtils.getJCheckBox(rcd, "t_usmc_upper"), false,
				true);
		addInputComponent("t_usmc_digit", TUIUtils.getJCheckBox(rcd, "t_usmc_digit"), false,
				true);
		addInputComponent("t_usmc_ssimb", TUIUtils.getJCheckBox(rcd, "t_usmc_ssimb"), false,
				true);
		addInputComponent("t_uspass_mlen",
				TUIUtils.getJFormattedTextField(rcd, "t_uspass_mlen"), true, true,
				SystemVariables.getintVar("passMinLength"),
				SystemVariables.getintVar("passMaxLength"));

		;;
		addInputComponent("t_usmax_attemps",
				TUIUtils.getJFormattedTextField(rcd, "t_usmax_attemps"), true, true,
				SystemVariables.getintVar("loginMinAttemps"),
				SystemVariables.getintVar("loginMaxAttemps"));
		addInputComponent("t_usstatus", TUIUtils.getJComboBox("rcd_sts", rcd, "t_usstatus"),
				false, true);

		FormLayout lay = new FormLayout("left:pref, 3dlu, pref, 7dlu, left:pref, 3dlu, pref", // columns
				"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, p, 3dlu p, 3dlu p"); // rows
		// lay.setColumnGroups(new int[][] { { 1, 5 }, { 3, 7 } });
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(getLabelFor("t_usrcd_type"), cc.xy(1, 1));
		build.add(getInputComponent("t_usrcd_type"), cc.xy(3, 1));
		build.add(getLabelFor("t_ususer_id"), cc.xy(1, 3));
		build.add(getInputComponent("t_ususer_id"), cc.xy(3, 3));
		build.add(getLabelFor("t_uspassword"), cc.xy(5, 3));
		build.add(getInputComponent("t_uspassword"), cc.xy(7, 3));
		build.add(getLabelFor("t_usname"), cc.xy(1, 5));
		build.add(getInputComponent("t_usname"), cc.xyw(3, 5, 5));
		build.add(getLabelFor("t_usroll"), cc.xy(1, 7));
		build.add(getInputComponent("t_usroll"), cc.xyw(3, 7, 3));
		build.add(getLabelFor("t_usmax_attemps"), cc.xy(1, 9));
		build.add(getInputComponent("t_usmax_attemps"), cc.xy(3, 9));
		build.add(getLabelFor("t_uspass_mlen"), cc.xy(5, 9));
		build.add(getInputComponent("t_uspass_mlen"), cc.xy(7, 9));
		build.add(getLabelFor("t_uspass_mlen"), cc.xy(5, 9));
		build.add(getInputComponent("t_uspass_mlen"), cc.xy(7, 9));
		build.add(getLabelFor("t_usdescrip"), cc.xy(1, 11));
		build.add(getInputComponent("t_usdescrip"), cc.xyw(1, 12, 7));

		JPanel jp1 = new JPanel(new GridLayout(0, 2));
		jp1.add(getInputComponent("t_usmc_upper"));
		jp1.add(getInputComponent("t_usmc_digit"));
		jp1.add(getInputComponent("t_usmc_ssimb"));
		jp1.add(Box.createGlue());

		jp1.setBorder(new TitledBorder(TStringUtils.getString("f05")));
		build.add(jp1, cc.xyw(1, 14, 7));

		build.add(getLabelFor("t_usstatus"), cc.xy(1, 16));
		build.add(getInputComponent("t_usstatus"), cc.xy(3, 16));

		setDefaultActionBar();
		add(build.getPanel());
		preValidate(null);
	}

	/*
	 * (non-Javadoc)
	 * @see gui.AbstractDataInput#preValidate(java.lang.Object)
	 */
	public void preValidate(Object src) {
		super.preValidate(src);
		if (!isShowingError()) {
			// tipo de registro
			boolean ena = ((TEntry) rcdT.getSelectedItem()).getKey().equals("user");
			setEnabledInputComponent("t_uspassword", ena);
			setEnabledInputComponent("t_usroll", ena);
			setEnabledInputComponent("t_usmax_attemps", ena);
			setEnabledInputComponent("t_uspass_mlen", ena);
			setEnabledInputComponent("t_usmc_upper", ena);
			setEnabledInputComponent("t_usmc_digit", ena);
			setEnabledInputComponent("t_usmc_ssimb", ena);
			super.preValidate(src);

			// t_uspassword min len > 4 se valida por el componente
		}
	}
}
