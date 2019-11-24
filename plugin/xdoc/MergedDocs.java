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
package plugin.xdoc;

import gui.*;
import gui.docking.*;

import java.beans.*;

import javax.swing.*;

import core.*;
import core.datasource.*;

/**
 * list of all merged or generated documents
 * 
 * @author terry
 * 
 */
public class MergedDocs extends UIListPanel implements DockingComponent {

	ServiceRequest request;

	/**
	 * new instance
	 */
	public MergedDocs() {
		super(null);
		this.request = new ServiceRequest(ServiceRequest.DB_QUERY, "xd_merged_doc", null);
		setToolBar(new MergeDocActions(this, MergeDocActions.OPEN), new MergeDocActions(this, MergeDocActions.PRINT));
		setColumns("xd_mddate;xd_mddoc_name;xd_mdcertificate;xd_mdremittent");
		setIconParameters("0;/plugin/xdoc/server_document");
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
