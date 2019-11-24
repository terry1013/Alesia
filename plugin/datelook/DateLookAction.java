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
package plugin.datelook;

import gui.docking.*;

import java.awt.event.*;

import action.*;

public class DateLookAction extends TAbstractAction {

	public DateLookAction() {
		super("datelook.action", "/plugin/datelook/dl", NO_SCOPE, "ttdatelook.action");
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		DockingContainer.addNewDynamicView("plugin.datelook.DateLook");
	}
}
