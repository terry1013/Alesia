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
package core.tasks;

import java.util.*;
import java.util.concurrent.*;

/**
 * guidelines TODO complete docs
 * <ol>
 * <li>when the {@link #run()} method start, the parameters are colected
 * <li>when the {@link #run()} method ends, the parameters are update (depend of task implementation)
 * </ol>
 * 
 * @author terry
 *
 */
public interface TRunnable extends Runnable {

	/**
	 * Set the {@link Future} for this tasks. this method is invoked by
	 * {@link TTaskManager#submitRunnable(TRunnable, TaskListener, boolean)} after sucsefully task sumbition.
	 * <p>
	 * this method is intended for show visual component like TProgressMonitor with actions for cancel this future task
	 * (if argumento is not null).
	 * 
	 * @param f - Future represetation of this Callable.
	 * @param ab - <code>true</code> if allow in background mode is active
	 */
	public void setFuture(Future f, boolean ab);

	/**
	 * return the task parameters. this parameters are the sum of the internal parameter plus the aditional list setted
	 * by {@link #setTaskParameters(Hashtable)}. Content may be changed by task execution and or external iteration.
	 * "Would like know more?" see the class doc
	 * 
	 * @return task parameters
	 */
	public Hashtable getTaskParameters();

	/**
	 * set the task parameters. This parameters are appended to this task internal parameters. Content may be changed by
	 * task execution and or external iteration. "Would like know more?" see the class doc
	 * 
	 * @param parms - the task parameters
	 * 
	 */
	public void setTaskParameters(Hashtable parms);
}
