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
package plugins.flicka;

import java.beans.*;

import javax.swing.*;

import org.javalite.activejdbc.*;

import com.alee.laf.table.*;

import action.*;
import core.*;
import core.datasource.model.*;
import gui.*;

public class RaceList extends TUIListPanel implements PropertyChangeListener {

	private TAbstractAction newFromTable, baseNewRecord, baseEditRecord;
	private RaceRecordFromTable recordFromTable;
	private WebTable table;
	private TAbstractTableModel tableModel;

	public RaceList() {
		showAditionalInformation(false);
		// actionMap = Alesia.getInstance().getContext().getActionMap(this);
		setToolBar(TActionsFactory.getActions("newModel", "editModel"));
		Action a = TActionsFactory.getAction("newModel");
		a.putValue(TActionsFactory.TUIPANEL, this);

//		newFromTable.setIcon("newFromTable");
		setColumns("restar_lane;rehorse;rejockey;reend_pos;rejockey_weight;recps");
		setIconParameters("-1; ");
	}

	@Override
	public TUIFormPanel getTUIFormPanel(Action action) {
		TUIFormPanel tuifp = null;
		if (action.getValue(Action.NAME).equals("newRecord")) {
			Races r = Races.create();
			tuifp = new RaceRecord(r, true, RaceRecord.BASIC);
		}
		return tuifp;
	}

	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		// if (aa == baseNewRecord) {
		// Record rcd = getRecordModel();
		// RaceRecord.copyFields(sourceRcd, rcd, RaceRecord.EVENT);
		// pane = new RaceRecord(rcd, true, RaceRecord.BASIC);
		// }
		// if (aa == baseEditRecord) {
		// Record rcd = getRecord();
		// pane = new RaceRecord(rcd, false, RaceRecord.BASIC);
		// }
		// if (aa == newFromTable) {
		// Record rcd = getRecordModel();
		// RaceRecord.copyFields(sourceRcd, rcd, RaceRecord.EVENT);
		// recordFromTable = new RaceRecordFromTable(rcd, true);
		// pane = recordFromTable;
		// }
		return pane;
	}

	@Override
	public void init() {
		// setMessage("flicka.msg01");
		LazyList<Races> list = Races.findAll().orderBy("redate");
		setServiceRequest(list);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// Object src = evt.getSource();
		// Object newv = evt.getNewValue();
		// if (src instanceof DBExplorer) {
		// this.sourceRcd = (Record) newv;
		// if (sourceRcd != null) {
		// Date d = (Date) sourceRcd.getFieldValue("redate");
		// int r = (int) sourceRcd.getFieldValue("rerace");
		// String wc = "redate = '" + d + "' AND rerace = " + r;
		// request.setData(wc);
		// setServiceRequest(request);
		// } else {
		// setMessage("flicka.msg01");
		// // setVisibleToolBar(true);
		// }
		// }
	}

	@Override
	public boolean executeAction(TActionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}
}
