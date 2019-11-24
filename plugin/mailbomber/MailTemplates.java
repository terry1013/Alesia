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

import gui.*;
import gui.docking.*;

import java.beans.*;
import java.sql.*;

import javax.swing.*;

import action.*;
import core.*;
import core.datasource.*;

public class MailTemplates extends UIListPanel implements DockingComponent {

	private ServiceRequest request;

	/**
	 * new instance
	 */
	public MailTemplates() {
		super(null);
		this.request = new ServiceRequest(ServiceRequest.DB_QUERY, "m_templates", null);
		setToolBar(new NewRecord2(this), new EditRecord2(this), new DeleteRecord2(this));
		setColumns("m_tename;m_tedescription;m_teversion;m_tecreated_at");
		setIconParameters("0;*;m_teicon");
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		boolean nr = aa instanceof NewRecord2;
		Record r = nr ? getRecordModel() : getRecord();
		if (nr) {
			r.setFieldValue(0, TStringUtils.getRecordId());
			r.setFieldValue("m_tecreated_at", new Timestamp(System.currentTimeMillis()));
		}
		return new MailTemplateRecord(r, nr);
	}
	@Override
	public void init() {
		setServiceRequest(request);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

	}
}
