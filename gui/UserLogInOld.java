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

import java.beans.*;
import java.util.*;

import javax.swing.*;

import action.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

public class UserLogInOld extends AbstractRecordDataInput implements PropertyChangeListener {

	private JCheckBox jcb_rem_usr;
	private JTextField jtf_user_id;
	private Record usrmod;
	private int t_usmax_attemps;

	/**
	 * nueva instancia
	 * 
	 * @param usr - registro de usuario
	 * 
	 */
	public UserLogInOld() {
		super("security.title04", null);
		this.t_usmax_attemps = -1;
		ServiceRequest r = new ServiceRequest(ServiceRequest.DB_REQUEST_MODEL, "t_users", null);
		this.usrmod = (Record) ((ServiceResponse) ServiceConnection.sendTransaction(r)).getData();
		setModel(usrmod);
		addPropertyChangeListener(TConstants.ACTION_PERFORMED, this);
		this.jtf_user_id = TUIUtils.getJTextField("ttt_ususer_id", (String) usrmod.getFieldValue("t_ususer_id"), 10);
		JPasswordField jtfp = TUIUtils.getJPasswordField("ttt_uspassword",
				(String) usrmod.getFieldValue("t_uspassword"), 10);
		jtfp.setText("");
		addInputComponent("t_ususer_id", jtf_user_id, true, true);
		addInputComponent("t_uspassword", jtfp, true, true);

		// recordar usuario
		boolean bol = (Boolean) TPreferences.getPreference(TPreferences.REMIND_USER, "", false);
		jtf_user_id.setText((String) TPreferences.getPreference(TPreferences.USER, "", ""));
		this.jcb_rem_usr = TUIUtils.getJCheckBox("security.r05", bol);

		FormLayout lay = new FormLayout("left:pref, 3dlu, pref, 7dlu, left:pref, 3dlu, pref",
				"pref, 3dlu, pref, 3dlu, pref, 3dlu, pref");
		PanelBuilder bui = new PanelBuilder(lay);
		CellConstraints cc = new CellConstraints();
		bui.add(getLabelFor("t_ususer_id"), cc.xy(1, 1));
		bui.add(getInputComponent("t_ususer_id"), cc.xy(3, 1));
		bui.add(getLabelFor("t_uspassword"), cc.xy(5, 1));
		bui.add(getInputComponent("t_uspassword"), cc.xy(7, 1));
		bui.add(jcb_rem_usr, cc.xyw(1, 5, 3));

		setDefaultActionBar();
		add(bui.getPanel());
		preValidate(null);
	}

	@Override
	public Record getRecord() {
		Record usr = super.getRecord();
		// guarda preferencias
		boolean bol = jcb_rem_usr.isSelected();
		String str = bol ? (String) usr.getFieldValue("t_ususer_id") : "";
		TPreferences.setPreference(TPreferences.REMIND_USER, "", bol);
		TPreferences.setPreference(TPreferences.USER, "", str);
		return usr;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getNewValue() instanceof AceptAction) {
			Record r1 = getRecord();
			Record r2 = ConnectionManager.getAccessTo("t_users").exist(
					"t_ususer_id = '" + r1.getFieldValue("t_ususer_id") + "'");
			if (r2 != null) {
				// check num_logins
				if (t_usmax_attemps < 0) {
					t_usmax_attemps = (Integer) r2.getFieldValue("t_usmax_attemps");
				}
				// check user inactive date
				long curd = System.currentTimeMillis();
				long usrd = ((Date) r2.getFieldValue("t_usexpiry_period")).getTime();
				if (usrd > 0 && (curd > usrd)) {
					showAplicationExceptionMsg("security.msg08");
					return;
				}
				// verifica contraceña. si se alcanza numero maximo de intentos, se desabilita el usuario
				if (!r2.getFieldValue("t_uspassword").equals(r1.getFieldValue("t_uspassword"))) {
					showAplicationExceptionMsg("security.msg10");

					t_usmax_attemps--;
					if (t_usmax_attemps == 0) {
						r2.setFieldValue("t_usstatus", "disa");
						ConnectionManager.getAccessTo("t_users").update(r2);
						showAplicationExceptionMsg("security.msg11");
						return;
					}
					return;
				}
				Session.setUser(r2);
			} else {
				showAplicationExceptionMsg("security.msg09");
				return;
			}
		}
		if (evt.getNewValue() instanceof CancelAction) {
			Exit.shutdown();
		}
	}
}
