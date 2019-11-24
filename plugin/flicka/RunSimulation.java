package plugin.flicka;

import java.awt.event.*;
import java.util.*;

import action.*;
import core.*;
import core.datasource.*;

public class RunSimulation extends TAbstractAction {

	private int race;
	private Date date;
	private String fieldPx;
	private String selector;

	public RunSimulation(EditableList el, String sel) {
		super(RECORD_SCOPE);
		this.editableList = el;
		this.selector = sel;
		setIcon(selector.equals("bySpeed") ? "SimulationBySpeed" : "SimulationByPosition");
		// prefix accorindg to origen class
		fieldPx = (editableList instanceof DBExplorer) ? "re" : "st";
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Record rcd = editableList.getRecord();
		race = (Integer) rcd.getFieldValue(fieldPx + "race");
		date = (Date) rcd.getFieldValue(fieldPx + "date");
		if (selector.equals("bySpeed")) {
			SelectorBySpeed.runSimulation(race, date);
		} else {
			Selector.runSimulation(race, date);
		}
	}
}
