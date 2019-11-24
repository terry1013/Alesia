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
package gui;

import javax.swing.*;

import core.*;
import core.datasource.*;
import core.reporting.*;

import action.*;

/**
 * users list
 * 
 */
public class TUserList extends UIListPanel {

	/**
	 * nueva instancia
	 * 
	 */
	private TAbstractAction userPerm;

	public TUserList() {
		super("title_users");
		this.userPerm = new UserPermissionAction(this);
		setToolBar(new NewRecord(this), new EditRecord(this), new DeleteRecord(this), userPerm, new ExportToFileAction(this, 
				""));
		setColumns("t_ususer_id;t_usname;t_usroll");
		putClientProperty(TConstants.SPECIAL_COLUMN, "t_ususer_id");
		setIconParameters("0;users4");
	}

	@Override
	public void init() {
		setServiceRequest(new ServiceRequest(ServiceRequest.DB_QUERY, "t_users", null));
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		if (aa instanceof NewRecord) {
			pane = new UserRecord(getRecordModel(), true);
		}
		if (aa instanceof EditRecord) {
			pane = new UserRecord(getRecord(), false);
		}
		return pane;
	}
}
