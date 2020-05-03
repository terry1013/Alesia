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
import java.awt.event.*;

import javax.swing.*;

import com.alee.laf.checkbox.*;
import com.alee.laf.text.*;
import com.alee.managers.settings.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import core.*;

public class UserLogIn extends TUIFormPanel implements ActionListener {

	private WebCheckBox remindUser;
	private JTextField jtf_user_id;
//	private Record usrmod;
	private int t_usmax_attemps;

	/**
	 * nueva instancia
	 * 
	 * @param usr - registro de usuario
	 * 
	 */
	public UserLogIn() {
		this.t_usmax_attemps = -1;
		setTitle("UserLogIn.title");
		setDescription("UserLogIn.description");
		WebTextField usertf = TUIUtils.getWebTextField("UserLogIn.user", "", 20);
		WebPasswordField passf = TUIUtils.getWebPasswordField("UserLogIn.password", "", 20);
		passf.setText("");
		addInputComponent(usertf, true, true);
		addInputComponent(passf, true, true);

		// recordar usuario
		WebCheckBox jcb = TUIUtils.getWebCheckBox("UserLogIn.rememberPassword");
		if (jcb.isSelected()) {
			usertf.setText(SettingsManager.get("RemindUser", ""));
		}
		FormLayout layout = new FormLayout("right:max(40dlu;pref), 3dlu, 80dlu", // 1st major colum
				""); // add rows dynamically
		DefaultFormBuilder builder = new DefaultFormBuilder(layout).border(Borders.DIALOG);

		builder.append(TStringUtils.getString("UserLogIn.user"), getInputComponent("UserLogIn.user"));
		builder.nextLine();
		builder.append(TStringUtils.getString("UserLogIn.password"), getInputComponent("UserLogIn.password"));
		builder.nextLine();
		builder.append(jcb, 3);
		
		JPanel jp = new JPanel(new BorderLayout());
		jp.add(new JLabel(TResources.getIcon("keys", 48)), BorderLayout.WEST);
		jp.add(builder.getPanel(), BorderLayout.CENTER);
		setBodyComponent(jp);
		setFooterActions("acept", "cancel");
		preValidate();
	}

	// public void propertyChange(PropertyChangeEvent evt) {
	// if (evt.getNewValue() instanceof AceptAction) {
	// Record r1 = getRecord();
	// Record r2 = ConnectionManager.getAccessTo("t_users").exist(
	// "t_ususer_id = '" + r1.getFieldValue("t_ususer_id") + "'");
	// if (r2 != null) {
	// // check num_logins
	// if (t_usmax_attemps < 0) {
	// t_usmax_attemps = (Integer) r2.getFieldValue("t_usmax_attemps");
	// }
	// // check user inactive date
	// long curd = System.currentTimeMillis();
	// long usrd = ((Date) r2.getFieldValue("t_usexpiry_period")).getTime();
	// if (usrd > 0 && (curd > usrd)) {
	// showAplicationExceptionMsg("security.msg08");
	// return;
	// }
	// // verifica contraceña. si se alcanza numero maximo de intentos, se desabilita el usuario
	// if (!r2.getFieldValue("t_uspassword").equals(r1.getFieldValue("t_uspassword"))) {
	// showAplicationExceptionMsg("security.msg10");
	//
	// t_usmax_attemps--;
	// if (t_usmax_attemps == 0) {
	// r2.setFieldValue("t_usstatus", "disa");
	// ConnectionManager.getAccessTo("t_users").update(r2);
	// showAplicationExceptionMsg("security.msg11");
	// return;
	// }
	// return;
	// }
	// Session.setUser(r2);
	// } else {
	// showAplicationExceptionMsg("security.msg09");
	// return;
	// }
	// }
	// if (evt.getNewValue() instanceof CancelAction) {
	// Exit.shutdown();
	// }
	// }

	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println("UserLogIn.actionPerformed()");
	}
}
