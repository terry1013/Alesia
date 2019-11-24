package plugin.dbtweezer;

import gui.docking.*;

import java.awt.event.*;
import java.util.concurrent.*;

import action.*;
import core.*;
import core.datasource.*;
import core.tasks.*;

public class TestScript extends TAbstractAction implements TaskListener {

	private DBTweezerTask tweezerTask;

	public TestScript(EditableList el) {
		super(RECORD_SCOPE);
		editableList = el;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Record srcd = editableList.getRecord();
		tweezerTask = new DBTweezerTask();
		tweezerTask.getTaskParameters().put(DBTweezerTask.TEST_RECORD, srcd);
		tweezerTask.getTaskParameters().put(DBTweezerTask.CONNECTION_PROFILE,
				DBTConnectionManager.getSourceDB().getConectionRecord().getFieldValue("t_cnname"));
		TTaskManager.submitRunnable(tweezerTask, this, false);
	}

	@Override
	public void taskDone(Future f) {
		ServiceRequest sr = (ServiceRequest) tweezerTask.getTaskParameters().get(DBTweezerTask.TEST_DATA);
		DockingContainer.fireProperty(this, DBTweezerTask.TEST_DATA, sr);
	}
}
