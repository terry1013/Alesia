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

public interface TCallable<V> extends Callable {

	/**
	 * Set the {@link Future} for this tasks. this method is invoked by
	 * {@link TTaskManager#submitCallable(TCallable, TaskListener, boolean)} after sucsefully task sumbition. <p>this
	 * method is intended for show visual component like TProgressMonitor with actions for cancel this future task (if
	 * argumento is not null).
	 * 
	 * @param f - Future represetation of this Callable.
	 * @param ab - <code>true</code> if allow in background mode is active
	 */
	public void setFuture(Future<V> f, boolean ab);
	
	public Hashtable getTaskParameters();
	
	public void setTaskParameters(Hashtable parms);
}
