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

import java.awt.event.*;
import java.util.*;

import action.*;
import core.datasource.*;
import core.tasks.*;

public class SendMail extends TAbstractAction {

	public SendMail(MailFolders el) {
		super(RECORD_SCOPE);
		editableList = el;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Record msg = editableList.getRecord();
		MailBomberTask mbt = new MailBomberTask();
		Hashtable ht = new Hashtable();
		ht.put("Record", msg);
		mbt.setTaskParameters(ht);
		TTaskManager.submitRunnable(mbt, null, true);
	}
}
