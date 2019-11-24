package plugin.flicka;

import java.awt.event.*;

import action.*;
import core.*;

public class CountEndPositions extends ConfirmAction {

	public CountEndPositions(EditableList el) {
		super(RECORD_SCOPE);
		this.editableList = el;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Selector.checkEndPositionCPS(editableList.getRecords());
	}
}
