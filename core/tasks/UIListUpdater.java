package core.tasks;

import gui.*;

import java.util.concurrent.*;

import javax.swing.*;

import core.*;

/**
 * task for automatic update for {@link UIListPanel}. this class is created and submited during {@link TTaskManager}
 * initialization ans contain a queqe of elements to update. To avoid overload, this class use a
 * {@link ArrayBlockingQueue} where every element is picked from the top, refres and send back to the end of the line at
 * frequency setted in {@link TTaskManager}
 * 
 * @author terry
 * 
 */
public class UIListUpdater implements Runnable {

	private ArrayBlockingQueue<UIListPanel> queue;

	/**
	 * new intance
	 */
	public UIListUpdater() {
		queue = new ArrayBlockingQueue<UIListPanel>(10);
	}

	/**
	 * insert a new {@link UIListPanel} instance. 
	 * 
	 * @param uilp - element to add
	 */
	public void add(UIListPanel uilp) {
//		queue.add(uilp);
	}

	/**
	 * remove the given {@link UIListPanel} form refeshin queqe
	 * 
	 * @param uilp - elemento to remove
	 */
	public void remove(UIListPanel uilp) {
		SystemLog.info(uilp.getClass().getName() + " Removed from UIListUpdater");
		queue.remove(uilp);
	}

	@Override
	public void run() {
		try {
			UIListPanel uilp = queue.poll();
			if (uilp != null) {
				// if has no JRootPanel, detach from refreshing
				JRootPane jrp = uilp.getRootPane();
				if (jrp != null) {
					uilp.freshen();
					queue.add(uilp);
				} else {
					SystemLog.info(uilp.getClass().getName() + " detached from UIListUpdater");
				}
			}
		} catch (Exception e) {
			SystemLog.logException1(e);
		}
	}
}