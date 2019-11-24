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
package plugin.mailbomber;

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
public class MailBomberAction extends TAbstractAction {

	public MailBomberAction() {
		super(NO_SCOPE);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
//		View hlp = DockingContainer.createDynamicView(MailBomberWellcome.class.getName());
		SplitWindow sw = new SplitWindow(false, 0.5f, DockingContainer.createDynamicView(MailFolders.class.getName()),
				DockingContainer.createDynamicView(MailTemplates.class.getName()));
		
		SplitWindow sw1 = new SplitWindow(false, 0.5f, sw,
				DockingContainer.createDynamicView(MailLog.class.getName()));
		SplitWindow sw2 = new SplitWindow(true, 0.3f, DockingContainer.createDynamicView(AddressBookTree.class
				.getName()), sw1);

//		SplitWindow sw3 = new SplitWindow(false, 0.5f, sw2, hlp);
		DockingContainer.setWindow(sw2, getClass().getName());
		// WindowBar wb = rootw.getWindowBar(Direction.RIGHT);
		// wb.addTab(hlp);
	}
}
