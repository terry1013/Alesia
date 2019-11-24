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
 * Generic action to load an object from T_LOCAL_PROPERTIE file. This action show a {@link JOptionPane} with a list of
 * previous saved property gruped by {@link #propertyID} parameter.
 * <p>
 * When the user select an option to load, the {@link #actionPerformed2()} method is called. Sub class use this metod to
 * retrive the selected property to load.
 * <p>
 * When using {@link SaveProperty} / {@link LoadProperty} together, the {@link #propertyID} MUST BE THE SAME for both
 * constructos, leting both actions save and load the same types of objects according to {@link #propertyID}
 */
public class LoadProperty extends TAbstractAction {

	private String propertyID;
	private Object value;

	public LoadProperty(String pid) {
		super(TAbstractAction.NO_SCOPE);
		this.propertyID = pid;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// options list
		Record ur = Session.getUser();
		String uid = ur == null ? "" : (String) Session.getUserName();
		String wc = "T_LPUSERID = '" + uid + "' AND T_LPID = '" + propertyID + "'";
		TEntry[] te = TStringUtils.getTEntryGroupFrom("T_LOCAL_PROPERTIES", "T_LPID", "T_LPOBJECT", wc, null);

		// last selected
		String lss = (String) TPreferences.getPreference(propertyID + ".LastSelected", "", null);
		TEntry ls = lss != null ? new TEntry(propertyID, lss) : null;

		TEntry sel = (TEntry) JOptionPane.showInputDialog(Alesia.mainFrame, "Select the elements to load", "Load",
				JOptionPane.PLAIN_MESSAGE, null, te, ls);

		// save selected and perform action2
		if (sel != null) {
			TPreferences.setPreference(propertyID + ".LastSelected", "", sel.getValue());
			this.value = TPreferences.getPreference(propertyID, (String) sel.getValue(), null);
			actionPerformed2();
		}
	}
}
