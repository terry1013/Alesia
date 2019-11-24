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

import gui.*;

import javax.swing.*;

import core.datasource.*;

/**
 * test implementation for futher migration of all actions framework
 * 
 */
public interface EditableList {

	public UIComponentPanel getUIFor(AbstractAction aa);
	public Record getRecord();
	public Record[] getRecords();
	public void freshen();

}
