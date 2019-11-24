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
import core.tasks.*;

/**
 * redirect the close operation to {@link TConstants#DEFAULT_CANCEL_ACTION} and clear list of open dialogs
 * 
 * @author terry
 * 
 */
public class DialogListener extends WindowAdapter {

	private TAbstractAction appact;
	private JComponent panel;

	public DialogListener(TAbstractAction aa, JComponent uicp) {
		this.appact = aa;
		this.panel = uicp;
	}

	@Override
	public void windowClosing(WindowEvent e) {
		if (appact != null) {
			// para que no se vea el doClick()
			appact.actionPerformed(new ActionEvent(appact, ActionEvent.ACTION_PERFORMED, (String) appact
					.getValue(TAbstractAction.NAME_ID)));
			TAbstractAction.dialogs.remove(panel.getClass().getName());
		}
		
		// remove form autoUpdate
		if (panel instanceof UIListPanel) {
			TTaskManager.getListUpdater().remove((UIListPanel) panel);
		}

	}
}
