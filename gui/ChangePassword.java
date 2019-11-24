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
/**
 * Copyright (c) Terry - All right reserved. PROPRIETARY/CONFIDENTIAL.
 * Use is subject to license terms.
 *
 * @author Terry
 *
 */
package gui;

import java.awt.event.*;

import javax.swing.*;



import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

public class ChangePassword extends AbstractRecordDataInput implements ActionListener {

	private JPasswordField oldPassJP, newPassJP, confPassJP;
	private JProgressBar progressBar;
	private String oldPass, newPass;
	private Record usrRcd;
	private int passlen;

	/**
	 * nueva instancia
	 * 
	 * @param rcd - registro de usuario actualmente en sesion
	 */
	public ChangePassword(Record rcd) {
		super("security.title08", rcd);
		this.usrRcd = rcd;
		this.passlen = (Integer) rcd.getFieldValue("t_uspass_mlen");
		this.oldPass = (String) rcd.getFieldValue("t_uspassword");
		rcd.setFieldValue("t_uspassword", "");
		this.oldPassJP = TUIUtils.getJPasswordField(rcd, "t_uspassword");
		addInputComponent("t_uspassword", oldPassJP, true, true);
		this.newPassJP = TUIUtils.getJPasswordField("ttchgpass.new", "", 10);
		addInputComponent("chgpass.new", newPassJP, true, true);
		this.confPassJP = TUIUtils.getJPasswordField("ttchgpass.verify", "", 10);
		confPassJP.getDocument().addDocumentListener(this);

		this.progressBar = new JProgressBar(0, passlen);

		FormLayout lay = new FormLayout("left:pref, 40dlu, left:pref",
				"p, 3dlu, p, 3dlu, p, 15dlu, p, p, ");// rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(getLabelFor("t_uspassword"), cc.xy(1, 1));
		build.add(getInputComponent("t_uspassword"), cc.xy(3, 1));
		build.add(getLabelFor("chgpass.new"), cc.xy(1, 3));
		build.add(newPassJP, cc.xy(3, 3));
		build.add(TUIUtils.getJLabel("chgpass.verify", true, true), cc.xy(1, 5));
		build.add(confPassJP, cc.xy(3, 5));
		build.add(TUIUtils.getJLabel("chgpass.strong", false, true), cc.xy(1, 7));
		build.add(progressBar, cc.xyw(1, 8, 3));

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
			String op = new String(oldPassJP.getPassword());
			newPass = new String(newPassJP.getPassword());
			String cp = new String(confPassJP.getPassword());
			setEnableDefaultButton(false);

			// barra de progreso
			progressBar.setValue(newPass.length());

			// campo contrace;a no contiene la actual
			if (op.equals("")) {
				return;
			}
			if (!oldPass.equals(op)) {
				showAplicationExceptionMsg("security.msg06");
				return;
			}

			// patron de contraceña
			if (src == newPassJP) {
				if (!newPass.equals("")) {
					String err = "";
					// debe contener mayuscula
					if ((Boolean) usrRcd.getFieldValue("t_usmc_upper")) {
						err = newPass.matches("(?=.*[A-Z]).{4,}") ? err : TStringUtils
								.getString("t_usmc_upper") + ", ";
					}
					// debe contener digitos numericos
					if ((Boolean) usrRcd.getFieldValue("t_usmc_digit")) {
						err += newPass.matches("(?=.*\\d).{4,}") ? err : TStringUtils
								.getString("t_usmc_digit") + ", ";
					}
					// debe contener caracteres especiales (?=.*[@#$%^&+=])
					if ((Boolean) usrRcd.getFieldValue("t_usmc_ssimb")) {
						err += newPass.matches("(?=.*[@#$%^&+=!¡]).{4,}") ? err : TStringUtils
								.getString("t_usmc_ssimb") + ", ";
					}
					// Longitud
					if (newPass.length() < passlen) {
						err += TStringUtils.getString("t_uspass_mlen") + " " + passlen
								+ ", ";
					}
					if (!err.equals("")) {
						err = err.substring(0, err.length() - 2);
						showAplicationException(new TError("security.msg05", err));
						return;
					}
				}
			}
			if (newPass.equals("") || cp.equals("")) {
				return;
			}
			// nueve y verificaicon no coinciden
			if (!newPass.equals(cp)) {
				showAplicationExceptionMsg("security.msg07");
				return;
			}
			setEnableDefaultButton(true);
		}
	}

	@Override
	public void validateFields() {
		// verifica contraceña

	}

	@Override
	public Record getRecord() {
		// cambia contrace;a
		Record r = super.getRecord();
		r.setFieldValue("t_uspassword", newPass);
		return r;
	}
}
