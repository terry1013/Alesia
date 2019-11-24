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
package action;

import java.awt.event.*;
import java.util.concurrent.*;

/**
 * Action for cancel current operation
 * 
 */
public class CancelOperation extends TAbstractAction {

	private Future future;
	public CancelOperation(Future f) {
		super(NO_SCOPE);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		future.cancel(true);
	}
}
