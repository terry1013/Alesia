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
package plugin.dbtweezer;

import gui.*;
import gui.docking.*;

import java.beans.*;

import javax.swing.*;

import action.*;
import core.*;
import core.datasource.*;

public class ScriptList extends UIListPanel implements DockingComponent {

	private TestScript testGroovyAction;
	private ServiceRequest request;

	public ScriptList() {
		super(null);
		this.testGroovyAction = new TestScript(this);
		this.request = new ServiceRequest(ServiceRequest.DB_QUERY, "S_SCRIPT", null);
		EditRecord2 er2 = new EditRecord2(this);
		er2.setAllowWrite(true);
		setToolBar(er2, new DeleteRecord2(this));
		addToolBarAction(testGroovyAction, new RunScriptList(this));
		setColumns("s_scname;s_scstatus;s_sctable;s_scwhere");
		setIconParameters("0;;s_scaction");

	}

	@Override
	protected void enableRecordScopeActions(boolean ena) {
		super.enableRecordScopeActions(ena);
		// for sql script, disable simulation
		Record r = getRecord();
		if (r != null && r.getFieldValue("S_SCTYPE").equals("Sql")) {
			testGroovyAction.setEnabled(false);
		}
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		if (aa instanceof EditRecord2) {
			Record rcd = getRecord();
			if (rcd.getFieldValue("S_SCTYPE").equals("Groovy")) {
				pane = new GroovyScriptUI(rcd);
			} else {
				pane = new SqlScriptUI(rcd, true);
			}
		}
		return pane;
	}

	@Override
	public void init() {
		setMessage("dbt.ui.msg03");
		setReferenceColumn("S_SCSTATUS", TStringUtils.getTEntryGroup("scstatus_"));
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object nwv = evt.getNewValue();
		Object prp = evt.getPropertyName();

		if (prp.equals(TConstants.CONNECTION_CHANGE)) {
			if (nwv != null) {
				Record cp = (Record) nwv;
				request.setData("s_sccnname = '" + cp.getFieldValue("t_cnname") + "'");
				setServiceRequest(request);
			} else {
				setMessage("dbt.ui.msg03");
			}
		}
	}
}
