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

import java.awt.event.*;
import java.beans.*;
import java.util.*;

import javax.swing.*;

import com.alee.laf.combobox.*;

import action.*;
import core.*;
import core.datasource.*;

public class DBExplorer extends UIListPanel implements DockingComponent, ActionListener {

	private WebComboBox track;

	public DBExplorer() {
		super(null);
		EditRecord2 editRecord2 = new EditRecord2() {
			@Override
			public void actionPerformed2() {
				// update all generals fields for all records in this date/race

				Record srcd = dataInput.getRecord();
				Date d = (Date) srcd.getFieldValue("redate");
				java.sql.Date d1 = new java.sql.Date(d.getTime());
				int r = (int) srcd.getFieldValue("rerace");
				DBAccess dba = ConnectionManager.getAccessTo(srcd.getTableName());
				Vector<Record> rlst = dba.search("redate = '" + d1 + "' AND rerace = " + r, null);
				for (Record rcd : rlst) {
					RaceRecord.copyFields(srcd, rcd, RaceRecord.EVENT);
					dba.update(rcd);
				}
				dialog.dispose();
				editableList.freshen();
			}
		};
		editRecord2.setEditableList(this);
		TEntry[] t = TStringUtils.getTEntryGroup("track_");
//		this.track = TUIUtils.getJComboBox("tttack", t, "lr");
		// track.s
		track = new WebComboBox(new TEntry[]{new TEntry("lr", "La rinconada"), new TEntry("val", "Valencia")});
		track.addActionListener(this);
//		track.setDrawBorder(false);
		track.setDrawFocus(false);

		getToolBar().add(track);
		setToolBar(new NewRecord2(this), editRecord2);
		addToolBarAction(new RunSimulation(this, "bySpeed"), new RunSimulation(this, "byPosition"),
				new RunMultiSimulation(this, "byPosition"), new CountEndPositions(this));
		setColumns("redate;rerace;redistance;reracetime;reserie;repartial1;repartial2;repartial3;repartial4");
		setIconParameters("0;gender-;rehorsegender");
		getJTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		Record rcd = getRecord();
		boolean newr = false;
		if (aa instanceof NewRecord2) {
			rcd = getRecordModel();
			rcd.setFieldValue("retrack", ((TEntry) track.getSelectedItem()).getKey());
			newr = true;
		}
		return new RaceRecord(rcd, newr, RaceRecord.EVENT);
	}

	@Override
	public void freshen() {
		getTableModel().setServiceRequest(filterReslr());
		super.freshen();
	}

	@Override
	public void init() {
		setServiceRequest(filterReslr());
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

	}

	private ServiceRequest filterReslr() {
		String tk = (String) ((TEntry) track.getSelectedItem()).getKey();
		Vector<Record> reslr = ConnectionManager.getAccessTo("reslr").search("retrack = '" + tk + "'", "redate DESC");
		Vector<Record> reslrr = new Vector<Record>();
		int race = 0;
		Date prevDate = null;
		Date date = null;
		for (Record rcd : reslr) {
			// retrive one race by date
			if (!(rcd.getFieldValue("redate").equals(date) && rcd.getFieldValue("rerace").equals(race))) {
				date = (Date) rcd.getFieldValue("redate");
				prevDate = (prevDate == null) ? date : prevDate; // init prevdate at first time
				race = (Integer) rcd.getFieldValue("rerace");
				reslrr.add(rcd);
			}
		}
		Record mod = ConnectionManager.getAccessTo("reslr").getModel();
		ServiceRequest sr = new ServiceRequest(ServiceRequest.CLIENT_GENERATED_LIST, "reslr", reslrr);
		sr.setParameter(ServiceResponse.RECORD_MODEL, mod);
		return sr;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		freshen();
	}
}
