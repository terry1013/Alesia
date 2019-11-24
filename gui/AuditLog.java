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
 * lista de elementos de auditoria
 * 
 */
public class AuditLog extends UIListPanel {

	/**
	 * nueva instancia.
	 * 
	 * @param pid - partnerid
	 */
	public AuditLog() {
		super("security.title02");
		setToolBar(new DisplayRecord(this), new DeleteRecord2(this), new ExportToFileAction(this, ""));

		setColumns("t_altable_name;t_alaudit_track;t_aldate_time;t_alkeyfields");
		setIconParameters("0;audit_;t_alaction");
	}

	@Override
	public void init() {
		setServiceRequest(new ServiceRequest(ServiceRequest.DB_QUERY, "t_audit_log", null));
		setView(TABLE_VIEW);
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		if (aa instanceof DisplayRecord) {
			pane = new AuditLogRecord(getRecord());
		}
		return pane;
	}
}
