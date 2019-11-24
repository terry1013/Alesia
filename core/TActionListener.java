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
package core;

import java.awt.event.*;

import javax.swing.*;

public interface TActionListener extends ActionListener {

	/**
	 * previous
	 * 
	 * @param aa
	 * @return
	 */
	public Object previousActionPerfored(AbstractAction aa);

	/**
	 * after
	 * 
	 * @param aa
	 */
	public void postActionPerfored(AbstractAction aa);
}
