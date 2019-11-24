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
package gui.datasource;

import gui.*;

import javax.swing.*;

import core.*;
import core.datasource.*;

/**
 * drivers list form t_drivers. 
 * 
 * 180405: this list is for reference only. the internal driver MUST BE manualy configured and tested.
 * 
 * @author terry
 * 
 */
public class TDrivers extends UIListPanel {

	public TDrivers() {
		super(null);
		// setToolBar(false, new CloneRecord(this));
		setColumns("t_drname;T_DRCORP");
		setIconParameters("0;drivers_;t_drname");
	}

	@Override
	public void init() {
		setServiceRequest(new ServiceRequest(ServiceRequest.DB_QUERY, "t_drivers", null));
		setView(LIST_VIEW_VERTICAL);
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		/*
		 * if (aa instanceof NewRecord) { pane = new TDriverManagerRecord(getRecordModel(), true); } if (aa instanceof
		 * EditRecord) { pane = new TDriverManagerRecord(getRecord(), false); }
		 */
		return pane;
	}
}
