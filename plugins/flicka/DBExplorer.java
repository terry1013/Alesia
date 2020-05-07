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

import java.util.*;
import java.util.function.*;

import javax.swing.*;

import org.javalite.activejdbc.*;
import org.jdesktop.application.*;

import core.*;
import core.datasource.model.*;
import gui.*;

public class DBExplorer extends TUIListPanel {

	public DBExplorer() {
		showAditionalInformation(false);
		setToolBar(TActionsFactory.getActions("newModel", "editModel", "deleteModel", "runSimulation"));
		setColumns("redate;rerace;redistance;reracetime;reserie;repartial1;repartial2;repartial3;repartial4");
		setIconParameters("0;gender-;rehorsegender");
	}


	@Override
	public TUIFormPanel getTUIFormPanel(ApplicationAction action) {
		// if (action.getName().equals("newModel")) {
		Race r = Race.create("retrack", "lr");
		// }
		return new RaceRecord(this, r, true, RaceRecord.EVENT);
	}

	@Override
	public void init() {
		// setMessage("flicka.msg01");
		Function<String, List<Model>> f = (par -> filterReslr());
		setDBParameters(f, Race.getMetaModel().getColumnMetadata());
		getWebTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}

	private List<Model> filterReslr() {
		List<Race> reslr = Race.find("retrack = ?", "lr").orderBy("redate DESC");
		List<Model> reslrr = new ArrayList<Model>();

		int race = 0;
		Date prevDate = null;
		Date date = null;
		for (Race races : reslr) {
			// retrive one race by date
			if (!(races.getDate("redate").equals(date) && races.getInteger("rerace").equals(race))) {
				date = races.getDate("redate");
				prevDate = (prevDate == null) ? date : prevDate; // init prevdate at first time
				race = races.getInteger("rerace");
				reslrr.add(races);
			}
		}
		return reslrr;
	}
}
