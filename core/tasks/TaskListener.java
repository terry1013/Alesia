package core.tasks;

import java.util.concurrent.*;

public interface TaskListener {
	
	public void taskDone(Future f);

}
