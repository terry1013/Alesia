package plugin.dbtweezer;

import action.*;
import core.*;
import core.tasks.*;

public class RunScriptList extends DeleteRecord2 {

	private DBTweezerTask tweezerTask;

	public RunScriptList(EditableList el) {
		super();
		setScope(TABLE_SCOPE);
		editableList = el;
		setDefaultValues("RunScriptList");
		setMessagePrefix("action.RunScriptList.");
		setEnabled(true);
	}

	@Override
	public void actionPerformed2() {
		tweezerTask = new DBTweezerTask();
		tweezerTask.getTaskParameters().put(DBTweezerTask.CONNECTION_PROFILE,
				DBTConnectionManager.getSourceDB().getConectionRecord().getFieldValue("t_cnname"));
		TTaskManager.submitRunnable(tweezerTask, null, true);
	}
}
