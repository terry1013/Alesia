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
package plugin.flicka;

import gui.*;
import gui.docking.*;

import java.beans.*;
import java.util.*;

import javax.swing.*;

import core.datasource.*;
import core.reporting.*;

public class HorseHistory extends UIListPanel implements DockingComponent {

	ServiceRequest request;

	public HorseHistory() {
		super(null);
		this.request = new ServiceRequest(ServiceRequest.DB_QUERY, "reslr", null);
		request.setParameter(ServiceRequest.ORDER_BY, "redate DESC");
		setToolBar(new ExportToFileAction(this, ""));
		setColumns("redate;rerace;redistance;restar_lane;rejockey;reend_pos;recps");
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;

		return pane;
	}

	@Override
	public void init() {
		setMessage("flicka.msg02");
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object src = evt.getSource();
		Object newv = evt.getNewValue();
		Record rcd = (Record) newv;

		String horse = null;
		Date date = null;
		if (src instanceof RaceList) {
			if (rcd != null) {
				horse = (String) rcd.getFieldValue("rehorse");
				date = (Date) rcd.getFieldValue("redate");
			}
		}
		if (src instanceof PDistribution) {
			if (rcd != null && rcd.getFieldValue("pdfield").equals("rehorse")) {
				horse = (String) rcd.getFieldValue("pdvalue");
				date = (Date) rcd.getFieldValue("pddate");
			}
		}
		if (horse != null) {
			String wc = "rehorse = '" + horse + "' AND redate < '" + date + "'";
			request.setData(wc);
			setServiceRequest(request);
		} else {
			setMessage("flicka.msg02");
		}
	}
}
