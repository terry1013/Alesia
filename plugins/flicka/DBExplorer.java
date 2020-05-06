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
		setToolBar(TActionsFactory.getActions("newModel", "editModel", "deleteModel"));
		addToolBarAction(TActionsFactory.getAction(this, "runSimulation"));
		setColumns("redate;rerace;redistance;reracetime;reserie;repartial1;repartial2;repartial3;repartial4");
		setIconParameters("0;gender-;rehorsegender");
	}

	@org.jdesktop.application.Action
	public void runSimulation(ActionEvent event) {
		AbstractButton src = (AbstractButton) event.getSource();
		ApplicationAction me = (ApplicationAction) src.getAction();
		TUIListPanel tuilp = (TUIListPanel) me.getValue(TActionsFactory.TUILISTPANEL);
		Model[] models = tuilp.getModels();

		String parms = (String) TPreferences.getPreference("RunMultiSimulation", "SimParms", "");
		parms = JOptionPane.showInputDialog(Alesia.mainFrame,
				"Selected records: " + models.length + "\n\nEnter the uper value for horseSample, JockeySample", parms);
		if (parms != null) {
			try {
				int horseSample = Integer.parseInt(parms.substring(0, 1));
				int jockeySample = Integer.parseInt(parms.substring(1, 2));
				// Selector.runSimulation(models, horseSample);
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(Alesia.mainFrame, "Error in input parameters", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	@Override
	public TUIFormPanel getTUIFormPanel(ApplicationAction action) {
		// if (action.getName().equals("newModel")) {
		Races r = Races.create("retrack", "lr");
		// }
		return new RaceRecord(this, r, true, RaceRecord.EVENT);
	}

	@Override
	public void init() {
		// setMessage("flicka.msg01");
		Function<String, List<Model>> f = (par -> filterReslr());
		setDBParameters(f, Races.getMetaModel().getColumnMetadata());
		getWebTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}

	private List<Model> filterReslr() {
		List<Races> reslr = Races.find("retrack = ?", "lr").orderBy("redate DESC");
		List<Model> reslrr = new ArrayList<Model>();

		int race = 0;
		Date prevDate = null;
		Date date = null;
		for (Races races : reslr) {
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
