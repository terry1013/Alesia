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

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import org.javalite.activejdbc.*;

import com.alee.laf.panel.*;
import com.alee.laf.splitpane.*;

import core.*;
import core.datasource.model.*;

public class Flicka extends TPlugin {

	protected static ActionMap actionMap; 

	public Flicka() {
		actionMap = Alesia.getInstance().getContext().getActionMap(this);
		Alesia.openDB("flicka");
	}
	
	@Override
	public ArrayList<javax.swing.Action> getUI(String type) {
		ArrayList<Action> alist = new ArrayList<>();
		alist.add(actionMap.get("races"));
		return alist;
	}

	@org.jdesktop.application.Action
	public void races(ActionEvent event) {
		DBExplorer dbe = new DBExplorer();		
		RaceList r = new RaceList();
		WebSplitPane sp = new WebSplitPane(WebSplitPane.VERTICAL_SPLIT);
		sp.add(dbe);
		sp.add(r);
		Alesia.getMainPanel().setContentPanel(new WebPanel(sp));
		dbe.init();
	}

	/**
	 * filter the reslr table returning one element according to the field argument. the valid field argument are
	 * rehorse or rejockey
	 * 
	 * @param field - field for filtering
	 * @return subset of reslr table with one element of the fileter argument
	 */
	public static TEntry[] getElemets(String field, String emptyF) {
		LazyList<Races> races = Races.findAll();
		Vector<String> tmpList = new Vector<String>();
		Vector<TEntry> reslrr = new Vector<TEntry>();
		if (emptyF != null) {
			reslrr.add(TStringUtils.getTEntry(emptyF));
		}
		for (Races race : races) {
			String ele = race.getString(field);
			if (!tmpList.contains(ele)) {
				tmpList.add(ele);
				reslrr.add(new TEntry(ele, ele));
			}
		}
		TEntry[] te = new TEntry[reslrr.size()];
		reslrr.copyInto(te);
		return te;
	}	

	/**
	 * return the reslr list filter by field parameter. e.g: if field = rehorse, the return list contain ony one element
	 * of reslr file ......
	 * 
	 * @param field - fiel to filter list
	 * 
	 * @return 
	public static ServiceRequest getFilterServiceReques(String field) {
		Vector<String> tmpList = new Vector<String>();
		Vector<Record> reslr = ConnectionManager.getAccessTo("reslr").search(null, "redate DESC");
		Vector<Record> reslrr = new Vector<Record>();
		for (Record rcd : reslr) {
			String ele = (String) rcd.getFieldValue(field);
			if (!tmpList.contains(ele)) {
				tmpList.add(ele);
				reslrr.add(rcd);
			}
		}
		ServiceRequest sr = new ServiceRequest(ServiceRequest.CLIENT_GENERATED_LIST, "reslr", reslrr);
		sr.setParameter(ServiceResponse.RECORD_MODEL, ConnectionManager.getAccessTo("reslr").getModel());
		return sr;
	}
	 */
}
