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

import action.*;
import core.tasks.*;

public class AddressBookUpdateAction extends ConfirmAction {

	private String action;

	public AddressBookUpdateAction(String act) {
		super(TAbstractAction.NO_SCOPE);
		this.action = act;
		setDefaultValues("updater." + act);
		setMessagePrefix("action.updater." + act + ".");
	}

	@Override
	public void actionPerformed2() {
		AddressBookUpdater abu = new AddressBookUpdater();
		abu.getTaskParameters().put(AddressBookUpdater.UPDATER_ACTION, action);
		TTaskManager.submitRunnable(abu, null, true);
	}
}
