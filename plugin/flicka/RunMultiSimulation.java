package plugin.flicka;

import java.awt.event.*;

import javax.swing.*;

import action.*;
import core.*;
import core.datasource.*;

public class RunMultiSimulation extends ConfirmAction {

	private int horseSample, jockeySample;
	private Record[] records;;
	private String selector;

	public RunMultiSimulation(EditableList el, String sel) {
		super(RECORD_SCOPE);
		this.editableList = el;
		this.selector = sel;
		setIcon(selector.equals("bySpeed") ? "MultiSimulationBySpeed" : "MultiSimulationByPosition");
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		records = editableList.getRecords();
		String parms = (String) TPreferences.getPreference("RunMultiSimulation", "SimParms", "");
		parms = JOptionPane.showInputDialog(Alesia.mainFrame, "Selected records: " + records.length
				+ "\n\nEnter the uper value for horseSample, JockeySample & upBounds", parms);
		if (parms != null) {
			try {
				horseSample = Integer.parseInt(parms.substring(0, 1));
				jockeySample = Integer.parseInt(parms.substring(1, 2));
				TPreferences.setPreference("RunMultiSimulation", "SimParms", parms);
				actionPerformed2();
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(Alesia.mainFrame, "Error in input parameters", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	@Override
	public void actionPerformed2() {
		if (selector.equals("bySpeed")) {
//			SelectorBySpeed.runSimulation(records, horseSample, jockeySample);
		} else {
			Selector.runSimulation(records, horseSample);
		}
	}
}
