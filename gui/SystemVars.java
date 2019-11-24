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


import action.*;

/**
 * mantenimiento de archivo constants
 * 
 */
public class SystemVars extends UIListPanel {

	private String constantGroup;

	/**
	 * nueva instancia. este memtodo presenta la lista de constantes completa
	 * 
	 */
	public SystemVars() {
		this("system.title03", null);
	}

	/**
	 * nueva instancia
	 * 
	 * @param tid - identificador de titulo para esta lista
	 * @param cg - identificador de grupo de constantes que se desean se presenten en la lista
	 */
	public SystemVars(String tid, String cg) {
		super(tid);
		this.constantGroup = cg;
		setToolBar(new NewRecord(this), new EditRecord(this), new DeleteRecord(this));
		if (constantGroup == null) {
			setColumns("t_svgroup;t_sv_id;t_svdescription;t_svvalue");
		} else {
			setColumns("t_sv_id;t_svdescription;t_svvalue");
		}
		putClientProperty(TConstants.SPECIAL_COLUMN, "t_svgroup");
		setIconParameters("0;Class_;t_svvalue_class");
	}

	@Override
	public void init() {
		String cg1 = constantGroup == null ? null : "t_svgroup = '" + constantGroup + "'";
		setServiceRequest(new ServiceRequest(ServiceRequest.DB_QUERY, "t_system_var", cg1));
//		setView(LIST_VIEW_MOSAIC);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lizard.core.AppAbstractTable#getRightPanel(javax.swing.AbstractAction)
	 */
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		if (aa instanceof NewRecord) {
			Record rcd = getRecordModel();
			if (constantGroup != null) {
				rcd.setFieldValue("t_svgroup", constantGroup);
			}
			pane = new SystemVarsRecord(rcd, true);
		}
		if (aa instanceof EditRecord) {
			pane = new SystemVarsRecord(getRecord(), false);
		}
		return pane;
	}

}
