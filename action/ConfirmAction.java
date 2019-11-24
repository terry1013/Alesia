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

/**
 * test implementation for futher migration of all actions framework
 * 
 */
public class ConfirmAction extends TAbstractAction implements NoActionForSpecialRecord {

	public ConfirmAction(int sco) {
		super(sco);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object[] options = {TStringUtils.getString(messagePrefix + "confirm"),
				TStringUtils.getString(messagePrefix + "cancel")};
		int o = JOptionPane.showOptionDialog(Alesia.mainFrame, TStringUtils.getString(messagePrefix + "message"),
				TStringUtils.getString(messagePrefix + "title"), JOptionPane.DEFAULT_OPTION,
				JOptionPane.WARNING_MESSAGE, null, options, options[1]);
		if (o == JOptionPane.YES_OPTION) {
			actionPerformed2();
		}
	}
}
