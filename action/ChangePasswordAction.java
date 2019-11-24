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

import gui.*;

import java.awt.event.*;
import java.beans.*;

import javax.swing.*;


import core.*;
import core.datasource.*;

/** Accion que presenta ventana para cambiar contrace;a
 * 
 */
public class ChangePasswordAction extends TAbstractAction implements PropertyChangeListener {
	
	private JDialog dialog;
	private ChangePassword changePass;
	
	public ChangePasswordAction() {
		super(TAbstractAction.NO_SCOPE);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		changePass = new ChangePassword(Session.getUser());
		changePass.addPropertyChangeListener(TConstants.ACTION_PERFORMED, this);
		dialog = getDialog(changePass, "action.changepassword");
		dialog.setVisible(true);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == changePass && evt.getNewValue() instanceof AceptAction) {
			Record r = changePass.getRecord();
			ConnectionManager.getAccessTo("t_users").write(r);
			Session.updateUserRecord(r);
		}
		dialog.dispose();
	}
}
