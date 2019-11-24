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
package plugin.eteam;

import gui.docking.*;

import java.awt.event.*;

import net.infonode.docking.*;
import action.*;

/**
 * build and load dashboard for mail plugin
 * 
 * @author terry
 * 
 */
public class ETeamAction extends TAbstractAction {

	public ETeamAction() {
		super("eteam.action", "/plugin/eteam/mail2", NO_SCOPE, "tteteam.action");
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		SplitWindow sw = new SplitWindow(false, 0.6f, DockingContainer.createDynamicView("plugin.mail.AddressBook"),
				DockingContainer.createDynamicView("plugin.eteam.ChatPanel"));
		DockingContainer.setWindow(sw, getClass().getName());
	}
}
