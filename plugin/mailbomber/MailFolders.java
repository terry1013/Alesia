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
import java.util.*;

import javax.swing.*;

import action.*;
import core.*;
import core.datasource.*;

/**
 * draft folder is where all composed mail are stored.
 * 
 * @author terry
 * 
 */
public class MailFolders extends UIListPanel implements DockingComponent {

	private ServiceRequest request;

	/**
	 * new instance
	 */
	public MailFolders() {
		super(null);
		this.request = new ServiceRequest(ServiceRequest.DB_QUERY, "m_messages", null);
		setToolBar(new NewRecord2(this), new EditRecord2(this), new DeleteRecord2(this), new SendMail(this));
		setColumns("m_mesubject;m_meto;m_mecreated_at");
		// setIconParameters("0;/plugin/mail/mail2");
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		if (aa instanceof NewRecord2) {
			Record r = getRecordModel();
			r.setFieldValue(0, TStringUtils.getRecordId());
			r.setFieldValue("m_mecreated_at", new Timestamp(System.currentTimeMillis()));
			pane = new ComposeMailDialog(r, true);
		}
		if (aa instanceof EditRecord2) {
			pane = new ComposeMailDialog(getRecord(), false);
		}
		return pane;
	}

	@Override
	public void init() {
		setServiceRequest(request);
		setReferenceColumn("m_meto",
				TStringUtils.getTEntryGroupFrom("M_ADDRESS_BOOK", "M_ABID", "M_ABNAME", null, null));
	}
	@Override
	public void propertyChange(PropertyChangeEvent evt) {

	}
}
