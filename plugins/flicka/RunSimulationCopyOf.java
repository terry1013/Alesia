package plugins.flicka;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import action.*;
import core.*;
import core.datasource.*;

public class RunSimulationCopyOf extends ConfirmAction {

	private int race, horseSample, jockeySample, uBound;
	private Date date;
	private String fieldPx;

	public RunSimulationCopyOf(EditableList el) {
		super(RECORD_SCOPE);
		this.editableList = el;
		setIcon("RunSimulation");
		// prefix accorindg to origen class
		fieldPx = (editableList instanceof DBExplorer) ? "re" : "st";
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Record rcd = editableList.getRecord();
		race = (Integer) rcd.getFieldValue(fieldPx + "race");
		date = (Date) rcd.getFieldValue(fieldPx + "date");
		String parms;
		if (fieldPx.equals("st")) {
			parms = rcd.getFieldValue("stsignature").toString();
		} else {
			parms = (String) TPreferences.getPreference("PrepareSimulation", "SimParms", "");
		}
		parms = JOptionPane.showInputDialog(Alesia.mainFrame, "Race: " + race + "\nDate: " + date
				+ "\n\nInput the Horse sample, Jockey sample & uper Bound:", parms);
		if (parms != null) {
			try {
				horseSample = Integer.parseInt(parms.substring(0, 1));
				jockeySample = Integer.parseInt(parms.substring(1, 2));
				uBound = Integer.parseInt(parms.substring(2, 3));
				TPreferences.setPreference("PrepareSimulation", "SimParms", parms);
				actionPerformed2();
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(Alesia.mainFrame, "Error in input parameters", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	@Override
	public void actionPerformed2() {
		Selector.runSimulation(race, date);
//		Selector.runSimulation(race, date, horseSample, jockeySample, uBound);
	}
}
