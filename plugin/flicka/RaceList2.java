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
import plugins.hero.*;

import java.beans.*;
import java.util.*;

import javax.swing.*;

import com.alee.laf.table.*;

import action.*;
import core.*;
import core.datasource.*;
import core.datasource.model.*;

public class RaceList2 extends TUIPanel {

	private TAbstractAction newFromTable, baseNewRecord, baseEditRecord;
	private RaceRecordFromTable recordFromTable;
private WebTable table;
private TAbstractTableModel tableModel;
	
	
	public RaceList2() {		
		showAditionalInformation(false);
		// actionMap = Alesia.getInstance().getContext().getActionMap(this);
		setToolBar(TActionsFactory.getActions("newRecord", "EditRecord"));
		Action a = TActionsFactory.getAction("newRecord");
		a.putValue(TActionsFactory.TUIPANEL, this);
		
		request.setParameter(ServiceRequest.ORDER_BY, "reend_pos");
		this.baseNewRecord = new NewRecord2(this);
		this.baseEditRecord = new EditRecord2(this);
		this.newFromTable = new NewRecord2(this) {
			@Override
			public boolean validateNewRecord(Record rcd) {
				return true;
			}
			@Override
			public void actionPerformed2() {
				recordFromTable.updateRecords();
				dialog.dispose();
				editableList.freshen();
			}
		};
		newFromTable.setIcon("newFromTable");
		setToolBar(baseNewRecord, baseEditRecord, newFromTable, new DeleteRecord2(this));
//		setColumns("restar_lane;rehorse;rejockey;reend_pos;rejockey_weight;recps");
//		setIconParameters("-1; ");
	}

	@Override
	public TUIFormPanel getTUIFormPanel(Action action) {
		TUIFormPanel tuifp = null;
		if (action.getValue(Action.NAME).equals("newRecord")) {
			tuifp = new RaceRecord(rcd, newr, mode)
		}
		// TODO Auto-generated method stub
		return super.getTUIFormPanel();
	}
	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;
		if (aa == baseNewRecord) {
			Record rcd = getRecordModel();
			RaceRecord.copyFields(sourceRcd, rcd, RaceRecord.EVENT);
			pane = new RaceRecord(rcd, true, RaceRecord.BASIC);
		}
		if (aa == baseEditRecord) {
			Record rcd = getRecord();
			pane = new RaceRecord(rcd, false, RaceRecord.BASIC);
		}
		if (aa == newFromTable) {
			Record rcd = getRecordModel();
			RaceRecord.copyFields(sourceRcd, rcd, RaceRecord.EVENT);
			recordFromTable = new RaceRecordFromTable(rcd, true);
			pane = recordFromTable;
		}
		return pane;
	}

	@Override
	public void init() {
		setMessage("flicka.msg01");
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object src = evt.getSource();
		Object newv = evt.getNewValue();
		if (src instanceof DBExplorer) {
			this.sourceRcd = (Record) newv;
			if (sourceRcd != null) {
				Date d = (Date) sourceRcd.getFieldValue("redate");
				int r = (int) sourceRcd.getFieldValue("rerace");
				String wc = "redate = '" + d + "' AND rerace = " + r;
				request.setData(wc);
				setServiceRequest(request);
			} else {
				setMessage("flicka.msg01");
//				setVisibleToolBar(true);
			}
		}
	}
}
