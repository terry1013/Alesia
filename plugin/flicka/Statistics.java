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

import javax.swing.*;

import action.*;
import core.datasource.*;
import core.reporting.*;

public class Statistics extends UIListPanel implements DockingComponent {

	ServiceRequest request;

	public Statistics() {
		super(null);
		this.request = new ServiceRequest(ServiceRequest.DB_QUERY, "statistics", null);
		setToolBar(new RunSimulation(this, "byPosition"), new DeleteRecord2(this), new ExportToFileAction(this, ""));
		setColumns("stdate;strace;stfield;stsignature;stdecision;stevent;stdistance;ststdev;stmean");
		// setColumns("stdate;strace;stfield;sthorsesample;stjockeysample;stupbound;stconvergence");
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		return pane;
	}

	@Override
	public void init() {
		setServiceRequest(request);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

	}
}
