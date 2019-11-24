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
package action;

import java.awt.event.*;

import javax.swing.*;

import core.*;
import core.datasource.*;

/**
 * Generic action to save an objet into T_LOCAL_PROPERTIE file. This action show a {@link JOptionPane} with a entry text
 * field allowing the user write the name of the property to save. Se values to store is previous setted using
 * {@link #setValue(Object)} method previous saved property gruped by {@link #propertyID} parameter.
 * <p>
 * See {@link LoadProperty} for more info of conbined {@link SaveProperty} / {@link LoadProperty} usage
 */
public class SaveProperty extends TAbstractAction {

	private String propertyID;
	private String object;
	private Object value;

	public SaveProperty(String pid) {
		super(TAbstractAction.NO_SCOPE);
		this.propertyID = pid;
	}

	public void setValue(Object nval) {
		this.value = nval;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// options list
		Record ur = Session.getUser();
		String uid = ur == null ? "" : (String) Session.getUserName();
		String wc = "T_LPUSERID = '" + uid + "' AND T_LPID = '" + propertyID + "'";

		// last selected
		String ls = (String) TPreferences.getPreference(propertyID + ".LastSelected", "", null);

		String savn = (String) JOptionPane.showInputDialog(Alesia.mainFrame, "Write the name", "Save",
				JOptionPane.PLAIN_MESSAGE, null, null, ls);

		// save selected and perform action2
		if (savn != null) {
			TPreferences.setPreference(propertyID + ".LastSelected", "", savn);
			TPreferences.setPreference(propertyID, savn, value);
			actionPerformed2();
		}
	}
}
