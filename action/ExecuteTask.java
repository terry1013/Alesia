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

import javax.swing.*;

import core.*;
import core.datasource.*;

public class ExecuteTask extends TAbstractAction {

	private UIListPanel uiListPanel;

	public ExecuteTask(UIListPanel adi) {
		super(RECORD_SCOPE);
		this.uiListPanel = adi;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object[] options = {TStringUtils.getString("action.task.inexe"),
				TStringUtils.getString("action.task.bgexe"),
				TStringUtils.getString("action.delete.cancel")};
		int opt = JOptionPane.showOptionDialog(null, TStringUtils.getString("action.task.ms03"),
				TStringUtils.getString("action.task.title"), JOptionPane.DEFAULT_OPTION,
				JOptionPane.WARNING_MESSAGE, null, options, options[2]);
		if (opt < 2) {
			Record rtta = uiListPanel.getRecord();
//			TTaskManager.submitTask(rtta, opt == 0);
		}
	}
}
